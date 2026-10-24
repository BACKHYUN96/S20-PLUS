package app.purebit

import android.app.Application

/** Install crash recording before Activity/window creation; keep Android's normal crash handling. */
class BitwireApplication:Application(){
    override fun onCreate(){super.onCreate();runCatching {AppFaults.install(this)}}
}
