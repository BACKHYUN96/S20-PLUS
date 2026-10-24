package app.purebit

import android.content.Context
import app.purebit.audio.PlaybackRuntime
import app.purebit.session.*
import android.os.SystemClock
import app.purebit.library.LibraryTrack

/** Process-scoped queue; callbacks never retain an Activity. Only explicit play/next arms playback. */
object PlaybackQueue {
    enum class Repeat { OFF, ALL, ONE }
    private var application: Context? = null
    private var memory:PlaybackMemory?=null
    private var restored=false
    var pendingPositionMs:Long?=null;private set
    var pendingRestored=false;private set
    var restoreNotice="";private set
    private var checkpointAt=0L
    private var checkpointKey=""
    private var wasPlaying=false
    fun restoreSelection(all:List<LibraryTrack>){
        if(restored)return
        restored=true
        val ctx=application?:return
        if(entries.isNotEmpty()||PlaybackRuntime.engine(ctx).state.sourceUri.isNotBlank())return
        val bookmark=memory?.read()?:return
        val track=all.firstOrNull {it.id==bookmark.id&&it.available}
        if(track==null){restoreNotice="마지막 곡에 접근할 수 없습니다. 음악 폴더나 파일 권한을 확인하세요.";return}
        entries=all.filter {it.available};index=entries.indexOfFirst {it.id==track.id};revision++
        pendingPositionMs=bookmark.positionMs;pendingRestored=true
        resetShuffle()
    }
    fun resumeIfPending():Boolean {
        val ctx=application?:return false
        val position=pendingPositionMs?:return false
        val track=current?:return false
        val engine=PlaybackRuntime.engine(ctx)
        if(engine.state.sourceUri.isNotBlank()||engine.state.loading||engine.state.pcm!=null)return false
        markRecent(track)
        PlaybackRuntime.whenServiceReady(ctx){
            pendingPositionMs=null;pendingRestored=false
            engine.load(track.uri,track.displayName,autoPlay=true,resumePositionMs=position)
        }
        return true
    }
    fun checkpoint(force:Boolean=false){
        val ctx=application?:return;val track=current?:return
        val state=PlaybackRuntime.engine(ctx).state
        if(!ResumeRules.canCheckpoint(track.uriString,state.sourceUri,state.comparisonRate,state.loading,state.pcm!=null))return
        val now=SystemClock.elapsedRealtime();val key=track.id+":"+state.sourceGeneration
        if(force||key!=checkpointKey||wasPlaying!=state.playing||now-checkpointAt>=5000){
            memory?.save(track.id,state.positionMs);checkpointAt=now;checkpointKey=key;wasPlaying=state.playing
        }
    }
    private var entries = emptyList<LibraryTrack>()
    private var index = -1
    var revision=0L;private set
    private var priorityNext=emptyList<String>()
    var repeat = Repeat.OFF
    private val shuffleCycle=ShuffleCycle()
    var shuffle = false
        set(value){field=value;resetShuffle()}
    private fun resetShuffle(){shuffleCycle.reset(entries.map {it.id},current?.id)}
    val tracks: List<LibraryTrack> get() = entries
    val current: LibraryTrack? get() = entries.getOrNull(index)
    fun bind(context: Context) {
        application = context.applicationContext
        if(memory==null)memory=PlaybackMemory(context)
        PlaybackRuntime.onNext = { next() }
        PlaybackRuntime.onPrevious = { previous() }
        PlaybackRuntime.onEnded = { next(automatic = true) }
    }
    fun select(track: LibraryTrack, candidates: List<LibraryTrack>, start: Boolean) {
        val ctx = application ?: return
        PlaybackRuntime.cancelPendingSource()
        checkpoint(true);pendingPositionMs=null;pendingRestored=false;restoreNotice=""
        entries = candidates.filter { it.available }.ifEmpty { listOf(track) }
        index = entries.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        priorityNext=emptyList();revision++
        resetShuffle()
        if (start) {
            markRecent(track)
            PlaybackRuntime.whenServiceReady(ctx){PlaybackRuntime.engine(ctx).load(track.uri,track.displayName,autoPlay=true)}
        }else PlaybackRuntime.engine(ctx).load(track.uri,track.displayName,autoPlay=false)
    }
    fun recentIds(context: Context): List<String> = context.applicationContext.getSharedPreferences("bitwire_history", Context.MODE_PRIVATE).getString("ids", "")!!.split(',').filter { it.isNotBlank() }
    fun markCurrent() {
        val ctx=application?:return;val engine=PlaybackRuntime.engine(ctx)
        current?.takeIf {engine.hasCurrentSource(it.uriString)&&engine.state.comparisonRate==0}?.let(::markRecent)
    }
    private fun markRecent(track: LibraryTrack) {
        val ctx=application ?: return
        val ids=(listOf(track.id)+recentIds(ctx).filter { it!=track.id }).take(30)
        ctx.getSharedPreferences("bitwire_history", Context.MODE_PRIVATE).edit().putString("ids",ids.joinToString(",")).apply()
    }
    private fun applyOrder(ids:List<String>,additional:LibraryTrack?=null){
        val before=entries.map {it.id}
        val selected=current?.id;val byId=entries.associateBy {it.id}.toMutableMap()
        additional?.let {byId[it.id]=it}
        val next=ids.mapNotNull {byId[it]}
        if(next==entries)return
        entries=next;index=entries.indexOfFirst {it.id==selected};revision++
        preparePendingSelection()
        priorityNext=priorityNext.filter {id->entries.any {it.id==id}}
        shuffleCycle.reconcile(before,entries.map {it.id},current?.id)
    }
    /** Stage the first queued music silently; a diagnostic source is never replaced by an edit. */
    private fun preparePendingSelection(){
        val ctx=application?:return
        if(index<0&&entries.isNotEmpty()&&PlaybackRuntime.engine(ctx).state.sourceUri.isBlank()){
            index=0;pendingPositionMs=0;pendingRestored=false;restoreNotice=""
        }
    }
    fun move(from:Int,to:Int){
        val a=entries.getOrNull(from)?.id?:return;val b=entries.getOrNull(to)?.id?:return
        val ids=QueueOrder.move(entries.map {it.id},a,b)
        if(ids!=entries.map {it.id})priorityNext=emptyList()
        applyOrder(ids)
    }
    fun moveBeforeDrop(from:String,to:String,expectedRevision:Long){
        if(revision==expectedRevision){
            val ids=QueueOrder.move(entries.map {it.id},from,to)
            if(ids!=entries.map {it.id})priorityNext=emptyList()
            applyOrder(ids)
        }
    }
    fun remove(id:String):Boolean {
        if(id==current?.id)return false
        val exists=entries.any {it.id==id};applyOrder(QueueOrder.remove(entries.map {it.id},current?.id,id));return exists
    }
    fun enqueueNext(track:LibraryTrack){
        if(!track.available||track.id==current?.id)return
        applyOrder(QueueOrder.next(entries.map {it.id},current?.id,track.id),track)
        priorityNext=(listOf(track.id)+priorityNext.filter {it!=track.id}).filter {it!=current?.id};revision++
    }
    /** Adds at the end without changing the active source, device, volume or selected music. */
    fun append(tracks:List<LibraryTrack>):Int {
        if(application==null)return 0
        val currentIds=entries.map {it.id};val known=currentIds.toSet()
        val added=tracks.filter {it.available&&it.id !in known}.distinctBy {it.id}
        if(added.isEmpty())return 0
        restoreNotice=""
        val selected=current?.id
        val byId=(entries+added).associateBy {it.id}
        entries=QueueOrder.append(currentIds,added.map {it.id}).mapNotNull {byId[it]}
        index=entries.indexOfFirst {it.id==selected}
        preparePendingSelection()
        revision++;shuffleCycle.reconcile(currentIds,entries.map {it.id},current?.id)
        return added.size
    }
    fun keepCurrent():Int {
        val selected=current?:return 0
        val removed=entries.size-1;if(removed<=0)return 0
        entries=listOf(selected);index=0;priorityNext=emptyList();revision++;resetShuffle()
        return removed
    }
    fun next(automatic: Boolean = false) {
        val ctx = application ?: return
        val engine = PlaybackRuntime.engine(ctx)
        if (entries.isEmpty()) return
        // Generated diagnostic sources do not advance a music queue.
        if (automatic && (PlaybackRuntime.hasPendingSource||engine.state.sourceUri != current?.uriString)) return
        if (automatic && repeat == Repeat.ONE) { PlaybackRuntime.play(ctx); return }
        val preferred=priorityNext.firstOrNull {id->entries.any {it.id==id}}
        val target = if(preferred!=null)entries.indexOfFirst {it.id==preferred} else if(shuffle) {
            if(shuffleCycle.remaining.isEmpty()) {
                if(automatic && repeat==Repeat.OFF)return
                resetShuffle()
            }
            shuffleCycle.remaining.firstOrNull()?.let { id->entries.indexOfFirst { it.id==id } } ?: index
        } else index+1
        if (target >= entries.size && automatic && repeat == Repeat.OFF) return
        playIndex(target.mod(entries.size))
    }
    fun previous() {
        val ctx = application ?: return
        val engine=PlaybackRuntime.engine(ctx)
        val state=engine.state
        when(val action=QueueNavigation.previous(index,entries.size,state.positionMs,pendingPositionMs,
            current?.let {engine.hasCurrentSource(it.uriString,state)}==true)) {
            QueueNavigation.Previous.None -> Unit
            QueueNavigation.Previous.SeekStart -> engine.seek(0)
            is QueueNavigation.Previous.Load -> playIndex(action.index)
        }
    }
    fun playIndex(target: Int) {
        val ctx = application ?: return
        val track = entries.getOrNull(target) ?: return
        checkpoint(true);pendingPositionMs=null;pendingRestored=false;restoreNotice=""
        index = target;revision++
        priorityNext=priorityNext.filter {it!=track.id}
        shuffleCycle.consume(track.id)
        markRecent(track)
        PlaybackRuntime.whenServiceReady(ctx){PlaybackRuntime.engine(ctx).load(track.uri,track.displayName,autoPlay=true)}
    }
}
