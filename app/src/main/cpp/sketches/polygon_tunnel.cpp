// Polygon Tunnel: a perspective tunnel of polygons. Rings recede geometrically towards a vanishing
// point; the tunnel's axis bends and each ring twists with depth. The seed picks the polygon (3-10
// sides, a star or a circle), the bend, the twist, and a style: outlines fading into the distance,
// filled colour bands, or a wireframe with ribs joining the rings.
#include "sketch_base.h"
#include <cmath>
#include <random>
#include <vector>

class PolygonTunnelSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        auto U = [&](float a, float b) { return std::uniform_real_distribution<float>(a, b)(rng); };
        auto I = [&](int a, int b) { return std::uniform_int_distribution<int>(a, b)(rng); };

        // Polygon outline in the ring's own plane (unit radius).
        int sides = I(3, 10);
        const float shape = U(0, 1);
        bool circle = shape < 0.15f;
        bool star = !circle && sides >= 5 && shape > 0.8f;
        if (circle) sides = 120;
        std::vector<Point2f> outline;
        int verts = star ? sides * 2 : sides;
        for (int i = 0; i < verts; ++i) {
            float a = 2 * (float)M_PI * i / verts + (float)M_PI / 2;
            float r = (star && i % 2) ? 0.55f : 1.0f;
            outline.push_back({r * std::cos(a), r * std::sin(a)});
        }

        const int rings = I(34, 80);
        const float ratio = U(1.045f, 1.10f);    // depth step between rings
        const float twist = U(-0.35f, 0.35f) * (I(0, 3) == 0 ? 0.0f : 1.0f);  // radians per unit depth
        const float bend = U(0.0f, 0.9f);        // axis sway
        const float bend_f = U(0.15f, 0.6f), bend_p = U(0, 6.28f), bend_q = U(0, 6.28f);
        const int style = I(0, 2);
        const float near = U(0.55f, 0.8f);       // nearest ring overflows the frame a little

        ctx.setWorldBounds(-1, 1, -1, 1);
        const SkColor bg = ctx.background();

        // Ring i at depth z: projected scale 1/z, centre offset by the bent axis (also /z).
        std::vector<std::vector<Point2f>> proj(rings);
        std::vector<float> depth(rings);
        for (int i = 0; i < rings; ++i) {
            float z = near * std::pow(ratio, (float)i);
            depth[i] = (float)i / (rings - 1);
            // Sway and twist follow the ring index (z grows ~1000x down the tunnel): the axis bends
            // smoothly and the far end settles on a vanishing point instead of scattering.
            float u = depth[i];
            float reach = 1.0f - near / z;                       // 0 at the mouth, ~1 far away
            float ox = bend * std::sin(bend_f * 10 * u + bend_p) * reach * z;
            float oy = bend * 0.7f * std::cos(bend_f * 8 * u + bend_q) * reach * z;
            float rot = twist * 12 * u;
            float c = std::cos(rot), s = std::sin(rot);
            for (const auto& p : outline) {
                float x = p.x * c - p.y * s + ox, y = p.x * s + p.y * c + oy;
                proj[i].push_back({x / z, y / z});
            }
        }

        auto closed = [](std::vector<Point2f> pts) { pts.push_back(pts[0]); return pts; };
        if (style == 1) {
            // Filled bands, nearest (largest) first so each smaller ring sits on top.
            for (int i = 0; i < rings; ++i) {
                SkColor col = (i % 2 == 0) ? ctx.getColor(depth[i]) : blend(ctx.getColor(depth[i]), bg, 0.55f);
                ctx.fillPolygon(proj[i], col, 1.0f);
            }
            std::vector<Point2f> hole = proj.back();
            ctx.fillPolygon(hole, bg, 1.0f);
        } else {
            // Far to near so near lines draw on top; width and opacity fade with distance.
            if (style == 2) {
                for (size_t v = 0; v < outline.size(); v += (circle ? 10 : 1)) {
                    std::vector<Point2f> rib;
                    for (int i = 0; i < rings; ++i) rib.push_back(proj[i][v]);
                    ctx.drawPolyline(rib, ctx.getColor(0.6f), 0.9f, 0.45f);
                }
            }
            for (int i = rings - 1; i >= 0; --i) {
                float fade = 1.0f - 0.85f * depth[i];
                float width = (style == 2 ? 1.4f : 3.2f) * (1.0f - 0.8f * depth[i]) + 0.4f;
                // Bright end of the colormap at the mouth, darker into the distance.
                ctx.drawPolyline(closed(proj[i]), ctx.getColor(1.0f - 0.85f * depth[i]), width, fade);
            }
        }
    }

private:
    static SkColor blend(SkColor a, SkColor b, float t) {
        auto m = [t](unsigned x, unsigned y) { return (unsigned)(x + (y - (float)x) * t); };
        return SkColorSetARGB(255, m(SkColorGetR(a), SkColorGetR(b)), m(SkColorGetG(a), SkColorGetG(b)),
                              m(SkColorGetB(a), SkColorGetB(b)));
    }
};
