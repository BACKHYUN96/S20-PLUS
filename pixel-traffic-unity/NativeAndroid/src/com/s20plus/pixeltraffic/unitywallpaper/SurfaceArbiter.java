package com.s20plus.pixeltraffic.unitywallpaper;

import java.util.LinkedHashMap;

/** One render target for the process, even when Android keeps live and preview engines together. */
final class SurfaceArbiter {
    static final class Target {
        final boolean preview;
        Object surface;
        boolean valid, visible;
        int width, height;
        Target(boolean preview) { this.preview = preview; }
    }
    private final LinkedHashMap<Object, Target> targets = new LinkedHashMap<>();
    void add(Object id, boolean preview) { targets.put(id, new Target(preview)); }
    void surface(Object id, Object surface, boolean valid, int width, int height) {
        Target t = targets.get(id);
        if (t == null) return;
        t.surface = surface; t.valid = valid; t.width = width; t.height = height;
    }
    void visible(Object id, boolean visible) {
        Target t = targets.get(id); if (t != null) t.visible = visible;
    }
    void remove(Object id) { targets.remove(id); }
    int count() { return targets.size(); }
    Target choose(boolean screenInteractive) {
        if (!screenInteractive) return null;
        Target live = null, preview = null;
        for (Target t : targets.values()) {
            if (!t.visible || !t.valid || t.surface == null || t.width <= 0 || t.height <= 0) continue;
            if (t.preview) preview = t; else live = t;
        }
        return preview != null ? preview : live;
    }
}
