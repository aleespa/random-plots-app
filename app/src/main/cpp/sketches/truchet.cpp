// Truchet Tiles: a grid of randomly rotated tiles (quarter arcs, double arcs or half-squares) whose
// pieces join into winding paths. Colour follows the distance to a random focus.
#include "sketch_base.h"
#include <cmath>
#include <random>
#include <vector>

class TruchetSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        auto U = [&](float a, float b) { return std::uniform_real_distribution<float>(a, b)(rng); };
        auto I = [&](int a, int b) { return std::uniform_int_distribution<int>(a, b)(rng); };
        ctx.setWorldBounds(0, 1, 0, 1);

        static const int kSizes[] = {6, 8, 10, 12, 16, 20};
        const int n = kSizes[I(0, 5)];
        const float s = 1.0f / n;
        const int style = I(0, 2);  // 0 arcs, 1 double arcs, 2 triangles
        const float width = 1000.0f * s * U(0.1f, 0.28f);  // stroke in 1000-px units
        const float ox = U(0.2f, 0.8f), oy = U(0.2f, 0.8f);

        auto arc = [&](float cx, float cy, float r, float a0, float color_t, float stroke) {
            std::vector<Point2f> pts;
            for (int k = 0; k <= 16; ++k) {
                float a = a0 + (float)M_PI / 2 * k / 16;
                pts.push_back({cx + r * std::cos(a), cy + r * std::sin(a)});
            }
            ctx.drawPolyline(pts, ctx.getColor(color_t), stroke);
        };

        for (int j = 0; j < n; ++j) {
            for (int i = 0; i < n; ++i) {
                float x0 = i * s, y0 = j * s;
                float t = 0.2f + 0.8f * std::min(1.0f, std::hypot((i + 0.5f) / n - ox, (j + 0.5f) / n - oy) * 1.25f);
                bool flip = U(0, 1) < 0.5f;
                if (style == 2) {
                    std::vector<Point2f> corners = {{x0, y0}, {x0 + s, y0}, {x0 + s, y0 + s}, {x0, y0 + s}};
                    corners.erase(corners.begin() + I(0, 3));
                    ctx.fillPolygon(corners, ctx.getColor(t));
                    continue;
                }
                std::vector<float> radii = style == 1 ? std::vector<float>{s / 3, 2 * s / 3} : std::vector<float>{s / 2};
                float stroke = style == 1 ? width * 0.6f : width;
                for (float r : radii) {
                    if (flip) {
                        arc(x0, y0, r, 0, t, stroke);
                        arc(x0 + s, y0 + s, r, (float)M_PI, t, stroke);
                    } else {
                        arc(x0 + s, y0, r, (float)M_PI / 2, t, stroke);
                        arc(x0, y0 + s, r, 1.5f * (float)M_PI, t, stroke);
                    }
                }
            }
        }
    }
};
