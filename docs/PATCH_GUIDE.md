# 작은 패치 작업 안내

## Unity 도시·조명 패치의 범위와 검사

| 변경 대상 | 관련 파일 | 필수 확인 |
| --- | --- | --- |
| 도시 지면·건물·가로수·재질 | pixel-traffic-unity/Assets/PixelTraffic/Editor/CityEnvironment.cs, StarterScene.cs | PC Unity compile, scene viewport ground coverage·triangle/renderer/material budget·shader/mipmap/shadow |
| 버전/장면/APK | Runtime/StarterConfig.cs, Editor/PrototypeBuild.cs, tools/run-pipeline.ps1 | 코드/파일명 일치, Android BuildPlayer·Lint, 원본v2 certificate·SDK·ARM64·APK 해시 |
| 실제 렌더 미리보기 | Editor/CityPreview.cs, tools/run-unity.ps1, workflow | GPU warmup 후 실제 PNG/loaded material colors; Editor 렌더와 phone/FPS를 구분 |
| APK 전달 | docs/UNITY_AUTOMATION.md 및 STATUS | 최신 Windows 폴더와 Unity SDK adb.exe 직접 호출·install 성공 후 Activity 시작 |

확인된 main 컴파일/scene 뒤 APK tag를 게시합니다. IPC 일시 실패는 원인 로그를 확인한 뒤 failed job만 제한적으로 재시도하며, source compile 실패에는 APK tag를 만들지 않습니다. Editor-only 캡처 수정은 장면/런타임/build 입력 blob 동일성을 확인하면 APK를 반복 빌드하지 않습니다. 결과 문서만 수정할 때 source pipeline을 재실행하지 않습니다. 누적 native 파일이나 dirty root 문서를 통째로 stage하지 말고 원격 main에서 Unity 범위·문서 entry만 반영합니다. 실제 폰 성능은 별도 확인입니다.

## 2026-10-09 — Unity PC 자동 검사 연결

사용자 Windows PC에서 수정 실행기의 `Unity Validate completed` 및 GitHub runner 2.337.0의 `Connected to GitHub` / `Listening for Jobs`를 화면으로 확인했습니다. Unity 6000.3.26f1 프로젝트와 공통 PowerShell pipeline을 `pixel-traffic-unity/`에 추가하고 main의 Unity 관련 변경을 자동 Validate에 연결합니다. main 수동 Validate/BuildApk도 지원하며 PR 코드는 자동 실행하지 않습니다. runner 라벨은 self-hosted/Windows/X64/pixel-traffic-unity입니다.

이번 게시 범위는 Unity 프로젝트·workflow·관련 문서입니다. 기존 native Android 앱 변경은 포함하지 않습니다. workflow의 main 제한·Validate 기본값·보고서 허용 목록 및 source 제외 항목을 정적으로 확인했습니다. 실제 첫 GitHub Actions 작업은 main 반영 후 확인해야 합니다. Unity APK·기기 실행·WallpaperService 연결·자동 캡처는 아직 검증하지 않았습니다. 기존 인증서 검증 후 APK를 공유하도록 구성했고 키/암호/원본 로그는 게시하지 않습니다.

[PC 실행과 결과 확인](UNITY_AUTOMATION.md), [전환 과정](UNITY_MIGRATION.md). GitHub source 게시, main 반영, Actions 성공은 각각 실제 결과를 따로 기록합니다.

목표는 필요한 조사와 검증을 한 번씩 수행하고, 다음 담당자가 같은 조사를 되풀이하지 않도록 남기는 것입니다. [AGENTS](../AGENTS.md)의 사용자 변경 보존·기록·APK 전달 규칙을 함께 따릅니다.

## 시작과 범위 확정

1. 루트 README의 진입점과 최신 버전, STATUS의 최신 전달·진행/다음 작업, WORK_LOG의 최신 항목을 읽습니다. 요청 관련 키워드를 `rg -n -m 6 '검색어' docs/WORK_LOG.md`로 찾고 해당 항목만 추가로 읽습니다. 근거가 부족하거나 이전 결정과 충돌하면 관련 이력·설계까지 확장합니다.
2. `git status --short`와 실제 대상 파일을 대조하고 이번 수정 파일을 정합니다. 미추적 앱 전체가 보일 수 있으므로 Git 차이만으로 패치 범위를 자동 선택하지 않습니다. 작업 전 파일 상태를 보존하고 이번에 손댄 경로를 별도로 기록합니다.
3. 아래 표에서 관련 모듈을 고르고 호출부·데이터 저장 경로까지 확인합니다. 독립 작업을 나눌 때는 파일별 담당자를 정하고, 검사 실행 담당자도 한 명 정합니다.

전체 WORK_LOG 대신 최신 항목만 읽는 명령(저장소 루트):

```bash
awk '/^## / { if (seen++) exit } { print }' docs/WORK_LOG.md
```

## 모듈별 조사와 검사

소스 경로의 기준은 `pixel-traffic/app/src/main/java/com/s20plus/pixeltraffic/`입니다. 검사 명령은 `pixel-traffic`에서 실행합니다. 여러 모듈이 함께 바뀌면 해당 행의 검사를 합치고 중복 검사는 한 번만 실행합니다.

| 모듈·변경 | 먼저 읽을 파일 | 선택할 검사 |
| --- | --- | --- |
| 전용 차선·원근·차체 크기 | `CinematicGeometry.java`, `VehicleLayout.java`, `CinematicScene.java`, `TrafficModel.java`의 간격 상수, `tests/CinematicGeometryChecks.java`, `tests/VehicleLayoutChecks.java` | 개발 중 계산 확인: `tools/check-patch.sh geometry`. 최종 Scene/차량 합성 확인: `tools/check-patch.sh cinematic`(기하·구간 계산 포함) |
| 전용 배경·시간대·노면/눈/안개·캐시·차량 아틀라스·조명/반사·비/물보라 | `CinematicScene.java`, `CinematicArtwork.java`, `CinematicVehicles.java`, `VehicleLights.java`, `VehicleLighting.java`, `LightTextures.java`, `CityLightAnchors.java`, `CinematicEffects.java`, `ThemeBlend.java`, `RoadWetness.java`, `tools/cinematic-preview/`, `app/src/main/assets/`, `docs/CINEMATIC_RENDERER.md` | `tools/check-patch.sh cinematic`; 필요한 최종 합성 이미지만 생성·육안 확인; 시간대 연결은 엔진/미리보기/UI 호출과 자동 정책도 확인 |
| 전용 도심 신호·감속·정차·브레이크등 | `CinematicTraffic.java`, `CinematicScene.java`, `VehicleLayout.java`, 설정/프리셋·엔진/미리보기 연결, `CinematicTrafficChecks.java`, `CinematicSignalChecks.java` | `tools/check-patch.sh cinematic`에 신규 주행/실제 신호 합성 포함. 실패 후 통과한 기하·자산 검사 반복 대신 수정된 클래스만 컴파일하고 관련 단계부터 재개. 프리셋은 코드 경로 확인+기기 확인 기록 |
| 설정 UI·프리셋·미리보기 연결 | `MainActivity.java`, `WallpaperSettings.java`, `PresetStore.java`, `ScenePreviewView.java`, `TrafficWallpaperService.java`와 해당 정책 클래스 | `tools/check-patch.sh settings` + Android 빌드/Lint. 저장·가져오기·기존 프리셋 호환은 해당 경로 확인과 실기기 확인 항목을 남김 |
| 차량 주행·프레임·날씨·장면 이벤트 | `TrafficModel.java`, `PlaybackPolicy.java`, `FramePacer.java`, `FrameStats.java`, `RainModel.java`, `SnowModel.java`, `SceneryClock.java`, `SceneEventModel.java` 중 관련 파일 | `tools/check-patch.sh model`; 렌더 호출도 바뀌면 기존/전용 합성 검사 추가 |
| 기존 픽셀 풍경·구도·시간 전환 | `CityScene.java`, `PixelArt.java`, `VehicleArt.java`, `CameraLayout.java`, `ThemeBlend.java`, `tools/render-preview/` | `bash tools/render-preview/run.sh --checks`; 계산 변경은 `settings` 또는 `model`도 선택 |
| 여러 렌더러·공유 모델을 함께 변경 | 위 공유 파일과 양쪽 호출부 | `tools/check-patch.sh all`(모델·설정·기존 합성·전용 합성, 기하 중복 없음) |
| 문서·검사 도우미 | 변경 문서의 링크 대상, 수정한 실행기의 인수·호출 경로 | 문서/공백 확인, 셸 구문·도움말·오류 처리 확인; 바뀐 실행 경로는 실제 관련 검사로 확인 |

`settings`는 시간/빠른 설정·미리보기/카메라 계산 검사입니다. Android UI·SharedPreferences·프리셋 JSON 전체를 검사했다고 보고하지 않습니다. 절전/주행 정책 변경은 `model`도 필요합니다. 새 모델/검사가 생기면 표와 실행기의 명시적 목록을 함께 갱신합니다.

`cinematic`은 `tools/cinematic-preview/run.sh --checks`를 호출해 전용신호/가감속·브레이크/정차 물보라 및 그래픽 어댑터·기하·실제 아틀라스·전용 합성을 검사합니다. 최종 검증에 이를 실행했다면 같은 변경에 `geometry`를 다시 실행할 필요가 없습니다. 새 공유 클래스를 참조하는 실행기의 컴파일 목록을 갱신합니다. 참조하지 않는 기존 렌더러까지 수정하지 않습니다.

## 도구와 최종 검증

`tools/check-patch.sh`는 범위를 필수로 받으며 Git 상태로 추정하지 않습니다. 설정된 `JAVA_HOME`, PATH의 전체 JDK, 기존 `/workspace/toolchains/jdk-21.0.8+9` 순서로 사용합니다. 설치·다운로드·자산 생성·APK 빌드는 수행하지 않습니다.

```bash
tools/check-patch.sh --help
tools/check-patch.sh cinematic
```

앱 소스·리소스·빌드 설정 변경 또는 APK 전달 시 Android 빌드/Lint를 수행합니다. 현재 클라우드에서는 기존 프록시/CA 설정 도우미를 재사용합니다.

```bash
JAVA_HOME=/workspace/toolchains/jdk-21.0.8+9 python /workspace/toolchains/run-gradle.py :app:assembleDebug :app:lintDebug
```

일반 환경은 전체 JDK와 Android SDK를 준비한 뒤 `./gradlew :app:assembleDebug :app:lintDebug`를 사용합니다. 기존 도구가 준비돼 있으면 재설치하지 않습니다. 빌드 뒤 전달 APK의 실제 서명·versionCode/versionName·파일명을 확인하고 AGENTS의 ADB 설치 명령도 제공합니다.

필요한 PNG 미리보기는 소스/자산 변경이 끝난 후 한 번 생성합니다. 기존 원본 자산은 해당 수정이 요청된 경우에만 바꿉니다. 통과 이후 관련 입력이 바뀌지 않았다면 검사·빌드·이미지 생성을 반복하지 않습니다. 실패를 수정했거나 입력이 바뀌면 영향을 받는 검사를 재실행합니다.

문서 저장과 검증 전에 변경 파일을 대조합니다. `git diff --check`는 추적 파일에 적용되므로 미추적 변경 파일도 별도로 내용·공백·링크를 확인합니다. 종료 시 WORK_LOG에는 요청/원인/변경 파일/결정/실행 명령과 실제 결과/한계/다음을, STATUS에는 완료·진행·대기를 적습니다. 설치·사용법이 달라지면 README도 갱신합니다. 기존 이력은 보존하며 파일 저장·커밋·푸시 여부를 각각 보고합니다.

예: 차체 크기 요동 패치는 Geometry/VehicleLayout/Scene과 해당 기하 검사를 우선 조사합니다. 모델 고리·설정·PNG가 그대로인지 확인하고 전용 합성·Android 빌드/Lint·APK 검증을 실행합니다. 다른 맵 자산 생성이나 전체 이력 재조사는 새 근거가 있을 때만 확장합니다.

0.26.0부터 `CinematicTrafficChecks --edges`는 이미 통과한48장기궤적의 입력이 그대로일 때 나머지 신호 경계·노랑 진입·정차/출발·토글·프레임 동등성만 재개할 수 있습니다. 인수 없는 실행은 장기궤적도 포함합니다. 주행 소스가 바뀌면 장기궤적도 다시 확인합니다. 결과는 생략한 범위를 구분해 기록하며 edge 검사를 전체48궤적 검사라고 보고하지 않습니다.

0.27.0부터 조명 변경은 `CinematicLightingChecks`를 포함합니다. 색온도 참조/차종 프로필, 독립 램프·안개등 fixture, 실제 아틀라스와 세 팔레트, alpha·캐시 해제, 도로 clip/색·별도 안개등 비춤/후면 빔 없음/젖은 잔물결/창문 위치를 확인합니다. 기존 어댑터에 실제 `clipPath`가 필요합니다. 실패 수정은 유지한 class 디렉터리에서 관련 클래스만 재컴파일하고 해당 단계부터 재개합니다. 조명 관련 Scene이 바뀌면 신호·시간대/날씨·최종 합성도 확인하되 그대로 통과한 주행48궤적·기하·PNG를 중복하지 않습니다.

0.28.0처럼 전조등 텍스처·투사폭만 바뀌면 기존 class 디렉터리를 재사용해 `LightTextures`, `CinematicScene`, `CinematicLightingChecks`만 재컴파일하고 조명/효과/신호/최종 합성을 확인할 수 있습니다. 캐시가 없으면 기존 runner로 한 번 컴파일합니다. 기하/주행 소스가 그대로면 별도 기하/48장기궤적·기본맵/설정 전체 반복은 하지 않습니다. 최종 합성 클래스가 포함하는 실제 Scene 차체 픽셀/프로필 확인은 그 단계에서 수행됩니다.

0.29.0처럼 공통크기 프로필이 바뀌면 `VehicleLayoutChecks`의 차선 간 동일폭/같은방향 길이·기하/실제차체픽셀을 검사하고 **48장기주행궤적도 재실행**합니다. 크기는 신호의 정차앞끝/앞차간격에 영향을 주므로 이전 통과를 그대로 재사용하지 않습니다. `CinematicLightingChecks`의 흰승용차 정차쌍/양방향전방비춤과 실제신호검사도 확인합니다. `/tmp`클래스는 새환경에서 사라질 수 있으므로 존재/사용입력을 확인하고 없으면 기존runner로한번컴파일합니다.
