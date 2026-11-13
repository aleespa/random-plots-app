// Voronoi Mosaic: cells around random or clustered sites, with background-coloured borders
// between them. Lloyd relaxation (unclustered only) evens cells out into stained glass; each cell
// is shaded darker away from its site.
#include "sketch_base.h"
#include "sketch_util.h"
#include <cmath>
#include <random>
#include <vector>

class VoronoiSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        auto U = [&](float a, float b) { return std::uniform_real_distribution<float>(a, b)(rng); };
        auto I = [&](int a, int b) { return std::uniform_int_distribution<int>(a, b)(rng); };
        const int W = ctx.width(), H = ctx.height();
        const float S = (float)std::min(W, H);

        const int n = I(35, 120);
        const bool clustered = U(0, 1) < 0.5f;
        std::vector<Point2f> centers(I(2, 4));
        for (auto& c : centers) c = {U(0.2f, 0.8f) * W, U(0.2f, 0.8f) * H};
        std::vector<Point2f> sites;
        for (int i = 0; i < n; ++i) {
            if (clustered && U(0, 1) < 0.7f) {
                const Point2f& c = centers[I(0, (int)centers.size() - 1)];
                float r = std::abs(U(-1, 1) * U(0, 1)) * S * 0.35f, a = U(0, 2 * (float)M_PI);
                sites.push_back({std::clamp(c.x + r * std::cos(a), 0.0f, (float)W),
                                 std::clamp(c.y + r * std::sin(a), 0.0f, (float)H)});
            } else {
                sites.push_back({U(0, (float)W), U(0, (float)H)});
            }
        }

        auto nearest = [&](float x, float y) {
            int best = 0; float bd = 1e30f;
            for (int k = 0; k < n; ++k) {
                float d = (x - sites[k].x) * (x - sites[k].x) + (y - sites[k].y) * (y - sites[k].y);
                if (d < bd) { bd = d; best = k; }
            }
            return best;
        };
        const int lloyd = clustered ? 0 : I(1, 3), G = 96;
        for (int it = 0; it < lloyd; ++it) {
            std::vector<double> sx(n), sy(n), cnt(n);
            for (int gy = 0; gy < G; ++gy) {
                for (int gx = 0; gx < G; ++gx) {
                    float x = (gx + 0.5f) * W / G, y = (gy + 0.5f) * H / G;
                    int k = nearest(x, y);
                    sx[k] += x; sy[k] += y; cnt[k] += 1;
                }
            }
            for (int k = 0; k < n; ++k) if (cnt[k] > 0) sites[k] = {(float)(sx[k] / cnt[k]), (float)(sy[k] / cnt[k])};
        }

        float ang = U(0, 2 * (float)M_PI), gx = std::cos(ang), gy = std::sin(ang);
        std::vector<float> tones(n);
        for (int k = 0; k < n; ++k) {
            float grad = 0.5f + (sites[k].x / W - 0.5f) * gx + (sites[k].y / H - 0.5f) * gy;
            tones[k] = 0.1f + 0.9f * std::clamp(0.6f * grad + 0.4f * U(0, 1), 0.0f, 1.0f);
        }
        const float cell_scale = S / std::sqrt((float)n), border = U(1.2f, 3.0f) * S / 480.0f;

        std::vector<FieldPixel> pixels((size_t)W * H);  // default alpha 0 = border, background shows
        sketchutil::parallelRows(H, [&](int r0, int r1) {
            for (int y = r0; y < r1; ++y) {
                for (int x = 0; x < W; ++x) {
                    float b1 = 1e30f, b2 = 1e30f; int k1 = 0;
                    for (int k = 0; k < n; ++k) {
                        float d = (x - sites[k].x) * (x - sites[k].x) + (y - sites[k].y) * (y - sites[k].y);
                        if (d < b1) { b2 = b1; b1 = d; k1 = k; } else if (d < b2) { b2 = d; }
                    }
                    float d1 = std::sqrt(b1);
                    if (std::sqrt(b2) - d1 < border) continue;
                    float shade = 1 - 0.4f * std::min(1.0f, d1 / cell_scale);
                    pixels[(size_t)y * W + x] = {tones[k1], shade, 1.0f};
                }
            }
        });
        ctx.drawField(pixels);
    }
};
