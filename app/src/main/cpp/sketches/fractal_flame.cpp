// Fractal Flame: a random iterated function system (2-4 affine maps, each followed by one or two
// nonlinear variations), with optional rotational symmetry, rendered by the chaos game into the
// density renderer. Each pixel is coloured by which maps landed there.
#include "sketch_base.h"
#include "sketch_util.h"
#include <cmath>
#include <random>
#include <vector>

class FractalFlameSketch : public SketchBase {
    struct Map { float a, b, c, d, e, f; int v1, v2; float w, color; };
    static constexpr int kVariations = 7;

    static void variation(int v, float x, float y, float& ox, float& oy) {
        switch (v) {
            case 0: ox = x; oy = y; break;                                         // linear
            case 1: ox = std::sin(x); oy = std::sin(y); break;                     // sinusoidal
            case 2: { float r2 = x * x + y * y + 1e-9f; ox = x / r2; oy = y / r2; break; }  // spherical
            case 3: { float r2 = x * x + y * y, s = std::sin(r2), c = std::cos(r2);       // swirl
                      ox = x * s - y * c; oy = x * c + y * s; break; }
            case 4: { float r = std::hypot(x, y) + 1e-9f;                                   // horseshoe
                      ox = (x - y) * (x + y) / r; oy = 2 * x * y / r; break; }
            case 5: ox = std::atan2(x, y) / (float)M_PI; oy = std::hypot(x, y) - 1; break;  // polar
            default: { float r = std::hypot(x, y), t = std::atan2(x, y);                    // handkerchief
                       ox = r * std::sin(t + r); oy = r * std::cos(t - r); break; }
        }
    }

public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        auto U = [&](float a, float b) { return std::uniform_real_distribution<float>(a, b)(rng); };
        auto I = [&](int a, int b) { return std::uniform_int_distribution<int>(a, b)(rng); };

        std::vector<Map> maps;
        int symmetry = 1;
        // Runs the chaos game for n steps with `fast`, calling visit(x, y, colour) after a warm-up.
        auto run = [&](long n, sketchutil::FastRng& fast, auto&& visit) {
            float x = fast.uniform() * 2 - 1, y = fast.uniform() * 2 - 1, cc = fast.uniform();
            for (long i = 0; i < n; ++i) {
                const Map& m = maps[fast.below((int)maps.size())];
                float ax = m.a * x + m.b * y + m.c, ay = m.d * x + m.e * y + m.f, vx, vy;
                variation(m.v1, ax, ay, vx, vy);
                if (m.v2 >= 0) {
                    float wx, wy;
                    variation(m.v2, ax, ay, wx, wy);
                    vx = vx * (1 - m.w) + wx * m.w; vy = vy * (1 - m.w) + wy * m.w;
                }
                x = vx; y = vy; cc = (cc + m.color) * 0.5f;
                if (symmetry > 1 && fast.uniform() < 0.5f) {
                    float ang = 2 * (float)M_PI * fast.below(symmetry) / symmetry, c = std::cos(ang), s = std::sin(ang);
                    float rx = x * c - y * s; y = x * s + y * c; x = rx;
                }
                if (!std::isfinite(x) || !std::isfinite(y) || std::abs(x) > 1e5f || std::abs(y) > 1e5f) {
                    x = fast.uniform() * 2 - 1; y = fast.uniform() * 2 - 1;
                    continue;
                }
                if (i > 20) visit(x, y, cc);
            }
        };

        sketchutil::SquareBounds bounds;
        for (int attempt = 0; attempt < 15; ++attempt) {
            int k = I(2, 4);
            maps.clear();
            for (int i = 0; i < k; ++i) {
                Map m{U(-1, 1), U(-1, 1), U(-1, 1), U(-1, 1), U(-1, 1), U(-1, 1),
                      I(0, kVariations - 1), U(0, 1) < 0.5f ? I(0, kVariations - 1) : -1, U(0.2f, 0.8f),
                      (float)i / std::max(1, k - 1)};
                maps.push_back(m);
            }
            static const int kSym[] = {2, 3, 4, 5, 6};
            symmetry = U(0, 1) < 0.65f ? kSym[I(0, 4)] : 1;
            sketchutil::FastRng fast(rng());
            std::vector<float> xs, ys;
            run(30000, fast, [&](float x, float y, float) { xs.push_back(x); ys.push_back(y); });
            bounds = sketchutil::fitSquare(xs, ys, 0.01f, 0.99f, 0.06f);
            if (sketchutil::occupiedCells(xs, ys, bounds, 80) > 1600) break;
        }

        bounds.apply(ctx);
        auto density = ctx.makeDensity();
        density.prepareConcurrent(true);
        const uint64_t base_seed = rng();
        constexpr int kChunks = 8;  // fixed, so the image doesn't depend on the core count
        const long per_chunk = sketchutil::densityBudget(ctx, 7.0e6, 25000000L) / kChunks;
        sketchutil::parallelChunks(kChunks, [&](int k) {
            sketchutil::FastRng fast(base_seed + 0x9E3779B97F4A7C15ULL * (k + 1));
            run(per_chunk, fast, [&](float x, float y, float cc) { density.addConcurrent({x, y}, cc); });
        });
        density.recomputeMax();
        ctx.drawDensity(density, 0.45f);
    }
};
