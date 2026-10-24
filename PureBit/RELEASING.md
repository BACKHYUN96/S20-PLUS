# BITWIRE GitHub Actions / Releases

## 저장소 확인과 범위

2026-10-24 원격 main `e8e786c1d59ded2208d930a874a95f81bc4e8cb3`를 확인했을 때
`PureBit/`와 BITWIRE workflow는 GitHub에 없었다. 로컬 최신 소스는 1.10.0/code47이었다.
기존 main의 다른 앱·워크플로·문서를 보존하며 최신 BITWIRE 소스와 배포 구성을 추가한다.
저장소는 확인 당시 이미 public이었다. 이 작업은 저장소 공개/비공개 설정을 변경하지 않는다.

## 실행과 파일

- 일반 main 푸시/PR: 네이티브 시험, APK 컴파일, JVM, Lint 및 릴리스 도구 검사를 실행한다. **Release를 만들지 않는다.**
- Actions → BITWIRE Android → Run workflow: main에서 `publish=false`는 검사만, `publish=true`는 서명 검사 후 Release를 게시한다.
- `bitwire-v1.10.0` 같은 태그 푸시: Gradle versionName과 정확히 같아야 게시한다. `unity-apk-*` 등 다른 앱 태그는 이 workflow를 실행하지 않는다.
- 정식 버전 태그 예: `bitwire-v1.10.0`. 시험 버전 예: `bitwire-v1.11.0-rc.1` (Gradle 버전도 같아야 함).
- 모든 릴리스 APK 이름은 `BITWIRE.apk`. `BITWIRE.apk.sha256`, `build-info.json`을 함께 첨부한다.
- 같은 버전 Release/draft는 자동 덮어쓰기하지 않는다. 실패 후 남은 draft는 명시적으로 정리하고 재실행하거나 새 버전을 사용한다.
- 이미 게시한 버전의 APK를 다른 바이트로 교체하지 않는다. 새 버전은 versionName/versionCode를 올린다.
- prerelease는 최신 안정 Release로 지정하지 않는다. 과거 BITWIRE 안정 버전을 latest로 되돌리는 게시도 막는다.

예정된 고정 주소(실제 게시/검증 결과는 저장소의 docs/STATUS.md 확인):

https://github.com/BACKHYUN96/S20-PLUS/releases/latest/download/BITWIRE.apk

버전별 주소:

https://github.com/BACKHYUN96/S20-PLUS/releases/download/bitwire-v1.10.0/BITWIRE.apk

`latest`는 GitHub 저장소 전체에서 하나다. 다른 앱도 이 저장소의 Release를 latest로 게시하면
대상이 바뀔 수 있다. BITWIRE의 버전별 주소는 영향을 받지 않는다.
비공개 저장소에서는 링크 형식이 같아도 로그인/인증이 필요하며, 공개 다운로드를 위해
저장소를 public으로 바꾸지 않는다.

## 기존 서명 보호 — 최초 1회 설정

기존 applicationId `app.purebit`, Gradle `assembleDebug`와 서명 설정을 유지한다.
새 release keystore나 CI의 임시 debug key로 배포를 대체하지 않는다.
기존 설치 APK의 인증서 SHA-256은 다음 공개 지문이다(개인키가 아님).

`a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`

필수 Repository Actions Secret:

`BITWIRE_DEBUG_KEYSTORE_BASE64` — 기존 BITWIRE `debug.keystore` 바이트의 Base64.

원본 키를 가진 인증된 관리자만 GitHub의 Settings → Secrets and variables → Actions에 등록한다.
키를 새로 생성하지 말고, 채팅/Issue/소스/Actions artifact/공개 다운로드에 키를 올리지 않는다.
기존 키 백업의 실제 위치는 소스 저장소에 포함하지 않는다.

기존 키 지문을 먼저 검사하고 GitHub CLI stdin으로 직접 등록하는 도구:

```bash
cd PureBit
python3 tools/release/register_signing_secret.py --keystore /secure/path/to/existing/debug.keystore
```

키 파일은 수정하지 않고 Secret 값이나 토큰을 출력/소스 저장하지 않는다. 실행 환경의 `api.github.com` 접근 및 GitHub 관리자 Secret 쓰기 권한이 필요하다.

GitHub CLI로 키를 화면에 출력하지 않고 등록하는 예시:

```bash
base64 -w0 /secure/path/to/existing/debug.keystore | gh secret set BITWIRE_DEBUG_KEYSTORE_BASE64 --repo BACKHYUN96/S20-PLUS
```

전용 Secret이 없으면 같은 저장소의 기존 `PIXEL_TRAFFIC_KEYSTORE_BASE64`를 후보로 사용할 수 있다. 기존 앱의 실제 공개 인증서가 BITWIRE와 일치함을 확인했으며 APK 게시 전 동일 지문을 다시 강제한다. 키가 없거나 지문이 다르면 새 키로 대체하지 않는다. 신뢰된 main 푸시의 probe는 값 노출 없이 가용성/지문만 검사하고 임시 복원 파일을 즉시 삭제한다.

키는 게시 빌드와 위 준비 검사에서 runner 임시 ANDROID_USER_HOME으로 복원하고, 마지막에 삭제한다.
Secret이 없으면 명확한 오류로 게시를 중단하며 대체 키를 만들지 않는다.
APK 생성 후 apksigner 인증서 지문이 위 값과 다르면 게시하지 않는다.

일반 CI는 기존 Gradle의 임시 debug 빌드를 컴파일 검사에만 사용한다.
이 APK는 Release/다운로드 artifact로 배포하지 않는다. 설치할 APK는 반드시 기존 키의
지문 검사와 패키지/버전/16KiB 정렬 검사를 통과한 게시 경로에서 생성한다.

## 검증·로그·권한

- JDK21, Gradle Wrapper9.4.1(기존 SHA256 고정), SDK37.0/build-tools36.0.0,
  NDK28.2.13676358/CMake3.22.1을 사용한다. 앱 Gradle 설정/오디오 기능을 바꾸지 않는다.
- build job은 contents:read, 게시 job만 contents:write를 사용한다. PR은 게시 job을 실행할 수 없다.
- 릴리스 노트에는 빌드 성공, 버전/code, 정확한 소스 커밋, APK/인증서 SHA-256,
  JVM 결과 및 Actions 로그 링크를 기록한다.
- draft에 파일을 올린 후 실제 자산을 다시 내려받아 원본과 비교하고 나서 게시한다.
- 공개 저장소에서는 버전별 및 latest 고정 주소를 실제 HTTP 다운로드하여 SHA-256을 비교한다.
- 비공개 저장소에서는 인증된 자산 다운로드를 검증하며 익명 접근 성공을 주장하지 않는다.
- Actions 실행 화면의 실패 step 로그와 `bitwire-logs-*`, `bitwire-publish-*` artifact로
  Gradle/native/도구 로그, 테스트/Lint 보고서를 확인한다. 보관은 7일이다.
- 최종 배포 파일은 GitHub Release에 유지하므로 Codex 클라우드 artifact에 의존하지 않는다.
- GitHub Actions 사용 한도/결제/조직 정책으로 실행이 거부되면 실제 오류를 보고한다.

## 사용자 PC 설치 예시

GitHub Release에서 BITWIRE.apk를 내려받고, 이전 설치를 유지한 채 업데이트한다.
아래 명령은 사용자 Windows PC에서 실행한다. 클라우드에서 실기기 설치한 것으로 기록하지 않는다.

```powershell
adb install -r "C:\Users\김백현\Desktop\AI\BITWIRE.apk"
```

기존 서명키가 없는 경우 앱 제거를 대안으로 강요하지 않는다. 먼저 기존 키를 복구한다.
빌드/다운로드 성공은 S26의 모든 기능·시각 품질·DAC bit-perfect 검증을 뜻하지 않는다.
