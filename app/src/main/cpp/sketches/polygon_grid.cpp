// Polygon Grid: string art between two rings. Line j joins vertex s on ring r_j to vertex s + skip
// on the mirrored ring, so the lines' envelope draws a star. The seed varies the number of points,
// the skip (sharper or rounder stars), how the rings are spaced (straight or curved envelopes), the
// line count, an optional second layer turned half a step, and colour by layer or around the star.
#include "sketch_base.h"
#include <cmath>
#include <random>
#include <vector>

class PolygonGridSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        auto U = [&](float a, float b) { return std::uniform_real_distribution<float>(a, b)(rng); };
        auto I = [&](int a, int b) { return std::uniform_int_distribution<int>(a, b)(rng); };

        const int sides = I(8, 14);
        const float min_radius = U(0.2f, 0.7f);
        const int n = I(24, 48);
        const int skip = U(0, 1) < 0.65f ? 1 : I(2, std::max(2, sides / 4));
        const float gamma = U(0, 1) < 0.5f ? 1.0f : U(0.55f, 1.8f);  // curved envelopes
        const bool second = U(0, 1) < 0.3f;                          // a layer turned half a step
        const bool around = U(0, 1) < 0.3f;                          // colour around the star

        ctx.setWorldBounds(-1.1f, 1.1f, -1.1f, 1.1f);

        std::vector<float> r(n);
        for (int i = 0; i < n; ++i) r[i] = min_radius + (1.0f - min_radius) * std::pow((float)i / (n - 1), gamma);

        auto layer = [&](float offset, float t0, float t1, float alpha) {
            for (int j = 0; j < n; ++j) {
                for (int s = 0; s < sides; ++s) {
                    float a1 = 2 * (float)M_PI * s / sides + offset;
                    float a2 = 2 * (float)M_PI * (s + skip) / sides + offset;
                    Point2f p1 = {r[j] * std::cos(a1), r[j] * std::sin(a1)};
                    Point2f p2 = {r[n - j - 1] * std::cos(a2), r[n - j - 1] * std::sin(a2)};
                    float t = around ? (float)s / sides : (float)j / n;
                    ctx.drawPolyline({p1, p2}, ctx.getColor(t0 + (t1 - t0) * t), 1.6f, alpha);
                }
            }
        };
        if (second) {
            layer((float)M_PI / sides, 0.5f, 1.0f, 0.55f);  // behind, in the colormap's other half
            layer(0, 0.0f, 0.5f, 0.9f);
        } else {
            layer(0, 0.0f, 1.0f, 0.9f);
        }
    }
};
