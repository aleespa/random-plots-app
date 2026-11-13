// Strange Attractor: Clifford or Peter de Jong map iterated millions of times into the density
// renderer. Random parameters are retried until the orbit fills enough of the frame.
#include "sketch_base.h"
#include "sketch_util.h"
#include <cmath>
#include <random>
#include <vector>

class StrangeAttractorSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        auto U = [&](float a, float b) { return std::uniform_real_distribution<float>(a, b)(rng); };
        float a = 0, b = 0, c = 0, d = 0;
        bool clifford = true;
        auto step = [&](float& x, float& y) {
            float nx, ny;
            if (clifford) {
                nx = std::sin(a * y) + c * std::cos(a * x);
                ny = std::sin(b * x) + d * std::cos(b * y);
            } else {
                nx = std::sin(a * y) - std::cos(b * x);
                ny = std::sin(c * x) - std::cos(d * y);
            }
            x = nx; y = ny;
        };

        sketchutil::SquareBounds bounds;
        for (int attempt = 0; attempt < 60; ++attempt) {
            clifford = U(0, 1) < 0.5f;
            a = U(-2.4f, 2.4f); b = U(-2.4f, 2.4f); c = U(-2.4f, 2.4f); d = U(-2.4f, 2.4f);
            std::vector<float> xs, ys;
            float x = 0.1f, y = 0.1f;
            for (int i = 0; i < 12000; ++i) {
                step(x, y);
                if (i > 50) { xs.push_back(x); ys.push_back(y); }
            }
            bounds = sketchutil::fitSquare(xs, ys, 0.002f, 0.998f, 0.05f);
            if (sketchutil::occupiedCells(xs, ys, bounds, 60) > 900) break;
        }

        bounds.apply(ctx);
        auto density = ctx.makeDensity();
        density.prepareConcurrent(false);
        constexpr int kChunks = 8;  // fixed, so the image doesn't depend on the core count
        const long per_chunk = sketchutil::densityBudget(ctx, 4.0e6, 30000000L) / kChunks;
        sketchutil::parallelChunks(kChunks, [&](int k) {
            float x = 0.1f + 0.013f * k, y = 0.1f - 0.007f * k;  // each chunk follows its own orbit
            for (int i = 0; i < 100; ++i) step(x, y);  // settle onto the attractor
            for (long i = 0; i < per_chunk; ++i) {
                step(x, y);
                density.addConcurrent({x, y});
            }
        });
        density.recomputeMax();
        ctx.drawDensity(density, 0.55f);
    }
};
