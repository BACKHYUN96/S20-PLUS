# Unity 자동 빌드 — PC에서 시작하고 클라우드로 이동하기

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
