// Random Eigenvalues ("Bohemian" matrices): a small matrix with entries from a tiny alphabet; a
// few entries run around circles, and the eigenvalues of every sample land in a density image
// coloured by where the first moving entry was. Random samples give soft clouds; sweeping that
// entry over a regular set of angles draws families of curves. The seed picks the size, the
// alphabet, how many entries move and how far, and the sampling.
#include "sketch_base.h"
#include "sketch_util.h"
#include <Eigen/Dense>
#include <cmath>
#include <complex>
#include <random>
#include <vector>

class RandomEigenSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        auto U = [&](float a, float b) { return std::uniform_real_distribution<float>(a, b)(rng); };
        auto I = [&](int a, int b) { return std::uniform_int_distribution<int>(a, b)(rng); };

        const long samples = sketchutil::densityBudget(ctx, 70000, 90000);
        for (int attempt = 0; attempt < 6; ++attempt) {
            int n = I(4, 9);
            int alphabet = I(0, 3);
            Eigen::MatrixXcf m(n, n);
            for (int i = 0; i < n; ++i)
                for (int j = 0; j < n; ++j) m(i, j) = entry(alphabet, rng);

            int moving = I(0, 2) == 0 ? 3 : 2;
            struct Move { int i, j; float r; };
            std::vector<Move> moves(moving);
            for (auto& mv : moves) mv = {I(0, n - 1), I(0, n - 1), U(0, 1) < 0.6f ? 1.0f : U(0.5f, 2.0f)};
            const bool grid = U(0, 1) < 0.8f;       // first entry on a regular set of angles
            const int grid_n = I(6, 28);
            const uint64_t base = rng();

            // Each sample's angles come from a hash of (sample, entry), so the image is the same
            // however the work is split between threads.
            std::vector<float> xs((size_t)samples * n), ys(xs.size()), ts(xs.size());
            sketchutil::parallelRows((int)samples, [&](int s0, int s1) {
                Eigen::MatrixXcf mt = m;
                for (int s = s0; s < s1; ++s) {
                    float t0 = 0;
                    for (int k = 0; k < moving; ++k) {
                        float u = unit(base, (uint64_t)s, (uint64_t)k);
                        if (k == 0 && grid) u = (float)((int)(u * grid_n)) / grid_n;
                        if (k == 0) t0 = u;
                        mt(moves[k].i, moves[k].j) = std::polar(moves[k].r, 2 * (float)M_PI * u);
                    }
                    Eigen::ComplexEigenSolver<Eigen::MatrixXcf> solver(mt, false);
                    const auto& ev = solver.eigenvalues();
                    for (int e = 0; e < n; ++e) {
                        size_t o = (size_t)s * n + e;
                        xs[o] = ev[e].real(); ys[o] = ev[e].imag(); ts[o] = t0;
                    }
                }
            });

            auto bounds = sketchutil::fitSquare(xs, ys, 0.004f, 0.996f, 0.05f);
            // Reject matrices whose eigenvalues barely move (a few dots, or one tight blob).
            if (attempt < 5 && sketchutil::occupiedCells(xs, ys, bounds, 48) < 260) continue;
            bounds.apply(ctx);
            auto density = ctx.makeDensity();
            for (size_t o = 0; o < xs.size(); ++o) density.add({xs[o], ys[o]}, ts[o]);
            ctx.drawDensity(density, 0.42f);
            return;
        }
    }

private:
    static std::complex<float> entry(int alphabet, std::mt19937_64& rng) {
        int k = std::uniform_int_distribution<int>(0, 2)(rng);
        int l = std::uniform_int_distribution<int>(0, 2)(rng);
        switch (alphabet) {
            case 0: return {(float)(k - 1), (float)(l - 1)};             // {-1,0,1} + {-1,0,1}i
            case 1: {                                                      // {0, ±1, ±i}
                static const std::complex<float> v[5] = {{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                return v[std::uniform_int_distribution<int>(0, 4)(rng)];
            }
            case 2: return {(float)(k - 1), 0.0f};                        // real {-1,0,1}
            default: return {(float)(k > 0), 0.0f};                       // {0,1}, mostly ones
        }
    }

    /** A uniform number in [0, 1) from a hash of (base, sample, entry). */
    static float unit(uint64_t base, uint64_t s, uint64_t k) {
        uint64_t z = base + s * 0x9E3779B97F4A7C15ULL + k * 0xBF58476D1CE4E5B9ULL;
        z = (z ^ (z >> 30)) * 0xBF58476D1CE4E5B9ULL;
        z = (z ^ (z >> 27)) * 0x94D049BB133111EBULL;
        z ^= z >> 31;
        return (float)(z >> 40) * (1.0f / 16777216.0f);
    }
};
