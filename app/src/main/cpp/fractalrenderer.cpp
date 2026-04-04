#include <jni.h>
#include <GLES3/gl31.h>
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <android/log.h>
#include <vector>
#include <string>
#include <cmath>
#include <cstdlib>
#include <cstring>
#include "fractal_shader.h"
#include "composition_shader.h"

#define LOG_TAG "FractalRenderer"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Forward declarations or define before use
struct EGLContextManager {
    EGLDisplay display = EGL_NO_DISPLAY;
    EGLContext context = EGL_NO_CONTEXT;
    EGLSurface surface = EGL_NO_SURFACE;

    bool init() {
        display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
        if (display == EGL_NO_DISPLAY) return false;

        if (!eglInitialize(display, nullptr, nullptr)) return false;

        EGLint configAttribs[] = {
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT_KHR,
            EGL_BLUE_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_RED_SIZE, 8,
            EGL_NONE
        };
        EGLConfig config;
        EGLint numConfigs;
        if (!eglChooseConfig(display, configAttribs, &config, 1, &numConfigs) || numConfigs == 0) return false;

        EGLint contextAttribs[] = {
            EGL_CONTEXT_CLIENT_VERSION, 3,
            EGL_NONE
        };
        context = eglCreateContext(display, config, EGL_NO_CONTEXT, contextAttribs);
        if (context == EGL_NO_CONTEXT) return false;

        EGLint pbufferAttribs[] = {
            EGL_WIDTH, 1,
            EGL_HEIGHT, 1,
            EGL_NONE
        };
        surface = eglCreatePbufferSurface(display, config, pbufferAttribs);
        if (surface == EGL_NO_SURFACE) return false;

        return eglMakeCurrent(display, surface, surface, context);
    }

    void destroy() {
        if (display != EGL_NO_DISPLAY) {
            eglMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
            if (context != EGL_NO_CONTEXT) eglDestroyContext(display, context);
            if (surface != EGL_NO_SURFACE) eglDestroySurface(display, surface);
            eglTerminate(display);
        }
        display = EGL_NO_DISPLAY;
        context = EGL_NO_CONTEXT;
        surface = EGL_NO_SURFACE;
    }
};

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

struct Instruction {
    int32_t op;
    float v1;
    float v2;
    float v3;
};

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_aleespa_randomsquare_tools_FractalRenderer_renderComposition(
    JNIEnv* env, jobject thiz, jint width, jint height, jintArray opcodes, jfloatArray params) {

    EGLContextManager egl;
    if (!egl.init()) {
        LOGE("Failed to init EGL for Composition");
        return nullptr;
    }

    GLuint computeShader = compileShader(GL_COMPUTE_SHADER, COMPOSITION_SHADER_SOURCE);
    if (!computeShader) {
        egl.destroy();
        return nullptr;
    }

    GLuint program = glCreateProgram();
    glAttachShader(program, computeShader);
    glLinkProgram(program);
    glUseProgram(program);

    jsize count = env->GetArrayLength(opcodes);
    std::vector<Instruction> instructions(256, {0, 0, 0, 0});
    jint* pOp = env->GetIntArrayElements(opcodes, nullptr);
    jfloat* pParam = env->GetFloatArrayElements(params, nullptr);

    int realCount = count > 256 ? 256 : count;
    for (int i = 0; i < realCount; ++i) {
        instructions[i].op = pOp[i];
        instructions[i].v1 = pParam[i * 3 + 0];
        instructions[i].v2 = pParam[i * 3 + 1];
        instructions[i].v3 = pParam[i * 3 + 2];
    }

    GLuint ubo;
    glGenBuffers(1, &ubo);
    glBindBuffer(GL_UNIFORM_BUFFER, ubo);

    size_t uboSize = sizeof(Instruction) * 256 + 16;
    std::vector<uint8_t> uboData(uboSize, 0);
    memcpy(uboData.data(), instructions.data(), sizeof(Instruction) * 256);
    memcpy(uboData.data() + sizeof(Instruction) * 256, &realCount, sizeof(int));

    glBufferData(GL_UNIFORM_BUFFER, (GLsizeiptr)uboSize, uboData.data(), GL_STATIC_DRAW);
    glBindBufferBase(GL_UNIFORM_BUFFER, 0, ubo);

    GLuint texture;
    glGenTextures(1, &texture);
    glBindTexture(GL_TEXTURE_2D, texture);
    glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGBA8, width, height);
    glBindImageTexture(0, texture, 0, GL_FALSE, 0, GL_WRITE_ONLY, GL_RGBA8);

    glUniform1i(glGetUniformLocation(program, "u_width"), width);
    glUniform1i(glGetUniformLocation(program, "u_height"), height);

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

    env->ReleaseIntArrayElements(opcodes, pOp, JNI_ABORT);
    env->ReleaseFloatArrayElements(params, pParam, JNI_ABORT);
    glDeleteBuffers(1, &ubo);
    glDeleteTextures(1, &texture);
    glDeleteFramebuffers(1, &fbo);
    glDeleteProgram(program);
    glDeleteShader(computeShader);
    egl.destroy();

    return result;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_aleespa_randomsquare_tools_FractalRenderer_renderInternal(
    JNIEnv* env, jobject thiz, jint type, jint width, jint height, jint maxIter,
    jdouble xCenter, jdouble yCenter, jdouble zoom, jdouble cx, jdouble cy,
    jfloatArray palT, jfloatArray palRGB) {

    EGLContextManager egl;
    if (!egl.init()) {
        LOGE("Failed to init EGL for Fractal");
        return nullptr;
    }

    GLuint computeShader = compileShader(GL_COMPUTE_SHADER, COMPUTE_SHADER_SOURCE);
    if (!computeShader) {
        egl.destroy();
        return nullptr;
    }

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
