#include "sketch_base.h"
#include <random>
#include <cmath>
#include <vector>

class RandomCirclesSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        std::uniform_real_distribution<float> dist_01(0.0f, 1.0f);

        int n = 120;
        ctx.setWorldBounds(-1.1f, 1.1f, -1.1f, 1.1f);

        for (int i = 0; i < n; ++i) {
            float x = dist_01(rng) * 2.0f - 1.0f;
            float y = dist_01(rng) * 2.0f - 1.0f;
            float r = dist_01(rng) * 0.2f + 0.05f;

            SkColor color = ctx.getColor(dist_01(rng));

            // Draw a circle as a polyline
            const int n_points = 50;
            std::vector<Point2f> pts;
            for (int j = 0; j <= n_points; ++j) {
                float theta = 2.0f * M_PI * j / n_points;
                pts.push_back({ x + r * std::cos(theta), y + r * std::sin(theta) });
            }
            ctx.drawPolyline(pts, color, 1.5f, 0.8f);
        }
    }
};
