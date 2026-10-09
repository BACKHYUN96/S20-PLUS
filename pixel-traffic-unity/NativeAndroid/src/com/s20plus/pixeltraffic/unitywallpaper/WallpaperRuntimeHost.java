package com.s20plus.pixeltraffic.unitywallpaper;

import android.content.Context;
import android.content.res.Configuration;
import android.os.PowerManager;
import android.util.Log;
import android.view.Surface;
import com.unity3d.player.IUnityPlayerLifecycleEvents;
import com.unity3d.player.UnityPlayerForActivityOrService;

final class WallpaperRuntimeHost {
    private final Context service;
    private final SurfaceArbiter arbiter = new SurfaceArbiter();
    private UnityPlayerForActivityOrService player;
    private Surface bound;
    private int boundWidth, boundHeight;
    private boolean running;
    WallpaperRuntimeHost(Context service) { this.service = service; }
    void add(Object id, boolean preview) { arbiter.add(id, preview); }
    void surface(Object id, Surface surface, int width, int height) {
        arbiter.surface(id, surface, surface != null && surface.isValid(), width, height); reconcile();
    }
    void visible(Object id, boolean value) { arbiter.visible(id, value); reconcile(); }
    void remove(Object id) { arbiter.remove(id); reconcile(); }
    void reconcile() {
        PowerManager power = (PowerManager) service.getSystemService(Context.POWER_SERVICE);
        SurfaceArbiter.Target target = arbiter.choose(power != null && power.isInteractive());
        if (target == null || !((Surface) target.surface).isValid()) { detach(); return; }
        if (player == null) {
            player = new UnityPlayerForActivityOrService(service, new IUnityPlayerLifecycleEvents() {
                @Override public void onUnityPlayerUnloaded() { WallpaperPreferences.rendering(false); }
                @Override public void onUnityPlayerQuitted() { WallpaperPreferences.rendering(false); }
            });
            player.setRunWithoutFocus(true);
        }
        Surface next = (Surface) target.surface;
        if (bound != next || boundWidth != target.width || boundHeight != target.height) {
            if (running) { WallpaperPreferences.rendering(false); player.windowFocusChanged(false); player.pause(); running = false; }
            if (!player.displayChanged(0, next)) { detach(); Log.w("PixelTrafficWallpaper", "Surface attachment deferred."); return; }
            bound = next;
            boundWidth = target.width; boundHeight = target.height;
        }
        if (!running) {
            WallpaperPreferences.rendering(true);
            // resume()/pause() support a Service context; Activity lifecycle wrappers need an Activity.
            player.resume(); player.windowFocusChanged(true); running = true;
        }
    }
    private void detach() {
        WallpaperPreferences.rendering(false);
        if (player == null) return;
        if (running) { player.windowFocusChanged(false); player.pause(); running = false; }
        if (bound != null) { player.displayChanged(0, null); bound = null; }
    }
    void configuration(Configuration configuration) { if (player != null) player.configurationChanged(configuration); }
    void destroy() {
        detach();
        // Unity's quit may terminate its process; settings live in the separate default process.
        if (player != null) { player.destroy(); player = null; }
    }
}
