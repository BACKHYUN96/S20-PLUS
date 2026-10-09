package com.s20plus.pixeltraffic.unitywallpaper;

public final class SurfaceArbiterChecks {
    private static void need(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    public static void main(String[] args) {
        SurfaceArbiter a = new SurfaceArbiter(); Object live = new Object(), preview = new Object();
        Object homeSurface = new Object(), previewSurface = new Object(), recreated = new Object();
        a.add(live, false); a.surface(live, homeSurface, true, 1440, 3200); a.visible(live, true);
        need(a.choose(true).surface == homeSurface, "Live surface not selected.");
        a.add(preview, true); a.visible(preview, true); a.surface(preview, previewSurface, false, 540, 1200);
        need(a.choose(true).surface == homeSurface, "Invalid preview hides a working live surface.");
        a.surface(preview, previewSurface, true, 540, 1200);
        need(a.choose(true).surface == previewSurface, "Preview does not take priority over live.");
        need(a.choose(false) == null, "Screen-off keeps rendering.");
        a.surface(preview, null, false, 0, 0);
        need(a.choose(true).surface == homeSurface, "Destroyed preview does not fall back to live.");
        a.surface(live, recreated, true, 1440, 3120);
        need(a.choose(true).surface == recreated && a.choose(true).height == 3120, "Surface generation/size is stale.");
        a.visible(live, false); need(a.choose(true) == null, "Hidden live engine keeps rendering.");
        a.surface(preview, previewSurface, true, 0, 1200); need(a.choose(true) == null, "Zero-width preview can bind.");
        a.surface(preview, previewSurface, true, 540, 1200); a.visible(preview, false);
        need(a.choose(true) == null, "Hidden preview keeps rendering.");
        a.visible(live, true); a.remove(preview); need(a.choose(true).surface == recreated && a.count() == 1, "Preview removal leaks or loses home.");
        for (int i = 0; i < 1000; i++) {
            Object id = new Object(), surface = new Object(); a.add(id, true); a.surface(id, surface, true, 1080, 2400); a.visible(id, true);
            need(a.choose(true).surface == surface, "Repeated preview failed.");
            a.visible(id, false); a.remove(id); need(a.choose(true).surface == recreated && a.count() == 1, "Repeated preview leaked engine references.");
        }
        a.remove(live); need(a.count() == 0 && a.choose(true) == null, "Final engine does not detach.");
        System.out.println("PASS: production surface selection, preview/live fallback, off/hidden, invalid/zero-size, generation/resize, 1000 recreate/remove cycles; not an Android device test.");
    }
}
