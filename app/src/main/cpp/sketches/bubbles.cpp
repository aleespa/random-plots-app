#include "sketch_base.h"
#include <random>
#include <cmath>

class BubblesSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        std::uniform_real_distribution<float> dist_01(0.0f, 1.0f);
        std::uniform_real_distribution<float> dist_ab(0.5f, 1.0f);
        std::uniform_real_distribution<float> dist_m11(-1.0f, 1.0f);

        int l = 300;
        int n = 28;

        float a1 = dist_ab(rng) * (rng() % 2 == 0 ? 1.0f : -1.0f);
        float b1 = dist_ab(rng) * (rng() % 2 == 0 ? 1.0f : -1.0f);
        float a2 = dist_m11(rng);
        float b2 = dist_m11(rng);

        ctx.setWorldBounds(-10.0f, 10.0f, -10.0f, 10.0f); // Will be adjusted by max_r

        std::vector<float> x_vals(l);
        for (int i = 0; i < l; ++i) x_vals[i] = 2.0f * M_PI * i / (l - 1);

        float max_val = 0.0f;
        struct Line {
            std::vector<Point2f> pts;
            SkColor color;
        };
        std::vector<Line> lines;

        for (int i = 0; i < n; ++i) {
            float z = -2.0f * M_PI + 4.0f * M_PI * i / (n - 1);
            for (int j = 0; j < n; ++j) {
                float w = -2.0f * M_PI + 4.0f * M_PI * j / (n - 1);

                float sum_zw = a1 * z + b1 * w;
                float radius = a2 * std::sin(sum_zw) + b2 * std::cos(sum_zw);

                std::vector<Point2f> pts;
                pts.reserve(l);
                for (int k = 0; k < l; ++k) {
                    float px = radius * std::cos(x_vals[k]) + z;
                    float py = radius * std::sin(x_vals[k]) + w;
                    pts.push_back({px, py});
                    max_val = std::max({max_val, std::abs(px), std::abs(py)});
                }
                lines.push_back({pts, ctx.getColor(dist_01(rng))});
            }
        }

        ctx.setWorldBounds(-max_val - 0.5f, max_val + 0.5f, -max_val - 0.5f, max_val + 0.5f);
        for (const auto& line : lines) {
            ctx.drawPolyline(line.pts, line.color, 2.2f, 0.9f);
        }
    }
};
