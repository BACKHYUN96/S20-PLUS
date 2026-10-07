#!/usr/bin/env bash
set -euo pipefail
tool_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
project_dir="$(cd -- "$tool_dir/../.." && pwd)"
source_dir="$project_dir/app/src/main/java/com/s20plus/pixeltraffic"
graphics_dir="$project_dir/tools/render-preview/android/graphics"
build_dir="${CINEMATIC_PREVIEW_BUILD_DIR:-/tmp/pixel-traffic-cinematic-preview}"
javac_bin="${JAVA_HOME:+$JAVA_HOME/bin/}javac"
java_bin="${JAVA_HOME:+$JAVA_HOME/bin/}java"
mkdir -p "$build_dir"
export XDG_CACHE_HOME="${XDG_CACHE_HOME:-$build_dir/cache}"
mkdir -p "$XDG_CACHE_HOME"
source_files=(CinematicScene CinematicArtwork CinematicGeometry CinematicVehicles VehicleLights VehicleLighting LightTextures CityLightAnchors CinematicEffects VehicleLayout CinematicTraffic TrafficModel RainModel SnowModel SceneryClock SceneEventModel VehicleArt ThemeBlend RoadWetness SceneTimePolicy)
compile_files=()
for source_name in "${source_files[@]}"; do
 if [[ -f "$source_dir/$source_name.java" ]]; then compile_files+=("$source_dir/$source_name.java"); fi
done
"$javac_bin" -d "$build_dir" "$graphics_dir"/*.java \
 "$tool_dir"/android/graphics/*.java "$tool_dir"/android/content/*.java "$tool_dir"/android/content/res/*.java \
 "${compile_files[@]}" "$project_dir/tools/render-preview/GraphicsChecks.java" \
 "$tool_dir/CinematicLightingChecks.java" "$tool_dir/CinematicTrafficChecks.java" "$tool_dir/CinematicSignalChecks.java" "$tool_dir/CinematicRenderChecks.java" "$tool_dir/CinematicRenderPreview.java" "$tool_dir/VehicleAtlasChecks.java" "$tool_dir/CinematicEffectChecks.java" "$tool_dir/CinematicThemeChecks.java" "$tool_dir/CinematicSurfaceChecks.java" "$tool_dir/CinematicWeatherChecks.java" "$project_dir/tests/CinematicGeometryChecks.java" "$project_dir/tests/VehicleLayoutChecks.java"
"$java_bin" -Djava.awt.headless=true -cp "$build_dir" com.s20plus.pixeltraffic.GraphicsChecks
if [[ "${1:-}" == "--checks" ]]; then
 "$java_bin" -Djava.awt.headless=true -cp "$build_dir" com.s20plus.pixeltraffic.CinematicGeometryChecks
 "$java_bin" -Djava.awt.headless=true -cp "$build_dir" com.s20plus.pixeltraffic.VehicleLayoutChecks
 "$java_bin" -Djava.awt.headless=true -cp "$build_dir" com.s20plus.pixeltraffic.VehicleAtlasChecks "${2:-$project_dir/app/src/main/assets}"
 "$java_bin" -Djava.awt.headless=true -cp "$build_dir" com.s20plus.pixeltraffic.CinematicEffectChecks "${2:-$project_dir/app/src/main/assets}"
 "$java_bin" -Djava.awt.headless=true -cp "$build_dir" com.s20plus.pixeltraffic.CinematicThemeChecks "${2:-$project_dir/app/src/main/assets}"
 "$java_bin" -Djava.awt.headless=true -cp "$build_dir" com.s20plus.pixeltraffic.CinematicSurfaceChecks "${2:-$project_dir/app/src/main/assets}"
 "$java_bin" -Djava.awt.headless=true -cp "$build_dir" com.s20plus.pixeltraffic.CinematicWeatherChecks "${2:-$project_dir/app/src/main/assets}"
 "$java_bin" -Djava.awt.headless=true -cp "$build_dir" com.s20plus.pixeltraffic.CinematicLightingChecks "${2:-$project_dir/app/src/main/assets}"
 "$java_bin" -Djava.awt.headless=true -cp "$build_dir" com.s20plus.pixeltraffic.CinematicTrafficChecks
 "$java_bin" -Djava.awt.headless=true -cp "$build_dir" com.s20plus.pixeltraffic.CinematicSignalChecks "${2:-$project_dir/app/src/main/assets}"
 "$java_bin" -Djava.awt.headless=true -cp "$build_dir" com.s20plus.pixeltraffic.CinematicRenderChecks "${2:-$project_dir/app/src/main/assets}"
else
 "$java_bin" -Djava.awt.headless=true -cp "$build_dir" com.s20plus.pixeltraffic.CinematicRenderPreview "$@"
fi
