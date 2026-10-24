package app.purebit.library

import org.junit.Assert.*
import org.junit.Test

class LibraryBrowserTest {
    private fun song(id:String,title:String=id,artist:String="Solo",album:String="Moon",duration:Long=100,tree:String?=null,folder:String?=null,path:String="",disc:Int=1,track:Int=0)=
        LibraryTrack(id,"content://music/$id","$id.flac",title,artist,album,duration,treeUriString=tree,discNumber=disc,trackNumber=track,folderUriString=folder,folderPath=path)
    @Test fun searchMatchesAllWordsAcrossTagsAndActualFolderPath(){
        val tracks=listOf(song("a",title="잿불",artist="HOYO-MiX",path="음악 / OST"),song("b",title="Summer",artist="Other"))
        assertEquals(listOf("a"),LibraryBrowser.search(tracks,"hoyo OST 잿불").map {it.id})
        assertTrue(LibraryBrowser.search(tracks,"Summer OST").isEmpty())
        assertEquals(tracks,LibraryBrowser.search(tracks,"  \t "))
    }
    @Test fun artistGroupingHonorsUnicodeEquivalenceAndDoesNotSplitJointCredits(){
        val groups=LibraryBrowser.artists(listOf(song("a",artist=" Café "),song("b",artist="CAFE\u0301"),song("c",artist="A & B"),song("d",artist="")))
        assertEquals(3,groups.size);assertEquals(2,groups.first {it.tracks.any {t->t.id=="a"}}.tracks.size)
        assertTrue(groups.any {it.title=="A & B"});assertTrue(groups.any {it.title=="알 수 없는 아티스트"})
    }
    @Test fun identicalFolderNamesAcrossRootsRemainDistinctAndUnknownParentsAreExplicit(){
        val groups=LibraryBrowser.folders(listOf(song("a",tree="content://tree/1",folder="content://folder/1",path="Music / OST"),song("b",tree="content://tree/2",folder="content://folder/2",path="Music / OST"),song("old",tree="content://tree/1"),song("single")))
        assertEquals(4,groups.size);assertEquals(2,groups.count {it.title=="Music / OST"})
        assertTrue(groups.any {it.title=="개별 추가 파일"&&it.detail.contains("미확인")})
        assertTrue(groups.any {it.detail.contains("다시 읽기")})
    }
    @Test fun missingDurationIsLastForBothDirectionsAndInputIsNotMutated(){
        val tracks=listOf(song("unknown",duration=0),song("short",duration=10),song("long",duration=30))
        assertEquals(listOf("short","long","unknown"),LibraryBrowser.sorted(tracks,TrackSort.DURATION,false).map {it.id})
        assertEquals(listOf("long","short","unknown"),LibraryBrowser.sorted(tracks,TrackSort.DURATION,true).map {it.id})
        assertEquals("unknown",tracks.first().id)
    }
    @Test fun albumSortUsesDiscAndTrackAndTiesRemainDeterministic(){
        val tracks=listOf(song("a",title="First alphabetically",disc=2,track=1),song("z",disc=1,track=1),song("b",disc=1,track=2))
        assertEquals(listOf("z","b","a"),LibraryBrowser.sorted(tracks,TrackSort.ALBUM,false).map {it.id})
        val tied=listOf(song("b",title="Same"),song("a",title="Same"))
        assertEquals(listOf("a","b"),LibraryBrowser.sorted(tied,TrackSort.TITLE,false).map {it.id})
        assertEquals(listOf("b","a"),LibraryBrowser.sorted(tied,TrackSort.FILE_NAME,true).map {it.id})
    }
    @Test fun pagingHandlesEmptyShrinkingAndChangedGridSizes(){
        assertEquals(0,LibraryBrowser.pageOffset(400,0,20))
        assertEquals(80,LibraryBrowser.pageOffset(1000,100,80))
        assertEquals(20,LibraryBrowser.pageOffset(37,100,20))
        assertEquals(0,LibraryBrowser.pageOffset(-20,100,20))
    }
}
