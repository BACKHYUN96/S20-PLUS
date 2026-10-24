package app.purebit.library

import java.text.Normalizer
import java.util.Locale

enum class LibraryMode(val label:String){TRACKS("전체 곡"),ALBUMS("앨범"),ARTISTS("아티스트"),FOLDERS("폴더"),PLAYLISTS("재생목록")}
enum class TrackSort(val label:String){TITLE("곡명"),ARTIST("아티스트"),ALBUM("앨범·트랙"),DURATION("재생 길이"),FILE_NAME("파일명")}
data class LibraryGroup(val key:String,val title:String,val detail:String,val tracks:List<LibraryTrack>)

/** Catalogue-only operations. Group identity never comes from a guessed filesystem path. */
object LibraryBrowser {
    private fun key(value:String)=Normalizer.normalize(value.trim(),Normalizer.Form.NFC).lowercase(Locale.ROOT)
    fun search(tracks:List<LibraryTrack>,query:String):List<LibraryTrack>{
        val words=key(query).split(Regex("\\s+")).filter {it.isNotBlank()}
        if(words.isEmpty())return tracks
        return tracks.filter {track->val text=key(listOf(track.title,track.artist,track.album,track.albumArtist,track.displayName,track.folderPath).joinToString("\n"));words.all {text.contains(it)}}
    }
    fun sorted(tracks:List<LibraryTrack>,sort:TrackSort,descending:Boolean):List<LibraryTrack>{
        // Normalize once per row rather than on every comparison in a large catalogue.
        data class Entry(val track:LibraryTrack,val title:String,val artist:String,val album:String,val albumArtist:String,val name:String)
        val cached=tracks.map {Entry(it,key(it.title),key(it.artist),if(sort==TrackSort.ALBUM)key(it.album)else "",if(sort==TrackSort.ALBUM)key(it.albumArtist)else "",key(it.displayName))}
        val title=compareBy<Entry>{it.title}.thenBy {it.artist}.thenBy {it.name}.thenBy {it.track.id}
        val order=when(sort){
            TrackSort.TITLE->title
            TrackSort.ARTIST->compareBy<Entry>{it.artist}.then(title)
            TrackSort.ALBUM->compareBy<Entry>{it.album}.thenBy {it.albumArtist}.thenBy {it.track.discNumber.takeIf {n->n>0}?:1}.thenBy {it.track.trackNumber.takeIf {n->n>0}?:Int.MAX_VALUE}.then(title)
            TrackSort.DURATION->compareBy<Entry>{it.track.durationMs}.then(title)
            TrackSort.FILE_NAME->compareBy<Entry>{it.name}.then(title)
        }.let {if(descending)it.reversed()else it}
        val ordered=if(sort==TrackSort.DURATION)cached.filter {it.track.durationMs>0}.sortedWith(order)+cached.filter {it.track.durationMs<=0}.sortedWith(title)else cached.sortedWith(order)
        return ordered.map {it.track}
    }

    fun artists(tracks:List<LibraryTrack>):List<LibraryGroup> = tracks.groupBy {key(it.artist)}.map {(id,items)->
        LibraryGroup(id,items.first().artist.trim().ifBlank {"알 수 없는 아티스트"},"곡 아티스트 기준",items)
    }.sortedWith(compareBy<LibraryGroup>{key(it.title)}.thenBy {it.key})
    fun folders(tracks:List<LibraryTrack>):List<LibraryGroup> = tracks.groupBy {
        if(it.treeUriString==null)"single" else it.treeUriString+"\u0000"+(it.folderUriString?:"unknown")
    }.map {(id,items)->
        val first=items.first()
        LibraryGroup(id,if(first.treeUriString==null)"개별 추가 파일" else first.folderPath.ifBlank {"경로 정보 없음"},
            if(first.treeUriString==null)"파일 선택기로 추가 · 부모 폴더 미확인" else if(first.folderUriString==null)"폴더 다시 읽기로 경로 정보를 갱신하세요" else "선택한 폴더 안의 실제 하위 경로",items)
    }.sortedWith(compareBy<LibraryGroup>{key(it.title)}.thenBy {it.key})
    fun pageOffset(offset:Int,count:Int,pageSize:Int):Int{
        require(pageSize>0)
        val last=((count.coerceAtLeast(1)-1)/pageSize)*pageSize
        return (offset.coerceAtLeast(0)/pageSize*pageSize).coerceAtMost(last)
    }
}
