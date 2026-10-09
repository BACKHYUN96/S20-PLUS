# Unity 자동 빌드 — PC에서 시작하고 클라우드로 이동하기

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
