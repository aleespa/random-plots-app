#pragma once
// Small helpers shared by sketches. Header-only and inline: sketch .cpp files are both compiled by
// the CMake glob and #included by jni_bridge.cpp.
#include "../render_context.h"
#include <algorithm>
#include <cmath>
#include <cstdint>
#include <thread>
#include <unordered_set>
#include <vector>

namespace sketchutil {

/** Fast xorshift64* generator for hot loops (millions of draws); seed it from the sketch's mt19937. */
struct FastRng {
    uint64_t s;
    explicit FastRng(uint64_t seed) : s(seed ? seed : 0x9E3779B97F4A7C15ULL) {}
    uint64_t next() {
        s ^= s >> 12; s ^= s << 25; s ^= s >> 27;
        return s * 0x2545F4914F6CDD1DULL;
    }
    float uniform() { return (float)(next() >> 40) * (1.0f / 16777216.0f); }  // [0, 1)
    int below(int n) { return (int)(uniform() * (float)n); }
};

/** Square world window centred on a point cloud (robust to outliers via percentiles). */
struct SquareBounds {
    float cx = 0, cy = 0, half = 1;
    void apply(RenderContext& ctx) const { ctx.setWorldBounds(cx - half, cx + half, cy - half, cy + half); }
};

inline SquareBounds fitSquare(std::vector<float> xs, std::vector<float> ys, float lo, float hi, float pad) {
    SquareBounds b;
    if (xs.empty()) return b;
    auto q = [](std::vector<float>& v, float f) {
        size_t i = std::min(v.size() - 1, (size_t)(f * (float)v.size()));
        std::nth_element(v.begin(), v.begin() + i, v.end());
        return v[i];
    };
    float x0 = q(xs, lo), x1 = q(xs, hi), y0 = q(ys, lo), y1 = q(ys, hi);
    float span = std::max(x1 - x0, y1 - y0) * (1 + 2 * pad);
    b.cx = (x0 + x1) / 2; b.cy = (y0 + y1) / 2; b.half = span > 0 ? span / 2 : 1;
    return b;
}

/** How many cells of a grid x grid raster over `b` the points touch: rejects collapsed attractors. */
inline int occupiedCells(const std::vector<float>& xs, const std::vector<float>& ys, const SquareBounds& b, int grid) {
    std::unordered_set<int> cells;
    for (size_t i = 0; i < xs.size(); ++i) {
        int gx = (int)((xs[i] - (b.cx - b.half)) / (2 * b.half) * grid);
        int gy = (int)((ys[i] - (b.cy - b.half)) / (2 * b.half) * grid);
        if (gx >= 0 && gy >= 0 && gx < grid && gy < grid) cells.insert(gy * grid + gx);
    }
    return (int)cells.size();
}

/** Runs fn(row_begin, row_end) over [0, rows) on up to 8 threads. */
template <class F>
void parallelRows(int rows, F fn) {
    unsigned threads = std::max(1u, std::min(8u, std::thread::hardware_concurrency()));
    int chunk = (rows + (int)threads - 1) / (int)threads;
    std::vector<std::thread> pool;
    for (unsigned t = 0; t < threads; ++t) {
        int r0 = (int)t * chunk, r1 = std::min(rows, r0 + chunk);
        if (r0 < r1) pool.emplace_back(fn, r0, r1);
    }
    for (auto& th : pool) th.join();
}

/** Point budget for density sketches: keeps hits per pixel constant across output sizes. */
inline long densityBudget(const RenderContext& ctx, double per_megapixel, long cap) {
    return std::min(cap, (long)(per_megapixel * ctx.width() * (double)ctx.height() / 1e6));
}

inline uint32_t argb(SkColor c, float shade) {
    auto ch = [shade](U8CPU v) { return (uint32_t)std::clamp(std::lround(v * shade), 0L, 255L); };
    return 0xFF000000u | (ch(SkColorGetR(c)) << 16) | (ch(SkColorGetG(c)) << 8) | ch(SkColorGetB(c));
}

}  // namespace sketchutil
