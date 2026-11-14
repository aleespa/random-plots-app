// Rotations: polygons turned through a sweep. Two families, picked by the seed:
//  - Rings: concentric rings of rotated copies of a polygon (or star), each copy slightly smaller,
//    with a twist that grows with the radius, so the rings form moiré bands and spiral arms.
//  - Whirl: the classic pursuit polygons; each polygon's vertices sit a fraction of the way along
//    the previous one's edges, which turns nested polygons into logarithmic spirals. Squares may
//    tile a grid with alternating spin.
#include "sketch_base.h"
#include <cmath>
#include <random>
#include <vector>

class RotationsSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        ctx.setWorldBounds(-1.08f, 1.08f, -1.08f, 1.08f);
        if (std::uniform_real_distribution<float>(0, 1)(rng) < 0.55f) rings(ctx, rng);
        else whirl(ctx, rng);
    }

private:
    using Rng = std::mt19937_64;
    static float U(Rng& r, float a, float b) { return std::uniform_real_distribution<float>(a, b)(r); }
    static int I(Rng& r, int a, int b) { return std::uniform_int_distribution<int>(a, b)(r); }

    /** Vertices of an n-gon, or of the star {n/2} when star is set, on the unit circle. */
    static std::vector<Point2f> polygon(int n, bool star) {
        std::vector<Point2f> pts;
        int step = star ? 2 : 1;
        for (int i = 0; i <= n; ++i) {
            float a = 2 * (float)M_PI * (i * step % n) / n;
            pts.push_back({std::cos(a), std::sin(a)});
        }
        return pts;
    }

    void rings(RenderContext& ctx, Rng& rng) {
        int sides = I(rng, 3, 9);
        bool star = sides >= 5 && U(rng, 0, 1) < 0.35f;
        int rings = I(rng, 3, 9);
        int copies = I(rng, 18, 48);
        float gamma = U(rng, 0.6f, 1.4f);                       // ring spacing
        float sweep = 2 * (float)M_PI / sides * (U(rng, 0, 1) < 0.5f ? 1.0f : U(rng, 0.3f, 0.9f));
        float twist = U(rng, -1.6f, 1.6f);                      // extra turn per unit radius
        float shrink = U(rng, 0.0f, 0.45f);                     // copies get smaller through the sweep
        bool by_copy = U(rng, 0, 1) < 0.6f;                     // colour along the sweep or by ring
        std::vector<Point2f> base = polygon(sides, star);

        for (int j = 0; j < rings; ++j) {
            float r = std::pow((j + 1.0f) / rings, gamma);
            for (int c = 0; c < copies; ++c) {
                float f = (float)c / copies;
                float a = sweep * f + twist * r;
                float s = r * (1 - shrink * f);
                float ca = std::cos(a), sa = std::sin(a);
                std::vector<Point2f> pts;
                for (const auto& p : base) pts.push_back({s * (p.x * ca - p.y * sa), s * (p.x * sa + p.y * ca)});
                float t = by_copy ? f : (float)j / std::max(1, rings - 1);
                ctx.drawPolyline(pts, ctx.getColor(t), 0.7f + 0.9f * r, 0.75f);
            }
        }
    }

    void whirl(RenderContext& ctx, Rng& rng) {
        int sides = std::vector<int>{3, 4, 4, 5, 6, 6, 8}[I(rng, 0, 6)];
        float f = U(rng, 0.03f, 0.11f);                          // pursuit step along each edge
        int levels = (int)(std::log(0.02f) / std::log(1 - 1.6f * f)) + 1;  // until it nearly vanishes
        bool fill = U(rng, 0, 1) < 0.35f;                       // alternate filled bands
        int tiles = (sides == 4 && U(rng, 0, 1) < 0.5f) ? I(rng, 2, 3) : 1;
        float cell = 2.0f / tiles;

        for (int ty = 0; ty < tiles; ++ty) {
            for (int tx = 0; tx < tiles; ++tx) {
                Point2f centre = {-1 + cell * (tx + 0.5f), -1 + cell * (ty + 0.5f)};
                bool flip = (tx + ty) % 2 == 1;                  // neighbours spin the other way
                float radius = tiles == 1 ? 1.0f : cell / 2 * std::sqrt(2.0f);
                std::vector<Point2f> poly;
                for (int i = 0; i < sides; ++i) {
                    float a = 2 * (float)M_PI * i / sides + (float)M_PI / sides + (sides == 4 ? 0 : (float)M_PI / 2);
                    poly.push_back({centre.x + radius * std::cos(a), centre.y + radius * std::sin(a)});
                }
                for (int l = 0; l < levels; ++l) {
                    float t = (float)l / levels;
                    std::vector<Point2f> loop = poly;
                    loop.push_back(poly[0]);
                    if (fill && l % 2 == 0) ctx.fillPolygon(poly, ctx.getColor(t), 0.9f);
                    ctx.drawPolyline(loop, ctx.getColor(t), 1.1f, 0.9f);
                    std::vector<Point2f> next(sides);
                    for (int i = 0; i < sides; ++i) {
                        const Point2f& a = poly[i];
                        const Point2f& b = poly[(flip ? i + sides - 1 : i + 1) % sides];
                        next[i] = {a.x + f * (b.x - a.x), a.y + f * (b.y - a.y)};
                    }
                    poly = next;
                }
            }
        }
    }
};
