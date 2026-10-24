package app.purebit.theme

import java.io.StringReader
import java.util.Properties
import kotlin.math.pow

enum class ThemeRenderer { ORIGINAL, HWAHYUN, ABYSS }
enum class IntroMode { FULL, SHORT, OFF }

data class ThemeSpec(
    val id: String, val name: String, val renderer: ThemeRenderer,
    val background: Int, val surface: Int, val accent: Int, val secondary: Int,
    val text: Int, val muted: Int, val petals: Int = 36, val petalSpeed: Float = 1f,
    val introMillis: Long = 8000,
    val layout: PlayerLayoutSpec = PlayerLayoutSpec.defaults(renderer)
)

object BuiltInThemes {
    val original = ThemeSpec("original", "ORIGINAL · NEON DECK", ThemeRenderer.ORIGINAL,
        0xff080b13.toInt(), 0xff101724.toInt(), 0xff31edd8.toInt(), 0xffae76ff.toInt(),
        0xffffffff.toInt(), 0xff8091a4.toInt(), 0, 1f, 3200)
    val hwahyun = ThemeSpec("hwahyun", "화현 · 花絃", ThemeRenderer.HWAHYUN,
        0xff100d19.toInt(), 0xff201927.toInt(), 0xfff1afc5.toInt(), 0xffdbb49b.toInt(),
        0xfffff1e5.toInt(), 0xffb3a2b2.toInt(), 44, .8f, 8000)
    val abyss = ThemeSpec("abyss", "ABYSS · 심연", ThemeRenderer.ABYSS,
        0xff020e19.toInt(), 0xff0a2232.toInt(), 0xff7be8f4.toInt(), 0xff83bdde.toInt(),
        0xffeafaff.toInt(), 0xff9dbdc9.toInt(), 44, .65f, 11000)
    val all = listOf(original, hwahyun, abyss)
}

/** v1 is bounded UTF-8 configuration, not an executable plugin or an archive. */
object ThemeCodec {
    const val MAX_BYTES = 32768
    private val keys = setOf("schema", "id", "name", "renderer", "background", "surface", "accent", "secondary", "text", "muted", "petals", "petalSpeed", "introMillis", "layout.blocks", "layout.artworkHeight", "layout.padding", "layout.titleSize", "layout.playSize")
    fun decode(text: String, importing: Boolean = true): ThemeSpec {
        require(text.toByteArray(Charsets.UTF_8).size <= MAX_BYTES) { "테마 파일은 32 KiB 이하여야 합니다" }
        val p = Properties().apply { load(StringReader(text)) }
        require(p.stringPropertyNames().all { it in keys }) { "지원하지 않는 테마 항목" }
        val schema = p.getProperty("schema")
        require(schema in listOf("1", "2")) { "지원하지 않는 테마 버전" }
        require(schema == "2" || p.stringPropertyNames().none { it.startsWith("layout.") }) { "레이아웃에는 schema=2가 필요합니다" }
        val id = p.getProperty("id", "")
        require(id.matches(Regex("[a-z0-9][a-z0-9_-]{0,47}"))) { "잘못된 테마 ID" }
        require(!importing || BuiltInThemes.all.none { it.id == id }) { "기본 테마 ID는 덮어쓸 수 없습니다" }
        val name = p.getProperty("name", "").trim()
        require(name.length in 1..60 && name.none { it.isISOControl() }) { "잘못된 테마 이름" }
        val base = when (p.getProperty("renderer")) {
            "original" -> BuiltInThemes.original
            "hwahyun" -> BuiltInThemes.hwahyun
            "abyss" -> BuiltInThemes.abyss
            else -> error("지원하지 않는 renderer · 앱 업데이트가 필요합니다")
        }
        fun color(key: String, fallback: Int): Int = p.getProperty(key)?.let(::parseColor) ?: fallback
        val result = base.copy(id=id, name=name, background=color("background",base.background), surface=color("surface",base.surface),
            accent=color("accent",base.accent), secondary=color("secondary",base.secondary), text=color("text",base.text), muted=color("muted",base.muted),
            petals=p.getProperty("petals")?.toInt() ?: base.petals,
            petalSpeed=p.getProperty("petalSpeed")?.toFloat() ?: base.petalSpeed,
            introMillis=p.getProperty("introMillis")?.toLong() ?: base.introMillis,
            layout=base.layout.copy(
                blocks=p.getProperty("layout.blocks")?.split(",")?.map { PlayerBlock.valueOf(it.trim().uppercase()) } ?: base.layout.blocks,
                artworkHeight=p.getProperty("layout.artworkHeight")?.toInt() ?: base.layout.artworkHeight,
                horizontalPadding=p.getProperty("layout.padding")?.toInt() ?: base.layout.horizontalPadding,
                titleSize=p.getProperty("layout.titleSize")?.toInt() ?: base.layout.titleSize,
                playSize=p.getProperty("layout.playSize")?.toInt() ?: base.layout.playSize
            ).validated())
        require(result.petals in 0..96 && result.petalSpeed.isFinite() && result.petalSpeed in .2f..2f) { "벚꽃 설정 범위 오류" }
        require(result.introMillis in 1500..if(result.renderer==ThemeRenderer.ABYSS)12000 else 10000) { "시작 연출 길이 범위 오류" }
        require(luminance(result.background) < .08 && luminance(result.surface) < .12) { "v1은 어두운 배경을 지원합니다" }
        for (bg in listOf(result.background,result.surface)) {
            require(contrast(result.text,bg)>=4.5 && contrast(result.accent,bg)>=4.5 && contrast(result.secondary,bg)>=3 && contrast(result.muted,bg)>=3) { "텍스트/강조색 대비가 부족합니다" }
        }
        return result
    }
    fun encode(t: ThemeSpec): String {
        require(t.name.none { it.isISOControl() }) { "테마 이름에 제어 문자를 사용할 수 없습니다" }
        return """
        schema=2
        id=${t.id}
        name=${t.name.replace("\\","\\\\").replace("=","\\=").replace(":","\\:")}
        renderer=${t.renderer.name.lowercase()}
        background=${hex(t.background)}
        surface=${hex(t.surface)}
        accent=${hex(t.accent)}
        secondary=${hex(t.secondary)}
        text=${hex(t.text)}
        muted=${hex(t.muted)}
        petals=${t.petals}
        petalSpeed=${t.petalSpeed}
        introMillis=${t.introMillis}
        layout.blocks=${t.layout.blocks.joinToString(",") { it.name.lowercase() }}
        layout.artworkHeight=${t.layout.artworkHeight}
        layout.padding=${t.layout.horizontalPadding}
        layout.titleSize=${t.layout.titleSize}
        layout.playSize=${t.layout.playSize}
    """.trimIndent()+"\n"
    }
    fun parseColor(s: String): Int {
        require(s.matches(Regex("#[0-9a-fA-F]{6}"))) { "색상은 #RRGGBB 형식입니다" }
        return (0xff000000L or s.drop(1).toLong(16)).toInt()
    }
    private fun hex(c: Int) = "#%06X".format(c and 0xffffff)
    private fun luminance(c: Int): Double {
        fun linear(v: Int): Double { val s=v/255.0;return if(s<=.04045)s/12.92 else ((s+.055)/1.055).pow(2.4) }
        return .2126*linear(c ushr 16 and 255)+.7152*linear(c ushr 8 and 255)+.0722*linear(c and 255)
    }
    private fun contrast(a: Int,b: Int): Double {val x=luminance(a);val y=luminance(b);return (maxOf(x,y)+.05)/(minOf(x,y)+.05)}
}

object IntroTimeline {
    fun duration(theme: ThemeSpec, mode: IntroMode, scene:IntroScene=IntroScene.THEME) = when(mode) { IntroMode.FULL->if(theme.renderer==ThemeRenderer.HWAHYUN&&scene!=IntroScene.THEME)maxOf(theme.introMillis,if(scene==IntroScene.PETAL_RAIN)9000L else 9500L)else theme.introMillis;IntroMode.SHORT->1800L;IntroMode.OFF->0L }
    fun stage(progress: Float, from: Float, to: Float): Float {
        val t=((progress-from)/(to-from)).coerceIn(0f,1f)
        return t*t*(3-2*t)
    }
}
