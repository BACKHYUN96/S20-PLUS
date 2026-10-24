package app.purebit.theme

/** Bundled visual programs only; no downloaded code, audio, or permission changes. */
enum class IntroScene(val label:String,val description:String){
    THEME("달빛 정원","달이 떠오르고 벚꽃 정원이 열립니다"),
    PETAL_RAIN("벚꽃비","겹겹이 쏟아지는 꽃잎 사이로 달빛이 나타납니다"),
    STRING_BLOOM("현의 개화","어둠 속 현의 빛이 이어지며 꽃이 피어납니다");
    companion object {fun decode(value:String?)=entries.firstOrNull {it.name==value}?:THEME}
}
