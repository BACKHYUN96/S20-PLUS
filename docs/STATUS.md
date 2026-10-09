# 현재 상태

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

최종 갱신: 2026-10-07

## 사용자 공개키 암호화 전달 준비 완료 — 2026-10-07

- 사용자가 Windows 준비 스크립트 실행 화면과 정확한 공개 XML을 제공했습니다. 실제 2048비트 공개키로 개인 서명 ZIP을 암호화했습니다. 원본 checksum/ZIP 검사는 정상이며 암호문 3,584bytes입니다. 개인키·원본 ZIP 내용은 Git/채팅에 출력하지 않습니다.
- 공통 `pixel-traffic/tools/Restore-BackupTransfer.ps1`과 사용자용 암호문 capsule을 준비했습니다. 동일 PC/계정의 DPAPI 키로 복호화하고 ZIP 길이/SHA256 확인 후 저장하며 다른 파일은 덮어쓰지 않습니다. [절차](BACKUP_TRANSFER.md).
- **사용자 노트북에서 ZIP 복원·검증 결과 확인 대기**입니다. 실제 복호화는 클라우드에 사용자 개인키가 없어 실행할 수 없습니다. 앱 소스/버전/자산은 바꾸지 않았습니다.

## 아티팩트 실패 대체 전달 준비 — 2026-10-07

- 사용자 보고: 새 sandbox 링크도 두 파일 모두 ‘아티팩트를 다운로드할 수 없다’고 표시됩니다. 3KB 서명 ZIP도 실패하므로 용량 단독 원인 가능성은 낮습니다. 파일 자체 검사는 정상이며 클라이언트/서비스의 정확한 원인은 미확인입니다.
- GitHub main ZIP의 HEAD 요청 HTTP 200 확인. 이 경로는 소스/그림/문서/검사를 포함하며 기존 전체 백업의 APK/Git 이력/개인 서명키는 제외합니다.
- [BACKUP_TRANSFER](BACKUP_TRANSFER.md)에 별도 개인 서명 ZIP의 암호화 텍스트 전달 절차를 준비했습니다. 실제 도우미의 임시 RSA fixture 암호화/복호화·hash 확인은 PASS이며 Windows/DPAPI와 사용자 전달은 아직 미확인입니다. **사용자 노트북 공개키 수신 대기**입니다. 개인키·원본 ZIP 내용을 채팅이나 Git에 노출하지 않습니다.

## 백업 다운로드 재전달 — 2026-10-07

- 사용자가 두 백업 다운로드 실패를 보고했습니다. 원본 ZIP과 새 다운로드 복사본의 hash/압축 검사는 정상이며 `sandbox:` 다운로드 링크로 다시 전달합니다. 클라이언트 다운로드 성공은 아직 확인되지 않았습니다.
- 프로젝트 ZIP은 기존 `903cb5e` 인수인계 스냅샷입니다. 아래 GitHub 소스/태그와 서명키 보존 상태는 유지합니다. 앱 기능/버전 변경은 없습니다.

## 새 채팅 인수인계 완료 — 2026-10-07

- 사용자 확인: **0.29.0이 잘 적용됐습니다.** 다음 기능 패치는 아직 선택하지 않았습니다. 앱 소스·버전·PNG를 바꾸지 않고 인수인계와 복원 절차를 보강했습니다.
- 소스·자산·검사·개발 기록을 GitHub main에 보존했고 원격 SHA를 확인했습니다. 소스 체크포인트는 `82c5cbdcdaf17d408565b26addf01411792a3f73`, 태그는 `pixel-traffic-v0.29.0`입니다. 이후 main의 커밋은 인수인계 완료 문서를 갱신합니다. 이전 항목의 ‘커밋/푸시 미실행’은 당시 기록입니다.
- 새 폴더 clone에서 portable 빌드 도우미로 Android assembleDebug/lintDebug **19초 성공, Lint 이슈 없음**. APK SHA256이 기존 전달 0.29.0과 완전히 같았고 기존 v2 인증서·버전·PNG 13개도 일치했습니다. 기존 도구와 캐시를 재사용했으며 새 머신 설치·새 클라우드 태스크·실기기 성능 검사를 수행한 것은 아닙니다.
- 체크포인트 Git bundle의 clone과 `git fsck --full`이 통과했습니다. 최종 프로젝트 백업은 `S20-PLUS-0.29.0-handoff.zip`이며 Git bundle·최신 APK·야간 합성·복원 안내·MANIFEST/SHA256SUMS를 포함합니다. 최종 커밋과 파일 checksum은 ZIP의 manifest에서 확인합니다.
- 기존 업데이트 서명키는 별도 `S20-PLUS-0.29.0-signing-backup.zip`으로 보관합니다. 키 복원·반복 복원·다른 키 덮어쓰기 거부와 키 없는 빌드 중지 검사가 통과했습니다. 새 환경에 키가 없다면 이 백업을 첨부해 복원해야 합니다. 개인키는 Git/프로젝트 백업에 없습니다.
- [HANDOFF](HANDOFF.md)에 새 채팅 문구·현재 기능·결정·미완료를, [BUILD_RESTORE](BUILD_RESTORE.md)에 Git/bundle·도구·서명 복원을 정리했습니다. 범위별 읽기/검사와 작업 기록 규칙은 AGENTS/PATCH_GUIDE를 유지합니다.
- 클라우드 설정 **start_skill 저장 성공**. 시작·서명·검사 절차만 갱신했으며 저장소 목록/설치 스크립트/네트워크/비밀 바인딩은 바꾸지 않았습니다. 초안 활성화는 환경 설정에서 검토·저장 후 Publish가 필요합니다. 게시/새 태스크 복원은 미실행입니다.
- 보존 한계: SDK/JDK/캐시 전체와 과거 채팅 첨부 원본은 백업하지 않았습니다. 원본 시안·기기 화면·레드존 입력/원본 소스·포켓프렌드 원본의 확보 여부는 HANDOFF에 명시했습니다. 보행자/횡단은 미구현 후보이며 정확한 실기기 FPS·전력/발열 측정은 대기 중입니다.

## 새 채팅 인수인계 준비 — 착수 당시 기록 (2026-10-07)

- 사용자가0.29.0을잘적용했다고확인했고,새채팅이동전완전보존을요청했습니다. 소스/문서/PNG의최초커밋·원격보존과별도복원백업을진행합니다. 기존소스버전은0.29.0으로유지합니다.
- [HANDOFF](HANDOFF.md)에새채팅문구·현재기능/미완료·결정을, [BUILD_RESTORE](BUILD_RESTORE.md)에Git/bundle·서명키/도구복원을정리했습니다. 서명키는Git에넣지않고별도백업으로보존합니다.
- 현재준비/검증단계이며커밋·푸시·새체크아웃빌드/최종bundle검증결과는완료시최신항목에추가합니다. 이전버전의‘커밋/푸시미실행’은당시상태기록입니다.

## 최신 전달 — 0.29.0

- 사용자0.28실기기스크린샷의 횡단보도 앞 흰 승용차 크기 차이와 반대 방향 조명 누락을 수정했습니다. 폭을 각 차선 폭에서 계산하던 `VehicleLayout`을 차종/깊이 공통 고정 프로필로 바꿨습니다. 동일 차종·같은 깊이는 모든 차선에서 같은 폭, 같은 방향은 같은 길이입니다. 정차쌍의 실제 폭/길이와 y810정차를 세 팔레트에서 확인했습니다. 차체 전체 차선/간격·원근 크기 연속성은 유지합니다.
- 반대 방향 차는 기존 후미등·붉은 반사에 더해 실제 앞쪽 끝에서 지평선 방향으로 전조등/안개등 부채꼴을 비춥니다. 차종CCT·기존텍스처/도로Path clip을 재사용하고 후미등의 작은 코어/halo를 보강했습니다. 새 설정 없이 전용 도심에 적용합니다. PNG13개·원본비율·주행/신호 알고리즘·설정키·기본4맵은 유지하며 크기 변경에 따른 신호 정차·간격은 재검증했습니다.
- 검사PASS: 단조크기114,696/전체차체28,704, 방향별/보수적·실제3팔레트573,480프로필의 차선 간 폭·같은방향 길이/전체차체경계·headway, 실제672픽셀/99합성. 조명10,020표본·실제12램프/세팔레트/정차쌍·양방향 비춤·색/도로clip·반사/캐시, 기존효과/시간대/노면/날씨·해제.48주행궤적3,110,400표본/302,789빨강정차/2,087루프·최대감속70과 실제신호/브레이크·정차물보라도PASS입니다.
- Android offline 빌드/Lint20초성공·이슈없음. APK code29/version0.29.0/min29/target35·기존v2서명, PNG13개가소스/0.28과byte동일. `/workspace/artifacts/pixel-traffic-0.29.0-debug.apk`37,460,214bytes/SHA256 `a04bfa9d27b9687f6af6feb41a3f944d5f8cc10355ceae73554ccc11688d978a`. 야간20초 신호대기 합성 `pixel-traffic-0.29.0-night-traffic.png`준비·육안확인했습니다. [설치](../pixel-traffic/README.md)/[설계](CINEMATIC_RENDERER.md)/[패치 안내](PATCH_GUIDE.md) 갱신. 파일저장완료/커밋·푸시미실행입니다.
- **실기기 대기**: S20+/S26 Ultra의 흰승용차 정차쌍/전체차종 차선·크기·화면밖 연속성, 반대방향 전방비춤·후미등/제동, 날씨·구도·숨김/복귀. 실제HW Canvas/FPS/전력·발열은미측정입니다. 기본맵·전체설정검사는무변경으로반복하지않았습니다. 후속보행자/횡단은미승인후보입니다.

## 이전 전달 — 0.28.0

- 사용자 요청의 살짝 부채꼴 헤드라이트 노면 비춤을 구현·APK 준비했습니다. 좁은 시작점에서 넓어지는 밝은 면, 부드러운 측면·둥근 끝/거리 fade를 적용했습니다. 전조등 한쪽 투사폭은 차체폭0.88배→1.14배이며 기존 길이·차종 색온도/안개등·시간대/날씨 강도와 도로 Path clip을 사용합니다. 전용 도심에 추가 설정 없이 적용됩니다.
- 변경 범위: 앱 `LightTextures.java`, `CinematicScene.java`, `app/build.gradle`; 검사 `CinematicLightingChecks.java`와 관련 문서입니다. 기존64×192 전조등 텍스처·그리기 호출을 재사용하고 PNG13개/차체·차선·주행/신호·설정키·기본4맵은 유지합니다. [설치](../pixel-traffic/README.md)/[설계](CINEMATIC_RENDERER.md)/[패치 안내](PATCH_GUIDE.md) 갱신.
- 검사PASS: 부채꼴 폭 증가/밝은 측면/둥근 끝·대칭·완전 fade, 기존CCT/강도10,020표본·실제12램프쌍/세 팔레트/도로 경계·色/반사·해제, 동일 스포츠차의 안개등 투명 대조군으로 실제 추가 비춤 확인. 기존효과12,105표본/100착지·실제 신호/정차·브레이크, Scene 차체·672픽셀과99합성도PASS입니다. 기존 검사 class를 재사용해 변경 클래스만 재컴파일했고 무변경 독립 기하/48주행 궤적/기본맵·설정 전체는 반복하지 않았습니다.
- Android offline 빌드/Lint16초성공·이슈없음. APK code28/version0.28.0/min29/target35·기존v2서명, PNG13개가 소스/0.27.0과byte동일. `/workspace/artifacts/pixel-traffic-0.28.0-debug.apk` 37,459,782bytes, SHA256 `57af56a59c9c4b0f97330b7956c1d54347bf77fdc6fa20ae02df4985e711161e`. 야간 합성 `pixel-traffic-0.28.0-night-fan.png` 준비·육안확인했습니다. 파일 저장 완료, 커밋/푸시 미실행.
- **실기기 대기**: S20+/S26 Ultra의 밤/비·차종별 부채꼴과 낮·눈·안개/구도·신호 대기/숨김·복귀. 실제 HW Canvas/FPS·메모리/전력은 미측정입니다. 후속 보행자/횡단은 후보이며 미승인·미구현입니다.

## 이전 전달 — 0.27.0

- 사용자가 라이팅 우선 패치와 차종별 색온도를 승인했고 구현·APK 준비를 완료했습니다. 승용/SUV/택시6500K LED, 스포츠 쿠페6500K전조등+6500K안개등, 버스/트럭4500K전조등+3000K안개등입니다. 실제 램프/범퍼 하단 그룹을 분리한 캐시 색 마스크, 길고 좁은 전조등·짧고 넓은 안개등 노면 비춤, 같은 색의 젖은 반사/잔물결을 적용했습니다. 낮·노을·밤/눈·안개에 강도·번짐을 연결하고 가로등3000K 노면 비춤·실제 밝은 창문/간판 최대8곳 발광을 보강했습니다. 추가 설정 없이 전용 도심에 적용됩니다.
- 차체·차선·주행/신호/기존 설정과 기본4맵, PNG13개는 유지합니다. 색온도는 CIE→화면 sRGB 근사이며 물리적 스펙트럼/폰색온도 측정이나 광선추적이 아닙니다. 공통 조명 기준은 `VehicleLighting`, 작은 텍스처는 `LightTextures`, 실제 램프 마스크는 `VehicleLights`, 창문 위치는 `CityLightAnchors`입니다. [설계](CINEMATIC_RENDERER.md)/[패치 안내](PATCH_GUIDE.md)/[설치](../pixel-traffic/README.md) 갱신.
- 검사PASS: 색온도와 깊이/날씨10,020표본, 독립 전조등/안개등 fixture·실제12종 램프/세 팔레트/alpha·해제, 노면 경계/LED·따뜻한 빔/추가 안개등 기여/후면 빔 없음/젖은 잔물결과 창문 위치. 기존 기하·차체573,480프로필/672픽셀·효과·시간대/날씨·신호/48궤적3,110,400표본·99합성도PASS. 창문 보강 뒤 영향받는 시간대/날씨·신호/최종 합성만 재실행했고 무변경 기본맵/공유모델·설정 전체는 반복하지 않았습니다.
- Android 빌드/Lint24초성공·이슈없음. APK code27/version0.27.0/min29/target35·기존v2서명, PNG13개가 소스와0.26.0에 byte일치. `/workspace/artifacts/pixel-traffic-0.27.0-debug.apk` 37,459,402bytes, SHA256 `a72f3002f3d897d110a00c2e6878f4170678b470163e9ef27a62f39f38e987db`. 노을/야간 합성 `pixel-traffic-0.27.0-{sunset,night}-lighting.png` 준비·육안확인. 파일저장완료, 커밋/푸시 미실행.
- **실기기 대기**: S20+/S26 Ultra의 밤/강한비 트럭4500/3000K와 스포츠6500K 두 램프·반사, 다른 팔레트/낮·눈·안개/신호대기·숨김/복귀. 실제 HW Canvas/FPS/메모리/전력·발열은 미측정입니다. 추가 텍스처 픽셀417,792bytes(408KiB)와 실제 앞차체6종 크기의 발광 마스크·대체 마스크가 필요하며 엔진/미리보기 및 GPU복사 비용은 별도입니다. 후속 후보는 보행자/횡단·같은 조명 기준 연결이며 미승인·미구현입니다.

## 이전 전달 — 0.26.0

- 2026-10-07 순서 상담: 사용자가 보행자 전에 라이팅을 개선할지 질문했습니다. 추천 순서는 **공통 광원·노면 반사 개선 → 보행자/횡단 → 보행자 조명 미세 조정**입니다. 다음 버전 후보를 라이팅 우선으로 제안하며 보행자는 후속으로 유지합니다. 현재 상담/제안만이며 라이팅·보행자 구현 승인이나 새 APK는 없습니다.
- 2026-10-07 후속 사용자 확인: 신호등 시스템이 아주 잘 적용됐다고 보고했습니다. 새 기기별 성능/전력 수치는 없으며 상세 실기기 대기는 유지합니다. 다음 추천은0.27.0 전용 도심 보행자/신호 대기·안전한 횡단과 비 우산·눈 겨울 옷·시간대 조명입니다. 차량 진입 확정/전체 차체의 횡단 구역 비움과 보행 완료까지 정차를 연결해야 하며 현재 제안만, 구현 승인/새 APK는 없습니다.
- 사용자가0.25.0 정상 적용 뒤 추천 교통 신호/감속·정차·순차 출발을 승인했고 구현·APK 전달 준비를 완료했습니다. 전용 도심의 기존 횡단보도 보도 양쪽에 픽셀 신호등, 초록14/노랑3/빨강9초 활성시간 순환과 안전한 노랑진입 통과를 추가합니다. 실제 차체 앞끝 정차/앞차 간격·속도/0.25초 순차 출발이며 기존 차선·크기·PNG13개/기본맵·공유TrafficModel은 유지합니다.
- 실제 감속/정차 상태의 뒤쪽 브레이크등·젖은 붉은 반사를 강화하고 정차 물보라는0으로 줄입니다. 비/눈/풍경 이벤트는 계속 진행합니다. `차량 · 종류와 색상 → 교통 신호 · 전용 도심` 기본켬이며 off이면 신호가 사라지고 대기차가 가속해 자유주행으로 돌아갑니다. UI/엔진/미리보기/진단·프리셋v1에연결, 구버전없는키true·엄격Boolean검사, 빠른성능설정은선택보존. 밀도변경은배열/초록신호 재시작, 날씨/시간대/구도/밝기는위치·신호시계 유지. [설치](../pixel-traffic/README.md)/[설계](CINEMATIC_RENDERER.md)/[패치 안내](PATCH_GUIDE.md) 갱신.
- 최종 검사PASS:48밀도/차종/속도90초3,110,400차량표본, 정차305,285표본/루프2,082회/최대감속70모델단위/s², 물리차체·최소원형간격/가감속·순차출발,15/30/60Hz·dt/토글·노랑가까움/먼접근. 실제팔레트3종신호색/보도위치·정차앞끝/브레이크·반사/헤드라이트불변·정차물보라·설정/그리기연속성, 기존차체573,480프로필/672픽셀/효과·날씨캐시99합성과관련설정정책도PASS. 오류/재개범위는 WORK_LOG에기록했고 통과한무변경기하·자산/기본맵 검사를중복하지않았습니다.
- Android빌드/Lint최종16초성공(초기19초), 이슈없음. APK code26/version0.26.0·기존v2서명/13PNG소스일치. `/workspace/artifacts/pixel-traffic-0.26.0-debug.apk` 37,445,298bytes, SHA256 `cce280103f9e07656b32ae9268adc59d123f66c699788fec127c3accc54d3822`. 최종합성 `pixel-traffic-0.26.0-{red-sunset,green-night}.png` 준비. 파일저장완료, 커밋/푸시 미실행.
- **실기기 대기**: S20+/S26 Ultra에서 양방향 버스·트럭앞끝/24대큐/정차·재출발, 신호off/on, 설정/프리셋JSON·복원·잘못된bool, 날씨/시간대·색상/속도 전환과 숨김/복귀·기존맵. Android SharedPreferences/JSON 전체실행·실제GPU표시FPS/메모리/전력·발열은미측정입니다. 교차로/보행자충돌·실제기상/법규모델은미구현이며 다음작업은사용자적용확인후선택합니다.

## 이전 전달 — 0.25.0

- 2026-10-07 후속 사용자 확인: 0.25.0이 매우 잘 적용됐다고 보고했습니다. 기기별 상세 조합/새 성능 측정값은 없으며 관련 실기기·전력 검증 대기는 유지합니다. 다음 추천은 0.26.0 전용 도심 횡단보도 신호·부드러운 감속/정차·순차 출발과 실제 감속에 따른 브레이크등입니다. 현재 제안이며 구현 승인은 아직 없습니다.
- 사용자가 눈·안개 전용 도심/세시간대·전환/필요한 배경만 캐시하는 개선을 승인했고 구현·APK 전달 준비를 완료했습니다. 눈은 지붕·가로수·보도 적설과 제설된 차선, 앞뒤 눈송이와 야간 따뜻한 조명을 추가합니다. 안개는 먼 산·도시·강을 흐리고 차량/가로등 halo·먼 차체/열차 alpha를 연결합니다. 첫 날씨 즉시 표시·이후4초 활성 전환/재선택과 낮/노을/밤을 지원합니다.
- 새841×1870 눈/안개 아트6장, CinematicArtwork 지연 캐시를 추가했습니다. 선택과 전환에 필요한 그림만 configure/setTheme에서 로드하고 update/draw에서는 디코딩하지 않습니다. 전환 후 불필요한 그림을 해제하며 노을 눈/안개2장, 낮/밤3장입니다. 빠른 시간대/날씨 재선택 검사 피크12장(약72MiB 픽셀), 정착 후2장 회수를 관측했습니다. 상시6장 유지 대신 선택별 캐시이며 실제 GPU/프로세스/여러엔진 메모리는 미측정입니다.
- 기존 차체/차선·차량PNG/주행·맑음/비/설정키·프리셋/기본4맵은 유지합니다. UI와 최근 노면 진단의 아트 대체·캐시 장수를 갱신했습니다. 손상/누락·허용 크기 밖/로드 메모리 부족은 기존 마른/젖은 아트로 대체하고 해제 시 소유 자산을 recycle합니다. [설치](../pixel-traffic/README.md)/[설계](CINEMATIC_RENDERER.md)/[패치 안내](PATCH_GUIDE.md) 갱신.
- 관련 검사PASS:12배경 중앙선96/횡단보도72표본,12색 시간대×날씨 oracle·초기/4초·재선택, 실제240tick/24대 위치/자세·cache퇴거/재사용/실패대체/해제/프레임 디코딩 없음/눈 레이어·소멸. 기존 기하·차체573,480프로필자세/672픽셀·효과12,105표본과99합성, 공유ThemeBlend/눈 ExpansionChecks도 PASS. 최초 중앙선 검사의 두 줄 합침 오류를 픽셀 프로브로 고치고 기존3.5px 오차는 유지했습니다. 안개 이벤트 표현 수정 후 영향받은 합성/빌드만 재검증했고 무관한 기본맵 전체 검사 반복은 하지 않았습니다.
- Android 빌드/Lint 최종 성공20초/이슈없음(초기22초, 최종 열차 alpha 반영 증분 빌드). APK code25/version0.25.0·기존 v2서명·PNG13개 소스 byte 일치. `/workspace/artifacts/pixel-traffic-0.25.0-debug.apk` 37,581,054bytes, SHA256 `b75aed93ec26298e9615568bc465d9824c0909718b59bf71348169aaf573ead8`. 같은폴더 `pixel-traffic-0.25.0-{day,sunset,night}-{snow,fog}.png` 최종 합성6장 준비·육안 확인. 파일 저장 완료, 커밋/푸시 미실행.
- **실기기 대기**: S20+/S26 Ultra의 눈/안개×3시간대, 맑음↔눈↔안개와 시간대 빠른 재선택·차선/크기, 설정/프리셋, 숨김·복귀/자동시간과 캐시·첫 자산로드 지연. 실제 GPU/표시FPS/전력·발열/저메모리 환경 미측정입니다. 다음 작업은 사용자 적용 확인 후 선택하며 실제 기상 동기화/물리적 적설/다른 전용 맵·교통 신호는 미구현입니다.

## 이전 전달 — 0.24.0

- 후속 사용자 확인: 0.24.0이 잘 적용됐다고 보고했습니다. 기기별 조합/새 성능 수치는 없으며 상세 기기·전력 검증 대기는 유지합니다. 다음 추천은 0.25.0 눈·안개 전용 도심 배경과 기존 시간대/날씨 전환 연결입니다. 현재는 제안이며 구현 승인은 아직 없습니다.
- 사용자가 날씨별 노면 패치를 승인했고 구현·전달 파일 준비를 완료했습니다. 전용 도심 낮/노을/밤에 마른 도로3장을 추가하고 맑음0/약한비0.68/강한비1의 젖음을 적용합니다. 첫 날씨 즉시 표시, 이후4초 활성시간 전환·재선택과 시간대 동시 전환을 지원합니다. 비를 끄면 빗줄기는 즉시 멈추고 반사·물보라는 마르는 동안 약해집니다. 눈은 마른 기본 노면+기존 눈, 안개는0.35 습윤+기존 안개이며 둘 다 물보라는 없습니다.
- 차체/차선/주행/차량PNG·설정키/프리셋·기본4맵은 유지합니다. UI와 최근 진단에 노면 상태/젖음%를 추가했습니다. [설치와 사용법](../pixel-traffic/README.md)/[설계](CINEMATIC_RENDERER.md)/[패치 안내](PATCH_GUIDE.md) 갱신. 캐시6장 해제/마른 파일 실패 시 젖은 아트 대체를 검증했습니다. 낮 마른 생성본842×1869는 원본841×1870와 각 축1픽셀 차이를 허용해 같은 구도로 표시하며 독립 표본을 확인했습니다.
- 관련 검사PASS: 중앙선48/횡단보도36표본, 독립6색 동시 혼합/초기선택·4초 건조·재선택/원래 하늘 보존, 실제180tick/24대 차량 위치·자세/캐시·대체·해제. 기존 기하·차체573,480프로필 자세/672픽셀·효과12,105표본 및 세시간대99합성도 통과. 실패를 수정한 뒤 영향받은 단계만 재개했고 무관한 기본맵·전체모델 반복은 하지 않았습니다.
- Android 빌드/Lint 성공22초/이슈없음. APK code24/version0.24.0·기존 서명·7PNG 소스일치. 파일 `/workspace/artifacts/pixel-traffic-0.24.0-debug.apk` (19,780,969bytes), SHA256 `a21a8b4aa7a321862f33da22c728bf0a68e54263373a3e7d481c672c0ff4ee21`. 데스크톱 최종합성 `pixel-traffic-0.24.0-{day-dry,sunset-dry,night-dry,night-wet}.png` 준비. 파일 저장 완료, 커밋/푸시 미실행.
- 추가 마른 배경 픽셀18,876,152bytes(약18MiB), 배경 총 약36MiB는 계산치입니다. 실제 GPU/여러엔진 메모리·표시FPS·전력/발열 미측정. **실기기 대기**: S20+/S26 Ultra 날씨·시간대 동시/연속 변경, 완전 마름과 반사/물보라, 차선·크기, 설정 유지, 숨김/복귀·추가 부하. 이후 후보는 신호/정차·교통 이벤트 또는 다른 전용 맵 확장이며 미승인·미구현입니다.

## 이전 전달 — 0.23.0

- 후속 사용자 확인: 0.23.0이 매우 잘 적용됐다고 보고했습니다. 기기별 세부 조합/새 성능 수치는 없으며 상세 검증·전력 측정 대기는 유지합니다. 다음 추천은0.24.0 전용 도심의 날씨별 마른/젖은 노면과 반사·물보라/전환 연결입니다(제안만, 미승인·미구현).
- 동일크기841×1870 전용 낮/밤 아트 추가, 기존노을과 함께 수동/자동 시간대를 지원합니다. 처음선택은즉시표시/이후4초활성전환·재선택, 시간대별차량/가로등·반사조명. 차체배치/차선·주행/원본노을·차량PNG/설정·프리셋·서명은 유지합니다.
- 기존 엔진·미리보기 자동 시간대 생략을 제거하고UI/진단을 갱신했습니다. 전용도심을 켠채 장면 시간대를 선택하거나 자동전환을 켭니다. 기존전용노을추천은 여전히노을입니다. 노면은 모든시간대에젖은아트입니다.
- 검사PASS: 원본중앙선24/횡단보도18표본, 실제색혼합/초기선택·4초·재선택/180tick·24대 위치와자세보존/캐시·대체·해제/자동시각경계. 기존기하·차체573,480프로필자세/672픽셀·효과와 세시간대99합성도통과. 초기검사 기준점·명암측정오류를 수정하고 해당검사부터 재개했으며 무관한기본4맵·전체모델 검사 반복은 하지않았습니다.
- Android 빌드/Lint 성공19초/이슈없음, APK서명·code23/version0.23.0·4PNG소스일치. APK `/workspace/artifacts/pixel-traffic-0.23.0-debug.apk`와 `pixel-traffic-0.23.0-{day,sunset,night}.png` 전달. [설치](../pixel-traffic/README.md)/[설계](CINEMATIC_RENDERER.md)/[패치 안내](PATCH_GUIDE.md) 갱신. 파일저장 완료, 커밋/푸시 미실행.
- 낮/밤자산2장 캐시추가 약12MiB픽셀데이터(계산치), 실제GPU/프로세스메모리·전환부하 미측정. 누락/손상·크기불일치/선택자산로드실패는 전용노을로 대체·진단표시하고 모드해제때세장recycle합니다.
- **실기기 대기**: S20+/S26 Ultra의 수동3시간대/자동현지시각·재선택/차선·크기·저장값/숨김·복귀와추가부하. 실제표시FPS/전력·발열 미측정. 후속후보 마른노면/다른전용맵 확장은미실행입니다.

## 이전 전달 — 0.22.0

- 후속 사용자 확인: 0.22.0을 좋게 평가하고 다음 패치를 질문했습니다. 기기별 세부 조합/새 성능 수치는 없으며 아래 상세 검증 대기·전력 미측정 상태는 유지합니다. 다음 제안은0.23.0 전용 도심 낮·밤 아트와 기존 시간대/자동 전환 연결입니다(제안만, 미승인·미구현).
- 전용 도심에서 실제 램프 위치의 빛·거리/날씨별 노면 반사, 가로등 통과 차체 명암, 원근 비의 앞뒤 레이어와 바퀴 물보라를 구현했습니다. 기존 전용 도심 설정으로 업데이트하면 반영됩니다.0.21.0 차선/크기, 주행 모델/원본PNG/설정·서명 유지, 새 권한·설정키 없음.
- 마스크·램프는 로드/팔레트 변경 때만 캐시하고 소유자가 해제합니다. 효과 시계는 유효 활성update에만 진행하며 비가 없거나 해제되면 멈춥니다. draw만으로 시간은 흐르지 않습니다. 추가 마스크 메모리/합성 비용은 실제기기 측정 전입니다.
- 전용 최종 검사 PASS: 신규12,105 깊이/날씨 샘플/100착지 깊이, 소스 램프·alpha 마스크/실제12아틀라스·대체팔레트/젖음·마름/실제 비 레이어/물보라·속도·시간·해제. 기존크기114,696샘플/전체차체28,704자세/5프로필573,480자세/실제Scene672픽셀/33합성도 통과. 기본4맵전체와 무관한 모델/설정 검사는 반복하지 않았습니다.
- Android assembleDebug/lintDebug 성공22초, Lint 이슈 없음. APK 서명·code22/version0.22.0·원본PNG일치 확인. APK `/workspace/artifacts/pixel-traffic-0.22.0-debug.apk`, 최종 합성 `/workspace/artifacts/pixel-traffic-0.22.0-cinematic.png`. [설치](../pixel-traffic/README.md), [설계](CINEMATIC_RENDERER.md), [패치 안내](PATCH_GUIDE.md) 갱신. 파일 저장 완료, 커밋/푸시 미실행.
- **실기기 대기**: S20+/S26 Ultra의 맑음/약한비/강한비, 차체 조명·앞뒤램프·반사/물보라, 기존 차선·크기 유지와 숨김/복귀. 실제 표시FPS·배터리·발열 미측정. 다음 후보는 같은 차선 구도의 전용 낮·밤 아트 확장입니다(미실행).

## 이전 전달 — 0.21.0

- 후속 사용자 확인: 0.21.0 적용 후 크기 요동이 잘 고쳐졌다고 보고했습니다. 이번 메시지의 기기별 구분/새 성능 수치는 없으며, 아래 세부 조합 검증 대기와 전력 미측정 상태는 유지합니다. 다음 업데이트 후보는 차량 조명·반사와 비 효과 보강이며 아직 구현 승인/착수하지 않았습니다.
- 이동 중 차체 크기 요동을 수정했습니다. 위치별 반복fit을 깊이별 선형 크기곡선/고정 차선 여유로 교체하고 실제 차체 길이 전체의 할선으로 기울기를 계산합니다. 차체·그림자·반사가 같은 자세를 사용합니다. 기존 전용 도심 설정으로 업데이트하며 PNG/주행 모델/설정/서명은 유지합니다.
- 최종 전용 검사 통과: 크기114,696샘플, 기존 전체차체28,704자세, 합성/보수적/실제Scene3팔레트 총573,480자세, 실제Scene672픽셀 자세/팔레트왕복, 기존33합성·아틀라스/그래픽/오류·해제. 최종 Android assembleDebug/lintDebug 성공(Lint 이슈 없음), code21/version0.21.0·기존서명·APK PNG 일치 확인.
- [패치 작업 안내](PATCH_GUIDE.md), AGENTS, `pixel-traffic/tools/check-patch.sh`에 최신/관련 문서·범위별 검사·도구/자산 재사용·검사 중복 방지 규칙을 추가했습니다. 실제 크레딧 절감량은 미측정입니다.
- 전달 APK `/workspace/artifacts/pixel-traffic-0.21.0-debug.apk`, 최종 합성 `/workspace/artifacts/pixel-traffic-0.21.0-cinematic.png`. [설치 안내](../pixel-traffic/README.md) 및 [렌더러 설계](CINEMATIC_RENDERER.md) 갱신. 파일 저장 완료, 커밋/푸시 미실행.
- **실기기 대기**: S20+/S26 Ultra에서 횡단보도 전후 긴 차종 크기/차선·방향·팔레트 전환 확인. 실제 표시FPS·배터리·발열 미측정. 무변경 기본맵 전체 합성과 모델/설정 전체 검사는 이번에 반복하지 않았습니다.

## 이전 전달 — 0.20.0

- 후속 보고: 사용자가0.20.0 적용을 확인했으나 이동 중 차체 크기 요동/횡단보도 버스 축소를 발견했습니다. 실제Geometry에서 lane0 버스폭groundY800→840이56.53→45.81px로 감소함을 재현했습니다. 전체차체fit의 위치별 축소가 원인이며 크기 단조/변화율 검사를 추가해야 합니다. 현재는 사용자의 작업 전 설명 요청에 따라 분석·계획만 기록했고 앱 코드/새APK는 변경하지 않았습니다.

- 사용자0.19.0 정상 적용 확인과 스크린샷6535.jpg에서 작은 차체·차선 어긋남 지적. 새 버전에서는 실제 다섯 차선 경계의 독립 보간, 불균등 차량 행 분리, 차선 폭 기준 차체 크기, 접지/원근·반사·그림자를 수정했습니다. PNG·기존 설정·서명·TrafficModel 고리는 유지합니다.
- 기존 ‘전용 아트 · 비 오는 노을 도심’ 설정을 켜둔 채 업데이트하면 반영됩니다. 노을 고정/다른 맵·팔레트 제한은 이전과 같습니다.
- Android assembleDebug/lintDebug 성공(Lint 이슈 없음), APK 서명/code20/version0.20.0·원본PNG 일치 검사 통과. 강화된 기하28,704자세, 실제 아틀라스12종/픽셀120자세/합성33조합/자산 오류·해제 검사 PASS. 여러 활성 시간과 휴대폰 비율 합성에서 차폭·차선 유지 확인. 기존0.19 검사가 실측 불일치/행 혼입을 놓쳤음을 재현해 검사 보강했습니다.
- 전달 APK `/workspace/artifacts/pixel-traffic-0.20.0-debug.apk`, 시안 `/workspace/artifacts/pixel-traffic-0.20.0-cinematic.png`. [렌더러 설계](CINEMATIC_RENDERER.md), [설치/사용법](../pixel-traffic/README.md) 갱신. 파일 저장 완료, 커밋/원격 푸시 미실행.
- 실기기0.20.0 차선 유지·크기/전환·성능 확인 대기. 실제 표시FPS·배터리·발열은 미측정이며 장시간 전력 작업은 추후 진행합니다.

## 이전 전달 — 0.19.0

- 사용자가 전용 아트/GPU 전환과 필요한 추가 배터리 사용을 허용하여, 노을 도심부터 고밀도 전용 배경·투명 차량 아틀라스와 GPU Canvas 직접 합성을 구현했습니다. 전용 배경841×1870, 차량1448×1086, 논리 구도540×1200. 선택형24대/날씨/구도/밝기/교량 전철 연결.
- **추천 프리셋 → 전용 아트 · 비 오는 노을 도심**으로 켭니다. 기본 설정은 유지합니다. 이번 전용 장면은 고정 노을/도심이며 기본 차량 색상에서 새 아틀라스를 사용합니다. 다른 시간대/맵·차량 컬러풀/파스텔은 기존 아트입니다. 새 선택값은 프리셋/JSON/앱·시스템 미리보기/홈 배경/진단에 연결했습니다.
- Surface별 HW 연결 방식 유지, 초기 HW 불가 시 SW 대체, HW 사용 중 일시 실패는 프레임 건너뛰기/재시도, 제출 시 Surface 소멸 예외 처리. 전용 자산 읽기 실패는 기존 장면으로 대체합니다.
- 최종 Android assembleDebug/lintDebug 성공(Lint 이슈 없음), APK 서명·code19/version0.19.0과 포함 PNG 원본 일치 검사 통과. 독립 계산11종, 기존 합성60조합, 전용 합성33조합/아틀라스/그래픽 어댑터 검사 통과. 데스크톱 최종 합성에서 차량 차선과 전철 교량 위치를 확인했습니다.
- 전달 APK `/workspace/artifacts/pixel-traffic-0.19.0-debug.apk`, 데스크톱 검토 이미지 `/workspace/artifacts/pixel-traffic-0.19.0-cinematic.png`. [설계·제약](CINEMATIC_RENDERER.md) 및 [설치/사용법](../pixel-traffic/README.md) 참고. 파일은 저장했으며 커밋/원격 푸시는 하지 않았습니다.
- **실기기 대기**: 새 버전의 S20+/S26 Ultra 적용·GPU Canvas 진단·기본 맵 복귀·숨김/복귀. 실제 표시FPS·배터리·발열은 미측정입니다. 장시간 전력 작업은 사용자 요청대로 추후 진행하며, 다음 아트 단계는 다른 시간대/맵과 전경 분리입니다.

## 이전 전달 — 0.18.0

- 사용자 S20+ 및 S26 Ultra 두 기기에서 정상 다운로드/적용·현재까지 문제 없음 보고. 추가 진단 수치/장시간 부하 측정 없음. 다음 제안은 전용 배경/전경/차량 아트 자산 제작을 도심부터 검증한 뒤 확장하는 것입니다(미실행).

- 시안 기반 도시/4맵 아트 재구성, 전용 차량36변형, 가로등/차량 발광 및 노면 반사 보강.
- 이벤트4종과 켬/빈도3단계, 프리셋/미리보기/진단 연결. 선택형24대와 노을 도심 추천 추가. 기존 설정/프리셋 호환.
- APK 빌드·Lint·서명·버전과 모델 검사10종 및 데스크톱 합성60조합/차량36종 검사 통과. 최종4맵 합성 시각 확인. Android 표현/24대·반사·이벤트 추가 부하/기존 프리셋 확인 대기.
- 시안과 동일한 표현 수준은 미확정. 실제 표시FPS·장시간 전력·발열은 미측정이며 후속 기기 확인이 필요합니다.

## 이전 전달 — 0.17.0

- 설정 접이 구역/추천3종/전체 화면 미리보기/프리셋 이름 변경 및 JSON 내보내기·가져오기/현재 설정 초기화 구현.
- 가져오기는128KB/10개/중복 거절 및 전체 검사 후 추가. 현재 설정 초기화는 저장한 프리셋 유지. 별도 권한 추가 없음.
- APK 빌드·Lint·서명·버전 검사 통과. Android UI·문서 제공자·손상파일·복원·취소 검증 대기. 다음은 사용자 기기에서 통합 편의성 확인.

## 이전 전달 — 0.16.0

- 사용자0.15.0 정상 적용 보고.
- 전체 풍경/차량 중심1.5배 및 확대 위치3종, 미리보기/홈 배경/진단/프리셋 연결. 기본 기존 구도 및 구버전 프리셋 호환.
- 빌드·Lint·서명·버전 및 화면 크기별 구도 계산 검사 통과. 실제 Android UI/크롭/저장/부하 확인 대기.

## 이전 전달 — 0.15.0

- 사용자0.14.0 정상 적용 및 제출59.6fps 보고, 현재까지 문제 없다고 보고.
- 맵별 네온/구름/산안개/파도/등대/안내판 움직임 및 풍경 스위치, 프리셋/미리보기/진단 연결. 기존 프리셋 호환.
- 빌드·Lint·서명·버전 및 활성 시간 검사 통과, Android 실제 표현/추가 부하 확인 대기. 장시간 전력 검증은 추후 진행.

## 이전 전달 — 0.14.0

- 차량/도심/해안/산길/고속도로 아트 보강. 등대·포말·숲/바위·주유소·도시 옥상/보도 소품 및 차체 명암/유리 반사 추가.
- 캐시 이미지 생성 경로만 확장하고 설정/프리셋/주행 모델 유지.12종 도로/시간대 정적 출력,36종 스프라이트 그림 존재/투명 테두리 검사 통과.
- APK 빌드·Lint·서명·버전 검사 통과. 실제 Android 표시/시간대 전환/프리셋 및 생성 부하 확인 대기. 장시간 전력 검증은 사용자 요청으로 추후 진행.

## 이전 전달 — 0.13.0

- 사용자 정상 적용 보고. 최신 진단: 해안/밤/강한 비/12대/1.5배/컬러풀, 제출59.6fps·그리기10.60ms·760프레임. 장시간 전력/발열 및 프리셋 개별 경로 검증은 남아 있습니다.

- 사용자 프리셋: 이름1~32자/최대10개, 현재11개 설정 캡처, 저장·이름순 목록·불러오기·덮어쓰기·삭제.
- 별도 로컬 저장/schema1 JSON, 전체 검증 후 단일 적용. 앱/엔진 설정 자동 반영. 삭제는 현재 설정 유지.
- 빌드·Lint·서명·버전 검사 통과. 실제 Android 저장/재시작/전체 필드 복원 검증 대기. 다음은 프리셋 실기기 확인.

## 이전 전달 — 0.12.0

- 0.12.0 사용자 정상 적용 보고. 홈 화면에서 해안·밤·비·컬러풀 차량 확인. 해당 조건 진단 제출59.8fps/그리기10.37ms/703프레임. 모든 조합 및 장시간 전력 검증은 미실행.
- 0.11.0 사용자 정상 동작 확인.
- 요청1~5 구현: 도시 디테일, 차량 그룹4개/색상3개, 도로4개, 날씨5개, 약4초 시간대 혼합. 앱/시스템 미리보기와 홈 배경에 연결, 기존 설정값 보존.
- APK 빌드·Lint·서명·버전 검사 및 모델 검사7종 통과, 도로4종 정적 아트 출력. 실제 Android UI·추가 캐시 메모리/합성 부하·장시간 전력 확인 대기.
- 다음: 사용자 설치 후 조합/전환/설정 유지 확인, 실제 기기 성능을 바탕으로 필요한 최적화.

## 이전 전달 — 0.11.0

- 사용자 0.10.0 정상 동작 확인(밤·강한 비·12대·1.5배, 제출 59.3fps/그리기 11.77ms).
- 배경 밝기 100/80/60 선택과 자동 저장, 미리보기/홈 배경 반영, 진단 밝기 기록 추가. 하드웨어 밝기는 변경하지 않습니다.
- 빌드·Lint·서명·버전 검사 통과. 실기기 표시/설정 유지 확인 대기.

## 이전 전달 — 0.10.0

- 사용자 0.9.0 정상 동작 확인. 최신 진단 48.2fps/12.92ms는 측정 조건 미확인으로 회귀 여부 미확정.
- 6종 차량의 그릴/루프/레일/택시 표지/버스 환기구/트럭 화물칸/미러 표현을 보강했습니다. 초기 스프라이트에 저장합니다.
- APK 빌드·Lint·서명·버전 및 정적 아트 출력/12종 이미지 존재 검사 통과. 실제 Android 표현·전력 확인 대기.

## 이전 전달 — 0.9.0

- 0.8.0 정상 동작은 사용자 확인. 제출 평균 56.5fps/그리기 11.20ms 기록(저녁·강한 비·12대·1.5배).
- 0.9.0: 설정 상단 실시간 미리보기, 전체 장면 비율 유지, 설정 자동 반영, 독립 15fps 재생 및 비가시/포커스 상실 중지. 실제 배경화면 진단은 보존합니다.
- APK 빌드·Lint·서명·버전과 순수 Java 배치 검사 통과. 실제 Android UI/추가 전력 검증 대기. 다음은 사용자 설치 후 미리보기 반영·정지·프리셋 확인.

## 저장소와 환경

- GitHub: https://github.com/BACKHYUN96/S20-PLUS
- 현재 클라우드 체크아웃: /workspace/S20-PLUS
- 저장소 이름 변경 후 origin 주소와 로컬 폴더를 갱신하고 원격 읽기 접근 및 Git 무결성을 확인했습니다.
- 클라우드 환경의 저장소 설정 초안은 새 이름과 경로로 저장했습니다. 게시 완료 여부와 새 작업에서의 복원은 확인하지 않았습니다.
- pixel-traffic/에 Android 기본 앱과 Gradle 빌드 설정이 있습니다. 차량 애니메이션을 구현했으며 완성 아트는 아직 없습니다. 0.1.0 적용은 사용자 확인을 받았고 0.2.0 실기기 검증은 대기 중입니다.

## 연결된 기기

- 사용자 노트북에 USB 디버깅으로 연결한 루팅된 Samsung Galaxy S20+ (SM-G986N).
- 사용자 PowerShell에서 ADB의 device 상태를 확인했습니다.
- Windows ADB 경로: `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe`
- 현재 클라우드 에이전트는 사용자 노트북 PowerShell이나 USB 기기를 직접 조작할 수 없습니다. 사용자 실행 결과 또는 첨부 파일로 확인합니다.

## 레드존 분석

- 패키지: com.redzone.app. 사용자 기기 출력 기준 버전 0.5.2, versionCode 21.
- 첨부 APK에서 센서 모니터링, 오버레이, CPU/GPU 클럭 제어, 밝기 고정, 온도 기반 복원 및 CSV 로깅 관련 코드·문자열을 확인했습니다.
- 제작자 정보, 개발 일지, 원본 프로젝트, Git 이력은 발견하지 못했습니다. 전체 소스 디컴파일과 동작 검증은 수행하지 않았습니다.
- CSV 27개, 37,301건을 분석했습니다. 요약과 한계는 WORK_LOG.md에 있습니다.
- APK와 로그 ZIP은 이 채팅의 첨부 파일이며 저장소에는 포함하지 않았습니다. 새 환경에 없으면 사용자에게 다시 확보해야 합니다.

## 다음 작업

- 실기기 확인 갱신: 사용자가 0.1.0 APK 설치·실행 성공을 보고했고 홈 화면 스크린샷에서 임시 장면 적용을 확인했습니다. 화면 꺼짐/복귀·잠금화면·장시간 전력 검증은 미실행입니다. 0.2.0에 차량 무한 주행을 구현했으며 업데이트 후 실기기 검증이 필요합니다.

- 새 앱 아이디어: 픽셀 차량 무한 주행 라이브 배경화면. 단계 2 기본 앱을 구현했습니다. [구상 문서](PIXEL_TRAFFIC_CONCEPT.md)를 참고합니다.
- 단계 1 화면 설계 완료: 저녁 도심, 양방향 4차선, 차량 6종, 기준 화면 360×800px와 분리된 아트 레이어를 [설계 문서](PIXEL_TRAFFIC_DESIGN.md)에 정의했습니다. 최종 아트는 미구현입니다. 기본 앱 진행은 아래 단계 2를 참고합니다.
- 단계 2: [기본 앱](../pixel-traffic/README.md)에 시스템 미리보기 버튼, WallpaperService, 정적 임시 도심 장면을 구현했습니다. JDK/SDK를 설치하고 APK 빌드·Lint·서명 검사를 진행했습니다. 실제 S20+ 적용은 사용자 기기에서 확인해야 합니다. 다음은 차량 무한 주행입니다.

- 새 앱의 목적과 기능 요구 사항을 정한 뒤 소스 및 빌드 환경을 구성합니다.
- 레드존을 이어 개발할 경우 원본 프로젝트 확보를 우선합니다. 없으면 APK 역분석 결과를 바탕으로 재구현 범위를 결정합니다.
- 레드존 보호 동작을 수정할 경우 상태 문구뿐 아니라 실제 설정 복원 결과를 검증해야 합니다.
- 모든 후속 작업은 AGENTS.md의 기록 규칙에 따라 이 문서와 WORK_LOG.md에 남깁니다.

## 단계 3 현재 결과

- 0.2.0(versionCode 2): 양방향 차량 6종, 화면 밖 재사용, 원근 크기, 차간 거리, 가시 상태 렌더링 루프 구현.
- 다음은 사용자 기기의 0.2.0 움직임 확인과 단계 4 완성 아트입니다. 0.1.0 실기기 성공이 0.2.0 성공을 뜻하지 않습니다.

## 단계 4 현재 결과

- 0.3.0 첫 아트 버전: 건물/상점/가로수/가로등, 차종별 도트 자산, 이동 반사와 공통 픽셀 프레임 합성 구현. 실기기 검증 대기 중이며 생성 시안의 완성도와 동일하지 않습니다.
- APK 제공 시 PowerShell 설치 명령어를 함께 제공하도록 기록했습니다. 다음은 아트 피드백과 단계 5 설정/전력 관리입니다.

## 단계 5 현재 결과

- 사용자 보고로 0.3.0 적용 성공 확인. 0.4.0에 차량 수/속도/프레임/OS 절전 연동 설정을 추가했습니다.
- 화면 꺼짐과 비가시 상태 중단, 재개 시 시간 초기화, 설정 자동 저장과 엔진 반영 구현. 정책/모델 검사는 통과했으나 실제 방송·화면 수명주기·배터리 사용은 기기 검증 대기 중입니다.
- 다음은 단계 6 실기기 설정 검수와 장시간 안정성/전력 확인입니다.

## 단계 6 현재 결과

- 사용자 보고로 0.4.0 정상 동작 확인. 0.5.0 진단 UI와 로컬 프레임 기록, [실기기 검증 절차](PIXEL_TRAFFIC_DEVICE_CHECKS.md), 읽기 전용 PowerShell 수집 도구를 추가했습니다.
- 장시간/배터리 검증은 미완료입니다. 0.5.0 진단 기록과 화면 꺼짐/다른 앱 복귀/절전 모드 결과를 받은 뒤 평가합니다.

## 단계 7 현재 결과

- 0.5.0 실기기 진단: 제한 60fps, 제출 평균 56.2fps, 그리기 평균 8.20ms, 프레임 614. 앱을 열 때 가려짐/중지 상태 확인. 장시간 안정성·배터리는 여전히 미검증입니다.
- 0.6.0에 누적 기한 기반 프레임 예약, 낮/저녁/밤 수동 선택, 한국어 진단 상태를 추가했습니다. 계산 및 아트 검사와 APK 빌드는 통과했으나 실제 개선 수치는 다음 기기 진단으로 비교해야 합니다.

## 최신 실기기 확인

- 0.6.0 낮/저녁/밤 적용 성공 사용자 확인. 60fps 제한에서 제출 평균 59.9fps, 그리기 평균 7.60ms, 성공 프레임 3336을 스크린샷으로 확인했습니다.
- 이전 관찰보다 제출 평균이 높지만 비교 조건은 동일함을 확인하지 못했습니다. 장시간 안정성·전력 검증은 남아 있습니다.

## 날씨 확장 현재 결과

- 0.7.0: 수동 맑음/약한 비/강한 비, 도로 물 튀김, 반사/밝기 보강 구현. 기존 장면 시간대와 독립적으로 선택합니다. 비 시뮬레이션 검사를 통과했으며 실제 기기의 비 표현/부하는 검증 대기 중입니다.
- 진단에 장면·날씨·차량 수·속도를 기록합니다. 장시간 안정성·전력 검증은 계속 남아 있습니다.

## 자동 시간대 및 빠른 모드 현재 결과

- 0.7.0 밤/강한 비/12대/1.5배에서 사용자 진단 59.3fps, 그리기 11.03ms, 프레임 1219 및 정상 동작 보고 확인. 장시간 전력 결과는 아닙니다.
- 0.8.0에 휴대폰 현지 시각 자동 시간대와 3개 빠른 설정 모드를 추가했습니다. 정책 검사는 통과했으며 UI/시간·시간대 변경 실기기 검증은 대기 중입니다.
