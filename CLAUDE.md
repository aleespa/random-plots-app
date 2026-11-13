# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Random Plots (`com.aleespa.randomsquare`): an Android app that generates generative art and fractals. The UI is Jetpack Compose and Kotlin. All image rendering happens in a native C++17 library (`librandomsquare.so`) built on Skia and Eigen and called through JNI. Single Gradle module `:app`. minSdk 29, compile/target SDK 37, NDK 26.1.10909125, CMake 3.22.1, Java 17 toolchain. ABIs: `arm64-v8a` and `x86_64` only.

## Commands

Run from the repo root (on Windows use `gradlew.bat` or `./gradlew` from Git Bash):

```sh
./gradlew assembleDebug                 # build debug APK (also builds native lib via CMake)
./gradlew installDebug                  # install on connected device/emulator
./gradlew testDebugUnitTest             # JVM unit tests (Robolectric + MockK)
./gradlew testDebugUnitTest --tests "com.aleespa.randomsquare.tools.GenerateTest"
./gradlew testDebugUnitTest --tests "com.aleespa.randomsquare.tools.GenerateTest.someMethod"
./gradlew connectedDebugAndroidTest     # instrumented tests: renders every figure natively (needs a device with OpenGL ES 3.1)
scripts/export-figure-samples.sh        # renders every figure on the device -> build/figure-samples/<key>.webp (1000x1000)
./gradlew lint
./gradlew bundleRelease                 # release AAB (R8 minify + resource shrinking)
```

**The native build needs prebuilt dependencies that git does not track.** `app/src/main/cpp/libs/skia/` (headers plus `<ABI>/libskia.a`) and `app/src/main/cpp/libs/eigen/` are gitignored, as is `*.a`. A fresh clone will not link until you supply them.

`app/build.gradle` references `stability_config.conf` at the repo root for the Compose compiler. Check that the file exists if the build complains about it.

## Architecture

### Rendering pipeline (Kotlin → JNI → C++)

There are three native render paths. All return encoded image bytes (`ByteArray`) that Kotlin decodes into a `Bitmap`:

1. **Sketches** (`SketchRenderer.renderSketch`, `cpp/jni_bridge.cpp`). Each sketch is a `SketchBase` subclass in `cpp/sketches/` that implements `render(RenderContext&, uint64_t seed)` and draws with Skia. Output is deterministic for a given seed. CMake globs `sketches/*.cpp`, so new files are picked up automatically. `RenderContext` (world coordinates via `setWorldBounds`) offers `drawPolyline`, `drawScatter`, `fillPolygon`, `drawCircle` (world-unit radius), `setBlendMode` (e.g. `SkBlendMode::kPlus` for additive glow), and a density renderer for millions of points (attractors, flow fields): `auto d = ctx.makeDensity(); d.add(p); ... ctx.drawDensity(d, gamma)`.
2. **Fractals** (`tools/FractalRenderer.render`, `cpp/fractalrenderer.cpp` + `fractal_shader.h`). Covers Mandelbrot, Julia, Tricorn, Multibrot and Newton. The type is an int, and the palette is passed as `palT`/`palRGB` float arrays.
3. **Compositions** (`FractalRenderer.renderComposition`, `composition_shader.h`). A small stack-based VM. Kotlin (`renderFigure` in `tools/FigureRendering.kt`) builds a random opcode/param bytecode, and the shader interprets it per pixel. Opcodes 0–10 are X, Y, Const, Sum, Prod, Mod, Well, Tent, Sin, Level and Mix. This is Andrej Bauer's Random Art (randomart.py).

**Prism** is in the Compositions category but is a C++ sketch (`cpp/sketches/prism.cpp`, `sketchId = 15`): an extended Random Art with palette colouring (`RenderContext::drawValues`), focal points, coordinate transforms (rotate/mirror/kaleidoscope/tile/polar), hard-edged ops and shared sub-formulas. `app/src/main/python/figures/prism.py` is its prototype; keep the two in sync when tuning weights or the quality gate. Prism also has a full-colour mode (`Colormaps.FULL_COLOR`, enabled by `Figures.supportsFullColor`): the same formula is evaluated once per RGB channel with per-channel constants, phases and focal scales (C++ only; the Python prototype is palette-only). Which colormaps a figure offers, and the fallback when switching figures, come from `Colormaps.isAvailableFor(figure)` / `Colormaps.defaultFor(figure)`; Prism hides the solid colours. Engine-specific behaviour (pan/zoom gestures, view reset, wallpaper sizing) keys off `Figures.hasPanZoomView` (fractals and all compositions, Prism included), never off the category. Pannable sketches receive the view through `RenderContext::view()` (`PlaneView`: world = center + ndc*zoom, y up); Prism fixes its contrast range from the default view so colours stay stable while panning.

**Native IDs live on the `Figures` enum and must match the C++ side:**
- `Figures.sketchId` ↔ the `switch` in `cpp/jni_bridge.cpp`; `Figures.fractalType` ↔ `u_type` in `cpp/fractal_shader.h`. `FiguresTest` checks that every sketch/fractal has an ID, that sketch IDs are unique and contiguous from 0, and pins the existing values — never renumber them.
- Composition opcodes in `generate.kt` must match `composition_shader.h` (no test covers these).

### Adding a figure

1. For a sketch, add `cpp/sketches/<name>.cpp` and register it with the next free case in `jni_bridge.cpp`. For a fractal, add a `u_type` branch in `fractal_shader.h`.
2. Add an entry to the `Figures` enum in `Figures.kt`: a `key` string, a `FigureType` category, a string resource (en/es/fr), a sample drawable, a menu lambda (`standardMenu` / `fractalMenu` / `newtonMenu` / `compositionMenu` from `pages/visualize/Menus.kt`), and `sketchId = <case>` or `fractalType = <u_type>`. `generate.kt` reads the ID from the enum; there is nothing else to map.
3. Add the new value to `existingNativeIdsAreUnchanged` in `FiguresTest`.
4. Run `scripts/export-figure-samples.sh` with a device connected: `FigureRenderTest` checks the new figure renders natively (non-null, non-blank) and the script pulls its Browse sample to `build/figure-samples/<key>.webp`; copy it to `res/drawable/`.
5. Optionally add `assets/latex/<key>.tex`. `readTexAssets` loads it by `Figures.key` to show the figure's math with jlatexmath.

Rendering for a figure outside the UI goes through `renderFigure(RenderSpec(...))` (`tools/FigureRendering.kt`); `FractalView.defaultsFor(figure)` is the single source of each fractal's starting view. `generateRandomPlot(visualizeModel, ...)` just builds a `RenderSpec` from the ViewModel.

`Figures.key` is persisted, both in Room (`ImageEntity.imageType`) and in DataStore (the selected figure), and is resolved back with `Figures.fromKey`. Treat existing keys as stable.

### App state and data

- `data/VisualizeModel` is the single shared ViewModel, built by `VisualizeModelFactory` from `ImageRepository` (Room) and `AppSettingsRepository` (DataStore). It holds Compose state for the selected figure, colormap, background, fractal parameters, theme and gallery filters. Setters write through to DataStore, and `init` collects the DataStore flows back into that state.
- Room: `AppDatabase` (version 3, schemas exported to `app/schemas/`) with `MIGRATION_2_3`. `DatabaseProvider` uses `fallbackToDestructiveMigration` and deletes and recreates the DB on open failure, so **a schema change without a migration silently wipes users' gallery**. Bump the version and add a migration.
- `workers/PruneImagesWorker` (WorkManager) prunes stored images.
- `pages/` holds the Compose screens: `visualize` (generate/edit), `gallery`, `browse` (figure catalog plus settings). `MainScreen`/`BottomBarScreen` handle navigation.
- `widget/` holds the Glance home-screen widgets (random number, dice, coin flip, 4-digit). Each widget is registered as a `<receiver>` in the manifest.
- `ads/AdManager` integrates Google Mobile Ads.

### Testing notes

Instrumented tests (`app/src/androidTest`) load the real native library: `FigureRenderTest` is parameterized over every `Figures` entry. Use a recent emulator image with a host GPU; old images (API 29) only expose OpenGL ES 2.0 and fractals/compositions return null there.

Unit tests run under Robolectric with `systemProperty "is_testing" = "true"`. The `SketchRenderer` and `FractalRenderer` objects check that property and skip `System.loadLibrary`, so tests must `mockkObject(...)` the renderers instead of calling native code. Set the property before touching those objects (see `GenerateTest`'s `@BeforeClass`).

### Python directory

`app/src/main/python/` is **not used by the app at runtime**. It holds the original matplotlib implementations of the sketches (`figures/`), from before the C++ migration, which are useful as reference when porting or comparing output. It also has scripts that generate icons and SVGs (`icons/`), plus `run.py`/`benchmark.py`. It is a `uv` project (Python ≥ 3.13): `cd app/src/main/python && uv run run.py` writes renders to `outputs/`.

### Native build constraints

- The library links with 16 KB page alignment (`-Wl,-z,max-page-size=16384`) for Android 15+. Keep these link options.
- It uses `-DANDROID_STL=c++_static` and `-O3`. Native libraries are packaged with `useLegacyPackaging = true`.
