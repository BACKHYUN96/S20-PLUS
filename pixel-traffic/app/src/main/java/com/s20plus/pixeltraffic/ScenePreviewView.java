package com.s20plus.pixeltraffic;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.*;
import android.view.View;
import android.widget.Toast;
import java.util.Calendar;

/** In-app visual preview. Separate simulation; never writes wallpaper diagnostics. */
final class ScenePreviewView extends View {
 private final CityScene scene=new CityScene();
 private final Bitmap image=Bitmap.createBitmap(360,800,Bitmap.Config.ARGB_8888);
 private final Canvas imageCanvas=new Canvas(image);
 private final Paint paint=new Paint();
 private final RectF bounds=new RectF();
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final FramePacer pacer=new FramePacer();
 private final SharedPreferences prefs;
 private CinematicScene cinematic;
 private boolean resumed,attached,released,cinematicActive,cinematicLoadFailed;
 private long lastFrame,nextClockCheck;
 private final Runnable frame=this::tick;
 private final Runnable settingsChanged=this::refreshSettings;
 private final SharedPreferences.OnSharedPreferenceChangeListener listener=(store,key)-> {
  handler.removeCallbacks(settingsChanged);handler.post(settingsChanged);
 };
 ScenePreviewView(Context context) {
  super(context);prefs=WallpaperSettings.prefs(context);paint.setFilterBitmap(false);
  setContentDescription("현재 설정의 도심 배경화면 미리보기");
  prefs.registerOnSharedPreferenceChangeListener(listener);refreshSettings();
 }
 private void selectTheme() {
  int theme=prefs.getBoolean("auto_time",false)?SceneTimePolicy.themeForHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)):WallpaperSettings.index(prefs,"theme",1);
  scene.setTheme(theme);
  if(cinematicActive){cinematic.setTheme(theme);setContentDescription(cinematic.themeDescription()+" 도심 배경화면 미리보기");}
 }
 private boolean prepareCinematic() {
  if(cinematicLoadFailed)return false;
  if(cinematic==null) {
   try {
    cinematic=new CinematicScene(getContext());
    if(!cinematic.available()) {cinematic.release();cinematic=null;cinematicLoadFailed=true;}
   } catch(Exception|OutOfMemoryError failure) {
    if(cinematic!=null)cinematic.release();cinematic=null;cinematicLoadFailed=true;
   }
   if(cinematicLoadFailed)Toast.makeText(getContext(),"전용 아트를 불러오지 못해 기본 장면을 표시합니다.",Toast.LENGTH_LONG).show();
  }
  return cinematic!=null;
 }
 private void refreshSettings() {
  if(released)return;
  int road=WallpaperSettings.index(prefs,"road",0);
  int perLane=WallpaperSettings.COUNTS[WallpaperSettings.index(prefs,"density",2)];
  double speed=WallpaperSettings.SPEED[WallpaperSettings.index(prefs,"speed",1)];
  int vehicles=WallpaperSettings.index(prefs,"vehicles",0),palette=WallpaperSettings.index(prefs,"palette",0);
  int weather=WallpaperSettings.index(prefs,"weather",0),brightness=WallpaperSettings.BRIGHTNESS[WallpaperSettings.index(prefs,"brightness",0)];
  int camera=WallpaperSettings.index(prefs,"camera",0),position=WallpaperSettings.index(prefs,"camera_position",1);
  boolean scenery=prefs.getBoolean("scenery_animation",true),events=prefs.getBoolean("scene_events",true);
  int frequency=WallpaperSettings.index(prefs,"event_frequency",1);
  cinematicActive=prefs.getBoolean("cinematic_city",false)&&road==0&&prepareCinematic();
  if(cinematicActive) {
   cinematic.configure(perLane,speed,vehicles,palette,weather,brightness,camera,position,scenery,events,frequency);
   cinematic.setSignals(prefs.getBoolean("traffic_signals",true));selectTheme();
  } else {
   if(cinematic!=null){cinematic.release();cinematic=null;}
   scene.configure(perLane,speed);scene.setRoad(road);scene.setSceneryEnabled(scenery);scene.setEvents(events,frequency);scene.setCamera(camera,position);scene.setVehicles(vehicles,palette);
   selectTheme();scene.setWeather(weather);scene.setBrightness(brightness);scene.draw(imageCanvas);
   setContentDescription("현재 설정의 배경화면 미리보기");
  }
  invalidate();restart();
 }
 void startPreview() {resumed=true;restart();}
 void stopPreview() {resumed=false;stop();}
 private boolean canRun() {
  return PlaybackPolicy.shouldAnimate(attached,getWindowVisibility()==VISIBLE&&hasWindowFocus(),resumed,released);
 }
 private void stop() {handler.removeCallbacks(frame);lastFrame=0;}
 private void restart() {
  stop();if(canRun()) {nextClockCheck=0;pacer.reset(SystemClock.elapsedRealtimeNanos(),15);handler.post(frame);}
 }
 private void tick() {
  if(!canRun())return;
  long now=SystemClock.elapsedRealtimeNanos();
  if(now>=nextClockCheck){selectTheme();nextClockCheck=now+60_000_000_000L;}
  if(lastFrame!=0) {
   double elapsed=(now-lastFrame)/1_000_000_000.0;
   if(cinematicActive)cinematic.update(elapsed);else scene.update(elapsed);
  }
  lastFrame=now;if(!cinematicActive)scene.draw(imageCanvas);invalidate();
  if(canRun())handler.postDelayed(frame,pacer.nextDelayMillis(SystemClock.elapsedRealtimeNanos()));
 }
 @Override protected void onAttachedToWindow(){super.onAttachedToWindow();attached=true;restart();}
 @Override protected void onDetachedFromWindow(){attached=false;stop();super.onDetachedFromWindow();}
 @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(handler!=null)restart();}
 @Override protected void onWindowVisibilityChanged(int visibility){super.onWindowVisibilityChanged(visibility);if(handler!=null)restart();}
 @Override protected void onSizeChanged(int w,int h,int oldW,int oldH) {
  PreviewLayout.Bounds fitted=PreviewLayout.fit(w,h);bounds.set(fitted.left,fitted.top,fitted.right,fitted.bottom);
 }
 @Override protected void onDraw(Canvas target) {
  super.onDraw(target);target.drawColor(0xff0a1120);
  if(released)return;
  if(cinematicActive) {
   int width=(int)bounds.width(),height=(int)bounds.height();if(width<=0||height<=0)return;
   int saved=target.save();target.translate(bounds.left,bounds.top);target.clipRect(0,0,width,height);
   cinematic.draw(target,width,height);target.restoreToCount(saved);
  } else target.drawBitmap(image,null,bounds,paint);
 }
 void release() {
  if(released)return;released=true;stop();handler.removeCallbacks(settingsChanged);
  prefs.unregisterOnSharedPreferenceChangeListener(listener);image.recycle();scene.release();if(cinematic!=null)cinematic.release();
 }
}
