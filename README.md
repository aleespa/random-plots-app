# Random Plots

Random Plots is an Android application that generates stunning generative art using high-performance native rendering. Originally built with a Python-based pipeline, the app has been fully migrated to a C++ backend utilizing Skia and custom shaders for superior performance, visual accuracy, and compatibility with modern Android standards (including 16 KB page alignment).

## 🎨 Features

- **Generative Sketches**: Over 15 unique algorithmic art styles, including Spirographs, Orbits, Noisy Circles, and more.
- **Fractal Explorer**: High-speed rendering of Mandelbrot, Julia, Tricorn, Multibrot, and Newton fractals.
- **Composition Engine**: Create complex abstract art using a custom stack-based opcode VM that combines mathematical functions.
- **Real-time Customization**: Adjust colormaps and background colors with instant feedback for most figure types.
- **Wallpaper Integration**: Set your favorite generated pieces as your device wallpaper directly from the app.
- **Gallery & History**: Save your creations to a local Room database and export them to your device gallery.
- **Glance Widgets**: Bring generative art to your home screen with customizable widgets that update automatically.

## 🛠 Tech Stack

- **UI**: Jetpack Compose (Material 3)
- **Language**: Kotlin & C++17
- **Graphics Engine**: [Skia](https://skia.org/) (via JNI)
- **Math**: [Eigen](https://eigen.tuxfamily.org/) for complex linear algebra and eigenvalue calculations.
- **Database**: Room for image metadata and local history.
- **Settings**: Jetpack DataStore for persistent user preferences.
- **Widgets**: Jetpack Glance.

## 🚀 How to Run

### Prerequisites
- Android Studio Ladybug (or newer).
- Android SDK 35 (compileSdk).
- NDK 26.1.10909125 (specified in `app/build.gradle`).
- CMake 3.22.1+.

### Build & Install
1. Clone the repository.
2. Open the project in Android Studio.
3. Wait for Gradle sync to complete.
4. Run the `:app` module on a physical device or emulator (API 29+).

*Note: The app is optimized for Android 15 and supports 16 KB page size devices.*

## 📂 Project Structure

- `app/src/main/cpp/`: Core rendering engine.
    - `sketches/`: Implementation of individual art algorithms (e.g., `spirograph.cpp`, `orbits.cpp`).
    - `libs/`: Pre-built static libraries for Skia and Eigen.
- `app/src/main/java/com/aleespa/randomsquare/`:
    - `data/`: Room entities, Repositories, and the `VisualizeModel` (ViewModel).
    - `tools/`: JNI Bridges (`SketchRenderer`, `FractalRenderer`) and utility functions.
    - `pages/`: UI screens built with Jetpack Compose.
    - `widget/`: Home screen widget implementation using Glance.

## 🛠 Adding New Sketches

The app is designed to be easily extensible. To add a new generative sketch:
1. Create a new class inheriting from `SketchBase` in `app/src/main/cpp/sketches/`.
2. Implement the `render(RenderContext& ctx, uint64_t seed)` method.
3. Register the new sketch in `jni_bridge.cpp`.
4. Add the new entry to the `Figures` enum in Kotlin.

## 📜 License

This project is licensed under the MIT License - see the LICENSE file for details.
