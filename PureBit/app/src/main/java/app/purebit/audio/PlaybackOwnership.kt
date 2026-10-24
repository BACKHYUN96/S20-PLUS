package app.purebit.audio

/** Main-thread foreground ownership. Deferred source starts never run on a retiring owner. */
class PlaybackOwnership {
    class Ticket internal constructor(internal val work:()->Unit)
    @Volatile var ready=false
        private set
    private var owner:Any?=null
    private val pending=ArrayDeque<Ticket>()
    val hasPending get()=synchronized(this){pending.isNotEmpty()}
    @Synchronized fun owns(token:Any)=ready&&owner===token
    @Synchronized fun submit(work:()->Unit):Ticket? {
        if(ready){work();return null}
        pending.clear() // A newer explicit source selection supersedes the older deferred selection.
        return Ticket(work).also {pending.addLast(it)}
    }
    @Synchronized fun cancel(ticket:Ticket){pending.remove(ticket)}
    @Synchronized fun cancelPending(){pending.clear()}
    @Synchronized fun started(token:Any){
        owner=token;ready=true
        while(ready&&pending.isNotEmpty())pending.removeFirst().work()
    }
    @Synchronized fun stopping(token:Any){if(owner===token)ready=false}
    @Synchronized fun stopped(token:Any):Boolean {
        if(owner!==token)return false
        owner=null;ready=false;return true
    }
}
