#pragma once
#include "../render_context.h"
#include <cstdint>

class SketchBase {
public:
    virtual ~SketchBase() = default;
    virtual void render(RenderContext& ctx, uint64_t seed) = 0;
};
