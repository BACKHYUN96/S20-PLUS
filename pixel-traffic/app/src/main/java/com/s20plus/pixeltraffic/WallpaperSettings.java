package com.s20plus.pixeltraffic;
import android.content.Context;
import android.content.SharedPreferences;

final class WallpaperSettings {
 static final String NAME="wallpaper_settings";
 static final int[] COUNTS={1,2,3,6},FPS={15,30,60},BRIGHTNESS={100,80,60};
 static final double[] SPEED={.5,1,1.5};
 static SharedPreferences prefs(Context context) {return context.getSharedPreferences(NAME,Context.MODE_PRIVATE);}
 static int index(SharedPreferences prefs,String key,int fallback) {
  int maximum=key.equals("camera")?1:key.equals("weather")?4:key.equals("density")?3:(key.equals("road")||key.equals("vehicles"))?3:2;
  return Math.max(0,Math.min(maximum,prefs.getInt(key,fallback)));
 }
}
