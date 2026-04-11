cmake_minimum_required(VERSION 3.22)
project(randomsquare)

# Ensure 16 KB page alignment for Android 15+ compatibility
# This must be set before add_library or use target_link_options
add_link_options("-Wl,-z,max-page-size=16384")
add_link_options("-Wl,-z,common-page-size=16384")

set(SKIA_ROOT ${CMAKE_CURRENT_SOURCE_DIR}/libs/skia)
set(EIGEN_ROOT ${CMAKE_CURRENT_SOURCE_DIR}/libs/eigen)

file(GLOB SKETCH_SOURCES "sketches/*.cpp")

add_library(randomsquare SHARED
    render_context.cpp
    jni_bridge.cpp
    fractalrenderer.cpp
    ${SKETCH_SOURCES}
)

target_include_directories(randomsquare PRIVATE
    ${SKIA_ROOT}
    ${SKIA_ROOT}/include
    ${SKIA_ROOT}/include/core
    ${SKIA_ROOT}/include/encode
    ${SKIA_ROOT}/include/private
    ${EIGEN_ROOT}
    ${CMAKE_CURRENT_SOURCE_DIR}
)

target_link_libraries(randomsquare
    ${SKIA_ROOT}/${ANDROID_ABI}/libskia.a
    android
    log
    jnigraphics
    GLESv3
    EGL
)

target_compile_options(randomsquare PRIVATE -std=c++17 -O3)
target_link_options(randomsquare PRIVATE "-Wl,-z,max-page-size=16384" "-Wl,-z,common-page-size=16384")
