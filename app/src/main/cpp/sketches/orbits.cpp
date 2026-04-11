#include "sketch_base.h"
#include <random>
#include <cmath>
#include <complex>
#include <vector>
#include <algorithm>

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

        struct ScatterData {
            std::vector<Point2f> pts;
            SkColor color;
        };
        std::vector<ScatterData> batches;
        float x_min = 1e9, x_max = -1e9, y_min = 1e9, y_max = -1e9;

        for (int l = 0; l < n_loops; ++l) {
            float r0 = dist_norm(rng);
            float r1 = dist_norm(rng);

            std::complex<float> cur_x = 1.0f + r1 - c * std::abs(r0);
            std::complex<float> cur_y = s * r0;

            SkColor color = ctx.getColor((float)l / n_loops);

            std::vector<Point2f> pts;
            pts.reserve(n_points);

            auto update_bounds = [&](float px, float py) {
                x_min = std::min(x_min, px);
                x_max = std::max(x_max, px);
                y_min = std::min(y_min, py);
                y_max = std::max(y_max, py);
            };

            pts.push_back({cur_x.real(), cur_y.real()});
            update_bounds(cur_x.real(), cur_y.real());

            for (int p = 1; p < n_points; ++p) {
                std::complex<float> next_x = 1.0f + cur_y - c * std::abs(cur_x);
                std::complex<float> next_y = s * cur_x;
                cur_x = next_x;
                cur_y = next_y;
                pts.push_back({cur_x.real(), cur_y.real()});
                update_bounds(cur_x.real(), cur_y.real());
            }
            batches.push_back({std::move(pts), color});
        }

        // Center on the data's bounding box to match Python's bbox_inches='tight'
        float mid_x = (x_min + x_max) / 2.0f;
        float mid_y = (y_min + y_max) / 2.0f;
        float span_x = x_max - x_min;
        float span_y = y_max - y_min;
        float max_span = std::max(span_x, span_y) * 1.05f; // 5% padding

        ctx.setWorldBounds(mid_x - max_span / 2.0f, mid_x + max_span / 2.0f,
                           mid_y - max_span / 2.0f, mid_y + max_span / 2.0f);

        for (const auto& batch : batches) {
            ctx.drawScatter(batch.pts, batch.color, 1.1f, 0.95f);
        }
    }
};

