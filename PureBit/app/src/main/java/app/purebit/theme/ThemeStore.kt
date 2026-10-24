package app.purebit.theme

import android.content.Context
import android.util.AtomicFile
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction

class ThemeStore(context: Context) {
    private val app=context.applicationContext
    private val prefs=app.getSharedPreferences("bitwire_ui",Context.MODE_PRIVATE)
    private val directory=File(app.filesDir,"themes-v1")
    companion object { private val lock=Any() }
    // Adding a supported-family preset needs only an asset file, not an Activity edit.
    private val bundled: List<ThemeSpec> by lazy {
        BuiltInThemes.all + app.assets.list("themes").orEmpty()
            .filter { it.endsWith(".bitwire-theme") }.sorted().take(32).mapNotNull { name ->
                runCatching { app.assets.open("themes/$name").use { ThemeCodec.decode(read(it)) } }.getOrNull()
            }.distinctBy { it.id }
    }
    fun all(): List<ThemeSpec> = synchronized(lock) {
        bundled + directory.listFiles().orEmpty().filter { !it.name.endsWith(".new") }.map { it.name.removeSuffix(".bak") }.filter { it.endsWith(".bitwire-theme") }.distinct().sorted().take(16).mapNotNull { name ->
            val file=File(directory,name)
            runCatching { AtomicFile(file).openRead().use { ThemeCodec.decode(read(it)) } }.getOrNull()?.takeIf { theme -> bundled.none { it.id==theme.id } }
        }
    }
    fun selected() = all().firstOrNull { it.id==prefs.getString("theme_id","original") } ?: BuiltInThemes.original
    fun select(theme: ThemeSpec) { require(all().any { it==theme });prefs.edit().putString("theme_id",theme.id).apply() }
    fun introMode(): IntroMode = runCatching {
        IntroMode.valueOf(prefs.getString("intro_mode",if(prefs.getBoolean("startup",true))"FULL" else "OFF")!!)
    }.getOrDefault(IntroMode.FULL)
    fun introScene(spec:ThemeSpec=selected()):IntroScene=if(spec.renderer==ThemeRenderer.HWAHYUN)IntroScene.decode(prefs.getString("hwahyun_intro_scene",null))else IntroScene.THEME
    fun setIntroScene(scene:IntroScene){prefs.edit().putString("hwahyun_intro_scene",scene.name).apply()}
    fun setIntroMode(mode: IntroMode) { prefs.edit().putString("intro_mode",mode.name).apply() }
    fun importTheme(input: InputStream): ThemeSpec = synchronized(lock) {
        val theme=ThemeCodec.decode(read(input))
        require(bundled.none { it.id==theme.id }) { "앱에 포함된 테마 ID는 덮어쓸 수 없습니다" }
        directory.mkdirs()
        val file=File(directory,"${theme.id}.bitwire-theme")
        require(file.exists() || directory.listFiles().orEmpty().count { it.name.endsWith(".bitwire-theme") }<16) { "가져온 테마는 최대 16개입니다" }
        val atomic=AtomicFile(file);val out=atomic.startWrite()
        try { out.write(ThemeCodec.encode(theme).toByteArray(Charsets.UTF_8));atomic.finishWrite(out) }
        catch(e:Throwable){atomic.failWrite(out);throw e}
        theme
    }
    private fun read(input: InputStream): String {
        val buffer=ByteArray(ThemeCodec.MAX_BYTES+1);var length=0
        while(length<buffer.size){val n=input.read(buffer,length,buffer.size-length);if(n<0)break;if(n==0){val b=input.read();if(b<0)break;buffer[length++]=b.toByte()}else length+=n}
        require(length<=ThemeCodec.MAX_BYTES) { "테마 파일은 32 KiB 이하여야 합니다" }
        val bytes=buffer.copyOf(length)
        return Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString()
    }
}
