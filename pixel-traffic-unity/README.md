# Pixel Traffic Unity — 시안 카메라 구도 0.11.0

## 현재 패치 — 시안 카메라 0.11.0 (2026-10-10)

사용자가 다시 제공한 시안의 도로 원근과 화면 배치에 맞춰 카메라를 조정했습니다. 덜 내려다보는 각도, 오른쪽 위 소실점, 아래쪽 횡단보도, 전경 차량과 상단 배경 여백을 담습니다. 위치(1.4,14,-37)/target(-4.9,1,68)/44°FOV. 그림의 강·구름·건축 외형 상세는 다음 작업이며 일부 수관 가림과 단순한 원경은 남아 있습니다.

**0.11.0/code12**,`ReferenceCamera-0.11.0.unity`/`Generated/ReferenceCamera0110`,`Builds/pixel-traffic-unity-prototype-0.11.0.apk`. 실제9:20/9:16 projection·ground/sky rays,기존 교통/보행/날씨·100우산 예산,같은상태의old/new GPU 비교를검증했습니다. 실제GPU39PNG·AndroidBuildPlayer/launcherLint0errors/경고8·원본v2cert/version/manifest/hash PASS. 최대100우산119,922tri/1515renderer/47material,unchangedhost기존Lint0errors/10warnings재사용. 실제폰0.11.0/FPS·발열은설치후확인합니다.

## 모바일 APK 다운로드

[Unity0.11.0 APK 다운로드](https://github.com/BACKHYUN96/S20-PLUS/raw/ff54aa438e6f782b727b566e619b8d826bd985fc/pixel-traffic-unity-prototype-0.11.0.apk) — 크롬 또는 삼성 인터넷에서 링크를 열어 받습니다. 로그인 없는 HTTP200 다운로드와 원본APK SHA 일치를 확인했습니다. WORK가 작업파일을 열 때 app-server 응답 오류가 나면 이 일반https 링크를 사용합니다. [실제 Unity 화면 미리보기](https://github.com/BACKHYUN96/S20-PLUS/raw/ff54aa438e6f782b727b566e619b8d826bd985fc/camera-preview.png)도 볼 수 있습니다. 앱APK는 29788679bytes, 원본서명이며 자동 release 설정은 추가하지 않았습니다. [설치·검증기록](../docs/STATUS.md).


## 이전 패치 — 카메라·전조등/브레이크등·깜빡이 0.10.0 (2026-10-10, 검증 완료)

카메라를 낮고 가까운 세로 구도로 바꾸고24대의야간전조등/도로빛·제동등과방향에맞는깜빡이를 구현했습니다. 같은방향옆차선의뒤60m관측차가전부지나가고앞뒤안전거리가확보되면1.8초간깜빡이를3번표시한뒤3초동안부드럽게차선을변경합니다. 새뒤차가깜빡이중접근하거나간격이없으면취소/대기하며비바람과횡단보도근처에서는새변경을시작하지않습니다. 일부상가의나무가림은남습니다.

**0.10.0/code11**,`Driving-0.10.0.unity`/`Generated/Driving0100`,`Builds/pixel-traffic-unity-prototype-0.10.0.apk`. 실제600초변경11회·최저gap1.800000m·실제등화MPB와15/30/60/120Hz x/z/merge·pause검사,기존보행/날씨/외형을통과했습니다. D3D11 GPU PNG36장과AndroidBuildPlayer/launcherLint 오류0/경고8,실제다운로드원본v2cert/version/hash/manifest를확인했습니다. 최대100우산119,922tri/1515renderer/47material로strict예산유지,tri여유78개입니다. unchangedhost기존Lint0errors/10warnings재사용. 폰FPS/발열·홈/잠금 lifecycle은설치후확인합니다. [검증/설치](../docs/STATUS.md),[자동화](../docs/UNITY_AUTOMATION.md)를참조합니다.

## 이전 패치 — 상가·창문 조명 0.9.0 (2026-10-10, 검증 완료)

건물24개의 1층에 카페·편의점·일반 매장8개씩을 추가했습니다. 진열 창문·출입문/손잡이·간판·OPEN 포스터와 차양을 만들고, 상층480개 창문에 불 꺼진 방과 따뜻한/차가운 빛·커튼/블라인드를 섞었습니다. 건물별 합친메시와 공유256×128 창문/512×512 상가 atlas를 사용하며 실제조명4개를 유지합니다. 노을부터 매장과 창문 조명이서서히 밝아지며 기존낮→노을→밤/밤→낮과날씨·신호·보행/차량을유지합니다.

**0.9.0/code10**,`Storefront-0.9.0.unity`/`Generated/Storefront090`,`Builds/pixel-traffic-unity-prototype-0.9.0.apk`. 실제Unity장면·시간shader검사와D3D11 GPU PNG28장(상가낮/밤6장),AndroidBuildPlayer/launcherLint 오류0/경고8,내려받은APK 원본v2cert/version/manifest/hash를확인했습니다. 최대100우산119,634tri/1443renderer/47material로기존예산을유지합니다. Nativehost입력은같아기존Lint 오류0/경고10을재사용했습니다. 폰FPS/발열은설치후확인합니다. [검증·설치명령](../docs/STATUS.md),[자동화](../docs/UNITY_AUTOMATION.md)를참조합니다.

## 이전 패치 — 차량 외형 개선 0.8.0 (2026-10-10, 검증 완료)

0.7.0 실제 폰 적용 성공 이후 Sedan/SportCoupe/SUV/Taxi4종의 보닛·지붕 곡면,둥근 타이어와 깊이 있는 tapered 휠 spoke,보이는 fender lip/모서리를 다듬은 램프/유리 gradient와 highlight를 추가했습니다. 차량24대의차폭·차고·타이어접지/unit scale과기존시간·날씨/신호·보행을유지합니다. 유리는64×64 공유 authored texture이며 실시간 반사 probe가 아닙니다. 기존14renderer/car/46material 구조와100우산 포함119394tri/2331renderer/46material 예산을확인했습니다.

버전 **0.8.0/code9**,`VehicleDetail-0.8.0.unity`/`Generated/VehicleDetail080`,`Builds/pixel-traffic-unity-prototype-0.8.0.apk`입니다. 실제PC geometry/traffic/climate검사와D3D11 EditorPNG22장/모델front·rear8장,AndroidBuildPlayer 오류0/경고0,launcherLint 오류0/경고8,내려받은APK의v2기존cert/version/manifest를검증했습니다. NativeAndroid/Bridge/host/shader SHA불변으로실제hostLint 오류0/경고10을재사용했습니다. 확대camera/차량pose는촬영용unsaved scene이며폰camera에는적용하지않습니다. 실제폰FPS/발열은설치후확인합니다. [최신검증·설치명령](../docs/STATUS.md),[자동화기록](../docs/UNITY_AUTOMATION.md)을참조합니다.

## 이전 패치 — 노을 경유·날씨 반응 도시 0.7.0 (2026-10-10, 검증 완료)

0.6.0 폰 적용 성공 이후, 낮에서 야간을 선택하면 **낮→노을 2초→밤 2초**로 전환합니다. 밤→낮과 다른 선택은 기존4초 전환입니다. 중간 재선택·같은 선택 반복·숨김 정지·저장한 상태 시작을 유지합니다.

비·눈·안개·비바람은 사람/차량 통행량과 운전 속도를 부드럽게 바꿉니다. 비바람의 사람 목표는 설정 인원의30%(최소4명), 차량은 차선당3대/총12대이며 기존 고정 pool100명/24대를 재사용합니다. 화면에 보이는 보행자는 인도 퇴장 경로를 걷고, 횡단 중이면 먼저 인도로 건넙니다. 날씨로 빠지거나 다시 등장하는 순간은 카메라 bounds 밖이며 복귀는1.5초 간격입니다. 감소가 즉시 완료되는 것은 아니며 퇴장하는 데 시간이 걸립니다. 비에는 공유 mesh 우산, 비바람에는 기울어진 우산과 최대18% 빠른 걸음, 악천후에는 감속과 비의 강도/차량 속도에 따른 바퀴 물보라를 추가합니다. 정지 차량/맑음은 물보라를 끕니다. 우산100개·물보라96개 fixed pool과 기존 GPU 예산을 검사합니다.

버전 **0.7.0/code8**, `WeatherLife-0.7.0.unity`/`Generated/WeatherLife070`, `Builds/pixel-traffic-unity-prototype-0.7.0.apk`를 사용합니다. NativeAndroid 서비스/UI·설정 저장소/appID와 v2 인증서는 유지합니다. 실제 검사·APK 결과는 [STATUS](../docs/STATUS.md), [자동화 기록](../docs/UNITY_AUTOMATION.md)의 최신 항목을 확인합니다. 실제 PC 검사·D3D11 PNG14장·Android BuildPlayer 오류0/경고0, launcher Lint 오류0/경고8, 다운로드한 원본 v2 서명·버전 검증을 완료해 전달했습니다. Java/res/Bridge/host 입력이0.6.0과 동일해 host Lint 오류0/경고10 결과를 재사용합니다. 실제 폰 표현/성능은 설치 후 확인합니다.

## 이전 패치 — 시간대·날씨 전환 0.6.0 (2026-10-10 KST)

0.5.1 실제 폰 적용 성공 이후 추가한 시간대·날씨 설정입니다. 설정 앱에서 **낮/노을/야간**과 **맑음/약한 비/강한 비/눈/안개/비바람**을 각각 선택합니다. 처음 실행할 때는 저장된 상태로 시작하고 이후 변경은 기존 앱과 같은 **4초 smoothstep**으로 밝기·색·안개·젖은 노면·조명·날씨 효과가 이어집니다. 전환 중 다시 선택하면 그 순간의 상태에서 새 목표로 진행합니다. 화면이 꺼지거나 배경화면이 숨겨진 동안에는 전환과 효과 시간도 멈춥니다. 수동 선택이며 실제 시각이나 기상 정보를 가져오지 않습니다.

노을·야간에는 가로등/창문/차량 라이트와 신호등 emission이 보입니다. 가까운 추가 조명은 그림자 없는 spot 4개로 제한합니다. 강한 비와 비바람은 비/노면 젖음, 눈은 고정 snow pool/노면 색, 안개는 먼 도시 가시거리를 변화시킵니다. 비바람에는 32개 나무 canopy 흔들림과 3~5초의 활성 시간 간격으로 날리는 신문지 1장을 추가합니다. 비/눈 mesh pool은 각각 160/96개이며 날씨 전환에 따라 투명도가 이어집니다. 이번에는 날씨별 차량/인원 감소를 추가하지 않았고 기존 24대/4~100명/절전 설정을 유지합니다.

버전은 **0.6.0/code7**, APK `Builds/pixel-traffic-unity-prototype-0.6.0.apk`입니다. `Climate-0.6.0.unity`/`Generated/Climate060`을 사용해 이전 장면을 보존합니다. appID와 기존 v2 서명이 같아 업데이트 설치하며 설정은 같은 저장소를 사용합니다. 실제 PC 검사·D3D11 렌더·Android BuildPlayer 오류0/경고0, launcher Lint 오류0/경고8 및 이번 Java/res의 unityLibrary Lint 오류0/경고10, 기존 v2 서명을 확인했습니다. 실제 검사 결과·설치 명령과 미확인 폰/FPS 범위는 [최신 상태](../docs/STATUS.md), [자동화 기록](../docs/UNITY_AUTOMATION.md)를 확인합니다. 설정 앱의 **배경화면 미리보기 및 적용**을 눌러 시스템 화면에서 적용합니다.

## 이전 패치 — 횡단보도 차선 보정 0.5.1 (2026-10-10 KST)



0.5.0의 실제 폰 적용 성공을 확인한 뒤, 횡단보도 내부에 겹친 노란 중앙선과 흰색 차선만 제거합니다. 횡단보도 바깥 차선·stop line·12개 흰 stripe와 도시/차량/보행/배경화면 설정을 유지합니다. 새 Wallpaper-0.5.1/Generated/Wallpaper051을 사용해 이전 장면을 보존하며 appID·기존 서명은 동일합니다. APK는 `Builds/pixel-traffic-unity-prototype-0.5.1.apk`이며 code6입니다.

기존 CityEnvironment 장면 검사에서 실제 renderer bounds 비중첩을 확인합니다. PC 장면/Editor PNG·Android 빌드/Lint·실제 APK 서명/버전 결과와 집 PC 설치 명령은 [최신 상태](../docs/STATUS.md)를 확인합니다. 변경되지 않은 NativeAndroid/host/model/vehicle SHA와 이전 host Lint 결과를 재사용합니다. 다음 추천은 낮·노을·야간 및 조명 전환이며 이번 버전에 추가하지 않습니다.

## 이전 패치 — 라이브 배경화면·저장 설정 0.5.0 (2026-10-10 KST)

0.4.0의 도시·차량·신호·인도 보행을 Android 라이브 배경화면으로 연결합니다. 앱을 열면 한국어 설정 화면이 나옵니다. 보행자 4~100명(4명 단위, 기본 32명)과 절전 모드를 자동 저장하며, **배경화면 미리보기 및 적용** 버튼으로 Android의 미리보기를 엽니다. 실제 적용은 시스템 화면에서 선택합니다. 기본 30 FPS, 절전 설정 또는 기기 절전 시 15 FPS는 목표값이며 측정 FPS가 아닙니다.

WallpaperService는 별도 `:wallpaper` 프로세스에서 Unity 실행 하나를 공유합니다. 보이는 유효한 Surface만 연결하고 미리보기를 우선하며, 숨김·화면 꺼짐에는 pause/Surface 해제를 수행합니다. 인원 설정은 비공개 같은 UID ContentProvider를 통해 전달합니다. 기본 설정 Activity에는 Unity 렌더러를 만들지 않습니다. Wallpaper-0.5.0/Generated/Wallpaper050을 사용하며 기존 prototype appID와 서명을 유지합니다. native 앱 0.48.0과 공존합니다.

PC 빌드·Lint·실제 APK 서비스/서명 검증 결과와 최신 설치 명령은 [현재 상태](../docs/STATUS.md)를 확인합니다. 실제 S20+/S26 Ultra 홈·잠금 화면의 미리보기/적용, 숨김 복귀, 재부팅 후 복원, 설정 저장, FPS·발열은 기기에서 확인해야 합니다. Editor PNG는 실제 폰 홈 화면 캡처가 아닙니다.

## 현재 빌드와 실행

Unity 6000.3.26f1/Android 모듈을 사용합니다. `tools/run-pipeline.ps1 -Operation Validate`로 장면·production 모델·manifest 템플릿 검사를 수행하고, `-Operation BuildApk`로 Android 빌드/Lint/원본 v2 인증서 및 APK 메타데이터를 검사합니다. Gradle 생성 후 `AndroidWallpaperBuild`가 `NativeAndroid/src`와 `res`를 복사하고 service/provider/설정 Activity를 등록합니다. Export에도 같은 처리를 적용합니다. 실제 Android 컴파일은 PC BuildApk 결과로 확인합니다. 새 NativeAndroid 모듈은 `tools/check-wallpaper-lint.ps1`로 unityLibrary Lint를 추가 확인합니다. 이 도구는 생성된 Java/리소스 SHA가 현재 소스와 같은지 먼저 확인하며 APK를 다시 만들지 않습니다. 0.11.0의 실제 launcher Lint는 오류0/경고8입니다. 변경 없는 NativeAndroid/host 입력은0.6.0 SHA를 확인하고 실제 unityLibrary Lint 오류0/경고10을 재사용합니다.

Editor 메뉴 **4. Build Signed Wallpaper APK**를 사용할 수 있습니다. APK는 `Builds/pixel-traffic-unity-prototype-0.11.0.apk`에 생성됩니다. 기존 `BuildActivityApk` 메서드 이름은 PowerShell 호출 호환용으로 유지하며 결과 앱은 라이브 배경화면입니다. 서명 환경 변수 및 PC runner 설정은 아래 초기 기록과 [자동화 안내](../docs/UNITY_AUTOMATION.md)를 참조합니다. 최신 안내가 아래 초기 0.1.0 기록보다 우선합니다.

## 이전 기록 — 0.4.0 및 초기 설정
## 이전 패치 — 신호·인도 보행·횡단 0.4.0 (2026-10-10 KST)

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
