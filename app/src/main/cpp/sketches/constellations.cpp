#include "sketch_base.h"
#include <random>
#include <cmath>
#include <vector>

class ConstellationsSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        std::uniform_int_distribution<int> dist_n(20, 65);
        std::uniform_real_distribution<float> dist_01(0.0f, 1.0f);
        std::uniform_real_distribution<float> dist_radius(0.15f, 0.3f);

        int n = dist_n(rng);
        std::vector<Point2f> pts(n);
        for (int i = 0; i < n; ++i) {
            pts[i] = {dist_01(rng), dist_01(rng)};
        }

        ctx.setWorldBounds(-0.05f, 1.05f, -0.05f, 1.05f);

        float radius_limit = dist_radius(rng);
        for (int i = 0; i < n; ++i) {
            for (int j = i + 1; j < n; ++j) {
                float dx = pts[i].x - pts[j].x;
                float dy = pts[i].y - pts[j].y;
                float dist = std::sqrt(dx * dx + dy * dy);
                if (dist < radius_limit) {
                    ctx.drawPolyline({pts[i], pts[j]}, ctx.getColor(dist_01(rng)), 1.2f);
                }
            }
        }

        // Points logic simplified
        for (const auto& p : pts) {
            ctx.drawScatter({p}, SK_ColorWHITE, 3.5f);
        }
    }
};
