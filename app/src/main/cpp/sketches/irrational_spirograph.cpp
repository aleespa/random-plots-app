#include "sketch_base.h"
#include <random>
#include <cmath>
#include <complex>
#include <vector>

class IrrationalSpirographSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        std::uniform_real_distribution<float> dist_kl(0.01f, 0.99f);

        float t_max = 200.0f * M_PI; // Large t for complex pattern
        int n_points = 10000;

        ctx.setWorldBounds(-1.5f, 1.5f, -1.5f, 1.5f);

        for (int i = 0; i < 4; ++i) {
            float k = dist_kl(rng);
            float l = dist_kl(rng);
            SkColor color = ctx.getColor(std::uniform_real_distribution<float>(0, 1)(rng));

            std::vector<Point2f> pts;
            for (int j = 0; j < n_points; ++j) {
                float t = t_max * j / (n_points - 1);
                std::complex<float> s = (1.0f - k) * std::polar(1.0f, t) + k * l * std::polar(1.0f, -t * (1.0f - k) / k);
                pts.push_back({s.real(), s.imag()});
            }
            ctx.drawPolyline(pts, color, 1.0f, 0.9f);
        }
    }
};
