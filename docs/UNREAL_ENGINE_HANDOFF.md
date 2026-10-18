# Pixel Traffic — Unreal Engine 5 개발 인수인계
작성: 2026-10-18 (한국 시각). 대상: 새 채팅·개발자가 Unity 결과를 기준으로 UE5 버전을 설계하고 구현하는 작업.

## 1. 요청과 범위
사용자는 현재 만든 한국 도심 라이브 배경화면의 컨셉과 누적 기능·버그 수정 내역을 UE5 개발로 이어갈 자료를 요청했다. 이 문서는 실제 저장소와 대화의 요구를 정리한 이식 설계이며 UE 프로젝트 구현·빌드 완료를 의미하지 않는다. Unity 중단·삭제 또는 UE용 앱 식별자·서명 변경을 결정한 것은 아니다.

읽는 순서: 이 문서 → [STATUS](STATUS.md) → [Unity 인수인계](HANDOFF.md) → [0.43 전용 기록](UNITY_043_HANDOFF.md). 역사·세부 수치·실패 해결은 [WORK_LOG](WORK_LOG.md), 실행 절차는 [UNITY_AUTOMATION](UNITY_AUTOMATION.md), 서명 복원은 [BUILD_RESTORE](BUILD_RESTORE.md). 이 저장소에는 기존 Java Android 앱 pixel-traffic/와 Unity 앱 pixel-traffic-unity/가 공존한다. 버전 번호를 혼동하지 않는다.

## 2. 현재의 실제 기준
- 마지막 검증·서명 APK 전달 완료: Unity 0.42.0 / versionCode43. source225a4c7fe7e9b53641dbef71ca063623c7a4fd1d, 완료 문서d1eceb1f457b653ee2e82fa356a9d9cf68f27b6a.
- 0.42 실제 PC Unity·190 GPU PNG·Android 빌드/Lint·원본 서명·공개 다운로드 확인. 폰 적용 확인은0.41까지 기록되어 있으며0.42 폰 성공을 추정하지 않는다.
- 최신 구현 소스: Unity0.43.0/code44, source4de8edd4e6f0245eb3318993b1479cdf781409bb, 진행 문서115fc85. 도로·보도·경계석 재질 개선.
- 2026-10-18 GitHub API 재확인: [0.43 검증 run38672418296](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38672418296)는 completed/cancelled. 과거 문서의 queued는 당시 관측이다. 0.43 새 APK·실제 Unity/GPU 성공 증거는 없다. 이식 시 검증 완료0.42와 미검증0.43 변경을 분리한다.
- UE5 엔진 버전·설치·프로젝트·러너·Android 패키징은 이번 작업에서 설정하거나 검증하지 않았다.

[0.42 APK](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/c23848da0f84725b47c7fc39106ce064368af8c9/pixel-traffic-unity-prototype-0.42.0.apk)
· [실제 도시 낮](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/c23848da0f84725b47c7fc39106ce064368af8c9/finish-city-day.png)
· [실제 외벽 낮](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/c23848da0f84725b47c7fc39106ce064368af8c9/finish-facade-day.png)
· [실제 외벽 밤](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/c23848da0f84725b47c7fc39106ce064368af8c9/finish-facade-night.png).
APK SHA256:65f54c4c1e13e43f48d66fd5a7e6b722be48abe93f828c691f007d17e201db6d.
이 PNG는 현재 Unity 결과이며 최종 시안이 아니다.

## 3. 최종 시안과 제품
제품은 운전 조작 게임이 아닌 자동 교통과 날씨가 움직이는 Android 세로 라이브 배경화면이다. 사용 기기 사진은 Samsung Galaxy S20+ SM-G986N,1440×3200. 실제 사용할 해상도·OS·GPU·지원 API는 개발 시 장치에서 확인한다.

최종 시안: 높은 곳에서 완만하게 내려다보는 한국 도심 도로. 전경 차량·횡단보도와 보행자, 양쪽 밝은 석재 계단형 건물·상가·발코니·옥상 식재, 풍성하고 세밀한 녹색 가로수, 중경 낮은 시가지, 뒤쪽 강과 다리·대도시 스카이라인·청록/푸른 산 능선, 선명한 파란 하늘과 작은 흰 구름. 따뜻한 햇빛·차가운 그늘·나뭇잎 그림자·노면 입자가 핵심이다. 초기에 만든 단순 로우폴리 모양 자체를 최종 목표로 고정하지 않는다.

시안 원본은 대화 첨부 이미지다. 현재 GitHub에 원본을 보관한 경로는 확인되지 않았다. 6650.jpg 등 과거 첨부 경로는 환경 한정이므로 존재를 가정하지 않는다. 새 채팅에서 동일 이미지를 다시 확보한 뒤 저장소의 승인된 Reference 폴더에 원본·용도·출처를 기록한다. 임의로 다른 시안을 생성해 기준을 바꾸지 않는다. 9:16에 가까운 시안과9:20 기기 화면은 잘라 맞추되 도로 폭·원근·강/산/하늘 높이의 비율을 비교한다.

로터리는 사용자 요청으로 도로 끝에 추가했다. 도시 전체 구도와 로터리 보기 옵션을 모두 유지한다. 시안과 달라 보인다는 이유로 로터리나 동작을 삭제하지 않는다.

## 4. 반드시 이어받을 동작
| 영역 | 유지할 동작·요구 | Unity 근거 (Assets/PixelTraffic/ 아래) |
| --- | --- | --- |
| 교통 | 왕복4차선,6차종 혼합24대 기준, 속도·차량 수 설정 및 기후 반응, 재등장 객체 재사용 | Runtime/StreetModel.cs, StreetSimulation.cs, PrototypeDrive.cs; Editor/TrafficFleet.cs |
| 차선변경 | 간헐적으로 방향지시등3회 후 부드럽게 변경. 목표 차로 뒤차가 있으면 충분히 지나간 다음 합류. 차량 길이·앞뒤 간격 고려 | Runtime/LaneChanges.cs; Editor/DrivingChecks.cs |
| 차량 | 일반차·택시·스포츠카·버스·박스 트럭 등 기존6차종. 정확한 모델 ID/구성은 TrafficFleet와 VehicleGeometry 확인 | Editor/VehicleGeometry.cs, TrafficFleet.cs, HeavyVehicleChecks.cs |
| 전조등 | 타원형 풀 대신 램프에서 앞으로 넓어지는 두 부채꼴, 가장자리/거리 감쇠, 진행 방향 일치 | Runtime/VehicleLighting.cs; Shaders/HeadlightFan.shader |
| 라이트 색 | 버스/트럭3200K 노란빛, 일반 승용 흰색, 스포츠7500K(사용자 요구6500~8000K), 램프/빔/반사 색 통일 | Runtime/VehicleLighting.cs |
| 후미등 | 주행 후미등과 감속·정차 브레이크등 구분 | Runtime/VehicleLighting.cs |
| 버스 | 양쪽 정류장, 대기 승객, 감속·정차·문 열림·승하차·문 닫힘·안전 출발. 승하차/문 닫힘 단계 비상깜빡이 | Runtime/BusStops.cs, BusDoors.cs; Editor/BusStopScene.cs, BusStopChecks.cs |
| 와이퍼 |6차종 유리 면에 맞는48개, 창문과 구분되는 회색. 약비 간헐/강비 빠른 왕복, 그친 뒤 마지막 닦기와 아래 정지, 정차 중도 작동 | Runtime/VehicleWipers.cs; Editor/WiperChecks.cs |
| 신호 | 한국식 가로형 차량 신호등·사람 모양 보행 신호. 차도 바로 옆 보도 라인에서 기둥, 가로등과 위치 기준 공유 | Editor/StreetScene.cs, StreetChecks.cs |
| 횡단보도 | 차량 신호/보행 신호/보행자·차량 정차 연동. 횡단보도 구간의 줄무늬 사이에도 중앙선/dash 없음 | Runtime/StreetModel.cs; Editor/CityEnvironment.cs |
| 보행자 | 보도 이동·대기·횡단·정류장 이동. 한 점에 겹치거나 바글거리는 경로 방지. 우산과 우비 혼합 | Runtime/SidewalkRoutes.cs, StreetSimulation.cs; Editor/StreetChecks.cs, WeatherLifeChecks.cs |
| 시간 | 낮/노을/밤 연속 전환, 낮→밤은 노을 경유, 밤→낮은 직접 전환 허용 | Runtime/SceneBlend.cs, CityClimate.cs; Editor/ClimateChecks.cs |
| 날씨 |6상태(맑음/약비/강비/눈/안개/폭풍: 정확한 enum·표시는 설정 코드 확인), 연속 전환, 보행·교통 밀도와 속도 반응, 바람·우천 생활 표현 | Runtime/CityClimate.cs, ClimateEffects.cs, WetTraffic.cs |
| 노면 | 아스팔트·보도·경계석·맨홀·배수구·타이어 흔적·차량 접지. 젖음/라이트에 따른 부드러운 반사, 비가 올 때 작은 수면 움직임 | Runtime/RoadSurface.cs; Editor/GroundScene.cs; Shaders/RoadOverlay.shader |
| 건물 | 전경 창틀/창턱·발코니·화분·실외기·상가. 켜진/꺼진방과 따뜻한/차가운 조명, 시간·날씨에 반응 | Editor/ArchitectureScene.cs, FrontageScene.cs, FrontageChecks.cs |
| 배경 | 강의 낮/노을/밤 반사·다리·도시 스카이라인·산 다층·구름/해/달/별·기후 가시성 | Runtime/DistantBackdrop.cs; Editor/DistantScene.cs, DistantChecks.cs |
| 카메라 | 도시 전체/로터리 보기 선택 및 저장. 원경 안개로 로터리가 완전히 가려지지 않음 | Runtime/CityCamera.cs, NativeAndroid 설정 |
| 생명주기 | 미리보기/홈 배경화면의 Surface 소유권, 숨김·화면OFF·pause에서 렌더/시간/입자 중지, 복귀 시 delta 폭주 방지 | Runtime/AndroidWallpaperBridge.cs, NativeAndroid/ |
| 설정 | 시간·날씨·교통·보행·FPS·카메라 등 현재 UI/저장 계약을 읽고 이식. 기존 Java 앱 기능과 Unity 기능을 혼동하지 않음 | NativeAndroid WallpaperPreferences/SettingsActivity/SettingsProvider |

상세 시간/속도/간격 수치는 C# 원본과 Checks를 권위 자료로 삼는다. 문서만 보고 추측한 값으로 덮어쓰지 않는다.

## 5. 로터리: 반복해서 수정했던 핵심
최종 사용자 의도는 입구 아래쪽 의미 없는 정체를 줄이고 여러 차가 충돌 없이 연속 진입해 배정된 첫 출구로 나가는 것이다.
- 한 대만 전체 교차로를 예약하는 방식 금지. 먼 출구 예약 때문에 빈 입구가 정차하지 않게 한다.
- 원주 전체 점유 대신 실제 근거리 합류·앞차 추종·회전한 차체 크기로 안전 판단한다.
- 동/서/남/북 무작위 출구와 카메라 쪽 복귀 경로. 배정한 출구를 지나 추가 한 바퀴 돌지 않는다.
- 사용자가 한때 항상5대를 요구했지만, 뒤에 불필요한 추가회전을 금지했다. 최신 의도를 우선한다:5대는 교통 공급의 목표이고 출구를 놓치게 만드는 강제 점유 조건이 아니다. 출입 중첩6대와 내부 정체·복구 검사를 참고한다.
- 낡은 차선변경 요청이 입구 차량을 묶지 않게 한다. 대형차의 실제 회전 차체, 버스 길이, 진입/진출 분리, 화단 clearance 포함.
- 0.42 회귀 기록:600초 완료125, 추가회전0, 최장 하단 대기 약4.03초. 이는 Unity의 특정 시뮬레이션 결과로 모든 조건 무대기를 보장하지 않는다. UE에서도 동등 시나리오를 재현한다.
근거: Runtime/RoundaboutTraffic.cs, StreetModel.cs, LaneChanges.cs; Editor/RoundaboutChecks.cs, RoundaboutScene.cs.

## 6. 업데이트 이력 지도
개별 모든 커밋·실패와 당시 설치 상태는 WORK_LOG/UNITY_AUTOMATION 및 Git 이력을 읽는다. 이 표는 개발 주제 색인이다.
| 버전 | 주제 |
| --- | --- |
|0.1~0.5.x|Unity 실험 앱·도시·왕복 교통·사람/신호·Android 배경화면 기반, 횡단보도 차선 겹침 수정|
|0.6~0.7|부드러운 시간·날씨, 낮→노을→밤과 생활 반응|
|0.8~0.10|차량 외형·건물/상가 조명·카메라/라이트/차선변경 발전: 정확한 변경 경계는 해당 이력 참조|
|0.11~0.13|시안 구도와 거리/풍경 정교화: 세부 내역은 당시 소스·WORK_LOG 참조|
|0.14|버스·트럭 혼합과 긴 차체의 안전 간격|
|0.15|정류장·문·승하차·비상깜빡이|
|0.16~0.17|강·다리·스카이라인·산·구름·해/달/별|
|0.18~0.21|도로 접지·부채꼴 전조등·차종별 색온도·빗길 반사 줄무늬 개선|
|0.22~0.23|한국식 신호·우비/우산·보행자 몰림·기둥 위치|
|0.24~0.25|와이퍼·회색 가시성·중복 차량 신호 보정|
|0.26~0.29|건물/거리 시각 개선 및 독립 보행 신호 중복 제거: 최종은 양쪽 결합 기둥 각1개|
|0.30~0.31|가로수 작은 군집·산 깊이|
|0.32~0.34|로터리 추가·야간 가시성·로터리 카메라 옵션|
|0.35~0.39|다중 진입·안전/정체·5대 공급·불필요한 추가회전 제거·낡은 차선 요청 수정|
|0.40~0.41|강변 연결 지형/계단/난간/건물 기단·로터리/외곽 조경·공중에 뜬 화단 보정|
|0.42|외벽 석재/물리 UV·유리 반사 느낌·창틀 마감, 최종 검증 전달|
|0.43|노면 잔골재·물리 크기 보도블록·경계석, 소스만 반영/검증 취소|

## 7. UE 구현 설계 제안
아래는 아직 구현하지 않은 제안이다. UE 안정 버전과 Android 지원 SDK/NDK/JDK 조합은 선택한 엔진 문서로 확정한다.
- C++의 독립 시뮬레이션 계층에 차·보행·신호·로터리·버스 상태기계를 둔다. 액터는 결과 위치/회전과 애니메이션을 표시한다. 먼저 전체 차량을 Chaos 물리로 운전시키는 방식은 기존 결정적 흐름과 검사를 옮기기 어렵다. 보간 표시와 고정 간격 모델 갱신을 고려한다.
- UWorldSubsystem 등으로 Traffic/Climate를 관리하고 DataAsset으로 차종 크기·라이트색·기후값을 정의한다. Material Parameter Collection/동적 Material Instance로 날씨·조명을 반영한다.
- 도로/로터리는 spline/명시적 진입·원주·출구 경로로 구성하되 단순 spline 겹침은 교통 안전 검사가 아니다. 방향·차체 간격과 출구 정책을 모델에서 처리한다.
- 나무/건물 반복은 ISM/HISM·LOD·재질 공유, 전경은 세밀한 메시, 원경은 단순화 자산. 화면에서 실제 보이는 실루엣부터 품질을 높인다.
- 모바일에서 다수의 실시간 shadow light/반사카메라를 무조건 켜지 않는다. 부채꼴 빔·접지·젖은 노면 반사는 모바일 호환 material/mesh 또는 지원되는 decal 방식으로 평가한다.
- Niagara는 비/눈 등에 사용할 후보이며 기기 성능·지원 여부를 실측한다. Lumen/Nanite/Virtual Shadow Maps/하드웨어 RT는 S20+ Android의 기본 전제로 두지 않는다. 데스크톱 화면을 폰 품질·발열의 증거로 취급하지 않는다.
- Unity1m→UE100cm. Unity(Y-up,Z-forward)→UE(Z-up,X-forward) 변환 예: (Xu,Yu,Zu)→(100Zu,100Xu,100Yu). yaw부호/메시전방/행렬/winding/법선은 단일 차량 테스트로 확인한다. CCT 색은 선형/sRGB 차이를 검사한다.

### 옮길 수 있는 것과 재작성할 것
C# 알고리즘·상태기계·수치·검사 시나리오는 C++로 포팅할 수 있다. MonoBehaviour/Unity Editor API·URP shader·.unity/.asset/.mat은 UE에서 직접 실행되지 않는다. 생성형 메시·texture는 Unity 생성기를 일회 실행하여 중립 포맷으로 내보내거나 UE 생성기로 재작성한다. 변환 가능 형식·라이선스·축·UV·LOD·material을 검사한다. FBX/glTF/PNG 등은 실제 지원되는 import/export 도구에 맞춰 선택한다. Unity 전용 코드를 UE완료로 간주하지 않는다.

## 8. Android 라이브 배경화면 선행 검증
UE Android 일반 게임 APK 패키징 성공은 WallpaperService 지원의 증거가 아니다. 선택 엔진 버전의 Activity/JNI/Surface/EGL 또는 Vulkan 생명주기에서 외부 wallpaper Surface를 지원할 수 있는지 최소 실험으로 검증해야 한다. 엔진 수정이 필요할 수도 있으며 현재 자동 이식 가능성은 미검증이다.

재사용 참고: pixel-traffic-unity/NativeAndroid/src/com/s20plus/pixeltraffic/unitywallpaper/ 의 PixelTrafficWallpaperService, WallpaperRuntimeHost, SurfaceArbiter, WallpaperPreferences, WallpaperSettingsActivity, WallpaperSettingsProvider. 이 코드는 Unity 연동부이므로 UE에 그대로 연결하지 않는다.

최소 통과 항목:
1. Android 일반 APK의 기본 3D 렌더와 기기 Vulkan/OpenGL ES 지원을 확인.
2. WallpaperService의 Surface 생성/크기변경/파괴와 UE 렌더 연결.
3. 미리보기와 홈이 동시/교대로 나타날 때 Surface 소유권, 검은 화면/크래시 없음.
4. 화면OFF·숨김·앱 전환에서 render/시뮬레이션 정지, 재개 시 시간 점프 없음.
5. 설정 변경·프로세스 재생성·반복 attach/detach·잠금화면 정책 확인.
이 경로가 막히면 원인·가능한 대안과 기능 차이를 사용자에게 보고하고 방향을 결정한다. 일반 전체화면 앱만 만들어 배경화면까지 완료했다고 하지 않는다.

UE 실험용 앱 ID는 Unity와 구분하는 것을 권장한다(값은 프로젝트 시작 때 결정). 기존 원본 서명키를 새로 생성/교체하거나 비밀값을 Git에 넣지 않는다. 같은 패키지 업데이트를 하려면 cert·versionCode·데이터 호환을 확인해야 한다.

## 9. 원격 개발 자동화와 전달
사용자는 에디터를 직접 계속 조작하지 않아도 채팅으로 개발·검증·APK 전달이 이어지기를 원한다.
- 현재 Unity Windows self-hosted runner/Unity6000.3.26f1 연결은 UE 환경 준비의 증거가 아니다.
- UE 설치 PC에서 선택 엔진·C++ 컴파일러·Android 도구체인 준비 후 commandlet/UnrealEditor-Cmd/AutomationTool BuildCookRun 기반 자동 검사·cook/package·실제 렌더 캡처를 만든다. 정확한 CLI는 엔진 버전에 맞춰 확인한다.
- commandlet 성공과 실제 GPU 캡처를 구분한다. 각 artifact에 source SHA·엔진 버전·기기/그래픽 API·빌드·검사 결과를 기록한다.
- runner는 설치된 집 PC에서 켜야 한다. 회사 노트북에는 D:\Unity\actions-runner가 없어 이전 명령이 실패했다. 경로는 그 PC의 설치 여부를 확인한 뒤 안내한다.
- APK는 사용자가 지정한 C:\Users\김백현\Desktop\AI 폴더에 다운로드·SHA256 확인·ADB 설치하는 전체 PowerShell 블록으로 전달한다. UE에서 사용할 adb 경로는 선택 SDK에서 확인하며 Unity SDK를 무조건 고정하지 않는다.
- 공개 APK 링크는 로그인 없는 직접 다운로드·bytes/SHA를 확인한다. 메일/Drive 전송은 과거 성공한 것으로 가정하지 않는다.

## 10. 개발 순서와 완료 기준
A. 시안 원본 확보·선택 UE버전·앱ID/빌드PC 확인, 기본 Android APK와 WallpaperService 최소 기술 검증.
B. 단순 메시로 카메라·도로·강/산/건물 비율을 시안에 맞춘다. UI겹침/9:20 실제 화면 비교.
C. 독립 교통 모델과 회귀 검사를 포팅: 신호/보행→차선변경→버스→로터리. 기능이 사라지지 않도록 목록별 추적.
D. 시간/날씨·조명·와이퍼·비/우비/반사·생활 반응을 포팅.
E. 가로수·빛·도로 / 건물·상가 / 차량·보행자 / 중경·원경을 큰 시각 묶음 단위로 정교화.
F. 폰 FPS·메모리·발열·배터리·장시간·pause/restart를 측정하고 LOD/효과를 조정.

Unity 참고값: 기본108608tri,최대119954tri,1598renderers,47materials(<110000/<120000/<2400/<48). 이는 Unity 검사 한도이며 UE에 무조건 같은 성능/한도를 적용하지 않는다. 목표30FPS에서 실제 frame time·CPU/GPU 병목·메모리·overdraw를 기기로 측정해 UE예산을 정한다.

수용 기준은 기존 Checks의 동작·안전 조건, 동일 시간/날씨/카메라 실제 캡처, 실제 Android 서명/manifest, 폰 생명주기 검증이다. 보행자 몰림, 중복 신호등, 횡단보도 차선, 반사 줄무늬, 로터리 단독 예약/추가회전/입구 정체는 필수 회귀 항목이다. 모든 기능이 이식됐다는 주장은 목록과 실행 증거로 확인한다.

## 11. 새 채팅에 붙여 넣을 시작 요청
> BACKHYUN96/S20-PLUS의 docs/UNREAL_ENGINE_HANDOFF.md를 먼저 읽고 Unity 픽셀트래픽의 UE5 버전을 개발해줘. 기존 Unity 프로젝트와 기록은 보존하고, 첨부 시안을 최종 그래픽 기준으로 삼아줘. Unity0.42는 검증 완료,0.43은 소스만 반영됐으며 검증 run은 취소됐다는 점을 구분해줘. 먼저 선택 UE버전·Android 빌드 환경과 WallpaperService 렌더 연결을 최소 실험으로 검증한 다음, 문서의 기능 목록·교통 회귀 조건과 그래픽 방향을 단계적으로 옮겨줘. 실제 작업 완료마다 검증 결과·미해결 사항·다음 추천 작업을 기록하고 알려줘.

이 문서 작성 작업에서는 게임/엔진/프로젝트 생성, Unity 검증 재실행 또는 UE APK 빌드를 하지 않았다. 다음 추천 작업은 UE의 Android 라이브 배경화면 최소 기술 검증이다.
