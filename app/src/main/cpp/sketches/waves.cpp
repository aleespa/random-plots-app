// Waves: a ridgeline landscape. Stacked lines, back to front, each hiding the ones behind it, with
// peaks built from a few random bumps shared by the whole stack (so they read as one terrain) plus
// fine per-line noise. The seed picks the terrain, the line count, the perspective and one of three
// styles: occluding lines, filled colour bands, or bands with a light rim.
#include "sketch_base.h"
#include <algorithm>
#include <cmath>
#include <random>
#include <vector>

class WavesSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        auto U = [&](float a, float b) { return std::uniform_real_distribution<float>(a, b)(rng); };
        auto I = [&](int a, int b) { return std::uniform_int_distribution<int>(a, b)(rng); };

        const int lines = I(26, 64);
        const int style = I(0, 2);              // 0 lines, 1 filled bands, 2 bands with a rim
        const float perspective = U(0.0f, 0.6f);  // rows bunch up towards the back
        const float height = U(0.18f, 0.45f);   // peak height, in frame units
        const bool mirror = U(0, 1) < 0.2f;     // ridges that rise and fall symmetrically

        // Terrain: 1-4 bumps (position, width, weight) along x; each line shifts them a little.
        struct Bump { float x, w, a; };
        std::vector<Bump> bumps(I(1, 4));
        for (auto& b : bumps) b = {U(0.2f, 0.8f), U(0.04f, 0.16f), U(0.4f, 1.0f)};
        const float drift = U(-0.25f, 0.25f);   // bumps slide sideways from back to front

        // Fine detail: value noise per line, a few octaves.
        const float freq = U(10.0f, 40.0f);
        const float rough = U(0.35f, 0.9f);

        const float margin = 0.0f;              // ridges run edge to edge
        const float vmargin = 0.05f;
        ctx.setWorldBounds(0, 1, 0, 1);
        const int n = 260;
        const SkColor bg = ctx.background();

        for (int j = 0; j < lines; ++j) {
            float depth = (float)j / (lines - 1);                 // 0 back (top) .. 1 front (bottom)
            float row = std::pow(depth, 1.0f + perspective);      // perspective spacing
            float base = 1.0f - vmargin - height * 0.6f - row * (1.0f - 2 * vmargin - height * 0.6f);
            float scale = 0.6f + 0.4f * depth;                    // nearer ridges are taller
            std::vector<float> noise = valueNoise(rng, n, freq, rough);

            std::vector<Point2f> ridge(n);
            for (int i = 0; i < n; ++i) {
                float x = (float)i / (n - 1);
                float xs = mirror ? 0.5f - std::fabs(x - 0.5f) + 0.25f : x;
                float env = 0;
                for (const auto& b : bumps) {
                    float d = (xs - b.x - drift * (depth - 0.5f)) / b.w;
                    env += b.a * std::exp(-0.5f * d * d);
                }
                env = std::min(env, 1.4f);
                float y = base + height * scale * env * (0.55f + 0.45f * noise[i])
                          + 0.006f * noise[i];                   // a hint of texture everywhere
                ridge[i] = {margin + x * (1 - 2 * margin), y};
            }

            // Area below the ridge, down past the frame: it hides everything behind.
            std::vector<Point2f> area = ridge;
            area.push_back({ridge.back().x, -0.1f});
            area.push_back({ridge.front().x, -0.1f});

            // Filled styles keep the front band (the largest area) away from the colormap's end.
            SkColor color = ctx.getColor(style == 0 ? depth : 0.08f + 0.8f * depth);
            if (style == 0) {
                ctx.fillPolygon(area, bg);
                ctx.drawPolyline(ridge, color, 1.6f + 0.8f * depth, 1.0f);
            } else {
                // Filled bands: a thin cut in the background colour (paper layers) or a light rim.
                ctx.fillPolygon(area, color, 1.0f);
                if (style == 1) ctx.drawPolyline(ridge, bg, 1.2f, 0.85f);
                else ctx.drawPolyline(ridge, lighten(color), 1.4f, 0.9f);
            }
        }
    }

private:
    /** Smooth noise in [0, 1] over n samples: octaves of cosine-interpolated random values. */
    static std::vector<float> valueNoise(std::mt19937_64& rng, int n, float freq, float rough) {
        std::uniform_real_distribution<float> U(0, 1);
        std::vector<float> out(n, 0);
        float amp = 1, total = 0;
        for (int o = 0; o < 4; ++o) {
            int knots = (int)(freq * std::pow(2.0f, o)) + 2;
            std::vector<float> k(knots);
            for (float& v : k) v = U(rng);
            for (int i = 0; i < n; ++i) {
                float t = (float)i / (n - 1) * (knots - 1);
                int a = std::min((int)t, knots - 2);
                float f = (1 - std::cos((t - a) * (float)M_PI)) / 2;
                out[i] += amp * (k[a] + (k[a + 1] - k[a]) * f);
            }
            total += amp;
            amp *= rough;
        }
        for (float& v : out) v /= total;
        return out;
    }

    static SkColor lighten(SkColor c) {
        auto l = [](unsigned v) { return (unsigned)std::min(255.0f, v + (255 - v) * 0.6f); };
        return SkColorSetARGB(255, l(SkColorGetR(c)), l(SkColorGetG(c)), l(SkColorGetB(c)));
    }
};
