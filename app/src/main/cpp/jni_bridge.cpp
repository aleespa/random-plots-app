#include <jni.h>
#include <android/log.h>
#include <memory>
#include <vector>
#include "render_context.h"
#include "sketches/sketch_base.h"

// Sketch headers are not needed if we use factory or include them correctly.
// Since we used GLOB in CMake, we should include the headers here.
#include "sketches/noisy_circles.cpp"
#include "sketches/bubbles.cpp"
#include "sketches/waves.cpp"
#include "sketches/constellations.cpp"
#include "sketches/polygon_grid.cpp"
#include "sketches/spiral.cpp"
#include "sketches/cubism.cpp"
#include "sketches/exponential_sum.cpp"
#include "sketches/polygon_feedback.cpp"
#include "sketches/polygon_tunnel.cpp"
#include "sketches/rotations.cpp"
#include "sketches/orbits.cpp"
#include "sketches/spirograph.cpp"
#include "sketches/irrational_spirograph.cpp"
#include "sketches/random_eigen.cpp"

#define LOG_TAG "SketchJNI"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jbyteArray JNICALL
Java_com_aleespa_randomsquare_SketchRenderer_renderSketch(
    JNIEnv* env, jobject,
    jint sketch_id, jlong seed,
    jint width, jint height,
    jint bg_color,
    jfloatArray colormap_t,
    jintArray colormap_rgb
) {
    RenderContext ctx(width, height, static_cast<SkColor>(bg_color));

    // Setup colormap
    jsize cmap_len = env->GetArrayLength(colormap_t);
    jfloat* t_ptr = env->GetFloatArrayElements(colormap_t, nullptr);
    jint* rgb_ptr = env->GetIntArrayElements(colormap_rgb, nullptr);

    std::vector<ColorStop> stops;
    for (int i = 0; i < cmap_len; ++i) {
        uint32_t c = rgb_ptr[i];
        stops.push_back({t_ptr[i], static_cast<uint8_t>((c >> 16) & 0xFF),
                                   static_cast<uint8_t>((c >> 8) & 0xFF),
                                   static_cast<uint8_t>(c & 0xFF)});
    }
    ctx.setColormap(stops);

    env->ReleaseFloatArrayElements(colormap_t, t_ptr, JNI_ABORT);
    env->ReleaseIntArrayElements(colormap_rgb, rgb_ptr, JNI_ABORT);

    std::unique_ptr<SketchBase> sketch;
    switch (sketch_id) {
        case 0: sketch = std::make_unique<NoisyCirclesSketch>(); break;
        case 1: sketch = std::make_unique<BubblesSketch>(); break;
        case 2: sketch = std::make_unique<WavesSketch>(); break;
        case 3: sketch = std::make_unique<ConstellationsSketch>(); break;
        case 4: sketch = std::make_unique<PolygonGridSketch>(); break;
        case 5: sketch = std::make_unique<SpiralSketch>(); break;
        case 6: sketch = std::make_unique<CubismSketch>(); break;
        case 7: sketch = std::make_unique<ExponentialSumSketch>(); break;
        case 8: sketch = std::make_unique<PolygonFeedbackSketch>(); break;
        case 9: sketch = std::make_unique<PolygonTunnelSketch>(); break;
        case 10: sketch = std::make_unique<RotationsSketch>(); break;
        case 11: sketch = std::make_unique<OrbitsSketch>(); break;
        case 12: sketch = std::make_unique<SpirographSketch>(); break;
        case 13: sketch = std::make_unique<IrrationalSpirographSketch>(); break;
        case 14: sketch = std::make_unique<RandomEigenSketch>(); break;
        default:
            LOGE("Unknown sketch_id %d", sketch_id);
            return nullptr;
    }

    sketch->render(ctx, static_cast<uint64_t>(seed));
    auto jpeg = ctx.encodeJpeg(92);

    jbyteArray result = env->NewByteArray(static_cast<jsize>(jpeg.size()));
    env->SetByteArrayRegion(result, 0, jpeg.size(),
        reinterpret_cast<const jbyte*>(jpeg.data()));
    return result;
}

} // extern "C"
