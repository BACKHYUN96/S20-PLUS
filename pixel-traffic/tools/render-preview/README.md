# Desktop production-scene previews

This development tool runs `CityScene` and its production models/assets through a small Java2D implementation of the Android graphics calls that the scene uses. Unlike `tools/art-preview`, it includes actual composition order, wet-road reflections, moving weather, theme blending, camera transforms, and scene events. No alternate scene geometry or reflection implementation is used here.

The generated files are **desktop previews, not Android screenshots**. Java2D rasterization and nearest-neighbor sampling can differ from Android Canvas/Skia. This tool cannot establish Android frame rate, memory use, battery drain, UI behavior, lifecycle behavior, or actual device appearance. The graphics shim exists only under `tools` and is not packaged in the APK.

From `pixel-traffic` with a full desktop JDK (including `java.desktop`):

```bash
JAVA_HOME=/workspace/toolchains/jdk-21.0.8+9 bash tools/render-preview/run.sh /tmp/city-scene.png 0 1 2 0 1 10 6 true 2
JAVA_HOME=/workspace/toolchains/jdk-21.0.8+9 bash tools/render-preview/run.sh --sheet /tmp/all-roads.png
JAVA_HOME=/workspace/toolchains/jdk-21.0.8+9 bash tools/render-preview/run.sh --checks
```

The individual arguments are output filename, road, theme, weather, camera mode, camera position, simulated seconds, cars per lane, event enabled, and event frequency. Defaults are city/evening/heavy rain/full scene/center/10 seconds/3 cars per lane/events on/frequency 2. The sheet renders all four roads at evening/heavy rain/6 cars per lane/10 seconds. Simulation advances at a fixed 30 Hz; it is not a performance benchmark.

The runner first checks expected pixel outcomes for source-over opacity, SRC reset, save/restore, transformed clipping, bitmap opacity, and destination bounds. It then compiles the selected production classes and exports the scene PNG. A successful export does not mean that the appearance has been reviewed or that Android tests passed.

`--checks` exercises 60 combinations of all four roads, three themes, and five weather choices, together with density/camera/brightness variations. It checks opaque nonempty output and identical output when the scene is drawn twice without advancing time. This can catch retained-frame artifacts and rendering exceptions; it does not establish that the art matches the reference or replace review on a phone.

Use `RENDER_PREVIEW_BUILD_DIR` to choose the compiled class directory (default `/tmp/pixel-traffic-render-preview`). Run sequentially for one class directory, or give each concurrent invocation a separate directory.
