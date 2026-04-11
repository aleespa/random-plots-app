#include "sketch_base.h"
#include <random>
#include <cmath>
#include <vector>

class WavesSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        std::uniform_int_distribution<int> dist_n_waves(12, 22);
        std::uniform_real_distribution<float> dist_lw(1.0f, 4.0f);
        std::uniform_real_distribution<float> dist_01(0.0f, 1.0f);

        int n = 750;
        int n_waves = dist_n_waves(rng);

        ctx.setWorldBounds(0, 2.0f * M_PI, -2.0f, n_waves * 1.6f + 2.0f);

        for (int k = 0; k < n_waves; ++k) {
            auto bb = brownian_bridge(rng, n);
            float lw = dist_lw(rng);
            SkColor color = ctx.getColor(dist_01(rng));

            std::vector<Point2f> pts;
            pts.reserve(n);
            for (int i = 0; i < n; ++i) {
                float x = (2.0f * M_PI * i) / (n - 1);
                float y = std::sin(bb[i]) * std::cos(i) + k * 1.6f;
                pts.push_back({x, y});
            }
            ctx.drawPolyline(pts, color, lw);
        }
    }

private:
    std::vector<float> brownian_bridge(std::mt19937_64& rng, int n) {
        std::normal_distribution<float> dist(0.0f, std::sqrt(1.0f / n));
        std::vector<float> W(n);
        float sum = 0;
        for (int i = 0; i < n; ++i) {
            sum += dist(rng);
            W[i] = sum;
        }
        float last = W.back();
        for (int i = 0; i < n; ++i) {
            float t = (float)i / (n - 1);
            W[i] -= t * last;
        }
        return W;
    }
};
