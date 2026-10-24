# BITWIRE Theme Packs — v1.2.0

ABYSS renderer가 추가되었다. `renderer=abyss`는 어비스 레이아웃/유리 스타일/수중 장면을 사용하며 내장 `abyss` ID는 보호된다. 전체 연출 길이는 ABYSS에서 최대 12000ms, 기본 11000ms다. 기존 두 렌더러는 최대 10000ms를 유지한다. [ABYSS.md](ABYSS.md)에 구현 및 실제 기기 점검 항목을 정리했다. 아래 v1.1.0 기록에서 두 프로그램으로 설명한 범위는 현재 세 프로그램으로 확장된다. 임의의 새 코드를 외부 파일에서 실행하지 않는 원칙은 동일하다.

## 현재 제작 방식

기존 v1 파일을 계속 읽으며 새 저장은 schema=2를 사용한다. 테마 파일은 오프라인 UTF-8 Properties이며 기존 32 KiB/외부 16개 제한을 유지한다. 새 파일은 `themes/hwahyun-studio.bitwire-theme`를 복사해 만들면 된다. 이 예제는 제목을 위로, 재생 버튼을 탐색 바 위로 옮기고 앨범 영역을 260 dp로 설정한다. 기존 ORIGINAL/화현의 기본 순서와 크기는 유지한다.

1. `id`, `name`, `renderer`를 지정한다. 기본 제공 ID는 덮어쓰지 않는다.
2. 색상/꽃잎/연출 길이와 아래 `layout.*`를 조정한다.
3. 휴대폰의 **설정 → 테마 및 시작 연출 → 테마 파일 가져오기**에서 적용한다.
4. 앱에 내장하려면 파일을 `app/src/main/assets/themes/`에 넣는다. ThemeStore가 최대 32개를 자동 발견하므로 Activity/재생 엔진 수정이 필요 없다. 중복 ID를 쓰지 않는다.

| schema=2 키 | 의미 / 범위 |
|---|---|
| layout.blocks | `route,artwork,metadata,seek,time,loop,transport,volume,message,picker` 각각 한 번, 원하는 순서 |
| layout.artworkHeight | 0: 테마의 화면 높이 대응값 / 160~480 dp: 고정 높이 |
| layout.padding | 좌우 여백 8~40 dp |
| layout.titleSize | 제목 16~32 sp, 시스템 글꼴 배율 적용 |
| layout.playSize | 중앙 재생 버튼 너비 64~112 dp |

누락된 layout 키는 해당 renderer 기본값을 사용한다. 필수 블록의 제거/중복, 지원하지 않는 이름, 과도한 크기는 가져오기 단계에서 거부한다. 화현에서는 route를 기본 화면에서 숨기며 음악 선택 버튼은 LOOP 도구 행과 함께 움직인다. 상세 출력 정보는 앱 내부 데이터에서 확인한다. layout 설정은 메인 재생 화면에 적용하며 가로 몰입/가사 화면은 기존 장면 구성을 사용한다.

## 개발 경계와 확장 지점

- `theme/packs/OriginalPack.kt`, `HwahyunPack.kt`: 테마별 재생 화면 생성. `ThemePack` 인터페이스와 `ThemePacks` 등록부로 선택한다.
- `ThemePlayerLayout`: 뷰 바인딩과 데이터 순서/크기 적용. 플레이어에는 `PlayerAction` 명령만 전달한다. 엔진/USB 참조가 없다.
- `ThemeWidgets`: 메인·라이브러리·대기열·설정에서 사용하는 공통 버튼/글꼴/패널/스위치 스타일. UI의 테마 스타일 수정 위치를 한곳으로 모았다.
- `ThemeSpec`, `PlayerLayoutSpec`, `ThemeCodec`: 순수 데이터와 호환성/범위 검증.
- `ThemeStore`: 내장 프리셋 자동 발견, 사용자 프리셋 저장, 기본 테마 복구.
- `MainActivity`: 사용자 명령과 상태/생명주기를 연결한다. 곡 선택/탐색 동작은 테마 팩에 복제하지 않는다.

현재 데이터 팩은 ORIGINAL/화현이라는 두 시각 프로그램을 재사용한다. 완전히 새로운 파티클·Canvas 장면·입장 연출·가로 구도는 Kotlin 시각 모듈과 앱 업데이트가 필요하다. `DeckView`, `StartupView`, `GardenAtmosphereView` 등에는 두 프로그램의 분기가 남아 있다. **모든 화면/임의 애니메이션을 외부 파일만으로 정의하는 범용 스킨 엔진은 아니다.** 새 시각 프로그램을 개발할 때는 이 UI 모듈 및 등록부를 확장하며 audio/core/native 코드를 수정하지 않는 것을 원칙으로 한다. 외부 ZIP/이미지/폰트/실행 코드/마켓은 이번 범위에 없다.

## 새 시각 모듈 제작 절차

기존 pack의 build(host)를 복사하여 의미 있는 블록 목록을 구성한다. `host.blocks`에 모든 PlayerBlock을 등록하고, 뷰 바인딩을 모두 채운다. 빈 블록은 emptyList로 명시한다. 데이터 재배치는 블록 순서만 바꾸므로 버튼 명령·탐색 로직을 테마 코드에서 재구현하지 않는다. 렌더러 enum/기본 ThemeSpec/ThemeCodec/ThemePacks에 새 프로그램을 등록하고 해당 Deck/Intro/전체화면 시각 프로그램을 연결한다. 지원되지 않는 렌더러 파일은 자동 대체하지 않고 업데이트가 필요하다고 거부한다.

검증: schema 1 로드, schema 2 왕복 저장, 기본/화현 복귀, 잘못된 파일 거부, 재생 중 전환, 가로/큰 글꼴, 테마 적용 후 seek·A–B·하드웨어 볼륨, 장면 종료 시 애니메이션 해제. JVM 테스트는 실제 Android 뷰 생명주기나 Samsung 화면을 대신하지 않는다.

---

아래는 이전 버전의 설계 기록이다. 현재 schema=2 추가 내용은 위 설명을 따른다.

## Theme v1 기록

v0.6의 `hwahyun` renderer는 앱 전체에 정원 배경/꽃잎 레이어, 전용 플레이어/버튼/탐색/화첩 라이브러리/패널/메뉴를 사용한다. 내장 renderer의 구현이 업데이트된 것이므로 schema는1을 유지하며 기존 화현 설정 파일도 이 스킨을 사용한다. 배경/달은 앱에 포함된 고정 artwork이고, v1 파일이 새 외부 이미지나 새로운 레이아웃 코드를 전달하지는 않는다.

테마 파일만 가져와 현재 앱의 그림 방식과 색상/입자/연출 설정을 재사용한다. 앱 내장 ORIGINAL은 삭제하거나 대체하지 않는다. 음악 엔진과 테마 renderer는 분리되어 있다.

## 설치

휴대폰에 `.bitwire-theme` 파일을 저장하고 **설정 → 테마 및 시작 연출 → 테마 파일 가져오기**에서 선택한다. 미리보기 뒤 적용한다. 월백 예제는 화현의 분홍/푸른 달빛 변형이며9초 오프닝과60개 기본 꽃잎을 사용한다. 앱 재설치 없이 새 설정 테마를 추가할 수 있다.

## 파일 형식

v1은 ZIP이나 실행 플러그인이 아닌 UTF-8 Java Properties 텍스트다. 최대32KiB, 가져온 테마 최대16개다. `#`로 시작하는 줄은 주석이다. 같은 custom ID를 다시 가져오면 해당 설정을 교체한다. 내장 `original` / `hwahyun` ID는 가져오기로 덮어쓸 수 없다. 이름의 `\`, `=`, `:`는 properties escape를 사용한다.

```properties
schema=1
id=hwahyun-moonlit
name=화현 · 월백
renderer=hwahyun
background=#0C101C
surface=#191F30
accent=#E7B7D7
secondary=#A7CFDF
text=#FFF1E5
muted=#A9AABC
petals=60
petalSpeed=0.65
introMillis=9000
```

| 항목 | 조건 |
|---|---|
| schema | 반드시1 |
| id | 소문자 영숫자로 시작, 영숫자/하이픈/밑줄, 최대48자 |
| name | 제어 문자 없이1~60자 |
| renderer | `original` 또는 `hwahyun` |
| 6개 색상 | 불투명 `#RRGGBB`; 생략하면 해당 renderer 기본값 |
| petals | 0~96; 앱의 꽃잎 양 설정을 곱한 뒤96개로 제한 |
| petalSpeed | 유한수0.2~2.0 |
| introMillis | 전체 모드 길이1500~10000ms; 짧게 모드는1800ms |

색상은 어두운 background/surface, text/accent 대비4.5 이상, secondary/muted 대비3 이상을 요구한다. 잘못된 버전/지원하지 않는 항목/renderer는 적용하지 않는다. 아직 밝은 테마, 외부 글꼴·이미지·음악 파일, URL, 스크립트, 임의 layout/셰이더를 지원하지 않는다. 새 장면/입자 종류는 renderer 앱 업데이트가 필요하다. 네트워크 권한과 앱 내 마켓은 없다.

## 구현 경계

- `ThemeSpec` / `ThemeCodec`: Android와 분리된 설정/검증/직렬화.
- `ThemeStore`: 선택값과 가져온 테마를 앱 private storage에 보관. 파싱을 통과한 파일만 atomic write. 손상 시 ORIGINAL fallback.
- `DeckView` / `HwahyunScene` / `StartupView`: 기존 원본 PCM 분석 결과를 읽어 그리는 UI. AudioEngine/native transport를 호출하지 않는다.
- `GardenAtmosphereView` / `GardenPainter`: 화면 전체의 배경/후경과 입력을 통과시키는 전경 꽃잎. `GardenArt`는 앱 assets의 불변 bitmap을 프로세스 내 공유한다. `HwahyunButton`/`BlossomThumb`는 네이티브 위젯 의미를 유지한 전용 그림이다.
- 폰트는 현재 앱에 포함된 Nanum Brush Script/Allura를 사용한다. 각 원본 OFL은 APK assets와 소스에 포함한다. 실제 필순 정보는 없으며 글자 reveal로 표시한다.
- 회로/현/달빛/오프닝은 장치 연결 성공, AudioFlinger 우회 또는 bit-perfect 검증을 의미하지 않는다.

향후 이미지/font archive, 추가 renderer, 마켓을 지원하려면 별도의 크기·호환성·라이선스·생명주기 설계와 schema 버전 확장이 필요하다. v1이 이를 지원하는 것으로 표시하지 않는다.

## v0.7 시작 연출 선택함

화현 렌더러는 앱에 번들된 달빛 정원, 벚꽃비, 현의 개화 세 프로그램을 선택할 수 있습니다. 정적 썸네일은 안내용이며 미리보기 버튼이 실제 오프닝을 재생합니다. 선택은 앱 환경설정에 저장하며 v1 .bitwire-theme 스키마는 변경하지 않습니다. 외부 테마 파일에 실행 코드나 새 연출 코드를 넣는 방식이 아닙니다. ORIGINAL은 기존 오프닝을 유지합니다. 전체/짧게/끄기, 김백현 & Codex 서명, 무음·접근성·건너뛰기 정책은 공통입니다.

## v0.8 가로 감상과 가사

화현 가로 몰입 화면은 별도 Canvas 구도로 달·표지 반사와 수면을 표시합니다. ORIGINAL의 DJ 데크를 대체하지 않습니다. 미니플레이어와 가사 서첩은 현재 테마의 색을 사용하며 가사는 화현에서 번들 손글씨 폰트로 표시합니다. 새 외부 테마 스키마 키/코드 플러그인은 추가하지 않았습니다. 물결은 원본 PCM의 기존 저음 envelope를 시각화할 뿐, 소리나 물리적 출력 경로를 변경하지 않습니다.


## v0.9.0: 미리보기와 집중 감상

테마 목록 위쪽 ORIGINAL 복귀 버튼과 현재 곡 표지/제목을 쓰는 미리보기를 제공한다. 화현 곡 전환 바람은 화면 장식이며 음악을 섞지 않는다. 집중 감상은 같은 내장 화현 renderer를 사용해 세로 달/아래 가사 또는 가로 달/오른쪽 가사로 배치한다. 동기 가사의 시각이나 PCM 바이트는 변경하지 않는다. 설정한 테마는 안전 모드에서도 저장된 상태로 유지되며 ORIGINAL을 임시 사용한다. 파일 schema1은 유지한다.


## v0.10.0: 정원의 밤

화현 내장 painter에 밤하늘 tint/별빛/물안개를 추가했다. 기본 AUTO는 전경 감상12분을 기준으로 하며 실제 시간이나 출력 측정이 아니다. 고정3종과10초 미리보기를 앱 설정에서 고른다. 선호 모드는 보존하고 감상 시간은 프로세스 범위이며 회전에서 유지한다. schema1/기존 테마 파일/ORIGINAL renderer는 유지한다. 시각 효과가 PCM이나 소프트웨어 음량을 변경하지 않는다.


## v0.11.0 라이브러리

새 아티스트·폴더 목록도 현재 테마의 패널/버튼/배경을 사용한다. 화현의20개 표지 화첩과 앨범 상세를 유지한다. ORIGINAL의 기본 표지는 앱 아이콘을 사용하며 정원 자산을 강제로 읽지 않는다. 테마 schema1은 변경하지 않았다. 정렬/검색/목록 복원은 재생 중인 PCM과 출력 경로를 변경하지 않는다.


## v0.12.0 재생목록

재생목록 목록/상세/대기열 도구는 각 renderer의 기존 버튼·패널·정원을 사용한다. 곡 여러 개 선택 창은 Android ListView의 체크 행을 재사용해 화면에 보이는 행을 재활용한다. schema1은 변경하지 않았다. 저장 목록의 편집과 화면 효과는 오디오 바이트를 처리하지 않는다.
