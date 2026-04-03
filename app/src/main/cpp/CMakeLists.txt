
cmake_minimum_required(VERSION 3.22.1)
project("fractalrenderer")

add_library(${CMAKE_PROJECT_NAME} SHARED
    fractalrenderer.cpp)


target_link_libraries(${CMAKE_PROJECT_NAME}
    GLESv3
    EGL
    android
    log)

target_link_options(${CMAKE_PROJECT_NAME} PRIVATE "-Wl,-z,max-page-size=16384")
