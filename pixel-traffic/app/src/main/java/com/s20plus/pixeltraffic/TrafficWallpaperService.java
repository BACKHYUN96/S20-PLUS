package com.s20plus.pixeltraffic;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.graphics.Canvas;
import android.os.*;
import android.content.*;
import android.util.Log;
import java.util.Locale;
import java.util.Calendar;

public class TrafficWallpaperService extends WallpaperService {
 @Override public Engine onCreateEngine() { return new TrafficEngine(); }
 private class TrafficEngine extends Engine {
  private final CityScene scene=new CityScene();
  private CinematicScene cinematic;
  private boolean cinematicRequested,cinematicActive,cinematicLoadFailed,hardwareUnavailable,hardwareConnected;
  private String renderer="기존 픽셀 합성";
  private final Handler handler=new Handler(Looper.getMainLooper());
  private final SharedPreferences prefs=WallpaperSettings.prefs(TrafficWallpaperService.this);
  private final PowerManager power=(PowerManager)getSystemService(POWER_SERVICE);
  private boolean surfaceReady,visible,destroyed,interactive,registered;
  private int frameRate=30;
  private int sceneTheme=1,weather=0,vehicleCount=12,sceneBrightness=100,road=0,vehicles=0,palette=0,camera=0,cameraPosition=1;
  private double vehicleSpeed=1;
  private boolean automaticTime,sceneryEnabled=true,eventsEnabled=true;
  private int eventFrequency=1;
  private long nextThemeCheck;
  private long lastFrame;
  private long lastSample;
  private final FrameStats stats=new FrameStats();
  private final FramePacer pacer=new FramePacer();
  private final Runnable frame=this::animate;
  private final Runnable settingsChanged=this::applySettings;
  private final SharedPreferences.OnSharedPreferenceChangeListener changed=(store,key)-> {handler.removeCallbacks(settingsChanged);handler.post(settingsChanged);};
  private final BroadcastReceiver receiver=new BroadcastReceiver() {
   @Override public void onReceive(Context context,Intent intent) {
    if(Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) interactive=false;
    else if(Intent.ACTION_SCREEN_ON.equals(intent.getAction())) interactive=true;
    applySettings();
   }
  };
  @Override public void onCreate(SurfaceHolder holder) {
   super.onCreate(holder);interactive=power.isInteractive();
   prefs.registerOnSharedPreferenceChangeListener(changed);
   IntentFilter filter=new IntentFilter();filter.addAction(Intent.ACTION_SCREEN_OFF);filter.addAction(Intent.ACTION_SCREEN_ON);filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);filter.addAction(Intent.ACTION_TIME_CHANGED);filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
   if(Build.VERSION.SDK_INT>=33) registerReceiver(receiver,filter,Context.RECEIVER_NOT_EXPORTED);
   else registerReceiver(receiver,filter);
   registered=true;applySettings();
  }
  private void applySettings() {
   if(destroyed) return;
   // Close the old sample window before applying a new frame limit.
   stop();
   int count=WallpaperSettings.COUNTS[WallpaperSettings.index(prefs,"density",2)];
   double speed=WallpaperSettings.SPEED[WallpaperSettings.index(prefs,"speed",1)];
   scene.configure(count,speed);road=WallpaperSettings.index(prefs,"road",0);vehicles=WallpaperSettings.index(prefs,"vehicles",0);palette=WallpaperSettings.index(prefs,"palette",0);scene.setRoad(road);sceneryEnabled=prefs.getBoolean("scenery_animation",true);scene.setSceneryEnabled(sceneryEnabled);eventsEnabled=prefs.getBoolean("scene_events",true);eventFrequency=WallpaperSettings.index(prefs,"event_frequency",1);scene.setEvents(eventsEnabled,eventFrequency);camera=WallpaperSettings.index(prefs,"camera",0);cameraPosition=WallpaperSettings.index(prefs,"camera_position",1);scene.setCamera(camera,cameraPosition);scene.setVehicles(vehicles,palette);
   automaticTime=prefs.getBoolean("auto_time",false);
   sceneTheme=automaticTime?localTheme():WallpaperSettings.index(prefs,"theme",1);weather=WallpaperSettings.index(prefs,"weather",0);vehicleCount=count*4;vehicleSpeed=speed;
   scene.setTheme(sceneTheme);scene.setWeather(weather);sceneBrightness=WallpaperSettings.BRIGHTNESS[WallpaperSettings.index(prefs,"brightness",0)];scene.setBrightness(sceneBrightness);
   cinematicRequested=prefs.getBoolean("cinematic_city",false)&&road==0;
   cinematicActive=false;
   if(cinematicRequested){
    cinematicActive=prepareCinematic();
    if(cinematicActive){cinematic.configure(count,speed,vehicles,palette,weather,sceneBrightness,camera,cameraPosition,sceneryEnabled,eventsEnabled,eventFrequency);cinematic.setSignals(prefs.getBoolean("traffic_signals",true));cinematic.setTheme(sceneTheme);}
   }
   if(!cinematicRequested&&cinematic!=null){cinematic.release();cinematic=null;}
   renderer=cinematicRequested&&!cinematicActive?"전용 아트 없음 · 기존 합성":"기존 픽셀 합성";
   frameRate=PlaybackPolicy.effectiveFps(WallpaperSettings.FPS[WallpaperSettings.index(prefs,"fps",1)],prefs.getBoolean("follow_saver",true),power.isPowerSaveMode());
   restart();
  }
  private boolean prepareCinematic(){
   if(cinematicLoadFailed)return false;
   if(cinematic==null){
    try{
     cinematic=new CinematicScene(TrafficWallpaperService.this);
     if(!cinematic.available()){cinematic.release();cinematic=null;cinematicLoadFailed=true;}
    }catch(Exception|OutOfMemoryError failure){
     if(cinematic!=null)cinematic.release();cinematic=null;cinematicLoadFailed=true;
     Log.w("PixelTraffic","Dedicated artwork unavailable; using classic scene",failure);
    }
   }
   return cinematic!=null;
  }
  private int localTheme() {return SceneTimePolicy.themeForHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));}
  private void refreshAutomaticTheme(long now) {
   if(!automaticTime||now<nextThemeCheck)return;
   nextThemeCheck=now+60_000_000_000L;
   int selected=localTheme();if(selected==sceneTheme)return;
   if(stats.frames>0)report("장면 전환");stats.reset();lastSample=now;
   sceneTheme=selected;scene.setTheme(selected);if(cinematicActive)cinematic.setTheme(selected);
  }
  private boolean running() {return PlaybackPolicy.shouldAnimate(surfaceReady,visible,interactive,destroyed);}
  @Override public void onSurfaceCreated(SurfaceHolder holder) { super.onSurfaceCreated(holder);surfaceReady=true;hardwareUnavailable=false;hardwareConnected=false;restart(); }
  @Override public void onSurfaceChanged(SurfaceHolder holder,int format,int width,int height) {super.onSurfaceChanged(holder,format,width,height);draw();}
  @Override public void onVisibilityChanged(boolean value) {visible=value;interactive=power.isInteractive();restart();}
  @Override public void onSurfaceRedrawNeeded(SurfaceHolder holder) {draw();}
  @Override public void onSurfaceDestroyed(SurfaceHolder holder) {surfaceReady=false;stop();super.onSurfaceDestroyed(holder);}
  @Override public void onDestroy() {
   destroyed=true;stop();handler.removeCallbacks(settingsChanged);prefs.unregisterOnSharedPreferenceChangeListener(changed);
   if(registered) {unregisterReceiver(receiver);registered=false;}
   scene.release();if(cinematic!=null){cinematic.release();cinematic=null;}super.onDestroy();
  }
  private void stop() {
   handler.removeCallbacks(frame);lastFrame=0;
   if(stats.frames>0) report("중지됨");
   stats.reset();
   Log.i("PixelTraffic","state=stopped preview="+isPreview()+" visible="+visible+" interactive="+interactive);
  }
  private void report(String state) {
   String summary=String.format(Locale.KOREA,"상태: %s\n프레임 제한: %d fps\n프레임 제출 평균: %.1f fps\n평균 그리기 시간: %.2f ms\n성공한 프레임: %d\n화면 켜짐: %s · 배경 표시: %s\n절전 모드: %s",state,frameRate,stats.fps(),stats.meanDrawMs(),stats.frames,interactive?"켜짐":"꺼짐",visible?"표시됨":"가려짐",power.isPowerSaveMode()?"켜짐":"꺼짐");
   summary+="\n렌더링: "+renderer+"\n전용 아트 도심: "+(cinematicActive?"켜짐 · 시간대 연동":cinematicRequested?"아트 없음 · 대체":"꺼짐");
   summary+="\n배경 밝기: "+sceneBrightness+"%";
   summary+=String.format(Locale.KOREA,"\n장면: %s · 날씨: %s\n차량: %d대 · 속도: %.1f배",cinematicActive?cinematic.themeDescription():new String[]{"낮","저녁","밤"}[sceneTheme],new String[]{"맑음","약한 비","강한 비","눈","안개"}[weather],vehicleCount,vehicleSpeed);
   if(cinematicActive)summary+="\n노면: "+cinematic.surfaceDescription()+"\n교통 신호: "+cinematic.trafficDescription()+"\n차량 조명: "+cinematic.lightingDescription();
   summary+="\n도로: "+new String[]{"도심","해안","산길","고속도로"}[road]+" · 차량 종류: "+new String[]{"전체","승용차","대형차","택시"}[vehicles]+" · 색상: "+new String[]{"기본","컬러풀","파스텔"}[palette];
   summary+="\n구도: "+(camera==1?"차량 중심":"전체 풍경")+" · 확대 위치: "+new String[]{"왼쪽","중앙","오른쪽"}[cameraPosition];
   summary+="\n풍경 애니메이션: "+(sceneryEnabled?"켜짐":"꺼짐");
   summary+="\n장면 이벤트: "+(eventsEnabled?"켜짐":"꺼짐")+" · 빈도: "+new String[]{"드물게","보통","자주"}[eventFrequency];
   summary+="\n시간대 자동 전환: "+(automaticTime?"켜짐":"꺼짐");
   Log.i("PixelTraffic",String.format(Locale.US,"sample preview=%s target_fps=%d submitted_fps=%.1f draw_ms=%.2f frames=%d state=%s",isPreview(),frameRate,stats.fps(),stats.meanDrawMs(),stats.frames,state));
   if(!isPreview()) getSharedPreferences("diagnostics",MODE_PRIVATE).edit().putString("summary",summary).putLong("timestamp",System.currentTimeMillis()).apply();
  }
  private void restart() {
   stop();if(running()) {
    lastSample=SystemClock.elapsedRealtimeNanos();nextThemeCheck=0;pacer.reset(lastSample,frameRate);handler.post(frame);
    Log.i("PixelTraffic","state=running preview="+isPreview()+" target_fps="+frameRate);
   }
  }
  private void animate() {
   if(!running())return;
   long now=SystemClock.elapsedRealtimeNanos();refreshAutomaticTheme(now);
   if(lastFrame!=0){double elapsed=(now-lastFrame)/1_000_000_000.0;if(cinematicActive)cinematic.update(elapsed);else scene.update(elapsed);}
   lastFrame=now;long drawStarted=SystemClock.elapsedRealtimeNanos();
   if(draw())stats.record(now,SystemClock.elapsedRealtimeNanos()-drawStarted);
   if(now-lastSample>=10_000_000_000L) {report("주행 중");lastSample=now;}
   if(running())handler.postDelayed(frame,pacer.nextDelayMillis(SystemClock.elapsedRealtimeNanos()));
  }
  private boolean draw() {
   if(!surfaceReady||destroyed)return false;
   SurfaceHolder holder=getSurfaceHolder();Canvas canvas=null;boolean success=false;
   try {
    // Keep a single producer type for this Surface, including when artwork modes change.
    // Classic scenes still rasterize their pixel frame, then post that bitmap to this Canvas.
    if(!hardwareUnavailable){
     try{canvas=holder.lockHardwareCanvas();if(canvas!=null)hardwareConnected=true;}
     catch(IllegalStateException|IllegalArgumentException unsupported){if(!hardwareConnected)hardwareUnavailable=true;Log.w("PixelTraffic","Hardware Canvas unavailable",unsupported);}
     // Once HWUI has connected, a transient failed lock skips this frame instead of
     // attaching a different software producer. Retry hardware on the next frame.
     if(canvas==null&&!hardwareConnected)hardwareUnavailable=true;
    }
    if(canvas==null&&hardwareUnavailable)canvas=holder.lockCanvas();
    if(canvas!=null){
     if(cinematicActive){cinematic.draw(canvas);renderer=canvas.isHardwareAccelerated()?"GPU Canvas · 전용 아트":"소프트웨어 Canvas · 전용 아트";}
     else {scene.draw(canvas);renderer=(cinematicRequested?"전용 아트 없음 · ":"")+(canvas.isHardwareAccelerated()?"기존 픽셀 합성 → GPU Canvas":"기존 픽셀 합성 → 소프트웨어 Canvas");}
     success=true;
    }
   }
   catch(IllegalArgumentException|IllegalStateException e) {Log.w("PixelTraffic","Surface unavailable",e);}
   finally {
    if(canvas!=null) {
     try {holder.unlockCanvasAndPost(canvas);} catch(IllegalArgumentException|IllegalStateException e) {success=false;Log.w("PixelTraffic","Surface submission failed",e);}
    }
   }
   return success;
  }
 }
}
