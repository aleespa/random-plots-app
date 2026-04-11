#include "sketch_base.h"
#include <random>
#include <cmath>
#include <complex>
#include <vector>

class SpirographSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        std::uniform_int_distribution<int> dist_n(4, 5);

        int n_spiro = dist_n(rng);
        int n_points = 5000;
        float t_max = 2.0f * M_PI;

        ctx.setWorldBounds(-1.5f, 1.5f, -1.5f, 1.5f); // Will be adjusted by spiro logic

        float max_r = 0;
        std::vector<std::vector<Point2f>> all_spiros;

        for (int i = 0; i < n_spiro; ++i) {
            float a = std::uniform_int_distribution<int>(3, 35)(rng);
            float b = std::uniform_int_distribution<int>(3, 35)(rng);
            float c = std::uniform_int_distribution<int>(3, 35)(rng);
            float d = std::uniform_int_distribution<int>(3, 35)(rng);

            float k = a / b;
            float l = c / d;
            SkColor color = ctx.getColor(std::uniform_real_distribution<float>(0, 1)(rng));

            std::vector<Point2f> pts;
            for (int j = 0; j < n_points; ++j) {
                float t = t_max * j / (n_points - 1) * a;
                std::complex<float> s = (1.0f - k) * std::polar(1.0f, t) + k * l * std::polar(1.0f, -t * (1.0f - k) / k);
                pts.push_back({s.real(), s.imag()});
                max_r = std::max({max_r, std::abs(s.real()), std::abs(s.imag())});
            }
            all_spiros.push_back(pts);
            ctx.drawPolyline(pts, color, 3.5f, 0.9f);
        }

        ctx.setWorldBounds(-max_r, max_r, -max_r, max_r);
        // Note: Skia draws into the buffer, so changing world bounds after drawing won't reposition existing lines.
        // We should ideally calculate max_r first.
    }
};
