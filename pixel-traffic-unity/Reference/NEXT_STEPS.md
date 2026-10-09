# 목표와 다음 검증

`unity-3d-target.png`는 승인된 생성형 디자인 시안입니다. 현재 첫 프로젝트의 실행 화면이나 Unity 렌더 결과가 아닙니다.

1. Unity6000.3.26f1에서 import/C#컴파일·첫 장면 검사·9:16 Play 화면을 확인합니다.
2. Android Gradle export의 unityLibrary/UnityPlayer API를 확인하고 WallpaperService.Surface/가시성·잠금/복귀·미리보기 전환을 구현합니다.
3. S20+/S26 Ultra에서 실제 배경화면 연결과30FPS/FPS·발열을 검증합니다.
4. 시안의3D차량/도시·재질/조명/가림을 확장하고 기존 주행/신호·보행4–100·날씨/설정을 이전합니다.

Unity as a Library의 단일runtime·렌더링/호스트수명주기 제약 때문에 WallpaperService 연결은 별도의 기술 검증입니다. 현재코드에는 WallpaperService가 없으며 일반Activity와Export를 먼저 준비했습니다. 전환 성공과시안전체품질을 미리 보장하지 않습니다.

실험 앱은기존0.48과공존하는별도ID를사용합니다. APK빌드는기존서명키의인증서를먼저확인하고,정식전환시기존ID·저장설정/프리셋·서명을유지합니다. 키/비밀번호는ZIP에포함되지않습니다.

공식근거:
- https://docs.unity3d.com/6000.3/Documentation/Manual/UnityasaLibrary.html
- https://docs.unity3d.com/Packages/com.unity.render-pipelines.universal@17.3/api/UnityEngine.Rendering.Universal.UniversalRenderPipelineAsset.html
