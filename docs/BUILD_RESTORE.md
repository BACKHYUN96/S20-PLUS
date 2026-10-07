# 새 환경 빌드·서명 복원

## Git 또는 오프라인 백업에서 복원

일반적인새채팅은 `BACKHYUN96/S20-PLUS`의main을선택하고 [HANDOFF](HANDOFF.md)를읽습니다. 소스체크포인트태그는 `pixel-traffic-v0.29.0`입니다. 오래된스냅샷에서문서가없다면원격main을fetch하고기존변경이없는경우에만fast-forward합니다. 기존작업을reset/덮어쓰기하지않습니다.

```bash
git clone https://github.com/BACKHYUN96/S20-PLUS.git
cd S20-PLUS
git show pixel-traffic-v0.29.0:docs/HANDOFF.md
```

GitHub를쓸수없다면사용자가다운로드한 `S20-PLUS-0.29.0-handoff.zip`을풀고manifest의SHA256SUMS를확인합니다. 그안의bundle은소스/그림/문서와Git이력을모두포함합니다.

```bash
git clone -b work S20-PLUS.bundle S20-PLUS
git -C S20-PLUS fsck --full
```

bundle체크아웃의branch명은work이며원격main과같은최신보존커밋입니다. bundle을clone한origin은로컬bundle경로입니다. 다시GitHub와연결할때는기존origin을확인하고 `git remote set-url origin https://github.com/BACKHYUN96/S20-PLUS.git`를사용합니다. 먼저local파일/커밋을보존하고강제push/reset은하지않습니다.

## 기존 APK 업데이트용 서명키

기존설치앱을업데이트하려면서명키가같아야합니다. `debug.keystore`는Git에포함하지않습니다. 사용자가별도로보관한 `S20-PLUS-0.29.0-signing-backup.zip`을새환경에첨부하거나이미보존된키를사용합니다. 공개인증서SHA256은 `a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`, 기존key파일SHA256은 `6e3050b987c1baa866c2c98cab3853bc4eee2fe05c6d1f778d3ad6eeece2b66c`입니다. 공개fingerprint/hash와개인키자체는다릅니다.

`pixel-traffic`에서:

```bash
python tools/restore-debug-signing.py /path/to/S20-PLUS-0.29.0-signing-backup.zip
```

복원기는고정entry하나만읽고key파일hash를확인합니다. 동일키면그대로두고다른키가있으면덮어쓰기를거부합니다. 이경우기존다른앱키를보존하고새 `ANDROID_USER_HOME`을정해복원과빌드에같이사용합니다. 아래는Linux예시이며Windows PowerShell은 `$env:ANDROID_USER_HOME`을사용합니다.

```bash
export ANDROID_USER_HOME="$PWD/.local/android-user"
python tools/restore-debug-signing.py /path/to/S20-PLUS-0.29.0-signing-backup.zip
python tools/build-cloud.py --offline :app:assembleDebug :app:lintDebug
```

앱설정은바꾸지않고AndroidGradle의debug서명경로를사용합니다. 새로생성된다른키로업데이트를빌드하거나서명오류때문에기존앱을삭제하지않습니다. 별도서명백업은개인보관하고Git/공개저장소에넣지않습니다.

## 도구·SDK·프록시

필수:JDK17이상전체컴파일러,AndroidSDK platform35/BuildTools35.0.0,Python3(저장소도우미),GradleWrapper8.11.1/AGP8.9.2. wrapperJAR와배포SHA256핀은저장소에있습니다. SDK/JDK/Gradle캐시전체는백업에없으며현재클라우드에있는지확인하고필요할때환경설정스킬의공식검증설치경로를사용합니다. 새환경에서설치과정/전체SDK복원이이미검증됐다고가정하지않습니다.

`tools/build-cloud.py`는설정된JAVA_HOME/ANDROID_HOME/ANDROID_USER_HOME/GRADLE_USER_HOME을우선합니다. 없으면실제로존재하는 `/workspace/toolchains`도구를사용합니다. Androidhome은없을때프로젝트 `.local/android-user`,Gradlehome은없을때 `.local/gradle-home`입니다. 이경로는Git제외입니다. SDK는ANDROID_HOME으로연결하거나미추적local.properties로설정합니다.

상속HTTPS_PROXY/HTTP_PROXY의호스트·포트를JVM에전달하고Linux시스템CA가있으면사용합니다. 기존프록시/인증/TLS검증을유지하며민감값을출력하지않습니다. `--offline`은필요한의존성이캐시된경우에만씁니다. 캐시가없으면실제네트워크정책/접근을확인한후공식의존성을검증다운로드합니다. 컨테이너/서비스는이앱에필요하지않습니다.

```bash
cd pixel-traffic
python tools/build-cloud.py --offline :app:assembleDebug :app:lintDebug
JAVA_HOME=/path/to/jdk bash tools/check-patch.sh cinematic
```

최종Android출력은 `app/build/outputs/apk/debug/app-debug.apk`입니다. aapt/apksigner로versionCode/versionName/min/target·기존인증서를확인하고APK에들어간PNG13개가소스와같은지확인합니다. 실제기기성능검사와Java2D합성검사는구분합니다. 적용할파일명으로복사한후 [앱README](../pixel-traffic/README.md)의PowerShell명령을전달합니다.
