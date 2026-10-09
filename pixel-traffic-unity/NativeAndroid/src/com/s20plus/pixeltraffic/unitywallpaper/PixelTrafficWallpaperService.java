package com.s20plus.pixeltraffic.unitywallpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public final class PixelTrafficWallpaperService extends WallpaperService {
    private WallpaperRuntimeHost host;
    private final BroadcastReceiver screen = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) { if (host != null) host.reconcile(); }
    };
    @Override public void onCreate() {
        super.onCreate(); WallpaperPreferences.initialize(this, true); host = new WallpaperRuntimeHost(this);
        IntentFilter filter = new IntentFilter(); filter.addAction(Intent.ACTION_SCREEN_ON); filter.addAction(Intent.ACTION_SCREEN_OFF);
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(screen, filter, Context.RECEIVER_NOT_EXPORTED);
        else registerReceiver(screen, filter);
    }
    @Override public Engine onCreateEngine() { return new CityEngine(); }
    @Override public void onConfigurationChanged(Configuration config) { super.onConfigurationChanged(config); if (host != null) host.configuration(config); }
    @Override public void onDestroy() {
        unregisterReceiver(screen);
        if (host != null) { host.destroy(); host = null; }
        super.onDestroy();
    }
    private final class CityEngine extends Engine {
        @Override public void onCreate(SurfaceHolder holder) { super.onCreate(holder); setTouchEventsEnabled(false); host.add(this, isPreview()); }
        @Override public void onSurfaceCreated(SurfaceHolder holder) { super.onSurfaceCreated(holder); Rect r = holder.getSurfaceFrame(); host.surface(this, holder.getSurface(), r.width(), r.height()); }
        @Override public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) { super.onSurfaceChanged(holder, format, width, height); host.surface(this, holder.getSurface(), width, height); }
        @Override public void onVisibilityChanged(boolean visible) { host.visible(this, visible); }
        @Override public void onSurfaceDestroyed(SurfaceHolder holder) { host.surface(this, null, 0, 0); super.onSurfaceDestroyed(holder); }
        @Override public void onDestroy() { if (host != null) host.remove(this); super.onDestroy(); }
    }
}
