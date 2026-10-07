package com.s20plus.pixeltraffic;
import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.widget.*;
import android.view.*;
import android.graphics.Color;

public class MainActivity extends Activity {
 private SharedPreferences prefs;
 private LinearLayout layout,settingsRoot;
 private ScenePreviewView livePreview;
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);prefs=WallpaperSettings.prefs(this);
  ScrollView scroll=new ScrollView(this);layout=new LinearLayout(this);layout.setOrientation(LinearLayout.VERTICAL);
  int pad=(int)(24*getResources().getDisplayMetrics().density);layout.setPadding(pad,pad,pad,pad);layout.setBackgroundColor(Color.rgb(18,24,43));
  settingsRoot=layout;
  label("픽셀 트래픽",28);label("도로의 움직임을 취향에 맞게 조절하세요. 설정은 자동 저장되고 적용 중인 배경화면에도 반영됩니다.",16);
  section("프리셋 · 저장과 백업",true);
  action("추천 프리셋",()->new AlertDialog.Builder(this).setTitle("추천 프리셋").setItems(new String[]{"비 오는 도심 야경","맑은 해안 드라이브","눈 내리는 산길","노을 도심 · 시안 스타일","전용 아트 · 비 오는 노을 도심"},(d,index)->{new PresetStore(this).recommended(index);recreate();}).setNegativeButton("취소",null).show());
  action("프리셋 파일 내보내기",()->{Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);intent.setType("application/json");intent.addCategory(Intent.CATEGORY_OPENABLE);intent.putExtra(Intent.EXTRA_TITLE,"pixel-traffic-presets.json");startActivityForResult(intent,100);});
  action("프리셋 파일 가져오기",()->{Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);intent.setType("*/*");intent.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(intent,101);});
  Button savePreset=new Button(this);savePreset.setText("현재 설정 저장");savePreset.setOnClickListener(v->savePreset());layout.addView(savePreset);
  Button loadPreset=new Button(this);loadPreset.setText("저장한 프리셋 · 불러오기 / 삭제");loadPreset.setOnClickListener(v->choosePreset());layout.addView(loadPreset);
  label("이름을 붙여 최대 10개 저장합니다. 도로·차량·색상·시간대·날씨·밝기·속도·프레임·절전 연동을 함께 보관합니다.",14);
  section("빠른 설정",false);
  LinearLayout modes=new LinearLayout(this);
  String[] names={"절약","균형","부드럽게"};
  for(int i=0;i<names.length;i++) {
   final int index=i;Button mode=new Button(this);mode.setText(names[i]);
   mode.setOnClickListener(v->{QuickMode selected=QuickMode.forIndex(index);prefs.edit().putInt("density",selected.densityIndex).putInt("speed",1).putInt("fps",selected.fpsIndex).putBoolean("follow_saver",true).apply();recreate();});
   modes.addView(mode,new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1));
  }
  layout.addView(modes);
  label("절약 4대/15fps · 균형 8대/30fps · 부드럽게 12대/60fps. 속도는 1배, 절전 연동은 켬으로 적용하며 장면과 날씨는 유지합니다.",14);
  section("장면 · 도로와 구도",true);
  choice("도로 테마","road",new String[]{"도심 · 네온과 건물","해안 · 바다와 모래사장","산길 · 숲과 능선","고속도로 · 안내판과 휴게소"},0);
  Switch cinematicSwitch=new Switch(this);cinematicSwitch.setText("전용 아트 도심");cinematicSwitch.setTextColor(Color.WHITE);cinematicSwitch.setChecked(prefs.getBoolean("cinematic_city",false));
  cinematicSwitch.setOnCheckedChangeListener((button,value)->prefs.edit().putBoolean("cinematic_city",value).apply());layout.addView(cinematicSwitch);
  label("도심에서 낮·노을·밤 전용 일러스트와 차량을 사용합니다. 장면 시간대와 자동 전환을 지원하며, 전환 중에도 차량은 계속 주행합니다. 맑음은 마른 도로, 비는 젖은 도로이며 날씨를 바꾸면 약 4초 동안 노면·반사·물보라가 함께 전환됩니다. 차량 색상 ‘기본’에서 새 차량 아트를 사용하고 컬러풀·파스텔은 기존 차량입니다. 눈은 지붕·가로수·보도에 쌓인 눈, 안개는 먼 풍경이 흐려지는 전용 배경을 사용합니다. 낮·노을·밤과 약 4초 전환을 지원합니다. 다른 도로는 기존 아트를 유지합니다.",14);
  Switch scenerySwitch=new Switch(this);scenerySwitch.setText("풍경 애니메이션");scenerySwitch.setTextColor(Color.WHITE);scenerySwitch.setChecked(prefs.getBoolean("scenery_animation",true));
  scenerySwitch.setOnCheckedChangeListener((button,value)->prefs.edit().putBoolean("scenery_animation",value).apply());layout.addView(scenerySwitch);
  label("파도·등대·네온·구름을 은은하게 움직입니다. 끄면 풍경은 정지하고 차량과 날씨는 계속 움직입니다.",14);
  Spinner cameraPosition=choice("확대 위치","camera_position",new String[]{"왼쪽 풍경","중앙","오른쪽 풍경"},1);
  Spinner cameraChoice=choice("화면 구도","camera",new String[]{"전체 풍경 · 기존 구도","차량 중심 · 1.5배 확대"},0);
  cameraPosition.setEnabled(WallpaperSettings.index(prefs,"camera",0)==1);
  cameraChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
   @Override public void onItemSelected(AdapterView<?> parent,View view,int position,long id){prefs.edit().putInt("camera",position).apply();cameraPosition.setEnabled(position==1);}
   @Override public void onNothingSelected(AdapterView<?> parent){}
  });
  label("확대하면 장면 가장자리가 잘립니다. 위치는 확대 구도에 적용되며 차량과 풍경의 픽셀 경계는 선명하게 유지합니다.",14);
  section("차량 · 종류와 색상",false);
  Switch signalSwitch=new Switch(this);signalSwitch.setText("교통 신호 · 전용 도심");signalSwitch.setTextColor(Color.WHITE);signalSwitch.setChecked(prefs.getBoolean("traffic_signals",true));
  signalSwitch.setOnCheckedChangeListener((button,value)->prefs.edit().putBoolean("traffic_signals",value).apply());layout.addView(signalSwitch);
  label("횡단보도 앞에서 감속·정차하고 초록 신호에 순서대로 출발합니다. 끄면 자유 주행으로 돌아갑니다. 다른 도로에는 적용하지 않습니다.",14);
  choice("차량 종류","vehicles",new String[]{"전체 · 6종 혼합","승용차 · 세단/쿠페/SUV/택시","대형차 · 버스/트럭","택시만"},0);
  choice("차량 색상","palette",new String[]{"기본 색상","컬러풀","파스텔"},0);
  label("전용 도심 라이트: 승용차·SUV·택시는 6500K LED, 스포츠 쿠페는 6500K 전조등·안개등, 버스·트럭은 4500K 전조등과 3000K 안개등입니다. 날씨·시간대에 따라 비춤과 반사가 달라집니다.",14);
  section("효과 · 시간대와 날씨",false);
  Spinner themeChoice=choice("장면 시간대","theme",new String[]{"낮 · 밝은 도심","저녁 · 노을과 조명","밤 · 별과 야경"},1);
  Switch automatic=new Switch(this);automatic.setText(R.string.automatic_time);automatic.setTextColor(Color.WHITE);
  automatic.setChecked(prefs.getBoolean("auto_time",false));themeChoice.setEnabled(!automatic.isChecked());
  automatic.setOnCheckedChangeListener((button,value)->{prefs.edit().putBoolean("auto_time",value).apply();themeChoice.setEnabled(!value);});layout.addView(automatic);
  label("장면 변경은 주행 중 약 4초에 걸쳐 색과 조명이 전환됩니다. 차량은 계속 주행합니다.",14);
  label("휴대폰 시간 기준: 06~17시 낮 · 17~20시 노을 · 20~06시 밤. 자동 전환을 끄면 저장된 수동 선택으로 돌아갑니다.",14);
  choice("날씨 효과","weather",new String[]{"맑음 · 비 효과 끄기","약한 비 · 잔잔한 빗방울","강한 비 · 많은 빗방울","눈 · 눈송이와 쌓인 가장자리","안개 · 먼 풍경을 흐리게"},0);
  Switch eventsSwitch=new Switch(this);eventsSwitch.setText("작은 장면 이벤트");eventsSwitch.setTextColor(Color.WHITE);eventsSwitch.setChecked(prefs.getBoolean("scene_events",true));layout.addView(eventsSwitch);
  Spinner eventFrequency=choice("이벤트 빈도","event_frequency",new String[]{"드물게 · 약 72초 간격","보통 · 약 42초 간격","자주 · 약 24초 간격"},1);
  eventFrequency.setEnabled(eventsSwitch.isChecked());eventsSwitch.setOnCheckedChangeListener((button,value)->{prefs.edit().putBoolean("scene_events",value).apply();eventFrequency.setEnabled(value);});
  label("도심의 먼 전철·해안의 배·산길의 새·고속도로 서비스 차량이 가끔 등장합니다. 첫 등장은 재생 후 약 18/10/5초이며, 화면이 가려지면 시간도 멈춥니다.",14);
  choice("배경 밝기","brightness",new String[]{"100% · 원래 밝기","80% · 조금 어둡게","60% · 아이콘 강조"},0);
  label("배경화면 그림만 어둡게 합니다. 휴대폰 화면 밝기는 변경하지 않습니다.",14);
  section("주행 · 속도와 성능",false);
  choice("차량 수","density",new String[]{"적게 · 전체 4대","보통 · 전체 8대","많이 · 전체 12대","매우 많이 · 전체 24대"},2);
  choice("이동 속도","speed",new String[]{"느리게 · 0.5배","보통 · 1배","빠르게 · 1.5배"},1);
  choice("프레임 제한","fps",new String[]{"15fps · 낮은 갱신 빈도","30fps · 기본","60fps · 부드러운 움직임"},1);
  Switch saver=new Switch(this);saver.setText(R.string.follow_saver);saver.setTextColor(Color.WHITE);saver.setChecked(prefs.getBoolean("follow_saver",true));
  saver.setOnCheckedChangeListener((button,checked)->prefs.edit().putBoolean("follow_saver",checked).apply());layout.addView(saver);
  label("화면이 꺼지거나 배경화면이 보이지 않을 때는 움직임을 멈춥니다. 차량 수는 화면 밖을 포함한 전체 수입니다. 높은 프레임 설정은 배터리 사용량이 늘어날 수 있습니다.",14);
  section("적용 · 진단과 초기화",true);
  action("현재 설정 초기화",()->new AlertDialog.Builder(this).setTitle("기본 설정으로 복원").setMessage("현재 설정만 초기화합니다. 저장한 프리셋은 유지합니다.").setNegativeButton("취소",null).setPositiveButton("초기화",(d,w)->{new PresetStore(this).resetCurrent();recreate();}).show());
  Button button=new Button(this);button.setText("배경화면 미리보기 · 설정");button.setOnClickListener(v->openPreview());layout.addView(button);
  Button diagnostic=new Button(this);diagnostic.setText(R.string.diagnostics);diagnostic.setOnClickListener(v->showDiagnostics());layout.addView(diagnostic);
  scroll.addView(settingsRoot);
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.rgb(18,24,43));
  livePreview=new ScenePreviewView(this);livePreview.setOnClickListener(v->startActivity(new Intent(this,FullscreenPreviewActivity.class)));
  int previewDp=Math.min(280,Math.max(160,getResources().getConfiguration().screenHeightDp/3));
  root.addView(livePreview,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(int)(previewDp*getResources().getDisplayMetrics().density)));
  TextView previewNote=new TextView(this);previewNote.setText(getString(R.string.preview_expand_note,getString(R.string.preview_note)));previewNote.setTextColor(Color.LTGRAY);previewNote.setPadding(pad,8,pad,8);root.addView(previewNote);
  root.addView(scroll,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1));setContentView(root);
  // Keep controls clear of system bars on edge-to-edge Android versions.
  root.setOnApplyWindowInsetsListener((v,insets)-> {
   if(android.os.Build.VERSION.SDK_INT>=30) {
    android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);
   } else v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
   return insets;
  });
 }
 @Override protected void onResume(){super.onResume();if(livePreview!=null)livePreview.startPreview();}
 @Override protected void onPause(){if(livePreview!=null)livePreview.stopPreview();super.onPause();}
 @Override protected void onDestroy(){if(livePreview!=null)livePreview.release();super.onDestroy();}
 private void label(String text,int size) {TextView view=new TextView(this);view.setText(text);view.setTextSize(size);view.setTextColor(Color.LTGRAY);view.setPadding(0,16,0,16);layout.addView(view);}
 private Spinner choice(String title,String key,String[] values,int fallback) {
  label(title,18);Spinner spinner=new Spinner(this);
  ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,values);adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);spinner.setAdapter(adapter);
  spinner.setSelection(WallpaperSettings.index(prefs,key,fallback));
  spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
   @Override public void onItemSelected(AdapterView<?> parent,View view,int position,long id) {prefs.edit().putInt(key,position).apply();}
   @Override public void onNothingSelected(AdapterView<?> parent) { }
  });layout.addView(spinner);return spinner;
 }
 private void showDiagnostics() {
  SharedPreferences report=getSharedPreferences("diagnostics",MODE_PRIVATE);
  long timestamp=report.getLong("timestamp",0);
  String text=report.getString("summary","아직 측정 기록이 없습니다. 배경화면을 적용하고 홈 화면에서 10초 이상 주행한 뒤 다시 열어주세요.");
  if(timestamp!=0) text="최근 기록: "+java.text.DateFormat.getDateTimeInstance().format(new java.util.Date(timestamp))+"\n\n"+text;
  text+="\n\n최근 배경화면 엔진 기록입니다. 앱을 열면 배경화면이 가려져 중지될 수 있습니다. 프레임 제출 평균은 화면에 실제 표시된 FPS나 배터리 사용량 측정값이 아닙니다.";
  String content=text;
  new AlertDialog.Builder(this).setTitle(R.string.diagnostics).setMessage(text).setPositiveButton("닫기",null).setNeutralButton("복사",(dialog,which)-> {
   ClipboardManager clipboard=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);clipboard.setPrimaryClip(ClipData.newPlainText("픽셀 트래픽 진단",content));
   Toast.makeText(this,"진단 정보를 복사했습니다.",Toast.LENGTH_SHORT).show();
  }).show();
 }
 private void savePreset() {
  EditText name=new EditText(this);name.setSingleLine(true);name.setHint("예: 비 오는 해안 야경");
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle("현재 설정 저장").setView(name).setNegativeButton("취소",null).setPositiveButton("저장",null).create();
  dialog.setOnShowListener(ignored->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
   String title=name.getText().toString().trim();
   if(title.isEmpty()||title.codePointCount(0,title.length())>32){name.setError("이름은 1~32자로 입력해주세요.");return;}
   PresetStore store=new PresetStore(this);
   if(store.exists(title))new AlertDialog.Builder(this).setTitle("같은 이름의 프리셋이 있어요").setMessage("‘"+title+"’을 현재 설정으로 덮어쓸까요?").setNegativeButton("취소",null).setPositiveButton("덮어쓰기",(d,w)->persistPreset(store,title,dialog,name)).show();
   else persistPreset(store,title,dialog,name);
  }));dialog.show();
 }
 private void persistPreset(PresetStore store,String title,AlertDialog dialog,EditText name) {
  try{store.save(title);dialog.dismiss();Toast.makeText(this,"프리셋을 저장했습니다.",Toast.LENGTH_SHORT).show();}
  catch(IllegalArgumentException e){name.setError(e.getMessage());}
  catch(org.json.JSONException e){Toast.makeText(this,"프리셋 저장에 실패했습니다.",Toast.LENGTH_LONG).show();}
 }
 private void choosePreset() {
  PresetStore store=new PresetStore(this);String[] names=store.names();
  if(names.length==0){Toast.makeText(this,"먼저 현재 설정을 저장해주세요.",Toast.LENGTH_SHORT).show();return;}
  new AlertDialog.Builder(this).setTitle("저장한 프리셋").setItems(names,(dialog,index)->{
   String title=names[index];new AlertDialog.Builder(this).setTitle(title).setItems(new String[]{"불러오기","이름 변경","삭제"},(d,action)->{
    if(action==0){try{store.load(title);recreate();}catch(org.json.JSONException e){message("프리셋을 읽을 수 없습니다. 현재 설정은 유지합니다.");}}
    else if(action==1){EditText input=new EditText(this);input.setSingleLine(true);input.setText(title);AlertDialog rename=new AlertDialog.Builder(this).setTitle("이름 변경").setView(input).setNegativeButton("취소",null).setPositiveButton("변경",null).create();rename.setOnShowListener(ignored->rename.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{try{store.rename(title,input.getText().toString());rename.dismiss();message("이름을 변경했습니다.");}catch(IllegalArgumentException e){input.setError(e.getMessage());}}));rename.show();}
    else new AlertDialog.Builder(this).setTitle("프리셋 삭제").setMessage("‘"+title+"’을 삭제할까요? 현재 설정은 유지됩니다.").setNegativeButton("취소",null).setPositiveButton("삭제",(confirm,which)->{store.delete(title);message("삭제했습니다.");}).show();
   }).setNegativeButton("닫기",null).show();
  }).setNegativeButton("닫기",null).show();
 }
 private void section(String title,boolean open) {
  Button heading=new Button(this);LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setVisibility(open?View.VISIBLE:View.GONE);
  heading.setText(getString(R.string.section_title,open?"▾":"▸",title));heading.setOnClickListener(v->{boolean show=content.getVisibility()!=View.VISIBLE;content.setVisibility(show?View.VISIBLE:View.GONE);heading.setText(getString(R.string.section_title,show?"▾":"▸",title));});settingsRoot.addView(heading);settingsRoot.addView(content);layout=content;
 }
 private void action(String title,Runnable action){Button button=new Button(this);button.setText(title);button.setOnClickListener(v->action.run());layout.addView(button);}
 private void message(String text){Toast.makeText(this,text,Toast.LENGTH_LONG).show();}
 @Override protected void onActivityResult(int request,int result,Intent data){
  super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;
  if(request==100){try{byte[] payload=new PresetStore(this).exportAll().getBytes(java.nio.charset.StandardCharsets.UTF_8);try(java.io.OutputStream out=getContentResolver().openOutputStream(data.getData(),"wt")){if(out==null)throw new java.io.IOException();out.write(payload);}message("프리셋 파일을 저장했습니다.");}catch(java.io.IOException|org.json.JSONException e){message("내보내기에 실패했습니다.");}}
  else if(request==101){try(java.io.InputStream in=getContentResolver().openInputStream(data.getData())){
   if(in==null)throw new java.io.IOException();java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();byte[] buffer=new byte[4096];int read;while((read=in.read(buffer))!=-1){if(bytes.size()+read>131072)throw new java.io.IOException("파일은 128KB 이하여야 합니다.");bytes.write(buffer,0,read);}String raw=bytes.toString("UTF-8");
   new AlertDialog.Builder(this).setTitle("프리셋 가져오기").setMessage("파일의 프리셋을 추가합니다. 중복 이름이나 잘못된 값이 있으면 전체 가져오기를 취소하며 현재 설정은 유지합니다.").setNegativeButton("취소",null).setPositiveButton("가져오기",(d,w)->{try{int count=new PresetStore(this).importAll(raw);message(count+"개 프리셋을 추가했습니다.");}catch(org.json.JSONException e){message("가져오기 실패: "+e.getMessage());}}).show();
  }catch(java.io.IOException e){message("파일을 읽을 수 없습니다. 128KB 이하의 프리셋 JSON을 선택해주세요.");}}
 }
 private void openPreview() {
  Intent intent=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
  intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new ComponentName(this,TrafficWallpaperService.class));
  try {startActivity(intent);} catch(ActivityNotFoundException e) {
   try {startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER));}
   catch(ActivityNotFoundException missing) {Toast.makeText(this,"이 기기에서 라이브 배경화면 설정 화면을 열 수 없습니다.",Toast.LENGTH_LONG).show();}
  }
 }
}
