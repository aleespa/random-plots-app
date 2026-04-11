#include "sketch_base.h"
#include <random>
#include <complex>
#include <vector>

class ExponentialSumSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        std::uniform_real_distribution<float> dist_p1(12.0f, 15.0f);
        std::uniform_real_distribution<float> dist_p2(90.0f, 120.0f);

        int n = 4500;
        int num_segments = 50;
        int step = 90;
        float p1 = dist_p1(rng);
        float p2 = dist_p2(rng);

        std::vector<std::complex<float>> s(n);
        std::complex<float> current(0, 0);
        float x_min = 0, x_max = 0, y_min = 0, y_max = 0;

        for (int i = 0; i < n; ++i) {
            float angle = (i / p2 + std::cos(i * p1)) * 2.0f * M_PI;
            current += std::polar(1.0f, angle);
            s[i] = current;
            x_min = std::min(x_min, current.real());
            x_max = std::max(x_max, current.real());
            y_min = std::min(y_min, current.imag());
            y_max = std::max(y_max, current.imag());
        }

        float mid_x = (x_min + x_max) / 2.0f;
        float mid_y = (y_min + y_max) / 2.0f;
        float span_x = x_max - x_min;
        float span_y = y_max - y_min;
        float max_span = std::max(span_x, span_y) * 1.05f;

        ctx.setWorldBounds(mid_x - max_span / 2.0f, mid_x + max_span / 2.0f,
                           mid_y - max_span / 2.0f, mid_y + max_span / 2.0f);

        for (int z = 0; z < num_segments; ++z) {
            int start = step * z;
            int end = std::min(n, step * (z + 1) + 1);
            std::vector<Point2f> pts;
            for (int i = start; i < end; ++i) {
                pts.push_back({s[i].real(), s[i].imag()});
            }
            ctx.drawPolyline(pts, ctx.getColor((float)z / num_segments + 0.2f), 1.0f, 0.8f);
        }
    }
};
