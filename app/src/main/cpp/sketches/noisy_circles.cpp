#include "sketch_base.h"
#include <random>
#include <cmath>
#include <vector>
#include <algorithm>

class NoisyCirclesSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);

        // variance = rng.exponential(1)
        std::exponential_distribution<float> dist_exp(1.0f);
        float variance = dist_exp(rng);

        int n = 150;
        float z_start = 1.0f;
        float z_end = 20.0f;
        int num_steps = 100;

        struct CurveData {
            std::vector<Point2f> pts;
            SkColor color;
        };
        std::vector<CurveData> curves;
        float max_limit = 0.01f;

        for (int i = 0; i < num_steps; ++i) {
            float z = z_start + (z_end - z_start) * i / (num_steps - 1);
            SkColor color = ctx.getColor(z / z_end);

            std::vector<Point2f> pts;
            pts.reserve(n);

            std::uniform_real_distribution<float> dist_r(1.0f * z, (1.0f + variance) * z);
            float r0 = dist_r(rng);

            for (int j = 0; j < n; ++j) {
                float theta = 2.0f * M_PI * j / (n - 1);
                float r;
                if (j == 0 || j == n - 1) {
                    r = r0;
                } else {
                    r = dist_r(rng);
                }
                float px = r * std::cos(theta);
                float py = r * std::sin(theta);
                pts.push_back({px, py});
                max_limit = std::max({max_limit, std::abs(px), std::abs(py)});
            }
            curves.push_back({std::move(pts), color});
        }

        // Match Python: symmetrical bounds with small padding
        ctx.setWorldBounds(-max_limit * 1.02f, max_limit * 1.02f, -max_limit * 1.02f, max_limit * 1.02f);

        for (const auto& curve : curves) {
            ctx.drawPolyline(curve.pts, curve.color, 1.1f, 1.0f);
        }
    }
};
