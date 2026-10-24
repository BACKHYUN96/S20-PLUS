package app.purebit.startup

/** A pending launch alone is not a crash: task removal and low-memory kills are ordinary. */
object StartupPolicy {
    enum class Destination { PLAYER, SAFE_PLAYER, RECOVERY }
    fun confirmedStartupCrash(startedAt:Long,processId:Int,exitAt:Long,exitPid:Int,crashOrAnr:Boolean)=
        startedAt>0 && processId>0 && processId==exitPid && exitAt>=startedAt && crashOrAnr
    fun destination(latched:Boolean,safeFailed:Boolean)=when {
        safeFailed->Destination.RECOVERY
        latched->Destination.SAFE_PLAYER
        else->Destination.PLAYER
    }
}
