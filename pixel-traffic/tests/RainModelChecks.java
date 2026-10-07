package com.s20plus.pixeltraffic;
public final class RainModelChecks {
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args) {
  RainModel off=new RainModel();check(off.count()==0,"Default weather off");
  double before=off.drops[0].y;off.update(.1);check(off.drops[0].y==before,"Clear weather does not simulate particles");
  for(int level:new int[]{1,2}) {
   RainModel a=new RainModel(),b=new RainModel();a.setLevel(level);b.setLevel(level);
   check(a.count()==(level==1?28:72),"Intensity count");
   for(int i=0;i<30*120;i++)a.update(1.0/30);
   for(int i=0;i<60*120;i++)b.update(1.0/60);
   for(int i=0;i<a.count();i++) {
    check(Math.abs(a.drops[i].x-b.drops[i].x)<1e-7&&Math.abs(a.drops[i].y-b.drops[i].y)<1e-7,"Frame-rate independent rain");
    check(a.drops[i].splashTicks==b.drops[i].splashTicks,"Same splash phase");
   }
   int hits=0;RainModel.Drop reference=a.drops[0];
   for(int step=0;step<60*120;step++) {
    a.update(1.0/60);
    check(a.drops[0]==reference,"Particle pool reused");
    for(int i=0;i<a.count();i++) {
     RainModel.Drop d=a.drops[i];check(Double.isFinite(d.x)&&Double.isFinite(d.y),"Finite particles");
     check(d.y<=d.hitY+1e-7&&d.splashTicks>=0&&d.splashTicks<=8,"Drop lifecycle");
     if(d.splashTicks==8) {
      double t=(d.hitY-260)/540,l=190-170*t,r=270+80*t;
      check(d.x>=l&&d.x<=r,"Splash lands on road");hits++;
     }
    }
   }
   check(hits>100,"Particles recycle and splash repeatedly");
   a.setLevel(0);before=a.drops[0].y;a.update(.1);check(a.drops[0].y==before,"Disabling freezes particles");
  }
  RainModel stalled=new RainModel(),normal=new RainModel();stalled.setLevel(2);normal.setLevel(2);
  stalled.update(100);normal.update(.1);check(stalled.drops[0].y==normal.drops[0].y,"Stall clamp");
  before=stalled.drops[0].y;stalled.update(Double.NaN);stalled.update(-1);check(stalled.drops[0].y==before,"Invalid delta ignored");
  System.out.println("PASS: clear/light/heavy, 30/60 cadence, particle reuse, on-road splashes, recycling and stall protection");
 }
}
