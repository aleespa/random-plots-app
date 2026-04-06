#ifndef FRACTAL_SHADER_H
#define FRACTAL_SHADER_H

const char* COMPUTE_SHADER_SOURCE = R"(#version 310 es
layout(local_size_x = 16, local_size_y = 16) in;
layout(rgba8, binding = 0) writeonly uniform highp image2D u_image;

uniform int u_type; // 0: Mandelbrot, 1: Julia, 2: Tricorn, 3: Multibrot, 4: Newton
uniform int u_width;
uniform int u_height;
uniform int u_maxIterations;
uniform vec2 u_center;
uniform float u_zoom;
uniform vec2 u_juliaC;
uniform float u_params[10];
uniform float u_palT[16];
uniform vec3 u_palRGB[16];
uniform int u_palSize;

vec2 complexMul(vec2 a, vec2 b) {
    return vec2(a.x * b.x - a.y * b.y, a.x * b.y + a.y * b.x);
}

vec2 complexDiv(vec2 a, vec2 b) {
    float denom = b.x * b.x + b.y * b.y;
    return vec2(a.x * b.x + a.y * b.y, a.y * b.x - a.x * b.y) / denom;
}

vec2 complexPow(vec2 z, float n) {
    float r = length(z);
    float theta = atan(z.y, z.x);
    return pow(r, n) * vec2(cos(n * theta), sin(n * theta));
}

vec2 complexSin(vec2 z) {
    // sin(x + iy) = sin(x)cosh(y) + i cos(x)sinh(y)
    return vec2(sin(z.x) * cosh(z.y), cos(z.x) * sinh(z.y));
}

vec2 complexCos(vec2 z) {
    // cos(x + iy) = cos(x)cosh(y) - i sin(x)sinh(y)
    return vec2(cos(z.x) * cosh(z.y), -sin(z.x) * sinh(z.y));
}

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
    if (u_type == 0 || u_type == 2 || u_type == 3) { // Mandelbrot, Tricorn, Multibrot
        x0 = xMin + (xMax - xMin) * float(pos.x) / float(u_width);
        y0 = yMax - (yMax - yMin) * float(pos.y) / float(u_height);
        x = 0.0;
        y = 0.0;
    } else if (u_type == 1) { // Julia
        x = xMin + (xMax - xMin) * float(pos.x) / float(u_width);
        y = yMax - (yMax - yMin) * float(pos.y) / float(u_height);
        x0 = u_juliaC.x;
        y0 = u_juliaC.y;
    } else { // Newton
        x = xMin + (xMax - xMin) * float(pos.x) / float(u_width);
        y = yMax - (yMax - yMin) * float(pos.y) / float(u_height);
    }

    int iter = 0;
    float exponent = (u_type == 3) ? u_params[0] : 2.0;
    if (u_type == 4) { // Newton
        vec2 z = vec2(x, y);
        int funcType = int(u_params[0]);
        for (iter = 0; iter < u_maxIterations; iter++) {
            vec2 p, dp;
            if (funcType == 0) { // z^3 - 1
                vec2 z2 = complexMul(z, z);
                p = complexMul(z2, z) - vec2(1.0, 0.0);
                dp = 3.0 * z2;
            } else if (funcType == 1) { // z^5 - 1
                vec2 z2 = complexMul(z, z);
                vec2 z4 = complexMul(z2, z2);
                p = complexMul(z4, z) - vec2(1.0, 0.0);
                dp = 5.0 * z4;
            } else if (funcType == 2) { // sin(z) - 1
                p = complexSin(z) - vec2(1.0, 0.0);
                dp = complexCos(z);
            } else { // z^6 + z^3 - 1
                vec2 z2 = complexMul(z, z);
                vec2 z3 = complexMul(z2, z);
                vec2 z6 = complexMul(z3, z3);
                p = z6 + z3 - vec2(1.0, 0.0);
                dp = 6.0 * complexMul(z3, z2) + 3.0 * z2;
            }
            vec2 diff = complexDiv(p, dp);
            z -= diff;
            if (length(diff) < 0.0001) break;
        }
    } else {
        while (x*x + y*y <= 4.0 && iter < u_maxIterations) {
            float xtemp;
            if (u_type == 0 || u_type == 1) { // Mandelbrot, Julia
                xtemp = x*x - y*y + x0;
                y = 2.0*x*y + y0;
            } else if (u_type == 2) { // Tricorn
                xtemp = x*x - y*y + x0;
                y = -2.0*x*y + y0;
            } else { // Multibrot
                vec2 z_pow = complexPow(vec2(x, y), exponent);
                xtemp = z_pow.x + x0;
                y = z_pow.y + y0;
            }
            x = xtemp;
            iter++;
        }
    }

    float t;
    if (u_type == 4) {
        t = float(iter) / float(u_maxIterations);
    } else {
        float nu = float(iter);
        if (iter < u_maxIterations) {
            float mag = sqrt(x*x + y*y);
            nu = float(iter) + 1.0 - log(log(mag)) / log(exponent);
        }
        t = (nu < float(u_maxIterations)) ? nu / float(u_maxIterations) : 0.0;
    }
    vec3 color = paletteColor(t);
    imageStore(u_image, pos, vec4(color, 1.0));
}
)";

#endif // FRACTAL_SHADER_H
