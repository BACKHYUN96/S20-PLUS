# 작은 패치 작업 안내

## 2026-10-12 — Unity0.35.0 로터리 차량 흐름 수정: 실제 검증·서명APK 전달 완료

0.34.0 로터리 카메라에서 한 대만 회전하고 다른 차량이 오래 기다리는 사용자 제보를 반영했다. 전체 접근/회전/진출 경로를 한 대가 예약하던global owner를 제거했다. 차량별admitted 상태, 같은진입로 합류순서, 이미 진입한 차의 합류예상구간/차체간격 및 서로의 진입/진출 곡선 교차를 검사한다. 미래곡선 예약은 새로 들어오는 차량에만 적용하고 이미 진입/진출한 차량은 실제 차체간격으로 이동한다. 예약 때문에 앞선 진출차까지 멈추던 순환 대기를 보완했다. 남쪽 두차로는 처음18m 직진한 뒤 합류하고, 진출은213m에서 차로에 정렬해 큰 차량이 반대편 대기차로에 걸리는 것을 피한다. 물리적으로 막혀 정차한 차량의 brake 상태를 유지한다.

변경5CS: RoundaboutTraffic,RoundaboutChecks,CityPreview,StarterConfig,StarterScene.0.35.0/code36, RoundaboutFlow-0.35.0.unity / Generated/RoundaboutFlow0350. 로터리 지형mesh·차량/조명meshes·카메라선택·날씨·신호/보행·버스승하차·와이퍼와host12/workflow를 유지한다.130검증입력/304기존파일·실제index/status를 보존했다.

**로컬 과정:** C#컴파일러 부재를 확인하여 기존toolchains에.NET8.0.419를 격리해 추가했다. offline NuGet/최소UnityMath shim으로 실제StreetModel/RoundaboutTraffic/LaneChanges/BusStops/SidewalkRoutes 소스를 실행하고0.34.0 실제보고서6차종치수와 독립SAT 차체검사를 사용했다. 합류 대기점 충돌·대형차출구각도·미래예약이 진출차까지 막는 시행을 보완했다.1200초 실제치수 완료115/최대동시3/최대무완료33.2초, 확대치수 완료101/최대동시2/39.3초, 폭풍 완료62/최대동시3/61.2초; 차체충돌 없음. 로컬helper는 실제Unity/Android/폰 검증을 대신하지 않는다.

**실제PC 검증:** source `2ab4579bd539c869e1b7ad25d492c1f6a03dd025` / [Validate](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38195420608). 저장된 실제24차량을 로터리 카메라로600초 검사: 진입50,완료48,복귀27,재진입21,네출구[8, 8, 8, 8]. 최대동시회전2대, 두 대 이상 이동1827tick, 최대무완료33.13초로90초 정체 방지 검사 PASS. 독립회전차체간격993140회, 중앙섬/반시계/연속이동·offscreen재진입·15/30/60/120Hz/pause·버스승하차/깜빡이·신호/보행·라이트/와이퍼·기후/반사 PASS. 기본108616tri, 최대119962tri/1592renderers/47materials로 기존 예산110000/120000/2400/48 미만.

**실제GPU:** 166PNG의source/bytes/SHA/ZIP CRC 확인.4연속 로터리 frame에서2개 실제 회전차량 각각의 이동>.5m 및 GPU 변경픽셀[3396, 3304, 3756] 검사 PASS. 새옵션 낮/노을/밤/강비4장과 기존카메라/안개 조건도 유지했다. Unity Editor/URP/GPU 결과이며 실제폰촬영/FPS가 아니다. 실패/수정 기록: [].

**Android:** `unity-apk-0.35.0-build1` / [BuildApk](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38196464073). BuildPlayer 오류0/경고0,launcherLint 오류0/경고8. 실제다운로드APK apksigner/aapt/ZIP CRC로0.35.0/code36/min29/target36/ARM64 IL2CPP와wallpaper service/privateprovider/settingslauncher/비활성UnityActivity를 확인했다. 원본v2cert SHA256 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6. 변경없는host12 SHA는 [0.34.0 unityLibraryLint](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38192276536) 오류0/경고10,8생성파일일치를 재사용했다.

[APK](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/78775fc2665e312be7a7a285fe32d2cb5a5878aa/pixel-traffic-unity-prototype-0.35.0.apk) · [실제회전frame0](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/78775fc2665e312be7a7a285fe32d2cb5a5878aa/roundabout-motion-0.png) · [frame1](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/78775fc2665e312be7a7a285fe32d2cb5a5878aa/roundabout-motion-1.png) · [로터리밤](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/78775fc2665e312be7a7a285fe32d2cb5a5878aa/camera-roundabout-night.png). APK31241787bytes/SHA256`6329f10efc789b8f3ff065b0d9c674fb34301b3d12233a63b3748399fc6648ec`. 다운로드branch`downloads/unity-0.35.0`의16파일을 로그인 없는HTTP200/no redirect/full bytes/SHA로 확인했다. main에APK·새workflow를 추가하지 않았다.

USB폰1대를 연결하고 디버깅을 허용한 뒤 아래PowerShell 전체블록을 실행한다. C:\Users\김백현\Desktop\AI에 다운로드·SHA 확인·기존앱 업데이트:
```powershell
& {
    $ErrorActionPreference = "Stop"
    $apkFolder = "C:\Users\김백현\Desktop\AI"
    $apkPath = Join-Path $apkFolder "pixel-traffic-unity-prototype-0.35.0.apk"
    $apkUrl = "https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/78775fc2665e312be7a7a285fe32d2cb5a5878aa/pixel-traffic-unity-prototype-0.35.0.apk"
    $trafficAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
    if (-not (Test-Path -LiteralPath $trafficAdb)) { throw "Unity의 adb.exe를 찾을 수 없습니다." }
    & $trafficAdb start-server
    if ($LASTEXITCODE -ne 0) { throw "ADB 시작 실패" }
    $deviceLines = & $trafficAdb devices
    if ($LASTEXITCODE -ne 0) { throw "폰 연결 확인 실패" }
    $connectedDevices = @($deviceLines | Where-Object { $_ -match '^\S+\s+device$' })
    if ($connectedDevices.Count -ne 1) { throw "폰 1대를 USB로 연결하고 USB 디버깅을 허용해주세요." }
    New-Item -ItemType Directory -Force -Path $apkFolder | Out-Null
    Write-Host "0.35.0 APK 다운로드 중..."
    Invoke-WebRequest -UseBasicParsing -Uri $apkUrl -OutFile $apkPath
    if ((Get-FileHash -LiteralPath $apkPath -Algorithm SHA256).Hash -ne "6329f10efc789b8f3ff065b0d9c674fb34301b3d12233a63b3748399fc6648ec") { throw "파일 검증 실패. 다시 실행해주세요." }
    Write-Host "폰에 앱 설치 중..."
    & $trafficAdb install -r $apkPath
    if ($LASTEXITCODE -ne 0) { throw "앱 설치 실패. 위 오류를 확인해주세요." }
    Write-Host "완료! 0.35.0 설치 성공." -ForegroundColor Green
}
```

설치 후 기존설정은 유지하며 앱설정 → 카메라구도 → 로터리보기에서 차량흐름을 확인한다. 합류간격이 부족하면 진입차는 기다리고, 이미 회전/진출하는 차는 계속 이동한다.0.34.0 로터리구도는 사용자 폰 적용 확인;0.35.0 폰 주행/외형/FPS/발열/장시간 확인 대기다. 클라우드에서 폰설치를 했다고 주장하지 않는다.

## 2026-10-12 — Unity 0.34.0 카메라 구도 선택: 검증·서명 APK 전달 완료

요청: 기존 도시 구도와 별도로 로터리가 잘 보이는 구도를 설정에서 선택한다. Android 설정의 카메라 구도에 ‘도시 전체’/‘로터리 보기’를 추가했다. 선택을 SharedPreferences에 저장하고 기존 비공개 provider/500ms 스냅샷/JNI bridge로 wallpaper 프로세스에 전달한다. 기존 설정은 도시 전체가 기본이다. 동일 카메라의 위치·방향·FOV만 변경하고 날씨·시간대·차량 clock을 초기화하지 않는다. 구도 선택은 즉시 적용하며 다시 시작할 때 복원한다. 폰의 실제 저장/재시작 동작은 사용자 확인 대기다.

로터리 보기: 위치(0,46,161), target(0,0,241), FOV60. 도시 전체: 원래 위치(1.4,14,-37), target(-4.9,1,68), FOV44. 카메라 clip/aspect·그래픽 geometry·재질·조명 예산은 유지한다. 버전0.34.0/code35, RoundaboutCamera-0.34.0.unity / Generated/RoundaboutCamera0340. 변경11소스·검증입력130파일·기존302파일과 user index/status를 보존한다. NativeAndroid 설정3개와 bridge/템플릿은 의도적으로 변경하고 나머지host7 SHA를 보존했다. 새 workflow는 추가하지 않았다.

**PC 검증:** source `77403093a9282d5334461db4e48ea39469250680` / [Validate](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38190798190). 9:20/9:16에서 로터리 외곽16점의 화면 여백·재진입 끝점이 화면밖인 조건·도시 구도의 정확한 복원·카메라 선택 시 차량 위치 보존 PASS. 실제 직렬화된24차량600초 검사: 진입24,완료23,복귀11,화면밖재진입11,네방향 선택[4, 4, 4, 5],차체간격검사993347회. 15/30/60/120Hz·pause·버스 승하차/비상깜빡이·신호·보행·기후·라이트·와이퍼·반사 PASS. 기본108616tri,최대119962tri/1592renderers/47materials로 기존110000/120000/2400/48 예산 미만.

**실제 GPU:** 166PNG의 source/bytes/SHA/ZIP CRC 확인. 새 옵션의 실제 CityCamera component로 낮/노을/밤/강비4장을 촬영하고 확인했다. 기존162장과 저장카메라 밤/구fog 조건 비교를 유지했다. Unity Editor/URP/GPU 결과이며 폰 촬영이나 FPS 검사가 아니다. 실패/수정 기록: [{'run': 38190055616, 'sourceCommit': '2c63e8c0a2aca4eef971253aa25da1b181bed7c9', 'reason': 'Automated validation and166GPU tests PASS but actual camera-option screenshots showed serialized vehicle positions inside grass. Editor ResetModel enables saved objects until Advance applies actual model visibility/poses. New option captures now Advance60seconds and assert no visible car inside island. Runtime camera/traffic unchanged. No APK built from this source.'}].

**Android:** `unity-apk-0.34.0-build1` / [BuildApk](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38191426649). BuildPlayer 오류0/경고0,launcherLint 오류0/경고8. 변경된 native host를 포함한 동일source의 [unityLibrary Lint](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38192276536) 오류0/경고10; 생성src/res SHA 일치8파일. 실제 내려받은 APK apksigner/aapt/ZIP CRC로0.34.0/code35/min29/target36/ARM64 IL2CPP와wallpaper service/private provider/settings launcher/비활성 Unity Activity를 확인했다. 원본v2인증서SHA256 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6. APK의 새 ‘카메라 구도’와 ‘도시 전체’/‘로터리 보기’ 리소스도 확인했다.

[APK](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/62a5f8bee65c9956bd5953591b1795207c9a558e/pixel-traffic-unity-prototype-0.34.0.apk) · [로터리 낮](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/62a5f8bee65c9956bd5953591b1795207c9a558e/camera-roundabout-day.png) · [로터리 밤](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/62a5f8bee65c9956bd5953591b1795207c9a558e/camera-roundabout-night.png) · [로터리 비](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/62a5f8bee65c9956bd5953591b1795207c9a558e/camera-roundabout-rain.png) · [도시 전체](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/62a5f8bee65c9956bd5953591b1795207c9a558e/roundabout-city-preview.png). APK31238223bytes/SHA256`90fc4ff4378592ae8cc678ce082e9c123bbe1ef932dde9ceb6a48e2aec9c1c06`. 다운로드branch`downloads/unity-0.34.0`의16파일을 로그인 없는HTTP200/no redirect/full bytes/SHA로 확인했다. main에APK를 추가하지 않았다.

USB폰1대를 연결하고 디버깅을 허용한 뒤 아래 Windows PowerShell 전체블록을 실행한다. 지정폴더 C:\Users\김백현\Desktop\AI에 다운로드·SHA 확인·기존앱 업데이트:
```powershell
& {
    $ErrorActionPreference = "Stop"
    $apkFolder = "C:\Users\김백현\Desktop\AI"
    $apkPath = Join-Path $apkFolder "pixel-traffic-unity-prototype-0.34.0.apk"
    $apkUrl = "https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/62a5f8bee65c9956bd5953591b1795207c9a558e/pixel-traffic-unity-prototype-0.34.0.apk"
    $trafficAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
    if (-not (Test-Path -LiteralPath $trafficAdb)) { throw "Unity의 adb.exe를 찾을 수 없습니다." }
    & $trafficAdb start-server
    if ($LASTEXITCODE -ne 0) { throw "ADB 시작 실패" }
    $deviceLines = & $trafficAdb devices
    if ($LASTEXITCODE -ne 0) { throw "폰 연결 확인 실패" }
    $connectedDevices = @($deviceLines | Where-Object { $_ -match '^\S+\s+device$' })
    if ($connectedDevices.Count -ne 1) { throw "폰 1대를 USB로 연결하고 USB 디버깅을 허용해주세요." }
    New-Item -ItemType Directory -Force -Path $apkFolder | Out-Null
    Write-Host "0.34.0 APK 다운로드 중..."
    Invoke-WebRequest -UseBasicParsing -Uri $apkUrl -OutFile $apkPath
    if ((Get-FileHash -LiteralPath $apkPath -Algorithm SHA256).Hash -ne "90fc4ff4378592ae8cc678ce082e9c123bbe1ef932dde9ceb6a48e2aec9c1c06") { throw "파일 검증 실패. 다시 실행해주세요." }
    Write-Host "폰에 앱 설치 중..."
    & $trafficAdb install -r $apkPath
    if ($LASTEXITCODE -ne 0) { throw "앱 설치 실패. 위 오류를 확인해주세요." }
    Write-Host "완료! 0.34.0 설치 성공." -ForegroundColor Green
}
```

설치 후 ‘픽셀 트래픽 Unity’ 설정 → ‘카메라 구도’ → ‘로터리 보기’를 선택한다. ‘도시 전체’로 기존 구도에 돌아갈 수 있다. 폰 외형·저장/재시작·FPS·발열·장시간 동작은 사용자 확인 대기이며 클라우드에서 폰 설치를 실행했다고 주장하지 않는다. Drive 탐색기 설정은 보류한다.

## 2026-10-12 — Unity 0.33.0 로터리 야간 가시성: 검증과 서명 APK 전달 완료

사용자 폰0.32.0 사진에서 로터리가 어둡게 사라졌다. 기존 저장카메라의 로터리 깊이 약270m에 맑은 밤 fogEnd270이 겹쳐 중앙섬과 회전 도로를 완전히 같은 안개색으로 덮는 것이 원인이었다. 맑은 날/노을/밤의 linear haze를250..490/470/450으로 뒤로 옮겼다. 선택 안개의10..105끝점은 유지한다. 폭풍은 기존 weather 가중치와 선택별 전환을 유지하며 새 clear 끝점에서 보간한다.

기존 Street Metal/Lamp Lens와 Lamp Pool 재질로 두 가로등(총72tri)과 접근부 glow quad두개(4tri)를 추가했다. 회전차로 밖 보도에 설치하고 지면 glow만 회전 접근부로 옮겼다. 실시간 조명은 기존4개 중2개를 로터리로 재배치하고38m/무그림자 spot으로 중앙섬과 도로를 비춘다. 총4개/개체당4개,공유재질47개,기존 그래픽 예산을 유지했다. 카메라·차량 경로·진입 예약·정류장·신호·날씨/와이퍼 동작은 유지한다. 지형5mesh/308tri. 버전0.33.0/code34, RoundaboutVisibility-0.33.0.unity / Generated/RoundaboutVisibility0330. 변경8소스·검증128입력·host12와workflow 보존. 시작302파일과 실제index/status를 확인했다.

**실제 PC 검증:** source `6222da7606c5258b83b781b38da7fc56e4349bd1` / [검증 포함 빌드](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38187596334). 맑은3시간대의 실제 저장카메라 fog fraction<0.15 및 선택 안개10..105 유지, 두 조명구성/무collider/총실시간light4 및 로터리spot2의 방향·범위·무그림자 PASS. 기존600초 실제 차량 검사는 진입24,완료23,복귀11,화면밖재진입11,독립 차체검사993347회와15/30/60/120Hz·pause PASS. 신호·보행·승하차·라이트·와이퍼·반사·기후 검사 PASS. 기본108616tri,최대119962tri/1592renderers/47materials로110000/120000/2400/48 미만.

**실제 GPU 화면:** 같은source의162PNG를bytes/SHA/ZIP CRC로 확인했다. 확대카메라뿐 아니라 기존 저장카메라 day/night/fog를 촬영한다. 같은정지장면·같은조명에서 구130..270 안개를 주입한 control과 새 맑은밤을 비교한다. 로터리ROI 변경픽셀1613,밤 close GPU의 red/green250이상 과노출<8% 검사 PASS. 평균absolute RGB delta0.039087,새 fog fraction0.106157. 실제 Editor 촬영이며 사용자 폰 촬영/FPS 검사가 아니다. 비교control은0.32.0 APK 캡처가 아니라 동일0.33.0 장면에 구 fog를 적용한 격리 검사다. 실패/보완 기록: [{"run": 38187432971, "job": 114612695931, "sourceCommit": "6222da7606c5258b83b781b38da7fc56e4349bd1", "reason": "Repository workflow requires APK tag to match current main; candidate tag was created before main publication. Publish exact candidate to main and rerun unchanged tag. No Unity/APK ran on the rejected attempt."}] / ["Saved clear-night fog end270 is reached by the junction from the original camera; use linear clear haze200..490/470/450 without weakening selected fog10..105.", "Two separate three-box shared-material fixtures and two existing pooled glows illuminate ring approaches; four realtime lights, camera/traffic behavior and47material budget retained.", "Add original-camera day/night/fog captures and old130..270 fault comparison with actual GPU ROI pixel differences; no close-camera-only visibility claim.", "Actual saved-night GPU review: move clear haze start200->250 and require<15% at junction in all clear themes, reducing night blue wash without camera/traffic/light-budget changes. Initial successful run not used as final APK source.", "Preserve total four realtime slots but allocate two near-camera and two directional, shadowless38m junction spots;160 intensity compensates the greater distance while retaining normal LampGain transitions. No added triangles/materials over the previous successful run.", "Actual third GPU review found160-intensity spots clipping grass/bus/road. Clamp all four lights to the existing2.3 intensity and require<8% red/green-clipped pixels in the real close-night render. Full BuildApk pipeline validates and captures this exact candidate before main/public delivery."].

**Android:** `unity-apk-0.33.0-build1` / [BuildApk](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38187432971). BuildPlayer 오류0/경고0,launcherLint 오류0/경고8. 실제 내려받은 APK를apksigner/aapt/ZIP CRC로 확인하여0.33.0/code34/min29/target36/ARM64 IL2CPP와wallpaper service/private provider/settings launcher/비활성 Unity Activity를 검증했다. 원본v2서명 인증서SHA256 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6. 변경 없는host12의hostLint38008378207 오류0/경고10을 재사용한다.

[APK](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/622384d010d95256b6c7490834a9b6fe0b30e296/pixel-traffic-unity-prototype-0.33.0.apk) · [기존구도 밤](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/622384d010d95256b6c7490834a9b6fe0b30e296/roundabout-saved-night-preview.png) · [구안개 회귀조건](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/622384d010d95256b6c7490834a9b6fe0b30e296/roundabout-oldfog-preview.png) · [전체 낮](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/622384d010d95256b6c7490834a9b6fe0b30e296/roundabout-city-preview.png) · [확대](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/622384d010d95256b6c7490834a9b6fe0b30e296/roundabout-oblique-preview.png). APK31237619bytes/SHA256`7121299ace3380cf7f361e44d8cc6cb39b158b6ace47fa8c662eb87cc3e19ae1`. 다운로드branch`downloads/unity-0.33.0`의12파일을 로그인 없는HTTP200/no redirect/full bytes/SHA로 확인했다. main에APK·새workflow를 추가하지 않았다.

USB폰1대와 디버깅 허용 후 Windows PowerShell 전체블록을 실행한다. C:\Users\김백현\Desktop\AI에 다운로드·SHA 확인·기존앱 업데이트:
```powershell
& {
    $ErrorActionPreference = "Stop"
    $apkFolder = "C:\Users\김백현\Desktop\AI"
    $apkPath = Join-Path $apkFolder "pixel-traffic-unity-prototype-0.33.0.apk"
    $apkUrl = "https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/622384d010d95256b6c7490834a9b6fe0b30e296/pixel-traffic-unity-prototype-0.33.0.apk"
    $trafficAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
    if (-not (Test-Path -LiteralPath $trafficAdb)) { throw "Unity의 adb.exe를 찾을 수 없습니다." }
    & $trafficAdb start-server
    if ($LASTEXITCODE -ne 0) { throw "ADB 시작 실패" }
    $deviceLines = & $trafficAdb devices
    if ($LASTEXITCODE -ne 0) { throw "폰 연결 확인 실패" }
    $connectedDevices = @($deviceLines | Where-Object { $_ -match '^\S+\s+device$' })
    if ($connectedDevices.Count -ne 1) { throw "폰 1대를 USB로 연결하고 USB 디버깅을 허용해주세요." }
    New-Item -ItemType Directory -Force -Path $apkFolder | Out-Null
    Write-Host "0.33.0 APK 다운로드 중..."
    Invoke-WebRequest -UseBasicParsing -Uri $apkUrl -OutFile $apkPath
    if ((Get-FileHash -LiteralPath $apkPath -Algorithm SHA256).Hash -ne "7121299ace3380cf7f361e44d8cc6cb39b158b6ace47fa8c662eb87cc3e19ae1") { throw "파일 검증 실패. 다시 실행해주세요." }
    Write-Host "폰에 앱 설치 중..."
    & $trafficAdb install -r $apkPath
    if ($LASTEXITCODE -ne 0) { throw "앱 설치 실패. 위 오류를 확인해주세요." }
    Write-Host "완료! 0.33.0 설치 성공." -ForegroundColor Green
}
```

0.32.0 폰 사진의 야간 가림은 확인했으며,0.33.0 폰 외형/FPS/발열/장시간 동작은 적용 확인 대기다. 사용자 폰에 클라우드에서 설치했다고 주장하지 않는다. PC Drive 설정은 보류한다.

## 2026-10-11 — Unity 0.32.0 회전교차로·실제 차량 순환: 검증과 서명 APK 전달 완료

사용자가 0.31.0 산 적용을 확인한 후 뒤쪽 도로 단절 문제를 제보하고, 회전교차로와 좌·우·직진 진출 및 카메라 방향 복귀를 승인했다. 기존24차량을 재사용해 반시계 회전차로, 낮은 잔디 중앙섬과 경계석, 네 방향 연결 도로와 양보 표시를 만들었다. 앞쪽 도로에서 오는 차량은 네 출구를 섞은 순번으로 선택해 네 방향이 모두 사용되도록 하고 측면·강변 도로 차량은 다시 카메라 방향 도로로 돌아온다. 화면 밖 도로 끝에서만 재진입한다. 원경의 낮은 건물12동은 교차로 통로에서 옮기고 바닥 높이를 지면에 맞췄다.

회전차로는 단일 차로/한 대 예약 방식이다. 진입 대기 차량은 긴 버스의 회전 공간 밖에서 기다리고, 선행 차량이 출구 통로를 벗어나면 다음 차량이 들어간다. 오른쪽 출구 깜빡이는 최소 세 번의 점등 주기를 확보하고 진출이 끝날 때까지 유지한다. 실제 회전하는 차체의 네 분리축 간격 검사를 사용한다. 기존 버스 정류장·승하차·차선변경·신호·기후/와이퍼/라이트를 함께 검증했다. 한 번에 여러 차량이 순환하는 높은 교통량 회전교차로는 구현하지 않았다.

버전0.32.0/code33, 장면 Roundabout-0.32.0.unity, 생성 Generated/Roundabout0320. 변경19소스(신규3CS+meta), 검증 입력128파일. 공유재질 지형5mesh/308tri, 원경의 불필요한 차선 일부를 제거해 기존 예산을 확보했다. 실제 GPU 촬영에서 발견한 보도 끝 겹침과 공유재질의 긴 박스 UV 배율을 정리해 연결부와 잔디섬을 보완했다. 카메라·산·34도시탑·32나무·24건물·20가로등·결합신호등2개와 Android host12 및 workflow를 유지했다. 스프레이와 헤드라이트/반사 bounds만 새 경로를 포함하도록 넓혔다. 시작296파일·실제index/status·128입력 SHA와 host12를 확인했다.

**실제 검증:** source `5dca913c15b1de54fca523ecc9d35e529e85660b` / [Validate](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38157588965). 실제 직렬화된24차량600초 검사에서 진입24, 순환완료23, 전방복귀11, 화면밖재진입11, 출구선택[4, 4, 4, 5], 회전차체검사993347회, 버스출발5회. 독립적인 차체 polygon overlap 검사·중앙섬 침범·반시계 진행·가시 구간 이동 연속성·우측 실제램프·폭풍·15/30/60/120Hz와pause PASS. 기존 보행/신호/버스/기후/와이퍼/반사 검사도 PASS. 기본108540tri, 최대119886tri/1584renderers/47materials로 기존110000/120000/2400/48 예산 미만이다.

**실패와 수정:** [{"run": 38150762827, "commit": "ffa84871ed996204a35002b6a8eb7dbfb7c81fec", "artifact": 11705808253, "cause": "CS0136 CityPreview new tracked variable conflicts with existing local; renamed junctionTracked. No APK built."}, {"run": 38150975311, "commit": "6ffc712185c719b2f9e41ba0b2f97bd39a313377", "artifact": 11706639597, "cause": "Full-fleet stop reservation minimum-gap assertion failed. Junction handoff free check now uses existing SafetyLength (yaw envelope) rather than raw body length; failure includes pair/tick/poses for diagnosis."}, {"run": 38151234902, "commit": "8c8f2d1b082552d1940049960e3974e79edf4c5c", "artifact": 11706684954, "cause": "Same legacy full-fleet minimum-gap assertion; run started before SafetyLength handoff repair. Not used as final validation."}, {"run": 38151534326, "commit": "db591fdb78df4c384d0d0dc8cb7f200514849032", "artifact": 11707425475, "cause": "Legacy bus gap check passed; new saved-portrait endpoint visibility check failed. Endpoints moved from +/-80 to +/-140m; roads extended to +/-160m. Initial reset vehicles beyond gate become offscreen sources instead of snapping visibly back to entry."}, {"run": 38152312146, "commit": "5bf6b6d92f49e8f53ad018597098b79314e5d658", "artifact": 11706509992, "cause": "Actual600sec path run found stall: entries1/completed0/returns0/recycles0. Admission now requires whole entry approach free, preventing a long rear vehicle acquiring reservation behind a front waiting vehicle. Added owner/progress/body/blocker failure diagnostic."}, {"run": 38153047219, "commit": "52a4854f04e0d8c5afdbb8d837bc97315d5d562f", "artifact": 11707083951, "cause": "Actual600sec stall remains: owner3, approach progress12.437/entry31.063 blocked by car12 advancing after reservation. Reserve before convergence and protect full remaining entry/ring/exit corridor against new junction and road movements; complete path must be free at admission. Exit tangent control reduced7m->3m to keep outbound turns away from opposing inbound lane. Diagnostic made single line."}, {"run": 38153781196, "commit": "2366638edf47b435992f112da7e81f5526edd4b1", "artifact": 11707597647, "cause": "Full corridor protection completes6 routes/returns5/recycle1 but later admission stalls with owner=-1. New path check isolated on fresh saved startup scene before unrelated legacy fixtures; admission blocker diagnostics record per-car future blocking pose without1500-character log truncation. No pass criteria relaxed."}, {"run": 38154749375, "commit": "e8eff4b2d9c83df409d0b1a971ede142af6d222d", "artifact": 11708351330, "cause": "Fresh startup confirms6/6/5/1 stall, owner=-1. Candidate7 blockedby rear normalcar23 at route distance0; candidate12 similarly rearcar22. RoadRoom used raw waiting vehicle length while admission used SafetyLength, allowing rear car too close to conservative reservation. Projected RoadRoom now uses SafetyLength on both width/length terms. Fixed consistency; no threshold relaxed."}, {"run": 38154676588, "commit": "9e870ca9fb44d8da13f2db5fc7cac1a6c8d00c21", "cause": "Intermediate single-file publication after transient push auth error lacked AdmissionBlocker source dependency. Complete19-source publication immediately replaced it; never built APK."}, {"run": 38155037923, "commit": "8303c761bb03db540f228fd573607abd2fe725b1", "artifact": 11707972903, "cause": "Flow improves8/8/6/2 but idle gate admission still blocked at distance0 by normal followers22 and2. Incoming Bezier gate heading is slightly rotated whereas rear road following assumes straight axes. Enabled-roundabout following now keeps2.2m versus reservation1.8m, including0.4m orientation/start-transition buffer. Legacy non-roundabout fixtures retain1.8m; all pass criteria unchanged."}, {"run": 38155463912, "commit": "4708db8fcca4b46ad969de197a6258e33ed77806", "artifact": 11708467211, "cause": "Actual600sec flow, returns/recycles and independent collisions pass thresholds; one of four random destinations not selected. Use Fisher-Yates shuffle bag across successful South admissions, consuming only when real handoff is clear; four directions per randomized group. Destination assertion remains."}, {"run": 38157035596, "job": 114522034009, "sourceCommit": "5f52bc34e3ef78487b8d336a1bf6dfc4ef5e3263", "reason": "Unity Package Manager IPC disconnected during initial fresh Library rebuild; package resolve cancelled after .23 seconds, before source compilation/validation. Retrying unchanged source."}]. source review 보완: ["Entry waiting line moved outside a full circulating bus swept envelope: body half-length plus16m before circle entry, reservation granted at that line. Earlier validation38150975311 still pending.", "Actual GPU review: connect adjacent flared pavement strips at common ring diagonal; remove intersecting cardinal annulus strips; curb approach edges; undo shared box material tiling for world-space terrain and map island once.", "Support both actual north-port Bezier river-road bends with 16 asphalt ribbon quads each, including body-width margin; open the near-bank pavement/curb across the road connection so buses do not cross a raised strip. Fresh run avoids duplicate artifact names on the retried failed run.", "Corrected new river ribbon face winding and added a saved-mesh upward-normal visibility assertion before any APK tag. Supersedes the pending intermediate source."]. 로컬 helper anchor 불일치/중복필드도 원격 실행 전에 수정했다. 중간본이나 실패본을 최종 PASS로 사용하지 않았다.

**실제 화면:** Unity6000.3.26f1/D3D11 GPU158PNG를 동일source/bytes/SHA/ZIP CRC로 확인했다. 회전교차로 낮·밤·비·사선과 실제 차량 연속4프레임, 전체 구도를 확인했다. 연속프레임의 변경픽셀[8388, 9885, 6841]과 실제 좌표·yaw는preview-result.json에 기록했다. 실제 Editor 촬영이며 폰/FPS 측정이 아니다. 시안과 동일한 완성도를 주장하지 않는다.

**Android:** `unity-apk-0.32.0-build1` / [BuildApk](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38158611856). BuildPlayer 오류0/경고0, launcherLint 오류0/경고8. 내려받은 APK를 apksigner/aapt/ZIP CRC로 검사하여0.32.0/code33/min29/target36/ARM64 IL2CPP와wallpaper service/private provider/settings launcher/비활성 Unity Activity를 확인했다. 원본v2서명 인증서SHA256 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6. 변경 없는host12에 대한hostLint38008378207 오류0/경고10 결과를 재사용했다.

[APK](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/9e4d3bcc63e610b93aabcbd1e7ff4285a28580b5/pixel-traffic-unity-prototype-0.32.0.apk) · [전체 화면](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/9e4d3bcc63e610b93aabcbd1e7ff4285a28580b5/roundabout-city-preview.png) · [회전교차로 낮](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/9e4d3bcc63e610b93aabcbd1e7ff4285a28580b5/roundabout-day-preview.png) · [밤](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/9e4d3bcc63e610b93aabcbd1e7ff4285a28580b5/roundabout-night-preview.png) · [사선](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/9e4d3bcc63e610b93aabcbd1e7ff4285a28580b5/roundabout-oblique-preview.png). APK31238263bytes/SHA256`260cf40e485ac26cf1c4ab5eeedcb2cdfb75a2f2ac38ddea1fad20b8bfb3adfb`. 다운로드branch`downloads/unity-0.32.0`의8파일에 대해 로그인 없는HTTP200/no redirect/full bytes/SHA를 확인했다. main에는APK·새workflow를 추가하지 않았다.

USB폰1대와 디버깅 허용 후 아래 전체 Windows PowerShell 블록을 실행한다. 사용자 지정 C:\Users\김백현\Desktop\AI에 다운로드하고SHA 검증 후 기존앱을 업데이트한다.
```powershell
& {
    $ErrorActionPreference = "Stop"
    $apkFolder = "C:\Users\김백현\Desktop\AI"
    $apkPath = Join-Path $apkFolder "pixel-traffic-unity-prototype-0.32.0.apk"
    $apkUrl = "https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/9e4d3bcc63e610b93aabcbd1e7ff4285a28580b5/pixel-traffic-unity-prototype-0.32.0.apk"
    $trafficAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
    if (-not (Test-Path -LiteralPath $trafficAdb)) { throw "Unity의 adb.exe를 찾을 수 없습니다." }
    & $trafficAdb start-server
    if ($LASTEXITCODE -ne 0) { throw "ADB 시작 실패" }
    $deviceLines = & $trafficAdb devices
    if ($LASTEXITCODE -ne 0) { throw "폰 연결 확인 실패" }
    $connectedDevices = @($deviceLines | Where-Object { $_ -match '^\S+\s+device$' })
    if ($connectedDevices.Count -ne 1) { throw "폰 1대를 USB로 연결하고 USB 디버깅을 허용해주세요." }
    New-Item -ItemType Directory -Force -Path $apkFolder | Out-Null
    Write-Host "0.32.0 APK 다운로드 중..."
    Invoke-WebRequest -UseBasicParsing -Uri $apkUrl -OutFile $apkPath
    if ((Get-FileHash -LiteralPath $apkPath -Algorithm SHA256).Hash -ne "260cf40e485ac26cf1c4ab5eeedcb2cdfb75a2f2ac38ddea1fad20b8bfb3adfb") { throw "파일 검증 실패. 다시 실행해주세요." }
    Write-Host "폰에 앱 설치 중..."
    & $trafficAdb install -r $apkPath
    if ($LASTEXITCODE -ne 0) { throw "앱 설치 실패. 위 오류를 확인해주세요." }
    Write-Host "완료! 0.32.0 설치 성공." -ForegroundColor Green
}
```

0.31.0은 사용자 폰 산 적용 확인,0.32.0 폰 외형/FPS/발열/장시간 동작은 확인 대기다. 클라우드에서 사용자 폰에 설치했다고 주장하지 않는다. PC Drive 설정은 보류 상태를 유지한다.

## 2026-10-11 — Unity 0.31.0 산 능선·원경 깊이: 검증과 서명 APK 전달 완료

사용자가 0.30.0의 폰 적용 성공을 확인하고 산·원경 그래픽 작업을 승인했다. 산 세 겹의 봉우리, 어깨와 능선 리듬을 달리하고, 가까운 산의 녹색과 먼 산의 푸른색·안개를 분리했다. 연속된 world-space 표면 명암과 비탈 패턴을 적용하고 거리가 멀수록 대비를 줄인다. 낮·노을·밤의 색과 비·안개·눈은 기존 기후 가중치에 따라 변한다.

변경 소스는 DistantScene, DistantChecks, Distant.shader, StarterScene, StarterConfig 5개다. 버전 0.31.0/code32, 장면 MountainDepth-0.31.0.unity, 생성 경로 Generated/MountainDepth0310. 각 산의 64 strip/128 triangle과 원경 8 mesh·단일 runtime 재질을 유지했다. 카메라, 강·하늘 shader 분기, 도시 34동·다리, 가로수·건물·결합 신호등 2개, 기능, Android host와 workflow는 변경하지 않았다. 산 shader의 fragment 계산은 추가됐으며 폰 성능은 아직 측정하지 않았다.

**실제 검증:** 최종 source `678d6055af58307b179dca4cbcec5f5f5d09b352` / [Validate](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38146868380). 산 3층/192 strip의 높이·깊이·seam 연속성·거리별 tone data·topology와 실제 shader _MountainDepth 기본값 검사 PASS. 기존 18 기후, 15/30/60/120Hz 전환, 낮→노을→밤·hidden clock·retarget와 차량·와이퍼·보행·승하차·반사·신호 검사를 통과했다. 기본 108592 triangles, 최대 119938 triangles/1609 renderers/47 materials로 기존 예산 110000/120000/2400/48 미만이다. 시작 시 296파일·실제 index/status·122입력·host12 SHA를 보존했다. 실패 기록: [].

**실제 화면과 보완:** 초기 Validate38146222498는 통과했지만, 실제 GPU 화면을 확인하니 가까운 산의 표면 명암이 약했다. 연속된 비탈 패턴과 대비를 보강한 최종 source로 다시 검증했다. Unity6000.3.26f1/D3D11의 최종 150 PNG를 source SHA·bytes·SHA·ZIP CRC로 확인했다. 전체 구도와 산 day/sunset/night/fog/storm 6장을 직접 관찰하고 0.30.0 전체/day 2장과 비교했다. 기존 캡처 코드를 재사용했다. 실제 Editor 화면이며 폰·FPS 측정이나 AI 시안이 아니다. 시안과 같은 완성 수준을 달성했다고 주장하지 않는다.

**Android:** `unity-apk-0.31.0-build1` / [BuildApk](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38147479517). BuildPlayer 오류0/경고0, launcher Lint 오류0/경고8. APK를 SDK apksigner/aapt/ZIP CRC로 검사해 0.31.0/code32/min29/target36/ARM64 IL2CPP, wallpaper service, private provider, settings launcher와 비활성 Unity Activity를 확인했다. 원본 v2 서명 인증서 SHA256은 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6이다. 변경 없는 host12 SHA에 대해 hostLint38008378207의 오류0/경고10 결과를 재사용했다.

**전달:** [APK](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/1b3e4b269aea190adb5b5b9a5857119e7d4869cb/pixel-traffic-unity-prototype-0.31.0.apk) · [전체 화면](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/1b3e4b269aea190adb5b5b9a5857119e7d4869cb/mountain-city-preview.png) · [산 확대](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/1b3e4b269aea190adb5b5b9a5857119e7d4869cb/mountain-day-preview.png) · [노을](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/1b3e4b269aea190adb5b5b9a5857119e7d4869cb/mountain-sunset-preview.png) · [밤](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/1b3e4b269aea190adb5b5b9a5857119e7d4869cb/mountain-night-preview.png) · [안개](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/1b3e4b269aea190adb5b5b9a5857119e7d4869cb/mountain-fog-preview.png). APK 31227127 bytes/SHA256 `9ac47dc95f54d8b64a29367593a7713bdec8187106d3e8535b16a4cbb70e51d8`. 다운로드 branch `downloads/unity-0.31.0`의 8파일은 로그인 없는 HTTP200/no redirect/full bytes/SHA 확인을 통과했다. main에는 APK나 새 workflow를 추가하지 않았다.

USB 폰 1대를 연결하고 디버깅을 허용한 뒤 아래 Windows PowerShell 블록 전체를 실행한다. 사용자 지정 C:\Users\김백현\Desktop\AI 폴더로 다운로드하고 SHA를 검증한 뒤 기존 앱을 업데이트한다. 클라우드에서 사용자 폰에 설치했다고 주장하지 않는다.
```powershell
& {
    $ErrorActionPreference = "Stop"
    $apkFolder = "C:\Users\김백현\Desktop\AI"
    $apkPath = Join-Path $apkFolder "pixel-traffic-unity-prototype-0.31.0.apk"
    $apkUrl = "https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/1b3e4b269aea190adb5b5b9a5857119e7d4869cb/pixel-traffic-unity-prototype-0.31.0.apk"
    $trafficAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
    if (-not (Test-Path -LiteralPath $trafficAdb)) { throw "Unity의 adb.exe를 찾을 수 없습니다." }
    & $trafficAdb start-server
    if ($LASTEXITCODE -ne 0) { throw "ADB 시작 실패" }
    $deviceLines = & $trafficAdb devices
    if ($LASTEXITCODE -ne 0) { throw "폰 연결 확인 실패" }
    $connectedDevices = @($deviceLines | Where-Object { $_ -match '^\S+\s+device$' })
    if ($connectedDevices.Count -ne 1) { throw "폰 1대를 USB로 연결하고 USB 디버깅을 허용해주세요." }
    New-Item -ItemType Directory -Force -Path $apkFolder | Out-Null
    Write-Host "0.31.0 APK 다운로드 중..."
    Invoke-WebRequest -UseBasicParsing -Uri $apkUrl -OutFile $apkPath
    if ((Get-FileHash -LiteralPath $apkPath -Algorithm SHA256).Hash -ne "9ac47dc95f54d8b64a29367593a7713bdec8187106d3e8535b16a4cbb70e51d8") { throw "파일 검증 실패. 다시 실행해주세요." }
    Write-Host "폰에 앱 설치 중..."
    & $trafficAdb install -r $apkPath
    if ($LASTEXITCODE -ne 0) { throw "앱 설치 실패. 위 오류를 확인해주세요." }
    Write-Host "완료! 0.31.0 설치 성공." -ForegroundColor Green
}
```

0.30.0은 사용자가 폰 적용 성공을 확인했다. 0.31.0의 폰 외형·FPS·발열·장시간 동작은 사용자 확인 대기다. PC Drive 설정 보류는 유지한다. 다음 작업은 사용자 확인과 요청에 따른다.

## 2026-10-11 — Unity 0.30.0 작은 잎 군집·수관 명암: 실제 검증·서명 APK 전달 완료

사용자가폰0.29.0 성공확인후가로수잎디테일작업승인. 가까운8그루의큰7덩어리를각2작은곡면로브와상부팁으로분할,15군집/1200tri/630vertices. 먼24그루7군집/140tri유지. 군집윤곽과틈,높이기반UV+공유256miptexture의상하gradient로햇빛/그늘강화. 기존32그루/6mesh/3잎재질과불투명렌더러·바람·기후·보행/차량·중복신호수정·카메라·건물·Android host/workflow유지. FoliageScene/SceneryChecks/StarterScene/StarterConfig4소스,0.30.0/code31,FineFoliage-0.30.0.unity/Generated/FineFoliage0300.

**실제 검사:** source `96bbbd73caab9f428bde1527b12604fc497a8620` / [Validate](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38141830753). 실제15분리군집/작은bounds/높이UV/texture상하gradient/near-far topology/geometry normals/winding/UV/보행clearance와기존기후/와이퍼/승하차/반사/2결합신호검사PASS. 기본108592triangles,최대119938triangles/1609renderers/47materials로기존110000/120000/2400/48예산미만. 새재질/Light/renderer추가없음. 시작296파일/index/status/122입력/host12SHA보존. 로컬편집helper가버전문자열의2회등장을1회로예상하여중단;원래baseline을보존한채남은버전편집만재개하고실제검증완료. 초기검증38141209479은PASS지만확대에서49저폴리군집이각지고떨어져보여15곡면군집으로다듬은뒤다시검증했다. Unity실패기록[].

**실제 화면:** Unity6000.3.26f1/D3D11 150PNG artifact sourceSHA/bytes/SHA/ZIPCRC확인. 전체와3수종day/night/storm을직접관찰하고이전0.29.0 전체/수종과비교하여변화확인. 실제Editor캡처이며AI시안/폰/FPS측정아님. 기존150캡처코드재사용. 개별잎자산완성이나시안과동일함을주장하지않는다.

**Android:** `unity-apk-0.30.0-build1` / [BuildApk](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38142366995). BuildPlayer오류0/경고0; launcherLint오류0/경고8. 실제APK SDK apksigner/aapt/ZIPCRC로0.30.0/code31/min29/target36/ARM64IL2CPP·wallpaper service/privateprovider/settingslauncher/비활성UnityActivity·원본v2cert a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6확인. 변경없는host12SHA는hostLint38008378207오류0/경고10재사용.

**전달:** [APK](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/b4b3810c4665cebfb12166b3fe45d9c7a6974d8c/pixel-traffic-unity-prototype-0.30.0.apk) · [전체](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/b4b3810c4665cebfb12166b3fe45d9c7a6974d8c/foliage-city-preview.png) · [확대](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/b4b3810c4665cebfb12166b3fe45d9c7a6974d8c/foliage-rounded-preview.png) · [밤](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/b4b3810c4665cebfb12166b3fe45d9c7a6974d8c/foliage-night-preview.png) · [넓은수종](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/b4b3810c4665cebfb12166b3fe45d9c7a6974d8c/foliage-spreading-preview.png). 31223459bytes/SHA256 `b2beba85040d5c75f2281aaa32b28861aa79392a493814454688869e0be7d72e`. `downloads/unity-0.30.0`8파일로그인없는HTTP200/no redirect/fullbytes/SHA PASS. main에APK/newworkflow추가없음.

USB폰1대연결·디버깅허용후아래WindowsPowerShell전체붙여넣기. 사용자 C:\Users\김백현\Desktop\AI로다운로드·SHA검증·기존앱업데이트. 클라우드에서사용자폰에설치했다고주장하지않는다.
```powershell
& {
    $ErrorActionPreference = "Stop"
    $apkFolder = "C:\Users\김백현\Desktop\AI"
    $apkPath = Join-Path $apkFolder "pixel-traffic-unity-prototype-0.30.0.apk"
    $apkUrl = "https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/b4b3810c4665cebfb12166b3fe45d9c7a6974d8c/pixel-traffic-unity-prototype-0.30.0.apk"
    $trafficAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
    if (-not (Test-Path -LiteralPath $trafficAdb)) { throw "Unity의 adb.exe를 찾을 수 없습니다." }
    & $trafficAdb start-server
    if ($LASTEXITCODE -ne 0) { throw "ADB 시작 실패" }
    $deviceLines = & $trafficAdb devices
    if ($LASTEXITCODE -ne 0) { throw "폰 연결 확인 실패" }
    $connectedDevices = @($deviceLines | Where-Object { $_ -match '^\S+\s+device$' })
    if ($connectedDevices.Count -ne 1) { throw "폰 1대를 USB로 연결하고 USB 디버깅을 허용해주세요." }
    New-Item -ItemType Directory -Force -Path $apkFolder | Out-Null
    Write-Host "0.30.0 APK 다운로드 중..."
    Invoke-WebRequest -UseBasicParsing -Uri $apkUrl -OutFile $apkPath
    if ((Get-FileHash -LiteralPath $apkPath -Algorithm SHA256).Hash -ne "b2beba85040d5c75f2281aaa32b28861aa79392a493814454688869e0be7d72e") { throw "파일 검증 실패. 다시 실행해주세요." }
    Write-Host "폰에 앱 설치 중..."
    & $trafficAdb install -r $apkPath
    if ($LASTEXITCODE -ne 0) { throw "앱 설치 실패. 위 오류를 확인해주세요." }
    Write-Host "완료! 0.30.0 설치 성공." -ForegroundColor Green
}
```

폰0.29.0 사용자성공확인;폰0.30.0 외형/FPS/발열/장시간동작은사용자확인대기. PC Drive설정보류유지. 다음작업은사용자확인과요청에따른다.

## 2026-10-11 — Unity 0.29.0 보행자 신호등 중복 제거: 실제 검증·서명 APK 전달 완료

사용자 이미지의 결합신호기둥 위쪽 별도보행자신호 중복 신고를 수정했다. StreetScene이각curb z7/13에2기둥씩4기둥을생성하던원인. 별도보행자전용기둥2개를제거하고 차량/보행자결합기둥양쪽1개씩2개유지. 실제2기둥/2차량머리8렌즈/2보행머리4아이콘렌즈를검사하고 orphan/중복머리도직접계수한다. 높이·위치·신호색·표시방향·보행입장/차량정지·기후/기능은유지. 기존4기둥용안전회피구역을보수적으로유지하여보행경로변경을섞지않았다. StreetScene/StreetChecks/StarterScene/StarterConfig4소스,0.29.0/code30,UniquePedestrianSignals-0.29.0.unity/Generated/UniquePedestrianSignals0290. 가로수/건물/카메라/Android host/workflow변경없음.

**실제 검증:** source `2e2c60b49f80d1bc4d7c6af9c38906b17f2a92ef` / [Validate](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38138769347). 기둥2·신호머리유일성·curb정렬·직렬화실제렌즈배열·6신호상태·양쪽보행아이콘동기·보행/차량안전·기존기후/와이퍼/승하차/반사/그래픽검사 PASS. 실제기본103472triangles,최대114818triangles/1609renderers/47materials로기존110000/120000/2400/48 예산미만. 시작296파일/index/status/122입력/host12SHA보존. Unity실패기록[].

**실제 화면:** Unity6000.3.26f1/D3D11 150PNG artifact의sourceSHA/bytes/SHA/ZIPCRC 확인. 전체구도·야간비·차량/보행stop/walk실제캡처를관찰해남은결합기둥과아이콘확인. 기존150캡처코드재사용; AI생성이미지나폰/FPS캡처아님.

**Android:** `unity-apk-0.29.0-build1` / [BuildApk](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38139284695). BuildPlayer오류0/경고0; launcherLint오류0/경고8. 실제APK SDK apksigner/aapt/ZIPCRC로0.29.0/code30/min29/target36/ARM64IL2CPP·wallpaper service/privateprovider/settingslauncher/비활성UnityActivity·원본v2cert a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6 확인. 변경없는host12SHA는hostLint38008378207 오류0/경고10재사용.

**전달:** [APK](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/2910337728d7649aad511dd01655c008870ed2bb/pixel-traffic-unity-prototype-0.29.0.apk) · [전체](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/2910337728d7649aad511dd01655c008870ed2bb/signal-city-preview.png) · [야간비](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/2910337728d7649aad511dd01655c008870ed2bb/signal-night-preview.png) · [보행신호](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/2910337728d7649aad511dd01655c008870ed2bb/signal-walk-preview.png). 31194891bytes/SHA256 `8b2fdee93eaa60dba5e1d0bd16e6ee9748bfbe97e75cab4829e754b20a6b731c`. 다운로드branch `downloads/unity-0.29.0`의8파일 로그인없는HTTP200/no redirect/fullbytes/SHA PASS. main에 APK/newworkflow추가없음.

사용자폴더C:\Users\김백현\Desktop\AI로다운로드·SHA검증·USB업데이트설치. 폰1대USB연결·디버깅허용후사용자WindowsPowerShell에전체붙여넣기. 클라우드에서사용자폰에설치했다고주장하지않는다.
```powershell
& {
    $ErrorActionPreference = "Stop"
    $apkFolder = "C:\Users\김백현\Desktop\AI"
    $apkPath = Join-Path $apkFolder "pixel-traffic-unity-prototype-0.29.0.apk"
    $apkUrl = "https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/2910337728d7649aad511dd01655c008870ed2bb/pixel-traffic-unity-prototype-0.29.0.apk"
    $trafficAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
    if (-not (Test-Path -LiteralPath $trafficAdb)) { throw "Unity의 adb.exe를 찾을 수 없습니다." }
    & $trafficAdb start-server
    if ($LASTEXITCODE -ne 0) { throw "ADB 시작 실패" }
    $deviceLines = & $trafficAdb devices
    if ($LASTEXITCODE -ne 0) { throw "폰 연결 확인 실패" }
    $connectedDevices = @($deviceLines | Where-Object { $_ -match '^\S+\s+device$' })
    if ($connectedDevices.Count -ne 1) { throw "폰 1대를 USB로 연결하고 USB 디버깅을 허용해주세요." }
    New-Item -ItemType Directory -Force -Path $apkFolder | Out-Null
    Write-Host "0.29.0 APK 다운로드 중..."
    Invoke-WebRequest -UseBasicParsing -Uri $apkUrl -OutFile $apkPath
    if ((Get-FileHash -LiteralPath $apkPath -Algorithm SHA256).Hash -ne "8b2fdee93eaa60dba5e1d0bd16e6ee9748bfbe97e75cab4829e754b20a6b731c") { throw "파일 검증 실패. 다시 실행해주세요." }
    Write-Host "폰에 앱 설치 중..."
    & $trafficAdb install -r $apkPath
    if ($LASTEXITCODE -ne 0) { throw "앱 설치 실패. 위 오류를 확인해주세요." }
    Write-Host "완료! 0.29.0 설치 성공." -ForegroundColor Green
}
```

폰0.29.0 외형/성능은 사용자확인대기. 폰0.27.0 사용자성공확인;0.28.0 전달후중복신호신고. PC Drive설정 보류유지. 다음그래픽개발은사용자확인과요청에따른다.

## 2026-10-11 — Unity 0.28.0 가로수 그래픽: 실제 검증·서명 APK 전달 완료

사용자가 폰0.27.0 적용 성공을 확인하고 가로수 그래픽 개선을 승인했다. 0.28.0/code29, TreeCanopy-0.28.0.unity/Generated/TreeCanopy0280. FoliageScene/CityEnvironment/SceneryChecks/StarterScene/StarterConfig 5소스를 변경했다. 32그루/3잎재질/공유256 mip texture를 유지하고 근거리8그루560tri·원거리24그루140tri의6mesh로 수관을 만든다. 기존400tri 대비 수관4960tri 절감. 서로다른7비대칭군집,3수종 형태,잎녹색·밝기세분화,크기·높이·기울기와 좌우yaw를 다양화했다. 기존3가지 길이를 늘려 잎과 연결하고 횡단보도 인접 crown을 축소한다. planted bed/보행 동선/카메라·건물·차량·기후·Android host/workflow 유지. 불투명 shared mesh로 투명overdraw/새재질/Light/renderer를 추가하지 않는다.

**실제 검사:** source `013f073034498beeacf228d59bdc603dbefaf26f` / [Validate](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38136988653). 원근 topology/수관 scale/줄기·winding/normal/UV·공유재질·mip texture/수종 형태·보행clearance와 기존건물/차량/와이퍼/기후/승하차/차선/신호/반사 검사 PASS. 실제 최대 114950triangles/1623renderers/47materials로 기존120000/2400/48 제한미만, 기본103604triangles로110000 예약예산미만. 시작296파일·실제index/status·122빌드입력·host12SHA 보존. 소스게시 전 preservation helper에 sourceCommit이 아직 없어 KeyError 발생; 게시 후 동일 검사는 PASS. Unity/빌드 실패기록: [{"run": 38136821459, "source": "2bc68ba24e1238481e609470e6f3c1cb1e9f91e6", "reason": "CS0136 tree near variable scope; tree checks duplicated into building blocks by global edit; fixed scoped checks and treeNear variable"}].

**실제 화면:** Unity6000.3.26f1/D3D11 150PNG를 받아 artifact sourceSHA/bytes/SHA/ZIPCRC 확인. 전체구도와 3수종day·storm/night를 직접관찰했다. 확대는 제품과 같은 장면의 임시Editor 카메라이며 실제폰/FPS 측정이 아니다. 기존150캡처를 재사용하고 새로운 preview code는 추가하지 않는다.

**Android:** `unity-apk-0.28.0-build1` / [BuildApk](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38137519032). BuildPlayer 오류0/경고0; launcherLint 오류0/경고8. 실제 APK를 SDK apksigner/aapt/ZIPCRC로0.28.0/code29/min29/target36/ARM64IL2CPP와 wallpaper service/private settings provider/settings launcher/비활성Unity Activity·원본v2인증서 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6 확인. 변경 없는 host12 SHA는 hostLint38008378207 오류0/경고10 결과를 재사용했다.

**전달:** [APK](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/0681dc12820ea7f0a6c46a21003de0962bd746a4/pixel-traffic-unity-prototype-0.28.0.apk) · [전체](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/0681dc12820ea7f0a6c46a21003de0962bd746a4/tree-city-preview.png) · [둥근수종](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/0681dc12820ea7f0a6c46a21003de0962bd746a4/tree-rounded-preview.png) · [직립수종](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/0681dc12820ea7f0a6c46a21003de0962bd746a4/tree-upright-preview.png) · [넓은수종](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/0681dc12820ea7f0a6c46a21003de0962bd746a4/tree-spreading-preview.png) · [밤](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/0681dc12820ea7f0a6c46a21003de0962bd746a4/tree-night-preview.png) · [폭풍](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/0681dc12820ea7f0a6c46a21003de0962bd746a4/tree-storm-preview.png). 31194795bytes/SHA256 `102f4fd6c887723a40d013586f9fc8bf6f4ec01947d77b01c250c53c09166583`. `downloads/unity-0.28.0` 8파일 실제로그인없는 HTTP200/no redirect/fullbytes/SHA PASS. main에 APK나 새workflow를 추가하지 않았다.

폰1대를USB 연결·디버깅 허용 후 아래PowerShell전체 실행. 사용자 지정 C:\Users\김백현\Desktop\AI 로다운로드·SHA검증·기존앱업데이트. 클라우드에서사용자폰에 설치했다고 주장하지 않는다.
```powershell
& {
    $ErrorActionPreference = "Stop"
    $apkFolder = "C:\Users\김백현\Desktop\AI"
    $apkPath = Join-Path $apkFolder "pixel-traffic-unity-prototype-0.28.0.apk"
    $apkUrl = "https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/0681dc12820ea7f0a6c46a21003de0962bd746a4/pixel-traffic-unity-prototype-0.28.0.apk"
    $trafficAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
    if (-not (Test-Path -LiteralPath $trafficAdb)) { throw "Unity의 adb.exe를 찾을 수 없습니다." }
    & $trafficAdb start-server
    if ($LASTEXITCODE -ne 0) { throw "ADB 시작 실패" }
    $deviceLines = & $trafficAdb devices
    if ($LASTEXITCODE -ne 0) { throw "폰 연결 확인 실패" }
    $connectedDevices = @($deviceLines | Where-Object { $_ -match '^\S+\s+device$' })
    if ($connectedDevices.Count -ne 1) { throw "폰 1대를 USB로 연결하고 USB 디버깅을 허용해주세요." }
    New-Item -ItemType Directory -Force -Path $apkFolder | Out-Null
    Write-Host "0.28.0 APK 다운로드 중..."
    Invoke-WebRequest -UseBasicParsing -Uri $apkUrl -OutFile $apkPath
    if ((Get-FileHash -LiteralPath $apkPath -Algorithm SHA256).Hash -ne "102f4fd6c887723a40d013586f9fc8bf6f4ec01947d77b01c250c53c09166583") { throw "파일 검증 실패. 다시 실행해주세요." }
    Write-Host "폰에 앱 설치 중..."
    & $trafficAdb install -r $apkPath
    if ($LASTEXITCODE -ne 0) { throw "앱 설치 실패. 위 오류를 확인해주세요." }
    Write-Host "완료! 0.28.0 설치 성공." -ForegroundColor Green
}
```

폰0.27.0 사용자성공확인; 폰0.28.0 외형/FPS/발열/장시간 lifecycle은 사용자확인대기. 시안과의차이는여전히있고 이번은 가로수그래픽단계다. 실제 확대에서 여전히 덩어리 형태가 읽히며 개별 잎이나 시안 수준의 수목 자산이 완성되었다고 주장하지 않는다. PC Drive설정 보류유지. 다음작업은실제폰확인과요청에따른다.

## 2026-10-11 — Unity 0.27.0 건물 형태·옆면 창문·상층 테라스: 실제 검증·서명 APK 전달 완료

사용자는 폰0.26.0 적용 성공을 확인하고 앞쪽 대표 건물부터 시안처럼 정교하게 만드는 작업을 승인했다. **0.27.0/code28**, `BuildingVariety-0.27.0.unity` / `Generated/BuildingVariety0270`으로 생성했다. 앞으로 APK는 사용자 Windows 폴더 `C:\Users\김백현\Desktop\AI`로 다운로드·SHA256 검증·USB ADB 설치를 한 번에 실행하는 PowerShell 블록으로 전달한다. 실제 명령 실행은 사용자 PC에서 하며 클라우드에서 설치했다고 주장하지 않는다.

**건물 형태:** 앞쪽6동의 층수를 왼쪽6/5/4층·오른쪽4/6/5층으로 달리하고 각 옥상에 서로 다른6setback cap을 더했다. 기존roof access 크기도 달리하며 설비와grill을 같이 옮겨 새cap과 겹치지 않게 했다. 건물 위치/폭/도로·카메라·보행 footprint는 유지한다. 기존 주도로480창 중 앞120창의 깊은 frame/sill은 유지하고 보이는 끝면에48창을 추가해총528창을 만든다. 끝면 유리와 드러낸 frame/sill은 실제벽 바깥이며 기존2shared meshes/material에 합친다. 기존lit/dark/warm/cool atlas/상가3종/낮→노을→밤 emission을 유지한다.

**발코니·옥상:** 가까운6동의 기존 발코니 폭/깊이를3규격으로 달리하고 각 상층에4m wide terrace/6post를 더했다. 실제총54발코니/228railposts/6상층테라스/18발코니식물/12실외기, roofProfiles6와 폭종류4를 검사했다. 화분 높이·위치를 profile마다 달리하며 sharedLeaves/RoofShrub는 그대로 재사용한다. 실외기중심은실제window열사이±3.9로 보완한다. 새재질/Light/Collider/Update/renderer/Android host/workflow는 추가하지 않는다.

**거리별 예산 확보:** 먼18동360창의 frame을glass뒤의 얇은backing quad로 바꿔 street-facing outline은 남기고, 먼 상가frame의wall-facing face만 생략했다. 앞쪽/끝면 shadow/실제room emission·상가 표면은 유지한다. 기본/최대 인원 예산을 올리지 않고 실제geometry/winding/oracle로 검사한다.

**범위·예산:** ArchitectureScene/CityEnvironment/FrontageScene/FrontageChecks/SceneryChecks/CityPreview/StarterScene/StarterConfig의8소스. 시작296파일·실제index/status·122입력·host12SHA 보존. 실제 최대 강우/승객 119910triangles/1623renderers/47materials로120000/2400/48 제한 미만이다. 제한을 높이지 않았다. 기본 장면 108564triangles로110000 예약 예산도 지킨다.

**실제 검증:** source `b4ca1c77ac5e17e7cc6fe5c6c3941a6560d21ae8` / [Validate 38133036369](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38133036369). 실제 생성된6높이profile/6roof profile/6upper terrace/528창(주도로480+끝면48)/18화분/12실외기·near-far frame topology·끝면normal/벽바깥depth·보행/옥상 clearance·면 winding/공유재질 검사 PASS. 기존상가/18기후·180전환,24대/48와이퍼·pause/6신호/4기둥, 보행/승하차/차선 안전·노면GPU/CCT·차종 검증도 PASS. 이번 실패 기록: [].

**실제 화면:** Unity6000.3.26f1/D3D11 150PNG를 받아 artifact sourceSHA/bytes/SHA/ZIPCRC를 확인했다. 기존145장에 좌우 끝면 day/night4장과상층테라스1장을 보완했다. 제품 구도와좌우끝면·옆면window depth·상층테라스/옥상 silhouette·밤창문을 직접 관찰했다. 확대는 같은 장면의 임시 Editor 카메라에서 촬영한 것이며 저장된 폰 카메라는 바꾸지 않는다. AI 생성 시안이나 폰/FPS 캡처가 아니다.

**Android:** `unity-apk-0.27.0-build1` / [BuildApk 38133617130](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38133617130). BuildPlayer 오류0/경고0, launcherLint 오류0/경고8. 실제 APK를 SDK apksigner/aapt/ZIPCRC로0.27.0/code28/min29/target36/ARM64IL2CPP, wallpaper service/private settings provider/settings launcher/Unity Activity 비활성, 원본v2인증서 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6 확인. 변경 없는 host12SHA는 hostLint38008378207(오류0/경고10) 결과를 재사용했다.

**전달:** [APK](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/64687628612cc6f90dabf150655e99030aa1a263/pixel-traffic-unity-prototype-0.27.0.apk) · [제품 전체](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/64687628612cc6f90dabf150655e99030aa1a263/building-city-preview.png) · [왼쪽 끝면](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/64687628612cc6f90dabf150655e99030aa1a263/building-corner-left-preview.png) · [밤 끝면](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/64687628612cc6f90dabf150655e99030aa1a263/building-corner-night-preview.png) · [상층 테라스](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/64687628612cc6f90dabf150655e99030aa1a263/building-terrace-preview.png) · [옥상/테라스](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/64687628612cc6f90dabf150655e99030aa1a263/building-terrace-preview.png). 31173119bytes/SHA256 `41e569dfb9ca624d5ca86cd278bc9c96b6368c1e0d815b6fc9540746afe4fca1`. `downloads/unity-0.27.0`의8파일을 로그인 없는 HTTP200/no redirect/fullbytes/SHA로 검증했다. main에는 APK binary나 새 workflow를 추가하지 않았다. 최종 문서는 검증한122빌드입력과 사용자 작업을 보존한다.

USB 디버깅을 허용한 폰1대를 연결한 뒤 Windows PowerShell에 아래 전체를 붙여넣는다. 다운로드·검증·기존 앱 업데이트까지 수행한다.
```powershell
& {
    $ErrorActionPreference = "Stop"
    $apkFolder = "C:\Users\김백현\Desktop\AI"
    $apkPath = Join-Path $apkFolder "pixel-traffic-unity-prototype-0.27.0.apk"
    $apkUrl = "https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/64687628612cc6f90dabf150655e99030aa1a263/pixel-traffic-unity-prototype-0.27.0.apk"
    $trafficAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
    if (-not (Test-Path -LiteralPath $trafficAdb)) { throw "Unity의 adb.exe를 찾을 수 없습니다." }
    & $trafficAdb start-server
    if ($LASTEXITCODE -ne 0) { throw "ADB 시작 실패" }
    $deviceLines = & $trafficAdb devices
    if ($LASTEXITCODE -ne 0) { throw "폰 연결 확인 실패" }
    $connectedDevices = @($deviceLines | Where-Object { $_ -match '^\S+\s+device$' })
    if ($connectedDevices.Count -ne 1) { throw "폰 1대를 USB로 연결하고 USB 디버깅을 허용해주세요." }
    New-Item -ItemType Directory -Force -Path $apkFolder | Out-Null
    Write-Host "0.27.0 APK 다운로드 중..."
    Invoke-WebRequest -UseBasicParsing -Uri $apkUrl -OutFile $apkPath
    if ((Get-FileHash -LiteralPath $apkPath -Algorithm SHA256).Hash -ne "41e569dfb9ca624d5ca86cd278bc9c96b6368c1e0d815b6fc9540746afe4fca1") { throw "파일 검증 실패. 다시 실행해주세요." }
    Write-Host "폰에 앱 설치 중..."
    & $trafficAdb install -r $apkPath
    if ($LASTEXITCODE -ne 0) { throw "앱 설치 실패. 위 오류를 확인해주세요." }
    Write-Host "완료! 0.27.0 설치 성공." -ForegroundColor Green
}
```

실제 폰0.27.0 외형/FPS·발열·장시간 lifecycle은 사용자 확인 대기. 폰0.26.0 적용 성공은 사용자 확인이다. 시안과의 차이는 여전히 있으며 이번 패치는 앞쪽6동의height/rooftop/upper balcony/cornerwindow 다양화 단계다. 다음 건물 외관 확대나 식생/배경 개선은 실제 폰 확인과 다음 요청에 따라 진행한다. PC Drive설정 보류 유지.

## 2026-10-11 — Unity 0.26.0 가까운 건물 외관: 실제 검증·서명 APK 전달 완료

사용자는 폰0.25.0 적용 성공을 확인하고 앞쪽 대표 건물부터 시안처럼 정교하게 만드는 작업을 승인했다. **0.26.0/code27**, `Architecture-0.26.0.unity` / `Generated/Architecture0260`으로 생성했다. 앞으로 APK는 사용자 Windows 폴더 `C:\Users\김백현\Desktop\AI`로 다운로드·SHA256 검증·USB ADB 설치를 한 번에 실행하는 PowerShell 블록으로 전달한다. 실제 명령 실행은 사용자 PC에서 하며 클라우드에서 설치했다고 주장하지 않는다.

**건물:** 가까운 좌우6동(z-22/-5/12)의120창에 깊은 창틀/두 칸 창살/가로 프레임/돌출 창턱을 넣었다. 유리는 solid shell 밖에 있어 벽에 가려지지 않는다. 먼18동360창은 잘 보이지 않는 얕은 reveal면을 생략해 성능 예산을 확보한다. 전체 외벽3색의 차이를 보완하고 기존 창문 atlas에 하늘색 반사/커튼색을 더했다. 모든480창의 occupied/dark/warm/cool variation과 기존 낮→노을→밤 emission 전환을 유지한다.

앞6동의 기존 발코니12곳에 화분·식물과 실외기12대를 넣고1층 상가 fascia·문턱을 기존 merged geometry에 연결했다. 전체48발코니·192난간기둥·옥상 정원12곳·물탱크/설비와 기존1층 상가3종은 유지한다. 추가 식물은 기존Leaves/60tri RoofShrub를 공유하며 건물별 한 renderer에 합친다. 새재질/Light/Collider/독립Update/Android host/workflow는 추가하지 않았다.

**범위·예산:** ArchitectureScene/CityEnvironment/FrontageScene/FrontageChecks/SceneryChecks/CityPreview/StarterScene/StarterConfig의8소스. 시작296파일·실제index/status·122입력·host12SHA 보존. 실제 최대 강우/승객 119730triangles/1623renderers/47materials로120000/2400/48 제한 미만이다. 제한을 높이지 않았다. 기본 장면 108384triangles로110000 예약 예산도 지킨다.

**실제 검증:** source `3d06d6456ea8ffcbd627f5287ec66bacaab36a5a` / [Validate 38130958043](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38130958043). 실제 생성된6상세건물/120깊은창/12화분/12실외기/near-far frame topology·glass-wall depth·보행/옥상 clearance·면 winding/공유재질 검사 PASS. 기존480창/상가/18기후·180전환,24대/48와이퍼·pause/6신호/4기둥, 보행/승하차/차선 안전·노면GPU/CCT·차종 검증도 PASS. 이번 실패 기록: [].

**실제 화면:** Unity6000.3.26f1/D3D11 145PNG를 받아 artifact sourceSHA/bytes/SHA/ZIPCRC를 확인했다. 기존140장에 오른쪽 건물 day/night와 가까운 창틀 day/sunset/night5장을 추가했다. 제품 구도와 좌우 가까운 외관/창턱·발코니·실외기·밤 창문·옥상 확대를 직접 관찰했다. 확대는 같은 장면의 임시 Editor 카메라에서 촬영한 것이며 저장된 폰 카메라는 바꾸지 않는다. AI 생성 시안이나 폰/FPS 캡처가 아니다.

**Android:** `unity-apk-0.26.0-build1` / [BuildApk 38131503616](https://github.com/BACKHYUN96/S20-PLUS/actions/runs/38131503616). BuildPlayer 오류0/경고0, launcherLint 오류0/경고8. 실제 APK를 SDK apksigner/aapt/ZIPCRC로0.26.0/code27/min29/target36/ARM64IL2CPP, wallpaper service/private settings provider/settings launcher/Unity Activity 비활성, 원본v2인증서 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6 확인. 변경 없는 host12SHA는 hostLint38008378207(오류0/경고10) 결과를 재사용했다.

**전달:** [APK](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/397a53566db1cc396e5822a79748d0b63c974ec1/pixel-traffic-unity-prototype-0.26.0.apk) · [제품 전체](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/397a53566db1cc396e5822a79748d0b63c974ec1/architecture-city-preview.png) · [낮 건물](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/397a53566db1cc396e5822a79748d0b63c974ec1/architecture-day-preview.png) · [밤 건물](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/397a53566db1cc396e5822a79748d0b63c974ec1/architecture-night-preview.png) · [창틀 확대](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/397a53566db1cc396e5822a79748d0b63c974ec1/architecture-window-preview.png) · [옥상](https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/397a53566db1cc396e5822a79748d0b63c974ec1/architecture-roof-preview.png). 31163831bytes/SHA256 `a0f3076f44c0195761a8255cbc8265ec04fbc46f53f4b837ad6fc2ab7deeecbd`. `downloads/unity-0.26.0`의8파일을 로그인 없는 HTTP200/no redirect/fullbytes/SHA로 검증했다. main에는 APK binary나 새 workflow를 추가하지 않았다. 최종 문서는 검증한122빌드입력과 사용자 작업을 보존한다.

USB 디버깅을 허용한 폰1대를 연결한 뒤 Windows PowerShell에 아래 전체를 붙여넣는다. 다운로드·검증·기존 앱 업데이트까지 수행한다.
```powershell
& {
    $ErrorActionPreference = "Stop"
    $apkFolder = "C:\Users\김백현\Desktop\AI"
    $apkPath = Join-Path $apkFolder "pixel-traffic-unity-prototype-0.26.0.apk"
    $apkUrl = "https://raw.githubusercontent.com/BACKHYUN96/S20-PLUS/397a53566db1cc396e5822a79748d0b63c974ec1/pixel-traffic-unity-prototype-0.26.0.apk"
    $trafficAdb = "C:\Program Files\Unity\Hub\Editor\6000.3.26f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK\platform-tools\adb.exe"
    if (-not (Test-Path -LiteralPath $trafficAdb)) { throw "Unity의 adb.exe를 찾을 수 없습니다." }
    & $trafficAdb start-server
    if ($LASTEXITCODE -ne 0) { throw "ADB 시작 실패" }
    $deviceLines = & $trafficAdb devices
    if ($LASTEXITCODE -ne 0) { throw "폰 연결 확인 실패" }
    $connectedDevices = @($deviceLines | Where-Object { $_ -match '^\S+\s+device$' })
    if ($connectedDevices.Count -ne 1) { throw "폰 1대를 USB로 연결하고 USB 디버깅을 허용해주세요." }
    New-Item -ItemType Directory -Force -Path $apkFolder | Out-Null
    Write-Host "0.26.0 APK 다운로드 중..."
    Invoke-WebRequest -UseBasicParsing -Uri $apkUrl -OutFile $apkPath
    if ((Get-FileHash -LiteralPath $apkPath -Algorithm SHA256).Hash -ne "a0f3076f44c0195761a8255cbc8265ec04fbc46f53f4b837ad6fc2ab7deeecbd") { throw "파일 검증 실패. 다시 실행해주세요." }
    Write-Host "폰에 앱 설치 중..."
    & $trafficAdb install -r $apkPath
    if ($LASTEXITCODE -ne 0) { throw "앱 설치 실패. 위 오류를 확인해주세요." }
    Write-Host "완료! 0.26.0 설치 성공." -ForegroundColor Green
}
```

실제 폰0.26.0 외형/FPS·발열·장시간 lifecycle은 사용자 확인 대기. 폰0.25.0 적용 성공은 사용자 확인이다. 시안과의 차이는 여전히 있으며 이번 패치는 앞쪽 대표 건물의 창틀/생활감 디테일 단계다. 다음 건물 외관 확대나 식생/배경 개선은 실제 폰 확인과 다음 요청에 따라 진행한다. PC Drive설정 보류 유지.

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

## Unity 0.14.0 — 3D 버스·박스 트럭 (2026-10-10)

- 범위18source/meta/102입력: VehicleGeometry/HeavyVehicleGeometry·TrafficFleet/DrivingScene·PrototypeDrive/StreetModel/LaneChanges/StreetSimulation·Driving/Street/WeatherLife/VehicleDetail/HeavyVehicleChecks·CityPreview/StarterScene/StarterConfig. HeavyTraffic0140/code15. NativeAndroid/원경·건축·가로수·시안camera 유지.
- 6종24대/4bus4truck16sedans, 공유8mesh와17renderer/vehicle, actual metres·axles/contact·대칭body/frontrearlamps/cargo seam·2lock bars6hinges/AC/windows/topology·sharedmaterial 검사. 고정8° longitudinal envelope, sedan3초/heavy6초 merge/3pulse1.8초/뒤차 모두 대기·projected gap/slow rotated-corner2896사례 및600초 mixed fleet/100우산/18기후·15/30/60/120Hz·810초weather 검사.
- 첫 Validate38045876827의 frame-rate 실패 후 reset wheel pose를 수정했다. 최종 source4f6a1122650ff0b240e7d0e2d7df4adc917d5178/Validate38046520813/GPU59PNG(heavy8확대)·BuildApk38046875651 동일입력 확인. 기본105266/1383/44,100우산116642/1551/44; 기존120000/2400/48 strict 유지.
- 실제BuildPlayer0errors/0warnings, launcherLint0errors/8warnings, 다운로드원본v2cert/code15/manifest/SHA. unchanged12host입력으로hostLint380083782070errors/10warnings재사용. 일회성publicAPK/PNG HTTP200/no redirect/fullSHA; 동일소스docs-only에는검사반복안함. 폰0.14 확인대기/Drive보류.

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
