package com.s20plus.pixeltraffic;
public final class FrameStatsChecks {
 private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args) {
  FrameStats stats=new FrameStats();check(stats.fps()==0,"No frames");
  for(int i=0;i<=60;i++)stats.record(i*16_666_667L,2_000_000L);
  check(Math.abs(stats.fps()-60)<.001,"Known 60fps cadence");
  check(stats.meanDrawMs()==2,"Mean timing");
  stats.reset();check(stats.frames==0&&stats.fps()==0&&stats.meanDrawMs()==0,"Reset");
  stats.record(9_000_000_000L,1_000_000L);check(stats.fps()==0,"Single frame not an fps estimate");
  stats.record(9_100_000_000L,3_000_000L);check(stats.fps()==10&&stats.meanDrawMs()==2,"Resumed clock window");
  System.out.println("PASS: submission frequency, draw timing, empty/single-frame and reset windows");
 }
}
