package com.s20plus.pixeltraffic;

/** Counts successful frame submissions; this is not display/GPU presentation FPS. */
final class FrameStats {
 long frames,firstNanos,lastNanos,totalDrawNanos;
 void reset() {frames=firstNanos=lastNanos=totalDrawNanos=0;}
 void record(long now,long drawNanos) {
  if(frames==0)firstNanos=now;
  lastNanos=now;frames++;totalDrawNanos+=Math.max(0,drawNanos);
 }
 double fps() {return frames>1&&lastNanos>firstNanos?(frames-1)*1_000_000_000.0/(lastNanos-firstNanos):0;}
 double meanDrawMs() {return frames==0?0:totalDrawNanos/1_000_000.0/frames;}
}
