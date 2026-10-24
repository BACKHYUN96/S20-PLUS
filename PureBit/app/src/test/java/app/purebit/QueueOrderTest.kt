package app.purebit

import org.junit.Assert.*
import org.junit.Test

class QueueOrderTest {
    @Test fun movingAcrossCurrentAndDeletingOthersPreservesCurrentIdentity(){
        var ids=listOf("a","playing","b","c")
        ids=QueueOrder.move(ids,"c","a");assertEquals(listOf("c","a","playing","b"),ids)
        ids=QueueOrder.remove(ids,"playing","playing");assertTrue("playing" in ids)
        ids=QueueOrder.remove(ids,"playing","a");assertEquals(listOf("c","playing","b"),ids)
        assertEquals(ids,QueueOrder.move(ids,"deleted","b"))
    }
    @Test fun playNextMovesExistingOrInsertsNewWithoutDuplicatesOrChangingCurrent(){
        var ids=listOf("before","playing","later","end")
        ids=QueueOrder.next(ids,"playing","end");assertEquals(listOf("before","playing","end","later"),ids)
        ids=QueueOrder.next(ids,"playing","new");assertEquals(listOf("before","playing","new","end","later"),ids)
        assertEquals(ids,QueueOrder.next(ids,"playing","playing"))
        assertEquals(ids,QueueOrder.next(ids,"playing","new"))
        assertEquals(listOf("first"),QueueOrder.next(emptyList(),null,"first"))
    }
    @Test fun arbitraryEditsNeverDuplicateOrDropUnrelatedIds(){
        var ids=(0..99).map {"id$it"};val expected=ids.toSet()
        repeat(200){i->ids=QueueOrder.move(ids,"id${i%100}","id${(i*13)%100}");ids=QueueOrder.next(ids,"id0","id${i%100}")
            assertEquals(expected,ids.toSet());assertEquals(100,ids.size)}
    }
}
