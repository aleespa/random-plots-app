#include "render_context.h"
#include "include/core/SkPaint.h"
#include "include/core/SkPath.h"
#include "include/encode/SkJpegEncoder.h"
#include "include/core/SkStream.h"
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
    float px = (p.x - wx_min_) / (wx_max_ - wx_min_) * width_;
    float py = (1.0f - (p.y - wy_min_) / (wy_max_ - wy_min_)) * height_;
    return {px, py};
}

void RenderContext::drawPolyline(const std::vector<Point2f>& pts, SkColor color, float stroke_width, float alpha) {
    if (pts.size() < 2) return;
    SkPaint paint;
    paint.setStyle(SkPaint::kStroke_Style);
    paint.setColor(color);
    paint.setAlphaf(alpha);
    paint.setStrokeWidth(stroke_width);
    paint.setAntiAlias(true);
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
    if (pts.empty()) return;
    SkPaint paint;
    paint.setStyle(SkPaint::kFill_Style);
    paint.setColor(color);
    paint.setAlphaf(alpha);
    paint.setAntiAlias(true);
    for (const auto& pt : pts) {
        auto p = worldToPixel(pt);
        canvas_->drawCircle(p.x, p.y, radius, paint);
    }
}

void RenderContext::fillPolygon(const std::vector<Point2f>& pts, SkColor color, float alpha) {
    if (pts.size() < 3) return;
    SkPaint paint;
    paint.setStyle(SkPaint::kFill_Style);
    paint.setColor(color);
    paint.setAlphaf(alpha);
    paint.setAntiAlias(true);
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

std::vector<uint8_t> RenderContext::encodeJpeg(int quality) const {
    SkDynamicMemoryWStream stream;
    SkJpegEncoder::Options opts;
    opts.fQuality = quality;
    SkJpegEncoder::Encode(&stream, bitmap_.pixmap(), opts);
    sk_sp<SkData> data = stream.detachAsData();
    return std::vector<uint8_t>(data->bytes(), data->bytes() + data->size());
}
