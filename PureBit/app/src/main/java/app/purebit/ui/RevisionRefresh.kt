package app.purebit.ui

/** Main-thread read bookkeeping. A mutation during a read must still trigger another read. */
class RevisionRefresh {
    data class Request internal constructor(val sequence: Long, val revision: Long)
    private var sequence = 0L
    private var pending: Request? = null
    private var displayedRevision: Long? = null

    fun needsRefresh(revision: Long): Boolean = pending == null && displayedRevision != revision
    fun begin(revision: Long): Request = Request(++sequence, revision).also { pending = it }
    fun complete(request: Request): Boolean {
        if (pending != request) return false
        pending = null
        displayedRevision = request.revision
        return true
    }
    fun cancel() { pending = null }
}
