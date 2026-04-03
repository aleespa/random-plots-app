#include <jni.h>
#include <GLES3/gl31.h>
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <android/log.h>
#include <vector>
#include <string>
#include <cmath>

#define LOG_TAG "FractalRenderer"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

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

GLuint compileShader(GLenum type, const char* source) {
    GLuint shader = glCreateShader(type);
    glShaderSource(shader, 1, &source, nullptr);
    glCompileShader(shader);
    GLint compiled;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
    if (!compiled) {
        GLint infoLen = 0;
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
        if (infoLen > 0) {
            char* infoLog = (char*)malloc(infoLen);
            glGetShaderInfoLog(shader, infoLen, nullptr, infoLog);
            LOGE("Error compiling shader:\n%s\n", infoLog);
            free(infoLog);
        }
        glDeleteShader(shader);
        return 0;
    }
    return shader;
}

struct EGLContextManager {
    EGLDisplay display = EGL_NO_DISPLAY;
    EGLContext context = EGL_NO_CONTEXT;
    EGLSurface surface = EGL_NO_SURFACE;

    bool init() {
        display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
        eglInitialize(display, nullptr, nullptr);
        EGLint configAttribs[] = {
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL_NONE
        };
        EGLConfig config;
        EGLint numConfigs;
        eglChooseConfig(display, configAttribs, &config, 1, &numConfigs);
        EGLint contextAttribs[] = {
            EGL_CONTEXT_CLIENT_VERSION, 3,
            EGL_NONE
        };
        context = eglCreateContext(display, config, EGL_NO_CONTEXT, contextAttribs);
        surface = eglCreatePbufferSurface(display, config, nullptr);
        return eglMakeCurrent(display, surface, surface, context);
    }

    void destroy() {
        if (display != EGL_NO_DISPLAY) {
            eglMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
            eglDestroyContext(display, context);
            eglDestroySurface(display, surface);
            eglTerminate(display);
        }
    }
};

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_aleespa_randomsquare_tools_FractalRenderer_renderInternal(
    JNIEnv* env, jobject thiz, jint type, jint width, jint height, jint maxIter,
    jdouble xCenter, jdouble yCenter, jdouble zoom, jdouble cx, jdouble cy,
    jfloatArray palT, jfloatArray palRGB) {

    EGLContextManager egl;
    if (!egl.init()) {
        LOGE("Failed to init EGL");
        return nullptr;
    }

    GLuint computeShader = compileShader(GL_COMPUTE_SHADER, COMPUTE_SHADER_SOURCE);
    GLuint program = glCreateProgram();
    glAttachShader(program, computeShader);
    glLinkProgram(program);
    glUseProgram(program);

    GLuint texture;
    glGenTextures(1, &texture);
    glBindTexture(GL_TEXTURE_2D, texture);
    glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGBA8, width, height);
    glBindImageTexture(0, texture, 0, GL_FALSE, 0, GL_WRITE_ONLY, GL_RGBA8);

    glUniform1i(glGetUniformLocation(program, "u_type"), type);
    glUniform1i(glGetUniformLocation(program, "u_width"), width);
    glUniform1i(glGetUniformLocation(program, "u_height"), height);
    glUniform1i(glGetUniformLocation(program, "u_maxIterations"), maxIter);
    glUniform2f(glGetUniformLocation(program, "u_center"), (float)xCenter, (float)yCenter);
    glUniform1f(glGetUniformLocation(program, "u_zoom"), (float)zoom);
    glUniform2f(glGetUniformLocation(program, "u_juliaC"), (float)cx, (float)cy);

    jsize palSize = env->GetArrayLength(palT);
    float* pT = env->GetFloatArrayElements(palT, nullptr);
    float* pRGB = env->GetFloatArrayElements(palRGB, nullptr);
    glUniform1fv(glGetUniformLocation(program, "u_palT"), palSize, pT);
    glUniform3fv(glGetUniformLocation(program, "u_palRGB"), palSize, pRGB);
    glUniform1i(glGetUniformLocation(program, "u_palSize"), palSize);

    glDispatchCompute((width + 15) / 16, (height + 15) / 16, 1);
    glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

    std::vector<uint8_t> pixels(width * height * 4);
    GLuint fbo;
    glGenFramebuffers(1, &fbo);
    glBindFramebuffer(GL_FRAMEBUFFER, fbo);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);
    glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixels.data());

    jbyteArray result = env->NewByteArray(pixels.size());
    env->SetByteArrayRegion(result, 0, pixels.size(), (const jbyte*)pixels.data());

    env->ReleaseFloatArrayElements(palT, pT, JNI_ABORT);
    env->ReleaseFloatArrayElements(palRGB, pRGB, JNI_ABORT);
    glDeleteTextures(1, &texture);
    glDeleteFramebuffers(1, &fbo);
    glDeleteProgram(program);
    glDeleteShader(computeShader);
    egl.destroy();

    return result;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_aleespa_randomsquare_tools_FractalRenderer_renderMandelbrot(
    JNIEnv* env, jobject thiz, jint width, jint height, jint maxIter,
    jdouble xCenter, jdouble yCenter, jdouble zoom,
    jfloatArray palT, jfloatArray palRGB) {
    return Java_com_aleespa_randomsquare_tools_FractalRenderer_renderInternal(
        env, thiz, 0, width, height, maxIter, xCenter, yCenter, zoom, 0.0, 0.0, palT, palRGB);
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_aleespa_randomsquare_tools_FractalRenderer_renderJulia(
    JNIEnv* env, jobject thiz, jint width, jint height, jint maxIter,
    jdouble xCenter, jdouble yCenter, jdouble zoom, jdouble cx, jdouble cy,
    jfloatArray palT, jfloatArray palRGB) {
    return Java_com_aleespa_randomsquare_tools_FractalRenderer_renderInternal(
        env, thiz, 1, width, height, maxIter, xCenter, yCenter, zoom, cx, cy, palT, palRGB);
}
