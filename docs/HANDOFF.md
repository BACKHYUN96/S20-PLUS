# 새 채팅 인수인계 — 픽셀 트래픽 0.29.0

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
