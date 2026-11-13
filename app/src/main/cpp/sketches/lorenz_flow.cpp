// Lorenz Flow: a random 3D chaotic flow (Lorenz, Roessler, Thomas or Aizawa) integrated with RK4
// for a few nearby starting points, viewed from a random angle and drawn as thin ribbons.
#include "sketch_base.h"
#include "sketch_util.h"
#include <array>
#include <cmath>
#include <functional>
#include <random>
#include <vector>

class LorenzFlowSketch : public SketchBase {
    using V3 = std::array<double, 3>;

public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        auto U = [&](double a, double b) { return std::uniform_real_distribution<double>(a, b)(rng); };

        std::function<V3(const V3&)> f;
        double dt;
        int steps;
        V3 start;
        switch (std::uniform_int_distribution<int>(0, 3)(rng)) {
            case 0: {  // Lorenz
                double s = U(9, 11), r = U(26, 32), b = 8.0 / 3.0;
                f = [=](const V3& p) { return V3{s * (p[1] - p[0]), p[0] * (r - p[2]) - p[1], p[0] * p[1] - b * p[2]}; };
                dt = 0.005; steps = 9000; start = {0.1, 0, 20};
                break;
            }
            case 1: {  // Roessler
                double a = U(0.15, 0.25), b = 0.2, c = U(5, 6.5);
                f = [=](const V3& p) { return V3{-p[1] - p[2], p[0] + a * p[1], b + p[2] * (p[0] - c)}; };
                dt = 0.02; steps = 9000; start = {1, 1, 0};
                break;
            }
            case 2: {  // Thomas
                double b = U(0.18, 0.21);
                f = [=](const V3& p) {
                    return V3{std::sin(p[1]) - b * p[0], std::sin(p[2]) - b * p[1], std::sin(p[0]) - b * p[2]};
                };
                dt = 0.06; steps = 9000; start = {0.1, 0, 0};
                break;
            }
            default: {  // Aizawa
                f = [](const V3& p) {
                    double x = p[0], y = p[1], z = p[2];
                    return V3{(z - 0.7) * x - 3.5 * y, 3.5 * x + (z - 0.7) * y,
                              0.6 + 0.95 * z - z * z * z / 3 - (x * x + y * y) * (1 + 0.25 * z) + 0.1 * z * x * x * x};
                };
                dt = 0.01; steps = 10000; start = {0.1, 0, 0};
                break;
            }
        }
        auto rk4 = [&](const V3& p) {
            auto add = [](const V3& a, const V3& b, double h) { return V3{a[0] + h * b[0], a[1] + h * b[1], a[2] + h * b[2]}; };
            V3 k1 = f(p), k2 = f(add(p, k1, dt / 2)), k3 = f(add(p, k2, dt / 2)), k4 = f(add(p, k3, dt));
            V3 out;
            for (int i = 0; i < 3; ++i) out[i] = p[i] + dt / 6 * (k1[i] + 2 * k2[i] + 2 * k3[i] + k4[i]);
            return out;
        };

        double yaw = U(0, 2 * M_PI), pitch = U(-1.2, 1.2);
        double cy = std::cos(yaw), sy = std::sin(yaw), cp = std::cos(pitch), sp = std::sin(pitch);
        auto project = [&](const V3& p) {
            double x1 = p[0] * cy - p[1] * sy, y1 = p[0] * sy + p[1] * cy;
            return Point2f{(float)x1, (float)(y1 * cp - p[2] * sp)};
        };

        V3 base = start;
        for (int i = 0; i < 1500; ++i) base = rk4(base);  // settle onto the attractor

        int count = std::uniform_int_distribution<int>(3, 6)(rng);
        std::vector<std::vector<Point2f>> paths;
        std::vector<float> xs, ys;
        for (int t = 0; t < count; ++t) {
            V3 p = {base[0] + U(-0.5, 0.5), base[1] + U(-0.5, 0.5), base[2] + U(-0.5, 0.5)};
            std::vector<Point2f> pts;
            pts.reserve(steps);
            for (int i = 0; i < steps; ++i) {
                p = rk4(p);
                Point2f q = project(p);
                if (!std::isfinite(q.x) || !std::isfinite(q.y)) break;
                pts.push_back(q);
                xs.push_back(q.x); ys.push_back(q.y);
            }
            paths.push_back(std::move(pts));
        }
        sketchutil::fitSquare(xs, ys, 0.002f, 0.998f, 0.06f).apply(ctx);

        const size_t chunk = 150;  // colour changes along each path
        for (int t = 0; t < (int)paths.size(); ++t) {
            const auto& pts = paths[t];
            for (size_t s = 0; s + 1 < pts.size(); s += chunk) {
                size_t e = std::min(pts.size(), s + chunk + 1);
                std::vector<Point2f> seg(pts.begin() + s, pts.begin() + e);
                float tone = 0.25f + 0.75f * (0.6f * (float)s / pts.size() + 0.4f * (float)t / count);
                ctx.drawPolyline(seg, ctx.getColor(tone), 0.8f, 0.5f);
            }
        }
    }
};
