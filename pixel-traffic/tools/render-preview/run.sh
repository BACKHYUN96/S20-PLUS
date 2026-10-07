#!/usr/bin/env bash
set -euo pipefail
tool_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
project_dir="$(cd -- "$tool_dir/../.." && pwd)"
source_dir="$project_dir/app/src/main/java/com/s20plus/pixeltraffic"
build_dir="${RENDER_PREVIEW_BUILD_DIR:-/tmp/pixel-traffic-render-preview}"
javac_bin="${JAVA_HOME:+$JAVA_HOME/bin/}javac"
java_bin="${JAVA_HOME:+$JAVA_HOME/bin/}java"
mkdir -p "$build_dir"
# Fontconfig otherwise tries the cloud account's read-only home cache for sheet labels.
export XDG_CACHE_HOME="${XDG_CACHE_HOME:-$build_dir/cache}"
mkdir -p "$XDG_CACHE_HOME"
source_files=(CityScene PixelArt TrafficModel RainModel SnowModel ThemeBlend SceneryClock CameraLayout SceneEventModel SceneEvents VehicleArt)
compile_files=()
for source_name in "${source_files[@]}"; do
 if [[ -f "$source_dir/$source_name.java" ]]; then compile_files+=("$source_dir/$source_name.java"); fi
done
"$javac_bin" -d "$build_dir" "$tool_dir"/android/graphics/*.java "${compile_files[@]}" "$tool_dir/SceneRenderPreview.java" "$tool_dir/GraphicsChecks.java" "$tool_dir/SceneRenderChecks.java"
"$java_bin" -Djava.awt.headless=true -cp "$build_dir" com.s20plus.pixeltraffic.GraphicsChecks
if [[ "${1:-}" == "--checks" ]]; then
 "$java_bin" -Djava.awt.headless=true -cp "$build_dir" com.s20plus.pixeltraffic.SceneRenderChecks
 exit 0
fi
"$java_bin" -Djava.awt.headless=true -cp "$build_dir" com.s20plus.pixeltraffic.SceneRenderPreview "$@"
