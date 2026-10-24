package app.purebit

/** Navigation uses the selected source's position, never an outgoing decoder/session snapshot. */
object QueueNavigation {
    sealed interface Previous {
        data object None : Previous
        data object SeekStart : Previous
        data class Load(val index: Int) : Previous
    }

    fun previous(index: Int, size: Int, positionMs: Long, pendingPositionMs: Long?, sourceReady: Boolean): Previous {
        if (size <= 0 || index !in 0 until size) return Previous.None
        if (pendingPositionMs != null) {
            return Previous.Load(if (pendingPositionMs > 3000) index else (index - 1).mod(size))
        }
        return if (sourceReady && positionMs > 3000) Previous.SeekStart
        else Previous.Load((index - 1).mod(size))
    }
}
