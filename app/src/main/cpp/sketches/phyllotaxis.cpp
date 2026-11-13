// Phyllotaxis: the sunflower spiral (dot i at angle i * golden angle, radius ~ sqrt(i)), always
// centred and grown past the corners so it fills the frame. Coloured along the Fibonacci spiral
// arms, by radius, or both; a slight angle detune bends the arms.
#include "sketch_base.h"
#include <cmath>
#include <random>

class PhyllotaxisSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        auto U = [&](float a, float b) { return std::uniform_real_distribution<float>(a, b)(rng); };
        ctx.setWorldBounds(-0.5f, 0.5f, -0.5f, 0.5f);

        const double golden = M_PI * (3.0 - std::sqrt(5.0));
        const double angle = U(0, 1) < 0.5f ? golden : golden + U(-0.004f, 0.004f);
        const float spacing = U(0.0104f, 0.023f), reach = 0.72f;  // reach > half-diagonal
        const int n = (int)std::ceil((reach / spacing) * (reach / spacing));
        static const int kArms[] = {8, 13, 21, 34, 55};
        const int arms = kArms[std::uniform_int_distribution<int>(0, 4)(rng)];
        const int mode = std::uniform_int_distribution<int>(0, 2)(rng);  // arms, radius, both
        const float size = U(0.3f, 0.55f);
        const bool grow = U(0, 1) < 0.5f;

        for (int i = n; i >= 0; --i) {  // outside in, so inner dots sit on top
            float r = spacing * std::sqrt((float)i);
            double th = i * angle;
            float x = r * (float)std::cos(th), y = r * (float)std::sin(th);
            if (std::abs(x) > 0.5f + spacing || std::abs(y) > 0.5f + spacing) continue;
            float tr = std::min(1.0f, r / reach), ta = (float)(i % arms) / arms;
            float t = mode == 0 ? ta : mode == 1 ? tr : 0.5f * (ta + tr);
            float rad = spacing * size * (grow ? 0.5f + tr : 1.0f);
            ctx.drawCircle({x, y}, rad, ctx.getColor(0.15f + 0.85f * t));
        }
    }
};
