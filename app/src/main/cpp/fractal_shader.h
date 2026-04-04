#ifndef FRACTAL_SHADER_H
#define FRACTAL_SHADER_H

const char* COMPUTE_SHADER_SOURCE = R"(#version 310 es
layout(local_size_x = 16, local_size_y = 16) in;
layout(rgba8, binding = 0) writeonly uniform highp image2D u_image;

uniform int u_type; // 0: Mandelbrot, 1: Julia
uniform int u_width;
uniform int u_height;
uniform int u_maxIterations;
uniform vec2 u_center;
uniform float u_zoom;
uniform vec2 u_juliaC;
uniform float u_palT[16];
uniform vec3 u_palRGB[16];
uniform int u_palSize;

vec3 paletteColor(float t) {
    t = clamp(t, 0.0, 1.0);
    int i = 0;
    for (int k = 0; k < u_palSize - 1; ++k) {
        if (u_palT[k] <= t) i = k;
    }
    int j = min(i + 1, u_palSize - 1);
    float span = u_palT[j] - u_palT[i];
    float frac = (span > 0.0) ? (t - u_palT[i]) / span : 0.0;
    return mix(u_palRGB[i], u_palRGB[j], frac);
}

void main() {
    ivec2 pos = ivec2(gl_GlobalInvocationID.xy);
    if (pos.x >= u_width || pos.y >= u_height) return;

    float aspectRatio = float(u_width) / float(u_height);
    float xMin = u_center.x - u_zoom;
    float xMax = u_center.x + u_zoom;
    float yMin = u_center.y - u_zoom / aspectRatio;
    float yMax = u_center.y + u_zoom / aspectRatio;

    float x0, y0, x, y;
    if (u_type == 0) { // Mandelbrot
        x0 = xMin + (xMax - xMin) * float(pos.x) / float(u_width);
        y0 = yMax - (yMax - yMin) * float(pos.y) / float(u_height);
        x = 0.0;
        y = 0.0;
    } else { // Julia
        x = xMin + (xMax - xMin) * float(pos.x) / float(u_width);
        y = yMax - (yMax - yMin) * float(pos.y) / float(u_height);
        x0 = u_juliaC.x;
        y0 = u_juliaC.y;
    }

    int iter = 0;
    while (x*x + y*y <= 4.0 && iter < u_maxIterations) {
        float xtemp = x*x - y*y + x0;
        y = 2.0*x*y + y0;
        x = xtemp;
        iter++;
    }

    float nu = float(iter);
    if (iter < u_maxIterations) {
        float mag = sqrt(x*x + y*y);
        nu = float(iter) + 1.0 - log(log(mag)) / log(2.0);
    }

    float t = (nu < float(u_maxIterations)) ? nu / float(u_maxIterations) : 0.0;
    vec3 color = paletteColor(t);
    imageStore(u_image, pos, vec4(color, 1.0));
}
)";

#endif // FRACTAL_SHADER_H
