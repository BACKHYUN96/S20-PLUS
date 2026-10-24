package app.purebit.storage

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.CharacterCodingException
import org.junit.Assert.*
import org.junit.Test

class MetadataInputTest {
    @Test fun emptyAndExactLimitStreamsAreAcceptedWithoutUsingAvailable() {
        assertArrayEquals(byteArrayOf(),MetadataInput.read(ByteArrayInputStream(byteArrayOf()),0))
        val input=object:ByteArrayInputStream(byteArrayOf(1,2,3,4)){override fun available()=0}
        assertArrayEquals(byteArrayOf(1,2,3,4),MetadataInput.read(input,4))
    }
    @Test fun oversizeStreamIsRejectedAfterAtMostOneExtraByte() {
        val input=ByteArrayInputStream(ByteArray(1000))
        assertThrows(IllegalArgumentException::class.java){MetadataInput.read(input,4)}
        assertEquals(995,input.available())
        assertThrows(IllegalArgumentException::class.java){MetadataInput.read(ByteArrayInputStream(byteArrayOf(1)),0)}
    }
    @Test fun zeroLengthBulkReadsStillMakeProgressAndEnforceTheLimit() {
        fun input()=object:InputStream(){var next=0;override fun read()=if(next<3)++next else -1
            override fun read(b:ByteArray,off:Int,len:Int)=0}
        assertArrayEquals(byteArrayOf(1,2,3),MetadataInput.read(input(),3))
        assertThrows(IllegalArgumentException::class.java){MetadataInput.read(input(),2)}
    }
    @Test fun readFailureDoesNotReturnAPartialSuccess() {
        val input=object:InputStream(){var next=0;override fun read():Int {
            if(next++==0)return 1
            throw IOException("simulated interrupted metadata read")
        }}
        assertThrows(IOException::class.java){MetadataInput.read(input,32)}
    }
    @Test fun unicodeIsPreservedAndMalformedUtf8IsRejected() {
        val text="달빛 café 日本語 🌸"
        assertEquals(text,MetadataInput.utf8(text.toByteArray(Charsets.UTF_8)))
        assertThrows(CharacterCodingException::class.java){MetadataInput.utf8(byteArrayOf(0xc3.toByte(),0x28))}
    }
    @Test fun atomicBackupsAreDiscoveredButUncommittedFilesDoNotBecomeSavedLists() {
        assertEquals(listOf("a.bwlist","b.bwlist"),AtomicMetadataNames.committed(listOf(
            "a.bwlist","a.bwlist.bak","a.bwlist.new","b.bwlist.bak","c.bwlist.new",
            "notes.txt","d.bwlist.bak.new","e.bwlist.bak.bak"),".bwlist"))
        assertTrue(AtomicMetadataNames.committed(listOf("only.bwlist.new"),".bwlist").isEmpty())
    }
}
