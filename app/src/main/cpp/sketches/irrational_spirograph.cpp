#include "sketch_base.h"
#include <random>
#include <cmath>
#include <complex>
#include <vector>
#include <algorithm>

class IrrationalSpirographSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        std::uniform_real_distribution<float> dist_kl(0.01f, 0.99f);

        float t_max = 200.0f * M_PI;
        int n_points = 10000;

        struct CurveData {
            std::vector<Point2f> pts;
            SkColor color;
        };
        std::vector<CurveData> curves;
        float max_limit = 0.01f;

        for (int i = 0; i < 4; ++i) {
            float k = dist_kl(rng);
            float l = dist_kl(rng);
            SkColor color = ctx.getColor(std::uniform_real_distribution<float>(0, 1)(rng));

            std::vector<Point2f> pts;
            for (int j = 0; j < n_points; ++j) {
                float t = t_max * j / (n_points - 1);
                std::complex<float> s = (1.0f - k) * std::polar(1.0f, t) + k * l * std::polar(1.0f, -t * (1.0f - k) / k);
                pts.push_back({s.real(), s.imag()});
                max_limit = std::max({max_limit, std::abs(s.real()), std::abs(s.imag())});
            }
            curves.push_back({std::move(pts), color});
        }

        ctx.setWorldBounds(-max_limit * 1.05f, max_limit * 1.05f, -max_limit * 1.05f, max_limit * 1.05f);

        for (const auto& curve : curves) {
            ctx.drawPolyline(curve.pts, curve.color, 1.0f, 0.9f);
        }
    }
};

