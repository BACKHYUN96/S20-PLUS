package app.purebit.library

import java.util.Locale

data class AlbumGroup(val key:String,val title:String,val artist:String,val tracks:List<LibraryTrack>)

object AlbumShelf {
    private fun normalized(s:String)=s.trim().lowercase(Locale.ROOT)
    fun group(tracks:List<LibraryTrack>):List<AlbumGroup> = tracks.groupBy {
        // Album artist distinguishes editions; an untagged album stays with its track artist.
        val owner=if(it.album.isBlank())it.artist else it.albumArtist
        listOf(normalized(it.album),normalized(owner)).joinToString("\u0000")
    }.map { (key,items) ->
        val ordered=items.sortedWith(compareBy<LibraryTrack>{it.discNumber.takeIf { n->n>0 }?:1}
            .thenBy {it.trackNumber.takeIf { n->n>0 }?:Int.MAX_VALUE}.thenBy {normalized(it.title)}.thenBy {it.id})
        val artists=items.map {it.albumArtist.ifBlank {it.artist}}.filter {it.isNotBlank()}.distinct()
        AlbumGroup(key,items.first().album.ifBlank {"앨범 정보 없음"},if(artists.size==1)artists.first() else if(artists.isEmpty())"알 수 없는 아티스트" else "여러 아티스트",ordered)
    }.sortedWith(compareBy<AlbumGroup>{normalized(it.title)}.thenBy {normalized(it.artist)}.thenBy {it.key})
}
