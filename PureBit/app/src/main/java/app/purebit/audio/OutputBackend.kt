package app.purebit.audio

import app.purebit.core.PcmFormat

/** Native isochronous implementation can be added without disguising Android routing as USB Direct. */
interface OutputBackend {
    val name: String
    fun availability(format: PcmFormat): String
}
