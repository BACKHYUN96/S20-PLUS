# 새 환경 빌드·서명 복원

## Git 또는 오프라인 백업에서 복원

새 채팅에서 `BACKHYUN96/S20-PLUS`의 main을 선택하고 [HANDOFF](HANDOFF.md)를 읽습니다. 소스 체크포인트 태그는 `pixel-traffic-v0.29.0`입니다. 오래된 스냅샷에 문서가 없다면 원격 main을 fetch하고, 기존 변경이 없는 경우에만 fast-forward합니다. 기존 작업을 reset하거나 덮어쓰지 않습니다.

```bash
git clone https://github.com/BACKHYUN96/S20-PLUS.git
cd S20-PLUS
git show pixel-traffic-v0.29.0:docs/HANDOFF.md
```

GitHub를 사용할 수 없다면 다운로드한 `S20-PLUS-0.29.0-handoff.zip`을 풀고 `SHA256SUMS`를 확인합니다. 그 안의 bundle은 소스·그림·문서와 Git 이력을 포함합니다.

```bash
sha256sum -c SHA256SUMS
git clone -b work S20-PLUS.bundle S20-PLUS
git -C S20-PLUS fsck --full
```

bundle의 branch명은 work이며 원격 main과 같은 최종 보존 커밋입니다. bundle을 clone한 origin은 로컬 bundle 경로입니다. 다시 GitHub와 연결할 때는 기존 origin을 확인하고 다음 명령을 사용합니다. 먼저 로컬 파일/커밋을 보존하고 강제 push나 reset을 하지 않습니다.

```bash
git -C S20-PLUS remote set-url origin https://github.com/BACKHYUN96/S20-PLUS.git
```

## 기존 APK 업데이트용 서명키

기존 설치 앱을 업데이트하려면 같은 서명키가 필요합니다. `debug.keystore`는 Git에 없습니다. 별도로 보관한 `S20-PLUS-0.29.0-signing-backup.zip`을 새 환경에 첨부하거나 이미 보존된 키를 사용합니다.

- 공개 인증서 SHA256: `a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`
- 기존 키 파일 SHA256: `6e3050b987c1baa866c2c98cab3853bc4eee2fe05c6d1f778d3ad6eeece2b66c`

공개 fingerprint/hash와 개인키 자체는 다릅니다. `pixel-traffic` 폴더에서 복원합니다.

```bash
python tools/restore-debug-signing.py /path/to/S20-PLUS-0.29.0-signing-backup.zip
```

복원기는 `debug.keystore` 항목 하나만 읽고 파일 hash를 확인합니다. 동일 키면 유지하고, 다른 키가 있으면 덮어쓰기를 거부합니다. 기존 다른 앱의 키를 보존하면서 별도 `ANDROID_USER_HOME`을 정해 복원과 빌드에 함께 사용할 수 있습니다.

```bash
export ANDROID_USER_HOME="$PWD/.local/android-user"
python tools/restore-debug-signing.py /path/to/S20-PLUS-0.29.0-signing-backup.zip
python tools/build-cloud.py --offline :app:assembleDebug :app:lintDebug
```

Windows PowerShell에서는 해당 환경 변수를 `$env:ANDROID_USER_HOME`으로 설정합니다. 앱 빌드 설정의 기본 Android debug 서명 경로를 사용합니다. 다른 키로 업데이트를 만들거나 서명 오류 때문에 기존 앱을 삭제하지 않습니다. 서명 백업은 개인 보관하고 Git이나 공개 저장소에 넣지 않습니다.

## 도구·SDK·프록시

필요한 도구는 JDK 17 이상 전체 컴파일러, Android SDK platform 35/BuildTools 35.0.0, Python 3, Gradle Wrapper 8.11.1/AGP 8.9.2입니다. wrapper JAR와 배포 SHA256 핀은 저장소에 있습니다. SDK/JDK/Gradle 캐시 전체는 백업에 없습니다. 현재 환경의 도구를 확인하고, 없을 때만 환경 설정 스킬의 공식 검증 설치 절차를 사용합니다.

`tools/build-cloud.py`는 설정된 `JAVA_HOME`, `ANDROID_HOME`, `ANDROID_USER_HOME`, `GRADLE_USER_HOME`을 우선합니다. 없으면 실제 존재하는 `/workspace/toolchains` 도구를 사용합니다. 기존 Android user home이 없으면 프로젝트 `.local/android-user`, Gradle 캐시가 없으면 `.local/gradle-home`을 사용합니다. 이 경로는 Git에서 제외됩니다. SDK는 `ANDROID_HOME`이나 미추적 `local.properties`로 연결합니다.

상속된 `HTTPS_PROXY`/`HTTP_PROXY`의 호스트·포트를 JVM에 전달하고 Linux 시스템 CA가 있으면 사용합니다. 프록시 인증과 TLS/체크섬 검증을 유지하며 민감값을 출력하지 않습니다. `--offline`은 필요한 의존성이 캐시된 경우에만 사용합니다. 캐시가 없으면 실제 네트워크 정책과 접근을 확인한 후 공식 의존성을 다운로드합니다. 이 앱에는 컨테이너나 상시 실행 서버가 필요하지 않습니다.

```bash
cd pixel-traffic
python tools/build-cloud.py --offline :app:assembleDebug :app:lintDebug
```

관련 Java/합성 검사는 [PATCH_GUIDE](PATCH_GUIDE.md)에 따라 선택합니다. 새 환경에 예전 `/tmp` 클래스가 있다고 가정하지 않습니다.

최종 APK는 `app/build/outputs/apk/debug/app-debug.apk`입니다. aapt/apksigner로 버전·min/target SDK·기존 인증서를 확인하고 포함된 PNG 13개가 소스와 같은지 검사합니다. 파일명에 실제 버전을 넣고 [앱 README](../pixel-traffic/README.md)의 PowerShell 설치 명령도 함께 전달합니다.

## 확인된 복원 범위

2026-10-07 새 폴더 clone에서 위 빌드 명령으로 assembleDebug/lintDebug가 19초에 성공했습니다. Lint 이슈가 없었고 APK는 기존 전달 0.29.0과 byte 단위로 같았습니다. 서명과 PNG 13개도 일치했습니다. 소스 체크포인트의 Git bundle clone과 `git fsck --full`도 통과했습니다.

이는 기존 JDK/SDK/캐시를 재사용한 현재 인스턴스의 복원 검사입니다. 새 머신에서 도구를 처음 설치하거나 새 클라우드 작업을 게시·실행한 결과는 아닙니다. 실제 기기 성능 검사와 Java2D 합성 검사도 구분합니다. 최종 백업의 커밋·파일 checksum·보존 범위는 ZIP의 `MANIFEST.json`과 `SHA256SUMS`에 기록합니다.
