# Pixel Traffic Unity — 회색 와이퍼·신호 중복 수정 0.25.0

## 2026-10-11 — Unity 0.25.0 회색 와이퍼·중복 차량 신호등 수정: 검증·서명 APK 전달 완료

사용자는 폰0.24.0 와이퍼 움직임을 확인했지만 유리에 묻혀 잘 보이지 않는다고 했고, 같은 방향 차량 신호등이 두 개씩 나오는 사진을 제공했다. **0.25.0/code26**, `Visibility-0.25.0.unity` / `Generated/Visibility0250`로 새 장면을 생성했다.

**와이퍼:** 6차종24대/48날만 RGB(.52,.55,.58)의 중간 밝기 회색으로 바꿨다. 이미 사용하는 Headlamp 발광 재질을 공유하고 MaterialPropertyBlock으로 날에만 색을 준다. PropertyBlock은 장면에 저장되지 않으므로 VehicleWipers.Awake와ResetState에서 복원한다. Editor에만 색을 적용하려던 접근을 게시 전에 player load 복원으로 보완했다. 야간 조명에서 여전히 묻혀 보이는 실제 Validate38120423048 사진(Sedan/CityBus)을 보고 와이퍼48날에만 낮은 emission RGB(.075,.08,.085)을 보완했다. 새재질/Light/번짐효과는 추가하지 않는다. Validate38120927994는 trim emission조건에서 실패해 기존 CityClimate의 Headlamp 발광 경로로 바꿨다. 회색·낮은 emission·metallic.05/smoothness.2는 와이퍼 PropertyBlock에만 적용하며 그릴/기존 라이트 값은 바꾸지 않는다. shader keyword와 날의 emission 및 초기화 복원 경로를 검사한다. Editor에서 reserved Awake SendMessage assertion을 발견해 공개 RestoreBladeTint 경로를 Awake와ResetState에서 호출하고 검사도 그 경로를 직접 호출한다. 첫 실제 검사는 통과했고 이 보완은 야간 관찰에 따른 결정이다. 기존 타이밍·유리 안쪽 geometry·정차/일시정지·마지막 닦기 동작은 유지한다.

**중복 원인·수정:** StreetScene이 좌우×z7/13의4보행 신호 기둥 모두에 같은 방향 차량 head와 가로대를 생성했다. 차량 신호는 방향별 횡단보도 건너편에 한 개씩(x-3.38/z7, x+3.38/z13), 총2head/8렌즈/2가로대만 만든다. 보행 신호4head/8아이콘·4기둥은 유지한다. 이전 패치의 가로등과 같은 curb 위치 x±7.1, 보행 경로 장애물, 신호주기/차량 정차/보행 연동은 유지한다.

**범위·보존:** Editor DrivingScene/StreetScene/StreetChecks/WiperChecks/CityPreview/StarterScene, Runtime VehicleWipers/StarterConfig의8소스. 새 파일/재질/Light/Android host/workflow 변경 없음. 시작296파일·실제 index/status·122입력·host12SHA를 기록하고 무관한 사용자 변경을 보존했다. 중복 head를 삭제한 최대 강우/승객 장면은 119154triangles/1617renderers/47materials, 기존120000/2400/48제한 이하다.

**실제 Unity:** source `c506a38de943564e4452d6751e6f53ff3375f96e` / [Validate 38121450125](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38121450125). 회색48날/공유재질/유리 texture 대비와 PropertyBlock 삭제 후 player 초기화에 쓰는 RestoreBladeTint 경로의 복원 검사 PASS. 실제 topology의 방향별 head 유일성·장면 전체2head/2arm·4보행 기둥·8차량렌즈/8보행아이콘·6신호 상태 검사 PASS. 와이퍼 유리 간섭 6528정점, 15/30/60/120Hz 일정강우30초 동일 결과, 약비96회/강비884회, 마지막닦기48case/정차24차량/일시정지와 기존 보행·승하차·기후·노면GPU·CCT·차선 안전 검사도 PASS.

**실제 화면:** Unity6000.3.26f1/D3D11 140PNG를 받았다. 기존133장에 6차종 야간 회색 와이퍼와 비오는 밤 제품 카메라 신호 구도1장을 추가했다. 전체 구도·야간 방향별 신호 하나씩·6차종 회색 날의 유리 대비와 중간/최대 각도 사진을 직접 관찰했다. 와이퍼30장은 건조/중간/최대/비그친정지/야간×6차종이며, 중간/최대 GPU 변화픽셀은 [4296, 4429, 3149, 4245, 2662, 4426]다. 확대 사진은 임시 layer로 주변 가림을 제외한 실제 rig 촬영이며 저장된 폰 카메라를 바꾸지 않는다. 이 사진은 폰 캡처나 FPS 검증이 아니다.

**Android:** tag `unity-apk-0.25.0-build1` / [BuildApk 38121984513](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38121984513). BuildPlayer 오류0/경고0, launcherLint 오류0/경고8. artifact sourceSHA/bytes/SHA256/ZIPCRC와 실제 SDK apksigner/aapt 검사로0.25.0/code26/min29/target36/ARM64IL2CPP, wallpaper service/private provider/settings launcher/Unity Activity 비활성, 원본v2서명 `a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6` 확인. 변경 없는 host12SHA는 hostLint38008378207(오류0/경고10)을 재사용했다.

**전달:** [APK](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/fcf0acf3bebf48e99a6df22fdbaf36ea6c6ed662/pixel-traffic-unity-prototype-0.25.0.apk) · [야간 신호 전체](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/fcf0acf3bebf48e99a6df22fdbaf36ea6c6ed662/unique-signals-night-preview.png) · [승용 와이퍼](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/fcf0acf3bebf48e99a6df22fdbaf36ea6c6ed662/wiper-sedan-preview.png) · [야간 승용 와이퍼](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/fcf0acf3bebf48e99a6df22fdbaf36ea6c6ed662/wiper-sedan-night-preview.png) · [버스 와이퍼](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/fcf0acf3bebf48e99a6df22fdbaf36ea6c6ed662/wiper-bus-preview.png). APK 31153731bytes/SHA256 `ddcfb09d3ca0c943c09828f65605fb46795476f9bb271255bac2fb9ccf8612b4`. `downloads/unity-0.25.0`의8파일을 로그인 없는 HTTP200/no redirect/fullbytes/SHA로 검증했다. main에는 APK binary나 새 workflow를 추가하지 않았다. 최종 문서는 통과한122빌드입력을 보존한다.

PC 설치(아래 폴더에 다운로드한 경우):
```powershell
$trafficAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $trafficAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.25.0.apk"
```

미확인: 실제 폰0.25.0 가독성/신호 중복 해소/성능·발열·장시간 lifecycle. 사용자 폰0.24.0 와이퍼 움직임 성공은 확인됐다. 다음은 사용자 방침에 따라 시안의 건물 외관 그래픽이며 별도 요청으로 진행한다. PC Drive설정 보류 유지.

## 2026-10-11 — Unity0.24.0 차량 와이퍼: 실제 검증·서명 APK 전달 완료

사용자는 폰0.23.0 적용 성공을 확인하고 와이퍼 패치를 요청했다. **0.24.0/code25**, `Wipers-0.24.0.unity` / `Generated/Wipers0240`. 승용차·스포츠카·SUV·택시·버스·트럭의 실제 앞유리 모서리를 기준으로24대/48날을 배치했다. 유리의 기울기·폭·높이에 맞춰 길이와 회전면을 잡았고, 같은 차종끼리 메시를 공유한다. 각 날은 arm/blade2얇은 quad/4tri이며 기존 VehicleTrim 재질을 사용한다. 기존보다192tri/48renderer를 추가해 최대 강우/승객 장면 119346triangles/1637renderers/47materials, 기존120000/2400/48제한 미만이다. renderer 계약검사는 실제 추가2개에 맞춰 승용19/버스21로 갱신하되 메시·총량 제한은 유지했다.

**동작:** 약한 비(RainGain.38)는1.265초 왕복 후6초 대기, 강한 비(1)는.8초 연속 왕복이며 중간 세기는 대기시간/다음 왕복 속도를 조절한다. 부드러운 cos 왕복으로8도 아래쪽에서95도 위쪽까지 닦고 돌아온다. 차량별 첫 시작 지연은0~.7초다. 진행 중 속도 변경은 다음 왕복부터 적용해 각도가 튀지 않는다. 비가 그치면 진행 중인 왕복을 마치고, 간헐 대기 중이면 마지막1회를 닦은 뒤 아래쪽에 정지한다. 정차 중인 활성 차량도 동작한다. 건조와 눈에서는 작동하지 않는다. StreetSimulation의 같은 elapsed clock에서만 움직이고 pause/focus/wallpaper rendering gate를 따른다. 독립 Update/coroutine이나 실시간 Light/Collider는 추가하지 않았다.

**범위·보존:** 기존10C#(VehicleGeometry,HeavyVehicleGeometry,DrivingScene,DrivingChecks,VehicleDetailChecks,HeavyVehicleChecks,StreetSimulation,StarterScene,StarterConfig,CityPreview), 새 VehicleWipers/WiperChecks와2meta, 총14파일이다. 시작293파일/index/status·기존 host12SHA를 기록했고122빌드입력(기존118+새4)과 무관한 사용자 변경을 보존했다. 정적 구분자 검사에서 새2클래스의 namespace 닫는 brace 누락을 발견해 게시 전에 수정했다. 첫 실제 Validate38117305459는 DrivingChecks가 와이퍼 추가 전 renderer17/19를 기대해 Fleet light parts incomplete로 실패했다. 실제19/21로 계약만 갱신한 최종 Validate38117615753는 통과했다. 초기 실패와 수정은 아래 진행 이력에도 보존한다.

**실제 Unity 검증:** source `dffcdd1cc5a2a1c058cd64135431fa744ab86ab0` / [Validate 38117615753](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38117615753). 와이퍼 profile이 실제 glass vertices와 일치하며 모든 회전각에서 날이 유리 안/위에 있는지6528vertex를 검사했다. 15/30/60/120fps의30초 일정 강우 시 동일 왕복 횟수/각도, 약비 총96회/강비 총884회, 움직임·간헐 대기에서 비가 그친 마지막 닦기48case, 건조/0delta/강도 변화 연속성, 정차 중24활성 차량과 실제 street pause 연동 PASS. 기존 기둥4곳/신호6상태/4·32·100명 보행/기후·우비·승하차/CCT·부채꼴·노면GPU 검사도 PASS.

**실제 화면:** Unity6000.3.26f1/D3D11 전체133PNG 중 와이퍼24장(6차종×건조 주차/중간/최대/비 그친 정지)을 촬영했다. 같은 장면·카메라에서 runtime 와이퍼 각도만 바꾼 중간/최대 사진의 픽셀 변화[3646, 1230, 882, 3735, 1779, 1686]를 확인했다. 실제 제품 카메라와 승용/스포츠/SUV/택시/버스/트럭 확대를 직접 관찰했다. 확대는 주변 가림을 제거하는 임시 layer에서 실제 차량 rig를 촬영한 것이며 끝나면 배치·layer·기후·와이퍼·카메라를 복구한다. 제품에는 확대 카메라/임시 layer가 저장되지 않는다. 폰 스크린샷이나 FPS 측정은 아니다.

**Android:** tag `unity-apk-0.24.0-build1` / [BuildApk 38118234099](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38118234099). BuildPlayer 오류0/경고0, launcherLint 오류0/경고8. 내려받은 실제 APK의0.24.0/code25/min29/target36/ARM64IL2CPP, wallpaper service/private provider/settings launcher와 원본 v2인증서 `a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`를 SDK로 확인했다. 바뀌지 않은 host12입력은 기존hostLint38008378207(오류0/경고10)을 재사용했다.

**전달:** [APK](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/f47ead0ed58a0c2c45b2c94d3315ed67a6c032da/pixel-traffic-unity-prototype-0.24.0.apk) · [제품 구도](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/f47ead0ed58a0c2c45b2c94d3315ed67a6c032da/wiper-city-preview.png) · [승용 중간](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/f47ead0ed58a0c2c45b2c94d3315ed67a6c032da/wiper-sedan-preview.png) · [승용 최대](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/f47ead0ed58a0c2c45b2c94d3315ed67a6c032da/wiper-sedan-upper-preview.png) · [버스](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/f47ead0ed58a0c2c45b2c94d3315ed67a6c032da/wiper-bus-preview.png) · [트럭](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/f47ead0ed58a0c2c45b2c94d3315ed67a6c032da/wiper-truck-preview.png). APK 31156331bytes/SHA256 `58150d5242e9e812a7ebe14c76aad53f421c9df3e5ba687216921b818f132cc3`. root다운로드 branch `downloads/unity-0.24.0`의6파일을 로그인 없는 HTTP200/no redirect/fullbytes/SHA로 확인했다. main에는 APK binary나 새 workflow를 추가하지 않았다. 문서 전용 최종 게시가122빌드입력을 보존하므로 통과한 검사를 반복하지 않는다.

PC 설치(이 폴더로 다운로드한 경우):
```powershell
$trafficAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $trafficAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.24.0.apk"
```

미확인: 실제 폰0.24.0 와이퍼 가독성/움직임·FPS/발열/장시간 lifecycle. 폰0.23.0 적용 성공은 사용자 확인이다. 와이퍼 기능 패치를 마쳤으며 이후 사용자 방침에 따라 새 기능보다 시안의 건물 외관 그래픽부터 집중한다. 다음 그래픽 패치는 별도 사용자 요청으로 진행한다. Drive PC설정 보류는 유지한다.

## 2026-10-11 — Unity0.23.0 신호등 기둥을 가로등과 같은 차도 쪽 보도 위치로 이동: 검증·서명 APK 전달 완료

사용자는 신호등 기둥이 기존 가로등과 같은 차도 앞쪽 보도 위치에서 뻗어나오도록 요청했다. **0.23.0/code24**, `CurbSignals-0.23.0.unity` / `Generated/CurbSignals0230`. 신호등4기둥의 좌우 위치를 x±10.08m→±7.1m로 옮겨 가로등과 같은 보도 라인으로 맞췄다. 횡단보도 옆 z7/13m와5.8m 기둥 높이는 유지한다. 가로대는6.7m→3.72m로 줄여 차량 신호등은 원래 차선 위 x±3.38m/y5.61m에 남고, 보행자 신호등은 옮겨진 같은 기둥에 붙는다.

가로등·신호등 생성과 보행 경로는 공유 `CurbFurnitureX` 위치를 사용한다. 기존 기둥 위치는 열고 새 기둥 위치를 장애물로 등록했다. 실제 생성 장면에서 가로등과 신호등의 좌우/바닥 높이 일치, 차도 침범 없음, 가로대 끝 연결, 차량 신호 위치와 보행 신호 부착, 새 위치의 보행 경로 차단/기존 위치 개방을 검사한다. 보행자 확대 카메라를 새 위치에 맞추고, 보도·가로등·신호등을 함께 보는 실제 확대 촬영1장을 추가했다. 신호 주기/차량/승하차/날씨/우비 제어는 바꾸지 않았다.

**범위·보존:** StreetScene,StreetChecks,CityEnvironment,CityPreview,StarterScene,SidewalkRoutes,StarterConfig,StreetModel의8소스. 새 코드·재질·renderer는 추가하지 않았다. 시작293파일·실제 index/status와118빌드입력·host12SHA를 기록하고 무관한 사용자 변경을 보존했다. 최대 강우/승객 장면 119154triangles/1589renderers/47materials, 기존120000/2400/48제한 미만이다.

**실패·보정:** 첫 Validate38115121451은 기둥 배치 검사를 통과했으나100명600초 검사에서94번의 첫 횡단 실패를 검출했다. 첫 횡단 목표±24m는 진입 범위±18m 밖을 고를 수 있어 외측 나무 통로에서 retry/retreat 경로를 반복할 수 있었다. 첫 횡단 목표를 횡단보도±12m/보도 안쪽3열로 선택하도록 수정하고 검사 조건을 유지한 채 재검증했다. 신호등 기둥 자체 충돌로 단정한 것은 아니다.

**실제 검증:** source `4f457e38998fc68c0a84f614027c985ed1e0dcd6` / [Validate 38115426333](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38115426333). 기둥4곳·신호6상태,4/32/100명 최대600초 횡단·발 간격·장애물·차량 안전·pause/frame clock, 기후별 인원 복구810초, 우비25명/우산75명과 승하차/출발 검사 PASS. 횡단보도±18m 평균 인원 지표 [2.1500000953674316, 14.981111526489258, 21.314443588256836], 최소 발 간격 0.40000003576278687m. 기존18기후/180전환·96CCT·144부채꼴·젖은 노면16실제GPU검사도 PASS. 총109장의 실제 Unity6000.3.26f1/D3D11 렌더를 받았고 제품 구도/기둥 배치 확대/보행 신호 확대를 직접 관찰했다. 기둥 배치 확대는 전체 제품 장면을 다른 카메라로 촬영했고 보행 신호 확대는 주변 가림을 제거한 실제 rig 촬영이다. 폰 스크린샷이나 FPS 측정은 아니다.

**Android:** tag `unity-apk-0.23.0-build1` / [BuildApk 38115883636](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38115883636). BuildPlayer 오류0/경고0, launcherLint 오류0/경고8. 내려받은 실제 APK의0.23.0/code24/min29/target36/ARM64IL2CPP와 wallpaper service/private provider/settings launcher, 원본 v2인증서 `a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`를 SDK로 확인했다. host12입력이 바뀌지 않아 기존 hostLint38008378207(오류0/경고10)을 재사용했다.

**전달:** [APK](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/63b9b3fa94e886c7a290f60d9508fb744a7201fd/pixel-traffic-unity-prototype-0.23.0.apk) · [제품 구도](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/63b9b3fa94e886c7a290f60d9508fb744a7201fd/curb-city-preview.png) · [가로등·기둥 배치](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/63b9b3fa94e886c7a290f60d9508fb744a7201fd/curb-signal-preview.png) · [보행 신호](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/63b9b3fa94e886c7a290f60d9508fb744a7201fd/korean-pedestrian-preview.png). APK 31148335bytes/SHA256 `dd04aebc632b9f36e10cf89338673e48847a9b358073e3a8760b6e0be4d04146`. root다운로드 branch `downloads/unity-0.23.0`의4파일을 로그인 없는 HTTP200/no redirect/fullbytes/SHA로 확인했다. main에는 APK binary나 새 workflow를 추가하지 않았다. 문서 전용 최종 게시에서118빌드입력이 보존되므로 통과한 검사를 반복하지 않는다.

PC 설치(이 폴더로 다운로드한 경우):
```powershell
$trafficAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $trafficAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.23.0.apk"
```

미확인: 실제 폰0.23.0 기둥 외형/보행 경로·FPS/발열/장시간 lifecycle. 사용자0.21.0 적용성공은 확인했지만0.22.0 적용 성공은 명시적으로 확인되지 않았다. 와이퍼는 별도 다음 기능 패치로 남아 있으며, 이후 새 기능보다 시안 그래픽 개선에 집중한다. Drive PC설정은 사용자 보류를 유지한다.

## 2026-10-11 — Unity0.22.0 한국식 신호등·우비·보행자 몰림: 검증·서명 APK 전달 완료

사용자가 폰0.21.0 적용 성공을 확인하고 차량/보행자 신호등 참조와 우비·보행자 몰림 점검을 요청했다. **0.22.0/code23**, `KoreanSignals-0.22.0.unity` / `Generated/KoreanSignals0220`. 기존 기능과 제품 카메라를 유지하며 다음을 적용했다.

- 차량: 5.8m지주·6.7m가로대에 가로형4구 외함, 원형 렌즈와 차광 후드. 각 정면은 해당 차선의 접근 운전자를 향하며 빨강/노랑/직진초록은 실제 교통 상태와 연동한다. 좌회전 구는 미점등이며 새 좌회전 운행을 추가하지 않는다.
- 보행자: 같은 지주에 세로3단 차광 후드, 위 빨간 정지 사람/아래 초록 보행 사람 아이콘. 가운데는 미점등 공간이며 타이머/숫자 기능은 추가하지 않는다. 기존 보행자 진입 허용/차량 구역 비움 주기를 그대로 사용한다.
- 비 오는 보행자: 25%는 후드·긴 몸통·소매가 있는 우비, 나머지는 우산. 실제100명에서 우비25명·우산75명을 검사했다. 기존 공유팔레트/5뼈/renderer를 재사용하고 맑은 날 원래 메시로 돌아온다. 승객8명에도 같은 옷 규칙을 적용하며 우비 착용자는 우산을 들지 않는다.
- 몰림: 첫 횡단 전 예약 실패 시에도 횡단보도 주변만 반복 선택하던 경로를 수정했다. 대기 자리가 없으면 잠시 다른 구간을 걷고 재시도하며, Roam 상태10초 실제 변위1m미만이면8m반대 방향 경로로 물러난다. 나무 옆 외측 통로는 발 간격보다 좁았던3열 대신 .5m간격2열로 정리하고 방향별stream을 유지한다. 순간 이동/인원 제거로 가리지 않는다. 4/32/100명 장기 검사에서 횡단보도±18m 평균 인원은 [2.2166666984558105, 15.566666603088379, 19.22888946533203]이며,32/100명에서는 전체80%미만임을 확인했다. 이 수치는 정의한 장기 시뮬레이션 밀집 지표이며 사용자 폰의 특정 몰림 화면을 직접 재현한 결과는 아니다.

**범위·보존:** StreetScene,StreetSimulation,StreetModel,SidewalkRoutes,StreetChecks,WeatherLifeChecks,BusStopChecks,CityPreview,StarterConfig,StarterScene의10소스이며 새 코드 파일은 없다. 시작293파일·실제index·미추적 상태·118입력·host12SHA를 기록하고 무관한 사용자 변경을 보존했다. 우비/신호 메시 자산은 버전별 Generated에 생성하며 재질/실시간Light는 추가하지 않는다. 최대 강우/승객 장면 119154triangles/1589renderers/47materials, 기존 제한120000/2400/48미만이다. 차종CCT/부채꼴/젖은 반사/날씨/승하차/비상등 동작을 보존했다.

**검증:** source `ec221563729b9f301a186db1100b82dfb25b1bf1` / [Validate 38113184005](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38113184005). 실제 신호6상태의16차량/8보행 램프 제어·기하·아이콘,4/32/100명 최대600초 횡단/걸음 이동/발 간격/장애물/차량 안전·frame clock/pause,810초 기후별 인원 복구, 우비/우산 실제 mesh·색상·맑음 회복, 승하차/출발 안전 검사 PASS. 기존18기후/180전환,96CCT/144부채꼴/24wet색상,노면16실제GPU렌더링 검사도 PASS. 새 촬영6장 포함 전체108장의 실제 Unity6000.3.26f1/D3D11 PNG를 받았고 신호 색상·사람 아이콘·우비와 우산을 직접 관찰했다. 보행자 신호등과 우비 확대사진은 주변 가림을 제거한 임시layer의 실제 제품rig detail렌더이며 전체사진은 제품구도다. 우비 확대는 offscreen mesh교체 직후 same-frame skin culling/pose 갱신 지연으로 추정한 사진 누락을 피하기 위해 실제 스킨pose를 Bake한 임시Renderer로 촬영했다. 임시Renderer/mesh는 삭제하고 기존skin과 배치/카메라/layer를 복구하며 저장하지 않는다. Editor 촬영이며 폰 screenshot/FPS 측정은 아니다.

**실패·수정:** 첫 Validate38111482726은 CityPreview 촬영 변수 captured 이름 중복(CS0136)으로 컴파일 실패했다. koreanCaptured로 수정하고 실제 검사를 재실행했다. 다음 Validate38111613706은100명 나무 옆 통로 정체를 검출했다. SidewalkRoutes 간격과 방향 유지/정체 후 retreat를 보정하고 재검사했다. Validate38111916537은 이동·108사진 검사를 통과했지만 실제관찰에서 정면색순서와 확대사진가림을 발견해 보정 후 다시 검증했다. Validate38112523546은 신호사진/픽셀도 통과했으나 우비가 확대사진에 누락된 것을 관찰해 실제 BakeMesh 확대촬영으로 수정했다.

**Android:** tag `unity-apk-0.22.0-build1` / [BuildApk 38113857641](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38113857641). BuildPlayer 오류0/경고0, launcherLint 오류0/경고8. 실제 내려받은 APK의 버전0.22.0/code23/min29/target36/ARM64IL2CPP,wallpaper service/private provider/settings launcher와 원본v2인증서 `a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`를 확인했다. 바뀌지 않은host12입력은 기존 hostLint38008378207(오류0/경고10)을 재사용했다.

**전달:** [APK](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/65b1ebf4444cbf7ce42c557a11cc0b90c3fbc80f/pixel-traffic-unity-prototype-0.22.0.apk) · [제품 구도](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/65b1ebf4444cbf7ce42c557a11cc0b90c3fbc80f/korean-city-preview.png) · [차량 신호등](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/65b1ebf4444cbf7ce42c557a11cc0b90c3fbc80f/korean-vehicle-preview.png) · [보행자 신호등](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/65b1ebf4444cbf7ce42c557a11cc0b90c3fbc80f/korean-pedestrian-preview.png) · [우비·우산](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/65b1ebf4444cbf7ce42c557a11cc0b90c3fbc80f/raincoat-preview.png). APK 31144631bytes/SHA256 `366c5c4010924c1afe73f3fe767d4baeee193de3e06b82c891e8c959161d7d5b`. root다운로드 branch `downloads/unity-0.22.0`의5파일을 로그인 없는 HTTP200/no redirect/fullbytes/SHA로 확인했다. main에는 APK binary를 넣지 않았다.

PC 설치(이 폴더로 다운로드한 경우):
```powershell
$trafficAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $trafficAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.22.0.apk"
```

미확인: 실제 폰0.22.0 외형·문제 위치의 몰림 해소·FPS/발열/장시간 lifecycle. 사용자0.21.0적용성공은 확인됐으나 성능 수치를 측정한 것은 아니다. 와이퍼는 별도 다음 기능 패치로 남아 있다. 이후 사용자 방침은 새 기능보다 시안의 그래픽 개선에 집중하는 것이다. Drive PC설정은 보류를 유지한다. 최종 문서 전용 게시가118입력을 바꾸지 않으므로 통과한 검사를 반복하지 않는다.

## 2026-10-11 — Unity 0.21.0 빗길 반사: 실제 GPU 검증·서명 APK 전달 완료

사용자가 0.20.0 적용 성공을 확인하고 빗길·가로등 반사 개선을 승인했다. **0.21.0/code22**, `WetReflections-0.21.0.unity` / `Generated/WetReflections0210`. 가로등·후미등 반사의 규칙적인 sine 띠를 world 좌표에 고정한 부드러운 noise로 교체했다. 차량 전조등의 젖은 반사도 같은 노면 패턴을 사용하며, 비가 있을 때만 작은 움직임을 섞는다. 미세 패턴은 화면 거리/해상도에 따라 derivative로 필터링한다. 반사 gain은 wet*(.28+.72*wet)로 조절하고 최대 강도를 낮춰 젖은 도로에서도 차선·횡단보도의 과한 번짐을 줄인다. 기존 날씨의 4초 blend를 사용하며 별도 물리적인 물 고임/장기 건조 모델은 추가하지 않는다.

- **범위·보존:** RoadOverlay.shader, RoadSurface, StarterConfig, GroundScene, CityPreview, StarterScene의 6파일이며 신규 파일은 없다. 시작 상태 293파일·실제 Git index·전체 미추적 상태와 118 빌드/촬영 입력을 기록하고 관련 없는 사용자 변경을 보존했다. Native Android·host·workflow 12 입력 SHA는 그대로다. 기존 버스/트럭 3200K·일반 승용차 흰색·스포츠카7500K, 부채꼴 기하·가로등 원색·브레이크/깜빡이/비상등·제품 카메라를 유지한다.
- **예산:** 새로운 mesh/material/renderer/실시간 Light/자산을 추가하지 않는다. 최대 우산·정류장 스트레스 장면 119424 triangles / 1584 renderers / 47 materials로 기존 제한 이내다. Shader 연산 비용과 실제 폰 FPS·발열은 측정하지 않았다.
- **Unity 검사:** source `22ccfdffb06e5ced3fdf92ec5b499e0887cb1bc9` / [Validate 38106654811](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38106654811). 실제 날씨/시간 상태 18개와 전환 160샘플의 wet/lamp/rain 전달·연속성, 숨김 clock/rain 정지와 disabled 차량 ghost 제거를 확인했다. 기존 차종별 색상/밝기96·부채꼴144·젖은 반사24대, 600초 교통 안전, 18기후·180전환, 승하차/비상등/안전 출발과 접지 검사도 PASS다.
- **실제 GPU 검사:** Unity 6000.3.26f1 / Direct3D11에서 실제 RoadOverlay를 Editor 전용 작은 RenderTexture로 16회 렌더링해 1048576픽셀을 검사했다. 건조/불꺼짐/눈 상태에서 반사 제거, 낮은 젖음 대비 강도 증가, 비 없는 wet 표면 정지와 비가 있을 때 작은 움직임 및 유한값/밝기 상한을 확인했다. 최대 linear 밝기 0.138432, 비 없는 최대 차이 0.000000, 2초 rain 평균 차이 0.00004756. 임시 probe는 finally에서 제거하고 제품 장면에 저장하지 않는다.
- **실제 화면:** 기존94 PNG와 새 8개 빗길/약한비/폭우/안개/낮/건조 전환 비교, 총102 PNG. 전체 제품 야간과 가까운 젖은 도로·약한비·2초/6초 맑아지는 전환을 직접 관찰해 반사의 규칙적인 띠 감소·차선 가독성을 확인했다. core와 PNG 3묶음의 bytes/SHA/CRC/source를 확인했다. Editor 사진이며 실제 폰 screenshot/FPS 측정은 아니다.
- **Android·서명:** 같은 source tag `unity-apk-0.21.0-build1` / [BuildApk 38107158675](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38107158675). BuildPlayer 오류0/경고0, launcher Lint 오류0/경고8. 실제 APK version0.21.0/code22/min29/target36/ARM64 IL2CPP, wallpaper service·private provider·settings launcher와 원본 v2 인증서 `a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`를 확인했다. 변하지 않은12 host 입력은 기존 host Lint38008378207(오류0/경고10)을 재사용했다.
- **전달:** [APK 직접 다운로드](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/4b89e1992ea3150386aa6285ba586ddd9d616aa8/pixel-traffic-unity-prototype-0.21.0.apk) · [전체 화면](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/4b89e1992ea3150386aa6285ba586ddd9d616aa8/wet-city-preview.png) · [가까운 빗길 반사](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/4b89e1992ea3150386aa6285ba586ddd9d616aa8/wet-reflections-preview.png) · [약한비 반사](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/4b89e1992ea3150386aa6285ba586ddd9d616aa8/wet-drizzle-preview.png). APK 31128311 bytes / SHA256 `95f737f52d85efc6db61fa8867f72eb9c1d4364b06d80b706dfa7b70e677d90a`. 일회성 `downloads/unity-0.21.0`의 APK·실제 사진을 로그인 없는 HTTP200/redirect 없음/전체 bytes/SHA로 확인했다. main에는 APK binary를 추가하지 않았다.

PC 설치(아래 폴더로 다운로드한 경우):
```powershell
$trafficAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $trafficAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.21.0.apk"
```

미확인: 실제 폰0.21.0 외형·FPS·발열·장시간 lifecycle. 사용자는0.20.0 적용 성공을 확인했다. APK 설치는 사용자 PC/폰에서 수행한다. Drive 설정은 기존 보류 상태다. 최종 문서 전용 commit의 118 입력 SHA가 같으므로 통과한 검사를 반복하지 않는다.

## 2026-10-11 — Unity 0.20.0 차종별 전조등 색상: 실제 화면 검증·서명 APK 전달 완료

사용자 요청에 따라 버스·트럭 전조등은 약 3200K의 따뜻한 노란빛, 일반 승용차·SUV·택시는 중립적인 흰색, 스포츠카는 요청한 6500–8000K 범위의 7500K 차가운 흰색으로 변경했다. **0.20.0/code21**, `HeadlightColors-0.20.0.unity` / `Generated/HeadlightColors0200`. 사용자가 첨부한 주황색 도로 가장자리 불빛은 젖은 노면에 반사된 가로등이며 가로등 색상·반사는 그대로 유지한다.

VehicleLighting의 차종 프로필을 램프 발광·부채꼴 빛·젖은 노면 반사에 함께 적용한다. Unity 색온도 linear RGB를 재질용 sRGB로 한 번만 변환하고 차량 참조를 캐시한다. 노면 메시에는 현재 렌더링 색 공간에 맞춘 값을 전달한다. 기존 낮·노을·밤 밝기 전환, 부채꼴 가장자리 감쇠와 차량 회전, 후미등·브레이크등·깜빡이·버스 비상등 제어를 유지한다. 7500K는 렌더링에 지정한 색온도이며 실제 디스플레이의 측정값이 아니다.

- **범위와 보존:** VehicleLighting, RoadSurface, StarterConfig, DrivingChecks, CityPreview, StarterScene의 6파일이며 신규 파일은 없다. 시작 상태 293파일·실제 index·전체 미추적 Git 상태와 118 빌드/촬영 입력을 기록하고 관련 없는 사용자 변경을 보존했다. Native Android·host·workflow 12 입력의 SHA는 그대로다.
- **예산:** Light·renderer·mesh·shader·공유 재질을 추가하지 않는다. 최대 우산·정류장 스트레스 장면 119424 triangles / 1584 renderers / 47 materials는 기존 제한 이내다. 실제 폰 FPS·발열은 별도 확인이 필요하다.
- **Unity 검사:** source `4347cb6757d2b6955f2a5c052754c7b581c8c881` / [Validate 38104624321](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38104624321). 차량 24대의 실제 램프/빔 MaterialPropertyBlock 색상·밝기 96사례를 검증했다. 노란빛 8대, 흰빛 13대, LED/HID 계열 3대이며 스포츠카 설정은 7500K다. 젖은 노면의 전조등 반사 RGB도 차량별 프로필을 현재 색 공간으로 변환한 값과 일치한다. 기존 부채꼴 방향/회전 144사례·젖은 반사 24대, 600초 교통 안전, 18기후·180전환과 승하차·비상깜빡이·안전 출발 검사도 PASS다.
- **실제 관찰 후 보정:** 첫 source549e68024ac57282a3498cbcb789ece41c325164 / Validate38104122433은 검사·94 PNG가 통과했지만 버스/트럭 불빛이 과도하게 주황색으로 보여 CCT linear RGB와 SetColor sRGB 입력의 변환을 보정했다. 첫 사진을 별도 보존하고 아래 최종 source로 검사·촬영을 다시 수행했다.
- **실제 GPU:** Unity 6000.3.26f1 / Direct3D11의 94 PNG. 6차종의 건조/젖은 야간 비교 사진 12장을 추가했다. 버스·트럭의 노란빛, 승용차의 흰빛, 스포츠카의 차가운 흰빛과 제품 야간 구도를 직접 관찰했다. 임시 촬영 구도·차량 배치는 저장하지 않는다. Editor 촬영은 실제 폰 screenshot/FPS 측정과 구분한다. core와 PNG 3묶음의 bytes/SHA/CRC/source를 확인했다.
- **Android·서명:** 같은 source tag `unity-apk-0.20.0-build1` / [BuildApk 38105051956](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38105051956). BuildPlayer 오류0/경고0, launcher Lint 오류0/경고8. 내려받은 실제 APK의 version0.20.0/code21/min29/target36/ARM64 IL2CPP, wallpaper service·private provider·settings launcher와 원본 v2 인증서 `a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`를 확인했다. 변경 없는 12 host 입력의 SHA가 같아 기존 host Lint38008378207(오류0/경고10)을 재사용했다.
- **전달:** [APK 직접 다운로드](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/140b775fa2072bae24cea4d17f7e90720a0cb4f7/pixel-traffic-unity-prototype-0.20.0.apk) · [전체 화면](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/140b775fa2072bae24cea4d17f7e90720a0cb4f7/headlight-city-preview.png) · [노란 버스 불빛·젖은 노면](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/140b775fa2072bae24cea4d17f7e90720a0cb4f7/headlight-halogen-preview.png) · [흰색 승용차 불빛](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/140b775fa2072bae24cea4d17f7e90720a0cb4f7/headlight-white-preview.png) · [7500K 스포츠카 불빛](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/140b775fa2072bae24cea4d17f7e90720a0cb4f7/headlight-led-preview.png). APK 31121075 bytes / SHA256 `66011b4e94a031f81cc603dff4e5ede30d2072b948f8aef8efebc0d4ebd594dd`. 일회성 `downloads/unity-0.20.0`의 APK·실제 사진을 로그인 없는 HTTP200/redirect 없음/전체 bytes/SHA로 확인했다. main에는 APK binary를 추가하지 않았다.

PC 설치(아래 폴더로 다운로드한 경우):
```powershell
$trafficAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $trafficAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.20.0.apk"
```

미확인: 실제 폰 0.20.0 외형·성능·발열·장시간 lifecycle. APK 설치는 사용자 PC/폰에서 수행하며 여기서 설치했다고 주장하지 않는다. Drive 설정은 기존 보류 상태다. 최종 문서 전용 commit의 118 입력 SHA가 같으므로 통과한 검사를 반복하지 않는다.

## 2026-10-11 — Unity 0.19.0 부채꼴 전조등: 실제 화면 검증·서명 APK 전달 완료

사용자 요청에 따라 차량 앞의 타원형 불빛을 좌우 램프에서 넓어지는 부채꼴로 변경했다. **0.19.0/code20**, `HeadlightFans-0.19.0.unity` / `Generated/HeadlightFans0190`.

실제 앞 램프 메시의 발광 위치에서 시작하는 두 사다리꼴을 생성한다. 각 불빛은 시작 폭 0.28m, 끝 폭 4.2m, 길이 8.68m이며 차량 회전 방향을 따라간다. 새 공유 HeadlightFan 셰이더가 중앙 밝기와 측면·먼 끝의 부드러운 감쇠를 처리한다. 가로등용 둥근 빛 번짐 재질과 분리해 가로등 외형을 유지했다. 젖은 도로의 전조등 반사도 같은 방향으로 넓어지는 메시·전용 마스크로 변경한다. 후미등·가로등 반사는 유지한다. GTA5의 부채꼴 외형을 참고한 가벼운 노면 표현이며 물리 조명·장애물 차폐 시스템은 구현하지 않았다.

- **범위와 보존:** DrivingScene, DrivingChecks, TrafficFleet, ClimateScene, CityEnvironment, CityPreview, StarterScene, RoadSurface, StarterConfig, RoadOverlay.shader, HeadlightFan.shader/meta의 12파일(신규 2). 시작 상태 291파일·실제 index·전체 미추적 Git 상태와 118 빌드/촬영 입력을 기록하고 관련 없는 사용자 변경을 보존했다. Native Android·host·workflow 입력은 그대로다.
- **동작·예산:** 차량별 renderer/Light/삼각형을 늘리지 않고 공유 재질 1개만 추가한다. 최대 우산·정류장 스트레스 장면은 119424 triangles / 1584 renderers / 47 materials로 기존 제한 이내다. 기존 낮·노을·밤의 부드러운 밝기와 비활성/화면 숨김 제어를 재사용한다. 같은 geometry 예산이 실제 폰 FPS·발열 동일을 보장하지는 않는다.
- **Unity 검사:** source `5ca655836083a05785c6ed12b02baa9028a12630` / [Validate 38101648688](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38101648688). 실제 차량 24대의 램프 위치·좁은 시작/넓은 끝·양 방향과 ±8도 회전 144사례, 낮/비활성 불빛 제거 및 젖은 반사 24대의 방향·마스크를 검증했다. 기존 600초 교통 안전, 18기후·180전환, 승하차·비상깜빡이·안전 출발과 접지 검사도 PASS다.
- **실제 GPU:** Unity 6000.3.26f1 / Direct3D11의 82 PNG. 야간 제품 화면·램프 확대와 새 위에서 보는 건조/젖은 회전 불빛 2장을 직접 관찰했다. 임시 촬영 구도·차량 배치는 저장하지 않는다. Editor 촬영은 실제 폰 screenshot/FPS 측정과 구분한다. 보고서 core와 PNG 3묶음의 bytes/SHA/CRC/source를 확인했다.
- **Android·서명:** 같은 source tag `unity-apk-0.19.0-build1` / [BuildApk 38102122530](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38102122530). BuildPlayer 오류0/경고0, launcher Lint 오류0/경고8. 내려받은 실제 APK의 version0.19.0/code20/min29/target36/ARM64 IL2CPP, wallpaper service·private provider·settings launcher와 원본 v2 인증서 `a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`를 확인했다. 변경 없는 12 host 입력의 SHA가 같아 기존 host Lint38008378207(오류0/경고10)을 재사용했다.
- **전달:** [APK 직접 다운로드](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/ad21ba9e3b2ce6275259ce4e1f0eadc3185cfb83/pixel-traffic-unity-prototype-0.19.0.apk) · [전체 화면](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/ad21ba9e3b2ce6275259ce4e1f0eadc3185cfb83/headlight-city-preview.png) · [젖은 도로·회전 부채꼴](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/ad21ba9e3b2ce6275259ce4e1f0eadc3185cfb83/headlight-wet-preview.png) · [건조한 도로 부채꼴](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/ad21ba9e3b2ce6275259ce4e1f0eadc3185cfb83/headlight-fan-preview.png). APK 31118519 bytes / SHA256 `084278b313f29cd2faa32d8a0802ef700eed9780201854d1db5e53a0a21be7ba`. 일회성 `downloads/unity-0.19.0`의 APK·실제 사진을 로그인 없는 HTTP200/redirect 없음/전체 bytes/SHA로 확인했다. main에는 APK binary를 추가하지 않았다.

PC 설치(아래 폴더로 다운로드한 경우):
```powershell
$trafficAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $trafficAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.19.0.apk"
```

미확인: 실제 폰 0.19.0 불빛 외형·성능·발열·장시간 lifecycle. APK 설치는 사용자 PC/폰에서 수행하며 여기서 설치했다고 주장하지 않는다. Drive 설정은 기존 보류 상태다. 최종 문서 전용 commit의 118 입력 SHA가 같으므로 통과한 검사를 반복하지 않는다.

## 2026-10-11 — Unity 0.18.0 도로·보도 마감과 접지감: 실제 GPU 검증·서명 APK 전달 완료

사용자는 폰 **0.17.0 적용 성공**을 확인하고 도로·보도 패치를 승인했다. **0.18.0/code19**, `GroundFinish-0.18.0.unity` / `Generated/GroundFinish0180`. 아스팔트512 색/미세 normal과 seam 없는주기적 grain/macro, 보도256의엇갈린블록/줄눈, 경계석 joint를 보강했다. 기존4맨홀은32tri disc+철판 ring/hatch로꾸미고 도로 가장자리24배수구를48tri병합메시로표현한다. 옅은8타이어흔적과전체24대의차체/4바퀴 접지 그림자·젖은전조등/후미등반사를 하나의672vertex/336tri 메시로갱신한다. 가로등20곳의길게끊어진노면반짝임도더했다. authored 반사이며실제reflectioncamera가아니다. Ground overlay는한번복제한공유재질/동적mesh로구현하고CityClimate wetness/lamp/snow/visibleclock을사용한다. 차량별renderer/실시간Light/콜라이더추가없음. 숨김/화면OFF에는mesh·반사clock을갱신하지않고inactive차량의shadow/reflection을제거한다.

- **범위·예산:** GroundScene+meta, RoadSurface+meta, RoadOverlay.shader+meta, CityEnvironment, CityPreview, StarterScene, StarterConfig 및기존read-only workflow 11source/meta/workflow(신규6). 기존285파일/index/status를보존하고 신규6을더한116 build/capture입력을추적한다. Ground는4renderer·overlay1재질/Drain Iron1재질, 기존차량geometry/renderercontract유지. 최대100기존우산+8정류장 rig/우산 stress 119424tri/1584renderer/46material, 기존strict120000/2400/48이내. 셰이더/동적메시비용의 실제폰FPS·발열은별도확인이다.
- **Unity:** source `f222879dc97a09d8f653316254e17b9a8933143c` / [Validate 38097682850](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38097682850) / reports artifact 11686822378. Ground 실제4바퀴×24차×3yaw의 288contact world좌표, hidden freeze/inactive ghost/원본asset/mip·seam/배수구roadedge/clear·wetnight mapping PASS. 기존18기후·180blend/15·30·60·120Hz 낮→노을→밤·밤→낮/숨김·retarget·신호/회전차체·차선변경·승하차/hazard·안전출발을 유지한다. 실제24대600초 신호 11순환/차선변경 21/정류장 출발 14, 최소차체gap 1.799999m, 후반승객탑승 23회.
- **전송 개선:** 첫 Validate38096983732/원본artifact11686656450은실행성공이었으나34,394,376bytes로32MiB executor전송제한을넘어local GPU확인이불가능했다. 파일domainHTTP403도확인하고정책우회없이기존workflow의허용보고서출력을core+PNG3묶음으로분리했다. 최종묶음의각bytes/SHA/CRC/source head를검증했다.
- **GPU:** 실제 Unity6000.3.26f1/Direct3D11 80PNG. 신규ground 낮·비오는밤·2초ripple·보도확대4사진과제품구도를관찰했다. 촬영카메라/기후변경은임시이며저장하지않는다. 실제Editor이미지는폰 screenshot/FPS 측정과구분한다. 실제관찰·수정이력은source manifest에기록한다.
- **Android·서명:** 같은source tag `unity-apk-0.18.0-build1` / [BuildApk 38098141335](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38098141335) / reports 11687096882 / APK 11686952036. BuildPlayer 오류0/경고0, launcher Lint 오류0/경고8. 실제 다운로드 APK의기존v2 인증서 `a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`, version0.18.0/code19/min29/target36/ARM64 IL2CPP, settings launcher·BIND_WALLPAPER service·private provider·disabled Unity activity를검증했다. 12host SHA가같아실제hostLint38008378207(오류0/경고10)을재사용한다. NativeAndroid/host 입력변경없음. 기존read-only workflow는core보고서/실제PNG3묶음으로분리하고그룹24MiB예산을검사한다.
- **전달:** [APK 직접다운로드](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/e8e570e0b071415480e3dc5d743214bf213effc6/pixel-traffic-unity-prototype-0.18.0.apk) · [실제전체화면](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/e8e570e0b071415480e3dc5d743214bf213effc6/ground-city-preview.png) · [비오는화면](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/e8e570e0b071415480e3dc5d743214bf213effc6/ground-night-preview.png) · [노면확대](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/e8e570e0b071415480e3dc5d743214bf213effc6/ground-detail-preview.png). APK 31114347bytes/SHA256 `6b9489ae2fa47bc949d6c4b52922e8887e11ac23b7eecb2507e4acb7849ed759`. 일회성 `downloads/unity-0.18.0`의APK1·실제PNG3파일을로그인없는HTTP200/redirect없음/전체bytes/SHA로검증했다. main에는binary/새Releaseworkflow를추가하지않는다.

PC 설치(다운로드한 실제 위치 사용):
```powershell
$trafficAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $trafficAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.18.0.apk"
```

**미확인/다음:** 폰0.18 도로·보도/젖은밤·전환/접지감/FPS·발열·장시간lifecycle확인. 폰0.17 적용성공은사용자확인이다. 실제설치는사용자PC에서수행한다. Drive는나중에PC설치후재개한다. 최종docs-onlycommit은116입력이같으므로검사를반복하지않는다.

## 2026-10-11 — Unity 0.17.0 하늘·구름·해·달: 실제 GPU 검증·서명 APK 전달 완료

사용자는 폰 **0.16.0 적용 성공**을 확인했고 하늘 패치를 승인했다. **0.17.0/code18**, `SkyClimate-0.17.0.unity` / `Generated/SkyClimate0170`. Distant.shader에서 넓고 둥근 구름의 밀도·내부 음영·밝은 가장자리와 노을빛을 표현한다. 낮의 원형 해/halo는 노을에 낮아지고, 밤에는 질감 있는 달/halo와 antialias 별이 나타난다. 구름은 해·달·별/halo를 가리며 비·안개·눈은 하늘과 천체의 선명도를 줄인다. 기존 CityClimate의 visible clock과 햇빛 dim/복구를 재사용한다. 추가 실시간 Light·reflection camera·모델 없이 기존 공유 불투명 셰이더로 처리한다. 제품 카메라/산·강·도시/차량24대·보행100명·정류장8명/승하차·hazard·안전 출발, NativeAndroid/host/workflow는 유지했다.

- **범위·예산:** Distant.shader, DistantChecks, CityPreview, StarterScene, StarterConfig 5소스. 새 scene/generated 경로로 이전 장면을 덮어쓰지 않는다. 원경 8mesh/1778tri·runtime material1개. 100기존 우산+8정류장 rig/우산 stress 119112tri/1580renderer/44material, 기존 strict120000/2400/48 이내. 셰이더 연산은 늘었으므로 같은 geometry 예산이 실제 폰 FPS 동일을 보장하지 않는다.
- **Unity:** source `95cc418a5d43e0a16561a9706adc38f20c3d50d7` / [Validate 38095123479](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38095123479) / reports artifact 11684929033. 실제 Sun의 storm dim→clear 복구 120frames/두 세로 aspect 해·달 위치 검사, 기존18기후·180blend·15/30/60/120Hz 낮→노을→밤·밤→낮·숨김 clock/retarget·8mesh/강65m/다리11lamp/산층·finite geometry/shared material을 확인했다. 실제24대600초 신호 11순환/차선변경 21/정류장 출발 14, 최소 차체 gap 1.799999m. 승객 후반 탑승 23회, 기존 안전 검사 PASS.
- **GPU:** 실제 Unity6000.3.26f1/Direct3D11 76PNG, 하늘 확대 낮/노을/밤/폭풍/6초 구름 이동5장 포함. 원래 제품 카메라의 낮·노을·밤과 확대 하늘을 직접 관찰했다. 촬영 카메라/기후 변경은 임시이며 저장하지 않는다. 실제 Editor 렌더와 폰 screenshot/FPS·발열 측정은 구분한다. 구름 이동 관찰은 source manifest에 기록한다.
- **시각 보정:** 첫 source742c388/Validate38094648632/artifact11685996336은 검사·76PNG가 통과했으나 구름이 큰 띠처럼 보여 noise 크기/가장자리와 내부 음영을 보정했다. 최종 source의 실제 GPU에서 다시 확인했다. 첫 관측과 수정 이력은 source manifest에 보존한다.
- **Android·서명:** 같은 source tag `unity-apk-0.17.0-build1` / [BuildApk 38095550311](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38095550311) / reports 11685299837 / APK 11685494079. BuildPlayer 오류0/경고0, launcher Lint 오류0/경고8. 실제 다운로드 APK의 기존v2 인증서 `a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`, version0.17.0/code18/min29/target36/ARM64 IL2CPP, settings launcher·BIND_WALLPAPER service·private provider·disabled Unity activity를 검증했다. 12 host 입력의 SHA가 같아 실제 host Lint38008378207(오류0/경고10)을 재사용한다.
- **전달:** [APK 직접 다운로드](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/7c562331c9837c7d5977cb9da3eda7b992c6307e/pixel-traffic-unity-prototype-0.17.0.apk) · [실제 Unity 전체 화면](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/7c562331c9837c7d5977cb9da3eda7b992c6307e/sky-city-preview.png) · [야간 화면](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/7c562331c9837c7d5977cb9da3eda7b992c6307e/sky-night-preview.png) · [노을 하늘 확대](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/7c562331c9837c7d5977cb9da3eda7b992c6307e/sky-detail-preview.png). APK 30036724bytes/SHA256 `12b60019361d07486b16634330ecb58bd00484d6d460306e9951d2574764536f`. 일회성 `downloads/unity-0.17.0` branch의APK1·실제PNG3파일을 로그인 없는HTTP200/redirect없음/전체bytes/SHA로 검증했다. main에binary/새Release workflow는 추가하지 않는다.

PC 설치(다운로드한 실제 위치 사용):
```powershell
$trafficAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
& $trafficAdb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.17.0.apk"
```

**미확인/다음:** 폰0.17 하늘 외형/낮·노을·밤/날씨/FPS·발열·장시간 lifecycle 확인. 폰0.16 적용 성공은 사용자가 확인했다. 실제 설치는 사용자 PC에서 수행한다. Drive는나중에PC설치후재개한다. 기존dirty285파일/index/status 및110 build/capture 입력을보존했다. 최종docs-onlycommit은빌드입력이같으므로검사를반복하지않는다.

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

## 이전 패치 — 혼합 차량·긴 차체 주행 (2026-10-10)

**0.14.0/code15**, `HeavyTraffic-0.14.0.unity`/`Generated/HeavyTraffic0140`. 총24대 중 파란 시내버스4대·흰 박스 트럭4대·기존 승용차16대를 섞습니다. 버스 옆창·문·옥상AC, 트럭 cab/cargo·뒤문 seam/잠금봉/힌지를 추가하고 기존 전조등·브레이크등·깜빡이를 연결했습니다. 긴 차체는6초에 걸쳐 차선을 바꾸며 실제 치수·회전범위·뒤차추월을 확인합니다. 시안camera·도시·시간/날씨·보행/신호·배경화면host는 유지합니다.

실제 Unity검사/GPU59PNG·Android BuildPlayer오류0/경고0·launcherLint오류0/경고8·원본v2서명/버전/manifest·공개파일 전체SHA를 확인했습니다. 최대100우산 116642tri/1551renderer/44material이며 폰 FPS·발열은 설치후 확인합니다. 사용자는폰0.13 적용성공을확인했고Drive구성은나중에PC에서재개합니다.

## 이전 0.14.0 APK 다운로드

[Unity0.14.0 APK 직접 다운로드](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/08fed474120a21f8908dc451a57388e4f7d7b586/pixel-traffic-unity-prototype-0.14.0.apk) · [실제 Unity 화면](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/08fed474120a21f8908dc451a57388e4f7d7b586/heavy-traffic-preview.png) · [설치·검증 기록](../docs/STATUS.md).

일반 Chrome/삼성 인터넷에서 열며 APK 29998135bytes입니다. 일회성downloadbranch에는APK/PNG만두고자동Release설정은없습니다.

```powershell
adb install -r "C:\Users\김백현\Desktop\AI\pixel-traffic-unity-prototype-0.14.0.apk"
```

## 이전 패치 — 가로수·건물 디테일 0.13.0 (2026-10-10)

가로수32그루의 둥근형/수직형/퍼지는형 수관과 공유 잎 무늬, 건물24개의 콘크리트 외벽·석재 테두리·발코니48개·옥상 난간/환기구 slit/출입실/일부안테나·12옥상화단을 추가했습니다. 기존 카메라·원경/날씨·노을/바람·보행/교통·등화/안전차선변경을 유지합니다. 수관400tri/sharedopaqueLit와 merged building mesh로100우산118710tri/1551renderer/43material 예산을 유지합니다.

**0.13.0/code14**, `Scenery-0.13.0.unity`/`Generated/Scenery0130`. 실제PC Unity검사·51GPU PNG(새수형/건축8확대)·BuildPlayer0errors/21warnings·launcherLint0errors/8warnings·다운로드원본v2cert/version/manifest/SHA 및공개HTTP200전체SHA검증 완료. 실제 slab/post bounds에서48발코니/192post를확인했습니다. 사용자는폰0.12 적용성공을확인했고폰0.13 외형/FPS·발열/전체lifecycle은설치후확인합니다. Drive작업은나중에PC에서재개합니다.

## 이전 패치 — 원경 0.12.0 (2026-10-10)

0.11 시안 카메라를 유지하고 하늘·천천히 흐르는 구름·세 겹 산능선·강/다리·건너편 도시와 야간 창문 빛을 추가했습니다. 기존4초 시간/날씨 전환과 낮→노을→밤, 숨김 정지에 연결합니다. 원경7메시/1108tri/공유재질1개이며 얇은 원통최적화로100우산 최대117318tri/1467renderer/43material을 유지합니다. 차량/보행/등화·차선변경은 유지합니다.

**0.12.0/code13**, `Landscape-0.12.0.unity`/`Generated/Landscape0120`. 실제 Windows Unity검사·D3D11 GPU43PNG·BuildPlayer 오류0/경고0·launcherLint 오류0/경고8·다운로드 원본v2cert/version/manifest/SHA 검증 및 로그인 없는공개HTTP200/SHA 확인. 원경 GPU표면 줄무늬는 공유world좌표로 수정한 후 다시검증했습니다. 폰0.11은 사용자가 적용 성공을 확인했고 폰0.12 외형/FPS·발열/전체lifecycle은 설치후확인합니다. Drive 작업은 나중에PC에서재개합니다.

## 이전 패치 — 시안 카메라 0.11.0 (2026-10-10)

사용자가 다시 제공한 시안의 도로 원근과 화면 배치에 맞춰 카메라를 조정했습니다. 덜 내려다보는 각도, 오른쪽 위 소실점, 아래쪽 횡단보도, 전경 차량과 상단 배경 여백을 담습니다. 위치(1.4,14,-37)/target(-4.9,1,68)/44°FOV. 그림의 강·구름·건축 외형 상세는 다음 작업이며 일부 수관 가림과 단순한 원경은 남아 있습니다.

**0.11.0/code12**,`ReferenceCamera-0.11.0.unity`/`Generated/ReferenceCamera0110`,`Builds/pixel-traffic-unity-prototype-0.11.0.apk`. 실제9:20/9:16 projection·ground/sky rays,기존 교통/보행/날씨·100우산 예산,같은상태의old/new GPU 비교를검증했습니다. 실제GPU39PNG·AndroidBuildPlayer/launcherLint0errors/경고8·원본v2cert/version/manifest/hash PASS. 최대100우산119,922tri/1515renderer/47material,unchangedhost기존Lint0errors/10warnings재사용. 사용자가 실제폰0.11.0 다운로드·적용 성공을 확인했습니다. FPS·발열과 전체 lifecycle은 별도 확인이 필요합니다. Drive 전달 구성은 사용자 요청으로 보류했습니다.

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

Unity 6000.3.26f1/Android 모듈을 사용합니다. `tools/run-pipeline.ps1 -Operation Validate`로 장면·production 모델·manifest 템플릿 검사를 수행하고, `-Operation BuildApk`로 Android 빌드/Lint/원본 v2 인증서 및 APK 메타데이터를 검사합니다. Gradle 생성 후 `AndroidWallpaperBuild`가 `NativeAndroid/src`와 `res`를 복사하고 service/provider/설정 Activity를 등록합니다. Export에도 같은 처리를 적용합니다. 실제 Android 컴파일은 PC BuildApk 결과로 확인합니다. 새 NativeAndroid 모듈은 `tools/check-wallpaper-lint.ps1`로 unityLibrary Lint를 추가 확인합니다. 이 도구는 생성된 Java/리소스 SHA가 현재 소스와 같은지 먼저 확인하며 APK를 다시 만들지 않습니다. 0.15.0의 실제 launcher Lint는 오류0/경고8입니다. 변경 없는 NativeAndroid/host 입력은0.6.0 SHA를 확인하고 실제 unityLibrary Lint 오류0/경고10을 재사용합니다.

Editor 메뉴 **4. Build Signed Wallpaper APK**를 사용할 수 있습니다. APK는 `Builds/pixel-traffic-unity-prototype-0.15.0.apk`에 생성됩니다. 기존 `BuildActivityApk` 메서드 이름은 PowerShell 호출 호환용으로 유지하며 결과 앱은 라이브 배경화면입니다. 서명 환경 변수 및 PC runner 설정은 아래 초기 기록과 [자동화 안내](../docs/UNITY_AUTOMATION.md)를 참조합니다. 최신 안내가 아래 초기 0.1.0 기록보다 우선합니다.

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
