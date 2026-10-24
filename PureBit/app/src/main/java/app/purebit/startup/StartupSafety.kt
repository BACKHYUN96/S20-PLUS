package app.purebit.startup

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Process
import android.os.SystemClock

/** Private startup markers. Never reset the theme, music library or audio preferences. */
// Tiny startup/failure markers must reach disk before risky UI construction or process death.
@SuppressLint("ApplySharedPref")
object StartupSafety {
    @Volatile private var token=0L
    @Volatile private var starting=false
    @Volatile private var safe=false
    private fun prefs(c:Context)=c.getSharedPreferences("bitwire_startup",Context.MODE_PRIVATE)
    @Synchronized fun destination(c:Context):StartupPolicy.Destination {
        val p=prefs(c)
        if(!starting && p.getBoolean("pending",false))runCatching {
            val start=p.getLong("started_at",0);val pid=p.getInt("pid",0)
            val confirmed=c.getSystemService(ActivityManager::class.java).getHistoricalProcessExitReasons(c.packageName,0,8).any {
                StartupPolicy.confirmedStartupCrash(start,pid,it.timestamp,it.pid,it.reason in listOf(ApplicationExitInfo.REASON_CRASH,ApplicationExitInfo.REASON_CRASH_NATIVE,ApplicationExitInfo.REASON_ANR))
            }
            if(confirmed)p.edit().putBoolean("latched",true).putBoolean("safe_failed",p.getBoolean("was_safe",false)).commit()
            // Ignore old pending markers without matching crash/ANR evidence.
        }
        return StartupPolicy.destination(p.getBoolean("latched",false),p.getBoolean("safe_failed",false))
    }
    @Synchronized fun begin(c:Context,safeMode:Boolean):Long {
        token=SystemClock.elapsedRealtimeNanos();safe=safeMode;starting=true
        prefs(c).edit().putBoolean("pending",true).putLong("started_at",System.currentTimeMillis()).putInt("pid",Process.myPid()).putBoolean("was_safe",safeMode).commit()
        return token
    }
    @Synchronized fun ready(c:Context,launch:Long){
        if(token!=launch||!starting)return
        starting=false;prefs(c).edit().putBoolean("pending",false).commit()
    }
    @Synchronized fun fault(c:Context){
        if(!starting)return
        prefs(c).edit().putBoolean("latched",true).putBoolean("safe_failed",safe).commit()
    }
    @Synchronized fun retrySelectedTheme(c:Context){prefs(c).edit().putBoolean("pending",false).putBoolean("latched",false).putBoolean("safe_failed",false).commit()}
    fun summary(c:Context):String {val p=prefs(c);return "STARTUP SAFETY: latched=${p.getBoolean("latched",false)} safeFailed=${p.getBoolean("safe_failed",false)} pending=${p.getBoolean("pending",false)}; no theme/library reset"}
}
