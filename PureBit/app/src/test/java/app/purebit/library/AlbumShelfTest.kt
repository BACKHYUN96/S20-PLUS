package app.purebit.library

import org.junit.Assert.*
import org.junit.Test

class AlbumShelfTest {
    private fun song(id:String,artist:String="Solo",album:String="Moon",albumArtist:String="",disc:Int=1,num:Int=0)=LibraryTrack(id,"content://music/$id","$id.flac",id,artist,album,albumArtist=albumArtist,discNumber=disc,trackNumber=num)
    @Test fun compilationStaysTogetherAndDiscTrackNumbersOverrideAlphabeticalOrder(){
        val albums=AlbumShelf.group(listOf(song("z","A",num=1),song("a","B",num=2),song("second",disc=2,num=1)))
        assertEquals(1,albums.size);assertEquals("여러 아티스트",albums.single().artist)
        assertEquals(listOf("z","a","second"),albums.single().tracks.map {it.id})
    }
    @Test fun albumArtistSeparatesEditionsWhileOldCataloguesAndMissingAlbumsWork(){
        val albums=AlbumShelf.group(listOf(song("one",albumArtist="A"),song("two",albumArtist="B"),song("no1",album=""),song("no2",artist="Other",album="")))
        assertEquals(4,albums.size)
        assertEquals(1,AlbumShelf.group(listOf(song("old"),song("other",album=" moon "))).size)
        assertTrue(AlbumShelf.group(emptyList()).isEmpty())
    }
}
