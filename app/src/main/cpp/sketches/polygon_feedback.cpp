#include "sketch_base.h"
#include <random>
#include <cmath>
#include <vector>

class PolygonFeedbackSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        std::uniform_int_distribution<int> dist_sides_choice(0, 6);

        int n_sides;
        int choice = dist_sides_choice(rng);
        if (choice < 6) n_sides = 4 + choice;
        else n_sides = 300; // circle

        std::vector<float> sides(n_sides);
        for (int i = 0; i < n_sides; ++i) sides[i] = 2.0f * M_PI * i / n_sides;

        int n_pol = 60;
        ctx.setWorldBounds(-6, 6, -6, 6);

        for (int i = 0; i < n_pol; ++i) {
            float z = 5.0f * i / (n_pol - 1);
            std::vector<Point2f> pts;
            for (float s : sides) {
                pts.push_back({ std::cos(s + z) * z, std::sin(s + z) * z });
            }
            pts.push_back(pts[0]);
            ctx.drawPolyline(pts, ctx.getColor(z / 5.0f), 3.0f);
        }
    }
};
