# Pixel Traffic — Unity 전환 준비

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

## PC runner 연결 확인 / GitHub 소스 게시·첫 자동 검사 진행 (2026-10-09)

사용자 화면에서 runner2.337.0 Connected to GitHub / Listening for Jobs를 확인했습니다. PC 로컬 Validate completed에 이어 원격 source/workflow 게시를 진행합니다. main은 effa3c1998fd0a758c648ad0d466cf97d57e77a4이며 Unity source/workflow가 없어 새 관련 파일과 선택 문서만 추가합니다. 기존 0.29→0.48 누적 Android 작업/로컬 index/checkout은 보존합니다. main Unity 변경은 자동 Validate로 연결하며 PR 자동 실행은 두지 않습니다.

GitHub API 직접 네트워크는 proxy403, connector의 runner admin endpoint 조회는 지원되지 않았습니다. 우회하지 않고 허용된 Git HTTPS fetch로 최신 main을 확보했습니다. 사용자 연결 화면을 근거로 진행하며 라벨/실제 job 결과는 첫 Actions 작업에서 확인합니다. 파일 게시·PR·main 반영·작업 결과는 실제 수행 뒤 이력에 구분해 기록합니다.

## 수정 실행기 PC 정상 완료 / GitHub runner 등록 단계 (2026-10-09)

사용자 Windows 재실행 화면에서 `Unity Validate running (PID …)` 후 `Unity Validate completed`와 prompt 복귀를 확인했습니다. 앞선 로그의 실제 장면 PASS/return code 0에 이어 수정 실행기의 프로세스 대기/종료 판정도 PC에서 동작했습니다. pipeline-result.json 원본/내용은 아직 받지 않았으므로 파일 내용까지 직접 확인했다고 기록하지 않습니다. 기존 성공 입력은 클라우드에서 반복 검사하지 않습니다.

GitHub app의 읽기 조회로 저장소 BACKHYUN96/S20-PLUS의 main/public/admin 권한을 확인했습니다. main의 `.github/workflows/pixel-traffic-unity.yml` 및 `pixel-traffic-unity/ProjectSettings/ProjectVersion.txt`는 각각 404로 미등록입니다. 따라서 아직 Actions workflow 실행을 안내하지 않고 PC runner 등록부터 진행합니다. 이번에는 branch/commit/PR/push/merge를 수행하지 않았습니다.

등록은 저장소 Settings → Actions → Runners → New self-hosted runner → Windows/X64입니다. GitHub가 표시한 Download/Configure 명령을 본인 PC에서 실행합니다. 추가 라벨은 `pixel-traffic-unity`, work folder는 기본 `_work`, 초기 서비스 등록은 N으로 사용자 세션에서 시작합니다. 마지막에 `.\run.cmd`로 실행하고 `Listening for Jobs`/GitHub Idle 상태를 확인합니다. 실제 등록 완료 화면을 받기 전까지 connected로 기록하지 않습니다. 등록 토큰은 본인 PC에서 직접 사용하고 성공 상태만 공유합니다.

이후 workflow와 Unity source를 GitHub에 반영하고 실제 main/Validate 작업 및 artifact를 확인해야 합니다. 현재 로컬 PC 검사 성공, GitHub source 게시, runner 연결, 원격 작업 성공, APK 생성은 서로 다른 단계입니다. APK/기기/WallpaperService 연결은 아직 미완료입니다.

## 사용자 PC 실제 batch 검사 PASS / return code 0 확인 (2026-10-09)

후속 로그 화면에서 FirstRoad 생성/저장, PrepareBatch→Validate, `PASS: first scene geometry and motion. Reports/scene-validation.json`, `Batchmode quit successfully invoked`, `Exiting batchmode successfully now`, `Application will terminate with return code 0`을 확인했습니다. 따라서 Unity batch 검사 자체는 성공했고 이전 실행기 FAILED 표시가 잘못됐음을 확정했습니다. 로그 원본 파일/JSON은 받지 않았고 화면 관측으로 구분합니다.

수정된 run-unity.ps1 파일 하나만 사용자 automation 프로젝트의 tools에서 교체하고, 새 프로세스 대기/ExitCode 처리와 pipeline-result.json의 PASS 표시를 확인하는 재실행을 안내합니다. 이는 앱 기능의 중복 검사가 아니라 변경된 실행기 종료 처리 확인입니다. Unity 코드/패키지/서명·기존 앱은 이번에 바꾸지 않습니다. 수정 실행기의 실제 Windows 실행, GitHub runner 접속, Activity APK 빌드와 WallpaperService 연결은 아직 미완료입니다.

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

## PC에서 시작하는 이식 가능한 자동 빌드 설정 준비 (2026-10-09)

[자동 빌드 설정 배포 ZIP](/workspace/artifacts/pixel-traffic-unity-pc-automation-0.1.0.zip): 23파일, YAML/문서 링크·ZIP CRC/source 일치 및 이전 도구/README 외 17파일 보존을 확인했습니다. PowerShell/Unity 기능 실행 검증은 아직 PC에서 필요합니다.

사용자가 PC 활용 후 필요에 따라 완전 클라우드로 변경 가능한 구성을 요청했습니다. 같은 Unity Editor 메서드를 호출하는 공통 PowerShell pipeline과 수동/main GitHub Actions workflow를 준비했습니다. 정확한 Editor 경로는 helper/환경변수에서 찾고 신규 체크아웃 Validate도 장면 생성 후 검사합니다. APK 빌드에는 기존 key/cert, v2/패키지/버전/SDK/ARM64 검증 후 artifact 업로드를 추가했습니다. 임시 cloud key 파일은 finally에서 삭제하며 원본 로그/키는 artifact에 넣지 않습니다.

자체 Windows/Linux runner로 이동할 때 runner 라벨·Unity/SDK 경로·사용 가능한 라이선스·서명 환경을 바꾸고 공통 소스와 검사를 재사용합니다. Unity Build Automation 서비스로 옮길 때는 서비스 설정과 생성 장면 hook 어댑터가 별도로 필요합니다. 라벨만 변경하면 Unity/라이선스가 자동 설치되는 것으로 설명하지 않습니다. [PC 등록/실행/전환](UNITY_AUTOMATION.md).

현재는 파일 준비 단계입니다. Git 커밋/푸시·runner 등록·PC batch/Android 빌드·새 APK 생성·클라우드 서비스 가입/설치/키 업로드는 하지 않았습니다. 정적 설정/파일 일치 검사를 기능 실행과 구분하고 캡처는 선택 작업으로 남깁니다.

## GitHub 연동 Unity 빌드 서버 선택 / 정적 화면 선호 (2026-10-09)

사용자는 주행 영상/시차 캡처까지 필요하지 않고 폰에서 보이는 정도의 정적 화면이면 충분하며, 어려우면 직접 설치해 확인해도 된다고 알려주었습니다. 이 요구를 반영해 캡처는 선택 작업으로 두고 별도 서버를 먼저 생성하지 않습니다. 서버의 세로 Game 뷰 이미지는 미리보기이며 실제 스마트폰/라이브 배경화면 캡처는 기기에서 확보해야 합니다.

Unity 프로젝트의 Assets/.meta/Packages/ProjectSettings/소스는 기존 GitHub 저장소의 pixel-traffic-unity 폴더로 버전 관리할 수 있습니다. 현재 클라우드 작업은 누적 로컬 미커밋/미푸시 상태라 GitHub에서 Unity 빌드가 이미 동작한다고 설명하지 않습니다. Library/Temp/빌드 결과/키는 source control에 넣지 않으며 APK 등 결과는 빌드 artifact로 공유합니다.

공식 문서를 확인한 두 방식:

1. 사용자 Windows PC를 GitHub Actions self-hosted runner로 등록합니다. 저장소 Settings → Actions → Runners → New self-hosted runner에서 Windows/아키텍처에 맞는 안내를 본인 PC에서 실행하고 runner를 켜둡니다. 워크플로가 승인된 코드의 checkout → 정확한 Unity/Android/라이선스 환경 → 장면 생성/검사 → 기존 키 서명 APK → artifact 업로드를 수행하도록 준비할 수 있습니다. Unity 창 수동 조작은 줄고 PC는 켜져 있어야 합니다. 현재 runner/workflow/결과 업로드는 미구성이고 실제 batch/서명 Unity 빌드도 미검증입니다.
2. Unity Dashboard의 DevOps → Build Automation에서 GitHub 저장소·브랜치·project subfolder(pixel-traffic-unity)·지원 Unity 버전·Android/서명 설정을 연결합니다. Unity 서비스가 변경을 감지해 클라우드 빌드하고 결과 다운로드 링크를 제공합니다. 정확한 6000.3.26f1 지원, 계정 요금/사용 조건과 생성 장면 hook을 설정 전에 확인해야 합니다. Personal Editor 무료 사용과 클라우드 빌드 무료 여부를 동일시하지 않고 이번 조사에서는 요금/서비스 가입을 확정하지 않습니다. 일반 빌드 서비스가 자동으로 실제 폰 화면을 촬영하거나 이 채팅에 이미지를 전달하는 것으로 설명하지 않습니다.

추천은 이미 Unity를 설치한 Windows PC를 활용하는 self-hosted 방식으로 실제 APK 자동 빌드부터 검증하는 것입니다. 사용자가 아직 방식 선택/등록을 완료한 것은 아닙니다. 스크린샷은 선택 단계로 남기고, 기존 WallpaperService 연결 미완료를 APK 자동화와 구분합니다.

공식 근거: [Unity Build Automation](https://docs.unity.com/en-us/build-automation), [빌드 설정](https://docs.unity.com/en-us/build-automation/basic-build-configuration/overview), [GitHub self-hosted runner](https://docs.github.com/en/actions/concepts/runners/self-hosted-runners), [runner 등록](https://docs.github.com/en/actions/how-tos/manage-runners/self-hosted-runners/add-runners).

## 자동 실행 스크린샷 / 플러그인 필요 여부 (2026-10-09)

사용자는 클라우드 개발 후 가동 화면을 찍어 전달받는 흐름과 GPT 플러그인 필요 여부를 질문했습니다. `plugin-management` 스킬을 읽고 Unity 및 데스크톱 원격 제어/스크린샷 키워드로 플러그인을 검색했습니다. 반환 항목 중 Unity Editor 실행이나 현재 사용자 Windows Unity를 직접 제어하고 이 채팅으로 자동 캡처 전달하는 기능은 확인하지 못했습니다. 설치/권한 변경/서비스 생성은 하지 않았습니다. GitHub 연결은 검색 결과에서 installed로 표시되지만 빌드 runner/Unity/GPU/라이선스/이미지 자동 공유는 별도로 연결되지 않았습니다.

필수 GPT 플러그인은 없습니다. 로컬 Unity에서 실제 Play 실행 후 PNG를 자동 저장하도록 스크립트를 구현할 수 있습니다. 이는 기존 batch 검사/빌드와 별도의 캡처 기능이며 현재 starter에 캡처 모드는 없습니다. 실제 렌더 캡처에는 그래픽 렌더링이 가능한 실행이 필요하므로 현재 batch 모드의 `-nographics`를 그대로 쓰면 안 됩니다. 단일 정적 장면 스크린샷만으로 움직임을 확인한 것으로 기록하지 않으며, 주행 확인에는 시차를 둔 여러 프레임 또는 영상과 실행 로그가 필요합니다.

현재 가능한 전달 흐름은 클라우드 소스/에셋 개발 → 사용자 PC 자동 실행/검사/촬영 스크립트 → 로컬 결과 파일 저장 → 사용자 이미지 첨부 → 내가 결과를 확인하고 수정하는 방식입니다. 사용자 PC의 저장 PNG를 이 채팅으로 직접 읽거나 자동 전송할 연결은 없습니다. 내가 작업 후 사용자 조작 없이 실제 Unity 화면을 캡처해 바로 전달하려면 접근 가능한 별도 Unity/그래픽 실행 환경과 결과 공유 연결이 갖춰져야 합니다. 현재 자동 캡처/자동 공유를 구현/검증한 것으로 주장하지 않습니다.

다음 구현 후보는 실행·검사·시차별 캡처·로그 저장을 묶는 로컬 도구입니다. 이번 질문 응답에서는 설계/기록만 갱신하고 Unity 소스/ZIP/APK 변경이나 새 실행을 하지 않았습니다.

## 첫 장면 렌더·검사 PASS / 클라우드 작업 범위 (2026-10-09)

사용자가 FirstRoad Game 화면을 전달했습니다. 도로/보도/나무·건물/차량이 표시되고 Console에 `PASS: first scene geometry and motion. Reports/scene-validation.json`이 나타납니다. 해당 시점 Console 오류 0/경고 11이며 URP 패키지 셰이더 형 변환 경고가 표시됩니다. Reports JSON 파일 자체는 받지 않았습니다. Play 버튼은 눌리지 않은 상태이고 18:9 Landscape 선택이라 실제 주행/세로 구도/기기 성능은 아직 검증하지 않았습니다.

사용자는 Unity를 직접 열지 않고 클라우드에서 작업할 수 있는지 문의했습니다. 현재 클라우드 PATH의 Unity/unity-editor/unityhub와 알려진 설치 경로에서 실행 가능한 Editor를 확인하지 못했고, 현재 callable tools에도 Unity 실행/사용자 PC 제어 도구가 없습니다. 네 PC에서 Unity를 직접 조작하는 연결도 없습니다. 소스·절차적 3D 형상·에셋·Android 연결 코드 개발과 패키징은 클라우드에서 진행할 수 있지만 실제 Unity 컴파일/렌더/APK 빌드에는 Editor·Android 모듈·해당 환경에서 사용 가능한 라이선스가 필요합니다. 이전 Registry 403 이후 이번에는 대용량 설치/우회/라이선스 이전을 시도하지 않았습니다.

현재 가능한 자동화 후보는 이미 제공한 `tools/run-unity.ps1`의 Prepare/Validate/ExportAndroid/BuildApk 모드입니다. 사용자 PC Editor 창을 닫고 PowerShell로 실행하면 로컬 Unity가 batch로 실행/종료하도록 구현되어 있습니다. GUI 수동 조작은 줄일 수 있으나 클라우드 실행이나 원격 제어는 아닙니다. 실제 Windows batch 실행은 아직 확인하지 않았고 APK 모드는 별도의 기존 서명 환경변수/인증서 검사를 요구합니다. 완전 클라우드 빌드 서버 구성/요금·계정 설정은 현재 미구성입니다.

이번에는 관측/가능 범위 문서만 갱신했습니다. 소스/ZIP/APK를 바꾸거나 통과한 검사를 반복하지 않았습니다. 다음은 사용자 작업을 줄이는 전달/자동화 흐름을 정하고 이후 시안의 차량/환경과 WallpaperService 연결을 단계별로 개발하는 것입니다.

## 사용자 PC 일반 Editor 열림 확인 (2026-10-09)

Camera 수정 명령 안내 후 사용자 화면에서 Unity 6.3 LTS(6000.3.26f1), Android 대상, 일반 Scene/Hierarchy/Project 뷰와 상단 Pixel Traffic 메뉴를 확인했습니다. Safe Mode 배너는 사라졌습니다. 이는 사용자 PC 일반 Editor 진입의 관측이며 클라우드 C# 컴파일 실행이나 장면 검사 PASS의 근거로 기록하지 않습니다.

현재 장면은 Untitled이며 Main Camera/Directional Light만 있습니다. Ctrl+R은 새 화면을 생성하는 명령이 아니므로 도로가 생기지 않은 상태 자체는 오류가 아닙니다. 상단 Pixel Traffic → 1. Prepare First Scene으로 FirstRoad를 생성하고, 2. Validate First Scene의 Console PASS를 확인한 뒤 Game 9:16/Play로 첫 주행을 확인합니다. 화면 하단 Input Manager deprecated 경고는 현재 도로 생성 단계의 중단 오류로 보지 않으며 이 단계에서 입력 패키지를 추가하지 않습니다.

이번 범위는 관측/진행 문서 4개 갱신입니다. Unity 소스/패키지/ZIP/APK를 변경하지 않았고 통과한 ZIP/정적 검사나 기존 Android 검사를 반복하지 않았습니다. 장면 렌더/검증 및 APK/실기기 결과는 아직 대기입니다.

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

## 첫 Editor 열기 — 컴파일 오류 / Safe Mode (2026-10-09)

사용자가 프로젝트 열기 과정에서 `The project you are opening contains compilation errors` / `Enter Safe Mode?` 창을 전달했습니다. 첫 실제 import가 오류 상태임을 기록하며, 기존 클라우드 정적 검사를 Unity 컴파일 성공으로 해석하지 않습니다. 오류 상세(Console/Editor.log)는 아직 받지 못해 C# API/참조/패키지 중 원인을 확정하지 않았습니다.

`Enter Safe Mode`를 선택합니다. 안전 모드에서 Console 창의 첫 빨간 오류를 클릭하고 하단 상세(오류 코드·파일명·행 번호·메시지)를 확보합니다. Console이 보이지 않으면 `Window > General > Console`로 엽니다. `Ignore`로 진행하거나 프로젝트/Library/패키지를 임의 삭제하지 않습니다. 실제 오류에 맞춰 영향 파일만 수정한 뒤 재컴파일·정상 import·장면 검사/Play를 재개합니다.

이번 확인은 전달한 네 C# 소스와 최신 문서/Git 상태 읽기에 한정했습니다. 원인을 확정할 새 근거가 없어 코드·manifest·전달 ZIP은 그대로이며, 기존 Android 빌드나 통과한 정적 검사를 반복하지 않았습니다. 클라우드에는 Unity Editor가 없어 실제 재현/수정 검증은 수행하지 않았습니다.

## Windows ZIP 경로 오류 해결 (2026-10-09)

사용자PowerShell화면에서Expand-Archive가 `C:\Users\김백현\Desktop\AI\pixel-traffic-unity-starter-0.1.0.zip`을찾지못해ArchiveCmdletPathNotFound오류가났습니다. 실제다운로드위치/파일명은미확인입니다. 이오류는프로젝트압축해제전단계로Unity컴파일문제의근거가아닙니다. 새Unity검증이나정상import로기록하지않습니다.

전달ZIP은현재클라우드에존재합니다. 사용자는ZIP을다운로드한뒤파일선택창에서실제파일을선택하면Downloads/다른폴더·자동번호붙은파일명도처리할수있습니다. 코드실행은사용자WindowsPowerShell에서하며클라우드Windows실행검증은하지않았습니다. `-Force`를쓰지않아기존같은파일을덮어쓰지않습니다.

```powershell
Add-Type -AssemblyName System.Windows.Forms
$starterZipPicker = New-Object System.Windows.Forms.OpenFileDialog
$starterZipPicker.Title = "다운로드한 Unity 프로젝트 ZIP 선택"
$starterZipPicker.Filter = "ZIP 파일 (*.zip)|*.zip"
if ($starterZipPicker.ShowDialog() -eq "OK") {
    Expand-Archive -LiteralPath $starterZipPicker.FileName -DestinationPath "D:\Unity\Projects"
}
```

선택취소시해제하지않습니다. 선택할파일이없으면먼저전달ZIP을다운로드합니다. 완료후 `D:\Unity\Projects\pixel-traffic-unity`를Hub에서추가합니다. 기존프로젝트가있는경우다른새폴더에풀며병합/덮어쓰기를하지않습니다.

## 첫 소스 프로젝트 0.1.0 전달 (2026-10-09)

사용자가6.3설치완료를보고하고Hub Personal활성화화면을첨부했습니다. 화면의활성화날짜는2026-09-10입니다. 설치/활성화준비를기록하고Editor6000.3.26f1/URP17.3.0을기준으로별도프로젝트 `pixel-traffic-unity`를작성했습니다. 이전미설치/활성화미확인항목은당시의준비기록입니다.

[사용자 PC에서 열기·검증·Windows 도구](../pixel-traffic-unity/README.md), [진행 상태](STATUS.md).

- [다운로드ZIP](/workspace/artifacts/pixel-traffic-unity-starter-0.1.0.zip)
- [검증 메타데이터](/workspace/artifacts/pixel-traffic-unity-starter-0.1.0-verification.json)

이번구현은실제3Dprimitive/재질을쓰는도로·보도·나무·건물·차량1대·바퀴와직선loop입니다. 목표시안은ReferencePNG로포함하고runtime자산으로사용하지않습니다. 첫화면은엔진/형상확인용이며브랜드차량·도시전체시안의완성품이아닙니다. 장면/URP/Pointfilter asphalt texture를Editor메뉴로만생성하고기존장면을덮어쓰지않습니다. 0시간/loopovershoot/여러wrap·15/30/60/120Hz 위상/invalidtime·실제차체axis/lane전체외곽/바퀴/접지·material검사를실제Editor에서실행할수있게넣었습니다.

Android export/빌드입력은실험앱ID·min29·ARM64·IL2CPP·Activity entry입니다. WallpaperService가아직없어서일반앱의render와Gradleexport부터확인합니다. 실제export의UnityPlayerAPI/수명주기를확인후Surface연결을추가하는순서로세분화했습니다. APK는환경변수의기존키를keytoolDER인증서SHA로확인후빌드하고서명설정을복원합니다. 키/패스워드/라이선스/새APK는소스ZIP에없습니다. cert검사/Android빌드는실제PC에서미실행입니다.

이클라우드에는Editor가없습니다. 공식URP17.3문서의버전/공개CreateAPI·Activity설정을확인하고정적리뷰/패키징검사를수행했습니다. Registry접속403때프록시를우회하지않고추가설치를중단했습니다. 실제C#컴파일/import/Render·Unity검사/Android빌드·기기FPS/발열은확인대기이며성공을주장하지않습니다. 기존0.48소스/자산/키는보존했습니다. 아래는이전준비/설계기록입니다.

## 현재 단계 (2026-10-09)

사용자는 생성된3D시안을 목표로 Unity 전환을 승인했고, 구현 전에 사용자가 준비하면 좋은 사항을 먼저 안내해 달라고 요청했습니다. 이 문서는 준비·검증 순서이며 구현 완료를 뜻하지 않습니다. 정식 앱은0.48.0입니다.

목표 이미지: `/workspace/artifacts/pixel-traffic-unity-3d-concept-01.png`(941×1672RGB, SHA256 `be226486c4e611ecef59db79193748d59df83b923abb6f231005f7f8dc16486a`). 생성형 콘셉트이며 실제Unity렌더/성능 결과가 아닙니다. 높은카메라·4차선·서울강변/산·픽셀풍을 유지하고차량·건물·나무/보행자·그림자를3D오브젝트로통일합니다.

## Unity 6.6 설치 상태·무료 라이선스 확인 (2026-10-09)

사용자 Hub 화면에서 Unity6.6(6000.6.5f1)의설치완료와 Unity6.3LTS(6000.3.26f1)의진행중상태를확인했습니다. Editor실행/Android모듈/라이선스활성화까지검증한것은아닙니다.

공식[지원안내](https://unity.com/releases/unity-6/support)와[6.6발표](https://discussions.unity.com/t/unity-6-6-is-now-available/1735357)를확인했습니다.6.6은새기능/플랫폼지원·성능개선이포함된Supported Update이며정식제작용이고LTS와같은QA/지원수준을받습니다.지원기간은다음릴리스까지입니다.6.3LTS는2027년12월까지지원됩니다.6.6을불안정한시험버전으로분류하지않습니다.이번프로젝트는라이브배경화면네이티브연결의재현성과기준버전유지를위해6000.3.26f1을계속사용하는설계판단입니다.더높은버전번호가목표그림의퀄리티를보장하지않으며기기성능차이는아직측정하지않았습니다.

[UnityPersonal공식안내](https://unity.com/products/unity-personal):개인/소규모조직은최근12개월관련매출과조달자금의자격기준(미화20만달러미만등)을충족하면무료사용/상업개발이가능합니다.개인은Unity관련프로젝트매출,사업체는전체매출/자금등신청자유형의규정을따릅니다.사용자개인의재무자격충족여부를조사/확정하지않고조건부로안내합니다.무료계획은Editor버전6.3/6.6선택과별개이며유료에셋/추가서비스는별도입니다.본인계정의Hub설정→라이선스에서Personal상태를확인하도록안내합니다.기존앱과Unity6.3설치를유지하고6.6을삭제할필요는없습니다.

## Editor 버전·모듈 선택 확인 (2026-10-09)

사용자설치화면에서Unity6.3LTS **6000.3.26f1**과Android Build Support·OpenJDK·Android SDK & NDK Tools의체크상태를확인했습니다. 프로젝트Editor기준은6000.3.26f1로정합니다. 화면은설치모듈선택단계이며다운로드/설치완료·Editor실행/라이선스·Android빌드검증완료를뜻하지않습니다. 디스크사용가능549.44GB/표시된필수19.03GB로현재선택의공간은충분합니다. VisualStudioCommunity2026도체크됐으며C#편집용선택사항입니다.

이선택으로설치를진행한뒤Hub목록에해당버전이설치됨으로나오는지확인합니다. 이후Unity프로젝트는같은버전의URP기준으로준비하고라이브배경화면연결검증을이어갑니다. 기존상단/하단의버전미확정기록은이항목이전준비단계의사실입니다.

## Unity Hub 설치 확인 (2026-10-09)

사용자가Windows의 `D:\Unity\Unity Hub`에설치했다고보고했고설정→정보화면에서UnityHub3.22.2가정상실행되는것을확인했습니다. 경로는사용자보고이며클라우드에서Windows파일을직접검사한결과는아닙니다. screenshot의Hub버전은Editor버전과다릅니다. Editor설치완료·정확한6000.3.xf1번호·Android Build Support/SDK&NDK/OpenJDK·라이선스상태는아직확인하지않았습니다.

다음확인: 설정창을닫고Hub메인화면의설치(Installs)목록에서Unity6.3LTS Editor항목/버전을확인합니다. Editor가없으면Editor설치(Install Editor)를진행하고Android모듈을함께선택합니다. 설치된Editor의모듈은해당항목메뉴에서확인/추가합니다. Hub설정창왼쪽의설치는설치경로설정이므로메인Editor목록과구분합니다.

## 사용자 PC 확인 (2026-10-09)

사용자가CPU Intel Core i9-14900K/RAM64GB/GPU RTX4070SUPER,Unity미설치를알려주었습니다. 이번모바일URP개발/Editor작업에충분한구성으로판단하며기기FPS결과를뜻하지않습니다. UnityHub→6.3LTS Editor와Android모듈설치를안내합니다. Editor정확한patch번호·라이선스활성화/모듈완료·디스크여유는아직확인하지않았습니다. 본인PC에서계정인증을진행하고완료후6000.3.xf1 버전을알려주면프로젝트버전을맞춥니다.

## 사용자 준비

1. 사용할Windows PC의CPU/RAM/GPU와SSD여유공간, Unity설치여부를 알려줍니다. Editor설치가이미있다면 정확한버전(6000.x.yf1)도 함께 확인합니다. 새하드웨어구매는 사양확인후 판단합니다.
2. [Unity Hub](https://unity.com/download)를설치하고본인Unity계정으로로그인/사용조건에맞는라이선스를활성화합니다. 새설치는Unity6.3 LTS를기본후보로하며Hub에서제공되는패치버전을확인후프로젝트/빌드버전을고정합니다. 기존설치를확인하기전에대용량Editor를중복설치하지않습니다.
3. Editor설치시Android Build Support·Android SDK & NDK Tools·OpenJDK를함께설치합니다. 기존Java앱의클라우드JDK21/SDK경로를Unity에그대로강제하지않고Editor가지원하는의존성을사용합니다. [공식Android준비](https://docs.unity3d.com/6000.3/Documentation/Manual/android-sdksetup.html).
4. S20+/S26 Ultra의USB디버깅과PC연결을준비합니다. 현재앱의프리셋파일내보내기를통해설정을백업할수있습니다. 기존0.48APK/서명백업을보관하며키를공개저장소에넣지않습니다.

초기검증은기본3D도형/간단모델로진행합니다. 브랜드차량등의완성형3D모델은현재관련프로젝트에확인되지않았고별도제작/확보가필요합니다. 유료에셋/벽지플러그인은기술검증과사용자선택전구매하지않습니다. 시안에가까운차량디자인은참고PNG만으로자동완성되지않습니다.

## 환경에서 확인한 사실

- `/workspace/S20-PLUS/pixel-traffic`는Android Java/Canvas앱입니다. `TrafficWallpaperService`가WallpaperService.Engine/SurfaceHolder/화면가시성·SCREEN_ON/OFF/절전모드와미리보기를관리합니다. Unity장면을만드는것과Android라이브배경화면으로연결하는것은별도작업입니다.
- `wallpaper_settings`와`saved_presets` SharedPreferences가현재설정/프리셋저장소입니다. 추후같은앱ID·키로업데이트할때이저장소와필드의읽기/이전이필요합니다.
- PATH의Unity/unity-editor/unityhub와`/opt/Unity*`,`/opt/unity*`,`/workspace/toolchains/*unity*`에서Editor를확인하지못했습니다. 숨겨진모든위치를전수검색한결과는아닙니다. 현재Editor컴파일/렌더/Android export를실행할수있는환경은확보되지않았습니다. 소스/설계준비와실제Unity검증을구분합니다.

## 권장 첫 검증: 라이브 배경화면 연결

최초목표는최종도시전체가아니라작은3D장면을배경화면으로표시하는기술검증입니다. URP로도로한구간·차량하나·카메라/조명을준비한뒤Android호스트와연결합니다.

[Unity as a Library의공식제한](https://docs.unity3d.com/6000.3/Documentation/Manual/UnityasaLibrary.html)은호스트의수명주기에따른미지원시나리오,Android의전체화면렌더링/단일runtime를명시합니다. 문서는WallpaperService에대한완성된연결을보장하지않습니다. 현재앱의설정미리보기·배경화면미리보기·적용된배경화면의Surface전환/공존과renderthread연결을먼저검증해야합니다. 필요한Android네이티브연결/플러그인방식은검증후결정하며동작을미리보장하지않습니다.

검증항목: 홈화면표시/실제배경화면미리보기,화면꺼짐/잠금·해제·다른앱으로숨김과복귀,Surface재생성/회전,설정화면과배경화면충돌,반복복귀의검은화면/메모리누적,숨김시렌더중단,30FPS목표의기기별진단/발열/배터리. 성능은목표이며측정완료가아닙니다. 초기실험빌드는기존0.48을덮지않는별도앱ID로만들고정식전환때기존`com.s20plus.pixeltraffic`·키/저장설정을유지합니다.

## 이후 순서

1. Editor/Android의존성버전고정과작은3D장면·라이브배경화면연결검증.
2. 시안과같은카메라·4차선·차량3D오브젝트/공통조명·가림/LOD·품질단계.
3. 기존Traffic/신호/차간거리·안전외곽을C#로이전하고차량수/길이·양방향정차검증.
4. 보행자4–100/보행영역·장애물·충돌회피/횡단을이전.
5. 낮/밤·비/눈/비바람/신문·나무·안개,설정·프리셋/진단·기존서명/업데이트 검증.

첫Editor버전은사용자설치정보확인후고정합니다. 기존정식앱의소스/자산을대량교체하지않고새Unity프로젝트입력·검사를별도관리합니다. 관련검사만선택하고통과한동일입력은반복하지않습니다. 구현/검사/기기결과는WORK_LOG/STATUS/HANDOFF에단계별기록합니다.

## 공식 근거 확인일: 2026-10-09

- [Unity6릴리스](https://unity.com/releases/unity-6):6.3LTS제공/지원기간확인. 설치할patch번호는미확정입니다.
- [Android환경](https://docs.unity3d.com/6000.3/Documentation/Manual/android-sdksetup.html):BuildSupport/SDK/NDK/JDK필요.
- [UnityasLibrary](https://docs.unity3d.com/6000.3/Documentation/Manual/UnityasaLibrary.html):네이티브통합가능성과runtime/렌더링제한.

본준비조사에서는Unity프로젝트/플러그인설치·라이선스활성화·3D자산구매/앱컴파일·새APK생성을수행하지않았습니다. 실제앱입력68개SHA는0.48최종검증과동일합니다.
