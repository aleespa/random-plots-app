#include "sketch_base.h"
#include "../colormap.h"
#include <random>
#include <cmath>
#include <vector>

class RandomCirclesSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        std::exponential_distribution<float> exp_dist(1.0f);

        float variance = exp_dist(rng);
        const int n = 150;
        const int n_rings = 100;
        auto cmap = viridis();

        float world_max = 20.0f * (1.0f + variance) * 1.1f;
        ctx.setWorldBounds(-world_max, world_max, -world_max, world_max);

        for (int zi = 0; zi < n_rings; ++zi) {
            float z = 1.0f + zi * (19.0f / (n_rings - 1));
            float t = z / 20.0f;
            SkColor color = sampleColormap(cmap, t);

            std::uniform_real_distribution<float> rand_r(z, (1.0f + variance) * z);
            float r0 = rand_r(rng);

            std::vector<Point2f> pts;
            pts.reserve(n);
            for (int i = 0; i < n; ++i) {
                float theta = 2.0f * M_PI * i / (n - 1);
                float r = (i == 0 || i == n - 1) ? r0 : rand_r(rng);
                pts.push_back({ std::cos(theta) * r, std::sin(theta) * r });
            }
            ctx.drawPolyline(pts, color, 1.1f);
        }
    }
};
