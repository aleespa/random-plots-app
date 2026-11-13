#include "render_context.h"
#include "include/core/SkPaint.h"
#include "include/core/SkPath.h"
#include "include/encode/SkJpegEncoder.h"
#include "include/core/SkStream.h"
#include "include/core/SkImage.h"
#include <algorithm>
#include <cmath>

RenderContext::RenderContext(int width, int height, SkColor bg_color)
    : width_(width), height_(height) {
    bitmap_.allocN32Pixels(width, height);
    canvas_ = std::make_unique<SkCanvas>(bitmap_);
    canvas_->clear(bg_color);
}

void RenderContext::setWorldBounds(float x_min, float x_max, float y_min, float y_max) {
    wx_min_ = x_min; wx_max_ = x_max;
    wy_min_ = y_min; wy_max_ = y_max;
}

Point2f RenderContext::worldToPixel(Point2f p) const {
    float dx = wx_max_ - wx_min_;
    float dy = wy_max_ - wy_min_;
    if (std::abs(dx) < 1e-6f || std::abs(dy) < 1e-6f) return {0, 0};

    float px = (p.x - wx_min_) / dx * (float)width_;
    float py = (1.0f - (p.y - wy_min_) / dy) * (float)height_;
    return {px, py};
}

void RenderContext::drawPolyline(const std::vector<Point2f>& pts, SkColor color, float stroke_width, float alpha) {
    markVectorDraw();
    if (pts.size() < 2) return;
    float scale = (float)width_ / 1000.0f;
    SkPaint paint;
    paint.setStyle(SkPaint::kStroke_Style);
    paint.setColor(color);
    paint.setAlphaf(alpha);
    paint.setStrokeWidth(stroke_width * scale);
    paint.setAntiAlias(true);
    paint.setBlendMode(blend_mode_);
    SkPath path;
    auto p0 = worldToPixel(pts[0]);
    path.moveTo(p0.x, p0.y);
    for (size_t i = 1; i < pts.size(); ++i) {
        auto p = worldToPixel(pts[i]);
        path.lineTo(p.x, p.y);
    }
    canvas_->drawPath(path, paint);
}

void RenderContext::drawScatter(const std::vector<Point2f>& pts, SkColor color, float radius, float alpha) {
    markVectorDraw();
    if (pts.empty()) return;
    float scale = (float)width_ / 1000.0f;
    SkPaint paint;
    paint.setStyle(SkPaint::kFill_Style);
    paint.setColor(color);
    paint.setAlphaf(alpha);
    paint.setAntiAlias(true);
    paint.setBlendMode(blend_mode_);
    for (const auto& pt : pts) {
        auto p = worldToPixel(pt);
        canvas_->drawCircle(p.x, p.y, radius * scale, paint);
    }
}

void RenderContext::fillPolygon(const std::vector<Point2f>& pts, SkColor color, float alpha) {
    markVectorDraw();
    if (pts.size() < 3) return;
    SkPaint paint;
    paint.setStyle(SkPaint::kFill_Style);
    paint.setColor(color);
    paint.setAlphaf(alpha);
    paint.setAntiAlias(true);
    paint.setBlendMode(blend_mode_);
    SkPath path;
    auto p0 = worldToPixel(pts[0]);
    path.moveTo(p0.x, p0.y);
    for (size_t i = 1; i < pts.size(); ++i) {
        auto p = worldToPixel(pts[i]);
        path.lineTo(p.x, p.y);
    }
    path.close();
    canvas_->drawPath(path, paint);
}

void RenderContext::drawCircle(Point2f center, float radius, SkColor color, float stroke_width, float alpha) {
    markVectorDraw();
    float px_per_unit = (float)width_ / (wx_max_ - wx_min_);
    float scale = (float)width_ / 1000.0f;
    SkPaint paint;
    paint.setStyle(stroke_width > 0 ? SkPaint::kStroke_Style : SkPaint::kFill_Style);
    if (stroke_width > 0) paint.setStrokeWidth(stroke_width * scale);
    paint.setColor(color);
    paint.setAlphaf(alpha);
    paint.setAntiAlias(true);
    paint.setBlendMode(blend_mode_);
    auto c = worldToPixel(center);
    canvas_->drawCircle(c.x, c.y, radius * px_per_unit, paint);
}

uint32_t RenderContext::packField(float t, float shade, float alpha) {
    auto q = [](float v, float max) { return (uint32_t)std::lround(std::clamp(v, 0.0f, 1.0f) * max); };
    return (q(t, 65535.0f) << 16) | (q(shade, 255.0f) << 8) | q(alpha, 255.0f);
}

void RenderContext::drawField(const std::vector<FieldPixel>& pixels) {
    if (pixels.size() != (size_t)width_ * height_) return;
    if (field_mode_) {
        for (size_t i = 0; i < pixels.size(); ++i) {
            if (pixels[i].alpha > 0) field_[i] = packField(pixels[i].t, pixels[i].shade, pixels[i].alpha);
        }
        return;
    }
    for (int y = 0; y < height_; ++y) {
        uint32_t* row = bitmap_.getAddr32(0, y);
        for (int x = 0; x < width_; ++x) {
            const FieldPixel& p = pixels[(size_t)y * width_ + x];
            if (p.alpha <= 0) continue;
            SkColor c = getColor(p.t);
            SkPMColor dst = row[x];
            float a = std::clamp(p.alpha, 0.0f, 1.0f), s = std::clamp(p.shade, 0.0f, 1.0f);
            auto mix = [&](U8CPU src, U8CPU d) { return (U8CPU)std::lround(src * s * a + d * (1 - a)); };
            row[x] = SkPreMultiplyARGB(0xFF, mix(SkColorGetR(c), ((dst >> SK_R32_SHIFT) & 0xFF)),
                                       mix(SkColorGetG(c), ((dst >> SK_G32_SHIFT) & 0xFF)),
                                       mix(SkColorGetB(c), ((dst >> SK_B32_SHIFT) & 0xFF)));
        }
    }
}

void RenderContext::drawDensity(const DensityAccumulator& density, float gamma, float alpha) {
    if (density.maxCount() == 0) return;
    if (field_mode_) {
        const float inv = 1.0f / std::log1p((float)density.maxCount());
        for (size_t i = 0; i < field_.size(); ++i) {
            uint32_t n = density.counts_[i];
            if (n == 0) continue;
            float t = std::pow(std::log1p((float)n) * inv, gamma);
            float hue = density.t_sum_.empty() ? t : density.t_sum_[i] / (float)n;
            field_[i] = packField(hue, 1.0f, t * alpha);
        }
        return;
    }
    // Tone-map counts into an overlay: color from the colormap, coverage from the same t, so
    // sparse areas fade into the background instead of being drawn as solid low-end color.
    SkBitmap overlay;
    overlay.allocN32Pixels(width_, height_);
    overlay.eraseColor(SK_ColorTRANSPARENT);
    const float inv_log_max = 1.0f / std::log1p((float)density.maxCount());
    for (int y = 0; y < height_; ++y) {
        uint32_t* row = overlay.getAddr32(0, y);
        const uint32_t* counts = density.counts_.data() + (size_t)y * width_;
        for (int x = 0; x < width_; ++x) {
            uint32_t n = counts[x];
            if (n == 0) continue;
            float t = std::pow(std::log1p((float)n) * inv_log_max, gamma);
            // With colour coordinates, hue comes from their mean and t only sets the intensity.
            SkColor c = density.t_sum_.empty()
                ? getColor(t)
                : getColor(density.t_sum_[(size_t)y * width_ + x] / (float)n);
            U8CPU a = (U8CPU)std::lround(std::clamp(t * alpha, 0.0f, 1.0f) * 255.0f);
            row[x] = SkPreMultiplyARGB(a, SkColorGetR(c), SkColorGetG(c), SkColorGetB(c));
        }
    }
    SkPaint paint;
    paint.setBlendMode(blend_mode_);
    canvas_->drawImage(overlay.asImage(), 0, 0, SkSamplingOptions(), &paint);
}

void RenderContext::drawValues(const std::vector<float>& values) {
    if (values.size() != (size_t)width_ * height_) return;
    if (field_mode_) {
        for (size_t i = 0; i < values.size(); ++i) field_[i] = packField((values[i] + 1.0f) * 0.5f, 1.0f, 1.0f);
        return;
    }
    for (int y = 0; y < height_; ++y) {
        uint32_t* row = bitmap_.getAddr32(0, y);
        const float* v = values.data() + (size_t)y * width_;
        for (int x = 0; x < width_; ++x) {
            float t = std::clamp((v[x] + 1.0f) * 0.5f, 0.0f, 1.0f);
            SkColor c = getColor(t);
            row[x] = SkPreMultiplyARGB(0xFF, SkColorGetR(c), SkColorGetG(c), SkColorGetB(c));
        }
    }
}

void RenderContext::drawRGB(const std::vector<float>& r, const std::vector<float>& g,
                            const std::vector<float>& b) {
    markVectorDraw();
    size_t n = (size_t)width_ * height_;
    if (r.size() != n || g.size() != n || b.size() != n) return;
    auto byte = [](float v) {
        return (uint8_t)std::lround(std::clamp((v + 1.0f) * 0.5f, 0.0f, 1.0f) * 255.0f);
    };
    for (int y = 0; y < height_; ++y) {
        uint32_t* row = bitmap_.getAddr32(0, y);
        size_t o = (size_t)y * width_;
        for (int x = 0; x < width_; ++x) {
            row[x] = SkPreMultiplyARGB(0xFF, byte(r[o + x]), byte(g[o + x]), byte(b[o + x]));
        }
    }
}

void RenderContext::drawPixels(const std::vector<uint32_t>& argb) {
    markVectorDraw();
    if (argb.size() != (size_t)width_ * height_) return;
    for (int y = 0; y < height_; ++y) {
        uint32_t* row = bitmap_.getAddr32(0, y);
        const uint32_t* src = argb.data() + (size_t)y * width_;
        for (int x = 0; x < width_; ++x) {
            uint32_t c = src[x];
            if (SkColorGetA(c) == 0) continue;
            row[x] = SkPreMultiplyARGB(0xFF, SkColorGetR(c), SkColorGetG(c), SkColorGetB(c));
        }
    }
}

std::vector<uint8_t> RenderContext::encodeJpeg(int quality) const {
    SkDynamicMemoryWStream stream;
    SkJpegEncoder::Options opts;
    opts.fQuality = quality;
    SkJpegEncoder::Encode(&stream, bitmap_.pixmap(), opts);
    sk_sp<SkData> data = stream.detachAsData();
    return std::vector<uint8_t>(data->bytes(), data->bytes() + data->size());
}
