# 작은 패치 작업 안내

## Unity 0.13.0 — 가로수·외벽·옥상 (2026-10-10)

- 범위10source/meta: StarterConfig/StarterScene/CityEnvironment/CityPreview 및 FoliageScene/ArchitectureScene/SceneryChecks+meta3. Scenery0130/code14,98build입력; 기존 카메라·원경/traffic/light/lane/NativeAndroid 유지.
- 3종 opaque 수관400tri/shared256texture32나무와24건물의3 merged detail mesh/12옥상shrub. 실제48slab/192standing post, topology·finiteUV/unitnormals/winding·보행/roof clearance·실제wind/lifecycle/18기후/기존traffic 검사. 기본107334/1383/43,100우산118710/1551/43; 기존120000/2400/48 strict 유지.
- 첫Validate38038675998과GPU51에서 외형 관찰 후 slab/post 실체 검사를 보강했다. 최종Validate38039103702,source1196faea4f345eb0e039be1d27b839f5998a2305와51PNG(기존43+수형/건축8확대); 임시camera는저장하지않으며Editor사진은폰FPS검사가아니다.
- Editor촬영-only 보정sourceabe19eb7eff5e5260c7f3d88423830081595aba6/Validate38039712854에서 Effects.Advance(0)·맑음 rain/snow OFF·51PNG 확인. APKsource와97입력동일/capture1개변경을구분한다.
- implementation source1196faea4f345eb0e039be1d27b839f5998a2305 APKtag0.13.0-build1/BuildApk38039430522, 실제BuildPlayer0errors21warnings/launcherLint0errors8warnings/다운로드v2cert/code14/manifest/SHA. 12host입력불변으로hostLint380083782070errors10warnings재사용. APK/PNG 일회성downloadbranch의공개HTTP/SHA검증; docs-only에는같은검사를반복하지않음. 폰0.13 확인대기,Drive보류.

## Unity 0.12.0 — 하늘·산·강·도시 원경 (2026-10-10)

- 변경13 Unity소스/meta: StarterConfig/CityClimate/StarterScene/CityEnvironment/CityPreview와 신규 DistantBackdrop/DistantScene/DistantChecks/Distant.shader +meta4개. Landscape0120/code13,92build입력; NativeAndroid12host 입력/카메라/기존traffic/light/lane변경 유지.
- 원경7mesh/1108tri/한 개 공유runtime재질,64tri 공유 원통최적화. 기본105942/1299/43,100우산117318/1467/43, strict120000/2400/48 유지. actual topology/finiteUVColors/materialclone18endpoint/독립15·30·60·120Hz sunset900frame/숨김100/retarget/projection4 검사.
- 첫Validate38031801525 GPU산UV줄무늬·평행glints 보정 후 실제Validate38032346679과새43PNG 확인. 원경낮/노을/밤/비바람 확대4장; Editor이미지≠phoneFPS.
- sourceb0c20f510d53c82f466310a5334824c32ac82526, 동일source APKtag0.12.0-build1/BuildApk38032677827, 실제BuildPlayer0errors0warnings/launcherLint0errors8warnings/다운로드APK v2cert/code13/manifest/hash. unchanged12host입력으로hostLint380083782070errors10warnings 재사용. public일회성APK/PNG branch HTTP/SHA검증; docs-only변경에는동일검사반복안함. 폰0.12/발열·FPS 대기, Drive보류.

## Unity 0.11.0 — 시안 카메라 (2026-10-10)

- 변경5소스: StarterConfig/StarterScene/CityEnvironment/DrivingChecks/CityPreview. ReferenceCamera-0.11.0.unity/Generated/ReferenceCamera0110/code12,84build inputs. Camera(1.4,14,-37),target(-4.9,1,68),44°FOV. 원경 강·구름 디테일은 다음 단계.
- 9:20/9:16 독립 world projection의 road vanishing/foregroundCentreX/crossingx,y/horizon,lower18 ground rays/upper6 sky rays 검사. 기존 차량/보행/날씨offscreen생성·퇴장과100우산 예산을 검사. 최대119922/1515/47,새geometry 없음.
- 실제 D3D11 PNG39장에 동일traffic/lighting의 old/new/9:16 비교3 추가. 첫PASS렌더의 과한 diagonal을 실제 관찰하고 카메라/target을1.8m shift한 뒤 최종Validate38028274105와새render를관찰했다. 사진은폰성능증거가아님.
- 동일 source의 APKtagunity-apk-0.11.0-build1/BuildApk38028607350,AndroidBuildPlayer/Lint·다운로드SHA/원본v2/code12/ARM64/manifest. unchanged12host SHA로기존Lint38008378207재사용. APK는일회성downloads/unity-0.11.0branch의공개HTTP/SHA검증링크로전달;자동release권한은없음. 문서-only에는같은검사반복불필요.

## Unity 0.10.0 — 카메라·차량 등화와 차선 변경 (2026-10-10)

- 변경19소스/meta/84build inputs. Runtime StarterConfig/PrototypeDrive/StreetModel/StreetSimulation/LaneChanges/VehicleLighting,Editor TrafficFleet/StarterScene/ClimateScene/CityPreview/StreetChecks/WeatherLifeChecks/VehicleDetailChecks/DrivingScene/DrivingChecks 및 신규meta4개. Driving-0.10.0.unity/Generated/Driving0100/code11.
- 뒤60m모든관측차 실제추월/새뒤차/loopodometer/앞뒤속도차·안전거리/동방향차선예약/3pulse1.8초후3초smoothmerge/50m횡단보도·storm금지/600초productiongap·보행phase/실제MPB·좌우·sharedmesh/15·30·60·120Hz z/x/stage·pause를 검사한다. 도로빛은BodyBounds제외,실제Light4개유지. 100우산max119922/1515/47,tri잔여78.
- WindowsValidate38019981969 및실제D3D11 36PNG에서낮/밤구도·4등화·4실제lane상태를관찰한다. GPU사진은폰성능증거가아니며Editor임시pose와camera는저장하지않는다. 부분수관가림은남는다.
- 동일source APKtagunity-apk-0.10.0-build1/BuildApk38020355014,AndroidBuildPlayer·launcherLint0errors/8warnings·downloadSHA/원본v2cert/11version/ARM64/service manifest확인. 12host입력불변으로실제hostLint38008378207재사용. 문서only는검사반복불필요.

## Unity 0.9.0 — 상가·창문 야간 조명 (2026-10-10)

- 변경10소스/meta: Editor CityEnvironment/ClimateScene/StarterScene/CityPreview/FrontageScene/FrontageChecks 및meta2개,Runtime CityClimate/StarterConfig. Storefront-0.9.0.unity/Generated/Storefront090/code10. 건물별window/frame/shop mesh 및shared256×128/512×512 atlas. 차량/보행/host geometry·native·서명 설정 불변.
- 실제24건물/480창문/3종8매장, 좌우 readable UV/finitevertex/unitnormals/degenerate없음/공유material·발광map·동선/15·30·60·120Hz directional시간shader oracle/숨김·retarget·asset불변/4개실제조명 제한을 검사한다. 기존차량/보행/날씨·100우산 검사 포함 actual최대119634/1443/47,기존120000/2400/48 strict예산 유지.
- 실제D3D11 EditorPNG28장, 상가낮/밤6장과도시밤/노을을 관찰한다. black/pink 자동검사PASS가 시각관찰을 대신하지 않는다. 임시카메라/포즈는APKscene에저장하지 않는다.
- mainValidate38017565116→같은source APKtag0.9.0-build1/BuildApk38017884342→AndroidBuildPlayer/launcherLint/다운로드SHA·원본v2cert/code10/ARM64/service manifest. unchangedhost입력SHA로실제hostLint38008378207재사용. 관련입력변경시재검사하며docs-only에는반복하지않는다. 폰FPS/발열은별도.

## Unity 0.8.0 — 차량 곡면·유리·휠·램프 (2026-10-10)

- Editor VehicleGeometry/TrafficFleet/CityPreview/StarterScene/VehicleDetailChecks(.meta),Runtime StarterConfig의7소스/meta. VehicleDetail-0.8.0.unity/Generated/VehicleDetail080/code9. 기존4모델24대 metre/unit scale과14renderer/car·신호/날씨·NativeAndroid를 유지한다. Curve normal smoothing은 Editor 생성만 수행하며64×64 공유glass finish는 authored texture이고 realtime reflection이 아니다.
- actual mesh topology/finite vertices/unit normals/UV·공유/crown/rounded tyre shoulder·contact 및기존4모델 dimensions/600초간격/15·30·60·120Hz/wheel/lifecycle를검사한다. 기존신호·보행·날씨·시간/100우산 worst-case120000tri/2400renderer/48material을통과한다. Actual119394/2331/46; fleet49080tri(+480).
- 실제 GPU EditorPNG22장과모델4종front/rear8을관찰한다. OpenScene의readback texture unload 및rear camera canopy가림을수정했다. 자동black/pink PASS만으로차량가림을확인했다고하지않는다. unsaved camera/pose는APK에저장하지않는다.
- mainValidate38013804973→동일source APKtag0.8.0-build1/BuildApk38014126469→실제BuildPlayer/launcherLint/다운로드SHA·v2cert/code9/ARM64/service manifest. NativeAndroid/Bridge/host/shader SHA같아 실제hostLint38008378207을재사용한다. source가바뀌면관련검사를다시수행하고docs-only에는반복하지않는다. 폰FPS/발열은별도이다.

## Unity 0.7.0 — 노을 경유·날씨 반응 도시 (2026-10-10)

- Runtime SceneBlend/CityClimate/StreetModel/StreetSimulation/StarterConfig/WetTraffic(.meta), Editor StreetScene/ClimateScene/ClimateChecks/WeatherLifeChecks(.meta)/CityPreview/StarterScene의14소스/meta만 변경한다. Generated/WeatherLife070,WeatherLife-0.7.0.unity,code8을 사용한다. 원본 도시/차선·차량 geometry/SidewalkRoutes/NativeAndroid·Surface host를 유지한다.
- 낮→밤은2초 낮→노을+2초 노을→밤; 밤→낮/다른 선택은4초. 전체 active-time,같은 Target 반복/양구간 retarget/저장 night snap/숨김 freeze를15/30/60/120Hz 독립 oracle와 actual scene에서 확인한다.
- 기존24car/100rig pool에날씨별 목표·감속/빠른 걸음을 연결한다. viewport3×3.2×6m 보수적 bounds 밖에서만날씨 active변경,횡단자는인도까지이동,보행재진입1.5초/차량간격을유지한다. count4/32/100각810초 storm600+recovery185초를기존1400초 모델검사와함께실행한다. 비바람 사람30%(최소4)/차선당3차량/주행60%/걸음118%이다. 퇴장시간이필요하다.
- 공유24tri umbrella100개와96quad spray 단일mesh,4색 runtime MPB/손에맞춘shaft/바람 tilt/정차·맑음 sprayOFF를검사한다. 100우산 포함120000tri/2400renderer/48material 제한과D3D11 Editor PNG14장을관찰한다. Editor이미지를실제폰/FPS증거로기록하지않는다.
- mainValidate뒤같은source APKtag, 실제BuildPlayer/launcher Lint/v2기존cert/0.7.0/code8/min29/ARM64/서비스manifest/다운로드SHA를확인한다. NativeJava/res와Bridge/AndroidWallpaperBuild/Atmosphere.shader SHA불변을근거로hostLint38008378207(errors0/warnings10)을재사용한다. 신규host source가바뀌면재실행한다. docs-only최종게시에는동일빌드검사를반복하지않는다.

## Unity 0.6.0 — 시간대·날씨 전환 (2026-10-10 KST)

관련 범위는 Runtime SceneBlend/CityClimate/ClimateEffects/AndroidWallpaperBridge/StarterConfig/StreetSimulation(신호 emission), Editor ClimateScene/ClimateChecks/StarterScene/CityPreview/CityEnvironment(shader 허용 검사), Shaders/Atmosphere.shader 및 meta, NativeAndroid WallpaperPreferences/WallpaperSettingsActivity/values XML이다. 도시·차선 생성 geometry, VehicleGeometry/TrafficFleet/StreetModel/SidewalkRoutes, Surface host 및 서비스/IPC 보안 코드는 유지한다.

- 네이티브 ThemeBlend/RoadWetness/CinematicScene/WindStorm의 4초 active-time smoothstep와 current weights retarget 계약을 먼저 읽었다. 시간대3/날씨6 및 UI 저장 설정을 연결한다. 수동 설정이며 실시간 시각/기상 API는 연결하지 않는다.
- 필수 실제 PC pipeline의 PrepareBatch/Validate: 기존 vehicle/street/100-rig budget + 새 ClimateChecks의15/30/60/120Hz 180 oracle cases/18 endpoints/actual rain/snow visibility/32 canopy sway/숨김 freeze/retarget/3~5초 paper cadence. 검사 임시 상태는 저장하지 않고 성공 후 원본 장면을 다시 연다.
- 실제 GPU Capture는 D3D11 Editor540×1200 낮/노을/야간·날씨6selected endpoints와0/2/4초 전환 PNG를 관찰한다. black/pink 검사가 실제 시각 관찰을 대체하지 않는다. Editor PNG를 폰 screenshot/FPS 증거로 기록하지 않는다.
- BuildApk의 실제 Android BuildPlayer/launcher Lint/certificate/version/manifest 및 `tools/check-wallpaper-lint.ps1`의 생성 Java/res SHA 확인 후 unityLibrary Lint가 필요하다. Java/res가 바뀌므로 이전 host Lint를 재사용하지 않는다. signed APK는 집PC 최신 폴더 명령과 함께 전달한다.
- 관련 검증 결과는 STATUS/WORK_LOG/UNITY_AUTOMATION 최신0.6.0항목, APK/보고서/source-manifest는 `/workspace/artifacts`를 참조한다. 실제 폰의 UI/저장설정/숨김·복귀/성능은 사용자 확인 대기다.

## Unity 횡단보도 차선 보정 0.5.1 범위와 검사

| 변경 대상 | 관련 파일 | 확인 |
| --- | --- | --- |
| 횡단보도 안 중앙선·흰색 dash 제거 | Editor/CityEnvironment.cs | 실제12stripe와 CentreLine/LaneDash renderer bounds 비중첩·횡단보도 바깥 표시·Editor PNG |
| 버전·새 장면 | Runtime/StarterConfig.cs, Editor/StarterScene.cs | 0.5.1/code6/Wallpaper051·이전 장면 보존·기존 pipeline Unity/BuildPlayer/launcher Lint·원본 v2 서명·APK 해시/manifest |
| 변경 없는 host·보행·차량 | NativeAndroid, StreetModel/SidewalkRoutes/StreetSimulation, 차량 geometry | 0.5.0과 SHA 동일. host library Lint38003123943(errors0/warnings14)을 재사용하며 source가 바뀌면 다시 수행 |

이 패치는 도로표시3파일만 수정합니다. 횡단보도 z8~12m의 간격까지 차선을 비우고 바깥 차선/stop line/횡단 band는 유지합니다. 시간대·날씨·차량 모델 기능을 추가하지 않습니다. 기존 자동 pipeline의 필수 검사 묶음을 사용하며 별도 무관한 native/host/model 검사를 중복 실행하지 않습니다. 최종 문서만 바꾸면 같은 source 빌드를 반복하지 않습니다.

## Unity 라이브 배경화면 0.5.0 범위와 검사

| 변경 대상 | 관련 파일 | 필수 확인 |
| --- | --- | --- |
| Service Surface 연결·미리보기/홈 공유 | NativeAndroid/src/*WallpaperService, WallpaperRuntimeHost, SurfaceArbiter | 실제 Unity Service API 대조·production selection 우선/숨김/화면OFF/invalid/resize/1000회 생성 제거·실제 Android 컴파일 |
| 인원·절전 설정 저장 및 Android bridge | NativeAndroid/src/*SettingsActivity, *SettingsProvider, WallpaperPreferences; Runtime/AndroidWallpaperBridge, StreetSimulation | provider 비공개 같은 UID·설정 범위·숨김 업데이트 중지/복귀 delta·production 보행/차량 검사; 폰 저장/재시작은 별도 |
| Gradle service/launcher 등록·APK | Editor/AndroidWallpaperBuild, StarterConfig, PrototypeBuild, tools/run-pipeline.ps1, workflow | manifest 템플릿 idempotence·실제 BuildPlayer/launcher 및 unityLibrary Lint·service BIND_WALLPAPER/process/metadata·provider exported=false·SettingsActivity launcher·Unity Activity disabled·원본 v2 cert·0.5.0/code5/ARM64/min/target/hash |
| 적용/기기 수명주기 | 앱 README, UNITY_AUTOMATION, STATUS | 사용자 시스템 미리보기 적용·홈/잠금·다른 앱/화면OFF/복귀·재부팅/설정 보존·FPS/발열. PC 검사와 구분 |

StreetModel/SidewalkRoutes/geometry를 바꾸지 않습니다. 별도 prototype appID를 유지하며 화면 적용은 사용자가 Android 시스템 화면에서 선택합니다. Editor 렌더는 폰 배경화면 스크린샷이 아닙니다. 15/30 FPS는 목표값입니다. 실제 PC 결과 문서만 갱신할 때 동일 소스 검사를 반복하지 않습니다.

## Unity 신호·인도 보행 0.4.0 범위와 검사

| 변경 대상 | 관련 파일 | 필수 확인 |
| --- | --- | --- |
| 공통30Hz 신호·차량 정차/재출발 | Runtime/StreetModel.cs, StreetSimulation.cs, PrototypeDrive.cs | 실제 production model 신호 복귀·보행 신호 시 road 비움·1.8m bumper gap·15/30/60/120Hz·pause |
| 인도/상점 앞 통로·화단/가로등 회피·자연스러운 횡단·인원 조절 | Runtime/SidewalkRoutes.cs, StreetModel.cs, StreetSimulation.cs | 4/32/100명200/600/600초·모든 사람 진행/횡단·연속0.40m 발 분리·실제 화단 bounds·최대12명 대기·감소 시 횡단자 안전 복귀 |
| 공유3D 사람·신호·실제 미리보기 | Editor/StreetScene.cs, StreetChecks.cs, CityPreview.cs, CityEnvironment.cs | 실제100rig·4limbs·공유mesh/palette·기존120000triangle/2400renderer/48material 예산·30초 model pose 실제 GPU PNG |
| 버전·APK | StarterConfig.cs 및 기존 build/pipeline | main 검사 후 같은 source tag·BuildPlayer/Lint errors0·기존v2 cert·0.4.0/code4/ARM64/min/target/hash·다운로드 실제APK 대조 |

검사는 기존0.3 교통 geometry/자유 주행 및0.2 도시 조건과 새 production model을 함께 확인합니다. 기본32명, 버튼4~100명, 대기 중에는 다른 보행을 계속합니다. 장애물에 갇힌 사람을 순간 이동하거나 테스트 인원을 숨겨 통과시키지 않습니다. 실제 폰 FPS/발열·날씨·라이브 배경화면 연결·설정 영구 저장은 후속이며 Editor 결과와 구분합니다. 결과 문서만 변경하면 같은 source 검사를 반복하지 않습니다.

## Unity 차량·왕복 교통 0.3.0 범위와 검사

| 변경 대상 | 관련 파일 | 필수 확인 |
| --- | --- | --- |
| metre 차체·경사 유리/지붕·램프·회전 wheels | Editor/VehicleGeometry.cs, TrafficFleet.cs | 실제24대 renderer bounds·접지·unit scale·차선/거리별 모델 크기 일치·SUV/스포츠 높이·mesh/material budget·실제 GPU PNG |
| 양방향 고정 lane·wrap·pause | Runtime/PrototypeDrive.cs, Editor/TrafficFleet.Validate | 실제 Step 15/30/60/120Hz를 독립 위치 oracle와 대조·600초 같은 lane 모든 bumper gap·wheel角/거리·wrap overshoot/multi-loop·pause/resume·invalid time |
| 버전·실제 APK | StarterConfig.cs 및 기존 build/pipeline | main compile/scene PASS 후 같은 commit tag, BuildPlayer/Lint errors0/원본 v2 cert/code3/version0.3.0/ARM64/min/target/hash·다운로드 ZIP/실제 APK 대조 |

0.2.0의 도시·조명·shader/mipmap/18portrait rays와 기존120000triangles/2400renderers/48materials 예산을 유지합니다. 합쳐진 mesh/material는 생성/로드 때만 만들고 root scale1·4wheel/차종shared mesh를 유지합니다. 재등장 때 객체 생성/도로폭 맞춤 비균일 scaling을 넣지 않습니다. 실제 GPU 렌더는 Editor 카메라 결과이며 폰 FPS·발열과 구분합니다. 신호/사람/추월/차선 변경·라이브 배경화면 연결은 별도 패치입니다. 관련 input이 바뀌지 않았다면 native 검사/이미 통과한 Unity build를 반복하지 않습니다. docs-only 결과 업데이트는 source 검사를 다시 시작하지 않습니다.

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
