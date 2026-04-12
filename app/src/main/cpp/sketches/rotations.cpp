#include "sketch_base.h"
#include <random>
#include <cmath>
#include <vector>

class RotationsSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        std::uniform_int_distribution<int> dist_n(4, 12);
        std::uniform_int_distribution<int> dist_i(4, 8);
        std::uniform_int_distribution<int> dist_k(50, 70);

        int n_circles = dist_n(rng);
        int sides = dist_i(rng);
        int rotations = dist_k(rng);

        ctx.setWorldBounds(-1.1f, 1.1f, -1.1f, 1.1f);

        std::vector<float> theta_base(sides);
        for (int i = 0; i < sides; ++i) theta_base[i] = 2.0f * M_PI * i / (sides - 1);

        for (int r_idx = 0; r_idx < n_circles; ++r_idx) {
            float r = (float)r_idx / (n_circles - 1);
            for (int t_idx = 0; t_idx < rotations; ++t_idx) {
                float t = M_PI * t_idx / (rotations - 1);
                SkColor color = ctx.getColor(t / M_PI);

                std::vector<Point2f> pts;
                for (float tb : theta_base) {
                    pts.push_back({ r * std::cos(tb + t), r * std::sin(tb + t) });
                }
                ctx.drawPolyline(pts, color, r + 0.8f, 0.8f);
            }
        }
    }
};
