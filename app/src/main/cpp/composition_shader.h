#ifndef COMPOSITION_SHADER_H
#define COMPOSITION_SHADER_H

const char* COMPOSITION_SHADER_SOURCE = R"(#version 310 es
layout(local_size_x = 16, local_size_y = 16) in;
layout(rgba8, binding = 0) writeonly uniform highp image2D u_image;

uniform int u_width;
uniform int u_height;

struct Instruction {
    int op;
    float v1;
    float v2;
    float v3;
};

layout(std140, binding = 0) uniform Bytecode {
    Instruction instructions[256];
    int count;
} u_bytecode;

vec3 well(vec3 x) {
    return 1.0 - 2.0 / pow(1.0 + x*x, vec3(8.0));
}

vec3 tent(vec3 x) {
    return 1.0 - 2.0 * abs(x);
}

void main() {
    ivec2 pos = ivec2(gl_GlobalInvocationID.xy);
    if (pos.x >= u_width || pos.y >= u_height) return;

    float x_coord = (float(pos.x) / float(u_width)) * 2.0 - 1.0;
    float y_coord = (float(pos.y) / float(u_height)) * 2.0 - 1.0;

    vec3 stack[64];
    int sp = 0;

    for (int i = 0; i < u_bytecode.count; ++i) {
        int op = u_bytecode.instructions[i].op;
        vec3 p = vec3(u_bytecode.instructions[i].v1, u_bytecode.instructions[i].v2, u_bytecode.instructions[i].v3);

        if (op == 0) { // X
            stack[sp++] = vec3(x_coord);
        } else if (op == 1) { // Y
            stack[sp++] = vec3(y_coord);
        } else if (op == 2) { // Constant
            stack[sp++] = p;
        } else if (op == 3) { // Sum
            vec3 b = stack[--sp];
            vec3 a = stack[--sp];
            stack[sp++] = (a + b) * 0.5;
        } else if (op == 4) { // Product
            vec3 b = stack[--sp];
            vec3 a = stack[--sp];
            stack[sp++] = a * b;
        } else if (op == 5) { // Mod
            vec3 b = stack[--sp];
            vec3 a = stack[--sp];
            vec3 res;
            res.x = (b.x != 0.0) ? mod(a.x, b.x) : 0.0;
            res.y = (b.y != 0.0) ? mod(a.y, b.y) : 0.0;
            res.z = (b.z != 0.0) ? mod(a.z, b.z) : 0.0;
            stack[sp++] = res;
        } else if (op == 6) { // Well
            stack[sp-1] = well(stack[sp-1]);
        } else if (op == 7) { // Tent
            stack[sp-1] = tent(stack[sp-1]);
        } else if (op == 8) { // Sin
            vec3 a = stack[sp-1];
            stack[sp-1] = sin(p.x + p.y * a);
        } else if (op == 9) { // Level
            vec3 e2 = stack[--sp];
            vec3 e1 = stack[--sp];
            vec3 L = stack[--sp];
            vec3 res;
            res.x = (L.x < p.x) ? e1.x : e2.x;
            res.y = (L.y < p.x) ? e1.y : e2.y;
            res.z = (L.z < p.x) ? e1.z : e2.z;
            stack[sp++] = res;
        } else if (op == 10) { // Mix
            vec3 e2 = stack[--sp];
            vec3 e1 = stack[--sp];
            vec3 w = stack[--sp];
            vec3 weight = (w + 1.0) * 0.5;
            stack[sp++] = mix(e2, e1, weight);
        }
    }

    vec3 finalColor = (stack[0] + 1.0) * 0.5;
    finalColor = clamp(finalColor, 0.0, 1.0);
    imageStore(u_image, pos, vec4(finalColor, 1.0));
}
)";

#endif
