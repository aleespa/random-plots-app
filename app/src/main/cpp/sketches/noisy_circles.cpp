// Noisy Circles: concentric circles whose radius is disturbed by noise. The seed picks the noise:
//  - smooth: a few harmonics whose phases drift from ring to ring, so the rings flow into each
//    other like wood grain or contour lines (optionally k-fold symmetric: flowers, mandalas);
//  - spiky: independent jitter at every point (the original look);
//  - mixed: smooth flow with fine grain on top.
// It also varies how the noise grows outward, the ring count, a slight drift of the centre, and
// the style: lines, or stacked filled rings (tree rings).
#include "sketch_base.h"
#include <algorithm>
#include <cmath>
#include <random>
#include <vector>

class NoisyCirclesSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        auto U = [&](float a, float b) { return std::uniform_real_distribution<float>(a, b)(rng); };
        auto I = [&](int a, int b) { return std::uniform_int_distribution<int>(a, b)(rng); };

        float pick = U(0, 1);
        const int mode = pick < 0.5f ? 0 : (pick < 0.75f ? 1 : 2);  // 0 smooth, 1 spiky, 2 mixed
        // Spiky rings blur into a halo, so there can be many; smooth rings need room to show.
        const int rings = mode == 1 ? I(70, 130) : I(18, 55);
        const int n = 360;
        const float amount = mode == 1 ? std::min(2.0f, std::exponential_distribution<float>(1.0f)(rng) + 0.15f)
                                       : U(0.18f, 0.6f);            // noise relative to the radius
        const float grain = mode == 2 ? U(0.02f, 0.08f) : 0.0f;
        const float growth = U(0.3f, 1.5f);         // how the noise grows with the radius
        const int symmetry = (mode != 1 && U(0, 1) < 0.35f) ? I(3, 9) : 1;
        const float drift = U(0, 1) < 0.3f ? U(0.02f, 0.08f) : 0.0f;
        const bool filled = mode != 1 && U(0, 1) < 0.25f;

        // Smooth noise: harmonics of the base angle (multiples of the symmetry), each with an
        // amplitude falling with its order and a phase that drifts from ring to ring.
        struct Harmonic { int k; float amp, phase, speed; };
        std::vector<Harmonic> harmonics;
        const int count = I(3, 7);
        const float decay = U(0.6f, 1.6f);
        float total = 0;
        for (int h = 1; h <= count; ++h) {
            Harmonic hm{h * symmetry * (symmetry == 1 ? I(1, 3) : 1), std::pow((float)h, -decay),
                        U(0, 6.28f), U(0.12f, 0.5f) * (I(0, 1) ? 1.0f : -1.0f)};
            total += hm.amp;
            harmonics.push_back(hm);
        }
        for (auto& hm : harmonics) hm.amp /= total;

        std::uniform_real_distribution<float> jitter(-1, 1);
        std::vector<std::vector<Point2f>> curves(rings);
        float extent = 0.01f;
        for (int i = 0; i < rings; ++i) {
            float f = (float)(i + 1) / rings;
            float z = f;                                             // base radius
            float amp = amount * std::pow(f, growth);
            float cx = drift * std::sin(f * 5.0f), cy = drift * std::cos(f * 4.0f);
            auto& pts = curves[i];
            pts.reserve(n + 1);
            float first_r = 0;
            for (int j = 0; j < n; ++j) {
                float th = 2 * (float)M_PI * j / n;
                float noise;
                if (mode == 1) {
                    noise = 0.5f * (jitter(rng) + 1);                // in [0, 1]: radius only grows
                } else {
                    noise = 0;
                    for (const auto& hm : harmonics) noise += hm.amp * std::cos(hm.k * th + hm.phase + hm.speed * i);
                    noise = 0.5f * (noise + 1);
                    if (grain > 0) noise += grain / amount * jitter(rng);
                }
                float r = z * (1 + amp * noise);
                if (j == 0) first_r = r;
                pts.push_back({cx + r * std::cos(th), cy + r * std::sin(th)});
                extent = std::max({extent, std::fabs(pts.back().x), std::fabs(pts.back().y)});
            }
            float r = first_r;
            pts.push_back({cx + r, cy});                                 // close the loop
        }

        float half = extent * 1.03f;
        ctx.setWorldBounds(-half, half, -half, half);
        if (filled) {
            // Outermost first, so every smaller ring sits on top; thin background cuts between.
            for (int i = rings - 1; i >= 0; --i) {
                float f = (float)(i + 1) / rings;
                ctx.fillPolygon(curves[i], ctx.getColor(f), 1.0f);
                ctx.drawPolyline(curves[i], ctx.background(), 0.9f, 0.5f);
            }
        } else {
            float width = mode == 1 ? 1.1f : 2.2f;
            for (int i = 0; i < rings; ++i) {
                float f = (float)(i + 1) / rings;
                ctx.drawPolyline(curves[i], ctx.getColor(f), width, mode == 1 ? 1.0f : 0.9f);
            }
        }
    }
};
