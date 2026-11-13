// Flow Field: particles follow a smooth value-noise vector field; their trails accumulate in the
// density renderer as fine streamlines, coloured by where each particle started.
#include "sketch_base.h"
#include "sketch_util.h"
#include <cmath>
#include <random>
#include <vector>

class FlowFieldSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        auto U = [&](float a, float b) { return std::uniform_real_distribution<float>(a, b)(rng); };
        ctx.setWorldBounds(0, 1, 0, 1);

        constexpr int G = 256;
        std::vector<float> grid(G * G);
        for (float& v : grid) v = U(0, 1);
        auto smooth = [](float t) { return t * t * (3 - 2 * t); };
        auto noise = [&](float x, float y) {
            int xi = (int)std::floor(x), yi = (int)std::floor(y);
            float xf = smooth(x - xi), yf = smooth(y - yi);
            auto g = [&](int a, int b) { return grid[(b & (G - 1)) * G + (a & (G - 1))]; };
            float top = g(xi, yi) + (g(xi + 1, yi) - g(xi, yi)) * xf;
            float bot = g(xi, yi + 1) + (g(xi + 1, yi + 1) - g(xi, yi + 1)) * xf;
            return top + (bot - top) * yf;
        };
        const float freq = U(1.5f, 4.5f), turns = U(1, 3), off = U(0, 100);
        auto field = [&](float x, float y) {
            return (noise(x * freq + off, y * freq) * 0.7f + noise(x * freq * 2.3f, y * freq * 2.3f + off) * 0.3f)
                   * 2 * (float)M_PI * turns;
        };
        const bool by_position = U(0, 1) < 0.5f;

        auto density = ctx.makeDensity();
        density.prepareConcurrent(true);
        const uint64_t base_seed = rng();
        const float scale = ctx.width() / 480.0f;
        const int particles = (int)(7000 * scale), steps = (int)(90 * scale);
        const float h = 1.1f / ctx.width();
        constexpr int kChunks = 8;  // fixed, so the image doesn't depend on the core count
        sketchutil::parallelChunks(kChunks, [&](int chunk) {
          sketchutil::FastRng fast(base_seed + 0x9E3779B97F4A7C15ULL * (chunk + 1));
          for (int k = chunk; k < particles; k += kChunks) {
            float x = fast.uniform(), y = fast.uniform();
            float t = by_position ? x : field(x, y) / (2 * (float)M_PI * turns);
            t = std::clamp(t, 0.0f, 1.0f);
            for (int s = 0; s < steps; ++s) {
                float a = field(x, y);
                x += std::cos(a) * h; y += std::sin(a) * h;
                if (x < 0 || x >= 1 || y < 0 || y >= 1) break;
                density.addConcurrent({x, y}, t);
            }
          }
        });
        density.recomputeMax();
        ctx.drawDensity(density, 0.5f);
    }
};
