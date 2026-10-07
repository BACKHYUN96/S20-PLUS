package com.s20plus.pixeltraffic;

/** Device-independent rendering policy. No claim about actual power consumption. */
final class PlaybackPolicy {
 static boolean shouldAnimate(boolean surface,boolean visible,boolean interactive,boolean destroyed) {
  return surface&&visible&&interactive&&!destroyed;
 }
 static int effectiveFps(int requested,boolean followSaver,boolean powerSaver) {
  int fps=requested==15||requested==60?requested:30;
  return followSaver&&powerSaver?Math.min(fps,15):fps;
 }
 static long intervalMillis(int fps) {return Math.round(1000.0/fps);}
}
