// Random Circles: a packing of non-overlapping circles (random positions, each grown up to its
// free space), outlined in colormap colours, sometimes with nested rings.
#include "sketch_base.h"
#include <algorithm>
#include <cmath>
#include <random>
#include <vector>

class RandomCirclesSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        auto U = [&](float a, float b) { return std::uniform_real_distribution<float>(a, b)(rng); };
        ctx.setWorldBounds(0, 1, 0, 1);

        struct Circle { float x, y, r; };
        std::vector<Circle> circles;
        const float r_max = U(0.08f, 0.22f), r_min = 0.006f, gap = U(0.003f, 0.01f), margin = 0.012f;
        const bool nested = U(0, 1) < 0.6f;
        for (int attempt = 0; attempt < 9000 && circles.size() < 700; ++attempt) {
            float x = U(0, 1), y = U(0, 1);
            float room = std::min({x, y, 1 - x, 1 - y}) - margin;
            for (const auto& c : circles) {
                room = std::min(room, std::hypot(x - c.x, y - c.y) - c.r - gap);
                if (room < r_min) break;
            }
            if (room < r_min) continue;
            circles.push_back({x, y, std::min(room, U(r_min, r_max))});
        }

        for (const auto& c : circles) {
            SkColor color = ctx.getColor(0.15f + 0.85f * U(0, 1));
            ctx.drawCircle({c.x, c.y}, c.r, color, 1.5f, 0.9f);
            if (nested && c.r > 0.03f) {
                for (float r = c.r * 0.7f; r > 0.008f; r *= 0.7f) ctx.drawCircle({c.x, c.y}, r, color, 1.5f, 0.45f);
            }
        }
    }
};
