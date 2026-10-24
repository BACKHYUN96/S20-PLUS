package app.purebit.storage

/** Serialize private-file transactions across owners; never hold this during provider/network IO. */
class StoreLifetime(private val fileLock:Any) {
    @Volatile var closed=false
        private set
    fun <T> access(work:()->T):T=synchronized(fileLock){
        check(!closed&&!Thread.currentThread().isInterrupted){"저장 작업이 취소됐습니다"}
        work()
    }
    fun offer(work:()->Unit):Boolean=synchronized(fileLock){
        if(closed)return@synchronized false
        work();true
    }
    fun close(cleanup:()->Unit)=synchronized(fileLock){
        if(!closed){closed=true;cleanup()}
    }
}
