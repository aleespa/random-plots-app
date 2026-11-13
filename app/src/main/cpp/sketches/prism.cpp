// Prism: extended Random Art (see app/src/main/python/figures/prism.py for the prototype).
//
// Andrej Bauer's Random Art builds a random formula f(x, y) and evaluates it per pixel. On top of
// the operators the app's GPU compositions already have (Sum, Product, Mod, Well, Tent, Sin, Mix,
// Level), Prism adds what his full generator at random-art.org appears to have:
//   1. Palette colouring: f yields one value per pixel, coloured through the colormap.
//   2. Focal points: distance to a point, to the nearest point, cell edges, rays around a point.
//   3. Coordinate transforms: a sub-formula is evaluated at rotated, mirrored, n-fold
//      kaleidoscope, tiled or polar coordinates (symmetry and repetition).
//   4. Hard edges: discretize, compare/select, min/max.
//   5. Shared sub-formulas: nodes can be reused (the formula is a DAG), so a motif reappears
//      under different transforms.
// Every node evaluates to a value in [-1, 1].
#include "sketch_base.h"
#include <algorithm>
#include <cmath>
#include <random>
#include <thread>
#include <vector>

namespace prism {

constexpr float kPi = 3.14159265358979f;

enum class Op {
    // leaves
    X, Y, Const, FocusDist, Nearest, Edges, Rays,
    // unary value ops
    Well, Tent, Sin, Neg, Steps,
    // coordinate transforms (unary)
    Rotate, Mirror, Kaleido, Torus, Polar,
    // binary / ternary / quaternary
    Sum, Prod, Max, Min, Mod, Mix, IfLess,
};

struct Node {
    Op op;
    int arity = 0;
    int kids[4] = {-1, -1, -1, -1};
    float p[4] = {0, 0, 0, 0};  // op parameters
};

struct Rng {
    std::mt19937_64 g;
    explicit Rng(uint64_t seed) : g(seed) {}
    float uniform(float a, float b) { return std::uniform_real_distribution<float>(a, b)(g); }
    int integers(int lo, int hi_exclusive) {
        return std::uniform_int_distribution<int>(lo, hi_exclusive - 1)(g);
    }
    template <size_t N>
    Op pick(const std::pair<Op, float> (&table)[N]) {
        float total = 0;
        for (const auto& e : table) total += e.second;
        float r = uniform(0, total);
        for (const auto& e : table) {
            if ((r -= e.second) <= 0) return e.first;
        }
        return table[N - 1].first;
    }
};

const std::pair<Op, float> kLeaves[] = {
    {Op::X, 1}, {Op::Y, 1}, {Op::Const, 0.5f}, {Op::FocusDist, 2},
    {Op::Nearest, 1.5f}, {Op::Edges, 1}, {Op::Rays, 1.5f}};
const std::pair<Op, float> kValueOps[] = {
    {Op::Well, 1}, {Op::Tent, 1}, {Op::Sin, 2}, {Op::Neg, 0.5f}, {Op::Steps, 1},
    {Op::Sum, 2}, {Op::Prod, 2}, {Op::Max, 1}, {Op::Min, 1}, {Op::Mod, 0.7f},
    {Op::Mix, 1.5f}, {Op::IfLess, 1}};
const std::pair<Op, float> kTransforms[] = {
    {Op::Rotate, 1}, {Op::Mirror, 1}, {Op::Kaleido, 1.5f}, {Op::Torus, 1}, {Op::Polar, 0.7f}};

constexpr float kReuseProbability = 0.2f;  // chance a child is an already-built node
constexpr int kMaxTransforms = 3;          // transforms compound quickly; keep images readable
constexpr int kMaxAttempts = 25;
constexpr int kPreviewSize = 96;

inline int arityOf(Op op) {
    switch (op) {
        case Op::X: case Op::Y: case Op::Const: case Op::FocusDist:
        case Op::Nearest: case Op::Edges: case Op::Rays:
            return 0;
        case Op::Sum: case Op::Prod: case Op::Max: case Op::Min: case Op::Mod:
            return 2;
        case Op::Mix:
            return 3;
        case Op::IfLess:
            return 4;
        default:
            return 1;  // value ops and transforms
    }
}

class Formula {
public:
    std::vector<Node> nodes;
    std::vector<Point2f> foci;
    int root = -1;

    void build(Rng& rng) {
        int n = rng.integers(2, 6);
        foci.clear();
        for (int i = 0; i < n; ++i) foci.push_back({rng.uniform(-0.8f, 0.8f), rng.uniform(-0.8f, 0.8f)});
        nodes.clear();
        pool_.clear();
        root = build(rng, rng.integers(5, 9), kMaxTransforms);
    }

    float eval(int i, float x, float y) const {
        const Node& n = nodes[i];
        auto k = [&](int j) { return eval(n.kids[j], x, y); };
        switch (n.op) {
            case Op::X: return x;
            case Op::Y: return y;
            case Op::Const: return n.p[0];
            case Op::FocusDist: {
                const Point2f& f = foci[(int)n.p[0]];
                return clip(1 - n.p[1] * std::hypot(x - f.x, y - f.y));
            }
            case Op::Nearest: {
                float d = 1e9f;
                for (const auto& f : foci) d = std::min(d, std::hypot(x - f.x, y - f.y));
                return clip(1 - n.p[0] * d);
            }
            case Op::Edges: {
                float d1 = 1e9f, d2 = 1e9f;
                for (const auto& f : foci) {
                    float d = std::hypot(x - f.x, y - f.y);
                    if (d < d1) { d2 = d1; d1 = d; } else if (d < d2) { d2 = d; }
                }
                return clip(1 - n.p[0] * (d2 - d1));
            }
            case Op::Rays: {
                const Point2f& f = foci[(int)n.p[0]];
                return std::sin(n.p[1] * std::atan2(y - f.y, x - f.x) + n.p[2]);
            }
            case Op::Well: {
                float v = k(0);
                return 1 - 2 / std::pow(1 + v * v, 8.0f);
            }
            case Op::Tent: return 1 - 2 * std::abs(k(0));
            case Op::Sin: return std::sin(n.p[0] + n.p[1] * k(0));
            case Op::Neg: return -k(0);
            case Op::Steps: {
                float levels = n.p[0];
                float v = (k(0) + 1) * 0.5f;
                return std::round(v * levels) / levels * 2 - 1;
            }
            case Op::Rotate: {
                float c = n.p[0], s = n.p[1];
                return eval(n.kids[0], c * x - s * y, s * x + c * y);
            }
            case Op::Mirror: {
                float nx = n.p[0], ny = n.p[1];
                float d = std::max(x * nx + y * ny, 0.0f);  // fold the positive half over
                return eval(n.kids[0], x - 2 * d * nx, y - 2 * d * ny);
            }
            case Op::Kaleido: {
                float sides = n.p[0], cx = n.p[1], cy = n.p[2];
                float r = std::hypot(x - cx, y - cy);
                float sector = 2 * kPi / sides;
                float t = std::atan2(y - cy, x - cx);
                t = t - sector * std::floor(t / sector);
                t = std::abs(t - sector / 2);  // mirror inside each sector
                return eval(n.kids[0], cx + r * std::cos(t), cy + r * std::sin(t));
            }
            case Op::Torus: {
                float s = n.p[0];
                auto wrap = [s](float v) { float w = v * s + 1; return w - 2 * std::floor(w / 2) - 1; };
                return eval(n.kids[0], wrap(x), wrap(y));
            }
            case Op::Polar: {
                float r = std::hypot(x, y) * 2 / std::sqrt(2.0f) - 1;
                float t = std::atan2(y, x) / kPi;
                return eval(n.kids[0], r, t);
            }
            case Op::Sum: return (k(0) + k(1)) * 0.5f;
            case Op::Prod: return k(0) * k(1);
            case Op::Max: return std::max(k(0), k(1));
            case Op::Min: return std::min(k(0), k(1));
            case Op::Mod: {
                float a = k(0), b = k(1);
                if (std::abs(b) <= 1e-3f) return 0;
                return clip(a - b * std::floor(a / b));  // sign follows b, like Python's %
            }
            case Op::Mix: {
                float w = (k(0) + 1) * 0.5f;
                return w * k(1) + (1 - w) * k(2);
            }
            case Op::IfLess: return k(0) < k(1) ? k(2) : k(3);
        }
        return 0;
    }

    /**
     * Evaluates the formula on a width x height grid, using all cores. The grid covers
     * [-1, 1]^2 (y up) mapped through `view`: (u, v) -> (cx + u * zoom, cy + v * zoom).
     */
    std::vector<float> evaluate(int width, int height, const PlaneView& view = {}) const {
        std::vector<float> out((size_t)width * height);
        unsigned threads = std::max(1u, std::min(8u, std::thread::hardware_concurrency()));
        const float cx = (float)view.cx, cy = (float)view.cy, zoom = (float)view.zoom;
        auto work = [&](int row0, int row1) {
            for (int py = row0; py < row1; ++py) {
                float y = cy + zoom * (1 - 2 * (py + 0.5f) / height);
                for (int px = 0; px < width; ++px) {
                    float x = cx + zoom * (-1 + 2 * (px + 0.5f) / width);
                    float v = eval(root, x, y);
                    out[(size_t)py * width + px] = std::isfinite(v) ? clip(v) : 0.0f;
                }
            }
        };
        std::vector<std::thread> pool;
        int chunk = (height + (int)threads - 1) / (int)threads;
        for (unsigned t = 0; t < threads; ++t) {
            int r0 = (int)t * chunk, r1 = std::min(height, r0 + chunk);
            if (r0 < r1) pool.emplace_back(work, r0, r1);
        }
        for (auto& th : pool) th.join();
        return out;
    }

private:
    std::vector<int> pool_;  // finished nodes available for reuse

    static float clip(float v) { return std::clamp(v, -1.0f, 1.0f); }

    int add(Node n) {
        nodes.push_back(n);
        int idx = (int)nodes.size() - 1;
        pool_.push_back(idx);
        return idx;
    }

    void setParams(Rng& rng, Node& n) {
        int nf = (int)foci.size();
        switch (n.op) {
            case Op::Const: n.p[0] = rng.uniform(-1, 1); break;
            case Op::FocusDist: n.p[0] = (float)rng.integers(0, nf); n.p[1] = rng.uniform(0.8f, 2.5f); break;
            case Op::Nearest: n.p[0] = rng.uniform(1.5f, 4.0f); break;
            case Op::Edges: n.p[0] = rng.uniform(3.0f, 8.0f); break;
            case Op::Rays:
                n.p[0] = (float)rng.integers(0, nf);
                n.p[1] = (float)rng.integers(2, 9);
                n.p[2] = rng.uniform(0, kPi);
                break;
            case Op::Sin: n.p[0] = rng.uniform(0, kPi); n.p[1] = rng.uniform(1.0f, 6.0f); break;
            case Op::Steps: n.p[0] = (float)rng.integers(2, 7); break;
            case Op::Rotate: {
                float a = rng.uniform(0, 2 * kPi);
                n.p[0] = std::cos(a); n.p[1] = std::sin(a);
                break;
            }
            case Op::Mirror: {
                float a = rng.uniform(0, kPi);
                n.p[0] = std::cos(a); n.p[1] = std::sin(a);
                break;
            }
            case Op::Kaleido: {
                static const int kSides[] = {3, 4, 5, 6, 8, 12};
                n.p[0] = (float)kSides[rng.integers(0, 6)];
                if (rng.uniform(0, 1) < 0.6f) {
                    n.p[1] = 0; n.p[2] = 0;
                } else {
                    const Point2f& f = foci[rng.integers(0, nf)];
                    n.p[1] = f.x; n.p[2] = f.y;
                }
                break;
            }
            case Op::Torus: n.p[0] = rng.uniform(1.5f, 4.0f); break;
            default: break;
        }
    }

    // Transforms are likelier near the root so they shape large parts of the image.
    int build(Rng& rng, int depth, int transforms_left) {
        if (!pool_.empty() && depth < 4 && rng.uniform(0, 1) < kReuseProbability) {
            return pool_[rng.integers(0, (int)pool_.size())];
        }
        Node n;
        if (depth <= 0) {
            n.op = rng.pick(kLeaves);
        } else if (transforms_left > 0 && rng.uniform(0, 1) < 0.12f + 0.05f * depth) {
            n.op = rng.pick(kTransforms);
            n.arity = 1;
            setParams(rng, n);
            n.kids[0] = build(rng, depth - 1, transforms_left - 1);
            return add(n);
        } else {
            n.op = rng.pick(kValueOps);
        }
        n.arity = arityOf(n.op);
        setParams(rng, n);
        for (int j = 0; j < n.arity; ++j) {
            n.kids[j] = build(rng, depth - 1 - rng.integers(0, 2), transforms_left);
        }
        return add(n);
    }
};

/**
 * Rejects near-flat formulas, ones mostly clipped to one extreme, and ones with only 2-3 distinct
 * tones (stretchContrast would turn those into flat two-colour images).
 */
inline bool isInteresting(const std::vector<float>& v) {
    double sum = 0, sq = 0;
    size_t saturated = 0;
    float lo = v[0], hi = v[0];
    for (float x : v) {
        sum += x; sq += (double)x * x;
        if (std::abs(x) > 0.98f) ++saturated;
        lo = std::min(lo, x); hi = std::max(hi, x);
    }
    double mean = sum / v.size();
    double stddev = std::sqrt(std::max(0.0, sq / v.size() - mean * mean));
    if (stddev <= 0.12 || (double)saturated / v.size() >= 0.55 || hi - lo < 1e-3f) return false;

    constexpr int kBins = 16, kMinBins = 4;
    int counts[kBins] = {0};
    for (float x : v) counts[std::min(kBins - 1, (int)((x - lo) / (hi - lo) * kBins))]++;
    int occupied = 0;
    for (int c : counts) occupied += c * 100 >= (int)v.size();  // bins with >= 1% of pixels
    return occupied >= kMinBins;
}

/** 2nd and 98th percentile of `v`: the value range stretched over the whole palette. */
inline std::pair<float, float> contrastRange(std::vector<float> v) {
    size_t lo_i = v.size() * 2 / 100, hi_i = v.size() * 98 / 100;
    std::nth_element(v.begin(), v.begin() + lo_i, v.end());
    float lo = v[lo_i];
    std::nth_element(v.begin(), v.begin() + hi_i, v.end());
    return {lo, v[hi_i]};
}

/** Maps [lo, hi] onto [-1, 1] (clamped) so the whole palette is used. */
inline void stretchContrast(std::vector<float>& v, std::pair<float, float> range) {
    auto [lo, hi] = range;
    if (hi - lo < 1e-6f) return;
    for (float& x : v) x = std::clamp((x - lo) / (hi - lo) * 2 - 1, -1.0f, 1.0f);
}

constexpr int kRangeSampleSize = 128;

}  // namespace prism

class PrismSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        prism::Formula formula;
        for (int attempt = 0; attempt < prism::kMaxAttempts; ++attempt) {
            // Retries are derived from the seed, so the result is deterministic per seed.
            prism::Rng rng(seed * 0x9E3779B97F4A7C15ULL + (uint64_t)attempt);
            formula.build(rng);
            if (prism::isInteresting(formula.evaluate(prism::kPreviewSize, prism::kPreviewSize))) break;
        }
        // The formula choice and the colour range come from the default (unzoomed) view, so
        // panning and zooming move over one fixed image instead of re-picking or recolouring it.
        auto range = prism::contrastRange(
            formula.evaluate(prism::kRangeSampleSize, prism::kRangeSampleSize));
        std::vector<float> values = formula.evaluate(ctx.width(), ctx.height(), ctx.view());
        prism::stretchContrast(values, range);
        ctx.drawValues(values);
    }
};
