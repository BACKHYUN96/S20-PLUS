package app.purebit

import kotlin.random.Random

/** One shuffle pass. Queue edits preserve unplayed survivors and add only genuinely new IDs. */
class ShuffleCycle(private val random:Random=Random.Default) {
    var remaining:List<String> = emptyList()
        private set
    fun reset(ids:List<String>,current:String?) {
        remaining=ids.distinct().filter {it!=current}.shuffled(random)
    }
    fun reconcile(before:List<String>,after:List<String>,current:String?) {
        val old=before.toHashSet();val valid=after.toHashSet()
        val survivors=remaining.filter {it in valid&&it!=current}
        val added=after.distinct().filter {it !in old&&it!=current}.shuffled(random)
        remaining=survivors+added
    }
    fun consume(id:String){remaining=remaining.filter {it!=id}}
}
