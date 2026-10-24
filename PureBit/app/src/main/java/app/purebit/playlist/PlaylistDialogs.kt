package app.purebit.playlist

import android.app.Activity
import android.app.AlertDialog
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import app.purebit.library.*
import app.purebit.theme.ThemeSpec

object PlaylistDialogs {
    fun name(activity:Activity,title:String,initial:String,save:(String,(Result<Unit>)->Unit)->Unit):AlertDialog {
        val input=EditText(activity).apply {setSingleLine();filters=arrayOf(android.text.InputFilter.LengthFilter(80));setText(initial);setSelection(text.length);hint="재생목록 이름";setPadding(24,20,24,20)}
        val dialog=AlertDialog.Builder(activity).setTitle(title).setView(input).setPositiveButton("저장",null).setNegativeButton("취소",null).create()
        dialog.setOnShowListener {dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name=runCatching {PlaylistRules.name(input.text.toString())}.getOrElse {input.error=it.message;return@setOnClickListener}
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled=false;dialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled=false;dialog.setCancelable(false)
            save(name){result->if(!activity.isDestroyed&&dialog.isShowing){
                result.fold({dialog.dismiss()},{error->input.error=if(error is java.io.IOException)"파일을 저장하지 못했습니다. 저장 공간을 확인해 주세요."else error.message?:"저장하지 못했습니다";dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled=true;dialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled=true;dialog.setCancelable(true)})
            }}
        }}
        dialog.show();return dialog
    }
    fun tracks(activity:Activity,theme:ThemeSpec,all:List<LibraryTrack>,existing:Set<String>,add:(List<String>)->Unit):AlertDialog {
        val handler=Handler(Looper.getMainLooper());val selected=linkedSetOf<String>()
        val sorted=LibraryBrowser.sorted(all,TrackSort.TITLE,false);var visible=sorted
        val d=activity.resources.displayMetrics.density;fun dp(v:Int)=(v*d).toInt()
        val container=LinearLayout(activity).apply {orientation=LinearLayout.VERTICAL;setPadding(dp(16),0,dp(16),0)}
        val search=EditText(activity).apply {setSingleLine();hint="곡 · 아티스트 검색";filters=arrayOf(android.text.InputFilter.LengthFilter(256))}
        val status=TextView(activity).apply {textSize=12f;setTextColor(theme.muted);setPadding(0,dp(8),0,dp(8))}
        val list=ListView(activity).apply {choiceMode=ListView.CHOICE_MODE_MULTIPLE}
        val labels=ArrayList<String>()
        val adapter=object:ArrayAdapter<String>(activity,android.R.layout.simple_list_item_multiple_choice,labels){
            override fun isEnabled(position:Int)=visible.getOrNull(position)?.id !in existing
            override fun getView(position:Int,convertView:View?,parent:ViewGroup):View=super.getView(position,convertView,parent).also {v->(v as? TextView)?.apply {textSize=14f;maxLines=2;setTextColor(if(isEnabled(position))theme.text else theme.muted)}}
        }
        list.adapter=adapter
        container.addView(search);container.addView(status);container.addView(list,LinearLayout.LayoutParams(-1,dp(if(activity.resources.configuration.orientation==android.content.res.Configuration.ORIENTATION_LANDSCAPE)125 else 290)))
        val dialog=AlertDialog.Builder(activity).setTitle("추가할 곡 선택").setView(container).setPositiveButton("추가",null).setNeutralButton("검색 결과 선택",null).setNegativeButton("취소",null).create()
        fun updateStatus(){status.text=activity.getString(app.purebit.R.string.playlist_selection_status,selected.size,visible.size);if(dialog.isShowing)dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled=selected.isNotEmpty()}
        fun refresh(){
            visible=LibraryBrowser.search(sorted,search.text.toString());adapter.setNotifyOnChange(false);adapter.clear();adapter.addAll(visible.map {"${it.title}${if(!it.available)" · 접근 불가" else ""}\n${it.artist.ifBlank {"알 수 없는 아티스트"}}"});adapter.notifyDataSetChanged()
            list.clearChoices();visible.forEachIndexed {i,t->if(t.id in selected||t.id in existing)list.setItemChecked(i,true)};updateStatus()
        }
        list.setOnItemClickListener {_,_,position,_->val track=visible.getOrNull(position)?:return@setOnItemClickListener
            if(track.id in existing){list.setItemChecked(position,true);return@setOnItemClickListener}
            if(list.isItemChecked(position)){
                if(existing.size+selected.size>=PlaylistRules.MAX_TRACKS){list.setItemChecked(position,false);Toast.makeText(activity,"목록은 최대 10,000곡입니다",Toast.LENGTH_SHORT).show()}else selected+=track.id
            }else selected-=track.id
            updateStatus()
        }
        val refreshSearch=Runnable {if(dialog.isShowing)refresh()}
        search.addTextChangedListener(object:TextWatcher {override fun beforeTextChanged(s:CharSequence?,start:Int,count:Int,after:Int){};override fun onTextChanged(s:CharSequence?,start:Int,before:Int,count:Int){handler.removeCallbacks(refreshSearch);handler.postDelayed(refreshSearch,250)};override fun afterTextChanged(s:Editable?){} })
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {if(selected.isNotEmpty()){add(selected.toList());dialog.dismiss()}}
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                val additions=visible.map {it.id}.filter {it !in existing&&it !in selected}
                if(existing.size+selected.size+additions.size>PlaylistRules.MAX_TRACKS)Toast.makeText(activity,"10,000곡 한도를 넘습니다. 검색 범위를 좁혀주세요.",Toast.LENGTH_LONG).show()
                else {selected.addAll(additions);refresh()}
            };refresh()
        }
        dialog.setOnDismissListener {handler.removeCallbacks(refreshSearch)};dialog.show();return dialog
    }
}
