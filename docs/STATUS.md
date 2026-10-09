# 현재 상태

## 2026-10-10 — Unity 0.6.0 검사 후 빌드 장면 초기화 보강 (KST)

wind sampling 보정 sourcee41a272cf6241728f5d8a24759296aa2d31bcc92의 실제 검사는 진행 중이다. 코드 검토에서 ClimateChecks.Initialize가 열린 Editor 장면의 Renderer/MeshFilter에 임시 runtime material/mesh를 연결한 채 BuildPlayer로 이어질 수 있음을 확인했다. BuildPlayer의 열린 scene 처리에 기대지 않도록 StarterScene.Validate 성공 후 저장된 ScenePath를 다시 열고 CityClimate.TimeBlend가 미초기화 null인지 확인한다. 소스 장면은 이미 검사 전 저장되어 있고 임시 검사 상태는 저장하지 않는다. 원래 재질 참조와 Android 저장 설정으로 player가 초기화되도록 보장하는 수정이며 실제 폰에서 발생한 버그라고 주장하지 않는다. Editor 임시 material/mesh는 앞서 OnDestroy에서 edit/play API를 구분해 정리한다. 변경은 StarterScene.cs와 진행 문서, 다음 실제 검사/빌드로 확인한다.

## 2026-10-10 — Unity 0.6.0 바람 검사 샘플링 보정 / 재검사 대기 (KST)

source26702d17e8b0f8b3056fc358be93a758baa922e5의 PC Validate38006299543/job114075768071에서 생성 재질 연결·기존 fleet/street/budget 검사·4초 oracle·18 endpoint·숨김/재선택 검사까지 진행한 뒤 단일 canopy의 시작/30초 끝 quaternion 비교에서 실패했다. 주기적인 흔들림은 끝에 비슷한 각도로 돌아올 수 있고 Quaternion.Angle의 float 분해능보다 작은 차이(해당 위상 계산에서 약0.02~0.03도)가0으로 판정될 수 있어, 두 끝점 비교가 움직임 유무의 근거로 부족했다. ClimateChecks.cs에서 32개 실제 canopy를 30초 동안 매0.1초 관찰해 각각의 최대 변위가1도 이상인지 확인하고 최소 측정 sway를 보고한다. Runtime wind 코드는 바꾸지 않았다. 실제 rain/snow renderer 활성 상태도18 endpoint에서 확인한다. artifact11651308245/8861bytes ZIP SHA256490cd06470ddc1334096d8539f2efe12e58286fa8771e2893ef087d440a28d55/CRC를 확인했다. 다음 재검사에서 실제 측정값을 기록한다.

## 2026-10-10 — Unity 0.6.0 생성 재질 연결 수정 / 재검사 대기 (KST)

source6a2d13a4bfd1c4c1fcf24dd474231791823d83da의 PC Validate38006052374/job114074980425에서 C# 컴파일은 exit0으로 통과했으나 ClimateScene.Create의 Climate surface missing 예외로 장면 생성이 중단됐다. Renderer.sharedMaterial.name 표시 이름 비교를 제거하고 StarterScene.Generated의 기존 확정 .mat 경로 6개를 AssetDatabase.LoadAssetAtPath로 직접 연결한다. Unity import/display name에 의존하지 않으며 누락 예외에는 asset 이름을 포함한다. 변경은 ClimateScene.cs 하나와 진행 문서다. 실패 artifact11651471415/6383bytes ZIP SHA2561b1a04666c1bc1e075bbd6a77733cf5aac22cd61dc8aa457fc44ccd3ee6bcd57/CRC를 확인했다. 다음 실제 재검사가 장면 연결·새 전환 검사를 확인한다.

## 2026-10-10 — Unity 0.6.0 추가 조명 API 컴파일 수정 / 재검사 대기 (KST)

source2197d37c5f30f9158829c5b4cfeb514c6130357c의 PC Validate38005928444/job114074595963에서 StarterScene.cs117의 CS0200으로 실패했다. URP17.3 additionalLightsRenderingMode는 읽기 전용이므로 기존 shadow 필드처럼 SerializedObject의 m_AdditionalLightsRenderingMode를 PerPixel로 설정한다. maxAdditionalLightsCount4 setter는 컴파일 오류가 없다. 실패 보고서 artifact11651540790/2081bytes ZIP SHA256 e01a92e45f31971413a0f0031eece7800890da2bea654203c3bb261a50cff75b/CRC를 확인했다. 동반 Licensing 로그는 entitlement 해결 후 실제 C# 컴파일까지 진행했으므로 실패 원인으로 판단하지 않는다. 변경은 StarterScene.cs 하나와 진행 문서이며 실제 재검사 결과는 다음 기록에 추가한다.

## 2026-10-10 — Unity 0.6.0 시간대·날씨 자연스러운 전환 구현 / 실제 검사 대기 (KST)

사용자가 0.5.1 실제 폰 적용 성공을 확인하고 다음 패치로 기존 앱과 같은 부드러운 시간대·날씨 변경을 요청했다. base d1d64e74b85db701647d4678dfbfd95c3bfecadd에서 0.6.0/code7을 구현한다. 아직 실제 Unity/Android 검사 결과를 완료로 기록하지 않는다.

- 원본 native ThemeBlend/RoadWetness/CinematicScene/WindStorm의 4초 smoothstep, 현재 가중치에서 재선택, 초기 저장 설정 snap, 0.1초 frame cap, active-time 계약을 읽고 SceneBlend/CityClimate로 옮겼다. 낮/노을/야간 3종과 맑음/약한 비/강한 비/눈/안개/비바람 6종은 독립적으로 4초 전환한다. Android provider로 두 설정을 저장/공유하며 기존 인원/절전 키와 appID를 유지한다. 수동 설정이며 실제 시각/기상 API는 연결하지 않는다.
- 조명·ambient·먼 안개·노면 색/젖음·창문/차량/가로등 emission을 혼합한다. 추가 shadowless spot light는 가까운 4개로 제한하고 20개 가로등의 따뜻한 노면 빛을 공유 quad로 그린다. 160 rain/96 snow 고정 mesh pool, 비바람 안개/32 canopy 흔들림과 3~5 active seconds 간격 신문지 1장을 추가한다. 나무의 canopy만 static flag를 풀어 실제 움직이게 하고 도로/차량/보행/장애물 좌표는 유지한다.
- 범위: 신규 Runtime SceneBlend/CityClimate/ClimateEffects, Editor ClimateScene/ClimateChecks 및 5 meta; StarterConfig/StarterScene/AndroidWallpaperBridge/CityPreview, 신호등 emission용 StreetSimulation 1줄, NativeAndroid WallpaperPreferences/WallpaperSettingsActivity/values XML. Generated/Climate060와 Climate-0.6.0.unity로 이전 장면을 보존한다. CityEnvironment/StreetModel/SidewalkRoutes/VehicleGeometry/Surface host 소스는 변경하지 않는다. dirty/index 시작 상태는 unity-0.6.0-start-state.json에 기록했다.
- 검사는 15/30/60/120Hz의 독립 smoothstep oracle 180경우, 재선택/invalid delta/frame cap/숨김 시간 정지, 실제 18개 scene endpoint/젖음/나무/신문지 cadence를 포함한다. 기존 fleet/횡단/인원/100명 budget 검사도 pipeline 필수 묶음으로 실행한다. 실제 D3D11 540×1200 selected climate endpoint 6종과 0/2/4초 전환 PNG를 캡처한다. 이는 Editor 렌더이며 폰 스크린샷/FPS 검사가 아니다.
- 다음: 실제 PC Validate → signed APK/launcher Lint → 변경된 Java/res를 unityLibrary host Lint로 검사 → 다운로드 ZIP/실제 APK CRC/hash/certificate/버전/manifest와 렌더 관찰 → 전달/최종 문서. 실패가 있으면 원인과 수정/재검사를 추가 기록한다.

## 2026-10-10 — Unity 0.5.1 횡단보도 차선 보정 APK 전달 (KST)

사용자가 **0.5.0 실제 폰 적용 성공**을 확인했다. 요청한 횡단보도 내부 중앙선·흰색차선만 제거한 **0.5.1/code6**을 전달한다. 실제0.5.1 폰 보정 표시는 설치후확인한다. 다음은 **낮·노을·야간과 가로등/창문/차량 라이트 전환**을 추천하며 이번패치에는구현하지않았다.

### 변경과 과정

- CityEnvironment.cs에서 횡단보도z8~12m내의CentreLine/LaneDash만clip한다. 바깥중앙선/흰dash·12zebra/stop line·도시/차량/보행/배경화면/저장설정을유지한다. 원인은 표시를횡단보도와관계없이도로전체길이로생성했던것이다. 기존장면검사에서 실제12stripe/94차선 renderer bounds 비중첩을확인한다.
- StarterConfig.cs 버전0.5.1/code6/Wallpaper-0.5.1.unity, StarterScene.cs Generated/Wallpaper051로이전장면을보존한다. 실제source변경은3C#파일과문서뿐이다. 기존dirty native/index를보존한다.
- 원격base85166199a0c0c4771618a10c53e52fa42e44b4a0 → source **40fc91cc78f4ebfb173634bafb75bc677ec7b8f1**의main Validate성공 → 같은source **unity-apk-0.5.1-build1**태그APK완료. 신규독립테스트/무관한native검사를추가하지않고기존pipeline필수검사묶음을사용했다. 실패한compile/build는없다.

### 실제 확인

| 확인 | 결과 |
| --- | --- |
| PCUnity6000.3.26f1 컴파일/장면 | [Validate38003973846](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38003973846), job114068403182 success. 실제12stripe와94차선 bounds 비중첩PASS |
| 기존도시/모델검사 | 기본106792triangles/2139renderers/43materials,최대100명115768/2207/43으로0.5.0과동일.4/32/100명·223횡단/33신호주기·minimumFootDistance0.3999990523/bumper1.7999997139 PASS |
| 실제GPU렌더 | D3D11 Editor540×1200 PNG PASS,횡단보도안노란/흰차선이사라진것을관찰. PNGSHA256 `8625a1b65c5e4723d66ab5a3262157f5a8d80149ca9955560820594c9294d551`. 폰스크린샷아님 |
| Android BuildPlayer/Lint | [BuildApk38004234027](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38004234027), job114069217140 success; BuildPlayer errors0/warnings0, launcher Lint errors0/warnings8 |
| 변경없는host library Lint | NativeAndroid/bridge/모델/차량18입력이0.5.0과byte동일.53빌드입력SHA기록. 기존 [host Lint38003123943](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38003123943) errors0/warnings14를재사용하며새기기테스트로기록하지않는다 |
| 다운로드실제APK | ZIPdigest/CRC·APKhash/bytes·aapt0.5.1/code6/min29/target36/ARM64·원본v2cert·SettingsActivity launcher·BIND_WALLPAPER/:wallpaper/meta/providerfalse/UnityActivitydisabled PASS |

이전PNG와rawpixel차분은화면전체에도차이가나므로 '횡단보도외픽셀동일'의근거로사용하지않았다. 실제renderer bounds/원본소스SHA/렌더관찰을확인근거로한다. 인원·절전설정이같은appID로보존되는구현을유지했지만실제0.5.1 업데이트후설정보존/홈·잠금·FPS/발열은사용자폰에서확인한다.

### 파일·서명·추적

- `/workspace/artifacts/pixel-traffic-unity-prototype-0.5.1.apk`, **29517535 bytes**, SHA256 **`eda001cfa9925e1754c31d70f0f9d404ca629a164b7bc6848836d9d092a66b8f`**.
- 기존인증서SHA256 **`a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`**, v2 PASS. PC기존DPAPI서명을재사용해원본키백업을중복복원하지않았다.
- Build reports artifact11650407942/1198294bytes ZIP SHA256 `066f98dab0b6c160bb319f3619d0ba972731d2bb7c86cec2c2045d33ddbb9bb3`; APK artifact11650597598/28543359bytes ZIP SHA256 `7774aa5d6d38fbfeb2e4ffbee256f5756d3e6274e2af9216358f9df4e40294a7`. API digest/CRC일치.
- 보고서 `/workspace/artifacts/unity-prototype-0.5.1-reports/`, source-manifest·download-verification·preview-comparison JSON은동일artifacts폴더. 문서commit은별도기록하고최종docs-only게시뒤같은검사를반복하지않는다.

### 집PC 설치·확인

APK를 `C:\Users\김백현\Desktop\AI`에저장한뒤PowerShell에서실행한다.

```powershell
$unityAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $unityAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.5.1.apk"
if ($LASTEXITCODE -eq 0) {
    & $unityAdb shell am start -W -n "com.s20plus.pixeltraffic.unityprototype/com.s20plus.pixeltraffic.unitywallpaper.WallpaperSettingsActivity"
}
```

앱의**배경화면 미리보기 및 적용**에서미리보고시스템화면에서적용한뒤횡단보도틈새의중앙선/차선이없는지확인한다. 이명령을클라우드에서사용자폰에실행하거나배경화면을자동적용하지않았다. next기능은사용자선택후진행한다.

## 2026-10-10 — Unity 0.5.1 횡단보도 안 차선 제거 시작 (KST, 진행 중)

사용자가0.5.0 실제폰 적용 성공을 확인했고, 횡단보도 사이 중앙선/흰색차선만 지워달라고 요청했다. 다음 기능은 추천만 요청했으므로 시간대/조명 기능을 구현하지 않는다. 원격main85166199a0c0c4771618a10c53e52fa42e44b4a0 확인. 실제index와 누적native변경을 보존한다.

범위는 CityEnvironment.cs의 도로표시 생성/기존 장면검사 및 StarterConfig.cs·StarterScene.cs 버전/새장면 경로3파일과 기록이다. 원인은 중앙선을 도로전체 길이로,흰dash를횡단보도와관계없이 생성한 것. 횡단보도z=8~12m내에서만 표시를clip하고 바깥표시/12zebra/stopline/도시·차량·사람·배경화면/설정/서명을 유지한다. 새Wallpaper-0.5.1/Generated/Wallpaper051과 code6으로 기존장면을보존한다. 기존 CityEnvironment.Validate에서 실제12stripe의renderer bounds와CentreLine/LaneDash renderer bounds 비중첩을확인한다. 새독립테스트/무관한native·host API검사를추가하지않는다.

실제PC compile/장면/Editor PNG→같은source APK build/launcherLint/cert/다운로드메타데이터를 확인할예정. NativeAndroid8소스/리소스와host코드가0.5.0에서동일하면 이미통과한libraryLint를중복실행하지않고SHA일치와기존결과를근거로남긴다. 실제폰0.5.1 보정표시는 전달후확인한다. 다음추천은낮·노을·야간시간대와가로등/창문/차량라이트이며이번에는구현하지않는다.

## 2026-10-10 — Unity 0.5.0 라이브 배경화면 APK 전달 / PC 검증 완료 (KST)

사용자가 Unity0.4.0 적용 성공과 다음 단계 진행을 확인했다. 이번0.5.0은 도시·차량·신호·인도 보행을 Android 라이브 배경화면으로 연결하고 인원4~100명/기본32명·절전 설정을 자동 저장한다. 별도prototype appID `com.s20plus.pixeltraffic.unityprototype`, 원본 서명, 기존native0.48.0 앱은 유지한다. 실제0.5.0 폰 홈 화면 적용 결과는 아직 확인 대기다.

### 구현과 개발 과정

- 새 `NativeAndroid/src/com/s20plus/pixeltraffic/unitywallpaper/`의6 Java 소스, `res/xml`·`res/values`, production `SurfaceArbiterChecks`, Runtime/AndroidWallpaperBridge(.meta), Editor/AndroidWallpaperBuild(.meta)를 추가했다. StreetSimulation/StarterConfig/StarterScene/PrototypeBuild/pipeline/workflow를 연결한다. StreetModel/SidewalkRoutes/CityEnvironment/VehicleGeometry/TrafficFleet는0.4와 byte 동일하다.
- 기존 APK 실제 DEX와 Unity6.3 API 문서를 대조해 UnityPlayerForActivityOrService의 외부Surface 및 `resume()/pause()`를 사용한다. Activity lifecycle wrapper를 Service에 호출하지 않는다. WallpaperService의단일Unity를 live/preview Engine이 공유하고 `:wallpaper` process로 분리해 설정 Activity의수명을보존한다. 같은UID·exported=false ContentProvider가 main process의SharedPreferences를읽어process간설정을전달한다.
- 유효하고 visible인Surface만 연결하며 미리보기를우선하고 live로복귀한다. 크기/Surface 변경 시 재연결하고 숨김·화면OFF에는pause/Surface해제한다. C# 업데이트를 host렌더상태로 제한하고 첫resumeDelta를 버려 숨겨진시간의catchup을피한다. 인원은4명단위·기본32명, 절전설정/기기절전은15FPS목표·기본30FPS목표이며 측정FPS가아니다. 배경화면에서People OnGUI를표시하지않는다.
- Gradle생성callback이Java/res를복사하고SettingsActivity를launcher로, service를BIND_WALLPAPER/metadata/:wallpaper로 등록한다. 별도UnityActivity는enabled/exported=false로한다. wallpaper.xml은SettingsActivity를설정화면으로연결한다. 설정앱의**배경화면 미리보기 및 적용** 버튼을누른뒤사용자가Android시스템화면에서적용한다. 자동으로사용자배경화면을바꾸지않는다.
- source main `76a6360cbf0c9f864f967919cfb54af801517c6c` → 실제Validate성공 → 같은source태그`unity-apk-0.5.0-build1` → APK빌드/검증을완료했다. 새Java가unityLibrary에속해해당모듈Lint를별도로보강했다. `tools/check-wallpaper-lint.ps1`은현재NativeJava/res8파일의SHA가생성프로젝트와일치할때만`:unityLibrary:lintDebug`를실행한다. 추가수동/`unity-host-lint-*`workflow는기존APK/장면검사를중복실행하지않는다.
- 직접workflow_dispatch는환경proxy403으로실행되지않았다. 허용된Git태그실행을사용했으며 check1의[skip ci]가tag에도적용되어실행이생성되지않은점을보정해check2로성공했다. 태그를옮기거나실패이력을삭제하지않았다. check2 main `17a4fb40921f1ad32afa61bd253e69ec2bf24a4b`에서기존51개빌드입력이원래APKsource와동일함을확인했다. 추가2검사도구/문서만게시했다. 실제Gitindex/누적native변경을보존했다.

### 실제 실행한 검증

| 검사 | 결과 및 출처 |
| --- | --- |
| 클라우드기존JDK21 production SurfaceArbiter | preview/live 우선·fallback·OFF/hidden·invalid/zero-size·generation/resize·1000회생성/제거 PASS; Android기기검사아님 |
| 실제PC Unity6000.3.26f1 컴파일/장면/manifest template | [Validate38001940380](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38001940380), job114061824829 success |
| 실제production차량/보행검사 | 4/32/100명200/600/600초·모두진행/최소1횡단·223횡단/33신호주기·minimumFootDistance0.3999990523/bumper1.7999997139·15/30/60/120Hz/pause. 최대100명115768triangles/2207renderers/43materials |
| 실제Editor URP camera | D3D11 540×1200 PNG/미리보기 PASS. SHA256 `6f3269d70576458eafbd941e5f60f3596b86213742e1fab68fa3a7cf0c217e3f`. 실제폰홈스크린샷/FPS검사아님 |
| 실제Android BuildPlayer | [BuildApk38002257912](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38002257912), job114062852173 success; Errors0/Warnings0 |
| 실제Android Lint | launcher errors0/warnings8; 추가 [host Lint38003123943](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38003123943), job114065669173 success; unityLibrary errors0/warnings14, Native8파일 SHA 일치 |
| 실제APK다운로드대조 | artifactZIPdigest/CRC·APKhash/bytes·aapt0.5.0/code5/min29/target36/ARM64·원본v2cert·SettingsActivity launcher/servicepermission/process/metadata/providerfalse/UnityActivitydisabled·APK DEX6nativeclass/4bridgemethod PASS |

라이브러리Lint14경고에는 Unity생성API/ABI/의존성 관련9건, application context를process수명동안저장하는StaticFieldLeak1건, 한국어설정문자열의SetTextI18n4건이있다. Activity Context를static으로저장하지않는다. 경고를0건으로보고하거나전체Lint를억제하지않았다. 다국어문자열정리는후속이며 실제기기누수/발열은PC검사로확인할수없다.

### 산출물과 추적 정보

- APK `/workspace/artifacts/pixel-traffic-unity-prototype-0.5.0.apk`, **29517587 bytes**, SHA256 **`d22ba8bce7d0a6c3f1849835c634bc5fdd228ebc7e92fa25a6437a852c1cfe04`**.
- 원본인증서SHA256 **`a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`**/v2. PC의기존DPAPI서명설정을재사용해백업키를중복복원하지않았다.
- 실제보고서 `/workspace/artifacts/unity-prototype-0.5.0-reports/`, 다운로드APK검증JSON/host-policy JSON/source-manifest JSON은동일artifacts폴더. 51빌드입력과추가검사도구SHA를기록한다.
- Build reports artifact11649789022/1198043bytes ZIP SHA256 `4abc2d0f8afddd68b49159a696431ccb9444ca268cd6c8651fb56ebe7c10efce`; APK artifact11649599487/28542851bytes ZIP SHA256 `f3a985942cb51f18448cb0d077c972cb97aa581b902ff81e4cbf5ea4923ee7e5`; host Lint artifact11650026947/1249bytes ZIP SHA256 `9d46fb473a67a4965b34eec28dbaa0da92ed0effed00ddccbc32681d9ed705e0`. 모두API digest 및CRC일치.
- 소스/결과문서/main게시는각각수행한단계를구분한다. 최종문서만[skip ci]로게시하며같은APK/소스검사를반복하지않는다. 문서commit은작업artifacts의source-manifest에별도기록한다.

### 집 PC 설치 및 적용

APK를 `C:\Users\김백현\Desktop\AI`에다운로드하고사용자PC PowerShell에서실행한다. bareadb가무출력인문제를피하기위해확인된Unity SDK adb.exe를직접사용한다.

```powershell
$unityAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $unityAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.5.0.apk"
if ($LASTEXITCODE -eq 0) {
    & $unityAdb shell am start -W -n "com.s20plus.pixeltraffic.unityprototype/com.s20plus.pixeltraffic.unitywallpaper.WallpaperSettingsActivity"
}
```

`Success`후설정화면에서인원/절전을정하고**배경화면 미리보기 및 적용**→시스템미리보기→적용을선택한다. 기존UnityPlayerActivity실행명령은이번버전에서사용하지않는다. 이명령을클라우드에서사용자폰에실행하거나배경화면을자동적용하지않았다.

### 미검증과 다음 단계

실제S20+/S26 Ultra에서첫미리보기·홈/잠금 적용·다른앱진입/홈복귀·화면OFF/ON·live/preview전환·인원/절전저장·재부팅후설정/배경화면복원·최대100명FPS/발열을확인한다. 표시·복귀문제가있다면동일appID Service runtime로그/기기정보로관련호스트만수정한다. 안정적용확인뒤저녁/야간·비/비바람/조명·차량세부모델을단계적으로이전하고목표시안품질을올린다. 기존native날씨/브랜드자산을복제재생성하거나완전이전이끝났다고보고하지않는다.

## 2026-10-10 — Unity host Lint tag 실행 보정 (KST, 진행 중)

check1 태그 commit의[skip ci]가tag push에도 적용되어 실행이생성되지않았다. 이사실을수정해 docs-only commit을skip문구없이게시하고check2태그를사용한다. 기존Unity workflow는docs 경로를대상으로하지않으므로 model/APK검사는반복되지않고새hostLint만실행한다. check1은옮기거나삭제하지않는다. 실제APK의6nativeclass와bridge4method도 DEX로확인했다.

## 2026-10-10 — Unity host Lint 실행 경로 보완 (KST, 진행 중)

직접 GitHub API workflow_dispatch 요청이 환경 proxy의403으로 거부되었다. API를 우회하지 않고 허용된 Git 게시/태그 실행 경로를 사용한다. unity-wallpaper-lint workflow에unity-host-lint-* 태그 실행을 추가한다. 기존unity-apk-* workflow와 분리되어 APK/장면검사를중복실행하지않는다. 동일NativeJava/리소스인기존생성프로젝트만Lint한다. 실제결과대기.

## 2026-10-10 — Unity 0.5.0 APK 완료 / host 모듈 Lint 추가 확인 (KST, 진행 중)

source76a6360cbf0c9f864f967919cfb54af801517c6c/tagunity-apk-0.5.0-build1, BuildApk38002257912/job114062852173 success. BuildPlayer errors0/warnings0, launcher Lint errors0/warnings8. 다운로드 APK29517587bytes SHA256d22ba8bce7d0a6c3f1849835c634bc5fdd228ebc7e92fa25a6437a852c1cfe04. 실제aapt/apksigner로0.5.0/code5/ARM64/min29/target36/원본v2cert/SettingsActivity launcher/BIND_WALLPAPER service/:wallpaper/metadata/privateprovider/UnityActivitydisabled를 대조했다. 실제 폰 홈 수명주기는 미검증.

새 Java가 unityLibrary에 들어가므로 해당 모듈의Lint도 별도 확인한다. check-wallpaper-lint.ps1와 수동 전용unity-wallpaper-lint workflow를 추가한다. 기존 생성 프로젝트8개NativeJava/리소스SHA가현재소스와같은지먼저검사하고 :unityLibrary:lintDebug만 실행한다. 이미통과한 APK/Unity 장면검사를 반복하지 않으며 [skip ci] 확인도구게시후 수동작업으로 실제PS parser/Lint를검증한다. 런타임/장면/서명은변경하지않는다. 결과아직대기.

## 2026-10-10 — Unity 0.5.0 라이브 배경화면 호스트 구현 시작 (KST, 진행 중)

사용자가0.4.0 적용 성공과 다음 단계 진행을 승인했다. 원격main 기준c5f1ea0571f0d146c9c586f77dcff65dabcf0de9을 확인했고 기존dirty native/index는 보존한다. 기존0.4 APK의 실제DEX에서 UnityPlayerForActivityOrService(Context),displayChanged(int,Surface),pause/resume/windowFocusChanged를 확인했다. Unity6.3 공식 라이브러리 설명 및 IPostGenerateGradleAndroidProject 설명과 Service resume 관련 Unity discussion을 대조했다.

범위: 새NativeAndroid Java/리소스/SurfaceArbiterChecks, 새Editor/AndroidWallpaperBuild·Runtime/AndroidWallpaperBridge(.meta), 기존StreetSimulation/StarterConfig/StarterScene/PrototypeBuild, pipeline/workflow 검증 및 문서. StreetModel/SidewalkRoutes/차량·도시 geometry·원본 signing 설정은 유지한다. 별도Unityprototype appID를0.5.0/code5로 업데이트하고 새Wallpaper-0.5.0/Generated/Wallpaper050을 쓴다.

기본 앱은 Android 설정 화면이며 시스템배경화면미리보기/적용으로 연결한다. UnityPlayerActivity는 비활성화해 두 번째 renderer를 만들지 않고 WallpaperService의단일Unity를 live/preview Engine이 공유한다. 서비스는 :wallpaper process로 분리하고 비공개 같은UIDContentProvider로 인원/절전설정을 읽어 process quit/설정수명주기 충돌을 줄인다. 인원4~100/기본32·15/30FPS목표는 자동저장되며 시스템절전도15FPS목표. 유효한visibleSurface만 선택/미리보기우선/hidden·screenOFFpause·surface generation/size재연결. C#는host가visible일때만업데이트하고첫resumeDelta를버려hidden시간catchup을하지않는다. 홈배경화면에는People OnGUI를표시하지않는다.

현재 구현 단계. 클라우드 production selection/manifest XML정적·실제PC Unitycompile/장면/AndroidBuild/Lint/서명/APKmanifest 검증 예정. 실제S20+/S26 Ultra 홈/잠금/미리보기전환/숨김복귀/발열은 기기에서 확인해야 하며 PC/독립Java검사로대신하지 않는다. 원본 키가PC에정상등록되어있어첨부백업을다시복원하지않는다. 동일입력의무관한native검사는반복하지않는다.

참고: https://docs.unity.com/en-us/engine/6000.3/manual/platform-specific/android/developing/unityasa-library · https://docs.unity.com/en-us/engine/6000.3/script-reference/unityeditor/android/ipostgenerategradleandroidproject/onpostgenerategradleandroidproject · https://discussions.unity.com/t/unityplayerforactivityorservice-instantiated-via-service/945528


클라우드 JDK21로 실제 SurfaceArbiter를 컴파일/실행하여 visible/preview 우선·live fallback·화면OFF·invalid/zero-size·surface 교체/크기·1000회 생성/제거 검사 PASS. Android/Unity 실행 검증은 아직 아니다. APK targetSDK는 manifest 정규식 이후 캡처가 덮이지 않도록 먼저 저장한다.
## 2026-10-10 — Unity 0.4.0 신호·인도 보행·횡단 APK / 실제 Editor 렌더 전달 완료 (KST)

사용자가0.3.0 적용 성공 뒤 다음 단계와 계속 진행을 승인하여 신호·차량 정차/재출발·인도 산책·횡단·군중 회피·폰4~100명 조절을 구현했다. 최종 source480020476c6e2984d15dc8f20de5ef5d77f04501/tag unity-apk-0.4.0-build1. [Windows main37960722841](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/37960722841)/job113922576563와 [APK37961277473](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/37961277473)/job113924438275가 success이다. 앞선 실패는 아래 이력에 유지하며 최종 통과로 덮어쓰지 않는다.

범위: Runtime/StarterConfig·PrototypeDrive·신규StreetModel/SidewalkRoutes/StreetSimulation(.meta), Editor/StarterScene·CityEnvironment·CityPreview·신규StreetScene/StreetChecks(.meta), Unity README와 관련 기록. 신규Crossing-0.4.0.unity/Generated/Crossing040은 이전 장면을 보존한다. 24차량/4차선·실제metre 크기·원본geometry·30FPS 목표와 원본 서명을 유지했다. 같은30Hz 시계가 신호/차량/사람을 관리하며 앞차 간격·황색에서 이미 진입 중인 차량·도로 비움·보행 신규 입장·횡단 완료 후 차량 재시작을 처리한다. wheels는 실제 이동거리만큼 회전하고 pause/focus 숨김 동안 시간을 따라잡지 않는다.

기본32명, 폰 오른쪽 People의 +/-4 버튼으로4~100명을 조절한다. 인원 감소 시 횡단 중 사람은 인도 도착 뒤 빠진다. 설정은 앱 재실행 시32명으로 돌아오며 영구 저장은 후속이다. 100개 사람 rig를 풀로 생성하고8개 공유 mesh/단일 palette material·머리/옷/피부색·높이 변형·4limb 보행을 사용한다. 재생 중 자산/사람을 매번 생성하지 않는다. 실제 인도와 비어 있는 상점 앞 포장 통로를 걷고 화단·가로등·신호 기둥·건물 경계를 피한다. 발 분리 목표0.40m/연속 이동으로 몸을 겹치거나 순간 이동하지 않는다. 최대12명만 횡단 자리를 예약하며 만석이면 산책 후 재시도한다. 횡단 의도와 재시도 시간을 분리하고18m 이내 접근자·첫 횡단 우선·이미 건넌 사람의 바깥 산책으로 정체를 분산한다. 좁은 외측 통로는 오른쪽 두 흐름으로 비켜 지나가며 회피 후 경유점이 막히면 최대1초 주기로 현재 발에서 안전한 경로를 재연결한다. 고정 장애물 edge는 최초에 계산해 BFS가 공유한다.

최종 실제 production model4/32/100명200/600/600초 검사: 모든 사람 후반 이동·최소1회 횡단, 총223횡단/33신호주기, minimumFootDistance0.3999990523m/minimumBumperGap1.7999997139m, 신규 입장 신호·횡단 band·보행 신호 차량 비움·최대12대기·감소 후4명 안전 복귀 PASS. 실제 runtime controller15/30/60/120Hz90초 비교·pause도PASS. 실제100개 rig/화단 bounds와 기존 도시18portrait rays·420m road·32trees/24buildings/20lamps/26skyline·차량24대 geometry/600초 자유 주행 조건도PASS. 최대100명115768triangles/2207renderers/43materials, 기본32명106792/2139/43로 기존120000/2400/48 예산 안이다. 이는 실제 폰 FPS·발열 측정이 아니다.

개발 과정에서 PC가 검출한 MaterialPropertyBlock의 MonoBehaviour field 초기화 오류, 정지선 부동소수 modulo 오류, 좁은 통로 경유점/회피 반복, 앞사람 뒤에서 이른 Cross 전환, 우회 후 zebra band 복귀 누락, 만석 분산이 먼 사람의 횡단 의도를 취소하는 문제를 수정했다. 반복 횡단자와 처음 접근자의 흐름 및 회피 후 실제 위치에서의 경로 재연결도 보완했다. 32명300초 검사에서 마지막 사람의 늦은 도착을 확인해600초로 확대했으며 각 사람의 실제 횡단/이동·안전 조건을 유지했다. 100명600초와4명200초는 유지했다. 전체 모의 통과 뒤 실제 화단 검사에서 root yaw=index*47이 bed/soil에도 상속되어 보행 경계와 달랐음을 검출했다. Bed/Soil만 역회전하여 세계 인도 방향에 맞췄고 나무/수관의 다양한 방향·크기/위치/mesh 수를 유지했다. 이전 도시 geometry 무변경 설명은 이 방향 보정에 대해 정정한다. 각 실패의 source/run/진단과 수정은 아래 WORK_LOG 이력에 기록했고 관련 입력을 바꾼 경우만 PC 검사를 다시 수행했다. 검사 인원을 숨기거나 순간 이동해 통과시키지 않았다.

PC BuildPlayer Succeeded/Errors0/Warnings0, BuildApk2026-10-10 01:44:41~01:49:22 KST PASS. Android :launcher:lintDebug Errors0/Warnings8/PASS. 실제 내려받은 APK version0.4.0/code4/appID com.s20plus.pixeltraffic.unityprototype/min29/target36/ARM64·v2 원본 인증서 SHA256a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6가 일치한다. APK29494030bytes/SHA256ddc3bf374caa08586e62aa68e3d8a4cb009d90e397a98218f2cc1abe59423548. APK artifact11631985656 ZIP28536399bytes/SHA25656eb805d7e009f6cd038bb3546a7d866d60fd159a7dfb71840e878f8c661fa15, report artifact11631019956 ZIP1197847bytes/SHA2568f156f5d602ae0dd2fe201de6e8ad795cf32fae101dd983f0f97aa1230596aa6의 GitHub digest/CRC와 APK apksigner/aapt/CRC/il2cpp/launchableActivity 대조PASS. 기존 PC DPAPI 서명을 재사용했고 키·암호·원본 로그는 공유하지 않았다.

같은 source의 실제 warmed Direct3D11 Editor camera540×1200 PNG를 열어 정차 차량/신호/횡단 중 사람/인도 보행/화단을 확인했다. 최종 PNG SHA256c23fd5a2f95c176d9d3660ebc23aea0111ae0080602cf9d051c866e35c6b1f3a. production model30초 pose를 캡처했고 AI 시안/폰 screenshot/주행 FPS로 표기하지 않는다. People OnGUI 패널은 카메라 PNG에 포함되지 않으며 실제 앱에서 표시한다.

APK: /workspace/artifacts/pixel-traffic-unity-prototype-0.4.0.apk. 실제 Editor PNG: /workspace/artifacts/city-preview-0.4.0.png. 검증: /workspace/artifacts/unity-prototype-0.4.0-download-verification.json, PC 보고서: /workspace/artifacts/unity-prototype-0.4.0-reports/, source/build/capture37개 입력 manifest: /workspace/artifacts/unity-0.4.0-source-manifest.json. 다음 환경에서 로컬 존재를 가정하지 않고 해당 run artifacts를 확보한다(보관7일). 기존 Unity prototype0.3.0을 업데이트하며 native0.48 앱과 공존한다. 아래 명령은 사용자 최신 집 PC 폴더 기준이며 사용자 PC에서 실행한다.

```powershell
$unityAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $unityAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.4.0.apk"
if ($LASTEXITCODE -eq 0) {
    & $unityAdb shell am start -W -n "com.s20plus.pixeltraffic.unityprototype/com.unity3d.player.UnityPlayerActivity"
}
```

미검증/후속:0.4.0 실제 S20+/S26 Ultra의 외형·4/32/100명 FPS/발열·장시간·숨김/복귀, WallpaperService 연결, 날씨·기존 전체 차종 이전·설정 영구 저장. 사용자0.3.0 적용 성공을0.4.0 기기 검증으로 간주하지 않는다. 현재 Activity 시제품이며 목표 시안 완성으로 표기하지 않는다. 다음은 실제 두 기기의 기본32명/최대100명 성능을 확인하고 Android 라이브 배경화면 연결 또는 보행자/거리 외형 보완으로 이어간다.

클라우드에 Unity를 새로 설치하지 않고 기존 PC runner/SDK/JDK/캐시/자산/키 설정을 재사용했다. 기본 shell의 proxy 접근 실패는 허용된 추가 network 실행으로 처리했다. 누적 dirty native 파일·실제 index를 보존하고 원격main 기반 임시 index로 Unity 범위/문서만 게시했다. APK/GPU 검증 뒤 변경은 결과Markdown8개뿐이며 docs-only commit의 [skip ci]로 동일 source 검사를 반복하지 않는다. 소스 검증·APK tag·최종 문서 게시 상태를 별도로 확인한다.

## 2026-10-10 — Unity 0.4.0 전체 보행 모의 통과 / 실제 화단 yaw 불일치 수정 (진행 중, KST)

- Windows37960186067/source92e99ccbb702447fb344f3d3fa5654d7c8c9f9b1에서4/32/100명200/600/600초의 모든 사람 진행/횡단·신호/차량 안전 및 실제 controller15/30/60/120Hz/pause 검사를 지나 실제100rig/화단 대조에서 Actual tree bed differs from foot navigation으로 실패했다. 전체 Validate가 통과한 것은 아니다.
- 실제 CityEnvironment.Tree는 root yaw=index*47을 나무뿐 아니라 정사각 화단/soil에도 상속하고 있었다. 따라서 보행의2.2m축정렬 경계와 실제 회전 화단 renderer bounds가 달랐다. Bed/Soil에 root yaw의 역회전을 적용하여 세계 인도 방향에 맞춘다. 나무/가지/수관의 다양한 방향·모든 크기/위치/mesh/material 수는 유지한다.
- 동일 실제 scene/모의/예산/GPU 검사를 재실행한 뒤 APK를 만든다. APK 아직 미생성. 이전의 도시 geometry 무변경 설명은 화단/soil 방향 보정에 대해 정정한다.

## 2026-10-10 — Unity 0.4.0 좁은 외측 통로의 양방향 보행 (진행 중, KST)

- Windows37959569564/source1d479ef29a2b61c3b51e31945b9fec93f95f2525에서 4/32명 통과,100명 중99명 횡단. 미횡단 id69는(10.03,-12.19)→(10.71,3.05) 이동 중, 화단 외측 통로에서 북/남향 사람들과 같은 중앙 경로를 사용하고 있었다. 먼 화단 단독 경로 문제는 재발하지 않았다.
- 외측 통로absx9.86~10.75에서 긴 종방향 구간은 세계 진행 방향 기준 오른쪽 두 발 흐름(absx10.05/10.55, 간격0.50m)으로 유도한다. 종점/내측 경로로 바뀔 때는 원래 경유점을 따른다. 모든 실제 움직임은 기존 인도 선분·0.40m 발 분리 검사를 통과해야 하며 겹침/순간 이동 없이 비켜 지나간다.
- 실제PC 재검증 전이다. 동일4/32/100명200/600/600초·각 사람 진행/횡단/안전 검사를 유지한다. APK 미생성.

## 2026-10-10 — Unity 0.4.0 회피 뒤 장애물 경로 재연결 (진행 중, KST)

- Windows37958992988/source746896dec422b44517f466b56a9bd71f08f18c58: 4/32명 통과,100명 중99명은 횡단했으나 id27 Roam(9.70,133.28),goal(10.00,26.90),path41/276, 주변3m에 다른 보행자 없음 상태에서 횡단0회였다. 이는 화단132m의 상단 경계133.26m 부근이며 인파만으로 설명할 수 없다.
- 회피 이동이 기존 경유점의 장애물 반대편으로 벗어나면 현재 위치→다음 점이 막혔는지 검사하고 최대1초 주기로 실제 위치에서 경로를 다시 연결한다. 첫 경로 node는 거리뿐 아니라 현재 발에서 장애물 없이 접근 가능한지 확인한다. 정적 그래프/실제 이동 선분 조건은 유지한다.
- 실패 보고서에 next node·detour 상태를 더해 추정과 실제 결과를 구분한다. 같은4/32/100명200/600/600초 기준 재검증 전이다. APK 미생성.

## 2026-10-10 — Unity 0.4.0 첫 횡단 접근 공간·먼 경로 갱신 (진행 중, KST)

- Windows37958351695/source2c43071605c7193f5689a43cdd7556e939cfa55b에서 32명600초는 통과했고100명600초 미횡단자는23/62 Approach,90/91 Roam이었다. id23 화단 모서리(8.89,16.26) 주변3m의12명은 모두이미1회 횡단했지만 첫 횡단자 우선 예약 거절 뒤에도 횡단 주변 산책 목표를 유지했다.
- 첫 횡단 의도는 유지하며 반복 방문자가 예약을 못 받으면45~105초 다른 구간을 걷도록 분산한다. 이미 건넌 사람이 횡단±24m에 있을 때 일반 산책은 우선 바깥 구간으로 향한다. cooldown이 끝나는 순간 먼 곳의 기존 산책 경로도 횡단 접근 목표로 갱신하여 긴 기존 경로가 접근을 지연시키지 않는다.
- SidewalkRoutes의 고정 장애물 인접 선분은 생성 시 검사해 양방향 edge로 캐시하고 BFS에서 재사용한다. 장애물 조건/탐색 순서는 동일하며 100명에서 매 탐색마다 수천 번 반복하던 정적 선분 검사를 줄인다. 실제 발 이동의 연속 선분/사람 분리 검사는 그대로 매 step 수행한다.
- 실제PC 재검증 전이다. 인원/신호 안전·모든 사람 진행/횡단·원래 예산을 유지한다. APK 아직 미생성.

## 2026-10-10 — Unity 0.4.0 늦은 도착과 정체를 구분하는 검사 시간 (진행 중, KST)

- Windows37957862224/sourcec8e00ca4db491c26e7106e6fdd47a8b43a7dd042: 32명300초 종료 시 마지막 미횡단 id31은 Wait(7.16,10.41), Approach+Wait 누적32.6초였다. 이전의 원거리 Roam/영구 정체와 구분해야 하며 다음 신호 기회를 포함해야 한다.
- 32명 실행을300초에서100명과 같은600초로 확대했다(4명200초 유지). 모든 사람의 후반 이동·최소1회 실제 횡단·신호 반복·분리·차량 비움·감소 후 안전 복귀 검사는 그대로 유지했다. simulatedSeconds=[200,600,600]을 실제 report에 추가한다. 이전300초 실패를 통과로 재작성하지 않는다.
- 실제 PC 검증 및 APK는 남아 있다.

## 2026-10-10 — Unity 0.4.0 횡단 의도·재시도 시간 분리 (진행 중, KST)

- Windows37957370022/sourcee861a08c63ef2b73c03b29a9b6d8ddcc1655db68: 4/32명 통과 후100명 중27명이600초 횡단0회. 진단에서 다수는 z55~105의 Roam으로 분산되어 있었고 일부는 Approach였다. 대기 초과 cooldown이 횡단 의도를 지우고 일반 먼 산책 목표를 만드는 구조가 남아 있었다.
- 횡단 희망 cooldown과 예약 재시도 retrySeconds를 분리했다. 만석이면8~20초 후 재시도하지만 횡단 쪽 이동 의도는 유지한다. 만석 보행 목표는 횡단 주변±24m에 분산한다. 예약은18m 이내 접근자만 받아 먼 사람의 장기 자리 선점을 줄이고 같은 쪽의 첫 횡단 대기자는 반복 횡단자보다 먼저 빈 자리를 사용한다.
- 인원100·12자리 상한·모든 사람 횡단·0.40m 발 분리·차량 안전/기존 시간 기준은 유지한다. 실제PC 재검증 전이며 APK 미생성.

## 2026-10-10 — Unity 0.4.0 원거리 보행 접근 유지 정정 (진행 중, KST)

- Windows37956943169/source655e24219bd68a92c14424acc5ab8dc4fcf1d735의 32명 검사에서 횡단0회인 id23은 z90.47의 먼 인도에서 Roam 중이었다. 직전 분산 변경이 예약 범위45m 밖에도 cooldown을 재설정하여 횡단 쪽으로 오는 목표를 계속 취소했다.
- 예약 범위 안에서 자리가 꽉 찬 경우에만 분산 cooldown을 적용한다. 먼 보행자는 기존 접근 목표를 유지한다. 4/32/100명·300/600초 내 모든 사람 진행/횡단 기준은 유지한다.
- 실제 PC 검증 및 APK는 남아 있다.

## 2026-10-10 — Unity 0.4.0 100명 대기 초과 흐름 분산 (진행 중, KST)

- Windows37956408482/source97f15a88e2096c5de9696f4e9c9ba94dd0043316에서 4명·32명 진행/횡단 검사를 지나 100명600초 종료 시 id15 Approach(7.42,12.81)가 횡단0회로 실패했다. 해당 사람은 이동은 계속했으며 안전/분리 조건은 이 지점까지 통과했다.
- 예약 실패한 사람은 매 tick 다시 시도하면서 횡단 주변 목표를 반복하는 대신 8~20초 다른 보행 목표를 따른 뒤 재시도한다. 최대12자리·100활성 사람·모든 사람 진행/횡단 검사를 유지했다. 실패 메시지에 인접 상태와 횡단0회 전체 목록을 추가하여 진단을 모았다.
- 실제 PC 재검증 및 APK 제작은 남아 있다.

## 2026-10-10 — Unity 0.4.0 우회 후 횡단 대기선 복귀 (진행 중, KST)

- Windows37955936329/source288b5f1f92d07c221102af151e33b83d667f4e20에서 횡단 zebra band 검사 실패. 옆 우회가 대기자의 z를 바꾼 뒤 출발 조건이 x만 확인하고 현재 z로 횡단을 시작했다.
- Wait 상태는 예약된 zebra band의 원래 z로 복귀한 뒤 출발한다. 횡단 전 우회 상태를 해제하며 이동량·0.40m 분리·초록 입장 조건은 유지한다. 실패 보고서에 count/tick/id/pos를 추가했다.
- 실제 PC 재검증 전이며 APK 아직 미생성.

## 2026-10-10 — Unity 0.4.0 대기자 옆 보행의 지속 회피 (진행 중, KST)

- 실제 Windows37938953619/source9f67394d4b37a21acd6b8150cd013220429e845a의 인접 상태: id0 Wait(-7.09,8.80), id2 Approach(-6.79,8.51), 두 사람 모두 횡단0회. 이동 후보를 매 프레임 목표로 재선택하면서 대기자 앞에서 좌우로 반복되는 것이 원인이다.
- StreetModel에 장애 보행자 앞에서 선택한 안전한 옆 우회 경유점을 도착 또는3초까지 유지하도록 추가했다. 실제 인도 선분 허용 검사와 연속0.40m 발 분리는 그대로 유지하며 순간 이동·강제 횡단·인원 제거를 하지 않는다.
- 4/32/100명·신호·차량 간격·보행 진행 기준을 동일하게 PC에서 재검증한다. APK 아직 미생성.

## 2026-10-09 — Unity 0.4.0 대기 지연 상태 조사 (진행 중)

- Windows Validate37938596907/source25f5a02d319307dcab23f8f6479300d0e8c5d4d1에서 4명 중 id0이 Wait 상태로192초 머물러 진행 검사 실패. 신호는6회 복귀했고 Cross 영구 정체는 발생하지 않았다. 원인 확정을 위해 해당 보행자 주변3m의 실제 active 사람·side·slot·activity·goal·횡단 횟수만 오류에 추가했다.
- 검사 기준·실행 동작은 유지한다. 아직 APK 전달 전이다.

## 2026-10-09 — Unity 0.4.0 횡단 출발 공간 검사 (진행 중)

- 실제 Windows Validate 37937812355, source dd2b4636f78282e0f06ba4aef2d762e0ad590863 실패: 4명 조건에서 뒤쪽 대기자가 앞사람을 확인하기 전에 Cross 상태로 전환되어 x=-7.08에서 멈춤. 차가 없는 신호임에도 보행 출발 통로가 막힌 것이 원인이다.
- StreetModel: 모든 활성 보행자의 실제 발 원과 출발 구간을 검사하고 앞사람이 비킨 뒤 입장한다. 0.40m 분리·연속 이동·신호 안전 검사를 유지했다. 경로 미리보기는 2m 안의 경유점으로 제한하여 긴 구간의 반복 샘플 비용을 줄였다.
- 실제 PC 재검증 예정. APK·실기기 성능은 아직 미검증.

## 2026-10-09 — Unity 0.4.0 실제 정체 원인 확인 / 보도와 storefront apron 경로 연결

main2d95060c2cd6467570143d16e9a7d39ad7e4439d 실행37937113151/job113841834308은13:29:00Z→13:30:13Z 실패했고 artifact11618798118 ZIP8899bytes SHA256a0fc95232e2420bcd0eefe5a9829fa6aab545a4265b02be12419e3f3e8e0d32b/CRC를 확인했습니다. 실제 실패state는 count32/id0/phasePedestrianClearance/time2.233333/cycles7/Approach/pos(10.21,26.75)/goal(7.92,10.40)/path0/44/wait158.2382입니다. 신호는7cycle 돌아왔으며 멈춤은 실제 경유점/회피 동선 문제로 구분됩니다. 차량 정지선 안전 예외는 재발하지 않았습니다.

나무 bed의 바깥쪽과 기존 보도 끝 사이가 발 중심 기준 약.36m라 .40m 몸 간격을 가진 반대 보행자가 통과하기 어려웠습니다. 이미 도시 geometry에 있는 storefront apron(범위x10.1..11.9,높이.06m)을 통행 공간에 포함하고 building 시작x11.3보다 앞인x10.75까지만 허용합니다. 보도/도로/나무/건물 mesh를 수정하거나 나무 위를 통행시키지 않습니다. shared graph를12열로 확장하고 바깥 포장 공간에서 비켜가며 실제 보도(.16)/apron(.06) 높이를 반영합니다. 초기 가장 가까운 node가 뒤에 있어 그 점으로 돌아가려는 상태를 막고, 실제 obstacle-safe segment가 보이는 다음 경유점을 최대3개 연결해 매끄러운 방향으로 진행합니다. local body/연속 segment 검사 .40m와tree/lamp/signal/curb/building 경계는 유지하며 강제 순간이동·검사 완화가 아닙니다.

다음 실제 검사에서4/32/100 진행·횡단·12대기 cap·vehicle separation/road-empty·안전인원 변경과100rig 예산을 다시 확인합니다. 아직 APK tag/최종 PASS는 없습니다. source 입력이 바뀐 관련 검사만 재개하고 실패 기록을 보존합니다.

## 2026-10-09 — Unity 0.4.0 정지선 침범 회귀 해결 / 보행 진행 정체 원인 추적

main879d10f56cee30d0c3905d69f4bfbf69e295676e 실행37936557670/job113839953527은13:24:21Z→13:25:41Z 실패했습니다. artifact11618058397 ZIP8809bytes SHA2561801af57da533fcaef52693257b1672a61d340af73f1e9b71e7a9d5dc3e10817/CRC를 확인했습니다. 이전 Vehicle enters pedestrian phase 예외는 재발하지 않았고 StreetChecks의 Pedestrian remains permanently stuck를 검출했습니다. 4/32/100 전체 PASS나 APK 완료로 기록하지 않습니다.

기존 메시지가 population/agent/phase/path를 포함하지 않아 신호 clearance와 실제 보행 경로 정체를 구분할 수 없었습니다. 해당 실패의 진단에 count/id/signal/time/cycles/activity/position/goal/path/wait와 인근 차량 state를 추가합니다. 동작을 강제로 완료시키거나 검사 기준을 낮추지 않고 동일한 production model에서 원인을 확인합니다. 이 변경은 Editor 검사 메시지뿐이며 수정 결과를 바탕으로 관련 runtime만 보완합니다. APK tag는 아직 게시하지 않았습니다.

## 2026-10-09 — Unity 0.4.0 보행 신호 차량 침범 검사 실패 / 정지선 부동소수 경계 수정

mainc062da689630125459eea249095b741e1d9ce0bb 실행37935956552/job113837943151은13:19:14Z→13:20:35Z 실패했습니다. native 객체 초기화 오류는 재발하지 않았고 실제 장면 생성 뒤 StreetChecks의 Vehicle enters occupied pedestrian phase를 검출했습니다. artifact11618092456 ZIP9285bytes SHA256ff5c09ce8a7e21ec8e6bee42cbf868f740e737e500dd30aa6546f493d903b9fd/CRC와 실제 예외를 확인했습니다. APK tag는 아직 게시하지 않았고 안전 검사를 무시하지 않습니다.

원인 후보를 실제 stop-line 계산과 대조했습니다. float 이동량을 double 위치에 누적할 때 정지선의 아주 작은 overshoot가 signed distance를 음수로 만들어 modulo에서 다음310m loop로 취급될 수 있었습니다. 정지선 ±1mm는 다음 루프로 해석하지 않고 정확한 stop center에 스냅/속도0 처리합니다. 실제 travelled distance도 스냅값으로 다시 산출하고 규칙상 이미 지나가는 committed 차량은 유지합니다. 이는 구현상 경계 오류 수정이며 다음 실제 검사가 원인 해결 여부를 확인합니다. 실패 메시지에 population/tick/phase와 해당 lane/z/speed/commit을 추가해 후속 근거를 확인합니다.

실제 도시 가로등 Base가 폭.35m인 것을 추가 대조해 foot radius.16을 합친.34m 통행 금지 radius로 보완했습니다. 기존 .27m은 pole만 고려한 값이었습니다. graph가 좁은 안쪽길에서 outer 길로 우회하며 tree bed/curb/crossing/body separation·입장/차량 비움 기준은 유지합니다. 관련 model/route만 다시 검증하고 무관한 geometry/키/SDK를 변경하지 않습니다.

## 2026-10-09 — Unity 0.4.0 첫 장면 생성 실패 / native 객체 초기화 시점 수정

mainacd0c571c147beeb1a8a334e2ce19d583b315e3d 실행37935392878/job113836033134은13:14:23Z→13:15:36Z Validate 실패입니다. 비밀 제거 artifact11618750011 ZIP5044bytes SHA2568d403ac53531e9499f2d280111ebbb0d0cf9265af2e9af910085cedcc6b52760/CRC를 확인했습니다. C# graph compilation은 exit0이나 scene 생성 중 StreetSimulation field initializer의 MaterialPropertyBlock.CreateImpl이 MonoBehaviour constructor에서 허용되지 않는 UnityException을 내고 뒤의Tint에서 null exception이 발생했습니다. 아직 production model/scene 검증·APK를 통과한 것으로 기록하지 않습니다. 초기 licensing 경고 뒤 entitlement는 resolved로 기록돼 라이선스 만료로 단정하지 않습니다.

MaterialPropertyBlock을 field initializer에서 제거하고 실제 main-thread ApplyViews에서 생성/재사용합니다. 동시에 실제 Editor 미리보기는 OpenScene 뒤 controller.ResetModel→Advance(30)→ApplyViews로 신호 property block(장면 직렬화 대상 아님)과30초 교통/횡단 pose를 적용한 후 GPU warmup/capture합니다. 렌더용 state를 scene asset에 저장하거나 렌더만으로 simulation 시간을 진행시키지 않습니다. 실제 APK source/tag는 아직 만들지 않았고 수정된 main의 compile/scene/crowd·렌더 결과를 먼저 확인합니다. pipeline/키/SDK/도시/차량 geometry·검사 기준은 유지했습니다.

## 2026-10-09 — Unity 0.4.0 신호·인도 보행·횡단 구현 / PC 검사 예정

사용자가0.3.0이 순조롭게 적용됐다고 확인하고 다음 단계를 승인했습니다. 이는 사용자 설치/적용 확인이며 두 기종 FPS/발열 측정을 주장하지 않습니다. 최신 main54b34b56854ad156569e1e904918fbf9de55f852와 소스를 확인하고 AGENTS/HANDOFF/STATUS/PATCH_GUIDE/README 및 WORK_LOG의0.32 보행 쏠림/정체·나무 발장애물 결정을 필요한 항목만 읽었습니다. dirty native/실제 index는 /workspace/artifacts/unity-0.4.0-start-state.json에 보존했습니다.

범위: 버전0.4.0/code4 및 새 Crossing-0.4.0.unity/Generated/Crossing040, Runtime/StarterConfig·PrototypeDrive와 신규SidewalkRoutes/StreetModel/StreetSimulation, Editor/StarterScene·CityEnvironment의skinned budget 집계와 신규StreetScene/StreetChecks(.meta), 관련 문서입니다. 0.3 차량 mesh/도시/조명/서명·SDK·pipeline은 재사용합니다.

하나의30Hz fixed clock으로 차량 green18s/yellow3s/차량 비움/보행 입장7s/기존 횡단 완료/재출발2s를 제어합니다. 노란 신호의 정지거리 내 차량과 이미 stop line을 지난 차량은 먼저 지나가게 하고 차량이 비운 뒤 walk를 엽니다. 보행 입장이 끝나도 횡단 중인 사람은 계속 건너며 모두 인도에 도착한 뒤 차량을 출발시킵니다. stop line·앞차 bumper gap/가감속·wrap을 연결하고 실제 travelled distance로4wheel을 회전합니다. 외형/차선 scale1을 유지합니다.

기본32명,폰 People +/-4 조절로4~100명 범위를 적용합니다. 100명 rig를 초기화 때 재사용하고 활성화는 빈 발 위치에서만 합니다. 인원 감소 시 도로를 건너는 사람은 인도 도착 후 빠집니다. 별도 설정 영구 저장·기존 날씨/WallpaperService는 후속입니다. 사람은 각자 skin/shirt/pants/hair palette와4limb rig를 갖고 single SkinnedMeshRenderer/공유8mesh·palette material로 모바일 예산을 유지합니다. shader/material budget은 skinned geometry도 실제 합산합니다.

보행은 실제2.2m tree bed rectangle와lamp/signal pole·curb/건물 범위를 피해 보도 graph를 탐색합니다. 나무 수관을 발 장애물로 취급하지 않습니다. 무작위 보도 목적지에서 이동하다 횡단 요청이 생기면 최대양쪽6자리씩 예약합니다. 빈 자리가 없으면 인도 이동을 계속합니다. 반대 방향은 zebra 안에서 서로 다른 두줄씩을 사용하고 실제 발 disc와 연속 segment 검사로 겹치지 않으며 비켜가는 후보를 선택합니다. 재등장/활성화/인원 감소 중 순간이동·도로 위 제거를 막고 focus/pause 동안 시계가 진행되지 않습니다.

StreetChecks는 실제 production model의4/32/100명,각200/300/600초 시뮬레이션에서 보도/stripe 경계·feet separation·연속이동·모든 사람 진행/횡단·대기12cap·신호복귀/도로비움·차량정차/추종/루프 bumper gap·안전 인원 감소를 확인합니다. 실제 controller의15/30/60/120Hz90초·pause와100개 rig/실제tree bed/triangle-renderer-material 예산도 확인하고 pose를 복원합니다. 현재 delimiter/meta 확인은 compile PASS가 아니며 실제 PC 검사·APK/Lint/cert·phone 결과는 대기입니다. 실패하면 근거를 기록하고 해당 입력만 수정합니다. 무관한 native 검사나 도구 설치는 반복하지 않습니다.

## 2026-10-09 — Unity 0.3.0 입체 차량·양방향 교통 APK / 실제 Editor 렌더 전달 완료

사용자가 도시 배경·조명 0.2.0 이후 다음 단계 진행을 승인해 차량 외형과 왕복 교통을 구현했습니다. 소스 commit3f733bae8c8208983a68f693cf83e9509c093634, tag unity-apk-0.3.0-build1입니다. [main 검사37931208057](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/37931208057)/job113822094112의 Windows parser·Unity6000.3.26f1 compile·실제 scene·Direct3D11 capture가 첫 실행 success이며 [APK37931656743](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/37931656743)/job113823594891도 첫 실행 success입니다. 이번에는 source compile/APK 실패나 재시도 없이 완료했습니다.

범위는 Runtime/StarterConfig·PrototypeDrive, Editor/StarterScene·신규 VehicleGeometry/TrafficFleet(.meta), Unity README와 관련 기록입니다. 경사진 앞/뒤/옆 유리·bevel 차체·A/B/C pillar·grille/앞뒤 램프·미러/도어/5spoke 회전4wheel을 4개 공유 mesh군에 생성했습니다. SUV rail/sunroof·높은 지붕, SportCoupe 낮은 roof/spoiler, Taxi roofsign, Sedan 기본형과 5색을 구분합니다. 특정 제조사 실차 모델 완성으로 표기하지 않습니다. 차선·거리에 맞춰 비균일 scaling을 적용하지 않고 root scale1로 실제 크기와 perspective를 유지합니다. 차량24대는4차선×6대, 왼쪽180도/-Z(앞모습)·오른쪽0도/+Z(뒷모습), lane별6/7/6.5/5.8m/s의 동일 속도와 route[-55,255) 원형 간격으로 통과합니다. 재등장 때 객체/자산을 새로 생성하지 않습니다. 시간은 double 누적, wheel 회전은 이동거리/반경, pause/focus 동안 이동 보류와 hidden time catch-up 없음, target30FPS입니다(실측 FPS 아님). 새 Traffic-0.3.0.unity/Generated/Traffic030은 이전 City-0.2.0/FirstRoad와 분리합니다.

실제 renderer bounds 크기/접지·unit scale·모델의 차선/거리별 동일성·방향·4wheel·URP/collider/static 플래그가 PASS입니다. 폭은 미러, 높이는 rail/sign, 길이는 램프·배기를 포함합니다.

| 모델 | 폭×높이×길이(m) |
| --- | --- |
| Sedan | 2.22×1.49×4.752 |
| SportCoupe | 2.28×1.28×4.652 |
| Suv | 2.30×1.91×5.072 |
| Taxi | 2.22×1.715×4.752 |

실제 Update의 Step을15/30/60/120Hz 각60초 후 독립 closed-form oracle와 대조해 frame phase PASS,600초/.5초 sample의 모든 같은 lane 쌍 최소 bumper gap46.75465m/횡이동·route bounds PASS입니다. wheel 각도/거리·pause/resume·zero time·양방향 wrap overshoot/3loop 및 invalid time 거부도 PASS이며 검사 후 초기 pose를 복원했습니다. 단순 함수나 expected 값만 확인하지 않고 실제 생성 fleet/Step과 bounds를 사용합니다. scene report의 visibleVehicles20은 당시 Editor aspect의 frustum count이며 폰9:20의 동일 개수를 주장하지 않습니다.

0.2.0 도시·조명·texture 생성기는 수정하지 않았습니다. 기존18개9:20/9:16 지면 rays,420m 도로/32가로수/24건물/20lamp/26skyline 및 mipmap·shader·soft shadow 검사 유지/PASS입니다. 실제 전체 scene102112triangles/2069renderers/42materials로 기존120000/2400/48 예산 안입니다. 추가 realtime light/physics collider는 넣지 않았고 같은 모델 mesh/material을 재사용합니다. 이 예산은 actual 폰 FPS/발열 검증을 대신하지 않습니다.

PC BuildPlayer 결과 Succeeded/Errors0/Warnings0, BuildApk pipeline12:41:25Z→12:45:06Z PASS입니다. Android :launcher:lintDebug Errors0/Warnings8/PASS, 실제 APK0.3.0/code3/appID com.s20plus.pixeltraffic.unityprototype/min29/target36/ARM64·v2 기존 인증서 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6가 일치합니다. APK artifact11616242104 ZIP28419919bytes(SHA2569e3753e57d2e6ba94bc7e3e905ce15e5af8eb8b4dd09d7015bab6af8d8094cf5), report artifact11616351893 ZIP1195714bytes(SHA256dc2850e52eb9fa77d64912682972bab5607a60a70a609e0c42ee1d5c1cf36da8)의 digest/CRC 및 내려받은 APK의 apksigner/aapt/CRC/il2cpp/launchable UnityPlayerActivity 검증을 통과했습니다. APK29380326bytes/SHA256 2aa10f167e8e4f0c6f9accdb10a4cb56335dde33ec6695cc4c1c816a93eb5afd입니다. 기존 PC DPAPI 서명을 재사용했고 키/암호/원본 로그는 공유하지 않았습니다.

APK와 같은 source의 실제 warmed Direct3D11 Editor camera540×1200 PNG를 열어 왕복 차량/앞뒤 램프·SUV높이와 택시 sign·도시·그림자를 확인했습니다. 최종 PNG SHA256 6a549d2dc7665c5c83852f1f66263275b862ee992f351c0524fa5bf2daf34519입니다. source는 Unity Editor 렌더이며 AI 시안/실제 폰 screenshot/주행 FPS로 표기하지 않습니다. raw desktop 캡처나 비밀 파일은 artifact에 포함하지 않습니다.

첫 Git 연결은 기본 sandbox의 proxy 접근 실패였고 허용된 추가 network 실행으로 정상 main을 확인했습니다. 최초 게시 전 git diff --check에서 새 meta2개의 각3개 empty value trailing space를 제거한 뒤 PASS입니다. 클라우드 delimiter/meta GUID 검사를 실제 compile로 주장하지 않았고 관련 PC 검사로 확인했습니다. 시작 dirty 누적 native 파일/실제 index는 보존했고 원격 main 기반 임시 index로 Unity 범위만 게시했습니다. APK/GPU 검증 후 변경은 결과 Markdown8개뿐이며 해당 docs-only commit은 [skip ci]로 같은 source 검사를 반복하지 않습니다. source/build/capture SHA manifest는 /workspace/artifacts/unity-0.3.0-source-manifest.json에 기록했습니다. 기존 SDK/JDK/캐시/자산과 서명 설정을 재사용하고 무관한 native 검사·자산 재생성은 하지 않았습니다.

전달 APK: /workspace/artifacts/pixel-traffic-unity-prototype-0.3.0.apk. 실제 Editor PNG: /workspace/artifacts/city-preview-0.3.0.png. 검증: /workspace/artifacts/unity-prototype-0.3.0-download-verification.json, 원본 PC 보고서: /workspace/artifacts/unity-prototype-0.3.0-reports/. 다음 환경에서는 해당 run artifact에서 확보하며 로컬 파일 존재를 가정하지 않습니다(보관7일). 기존 Unity prototype0.1/0.2를 업데이트하고 native0.48 앱과 공존합니다. 다운로드 폴더는 사용자 최신 집 경로입니다.

```powershell
$unityAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $unityAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.3.0.apk"
if ($LASTEXITCODE -eq 0) {
    & $unityAdb shell am start -W -n "com.s20plus.pixeltraffic.unityprototype/com.unity3d.player.UnityPlayerActivity"
}
```

미검증:0.3.0 실제 S20+/S26 Ultra 외형·FPS/발열·장시간/숨김복귀, WallpaperService 연결. 고정 lane 시제품이며 신호·인도 보행자·횡단보도/군중 회피·날씨·기존 차종 전체 이전은 후속입니다. 다음 후보는 신호/인도 보행자를 실제3D world 좌표로 이전해 안전한 횡단을 구현하고, Android 라이브 배경화면 연결 기술 검증을 이어가는 것입니다. 현재 Activity 시제품을 전체 시안 완성으로 표기하지 않습니다.

## 2026-10-09 — Unity 0.3.0 입체 차량·양방향 교통 구현 / 실제 PC 검사 예정

사용자가 도시 배경·조명 0.2.0 전달 뒤 다음 단계 진행을 승인했습니다. 최신 main46c38d3ef9344ac3f2162fc9203d1da6c96f8f12 및 Unity 관련 소스 일치를 확인했습니다. AGENTS/HANDOFF/STATUS/PATCH_GUIDE/README와 WORK_LOG의 최신 Unity 차량/시안 결정만 확인했습니다. 로컬 AGENTS의 최신 집 폴더 기록은 원격의 오래된 회사 경로보다 사용자 최신 지시가 우선합니다. 초기 dirty 누적 native 및 실제 index는 /workspace/artifacts/unity-0.3.0-start-state.json에 기록하고 보존했습니다. 초기 기본 sandbox Git 연결은 proxy 접근 실패였으나 허용된 network 실행으로 같은 main을 확인했습니다. 우회·토큰/원본키 출력이나 SDK 설치는 하지 않습니다.

범위: Runtime/StarterConfig·PrototypeDrive, Editor/StarterScene 및 신규 VehicleGeometry/TrafficFleet(.meta 포함), Unity README/관련 기록입니다. Unity6000.3.26f1/URP17.3.0, 기존 시안/도시 생성기/조명 및 DPAPI 서명·Android/Lint pipeline을 재사용합니다. 버전0.3.0/code3, 새 Traffic-0.3.0.unity/Generated/Traffic030으로 이전 City-0.2.0/FirstRoad 자산을 보존합니다.

차량은 특정 제조사 모델의 완성본이 아니라 3D 세단·낮은 스포츠 쿠페·높은 SUV·택시 기본군입니다. metre 단위 bevel 차체/경사진 windshield·rear/side glass, A/B/C pillar·범퍼/grille·앞뒤 램프·미러·도어선·배기·5spoke wheel을 공통 mesh/material별로 합쳐 생성하고 재사용합니다. SUV roofrail/sunroof, sports rear spoiler, taxi roof sign을 구분합니다. root scale1을 유지해 차선·거리별 폭/높이 압축이 없습니다. 각 차량 바퀴4개는 이동거리와 반경으로 회전하며 실제 단일 태양 그림자를 사용하고 추가 real-time light/physics collider를 만들지 않습니다.

4개 차선×6대=24대, 왼쪽 두 차선은 -Z/180도(앞모습), 오른쪽은 +Z/0도(뒷모습)입니다. 각 차선은 고정 동일 속도6/7/6.5/5.8m/s로 순환 간격을 유지합니다. route[-55,255), phase는 double 누적으로 wrap overshoot/multi-loop를 유지하며 루프 때 객체/자산을 생성하지 않습니다. pause/focus callback과 target30FPS를 유지하고 숨긴 시간은 따라잡지 않습니다. signal/보행자/차선 변경/추월은 후속이며 현재 교통을 기존 native simulation 전체 이전으로 표기하지 않습니다.

TrafficFleet.Validate는 실제 생성된24대/4모델·차선/정방향·전체 renderer bounds/접지·root unit scale/모델별 크기 동일·SUV높이와 스포츠 낮은 높이·4wheel·URP/collider/static 플래그·카메라 내 차량 수를 검사합니다. 실제 Update의 Step을15/30/60/120Hz에서 독립 closed-form 위치와 대조하고,10분/.5초 샘플 모든 같은 차선 쌍의 bumper gap·wrap/횡이동,바퀴각/거리·pause/resume와 invalid time을 확인한 뒤 모든 pose를 복원합니다. 기존 CityEnvironment의18portrait rays/mipmap/shadow 및120000triangles/2400renderers/48materials 예산은 낮추지 않습니다.

클라우드 기본 delimiter 확인은 C# compile이 아닙니다. main의 실제 Windows parser/Unity compile·scene·GPU preview를 먼저 확인한 뒤 같은 검증 commit에 APK tag를 게시합니다. Android BuildPlayer/Lint errors0/원본v2 cert/version/ARM64·SDK·해시를 확인하고 APK/실제 Editor PNG를 전달합니다. 현재 검증/빌드/phone FPS·발열은 미완료입니다. 현 폴더 C:\Users\김백현\Desktop\AI와 명시적 Unity SDK adb.exe 설치/Activity 실행 방식을 유지합니다.

## 2026-10-09 — Unity 도시 배경·조명 0.2.0 APK 및 실제 렌더 미리보기 전달

사용자가 승인한 도시 배경·조명 패치를 완료했습니다. APK 소스2ebedb50873fb0a6678fe7c82cae220a5abd68d8/tag unity-apk-0.2.0-build1의 [실행37926489436 attempt2](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/37926489436)이 success입니다. 첫 attempt는 UPM IPC disconnected/package resolve cancelled0.24초로 BuildPlayer 전 실패했고 소스/서명/캐시를 바꾸지 않은 동일 failed-job 재시도1회에서 회복했습니다. 최초 main의 internal softShadowQuality CS1061은 serialized asset 설정으로 수정 후 [실행37926186270](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/37926186270)의 Windows parser/Unity 컴파일/장면 검사 PASS로 확인했습니다. 실패 이력은 보존하며 검사를 끄거나 APK 실패를 무시하지 않았습니다.

실제 도시는 도로420m/지면520m, 나무32/건물24/가로등20/먼 skyline26동/능선3개입니다. 건물 창틀·층 경계·차양·옥상 설비, 겹친 수관/화단, trilinear/mipmap 아스팔트·석재·보도, 따뜻한 단일 태양과70m/2048/2cascade/Low soft shadow를 적용했습니다. 카메라 9:20·9:16 총18지면 ray coverage와54344triangles/1757renderers/39materials budget, URP shader·그림자·fog·mip 검사 PASS입니다. 기존 차량 한 대/6m/s/4wheels/2.13m×1.55m×4.30m 차체/차선·직선 주행·프레임 독립 검사도 PASS입니다. 새 City-0.2.0 장면/Generated/City020은 FirstRoad와 분리했습니다.

PC Unity6000.3.26f1 BuildPlayer11:58:06Z→12:01:38Z가 Succeeded/Errors0/Warnings0이며 pipeline12:02:02Z PASS, Android Lint :launcher:lintDebug Errors0/Warnings8/PASS입니다. 내려받은 reports ZIP261141bytes(artifact11615040226), APK ZIP28337653bytes(artifact11614845508)의 GitHub digest/CRC와 실제 APK의 apksigner/aapt/CRC/il2cpp/UnityPlayerActivity 검증이 통과했습니다. APK29290238bytes/SHA256 d5bfc5ab4bdea167ba86ffdb12475ae161317ebc8934822976238814620a3703입니다. 기존 v2 인증서 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6, appID com.s20plus.pixeltraffic.unityprototype/0.2.0/code2/min29/target36/ARM64가 PC report와 일치합니다. 키/암호/원본 Editor·Lint 로그는 공개하지 않았습니다.

처음 actual Editor PNG에서 색이 다르게 보이는 문제를 실제 이미지 확인으로 발견해 전달을 보류했습니다. 로드된 주요 _BaseColor는 소스와 일치했고, 별도 Editor update에서 실제 렌더3회로 GPU를 준비한 다음 캡처하자 회색 도로·초록 나무·파란 차량·노면 texture가 정상으로 표시됐습니다. 이는 관측된 초기 캡처 문제이며 엔진 내부 원인을 특정 버그로 단정하지 않습니다. 기본 SRP Batcher 렌더와 잠시 끈 diagnostic의 전체 pixel bytes가 완전히 동일하므로 최적화 설정은 유지했습니다. [미리보기 검사37928464521](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/37928464521), main f58d1e8055ce24ada183565acb1f2bad64c7b203의 compile/parser/scene/capture/report가 success입니다. 실제 Direct3D11/540x1200 기본 PNG SHA256 9498572fda2e703221576e6288e00c08de951fbc6a58dbdedbdcd089170b3138입니다. Unity Editor 카메라 결과이며 폰 screenshot/FPS 검증이 아닙니다.

캡처 수정은 Editor CityPreview와 workflow만 포함합니다. APK 빌드 당시와 현재의 StarterConfig/PrototypeDrive/StarterScene/CityEnvironment/PrototypeBuild/run-pipeline Git blobs가 동일함을 확인해 장면·런타임·빌드 입력이 변하지 않았습니다. 해당 Editor-only 문제 확인에 Android를 반복 빌드하지 않았습니다. main Validate에도 선택적 실제 렌더를 넣어 앞으로 소스 변경 때 시각 오류를 확인할 수 있고, 그래픽 없는 runner는 PIXEL_TRAFFIC_CAPTURE=false로 생략합니다. raw 로그 대신 비밀 제거 JSON만 공유합니다. 최신 결과 문서/README/Unity 검사 표는 docs-only로 반영해 검사를 재실행하지 않습니다. 누적 native 변경과 실제 index는 보존했습니다.

전달 APK: /workspace/artifacts/pixel-traffic-unity-prototype-0.2.0.apk. 실제 기본 Editor 미리보기: /workspace/artifacts/city-preview-0.2.0.png. APK 검증: /workspace/artifacts/unity-prototype-0.2.0-download-verification.json. 원본 PC 보고서: /workspace/artifacts/unity-prototype-0.2.0-reports/ 및 /workspace/artifacts/unity-0.2.0-preview-diagnostics/. 다음 환경의 로컬 파일 존재를 가정하지 말고 각 Actions run의 artifact에서 확보합니다(7일 보관). APK와 미리보기의 source commit은 위처럼 구분합니다.

사용자 다운로드 폴더에 새 APK를 저장하고 아래 Unity SDK ADB를 직접 호출합니다. 이전 bare adb 무출력 원인은 확정하지 않았으나 이 직접 호출 방식 후 0.1.0 실제 폰 실행을 사용자에게 확인했습니다. 같은 prototype appID/code2라 테스트앱0.1.0을 업데이트하며 기존 native 앱과 공존합니다.

```powershell
$unityAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $unityAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.2.0.apk"
if ($LASTEXITCODE -eq 0) {
    & $unityAdb shell am start -W -n "com.s20plus.pixeltraffic.unityprototype/com.unity3d.player.UnityPlayerActivity"
}
```

0.2.0 실제 폰의 외형/FPS·발열·장시간 실행과 두 기종 모두의 확인은 사용자 테스트 대기입니다. Development Activity 시제품이며 WallpaperService/기존 군중·신호·날씨·차종은 아직 이전하지 않았습니다. 다음은 목표 시안에 맞춘 차량 본체 3D 외형·양방향 교통을 별도 패치로 보강하고 Android 라이브 배경화면 연결 기술 검증을 이어갑니다. 이번 패치를 시안 전체 완성으로 표기하지 않습니다.

## 2026-10-09 — Unity 0.2.0 Android 성공 / 실제 Editor 색상 렌더 조사

실행37926489436 attempt2/main2ebedb50873fb0a6678fe7c82cae220a5abd68d8/build1은 completed/success입니다. 동일 실패 job만 한 번 재시도한 뒤 UPM 초기 IPC 실패가 재발하지 않았고 실제 Unity BuildApk11:58:06Z→12:01:38Z, pipeline12:02:02Z PASS, Lint Error0/Warnings8, code2/version0.2.0/min29/target36/ARM64/v2/기존 cert 검증이 통과했습니다. 내려받은 APK29290238bytes의 SHA256 d5bfc5ab4bdea167ba86ffdb12475ae161317ebc8934822976238814620a3703와 양쪽 ZIP의 GitHub digest/CRC, 실제 APK apksigner/aapt/launcher/il2cpp 검증도 PASS입니다.

Direct3D11 Editor Capture는12:02:03Z→12:02:21Z에 PASS로540x1200 PNG를 만들었으나 실제 이미지를 열어보니 도로가 갈색이고 파란 차량이 밝은 베이지처럼 보여 지정 재질 색과 다른 것을 발견했습니다. 초기 black/pink 검사는 이 오류를 구분하지 못했습니다. 최종 전달을 보류하고 캡처/재질/renderer 경로를 구분합니다. 과거 다른 Unity 버전의 issue 검색은 이 버전의 원인 근거로 단정하지 않습니다.

이번 추가 수정은 CityPreview Editor 캡처 코드와 workflow뿐이며 앱의 장면 생성/런타임/서명/버전은 유지합니다. 세 번의 실제 renderer warmup을 별도 Editor update에서 수행한 뒤 캡처하고 지정 주요 재질의 실제 loaded _BaseColor를 JSON에 저장합니다. SRP Batcher를 잠시 끈 추가 diagnostic PNG도 별도로 만들고 반드시 설정을 복원합니다. 기본 렌더와 diagnostic을 구분하며 diagnostic을 그대로 폰 화면이라고 표기하지 않습니다. main Validate 후 optional 실제 capture도 수행하게 해 APK 재빌드 없이 이 Editor 코드의 컴파일·렌더를 확인합니다. actionlint1.7.7 exit0입니다. 다음 실제 이미지/재질 data로 원인을 판단하고 필요한 소스만 수정합니다.

## 2026-10-09 — Unity 0.2.0 최초 Validate 실패 / 내부 그림자 품질 API 수정

실행37925818743/main4d37a726f22550bb51e90f6d6333135903c11b91은 Windows parser 이후 Unity Validate가11:47:31Z에 exit1로 실패했습니다. 무거운 APK tag를 아직 게시하지 않아 동일 오류로 APK 작업을 반복하지 않았습니다. 초기 workflow는 Validate 상세 raw 로그를 보존/비밀 제거하지 않아 원인 본문이 보고서에 없었습니다.

독립적인 source 대조에서 URP6000.3 softShadowQuality가 internal property임을 확인했습니다(https://raw.githubusercontent.com/Unity-Technologies/Graphics/6000.3/staging/Packages/com.unity.render-pipelines.universal/Runtime/Data/UniversalRenderPipelineAsset.cs). 직접 호출을 제거하고 같은 URP asset의 serialized m_SoftShadowQuality를 Low로 지정합니다. 이는 확인된 source 접근 오류를 수정하는 것이며 이전 PC 오류 메시지는 다음 redacted diagnostic으로 별도 확인합니다. run-unity는 모든 실패 Mode의 로그를 즉시 unity-failure.json으로 비밀 제거하고 workflow는 이전 Validate/BuildApk 실패 로그를 checkout clean 전에 보존합니다. raw 로그/키/암호는 공유하지 않습니다. actionlint1.7.7 exit0이며 수정된 main Validate에서 실제 컴파일/geometry 결과를 확인합니다. APK/시각 결과는 아직 미완료이며 누적 native/실제 index를 보존합니다.

## 2026-10-09 — Unity 도시 배경·조명 0.2.0 구현 / PC 검사 예정

사용자가 실제 폰 0.1.0 화면 확인 뒤 목표 시안으로 도시 배경·조명 패치를 진행하도록 지시했습니다. 최신 원격 main ed70807c0948002e0000ce4a58f29e406db1925d와 기존 Unity 소스6개 hash를 대조해 일치했고 실제 index가 비어 있음을 확인했습니다. 로컬 native/문서 누적 수정37개·미추적 묶음40개는 보존하며 초기 범위는 /workspace/artifacts/unity-0.2.0-start-state.json에 기록했습니다. AGENTS·HANDOFF·STATUS·PATCH_GUIDE와 필요한 최신 WORK_LOG 항목/실제 시안 PNG를 읽었습니다. 유니티 시안은 생성형 목표 이미지이며 현재 실행 화면으로 취급하지 않습니다.

StarterConfig 버전0.2.0/code2와 새 City-0.2.0 장면/Generated/City020을 사용해 기존 FirstRoad 자산과 구분합니다. CityEnvironment Editor 생성기로 420m 도로·520m 지면·먼 skyline26동/능선3개·건물24개(창틀/층 경계/상점 차양/옥상 설비)·가로수32개(저다각형 겹친 수관/화단)·가로등20개와 mipmap/trilinear 노면·보도·석재 texture를 구현했습니다. 근거리 warm 태양 하나, URP soft shadows/70m/2048/2cascades/Low, foreground collider 제거와 static batching flag로 모바일 범위를 제한합니다. 차량6m/s/한대/4wheels/기존 물리 차선·30FPS 설정은 유지하며 실제 FPS를 주장하지 않습니다.

Scene Validate에 실제 생성된 카메라/지면의 9:20·9:16 전체18ray/도로끝 coverage, shader/material·그림자·fog·texture mip, object 수와 triangle<120000/renderer<2400/material<48 budget을 추가했습니다. Version/파일명/metadata 검사는 StarterConfig 상수와 일치시키고 검증된 APK 경로만 GITHUB_ENV로 upload합니다. 기존 서명 복원/ASCII cache/Windows properties 정규화/Lint Error0 판정은 유지합니다.

선택적 CityPreview와 Capture 모드는 PC GPU/URP.SingleCameraRequest로 540x1200 Editor PNG를 생성합니다. black/pink 검사를 포함하고 원본 로그/키/암호는 공유하지 않습니다. 캡처는 optional이며 APK 필수 검사 실패를 우회하지 않습니다. UnityEditor 렌더를 실제 폰 화면/FPS라고 표기하지 않습니다. 그래픽 없는 runner는 PIXEL_TRAFFIC_CAPTURE=false로 생략할 수 있고 Capture 프로세스는 최대3분 제한합니다.

공식 URP17.3 API/Unity Graphics6000.3 원본에서 shadows getter/serialized field 및 render request를 확인했습니다. 직접 docs HTTPS 읽기는 session proxy403으로 실패해 제공된 web tool과 허용된 GitHub 원본을 사용했고 우회하지 않았습니다. workflow actionlint1.7.7 exit0입니다. 변경 C#·PowerShell의 실제 컴파일/parser/장면 검사는 main Validate에서 먼저 확인하고, 통과한 같은 소스에 APK tag를 게시해 Android/Lint/cert/metadata/다운로드 검증을 이어갑니다. 현재 PC 실행 전이므로 APK/시각 결과는 완료로 기록하지 않습니다. 누적 native 작업/실제 index/비밀값은 게시하지 않습니다.

## 2026-10-09 — Unity 0.1.0 실제 폰 첫 화면 표시 확인

사용자가 명시적 Unity SDK adb.exe 호출·install -r·UnityPlayerActivity am start 안내 후 “오! 됐다”라고 답하고 세로 화면 스크린샷을 제공했습니다. 해당 화면에서 원근 도로/중앙선/횡단보도·보도·양쪽 건물과 나무·파란 차량1대·Development Build 표기를 확인했습니다. 사용자 결과로 첫 설치/Activity 실행과 실제 폰의 초기 3D 화면 표시를 확인한 상태로 갱신합니다. 처음 bare adb 명령의 무출력 원인은 확인되지 않았으므로 alias/다른 실행 파일 문제로 단정하지 않습니다.

단일 스크린샷은 차량 움직임·지속 FPS·발열 추세·장시간 안정성·S20+와 S26 Ultra 모두의 통과를 입증하지 않습니다. 현재 사진의 기종도 별도 확인되지 않았으며 오버레이 온도 수치를 앱의 성능/발열 원인이나 측정 결과로 해석하지 않습니다. WallpaperService/기존 설정·날씨·군중·차종 이전은 여전히 다음 단계입니다. APK·소스·빌드 입력 변경 없이 사용자 검증 결과만 기록하며 Android/Lint/서명 검사를 반복하지 않습니다.

다음 시각 품질 패치 제안: 도로 끝/도시 지면과 배경을 이어 화면 상단의 단순한 푸른 빈 공간을 다듬고, 따뜻한 오후 조명과 접지 그림자·노면/보도 재질을 보강해 목표 시안의 구도와 분위기에 접근합니다. 이어 차량 본체의 3D 외형·양방향 주행과 Android 배경화면 연결을 별도 작은 단계로 진행합니다. 이 항목은 제안이며 아직 구현하지 않았습니다. PC runner를 활용한 소스→빌드/검사→APK→사용자 폰 확인 흐름이 실제로 연결됐습니다.

## 2026-10-09 — 폰 설치 명령 무출력 / 실기기 설치·실행 확인 대기

사용자 PowerShell 화면에서 일반 adb install -r 명령 뒤 Success/Failure/Performing Streamed Install 없이 prompt 복귀를 확인했습니다. 실제 설치 성공이나 앱 실행 실패를 확정하지 않습니다. adb install은 설치 후 앱을 자동 실행하지 않으므로 설치와 Activity 시작을 구분합니다. 기존 bare adb가 어떤 alias/function/executable로 해석됐는지도 미확인입니다.

실제 빌드 로그에서 확인한 Unity6000.3.26f1의 C:/Program Files/Unity/Hub/Editor/6000.3.26f1/Editor/Data/PlaybackEngines/AndroidPlayer/SDK/platform-tools/adb.exe를 PowerShell 호출 연산자(&)로 직접 실행하는 안내를 준비했습니다. devices→동일 APK install -r→성공 종료 코드인 경우 검증된 com.unity3d.player.UnityPlayerActivity를 am start -W -n으로 시작합니다. unauthorized는 폰의 USB 디버깅 허용, 기기 목록 없음은 연결/디버깅, 여러 기기는 -s 대상 선택으로 구분합니다. 실제 사용자 PC의 ADB 결과는 아직 도착하지 않았고 폰 설치/실행 PASS로 기록하지 않습니다. APK/소스/빌드 입력이 바뀌지 않아 Android/Lint를 반복하지 않습니다.

## 2026-10-09 — 첫 Unity 테스트 APK 0.1.0 빌드·검증 완료

사용자의 PC 서명 READY를 확인한 뒤 기존 키로 첫 Unity Activity 테스트 APK를 만들었습니다. [실행37922009684](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/37922009684)은 completed/success이며 소스 main1610a02d17d8761d533356c6426882df22ebf599, 태그 unity-apk-0.1.0-build5입니다. 동일 소스 main Validate 실행37922007439도 success입니다. 사용자 PC의 Unity6000.3.26f1 BuildPlayer는11:11:35Z→11:14:46Z에 Succeeded/Errors0/Warnings0, 전체 BuildApk pipeline은11:15:07Z에 PASS였습니다. 생성된 Windows properties의 drive colon 정규화 후 Android Lint :launcher:lintDebug는 Errors0/Warnings8/PASS입니다. 경고는 0으로 기록하지 않으며 경고 상세의 분류/수정은 이번 전달에서 수행하지 않았습니다.

Windows 소유권 guard·비ASCII Prefab 캐시·bundled Gradle 실행 파일·PropertyEscape 실패를 실제 오류 근거에 따라 단계별 수정했습니다. 독립 읽기 조사로 Unity Gradle User Home API, 실제 Editor의 Java launcher 호출, Java properties의 동일 경로 의미와 Android PropertyEscape 규칙을 대조했습니다. 원인별 실패 이력은 아래에 보존하며 검사를 끄거나 전체 오류 ignore를 추가하지 않았습니다. PC 원본 키/DPAPI 설정·게임 소스·기존 native 누적 로컬 변경과 실제 index는 보존했고 필요한 Unity 자동화 파일만 main에 게시했습니다.

다운로드한 Actions reports ZIP1621bytes(artifact11612244264)와 APK ZIP27325842bytes(artifact11612079438)의 GitHub SHA256 및 ZIP CRC가 일치했습니다. 클라우드 기존 SDK35의 apksigner/aapt로 실제 내려받은 APK를 별도 검증했습니다. 기존 인증서 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6와 v2 서명이 일치하며 appID com.s20plus.pixeltraffic.unityprototype/version0.1.0/code1/minSdk29/targetSdk36/ARM64만 포함합니다. UnityPlayerActivity launcher와 libil2cpp.so/APK CRC를 확인했습니다. 실제 APK28073846bytes의 SHA256은6b96c6cbfeb613621ec2c81e15c9b70a31acb7bcb24df7d4c807d9f76ee4b20f이며 PC report의 값과 일치합니다.

전달 파일은 /workspace/artifacts/pixel-traffic-unity-prototype-0.1.0.apk, 보고서는 /workspace/artifacts/unity-prototype-0.1.0-reports/ 및 /workspace/artifacts/unity-prototype-0.1.0-download-verification.json입니다. 특정 cloud 로컬 파일의 다음 환경 존재를 가정하지 말고 Actions run의 artifacts에서도 확보할 수 있습니다. Actions artifacts는7일 보관입니다. 새 키/비밀번호/원본 Editor·Lint 로그는 Git/공유 artifacts에 포함하지 않습니다.

사용자 Windows 폴더에 APK를 다운로드한 뒤 아래 명령을 실행하고 앱 목록의 Pixel Traffic Unity Prototype을 엽니다. 기존 native 앱과 다른 appID라 함께 설치됩니다.

```powershell
adb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.1.0.apk"
```

완료 범위는 3D 첫 도로·보도·건물·나무·차량1대의 세로 Activity 시제품과 PC 자동 빌드/검사/결과 공유입니다. 라이브 배경화면 WallpaperService, 기존 설정/군중/날씨/차종 이전, 실제 폰 실행/FPS·발열·캡처는 아직 미검증/미구현입니다. S20+/S26 Ultra에서 실행/세로 화면을 먼저 확인한 후 품질 패치와 Android 배경화면 연결을 별도 단계로 이어갑니다. 앞으로 PC runner 창을 유지하면 Unity GUI를 직접 열지 않고 같은 pipeline을 실행할 수 있으며 클라우드 runner 이동 구성은 유지합니다. 결과 문서만 게시해 같은 Unity 검사를 반복하지 않습니다.

## 2026-10-09 — Lint PropertyEscape 5건 / 생성된 Windows 경로 표기 수정

진단 실행37921621539의 report ZIP6040bytes(CRC 정상)에서 prior-lint-failure.json 원본을 확보했습니다. 이전 build4의 오류5건은 모두 PropertyEscape이며 Unity 프로젝트 D:/ 경로2건과 Unity Android SDK/NDK/OpenJDK의 C:/ 경로3건이었습니다. 이 결과는 APK 실행/서명 실패가 아니며 생성된 properties의 drive colon 표기 검사입니다.

run-pipeline.ps1의 Lint 직전에 생성된 Android Gradle root gradle.properties와 local.properties만 읽고 값 시작의 미이스케이프 Windows drive colon을 D:/→D\:/ 형식으로 정규화합니다. 이미 escaped 된 값·상대/Unix 경로·다른 설정·Git 소스 템플릿·APK 내용은 바꾸지 않습니다. UTF8 BOM 없이 저장하며 Java Properties로 읽은 경로 의미를 유지합니다. Lint 검사/오류 판정을 유지하고 일괄 ignore/baseline을 추가하지 않습니다. 실제 재검사는 build5에서 수행하며 아직 완료로 기록하지 않습니다.

진단 main의 parser/Validate/보고서 업로드도 성공했습니다. 이번 변경은 공유 pipeline 한 파일과 결과 문서이며 PC 서명 READY·게임 소스·기존 native 누적 변경/실제 index를 보존합니다. 향후 검증 실패는 오류 개수/비밀 제거 요약을 같은 run의 보고서로 남깁니다. APK는 Lint·기존 인증서·메타데이터 사후검사 통과 후 전달합니다.

## 2026-10-09 — APK build4 생성/Gradle 실행 성공 / Lint 오류 상세 확보

main94ad9adecc22fc88df3210a085c6e5c4939163dd/build4의 실행37920861811에서 Windows parser와 APK BuildPlayer가 성공했습니다. Unity 빌드는11:00:12Z→11:03:04Z였고 bundled launcher 방식의 :launcher:lintDebug도 실행됐습니다. Gradle exit0 후 XML에서 Fatal/Error를 발견해 pipeline이11:03:38Z에 의도대로 실패했고 APK 공유는 하지 않았습니다. 기존 구현은 오류 개수 JSON을 throw 뒤에 저장해 오류 상세를 artifact에 남기지 못했습니다.

run-pipeline.ps1은 PASS/FAILED와 실제 오류·경고 개수를 throw 전에 저장하고 실패 XML을 기존 비밀 제거 collector로 요약합니다. workflow는 다음 checkout의 clean 전에 이전 Lint XML을 RUNNER_TEMP에 임시 보존하고 prior-lint-failure.json만 공유하며 임시 원본은 finally에서 삭제합니다. 검사를 끄거나 오류를 일괄 억제하지 않으며, 원인 항목을 먼저 확인합니다. actionlint1.7.7 exit0이고 다음 main Validate에서 이전 XML 상세를 확보한 뒤 필요한 Android 수정만 결정합니다. 게임/서명 설정·누적 native 변경/실제 index를 보존하며 APK는 아직 미전달입니다.

## 2026-10-09 — APK build3 생성 성공 / Unity bundled Gradle Lint 실행 수정

[실행37919979188](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/37919979188), main814cd93e34a8d2ae66fd9f3f2d2d694bc28c30f6/build3의 원본 보고서 ZIP1009bytes(CRC 정상)는 Android BuildPlayer Succeeded/Errors0/Warnings0, scene PASS를 기록합니다. ASCII Gradle/temp 우회 후 CXX1429가 재발하지 않아 해당 우회 효과를 실제 빌드로 확인했습니다. 다만 후속 Lint에서 Tools/gradle/bin/gradle.bat가 없어 pipeline은 FAILED였고 Lint·APK 사후검사·공유는 미완료입니다.

Unity 실제 로그의 Gradle 실행 형식을 별도 읽기 조사로 확인했습니다. run-pipeline.ps1은 bin 스크립트가 있으면 유지하며 없으면 Tools/gradle/lib/gradle-launcher-*.jar 정확히1개를 찾아 Unity OpenJDK의 java -classpath launcherJar org.gradle.launcher.GradleMain으로 동일 :launcher:lintDebug를 실행합니다. 공백 경로는 PowerShell 인자 배열로 전달합니다. Gradle8.13 launcher manifest/CLI main 원본도 대조했습니다. 새로운 도구를 설치하거나 게임 소스·서명 설정을 바꾸지 않습니다.

Lint 실행 실패 시 raw 출력은 PC Reports/android-lint.log에만 두고 기존 비밀 제거 collector가 verification-failure.json을 생성해 원인을 같은 실행에서 확인하도록 했습니다. workflow는 해당 JSON만 허용 목록에 추가하며 원본 로그·비밀번호·키를 공유하지 않습니다. actionlint1.7.7 exit0을 확인했고 수정 PowerShell은 PC의 실제 parser와 build4로 검증합니다. build1/2/3 실패 이력과 누적 native 변경/실제 index를 보존했습니다. APK는 모든 검사 통과 후 전달하며 현재 미전달입니다.

## 2026-10-09 — Android Prefab CXX1429 / ASCII 빌드 캐시 우회

진단 실행37919224433에서 이전 로그 보존/비밀 제거/새 PS parser/Validate/보고서 업로드가 성공했습니다. 진단 ZIP4369bytes의 prior-build-failure.json을 내려받아 :unityLibrary:configureCMakeDebug[arm64-v8a] / CXX1429 / prefab_command.bat exit1을 확인했습니다. 일반화된 원인 메시지는 한국어 출력이 ???로 손실돼 확정하지 않았습니다. 기본 Gradle 캐시가 비ASCII Windows 사용자 폴더이며 공식 Unity6.3 API가 같은 경우 사용자명 변경 대신 Gradle User Home 지정 방법을 안내하는 근거를 확인했습니다: https://docs.unity3d.com/6000.3/Documentation/ScriptReference/Android.AndroidExternalToolsSettings.Gradle-userHomePath.html. 독립 읽기 조사 에이전트도 이 근거와 같은 우회를 제안했습니다.

BuildApk Windows 프로세스에 한해 runner의 ASCII PixelTrafficBuildCache/gradle 및 temp를 지정합니다. 기존 .gradle의 modules-2 의존성을 robocopy로 복사해 재사용하고 원본 캐시/사용자 계정/레지스트리/SDK를 바꾸거나 삭제하지 않습니다. cacheRoot 비ASCII는 거부하며 PIXEL_TRAFFIC_BUILD_CACHE로 지정할 수 있습니다. GRADLE_USER_HOME/TEMP/TMP는 finally에서 복원합니다. Unity가 자체 Gradle User Home 설정을 우선할 수 있어 PrototypeBuild의 APK 빌드 동안 공식 AndroidExternalToolsSettings.Gradle.userHomePath도 같은 경로로 지정 후 finally에서 복원합니다. 게임 장면/런타임/서명 입력은 유지합니다.

이는 관측된 native path 실패에 대한 근거 있는 우회이며 아직 성공으로 확정하지 않습니다. 변경 C#의 PC 컴파일 및 새 PS 실제 parser/Validate 후 build3에서 실제 Android/Lint/cert/metadata를 확인합니다. 기존 실패 APK를 전달하지 않고 build1/2 이력을 보존합니다.

## 2026-10-09 — APK build2 Android 오류 3건 / 원인 요약 확보

실행37918287205는 main e1e15781fac2601d4d349f213d2a928db55be7c7/build2에서 tag guard가 통과했고 DPAPI 서명 읽기·원본 cert·장면 검사를 지나 실제 BuildPlayer가 실패했습니다. 다운로드한 report ZIP1008bytes의 scene-validation PASS, android-build-result Failed/Errors3/Warnings0, pipeline BuildApk FAILED를 확인했습니다. Lint/서명 사후검사/APK 업로드는 미실행입니다. Unity는10:34:56Z→10:38:33Z에 exit1로 종료됐습니다.

보고서에는 오류 원인 상세가 없어 PC의 이전 Editor 로그를 다음 checkout의 git clean 전에 RUNNER_TEMP에 임시 보존하고 새 collect-build-failure.ps1로 오류 주변 최대180줄을 요약합니다. 암호화 PC credentials/환경 비밀번호·키경로·사용자 경로를 제거하고 암호를 읽을 수 없으면 실패하도록 합니다. 원본 raw 로그는 Git/Actions artifact에 넣지 않고 임시 사본은 finally에서 삭제합니다. 요약만 prior-build-failure.json으로 공유해 실제 원인을 확인한 뒤 필요한 수정만 합니다. workflow actionlint1.7.7 exit0이며 PC parser/요약 추출 실행은 후속 job에서 확인합니다. APK는 아직 전달하지 않습니다.

## 2026-10-09 — PC 서명 READY / 첫 APK tag 소유권 guard 수정

사용자가 PC 서명 복원 READY를 확인했습니다. 원격 main08764ad7673914de1adf3e025fcfb0c86a8bc572에 unity-apk-0.1.0-build1을 게시했고 실행37918104064가 PC에서 시작됐으나 Git guard가 dubious ownership / filesystem does not record ownership으로 실패했습니다. Unity/서명/APK 단계는 실행되지 않았고 보고서도 없었습니다. checkout action의 임시 HOME safe.directory 설정이 후속 사용자 shell에 이어지지 않는 문제입니다. guard의 fetch/rev-parse에 -c safe.directory=$env:GITHUB_WORKSPACE를 각각 지정해 실제 checkout 폴더만 해당 명령에서 신뢰하도록 수정합니다. PC 전체 Git 신뢰 설정이나 '*' 신뢰는 추가하지 않습니다.

workflow actionlint1.7.7 exit0을 확인했고 수정 main에 새 build2 tag로 재개합니다. 기존 build1 tag/실패 기록을 보존합니다. 키/비밀번호/실제 index 및 누적 native 작업을 보존하며 APK는 아직 생성되지 않았습니다. 이후 실제 APK/Lint/cert/metadata 결과를 별도로 확인합니다.

## 2026-10-09 — APK 준비 자동화 Windows 검사 성공 / 최초 복원 대기

main179e795201b6f67683ac87819973c2db2fd4aab0의 [실행37916907750](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/37916907750)은 completed/success입니다. 실제 Windows parser로 신규 configure-signing.ps1을 포함한 tools/*.ps1 구문 검사 및 변경 run-pipeline의 Validate/Unity 종료/보고서 업로드가 통과했습니다. Unity 실행 로그는10:19:59Z→10:21:02Z입니다. DPAPI 복원과 BuildApk/Lint/태그 guard의 실제 실행은 이 검사에서 하지 않았습니다.

사용자에게 Git 밖의 원본 백업 ZIP와 개인 setup-unity-signing.ps1을 전달했습니다. 개인 설정 파일은 공개 helper의 SHA256을 확인하며 Git Windows LF/CRLF 체크아웃 두 형식을 허용합니다. 기존 키의 암호는 클라우드 keytool로 인증서 확인 후 개인 파일에만 준비했고 Git/문서/명령 예시/Actions artifacts에는 게시하지 않았습니다. 본인 Windows 사용자로 별도 PowerShell에서 파일 선택/DPAPI 설정을 실행하면 READY를 표시합니다. runner 창을 유지하고 READY 결과가 도착하기 전에는 APK 태그를 만들지 않습니다. 이는 승인 재요청이 아니라 최초 PC 키 복원에 필요한 작업입니다.

현재 전달은 서명 복원 도구이며 새 APK가 아닙니다. 다음 담당자는 READY를 확인하고 최신 원격 main SHA에 unity-apk-0.1.0-build1 태그를 게시한 뒤 실제 job·Lint·cert/metadata 보고서·APK를 확인해야 합니다. 실제 APK 전달 때 C:\Users\김백현\Desktop\AI 경로의 PowerShell 설치 명령을 제공하고 Activity 시제품과 배경화면 이전을 구분합니다. native 누적 변경/real index는 보존하며 이 문서 결과 갱신은 docs-only로 게시해 Unity 검사를 반복하지 않습니다.

## 2026-10-09 — 첫 Unity APK 준비 / PC 원본 키 복원 대기

사용자가 기존 서명으로 Unity 테스트 APK 빌드를 승인했고 PC에는 원본 서명 백업이 없다고 답했습니다. 첨부 원본 ZIP의 debug.keystore hash가 기존6e3050b…와 일치하고 클라우드 기존 키/keytool의 공개 인증서가 a6e489ad…임을 확인했습니다. 원본 백업 사본과 개인 복원 실행 파일을 Git 밖의 사용자 다운로드용 artifacts에 준비했습니다. 실제 키/암호는 Git/Actions artifact에 넣지 않습니다. 최초 keytool 경로 가정이 틀려 실제 설치된 /usr/bin/keytool로 확인했으며 새 도구를 설치하지 않았습니다.

변경 범위는 Unity 빌드 자동화입니다. tools/configure-signing.ps1이 백업의 항목/32KB 상한/hash를 확인하고 다른 키는 덮어쓰지 않으며 LOCALAPPDATA/PixelTraffic/Signing에 복원합니다. 현재 Windows 사용자/SYSTEM만 접근하도록 ACL을 제한하고 인증서 확인 후 PSCredential을 DPAPI 사용자 암호화 XML로 보관합니다. 같은 라이선스 사용자로 runner를 실행해야 합니다. 개인 복원 실행 파일은 암호를 공개 소스나 명령 예시에 넣지 않기 위해 Git 밖에만 두며 사용자 PC에서 백업 선택을 돕습니다.

run-pipeline은 BuildApk에 한해 명시된 cloud/env 서명이 없으면 PC의 암호화 설정을 읽고 환경을 finally에서 복원합니다. 이전 workflow의 빈 secrets 환경변수가 PC 상속 암호를 가리는 문제도 해결합니다. Windows PowerShell의 native stderr 성공 메시지는 종료 코드로 판단합니다. Unity APK 이후 생성된 Gradle launcher:lintDebug/원본 cert/v2/appID/version/min29/target>=29/ARM64를 확인한 뒤 업로드합니다. Lint 요약 JSON만 추가 공유하고 raw 로그/키/암호는 제외합니다.

main 변경은 계속 Validate입니다. unity-apk-* 태그를 현재 main에 붙이면 BuildApk가 실행되며, job에서 tag SHA와 원격 main을 대조해 다른 코드면 중단합니다. 기존 수동 main BuildApk도 유지합니다. 공통 진입점에서 Windows PowerShell 실제 parser로 tools/*.ps1을 검사합니다. workflow는 actionlint1.7.7 exit0을 확인했습니다. 이번 신규 DPAPI/키 복원·Lint·APK 빌드는 아직 PC 실행 전이며 이전 장면 검사 성공과 구분합니다.

다음 필수 단계는 사용자가 별도 PowerShell 창에서 개인 설정 파일을 실행해 다운로드한 원본 백업을 선택하고 READY를 확인하는 것입니다. runner 창은 유지합니다. READY 이후 에이전트가 main APK 태그를 게시해 실제 빌드·APK 검증 보고서를 확인하고 설치 APK/집 폴더 PowerShell 명령을 전달합니다. 현재는 APK를 생성했다고 보고하지 않습니다. Activity 시제품0.1.0/com.s20plus.pixeltraffic.unityprototype이며 정식 native0.48 배경화면 이전과 기기 검증은 별도입니다.

## 2026-10-09 — GitHub → 사용자 PC Unity 검사·결과 공유 성공

[실제 성공 실행 37914772887](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/37914772887)은 main 53263e2ab1a7f148d94bf5425b99605f788839e7에서 completed/success입니다. Windows self-hosted runner의 checkout, 공통 Validate 및 보고서 업로드가 모두 success이며 APK 단계는 Validate 작업이므로 skipped입니다. Unity 실행은 사용자 PC에서 수행됐고 이 클라우드에 Editor를 설치한 결과가 아닙니다. runner 창과 PC가 켜져 있어야 후속 작업을 받습니다. 별도 GPT 플러그인은 필요하지 않습니다.

보고서 artifact11608622499를 다운로드해 ZIP CRC와 두 JSON 원본을 직접 확인했습니다. pipeline-result.json은 operation Validate/result PASS/wallpaper false, 09:59:31.6753513Z→10:00:55.4538723Z(약84초)입니다. scene-validation.json은 Editor6000.3.26f1, 실제 장면 geometry/material/lane bounds/straight motion PASS, 차량 폭2.130m/높이1.550m/길이4.300m/바퀴pivot4개를 기록합니다. 이 수치는 장면 검사 결과이며 APK/실제 기기 검사가 아닙니다. 보고서 ZIP710bytes, SHA2569a2fd21c60468b3cb227577df0ff22fab465002961a8c1b40caf5428e3e1253f, 보관 만료2026-10-16입니다. 원본 Editor 로그/키/비밀번호는 artifact에 없습니다.

PR #1(main e08955c) 게시 이후 workflow 표현식 오류 두 번과 PowerShell 정책 오류를 각각 수정했고 최종 workflow는 actionlint1.7.7 exit0 및 실제 PC 성공으로 확인했습니다. 이전 배포 fix1의 README 외 Unity/공통 pipeline20파일을 byte 비교해 그대로임을 확인했고 관련 없는 Android 검사/빌드는 반복하지 않았습니다. 실제 local index에는 staged 변경이 없고 누적 native 작업을 보존했습니다. 원격 main native 소스는 기존0.29.0이며 클라우드 누적0.48.0 native 변경은 이번 Unity 게시에 포함하지 않았습니다.

완료: Unity source/main 게시, PC 작업 배정, Unity batch Validate, GitHub 보고서 공유. 남음: 기존 키를 사용할 PC 서명 환경 준비, Activity APK 실제 빌드·서명/metadata 확인 및 S20+/S26 Ultra 설치, WallpaperService 통합과 시안 품질 향상. 이번에는 APK나 자동 화면 캡처를 만들지 않았습니다. 앞으로 main Unity 변경은 자동 Validate, BuildApk는 수동 선택입니다. Linux runner 전환 시 준비된 Editor/라이선스/Android 환경과 라벨을 지정하고 PIXEL_TRAFFIC_RUNNER_SHELL=pwsh로 변경합니다.

## 2026-10-09 — PC 작업 수신 확인 / PowerShell 프로세스 정책 수정

main baa6e1c64cc77536ee34d2851d04468ccddbb803의 실행 37914607324에서 실제 Windows PC가 unity job을 수신했고 checkout에 성공했습니다. 로그 작업 경로는 D:\Unity\actions-runner\_work\S20-PLUS\S20-PLUS입니다. 추가 runner 라벨이 일치해 작업이 배정된 사실도 확인했습니다. Unity 실행 전 GitHub 임시 .ps1 호출이 PSSecurityException/scripts disabled로 종료돼 보고서는 생성되지 않았습니다. PC 전체 ExecutionPolicy를 변경하지 않고 기본 shell 호출에 -NoProfile -NonInteractive -ExecutionPolicy Bypass -File "{0}"를 지정해 해당 프로세스에만 적용합니다. 사용자 이전 로컬 Validate도 같은 실행 옵션을 사용했습니다. Linux 이전은 PIXEL_TRAFFIC_RUNNER_SHELL=pwsh로 설정합니다.

이 실행은 Unity 검사 실패가 아니라 PowerShell 시작 실패입니다. 실제 Unity 결과는 후속 실행에서 확인합니다. APK/기기/WallpaperService는 미완료입니다.

## 2026-10-09 — workflow 표현식 검사 보완

첫 수정 실행 37914410060도 작업 시작 전 failure였습니다. actionlint1.7.7로 GitHub 표현식 의미 검사를 추가했고 steps.shell 자체에서 모든 context가 허용되지 않는 오류를 확인했습니다. 셸 선택을 허용된 jobs.unity.defaults.run.shell로 옮겨 PIXEL_TRAFFIC_RUNNER_SHELL 기본 powershell / Linux pwsh 설정을 유지했습니다. 변경 후 actionlint -shellcheck= -pyflakes= 검사 exit0을 확인했습니다. 첫 YAML 구조 검사와 달리 이번 검사는 GitHub context 허용 범위를 검사합니다. 실제 원격 실행은 이후 결과로 별도 기록합니다.

## 2026-10-09 — GitHub 게시 완료 / 첫 workflow 설정 수정

PR #1을 main에 squash 반영했습니다: e08955c27b111622203e6059284ab68b951d85ff. Unity 관련 29파일만 게시했고 기존 native 앱의 누적 로컬 변경과 실제 index/checkout을 보존했습니다. 첫 push 실행 37914163143은 failure이며 jobs가 0개로 PC/Unity 작업 이전에 종료됐습니다. GitHub workflow steps.shell에 허용되지 않는 runner.os 참조를 발견해 vars.PIXEL_TRAFFIC_RUNNER_SHELL 기본 powershell로 변경합니다. Linux 이전 시 이 변수에 pwsh를 설정합니다. 첫 정적 YAML 구조 검사만으로 GitHub 표현식 의미 검증을 대체하지 못한 점을 기록합니다.

첫 실패는 Unity 장면 검사 실패로 기록하지 않습니다. 실제 remote 재검사 및 artifact 결과는 이후 확인하며 Unity APK/기기/WallpaperService는 미완료입니다. 키/암호를 게시하지 않았습니다.

## 2026-10-09 — Unity PC 자동 검사 연결

사용자 Windows PC에서 수정 실행기의 `Unity Validate completed` 및 GitHub runner 2.337.0의 `Connected to GitHub` / `Listening for Jobs`를 화면으로 확인했습니다. Unity 6000.3.26f1 프로젝트와 공통 PowerShell pipeline을 `pixel-traffic-unity/`에 추가하고 main의 Unity 관련 변경을 자동 Validate에 연결합니다. main 수동 Validate/BuildApk도 지원하며 PR 코드는 자동 실행하지 않습니다. runner 라벨은 self-hosted/Windows/X64/pixel-traffic-unity입니다.

이번 게시 범위는 Unity 프로젝트·workflow·관련 문서입니다. 기존 native Android 앱 변경은 포함하지 않습니다. workflow의 main 제한·Validate 기본값·보고서 허용 목록 및 source 제외 항목을 정적으로 확인했습니다. 실제 첫 GitHub Actions 작업은 main 반영 후 확인해야 합니다. Unity APK·기기 실행·WallpaperService 연결·자동 캡처는 아직 검증하지 않았습니다. 기존 인증서 검증 후 APK를 공유하도록 구성했고 키/암호/원본 로그는 게시하지 않습니다.

[PC 실행과 결과 확인](UNITY_AUTOMATION.md), [전환 과정](UNITY_MIGRATION.md). GitHub source 게시, main 반영, Actions 성공은 각각 실제 결과를 따로 기록합니다.

최종 갱신: 2026-10-07

## 사용자 공개키 암호화 전달 준비 완료 — 2026-10-07

- 사용자가 Windows 준비 스크립트 실행 화면과 정확한 공개 XML을 제공했습니다. 실제 2048비트 공개키로 개인 서명 ZIP을 암호화했습니다. 원본 checksum/ZIP 검사는 정상이며 암호문 3,584bytes입니다. 개인키·원본 ZIP 내용은 Git/채팅에 출력하지 않습니다.
- 공통 `pixel-traffic/tools/Restore-BackupTransfer.ps1`과 사용자용 암호문 capsule을 준비했습니다. 동일 PC/계정의 DPAPI 키로 복호화하고 ZIP 길이/SHA256 확인 후 저장하며 다른 파일은 덮어쓰지 않습니다. [절차](BACKUP_TRANSFER.md).
- **사용자 노트북에서 ZIP 복원·검증 결과 확인 대기**입니다. 실제 복호화는 클라우드에 사용자 개인키가 없어 실행할 수 없습니다. 앱 소스/버전/자산은 바꾸지 않았습니다.

## 아티팩트 실패 대체 전달 준비 — 2026-10-07

- 사용자 보고: 새 sandbox 링크도 두 파일 모두 ‘아티팩트를 다운로드할 수 없다’고 표시됩니다. 3KB 서명 ZIP도 실패하므로 용량 단독 원인 가능성은 낮습니다. 파일 자체 검사는 정상이며 클라이언트/서비스의 정확한 원인은 미확인입니다.
- GitHub main ZIP의 HEAD 요청 HTTP 200 확인. 이 경로는 소스/그림/문서/검사를 포함하며 기존 전체 백업의 APK/Git 이력/개인 서명키는 제외합니다.
- [BACKUP_TRANSFER](BACKUP_TRANSFER.md)에 별도 개인 서명 ZIP의 암호화 텍스트 전달 절차를 준비했습니다. 실제 도우미의 임시 RSA fixture 암호화/복호화·hash 확인은 PASS이며 Windows/DPAPI와 사용자 전달은 아직 미확인입니다. **사용자 노트북 공개키 수신 대기**입니다. 개인키·원본 ZIP 내용을 채팅이나 Git에 노출하지 않습니다.

## 백업 다운로드 재전달 — 2026-10-07

- 사용자가 두 백업 다운로드 실패를 보고했습니다. 원본 ZIP과 새 다운로드 복사본의 hash/압축 검사는 정상이며 `sandbox:` 다운로드 링크로 다시 전달합니다. 클라이언트 다운로드 성공은 아직 확인되지 않았습니다.
- 프로젝트 ZIP은 기존 `903cb5e` 인수인계 스냅샷입니다. 아래 GitHub 소스/태그와 서명키 보존 상태는 유지합니다. 앱 기능/버전 변경은 없습니다.

## 새 채팅 인수인계 완료 — 2026-10-07

- 사용자 확인: **0.29.0이 잘 적용됐습니다.** 다음 기능 패치는 아직 선택하지 않았습니다. 앱 소스·버전·PNG를 바꾸지 않고 인수인계와 복원 절차를 보강했습니다.
- 소스·자산·검사·개발 기록을 GitHub main에 보존했고 원격 SHA를 확인했습니다. 소스 체크포인트는 `82c5cbdcdaf17d408565b26addf01411792a3f73`, 태그는 `pixel-traffic-v0.29.0`입니다. 이후 main의 커밋은 인수인계 완료 문서를 갱신합니다. 이전 항목의 ‘커밋/푸시 미실행’은 당시 기록입니다.
- 새 폴더 clone에서 portable 빌드 도우미로 Android assembleDebug/lintDebug **19초 성공, Lint 이슈 없음**. APK SHA256이 기존 전달 0.29.0과 완전히 같았고 기존 v2 인증서·버전·PNG 13개도 일치했습니다. 기존 도구와 캐시를 재사용했으며 새 머신 설치·새 클라우드 태스크·실기기 성능 검사를 수행한 것은 아닙니다.
- 체크포인트 Git bundle의 clone과 `git fsck --full`이 통과했습니다. 최종 프로젝트 백업은 `S20-PLUS-0.29.0-handoff.zip`이며 Git bundle·최신 APK·야간 합성·복원 안내·MANIFEST/SHA256SUMS를 포함합니다. 최종 커밋과 파일 checksum은 ZIP의 manifest에서 확인합니다.
- 기존 업데이트 서명키는 별도 `S20-PLUS-0.29.0-signing-backup.zip`으로 보관합니다. 키 복원·반복 복원·다른 키 덮어쓰기 거부와 키 없는 빌드 중지 검사가 통과했습니다. 새 환경에 키가 없다면 이 백업을 첨부해 복원해야 합니다. 개인키는 Git/프로젝트 백업에 없습니다.
- [HANDOFF](HANDOFF.md)에 새 채팅 문구·현재 기능·결정·미완료를, [BUILD_RESTORE](BUILD_RESTORE.md)에 Git/bundle·도구·서명 복원을 정리했습니다. 범위별 읽기/검사와 작업 기록 규칙은 AGENTS/PATCH_GUIDE를 유지합니다.
- 클라우드 설정 **start_skill 저장 성공**. 시작·서명·검사 절차만 갱신했으며 저장소 목록/설치 스크립트/네트워크/비밀 바인딩은 바꾸지 않았습니다. 초안 활성화는 환경 설정에서 검토·저장 후 Publish가 필요합니다. 게시/새 태스크 복원은 미실행입니다.
- 보존 한계: SDK/JDK/캐시 전체와 과거 채팅 첨부 원본은 백업하지 않았습니다. 원본 시안·기기 화면·레드존 입력/원본 소스·포켓프렌드 원본의 확보 여부는 HANDOFF에 명시했습니다. 보행자/횡단은 미구현 후보이며 정확한 실기기 FPS·전력/발열 측정은 대기 중입니다.

## 새 채팅 인수인계 준비 — 착수 당시 기록 (2026-10-07)

- 사용자가0.29.0을잘적용했다고확인했고,새채팅이동전완전보존을요청했습니다. 소스/문서/PNG의최초커밋·원격보존과별도복원백업을진행합니다. 기존소스버전은0.29.0으로유지합니다.
- [HANDOFF](HANDOFF.md)에새채팅문구·현재기능/미완료·결정을, [BUILD_RESTORE](BUILD_RESTORE.md)에Git/bundle·서명키/도구복원을정리했습니다. 서명키는Git에넣지않고별도백업으로보존합니다.
- 현재준비/검증단계이며커밋·푸시·새체크아웃빌드/최종bundle검증결과는완료시최신항목에추가합니다. 이전버전의‘커밋/푸시미실행’은당시상태기록입니다.

## 최신 전달 — 0.29.0

- 사용자0.28실기기스크린샷의 횡단보도 앞 흰 승용차 크기 차이와 반대 방향 조명 누락을 수정했습니다. 폭을 각 차선 폭에서 계산하던 `VehicleLayout`을 차종/깊이 공통 고정 프로필로 바꿨습니다. 동일 차종·같은 깊이는 모든 차선에서 같은 폭, 같은 방향은 같은 길이입니다. 정차쌍의 실제 폭/길이와 y810정차를 세 팔레트에서 확인했습니다. 차체 전체 차선/간격·원근 크기 연속성은 유지합니다.
- 반대 방향 차는 기존 후미등·붉은 반사에 더해 실제 앞쪽 끝에서 지평선 방향으로 전조등/안개등 부채꼴을 비춥니다. 차종CCT·기존텍스처/도로Path clip을 재사용하고 후미등의 작은 코어/halo를 보강했습니다. 새 설정 없이 전용 도심에 적용합니다. PNG13개·원본비율·주행/신호 알고리즘·설정키·기본4맵은 유지하며 크기 변경에 따른 신호 정차·간격은 재검증했습니다.
- 검사PASS: 단조크기114,696/전체차체28,704, 방향별/보수적·실제3팔레트573,480프로필의 차선 간 폭·같은방향 길이/전체차체경계·headway, 실제672픽셀/99합성. 조명10,020표본·실제12램프/세팔레트/정차쌍·양방향 비춤·색/도로clip·반사/캐시, 기존효과/시간대/노면/날씨·해제.48주행궤적3,110,400표본/302,789빨강정차/2,087루프·최대감속70과 실제신호/브레이크·정차물보라도PASS입니다.
- Android offline 빌드/Lint20초성공·이슈없음. APK code29/version0.29.0/min29/target35·기존v2서명, PNG13개가소스/0.28과byte동일. `/workspace/artifacts/pixel-traffic-0.29.0-debug.apk`37,460,214bytes/SHA256 `a04bfa9d27b9687f6af6feb41a3f944d5f8cc10355ceae73554ccc11688d978a`. 야간20초 신호대기 합성 `pixel-traffic-0.29.0-night-traffic.png`준비·육안확인했습니다. [설치](../pixel-traffic/README.md)/[설계](CINEMATIC_RENDERER.md)/[패치 안내](PATCH_GUIDE.md) 갱신. 파일저장완료/커밋·푸시미실행입니다.
- **실기기 대기**: S20+/S26 Ultra의 흰승용차 정차쌍/전체차종 차선·크기·화면밖 연속성, 반대방향 전방비춤·후미등/제동, 날씨·구도·숨김/복귀. 실제HW Canvas/FPS/전력·발열은미측정입니다. 기본맵·전체설정검사는무변경으로반복하지않았습니다. 후속보행자/횡단은미승인후보입니다.

## 이전 전달 — 0.28.0

- 사용자 요청의 살짝 부채꼴 헤드라이트 노면 비춤을 구현·APK 준비했습니다. 좁은 시작점에서 넓어지는 밝은 면, 부드러운 측면·둥근 끝/거리 fade를 적용했습니다. 전조등 한쪽 투사폭은 차체폭0.88배→1.14배이며 기존 길이·차종 색온도/안개등·시간대/날씨 강도와 도로 Path clip을 사용합니다. 전용 도심에 추가 설정 없이 적용됩니다.
- 변경 범위: 앱 `LightTextures.java`, `CinematicScene.java`, `app/build.gradle`; 검사 `CinematicLightingChecks.java`와 관련 문서입니다. 기존64×192 전조등 텍스처·그리기 호출을 재사용하고 PNG13개/차체·차선·주행/신호·설정키·기본4맵은 유지합니다. [설치](../pixel-traffic/README.md)/[설계](CINEMATIC_RENDERER.md)/[패치 안내](PATCH_GUIDE.md) 갱신.
- 검사PASS: 부채꼴 폭 증가/밝은 측면/둥근 끝·대칭·완전 fade, 기존CCT/강도10,020표본·실제12램프쌍/세 팔레트/도로 경계·色/반사·해제, 동일 스포츠차의 안개등 투명 대조군으로 실제 추가 비춤 확인. 기존효과12,105표본/100착지·실제 신호/정차·브레이크, Scene 차체·672픽셀과99합성도PASS입니다. 기존 검사 class를 재사용해 변경 클래스만 재컴파일했고 무변경 독립 기하/48주행 궤적/기본맵·설정 전체는 반복하지 않았습니다.
- Android offline 빌드/Lint16초성공·이슈없음. APK code28/version0.28.0/min29/target35·기존v2서명, PNG13개가 소스/0.27.0과byte동일. `/workspace/artifacts/pixel-traffic-0.28.0-debug.apk` 37,459,782bytes, SHA256 `57af56a59c9c4b0f97330b7956c1d54347bf77fdc6fa20ae02df4985e711161e`. 야간 합성 `pixel-traffic-0.28.0-night-fan.png` 준비·육안확인했습니다. 파일 저장 완료, 커밋/푸시 미실행.
- **실기기 대기**: S20+/S26 Ultra의 밤/비·차종별 부채꼴과 낮·눈·안개/구도·신호 대기/숨김·복귀. 실제 HW Canvas/FPS·메모리/전력은 미측정입니다. 후속 보행자/횡단은 후보이며 미승인·미구현입니다.

## 이전 전달 — 0.27.0

- 사용자가 라이팅 우선 패치와 차종별 색온도를 승인했고 구현·APK 준비를 완료했습니다. 승용/SUV/택시6500K LED, 스포츠 쿠페6500K전조등+6500K안개등, 버스/트럭4500K전조등+3000K안개등입니다. 실제 램프/범퍼 하단 그룹을 분리한 캐시 색 마스크, 길고 좁은 전조등·짧고 넓은 안개등 노면 비춤, 같은 색의 젖은 반사/잔물결을 적용했습니다. 낮·노을·밤/눈·안개에 강도·번짐을 연결하고 가로등3000K 노면 비춤·실제 밝은 창문/간판 최대8곳 발광을 보강했습니다. 추가 설정 없이 전용 도심에 적용됩니다.
- 차체·차선·주행/신호/기존 설정과 기본4맵, PNG13개는 유지합니다. 색온도는 CIE→화면 sRGB 근사이며 물리적 스펙트럼/폰색온도 측정이나 광선추적이 아닙니다. 공통 조명 기준은 `VehicleLighting`, 작은 텍스처는 `LightTextures`, 실제 램프 마스크는 `VehicleLights`, 창문 위치는 `CityLightAnchors`입니다. [설계](CINEMATIC_RENDERER.md)/[패치 안내](PATCH_GUIDE.md)/[설치](../pixel-traffic/README.md) 갱신.
- 검사PASS: 색온도와 깊이/날씨10,020표본, 독립 전조등/안개등 fixture·실제12종 램프/세 팔레트/alpha·해제, 노면 경계/LED·따뜻한 빔/추가 안개등 기여/후면 빔 없음/젖은 잔물결과 창문 위치. 기존 기하·차체573,480프로필/672픽셀·효과·시간대/날씨·신호/48궤적3,110,400표본·99합성도PASS. 창문 보강 뒤 영향받는 시간대/날씨·신호/최종 합성만 재실행했고 무변경 기본맵/공유모델·설정 전체는 반복하지 않았습니다.
- Android 빌드/Lint24초성공·이슈없음. APK code27/version0.27.0/min29/target35·기존v2서명, PNG13개가 소스와0.26.0에 byte일치. `/workspace/artifacts/pixel-traffic-0.27.0-debug.apk` 37,459,402bytes, SHA256 `a72f3002f3d897d110a00c2e6878f4170678b470163e9ef27a62f39f38e987db`. 노을/야간 합성 `pixel-traffic-0.27.0-{sunset,night}-lighting.png` 준비·육안확인. 파일저장완료, 커밋/푸시 미실행.
- **실기기 대기**: S20+/S26 Ultra의 밤/강한비 트럭4500/3000K와 스포츠6500K 두 램프·반사, 다른 팔레트/낮·눈·안개/신호대기·숨김/복귀. 실제 HW Canvas/FPS/메모리/전력·발열은 미측정입니다. 추가 텍스처 픽셀417,792bytes(408KiB)와 실제 앞차체6종 크기의 발광 마스크·대체 마스크가 필요하며 엔진/미리보기 및 GPU복사 비용은 별도입니다. 후속 후보는 보행자/횡단·같은 조명 기준 연결이며 미승인·미구현입니다.

## 이전 전달 — 0.26.0

- 2026-10-07 순서 상담: 사용자가 보행자 전에 라이팅을 개선할지 질문했습니다. 추천 순서는 **공통 광원·노면 반사 개선 → 보행자/횡단 → 보행자 조명 미세 조정**입니다. 다음 버전 후보를 라이팅 우선으로 제안하며 보행자는 후속으로 유지합니다. 현재 상담/제안만이며 라이팅·보행자 구현 승인이나 새 APK는 없습니다.
- 2026-10-07 후속 사용자 확인: 신호등 시스템이 아주 잘 적용됐다고 보고했습니다. 새 기기별 성능/전력 수치는 없으며 상세 실기기 대기는 유지합니다. 다음 추천은0.27.0 전용 도심 보행자/신호 대기·안전한 횡단과 비 우산·눈 겨울 옷·시간대 조명입니다. 차량 진입 확정/전체 차체의 횡단 구역 비움과 보행 완료까지 정차를 연결해야 하며 현재 제안만, 구현 승인/새 APK는 없습니다.
- 사용자가0.25.0 정상 적용 뒤 추천 교통 신호/감속·정차·순차 출발을 승인했고 구현·APK 전달 준비를 완료했습니다. 전용 도심의 기존 횡단보도 보도 양쪽에 픽셀 신호등, 초록14/노랑3/빨강9초 활성시간 순환과 안전한 노랑진입 통과를 추가합니다. 실제 차체 앞끝 정차/앞차 간격·속도/0.25초 순차 출발이며 기존 차선·크기·PNG13개/기본맵·공유TrafficModel은 유지합니다.
- 실제 감속/정차 상태의 뒤쪽 브레이크등·젖은 붉은 반사를 강화하고 정차 물보라는0으로 줄입니다. 비/눈/풍경 이벤트는 계속 진행합니다. `차량 · 종류와 색상 → 교통 신호 · 전용 도심` 기본켬이며 off이면 신호가 사라지고 대기차가 가속해 자유주행으로 돌아갑니다. UI/엔진/미리보기/진단·프리셋v1에연결, 구버전없는키true·엄격Boolean검사, 빠른성능설정은선택보존. 밀도변경은배열/초록신호 재시작, 날씨/시간대/구도/밝기는위치·신호시계 유지. [설치](../pixel-traffic/README.md)/[설계](CINEMATIC_RENDERER.md)/[패치 안내](PATCH_GUIDE.md) 갱신.
- 최종 검사PASS:48밀도/차종/속도90초3,110,400차량표본, 정차305,285표본/루프2,082회/최대감속70모델단위/s², 물리차체·최소원형간격/가감속·순차출발,15/30/60Hz·dt/토글·노랑가까움/먼접근. 실제팔레트3종신호색/보도위치·정차앞끝/브레이크·반사/헤드라이트불변·정차물보라·설정/그리기연속성, 기존차체573,480프로필/672픽셀/효과·날씨캐시99합성과관련설정정책도PASS. 오류/재개범위는 WORK_LOG에기록했고 통과한무변경기하·자산/기본맵 검사를중복하지않았습니다.
- Android빌드/Lint최종16초성공(초기19초), 이슈없음. APK code26/version0.26.0·기존v2서명/13PNG소스일치. `/workspace/artifacts/pixel-traffic-0.26.0-debug.apk` 37,445,298bytes, SHA256 `cce280103f9e07656b32ae9268adc59d123f66c699788fec127c3accc54d3822`. 최종합성 `pixel-traffic-0.26.0-{red-sunset,green-night}.png` 준비. 파일저장완료, 커밋/푸시 미실행.
- **실기기 대기**: S20+/S26 Ultra에서 양방향 버스·트럭앞끝/24대큐/정차·재출발, 신호off/on, 설정/프리셋JSON·복원·잘못된bool, 날씨/시간대·색상/속도 전환과 숨김/복귀·기존맵. Android SharedPreferences/JSON 전체실행·실제GPU표시FPS/메모리/전력·발열은미측정입니다. 교차로/보행자충돌·실제기상/법규모델은미구현이며 다음작업은사용자적용확인후선택합니다.

## 이전 전달 — 0.25.0

- 2026-10-07 후속 사용자 확인: 0.25.0이 매우 잘 적용됐다고 보고했습니다. 기기별 상세 조합/새 성능 측정값은 없으며 관련 실기기·전력 검증 대기는 유지합니다. 다음 추천은 0.26.0 전용 도심 횡단보도 신호·부드러운 감속/정차·순차 출발과 실제 감속에 따른 브레이크등입니다. 현재 제안이며 구현 승인은 아직 없습니다.
- 사용자가 눈·안개 전용 도심/세시간대·전환/필요한 배경만 캐시하는 개선을 승인했고 구현·APK 전달 준비를 완료했습니다. 눈은 지붕·가로수·보도 적설과 제설된 차선, 앞뒤 눈송이와 야간 따뜻한 조명을 추가합니다. 안개는 먼 산·도시·강을 흐리고 차량/가로등 halo·먼 차체/열차 alpha를 연결합니다. 첫 날씨 즉시 표시·이후4초 활성 전환/재선택과 낮/노을/밤을 지원합니다.
- 새841×1870 눈/안개 아트6장, CinematicArtwork 지연 캐시를 추가했습니다. 선택과 전환에 필요한 그림만 configure/setTheme에서 로드하고 update/draw에서는 디코딩하지 않습니다. 전환 후 불필요한 그림을 해제하며 노을 눈/안개2장, 낮/밤3장입니다. 빠른 시간대/날씨 재선택 검사 피크12장(약72MiB 픽셀), 정착 후2장 회수를 관측했습니다. 상시6장 유지 대신 선택별 캐시이며 실제 GPU/프로세스/여러엔진 메모리는 미측정입니다.
- 기존 차체/차선·차량PNG/주행·맑음/비/설정키·프리셋/기본4맵은 유지합니다. UI와 최근 노면 진단의 아트 대체·캐시 장수를 갱신했습니다. 손상/누락·허용 크기 밖/로드 메모리 부족은 기존 마른/젖은 아트로 대체하고 해제 시 소유 자산을 recycle합니다. [설치](../pixel-traffic/README.md)/[설계](CINEMATIC_RENDERER.md)/[패치 안내](PATCH_GUIDE.md) 갱신.
- 관련 검사PASS:12배경 중앙선96/횡단보도72표본,12색 시간대×날씨 oracle·초기/4초·재선택, 실제240tick/24대 위치/자세·cache퇴거/재사용/실패대체/해제/프레임 디코딩 없음/눈 레이어·소멸. 기존 기하·차체573,480프로필자세/672픽셀·효과12,105표본과99합성, 공유ThemeBlend/눈 ExpansionChecks도 PASS. 최초 중앙선 검사의 두 줄 합침 오류를 픽셀 프로브로 고치고 기존3.5px 오차는 유지했습니다. 안개 이벤트 표현 수정 후 영향받은 합성/빌드만 재검증했고 무관한 기본맵 전체 검사 반복은 하지 않았습니다.
- Android 빌드/Lint 최종 성공20초/이슈없음(초기22초, 최종 열차 alpha 반영 증분 빌드). APK code25/version0.25.0·기존 v2서명·PNG13개 소스 byte 일치. `/workspace/artifacts/pixel-traffic-0.25.0-debug.apk` 37,581,054bytes, SHA256 `b75aed93ec26298e9615568bc465d9824c0909718b59bf71348169aaf573ead8`. 같은폴더 `pixel-traffic-0.25.0-{day,sunset,night}-{snow,fog}.png` 최종 합성6장 준비·육안 확인. 파일 저장 완료, 커밋/푸시 미실행.
- **실기기 대기**: S20+/S26 Ultra의 눈/안개×3시간대, 맑음↔눈↔안개와 시간대 빠른 재선택·차선/크기, 설정/프리셋, 숨김·복귀/자동시간과 캐시·첫 자산로드 지연. 실제 GPU/표시FPS/전력·발열/저메모리 환경 미측정입니다. 다음 작업은 사용자 적용 확인 후 선택하며 실제 기상 동기화/물리적 적설/다른 전용 맵·교통 신호는 미구현입니다.

## 이전 전달 — 0.24.0

- 후속 사용자 확인: 0.24.0이 잘 적용됐다고 보고했습니다. 기기별 조합/새 성능 수치는 없으며 상세 기기·전력 검증 대기는 유지합니다. 다음 추천은 0.25.0 눈·안개 전용 도심 배경과 기존 시간대/날씨 전환 연결입니다. 현재는 제안이며 구현 승인은 아직 없습니다.
- 사용자가 날씨별 노면 패치를 승인했고 구현·전달 파일 준비를 완료했습니다. 전용 도심 낮/노을/밤에 마른 도로3장을 추가하고 맑음0/약한비0.68/강한비1의 젖음을 적용합니다. 첫 날씨 즉시 표시, 이후4초 활성시간 전환·재선택과 시간대 동시 전환을 지원합니다. 비를 끄면 빗줄기는 즉시 멈추고 반사·물보라는 마르는 동안 약해집니다. 눈은 마른 기본 노면+기존 눈, 안개는0.35 습윤+기존 안개이며 둘 다 물보라는 없습니다.
- 차체/차선/주행/차량PNG·설정키/프리셋·기본4맵은 유지합니다. UI와 최근 진단에 노면 상태/젖음%를 추가했습니다. [설치와 사용법](../pixel-traffic/README.md)/[설계](CINEMATIC_RENDERER.md)/[패치 안내](PATCH_GUIDE.md) 갱신. 캐시6장 해제/마른 파일 실패 시 젖은 아트 대체를 검증했습니다. 낮 마른 생성본842×1869는 원본841×1870와 각 축1픽셀 차이를 허용해 같은 구도로 표시하며 독립 표본을 확인했습니다.
- 관련 검사PASS: 중앙선48/횡단보도36표본, 독립6색 동시 혼합/초기선택·4초 건조·재선택/원래 하늘 보존, 실제180tick/24대 차량 위치·자세/캐시·대체·해제. 기존 기하·차체573,480프로필 자세/672픽셀·효과12,105표본 및 세시간대99합성도 통과. 실패를 수정한 뒤 영향받은 단계만 재개했고 무관한 기본맵·전체모델 반복은 하지 않았습니다.
- Android 빌드/Lint 성공22초/이슈없음. APK code24/version0.24.0·기존 서명·7PNG 소스일치. 파일 `/workspace/artifacts/pixel-traffic-0.24.0-debug.apk` (19,780,969bytes), SHA256 `a21a8b4aa7a321862f33da22c728bf0a68e54263373a3e7d481c672c0ff4ee21`. 데스크톱 최종합성 `pixel-traffic-0.24.0-{day-dry,sunset-dry,night-dry,night-wet}.png` 준비. 파일 저장 완료, 커밋/푸시 미실행.
- 추가 마른 배경 픽셀18,876,152bytes(약18MiB), 배경 총 약36MiB는 계산치입니다. 실제 GPU/여러엔진 메모리·표시FPS·전력/발열 미측정. **실기기 대기**: S20+/S26 Ultra 날씨·시간대 동시/연속 변경, 완전 마름과 반사/물보라, 차선·크기, 설정 유지, 숨김/복귀·추가 부하. 이후 후보는 신호/정차·교통 이벤트 또는 다른 전용 맵 확장이며 미승인·미구현입니다.

## 이전 전달 — 0.23.0

- 후속 사용자 확인: 0.23.0이 매우 잘 적용됐다고 보고했습니다. 기기별 세부 조합/새 성능 수치는 없으며 상세 검증·전력 측정 대기는 유지합니다. 다음 추천은0.24.0 전용 도심의 날씨별 마른/젖은 노면과 반사·물보라/전환 연결입니다(제안만, 미승인·미구현).
- 동일크기841×1870 전용 낮/밤 아트 추가, 기존노을과 함께 수동/자동 시간대를 지원합니다. 처음선택은즉시표시/이후4초활성전환·재선택, 시간대별차량/가로등·반사조명. 차체배치/차선·주행/원본노을·차량PNG/설정·프리셋·서명은 유지합니다.
- 기존 엔진·미리보기 자동 시간대 생략을 제거하고UI/진단을 갱신했습니다. 전용도심을 켠채 장면 시간대를 선택하거나 자동전환을 켭니다. 기존전용노을추천은 여전히노을입니다. 노면은 모든시간대에젖은아트입니다.
- 검사PASS: 원본중앙선24/횡단보도18표본, 실제색혼합/초기선택·4초·재선택/180tick·24대 위치와자세보존/캐시·대체·해제/자동시각경계. 기존기하·차체573,480프로필자세/672픽셀·효과와 세시간대99합성도통과. 초기검사 기준점·명암측정오류를 수정하고 해당검사부터 재개했으며 무관한기본4맵·전체모델 검사 반복은 하지않았습니다.
- Android 빌드/Lint 성공19초/이슈없음, APK서명·code23/version0.23.0·4PNG소스일치. APK `/workspace/artifacts/pixel-traffic-0.23.0-debug.apk`와 `pixel-traffic-0.23.0-{day,sunset,night}.png` 전달. [설치](../pixel-traffic/README.md)/[설계](CINEMATIC_RENDERER.md)/[패치 안내](PATCH_GUIDE.md) 갱신. 파일저장 완료, 커밋/푸시 미실행.
- 낮/밤자산2장 캐시추가 약12MiB픽셀데이터(계산치), 실제GPU/프로세스메모리·전환부하 미측정. 누락/손상·크기불일치/선택자산로드실패는 전용노을로 대체·진단표시하고 모드해제때세장recycle합니다.
- **실기기 대기**: S20+/S26 Ultra의 수동3시간대/자동현지시각·재선택/차선·크기·저장값/숨김·복귀와추가부하. 실제표시FPS/전력·발열 미측정. 후속후보 마른노면/다른전용맵 확장은미실행입니다.

## 이전 전달 — 0.22.0

- 후속 사용자 확인: 0.22.0을 좋게 평가하고 다음 패치를 질문했습니다. 기기별 세부 조합/새 성능 수치는 없으며 아래 상세 검증 대기·전력 미측정 상태는 유지합니다. 다음 제안은0.23.0 전용 도심 낮·밤 아트와 기존 시간대/자동 전환 연결입니다(제안만, 미승인·미구현).
- 전용 도심에서 실제 램프 위치의 빛·거리/날씨별 노면 반사, 가로등 통과 차체 명암, 원근 비의 앞뒤 레이어와 바퀴 물보라를 구현했습니다. 기존 전용 도심 설정으로 업데이트하면 반영됩니다.0.21.0 차선/크기, 주행 모델/원본PNG/설정·서명 유지, 새 권한·설정키 없음.
- 마스크·램프는 로드/팔레트 변경 때만 캐시하고 소유자가 해제합니다. 효과 시계는 유효 활성update에만 진행하며 비가 없거나 해제되면 멈춥니다. draw만으로 시간은 흐르지 않습니다. 추가 마스크 메모리/합성 비용은 실제기기 측정 전입니다.
- 전용 최종 검사 PASS: 신규12,105 깊이/날씨 샘플/100착지 깊이, 소스 램프·alpha 마스크/실제12아틀라스·대체팔레트/젖음·마름/실제 비 레이어/물보라·속도·시간·해제. 기존크기114,696샘플/전체차체28,704자세/5프로필573,480자세/실제Scene672픽셀/33합성도 통과. 기본4맵전체와 무관한 모델/설정 검사는 반복하지 않았습니다.
- Android assembleDebug/lintDebug 성공22초, Lint 이슈 없음. APK 서명·code22/version0.22.0·원본PNG일치 확인. APK `/workspace/artifacts/pixel-traffic-0.22.0-debug.apk`, 최종 합성 `/workspace/artifacts/pixel-traffic-0.22.0-cinematic.png`. [설치](../pixel-traffic/README.md), [설계](CINEMATIC_RENDERER.md), [패치 안내](PATCH_GUIDE.md) 갱신. 파일 저장 완료, 커밋/푸시 미실행.
- **실기기 대기**: S20+/S26 Ultra의 맑음/약한비/강한비, 차체 조명·앞뒤램프·반사/물보라, 기존 차선·크기 유지와 숨김/복귀. 실제 표시FPS·배터리·발열 미측정. 다음 후보는 같은 차선 구도의 전용 낮·밤 아트 확장입니다(미실행).

## 이전 전달 — 0.21.0

- 후속 사용자 확인: 0.21.0 적용 후 크기 요동이 잘 고쳐졌다고 보고했습니다. 이번 메시지의 기기별 구분/새 성능 수치는 없으며, 아래 세부 조합 검증 대기와 전력 미측정 상태는 유지합니다. 다음 업데이트 후보는 차량 조명·반사와 비 효과 보강이며 아직 구현 승인/착수하지 않았습니다.
- 이동 중 차체 크기 요동을 수정했습니다. 위치별 반복fit을 깊이별 선형 크기곡선/고정 차선 여유로 교체하고 실제 차체 길이 전체의 할선으로 기울기를 계산합니다. 차체·그림자·반사가 같은 자세를 사용합니다. 기존 전용 도심 설정으로 업데이트하며 PNG/주행 모델/설정/서명은 유지합니다.
- 최종 전용 검사 통과: 크기114,696샘플, 기존 전체차체28,704자세, 합성/보수적/실제Scene3팔레트 총573,480자세, 실제Scene672픽셀 자세/팔레트왕복, 기존33합성·아틀라스/그래픽/오류·해제. 최종 Android assembleDebug/lintDebug 성공(Lint 이슈 없음), code21/version0.21.0·기존서명·APK PNG 일치 확인.
- [패치 작업 안내](PATCH_GUIDE.md), AGENTS, `pixel-traffic/tools/check-patch.sh`에 최신/관련 문서·범위별 검사·도구/자산 재사용·검사 중복 방지 규칙을 추가했습니다. 실제 크레딧 절감량은 미측정입니다.
- 전달 APK `/workspace/artifacts/pixel-traffic-0.21.0-debug.apk`, 최종 합성 `/workspace/artifacts/pixel-traffic-0.21.0-cinematic.png`. [설치 안내](../pixel-traffic/README.md) 및 [렌더러 설계](CINEMATIC_RENDERER.md) 갱신. 파일 저장 완료, 커밋/푸시 미실행.
- **실기기 대기**: S20+/S26 Ultra에서 횡단보도 전후 긴 차종 크기/차선·방향·팔레트 전환 확인. 실제 표시FPS·배터리·발열 미측정. 무변경 기본맵 전체 합성과 모델/설정 전체 검사는 이번에 반복하지 않았습니다.

## 이전 전달 — 0.20.0

- 후속 보고: 사용자가0.20.0 적용을 확인했으나 이동 중 차체 크기 요동/횡단보도 버스 축소를 발견했습니다. 실제Geometry에서 lane0 버스폭groundY800→840이56.53→45.81px로 감소함을 재현했습니다. 전체차체fit의 위치별 축소가 원인이며 크기 단조/변화율 검사를 추가해야 합니다. 현재는 사용자의 작업 전 설명 요청에 따라 분석·계획만 기록했고 앱 코드/새APK는 변경하지 않았습니다.

- 사용자0.19.0 정상 적용 확인과 스크린샷6535.jpg에서 작은 차체·차선 어긋남 지적. 새 버전에서는 실제 다섯 차선 경계의 독립 보간, 불균등 차량 행 분리, 차선 폭 기준 차체 크기, 접지/원근·반사·그림자를 수정했습니다. PNG·기존 설정·서명·TrafficModel 고리는 유지합니다.
- 기존 ‘전용 아트 · 비 오는 노을 도심’ 설정을 켜둔 채 업데이트하면 반영됩니다. 노을 고정/다른 맵·팔레트 제한은 이전과 같습니다.
- Android assembleDebug/lintDebug 성공(Lint 이슈 없음), APK 서명/code20/version0.20.0·원본PNG 일치 검사 통과. 강화된 기하28,704자세, 실제 아틀라스12종/픽셀120자세/합성33조합/자산 오류·해제 검사 PASS. 여러 활성 시간과 휴대폰 비율 합성에서 차폭·차선 유지 확인. 기존0.19 검사가 실측 불일치/행 혼입을 놓쳤음을 재현해 검사 보강했습니다.
- 전달 APK `/workspace/artifacts/pixel-traffic-0.20.0-debug.apk`, 시안 `/workspace/artifacts/pixel-traffic-0.20.0-cinematic.png`. [렌더러 설계](CINEMATIC_RENDERER.md), [설치/사용법](../pixel-traffic/README.md) 갱신. 파일 저장 완료, 커밋/원격 푸시 미실행.
- 실기기0.20.0 차선 유지·크기/전환·성능 확인 대기. 실제 표시FPS·배터리·발열은 미측정이며 장시간 전력 작업은 추후 진행합니다.

## 이전 전달 — 0.19.0

- 사용자가 전용 아트/GPU 전환과 필요한 추가 배터리 사용을 허용하여, 노을 도심부터 고밀도 전용 배경·투명 차량 아틀라스와 GPU Canvas 직접 합성을 구현했습니다. 전용 배경841×1870, 차량1448×1086, 논리 구도540×1200. 선택형24대/날씨/구도/밝기/교량 전철 연결.
- **추천 프리셋 → 전용 아트 · 비 오는 노을 도심**으로 켭니다. 기본 설정은 유지합니다. 이번 전용 장면은 고정 노을/도심이며 기본 차량 색상에서 새 아틀라스를 사용합니다. 다른 시간대/맵·차량 컬러풀/파스텔은 기존 아트입니다. 새 선택값은 프리셋/JSON/앱·시스템 미리보기/홈 배경/진단에 연결했습니다.
- Surface별 HW 연결 방식 유지, 초기 HW 불가 시 SW 대체, HW 사용 중 일시 실패는 프레임 건너뛰기/재시도, 제출 시 Surface 소멸 예외 처리. 전용 자산 읽기 실패는 기존 장면으로 대체합니다.
- 최종 Android assembleDebug/lintDebug 성공(Lint 이슈 없음), APK 서명·code19/version0.19.0과 포함 PNG 원본 일치 검사 통과. 독립 계산11종, 기존 합성60조합, 전용 합성33조합/아틀라스/그래픽 어댑터 검사 통과. 데스크톱 최종 합성에서 차량 차선과 전철 교량 위치를 확인했습니다.
- 전달 APK `/workspace/artifacts/pixel-traffic-0.19.0-debug.apk`, 데스크톱 검토 이미지 `/workspace/artifacts/pixel-traffic-0.19.0-cinematic.png`. [설계·제약](CINEMATIC_RENDERER.md) 및 [설치/사용법](../pixel-traffic/README.md) 참고. 파일은 저장했으며 커밋/원격 푸시는 하지 않았습니다.
- **실기기 대기**: 새 버전의 S20+/S26 Ultra 적용·GPU Canvas 진단·기본 맵 복귀·숨김/복귀. 실제 표시FPS·배터리·발열은 미측정입니다. 장시간 전력 작업은 사용자 요청대로 추후 진행하며, 다음 아트 단계는 다른 시간대/맵과 전경 분리입니다.

## 이전 전달 — 0.18.0

- 사용자 S20+ 및 S26 Ultra 두 기기에서 정상 다운로드/적용·현재까지 문제 없음 보고. 추가 진단 수치/장시간 부하 측정 없음. 다음 제안은 전용 배경/전경/차량 아트 자산 제작을 도심부터 검증한 뒤 확장하는 것입니다(미실행).

- 시안 기반 도시/4맵 아트 재구성, 전용 차량36변형, 가로등/차량 발광 및 노면 반사 보강.
- 이벤트4종과 켬/빈도3단계, 프리셋/미리보기/진단 연결. 선택형24대와 노을 도심 추천 추가. 기존 설정/프리셋 호환.
- APK 빌드·Lint·서명·버전과 모델 검사10종 및 데스크톱 합성60조합/차량36종 검사 통과. 최종4맵 합성 시각 확인. Android 표현/24대·반사·이벤트 추가 부하/기존 프리셋 확인 대기.
- 시안과 동일한 표현 수준은 미확정. 실제 표시FPS·장시간 전력·발열은 미측정이며 후속 기기 확인이 필요합니다.

## 이전 전달 — 0.17.0

- 설정 접이 구역/추천3종/전체 화면 미리보기/프리셋 이름 변경 및 JSON 내보내기·가져오기/현재 설정 초기화 구현.
- 가져오기는128KB/10개/중복 거절 및 전체 검사 후 추가. 현재 설정 초기화는 저장한 프리셋 유지. 별도 권한 추가 없음.
- APK 빌드·Lint·서명·버전 검사 통과. Android UI·문서 제공자·손상파일·복원·취소 검증 대기. 다음은 사용자 기기에서 통합 편의성 확인.

## 이전 전달 — 0.16.0

- 사용자0.15.0 정상 적용 보고.
- 전체 풍경/차량 중심1.5배 및 확대 위치3종, 미리보기/홈 배경/진단/프리셋 연결. 기본 기존 구도 및 구버전 프리셋 호환.
- 빌드·Lint·서명·버전 및 화면 크기별 구도 계산 검사 통과. 실제 Android UI/크롭/저장/부하 확인 대기.

## 이전 전달 — 0.15.0

- 사용자0.14.0 정상 적용 및 제출59.6fps 보고, 현재까지 문제 없다고 보고.
- 맵별 네온/구름/산안개/파도/등대/안내판 움직임 및 풍경 스위치, 프리셋/미리보기/진단 연결. 기존 프리셋 호환.
- 빌드·Lint·서명·버전 및 활성 시간 검사 통과, Android 실제 표현/추가 부하 확인 대기. 장시간 전력 검증은 추후 진행.

## 이전 전달 — 0.14.0

- 차량/도심/해안/산길/고속도로 아트 보강. 등대·포말·숲/바위·주유소·도시 옥상/보도 소품 및 차체 명암/유리 반사 추가.
- 캐시 이미지 생성 경로만 확장하고 설정/프리셋/주행 모델 유지.12종 도로/시간대 정적 출력,36종 스프라이트 그림 존재/투명 테두리 검사 통과.
- APK 빌드·Lint·서명·버전 검사 통과. 실제 Android 표시/시간대 전환/프리셋 및 생성 부하 확인 대기. 장시간 전력 검증은 사용자 요청으로 추후 진행.

## 이전 전달 — 0.13.0

- 사용자 정상 적용 보고. 최신 진단: 해안/밤/강한 비/12대/1.5배/컬러풀, 제출59.6fps·그리기10.60ms·760프레임. 장시간 전력/발열 및 프리셋 개별 경로 검증은 남아 있습니다.

- 사용자 프리셋: 이름1~32자/최대10개, 현재11개 설정 캡처, 저장·이름순 목록·불러오기·덮어쓰기·삭제.
- 별도 로컬 저장/schema1 JSON, 전체 검증 후 단일 적용. 앱/엔진 설정 자동 반영. 삭제는 현재 설정 유지.
- 빌드·Lint·서명·버전 검사 통과. 실제 Android 저장/재시작/전체 필드 복원 검증 대기. 다음은 프리셋 실기기 확인.

## 이전 전달 — 0.12.0

- 0.12.0 사용자 정상 적용 보고. 홈 화면에서 해안·밤·비·컬러풀 차량 확인. 해당 조건 진단 제출59.8fps/그리기10.37ms/703프레임. 모든 조합 및 장시간 전력 검증은 미실행.
- 0.11.0 사용자 정상 동작 확인.
- 요청1~5 구현: 도시 디테일, 차량 그룹4개/색상3개, 도로4개, 날씨5개, 약4초 시간대 혼합. 앱/시스템 미리보기와 홈 배경에 연결, 기존 설정값 보존.
- APK 빌드·Lint·서명·버전 검사 및 모델 검사7종 통과, 도로4종 정적 아트 출력. 실제 Android UI·추가 캐시 메모리/합성 부하·장시간 전력 확인 대기.
- 다음: 사용자 설치 후 조합/전환/설정 유지 확인, 실제 기기 성능을 바탕으로 필요한 최적화.

## 이전 전달 — 0.11.0

- 사용자 0.10.0 정상 동작 확인(밤·강한 비·12대·1.5배, 제출 59.3fps/그리기 11.77ms).
- 배경 밝기 100/80/60 선택과 자동 저장, 미리보기/홈 배경 반영, 진단 밝기 기록 추가. 하드웨어 밝기는 변경하지 않습니다.
- 빌드·Lint·서명·버전 검사 통과. 실기기 표시/설정 유지 확인 대기.

## 이전 전달 — 0.10.0

- 사용자 0.9.0 정상 동작 확인. 최신 진단 48.2fps/12.92ms는 측정 조건 미확인으로 회귀 여부 미확정.
- 6종 차량의 그릴/루프/레일/택시 표지/버스 환기구/트럭 화물칸/미러 표현을 보강했습니다. 초기 스프라이트에 저장합니다.
- APK 빌드·Lint·서명·버전 및 정적 아트 출력/12종 이미지 존재 검사 통과. 실제 Android 표현·전력 확인 대기.

## 이전 전달 — 0.9.0

- 0.8.0 정상 동작은 사용자 확인. 제출 평균 56.5fps/그리기 11.20ms 기록(저녁·강한 비·12대·1.5배).
- 0.9.0: 설정 상단 실시간 미리보기, 전체 장면 비율 유지, 설정 자동 반영, 독립 15fps 재생 및 비가시/포커스 상실 중지. 실제 배경화면 진단은 보존합니다.
- APK 빌드·Lint·서명·버전과 순수 Java 배치 검사 통과. 실제 Android UI/추가 전력 검증 대기. 다음은 사용자 설치 후 미리보기 반영·정지·프리셋 확인.

## 저장소와 환경

- GitHub: https://github.com/BACKHYUN96/S20-PLUS
- 현재 클라우드 체크아웃: /workspace/S20-PLUS
- 저장소 이름 변경 후 origin 주소와 로컬 폴더를 갱신하고 원격 읽기 접근 및 Git 무결성을 확인했습니다.
- 클라우드 환경의 저장소 설정 초안은 새 이름과 경로로 저장했습니다. 게시 완료 여부와 새 작업에서의 복원은 확인하지 않았습니다.
- pixel-traffic/에 Android 기본 앱과 Gradle 빌드 설정이 있습니다. 차량 애니메이션을 구현했으며 완성 아트는 아직 없습니다. 0.1.0 적용은 사용자 확인을 받았고 0.2.0 실기기 검증은 대기 중입니다.

## 연결된 기기

- 사용자 노트북에 USB 디버깅으로 연결한 루팅된 Samsung Galaxy S20+ (SM-G986N).
- 사용자 PowerShell에서 ADB의 device 상태를 확인했습니다.
- Windows ADB 경로: `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe`
- 현재 클라우드 에이전트는 사용자 노트북 PowerShell이나 USB 기기를 직접 조작할 수 없습니다. 사용자 실행 결과 또는 첨부 파일로 확인합니다.

## 레드존 분석

- 패키지: com.redzone.app. 사용자 기기 출력 기준 버전 0.5.2, versionCode 21.
- 첨부 APK에서 센서 모니터링, 오버레이, CPU/GPU 클럭 제어, 밝기 고정, 온도 기반 복원 및 CSV 로깅 관련 코드·문자열을 확인했습니다.
- 제작자 정보, 개발 일지, 원본 프로젝트, Git 이력은 발견하지 못했습니다. 전체 소스 디컴파일과 동작 검증은 수행하지 않았습니다.
- CSV 27개, 37,301건을 분석했습니다. 요약과 한계는 WORK_LOG.md에 있습니다.
- APK와 로그 ZIP은 이 채팅의 첨부 파일이며 저장소에는 포함하지 않았습니다. 새 환경에 없으면 사용자에게 다시 확보해야 합니다.

## 다음 작업

- 실기기 확인 갱신: 사용자가 0.1.0 APK 설치·실행 성공을 보고했고 홈 화면 스크린샷에서 임시 장면 적용을 확인했습니다. 화면 꺼짐/복귀·잠금화면·장시간 전력 검증은 미실행입니다. 0.2.0에 차량 무한 주행을 구현했으며 업데이트 후 실기기 검증이 필요합니다.

- 새 앱 아이디어: 픽셀 차량 무한 주행 라이브 배경화면. 단계 2 기본 앱을 구현했습니다. [구상 문서](PIXEL_TRAFFIC_CONCEPT.md)를 참고합니다.
- 단계 1 화면 설계 완료: 저녁 도심, 양방향 4차선, 차량 6종, 기준 화면 360×800px와 분리된 아트 레이어를 [설계 문서](PIXEL_TRAFFIC_DESIGN.md)에 정의했습니다. 최종 아트는 미구현입니다. 기본 앱 진행은 아래 단계 2를 참고합니다.
- 단계 2: [기본 앱](../pixel-traffic/README.md)에 시스템 미리보기 버튼, WallpaperService, 정적 임시 도심 장면을 구현했습니다. JDK/SDK를 설치하고 APK 빌드·Lint·서명 검사를 진행했습니다. 실제 S20+ 적용은 사용자 기기에서 확인해야 합니다. 다음은 차량 무한 주행입니다.

- 새 앱의 목적과 기능 요구 사항을 정한 뒤 소스 및 빌드 환경을 구성합니다.
- 레드존을 이어 개발할 경우 원본 프로젝트 확보를 우선합니다. 없으면 APK 역분석 결과를 바탕으로 재구현 범위를 결정합니다.
- 레드존 보호 동작을 수정할 경우 상태 문구뿐 아니라 실제 설정 복원 결과를 검증해야 합니다.
- 모든 후속 작업은 AGENTS.md의 기록 규칙에 따라 이 문서와 WORK_LOG.md에 남깁니다.

## 단계 3 현재 결과

- 0.2.0(versionCode 2): 양방향 차량 6종, 화면 밖 재사용, 원근 크기, 차간 거리, 가시 상태 렌더링 루프 구현.
- 다음은 사용자 기기의 0.2.0 움직임 확인과 단계 4 완성 아트입니다. 0.1.0 실기기 성공이 0.2.0 성공을 뜻하지 않습니다.

## 단계 4 현재 결과

- 0.3.0 첫 아트 버전: 건물/상점/가로수/가로등, 차종별 도트 자산, 이동 반사와 공통 픽셀 프레임 합성 구현. 실기기 검증 대기 중이며 생성 시안의 완성도와 동일하지 않습니다.
- APK 제공 시 PowerShell 설치 명령어를 함께 제공하도록 기록했습니다. 다음은 아트 피드백과 단계 5 설정/전력 관리입니다.

## 단계 5 현재 결과

- 사용자 보고로 0.3.0 적용 성공 확인. 0.4.0에 차량 수/속도/프레임/OS 절전 연동 설정을 추가했습니다.
- 화면 꺼짐과 비가시 상태 중단, 재개 시 시간 초기화, 설정 자동 저장과 엔진 반영 구현. 정책/모델 검사는 통과했으나 실제 방송·화면 수명주기·배터리 사용은 기기 검증 대기 중입니다.
- 다음은 단계 6 실기기 설정 검수와 장시간 안정성/전력 확인입니다.

## 단계 6 현재 결과

- 사용자 보고로 0.4.0 정상 동작 확인. 0.5.0 진단 UI와 로컬 프레임 기록, [실기기 검증 절차](PIXEL_TRAFFIC_DEVICE_CHECKS.md), 읽기 전용 PowerShell 수집 도구를 추가했습니다.
- 장시간/배터리 검증은 미완료입니다. 0.5.0 진단 기록과 화면 꺼짐/다른 앱 복귀/절전 모드 결과를 받은 뒤 평가합니다.

## 단계 7 현재 결과

- 0.5.0 실기기 진단: 제한 60fps, 제출 평균 56.2fps, 그리기 평균 8.20ms, 프레임 614. 앱을 열 때 가려짐/중지 상태 확인. 장시간 안정성·배터리는 여전히 미검증입니다.
- 0.6.0에 누적 기한 기반 프레임 예약, 낮/저녁/밤 수동 선택, 한국어 진단 상태를 추가했습니다. 계산 및 아트 검사와 APK 빌드는 통과했으나 실제 개선 수치는 다음 기기 진단으로 비교해야 합니다.

## 최신 실기기 확인

- 0.6.0 낮/저녁/밤 적용 성공 사용자 확인. 60fps 제한에서 제출 평균 59.9fps, 그리기 평균 7.60ms, 성공 프레임 3336을 스크린샷으로 확인했습니다.
- 이전 관찰보다 제출 평균이 높지만 비교 조건은 동일함을 확인하지 못했습니다. 장시간 안정성·전력 검증은 남아 있습니다.

## 날씨 확장 현재 결과

- 0.7.0: 수동 맑음/약한 비/강한 비, 도로 물 튀김, 반사/밝기 보강 구현. 기존 장면 시간대와 독립적으로 선택합니다. 비 시뮬레이션 검사를 통과했으며 실제 기기의 비 표현/부하는 검증 대기 중입니다.
- 진단에 장면·날씨·차량 수·속도를 기록합니다. 장시간 안정성·전력 검증은 계속 남아 있습니다.

## 자동 시간대 및 빠른 모드 현재 결과

- 0.7.0 밤/강한 비/12대/1.5배에서 사용자 진단 59.3fps, 그리기 11.03ms, 프레임 1219 및 정상 동작 보고 확인. 장시간 전력 결과는 아닙니다.
- 0.8.0에 휴대폰 현지 시각 자동 시간대와 3개 빠른 설정 모드를 추가했습니다. 정책 검사는 통과했으며 UI/시간·시간대 변경 실기기 검증은 대기 중입니다.
