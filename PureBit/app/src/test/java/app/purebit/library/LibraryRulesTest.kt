package app.purebit.library

import org.junit.Assert.*
import org.junit.Test

class LibraryRulesTest {
    @Test fun completeScanMarksOnlyUnseenTracksInItsOwnTreeUnavailable() {
        val a=track("content://test/a","A").copy(treeUriString="tree-a")
        val b=track("content://test/b","B").copy(treeUriString="tree-a",available=false)
        val other=track("content://test/c","C").copy(treeUriString="tree-b")
        val result=LibraryRules.reconcileScan(listOf(a,b,other),listOf(b.copy(available=true)),"tree-a",false).associateBy {it.id}
        assertFalse(result.getValue(a.id).available)
        assertTrue(result.getValue(b.id).available)
        assertEquals(other,result.getValue(other.id))
    }
    @Test fun cappedScanPreservesUnvisitedTracksLastKnownAvailability() {
        val a=track("content://test/a","A").copy(treeUriString="tree-a")
        val b=track("content://test/b","B").copy(treeUriString="tree-a",available=false)
        val c=track("content://test/c","C").copy(treeUriString="tree-a",available=false)
        val result=LibraryRules.reconcileScan(listOf(a,b,c),listOf(b.copy(title="Updated",available=true)),"tree-a",true).associateBy {it.id}
        assertEquals(a,result.getValue(a.id))
        assertTrue(result.getValue(b.id).available)
        assertEquals("Updated",result.getValue(b.id).title)
        assertFalse(result.getValue(c.id).available)
    }
    @Test fun scanReconciliationPreservesIdentityAndDoesNotMutateItsInputs() {
        val original=track("content://test/a","A").copy(treeUriString="tree-a")
        val existing=listOf(original);val found=listOf(original.copy(title="New"))
        val result=LibraryRules.reconcileScan(existing,found,"tree-a",false)
        assertEquals(listOf(original),existing)
        assertEquals(1,result.size);assertEquals(original.id,result.single().id)
        assertEquals("New",result.single().title);assertTrue(result.single().available)
    }
    @Test fun onlyLosslessDocumentTypesEnterTheCatalogue() {
        assertTrue(LibraryRules.isSupported("TRACK.FLAC", "application/octet-stream"))
        assertTrue(LibraryRules.isSupported("song.WaV", null))
        assertTrue(LibraryRules.isSupported("track", "audio/x-flac"))
        assertFalse(LibraryRules.isSupported("song.mp3", "audio/flac"))
        assertFalse(LibraryRules.isSupported("track", "audio/mpeg"))
        assertFalse(LibraryRules.isSupported("cover.jpg", "image/jpeg"))
    }

    @Test fun duplicateDocumentKeepsQueueIdentityAndUpdatedMetadata() {
        val old = track("content://test/document/one", "Old tag")
        val updated = old.copy(title = "New tag", available = true)
        val merged = LibraryRules.merge(listOf(old.copy(available = false)), listOf(updated))
        assertEquals(1, merged.size)
        assertEquals(old.id, merged.single().id)
        assertEquals("New tag", merged.single().title)
        assertTrue(merged.single().available)
    }

    @Test fun sameFilenameInDifferentDirectoriesIsNotADuplicate() {
        val first = track("content://test/document/folder-a/song.flac", "Song")
        val second = track("content://test/document/folder-b/song.flac", "Song")
        assertNotEquals(first.id, second.id)
        assertEquals(2, LibraryRules.merge(listOf(first), listOf(second)).size)
        assertEquals(LibraryRules.sorted(listOf(first, second)), LibraryRules.sorted(listOf(second, first)))
    }

    private fun track(uri: String, title: String) = LibraryTrack(LibraryRules.stableId(uri), uri, "song.flac", title)
}
