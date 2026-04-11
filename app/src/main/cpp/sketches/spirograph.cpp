#include "sketch_base.h"
#include <random>
#include <cmath>
#include <complex>
#include <vector>
#include <algorithm>

class SpirographSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        std::uniform_int_distribution<int> dist_n(4, 5);
        std::uniform_int_distribution<int> dist_param(3, 34);

        int n_spiro = dist_n(rng);
        int n_points = 5000;
        float t_max = 2.0f * M_PI;

        struct SpiroData {
            std::vector<Point2f> pts;
            SkColor color;
        };
        std::vector<SpiroData> curves;
        float max_limit = 0.01f;

        for (int i = 0; i < n_spiro; ++i) {
            float a, b, c, d, k, l;
            // Match Python while True: generate_k_l logic
            int safety = 0;
            do {
                std::vector<int> vals(4);
                for (int& v : vals) v = dist_param(rng);
                std::sort(vals.begin(), vals.end());
                a = (float)vals[0]; b = (float)vals[1];
                c = (float)vals[2]; d = (float)vals[3];
                k = (b == 0) ? 0.5f : a / b;
                l = (d == 0) ? 0.5f : c / d;
                if (++safety > 100) break;
            } while (k == 1.0f || l == 1.0f || k == l);

            SkColor color = ctx.getColor(std::uniform_real_distribution<float>(0, 1)(rng));

            std::vector<Point2f> pts;
            for (int j = 0; j < n_points; ++j) {
                float t = t_max * j / (n_points - 1) * a;
                std::complex<float> s = (1.0f - k) * std::polar(1.0f, t) + k * l * std::polar(1.0f, -t * (1.0f - k) / k);
                pts.push_back({s.real(), s.imag()});
                max_limit = std::max({max_limit, std::abs(s.real()), std::abs(s.imag())});
            }
            curves.push_back({std::move(pts), color});
        }

        ctx.setWorldBounds(-max_limit * 1.05f, max_limit * 1.05f, -max_limit * 1.05f, max_limit * 1.05f);

        for (const auto& curve : curves) {
            ctx.drawPolyline(curve.pts, curve.color, 3.5f, 0.9f);
        }
    }
};

