package app.purebit.library

import android.content.Context

/** Stable URI-derived IDs; rescan does not discard favourites or modify music tags. */
class FavoritesStore(context:Context){
    private val prefs=context.applicationContext.getSharedPreferences("bitwire_favorites",Context.MODE_PRIVATE)
    fun contains(id:String)=prefs.getStringSet("ids",emptySet()).orEmpty().contains(id)
    fun ids():Set<String> = prefs.getStringSet("ids",emptySet()).orEmpty().toSet()
    fun toggle(id:String):Boolean=synchronized(lock){
        require(id.matches(Regex("[0-9a-f]{64}")))
        val next=ids().toMutableSet();val added=if(next.remove(id))false else {require(next.size<10000){"즐겨찾기는 최대 10,000곡입니다"};next.add(id);true}
        prefs.edit().putStringSet("ids",next).apply();added
    }
    companion object {private val lock=Any()}
}
