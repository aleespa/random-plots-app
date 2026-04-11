#include "sketch_base.h"
#include <random>
#include <cmath>
#include <vector>

class PolygonTunnelSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        std::uniform_int_distribution<int> dist_sides(5, 12);
        std::uniform_real_distribution<float> dist_01(0.0f, 1.0f);

        int n_sides = dist_sides(rng);
        if (rng() % 5 == 0) n_sides = 300; // circle

        std::vector<float> sides(n_sides);
        for (int i = 0; i < n_sides; ++i) sides[i] = 2.0f * M_PI * i / n_sides;

        ctx.setWorldBounds(-32, 32, -32, 32);

        std::uniform_int_distribution<int> dist_norm(10, 25);
        std::uniform_int_distribution<int> dist_scale(30, 120);
        int norm = dist_norm(rng);
        int scale = dist_scale(rng);

        for (float k = 0; k <= 30.0f; k += 0.3f) {
            std::vector<Point2f> pts;
            pts.reserve(n_sides + 1);
            for (float s : sides) {
                pts.push_back({
                    k * std::cos(s) + std::cos(k + 1.0f),
                    k * std::sin(s) + std::sin(k - 1.0f)
                });
            }
            pts.push_back(pts[0]);

            std::uniform_real_distribution<float> dist_lw(0.5f, 4.0f);
            ctx.drawPolyline(pts, ctx.getColor((k + norm) / (float)scale), dist_lw(rng), 0.7f);
        }
    }
};
