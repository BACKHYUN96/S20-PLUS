# 새 채팅 인수인계 — 픽셀 트래픽 0.29.0

## 2026-10-11 — Unity 0.17.0 하늘·구름·해·달: 구현 완료, 실제 검사 대기

사용자가 폰0.16 적용 성공을 확인하고 다음 하늘 패치를 승인했다. 범위5소스: Distant.shader, DistantChecks, CityPreview, StarterScene, StarterConfig. 시작 시 기존dirty285파일·git status-uall·index SHA와110 빌드 입력/12 host 입력을 기록했다. 0.17.0/code18, 새 SkyClimate-0.17.0 장면과 SkyClimate0170 generated 경로. 기존8원경 mesh/공유 불투명 셰이더에서 둥근 구름 밀도/내부 음영/노을 가장자리, 해의 원형 disc와 halo/노을 하강, 달의 질감/halo·별 antialias와 구름 가림을 구현했다. 움직임은 기존 CityClimate visible clock을 사용한다. 기존 조명의 기후 연동을 재사용하며 storm dim→clear 복구의 실제 Light 검사120frames 및 두 세로 aspect 해/달 위치 검사를 추가했다. 임시 sky GPU 촬영5장을 추가하며 카메라와 기후는 복원하고 저장하지 않는다.

현재 로컬 입력 SHA·수정 범위·기존 index 보존을 확인했으며 Unity 실행/GPU/Android 빌드·서명/APK 전달은 아직 대기다. 낮→노을→밤/밤→낮·숨김 freeze/traffic·정류장 회귀 검사를 기존 pipeline에서 함께 수행한다. NativeAndroid/host/workflow/traffic 변경 없음; host 입력12개의 SHA가 같아 기존 실제 host Lint를 재사용한다. 폰0.17/FPS·발열은 미확인, Drive PC 구성은 보류한다.

## 2026-10-11 — Unity 0.16.0 강·도시·산 원경 보강: 실제 화면 검증·서명 APK 전달 완료

사용자는 폰 **0.15.0 적용 성공**을 확인했고 이번 배경 보강을 승인했다. **0.16.0/code17**, `RiverCity-0.16.0.unity` / `Generated/RiverCity0160`. 기존 원경 코드를 재사용해 산3겹의 높이와 봉우리를 분리하고, 도시34건물·첨탑·강변12저층군과 양안을 보강했다. 강은 upright sheet에서 깊이65m의 완만하게 상승하는 stylized surface로 바뀌고 다리 deck/기둥/난간·11램프가 수면 위에 보인다. 창문과 다리 불빛 아래 월드좌표 반사 ribbon, 노을 반짝임과 움직이는 잔물결, 비/눈/안개 흐림을 단일 공유 불투명 shader로 표현한다. 반사는 authored 효과이며 실제 reflection camera가 아니다. 원래 제품 카메라와 차량24대/보행100명/정류장8명/승하차·hazard·안전 출발 동작, NativeAndroid/host/workflow를 유지했다.

- **수정 범위:** Editor DistantScene/DistantChecks/CityEnvironment/CityPreview/StarterScene, Runtime StarterConfig, Shaders Distant.shader의7소스. 새 scene/generated 경로로 이전 장면을 덮어쓰지 않는다. distant 8mesh/1778tri, runtime material1개. 실시간 Light·reflection camera·collider 추가 없음. 최대100기존우산+8정류장 rig/우산 stress 119112tri/1580renderer/44material, 기존 strict120000/2400/48 이내.
- **실제 검증:** source `d431b0b0f25e09b77d393cac0c9738e736303175` / [Validate 38068045214](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38068045214) / reports artifact 11675947051. 강 깊이65m/다리 deck 수면 clearance 7.781m/11lamp/산층·finite geometry/shared material을 확인했고, 기존18기후·180blend·15/30/60/120Hz directional 낮→노을→밤·밤→낮·숨김 clock/retarget 검사를 통과했다. 실제24대600초 signal 11순환/차선변경 21/정류장 출발 14, rear/front 최소 gap 1.799999m. 승객 반복운행 후반 탑승 23회, 기존 모든 안전 검사 PASS.
- **GPU:** 실제 Unity6000.3.26f1/Direct3D11 71PNG. 원래 카메라의 낮/노을/밤/안개·비, 확대 원경/강 낮·밤·6초 ripple를 관찰했다. 추가 촬영의 카메라/기후 상태는 Editor 임시 변경이고 APK 장면에 저장하지 않는다. GPU PNG는 실제 폰 screenshot/FPS·발열 측정과 구분한다.
- **진행 중 오류와 해결:** 사용자 PC 강제 재부팅으로 runner 창이 닫혀 queued였으나 `D:\Unity\actions-runner\run.cmd` 재실행으로 작업을 받았다. 첫 Validate38065914574/attempt1은 Unity Personal 라이선스 entitlement0/exit198로 컴파일 전에 실패했고 artifact11674804394의 bytes/SHA/CRC/source를 확인했다. Hub 로그인/Personal 활성 상태를 사용자가 확인한 뒤 같은 source를 재검사했다. 후속 GPU에서 새 건물 색상 sine-hash의 보간 seed 때문에 낮 표면 잔점이 관찰되어, fwidth 창문 필터와 건물ID quantize/안정된 diffuse lerp로 보정했다. 최종 사진에서 다시 확인했다. 추가 시도와 실제 결과는 source manifest에 기록한다. 라이선스 문제를 코드 검사 PASS로 취급하지 않는다.
- **Android·서명:** 같은 source tag `unity-apk-0.16.0-build1` / [BuildApk 38068543923](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38068543923) / reports 11676148220 / APK 11675404034. BuildPlayer 오류0/경고0, launcher Lint 오류0/경고8. 다운로드한 APK의 기존v2 인증서 `a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`, version0.16.0/code17/min29/target36/ARM64 IL2CPP, settings launcher·exported BIND_WALLPAPER service·private provider·disabled Unity activity를 직접 검증했다. 변경 없는12 host 입력의 SHA를 비교해 실제 host Lint38008378207(오류0/경고10)을 재사용한다.
- **전달:** [APK 직접 다운로드](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/e635e0a1d513611a216f24526ed20e15ecef5b69/pixel-traffic-unity-prototype-0.16.0.apk) · [실제 Unity 전체 화면](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/e635e0a1d513611a216f24526ed20e15ecef5b69/river-city-preview.png) · [야간 전체 화면](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/e635e0a1d513611a216f24526ed20e15ecef5b69/river-night-preview.png) · [강·다리 확대](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/e635e0a1d513611a216f24526ed20e15ecef5b69/river-detail-preview.png). APK 30037656bytes/SHA256 `6dbf63e679817a5f51e20bd310897f8358c32949c70bcfd5a62afbb3828039ab`. 일회성 `downloads/unity-0.16.0` branch의APK1·PNG3파일을 로그인 없는HTTP200/redirect없음/전체bytes/SHA로 검증했다. main에binary/새Releaseworkflow를추가하지않는다.

PC 설치(다운로드한 실제 파일 위치를 사용):
```powershell
$trafficAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $trafficAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.16.0.apk"
```

**미확인/다음:** 폰0.16 적용 후 강·다리/낮·노을·밤/장시간 주행/FPS·발열 확인. 실제 설치는 사용자 PC에서 수행하며 여기서 폰에 설치했다고 주장하지 않는다.0.15는사용자적용성공확인이다. Drive는나중에PC설치후재개한다. 이전dirty285파일/index/status 및110 build/capture입력을보존했다. 최종docs-onlycommit은빌드입력이같으므로검사를반복하지않는다.

## 2026-10-11 — Unity 0.16.0 실제 화면 건물 색상의 잔점 보정

source2b221d8의Validate38067479489/job114257960606/artifact11675546625는Runtime와GPU71PNG PASS였다.26957271bytes/digest65fb7f8f3ebc602354d8b6e971ff5cb0d6139e17499cd4ed9b18721928122197/CRC/source를확인했다. 실제낮/밤/원경/안개사진에서window fwidth filter로밤subpixel창문은개선됐지만낮의벽전체에잔점이남았다. 새diffuse색상의sineHash에perspective-interpolatedseed를직접넣은것을원인으로판단해건물ID를34cell로quantize하고diffusehash를안정된seedlerp로교체한다. 창문은floorgrid와quantizedseed로계산한다. Shader만변경하고geometry/runtime/host는같다. 낮표면잔점을검증PASS만으로넘기지않으며최종GPU와같은sourceAPK를재검증한다. 아직APK0.16은미생성이다.

## 2026-10-11 — Unity 0.16.0 첫 실제 검증 통과·원경 창문 필터 보정

사용자가UnityHub로그인/Personal활성을확인한뒤같은source836a03c의Validate38065914574/attempt2/job114256052286은PASS했다. reports11675745377의26628723bytes/digest3a49b3d700adddd56cb71945024f9f977bdec126697be9d16d7b72eaf3e26e3a/CRC/source와GPU71PNG를확인했다. 원래카메라낮/밤·강낮/밤사진을관찰해다리/산층/밤수면반사는확인했으나건물옆면의hard-step창문grid가subpixel에서거칠게alias되어shader만보정한다. fwidth기반edge와area-average창문/점등/색상을거리에서섞고낮강base를더푸르게맞춘다. 실제1778원경tri/8mesh/11lamp/65mdepth/7.781mdeckclearance,최대119112tri/1580renderer/44material로기존strict예산안이다. runtime/geometry/NativeAndroid/host입력은변하지않으며최종sourceGPU·서명APK는아직대기다. 숨김active clock/directional전환/18기후/기존교통·승하차검사는첫실제source에서PASS였고최종source에서도pipeline을실행한다.

## 2026-10-11 — Unity 0.16.0 강·도시·산 원경 보강 진행 중

사용자는 폰0.15.0 적용 성공을 확인하고 시안의 강·다리·스카이라인·산 배경 보강을 승인했다. 기존0.11 원경 기반을 재사용해 산3층의 높이/봉우리를 분리하고,34개 건물과첨탑/12개 강변 저층군·양안, 실제 깊이65m를가진 stylized 강 표면과 물에잠기지않는 다리·11램프를 구현한다. 단일공유불투명shader/material과기존active clock을 유지하고 밤건물·교각램프 아래월드좌표 반사,노을 반짝임,비/눈/안개 흐림을 보강한다. 새실시간Light/반사카메라/물리collider를추가하지않는다. 기존차량/보행/정류장/서명/host/workflow는유지한다.

변경7파일: Editor DistantScene/DistantChecks/CityEnvironment/CityPreview/StarterScene,Runtime StarterConfig,Shaders Distant.shader. 별도RiverCity-0.16.0.unity/GeneratedRiverCity0160/code17. 초기geometry/runtime 검사와실제GPU사진71장,같은source의Android빌드/Lint/원본v2서명은 아직 대기다. 단순source검토를실제Unity/폰검증으로보고하지않는다. 최대100+8우산strict120000tri/2400renderer/48material예산을유지하며새물깊이/다리clearance/11lamp/산층검사와안개·강낮/밤/6초motion촬영4장을추가한다. 이전285파일/index/status와110입력기준을기록했다. Drive는PC설치후재개하며폰0.16/FPS·발열은추후사용자확인이다.

## 2026-10-10 — Unity 0.15.0 정류장·승하차·비상깜빡이: 실제 검증·서명 APK 전달 완료

사용자는 폰 **0.14.0 적용 성공**을 확인했다. 이번 **0.15.0/code16**은 양측 정류장 표지·쉼터·벤치, 별도 8명 승객 pool, 버스의 실제 우측 문 opening/공유 sliding panels를 추가한다. 장면 `BusStops-0.15.0.unity`, generated `BusStops0150`. 정차 후 .8초 문 열림→하차/순차 승차→최소8초 대기 및 이동 승객 완료→.8초 문 닫힘이다. **승하차와 문 닫힘 중 양쪽 비상깜빡이**를 고정30Hz의9tick ON/9tick OFF로 켠다. 하차 승객은 같은 정차 방문에서 즉시 재승차하지 않는다. 버스의 다음 방문과15초 cooldown을 지난 뒤 pool을 다음 승객으로 재사용한다. 승객은 기존 보행자/쉼터/벤치/기둥과 발 간격을 지키고 우산을 접고 탑승한다.

버스는 이미 바깥 차선 안에 정차하므로 앞뒤 안전간격 확인→왼쪽 방향 깜빡이 **3회/1.8초**→**6초 동안 현재 바깥 차선 중앙으로 복귀하며 가속**한다. 실제 다른 차선으로 변경할 때는 기존60m 범위 뒤차 모두 추월 대기·최종 앞뒤 gap·예약·새 뒤차 취소·긴 차량6초 변경 조건을 유지한다. 정류장은 횡단보도를 지난 위치다. 차체 전체가 실제 occupied crossing band(±2.5m)를 지나 추가2m rear여유를 확보한 버스는 빨간 신호 중에도 앞뒤 안전조건을 만족하면 현재 차선으로 출발해 뒤 차량의 횡단보도 clearance를 방해하지 않는다. upstream 차량의 정지선·보행 신호 요건은 유지한다.

- **실제 검사:** source `a4fc34b83f8c08f78da1b8af01826676f9b81fa9` / [Validate 38053719608](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38053719608) / artifact 11670702853. 600초4/32/100 보행자 서비스 boarding 60/alighting 48/departures 44, 뒤300초에도 양쪽 실제승차 23회로 반복 운행을 확인했고, hazard 25405frames, 최소 발 간격 0.400056m. 실제 serialized24대600초 신호11순환/차선변경21회, arrivals 14/departures 14/최소 차체 gap 1.799999m; 비바람 출발 2회, 15/30/60/120Hz와 pause PASS. 양측 보도 graph를 독립적으로 만들고 실제 장애물에 대한 모든 edge를 검사한다. 기존18기후/180blend/교통·보행·바퀴·회전차체 안전검사를 유지한다.
- **GPU·예산:** 실제 Unity6000.3.26f1/Direct3D11 **67PNG**(정류장 양쪽 하차/승차/밤 hazardON/OFF8장 포함)와 원래 폰 구도를 관찰했다. 차선변경 촬영은 실제 시뮬레이션 차량을 임시카메라로 따라가며 pose/camera 변경은 저장하지 않는다. 100기존 보행자/우산+8정류장 rig/우산 stress **118442tri/1579renderer/44material**, 기존 strict120000/2400/48 이내. 버스만19renderer(문2개), 다른 차량17, 추가 실시간 Light 없음. Editor 그림은 폰 스크린샷/FPS 측정과 구분한다.
- **실패와 해결:** 단일 버스 노선에서 pool이 소진되는 영구 같은버스 금지를 방문별 재승차 금지와 cooldown으로 고쳐 후반300초에도 양측 탑승을 확인했다. 첫 컴파일의 두 local 이름 충돌은 각 scope 이름을 분리했다. 비대칭 정류장에 양측이 공유하던 navigation을 분리해 서쪽 보행자 정체를 해결했다. 첫 확대GPU의 수관 가림은 임시 카메라를 수관 아래로 옮겼다. 실제24대 검사에서 forced inner-lane 출발과 downstream green 대기가 교착을 만들었으므로 실제 바깥 차선 복귀와 crossing clearance 조건으로 해결했다. 촬영마다 serialized 초기위치를 복원하고 실제 차량을 추적한다. 서쪽 정류장1m이동의100명 보행 회귀 때문에 원래 위치를 복원했다. RoadOccupied의2.5m 대역 밖 추가2m rear여유와 회전차체 envelope를 포함해 downstream 출발을 판정하며 신호 여러 순환과 실제 차선변경 진행도 확인했다. 실패 이력을 아래에 보존하며 최종 source에서 다시 검증했다.
- **Android·서명:** 같은 source의 tag `unity-apk-0.15.0-build1` / [BuildApk 38054287584](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38054287584) / reports artifact 11670319573, APK artifact 11670574374. BuildPlayer 오류0/경고0, launcher Lint 오류0/경고8. 다운로드한 APK의 v2 원본 인증서 `a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`, version0.15.0/code16/min29/target36/ARM64 IL2CPP, settings launcher·exported BIND_WALLPAPER service·private provider·disabled Unity activity를 직접 확인했다. 변경 없는12 NativeAndroid/host 입력 SHA를 비교해 실제 host Lint38008378207(오류0/경고10)을 재사용한다.
- **전달:** [APK 직접 다운로드](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/be2692cd9200f0fef7387d8e761961fa53a476d4/pixel-traffic-unity-prototype-0.15.0.apk) · [실제 Unity 전체 화면](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/be2692cd9200f0fef7387d8e761961fa53a476d4/bus-stops-preview.png) · [승차 확대](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/be2692cd9200f0fef7387d8e761961fa53a476d4/boarding-preview.png) · [야간 비상깜빡이](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/be2692cd9200f0fef7387d8e761961fa53a476d4/hazards-preview.png). APK 30018680bytes/SHA256 `8a1d2265a7a7ad88b7f94107d0cf3edffbafe60be694e1c2d6dcc35ae2366efb`. 일회성 `downloads/unity-0.15.0` branch의APK1개·실제PNG3개를 로그인 없는 HTTP200·redirect 없음·전체 bytes/SHA로 확인했다. main에는 binary/자동Release workflow를 추가하지 않았다.

PC 설치:
```powershell
adb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.15.0.apk"
```

**미확인/다음:** 폰0.15 설치 후 정류장 문/승하차·hazard·장시간 주행/FPS·발열·전체 배경화면 lifecycle 확인. Drive 구성은 나중에 PC에서 재개한다. source23파일/110 build·capture입력과기존dirty/index를보존했다. 문서-only최종커밋은위검사입력을바꾸지않으므로같은검사를반복하지않는다. 이후 승객 분포·정류장 전경 시인성은 사용자 실제 폰 확인에 따라 조정할 수 있다.

## 2026-10-10 — Unity 0.15.0 최종 Runtime 통과·비상깜빡이 촬영 프레이밍

source264fe94429cfca66bdfcb745589b0277c3c225dd / Validate38053099084(job114216046151)/artifact11670472080의25,306,315bytes/digest/CRC/source를확인했다. 전체Runtime와GPU67PNG PASS: 실제24대600초11signalcycles/21lanechanges/14arrivals14departures/minGap1.799999m, 독립4/32/100명 boarding60/alighting48/departures44/뒤300초 양쪽탑승23/hazard25405frames/minFoot.400056m,storm2/4fps/pause/최대118442tri1579renderer44material. 기존모든사람횡단조건도PASS다. 실제8정류장 확대와production전체사진을관찰해문opening/승객/쉼터시인성은확인했다. 그러나hazard ON/OFF 사진은버스앞램프가왼쪽밖으로잘려시각확인에부적합했다. runtime의양쪽MPB검사는PASS이고제품구도는유지하며, hazard확대2단계만버스전면앞에서비춰램프를포함한다. Editor촬영1파일만변경하며새GPU와같은source서명APK를재검증한다.

## 2026-10-10 — Unity 0.15.0 보행 회귀 보정·실제 횡단보도 clearance 기준

sourcefda16f92837149a7338a5a19195ab58ff3fc6115 / Validate38052325990(job114214725982)/artifact11670326172는기존StreetChecks100명600초의person23이횡단하지못하는회귀로실패했다. 정류장1m이동이보도 graph와군중경로를바꿨으므로기존 west doorZ=-4.5를복원한다. 보행검사의모든사람횡단/600초/foot/경로요건은완화하지않는다. 실제RoadOccupied는crossing±2.5m+SafetyLength/2이며CommittedApproach는4m+SafetyLength/2다. 이미그구간을완전히지난정류장출발은occupied band밖 추가2mrear여유(4.5m+SafetyLength/2)를확인한다. 원래서쪽정차점은rear clearance약4.94m로이기준보다.3m이상여유가있고, 별도모든busdefinition·red출발·600초signalCycles>=2검사를유지한다. upstream신호/차체보정/앞뒤gap은불변이다. 이는이전5m추가buffer로발생한downstream교착을실제안전대역과맞춰보정한것이며, 문닫힘/3pulse/6초현재차선복귀를유지한다. 반복승객visit/cooldown보정도함께최종재검증한다. 아직최종GPU/서명APK는대기다.

## 2026-10-10 — Unity 0.15.0 반복 운행의 승객 pool 재사용 보정

600초 서비스 수치를 검토하니 같은버스 영구재승차 금지가 단일버스노선의 pool을 소진시킬 수 있었다(boarding18/alighting24/departures43). 하차 직후 같은 정차 방문에서 다시타는 것은 금지하되, 버스가한바퀴돌아새로방문하고15초cooldown이지났다면 pool을다음승객으로재사용하도록 lastAlightArrival/busArrivalTick를 추가한다. 새 rig/우산/재질은 늘리지 않는다. 4/32/100명 각각600초 검사에서 뒤300초에도양쪽정류장에실제탑승완료가있는지assert하고lateBoardings를기록한다. 같은방문재승차/15초 cooldown/foot/curb/open-door/정지/3pulse/6초/clock/pause 요건을 유지한다. 최종Runtime/GPU/서명APK는 재검증대기이며 초기방문PASS만으로영구운행승차를주장하지않는다.

## 2026-10-10 — Unity 0.15.0 서쪽 정류장 실제 차체 clearance 여유 보강

실제 버스 safetyLength에는8°회전 envelope가 포함된다. 서쪽 기존 centerZ=-.425는 crossingZ10에서10.425m지난위치로, 약10.49m인5m+SafetyLength/2 출발조건보다조금짧았다. 동쪽은충분히떨어져있으나서쪽버스가빨간신호에서Ready로정차한채뒤차의교차로비우기를막을수있다. west doorZ를-4.5에서-5.5로1m앞으로옮겨centerZ=-1.425와추가.3m이상의검증된여유를확보한다. 신호/차체/foot gap기준을낮추지않으며실제모든busdefinition에서stopclearance를검사한다. serialized24대600초검사에signalCycles>=2와실제완료laneChanges측정도추가해서비스횟수만으로장기교통정체를놓치지않는다. 이전Runtime횟수PASS는장기signal진행전체검증과동일하지않음을정정하며최종Runtime/GPU/서명APK는재검증대기다.

## 2026-10-10 — Unity 0.15.0 촬영 ResetModel 시작 위치 보정

source45e01d0bf7f55f552ec87d6d78316ceed707f212 / Validate38051670312(job114211889567)/artifact11670100882에서 전체 Runtime 검사는 다시 PASS였지만 GPU55PNG후 실제 차선변경 촬영은 계속 실패했다. 촬영 originalZ가 CaptureBatch에서 이미30초 진행한 위치를 사용하면서 ResetModel의 승하차/신호상태만 초기화되던 부분을 발견했다. serialized장면의 차량 위치를 첫 Advance전에 저장하고 각 독립 촬영마다 그 위치로 복원한다. 실제maneuver Signaling/Merging ticks·cycles/정류장 진행을 실패진단에 추가한다. runtime/productioncamera/geometry는불변, 최종GPU/APK는재검증대기다. 앞의viewport 추정만으로문제가해결되지않았음을명시한다.

## 2026-10-10 — Unity 0.15.0 전체 승하차 검사 통과·실제 차선변경 추적 촬영 보정

source9bd8f58b7d9df6aa2a001e974531145f577d1def / Validate38050819623(job114209413052)/artifact11669780165에서 Runtime PASS: 실제24대600초 arrivals3/departures2/minimumGap1.799999m, 독립4/32/100명 boarding18/alighting24/departures43/hazard26233frames/minfoot.400042m/stormdepartures2, 15/30/60/120Hz/pause와118442tri/1579renderer/44material 예산을 확인했다. GPU촬영은55PNG이후 기존폰구도 viewport안에서차선변경차량을못찾아실패했으므로 성공으로간주하지않는다. 실제시뮬레이션의Signaling→Merging→완료상태는그대로사용하고임시촬영카메라가해당차량을따라가게한다. Runtime/productioncamera/차선변경안전요건은불변이며촬영/최종APK는재검증대기다.

## 2026-10-10 — Unity 0.15.0 횡단보도 이후 정류장 신호 교착 보정

source55ae3daa4e92077aba2e241ffa0655f11df3788b / Validate38050278331(job114207848807)/artifact11668294656의 실제24차량600초 검사에서arrivals3/boarded6/departures1을 확인했다. 횡단보도를 이미 차체전체가 지난 정류장 버스가green만 기다리면 뒤 차량이횡단보도를비우지못해VehicleClearance가끝나지않는경우를보정한다. 문닫힘/현재차선앞뒤gap/3pulse는유지하고, 빨간신호때도차체전체가crossing+5m안전범위를이미지났다면현재바깥차선으로출발한다. 신호가아직적용되는upstream차량은green을기다리며기존정지선/횡단보도/보행clearance는그대로사용한다. 전체fleet검사는red시departure의실제crossingclearance와실패시busstate를함께검사한다. 최종Runtime/GPU/서명APK는재검증대기, 폰0.14성공/Drive보류.

## 2026-10-10 — Unity 0.15.0 실제 전체 교통 교착 보정: 바깥 차선에서 정류장 출발

sourcedf73618c5a79f6bb0ac1e9f9fdfaa64c5f0fd0a7 / Validate38049517205(job114205646178)/artifact11669690043은추가한serialized24차량600초검사에서arrivals1/boarded2/departures0으로실패했다. 버스가이미바깥차선내(x±4.92)에정차하는구조에서출발마다안쪽차선전체합류를요구하면혼잡때계속대기하게된다. 정류장출발은현재차선앞/뒤gap과green을확인하고문닫힘→왼쪽방향3pulse/1.8초→6초에걸쳐바깥차선중앙(x±4.8)으로복귀하며가속한다. Boarding/Closing은양쪽hazard,Ready/DepartureSignal은stationary,Leaving은door0과following solver를유지한다. 다른차선으로실제로변경할때는기존60m뒤차모두통과/앞뒤예측gap/예약/새뒤차취소/3pulse/heavy6초/일반기후제한을그대로사용한다.

하차한승객이같은버스에즉시다시타지않도록lastAlightOwner와15초재승차대기를추가했다. 독립서비스/비바람/실제24차량600초·정류장출발3pulse/6초복귀/foot/curb/crossing/clock/pause와새나무아래확대GPU를재검증한다. 아직최종Runtime/GPU/서명APK는대기이며폰0.14성공/Drive보류와기존dirty/index보존을유지한다.

## 2026-10-10 — Unity 0.15.0 첫 실제 검사·GPU67 통과, 정류장 촬영 위치와 전체 교통 검사 보강

source913227f9b3bd45cf6fa124e3eedb64f628bffd2a / Validate38049032287(job114204258172)/artifact11669145181의25,016,442bytes/digestcfd6ac9ed218c89d857e14a59962b7f18eaef7e62f6e4f741a7047dcc27c5b25/ZIP CRC/source를확인했다. 실제4/32/100명600초서비스: boarding12/alighting6/departure6/hazard1590frames/minfoot.400352m,storm departure1,15/30/60/120Hz/pause·기존기후/교통/바퀴/보행검사PASS. 실제100기존우산+정류장8rig/8우산stress118442tri/1579renderer/44material,기본weathermax6정류장활성상태118130/1575/44로strict120000/2400/48예산을유지한다.

실제GPU67PNG파일은정상저장됐지만west boarding 확대를관찰하니나무수관이촬영카메라를가려정류장이보이지않았다. production폰camera는유지하고임시stop줌을나무아래2.25m/보도안쪽x±7.5/68°FOV로옮긴다. 추가로실제serialized24대전체fleet의600초door/queue/reservation/pedestrian-green검사와departure진행을확인해대형차정류장대기와전체교통의교착이없는지검증한다. Runtime와geometry입력은불변이며Editor검사와촬영2파일만보강했다. 같은최종source실제Validate/GPU후서명APK를빌드한다. 아직APK/공개download/폰0.15검증은하지않았다.

## 2026-10-10 — Unity 0.15.0 경로 검증 변수명 보정·비바람 출발/최대 승객 예산 추가

source38bc2951102725c8d132b918db3d30e30014db6b / Validate38048846997(job114203726577)/artifact11668109208은새경로검사의local z와기존pose배열z가C#scope상중복되어CS0136으로실패했다. route변수를stopZ로분리하여양쪽navigation재검증을진행한다. 보강검사에서storm pace에서도승하차완료버스가출발하는지확인하고, 실제100보행자+8정류장승객/우산을모두활성화한strict120000tri/2400renderer/48material예산을측정한다. 정류장우산도기존windblend기울기를따른다. renderer통계는승용차/트럭17·버스19를각각명시하고기존Heavyreport의완전대칭설명을오른쪽문opening이있는metrebody로정정한다. 실제Runtime/GPU/서명APK는재검증대기이며성공주장하지않는다.

## 2026-10-10 — Unity 0.15.0 실제 보행 검사 실패: 양쪽 보도 경로 분리

sourceb7273c4db99f24049ebd0ce1babcbdf0776edccc / Validate38048620357(job114203088402)/artifact11668014038은Unity컴파일과traffic/vehicle mesh 검사를통과했지만StreetChecks4명/200초의보행자0이서쪽정류장옆에서교차로로가도록예약한뒤멈춰횡단하지못했다. 기존SidewalkRoutes는+side의obstacle graph를양쪽에공유했으나새정류장두곳의z위치가다르다. ±side각각의open/link cache를만들고Nearest/Find/Place/Roam/weather-reentry에actualside를사용한다. 실제양쪽쉼터주변 경로의모든edge를SegmentAllowed로확인하는독립검사도추가한다. 기존교차/간격/모든보행자횡단요건을그대로유지하고재검증한다. GPU/서명APK는아직대기이며폰0.14성공/Drive보류와기존dirty/index를보존한다.

## 2026-10-10 — Unity 0.15.0 실제 첫 컴파일 오류 보정

source1cf232604f465391174bb44a5f3e5d7b3f0e293e / Validate38048497084(job114202732886), artifact11668108617: 실제Unity컴파일에서CityPreview의기존labels와정류장촬영labels가중복되어CS0136으로실패했다. 정류장변수를stopLabels로변경한다. Runtime검사/GPU/APK는아직실행되지않았다. 통합검토에서하차끝→Returning전환tick에승객이두번걷는경로도발견해같은tick의velocity가이미있으면Returning을다음tick부터진행한다. SidewalkOpen의짧은배열생성을제거해내비게이션반복검사의allocation을줄였다. 실제재검증대기이며기존안전검사와허용오차를유지한다. 0.14폰적용성공/Drive보류를유지한다.

## 2026-10-10 — Unity 0.15.0 정류장·승하차·비상깜빡이 구현, 실제 검사 대기

사용자가 폰0.14.0 적용 성공을 확인하고 정류장 표지/쉼터/벤치·대기 승객·정차/문/승하차·뒤차 안전거리·출발3회깜빡이/안전합류를 승인했다. 추가로 승하차 중 양쪽 비상깜빡이를 요청했다. 0.15.0/code16/BusStops-0.15.0.unity/Generated/BusStops0150로 별도 장면을 생성한다. 버스문 실제 오른쪽 skin opening·공유 sliding panels2개/19renderer(bus only), 양측 보도 정류장2곳과 shared 8승객 rig/umbrella를 추가한다. 정류장은 횡단보도 후방(doorZ -4.5/34.5)에 놓고 tree/bench/post 통로를 함께 피한다. 버스가 완전 정차한 뒤 .8초에 문을 열고 하차 후 순차승차, 최소8초 dwell와 transfer완료를 기다린다. Boarding/Closing은 양쪽hazard9on/9off ticks, 문 닫힘 후 Ready는 hazard off→뒤60m대상 모두 통과·앞뒤gap·green확인→3pulse/1.8초→6초inner lane 합류다. 새뒤차/신호취소/예약/보행안전요건을 유지한다. weather pace가 낮아도 정류장에서 무기한 묶이지 않도록 정류장출발에 한해 pace 제한을 분리한다.

BusStops fixed30Hz가 문·승객·정차/출발을 소유하며 걷는 승객과 기존People의foot discs를 함께 회피한다. 보행정원4/32/100은 기존People이며 별도대중교통8pool을 사용한다. 줄여도 기존버스/날씨・차선검사를 재사용하고 추가600초서비스·문/MPB양쪽hazard/3pulse/뒤차/rotatedcorner/curb/crossing/foot간격·15/30/60/120Hz와pause검사를 작성했다. 실제transfer와hazard-on/off낮밤8확대GPU를 추가해총67PNG촬영할 계획이다. 이 시점에는 실제Unity compile/check/GPU/AndroidBuild/Lint/서명APK/공개download를 아직 실행하지 않아 통과를 주장하지 않는다. 다음은 Windows runner 검증과 결과 기반보정, 같은최종source서명APK/다운로드와폰확인이다. 기존사용자dirty/index·native source와12unchangedhost입력·Drive보류를 보존한다.

## 2026-10-10 — Unity 0.14.0 버스·박스 트럭: 실제 검증·서명 APK 전달 완료

사용자가 **0.13.0 실제 폰 적용 성공**을 확인하고 다음 패치를 승인했다. **0.14.0/code15**, `HeavyTraffic-0.14.0.unity`/`Generated/HeavyTraffic0140`, `pixel-traffic-unity-prototype-0.14.0.apk`를 전달한다. 기존 시안 카메라·원경/건물/가로수·시간/날씨/노을 경유·보행/신호·설정과 라이브 배경화면 host를 유지한다.

- **모델·배치:** 차량 24대/차선당6대 중 파란 CityBus **4대**, 흰 BoxTruck **4대**, 기존 승용차16대를 섞는다. 6종 공유 메시·기존17 renderer/4 wheel pivots·64×64 opaque glass·기존 램프/road beam을 사용하며 공유 흰 재질1개를 추가한다. 버스는 대칭10.6m 차체·옆창/승차문·앞뒤창/그릴/거울·옥상 AC, 트럭은7.4m cab/cargo·뒤 중앙문 틈·실제2 locking bars/6 hinges·범퍼/미러를 만든다. 전경 차량을 늘리기 위해 외측 차선에 긴 차량6대, 내측에2대를 배치했다. 본체는±X대칭이며 차선별 scale1/실제 폭·높이·길이와 앞뒤 lamp 좌표를 검사했다.
- **주행 안전:** 실제 치수에8° 회전을 덮는 고정 길이 envelope를 추가하여 앞뒤 following/stop/횡단보도 clearance/날씨 재진입/차선 예약 간격을 계산한다. 승용차3초, 긴 차량6초 smooth merge와 깜빡임·차선변경 시간을 포함한 상대속도별 예측 여유를 적용한다. 기존3pulse/1.8초 깜빡이와60m 관측 범위의 뒤차 모두 추월 대기, 새 뒤차 취소/마지막 gap 검사를 유지한다. 저속에서 회전한 차체의 모서리도 중앙선/보도 안에 머물도록 yaw를 제한한다. 차량 전체 bounds로 날씨 퇴장/재진입 visibility를 판정하며 보행 visibility와 분리한다.
- **실제 검사:** 600초 실제 mixed fleet에서 merge starts14/완료14/긴차량 starts5, projected bumper 최소2.1090m, 느린 버스·트럭/양방향 sweep2896개를 검사했다. 버스가 뒤의 트럭·버스 모두를 보낸 뒤3번 깜빡이고6초 merge 완료하는 사례도 통과했다. 기존18기후·15/30/60/120Hz·600초 교통/보행·810초 날씨 drain/recovery·100우산·wind/숨김·실제 light4개 검사 PASS. 각 표본의 원본 수치는 scene-validation.json에 있다.
- **실패와 보정:** 첫 source9f4d0d4/Validate38045876827은 StreetChecks의90초 프레임속도 비교에서 실패했다. 이전 wheel steer/roll이 ResetPosition 후 남아 BodyBounds/폭 envelope에 영향을 줄 수 있어 logical roll과 실제 wheel.localRotation을 함께 초기화했다. reset wheel pose 검사도 추가하고 기존 frame-rate 허용오차를 유지했다. 두 번째 Validate38046156301에서는 프레임 비교가 통과했지만 뒤차 추월 사례가 실패했다. production 신호/안전요건을 유지하며 검사 요청버스4.8m/s·뒤차10m/s로 속도 차가 충분한 사례를 구성하고 실제 stage/mask 진단을 넣었다. 최종 [38046520813](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38046520813)에서 재검증했다.
- **GPU·예산:** 실제 Unity 6000.3.26f1/Direct3D11 **59PNG**(기존51+heavy 앞/뒤 낮·밤8). 전체 화면과 두 모델의 앞뒤·야간 램프/브레이크등을 관찰했다. 임시 pose/camera/숨김은 저장하지 않아 APK 장면은 production fleet/camera를 사용한다. 이전 Effects.Advance(0) 촬영 보정을 유지했다. 실제 기본 105266tri/1383renderer/44material, 최대100우산 **116642tri/1551renderer/44material**로 기존 <120000/<2400/<48 예산을 지켰다. Editor GPU 결과는 폰 FPS나 발열 측정과 구분한다.
- **APK 검증:** 동일 source `4f6a1122650ff0b240e7d0e2d7df4adc917d5178`/tag `unity-apk-0.14.0-build1`의 [38046875651](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38046875651)에서 실제 Android BuildPlayer 오류0/경고0, launcher Lint 오류0/경고8. 102입력 SHA와12 unchanged NativeAndroid/Bridge/build host 입력을 대조하여 기존 host Lint38008378207(오류0/경고10)을 재사용했다. 다운로드 artifact digest/bytes/CRC/source, 실제 APK v2 원본 cert `a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`/version0.14.0/code15/min29/target36/ARM64/설정 launcher/별도 wallpaper process·BIND_WALLPAPER·private provider·metadata와 비활성 Unity Activity를 검증했다. **29998135bytes**, SHA256 `4c24ee44c09750378b23cc8e05720c334b756da29c8f22c5953369ce2494509a`.
- **전달·보존:** [모바일 직접 APK](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/08fed474120a21f8908dc451a57388e4f7d7b586/pixel-traffic-unity-prototype-0.14.0.apk), [실제 Unity 화면](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/08fed474120a21f8908dc451a57388e4f7d7b586/heavy-traffic-preview.png). 로그인·redirect 없이 HTTP200 전체 bytes/SHA가 검증 파일과 같았다. 일회성 `downloads/unity-0.14.0`/`08fed474120a21f8908dc451a57388e4f7d7b586`에 APK/PNG2개만 두고 main에는 binary/지속 Release workflow/권한을 추가하지 않았다. 변경18source/meta와 관련8문서만 게시하고 기존 dirty native/user 파일·실제 index를 보존했다. 사용자 폰0.14 외형/홈·잠금·복귀·재부팅/FPS·발열은 확인 대기다. Drive/메일·Colab는 사용자의 PC Drive 구성 때까지 보류한다. 다음 후보는 버스 정류장과 상가·보행 장면의 생활 디테일이다.

집 PC의 최신 폴더에서 업데이트 설치(사용자 PC에서 실행):

```powershell
adb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.14.0.apk"
```

## 2026-10-10 — Unity 0.14.0 프레임 검사 통과, 긴차량 추월 시나리오 보정

source8a0810da/Validate38046156301(job114195996689)/artifact11666704099는reset-wheel보정후StreetChecks의실제90초15/30/60/120Hz비교를통과해DrivingChecks까지진행했고Rear-pass maneuver fails to finish로실패했다. 긴body를대입한원래6m/s대10m/s추월사례는두차가충분히앞서가는시점을현재12초요청가능구간끝에가깝게만들수있었다. 요청버스를4.8m/s,목표차선을10m/s로두어속도차가충분한실제뒤차추월사례를검사한다. production속도·신호허용구간·안전gap·60m뒤차범위·3pulse·6초merge요건은완화하지않으며실패메시지에actualstage/mask/z/ticks/pulses/phase를추가했다. 재검증대기다.

## 2026-10-10 — Unity 0.14.0 실제 첫 검사 실패: 초기화 바퀴 자세 보정

첫source9f4d0d4/Validate38045876827(job114195187333)/artifact11667219347는Unity컴파일·장면생성을수행한뒤StreetChecks90초15/30/60/120Hz에서Signal/merge traffic depends on frame rate로실패했다. rawlog대신비밀값제거unity-failure.json의실제예외/소스위치를확인했다. 이전테스트주행의앞바퀴steer/roll이ResetPosition뒤에도남았고,긴차량에서는steer된tyre가미러밖으로돌출해ResetModel의actualBodyBounds/폭안전envelope에영향을줄수있었다.

PrototypeDrive.ResetPosition이logicalwheelRoll=0과실제4wheel.localRotation=identity를함께초기화하도록수정하고StreetChecks에서실제resetwheelpose를검사한다. 프레임속도검사의허용오차/횟수/간격요건은완화하지않았다.18source/meta/102input으로범위를갱신했다. 실제재검증/GPU/Android/APK는아직대기다. 이전0.13폰성공과Drive보류/원본서명host입력보존을유지한다.

## 2026-10-10 — Unity 0.14.0 버스·박스 트럭 구현, 실제 검증 대기

사용자가0.13.0실제폰적용성공을확인하고3D버스/박스트럭패치를승인했다.0.14.0/code15,HeavyTraffic-0.14.0.unity/Generated/HeavyTraffic0140. 차량24대/6perlane에서파란CityBus4·흰BoxTruck4·기존승용차16을혼합하고시안카메라·나무/건축·시간/날씨/노을·보행/신호·NativeAndroid를유지한다.

- 버스는10.6m대칭body/옆창·승차문·앞뒤창/그릴·거울·옥상AC,트럭은7.4m분리cab/cargo/뒤중앙doorseam·lockingbars·hinges·bumper를실제mesh로생성한다. 8공유mesh/material4wheelpivots와차량당17renderer계약을유지한다. 기존bodymetres/opaqueglass64finish/등화재질을재사용하며트럭white공유재질1개만추가한다. 실제triangles/materialbudget은PC검증전이므로추정과구분한다.
- 실제길이·폭·높이를model에전달하고8°회전의보수적앞뒤envelope로following/stop/횡단보도clearance/재진입/차선변경안전거리를계산한다. 승용차3초/긴차량6초smoothmerge,깜빡이3pulse1.8초/관측뒤차60m모두추월대기/마지막gap재검사를유지하며느려진차의회전corners도중앙선/curb바깥으로나가지않게한다. 보행viewport와차량전체boundsviewport를분리해긴차의뒤쪽이아직보이는데날씨로사라지는일을막는다.
- 실제6종24fleet메트르/axle/contact·대칭body·front/rearlamps·cargo2locks/6hinges·buswindows/AC·normal/UV/winding·sharedmesh검사, 실제mixedfleet600초/긴차rearpass·6초merge·slowcorners검사를추가한다(실제보고서수치로정정예정). 기존18기후·15/30/60/120Hz·보행/100우산budget검사유지. 실제촬영은기존51+두heavy앞/뒤낮·밤8=59PNG로준비하며임시camera/pose는저장하지않는다.
- 변경17source/meta,102buildinputSHA를기록했다. 이전12host입력불변이면실제hostLint38008378207을재사용한다. 실제Unity/캡처/AndroidBuildPlayer·launcherLint/원본서명·공개APK직접주소검증은대기이며실행하지않은PASS를주장하지않는다. 이전촬영보정Effects.Advance(0)는유지한다. 폰0.14외형/FPS·발열/lifecycle는설치후확인한다.
- 기존dirty native/user파일/index보존,별도worktree없음. Drive/메일·지속releaseworkflow는추가하지않으며APK는검증후기존일회성publicdownload방식으로전달한다. 사용자가0.13직접주소다운로드와적용성공을확인했다. 초기병렬작업자는사용량한도로그만둔뒤본담당자가생성/통합/검증을계속수행했다.

## 2026-10-10 — Unity 0.13.0 가로수·외벽·옥상: 실제 검증·서명 APK 및 모바일 전달 완료

사용자가 **0.12.0 실제 폰 적용 성공**을 확인하고 다음 패치를 승인했다. 이번 전달은 **0.13.0/code14**, `Scenery-0.13.0.unity`/`Generated/Scenery0130`, `pixel-traffic-unity-prototype-0.13.0.apk`. 시안 카메라(1.4,14,-37)/target(-4.9,1,68)/44°FOV, 하늘·산·강/원경·24차량/보행/신호·시간/날씨/노을 경유·기존 전조등/브레이크등/깜빡이 세 번/안전 차선변경을 유지한다.

- **가로수:** 32그루의 수관을 둥근형·수직형·퍼지는형 3종 공유 메시와 서로 다른 잎 색으로 바꿨다. 기존8clump/640tri에서5clump/400tri로 줄이고 실루엣을 변형했다. 공유256×256 잎 texture는 반복 경계를 연결한 잎 색/음영 무늬와 mip/trilinear를 사용한다. opaque Lit 및 기존32수관의 비바람 흔들림/숨김 정지를 유지한다. 투명 잎 카드/추가 실시간 light를 넣지 않았다. 옥상12개에60tri 공유 shrub과 기존 잎 재질을 재사용한다.
- **건물:** 24개 외벽을 큰 콘크리트 패널·옅은 얼룩과 밝은 석재색으로 바꾸고 모서리/상부 테두리를 넣었다. **실제 발코니48개/standing rail post192개**와 끝 옥상 난간·환기구 slit·출입실/일부 안테나·옥상 화단을 넣었다. 각 건물의 stone/metal/plaster3 merged mesh와 기존 재질을 재사용한다. 기존480개 창문/상가24개·실제 조명4개를 유지한다. 새 architecture mesh 합계8352tri이며 건물/보행 높이와 옥상 footprint를 검사했다.
- **예산:** 실제 기본 **107334tri/1383renderer/43material**, 최대100우산 **118710tri/1551renderer/43material**. 기존 strict <120000/<2400/<48은 그대로이며 수관 단순화로 건물 추가 비용을 확보했다. 이 수치는 geometry/material 예산이며 실제 폰 FPS/발열 개선을 측정한 값은 아니다.
- **검증 과정:** 첫 source646020f9e2818b98d4dd16c4d9dbd56802562508 / Validate38038675998과 GPU51PNG PASS 후 전체낮·수형3종·외벽/옥상·밤·비바람 실제 사진을 관찰했다. 첫 발코니 통계의 buildings×2 계산을 실제 merged slab/post bounds 검사로 보강하여, 옥상 slit만 있어도 빈 난간 검사를 통과하는 일을 막았다. 최종 source **`1196faea4f345eb0e039be1d27b839f5998a2305`**, Validate [38039103702](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38039103702)에서 실제48slab/192post를 확인하고 같은 source tag `unity-apk-0.13.0-build1`로 BuildApk [38039430522](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38039430522) PASS. 새3수형/공유 opaque texture/finite UV·unit normals·winding/보행·옥상 clearance 검사, 기존18시간/날씨·15/30/60/120Hz 노을/숨김·wind/보행/600초차량/차선변경 검사 PASS.
- **촬영 보정:** 야간 나무 확대에서 이전 비바람 빗줄기가 남는 Editor Snap/readback 문제를 발견했다. capture-only source `abe19eb7eff5e5260c7f3d88423830081595aba6` / Validate+GPU [38039712854](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38039712854)에서 Effects.Advance(0)로 현재 날씨 visibility와 임시camera billboard를 갱신하고 맑음의 실제 rain/snow renderer OFF를 검사했다. active clock/전환값은 움직이지 않는다. APK는 검증된1196faea source를 사용한다. Editor 촬영파일1개 외 Runtime·shader·geometry generator/config·NativeAndroid/빌드도구 등97/98입력이 같은지 확인하여 변경 없는Android빌드는반복하지않았다. 공개 PNG는 보정된 capture source에서 가져온다.
- **실제 이미지:** Editor6000.3.26f1/Direct3D11 **51PNG**. 기존43장과3수형낮/둥근나무밤·비바람/건축낮·밤·옥상8장. 임시 확대카메라를 복원하고 장면에 저장하지 않았다. 새 날씨/야간 사진에서 기존빛과 바람을 확인했다. Editor GPU 그림은 실제 휴대폰 홈 화면이나 성능 검사와 구분한다.
- **Android/APK:** 실제 BuildPlayer Succeeded/오류0/경고21, launcher Lint 오류0/경고8. APKsource의98입력과capture source의98입력 SHA·97개동일/EditorCapture1개변경·12unchanged NativeAndroid/Bridge/host 입력을 확인하여 기존 unityLibrary Lint run38008378207 오류0/경고10을 재사용한다. artifact digest/bytes/ZIP CRC와 실제 다운로드 APK CRC/SHA, SDK apksigner/aapt의 원본 v2 cert `a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`/version0.13.0/code14/min29/target36/ARM64, 설정 Activity launcher/BIND_WALLPAPER 별도process서비스/meta/private provider/비활성 Unity Activity를 검증했다. APK **29966435bytes**, SHA256 `7a8e3a55999289272865eaf1da6af3e9791020b24a686ddd450667f30188b776`.
- **전달:** [모바일 APK](https://github.com/BACKHYUN96/S20-PLUS/raw/cb1522d10644096f540e24714d8e486098c57dcc/pixel-traffic-unity-prototype-0.13.0.apk), [실제 Unity 화면](https://github.com/BACKHYUN96/S20-PLUS/raw/cb1522d10644096f540e24714d8e486098c57dcc/scenery-preview.png). 일회성 `downloads/unity-0.13.0` branch `cb1522d10644096f540e24714d8e486098c57dcc`에는 APK/PNG만2파일, source main에는 APK/지속 release workflow/contents:write를 추가하지 않았다. 로그인 없는 공개HTTP200의 전체 파일 크기/SHA가 검증 APK/PNG와 같음을 확인했다. WORK 작업파일 응답 오류가 있으면 일반https링크를 크롬/삼성인터넷에서 연다.
- **보존/다음:** Unity10source/meta와 관련8문서만 게시했다. 기존 native 앱/사용자 dirty파일·실제index와97개 동일테스트입력·별도로검증한EditorCapture1개입력을유지했다. 실제폰0.13 외형·홈/잠금/복귀/재부팅·FPS/발열은 설치 후 확인한다. 다음 추천은 시안의 전경에 보이는 버스·박스트럭의 3D 모델 추가이며 차량 길이/안전간격/예산도 함께 검증해야 한다. Drive/Colab 작업은 사용자 지시대로 나중에 PC Drive 탐색기 설치 후 재개하며 이번에는 Drive 업로드/메일을 하지 않는다. 시안6650.jpg는 임시 첨부이며 새 환경에서는 재확보가 필요할 수 있다.

집 PC에서 최신 다운로드 폴더에 APK를 저장한 뒤 업데이트 설치:

```powershell
$unityAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $unityAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.13.0.apk"
```

## 2026-10-10 — Unity 0.13.0 확대 촬영의 날씨 잔상 보정

최종 implementation source1196faea / Validate38039103702는 실제48slab/192railpost·기존 검사와GPU51PNG PASS이며 동일source APKtag0.13.0-build1/BuildApk38039430522가진행중이다. 야간 나무 확대사진에서 이전 비바람의빗줄기가 남아있음을 발견했다. Editor Preview는 시간/날씨를 Snap하지만 Runtime ClimateEffects 갱신은 매프레임 Advance에서 수행되므로 readback직전엔 이전particle renderer상태가남을수있었다. CityPreview.CaptureState에서 Effects.Advance(0,currentRain,currentSnow,currentWind,currentTarget)를 호출해 시계/전환값을 움직이지않고 현재weathervisibility와임시camera billboard를 갱신한다. 맑음에서 실제 Rain/Snow renderer가꺼졌는지검사한다.

이번 변경은 CityPreview Editor 촬영파일1개뿐이다. 이미검증된Runtime/셰이더/geometry generator/config/NativeAndroid/빌드도구97개입력은1196faea와같다. source1196faea의서명APK빌드는계속진행하고 새capture-onlysource에서 실제Validate/촬영을확인한다. APKsource와새capture source를최종문서에구분하여기록하며 변경없는Android빌드는반복하지않는다. 기존폰0.12성공/Drive보류를유지한다.

## 2026-10-10 — Unity 0.13.0 첫 실제 검증·GPU 확인, 발코니 실체 검사 강화

source646020f9 / Validate38038675998(job114174391564) 및 실제 GPU51PNG PASS. artifact11665420067의20,392,603bytes/digest7bc3ff9c52f6a18994b6fe6ad2f87a19ada466d126cf5e994c5131cee87b1c4e/CRC/source를 확인했다. 실제 전체 낮·둥근 나무·외벽·옥상 사진에서 잎 무늬와 수형, 석재 테두리·발코니, 옥상 slit/난간/화단/안테나를 관찰했다. 기본107334tri/1383renderer/43material,100우산 최대118710/1551/43으로 기존 strict120000/2400/48을 유지했다. 수관은 실제400tri/32나무/3종·공유256잎 texture contrast.3294, 건물24/옥상garden12·architecture8352tri이다.

첫 결과의 발코니 통계는 buildings×2에서 계산했으므로 이를 실제 geometry 검사로 강화했다. SceneryChecks가 merged mesh의 slab bounds를 세고, 각 slab에 최소4개의 실제 standing rail post가 정렬되어 있는지 검사한다. 옥상 slat만 있어도 빈 난간 검사에 통과하는 일을 막는다. 새소스에서 발코니48개·실제 난간post와 기존 장면 검사/GPU를 다시 확인한 뒤 동일source 서명 APK를 빌드한다. geometry/재질/runtime/native source는 바꾸지 않았다. 0.12 실제폰 성공과 Drive 보류는 유지한다.

## 2026-10-10 — Unity 0.13.0 가로수·외벽·옥상 디테일 구현, 실제 검증 대기

사용자가 0.12.0의 실제 폰 적용 성공을 확인하고 다음 패치를 승인했다. 이번은 앞서 추천한 가로수 수관과 건물 외벽·옥상 디테일이다. **0.13.0/code14**, `Scenery-0.13.0.unity`/`Generated/Scenery0130`. 변경10 Unity 소스/meta: StarterConfig/StarterScene/CityEnvironment/CityPreview와 신규 FoliageScene/ArchitectureScene/SceneryChecks 및meta3개. 기존 카메라·원경·차량/차선변경·보행/신호·시간/날씨·NativeAndroid host/서명 설정은 유지한다. Drive/Colab는 사용자 지시대로 나중에 PC에서 재개하며 메일/Drive 작업을 하지 않는다.

- 가로수32그루에 둥근형/수직형/퍼지는형3종 공유 수관과 서로 다른 잎 색을 넣는다. 기존8clump/640tri 대신5clump/400tri의 변형된 부드러운 메시를 사용한다. 반복 경계를 연결한 공유256×256 잎 색Texture·mip/trilinear를 넣고 opaque Lit를 유지하여 alpha 잎 층/새 실시간 조명을 추가하지 않는다. 기존32수관 wind/lifecycle 연결을 유지한다.
- 건물24개 외벽을 큰 콘크리트 패널/옅은 얼룩으로 바꾸고 모서리 pilaster·상부 띠·양 끝 옥상 난간, 건물당2개 발코니/난간(총48개), 기존 환기장치의 slit·옥상 출입실/일부 안테나,12개 옥상 화단 foliage를 추가한다. building당 stone/metal/plaster3 merged mesh와 기존 재질을 재사용하여 작은 난간마다 renderer/material을 만들지 않는다. 기존창문480개/상가24개·실제조명4개를 유지한다. 보행 높이/건물 간격/옥상 footprint를 실제 geometry로 검사한다.
- SceneryChecks는 실제3종수형/UV/normal/finite/winding·opaque공유 texture·32나무/24건물/12garden·보행/옥상 clearance·예산을 검사한다. 기존 기후 검사가32수관 바람/숨김과18시간/날씨·노을 전환을 확인한다. Capture에3종나무낮/밤/비바람·외벽낮/밤/옥상8개확대가추가되어 총51PNG를준비한다. 임시카메라는저장하지않는다.
- 98build input SHA와 기존12host 입력 불변을 확인했다. 실제 Windows Validate/새 GPU 렌더·최대100우산 geometry budget·Android BuildPlayer/Lint/서명 APK/공개 HTTP 검증은 아직 실행 대기다. 예상/코드상 비용과 실제 관측 결과를 구분한다. 폰0.13 외형/FPS·발열은 추후 설치 후 확인한다. 실제 검증·보정·서명 빌드까지 이번 작업에서 진행한다.

## 2026-10-10 — Unity 0.12.0 원경 패치: 실제 렌더·서명 APK 검증 및 모바일 전달 완료

사용자가 실제 폰0.11.0 적용 성공을 확인하고 원경 패치를 승인했다. **0.12.0/code13**, `Landscape-0.12.0.unity`/`Generated/Landscape0120`, `pixel-traffic-unity-prototype-0.12.0.apk`. 0.11 카메라 위치(1.4,14,-37)/target(-4.9,1,68)/44°FOV와 교통·보행·횡단보도/차량 등화·세 번 깜빡임/안전 차선변경을 유지한다. 시안 원본은 사용자 첨부6650.jpg이며 이 환경의 임시 첨부 경로에만 존재하므로 새 환경에서는 재확보가 필요할 수 있다.

- **구현:** sky/3산능선/맞은편26도시와 정상 랜드마크/강/다리의7개 opaque mesh, 원경 총1108tri 및 한 개 공유 runtime material. 느린 구름·산음영·적설·도시 창문 불빛·강 glints/노을/야간 반짝임을 기존 active clock와 18시간/날씨 조합에 연결한다. 낮→노을→밤4초, 밤→낮 직접4초, 재선택과 숨김 시간 정지를 유지한다. realtime조명은4개이며 원경은 새 조명/투명 fullscreen층/매프레임 geometry를 추가하지 않는다. Distant.shader는 재질 종류에 맞춰 world좌표와UV를 사용한다.
- **예산:** tree trunk/branch·lamp pole 등 CityEnvironment의 원통만 같은1×2×1 bounds와 smooth normals의16면/64tri 공유 mesh로 바꾼다. 차량 타이어·수관과32그루 수는 유지한다. 기본 105942tri/1299renderer/43material, 최대100우산 **117318tri/1467renderer/43material**. 이전119922/1515/47보다 감소하며 기존 strict <120000/<2400/<48 검사를 유지한다. 이는 geometry 예산이며 실제 폰 성능 측정이 아니다.
- **개발 중 보정:** 첫 Validate 38031801525은 PASS였지만 실제 GPU 이미지에서 산 quad별UV 반복이 세로 줄무늬로 나타났고 강 반짝임이 지나치게 평행했다. 산 음영/적설을 공유 world좌표로 고치고 강 ripples에 불규칙 phase와 glint mask를 추가했다. 수정 후 Validate 38032346679의 새 GPU 이미지에서 보정을 확인했으며 첫 결과만으로 완료라고 판단하지 않았다.
- **실제 검증:** source `b0c20f510d53c82f466310a5334824c32ac82526`, Validate [38032346679](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38032346679)와 동일 source APK tag `unity-apk-0.12.0-build1`, BuildApk [38032677827](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38032677827) PASS. 원경 topology/색·UV/finite 자료·runtime단일 재질·saved18endpoint·15/30/60/120Hz 양방향시간oracle900frame·숨김100frame·retarget·9:20/9:16 projection4sample, 기존 날씨/노을/차량/보행/차선변경 검사 PASS. 실제 Editor 6000.3.26f1 / Direct3D11 **43PNG**: 기존39+원경 낮/노을/밤/비바람 확대4. GPU 캡처는 실제 폰 홈 화면 사진과 구분한다.
- **Android/APK:** 실제 BuildPlayer Succeeded/오류0/경고0, launcher Android Lint 오류0/경고8. NativeAndroid/Bridge/host 입력12개의 SHA 불변과92build input SHA 일치로 이전 unityLibrary Lint run38008378207 오류0/경고10을 재사용한다. 내려받은 artifact ZIP digest·CRC, APK CRC/SHA/크기와 Linux SDK `apksigner`/`aapt` 원본 v2 cert `a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`, version/code13/min29/target36/ARM64, 설정 Activity launcher/BIND_WALLPAPER 별도process서비스/meta/private provider/비활성 Unity Activity를 확인했다. APK **29686703bytes**, SHA256 `540c7f3be7010e49add8101c441c550bc3b23f9f7455cb97958fc9367c09f2a2`.
- **전달:** [로그인 없는 모바일 APK](https://github.com/BACKHYUN96/S20-PLUS/raw/d513f037b9592bf747bfededf3fdcc5ed1150b20/pixel-traffic-unity-prototype-0.12.0.apk), [실제 Unity GPU 화면](https://github.com/BACKHYUN96/S20-PLUS/raw/d513f037b9592bf747bfededf3fdcc5ed1150b20/landscape-preview.png). 일회성 `downloads/unity-0.12.0` branch `d513f037b9592bf747bfededf3fdcc5ed1150b20`에는 APK/PNG만2개; source main에 APK/지속 release workflow/contents:write를 추가하지 않았다. 공개 HTTP200에서 파일 전체 bytes/SHA가 검증 APK/PNG와 같은지 확인했다. 작업파일 열기 오류가 있으면 이 일반 https 링크를 크롬/삼성 인터넷에서 연다.
- **작업 보존:** 변경13 Unity 소스/meta와 관련 문서만 원격에 게시했다. 기존 nativeAndroid 작업 및 실제index/사용자 dirty파일을 보존하고 checkout/reset/pull/worktree를 사용하지 않았다. 0.11 카메라/nativehost 입력은 유지한다.
- **아직 미확인/다음:** 실제폰0.12 외형·홈/잠금/복귀/재부팅/FPS·발열은 설치 후 확인한다. 구름/원경은 이번 단계의 procedural 표현이며 시안의 완성 사진 수준 자산은 아니다. 다음 추천은 수관/가로수 품종·건물 옥상/외벽 디테일이며 geometry 예산을 지켜 단계적으로 진행한다. 사용자의 Drive/Colab 연결은 나중에 PC Drive 탐색기를 설치한 뒤 재개하므로 이번에는 Drive 업로드/메일을 하지 않는다.

집 PC에서 APK를 최신 다운로드 폴더에 저장한 뒤 업데이트 설치:

```powershell
$unityAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $unityAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.12.0.apk"
```

## 2026-10-10 — Unity 0.12.0 첫 실제 검증 통과, 원경 GPU 표면 보정 후 재검증

첫 실제 Windows Validate run38031801525/source64ed2349는 장면·원경·기존 날씨/노을/보행자/차량/차선변경 검사 PASS, GPU43PNG 및 pipeline PASS다. 최대100우산117318tri/1467renderer/43material로 기존119922/1515/47보다 감소했다. 실제 이미지를 검토하니 산48구간마다0..1UV가 반복되어 세로 줄무늬가 보였고 강광택이 지나치게 평행했다. Distant.shader를 공유 world-position 기반 산음영/적설로 수정하고 강 ripples에 불규칙한 phase와 glint mask를 넣었다. geometry/카메라/Android host 입력은 그대로다. 새 소스에서 실제 Validate·GPU 캡처를 다시 확인한 후 signed APK를 빌드한다. 첫 검사와 수정 후 검사를 구분하고 폰0.12/FPS·발열은 확인 대기다. Drive 구성은 사용자 지시대로 보류한다.
## 2026-10-10 — Unity 0.12.0 하늘·산·강·맞은편 도시 원경 구현, 실제 검증 대기

사용자가 기존0.11.0 APK 다운로드·폰 적용 성공을 확인했고 다음 원경 패치 진행을 승인했다. Drive/Colab 구성은 나중에 PC에서 재개하므로 이번에 연결·업로드·메일 작업을 하지 않는다. 변경13소스/meta: StarterConfig/StarterScene/CityEnvironment/CityClimate/CityPreview 및 DistantBackdrop/DistantScene/DistantChecks/Distant.shader와meta. 0.12.0/code13, Landscape-0.12.0.unity/Generated/Landscape0120.

- 기존 나무 줄기·가지·가로등 등 원통을 같은1×2×1 크기의 smooth-normal16면/64triangle 공유메시로 바꿔 엄격한120000triangle/2400renderer/48material 예산 여유를 확보한다. 차량/수관/나무수와사용자0.11카메라(1.4,14,-37)/target(-4.9,1,68)/44°는유지한다. 실제최종예산은Windows검사대기다.
- 기존단순tower/3ridge 대신 sky/3산능선/26도시/강/다리7메시·공유재질1개를만든다. shader에하늘gradient·천천히움직이는구름·산음영/적설·26건물창문/낮→노을→밤도시빛·강ripples/노을과야간반짝임을넣고기존Climate활성시간/시간·날씨weights에연동한다. realtimeLight/투명full-screen층/매프레임geometry생성을추가하지않는다.
- 새실제geometry/자료·단일runtime재질·asset불변·18저장endpoint·독립15/30/60/120Hz노을경유oracle·밤→낮노을없음·숨김cloud/shimmerfreeze·retarget·두세로화면비projection검사를추가했다. 같은기존geometry/traffic/weather/100우산검사도실행할예정이며단순소스읽기를PASS라고기록하지않는다.
- 실제GPU기존39PNG에원경확대낮/노을/밤/비바람4PNG를추가해원경묘사와전체portrait구도를관찰한다. shadercompilation/실제새원경렌더/AndroidBuild/Lint/서명code13/hash/실제폰은아직검증전이다. source게시→WindowsValidate및GPU→동일sourceAPKtag순서로진행한다. host12입력이0.11과불변이므로기존실제Lint38008378207을재사용한다.

기존native dirty/realindex와원본입력상태를 `/workspace/artifacts/unity-0.12.0-start-state.json`에저장했다. 새buildinputs92개와13파일whitelist/hostinputSHA는 `unity-0.12.0-source-manifest.json`에기록한다. 이번원경이시안사진의건축·식생·차량상세까지완성한것을의미하지않는다.

## 2026-10-10 — Unity 0.11.0 시안 카메라 구도·모바일 APK 전달 완료

사용자는 0.10.0의 실제 폰 적용 성공을 확인했고, 외출 중 모바일 WORK에서 이전 시안6650.jpg를 다시 제공했다. 카메라 → 원경 배경 → 나무·건물 순서를 작업 전에 설명한 뒤 이번 카메라 패치 진행 승인을 받았다. 이번 범위는 **0.11.0/code12 카메라 구도**이며 강·구름 등 배경 상세 작업은 다음 단계다.

### 구현·시안 비교

- 원본 첨부 `/tmp/codex-remote-attachments/01a1148a-d85a-7329-b63f-154bf06c802b/93b2f3fc-8317-4832-bf9f-7214372258cd/1-6650.jpg`를 실제로 관찰했다. 시안처럼 도로 소실점을 오른쪽 위에 두고 횡단보도를 아래쪽 중간에 배치하며, 전경 차량과 상단 배경 여백을 함께 담는다.
- 카메라 위치 **(1.4,14,-37)**, 시선 목표 **(-4.9,1,68)**, FOV **44°**. 기존0.10.0의 (-1.2,20.5,-27)/target(0,1.6,23)보다 덜 내려다본다. 9:20과9:16 화면비에서 독립 world projection으로 소실점·전경 중앙선·횡단보도 위치를 검사한다. lower-band18 ground rays와 upper-band6 sky rays를 구분해 의도적인 하늘 여백을 지면 누락으로 오인하지 않는다. 실제 lower band는 모두 기존 지면 bounds 안에 있어야 한다.
- 첫 source **f7e885d53c83d315dfe7b1fe46ae0104780287f6**, Validate38027918710/job114142612019는 장면·39PNG 생성 PASS였다. 하지만 old/new 실제 이미지에서 전경 중앙선이 시안보다 너무 왼쪽으로 치우친 것을 확인했다. 카메라/target x를 함께1.8m 옮겨 pitch/yaw/FOV를 유지하면서 하단 도로를 중앙에 맞췄다. 보정 source에 대한 실제 검증과 캡처를 다시 수행한 뒤 APK 태그를 만들었다.
- 변경5소스: Runtime StarterConfig, Editor StarterScene/CityEnvironment/DrivingChecks/CityPreview. `ReferenceCamera-0.11.0.unity`, `Generated/ReferenceCamera0110`, build inputs84개. 새geometry/material/실제Light는 추가하지 않았다. 실제 시야가 날씨 offscreen 생성·퇴장 판정에 영향을 주므로 보행·교통·날씨 검사도 실행했다.
- 같은 차량/조명 상태의 old camera, new camera, reference9:16 캡처3장을 추가했다. 최종 GPU 장면에서 도로 하단과 횡단보도, 전경 차량, 하늘 여백, 낮/밤 등화를 관찰했다. 일부 상가는 수관에 가려지고 상단 원경은 아직 기존 단순 fog/skyline/ridge다. 그림의 강·구름·건축/차량 디테일까지 구현했다고 주장하지 않는다.

### 실제 검사

| 검사 | 관측 결과 |
| --- | --- |
| Windows Unity | [Validate38028274105](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38028274105), job114143665812 success. 실제 scene/기존 검사와 GPU 캡처 PASS |
| 카메라 | 9:20/9:16 모두 ground rays18/sky rays6. 마지막9:16 측정 road vanishingX0.632975/horizonY0.652768/crossingY0.291969/foregroundCentreX0.512751; viewport y는 아래0/위1 |
| 실제 GPU | D3D11 URP Editor PNG39장: 기존 도시14·차량8·상가6·등화/변경8 + 카메라 비교3. old/new는 같은 교통·조명 상태, reference9:16은720×1280. 실제 사진을 관찰했으며 phone screenshot/FPS 증거는 아님. 임시 카메라 비교 포즈는 APK scene에 저장하지 않음 |
| 교통/보행 | 차선 변경600초 완료11회/뒤차대기73회/최저 gap1.800000m,3pulse 후3초merge·중앙선/연석·실제램프·15/30/60/120Hz/pause PASS. 보행4/32/100명200/600/600초, 횡단223회/신호33주기 |
| 날씨/시간 | 각810초 storm drain/recovery·offscreen-only생성/퇴장, 퇴장92/복귀92, 우산100/spray96·기존낮→노을→밤/retarget/숨김·32나무·신문지3~5초 PASS |
| 예산 | 기본108,546tri/1347renderer/47material; 최대100우산119,922/1515/47, 기존 strict <120000/<2400/<48 유지. 삼각형 여유78개뿐이므로 다음 원경 geometry 추가 전에 기존 geometry 비용을 줄여야 함 |
| Android | [BuildApk38028607350](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38028607350),job114144636634 success. 실제 BuildPlayer Succeeded/errors0, launcher Lint errors0/warnings8 |
| Host 검사 | 관련12입력 SHA가0.10.0과 같아 기존 실제 host Lint38008378207(0errors/10warnings/nativeFilesMatched8)을 재사용. 새 host 검사를 했다고 주장하지 않음 |
| APK | 실제다운로드 artifact digest/size/CRC, aapt0.11.0/code12/min29/target36/ARM64/SettingsActivity launcher·BIND_WALLPAPER/:wallpaper service/meta·private provider·disabledUnityActivity·원본v2cert·APK SHA/bytes PASS |
| 모바일 전달 | [로그인 없는 APK 다운로드](https://github.com/BACKHYUN96/S20-PLUS/raw/ff54aa438e6f782b727b566e619b8d826bd985fc/pixel-traffic-unity-prototype-0.11.0.apk), HTTP200/전체bytes/공개파일SHA 일치를 다시 확인. 원본APK 그대로 일회성 downloadbranch로 게시. WORK 로컬 파일 전송 오류를 피하며 자동 release workflow/지속 쓰기 권한은 추가하지 않음. [실제 Unity 카메라 화면](https://github.com/BACKHYUN96/S20-PLUS/raw/ff54aa438e6f782b727b566e619b8d826bd985fc/camera-preview.png)도 공개 HTTP/SHA 검증 |

실제폰0.11.0 적용과 설정 보존·홈/잠금·숨김/복귀·재부팅·발열/FPS는 사용자 설치 후 확인 대기다. 최신 실제폰 성공은0.10.0이다. launcher 경고8와 기존 host 경고10은 오류0과 구분한다.

### 산출물·소스 추적

- Source **a04836ca7a36a4b0460a74814c5e82dd90a99729**, tag **unity-apk-0.11.0-build1**, 다운로드 전용branch **downloads/unity-0.11.0**/commit **ff54aa438e6f782b727b566e619b8d826bd985fc**. 앱 source main에 APK binary를 넣지 않았다. 수정5파일은 temp index whitelist로 게시하며 real index/기존 native dirty 상태를 보존했다. manifest `/workspace/artifacts/unity-0.11.0-source-manifest.json`에84입력SHA·원본시안경로·첫렌더보정내역·run/artifact/문서commit을 기록한다.
- APK `/workspace/artifacts/pixel-traffic-unity-prototype-0.11.0.apk`, **29788679bytes**, SHA256 **528363f609f6a67c59583e5e16190befe1814d71fc0dce009b9b9987afab1084**. original v2cert **a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6**.
- Validateartifact11660639198/SHAa9a421e881be70aab66c9ba1bc89ebe0103ffaf107a3e54902a894168f8786c0; APKartifact11661475378/reportsartifact11661880061. actual reports `/workspace/artifacts/unity-prototype-0.11.0-reports`, download verification `unity-prototype-0.11.0-download-verification.json`, public delivery `unity-0.11.0-mobile-apk-download.json`. 특정 cloud 첨부/7일Actionsartifact의다음세션존속은가정하지않는다.

### 설치

모바일에서는 위 링크를 크롬/삼성인터넷으로 열고 다운로드한 APK를 설치한다. PC라면 `C:\Users\김백현\Desktop\AI`에 저장하고 실행한다.

```powershell
$unityAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $unityAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.11.0.apk"
if ($LASTEXITCODE -eq 0) {
    & $unityAdb shell am start -W -n "com.s20plus.pixeltraffic.unityprototype/com.s20plus.pixeltraffic.unitywallpaper.WallpaperSettingsActivity"
}
```

다음 단계는 **강·산·구름과 맞은편 도시 원경**이다. 먼저 기존 메시의삼각형 비용을 낮추고, 이구도에서 낮/노을/밤·fog 전환을 비교하며 만든다.

## 2026-10-10 — Unity 0.11.0 실제 카메라 렌더 관찰 후 좌우 위치 보정

첫sourcef7e885d53c83d315dfe7b1fe46ae0104780287f6의WindowsValidate38027918710/job114142612019와D3D11 PNG39장생성은PASS였다. old/new/9:16과야간렌더를관찰했다. 새pitch와상단배경여백/횡단보도y는적절하지만전경중앙선이시안보다왼쪽으로치우쳐대각선이과했다. 카메라와target의x를동시에1.8m왼쪽으로옮겨yaw/pitch/FOV를유지하면서도로하단을중앙에맞춘다. 최종position(1.4,14,-37),target(-4.9,1,68). ForegroundCenterX와crossingx의독립worldprojection검사를추가/강화했다.

변경은기존5파일중StarterScene/CityEnvironment/DrivingChecks3파일이다. 실제geometry/재질예산은첫검사에서기본108546/1347/47·100우산119922/1515/47로유지,보행223회/33주기와날씨810초퇴장92/복귀92도PASS였다. 보정source에대해같은검증·실제GPU이미지를다시확인한후APKtag를생성한다. 원경의강/구름디테일은다음패치이며현재상단은기존fog/skyline에따라비어보이는부분이있다.

## 2026-10-10 — Unity 0.11.0 시안 카메라 구도 구현 (검증·APK 대기)

사용자는0.10.0실제폰적용성공을확인했고외출중모바일WORK에서원본시안6650.jpg를제공했다. 카메라→원경→나무/건물순서를미리설명한후이번카메라패치진행을승인했다. 시안은도로소실점이오른쪽위,횡단보도가아래쪽중간,전경차량과상단원경/하늘이함께보인다. 실제첨부 `/tmp/codex-remote-attachments/01a1148a-d85a-7329-b63f-154bf06c802b/93b2f3fc-8317-4832-bf9f-7214372258cd/1-6650.jpg`를관찰했다.

- 카메라위치(3.2,14,-37),target(-3.1,1,68),44°FOV로내려다보는각도를줄이고소실점을오른쪽으로옮겼다. 시안의배경상세/강·구름은다음단계이며현재카메라와시안의화풍까지일치한다고주장하지않는다. 앱0.11.0/code12,ReferenceCamera-0.11.0.unity/Generated/ReferenceCamera0110.
- 수정5소스 StarterConfig/StarterScene/CityEnvironment/DrivingChecks/CityPreview,build inputs84개. 새geometry/material/실제Light는없다. lower-band18groundray와upper6skyray,도로소실점/횡단보도projection을9:20/9:16두화면비로검사하고같은traffic/lightingstate의old/new/9:16실제렌더를추가한다. 카메라시야가날씨offscreen판정에영향을주므로기존weather/drain/생성·교통/보행검사도실행한다.
- Source검토/WindowsValidate/실제GPU렌더관찰/서명APK/code12·원본cert/hash 확인은아직대기다. 새자동릴리스workflow/contents:write권한은추가하지않는다. 완료APK는직전에성공한일회성GitHub downloadbranch로배포하여모바일WORK의로컬파일다운로드문제를피한다.

## 2026-10-10 — 모바일 WORK APK 다운로드 오류: 외부 브라우저 링크 배포 완료

사용자폰에서작업경로APK링크를열때 `Interrupted waiting for app-server response for seq_id 6`가발생했다. WORK의파일전송경로실패로추정하며모바일클라이언트자체를수정했다고주장하지않는다. 같은 **Unity0.10.0/code11** APK를GitHub공개파일링크로별도배포했고로그인없는HTTP200다운로드/bytes/SHA일치를실제로확인했다. 사용자폰에서새링크로받는것은확인대기다.

- [모바일/PC APK 직접 다운로드](https://github.com/BACKHYUN96/S20-PLUS/raw/50a5b1945a646e7f6dbec5493cc0b599b0bd66d4/pixel-traffic-unity-prototype-0.10.0.apk). WORK내부파일열기대신크롬/삼성인터넷에서URL을연다. 파일명 `pixel-traffic-unity-prototype-0.10.0.apk`, 29796063bytes, SHA256 **40f0ab6cdd699c443a1b8b6998ac0d98f57d31cbbd8ffc47a7636eb013fbaf9e**. 원본APK·서명·앱소스는변경없고이미실행한Unity검증/AndroidBuildPlayer/Lint와다운로드원본v2서명확인을재사용한다.
- 전용 `downloads/unity-0.10.0` branch/commit **50a5b1945a646e7f6dbec5493cc0b599b0bd66d4**의root tree에APK한파일만업로드했다. workflow·토큰·서명키·앱소스는없다. 소스main에는배포binary를추가하지않았다. 일반https GitHub/raw URL을기반으로하며로그인없는직접다운로드의Content-Type은application/octet-stream, 실제전체29796063bytes의SHA가원본과동일했다. 특정세션작업폴더/7일Actionsartifact링크에의존하지않는다.
- 먼저GitHubRelease방식을준비했지만cloud에서GitHubAPI직접호출이Forbidden이었다. 이후준비한자동릴리스workflow의main push는자동승인검토에실행전거절됐다: 지속적인contents:write/release게시권한은사용자의다운로드문제해결요청에명시적으로포함되지않고일회성방법이있다는이유다. 새권한설정없이APK한파일만배포하는더좁은방법은실행됐다. 거절된workflow는원격에게시하지않았고local작업파일도제거했다. 조사용준비본만 `/workspace/artifacts/unity-mobile-download-unpublished-workflow.yml`에보관하며적용된기능으로취급하지않는다. **향후자동릴리스는구현하지않았다.**
- 초기상태 `/workspace/artifacts/unity-mobile-download-start-state.json`, 공개다운로드관측 `/workspace/artifacts/unity-mobile-apk-download.json`. source빌드입력84개·realindex·기존native상태를보존한다. 원격branch게시/공개파일접근확인은완료했고docs는[skipci]범위로만게시해앱재빌드를유발하지않는다.

PC에서설치할경우기존집경로 `C:\Users\김백현\Desktop\AI`에APK를저장하고실행한다:

```powershell
$unityAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $unityAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.10.0.apk"
```

## 2026-10-10 — Unity 0.10.0 카메라·차량 등화·안전한 차선 변경 APK 전달 완료

사용자가 요청한 카메라 구도 변경, 전조등·브레이크등, 간헐적인 깜빡이3회 후 차선 변경을 **0.10.0/code11**에 적용했다. 이전 명시 승인에 따라 main 게시와 연결된 Windows PC의 검증·GPU 캡처·서명 APK 빌드를 실행했다.

### 구현과 동작

- 카메라 위치를(0,26,-32)→(-1.2,20.5,-27), 시선 목표를(0,0,28)→(0,1.6,23), FOV50→44로 바꿔 도로/차량/건물을 낮고 가까운 세로 구도로 담는다. 원래 시안 파일을 이번 작업환경에서 확보하지 못해 픽셀 단위 일치를 주장하지 않는다. 일부 상가는 수관에 여전히 가려진다. 다음 시야 개선은 나무 배치/수관 크기를 따로 다듬어야 한다.
- 24대에 기존 전후 램프의 공유 재질과 MaterialPropertyBlock을 활용한다. 야간/노을/흐린 날 전조등이 기존4초 시간전환을 따라 밝아지고, 제동/정차 시 브레이크등이 강해진다. 차량별 새 실제 Light 없이 기존 Atmosphere 재질의 도로 light pool을 사용한다. 추가3renderer/12tri/car, 전체288tri이며 새 재질은 없다. 투명 도로 빛은 차량 충돌/추종용 BodyBounds에서 제외한다.
- 같은 방향의 인접 차선만 변경한다. 목표 차선의 뒤60m 관측 범위 차량을 개별 odometer로 추적해 모두 앞서 나갈 때까지 현재 차선에서 기다리고72%속도로 양보한다. 대기 중 새 뒤차도 추적하며 앞뒤 거리·속도 차를 고려한다. 뒤차가 느리거나 간격이 없으면 기다리거나 취소/재시도한다. 310m loop의 단순 signed-distance wrap을 실제 추월로 오인하지 않는다.
- 안전거리 확보 후0.3초on/0.3초off를3회, 총1.8초 표시한 다음3초 smoothstep으로 차선을 변경한다. 깜빡이 중 뒤차가 새로60m 안에 들어오면 취소한다. 시작 직전 앞뒤 간격을 다시 확인하고 깜빡이/변경 중 두 차선을 예약해 주변 차가 추종한다. 50m 내 횡단보도·신호 종료 직전·비바람에서는 새 변경을 시작하지 않는다. 차량 yaw±8°와 앞바퀴 조향/회전을 함께 반영하며 같은30Hz 모델을 사용한다.
- 소스/meta19파일, build inputs84개: StarterConfig/PrototypeDrive/StreetModel/StreetSimulation, TrafficFleet/StarterScene/ClimateScene/CityPreview/StreetChecks/WeatherLifeChecks/VehicleDetailChecks 및 신규 LaneChanges/VehicleLighting/DrivingScene/DrivingChecks와meta4개. Driving-0.10.0.unity/Generated/Driving0100. 원본 Android 앱 변경·서명/NativeAndroid/bridge는 유지했다.

### 실제 검사와 한계

| 검사 | 실제 결과 |
| --- | --- |
| Windows Unity | [Validate38019981969](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38019981969),job114118617236; scene/DrivingChecks/GPU 캡처 모두 PASS |
| 차선 변경 | 2뒤차 실제 통과 후3pulse, 앞차 간격 부족 대기, 깜빡이 중 새 뒤차 접근 취소, 횡단보도/비바람 시작 금지,600초 production교통 검사. 완료11회/독립 merge검사11회/뒤차대기73회, 최소 bumper gap1.800000m, 순간 lateral이동<.055m/30Hz tick, 중앙선/연석 침범 없음 |
| 실제 lamp/clock | 기존 실제Renderer MPB의 낮/밤·제동 발광/좌우 방향·54tick3pulse,17renderer/car/sharedmesh 및 실제Light4개 확인. 실제 StreetSimulation15/30/60/120Hz의 z/x/merge state와 pause tick freeze PASS |
| 보행·날씨·외형 | 4/32/100명200/600/600초/223회 횡단/33signal cycles, 최저 foot거리0.399999m. 날씨 각810초/퇴장92/복귀92/100우산/96spray PASS. 차량24대4모델/600초, curved mesh/window/tyre/wrap·wheel·기존상가/창문 전환 PASS |
| 예산 | 기본108,546tri/1347renderer/47material. 최대100우산119,922/1515/47, 기존strict <120000/<2400/<48 유지. 삼각형 여유78개뿐이므로 다음geometry추가 전 기존메시 감축 필요 |
| 실제 화면 | D3D11 URP Editor PNG36장(도시14·차량8·상가6·등화4·실제lane상태4). 낮/밤 도시와 등화on/off 및실제merge시작/중간/완료를 관찰했다. 등화 closeup의 임시카메라/pose/state는 unsaved Editor scene에만 적용하고 APKscene에는 저장하지 않는다. lane캡처는 실제 모델 진행 상태이며 잔여 weather효과가 일부 보인다. phone screenshot/FPS 증거 아님 |
| Android | [BuildApk38020355014](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38020355014),job114119779332; 실제BuildPlayer Succeeded/errors0, launcher Lint errors0/warnings8 |
| unchanged host | 관련12input SHA 불변으로 기존 실제host Lint38008378207(0errors/10warnings/nativeFilesMatched8)을 재사용한다. 새host Lint를 실행했다고 주장하지 않는다 |
| 실제 다운로드 APK | artifact API size/digest·ZIP CRC·APK hash/bytes, aapt0.10.0/code11/min29/target36/ARM64/SettingsActivity launcher·exportedBIND_WALLPAPER/:wallpaper service/meta·private provider·disabledUnityActivity, original v2cert PASS |

폰에서0.10.0 구도·깜빡이와 양보·설정 보존·홈/잠금·숨김/복귀·재부팅·발열/FPS는 설치 후 사용자 확인이다. 기존 launcher경고8와host경고10이 남으며 오류0과 구분한다. 안전조건이 맞지 않으면 차량이 차선을 바꾸지 않는 것이 정상이다. 모든 후방310m 차가 아닌 관측 범위60m 안의 차를 기다리는 정책이다.

### 산출물·추적

- Source **27e6dda036ec72648bbaf1639012c4fa4676e182**, tag **unity-apk-0.10.0-build1**. 19파일 whitelist/84입력SHA·host입력 불변은 `/workspace/artifacts/unity-0.10.0-source-manifest.json`; 기존 real index와 native dirty 상태를 보존했다. 문서만 추가하는 최종commit은 이manifest에 별도 기록하며 source SHA가 같으면 검사를 반복하지 않는다.
- APK `/workspace/artifacts/pixel-traffic-unity-prototype-0.10.0.apk`, **29796063bytes**, SHA256 **40f0ab6cdd699c443a1b8b6998ac0d98f57d31cbbd8ffc47a7636eb013fbaf9e**, original v2certificate **a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6**.
- Validateartifact11657144723/SHAe650d318a31365b4cd0cd50b84ff5a3506bc01e6a7d91fb140508448ffdbcb55, APKartifact11658136898, APKreportsartifact11657777254; 실제report `/workspace/artifacts/unity-prototype-0.10.0-reports`/검증 `unity-prototype-0.10.0-download-verification.json`. 7일 GitHubartifact와 cloud 첨부파일의 다음세션 존속을 가정하지 않는다.

### 집 PC 설치

APK를 `C:\Users\김백현\Desktop\AI`에 저장한 다음 실행한다.

```powershell
$unityAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $unityAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.10.0.apk"
if ($LASTEXITCODE -eq 0) {
    & $unityAdb shell am start -W -n "com.s20plus.pixeltraffic.unityprototype/com.s20plus.pixeltraffic.unitywallpaper.WallpaperSettingsActivity"
}
```

다음 후보: 수관/나무 위치로상가시야개선, 주차/정류장·생활소품. 최대우산 예산의tri여유가78개이므로 먼저기존geometry비용을낮추고실제구도를비교한다.

## 2026-10-10 — Unity 0.10.0 camera and safe vehicle maneuver implementation (validation pending)

- User requested a lower/closer portrait camera, vehicle headlights/brake lamps, and intermittent lane changes after exactly three blinks and rear traffic passes. Existing permission covers main publication and the connected Windows runner.
- Camera (-1.2,20.5,-27), 44° FOV; 24 shared-geometry light rigs, night road beams, brighter braking lamps, direction-correct amber signals. Existing four street spotlights/material budget retained.
- Same-direction adjacent lanes only. Track rear traffic within 60m until it actually passes; yield in the original lane, reserve both lanes during signaling/merging, cancel when a new rear vehicle approaches or a safe gap disappears. 1.8s/three pulses then 3s smooth merge; avoid starting within 50m of the crossing and storms.
- Added deterministic rear-pass, late-gap, three-blink, 600s traffic and actual lamp tests; preserve pedestrian/weather/Android host checks. Actual Windows validation, GPU captures and signed APK are pending. Do not present this source as a delivered APK.

## 2026-10-10 — Unity 0.9.0 상가·다양한 창문 조명 APK 전달 완료

카페·편의점·일반 매장과 자연스러운 야간 조명을 **0.9.0/code10**에 적용했다. 사용자가 이번 수정본을 BACKHYUN96/S20-PLUS main에 게시하고 연결된 PC에서 자동 검증·화면 캡처·서명 APK 빌드까지 진행하도록 명시 승인했다. 직전 main/기능 브랜치 push는 자동 승인 검토에 실행 전 차단됐고 승인 뒤 정상 게시했다. 최신 확인된 사용자 폰 적용 성공은0.7.0이며0.8.0/0.9.0 폰 확인을 가정하지 않는다.

### 구현·범위

- 24개 건물에 카페·편의점·일반 매장 각각8개를 추가했다. 1층 진열 창문·문/손잡이·매장 간판·OPEN 포스터·차양을 구성하고 좌우 간판 글자 방향을 맞췄다. 건물별 프레임/유리 메시로 합쳤으며 실제 저층 geometry는 보행 경로 밖, 차양 간판은 머리 위에 둔다.
- 480개 상층 창문에 불 꺼진 방, 따뜻한 조명과 차가운 조명, 커튼/블라인드/가구 실루엣을 공유256×128 atlas로 만들었다. 상가는512×512 색상/발광 atlas를 공유한다. 건물별 재질 복제·추가 실제 조명·창문별 Update 없이 기존 공유 재질 노출만 변경한다.
- 낮→노을2초→밤2초와 밤→낮4초를 유지하며 노을에 창문52%/매장86%, 밤100%로 밝아진다. 흐린 날에도 낮은 발광을 적용한다. 실제 shader 발광 값의 독립 시간 oracle, 재선택/양구간 retarget/숨김 freeze/asset 불변과 4개의 기존 실제 조명 제한을 검사했다.
- 변경10소스/meta: Editor CityEnvironment/ClimateScene/StarterScene/CityPreview/FrontageScene/FrontageChecks 및 신규meta2개, Runtime CityClimate/StarterConfig. `Storefront-0.9.0.unity`, `Generated/Storefront090`. 차량 geometry·교통/보행 모델·NativeAndroid·Android bridge·서명 설정은 동일하다. 생성 텍스처/메시는 Editor에서 제작하며 소스에 키·토큰을 추가하지 않았다.

### 실제 렌더 수정 과정

첫source 47cdcc68a3657100c74e0431ea9da8b72e65829a의 Validate38017177473는 장면/28PNG 생성PASS였지만 실제 상가낮/밤6장 관찰에서 간판 반전과 Cafe 간판 일부 나무 가림을 발견했다. UV 검사가 잘못된 viewer-right 가정을 공유해 통과했음을 확인하고, FrontageScene의좌우UV flip을 고치며 FrontageChecks를카메라up/forward 벡터 외적으로검사하게바꿨다. CityPreview카메라를낮고반대편도로로옮겼다. 최종source를다시실행하고실제PNG를확인한후APK태그를생성했다. 게임카메라/거리geometry는변경하지않았다.

### 실제 검사

| 검사 | 관측 결과 |
| --- | --- |
| Windows Unity | [Validate38017565116](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38017565116), job114111142156 success. 실제 scene 결과PASS, 검사가 만든 임시 재질/상태는 원본 장면 재열기로 제거 |
| 상가·창문 | 실제 건물24/창문480/매장[8, 8, 8]; lit302/dark178/warm180/cool61. 실제 메시에 degenerate triangle 없음, finite vertex/unit normals/UV·material 공유/좌우 글자 방향/동선 PASS |
| 실제 시간 전환 | 16개 실제 scene 전환·retarget 조건,15/30/60/120Hz shader emission oracle·숨김 freeze·반복 target·18 기존 날씨/시간 endpoint PASS |
| 예산 | 기본108,258tri/1275renderer/47material. 100우산 최대119,634/1443/47; strict <120000/<2400/<48 유지. 0.8.0 최대119394/2331/46 대비 실제 renderer 수 감소 |
| 기존 차량·보행·날씨 | 24대4모델/600초 차량 검사/최소 gap46.754658m, vehicle detail49,080tri/14renderer/car PASS. 보행4/32/100명200/600/600초/223횡단/33신호주기, 날씨 각810초/퇴장92/복귀92/100우산96spray/32나무·신문지3~5초 PASS |
| 실제 GPU 화면 | D3D11 URP Editor PNG28장: 기존 도시14·차량8(540×1200), 상가3종 낮/밤6(960×540). 최종 source 실제 상가6장과 도시 야간·노을 전환을 관찰했다. 임시 카메라/차량 포즈는 저장하지 않으며 APK phone camera는 유지. 폰 screenshot/FPS 증거 아님 |
| Android | [BuildApk38017884342](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38017884342),job114112125299 success. 실제BuildPlayer 결과Succeeded/오류0; launcher Lint errors0/warnings8 |
| 변경 없는 host | NativeAndroid/Bridge/AndroidWallpaperBuild/Atmosphere.shader 입력 SHA 불변. 기존 실제host Lint38008378207(0errors/10warnings/nativeFilesMatched8) 재사용. 신규host Lint를 실행했다고 주장하지 않음 |
| 내려받은 APK | API ZIP digest/bytes·CRC와 APK SHA/bytes 일치. 실제aapt 0.9.0/code10/min29/target36/ARM64/SettingsActivity launcher, BIND_WALLPAPER/:wallpaper/exported서비스/meta/비공개provider/disabledUnityActivity, 원본 v2 certificate PASS |

launcher 기존 경고8와host 경고10은 오류가 없다는 결과와 구분한다. 실제폰0.9.0 화면·설정 보존·홈/잠금·숨김 복귀·재부팅·FPS/발열 검사는 사용자 설치 후 확인이다.

### 산출물·소스 추적

- 최종source **52ba0b3fc3f47509341be82ec9ebfb16ebfde29e**, tag **unity-apk-0.9.0-build1**. 최초source/실패 수정 내역 및 최종76입력 SHA는 `/workspace/artifacts/unity-0.9.0-source-manifest.json`에 기록한다. 기존 real index/native dirty 상태를 보존했고 임시 index whitelist로 게시했다.
- APK `/workspace/artifacts/pixel-traffic-unity-prototype-0.9.0.apk`, **29777431bytes**, SHA256 **ea2be19cbcc598dd40565216a8d69038f517357e7ab2ec826adda8a5f81a6d90**. 원본 v2 cert **a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6**.
- Validate artifact11655984967/SHAa7414949574bb924d3b03bee1553a1ff8ad713170cdd872b8e80f5c472ebc058; APKartifact11656568877, build reportsartifact11656858746. 실제reports `/workspace/artifacts/unity-prototype-0.9.0-reports`, 다운로드 검증 `unity-prototype-0.9.0-download-verification.json`. 원격7일artifact와 특정cloud 첨부파일이 다음세션에도 남는다고 가정하지 않는다.
- 문서-only 최종8파일 게시 후76입력 SHA가 같으면 검사를 반복하지 않는다. 문서commit은source-manifest에 별도 기록한다.

### 집 PC 설치

APK를 `C:\Users\김백현\Desktop\AI`에 다운로드하고 아래 명령을 실행한다.

```powershell
$unityAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $unityAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.9.0.apk"
if ($LASTEXITCODE -eq 0) {
    & $unityAdb shell am start -W -n "com.s20plus.pixeltraffic.unityprototype/com.s20plus.pixeltraffic.unitywallpaper.WallpaperSettingsActivity"
}
```

설정에서 배경화면에 적용한 뒤 낮·노을·밤의상가/창문·비/우산·차량/보행·설정 보존을 폰에서 확인한다. 다음 후보는 도로의 정류장·벤치·생활 소품이며 최대100우산 예산 여유를 실제 계산한 뒤 적용한다.

## 2026-10-10 — Unity 0.9.0 실제 렌더에서 간판 반전 발견·수정

source47cdcc68a3657100c74e0431ea9da8b72e65829a의 실제Validate38017177473/job114109948455는scene/previewPASS,artifact11656294283/12496027bytes/SHA681d21462d39c674e1d8a1e481c7212308ba469bbea7c90460f79cb6548e5b0b이다. 기본108258tri/1275renderer/47material,최대100우산119634/1443/47로예산PASS. 24건물/480창문(302lit,178dark,180warm,61cool),매장3종8개씩/16실제전환/15·30·60·120Hz 및기존차량/보행/날씨검사PASS.

실제상가낮/밤6장관찰에서글자가좌우반전됨을확인했다. 기존UV검사가 잘못된카메라right방향 가정을공유했으므로PASS를시각확인으로대체하지않았다. FrontageScene의좌우UVflip을반대로고치고FrontageChecks를카메라forward/up cross제품으로검사하게수정했다. CityPreview의카메라를낮고반대쪽도로로옮겨Cafe간판나무가림을줄인다. 변경3소스만재게시하며실제폰camera나거리geometry를바꾸지않는다. 최종같은source Unity재검사/PNG관찰전에는APK태그를만들지않는다.

## 2026-10-10 — Unity 0.9.0 상가·창문 조명 구현 / 원격 게시 승인 확인·Windows 검증 시작

- 사용자가 이번 수정본을 BACKHYUN96/S20-PLUS main에 게시하고 연결된 PC의 자동 검증·실제 화면 캡처·서명 APK 빌드까지 진행하도록 명시 승인했다. 직전 main/기능 브랜치 push는 자동 승인 검토에 차단됐으며 실행되지 않았다.
- 사용자 기능 승인: 1층 카페·편의점·일반 매장, 켜짐/꺼짐 및 따뜻한/차가운 창문 조명. 0.8.0 폰 적용 여부는 확인되지 않았으며 최신 실기기 적용 확인은 0.7.0이다.
- 24개 건물의 480개 창문을 건물별 프레임/유리 메시로 합치고, 공유 256×128 창문 및 512×512 상가 색상/발광 atlas를 Editor에서 생성한다. 카페·편의점·매장 각각 8개, 문·손잡이·진열대·간판·OPEN 포스터를 추가한다. 좌우 간판 UV 방향을 달리해 글자 반전을 방지한다.
- 기존 4초 전환에 노을 창문 52%·매장 86%, 밤 100% 발광을 연결한다. 기존 낮→노을→밤과 밤→낮, 재선택/중단/숨김 정지가 유지된다. 추가 실제 조명 없이 공유 재질 노출만 변경한다.
- 수정 범위: CityEnvironment/ClimateScene/CityClimate/StarterConfig/StarterScene/CityPreview, FrontageScene/FrontageChecks 및 신규 meta 2개. 상가 낮/밤 6장과 기존 도시·차량 22장을 실제 URP GPU로 캡처하도록 구성했다. 임시 카메라/차량 포즈는 저장하지 않는다.
- 모바일 예산 <120000 triangles / <2400 renderers / <48 materials와 실제 조명 4개를 유지하며 예산 검사·전환 oracle·공유 재질·UV·기하·보행 동선 검사를 추가했다. 정적 예상 최대 우산 예산 119634/1443/47은 측정 결과가 아니다.
- 시작 Git 상태/기존 파일 해시: /workspace/artifacts/unity-0.9.0-start-state.json. 소스 76개 입력 해시 및 범위: /workspace/artifacts/unity-0.9.0-source-manifest.json. NativeAndroid 등 관련 입력은 0.8.0과 동일하여 host Lint 38008378207 결과(0 errors/10 warnings)를 재사용한다.
- 다음: 동일 소스 Windows Unity 6.3 LTS 검증·실제 상가 낮/밤 이미지 확인 후 기존 서명 ARM64 APK 빌드, launcher Lint·서명·manifest·다운로드 SHA 검증. 아직 Unity/GPU/Android 검사와 0.9.0 실기기 검사를 통과했다고 주장하지 않는다.

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

## 2026-10-09 — Unity PC 자동 검사 연결

사용자 Windows PC에서 수정 실행기의 `Unity Validate completed` 및 GitHub runner 2.337.0의 `Connected to GitHub` / `Listening for Jobs`를 화면으로 확인했습니다. Unity 6000.3.26f1 프로젝트와 공통 PowerShell pipeline을 `pixel-traffic-unity/`에 추가하고 main의 Unity 관련 변경을 자동 Validate에 연결합니다. main 수동 Validate/BuildApk도 지원하며 PR 코드는 자동 실행하지 않습니다. runner 라벨은 self-hosted/Windows/X64/pixel-traffic-unity입니다.

이번 게시 범위는 Unity 프로젝트·workflow·관련 문서입니다. 기존 native Android 앱 변경은 포함하지 않습니다. workflow의 main 제한·Validate 기본값·보고서 허용 목록 및 source 제외 항목을 정적으로 확인했습니다. 실제 첫 GitHub Actions 작업은 main 반영 후 확인해야 합니다. Unity APK·기기 실행·WallpaperService 연결·자동 캡처는 아직 검증하지 않았습니다. 기존 인증서 검증 후 APK를 공유하도록 구성했고 키/암호/원본 로그는 게시하지 않습니다.

[PC 실행과 결과 확인](UNITY_AUTOMATION.md), [전환 과정](UNITY_MIGRATION.md). GitHub source 게시, main 반영, Actions 성공은 각각 실제 결과를 따로 기록합니다.

기준일: 2026-10-07. 저장소: https://github.com/BACKHYUN96/S20-PLUS . 앱 패키지 `com.s20plus.pixeltraffic`, versionCode 29/versionName 0.29.0, minSdk 29/targetSdk 35. 소스 체크포인트 태그는 `pixel-traffic-v0.29.0`이며 커밋은 `82c5cbdcdaf17d408565b26addf01411792a3f73`입니다. 이 소스와 태그의 GitHub 보존을 확인했습니다. 이후 인수인계 완료 문서는 main에 추가합니다. [STATUS](STATUS.md)의 최신 항목을 확인하세요.

## 새 채팅에 붙여 넣기

```text
BACKHYUN96/S20-PLUS 저장소의 main에서 픽셀 트래픽 개발을 이어가자.
먼저 AGENTS.md, docs/HANDOFF.md, docs/STATUS.md의 최신 항목과 docs/PATCH_GUIDE.md를 읽어줘.
현재 0.29.0은 내가 설치해서 잘 적용됐다고 확인했어.
모든 작업은 README·STATUS·WORK_LOG에 기록하고, 관련 파일/검사만 확인해서 중복 작업과 크레딧 사용을 줄여줘.
APK를 제공할 때 내 Windows 폴더의 정확한 PowerShell 설치 명령도 함께 줘.
다음 패치는 아직 정하지 않았으니 현재 상태를 파악한 뒤 추천해줘.
기존 앱을 업데이트할 빌드에서는 docs/BUILD_RESTORE.md에 따라 기존 서명키를 보존해줘.
```

## 현재 구현과 사용자 확인

사용자가 0.29.0을 설치해 잘 적용됐다고 확인했습니다. S20+와 S26 Ultra에서 이전 버전들도 사용했습니다. 기기별 모든 조합과 FPS·전력 정밀 측정은 아직 완료하지 않았습니다.

- Android 라이브 배경화면, 설정, 실시간 미리보기와 최근 진단. 기본 도심/해안/산길/고속도로, 오리지널 차량 6종, 밀도·속도·프레임/절전 연동·밝기·구도·프리셋 가져오기/내보내기.
- 전용 도심은 540×1200 논리 좌표를 사용합니다. 배경 PNG 12장(낮/노을/밤 × 젖음/마름/눈/안개)과 차량 6종 앞뒤 아틀라스 PNG 1장을 Hardware Canvas로 합성합니다. 선택/전환에 필요한 배경만 캐시합니다. 배경에 그려진 빛과 수면은 정적 이미지입니다.
- 양방향 무한 주행, 실측 차선/원근과 완전 차체 아틀라스, 날씨/시간대의 4초 활성 전환, 비/눈/안개·노면 반사/물보라와 작은 풍경 이벤트. 숨김/화면 꺼짐에는 활성 시계가 멈춥니다.
- 교통 신호는 초록 14초/노랑 3초/빨강 9초입니다. 차체 앞끝 기준 정차, 간격 유지, 부드러운 제동, 순차 출발과 브레이크등을 구현했습니다. 보행자는 아직 없습니다.
- 라이팅은 승용/SUV/택시 6500K LED, 스포츠쿠페 6500K 전조등+6500K 안개등, 버스/트럭 4500K 전조등+3000K 안개등입니다. 화면 RGB 근사이며 양방향 전방의 부채꼴 노면 비춤, 실제 램프·후미등/제동 반사·가로등과 국소 창문 발광을 합성합니다.
- 0.29 수정: 같은 차종/깊이의 폭은 모든 차선에서 같고 같은 방향은 길이도 같습니다. 차선 폭마다 크기가 달라지던 문제를 제거했습니다. 반대 방향 비춤은 전체 차체 앞끝에서 지평선 쪽으로 향하며, 뒤에 보이는 램프는 붉은 후미등입니다.

## 가장 먼저 지킬 결정

차체를 매 프레임 차선에 맞춰 다시 축소하지 않습니다. 크기/길이는 고정된 연속 깊이 프로필이며 전체 차체의 할선 기울기를 사용합니다. 신호 정차는 실제 차체 앞끝으로 계산합니다. 크기 프로필이 바뀌면 기하와 48가지 장기 주행 궤적·실제 신호/차체 픽셀을 다시 검사합니다. 차량 종류/팔레트별 아트 비율도 유지합니다.

매 패치마다 프로젝트와 WORK_LOG 전체를 다시 읽지 않습니다. 최신 진입점 → 요청 관련 파일/호출 관계 → 선택 검사 순서로 진행하고, 통과한 검사에 입력 변화가 없으면 반복하지 않습니다. 임시 class/cache가 실제 있을 때만 재사용합니다. 새 환경에 `/tmp`, `/workspace/artifacts`나 외부 빌드 도우미가 보존된다고 가정하지 않습니다.

실제 차 브랜드 복제, 실제 기상·위치 동기화, 보행자·횡단은 미구현입니다. 다음 후보는 보행자·신호 대기/안전 횡단과 현재 조명의 연결이지만 아직 선택되지 않았습니다. 보행자를 구현한다면 노랑에 진입한 차나 긴 버스까지 횡단 구역을 완전히 비운 뒤 진입하도록 설계해야 합니다.

## 빌드·설치와 검증

[빌드/서명 복원](BUILD_RESTORE.md)을 사용합니다. JDK 17 이상(현재 21.0.8), Gradle 8.11.1/AGP 8.9.2, Android SDK 35/BuildTools 35.0.0이 필요합니다. `pixel-traffic/tools/build-cloud.py`는 현재 체크아웃 위치와 기존 프록시/CA·도구를 사용합니다. 개인 서명키는 Git에 없으며 기존 키가 다르면 업데이트 빌드를 중지합니다.

0.29 검증은 기하/차체 573,480프로필·672픽셀, 주행 48궤적/3,110,400표본, 색온도/조명 10,020표본·양방향 비춤/정차쌍, 효과/시간대·노면/날씨 캐시·신호와 99합성이 통과했습니다. Android 빌드/Lint와 기존 v2 서명·버전·PNG 13개 일치도 확인했습니다. 실기기 GPU 완료 시간/실제 표시 FPS·전력·발열의 정밀 측정은 미완료입니다.

현재APK SHA256: `a04bfa9d27b9687f6af6feb41a3f944d5f8cc10355ceae73554ccc11688d978a` (37,460,214bytes). 서명인증서SHA256: `a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`.

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r "C:\Users\LNS HRD\Desktop\블랙\AI\새 폴더\pixel-traffic-0.29.0-debug.apk"
```

새 버전마다 실제 버전/파일명으로 바꿉니다. 사용자 컴퓨터의 PowerShell/USB 스마트폰은 이 클라우드에서 직접 제어할 수 없습니다. 루팅/인터넷 권한은 이 앱에 필요하지 않습니다.

## 자료 보존 범위

소스·검사·문서·Gradle Wrapper·PNG 13개를 Git에 보존했습니다. 이전 패치별 설계/실패/검증은 WORK_LOG에 있지만 각 버전의 독립 Git 커밋으로 재구성하지는 않았습니다. 최신 0.29 소스 스냅샷부터 Git 보존을 시작했습니다.

프로젝트 백업 `S20-PLUS-0.29.0-handoff.zip`에는 Git bundle, 최신 APK, 야간 합성, 복원 안내와 manifest가 들어갑니다. 서명 백업 `S20-PLUS-0.29.0-signing-backup.zip`은 별도 개인 보관용입니다. Git이나 공개 공유에 넣지 않습니다.

채팅 첨부의 원본 시안·기기 스크린샷과 레드존 원본 APK/CSV ZIP은 저장소에 없습니다. 관련 조사 결과는 WORK_LOG에 있고 레드존 원본 소스는 확보하지 못했습니다. 포켓프렌드 원본 프로젝트도 이 저장소에 없습니다. 새 채팅이 이 자료나 과거 대화를 기억한다고 가정하지 않습니다.

## 이번 복원 확인과 환경 설정

소스 체크포인트를 새 폴더에 clone하고 저장소의 `tools/build-cloud.py`로 Android 빌드/Lint를 실행했습니다. 19초에 성공했고 Lint 이슈가 없었습니다. APK의 SHA256이 기존 전달 파일과 완전히 같았으며, 버전·기존 v2 서명·PNG 13개도 일치했습니다. 기존 JDK/SDK/의존성 캐시와 서명키를 재사용한 검사입니다. 새 머신에서 SDK를 처음 설치하거나 실제 스마트폰을 테스트한 결과는 아닙니다.

클라우드 설정의 `start_skill`에 이 저장소의 시작·서명·범위별 검증 절차를 저장했습니다. 저장 결과는 확인됐지만 초안이며 자동 적용/게시되지는 않았습니다. 재사용 환경에 활성화하려면 환경 설정에서 변경 사항을 검토·저장한 뒤 Publish합니다. 저장소와 다운로드 백업의 보존은 이 게시 작업과 별개입니다.
