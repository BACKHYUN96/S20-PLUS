# Pixel Traffic Unity — 신호·인도 보행·횡단 0.4.0

## 현재 패치 — 신호·인도 보행·횡단 0.4.0 (2026-10-10 KST)

차량 신호/stop line·앞차 간격과 인도 보행자를 연결합니다. 기본32명/폰 People +/-4로4~100명, 인도 무작위 목적지/화단·가로등 회피·최대12대기자리·양방향 zebra band를 사용합니다. 정적인 화단 edge를 공유하고 좁은 통로 양방향 비킴·회피 후 경로 재연결·첫 횡단 우선/만석 후 산책 재시도를 사용합니다. 실제 화단/soil은 인도 방향으로 맞춥니다. 차량/보행이 비운 뒤 다음 흐름을 시작하고 인원 감소 시 횡단 중인 사람은 인도에 도착한 뒤 빠집니다. Crossing-0.4.0/Generated/Crossing040은 이전 장면을 보존하며 동일prototype appID/기존 서명으로 업데이트합니다. PC compile·4/32/100명200/600/600초 production model·실제100rig/화단·GPU·Android BuildPlayer/Lint(errors0/warnings8)·다운로드 실제APK/원본v2서명 검증을 완료했습니다. 모든 사람이 최소1회 횡단했고223횡단/33신호주기를 확인했습니다. 실제 폰 결과는 아직 별도입니다. 최신 결과와 PowerShell은 [STATUS](../docs/STATUS.md)를 확인합니다. 실제 폰 FPS·발열·WallpaperService/기존 날씨·영구 설정 저장은 후속입니다.

## 이전 패치 — 3D 차량·양방향 교통 0.3.0 (2026-10-09)

세단·스포츠 쿠페·SUV·택시의 경사진 차체/유리·앞뒤 램프·회전하는4바퀴를 실제 metre 단위로 생성합니다. 차선별 root scale1로 폭/높이를 압축하지 않습니다. 4차선×6대=24대, 왼쪽은 다가오고 오른쪽은 멀어지며 고정 lane별 속도/순환간격을 유지합니다. 차체 mesh는4모델이 재사용하고 재등장 때 새 객체를 생성하지 않습니다.

Traffic-0.3.0.unity/Generated/Traffic030 장면·자산은 이전 버전을 보존합니다. prototype appID/기존 서명으로0.2.0을 업데이트하며 native 앱0.48.0과 공존합니다. PC compile/scene·10분 교통 검사·실제 Editor 렌더·BuildApk/Lint(errors0/warnings8)·기존v2서명 검증을 완료했습니다. 최신 결과와 설치 명령은 [STATUS](../docs/STATUS.md), [자동 빌드 안내](../docs/UNITY_AUTOMATION.md)를 확인합니다. 이전0.3.0 시점의 후속 목록은 신호/군중/날씨·라이브 배경화면 연결이었으며 신호/군중은 위0.4.0에 반영했습니다. 실제 폰 FPS/발열·날씨·라이브 배경화면 연결은 후속입니다.

## 이전 패치 — 도시 배경·조명 0.2.0 (2026-10-09)

0.1.0의 실제 폰 첫 화면 표시를 확인한 뒤 목표 시안의 거리 깊이·오후빛·도시 디테일을 구현합니다. 420m 도로와 넓은 지면, 먼 skyline/산 능선, 24개 건물의 창틀·차양·옥상 설비, 32가로수의 겹친 수관과 화단, 20가로등, mipmap 노면·석재 재질, 따뜻한 단일 태양 및 접지 그림자입니다. 차량 주행·차종 확장과 배경화면 연결은 후속 단계입니다.

Unity6000.3.26f1/URP17.3.0, appID com.s20plus.pixeltraffic.unityprototype/0.2.0/code2/ARM64/min29/기존 서명을 유지합니다. 새 장면 City-0.2.0.unity와 Generated/City020 자산은 기존 FirstRoad와 분리합니다. 버전·APK 파일명/사후검사는 StarterConfig의 버전 상수를 사용합니다. 최신 실제 PC 빌드·Lint·서명 결과는 [현재 상태](../docs/STATUS.md)를 확인합니다. 이 항목을 작성할 때는 검사 실행 전이며 구현을 검사 통과로 간주하지 않습니다.

`tools/run-unity.ps1 -Mode Capture`는 GPU를 사용하는 Editor camera PNG를 Reports에 저장합니다. 실제 Android 폰 스크린샷/FPS 테스트가 아니며 cloud runner에서 그래픽 장치가 없으면 캡처를 생략할 수 있습니다(`PIXEL_TRAFFIC_CAPTURE=false`). APK의 필수 build/Lint/cert 검사는 그대로 유지합니다. 앱 설치·실행은 [자동화 안내](../docs/UNITY_AUTOMATION.md)의 Unity SDK adb.exe 직접 호출 방식과 새 파일명을 사용합니다.

## Camera 컴파일 오류 수정 — 0.1.0 fix1 (2026-10-09)

사용자 Safe Mode Console에서 `Camera`에 `AddComponent`가 없다는 CS1061 오류를 확인했습니다. 저장소 `StarterScene.cs` 78행에서 Camera 컴포넌트에 메서드를 호출한 것이 원인입니다. `camera.AddComponent<UniversalAdditionalCameraData>()`를 `camera.gameObject.AddComponent<UniversalAdditionalCameraData>()`로 수정했습니다. 같은 파일의 다른 AddComponent 호출은 GameObject를 대상으로 하고 있어 변경하지 않았습니다.

기존 프로젝트에는 아래 PowerShell을 실행해 한 줄만 수정합니다. 현재 Unity 창을 열어둬도 저장 후 재컴파일됩니다. `.cs.bak`은 C# 스크립트로 컴파일되지 않는 원본 백업이며 기존 `.meta`/GUID와 나머지 로컬 코드는 유지합니다. 프로젝트 폴더를 다른 위치에 풀었다면 첫 줄의 경로를 실제 위치에 맞춥니다.

```powershell
$sceneFile = "D:\Unity\Projects\pixel-traffic-unity\Assets\PixelTraffic\Editor\StarterScene.cs"
$sceneCode = Get-Content -LiteralPath $sceneFile -Raw -ErrorAction Stop
if ($sceneCode.Contains("camera.AddComponent<UniversalAdditionalCameraData>();")) {
    Copy-Item -LiteralPath $sceneFile -Destination "$sceneFile.bak" -ErrorAction Stop
    $sceneCode = $sceneCode.Replace("camera.AddComponent<UniversalAdditionalCameraData>();", "camera.gameObject.AddComponent<UniversalAdditionalCameraData>();")
    [System.IO.File]::WriteAllText($sceneFile, $sceneCode, [System.Text.UTF8Encoding]::new($false))
}
```

오류가 사라지면 자동으로 정상 모드에 진입하는지 확인하고, 안전 모드가 남아 있으면 `Exit Safe Mode`를 누릅니다. 이후 Prepare First Scene → Validate First Scene → Play를 재개합니다. 다른 빨간 오류가 남으면 첫 오류 상세를 확인합니다. 정상 import/Play는 사용자 결과를 받기 전까지 확인 대기입니다.

수정본 전체 ZIP은 `/workspace/artifacts/pixel-traffic-unity-starter-0.1.0-fix1.zip`이며, 원래 ZIP은 이전 전달 기록으로 보존합니다. 버전/패키지/서명/기존 Android 앱 입력은 바꾸지 않습니다. 클라우드에서는 단일 소스 수정 및 ZIP 일치만 검사하며 Unity C# 컴파일/Windows 명령 실행을 대신하지 않습니다.

Unity **6000.3.26f1** / Universal Render Pipeline **17.3.0**용 첫 소스 프로젝트입니다. 도로·보도·나무·건물과 간단한 3D 차량 한 대의 형상/접지/그림자·직선 주행을 확인하는 단계입니다. 브랜드 차량·완성 도시 시안의 품질, 기존 신호/보행/날씨 로직, 라이브 배경화면 연결은 다음 단계입니다.

이 ZIP은 이 클라우드에서 Unity Editor로 컴파일/렌더한 결과물이 아닙니다. 아래 절차로 사용자 PC에서 실제 import/장면 검증을 수행합니다. 기존 Android 앱0.48.0은 그대로 사용할 수 있습니다.

## 처음 열기

1. ZIP을 `D:\Unity\Projects`에 풀어 `D:\Unity\Projects\pixel-traffic-unity`에 Assets/Packages/ProjectSettings가 있는지 확인합니다. 기존 폴더와 합쳐 덮어쓰지 않고 새 폴더를 사용합니다.
2. Unity Hub의 **프로젝트 → 추가(Add) → 디스크의 프로젝트 추가**에서 `pixel-traffic-unity` 폴더를 선택합니다. **6000.3.26f1**로 엽니다.
3. 패키지 import와 C# 컴파일이 끝나면 Editor 상단 **Pixel Traffic → 1. Prepare First Scene**을 누릅니다. 장면/URP/재질이 생성됩니다. 기존 FirstRoad가 있으면 다시 만들지 않고 열기만 합니다.
4. **Pixel Traffic → 2. Validate First Scene**을 실행합니다. 성공하면 Console에 PASS, `Reports/scene-validation.json`이 생깁니다.
5. **Game** 창의 화면 비율을 **9:16**으로 설정하고 ▶ Play를 누릅니다. 파란 차량 한 대가 카메라 쪽으로 곧게 달려야 합니다. 이 첫 화면을 확인한 뒤 디자인과 Android 연결을 진행합니다.

첫 화면은 형상/엔진 검증용이며 생성한 목표 시안보다 단순합니다. 3D 모델·재질·도시 디테일은 단계별로 높입니다. Editor 자체의 기본 설정 파일과 패키지 lock은 첫 import에서 Unity가 생성합니다. `.meta`는 소스와 함께 제공됩니다.

Console에 빨간 오류가 있으면 첫 오류의 전체 내용이나 `Reports`의 Editor 로그를 공유합니다. 오류 상태에서는 실제 Unity 검증이 통과한 것으로 기록하지 않습니다. 빈 화면이면 Prepare First Scene/카메라/Game창을 먼저 확인합니다.

## Windows 실행 도구

PC에서 시작하고 나중에 클라우드로 이동할 공통 `tools/run-pipeline.ps1`과 GitHub Actions 설정을 준비했습니다. [PC 등록/실행/전환 안내](../docs/UNITY_AUTOMATION.md). 사용자 PC에서 batch Validate completed와 GitHub runner Listening for Jobs를 확인했습니다. main의 Unity 변경은 자동 Validate이며 실제 Actions 결과와 APK 빌드는 다음 확인 단계입니다.

Hub에서 여는 방법과 별도로 `tools/run-unity.ps1`을 제공합니다. 기본 Editor 설치 경로를 찾고, 경로가 다르면 **Editor의 Unity.exe**를 `-UnityExe`로 전달합니다. Hub의 Unity Hub.exe 경로와 다릅니다.

```powershell
# 선택사항: PowerShell로 프로젝트 열기
& "D:\Unity\Projects\pixel-traffic-unity\tools\run-unity.ps1" -Mode Open

# Editor를 닫은 뒤 한 번 생성·검증 (열려 있는 프로젝트와 동시에 실행하지 않습니다)
& "D:\Unity\Projects\pixel-traffic-unity\tools\run-unity.ps1" -Mode Prepare
```

PowerShell 실행 정책으로 막히면 Hub/Editor의 메뉴 방법을 사용할 수 있습니다. 시스템 전체 실행 정책을 바꿀 필요는 없습니다.

## Android 단계

Editor의 **Pixel Traffic → 3. Export Android Project**는 `Builds/AndroidExport`에 Gradle 프로젝트를 내보냅니다. `launcher`/`unityLibrary`와 선택한 Activity 진입점, 실제 UnityPlayer API를 확인한 뒤 WallpaperService의 Surface/가시성·잠금/복귀·배경화면 미리보기 연결을 구현합니다. 이 단계의 export/APK는 일반 실행 앱이며 아직 라이브 배경화면으로 등록되지 않습니다.

실험 앱ID는 `com.s20plus.pixeltraffic.unityprototype`, 버전0.1.0/code1/min29/ARM64/IL2CPP입니다. 기존 `com.s20plus.pixeltraffic`와 공존합니다. 설치된 Unity Android 의존성의 target SDK를 사용합니다. 정식 이전에서는 기존 앱ID·설정/프리셋·서명을 유지합니다.

**Pixel Traffic → 4. Build Signed Activity APK**는 기존 서명키를 확인한 뒤만 빌드합니다. Unity를 시작하기 전에 개인 환경에 다음 변수를 설정합니다. 비밀번호 값은 프로젝트·ZIP·Git에 넣지 않습니다.

- `PIXEL_TRAFFIC_KEYSTORE`: 기존 debug.keystore의 로컬 전체 경로
- `PIXEL_TRAFFIC_KEYSTORE_PASSWORD`: 기존 키 저장소 비밀번호
- `PIXEL_TRAFFIC_KEY_ALIAS`: 기본 androiddebugkey
- `PIXEL_TRAFFIC_KEY_PASSWORD`: 생략하면 키 저장소 비밀번호 사용

Unity의 OpenJDK/keytool로 인증서SHA256 `a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`을 확인하고 다르면 중단합니다. 비밀번호를 명령 인자/로그에 출력하지 않습니다. 빌드 후 원래 Editor 서명 설정을 복원합니다. APK는 `Builds/pixel-traffic-unity-prototype-0.1.0.apk`에 생성되며 `Reports/android-build-result.txt`에 결과를 남깁니다. **이번 전달에는 APK가 포함되지 않습니다.** 실제 build/metadata/서명 검증 후 APK를 전달할 때 최신 Windows 다운로드 폴더의 설치 명령을 함께 제공합니다.

## 검증 구분

클라우드에서 확인한 것은 소스 파일·버전/manifest/meta·ZIP의 완전성·기존 앱 입력 보존입니다. Unity C# 컴파일/URP 렌더·Android 빌드·서명·기기30FPS/발열과 배경화면 연결은 사용자 Editor/기기에서 확인 대기입니다.

Editor의 장면 검사는 실제 renderer/material·차량 축/전체차체 차선 외곽·바퀴4개·접지/높이,0시간·경계wrap/여러loop·15/30/60/120Hz 주행 위상·잘못된 시간을 확인합니다. 이 검사가 통과해도 Android 동작/배터리 검증을 대신하지 않습니다. 프레임당 새 차체/재질/게임오브젝트를 만들지 않으며 Update는 transform과 바퀴만 갱신합니다.

[목표 시안](Reference/unity-3d-target.png), [단계별 검증 순서](Reference/NEXT_STEPS.md). 저장소의 docs/UNITY_MIGRATION.md·STATUS.md·WORK_LOG.md에도 같은 진행 상태와 검증 결과를 기록합니다.
