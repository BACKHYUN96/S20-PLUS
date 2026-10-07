package com.s20plus.pixeltraffic;

/** One occasional event at a time, driven solely by bounded visible/active time. */
final class SceneEventModel {
 static final double DURATION=10;
 private static final double[] FIRST_WAIT={18,10,5},PERIOD={72,42,24};
 private boolean enabled=true,active;
 private int frequency=1,variant;
 private double remaining=FIRST_WAIT[1],elapsed;
 private long starts;
 void configure(boolean value,int selected) {
  enabled=value;int next=Math.max(0,Math.min(2,selected));
  if(next!=frequency){frequency=next;active=false;elapsed=0;remaining=FIRST_WAIT[next];}
 }
 void update(double seconds) {
  if(!enabled||!Double.isFinite(seconds)||seconds<=0)return;
  double delta=Math.min(.1,seconds);
  if(active) {
   double advance=Math.min(delta,DURATION-elapsed);elapsed+=advance;delta-=advance;
   if(elapsed>=DURATION-1e-9){active=false;elapsed=0;remaining=PERIOD[frequency]-DURATION;}
  }
  if(!active) {
   remaining-=delta;
   if(remaining<=1e-9){elapsed=Math.max(0,-remaining);remaining=0;active=true;starts++;variant=(variant+1)%6;}
  } else elapsed+=delta;
 }
 boolean visible(){return enabled&&active;}
 double progress(){return active?elapsed/DURATION:0;}
 int variant(){return variant;}
 long starts(){return starts;}
}
