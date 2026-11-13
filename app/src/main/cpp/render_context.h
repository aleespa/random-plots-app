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
 * Pan/zoom window onto the plane, same convention as the fractal shader: a pixel at normalized
 * device coordinates (u, v) in [-1, 1] (v up) shows the point (cx + u * zoom, cy + v * zoom).
 * Only sketches that support pan/zoom (Figures.hasPanZoomView) read it; the default is identity.
 */
struct PlaneView {
    double cx = 0.0, cy = 0.0, zoom = 1.0;
};

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
    /**
     * Like add(), plus a colour coordinate t in [0, 1]: drawDensity then colours each pixel by
     * the mean t of its hits (e.g. which map of a fractal flame landed there) instead of by density.
     */
    void add(Point2f world, float t) {
        int px = (int)((world.x - wx_min_) * sx_);
        int py = (int)((wy_max_ - world.y) * sy_);
        if (px < 0 || py < 0 || px >= width_ || py >= height_) return;
        size_t i = (size_t)py * width_ + px;
        if (t_sum_.empty()) t_sum_.assign(counts_.size(), 0.0f);
        t_sum_[i] += t;
        uint32_t c = ++counts_[i];
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
    std::vector<float> t_sum_;  // only allocated when add(p, t) is used
    uint32_t max_count_ = 0;
};

/** One pixel of a colour field: palette position, brightness factor, coverage (all in [0, 1]). */
struct FieldPixel { float t = 0, shade = 1, alpha = 0; };

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
    /**
     * Writes ARGB colours (row-major, width() * height()) straight into the image. Pixels with
     * alpha 0 are skipped, so the background shows through them.
     */
    void drawPixels(const std::vector<uint32_t>& argb);
    /**
     * Per-pixel palette image: colour = colormap(t) * shade, blended over the background by alpha
     * (alpha 0 leaves the background). Row-major, width() * height().
     */
    void drawField(const std::vector<FieldPixel>& pixels);
    /** Like drawValues, but one field per channel (each in [-1, 1]); ignores the colormap. */
    void drawRGB(const std::vector<float>& r, const std::vector<float>& g, const std::vector<float>& b);
    int width() const { return width_; }
    int height() const { return height_; }

    void setView(const PlaneView& view) { view_ = view; }
    const PlaneView& view() const { return view_; }

    /** True when the user picked full RGB colour instead of a colormap (Colormaps.FULL_COLOR). */
    void setFullColor(bool full) { full_color_ = full; }
    bool fullColor() const { return full_color_; }

    /** The image as unpremultiplied RGBA bytes (row-major), the layout Android's ARGB_8888 uses. */
    std::vector<uint8_t> readRgba() const;

    /**
     * Colour-field mode (Figures.rendersColorField): drawValues, drawDensity and drawField record
     * a palette-independent field instead of colours, packed per pixel as t (16 bits) | shade (8) |
     * alpha (8), and Kotlin colours it (tools/ColorField.kt). A colormap or background change then
     * needs no re-render. Vector primitives can't be recoloured per pixel, so drawing one in this
     * mode makes the field invalid (fieldValid() is false and the caller renders normally).
     */
    void enableFieldMode() { field_mode_ = true; field_.assign((size_t)width_ * height_, 0u); }
    bool fieldValid() const { return field_mode_ && !field_invalid_; }
    const std::vector<uint32_t>& field() const { return field_; }
    static uint32_t packField(float t, float shade, float alpha);

private:
    SkBitmap bitmap_;
    std::unique_ptr<SkCanvas> canvas_;
    int width_, height_;
    std::vector<ColorStop> colormap_;
    float wx_min_ = -1, wx_max_ = 1, wy_min_ = -1, wy_max_ = 1;
    SkBlendMode blend_mode_ = SkBlendMode::kSrcOver;
    PlaneView view_;
    bool full_color_ = false;
    bool field_mode_ = false, field_invalid_ = false;
    std::vector<uint32_t> field_;
    void markVectorDraw() { if (field_mode_) field_invalid_ = true; }
    Point2f worldToPixel(Point2f p) const;
};
