#include "sketch_base.h"
#include <random>
#include <vector>

class CubismSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        std::uniform_int_distribution<int> dist_n(10, 50);
        std::uniform_real_distribution<float> dist_coord(-1.0f, 1.0f);
        std::uniform_real_distribution<float> dist_01(0.0f, 1.0f);

        int n = dist_n(rng);
        float alpha = std::max(0.2f, std::min(0.65f, 20.0f / n));

        ctx.setWorldBounds(-1.1f, 1.1f, -1.1f, 1.1f);

        for (int i = 0; i < n; ++i) {
            float x1 = dist_coord(rng), x2 = dist_coord(rng);
            float y1 = dist_coord(rng), y2 = dist_coord(rng);
            float y3 = dist_coord(rng), y4 = dist_coord(rng);

            std::vector<Point2f> pts = {
                {x1, y1}, {x2, y2}, {x2, y4}, {x1, y3}
            };
            ctx.fillPolygon(pts, ctx.getColor(dist_01(rng)), alpha);
        }
    }
};
