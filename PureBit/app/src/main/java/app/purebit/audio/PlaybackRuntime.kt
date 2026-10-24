package app.purebit.audio

import android.content.Context
import android.content.Intent
import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper

/** One process-owned engine; Activity recreation and screen lock do not replace the source/session. */
object PlaybackRuntime {
    @SuppressLint("StaticFieldLeak") // Engine retains applicationContext only, for process-owned foreground playback.
    private var instance: AudioEngine? = null
    private val main = Handler(Looper.getMainLooper())
    private val ownership=PlaybackOwnership()
    internal val serviceRunning get()=ownership.ready
    val hasPendingSource get()=ownership.hasPending
    fun cancelPendingSource()=ownership.cancelPending()
    @Volatile var onNext: (() -> Unit)? = null
    @Volatile var onPrevious: (() -> Unit)? = null
    @Volatile var onEnded: (() -> Unit)? = null

    @Synchronized fun engine(context: Context): AudioEngine = instance ?: AudioEngine(context.applicationContext).also {
        val engine = it
        it.onSourceControlChanged={ownership.cancelPending()}
        it.onEnded = { completion -> main.post { if (engine.isCurrentCompletion(completion)) onEnded?.invoke() } }
        instance = it
    }
    fun ensureService(context: Context):Boolean {
        if(serviceRunning)return true
        return runCatching {context.applicationContext.startForegroundService(Intent(context,PlaybackService::class.java));true}
            .getOrElse {engine(context).serviceUnavailable(it);false}
    }
    fun whenServiceReady(context:Context,work:()->Unit){
        val ticket=ownership.submit(work)?:return
        if(!ensureService(context))ownership.cancel(ticket)
    }
    internal fun serviceStarted(token:Any)=ownership.started(token)
    internal fun serviceStopping(token:Any)=ownership.stopping(token)
    internal fun serviceStopped(token:Any)=ownership.stopped(token)
    internal fun ownsService(token:Any)=ownership.owns(token)
    internal fun action(context: Context, action: String) {
        val intent = Intent(context.applicationContext, PlaybackService::class.java).setAction(action)
        runCatching {
            if (serviceRunning) context.applicationContext.startService(intent)
            else context.applicationContext.startForegroundService(intent)
        }.onFailure {ownership.cancelPending();engine(context).serviceUnavailable(it)}
    }
    fun toggle(context: Context) = action(context, PlaybackService.ACTION_TOGGLE)
    fun play(context: Context) = action(context, PlaybackService.ACTION_PLAY)
    fun pause(context: Context) = action(context, PlaybackService.ACTION_PAUSE)
    // MediaSession/Service callbacks already run on main. Posting again could move NEXT after PAUSE/STOP.
    private fun dispatch(action:()->Unit) {
        if(Looper.myLooper()==main.looper)action() else main.post {action()}
    }
    internal fun next() = dispatch { onNext?.invoke() }
    internal fun previous() = dispatch { onPrevious?.invoke() }
}
