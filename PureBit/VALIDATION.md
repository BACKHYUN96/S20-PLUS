# v0.1 검증 기록

검증 일자: 2026-10-14 UTC. Galaxy S26 Ultra 실기기는 연결되지 않았다.

- Android SDK 37.0 / compileSdk 37 / targetSdk 37 / minSdk 31
- Android Gradle Plugin 9.2.0 (내장 Kotlin), Gradle 9.4.1
- 전체 JDK: Temurin 21.0.8+9
- 실행 task: `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug`
- 결과: **BUILD SUCCESSFUL**
- JVM 테스트: **12 passed, 0 failures/errors/skipped**
- Android lint: **No issues found.** API 31/32 private receiver는 signature permission으로 보호하고, API 33 이상에서는 NOT_EXPORTED를 사용한다. 낮은 API helper의 lint 오탐만 좁게 설명/억제했다. 캐시 여유 공간은 다른 앱 데이터를 회수하지 않는 보수적 정책이며 기술용 테스트 UI의 localization lint는 명시적으로 범위를 제한했다.
- APK 서명: `apksigner verify --verbose`, v2 signature 검증 성공
- APK package: `app.purebit`, version `0.1.0` / code 1, debug build
- APK SHA-256: `35fe8f87e9a8f4b2e2fd823bf5349ae9aca10c82700ddbadb6d8dba3131b503f`
- 기존 저장소 README 내용 유지. 원격 push/PR 생성 없음.

## 테스트 범위

`PcmTest` 7개: 정수 WAV16/24/32의 byte equality, float/truncated/misaligned/multichannel WAV 거부, 독립 생성 FLAC16/24 reference와 디코딩 결과/MD5 비교, MD5 불일치/잘린 FLAC 거부, MD5 없는 FLAC 상태, CRC 손상 시 합성 silence 대신 거부.

`UsbDescriptorsTest` 3개: UAC1 rate/채널 배치/iso OUT parsing, UAC2 bit depth/subslot/config parsing 및 조회하지 않은 clock rate의 UNKNOWN 유지, malformed descriptor와 입력 endpoint 제외.

`VerificationTest` 2개: AudioTrack이 수락한 구간만 비교/hash, 샘플 rate만 같은 경우 또는 client 포맷만 같은 경우의 승격 범위 제한. DEVICE PATH 근거 없이 DIRECT OUTPUT/VERIFIED BIT-PERFECT에 도달하지 않음.

수동 테스트 음원 생성 스크립트 실행 및 로그 수집 shell 문법 검사도 성공했다. 이 검사는 Android Activity 실행이나 실제 AudioTrack/USB hardware 재생을 확인하지 않는다.

## 아직 실행하지 못한 검증

실기기 APK 설치/Activity 실행, SAF chooser, AudioTrack의 실제 PCM24/32 수락과 소리 출력, 재생/탐색/route 변경의 장치 동작, preferred mixer 정책 적용, Samsung AudioFlinger/HAL/DSP 출력, USB permission/descriptor의 실물 응답 및 DAC 수신 샘플 비교는 **미검증**이다. README의 실기기 절차로 확인해야 한다. 외부 digital capture가 없어 VERIFIED BIT-PERFECT를 주장하지 않는다.

## v0.1.1 — PC 없는 자체진단

2026-10-14 UTC: `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug` BUILD SUCCESSFUL.
자동 테스트 **17개 통과**, 실패/오류 0. lint: **No issues found**.
추가 5개 테스트는 실기기에서 보고된 44.1 kHz 원본/48·96 kHz capability 불일치의 주의 표시, 일치하는 capability로 실제 DAC 경로를 검증했다고 주장하지 않는 동작, paused/empty/unknown evidence, buffer mismatch/underrun 표시, 최근 앱 로그의 메모리 한도를 확인한다.
APK 버전 0.1.1 / code 2. apksigner 검증 성공 및 기존 v0.1 APK와 certificate SHA-256 일치 확인. 기존 앱 위에 업데이트 설치할 수 있는 서명이다.
APK SHA-256: `3500aa377e206fc0f7e97ac382921fb30cda4ec7df03cb8dbbe64655d25cd4a9`

PC 없이 원본/클라이언트/route/capability/PCM 검증/underrun 분석 및 자체 로그 보고서를 생성하고 공유/SAF 저장한다. 저장/공유 전에 immutable 재생 snapshot을 캡처한다. 파일 교체/seek 후 이전 PCM 검증 구간을 새 파일/구간에 재사용하지 않도록 초기화한다.

실기기 자료에서 SM-S948N / Android 17 / Samsung USB C Earphones(04e8:a05b)의 원본·앱 버퍼 검증과 USB route가 보고된 것을 확인했다. **새 v0.1.1의 Activity/provider 공유, 파일 picker snapshot 유지, 실제 사용자 공유 앱 수신은 이 환경에서 실기기 실행하지 못했다.** AudioFlinger/HAL/DAC 데이터 무변형은 여전히 미검증이다. 일반 앱 권한 밖의 dumpsys나 다른 앱 logcat을 읽으려고 시도하지 않는다.

## v0.2.0-prototype — Samsung native USB Direct

2026-10-14 UTC. AGP 9.2.0 / Gradle 9.4.1 / SDK 37 / NDK 28.2.13676358 / CMake 3.22.1.
`:app:assembleDebug :app:testDebugUnitTest :app:lintDebug` **BUILD SUCCESSFUL**. Native arm64-v8a/x86_64 cross-compilation with `-Wall -Wextra -Werror` passed. JVM tests **23 passed, 0 failures/errors**. lint **No issues found**.

Native production transport mock tests passed under AddressSanitizer/UndefinedBehaviorSanitizer: standard SHA-256 vectors (empty/abc/million a), fixed packet lengths, signed 16/24/32-bit -60 dBFS peak boundaries, successful ISO completions, EPERM, short/out-of-order/corrupt packet rejection and fd cleanup. LeakSanitizer cannot inspect processes in this sandbox (ptrace restriction); native test invocation used `ASAN_OPTIONS=detect_leaks=0`. These are mock host tests, not USB hardware tests.

Additional JVM tests cover exact Samsung format/endpoint selection, refusal of 44.1 kHz/other device/asynchronous/adaptive/UAC2/slot conversion, actual reported descriptor fields, generated 24/48 source duration/peak/silent ends, and DIRECT OUTPUT promotion only for an active native session with completed frames; no VERIFIED BIT-PERFECT promotion.

APK v2 signature verified, signing certificate matches v0.1.1, version code 3 / name 0.2.0-prototype. ELF PT_LOAD alignment **>=16 KiB** for both ABIs and APK `zipalign -c -P 16 4` passed. NDK runtime notices retained in source and APK.
APK SHA-256: `2646ebbd175d17723540a935cf1459df98bdbd386fa9e71d84dc2b27d2924e85`

**Not verified:** S26 Ultra permission-fd native ioctls, GET_INTERFACE/GET_CUR responses, streaming interface takeover, SET_INTERFACE/clock acceptance, real ISO transfer, audible signal, Android driver/route restoration, native pause/seek/detach hardware behavior and DAC sample identity. Known kernel/SELinux/ALSA policy failures are surfaced with stage/errno and do not silently switch to AudioTrack. Testing is limited to Samsung 04e8:a05b, measured FULL speed, synchronous UAC1 stereo 48/96 kHz, <=30s and peak <=-60 dBFS files.

The main expected device artifact is a PC-free diagnostic report after the read-only probe and/or the built-in 5s test. DEVICE PATH NOT VERIFIED / BIT-PERFECT NOT VERIFIED remains unconditional.

## v0.3.0-volume-prototype

2026-10-14 UTC: Gradle assembleDebug/testDebugUnitTest/lintDebug **BUILD SUCCESSFUL**. 23 JVM tests passed, 0 failures/errors; lint **No issues found**. arm64/x86_64 native build with compiler warnings treated as errors passed. Expanded production-native mock tests passed under ASan/UBSan (`detect_leaks=0` for sandbox ptrace limitations).

Native checks additionally cover streaming PCM cache reads without whole-file RAM storage, hardware volume target quantization/no increase from quieter values, observed FU5 topology validation, control claim/GET/range failures, mute SET/readback before volume SET, ignored SET detection, first-packet volume mismatch with **0 PCM submissions**, full-scale PCM SHA-256 equality under hardware attenuation, original mute restoration and refusal to restore a louder original volume. No physical hardware/acoustic claim is derived from these mocks.

APK signature verified and matches v0.2.0. Version code 4 / name 0.3.0-volume-prototype. Both native ABIs have ELF LOAD alignment >=16KiB; APK zipalign -P16 passed. APK SHA-256: `69832ab0926416aca05e4a4eb5022bda26c254af565c67617339939ef589daa0`

User-confirmed v0.2 baseline: 5s native test completed 240000 stereo frames at 24/48 with errors=0 and full PCM/payload hash equality; test signal heard; manual reselection of original music in Android mode played normally.

**v0.3 still unverified on-device:** FU5 GET_CUR/MIN/MAX/RES, control IF0 takeover, mute/volume SET/readback, actual attenuation and loudness, periodic checks under long playback, Android/microphone restoration. Normal music is conditional on a fresh per-PLAY handshake; metadata/claim/readback failure blocks before PCM. Low-level test mode remains capped at <=30s/-60dBFS. Hardware-gated music streams from a private cache up to the existing 2GiB limit; no software volume/SRC.

PureBit does not automatically restore a louder original hardware volume on cleanup. Android driver reconnect may change device state outside the app's control; stop playback before reconnecting. Actual DSP/DAC sample identity remains NOT VERIFIED.

## v0.3.1-comparison-prototype

2026-10-14: assembleDebug/testDebugUnitTest/lintDebug BUILD SUCCESSFUL; 25 JVM tests, zero failures/errors; lint No issues found. Native ASan/UBSan mock tests passed (detect_leaks=0). Added checks for matching 48/96k waveform at corresponding sample times, exact WAV formats/lengths, first/last second silence, stereo equality, peak limits, and playback volume/mute readback observations and refusal on mismatch before any PCM submission. APK signature and 16KiB ZIP/ELF LOAD alignment passed; signer certificate unchanged from v0.3.0. APK SHA-256: `60aeaa008c51e59d00fbbc446199fcce55101832771b3032fe1e85c6ff073a33`.

Real-device reports (9)/(10) now establish UAC1 FU5 master range -60..0 dB/0.5 dB step, mute/volume SET/readback success at -50 dB, and 24/96 music kernel ISO completions with no reported errors and equal source/payload segment hashes. User reported no audible music. Acoustic attenuation, actual DAC sample identity, and v0.3.1 comparison remain unverified. Last successful playback hardware readback and count are now exposed; these are distinct from cleanup mute/current device state. Results for each test are retained only within the current engine instance and included in saved diagnostics.

## v0.3.2-clock-prototype

2026-10-14: assembleDebug/testDebugUnitTest/lintDebug BUILD SUCCESSFUL. 25 JVM tests, 0 failures/errors; lint No issues found. Native ASan/UBSan syscall mocks passed (detect_leaks=0). Additional native cases model GET_CUR claiming 96k while physical mock state remains 48k: forced SET_CUR still occurs, idle -> active -> exact SET order checked, repeated rate confirmations verified. Idle/active alternate mismatch, idle SET failure, clock SET failure, first/delayed rate mismatch are rejected before PCM submissions. 48k matching/changed-rate baseline paths tested. Post-clock mute/volume mismatch blocks before unmute/PCM. These tests verify application control flow, not a Samsung firmware fix.

APK signer SHA-256 certificate unchanged; ZIP/ELF 16KiB alignment passed for arm64-v8a/x86_64. Version code 6 / name 0.3.2-clock-prototype, displayed and exported from BuildConfig. APK SHA-256: `2e9559e13912c07d895bd827474503fad0662c57922276128e4d233b156a60af`.

User confirmed v0.3.1 comparison: 48k heard, 96k not heard; both had all 5-second frame completions, errors=0, matching payload/source SHA, 20 hardware checks with last volume -50dB/mute=0. **The actual cause of 96k silence and effectiveness of v0.3.2 remain unverified until another device report.** SOURCE PCM/APP USB OUTPUT BUFFER scopes remain distinct from DAC sample identity; BIT-PERFECT NOT VERIFIED stays unconditional. Current 96k hypothesis and 10/20ms delays are experimental and documented in README.

## BITWIRE v0.4.0 preview

2026-10-15 KST: final assembleDebug/testDebugUnitTest/lintDebug BUILD SUCCESSFUL. 32 JVM tests, zero failures/errors, lint No issues found. Four new spectrum tests cover signed16/24/32PCM and unchanged input,48/96k sine frequency bands/levels, silence/partial source window and stereo phase cancellation. Three library rules tests cover valid file hints, stable IDs/duplicates and ordering. Production-native syscall mocks pass ASan/UBSan (detect_leaks=0 for sandbox restrictions), including source-validation reuse, preserved-mtime modification rejected throughctime, path replacement/wronghash/format rejection, and prepare/begin cancellation before PCM/unmute, during unmute/hash, with normal zero-error cancellation. Native96k workaround/volume/transport checks remain enabled.

Integration review corrected stale auto-start/EOF callbacks, native cancellation being confused withEOF, stale seek resume intent and redundantPLAY, startup background/destruction permission requests, shared library atomicwrite/cancellation races, 80-row pagination, shuffle traversal end withrepeatOFF, and API33+ back handling. Main app/source spectrum never changes playback PCM. Foreground service preserves engine through Activity recreation/stop; source cache proof scope is explicitly documented as initial hash plus identity guard, with per-buffer hashes retained.

APK signature certificate matches all earlierPureBit APKs, applicationId app.purebit / labelBITWIRE / versionCode7 / versionName0.4.0-bitwire-preview. ZIPalign and arm64/x86_64 ELF LOAD alignments>=16KiB passed. APK SHA-256: `d4aceee1d15f9cb8f3efa7d51ae8f2020550cf62a74bfc7326a4390a9524a4c4`.

**Not device-tested here:** visual layout/accessibility onSamsung, actual sustained60FPS/frame pacing/heat, MediaSession/notifications/system sleep/background, firstpreparecancellation and queue transitions, seek responsiveness under device I/O, large real SAFprovider scans, USB replug/profile compatibility with the new runtime. The prior user's v0.3.2 audible48/96k/FLAC successes do not independently verifyv0.4. No Android emulator/physicalGalaxy was attached in this buildenvironment. SourcePCM and appUSB buffer evidence remain separate from DEVICE PATH VERIFIED; actualDAC samples/internalDSP/hardwarevolume acousticattenuation remain unverified.

## BITWIRE v0.4.1 volume fix

2026-10-15 KST: assembleDebug/testDebugUnitTest/lintDebug BUILD SUCCESSFUL,32 JVM tests passed,lint No issues found. Native ASan/UBSan tests passed, including user volume up/down, min/maxclamp, ignored SET/readback refusal, muted-state rejection, remembered explicit volume under mute, and refusal to automatically raise above an already quieter device. Source/payload tests retained. User v0.4 report had USB96k transfer/errors0/equalhash but fixed-50dB and no volume UI; this motivated hardware volume controls.

No active-session volume SET is issued from the audio-worker/Java concurrently with the native guard: pending direction is consumed by the native transferthread. Phone/media controls and UI steps are relative; PCM scaling remainsoff. Confirmed usergain remembered within connection, reset onUSBdetach/Androidmode; initialcold setting remains-50dB orlower. PausedUSBvolume controls intentionallydisabled. New controls/acoustic response/systemremotevolume routing requireGalaxytesting. DACsampleidentity remainsNOT VERIFIED.

VersionCode8/name0.4.1-volume-preview; same debug signer,ZIP andELF16KiB alignment verified. APK SHA256 `ae682c5ea45aae9103c2dd690d367a38e45b294d26b42de2744bc21c528fb684`.

## BITWIRE v0.4.2 effects preview

2026-10-15 KST: final assembleDebug/testDebugUnitTest/lintDebug BUILD SUCCESSFUL. 40 JVM tests passed,0 failures/errors; lint No issues found. New tests cover multichannel signed16/24/32waveform peaks/inputimmutability, opposite-phase preservation, chunked global bucket mapping/transient placement, silence/shortsource/partialframe/overflow/incomplete rejection, signed DC offsets, source-energy attack/decay/silence/disable and differentframeintervals, and distance-uniform/degenerate circuit traversal. Overview read is separate low-priority read-only worker withsourceepoch cancellation, lifecycle stop andbasicSeekBar fallback onfailure.

All audio/native sourcefiles matchv0.4.1 source archive byteforbyte. Both packaged libpurebit_usb.soABIs matchv0.4.1 binaries byteforbyte. Same debugsigner, ZIPalign16KiB andELF LOAD>=16KiB passed. VersionCode9/name0.4.2-effects-preview. APK SHA256 `7cbb3126506c12d9c1b34bbea8a60b7ced5728596cd46d25b18c541ba5bb3191`.

User's v0.4.1 report/confirmation established working volume adjustments:71accepted hardwareupdates,last-31dB,transmissionerrors0 andmatchingsegmentPCM/payloadSHA. It does not device-verify newgraphics. PendingonGalaxy: visual appearance,actual60FPS/framepacing/heat,initialoverviewreadcost andplaybackcontinuity, wave touch/keyboard/a11y, switches/reduced-motion/background/fullscreen/sourcechanges. No hardware/emulatorattached. Bass/circuit are source-data-driven decoration,notBPM/physicalroutingverification. Waveform isfloatdisplaydata,not DACcapture. Actual bitperfectstatus staysNOTVERIFIED.

## BITWIRE v0.4.3 drag seek / A–B repeat preview

2026-10-15: final assembleDebug/testDebugUnitTest/lintDebug BUILD SUCCESSFUL. 45 JVM tests, zero failures/errors/skips; lint No issues found. Added range setup/order/minimum interval rejection, rearming A, active/inactive seek constraints and exclusive B, frame conversion at44.1/48/96kHz with unselected tail frames preserved when repeat is inactive, and source/intent token invalidation cases. Touch-driven engine pause/commit is reviewed in code; no Android UI instrumentation/device test ran here.

Production-native usbfs mocks passed ASan/UBSan (detect_leaks=0, sandbox restriction), with non-packet-aligned starts/ends, a one-frame range, partial final packets, invalid/reversed/overflow bounds, exact selected-subrange reference/payload hashes, and completed/submitted frame counts for stereo16/24/32-bit at48/96kHz from memory, file and pinned validated source. Existing clock/volume/cancellation/failure cases retained. The Java/native JNI endFrameExclusive contract builds for both ABIs. Unexpected incomplete native ranges fail without repeat/queue advance.

Same debug signer as earlier APKs, ZIPalign16KiB and arm64/x86_64 ELF LOAD>=16KiB verified. VersionCode10/name0.4.3-ab-repeat-preview. APK SHA256 `df2d8aa79a14f61f3f86aeb7c5c94dcf2283dbc2e8c2d9914ea968dba09c5c65`. PCM decoder, volume guards, source cache identity/integrity and no-SRC/no-DSP policies retained; USB transport adds an exclusive source range limit.

Pending on Galaxy: touch drag preview/release/cancel and accessibility, paused seek remaining paused, interruption/source switch/replug during gestures, long gesture/service idle recovery, A10s/B15s repeated3+times in Android and supportedUSB48/96kHz, visible markers/counter, pause/resume/clear/new-track reset, background loop continuity, physical boundary gap/clicks and volume continuity. Reinitialization may create gaps; not gapless. No device attached here; kernel/app buffer comparisons do not establish DAC received samples or physical boundary timing. BIT-PERFECT NOT VERIFIED remains unconditional.

## BITWIRE v0.4.4 uninterrupted preview / RMS waveform

2026-10-15: assembleDebug/testDebugUnitTest/lintDebug BUILD SUCCESSFUL.48 JVM tests,0failures/errors/skips;lint No issues found. New/extended waveform cases cover signed16/24/32 input immutability and opposite-phase stereo power, equal full-scale peaks with different RMS body levels, uneven bucket weights, empty buckets, chunk-independent power/count aggregation, silence and fixed display curve/headroom/nonfinite input.

Scrub begin/cancel now read/log preview tokens only: no playback intent invalidation, native cancellation fence, pause, seek or output mutation. Release alone uses the existing guarded seek; source/intent changes still reject stale release. UI shows preview target and ongoing current time. Code reviewed for A/B loop boundaries (same intent) and natural queue/source changes (stale token rejected). No Android instrumentation/device test ran here.

Native source and both packagedlibpurebit_usb.soABIs are unchanged fromv0.4.3 (binary byte comparison passed); native tests were not rerun because transport/volume code is unchanged. Source PCM decoder/hash and buffer verification policies remain unchanged. Display RMS uses separate read-only source analysis, weighted channel sample power and fixed0.9*RMS^1.25; signed min/max peaks remain faint. No playback PCM DSP/gain was introduced.

VersionCode11/name0.4.4-seek-waveform-preview; debug signer matches previous versions, ZIPalign16K andELF LOAD>=16K verified. APK SHA256 `e87921331759a784aec7a7e87b203ff6e819affdef03dfd99387b5e29f1ef3c0`.

Pending on Galaxy: current music continues throughout a5s+drag and cancellation/page/background change, single seek onrelease, paused seek remains paused, stale release aftermanualpause/focusloss/source switch/USBdetach, A/B wrap duringdrag, dense/quiet source waveform contrast and touch accessibility. Release still reinitializes the selected output and may briefly interrupt audio; no gapless claim. Actual60FPS/DAC sample identity remain unverified. BIT-PERFECT NOT VERIFIED retained.

## BITWIRE v0.5.0 Hwahyun theme preview

2026-10-15: final assembleDebug/testDebugUnitTest/lintDebug BUILD SUCCESSFUL.55 JVM tests,0 failures/errors/skips;lint No issues found. Added both theme renderer roundtrips, builtin ID protection, properties escaping/Korean names, schema/renderer/unknown-field rejection, path-like ID/control-character/oversized payload rejection, particle/speed/time bounds and nonfinite values, dark palette/contrast rules, full/short/off intro timeline, and import/roundtrip of the downloadable moonlit example. Initial lint flagged a Typeface style literal; replaced with Typeface.NORMAL and full checks passed.

15 audio/native/PCM core source files matchv0.4.4. Both packaged native libraries matchv0.4.4 byteforbyte. Native transport tests were not rerun because native/audio code is unchanged. Theme/startup code has no audio engine/native transport calls. Activity recreation retains the process-owned engine/service, and previous source UI/transport/volume behavior is retained.

Offline unmodified Nanum Brush Script andAllura font binaries plus originalOFLs are included inAPK assets and source; file bytes checked against downloaded originals. Font cmap covers 김백현 and & Codex. A standalone font specimen was visually inspected; it is not an Android UI screenshot. Hanja labels use platform serif fallback. Handwriting is an ink reveal, not stroke-order reconstruction.

VersionCode12/name0.5.0-hwahyun-preview. Same debug signer as previous versions; ZIPalign16K andELF LOAD>=16K checked. APK SHA256 `71a02fa4e2067b17a53d6bd5b537ee29edfc39fed41708ea83dcb0ed00504aa9`.

No Android device/emulator rendering or FPS test ran here. Pending onGalaxy: Hwahyun scene/layout/album crop/signature legibility;8s full/1.8s short/off and skip/back; intro background/cancel/recreation and animation accessibility settings; preview dialog lifecycle; theme switch while playing inAndroid/USB without audio/volume changes; waveform/A-B/fullscreen palettes; process relaunch selection persistence; import ofmoonlit/invalid file andOriginal restore; sustained60FPS/heat. Intro's final scene shrinks/fades toward the main visual area; actual frame continuity remains unmeasured. Theme v1 is bounded UTF8 properties for preinstalled renderers, not executable assets/new renderers/download market. DAC sample identity staysNOT VERIFIED.

## BITWIRE v0.6.0 Sakura garden full skin preview

2026-10-15 KST: final assembleDebug/testDebugUnitTest/lintDebug BUILD SUCCESSFUL.57 JVM tests,0 failures/errors/skips;lint No issues found. Two new garden motion tests cover finite normalized positions/sizes/opacity over0..7200s across speed limits and96particles, foreground edge placement for central legibility, density cap, deterministic independent layer sampling and hero layout bound. All existing PCM/source/hash/route/USB state/seek token/A-B/theme schema/waveform tests retained. Initial lint constructor warnings were resolved by Context constructors.

15 audio/native/PCM core source files matchv0.5.0. Both packaged libpurebit_usb.so ABIs matchv0.5.0 byteforbyte. Native transport tests were not rerun because transport/engine/volume source and binaries are unchanged. Library artwork adds stale-request predicates before media reads and before delivery; source PCM paths are not changed.

Bundled generated garden/moonPNG assets matchAPK bytes and have no game character/extracted footage. Original UI uses its existing player path; Hwahyun uses a separate player arrangement, full-window background/foreground decoration, vector controls/flower switches/silk seek/tabs,20-track cover grid and queue rail. Native widget touch/keyboard/accessibility semantics are retained in code. Global decorative views cannot consume touch or expose accessibility nodes; frame callbacks are lifecycle-managed. Shared immutable artwork is decoded once per process and blurred foreground sprites are prepared outside frame rendering.

VersionCode13/name0.6.0-sakura-garden-preview. Same debug signer as previous versions; ZIPalign16K andELF LOAD>=16K verified. APK SHA256 `70e05fc8accf597923aebd7f066c96aedd06ea95d04d677e22fff693a46952a9`.

No Android instrumentation/emulator/physical Galaxy rendering ran here. Source concept and generated asset previews are not app screenshots. Pending: actual composition/text contrast/fontscale/rotation/scroll reach,8s intro and moon handoff/skip, header-to-navigation petals and touch pass-through, single seek onrelease while music continues duringdrag, A-B expanded tools/state, hardware volume and route failure message visibility, cover-grid/search/paging/reorder and stale artwork, theme switching/Original restore, screenoff/background and real60FPS/framepacing/heat. Raster art + live Canvas layers create depth; no claim of3D game scene or pixel-identical reproduction ofconcept. DAC received samples remainNOT VERIFIED.

### v0.6.1 · Moon glow

assembleDebug, 57 JVM tests (0 failures/errors/skips), lintDebug passed. Same signer and ZIP 16K alignment verified. Both native libraries are byte-identical to v0.6.0; audio/core/native sources unchanged. Short empty-state text, 218/255 album alpha and layered pink bass halo are UI-only. New rendering/contrast/animation strength require phone validation; no measured FPS or device sample verification claimed. APK SHA256 `7f67e7ecb317d97ac6a57b11acda984b68abf88ae3e3f4fcb8c3002350f6a7af`.

### v0.6.2 · Pink halo

assembleDebug and lintDebug passed. UI-only shader/stroke change; unit tests not rerun. Audio/core/native sources and native binaries match v0.6.1. Same debug signer and ZIP16K alignment verified. Cached radial gradient adds a softer wider pink halo with 1.7–2.6dp core stroke using existing bass/motion settings. Actual phone appearance/FPS remains to be checked. APK SHA256 `1ee533bf3362098d1459ac84eec97c5af22148121ab7ab1f7585f9ea194fd47c`.

### v0.6.3 · Flower immersion

Final assembleDebug and lintDebug passed. UI-only change; unit tests not rerun. Audio/core/native sources and both native binaries match v0.6.2. Same debug signer and ZIP16K alignment verified. Hwahyun uses a bounded 1.45s flower sweep on new artwork, with no particles when motion is disabled or petal density is zero. Album single/accessibility click and double tap enter fullscreen garden; double tap/back/explicit exit button leave it. Instructions fade with motion enabled and can be shown by single tap. Delayed gesture callbacks ignore detached views. Stale artwork completion is fenced by current artwork token. Android rendering, double-tap/scroll arbitration, TalkBack, reduced motion, back/exit/background/track changes and real FPS need physical testing. ORIGINAL retains its fullscreen layout. No DAC sample verification claim. APK SHA256 `fc43cccf223eebaab1aae8b43be8a8cd7e2759bb084fa54d4a2444eb3029fe37`.

### v0.6.4 · Moon resonance

assembleDebug, 60 JVM tests (0 failures/errors/skips), and lintDebug passed. New tests cover gray/transparent artwork fallback, bounded dominant tint despite dark borders, and monotonic playback visual envelope/reduced motion. 12×12 sampling occurs on artwork/settings changes; radial shaders are rebuilt only for a different selected color. Moon/halo tint crossfades over 0.9s, preserving 72% theme accent. Stronger source-spectrum strings gain trailing light dots, and play/pause gently adjusts string activity and petal opacity without changing petal travel speed. Static reduced-motion render skips moving strings; original theme drawing unchanged. Audio/core/native sources and both native binaries match v0.6.3; same debug signer and ZIP16K alignment verified. Physical rendering/performance and hardware playback regression not tested here. Pending phone checks: colored/monochrome/no-cover tracks, palette switch on/off, pause/resume, reduced motion, normal/fullscreen and source changes. Device path samples remain unverified. APK SHA256 `d2f442a35ed43223651cd1878c3e4ce909a0d191dd85b1d3728b2db015f84fab`.

### v0.7.0 · Album studio

Final assembleDebug, 66 JVM tests (0 failures/errors/skips), and lintDebug passed. New tests cover album grouping/compilations/disc-track ordering/legacy untagged catalogues, queue move/delete/current protection/play-next uniqueness and repeated edit invariants, and intro preference fallback/scene duration bounds/Original preservation.

Album catalogue fields are optional additions to v1 JSON: albumArtist, trackNumber, discNumber. Rescan needed for previously imported tags. Album views use bounded pagination, epoch-fenced asynchronous artwork and native text/buttons. Queue edits use stable IDs, preserve current selection, reject stale native drag revisions, auto-scroll at viewport edges, and protect current track from deletion. Explicit next requests take priority over shuffle; manual reordering clears that preference. Queue is still process-local; no automatic playback restoration introduced. Swipe commits only after horizontal intent/threshold; cancel/multitouch do not delete. Buttons provide an alternative to gestures.

MediaSession now publishes matching source tags and bounded 256px artwork, fences stale results and avoids repeated metadata bitmap publishing. Previous/play-pause/next/stop/seek and USB relative hardware-volume callbacks remain. Healthy paused source gets up to 5min notification grace without wake lock; process death remains START_NOT_STICKY. Bundled intro scenes add selection/static thumbnails/actual preview while retaining silent signature, short/off/skip and reduced-motion behavior.

Same debug signer and ZIP16K alignment verified. Both native binaries are byte-identical to v0.6.4. Decoder/core/transport sources unchanged; audio PlaybackService changes concern presentation/lifecycle only. No physical phone/emulator UI or lockscreen verification ran here. Pending: album cover/track metadata and navigation, drag/drop/swipe cancellation/edge scroll/current protection, shuffle/play-next/queue-end behavior, lockscreen art/buttons/seek/volume/pause retention, scene selection across new process/preview/skip/short/off, Original restore, USB48/96k/reconnect and A-B regression. OS controls notification appearance; no actual60FPS/device bit-perfect guarantee added.

An initial final-build request did not execute because automatic approval review timed out. The explicitly permitted retry completed successfully; no outstanding approval block. APK SHA256 `d741f94f630367c76a89f53a9087ee20fc3edc27389835b9f6d504d6808a353e`.

Final warning cleanup: programmatic custom views provide Context constructors for Android tooling. The swipe-only touch override documents its scoped accessibility lint suppression; native child clicks and explicit remove/move buttons remain. Clean rebuild: 66 tests pass and lint reports No issues found.

### v0.8.0 · Moonlit observatory

assembleDebug, 71 JVM tests (0 failures/errors/skips), and lintDebug (No issues found) passed. Five new LRC tests cover UTF-8/BOM, multiple cues and translations, exact cue boundaries/blank cues/seek-back, signed offset semantics, fraction precision/sort order, literal timestamp text, malformed data and input/cue/line limits. Manual installed-device UI rendering was not run here.

Mini transport uses the existing process playback commands and artwork identity/token. Landscape Hwahyun is a separate Canvas composition with mirrored clipped artwork bands, bounded source-bass ripple amplitude, readable native controls and system insets; original deck rendering is preserved. Activity recreation reuses PlaybackRuntime. Automatic landscape entry/dismissal and manual fullscreen/lyric overlays are saved/restored; system rotation lock is respected. No force-orientation manifest flags or new permissions.

LRC imports are bounded strict UTF-8, validated before atomic private-copy replacement, keyed by SHA256 of selected source URI. File-picker target survives recreation. Async loads use generation/source fences and store callbacks stop after Activity ownership closes. Removing an association deletes only the app-private copy. Line display follows reported position at existing200ms UI poll and does not claim DAC clock synchronization/word-level timing. Text animation respects reduced motion and touch exploration.

Same debug signer and ZIP16K alignment verified. Both native libraries are byte-identical to v0.7.0. Audio/core/native/PlaybackQueue sources are unchanged, including output formats, hardware volume, service/MediaSession, seek and A-B logic. No bit-perfect verification scope change. Pending physical checks: mini transport on non-player pages and artwork changes, portrait↔landscape playback continuity/manual-back/auto-off/rotation lock/insets/large-font scroll, water performance/visibility and reduced-motion behavior, LRC import/reconnect/restart/remove/seek/A-B/source changes while picker open, and existing USB48/96k/reconnect/hardware volume/lockscreen behavior. No actual FPS or DAC received samples are verified.

VersionCode19, versionName0.8.0-moonlit-observatory-preview. APK SHA256 `9b4de62a169767cd6ad2b7f74fe93b6d42bad9c1f9f49e166130136fca4e52fe`.

### v0.8.1 · Embedded lyrics

Final assembleDebug, 79 JVM tests (0 failures/errors/skips), and lintDebug (No issues found) passed. Eight embedded-tag tests cover Unicode/case-insensitive FLAC comments, timed-over-plain selection, ID3v2.3/v2.4 USLT UTF-8/UTF-16, millisecond SYLT and rejection of MPEG-frame timing, WAV INFO/ILYR and ID3-prefixed FLAC, malformed lengths/missing tags/terminators, v2.3 unsynchronization/Latin1, and fast skipping a synthetic32MiB sparse WAV data chunk versus bounded failure on a nonseekable source. Existing LRC tests continue to pass. Synthetic tag fixtures test metadata parsing only, not audio decoding/DAC receipt.

LyricStore checks an explicitly attached private LRC first; otherwise reads the selected source's metadata in its existing worker. Audio files are never modified. Plain lyrics use a selectable native scroll view with no invented timing; synchronized content retains existing playhead-following behavior. Untimed landscape display links to the full sheet. External-LRC removal reloads embedded tags and cannot remove embedded text. Tag origins/cue counts/plain-character counts and failures enter PureBitAudio/app diagnostics without lyric body text. Bounded tags/reads/text and source/generation fences remain. Unsupported metadata is labeled undetected rather than proof of no lyrics.

Same debug signer and ZIP16K alignment verified. Audio/core/native/PlaybackQueue sources and both native binaries are byte-identical to v0.8.0. No new audio processing, permission, internet request, or bit-perfect claim. Actual user files/Galaxy lyric display were not available for verification: pending FLAC/WAV tag variants, long untimed scrolling, timed seek/A-B/source switches, external override/removal/reopen, and USB playback regression. See README for unsupported tag/encoding/stream-provider cases.

VersionCode20/name0.8.1-embedded-lyrics-preview. APK SHA256 `14584f9d10ae6a66daca0c4fdc4e4369a846109b2513f11b72cf4efb674853b2`.

### v0.8.2 · Edge to edge

assembleDebug and lintDebug passed. UI-only change; unit tests not rerun. Full-window edge-to-edge and cutout ALWAYS are explicit for API31+; status/navigation bar colors transparent with contrast enforcement disabled and light icons retained. Background/fullscreen DeckView no longer receive system/cutout padding. Only a separate non-clickable chrome container reserves safe insets for exit controls, hint and landscape scroll panel. Panel width follows the safe container width; normal shell and lyric widgets keep their existing safe insets. Startup skip hint avoids bottom bars. No system bars are hidden.

Same debug signer and ZIP16K alignment verified. Both native binaries and audio/core/USB/lyrics/queue sources match v0.8.1. Temporary /tmp build helper was absent in the resumed environment; used the persisted Gradle9.4.1/JDK21/cache/ANDROID_USER_HOME directly and built offline. No outstanding build/approval block. Physical Galaxy screenshots not reproduced here: verify camera-edge fill in both landscape rotations, transparent status/navigation regions in gesture and3-button modes, safe controls/text/cutout handling, portrait/library/lyric/intro overlays and rotation while USB music continues. Samsung/Good Lock can impose its own system-bar contrast policy; no claim to override OEM policy or fill physical camera hardware.

VersionCode21/name0.8.2-edge-to-edge-preview. APK SHA256 `17048b4142c778321b19518112c2f6cd4da6f439f7f2d3758755fb5bec9ac086`.

### v0.8.3 · Startup recovery

User reports v0.8.2 fails to launch even after Samsung's clear-cache prompt. No device exception stack supplied; the exact cause is NOT confirmed and no local emulator/physical launch reproduced the failure. This is a conservative recovery patch, not a proven root-cause diagnosis.

Removed v0.8.2 startup window-theme attributes and pre-content window calls. Optional appearance defaults OFF (new preference), uses a posted callback after content creation, applies legacy decor-fitting/color changes only belowAPI35, and catches failures/disables the option. Chrome sizing waits for a measured width, checks layoutParams and resizes only when container width changes; no metrics fallback/repeated unconditional layout callback. Controls/background separation retained.

Application installs a bounded private uncaught-exception recorder before Activity creation, then delegates normal fatal handling. Subsequent launches disable optional appearance after an uncaught exception. OOM avoids stack formatting; StackOverflow can be recorded. No audio restart or user-library reset. Diagnostics explicitly query own-app OS exit descriptions in a worker and expose a share button; v0.8.2 Java stacks may be unavailable. No upload or claim of recovered old stacks. Existing self-diagnostics append this record.

Final assembleDebug,79 JVM tests (0 failures/errors/skips), and lintDebug (No issues found) passed. Tests cover existing core/tag logic and do not exercise real Android startup/window behavior. Same debug signer and ZIP16K alignment verified; both native binaries and audio/core/USB/lyrics/PlaybackQueue source match v0.8.2. Pending essential phone check: update without uninstall/data clearing, launch portrait and landscape with appearance OFF, navigate/play/lyrics, export previous crash history, then optional appearance ON if desired. Recovery on Galaxy remains unverified until user tests it.

VersionCode22/name0.8.3-startup-recovery-preview. APK SHA256 `506d66a7f3f666cdf0ced0e6ec644c94848a8a7e1f03b9fd2fe52839abea4fbd`.


### v0.8.4 startup recovery console
Samsung generic crash dialog does not identify the cause. Root cause remains unconfirmed. This diagnostic build intentionally opens an independent native recovery Activity first, without the audio engine or theme assets. Copy/share the persisted previous crash report from this screen, then try the player with the selected theme or a temporary ORIGINAL theme with intro and visualizer effects disabled. Theme preferences and library data are preserved. Crash stage markers identify player construction steps. No device launch verification is claimed; a device report is still required. Audio/native playback code is unchanged.


## v0.9.0 garden focus / startup safety

- Final command: `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug --offline`, JDK21/Gradle9.4.1/SDK37. BUILD SUCCESSFUL (1m31s); JVM82 passed/0 failures/0 errors/0 skipped; lint No issues found.
- Three new StartupPolicy tests cover normal/safe/console routing, ordinary removal/memory kill exclusion, and PID/time matching for previous startup crash evidence. These are policy tests, not Android Activity/SharedPreferences/device integration tests.
- apksigner verified the same debug signer SHA-256 `a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6`; zipalign16KiB check passed. Package app.purebit, versionCode24. APK SHA-256 `83e34084fcd03314930f06e97cf52765c90d40156377e963850e11e08f005497`.
- Compared25 audio/core/native/lyrics/PlaybackQueue source files with v0.8.3: unchanged. Both packaged libpurebit_usb.so binaries are byte-identical. No decoder, USB packet, software volume, resampler, lyric parser or audio crossfade changes.
- APK: `/workspace/artifacts/BITWIRE-v0.9.0-garden-focus-debug.apk`. Build log: `/tmp/purebit-research/build-0.9.0-final.log`.
- No Galaxy/emulator attached. No claim of successful Activity launch, measured60FPS, OS crash recovery, hardware volume, or bit-perfect physical path from this build alone. Historical0.8.2/0.8.3 startup failure remains unconfirmed.

### Galaxy checks

1. Install as an update, preserve data; open from both home icon and app drawer; close/reopen and rotate during/after intro. Optional system-bar effect stays defaultOFF.
2. Settings → startup recovery → recovery console → temporary ORIGINAL: confirm no intro/visualizer and preserved library/selected theme. Use settings → retry saved theme to return. This tests manual fallback without deliberately crashing the app; automatic crash detection still requires device evidence.
3. Play two tracks with different/no covers, switch rapidly and check final cover and petal transition; audio must remain as before. Try additional petal-wind switch OFF and reduced motion.
4. Open embedded/LRC lyrics → focus; portrait/landscape, no lyrics, plain text scroll, paused playback; wait4.5s, first tap reveals controls without transport changes, then use playback buttons. Back, rotation, screen-off/return must work. Touch exploration should retain controls.
5. Preview both themes with current cover, close without applying, preview opening, explicitly apply and return ORIGINAL; test USB/Android output, seek release and A–B as before. Export app fault report if any new crash occurs.


## v0.10.0 night / favourites / resume

- Final tasks: `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug --offline`, BUILD SUCCESSFUL in1m26s. JVM87 passed, zero failed/errors/skipped. Lint: No issues found. Existing Notification.Action.Builder deprecation warnings remain; no new dependency or network runtime permission.
- New ResumeRules tests cover revalidated duration/EOF/negative/overflow positions and diagnostic/loading/non-current-source exclusion. New NightProgress tests cover monotonic bounds, paused/reduced-motion/large-delta exclusion, fixed modes and preview expiry. No Android SharedPreferences/Activity/device integration test is claimed.
- Same package app.purebit, versionCode25, signer SHA-256 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6. apksigner and16KiB zipalign passed. Both packaged USB .so binaries are identical to0.9.0.
- AudioEngine change is an optional resumePositionMs applied after decoding/source verification and before play; output/backend/decoder/PCM/native implementation unchanged. PlaybackService/PlaybackQueue add bookmark persistence and explicitly requested resume.22 other audio/core/native/lyric source files are unchanged from0.9.0.
- APK `BITWIRE-v0.10.0-night-memory-debug.apk` SHA-256 `d18b5a88db11055a81a2aec7d7903725ec26b0d608dbeb6913a1103429fb8c92`. Build log `/tmp/purebit-research/build-0.10.0-final.log`.
- No connected Galaxy/emulator. Cold process restore, persisted URI grant handling, foreground-service resume, favourites persistence, actual frame timing/night appearance and screen rotation still require device testing. No stronger bit-perfect evidence is introduced.

### Manual checks on Galaxy

1. Update without uninstall. Play30s, pause, wait1s for service checkpoint, use Android app info → force stop (do not clear data), reopen. Last track/time should display silently; Play revalidates PCM and resumes within checkpoint granularity. Repeat with background playback, EOF, a changed/shortened source, USB mode explicitly reselected, and revoked SAF permission. Safe-mode cold launch must skip bookmark restoration.
2. While loading restored audio, cancel; rapidly select another song. No stale decode should start playback. Seek/A–B/volume and source integrity remain as before. Test diagnostic generated tones/comparison mode and verify they do not replace the previous music bookmark.
3. Star/unstar from player and long-press, search favourite-only view, play its filtered queue, remove final star, rotate/update/restart and check persistence. Missing items stay represented in catalogue and can be unstarred.
4. Hwahyun settings → deep-night10s preview, fixed warm/dusk/deep and AUTO. Listen in foreground12min, pause/background/reduced-motion to check the timeline pauses; rotate and change tracks without resetting; restart process for a fresh automatic night. Night is decorative, not hardware/output verification.


## v0.11.0 library browser

- Final tasks `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug --offline`: BUILD SUCCESSFUL in1m29s. JVM93 passed/zero failed/errors/skipped. Lint No issues found. Existing Notification.Action.Builder deprecation compiler warnings remain.
- Six new LibraryBrowser tests cover multiword cross-tag/path search, NFC/case artist grouping, distinct same-name folder roots/unknown parents, unknown-duration ordering in both directions and immutable input, disc/track deterministic ordering, and page clamps for empty/shrinking/layout-changing results. Android SAF/cursor/AtomicFile/Activity scroll restoration are not covered by these pure JVM tests.
- Same applicationId app.purebit, versionCode26. apksigner verified same SHA-256 certificate a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6;16KiB zipalign passed. Both packaged native USB libraries are byte-identical to0.10.0.25 audio/core/native/lyrics/PlaybackQueue source files are unchanged.
- Catalogue fields folder/folderPath are additive and old records retain defaults; rescan collects provider folder labels/parent document identity. Ordinary add/rescan commits roll back shared snapshot/tree list on save failure and AtomicFile protects the disk catalogue. Root permission denial intentionally marks records unavailable as before.16MiB metadata is checked before opening a new AtomicFile write; path labels are limited to512 characters.
- APK BITWIRE-v0.11.0-library-browser-debug.apk, SHA-256 111877a8a97c3545fd7c49e93017972415de34a87f2fbc26a8ae4e3ae4ab3693. Build log /tmp/purebit-research/build-0.11.0-final.log.
- No attached Android device or emulator: no claim of measured frame rate, successful Samsung Activity launch/SAF migration, keyboard/rotation/scroll behaviour or physical bit-perfect output. Previous startup crash remains unconfirmed; safety mechanism retained.

### Galaxy manual checks

1. Update over0.10.0 without uninstall. Existing files/favourites/resume/theme must remain. Reopen, rotate during and after intro, confirm safe-mode recovery access.
2. Library → rescan folders once. Nested folders should show provider names; same names in separate roots remain distinct. Individually added files show unknown parent rather than guessed filesystem paths. Test revoked grant/deleted file, missing tags and old catalogue defaults.
3. All tracks/artist/folder detail: change sort/direction, multiword search, select a song and verify following queue uses that displayed order. Sorting by itself must not replace current queue or interrupt music. Albums retain disc/track order; recent keeps history order; favourites remain editable.
4. Open a later page and scroll; select song and return, enter group/back, rotate and return from background. Search/group/page/scroll should restore within the Activity task. Force stop/new cold launch retains mode/sort preferences, not permanent query/scroll history.
5. Try empty and93+ track pages, large catalogue, long folder names/large fonts, IME and both themes; check scrolling and artwork cancellation. Verify original USB/Android playback, volume, seek release/A–B/lyrics/night/resume still work.


## v0.12.0 saved playlists / named queue snapshots

- Final tasks `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug --offline`: BUILD SUCCESSFUL in1m37s. JVM99 passed/0 failures/0 errors/0 skipped. Lint: No issues found. New selection-count string uses a formatted Android string resource.
- Six new PlaylistTest cases exercise UTF-8/Korean/Japanese name and empty/order/revision round-trip, truncated/modified payload rejection, invalid encoding/version/size, invalid names/IDs and duplicate entries, stable order/deduplicated appends and stale revision rejection,10,000-reference bound, and queue append preserving existing order. These are JVM policy/codec tests; Android AtomicFile/Activity/ListView and transport integration are not device-tested here.
- Same applicationId app.purebit, versionCode27; same signer SHA-256 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6. apksigner verified,16KiB zipalign passed. Both native USB .so binaries are byte-identical to0.11.0, and24 audio/core/native/lyrics source files are unchanged. Queue edits add append/keep-current and distinguish fresh pending queue from restored bookmark; no edit calls audio load/play. Explicit play uses the existing service/engine pathway.
- Store uses application filesDir, a single process worker, AtomicFile, bounded strict UTF-8 plus count/SHA-256, UUID filenames, global limits and revision checks. IDs missing from library are retained in saved files and shown as inaccessible. Playlist metadata checksum provides no new PCM/device verification evidence.
- Root playlist-name search is separated from detail song search and parent query/scroll is restored on back. Programmatic query updates do not enqueue stale search resets. Saves complete after Activity recreation, but unconfirmed dialog choices may close on rotation. Corrupt files are kept and counted; no automatic repair/deletion is claimed.
- APK `BITWIRE-v0.12.0-playlists-debug.apk` SHA-256 `2547a52abf5596a4a958356dfb6094937db125f814a399b0880d20196b8b9754`. Build log `/tmp/purebit-research/build-0.12.0-release.log`.
- No connected Galaxy/emulator. No claim of successful hardware startup, persisted-files lifecycle integration, actual UI frame rate, playback continuity, USB compatibility or physical bit-perfect proof from this build alone. Previous crash root cause remains unconfirmed and startup recovery is retained.

### Galaxy manual checks

1. Install as update without data clearing. In Library → playlists create a named empty list; add several songs with search/multiselect and result selection; verify duplicate names/duplicate songs are handled clearly. Search a list name, open it, search a song, back/rotate and confirm the name query/scroll returns.
2. Reorder/remove/rename; close and reopen app and check saved order. Repeat quick taps and rotate during a confirmed save; completed writes should persist and stale edits should reload rather than overwrite newer content. Unconfirmed dialog selection is not promised to survive rotation. Try landscape/IME/large fonts in both themes.
3. While music plays, add saved tracks to queue and keep-current; current position/device/volume should remain unchanged. Named queue save must not start/stop audio. Explicit playlist Play should start available tracks in saved order; repeats/shuffle/A–B retain existing engine behaviour. Generated diagnostic sources must not auto-advance into queued music.
4. Save a queue by name, reopen it and compare order. Saved lists and active queue are independent snapshots. Check cold bookmark resume remains as before. If a fresh empty session is available, appending sets QUEUE READY silently until Play; do not clear existing user data to create this case.
5. Revoke/remove a file then rescan; its saved reference remains visible, playback skips unavailable references, and regrant/rescan with the same URI reconnects it. Confirm deleting a saved list leaves source files and active queue intact. Damaged store files should remain preserved and counted rather than silently replaced.


## v0.12.1 lifecycle / playlist stability

- Confirmed by code review: cancelling the 250ms search onStop could leave results out of sync with the query onStart; playlist writes survived recreation but the replacement Activity had no notification of completion; a write callback invoked its old Activity completion before checking destruction; an async add-to-playlist chooser could appear after navigation.
- Fixes: visible-library reload onStart, pending-search-aware scroll capture, stopped/hidden search cancellation; process-wide successful-write revision with one in-flight revision read, lifecycle/generation checks and stale-dialog suppression; destroyed/finishing UI callback rejection and Handler cleanup. Existing explicit-save worker/AtomicFile/revision checks remain. No audio transport or queue logic changes.
- Three RevisionRefresh tests cover mutation during a read (must refresh again), stale completion while a newer read is pending, and cancellation/reentry without acknowledging undisplayed data. Existing playlist/queue/audio/format tests remain. These are JVM tests, not Android Activity/AtomicFile integration tests.
- No Galaxy/emulator execution is claimed. Startup crash root cause from0.8.2 is still unknown; no crash is deliberately induced, and recovery/data-preservation behavior is retained. Unconfirmed dialog input can still close on rotation; process death does not guarantee completion of an in-flight save. No new device-path or bit-perfect proof.

### Galaxy regression checks

1. Update over0.12.0 without uninstall or data clearing. Both themes: launch/intro, rotate during and after intro, background/return, reopen, and enter startup-recovery console. Check existing library/favourites/last-track/playlist data.
2. Type a search and immediately go Home, return; repeat while scrolled and switch tabs immediately after typing. Query/results must agree, with the new search starting at the beginning rather than retaining the old results’ scroll. Repeat in track, artist/folder and playlist views.
3. Confirm a playlist create/rename/reorder/add, immediately rotate or go Home/return; verify completed changes appear without restarting. Reopen saved list to compare order. While music plays repeat these operations; UI edits must not restart playback or change volume/output. Input not yet confirmed may close on rotation.
4. Request add-to-playlist, then immediately switch page/background or request another chooser. No older chooser should unexpectedly appear over the new screen. Tap edits rapidly: stale revision rejection must preserve newer saved data.
5. Repeat Android and USB48/96k music play/pause/seek-release/A–B, screen lock/return and USB unplug/reconnect using existing safe volume practice. Confirm reconnect requires explicit playback and no automatic fallback/resampling. Export diagnostics if a new fault occurs; successful compilation/JVM tests do not prove Samsung lifecycle or output behavior.

### Final build evidence

- Final offline tasks `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug`: BUILD SUCCESSFUL in1m40s;102 JVM tests,0 failures/errors/skips. Lint: No issues found. Log `/tmp/purebit-research/build-0.12.1-final.log`.
- Package app.purebit, versionCode28/versionName0.12.1-stability-preview. apksigner verification and16KiB zipalign passed; unchanged certificate SHA-256 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6. Both packaged native USB libraries are byte-identical to0.12.0. Audio/core/native/lyrics/PlaybackQueue sources are unchanged from the0.12.0 source artifact.
- APK `BITWIRE-v0.12.1-stability-debug.apk`, SHA-256 `b52a62a030277b241f4ec88b6b3966d3267c0e38490b8476e149b7d244e1252b`. Source bundle `BITWIRE-v0.12.1-source.zip`. No device/emulator run or end-to-end audio verification is represented by these checks.


## v0.12.2 transport command stability

- Code-review findings: repeated toggles used a worker-published playing/preparing state rather than the immediately updated play intent; duplicate PLAY invalidated pending work; posted Service/MediaSession NEXT/PREVIOUS could execute after a subsequent PAUSE/STOP; PREVIOUS could use an outgoing track’s position after queue selection changed; seek during decode invalidated auto-play before discovering no PCM was available.
- Changes: logical-intent toggle and idempotent PLAY, close-before-reopen when a superseded toggle pause leaves an active output, inline main-thread skip dispatch (off-main fallback remains posted), one-snapshot/current-source-gated previous action, and seek-readiness check before intent cancellation. Natural EOF validation, focus/route safety, explicit USB re-entry and existing source/output integrity labeling remain.
- New pure regression cases: five QueueNavigation tests (outgoing position after fast skips,3-second boundary, cold bookmark, wrap/single track, invalid selection); two AudioState tests (unpublished source generation/loading rejection, decoded paused source accepted). These cover decisions/state only. Actual rapid TOGGLE/duplicate PLAY scheduling, Android MediaSession ordering and hardware reopen require device tests; JVM tests are not presented as that evidence.
- No changes to PCM decoder, output backends, USB native implementation, software gain/SRC or hardware-volume validation. No new device sample verification or stronger bit-perfect claim. Previous startup-crash root cause remains unconfirmed.

### Galaxy regression checks

1. Update without uninstall/clearing data. In both themes play/pause repeatedly, including two quick toggles from playing and from paused. Wait for output preparation before assessing the final state. During loading the app UI remains an explicit cancel control as before. USB preparation may take time; do not increase volume to compensate for a temporary gap.
2. Play A beyond3seconds, select B then immediately PREVIOUS before B is ready. It should choose A instead of seeking the outgoing stream and leaving B silently prepared. Repeat NEXT→NEXT→PREVIOUS, first/last queue entry and one-track queue. On a prepared current song, PREVIOUS after3seconds seeks its start; at/before3seconds chooses the previous entry.
3. Notification/lockscreen: NEXT then immediately PAUSE or STOP. The later pause/stop should prevail. Explicitly PLAY again, including after paused service timeout, background and screen-off. On a controller that can issue PLAY repeatedly, check pending decode/USB preparation/playing seek completes without repeated cancellation. No automated MediaSession integration test is claimed.
4. During large-file decoding try lockscreen seek; it should be ignored and the requested decode/play should finish. After preparation, seek works normally. While playing drag repeatedly: audio continues at the old position until release, then resumes at the target. Verify paused seek, A–B, EOF/repeat/shuffle and cold bookmark PREVIOUS still work.
5. Android and native USB48/96k: volume safety, playback during rotation, screen lock/return, disconnect/reconnect with explicit PLAY. Check diagnostic transport_request/seek_ignored records if anything fails. Existing44.1k native limitation and no automatic resampling/fallback remain.

### Final build evidence

- Offline `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug`: BUILD SUCCESSFUL in1m30s.109 JVM tests,0 failures/errors/skips. Lint: No issues found. Final build log `/tmp/purebit-research/build-0.12.2-final.log`.
- Same app.purebit, versionCode29/versionName0.12.2-transport-stability-preview. apksigner and16KiB zipalign passed; same signer SHA-256 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6. Both packaged native USB libraries are identical to0.12.1. Existing core/native/output-backend sources are unchanged; AudioEngine command handling and queue/runtime control changed as described above.
- APK `BITWIRE-v0.12.2-transport-stability-debug.apk`, SHA-256 `8d6d29514252175012c0d5b725156c3ba50253105e0e38bd0381b271214386ad`; source bundle `BITWIRE-v0.12.2-source.zip`. No Android device/emulator execution, measured rapid-control latency, MediaSession/foreground-service integration success or device sample identity is claimed by these build/JVM checks.


## v0.12.3 bounded metadata / library reconciliation

- Findings: library load checked baseFile.exists before AtomicFile.openRead, skipping legacy backup-only recovery; base-file length did not bound the recovered stream; capped folder scans marked all unvisited prior tracks inaccessible; library writes used prefix limits that could silently omit excess catalogue/tree entries; orphan playlist .new files counted as saved/corrupt lists.
- Fixes: invoke AtomicFile.openRead unconditionally (missing fresh-install file is normal); read opened metadata with strict byte bounds and strict UTF-8; preserve last-known availability of unvisited entries only for truncated scans; reject over-capacity saves before opening a write, relying on existing snapshot rollback; discover committed base/legacy backup playlist names while leaving uncommitted .new files untouched.
- Six MetadataInput tests cover exact/empty bound with misleading available(), one-byte oversize detection without draining input, zero-byte bulk reads, IOException propagation rather than partial success, Unicode/invalid UTF-8 and committed/backup/temp filename classification. Three LibraryRules cases cover full own-tree reconciliation, truncated scans retaining both available/unavailable prior records while applying new observations, and immutable inputs/stable identity.
- These are JVM stream/decision tests. They do not instantiate Android AtomicFile, SAF providers or the Activity. No backup-only recovery/commit rollback integration test, physical device run, large-catalogue latency measurement or stronger bit-perfect proof is claimed. Arbitrarily corrupted catalogue repair and process-death completion guarantees remain outside scope.

### Galaxy checks

1. Update over0.12.2 without clearing data. Reopen normally and verify library count, favourites, saved playlists, selected theme and last track. Scan an existing music folder again, reopen and compare count/metadata. The normal full scan behavior must remain as before.
2. Save/rename/add songs to a playlist; rotate, return from Home and restart app, comparing stored name/order. Confirm ordinary edits and saved-list playback continue working, including the0.12.1 lifecycle corrections.
3. If a provider grant was already lost, reselect the same folder and rescan; available files should be observed again and saved references should reconnect by URI. Do not revoke personal-data grants or corrupt private files just to exercise a test.
4. Large libraries: a capped scan warns about its limit and leaves unvisited records at their last-known availability. This is not current-file verification. Over10,000 total catalogue entries or64 selected roots must reject the change and keep prior state;16MiB metadata limit also applies. Only use a separate test library for capacity testing, not destructive changes to the main collection.
5. Backup-only/oversize-backup/temp-file integration needs an isolated Android test installation. No user instructions to modify private files or interrupt writes are required. Android/USB48/96k playback, hardware volume, seek-release/A–B, screen lock and reconnect remain normal regression checks.

### Final build evidence

- Offline `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug`: BUILD SUCCESSFUL in1m51s.118 JVM tests/0 failures/0 errors/0 skipped. Lint: No issues found. Build log `/tmp/purebit-research/build-0.12.3.log`.
- Package app.purebit/versionCode30/versionName0.12.3-storage-stability-preview; same signer SHA-256 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6. apksigner and16KiB zipalign passed. Both packaged native USB .so binaries and24 audio/core/native/theme source files are identical to0.12.2.
- APK `BITWIRE-v0.12.3-storage-stability-debug.apk`, SHA-256 `0ede158fc98fb134c1e2d0b2cc7a95ac9883b9554dccc95f30a21923e73e9c97`. Source `BITWIRE-v0.12.3-source.zip`. No Android device/emulator integration execution or end-to-end bit-perfect verification is represented by these checks.


## v1.0.0-rc1 integrated stability

- Scope: explicit notification PLAY/PAUSE/cancel-preparation actions; Android becoming-noisy interruption distinct from native USB handoff; fixed MediaSession volume route when native hardware readback is absent; stopped-service Handler/volume callback cleanup; existing Activity reuse and Main-owned startup marker; serialized LRC file lifecycle with backup-aware bounded read; IME-aware main chrome, stopped lyric animation reset and foreground/page guards around delayed diagnostic actions.
- Existing focus-loss pause/no auto-resume and physical USB-detach guards were reviewed and retained. Decoder/PCM processing/output backends/native USB/hardware-volume implementation were not rewritten. AudioEngine adds only interruption control. No new dependency/network permission or automatic resampling/software gain was added.
- StoreLifetime tests use latches/futures and temporary files to verify close waits for an in-flight transaction, delayed provider work cannot commit after owner closure, shared-file operations serialize across independent owners, and no enqueue occurs after close. These are actual JVM concurrency tests of the gate used by LyricStore, not Android AtomicFile/provider integration tests.
- No emulator image or device was used. No claim of measured FPS/memory improvement, successful Samsung cold launch/Activity stack/IME/MediaSession/foreground-service teardown integration or DAC sample identity. The RC matrix in RC_CHECKLIST.md remains device-pending. The0.8.2 startup crash root cause is still unconfirmed.
- First compilation found a nested method boundary and a local/property name collision introduced during this patch; both were corrected before the final build. No runtime-fault root cause is inferred from those development-time errors.

### Final RC build evidence

- Offline `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug`: BUILD SUCCESSFUL in2m3s.121 JVM tests,0 failures/errors/skips. Lint: No issues found. Final log `/tmp/purebit-research/build-1.0.0-rc1-final.log`.
- Same app.purebit, versionCode31/versionName1.0.0-rc1. apksigner verification and16KiB zipalign passed; certificate SHA-256 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6. Both USB .so binaries and15 core/native/backend source files are identical to0.12.3. Manifest contains no INTERNET permission.
- APK `BITWIRE-v1.0.0-rc1-debug.apk`, SHA-256 `63177bd3728620ca4e8e3b05405db3bb664dca307703d3a718ceaef91db71a7c`. Source `BITWIRE-v1.0.0-rc1-source.zip`; device checklist `RC_CHECKLIST.md`. All physical-device matrix entries remain pending; this is a release candidate, not a claim of complete Samsung runtime validation or verified bit-perfect output.


## v1.0.0-rc2 queue / shuffle reconciliation

- Findings: applyOrder/append reset the shuffle pool, reintroducing previously consumed IDs during a pass; enqueueNext on an empty source-less session left index=-1 and no pending selection; the player markCurrent path could mark a stale music queue item recent while a generated diagnostic source was loaded.
- Changes: ShuffleCycle maintains unplayed survivors through queue edits, removes deleted IDs and shuffles only genuinely added candidates. Explicit queue/new-source or new-pass authorization still resets as before. An empty source-less queue stages its first selection silently and excludes it from self-priority-next. Recent stamping requires the selected current music PCM/source generation. AudioEngine and output code remain unchanged.
- Six ShuffleCycle tests cover reset/duplicate/current exclusion, reorder preserving survivors without replay, deduplicated appends, removal/re-add intent, exhausted-pass preservation with explicit reset, and100 consume/edit iterations checking remaining candidates never include already consumed IDs unless explicitly re-added. These exercise the actual pure shuffle state used by PlaybackQueue. Android Context/queue pending-selection/Service/EOF/bookmark integration is not executed by these JVM tests.
- RC1 was reported as applied/working by the user. No per-row device acceptance results were supplied and no whole-matrix pass is inferred. RC_CHECKLIST.md carries existing outstanding device checks plus RC2 cases. No Android device/emulator run, actual DAC verification, gapless guarantee or broader44.1k USB support is claimed.

### Final RC2 build evidence

- Offline `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug`: BUILD SUCCESSFUL in1m39s.127 JVM tests/0 failures/0 errors/0 skipped. Lint: No issues found. Build log `/tmp/purebit-research/build-1.0.0-rc2.log`.
- Package app.purebit, versionCode32/versionName1.0.0-rc2. apksigner and16KiB zipalign passed; same certificate SHA-256 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6. Both native USB .so files are byte-identical toRC1. Existing AudioEngine/core/native/backends/UI source files are unchanged fromRC1; PlaybackQueue plus new pure ShuffleCycle manage the corrected behavior.
- APK `BITWIRE-v1.0.0-rc2-debug.apk`, SHA-256 `3ac6f078c3c02ef13b65e7f166c1df6d710e9a268ce9ab27e5afe572929bf459`; source `BITWIRE-v1.0.0-rc2-source.zip`. No new device/emulator integration or DAC sample verification is represented by these checks.


## v1.0.0 final control / foreground ownership / text layout

- Fixes: retain committed seek targets through explicit resume or pause/interruption; revoke automatic resume while preserving interruption reason; recheck intent after potentially blocking USB close. Source changes still invalidate requests and unfinished drag previews remain uncommitted.
- New PlaybackOwnership broker defers automatic source loads until a foreground owner is ready, coalesces pending source choices, cancels obsolete/denied tasks, fences late retired-owner destroy/notification-art callbacks and excludes old EOF while a new source is pending. stopSelfResult uses the latest delivered startId. Current-owner MediaSession actions stay ordered; retiring sessions forward commands and cannot seek/adjust volume directly. Source/output/diagnostic/interruption hooks cancel pending starts. FGS start exceptions become engine stop/error reports. Runtime owns application-context work only, not Activities.
- Five JVM PlaybackOwnership sequence tests cover stopped owner→deferred source→replacement readiness, late old-owner destroy, latest choice plus stale cancellation, denied/manual-cancel work not replaying, and draining that pauses when its owner retires. These exercise the actual broker; they do not run Android AMS/Service/MediaSession/FGS or USB scheduling. Existing PCM/format/verification/seek rules and other regression tests are retained.
- Mini player text area wraps content with minimum heights; main long titles ellipsize; noisy-stop reason remains visible. No device layout/frame-time/memory profiling or screenshot comparison is claimed. Source/core/native/output-backend implementations remain unchanged.
- Version1.0.0 is a baseline build, not whole-matrix Samsung acceptance or bit-perfect certification. Real seek/interrupt timing, service teardown/restart, OS denial, large-font layout and DAC output remain device checks. Prior startup-crash root cause is still unconfirmed and recovery remains available.

### Final v1.0.0 build evidence

- Final offline `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug`: BUILD SUCCESSFUL in1m49s.132 JVM tests/0 failures/0 errors/0 skipped. Lint: No issues found. Final log `/tmp/purebit-research/build-1.0.0-ship.log`.
- Same app.purebit, versionCode33/versionName1.0.0. apksigner and16KiB zipalign passed; certificate SHA-256 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6. Both native USB .so files and15 PCM/core/native/backend sources are identical toRC2. No INTERNET permission.
- APK `BITWIRE-v1.0.0-debug.apk`, SHA-256 `35aa2e664272e4ee6ac5626a9d049a22243661f5e2b8bcf7d397ab3e1d7ca22e`. Source `BITWIRE-v1.0.0-source.zip`; device checklist `RC_CHECKLIST.md`. New foreground broker has JVM sequence coverage; real Android service/AMS/MediaSession and pending-seek timing have not been integration-tested here. No whole-device acceptance, measured performance or DAC bit-perfect certification is claimed.
# v1.1.0 Theme Packs — 2026-10-19

- Offline assembleDebug, testDebugUnitTest, lintDebug: BUILD SUCCESSFUL (final log `/tmp/bitwire-1.1.0-ship.log`, 1m41s).
- 138 JVM tests, zero failures/errors/skips. Six new cases cover schema1 defaults, custom order/dimension round trip, missing/duplicated controls, oversized/unknown fields, schema isolation, supplied studio example.
- Lint: No issues found. APK app.purebit / versionCode34 / versionName1.1.0 / min31 / target37.
- apksigner verified with the existing signer SHA256 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6. 16 KiB zip alignment check passed.
- Compared 22 audio/core/native source files against v1.0.0 source archive: identical.
- ThemePlayerLayout hosts OriginalPack/HwahyunPack and bounded block rearrangement; ThemeWidgets owns shared chrome. The Activity keeps playback actions and lifecycle. Schema2 save retains schema1 read compatibility.
- Preview uses the actual themed player layout with inert playback actions and a disabled seek control. Preview drawing reads current source analysis only; dismissal closes the deck and disables seek updates.
- Device testing was not run here. The user's report that prior S26 Ultra functions work is recorded separately from this new build's tests. Follow THEME_CHECKLIST.md for theme switching during playback, large fonts, rotation, import and restoration. JVM tests do not verify Android View/Activity lifecycle or USB playback on hardware.
- No new device-path or bit-perfect claim. Theme files cannot alter audio policy or run scripts. New arbitrary visual programs still require a UI module/APK update; this is not an unrestricted external-layout engine.

# v1.2.0 ABYSS — 2026-10-19

- Final offline assembleDebug/testDebugUnitTest/lintDebug: BUILD SUCCESSFUL in 1m43s (`/tmp/bitwire-1.2.0-ship.log`). 142 tests, zero failures/errors/skips. Lint: No issues found.
- Added four pure tests: satellite overlap/bounds, visible circular hit region excluding the occluded area, continuous round-to-rectangle corner endpoints, ABYSS preset/11-second intro serialization. They do not validate actual Android animation rendering.
- Existing signer retained (SHA256 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6), apksigner and 16 KiB zipalign pass. app.purebit / code35 / 1.2.0 / min31 / target37.
- 22 audio/core/native source files compared with v1.1.0 source bundle: identical. Both packaged arm64-v8a/x86_64 USB .so entries are byte-identical to v1.1.0 APK.
- Main and landscape use the same portal/satellite geometry for draw and hit testing. Runtime album overlay handles reverse close, Back, source change, onStop/onDestroy and reduced motion. Album art expands from centered square crop to the complete bitmap aspect ratio; the library's existing 512-pixel artwork request is retained, so this is not full-resolution extraction of arbitrary embedded artwork.
- First draft compile/unit tests passed but Lint rejected a literal Typeface style constant; replaced it with Typeface.NORMAL. Draw-time overlay shader allocation was removed and the runtime-only view constructor documented. Final checks pass without a Lint baseline.
- No Samsung/Android device or emulator rendering test was performed here. Test the list in ABYSS.md. Frame pacing is a 60 FPS target, not a measured result. Artwork position, contrast under actual covers, maximum font scaling, rotation and rapid album expand/close need hardware review.
- Initial version uses a cinematic background zoom/fade for entry; the exact concept's continuous portal-shape landing and arbitrary theme-file assets are not implemented. Music/USB formats, PCM output and bit-perfect verification claims are unchanged.

# v1.2.1 ABYSS choreography — 2026-10-19

- Offline assembleDebug/testDebugUnitTest/lintDebug: BUILD SUCCESSFUL in 1m59s (`/tmp/bitwire-1.2.1-build.log`). 145 tests, zero failures/errors/skips. Lint: No issues found.
- Three new tests cover exact deck-coordinate/radius landing endpoints and clamping, monotonic delayed uncropping vs travel, zero-intensity bubble endpoints. Existing visible-area geometry tests pass after portal sizing changes.
- app.purebit/code36/1.2.1/min31/target37; existing signer a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6 verified; 16 KiB zipalign passed.
- 22 audio/core/native source files match v1.2.0. Changes are confined to theme/UI geometry, visual timing, glass/control styling and documentation.
- Intro receives the actual laid-out deck rectangle via the existing host mechanism. In the final stage, shared scene rendering interpolates a portal to that rectangle. Settings previews fall back to their full-window destination when the player page is hidden. Runtime artwork remains circular behind the portal.
- Artwork travels along a shallow arc, uncrops later, and uses an elapsed animation clock for upward bubble motion in either direction. Glass gradient is cached on drawable bounds/state changes; transport symbols are drawn independently of glyph fonts.
- No Android rendering/device test was performed here. Unit tests verify math/data, not perceived smoothness. On S26 Ultra check automatic intro and its final handoff, reduced motion, rapid reverse/Back, rotation during expansion, extreme portrait/landscape cover ratios, header contrast and playback during all transitions. 60 FPS remains a target, not a measurement.
- The user's reference remains a visual goal; this update does not claim pixel-identical reproduction, physical water simulation or new device-path verification.

# v1.2.2 ABYSS depth / transient waves — 2026-10-19

- Offline assembleDebug/testDebugUnitTest/lintDebug: BUILD SUCCESSFUL in 2m (`/tmp/bitwire-1.2.2-build.log`). 149 tests, zero failures/errors/skips. Lint: No issues found.
- Four new tests cover sustained loudness settling without repeated triggers, distinct bass attacks, disabled/source reset and nonfinite input handling, deterministic particle depth/size/speed/bounds.
- app.purebit/code37/1.2.2/min31/target37. Existing debug signer verified, 16 KiB zipalign passed. Both USB native .so files byte-identical to v1.2.1; 22 audio/core/native sources unchanged.
- Wave detector consumes the existing read-only source band envelope in DeckView's frame callback. Fixed-size wave storage; no microphone/device samples and no PCM mutation. Drawing reads state without advancing detector time. Source changes, inactive views, pause, reaction/bass disabled and reduced motion reset waves.
- Particle positions use a reused FloatArray. Foreground soft bubbles use a cached radial gradient/local matrix rather than per-particle real-time blur. Background/mid/front proportions preserve the existing particle limit and noninteractive overlay.
- No device/emulator rendering or frame-time test. S26 Ultra checks: busy music vs quiet music sensitivity, bass onset timing, layer visibility/legibility, toggling reaction OFF, pause/seek/source changes, album expansion and landscape. 60 FPS remains an unmeasured target. Actual USB/device verification scope is unchanged.

# v1.2.3 ABYSS cover / jellyfish — 2026-10-19

- Offline assembleDebug/testDebugUnitTest/lintDebug: BUILD SUCCESSFUL in 1m56s (`/tmp/bitwire-1.2.3-build.log`). 153 tests, zero failures/errors/skips. Lint: No issues found.
- Four new pure tests cover old-cover release at completion, dominant-cover/latest-target behavior on rapid changes, missing cover/reduced-motion settling, and idempotent same-bitmap delivery. Actual Android Canvas composition is not tested by these cases.
- app.purebit/code38/1.2.3/min31/target37; existing signer verified, 16 KiB zipalign passed. 22 audio/core/native sources match v1.2.2.
- CoverFade borrows at most two bitmap references and never recycles shared library assets. Scene uses time-based fade and a short local bubble burst; inactive views settle the fade. The existing source-generation checks still control asynchronous artwork delivery.
- Jellyfish use distinct time scales, body contraction and lagged tentacle curves. Light beam opacity attenuates downward and in quiet pages. No audio behavior or device-path verification changes.
- No Android device/emulator rendering/frame-time/memory measurement here. Test rapid NEXT/previous, no-cover songs, landscape, expanded artwork/source change, animation OFF, returning to app, and readability under bubbles. 60 FPS remains an unmeasured target.

# v1.3.0 ABYSS completion bundle — 2026-10-19

- Final offline assembleDebug/testDebugUnitTest/lintDebug: BUILD SUCCESSFUL in 1m45s (`/tmp/bitwire-1.3.0-ship.log`). 157 tests, zero failures/errors/skips; Lint: No issues found.
- Four new pure tests cover effect default compatibility, independent OFF/strength levels, bounded invalid levels, and complete calm/luminous preset values. These do not validate actual Android SharedPreferences/UI gestures.
- app.purebit/code39/1.3.0/min31/target37; existing signer verified; 16 KiB zipalign passed. 22 audio/core/native sources and both packaged USB .so entries match v1.2.3.
- Effects propagated to main/background/foreground/immersive/lyrics/preview/intro/album transition. Native picture content remains visible even when animated jellyfish/light are OFF; settings explicitly say so.
- Immersive root consumes the first wake touch and prevents pass-through to main controls. The always-visible exit button is exempt from wake-touch consumption. Touch/focus and reduced motion gate auto-hide; onStop restores chrome and cancels pending fullscreen scrub. closeFullscreen removes callbacks, closes the waveform widget worker and releases view references.
- Fullscreen seeking uses the existing WaveformSeekBar cancellation flag and AudioEngine scrub token. Touch cancellation and source generation changes cancel; release commits; keyboard/accessibility progress actions use existing engine.seek. Native A-B controls retain source-generation checks. Playback core is unchanged.
- Abyss lyrics use a glass panel and scrolling timed/untimed native text. Landscape title/artist are bounded; effect controls use content height for font scaling. Layout/readability/accessibility behavior still needs actual device review.
- First two full builds also passed; final pass includes touch-cancel handling, fullscreen gesture containment, effect propagation on renderer entry, non-touch seek, keyboard auto-hide gating and adaptive settings button height.
- No real Android device/emulator integration, 30-minute playback, temperature, frame pacing, memory pressure or maximum-font measurement was performed here. ABYSS_FINAL_CHECKLIST.md explicitly lists the remaining physical tests. This is a feature-completion build, not all-device or bit-perfect certification.

# v1.3.1 ABYSS detail / wide-button reflection — 2026-10-19

- Offline assembleDebug/testDebugUnitTest/lintDebug: BUILD SUCCESSFUL in 2m2s (`/tmp/bitwire-1.3.1-build.log`). 160 tests, zero failures/errors/skips. Lint: No issues found.
- Three regression tests distinguish wide primary actions from near-square play/volume orbs and reject panel/empty bounds. Wide USB Direct action no longer enters the orbital reflection branch; the remaining faint reflection follows its top edge.
- app.purebit/code40/1.3.1/min31/target37; existing signer verified; 16 KiB zipalign passed. 22 audio/core/native sources unchanged from v1.3.0.
- Added generated transparent jellyfish asset (1254x1254 RGBA, alpha range 0..255, 63% fully transparent), decoded at half resolution once per process and transformed in Canvas. Existing vector fallback retained.
- Added cached radial lens/bubble lighting and small specular details, plus subtle underwater shimmer. These are 2D visual approximations, not physical fluid optics. Circular cover behavior, user effect levels and audio policy retained.
- No Samsung device/emulator pixel inspection or frame/memory/temperature measurement here. On device confirm wide-button arc is gone, orb reflection remains, new jellyfish transparency/brightness and glass edge intensity are appropriate, and effect OFF settings continue to work. 60 FPS remains a target.

# v1.4.0 ABYSS dedicated layout and layered scene — 2026-10-19

- Final offline assembleDebug/testDebugUnitTest/lintDebug: BUILD SUCCESSFUL (`/tmp/bitwire-1.4.0-final.log`). 163 tests, zero failures/errors/skips. Lint: No issues found.
- Three added tests cover bounded viewport/controls height allocation and deterministic, finite mesh deformation with separate bell/tail motion. These are pure calculation tests, not rendered Android View tests.
- app.purebit/code41/1.4.0/min31/target37. Existing signing certificate SHA-256 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6 verified. 16 KiB zipalign passed.
- All 22 audio/core/native source files and both packaged native .so entries match v1.3.1. PCM/USB/gain and bit-perfect verification policy are unchanged.
- Dedicated measured-height hero layout, compact tools, native buttons with custom icons, separate drifting foreground terrain and foreground bubble plane. Terrain stays behind native controls to preserve readability. Circular album remains behind portal. Preview and live layout share lyrics/favorite actions.
- Clean ocean plate removes baked jellyfish. Animated sprite uses an 8x16 mesh with bell contraction and delayed tentacles. Bubble refraction approximates the ocean texture; this is not a physical 3D renderer or optical measurement.
- No connected Galaxy/emulator system image was available. Actual View fit, touch/accessibility, visual similarity, effect OFF, foreground contrast, rotation, 60 FPS, memory and temperature remain device checks in ABYSS_RECONSTRUCTION.md and ABYSS_FINAL_CHECKLIST.md. No physical DAC sample verification is claimed.

# v1.5.0 ABYSS living ocean — 2026-10-19

- Final offline assembleDebug/testDebugUnitTest/lintDebug: BUILD SUCCESSFUL in 1m35s (`/tmp/bitwire-1.5.0-releasecheck.log`). 168 tests, zero failures/errors/skips. Lint: No issues found. Earlier build passed with a UseRequiresApi warning; final code uses direct SDK guards around construction and calls, without suppressing that check or adding a dependency.
- Five added tests cover asymmetric propulsion/recovery, continuous non-reversing ascent, deterministic bounded long-duration motion, larger bubbles rising/deforming more, and invisible bubble loop transitions. They do not assert biological accuracy or rendered image quality.
- app.purebit/code42/1.5.0/min31/target37; original signing certificate verified, 16 KiB zipalign passed. All 22 audio/core/native sources and both packaged native libraries match v1.4.0. No source PCM or DAC-path verification policy changes.
- New jellyfish (1024x1536 RGBA) and bubble (1254x1254 RGBA) assets have transparent corners and nonconstant alpha; packaged asset entries verified. Decode sampling is 2 and 4 respectively. Existing terrain/background remain separate.
- Jellyfish mesh uses a short power stroke and long recovery with delayed tails, frame-independent ascent and individual drift. Bubble streams use size-dependent rise, wobble, expansion, deformation and edge fades. Native controls retain input; foreground elements are decorative only.
- API33+ hardware Canvas attempts AGSL scenic refraction/caustics. Source crop/scale transforms are rebound to the child BitmapShader with linear filtering. Shader creation/draw exceptions fall back to Canvas, and canvas transforms restore in finally. API31–32 do not construct the runtime shader. Renderer status is visible in Abyss settings.
- No connected Android runtime/emulator system image here: AGSL runtime compilation/GPU output/fallback behavior, physical realism, real View screenshots, accessibility gestures, frame pacing, 60 FPS, temperature and memory still require S26 Ultra review. Unit tests and Lint do not prove those properties. See ABYSS_LIVING_OCEAN.md and ABYSS_FINAL_CHECKLIST.md.

# v1.6.0 ABYSS composition / underwater lighting — 2026-10-19

- Offline assembleDebug/testDebugUnitTest/lintDebug: BUILD SUCCESSFUL in 2m16s (`/tmp/bitwire-1.6.0-build.log`). 171 tests, zero failures/errors/skips. Lint: No issues found. Sandbox-local Gradle lock networking required an escalated local build; the build itself remained offline.
- Added three tests prevent controls/expanded A–B from collapsing a width-led phone portal, check 85–90% screen-width diameter for normal 360–480dp phones/default padding, and verify album bounds and shared hit regions. Existing intro-landing/album occlusion tests also pass.
- Reviewed supplied 7.174878s H.264 1080x2340 recording at 2-second intervals and a 4 fps jellyfish crop sequence. This establishes the prior visible layout/pattern problems; it is not a rendered validation of the new APK, nor a measurement of app 60 FPS from recording metadata.
- app.purebit/code43/1.6.0/min31/target37. Same signing SHA-256 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6 verified; 16 KiB zipalign passed. All 22 audio/core/native sources and both packaged .so entries match v1.5.0.
- Automatic hero sizing retains a width-derived minimum and uses ScrollView on short/large-font/expanded screens. Explicit theme artworkHeight still wins. Default Abyss playSize is 72dp square; other renderer defaults stay unchanged. Supplementary glass is inset within 48dp Buttons. Time/tool labels may grow vertically. Shared geometry still drives rendering, album input, expansion and intro landing.
- Abyss seek drawing has a thinner line and small cursor without the vertical crosshair; touch/seek listeners, A–B markers and waveform option stay unchanged. No audio commands were added.
- AGSL no longer adds a full-scene sharp line network. Low-amplitude shimmer is masked to surface and side texture edges; scenic warp is reduced. Soft gradient light shafts and static depth grading replace hard-edged triangle rays and internal horizontal curve overlays.
- No new visual assets or jellyfish/bubble kinematics changes. No connected Galaxy/emulator rendering, new shader compilation/output, real View/gesture/accessibility inspection, 60 FPS/temperature/memory measurement performed. Recheck oversized window/scroll, album touch, subtle lighting, landscape and theme switching on device using ABYSS_COMPOSITION.md / ABYSS_FINAL_CHECKLIST.md. DAC path remains unverified.

# v1.7.0 ABYSS reference-based materials / compact layout — 2026-10-19

- Offline assembleDebug/testDebugUnitTest/lintDebug: BUILD SUCCESSFUL in 2m5s (`/tmp/bitwire-1.7.0-build.log`). 172 tests, zero failures/errors/skips. Lint: No issues found.
- Inspected supplied 5.807311s 1080x2340 recording at 2s and temporal frames against concept 7210.jpg. Identified smooth/empty canyon, oversized foreground objects and controls below the viewport. New rendered output has not been recorded here.
- Reconstructed and visually inspected ocean.png (841x1870 RGB) and new portal.png (1254x1254 RGB) using the supplied concept as image reference. Removed a generated second water surface before packaging. These are regenerated art assets, not exact reference pixels. Dedicated portal bitmap/shader avoids cropping a tall background into the window; missing portal asset falls back to ocean.
- Window diameter now computes to about 82–84% of screen width at 360–480dp/default padding, rather than prior 86–88%. Minimum hero height, shared album hit bounds and intro landing tests pass with updated proportions. Existing explicit external artworkHeight/playSize remain authoritative.
- Native supplementary targets remain 48dp; default Abyss playSize is 64dp and navigation row height 56dp. At fontScale<=1.15 and adjacent SEEK/TIME blocks, a 48dp module contains a full native SeekBar and non-clickable time TextView. Larger font or custom block order uses independent rows. Added one test verifies combining cannot silently reorder separated TIME/SEEK blocks. Native gesture listeners/commit semantics are unchanged.
- Foreground bubble size/opacity and jellyfish tint/size adjusted for scenery readability; existing kinematic curves unchanged. Old foreground terrain opacity reduced so it does not dominate the replacement plate.
- app.purebit/code44/1.7.0/min31/target37, existing signing certificate verified, 16 KiB zipalign passed. All 22 audio/core/native source files and both packaged native libraries match v1.6.0. New portal asset confirmed in APK.
- No attached Galaxy/emulator runtime: no new View layout fit, compact time-over-seek touch dispatch, GPU/shader output, frame/temperature/memory or pixel-identical comparison certification. These are explicitly listed for device review in ABYSS_MATERIALS.md / ABYSS_FINAL_CHECKLIST.md. Source PCM/DAC path verification remains unchanged.



## v1.8.0
Offline assembleDebug/testDebugUnitTest/lintDebug successful. 172 JVM tests, zero failures/errors/skips. APK signature matches v1.7; 16KiB zip alignment passes. Both packaged native libraries identical to v1.7; audio sources unchanged. Actual device layout, accessibility sizing and GPU animation require Galaxy testing.


## v1.9.0 / versionCode46

assembleDebug/testDebugUnitTest/lintDebug successful (offline Gradle, 1m50s final build). 177 JVM tests, zero failures/errors/skips. Lint: No issues found. APK package app.purebit, signing SHA256 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6 unchanged; 16KiB zip alignment passes. New play-orb.png is packaged. 31 audio/core/native source files and both packaged native libraries match v1.8.0 byte-for-byte. Geometry tests cover circular playback material inside weighted landscape targets, optical/touch size separation, small/large-font group padding, degenerate sizes and keeping primary USB settings rectangular. Galaxy-rendered layout, actual frame pacing and touch behavior have not been measured here.


## v1.10.0 / versionCode47 — ABYSS polish

Offline assembleDebug/testDebugUnitTest/lintDebug successful (2m18s). 182 JVM tests; zero failures/errors/skips. Lint: No issues found. APK package app.purebit, unchanged signer SHA256 a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6; 16KiB zip alignment passes. 31 audio/core/native source files, both packaged native libraries and all seven ABYSS asset entries match v1.9.0 byte-for-byte. New tests cover control fitting vs deliberate large-font/expanded-panel scrolling, smooth rectangle-based particle attenuation and non-folding strand variants. These are mathematical/unit checks, not Android View, actual transport UI or GPU verification.

Visual evidence: reviewed supplied v1.9.0 recording and user screenshot. No connected Galaxy or runnable emulator system image available. No AFTER screenshot or same-condition recording exists yet; visual completion, frame stability, CPU/GPU and battery results remain pending. See ABYSS_POLISH.md for exact files, performance tradeoffs and the user comparison checklist.
