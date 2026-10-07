#!/usr/bin/env bash
set -euo pipefail

usage() {
 cat <<'EOF'
사용법: tools/check-patch.sh geometry|cinematic|settings|model|all
  geometry   전용 차선/차체 계산 검사
  cinematic  기존 전용 합성 실행기 --checks (기하/아틀라스 포함)
  settings   시간/빠른 설정/미리보기/카메라 계산 검사
  model      주행/프레임/날씨/풍경/이벤트 계산 검사
  all        model + settings + 기존 합성 + 전용 합성 (기하 1회)
  --help     도움말만 출력
범위는 필수입니다. Git 변경을 자동 분류하지 않습니다.
Android 빌드/Lint, APK/실기기 검사, 자산 생성은 별도로 수행합니다.
JDK 선택: JAVA_HOME → PATH의 전체 JDK → /workspace/toolchains/jdk-21.0.8+9.
클라우드 Android 빌드: python /workspace/toolchains/run-gradle.py :app:assembleDebug :app:lintDebug
EOF
}

if [[ $# == 1 && ( "$1" == --help || "$1" == -h ) ]]; then
 usage
 exit 0
fi
if [[ $# != 1 ]]; then
 usage >&2
 exit 2
fi
scope="$1"
case "$scope" in
 geometry|cinematic|settings|model|all) ;;
 *) printf '알 수 없는 검사 범위: %s\n' "$scope" >&2; usage >&2; exit 2 ;;
esac

tool_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
project_dir="$(cd -- "$tool_dir/.." && pwd)"
source_dir="$project_dir/app/src/main/java/com/s20plus/pixeltraffic"

if [[ -n "${JAVA_HOME:-}" ]]; then
 javac_bin="$JAVA_HOME/bin/javac"
 java_bin="$JAVA_HOME/bin/java"
elif command -v javac >/dev/null 2>&1 && command -v java >/dev/null 2>&1; then
 javac_bin="$(command -v javac)"
 java_bin="$(command -v java)"
elif [[ -x /workspace/toolchains/jdk-21.0.8+9/bin/javac && -x /workspace/toolchains/jdk-21.0.8+9/bin/java ]]; then
 export JAVA_HOME=/workspace/toolchains/jdk-21.0.8+9
 javac_bin="$JAVA_HOME/bin/javac"
 java_bin="$JAVA_HOME/bin/java"
else
 printf '전체 JDK가 필요합니다. 기존 JDK의 JAVA_HOME을 설정하세요.\n' >&2
 exit 1
fi
if [[ ! -x "$javac_bin" || ! -x "$java_bin" ]]; then
 printf '선택한 JDK에서 java/javac를 실행할 수 없습니다. JAVA_HOME을 확인하세요.\n' >&2
 exit 1
fi

source_names=()
check_names=()
if [[ "$scope" == geometry ]]; then
 source_names+=(CinematicGeometry VehicleLayout TrafficModel)
 check_names+=(CinematicGeometryChecks VehicleLayoutChecks)
fi
if [[ "$scope" == model || "$scope" == all ]]; then
 source_names+=(TrafficModel PlaybackPolicy FrameStats FramePacer RainModel ThemeBlend SnowModel SceneryClock SceneEventModel)
 check_names+=(TrafficModelChecks FrameStatsChecks FramePacerChecks RainModelChecks ExpansionChecks SceneryChecks SceneEventChecks)
fi
if [[ "$scope" == settings || "$scope" == all ]]; then
 source_names+=(SceneTimePolicy QuickMode PreviewLayout CameraLayout)
 check_names+=(SceneTimeChecks PreviewLayoutChecks CameraChecks)
fi

if [[ ${#check_names[@]} -gt 0 ]]; then
 build_root="${PATCH_CHECK_BUILD_DIR:-/tmp/pixel-traffic-patch-checks}"
 mkdir -p "$build_root"
 build_dir="$(mktemp -d "$build_root/classes.XXXXXX")"
 trap 'rm -rf -- "$build_dir"' EXIT
 compile_files=()
 for source_name in "${source_names[@]}"; do
  compile_files+=("$source_dir/$source_name.java")
 done
 for check_name in "${check_names[@]}"; do
  compile_files+=("$project_dir/tests/$check_name.java")
 done
 "$javac_bin" -d "$build_dir" "${compile_files[@]}"
 for check_name in "${check_names[@]}"; do
  printf '검사: %s\n' "$check_name"
  "$java_bin" -cp "$build_dir" "com.s20plus.pixeltraffic.$check_name"
 done
fi

if [[ "$scope" == all ]]; then
 bash "$tool_dir/render-preview/run.sh" --checks
fi
if [[ "$scope" == cinematic || "$scope" == all ]]; then
 bash "$tool_dir/cinematic-preview/run.sh" --checks
fi
printf '완료: %s (Android/실기기 검사는 별도)\n' "$scope"
