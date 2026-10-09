package com.s20plus.pixeltraffic.unitywallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

public final class WallpaperPreferences {
    private static Context context;
    private static boolean wallpaperRuntime, warned;
    private static volatile boolean rendering;
    private static long refreshed = -1000;
    private static Bundle snapshot;
    private WallpaperPreferences() { }
    static void initialize(Context c, boolean wallpaper) {
        context = c.getApplicationContext(); wallpaperRuntime = wallpaper;
    }
    static SharedPreferences store(Context c) {
        return c.getSharedPreferences("pixel_unity_wallpaper", Context.MODE_PRIVATE);
    }
    static int population(Context c) {
        return Math.max(4, Math.min(100, store(c).getInt("people", 32)));
    }
    static void savePopulation(Context c, int count) {
        store(c).edit().putInt("people", Math.max(4, Math.min(100, count))).apply();
    }
    static void saveEconomy(Context c, boolean economy) { store(c).edit().putBoolean("economy", economy).apply(); }
    static boolean economy(Context c) { return store(c).getBoolean("economy", false); }
    static Bundle read(Context c) {
        Bundle b = new Bundle(); b.putInt("people", population(c)); b.putInt("fps", economy(c) ? 15 : 30); return b;
    }
    private static synchronized Bundle current() {
        long now = SystemClock.elapsedRealtime();
        if (snapshot != null && now - refreshed < 500) return snapshot;
        try {
            if (context != null) {
                Bundle b = context.getContentResolver().call(
                        Uri.parse("content://" + context.getPackageName() + ".wallpaper.settings"), "read", null, null);
                if (b != null) { snapshot = b; refreshed = now; return b; }
            }
        } catch (RuntimeException e) {
            if (!warned) { Log.w("PixelTrafficWallpaper", "Settings unavailable; keeping last values."); warned = true; }
        }
        if (snapshot == null) { snapshot = new Bundle(); snapshot.putInt("people", 32); snapshot.putInt("fps", 30); }
        refreshed = now; return snapshot;
    }
    public static boolean isWallpaperRuntime() { return wallpaperRuntime; }
    public static boolean isRendering() { return rendering; }
    static void rendering(boolean value) { rendering = value; }
    public static int getPopulation() { return Math.max(4, Math.min(100, current().getInt("people", 32))); }
    public static int getFrameRate() {
        PowerManager power = context == null ? null : (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return power != null && power.isPowerSaveMode() ? 15 : current().getInt("fps", 30);
    }
}
