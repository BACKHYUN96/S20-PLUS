package com.s20plus.pixeltraffic;
public final class FramePacerChecks {
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args) {
  for(int fps:new int[]{15,30,60}) {
   FramePacer pacer=new FramePacer();long start=7_000_000_000L,now=start;pacer.reset(start,fps);
   int count=0;
   while(now-start<60_000_000_000L) {
    // Synthetic 8.2ms rendering, and up to 0.8ms callback jitter.
    now+=8_200_000L;long delay=pacer.nextDelayMillis(now);
    check(delay>=1,"No zero-delay bursts");now+=delay*1_000_000L+(count%3)*400_000L;count++;
   }
   double measured=count*1_000_000_000.0/(now-start);
   check(Math.abs(measured-fps)<.1,"Fractional cadence at "+fps+": "+measured);
  }
  FramePacer slow=new FramePacer();slow.reset(0,60);
  check(slow.nextDelayMillis(2_000_000_000L)<=17,"Skip missed deadlines rather than queue catch-up frames");
  slow.reset(9_000_000_000L,15);
  check(slow.nextDelayMillis(9_000_000_000L)==67,"Reset after pause or settings change");
  System.out.println("PASS: 15/30/60 cadence under synthetic draw/jitter, stalled deadline skipping, restart");
 }
}
