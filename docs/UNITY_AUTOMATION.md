# Unity 자동 빌드 — PC에서 시작하고 클라우드로 이동하기

## 2026-10-10 — Unity 0.8.0 차량 곡면·유리·휠·램프 개선 APK 전달 완료

사용자가 **0.7.0 실제 폰 적용 성공**을 확인하고 다음 단계를 승인했다. 이전 추천인 차량 3D 외형 고도화를 **0.8.0/code9**에 적용했다. 실제 PC 장면·GPU 렌더·Android BuildPlayer/launcher Lint와 내려받은 기존 v2 서명 APK를 검증해 전달한다. 실제 폰의0.8.0 외형·설정 보존·숨김 복귀·FPS/발열은 설치 후 확인이며 PC/Editor 결과로 대체하지 않는다.

### 구현과 범위

- 기존 **Sedan/SportCoupe/Suv/Taxi4종·차량24대**의 metre 크기와 unit scale을 유지한다. 보닛 ridge와 crowned/tapered roof를 만들고 위치를 움직이지 않는55도 paint normal smoothing으로 곡면에 빛이 이어지게 했다. tyre shoulder를 둥글게 만들고65도 normals로 tread seam을 연결하며 radius와 road contact를 유지한다. 휠 spoke는 두께가 있는 tapered face로 바꾸고 가려지는 box 면을 줄여 예산을 확보했다. fender lip을 실제 tyre 바깥에 보이게 배치하고 front/rear lamp에 chamfered silhouette를 추가했다.
- 기존14renderer/car 및4모델 shared mesh/46material을 유지한다. 유리에UV와공유 **64×64 authored sky-tint/highlight texture**를 추가했다. 실시간 반사 probe가 아니며 새 per-car material/texture를 만들지 않는다. 곡면 계산은 Editor 생성시에만 수행한다. 휴대폰 camera/신호·보행/시간·날씨/우산·물보라/NativeAndroid 서비스·UI·저장소/appID·15/30FPS 목표는 유지한다. 모델은 기존 generic4종이며 별도 브랜드 신차를 추가한 패치가 아니다.
- 범위 **7소스/meta**: Editor VehicleGeometry/TrafficFleet/CityPreview/StarterScene/VehicleDetailChecks(.meta),Runtime StarterConfig. `VehicleDetail-0.8.0.unity`/`Generated/VehicleDetail080`,code9를 사용한다. native0.48 및누적 dirty/real index를 보존했고 임시 index whitelist로 게시했다.72빌드 입력 SHA가 최종source와 일치한다.

### 실제 과정·수정

base01e70ec78bb5ce25154f4c499d33ef1a3c1a834e에서 시작하고 시작 Git 상태/index·수정 전 SHA를 기록했다. 첫 source70323806cab8098afbdb739e813b600feb9d6a87의 Validate38013153642/job114097483337는 geometry/모델 검사PASS였으나 새 확대 캡처의 OpenScene이 임시 readback Texture2D를 unload하여 MissingReferenceException이 났다. Capture가 선택 단계여서 overall success여도 preview-result FAILED였으며 렌더 성공으로 기록하지 않았다. CityPreview를 같은 unsaved scene의 ResetModel/ApplyViews 동기화로 수정했다.

다음 source3f453bf1453f7a978a98835d6651a4f662daa1e3의 Validate38013490732/job114098566506는 PNG22장 생성까지PASS였다. 실제 이미지 관찰에서 후면 camera가 인도 canopy 안에 있어 녹색 잎에 가려졌음을 발견했다. black/pink 자동검사PASS를 시각검증으로 대체하지 않고 후면 camera x를-8.6→-1m 도로 안으로 옮겼다. 수정은 촬영용 unsaved Editor pose이며 앱 camera/geometry에는 영향을 주지 않는다.

최종 source **ed853b6d354fdffe75aac1f3a07684d27efd3674**, tag **unity-apk-0.8.0-build1**에서 mainValidate 및APK를 실제 재검사했다. 문서-only 최종 게시에는 빌드 입력이 같으므로 동일 검사를 반복하지 않는다. 이번 실제 geometry/Android 컴파일 실패는 없었고 위2캡처 문제를 해결했다.

### 실제 확인

| 검사 | 결과 |
| --- | --- |
| 최종 Unity | [Validate38013804973](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38013804973),job114099550164 success. 검사 후 원본 장면 재열기·임시 상태 제거 |
| 차량 새 geometry | 4종24대/49,080tri/14renderer씩,유한 vertex·unit normals/degenerate triangle 없음/UV·mesh 공유/crowned roof/tyre shoulder·contact PASS. checked vertices10,544/curved seam3,667 |
| 차량 기존 계약 | 600초 주행,15/30/60/120Hz oracle/차선·방향·unit scale/4wheel 회전·접지/순환·숨김 복귀·간격 PASS. 최소bumper gap46.754677m. Sedan2.22×1.49×4.752m, Sport2.28×1.28×4.652m,SUV2.30×1.91×5.072m,Taxi2.22×1.715×4.752m(폭×높이×길이),0.7.0과float 허용범위 일치 |
| 신호·보행·날씨 | 4/32/100명200/600/600초,223횡단/33신호주기/foot≥0.399999m/bumper≥1.799999m. 날씨4/32/100명각810초,퇴장92/복귀92/viewport 밖 active변경/횡단 유지/감속·인도·장애물·복귀 PASS |
| 시간·효과 | 180generic oracle,15/30/60/120Hz낮→노을2초→밤2초/밤→낮4초/retarget·반복·저장snap·freeze,18endpoints/100우산/96spray/32canopy/신문지3~5초 PASS |
| 모바일 geometry 예산 | 기본108,018tri/2163renderer/46material,맑음100명116,994/2231/46,100우산 **119,394/2331/46**,기존120000/2400/48제한 내.0.7 대비fleet 전체+480tri/renderer·material 증가 없음 |
| 실제 GPU | D3D11 Editor540×1200 PNG22장: 기존도시·diagnostic/6weather/전환0~4초/200초storm14 +모델4종front/rear8. 최종 source8확대/전체도시·야간·비·노을2초를 실제 관찰; APK 빌드도 previewPASS/22PNG. 폰 screenshot/FPS 증거 아님 |
| 실제 Android | [BuildApk38014126469](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38014126469),job114100541906 success; BuildPlayer errors0/warnings0,launcher Lint errors0/warnings8 |
| 변경 없는 host | NativeAndroid Java/res 및Bridge/AndroidWallpaperBuild/Atmosphere.shader SHA 불변. [host Lint38008378207](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38008378207)의 실제 errors0/warnings10/nativeFilesMatched8을 재사용하며 새host 검사를 했다고 기록하지 않음 |
| 내려받은 APK | Reports/APK ZIP digest·CRC/APK SHA·bytes,실제aapt0.8.0/code9/min29/target36/ARM64/SettingsActivity launcher/BIND_WALLPAPER/:wallpaper/meta/providerfalse/UnityActivitydisabled,원본 v2cert PASS |

launcher 경고8와 기존host 경고10은 유지한다. host 경고는Unity/Android 호환·ABI·의존성9 및기존 ApplicationContext StaticFieldLeak1이며 오류0을 경고0으로 바꾸지 않는다. 0.8.0 폰 측정은 미실행이다.

### 산출물·추적

- APK **`/workspace/artifacts/pixel-traffic-unity-prototype-0.8.0.apk`**, **29778264bytes**, SHA256 **`8bbb76e92b737eff0ff1f80382560cdab225c697a345528239bfae8b74367c1b`**.
- 원본 v2 cert SHA256 **`a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`**. PC DPAPI 설정을 재사용하며 키/토큰을 게시하지 않았다.
- 최종Validate artifact11654573087/10434160bytes/SHA`110d4c14386b79f952cf600b8e3b680245e7e4b3e3cf8d7a33e3ec177617310d`;Reports`/workspace/artifacts/unity-0.8.0-final-render-reports/`.
- APK artifact11655403920,build reports artifact11655643864;digest·bytes·로컬 ZIP 경로는`/workspace/artifacts/unity-0.8.0-source-manifest.json`에 기록했다. 실제 추출Reports`/workspace/artifacts/unity-prototype-0.8.0-reports/`,다운로드 검증`unity-prototype-0.8.0-download-verification.json`.
- 문서 저장/원격 source게시/태그/검증·APK 전달은 실제 각 단계 결과로 구분한다. 최종docs 커밋 및72입력 동일 확인은source manifest에 별도 기록한다. 실제 폰 사진을 생성했다고 기록하지 않는다.

### 집 PC 설치

APK를`C:\Users\김백현\Desktop\AI`에 다운로드하고 PowerShell에서 실행한다. SDK 경로는 이전 설치에 사용한 Unity6000.3.26f1의adb다.

```powershell
$unityAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $unityAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.8.0.apk"
if ($LASTEXITCODE -eq 0) {
    & $unityAdb shell am start -W -n "com.s20plus.pixeltraffic.unityprototype/com.s20plus.pixeltraffic.unitywallpaper.WallpaperSettingsActivity"
}
```

설정에서 적용한 뒤차체·휠/램프/유리,낮·노을·밤/비/우산·통행,저장 설정/숨김·재부팅 복원/FPS·발열을 폰에서 확인한다. 다음 후보는 도시 1층 상점·간판과 도로 생활 디테일이다. 다음geometry 추가 전현재100우산 조건에서추가가능한최대605tri/68renderer/1material(strict less-than 제한)을 고려해 mesh 합치기/atlas로 비용을 줄인다. 이번패치가새상점/브랜드차량을완료했다고기록하지않는다.

## 2026-10-10 — Unity 0.8.0 실제 확대 렌더 관찰·후면 camera 가림 보정

source3f453bf1453f7a978a98835d6651a4f662daa1e3의 Validate38013490732/job114098566506는 장면/렌더 모두 실제 PASS이며22PNG를 작성했다. artifact11655602445/8,781,715bytes/SHA b0711af6bffa9b4e9a04d3b07a20ffa069fa62b9b6c768486a0b1df21fab9d68를 내려받아CRC와result를 확인했다. 앞면 Sedan/SUV/Taxi에서 보닛 곡면의 빛/유리 gradient/휠·lamp를 관찰했다. 후면 확대 camera가 인도 canopy 안에 들어가 SportCoupe rear PNG가 녹색 잎에 가려져 있었으므로, 단순 black/pink 자동검사 PASS를 시각 검증 완료로 취급하지 않는다.

CityPreview 후면 camera의 x를-8.6→-1m 도로 안으로 옮겨 전·후면 camera 모두 canopy 밖으로 둔다. 실제 앱 camera와 geometry에는 영향이 없고 캡처용 unsaved pose만 변경한다. 다시 실제 PNG 확인 뒤APK를 빌드한다. 이전0.7 실제 폰 성공/0.8 geometry·예산 PASS와 누적 native dirty 보존은 그대로이다.

## 2026-10-10 — Unity 0.8.0 차량 검사 PASS·확대 캡처 텍스처 수명 수정

첫 source70323806cab8098afbdb739e813b600feb9d6a87의 Validate38013153642/job114097483337에서 장면/기존 교통·날씨·전환과 새 차량 geometry 검사는 모두 PASS였다. 차량49,080tri/14renderer씩,기본 도시108,018tri/2163renderer/46material,100우산119,394tri/2331renderer/46material로 기존 제한 내이며 모델별 metre dimensions가0.7.0과 일치한다. 첫 report ZIP11655256763/6,727,748bytes/SHA b00d666b0f672782b85873bd443c06d0bb936de006025b73ee8707319f20c826를 CRC 확인하여 실제 보고서를 읽었다.

GPU 캡처는 기존 도시/날씨/전환 PNG14장을 작성했지만, 새 확대 캡처 직전 EditorSceneManager.OpenScene이 임시 readback Texture2D를 unload하여 MissingReferenceException으로 실패했다. job의 Capture가 선택 단계여서 overall success여도 preview-result FAILED였으며 성공으로 취급하지 않았다. CityPreview의 해당 재열기를 제거하고 같은 unsaved scene의 Street.ResetModel/Advance(0)/ApplyViews로 맑음 상태만 동기화한다. runtime/build geometry는 문제 없고 캡처 장면은 APK에 저장하지 않는다. 최종 source의 실제 장면·GPU 검사를 다시 수행한 뒤 같은 source APK로 진행한다. APK/실기기 검증은 아직 미완료이다.

## 2026-10-10 — Unity 0.8.0 차량 외형 고도화 구현·실제 검사 진행 중

사용자가 0.7.0의 실제 폰 적용 성공을 확인하고 다음 단계를 승인했다. 기존 다음 추천인 차량 3D 외형을 진행한다. base01e70ec78bb5ce25154f4c499d33ef1a3c1a834e에서 차량 크기/차선 unit scale/24대·4모델과 날씨·신호·NativeAndroid를 유지한다. 0.8.0/code9,VehicleDetail-0.8.0.unity,Generated/VehicleDetail080을 사용한다.

- 범위7소스/meta: Editor VehicleGeometry/TrafficFleet/CityPreview/StarterScene/VehicleDetailChecks(.meta),Runtime StarterConfig. 지붕 crown·보닛 ridge, 위치를 움직이지 않는 threshold normal smoothing, 둥근 타이어 shoulder와 깊이 있는 tapered spoke, 보이는 fender lip, chamfered lamp silhouette를 적용한다. 기존 hidden spoke box 면을 줄여 모바일 예산을 확보한다. 기존14renderer/car,공유4mesh 세트/46material 구조를 유지하고 glass 공유64×64 tint/highlight texture와 UV를 추가한다. 고정 authored 유리 표현이며 realtime 반사 probe를 추가하지 않는다.
- 이전 차량48,600tri는 geometry formula로 계산한 값이다. 변경 후 예상49,080tri/도시100우산119,394tri는 아직 실제 측정 전 추정이다. 기존120,000tri/2400renderer/48material 제한을 올리지 않으며 실제 최악 조건 검사를 계속한다.
- 기존 차량 크기/방향/타이어 contact/회전/차선/600초 간격/15·30·60·120Hz,신호·보행/날씨·전환·100우산 검사를 유지한다. 새 mesh topology/유한 vertex·unit normal/UV·공유/roof crown·tyre shoulder 검사와 실제 GPU 차량4종 front/rear8PNG를 추가한다. 확대 camera/차량 pose는 unsaved Editor 검사 장면에만 적용하며 폰 camera는 유지한다.
- 시작 Git dirty/index와 수정 전 hash를 unity-0.8.0-start-state.json에 기록했다. native 누적 변경과 real index는 보존하고 임시 index의 명시7파일 및5진행문서만 게시한다. 변경 없는 기존 build 입력 SHA를 확인했다. 아직 이번 실제 Unity/GPU/Android/APK 결과 없음. 첫 main Validate 결과 후 실제 PNG를 관찰하고 같은 source의 APK를 빌드한다. 기존 host SHA가 같으면 실제 host Lint38008378207 결과를 재사용한다. 실기기 성능은 사용자 설치 후 확인이다.

## 2026-10-10 — Unity 0.7.0 노을 경유·날씨 반응 도시 APK 전달 완료 (KST)

사용자가 **0.6.0 실제 폰 적용 성공**을 확인했고, 낮→밤 직접 선택에 노을을 거치게 하면서 이전에 추천한 날씨별 통행량·우산/보행·차량 감속/물보라 패치를 승인했다. **0.7.0/code8**의 실제 PC 장면·GPU 렌더·Android 빌드/Lint 및 내려받은 기존 v2 서명 APK를 검증해 전달한다. 0.7.0 폰에서의 업데이트·저장 설정·표현·FPS/발열·숨김 복귀는 설치 후 사용자 확인이다.

### 구현과 범위

- **낮→노을 2초→밤 2초**의 총4초 active-time 전환을 추가한다. 밤→낮/나머지 선택은 기존4초 직행이다. 노을 경유 도중 새 선택은 현재 가중치에서 이어지고 같은 야간 선택 반복은 경유를 재시작하지 않는다. 두 구간 모두 취소/재선택, 저장된 야간으로 처음 시작하는 snap, 숨김 정지를 보존한다. 날씨 blend는 기존 독립4초 smoothstep이다.
- 기존 **차량24개/사람100개 pool**을 재사용한다. 날씨 가중치에 따라 인원 목표/차선당 차량 목표/속도를 연결한다. 비바람은 사람 설정 인원의30%(최소4명), 기본32→10명/100→30명, 차량 차선당3대·총12대, 주행 속도60%/걸음118%가 목표다. 약한 비/강한 비/눈/안개도 각각 인원/차량·속도 계수를 적용하며 차량수는 차선별 정수 반올림이다. 인원 UI의4~100명 값과 저장소를 덮어쓰지 않는다.
- 날씨로 퇴장할 때 보이는 사람은 인도 길을 걷고 횡단 중이면 인도까지 건넌다. **3×3.2×6m 보수적 카메라 bounds 밖**에서만 active를 끈다. 차량도 기존 순환도로를 유지하며 bounds 밖에서 빠진다. 복귀 사람은 인도 입구에서1.5초 간격, 차량은 순환도로 입구에서 앞뒤 간격을 확인한 뒤 다시 나타난다. 화면 안의 사람을 순간 이동시키지 않는다. 퇴장 거리에 따라 감소 시간이 걸리며 즉시 목표 수가 되는 것은 아니다. 맑아지면 남은 퇴장을 취소하고 서서히 복귀한다.
- 공유 **24tri 우산 mesh100개**를 추가한다. 비에 맞춰 펴지고, 올린 오른손에 shaft를 맞추고, 비바람에는 세계 바람 방향으로 기울인다.4종 tint/광택을 runtime MaterialPropertyBlock으로 연결한다. 빗길의 이동 차량에는 rear wheel2개/4billboard씩 **96quad 단일 mesh pool** 물보라를 사용하며 비 강도/차량 속도에 반응하고 정차/맑음에는 꺼진다. 기존 alpha lamp material/texture를 공유하고100개 새 material을 만들지 않는다.
- 범위 **14소스/meta**: Runtime SceneBlend/CityClimate/StreetModel/StreetSimulation/StarterConfig/WetTraffic(.meta), Editor StreetScene/ClimateScene/ClimateChecks/WeatherLifeChecks(.meta)/CityPreview/StarterScene. `WeatherLife-0.7.0.unity`/`Generated/WeatherLife070`, code8을 사용한다. 도시·차선 생성/VehicleGeometry/TrafficFleet/SidewalkRoutes/NativeAndroid/Surface·서비스·설정 저장소/appID·15/30FPS 목표를 유지한다. native0.48·누적 dirty/index는 보존했고 임시 index로 Unity 관련 범위만 게시한다.

### 과정과 결정

base **6d32fb9794ecf9b0b3dd24a365e33a83c1aa6e1b**에서 시작했다. 첫 sourceb8ac7743f8c88f74134a3893657be5888e48ec50의 실제 Validate38010587500/job114089410451와 렌더가 success였다. 코드 검토에서 car 앞뒤/우산을 덮는 viewport bounds 및 재진입 guard를 보강했다.100우산 예산 검사는 이전 날씨 퇴장 상태를 이어받지 않도록 새 모델을 준비하여 최악 조건으로 검사한다. 손 위치를 보정하고 실제 감속 상한도 검사했다. source2c1267216c7148b1425f7a77a8b1befe90077d4f의 Validate38010821738/job114090332600도 success였다.

첫 실제 PNG에서 우산 색이 동일하게 보였다. Editor 생성시의 MaterialPropertyBlock은 scene에 serialize되지 않으므로 runtime ApplyViews에서 색/광택을 복원하고 실제 로드된100개 renderer의 block을 확인했다. 전환 PNG에는 직전 storm preset의 우산이 남아 있어, Editor 캡처 시작시 Street.Advance(0)/ApplyViews로 맑음 우산·물보라·pose를 동기화했다. Runtime은 이미 매프레임 이를 수행하므로 캡처의 문제를 폰 버그라고 기록하지 않는다. 이 보정이 모두 포함된 최종 source **b5e74a16fe4548a68bdbc622a3990595388f85c6**, tag **unity-apk-0.7.0-build1**을 실제 재검사/빌드했다. 최종 문서 게시만 별도 수행하며70빌드 입력이 같으면 검사하지 않는다.

중간 pending source7ea0ab2 run38010708155와 sourceb22d655 run38011021957은 concurrency의 최신 pending 교체로 cancelled이며 컴파일 실패가 아니다. 이번 실제 컴파일/장면/빌드 실패는 없었다. 중간 source의 성공을 최종 APK의 결과로 대체하지 않았다.

### 실제 확인

| 검사 | 실제 결과 |
| --- | --- |
| 최종 Unity | [Validate38011110267](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38011110267), job114091262710 success; 원본 장면 재열기/임시 검사 상태 제거 포함 |
| 시간대·날씨 | 180 generic smoothstep oracle,15/30/60/120Hz 노을 양구간/밤→낮 직행/양구간 retarget·같은 선택·저장 night snap·숨김 freeze,18 actual endpoints PASS.32canopy 최소sway4.602755도/신문지8회·마지막 예약3.146284초 유지 |
| 새 날씨 교통 | 4/32/100명 각각810초(초기25초+비바람600초+맑음185초), 퇴장92/복귀92, viewport 안 active 변경 없음/횡단 유지/감속/인도·장애물·간격·복귀 PASS. 최소 foot0.4000002146m/bumper1.7999997139m |
| 실제 우산·물보라 | 실제100 umbrella renderer의 runtime tint/비 활성·맑음 비활성, 물보라96quad/주행 활성/정차·맑음 OFF PASS. 최대100우산118914triangles/2331renderers/46materials로120000/2400/48 제한 내 |
| 기존 교통·도시 | 기존4/32/100명200/600/600초,223횡단/33신호주기,foot0.3999990523m/bumper1.7999997139m PASS. 기본107538tri/2163renderers/46mats; 맑음100명116514/2231/46.12zebra/94차선 비중첩/24차량4모델·차체 크기 유지 |
| 실제 렌더 | D3D11 Editor540×1200 PNG14장(기본/diagnostic/6selected weather endpoints/전환0·1·2·3·4초/200초비바람) PASS. 최종 source와 APK 빌드 rain/노을·야간,4종 우산색/손 위치·기울기/안개·비·줄어든 통행을 관찰. 폰 screenshot/영상/FPS 증거 아님 |
| Android | [BuildApk38011593176](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38011593176), job114092600811 success. BuildPlayer errors0/warnings0, launcher Lint errors0/warnings8 |
| 변경 없는 host | NativeAndroid8 Java/res 및Bridge/AndroidWallpaperBuild/Atmosphere.shader가0.6.0과 SHA 동일. [host Lint38008378207](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38008378207)의 실제 errors0/warnings10/nativeFilesMatched8을 재사용하며 새 host 검사를 했다고 기록하지 않음 |
| 다운로드 APK | Reports/APK ZIP digest·CRC/APK hash·bytes, aapt0.7.0/code8/min29/target36/ARM64/SettingsActivity launcher/BIND_WALLPAPER/:wallpaper/service meta/providerfalse/UnityActivitydisabled, 원본 v2 cert PASS |

host 경고10은 기존 Unity/Android 호환·의존성/ABI9 및 ApplicationContext StaticFieldLeak1이다. launcher 경고8도 기록대로 남기며 오류0을 경고0으로 바꾸지 않는다. 실제 폰 성능은 여전히 설치 후 확인이다.

### 파일·추적

- APK **`/workspace/artifacts/pixel-traffic-unity-prototype-0.7.0.apk`**, **29746064 bytes**, SHA256 **`897b6da16efa34d10b22d42477c710afd4da8d10f855ffbaec33444dd5838e14`**.
- 기존 v2 certificate SHA256 **`a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`**. PC DPAPI 서명 설정을 재사용했고 키 백업을 다시 복원하거나 게시하지 않았다.
- 최종 Validate artifact11653167624/6495769bytes ZIP SHA256`ed0367fe83c149bcf9a0f83c9f294dc82f760ae1f9f9d562af9e33b384616ee7`; Build reports artifact11654086515/6529199bytes ZIP SHA256`a2455e49855a012005f80ccb8c31e161ca436e921936baa0d343c5f65d494f35`; APK artifact11653632134/28784210bytes ZIP SHA256`16dfe7443b8b41e7149e723096549bed832496c7aa22063f8c389a400f5cd4fe`. API digest/CRC 일치.
- 보고서 **`/workspace/artifacts/unity-prototype-0.7.0-reports/`**, night PNG SHA256`5b1ba84804646902261fe4fc351d463a29b65654784359691639776028b70fea`. 같은 artifacts의source-manifest70입력/download-verification JSON과 main Validate 보고서에 원본 근거를 남긴다. 클라우드 파일이 다른 인스턴스에도 남는다고 가정하지 않으며 GitHub 소스와 Actions artifacts7일 보존도 참조한다.

### 집 PC 설치·확인

APK를 **`C:\Users\김백현\Desktop\AI`**에 다운로드한 뒤 PowerShell에서 실행한다.

```powershell
$unityAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $unityAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.7.0.apk"
if ($LASTEXITCODE -eq 0) {
    & $unityAdb shell am start -W -n "com.s20plus.pixeltraffic.unityprototype/com.s20plus.pixeltraffic.unitywallpaper.WallpaperSettingsActivity"
}
```

필요하면 **배경화면 미리보기 및 적용**으로 시스템 화면에서 적용한다. 낮→밤을 선택하여 노을을 거치는지, 밤→낮이 바로 이어지는지 확인한다. 비/비바람에서 우산·물보라/감속과 서서히 줄어드는 통행량, 맑음 복귀, 중간 재선택/화면 OFF·복귀/기존 설정 유지와32/100명 FPS·발열을 폰에서 확인한다. 이 명령이나 시스템 적용을 클라우드에서 사용자 폰에 실행했다고 주장하지 않는다.

다음은 사용자 기기 피드백을 반영한 뒤 차량3D 외형/거리 디테일 고도화를 후보로 삼는다. 자동 시각·기상 API와 다음 외형 패치를 이번 완료 범위로 기록하지 않는다.

## 2026-10-10 — Unity 0.7.0 최종 Unity 장면·렌더 PASS / APK 빌드 시작

최종 sourceb5e74a16fe4548a68bdbc622a3990595388f85c6의 [Validate38011110267](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38011110267)/job114091262710 success. 180 generic smoothstep oracle cases와15/30/60/120Hz 노을2초+야간2초/밤→낮 직행/두 구간 retarget·저장 night snap,18 actual endpoints/숨김 freeze/32나무/신문지3~5초 PASS. 신규 weather model810초×4/32/100명: 퇴장92/복귀92,viewport 안 active 변경없음/횡단 유지/비바람 감속/foot0.4000002146m·bumper1.7999997139m PASS. 실제 우산100개·물보라96quad,정차/맑음 spray 꺼짐과runtime색block/100우산 예산118914tri·2331renderers·46materials PASS. 기존223횡단/33신호주기·foot0.3999990523m·기본107538tri/2163renderers/46mats·12zebra/94차선 비중첩 유지.

최종 D3D11 Editor540×1200 PNG14장 PASS. rain 이미지의4종 우산색과shaft손위치,2초노을/4초밤,맑음 전환에서 우산·spray 정리,200초비바람의 줄어든 통행량/기울어진 우산·비·안개를 관찰했다. 실제폰 screenshot/FPS검증은 아니다. artifact11653167624/6495769bytes ZIP SHA256ed0367fe83c149bcf9a0f83c9f294dc82f760ae1f9f9d562af9e33b384616ee7/CRC 확인; `/workspace/artifacts/unity-0.7.0-validate-reports/` 및source-manifest70빌드 입력 SHA에 근거가 있다. 같은 최종 source의unity-apk-0.7.0-build1 태그를 게시하여 BuildApk를 시작한다. 실제Android/Lint/원본v2서명·버전/다운로드 확인은 진행 중이다. Java/res/Bridge/AndroidWallpaperBuild/Shader 불변 SHA와0.6.0 hostLint38008378207의errors0/warnings10을 재사용하며 변경없는hostLint를 다시 실행하지 않는다.

중간source2c1267216c7148b1425f7a77a8b1befe90077d4f의Validate38010821738/job114090332600도success였다. source7ea0ab2 run38010708155와sourceb22d655 run38011021957은 실행대기 중 newer pending 교체로cancelled이며compile실패가아니다. 최종 source가다르므로중간검사를최종APK근거로대체하지않는다.

## 2026-10-10 — Unity 0.7.0 실제 전환 PNG 관찰·캡처 상태 동기화

첫 source의 transition-2s/4s 실제PNG를 관찰해2초 노을/4초 야간을 확인했다. 다만 Editor 캡처의 바로 앞 storm-night preset에서 열린 우산이, 모델을 고정하고 조명만 바꾸는 전환 PNG에 남아 있었다. 실제 runtime은 매프레임 StreetSimulation.ApplyViews를 수행하므로 런타임 결함이라고 기록하지 않는다. CityPreview에서 맑음 전환 시작 직전에 Street.Advance(0)/ApplyViews를 호출해 우산·물보라·걸음 pose를 실제 선택 상태에 동기화한 뒤 고정 교통 pose의5장 전환 캡처를 생성한다. 캡처의 날씨/표시를 일치시키는 관련 Editor 변경이며 최종 source의 장면·렌더 확인 후 같은 source APK를 빌드한다. 우산 runtime색 보정 sourceb22d65596104db92f4666faa175c6c3d89fa1bea 검사는 대기 중이다.

## 2026-10-10 — Unity 0.7.0 첫 실제 검사·렌더 PASS / 우산 색 보정

sourceb8ac7743f8c88f74134a3893657be5888e48ec50의 Validate38010587500/job114089410451 success 및 D3D11 Editor540×1200 실제PNG14장(기본/diagnostic/6 endpoints/전환0·1·2·3·4초/200초비바람)을 확인했다. artifact11652759860/6503654bytes ZIP SHA256f77957d0becb0f45881d0852a55ddd77b403bf068e62fb2b1ffecafd217120f8/CRC 일치. 실제 810초×4/32/100명 날씨 퇴장92/복귀92,foot0.3999992907m/bumper1.7999997139m,100개 우산+물보라 최대118914tri/2331renderers/46materials PASS. 기존 모델/차선/숨김·전환·바람 검사도 PASS다. viewport/우산 손 위치 보정 source2c1267216c7148b1425f7a77a8b1befe90077d4f 검사38010821738/job114090332600가 진행 중이며 최종결과로 첫 source를 재사용하지 않는다. 중간 source7ea0ab2의 pending38010708155는 concurrency의 newer pending 교체로 cancelled이며 compile실패가 아니다.

실제 rain PNG에서 우산이 모두 같은 색으로 보였다. Editor 생성시 MaterialPropertyBlock은 scene에 serialize되지 않으므로 runtime ApplyViews에서 공유 palette의4종 우산 tint/광택을 직접 설정하고 로드된 실제100개 renderer의 block 존재를 검사한다. 새 Material100개를 만들지 않고46개 공유 재질 예산을 유지한다. 실제 폰 캡처나 FPS검증은 아니다. 보정 후 source의 실제 렌더·Android 빌드/서명 검증을 완료해야 전달한다.

## 2026-10-10 — Unity 0.7.0 우산 예산 검사 준비·손 위치 보정 (검사 중)

실제 첫 검사38010587500는 진행 중이며 아직 성공/실패를 판정하지 않는다. 검토에서 우산100개 예산 검사가 바로 직전 비 상태에서 이미 퇴장한 사람의 pool 상태를 이어받을 수 있음을 확인했다. 실제 게임의 날씨 퇴장 조건을 무시하지 않고 이 예산 검사만 새 StreetModel을 준비한 뒤100명 모두 활성화해 최악 예산을 측정한다. 기존 별도810초×4/32/100 weather drain/recovery/viewport/collision 검사를 그대로 유지하고 비바람 감속도 실제 Car.speed≤cruise×0.6으로 확인한다. 우산 shaft의 시작을 올린 오른손 z위치0.32m로 맞춘다. 후속 source를 실제 PC 검사/렌더한 뒤 전달한다.

## 2026-10-10 — Unity 0.7.0 화면 밖 퇴장·재등장 경계 보강 (검사 중)

첫 sourceb8ac7743f8c88f74134a3893657be5888e48ec50의 실제 Validate38010587500/job114089410451가 진행 중이다. 검토 중 사람 중심 bounds를 자동차에도 사용하는 공통 판정이 차체 앞뒤를 충분히 덮지 않는 것을 확인했다. 자동차 길이5.1m와 우산을 모두 덮는 보수적인3×3.2×6m bounds로 StreetSimulation과 WeatherLifeChecks를 맞추고 차량 재진입도 같은 viewport 밖인지 확인한다. source 결과를 전달 전에 다시 검사한다. 기존 순환도로 wrap은 유지하고 날씨로 active를 끄는 시점은 실제 bounds가 viewport 밖일 때만이다. 우산은8면 canopy 양면16tri+shaft8tri=24tri 공유 mesh100개, 물보라는96quad/192tri의 단일 mesh이며 기존 Lamp Pool alpha 재질/texture를 공유한다.

## 2026-10-10 — Unity 0.7.0 노을 경유·날씨 반응 도시 시작 (진행 중)

사용자가 0.6.0 실제 폰 적용 성공을 확인했다. 낮→밤 직접 선택은 총4초(2초 낮→노을,2초 노을→밤)로 이어지고 밤→낮은 기존4초 직행을 유지한다. 현재 가중치 retarget/같은 선택/저장 상태 snap/숨김 active-time 정지를 보존한다. 이전에 제안한 날씨별 통행량·보행자 우산/빠른 걸음·차량 감속/물보라도 승인받았다. 차량24개·사람100개 pool을 유지하고 화면 밖에서만 날씨로 퇴장/재등장하며 횡단자는 인도까지 이동한다. 날씨 감소에는 퇴장 경로를 걷는 시간이 걸린다.

범위: Runtime SceneBlend/CityClimate/StreetModel/StreetSimulation/StarterConfig, Editor StreetScene/ClimateScene/ClimateChecks/CityPreview/StarterScene 및 관련 새 효과/검사 파일. 도시·차선/차량 원형 geometry·서비스/설정 저장 구조는 유지한다. 새 WeatherLife-0.7.0.unity/Generated/WeatherLife070과 code8을 사용한다. 원격 base6d32fb9794ecf9b0b3dd24a365e33a83c1aa6e1b와 누적 dirty/index를 보존하며 임시 index로 관련 source만 게시한다.

실제 PC Unity compile/장면·교통 안전/새 전환과 날씨 교통·우산100개 예산/실제 GPU 렌더, 같은 source BuildApk/launcher Lint/원본 서명·버전 확인이 필요하다. NativeAndroid source 불변이면 0.6.0 host Lint errors0/warnings10과 SHA 일치를 재사용한다. 검사·APK는 아직 실행/전달하지 않았으며 폰 성능은 사용자 확인이다.

## 2026-10-10 — Unity 0.6.0 시간대·날씨 4초 전환 APK 전달 (KST)

사용자가 **0.5.1 실제 폰 적용 성공**을 확인했고, 기존 앱처럼 날씨·시간대 변경을 부드럽게 연결하는 패치를 요청했다. **0.6.0/code7**의 실제 PC 검사/렌더와 Android 빌드/두 모듈 Lint/기존 서명을 확인해 전달한다. 이번0.6.0의 실제 폰 설치·설정 UI/저장/숨김 복귀·FPS/발열은 사용자 확인 대기다.

### 결과와 범위

- 설정 앱에 **낮/노을/야간**과 **맑음/약한 비/강한 비/눈/안개/비바람**을 추가한다. 시간대·날씨는 독립 **4초 active-time smoothstep**으로 색/밝기/안개/노면 젖음/창문·가로등·차량 조명/비·눈·바람 효과를 연결한다. 중간 재선택은 현재 가중치에서 시작하며 같은 선택은 진행을 재시작하지 않는다. 처음 실행은 저장된 상태로 snap하고 화면 꺼짐·숨김 중에는 전환/효과 시간도 멈춘다. 기존 native ThemeBlend/RoadWetness/CinematicScene/WindStorm의 계약을 확인해 옮겼다. 2D와3D의 픽셀 모양이 동일하다는 뜻은 아니다.
- 가까운 shadowless spot4개, 가로등 노면 pool20개, rain160/snow96 고정 mesh pool, canopy32개 흔들림, 신문지1장/3~5초 활성 시간 간격을 추가한다. gain0인 lamp pool은 draw를 끈다. 노면 wetness는 native값인 약한 비0.68/강한 비·비바람1/안개0.35/맑음·눈0을 사용한다. 비 줄기와 이동/바람 방향을 맞춘다.
- 수동 설정이며 실제 시각/기상 API와 날씨별 차량·인원 감소는 추가하지 않았다. 기존24대/4~100명(기본32)/절전15·기본30FPS 목표/신호·보행·장애물/기존 appID·v2 서명을 유지한다. FPS는 목표이며 실측 성능 보장이 아니다.
- 범위22소스/meta: Runtime SceneBlend/CityClimate/ClimateEffects/AndroidWallpaperBridge/StarterConfig/StreetSimulation(신호 emission1줄), Editor ClimateScene/ClimateChecks/StarterScene/CityPreview/CityEnvironment(shader 허용 검사1곳), Atmosphere.shader 및 meta, NativeAndroid WallpaperPreferences/WallpaperSettingsActivity/values XML. 새 Climate-0.6.0.unity/Generated/Climate060으로 이전 장면을 보존한다. CityEnvironment geometry는 byte-equivalent 구간이며 변경은 shader 허용 조건뿐이다. VehicleGeometry/TrafficFleet/StreetModel/SidewalkRoutes/서비스·Surface host 소스는 유지한다. dirty/index를 보존하고 임시 index로 필요한 파일만 게시했다.

### 과정과 보정

base **d1d64e74b85db701647d4678dfbfd95c3bfecadd**에서 구현했다. 첫 실제 검사에서 URP17.3 additionalLightsRenderingMode 읽기 전용 CS0200이 나와 SerializedObject 방식으로 수정했고, 다음에는 표시 이름 기반 재질 찾기 실패를 확정 asset 경로 연결로 고쳤다. 단일 나무의 시작/30초 끝 각도 비교가 주기/float 분해능 때문에 실패해, 전체32개를 매0.1초 관찰하는 더 강한 검사로 바꿨다. 바람 runtime을 무시하거나 검사 조건만 느슨하게 하지 않았다. 검사 임시 재질/mesh가 빌드에 남지 않도록 성공 후 저장된 원본 scene을 다시 연다.

실제 첫 렌더의 회색 사각 lamp pool/약한 window emission을 발견해 전달을 보류하고, 명시적 alpha blend/ZWriteOff/CullOff URP **Atmosphere.shader** 및 emission variant 유지/명시적 runtime keyword로 보정했다. 최종 PNG에서 부드러운 따뜻한 노면 빛·창문 발광, 비/안개/신문지를 관찰했다. 이전 실패/중간 source/run/artifact는 바로 아래 진행 이력에 남긴다. APK 추가 리소스 파서는 aapt의 spec declaration 대신 실제 bag을 조회하도록 보정해 시간대3/날씨6의 개수·순서·한국어 문자열을 확인했다.

최종 코드 **a29e9168e894cb120a9d94c659463227361f5419**, 태그 **unity-apk-0.6.0-build1**, host Lint 태그 **unity-host-lint-0.6.0-check1**. 최종 문서 게시만 별도로 수행하고, 동일한66빌드 입력을 유지하면 검사를 반복하지 않는다.

### 실제 확인

| 검사 | 결과와 근거 |
| --- | --- |
| 최종 PC 컴파일/장면/렌더 | [Validate38007422113](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38007422113), job114079822901 success. Shader/클린 scene 재열기 포함 |
| 기존 모델/횡단/장애물 | 차량24대/4model,4/32/100명,200/600/600초,223횡단/33신호주기; min foot0.3999990523m/bumper1.7999997139m PASS.12zebra/94차선 bounds 비중첩 유지 |
| 새 전환/효과 | 15/30/60/120Hz 180 독립 native oracle cases,18 scene endpoints/actual rain/snow 활성/retarget/invalid delta/frame cap/숨김 freeze PASS.32개 canopy 최소 측정 sway4.602755도, paper8회/마지막 예약 간격3.146284초 |
| 실제 장면 budget | 기본107346triangles/2162renderers/46materials;100명116322/2230/46. 추가 실시간 조명4개/그림자없음, material/geometry 제한 내. APK/메모리/FPS 프로파일은 별도 |
| 실제 GPU 화면 | D3D11 Editor540×1200 PNG11장(기본/diagnostic/selected endpoints6/전환0·2·4초) PASS. 최종 APK 빌드의 야간/비바람/노을도 관찰. 폰 screenshot·영상·FPS 측정 아님 |
| Android APK | [BuildApk38007659419](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38007659419), job114080698818 success. BuildPlayer errors0/warnings0, launcher Lint errors0/warnings8 |
| 이번 변경의 host Lint | [HostLint38008378207](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38008378207), job114082376858 success. 생성된 이번 Java/res8개 SHA 일치 후 unityLibrary Lint errors0/warnings10. 이전 host Lint 재사용 아님 |
| 다운로드 실제 APK | reports/APK/host ZIP digest/CRC,APK digest/bytes/v2 원본cert,aapt0.6.0/code7/min29/target36/ARM64,SettingsActivity launcher,BIND_WALLPAPER/:wallpaper/meta/providerfalse/UnityActivitydisabled, compiled 시간대3/날씨6 arrays PASS |

남은 host 경고10은 Unity/Android 호환·의존성/ARM64 범위9개와 ApplicationContext를 보관하는 기존 StaticFieldLeak1개다. UI 문자열을 resources로 옮겨 이전 SetTextI18n4개가 사라졌다. 경고를 숨기거나 오류0을 경고0으로 기록하지 않는다. 실제 폰에서 같은 설정 저장소 보존·구동/숨김 복귀와 열·전력을 확인한다.

### 파일·서명·추적

- APK **`/workspace/artifacts/pixel-traffic-unity-prototype-0.6.0.apk`**, **29725640 bytes**, SHA256 **`33f895e8a93c2d84b01f7cccf3478e826d9d6078a0b35c21d4a4a3f12a23f6de`**.
- 기존 certificate SHA256 **`a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`**, v2 PASS. PC DPAPI 서명을 재사용해 원본 키 백업을 다시 복원하지 않았다.
- 최종 Validate artifact11651564231/5050267bytes ZIP SHA256`a757ffb2fae0b75de946a0f8af24fb94626e0ffcf8f2462595bb3ede411a5fb4`. Build reports artifact11652750233/5078147bytes ZIP SHA256`caa49da255a2792abc79fc92df047150e7a8a2ccbb51accd8f41082bcff9ae00`; APK artifact11652650606/28761363bytes ZIP SHA256`8bc062b2031d237fab23c3575e8e7bd8b054e22b6400231fe4947933b1159202`; host artifact11651909484/1152bytes ZIP SHA256`fe9986040672053399b30e03ab3dc8a2e4d57b87df806ed361566c909bb1a629`.
- 최종 보고서 **`/workspace/artifacts/unity-prototype-0.6.0-reports/`**, 야간 PNG SHA256`4c1a61edc72920f768a64dc1f9601eaafa77ef9defa91991e7ba37aa45d4ceac`. 같은 artifacts 폴더의 source-manifest(66입력),download-verification,compiled-settings,preview-metrics JSON에 원본 근거를 기록한다. 특정 클라우드 파일이 새 환경에도 남는다고 가정하지 않고 원격 source와 Actions artifacts 보존기간7일을 함께 사용한다.

### 집 PC 설치·사용

APK를 **`C:\Users\김백현\Desktop\AI`**에 저장하고 PowerShell에서 실행한다.

```powershell
$unityAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $unityAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.6.0.apk"
if ($LASTEXITCODE -eq 0) {
    & $unityAdb shell am start -W -n "com.s20plus.pixeltraffic.unityprototype/com.s20plus.pixeltraffic.unitywallpaper.WallpaperSettingsActivity"
}
```

앱에서 시간대·날씨를 선택하고 **배경화면 미리보기 및 적용**을 눌러 시스템 화면에서 적용한다. 기존 적용 중이면 홈으로 돌아가 선택한 상태로 4초 전환되는지 확인한다. 전환 도중 다른 선택,맑음↔비바람,낮↔야간,화면 꺼짐/복귀와 인원·절전 저장을 사용자 폰에서 확인한다. 클라우드에서 사용자 폰 설치나 배경화면 자동 적용을 실행했다고 주장하지 않는다.

다음 후보는 실제 폰 피드백 후 비바람의 차량/보행 인원 감소와 더 자세한 노면 반사·창문 밝기 variation이다. 이 후속 기능을 이번 완료 범위에 포함하지 않는다.

## 2026-10-10 — Unity 0.6.0 최종 장면·렌더 PASS / APK 빌드 중 (KST)

최종 sourcea29e9168e894cb120a9d94c659463227361f5419, PCValidate38007422113/job114079822901 success. 180 native smoothstep oracle cases/18 actual endpoints/retarget/숨김 freeze/actual rain/snow renderer/32 canopy sway/paper3~5초 PASS. minimumCanopySwing4.602755도, paperLaunches8/lastInterval3.146284초. 기존차량24대/4model·4/32/100인원·횡단/장애물/100명 budget 검사를 통과했다. 기본107346triangles/2162renderers/46materials,100명116322/2230/46;12zebra/94차선 비중첩을 유지한다. 원본장면 재열기/임시 검사상태 제거도 통과했다.

실제 D3D11 Editor540×1200 PNG11장(기본/diagnostic/selected endpoints6/전환0·2·4초) PASS. sourceee3872f 보정 렌더에서 회색 사각 pool이 사라지고 따뜻한 창문/가로등·강한 비·안개·신문지가 보이는 것을 관찰했으며 finala29e9168 렌더를 추가 확인한다. 폰 screenshot/FPS 테스트는 아니다. Validate reports artifact11651564231/5050267bytes ZIP SHA256a757ffb2fae0b75de946a0f8af24fb94626e0ffcf8f2462595bb3ede411a5fb4/CRC를 확인했다. 같은 최종 source의 unity-apk-0.6.0-build1 태그 BuildApk38007659419/job114080698818가 진행 중이고 새 Java/res의 host Lint·실제 APK 서명/메타데이터는 완료 후 기록한다. 빌드 입력66개 SHA를 source-manifest에 기록했다.

## 2026-10-10 — Unity 0.6.0 효과 방향·낮 렌더 보완 (KST)

투명 shader sourceee3872f6ce2ad5532383a698d92630910aba0652의 실제 검사는 진행 중이다. ClimateEffects.cs만 보완해 lamp gain0일 때 20개 노면 pool renderer를 꺼 낮의 불필요한 alpha0 draw를 줄인다. 비 줄기의 위쪽 끝은 이동 속도 반대인 +x/+y로 두어 왼쪽 아래로 이동하는 비·왼쪽으로 날리는 신문지/나무 tilt와 방향을 맞춘다. 고정 pool 수·신문지 간격·blend 계약은 유지한다. 최종 source의 실제 Validate/렌더/Android 빌드를 확인하고 이전 시각 결과를 최종 결과로 재사용하지 않는다.

## 2026-10-10 — Unity 0.6.0 실제 렌더 관찰 후 투명·발광 재질 보정 (KST)

sourcee41a272cf6241728f5d8a24759296aa2d31bcc92의 PC Validate38006733953/job114077126272와 D3D11 540×1200 캡처가 success다. 실제 180 blend oracle/18 endpoint/32 canopy 측정 PASS, minimumCanopySwing4.602755도, paperLaunches8/lastInterval3.146284초. 기본107346 triangles/2162 renderers/46 materials,100명116322/2230/46. reports artifact11651143952/5029068bytes ZIP SHA256700c5f4940a83cc961ca43004d51c121a2c79723ca1861745eb4df849db7f35b/CRC를 확인했다.

실제 PNG를 관찰하니 가로등 pool이 사각 회색 패치로 보이고 창문 emission이 약해 시각 품질은 통과로 전달하지 않았다. 3종 투명 효과용 명시적 alpha blend/ZWriteOff/CullOff URP unlit Atmosphere.shader를 추가한다. Radial/Newsprint texture와 property block alpha/fog를 그대로 사용해 ShaderGUI의 Lit surface 상태에 의존하지 않는다. 소스 lamp/window/head/tail emission은 tiny non-black0.001을 넣어 black emission 자동 처리/variant stripping에 대응하고 runtime clones에 _EMISSION을 명시한다. Scene checks는 custom atmosphere shader와 runtime window keyword도 확인한다. CityEnvironment의 shader 허용 검사만 관련 범위에 추가하며 도시/차선 geometry는 변경하지 않는다. scene-clean sourcebf04e953773757afae0c11f7b12053652e67ff55 검사는 별도로 진행 중이며 이 보정 후 최종 실제 렌더/빌드 결과를 확인한다. 아직 APK를 전달하지 않는다.

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

## 현재 단계 — PC 연결 및 GitHub 반영 준비 (2026-10-09)

사용자 화면에서 runner2.337.0의 `Connected to GitHub`와 `Listening for Jobs`를 확인했습니다. 수정된 Unity 실행기의 로컬 Validate completed도 확인했습니다. 현재 소스/workflow를 원격 main에 반영하고 첫 Actions 검사를 확인하는 단계입니다. 이 문서 아래의 미검증/미등록 설명은 당시 준비 이력입니다.

workflow는 main의 pixel-traffic-unity 또는 workflow 파일이 바뀌면 Validate를 자동 실행합니다. 수동 Validate/BuildApk도 지원합니다. PR/다른 브랜치 코드를 PC에서 자동 실행하지 않습니다. 추가 라벨 pixel-traffic-unity가 실제 runner에 등록돼 있어야 하고 runner 창은 실행 중이어야 합니다. 실제 첫 Actions 결과 확인 전까지 원격 자동 검사 성공으로 기록하지 않습니다.

## 수정 실행기 PC 정상 완료 / GitHub runner 등록 단계 (2026-10-09)

사용자 Windows 재실행 화면에서 `Unity Validate running (PID …)` 후 `Unity Validate completed`와 prompt 복귀를 확인했습니다. 앞선 로그의 실제 장면 PASS/return code 0에 이어 수정 실행기의 프로세스 대기/종료 판정도 PC에서 동작했습니다. pipeline-result.json 원본/내용은 아직 받지 않았으므로 파일 내용까지 직접 확인했다고 기록하지 않습니다. 기존 성공 입력은 클라우드에서 반복 검사하지 않습니다.

GitHub app의 읽기 조회로 저장소 BACKHYUN96/S20-PLUS의 main/public/admin 권한을 확인했습니다. main의 `.github/workflows/pixel-traffic-unity.yml` 및 `pixel-traffic-unity/ProjectSettings/ProjectVersion.txt`는 각각 404로 미등록입니다. 따라서 아직 Actions workflow 실행을 안내하지 않고 PC runner 등록부터 진행합니다. 이번에는 branch/commit/PR/push/merge를 수행하지 않았습니다.

등록은 저장소 Settings → Actions → Runners → New self-hosted runner → Windows/X64입니다. GitHub가 표시한 Download/Configure 명령을 본인 PC에서 실행합니다. 추가 라벨은 `pixel-traffic-unity`, work folder는 기본 `_work`, 초기 서비스 등록은 N으로 사용자 세션에서 시작합니다. 마지막에 `.\run.cmd`로 실행하고 `Listening for Jobs`/GitHub Idle 상태를 확인합니다. 실제 등록 완료 화면을 받기 전까지 connected로 기록하지 않습니다. 등록 토큰은 본인 PC에서 직접 사용하고 성공 상태만 공유합니다.

이후 workflow와 Unity source를 GitHub에 반영하고 실제 main/Validate 작업 및 artifact를 확인해야 합니다. 현재 로컬 PC 검사 성공, GitHub source 게시, runner 연결, 원격 작업 성공, APK 생성은 서로 다른 단계입니다. APK/기기/WallpaperService 연결은 아직 미완료입니다.

## Windows 첫 batch 실행 — 빈 종료 코드 수정 (2026-10-09)

사용자 PowerShell에 `Unity Validate failed (exit )`가 표시됐습니다. run-unity.ps1이 `$LASTEXITCODE`를 빈 값으로 읽고 이를 0과 다르다고 판단한 것은 확인됐지만, 실제 Unity가 검사를 통과했는지/실패했는지는 사용자 PC의 `Reports/unity-Validate.log`를 받아야 확정할 수 있습니다. 직전 Unity 프로세스가 아직 import/검사를 진행 중일 수도 있어 로그 확인 전 무조건 재실행하지 않습니다.

수정은 GUI 프로그램 직접 호출 후 LASTEXITCODE를 읽는 부분을 ProcessStartInfo/Process.Start→WaitForExit→해당 Process.ExitCode로 바꾸는 것입니다. Windows PowerShell5.1은 공백/한글/quote/마지막 backslash를 보존하는 인자 문자열을 만들고 PowerShell7은 ArgumentList를 사용합니다. 같은 프로세스 종료를 기다려 pipeline의 서명 환경 복원/임시 키 삭제가 빌드보다 먼저 실행되지 않도록 합니다. Open 모드는 기존 동작을 유지합니다.

현재 PC 로그 확인 명령(Windows PowerShell):

```powershell
Get-Process Unity -ErrorAction SilentlyContinue | Select-Object Id, CPU
Get-Content -LiteralPath "D:\Unity\PixelTraffic-Automation\pixel-traffic-unity\Reports\unity-Validate.log" -Tail 60
```

첫 줄에 프로세스가 나오면 다른 Editor일 수도 있으므로 임의 종료하지 않습니다. 로그/활성 프로젝트를 대조한 뒤 해당 검사 프로세스 종료 또는 종료된 결과를 확인합니다. 수정 `run-unity.ps1`은 기존 automation 프로젝트의 tools에 같은 파일명으로 교체하고 기존 helper/pipeline을 유지합니다. 실제 종료 코드가 0이 아니면 그때 출력된 숫자와 Unity 로그의 원인을 확인합니다.

클라우드에는 PowerShell/Unity가 없어 수정의 Windows 실행은 미검증입니다. 신규 유료 서비스/runner 등록/원격 push/앱 코드 변경은 하지 않습니다.

현재는 설정 파일을 준비한 단계입니다. GitHub 커밋/푸시, PC runner 등록, 실제 batch/서명 APK 빌드는 아직 실행하지 않았습니다. Unity 화면의 FirstRoad 렌더/검사 PASS는 별도로 확인했습니다.

## 공통 구조

GitHub 코드 → `pixel-traffic-unity/tools/run-pipeline.ps1` → 기존 Unity Editor 메서드 → `Reports`/검증된 APK → GitHub Actions artifact 순서입니다. PC와 향후 Windows/Linux 서버 모두 같은 소스·버전·기존 인증서를 사용합니다. 실행 환경에 Unity6000.3.26f1·Android 모듈과 사용 가능한 라이선스가 있어야 합니다. Linux PowerShell은 별도로 준비합니다. macOS runner는 이번 지원 범위가 아닙니다.

Unity 앱은 아직 Activity 테스트 프로젝트입니다. 자동 빌드 설정을 마련했다고 WallpaperService 이전이나 Android 실행 검증을 완료한 것은 아닙니다. 정식 0.48 앱과 다른 앱 ID로 공존하며 기존 인증서가 다르면 빌드를 거부합니다.

## 네 PC에서 먼저 검사

Editor에서 작업을 저장한 뒤 Unity 창을 닫습니다. 프로젝트의 tools 폴더에 `run-unity.ps1`, `unity-editor.ps1`, `run-pipeline.ps1` 세 파일이 있어야 합니다. 새 배포 묶음은 기존 프로젝트와 합치지 않고 별도 폴더에 풉니다. 기존 프로젝트를 사용할 경우 tools 세 파일만 교체합니다.

```powershell
& "D:\Unity\PixelTraffic-Automation\pixel-traffic-unity\tools\run-pipeline.ps1" -Operation Validate
```

Unity를 못 찾으면 같은 명령에 `-UnityExe "실제 6000.3.26f1 Editor\Unity.exe 전체 경로"`를 전달합니다. Hub 실행 파일이 아닌 Editor입니다. 필요하면 해당 프로세스의 `UNITY_EDITOR_PATH`를 설정할 수 있습니다. 시스템 전체 PowerShell 실행 정책은 바꾸지 않습니다. 정책에 막히면 오류를 확인하고 현재 프로세스에 한정해 대응합니다.

새 체크아웃에는 생성 장면이 없으므로 Validate는 PrepareBatch를 통해 장면을 생성하고 검사합니다. 성공하면 Reports/scene-validation.json 및 pipeline-result.json, 실패하면 FAILED summary와 로컬 Reports/unity-Validate.log가 남습니다. 자동 캡처는 선택 후보이며 이번 도구에는 없습니다.

## GitHub에 PC 연결

1. `.github/workflows/pixel-traffic-unity.yml`과 Unity 소스는 main에 게시됐고 첫 PC Validate/보고서 공유가 성공했습니다. Actions → Pixel Traffic Unity에서 실행과 결과를 확인합니다. 아래 등록 단계는 새 PC를 연결할 때의 절차입니다.
2. [저장소 runner 설정](https://github.com/BACKHYUN96/S20-PLUS/settings/actions/runners)에서 New self-hosted runner → Windows → X64를 선택하고 GitHub가 표시하는 다운로드/등록 명령을 네 PC에서 실행합니다. 등록 토큰은 GitHub 화면에서 PC에 직접 사용합니다.
3. 등록 중 추가 라벨 `pixel-traffic-unity`를 붙입니다. 기본 self-hosted/Windows/X64와 함께 네 개가 필요합니다. 초기에는 Unity를 활성화한 동일 Windows 사용자로 runner의 run.cmd를 실행해둡니다. 서비스 계정 실행은 라이선스와 환경변수 접근을 별도로 확인한 뒤 사용합니다.
4. 저장소 Settings → Secrets and variables → Actions의 Variables에 `UNITY_EDITOR_PATH`를 실제 Editor 경로로 설정합니다. Android SDK를 따로 사용하는 경우 `UNITY_ANDROID_SDK_PATH`도 설정합니다.
5. Actions → Pixel Traffic Unity → Run workflow → main / Validate를 선택해 첫 실제 batch 결과를 확인합니다. main의 Unity 코드/workflow 변경은 자동 Validate이며, 수동 실행도 지원합니다. 다른 브랜치/PR 자동 실행은 없습니다. PC와 runner는 켜져 있어야 하며 한 번에 한 작업만 실행합니다.

결과는 해당 Actions 실행의 Artifacts에서 다운로드합니다. 로그·검사 요약을 담은 unity-reports와 서명/metadata 검증을 통과한 unity-activity-apk를 구분합니다. 원본 Editor 로그/키 파일/비밀번호는 artifact 목록에 넣지 않습니다. raw 로그는 runner의 작업 폴더에 남습니다. 이 채팅으로 이미지를 직접 보내는 연결은 아직 없습니다.

## 기존 서명으로 APK 빌드

PC의 runner가 읽을 수 있는 Git 체크아웃 밖 경로에 기존 keystore를 보관하고, runner 실행 전에 `PIXEL_TRAFFIC_KEYSTORE`를 그 경로로 설정합니다. 비밀번호는 본인 PC 또는 GitHub Actions Secrets에만 설정합니다. workflow에서 쓰는 Secrets는 `PIXEL_TRAFFIC_KEYSTORE_PASSWORD`, 선택 `PIXEL_TRAFFIC_KEY_PASSWORD`입니다. 별도의 key password가 없으면 store password를 사용합니다. alias는 기본 androiddebugkey이며 기존 alias를 바꾼 경우 runner 환경의 `PIXEL_TRAFFIC_KEY_ALIAS`를 설정합니다. ZIP에 키/암호를 넣지 않습니다.

```powershell
# 위 서명 환경을 준비한 후, Editor를 닫고 실행
& "D:\Unity\PixelTraffic-Automation\pixel-traffic-unity\tools\run-pipeline.ps1" -Operation BuildApk
```

Actions에서도 BuildApk를 선택할 수 있습니다. Unity가 빌드하기 전에 기존 인증서를 확인하며, 완료 후 SDK apksigner/aapt로 v2·인증서·app ID·0.1.0/code1/min29/target>=29/ARM64를 확인합니다. 실패하면 APK를 upload하지 않습니다. 이전 APK/검사 보고서는 현재 실행의 성공으로 오인하지 않도록 처리합니다. 원본 키 인증서 SHA256는 기존 0.48과 같은 `a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`입니다.

현재 APK를 실제 생성한 것은 아닙니다. 빌드 결과를 확인해 APK를 전달할 때는 당시 실제 파일·버전·서명 근거와 최신 Windows 폴더 설치 명령을 함께 제공합니다.

## 나중에 클라우드로 변경

- **자체 Windows/Linux 클라우드 runner:** 같은 Editor/Android 모듈/사용 가능한 라이선스/스크립트 실행 환경을 준비합니다. GitHub Actions Variable `PIXEL_TRAFFIC_RUNNER_LABELS`를 새 서버의 라벨 JSON 배열로 바꾸면 됩니다. 예: `["self-hosted","Linux","X64","pixel-traffic-cloud"]`. `PIXEL_TRAFFIC_RUNNER_SHELL`은 Linux에서 `pwsh`로 설정하고 `UNITY_EDITOR_PATH`와 필요시 SDK 경로도 바꿉니다. 호스팅 runner를 택할 때도 Unity 설치/활성화 단계를 별도로 추가해야 하며 라벨 변경만으로 Unity가 생기지는 않습니다.
- **클라우드의 원본 키:** 로컬 파일 경로 대신 Actions Secret `PIXEL_TRAFFIC_KEYSTORE_BASE64`를 사용할 수 있습니다. 이 값은 원본 키의 인코딩이며 암호화 대체가 아닙니다. Secret에만 넣고 코드/로그로 공유하지 않습니다. 공통 스크립트는 temp 파일로 복원하고 finally에서 지웁니다. 기존 인증서 검사는 유지합니다.
- **Unity Build Automation 서비스:** Git 저장소와 하위 폴더, 지원 Editor 버전, Android·서명 설정을 연결합니다. GitHub Actions 실행 설정 대신 서비스 설정과 장면 생성/build hook 어댑터가 필요합니다. 공통 Unity 소스·Editor 검사/빌드 메서드는 재사용할 수 있습니다. 서비스에서의 실제 버전 지원·요금·키 연결·결과 API는 당시 확인합니다.

실행 장소를 바꿔도 차량·맵/게임 로직을 다시 만들 필요는 없습니다. 새 환경에서 같은 버전/인증서로 빌드되는지 검증한 뒤 전환합니다. 완전 클라우드 서버/라이선스/결과 API 구성은 아직 미수행입니다.

## 이번 확인 범위

클라우드에서는 workflow의 수동/main 조건·runner 라벨·artifact 범위, ZIP의 실제 파일 일치와 소스 보존만 검사합니다. PowerShell/Unity/Android 빌드의 기능 검증은 PC에서 첫 실행한 결과가 필요합니다. 준비 완료와 연결/실행 완료를 구분합니다.
