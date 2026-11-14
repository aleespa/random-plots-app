// Spiral: particles swept through one, two or three vortices, drawn as glowing streamlines. Each
// vortex turns the flow and pulls it in (or pushes it out) at a random pitch, so streamlines wind
// into logarithmic spirals; an optional k-fold wave gathers them into k arms. Several vortices may
// spin in opposite directions. The flow fills the square or a round disc. Colour follows the particle's starting angle (rainbow arms), its
// progress along the trail, or its starting radius.
#include "sketch_base.h"
#include "sketch_util.h"
#include <cmath>
#include <random>
#include <vector>

class SpiralSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        auto U = [&](float a, float b) { return std::uniform_real_distribution<float>(a, b)(rng); };
        auto I = [&](int a, int b) { return std::uniform_int_distribution<int>(a, b)(rng); };

        struct Vortex { float x, y, spin, pitch, core, falloff; };
        float pick = U(0, 1);
        int count = pick < 0.45f ? 1 : (pick < 0.8f ? 2 : 3);
        std::vector<Vortex> vs(count);
        const float sign = U(0, 1) < 0.75f ? 1.0f : -1.0f;      // inflow (sink) or outflow
        for (int k = 0; k < count; ++k) {
            float a = 2 * (float)M_PI * k / count + U(0, 6.28f) * (count > 1 ? 0.3f : 0.0f);
            float d = count == 1 ? 0.0f : U(0.3f, 0.5f);
            vs[k] = {d * std::cos(a), d * std::sin(a),
                     (count > 1 && U(0, 1) < 0.5f) ? -1.0f : 1.0f,
                     sign * U(0.08f, 0.55f),                      // radial/tangential ratio
                     U(0.03f, 0.12f),
                     U(0.5f, 1.2f)};
        }
        const int arms = U(0, 1) < 0.35f ? 0 : I(2, 6);
        const float arm_amp = U(0.25f, 0.8f), arm_twist = U(-3.0f, 3.0f), arm_phase = U(0, 6.28f);
        // 0 starting angle, 1 progress along the trail, 2 arm phase (only with arms).
        const int colour_mode = arms ? I(0, 2) : I(0, 1);

        // Velocity field: sum of vortices, each 1/r^falloff away from its core.
        auto velocity = [&](float x, float y, float& vx, float& vy) {
            vx = vy = 0;
            for (const auto& v : vs) {
                float dx = x - v.x, dy = y - v.y;
                float r2 = dx * dx + dy * dy, r = std::sqrt(r2) + 1e-6f;
                float g = 1.0f / std::pow(r2 + v.core * v.core, 0.5f * v.falloff);
                float radial = -v.pitch;
                if (arms) {
                    float phi = std::atan2(dy, dx);
                    radial *= 1.0f + arm_amp * std::sin(arms * phi + arm_twist * std::log(r) + arm_phase);
                }
                vx += g * (-dy * v.spin + radial * dx);
                vy += g * (dx * v.spin + radial * dy);
            }
        };

        ctx.setWorldBounds(-1, 1, -1, 1);
        auto density = ctx.makeDensity();
        density.prepareConcurrent(true);
        const float scale = ctx.width() / 480.0f;
        const int particles = (int)(1800 * scale), steps = (int)(260 * scale);
        const float h = 1.2f / ctx.width() * 2;                  // ~one pixel per step
        // A round disc glowing on black, or streamlines filling the whole square.
        const bool disc = U(0, 1) < 0.4f;
        const float disc_r = 0.96f;
        const uint64_t base = rng();
        constexpr int kChunks = 8;  // fixed, so the image doesn't depend on the core count
        sketchutil::parallelChunks(kChunks, [&](int chunk) {
            sketchutil::FastRng fast(base + 0x9E3779B97F4A7C15ULL * (chunk + 1));
            for (int p = chunk; p < particles; p += kChunks) {
                float x, y;
                do {
                    x = fast.uniform() * 2.2f - 1.1f; y = fast.uniform() * 2.2f - 1.1f;
                } while (disc && x * x + y * y > disc_r * disc_r);
                float t;
                if (colour_mode == 0) t = (std::atan2(y - vs[0].y, x - vs[0].x) / (float)M_PI + 1) / 2;
                else if (colour_mode == 2) {
                    float r = std::hypot(x - vs[0].x, y - vs[0].y) + 1e-6f;
                    float ph = arms * std::atan2(y - vs[0].y, x - vs[0].x) + arm_twist * std::log(r) + arm_phase;
                    t = 0.5f + 0.5f * std::sin(ph);
                } else t = 0;
                for (int s = 0; s < steps; ++s) {
                    float vx, vy;
                    velocity(x, y, vx, vy);
                    float speed = std::sqrt(vx * vx + vy * vy) + 1e-9f;
                    x += vx / speed * h; y += vy / speed * h;     // fixed arc length: even lines
                    if (x < -1 || x >= 1 || y < -1 || y >= 1) break;
                    if (disc && x * x + y * y > disc_r * disc_r) break;
                    bool captured = false;
                    for (const auto& v : vs)
                        if ((x - v.x) * (x - v.x) + (y - v.y) * (y - v.y) < v.core * v.core * 0.15f) captured = true;
                    if (captured) break;
                    density.addConcurrent({x, y}, colour_mode == 1 ? (float)s / steps : t);
                }
            }
        });
        density.recomputeMax();
        ctx.drawDensity(density, 0.5f);
    }
};
