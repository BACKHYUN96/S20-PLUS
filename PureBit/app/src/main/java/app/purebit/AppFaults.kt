package app.purebit

import app.purebit.startup.StartupSafety
import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build
import android.util.AtomicFile
import java.io.File

/** App-private, bounded crash diagnostics. No upload, playback restart, or swallowing fatal errors. */
object AppFaults {
    @Volatile var stage="application startup"
    private var installed=false
    private const val LIMIT=65536
    private fun file(context:Context)=AtomicFile(File(context.filesDir,"last-app-fault.txt"))
    fun record(context:Context,title:String,error:Throwable){
        runCatching {
            val bytes=("BITWIRE ${BuildConfig.VERSION_NAME}\n${Build.MANUFACTURER} ${Build.MODEL} API${Build.VERSION.SDK_INT}\nTime: ${java.time.Instant.now()}\nStage: $stage\n$title\n${error.stackTraceToString()}").take(LIMIT).toByteArray(Charsets.UTF_8)
            val atomic=file(context);val output=atomic.startWrite()
            try{output.write(bytes);atomic.finishWrite(output)}catch(e:Throwable){atomic.failWrite(output);throw e}
        }
    }
    @Synchronized fun install(context:Context){
        if(installed)return;installed=true
        val app=context.applicationContext;val previous=Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler {thread,error->
            runCatching {StartupSafety.fault(app)}
            if(error !is OutOfMemoryError)record(app,"Uncaught on ${thread.name}",error)
            // A subsequent launch uses the proven base window styling; never reset music data.
            runCatching {app.getSharedPreferences("bitwire_ui",Context.MODE_PRIVATE).edit().putBoolean("system_bars_enabled",false).commit()}
            if(previous!=null)previous.uncaughtException(thread,error)
            else {android.os.Process.killProcess(android.os.Process.myPid());kotlin.system.exitProcess(1)}
        }
    }
    /** Call from diagnostics worker. Older builds may have OS exit descriptions but no Java stack. */
    fun report(context:Context):String=buildString {
        append("=== APP STARTUP / LAST FAULT ===\n")
        append(StartupSafety.summary(context)+"\n")
        append(runCatching {file(context).openRead().bufferedReader().use {it.readText().take(LIMIT)}}.getOrDefault("No app-recorded exception. v0.8.2 did not record uncaught stacks.\n"))
        append("\n=== ANDROID EXIT HISTORY (OWN APP) ===\n")
        append(runCatching {
            context.getSystemService(ActivityManager::class.java).getHistoricalProcessExitReasons(context.packageName,0,6)
                .filter {it.reason in listOf(ApplicationExitInfo.REASON_CRASH,ApplicationExitInfo.REASON_CRASH_NATIVE,ApplicationExitInfo.REASON_ANR)}
                .joinToString("\n") {"time=${it.timestamp} reason=${it.reason} status=${it.status} description=${it.description.orEmpty().take(2048)}"}
                .ifBlank {"No OS crash/ANR records available."}
        }.getOrElse {"OS exit history unavailable: ${it.javaClass.simpleName}"})
        append("\nOS exit descriptions may not contain a Java stack; no confirmed cause inferred from them.\n")
    }
}
