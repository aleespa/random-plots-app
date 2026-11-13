// Domain Colouring: a random rational function f(z) = prod(z - root) / prod(z - pole), shown by
// colouring each point by the phase of f (hue) and log2|f| (brightness bands), optionally with
// phase contour lines. Fills the plane and pans/zooms through ctx.view() like Prism.
#include "sketch_base.h"
#include "sketch_util.h"
#include <cmath>
#include <random>
#include <vector>

class DomainColoringSketch : public SketchBase {
    static SkColor hsv(float h, float s, float v) {
        float hh = (h - std::floor(h)) * 6;
        int i = (int)hh;
        float f = hh - i, p = v * (1 - s), q = v * (1 - f * s), t = v * (1 - (1 - f) * s);
        float r, g, b;
        switch (i % 6) {
            case 0: r = v; g = t; b = p; break;
            case 1: r = q; g = v; b = p; break;
            case 2: r = p; g = v; b = t; break;
            case 3: r = p; g = q; b = v; break;
            case 4: r = t; g = p; b = v; break;
            default: r = v; g = p; b = q; break;
        }
        return SkColorSetARGB(0xFF, (U8CPU)(r * 255), (U8CPU)(g * 255), (U8CPU)(b * 255));
    }

public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        auto U = [&](float a, float b) { return std::uniform_real_distribution<float>(a, b)(rng); };
        auto I = [&](int a, int b) { return std::uniform_int_distribution<int>(a, b)(rng); };

        std::vector<Point2f> roots(I(2, 4)), poles(I(1, 3));
        for (auto& r : roots) r = {U(-1.4f, 1.4f), U(-1.4f, 1.4f)};
        for (auto& p : poles) p = {U(-1.4f, 1.4f), U(-1.4f, 1.4f)};
        static const int kLines[] = {0, 6, 12};
        const int lines = kLines[I(0, 2)];
        constexpr float kExtent = 2.0f;  // the default view shows [-2, 2]^2

        const int W = ctx.width(), H = ctx.height();
        const PlaneView view = ctx.view();
        std::vector<uint32_t> pixels((size_t)W * H);
        sketchutil::parallelRows(H, [&](int r0, int r1) {
            for (int py = r0; py < r1; ++py) {
                float y = kExtent * (float)(view.cy + view.zoom * (1 - 2 * (py + 0.5) / H));
                for (int px = 0; px < W; ++px) {
                    float x = kExtent * (float)(view.cx + view.zoom * (-1 + 2 * (px + 0.5) / W));
                    float nr = 1, ni = 0, dr = 1, di = 0;
                    for (const auto& r : roots) {
                        float zr = x - r.x, zi = y - r.y, t = nr * zr - ni * zi;
                        ni = nr * zi + ni * zr; nr = t;
                    }
                    for (const auto& p : poles) {
                        float zr = x - p.x, zi = y - p.y, t = dr * zr - di * zi;
                        di = dr * zi + di * zr; dr = t;
                    }
                    float den = dr * dr + di * di + 1e-20f;
                    float fr = (nr * dr + ni * di) / den, fi = (ni * dr - nr * di) / den;
                    float mag = std::hypot(fr, fi), arg = std::atan2(fi, fr);
                    float band = std::log2(mag + 1e-20f);
                    float v = 0.62f + 0.38f * (band - std::floor(band));
                    if (lines) {
                        float ph = (arg / (2 * (float)M_PI) + 1) * lines;
                        if (ph - std::floor(ph) < 0.05f) v *= 0.55f;
                    }
                    pixels[(size_t)py * W + px] = hsv(arg / (2 * (float)M_PI) + 1, 0.85f, std::isfinite(v) ? v : 0);
                }
            }
        });
        ctx.drawPixels(pixels);
    }
};
