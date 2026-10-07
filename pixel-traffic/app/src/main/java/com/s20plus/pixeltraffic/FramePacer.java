package com.s20plus.pixeltraffic;

/** Preserve fractional frame periods instead of accumulating millisecond rounding. */
final class FramePacer {
 private long deadline,period;
 void reset(long now,int fps) {deadline=now;period=1_000_000_000L/(fps==15||fps==60?fps:30);}
 long nextDelayMillis(long now) {
  deadline+=period;
  if(deadline<=now) deadline+=((now-deadline)/period+1)*period;
  return Math.max(1,(deadline-now+999_999)/1_000_000);
 }
}
