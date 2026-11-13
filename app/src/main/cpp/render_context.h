#pragma once
#include <vector>
#include <cstdint>
#include <memory>
#include "include/core/SkCanvas.h"
#include "include/core/SkBitmap.h"
#include "include/core/SkColor.h"
#include "include/core/SkBlendMode.h"
#include "colormap.h"

struct Point2f { float x, y; };

/**
 * Per-pixel hit counter for very dense point sets (strange attractors, flow fields): add
 * millions of points cheaply, then render once with RenderContext::drawDensity. Create it with
 * RenderContext::makeDensity() *after* setWorldBounds(); it keeps the mapping it was created with.
 */
class DensityAccumulator {
public:
    void add(Point2f world) {
        int px = (int)((world.x - wx_min_) * sx_);
        int py = (int)((wy_max_ - world.y) * sy_);
        if (px < 0 || py < 0 || px >= width_ || py >= height_) return;
        uint32_t c = ++counts_[(size_t)py * width_ + px];
        if (c > max_count_) max_count_ = c;
    }
    uint32_t maxCount() const { return max_count_; }

private:
    friend class RenderContext;
    DensityAccumulator(int w, int h, float x_min, float x_max, float y_min, float y_max)
        : width_(w), height_(h), wx_min_(x_min), wy_max_(y_max),
          sx_((float)w / (x_max - x_min)), sy_((float)h / (y_max - y_min)),
          counts_((size_t)w * h, 0) {}
    int width_, height_;
    float wx_min_, wy_max_, sx_, sy_;
    std::vector<uint32_t> counts_;
    uint32_t max_count_ = 0;
};

class RenderContext {
public:
    RenderContext(int width, int height, SkColor bg_color = SK_ColorBLACK);
    void setColormap(const std::vector<ColorStop>& stops) { colormap_ = stops; }
    SkColor getColor(float t) const { return sampleColormap(colormap_, t); }

    void drawPolyline(const std::vector<Point2f>& pts, SkColor color, float stroke_width = 1.1f, float alpha = 1.0f);
    void drawScatter(const std::vector<Point2f>& pts, SkColor color, float radius = 3.0f, float alpha = 1.0f);
    void fillPolygon(const std::vector<Point2f>& pts, SkColor color, float alpha = 1.0f);
    /** Circle with a radius in world units. stroke_width <= 0 fills it; otherwise an outline. */
    void drawCircle(Point2f center, float radius, SkColor color, float stroke_width = 0.0f, float alpha = 1.0f);
    void setWorldBounds(float x_min, float x_max, float y_min, float y_max);

    /** Blend mode for all following draw calls. SkBlendMode::kPlus gives additive glow. */
    void setBlendMode(SkBlendMode mode) { blend_mode_ = mode; }

    /** Counter matching the current world bounds; see DensityAccumulator. */
    DensityAccumulator makeDensity() const {
        return DensityAccumulator(width_, height_, wx_min_, wx_max_, wy_min_, wy_max_);
    }
    /**
     * Draws the accumulated density through the colormap: t = (log(1+n) / log(1+max))^gamma.
     * Pixels never hit stay untouched (background shows); lower gamma brings out faint areas.
     */
    void drawDensity(const DensityAccumulator& density, float gamma = 0.5f, float alpha = 1.0f);

    /**
     * Paints a per-pixel scalar field through the colormap, replacing the whole image.
     * `values` is row-major, width() * height(), each in [-1, 1] (mapped to t = (v + 1) / 2).
     */
    void drawValues(const std::vector<float>& values);
    int width() const { return width_; }
    int height() const { return height_; }

    std::vector<uint8_t> encodeJpeg(int quality = 92) const;

private:
    SkBitmap bitmap_;
    std::unique_ptr<SkCanvas> canvas_;
    int width_, height_;
    std::vector<ColorStop> colormap_;
    float wx_min_ = -1, wx_max_ = 1, wy_min_ = -1, wy_max_ = 1;
    SkBlendMode blend_mode_ = SkBlendMode::kSrcOver;
    Point2f worldToPixel(Point2f p) const;
};
