#pragma once
#include <vector>
#include <cstdint>
#include <memory>
#include "include/core/SkCanvas.h"
#include "include/core/SkBitmap.h"
#include "include/core/SkColor.h"
#include "colormap.h"

struct Point2f { float x, y; };

class RenderContext {
public:
    RenderContext(int width, int height, SkColor bg_color = SK_ColorBLACK);
    void setColormap(const std::vector<ColorStop>& stops) { colormap_ = stops; }
    SkColor getColor(float t) const { return sampleColormap(colormap_, t); }

    void drawPolyline(const std::vector<Point2f>& pts, SkColor color, float stroke_width = 1.1f, float alpha = 1.0f);
    void drawScatter(const std::vector<Point2f>& pts, SkColor color, float radius = 3.0f, float alpha = 1.0f);
    void fillPolygon(const std::vector<Point2f>& pts, SkColor color, float alpha = 1.0f);
    void setWorldBounds(float x_min, float x_max, float y_min, float y_max);
    std::vector<uint8_t> encodeJpeg(int quality = 92) const;

private:
    SkBitmap bitmap_;
    std::unique_ptr<SkCanvas> canvas_;
    int width_, height_;
    std::vector<ColorStop> colormap_;
    float wx_min_ = -1, wx_max_ = 1, wy_min_ = -1, wy_max_ = 1;
    Point2f worldToPixel(Point2f p) const;
};
