package app.purebit

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.net.Uri
import android.widget.*
import app.purebit.startup.*
import java.io.File
import java.util.UUID

/** Independent native controls: no audio engine, theme assets or player views. */
class RecoveryActivity : Activity() {
    private var report="오류 기록을 읽고 있습니다…"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!intent.getBooleanExtra("show_recovery",false)&&savedInstanceState==null){
            val destination=StartupSafety.destination(this)
            if(destination!=StartupPolicy.Destination.RECOVERY){launch(destination==StartupPolicy.Destination.SAFE_PLAYER);finish();return}
        }
        AppFaults.stage="recovery console"
        val column=LinearLayout(this).apply {orientation=LinearLayout.VERTICAL;setPadding(24,32,24,32)}
        fun label(value:String,size:Float)=TextView(this).apply {text=value;textSize=size;setTextColor(0xffeeeeee.toInt());setPadding(0,12,0,12)}
        fun action(title:String,run:()->Unit){column.addView(Button(this).apply {text=title;setOnClickListener {run()}})}
        column.addView(label("BITWIRE · 실행 복구",24f))
        column.addView(label("${BuildConfig.VERSION_NAME}\n먼저 오류 기록을 복사하거나 공유해 주세요. 음악 목록과 테마 설정은 삭제하지 않습니다.",15f))
        action("오류 기록 복사") {
            getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("BITWIRE startup fault",report))
            Toast.makeText(this,"복사했습니다. 대화창에 붙여넣어 주세요.",Toast.LENGTH_LONG).show()
        }
        action("오류 기록 파일 공유") {
            runCatching {
                val dir=File(filesDir,"diagnostic-reports").apply {mkdirs()}
                val file=File(dir,"PureBit-${UUID.randomUUID()}.txt").apply {writeText(report)}
                val uri=Uri.parse("content://$packageName.diagnostics/${file.name}")
                val send=Intent(Intent.ACTION_SEND).apply {type="text/plain";putExtra(Intent.EXTRA_STREAM,uri);clipData=ClipData.newRawUri("BITWIRE fault",uri);addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)}
                startActivity(Intent.createChooser(send,"오류 기록 공유"))
            }.onFailure {Toast.makeText(this,"공유 실패: ${it.javaClass.simpleName}. 복사를 이용해 주세요.",Toast.LENGTH_LONG).show()}
        }
        action("기본 테마로 임시 실행 · 연출 끄기") {launch(true)}
        action("기존 테마로 실행") {launch(false)}
        val details=label(report,12f).apply {setTextIsSelectable(true)}
        column.addView(details)
        val scroll=ScrollView(this).apply {setBackgroundColor(0xff10131a.toInt());addView(column)}
        scroll.setOnApplyWindowInsetsListener {v,insets->val b=insets.getInsets(android.view.WindowInsets.Type.systemBars() or android.view.WindowInsets.Type.displayCutout());v.setPadding(b.left,b.top,b.right,b.bottom);insets}
        setContentView(scroll)
        Thread({
            val result=runCatching {AppFaults.report(applicationContext)}.getOrElse {"진단 읽기 실패: ${it.stackTraceToString()}"}
            runOnUiThread {if(!isDestroyed){report=result;details.text=result}}
        },"BITWIRE-recovery-report").start()
    }
    private fun launch(safe:Boolean){
        if(!safe)StartupSafety.retrySelectedTheme(this)
        // MainActivity owns its startup marker, including recreation for a recovery-mode change.
        startActivity(Intent().setClassName(this,"app.purebit.MainActivity").putExtra("recovery_safe",safe)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
    }
}
