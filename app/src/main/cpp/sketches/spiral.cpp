#include "sketch_base.h"
#include <random>
#include <cmath>
#include <complex>
#include <vector>

class SpiralSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        std::uniform_real_distribution<float> dist_c(-0.02f, 0.03f);
        float c_val = dist_c(rng);
        float s = -0.99f;

        int n_loops = 80;
        int n_points = 1000;

        std::normal_distribution<float> dist_norm(0, 1);
        std::complex<float> c(c_val, 0); // Simplified as real part

        // The Python script uses fixed bounds which implies a specific zoom.
        // We add a bit of extra margin to "zoom out" as requested.
        float margin = 0.4f;
        ctx.setWorldBounds(-1.4f - margin, 2.4f + margin, -2.4f - margin, 1.4f + margin);

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
            ctx.drawScatter(pts, color, 2.5f, 0.9f);
        }
    }
};
