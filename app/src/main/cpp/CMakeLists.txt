cmake_minimum_required(VERSION 3.22)
project(randomsquare)

set(SKIA_ROOT ${CMAKE_SOURCE_DIR}/../../../libs/skia)

add_library(randomsquare SHARED
    render_context.cpp
    jni_bridge.cpp
)

target_include_directories(randomsquare PRIVATE
    ${SKIA_ROOT}
)

target_link_libraries(randomsquare
    ${SKIA_ROOT}/${ANDROID_ABI}/libskia.a
    android
    log
)

target_compile_options(randomsquare PRIVATE -std=c++17 -O2)
