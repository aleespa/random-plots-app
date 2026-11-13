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
#include "sketches/prism.cpp"
#include "sketches/strange_attractor.cpp"
#include "sketches/lorenz_flow.cpp"
#include "sketches/random_circles.cpp"
#include "sketches/phyllotaxis.cpp"
#include "sketches/truchet.cpp"
#include "sketches/flow_field.cpp"
#include "sketches/fractal_flame.cpp"
#include "sketches/voronoi.cpp"
#include "sketches/domain_coloring.cpp"

#define LOG_TAG "SketchJNI"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// IDs must match Figures.sketchId in Figures.kt (checked by FiguresTest).
static std::unique_ptr<SketchBase> makeSketch(int sketch_id) {
    switch (sketch_id) {
        case 0: return std::make_unique<NoisyCirclesSketch>();
        case 1: return std::make_unique<BubblesSketch>();
        case 2: return std::make_unique<WavesSketch>();
        case 3: return std::make_unique<ConstellationsSketch>();
        case 4: return std::make_unique<PolygonGridSketch>();
        case 5: return std::make_unique<SpiralSketch>();
        case 6: return std::make_unique<CubismSketch>();
        case 7: return std::make_unique<ExponentialSumSketch>();
        case 8: return std::make_unique<PolygonFeedbackSketch>();
        case 9: return std::make_unique<PolygonTunnelSketch>();
        case 10: return std::make_unique<RotationsSketch>();
        case 11: return std::make_unique<OrbitsSketch>();
        case 12: return std::make_unique<SpirographSketch>();
        case 13: return std::make_unique<IrrationalSpirographSketch>();
        case 14: return std::make_unique<RandomEigenSketch>();
        case 15: return std::make_unique<PrismSketch>();
        case 16: return std::make_unique<StrangeAttractorSketch>();
        case 17: return std::make_unique<LorenzFlowSketch>();
        case 18: return std::make_unique<RandomCirclesSketch>();
        case 19: return std::make_unique<PhyllotaxisSketch>();
        case 20: return std::make_unique<TruchetSketch>();
        case 21: return std::make_unique<FlowFieldSketch>();
        case 22: return std::make_unique<FractalFlameSketch>();
        case 23: return std::make_unique<VoronoiSketch>();
        case 24: return std::make_unique<DomainColoringSketch>();
        default:
            LOGE("Unknown sketch_id %d", sketch_id);
            return nullptr;
    }
}

extern "C" {

JNIEXPORT jbyteArray JNICALL
Java_com_aleespa_randomsquare_SketchRenderer_renderSketch(
    JNIEnv* env, jobject,
    jint sketch_id, jlong seed,
    jint width, jint height,
    jint bg_color,
    jfloatArray colormap_t,
    jintArray colormap_rgb,
    jdouble x_center, jdouble y_center, jdouble zoom,
    jboolean full_color
) {
    RenderContext ctx(width, height, static_cast<SkColor>(bg_color));
    ctx.setView({x_center, y_center, zoom});
    ctx.setFullColor(full_color == JNI_TRUE);

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

    std::unique_ptr<SketchBase> sketch = makeSketch(sketch_id);
    if (!sketch) return nullptr;

    sketch->render(ctx, static_cast<uint64_t>(seed));
    auto jpeg = ctx.encodeJpeg(92);

    jbyteArray result = env->NewByteArray(static_cast<jsize>(jpeg.size()));
    env->SetByteArrayRegion(result, 0, jpeg.size(),
        reinterpret_cast<const jbyte*>(jpeg.data()));
    return result;
}

/**
 * Colour-field render for Figures.rendersColorField sketches: returns the palette-independent
 * field (RenderContext::enableFieldMode) as packed ints, or null if the sketch drew something that
 * can't be recoloured per pixel (the caller then falls back to renderSketch).
 */
JNIEXPORT jintArray JNICALL
Java_com_aleespa_randomsquare_SketchRenderer_renderSketchField(
    JNIEnv* env, jobject,
    jint sketch_id, jlong seed,
    jint width, jint height,
    jdouble x_center, jdouble y_center, jdouble zoom
) {
    std::unique_ptr<SketchBase> sketch = makeSketch(sketch_id);
    if (!sketch) return nullptr;
    RenderContext ctx(width, height);
    ctx.setView({x_center, y_center, zoom});
    ctx.setColormap({{0.0f, 0, 0, 0}, {1.0f, 255, 255, 255}});  // unused by field draws; keeps getColor safe
    ctx.enableFieldMode();
    sketch->render(ctx, static_cast<uint64_t>(seed));
    if (!ctx.fieldValid()) return nullptr;

    const auto& field = ctx.field();
    jintArray result = env->NewIntArray(static_cast<jsize>(field.size()));
    env->SetIntArrayRegion(result, 0, static_cast<jsize>(field.size()),
        reinterpret_cast<const jint*>(field.data()));
    return result;
}

} // extern "C"
