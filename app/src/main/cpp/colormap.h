#pragma once
#include "include/core/SkColor.h"
#include <vector>
#include <algorithm>
#include <cstdint>

struct ColorStop { float t; uint8_t r, g, b; };

inline SkColor sampleColormap(const std::vector<ColorStop>& stops, float t) {
    t = std::clamp(t, 0.0f, 1.0f);
    for (size_t i = 1; i < stops.size(); ++i) {
        if (t <= stops[i].t) {
            float local = (t - stops[i-1].t) / (stops[i].t - stops[i-1].t);
            auto lerp = [](uint8_t a, uint8_t b, float f) -> uint8_t {
                return static_cast<uint8_t>(a + (b - a) * f);
            };
            return SkColorSetRGB(
                lerp(stops[i-1].r, stops[i].r, local),
                lerp(stops[i-1].g, stops[i].g, local),
                lerp(stops[i-1].b, stops[i].b, local)
            );
        }
    }
    return SkColorSetRGB(stops.back().r, stops.back().g, stops.back().b);
}

inline std::vector<ColorStop> viridis() {
    return {
        {0.00f,  68,   1,  84},
        {0.25f,  59,  82, 139},
        {0.50f,  33, 145, 140},
        {0.75f,  94, 201,  98},
        {1.00f, 253, 231,  37},
    };
}
