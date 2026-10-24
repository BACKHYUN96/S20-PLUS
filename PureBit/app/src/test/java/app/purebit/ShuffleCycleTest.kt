package app.purebit

import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class ShuffleCycleTest {
    @Test fun resetExcludesCurrentAndDuplicateIds() {
        val cycle=ShuffleCycle(Random(7))
        cycle.reset(listOf("a","b","b","c"),"a")
        assertEquals(setOf("b","c"),cycle.remaining.toSet())
        assertEquals(2,cycle.remaining.size)
    }
    @Test fun movingTheQueueDoesNotReintroducePlayedTracksOrReshuffleSurvivors() {
        val cycle=ShuffleCycle(Random(12));val ids=listOf("a","b","c","d")
        cycle.reset(ids,"a");val played=cycle.remaining.first();cycle.consume(played)
        val unplayed=cycle.remaining.toList()
        cycle.reconcile(ids,ids.reversed(),played)
        assertEquals(unplayed,cycle.remaining)
        assertFalse("a" in cycle.remaining);assertFalse(played in cycle.remaining)
    }
    @Test fun appendAddsOnlyNewTracksOnceWithoutResettingThePass() {
        val cycle=ShuffleCycle(Random(3));val ids=listOf("a","b","c")
        cycle.reset(ids,"a");cycle.consume("b")
        val prior=cycle.remaining.toList();val expanded=ids+listOf("d","e","d")
        cycle.reconcile(ids,expanded,"b")
        assertEquals(prior,cycle.remaining.take(prior.size))
        assertEquals(setOf("c","d","e"),cycle.remaining.toSet())
        assertEquals(3,cycle.remaining.size)
        val same=cycle.remaining.toList();cycle.reconcile(expanded,expanded,"b")
        assertEquals(same,cycle.remaining)
    }
    @Test fun removedCandidatesDisappearAndAReaddedTrackIsANewExplicitAddition() {
        val cycle=ShuffleCycle(Random(5));val ids=listOf("a","b","c","d")
        cycle.reset(ids,"a");val reduced=ids.filter {it!="c"}
        cycle.reconcile(ids,reduced,"a");assertFalse("c" in cycle.remaining)
        val survivors=cycle.remaining.toList()
        cycle.reconcile(reduced,reduced+"c","a")
        assertEquals(survivors+"c",cycle.remaining)
    }
    @Test fun editingAnExhaustedPassDoesNotSilentlyStartAnotherPass() {
        val cycle=ShuffleCycle(Random(1));val ids=listOf("a","b","c")
        cycle.reset(ids,"a");cycle.consume("b");cycle.consume("c")
        cycle.reconcile(ids,listOf("c","a","b"),"c")
        assertTrue(cycle.remaining.isEmpty())
        cycle.reset(ids,"c") // The queue chooses this only when a fresh pass is authorized.
        assertEquals(setOf("a","b"),cycle.remaining.toSet())
    }
    @Test fun repeatedEditsPreserveTheNoRepeatInvariantWithinAPass() {
        val random=Random(77);val cycle=ShuffleCycle(Random(23))
        var ids=(0..12).map {"track$it"};var current="track0";val played=mutableSetOf(current)
        cycle.reset(ids,current)
        repeat(100){i->
            cycle.remaining.firstOrNull()?.let {current=it;played+=it;cycle.consume(it)}
            val before=ids
            if(i%3==0)ids=ids+"new$i"
            if(i%4==0)ids=ids.filterIndexed {index,id->index!=0||id==current}
            ids=ids.shuffled(random)
            cycle.reconcile(before,ids,current)
            assertTrue(cycle.remaining.none {it in played})
            assertTrue(cycle.remaining.all {it in ids&&it!=current})
            assertEquals(cycle.remaining.size,cycle.remaining.distinct().size)
        }
    }
}
