#include "sketch_base.h"
#include <random>
#include <cmath>
#include <vector>

class PolygonGridSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        std::uniform_int_distribution<int> dist_sides(8, 14);
        std::uniform_real_distribution<float> dist_radius(0.2f, 0.7f);

        int n = 30;
        int sides = dist_sides(rng);
        float min_radius = dist_radius(rng);

        ctx.setWorldBounds(-1.1f, 1.1f, -1.1f, 1.1f);

        std::vector<float> r(n);
        for (int i = 0; i < n; ++i) {
            r[i] = min_radius + (1.0f - min_radius) * i / (n - 1);
        }

        std::vector<float> cos_a(sides), sin_a(sides);
        for (int i = 0; i < sides; ++i) {
            float angle = 2.0f * M_PI * i / sides;
            cos_a[i] = std::cos(angle);
            sin_a[i] = std::sin(angle);
        }

        for (int j = 0; j < n; ++j) {
            SkColor color = ctx.getColor((float)j / n);
            for (int s = 0; s < sides; ++s) {
                Point2f p1 = { r[j] * cos_a[s], r[j] * sin_a[s] };
                int next_s = (s + 1) % sides;
                Point2f p2 = { r[n - j - 1] * cos_a[next_s], r[n - j - 1] * sin_a[next_s] };
                ctx.drawPolyline({p1, p2}, color, 2.0f);
            }
        }
    }
};
