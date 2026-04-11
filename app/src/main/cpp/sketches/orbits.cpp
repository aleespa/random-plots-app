#include "sketch_base.h"
#include <random>
#include <cmath>
#include <complex>
#include <vector>

class OrbitsSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        std::uniform_real_distribution<float> dist_c(-0.02f, 0.02f);

        float cc_r = dist_c(rng);
        float cc_i = dist_c(rng);
        std::complex<float> c(cc_r, cc_i);
        float s = -1.0f;

        int n_loops = 400;
        int n_points = 256;

        std::normal_distribution<float> dist_norm(0, 1);

        ctx.setWorldBounds(-1.5f, 2.5f, -2.5f, 1.5f); // Approximation

        for (int l = 0; l < n_loops; ++l) {
            float r0 = dist_norm(rng);
            float r1 = dist_norm(rng);

            std::complex<float> cur_x = 1.0f + r1 - c * std::abs(r0);
            std::complex<float> cur_y = s * r0;

            SkColor color = ctx.getColor((float)l / n_loops);

            std::vector<Point2f> pts;
            pts.push_back({cur_x.real(), cur_y.real()});

            for (int p = 1; p < n_points; ++p) {
                std::complex<float> next_x = 1.0f + cur_y - c * std::abs(cur_x);
                std::complex<float> next_y = s * cur_x;
                cur_x = next_x;
                cur_y = next_y;
                pts.push_back({cur_x.real(), cur_y.real()});
            }
            ctx.drawScatter(pts, color, 1.1f, 0.95f);
        }
    }
};
