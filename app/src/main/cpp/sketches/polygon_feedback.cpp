// Polygon Feedback: a polygon grows outward while it turns, and its copies form spiral arms. The
// seed varies the polygon (4-9 sides, a star, or a circle), how fast and which way it turns, how
// many copies there are and how their size grows, a slight drift of the centre (which also turns
// the circle into an off-centre spiral), and the style: outlines, or stacked filled tiles.
#include "sketch_base.h"
#include <cmath>
#include <random>
#include <vector>

class PolygonFeedbackSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        auto U = [&](float a, float b) { return std::uniform_real_distribution<float>(a, b)(rng); };
        auto I = [&](int a, int b) { return std::uniform_int_distribution<int>(a, b)(rng); };

        int choice = I(0, 6);
        bool circle = choice == 6;
        int n_sides = circle ? 300 : 4 + choice;
        bool star = !circle && n_sides >= 5 && U(0, 1) < 0.2f;

        // Outline on the unit circle; a star alternates outer and inner points.
        std::vector<Point2f> unit;
        int verts = star ? n_sides * 2 : n_sides;
        for (int i = 0; i < verts; ++i) {
            float a = 2 * (float)M_PI * i / verts;
            float r = (star && i % 2) ? 0.6f : 1.0f;
            unit.push_back({r * std::cos(a), r * std::sin(a)});
        }

        const int n_pol = I(45, 85);
        const float turn = U(0.55f, 1.5f) * (U(0, 1) < 0.5f ? -1.0f : 1.0f);  // radians per unit size
        const float growth = U(0.8f, 1.5f);        // <1 packs the outer copies, >1 the inner ones
        const float drift = circle ? U(0.08f, 0.22f) : (U(0, 1) < 0.3f ? U(0.03f, 0.1f) : 0.0f);
        const bool filled = U(0, 1) < 0.25f;
        const bool reverse = U(0, 1) < 0.3f;        // colormap from the outside in

        ctx.setWorldBounds(-6, 6, -6, 6);
        const float max_size = 5.0f / (1 + drift);  // keep the drifted outline inside the frame

        // Filled tiles draw largest first so every smaller copy shows on top.
        for (int k = 0; k < n_pol; ++k) {
            int i = filled ? n_pol - 1 - k : k;
            float f = std::pow((float)i / (n_pol - 1), growth);
            float z = max_size * f;
            float a = turn * z;
            float c = std::cos(a), s = std::sin(a);
            float cx = drift * z * std::cos(a * 0.5f), cy = drift * z * std::sin(a * 0.5f);
            std::vector<Point2f> pts;
            for (const auto& p : unit) pts.push_back({cx + z * (p.x * c - p.y * s), cy + z * (p.x * s + p.y * c)});
            SkColor color = ctx.getColor(reverse ? 1 - f : f);
            if (filled) {
                ctx.fillPolygon(pts, color, 1.0f);
                pts.push_back(pts[0]);
                ctx.drawPolyline(pts, ctx.background(), 1.2f, 0.6f);
            } else {
                pts.push_back(pts[0]);
                ctx.drawPolyline(pts, color, 1.6f + 2.2f * f);
            }
        }
    }
};
