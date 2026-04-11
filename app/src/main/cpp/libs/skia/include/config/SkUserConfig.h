#ifndef SkUserConfig_DEFINED
#define SkUserConfig_DEFINED

// Standard Skia configuration for Android
#ifdef NDEBUG
    #define SK_RELEASE
#else
    #define SK_DEBUG
#endif

#define SK_CPU_LENDIAN
#define SK_SUPPORT_PDF
#define SK_SUPPORT_GPU 0
#define SK_FORCE_RASTER_PIPELINE_BLITTER

#endif
