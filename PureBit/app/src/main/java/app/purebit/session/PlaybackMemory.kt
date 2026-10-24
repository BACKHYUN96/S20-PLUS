package app.purebit.session

import android.content.Context

/** Tiny private bookmark. No auto-play, URI access or output settings are restored here. */
class PlaybackMemory(context:Context){
    private val prefs=context.applicationContext.getSharedPreferences("bitwire_resume",Context.MODE_PRIVATE)
    data class Bookmark(val id:String,val positionMs:Long)
    fun read():Bookmark? {
        val id=prefs.getString("track_id",null)?:return null
        if(!id.matches(Regex("[0-9a-f]{64}")))return null
        return Bookmark(id,prefs.getLong("position_ms",0).coerceAtLeast(0))
    }
    fun save(id:String,positionMs:Long){prefs.edit().putString("track_id",id).putLong("position_ms",positionMs.coerceAtLeast(0)).apply()}
}
