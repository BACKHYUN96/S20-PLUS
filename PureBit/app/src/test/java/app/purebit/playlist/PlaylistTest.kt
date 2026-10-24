package app.purebit.playlist

import app.purebit.QueueOrder
import org.junit.Assert.*
import org.junit.Test

class PlaylistTest {
    private val id="12345678-1234-1234-1234-123456789abc"
    private fun track(n:Int)=n.toString(16).padStart(64,'0')
    private fun saved(ids:List<String> = listOf(track(3),track(1),track(2)))=SavedPlaylist(id,"벚꽃 밤 / 日本語 & café",7,1234567,ids)
    @Test fun codecPreservesUnicodeNameOrderVersionAndEmptyLists(){
        for(value in listOf(saved(),saved(emptyList())))assertEquals(value,PlaylistCodec.decode(PlaylistCodec.encode(value).toByteArray(Charsets.UTF_8)))
    }
    @Test fun truncationAndChangedPayloadCannotSilentlyLoseTracks(){
        val encoded=PlaylistCodec.encode(saved())
        assertThrows(IllegalArgumentException::class.java){PlaylistCodec.decode(encoded.dropLast(10).toByteArray())}
        assertThrows(IllegalArgumentException::class.java){PlaylistCodec.decode(encoded.replace(track(3),track(4)).toByteArray())}
        assertThrows(IllegalArgumentException::class.java){PlaylistCodec.decode(encoded.replace(track(3)+"\n","").toByteArray())}
    }
    @Test fun invalidEncodingVersionAndOversizeAreRejected(){
        assertThrows(Exception::class.java){PlaylistCodec.decode(byteArrayOf(0xc3.toByte(),0x28))}
        assertThrows(IllegalArgumentException::class.java){PlaylistCodec.decode(PlaylistCodec.encode(saved()).replace("BITWIRE-PLAYLIST-1","BITWIRE-PLAYLIST-2").toByteArray())}
        assertThrows(IllegalArgumentException::class.java){PlaylistCodec.decode(ByteArray(PlaylistCodec.MAX_BYTES+1))}
    }
    @Test fun namesAndIdsAreValidatedInsteadOfUsedAsPaths(){
        for(name in listOf("", "x\ny", "x\u2028y", "a".repeat(81)))assertThrows(IllegalArgumentException::class.java){PlaylistRules.name(name)}
        assertEquals("달밤",PlaylistRules.name(" 달밤 "))
        assertEquals(PlaylistRules.normalizedName(" Café "),PlaylistRules.normalizedName("CAFE\u0301"))
        assertThrows(IllegalArgumentException::class.java){PlaylistCodec.encode(saved().copy(id="../../outside"))}
        assertThrows(IllegalArgumentException::class.java){PlaylistCodec.encode(saved().copy(trackIds=listOf("content://music/a")))}
        assertThrows(IllegalArgumentException::class.java){PlaylistCodec.encode(saved(listOf(track(1),track(1))))}
    }
    @Test fun editsPreserveUniqueOrderAndRejectStaleRevision(){
        val ids=listOf(track(1),track(2),track(3))
        assertEquals(listOf(track(1),track(2),track(3),track(4)),PlaylistRules.append(ids,listOf(track(2),track(4))))
        assertEquals(listOf(track(3),track(1),track(2)),PlaylistRules.move(ids,track(3),track(1)))
        assertEquals(listOf(track(1),track(2),track(3)),ids)
        PlaylistRules.requireRevision(8,8)
        assertThrows(IllegalArgumentException::class.java){PlaylistRules.requireRevision(8,7)}
    }
    @Test fun trackCountLimitIsAtomicAndQueueAppendDoesNotReorderExistingMusic(){
        val full=(0 until PlaylistRules.MAX_TRACKS).map(::track)
        assertEquals(full,PlaylistRules.append(full,listOf(full.first())))
        assertThrows(IllegalArgumentException::class.java){PlaylistRules.append(full,listOf(track(20000)))}
        assertEquals(listOf("current","next","later","new"),QueueOrder.append(listOf("current","next","later"),listOf("next","new","new")))
    }
}
