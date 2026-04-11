#include "sketch_base.h"
#include <random>
#include <cmath>
#include <vector>

class SpiralSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        std::uniform_int_distribution<int> dist_sides(5, 12);
        std::uniform_real_distribution<float> dist_01(0.0f, 1.0f);

        int n_sides = dist_sides(rng);
        if (rng() % 5 == 0) n_sides = 300; // approximation of circle

        float sides_step = 2.0f * M_PI / n_sides;
        std::vector<float> sides(n_sides);
        for (int i = 0; i < n_sides; ++i) sides[i] = i * sides_step;

        ctx.setWorldBounds(-32, 32, -32, 32);

        for (float k = 0; k <= 30.0f; k += 0.3f) {
            std::vector<Point2f> pts;
            pts.reserve(n_sides + 1);
            for (float s : sides) {
                pts.push_back({
                    k * std::cos(s) + std::cos(k + 1.0f),
                    k * std::sin(s) + std::sin(k - 1.0f)
                });
            }
            pts.push_back(pts[0]); // close loop

            std::uniform_real_distribution<float> dist_lw(0.5f, 4.0f);
            ctx.drawPolyline(pts, ctx.getColor((k + 15.0f) / 60.0f), dist_lw(rng), 0.7f);
        }
    }
};
