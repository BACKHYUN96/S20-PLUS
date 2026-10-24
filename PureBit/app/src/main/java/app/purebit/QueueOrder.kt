package app.purebit

/** Pure queue edits by stable ID. No edit starts or replaces a playback source. */
object QueueOrder {
    fun append(ids:List<String>,added:List<String>)=(ids+added).distinct()
    fun move(ids:List<String>,from:String,to:String):List<String> {
        val a=ids.indexOf(from);val b=ids.indexOf(to)
        if(a<0||b<0||a==b)return ids
        return ids.toMutableList().apply {add(b,removeAt(a))}
    }
    fun remove(ids:List<String>,current:String?,id:String)=if(id==current)ids else ids.filter {it!=id}
    fun next(ids:List<String>,current:String?,id:String):List<String> {
        if(id==current)return ids
        val result=ids.filter {it!=id}.toMutableList()
        result.add((result.indexOf(current)+1).coerceIn(0,result.size),id)
        return result
    }
}
