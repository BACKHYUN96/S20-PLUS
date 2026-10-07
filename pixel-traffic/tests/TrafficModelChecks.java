package com.s20plus.pixeltraffic;

/** Standalone simulation checks; no device/UI claims. Run with a full JDK. */
public final class TrafficModelChecks {
 private static void check(boolean condition,String message) {
  if(!condition) throw new AssertionError(message);
 }
 public static void main(String[] args) {
  TrafficModel a=new TrafficModel(), b=new TrafficModel();
  double down=a.cars[0].y(), up=a.cars[6].y();
  a.update(1.0/30);
  check(a.cars[0].y()>down,"Incoming lane must approach camera");
  check(a.cars[6].y()<up,"Outgoing lane must recede");
  a=new TrafficModel();
  for(int i=0;i<30*600;i++) a.update(1.0/30);
  for(int i=0;i<60*600;i++) b.update(1.0/60);
  for(int i=0;i<a.cars.length;i++) {
   check(Math.abs(a.cars[i].position-b.cars[i].position)<1e-7,"30/60 fps must produce same traffic");
   check(a.cars[i].type==b.cars[i].type,"Recycling must be frame-rate independent");
  }
  int recycled=0;
  a=new TrafficModel();
  for(int frame=0;frame<60*1200;frame++) {
   int old=a.cars[0].type;
   a.update(frame%3==0?.1:1.0/60);
   if(old!=a.cars[0].type) recycled++;
   for(int lane=0;lane<4;lane++) for(int i=0;i<3;i++) {
    TrafficModel.Car car=a.cars[lane*3+i],front=a.cars[lane*3+(i+1)%3];
    check(car.position>=0&&car.position<TrafficModel.TRACK_LENGTH,"Track bounds");
    double gap=(front.position-car.position+TrafficModel.TRACK_LENGTH)%TrafficModel.TRACK_LENGTH;
    check(gap>=TrafficModel.MIN_GAP-1e-7,"Cars must not overlap or overtake");
   }
  }
  check(recycled>10,"Traffic must repeatedly recycle");
  a=new TrafficModel();b=new TrafficModel(); a.update(60);b.update(.1);
  check(a.cars[0].position==b.cars[0].position,"Stalled frame must not teleport traffic");
  double before=a.cars[0].position;
  a.update(Double.NaN);a.update(-1);a.update(Double.POSITIVE_INFINITY);
  check(a.cars[0].position==before,"Invalid delta must not corrupt traffic");
  for(int count=1;count<=6;count++) for(double speed:new double[]{.5,1,1.5}) {
   TrafficModel configured=new TrafficModel(count);configured.setSpeedMultiplier(speed);
   check(configured.cars.length==4*count,"Configured total count");
   for(int step=0;step<30*180;step++) {
    configured.update(1.0/30);
    for(int lane=0;lane<4;lane++)for(int c=0;c<count;c++) {
     TrafficModel.Car car=configured.cars[lane*count+c];
     check(car.position>=0&&car.position<TrafficModel.TRACK_LENGTH,"Configured track bounds");
     if(count>1) {
      TrafficModel.Car front=configured.cars[lane*count+(c+1)%count];
      check((front.position-car.position+TrafficModel.TRACK_LENGTH)%TrafficModel.TRACK_LENGTH>=TrafficModel.MIN_GAP-1e-7,"Configured spacing");
     }
    }
   }
  }
  TrafficModel slow=new TrafficModel(1),fast=new TrafficModel(1);
  slow.setSpeedMultiplier(.5);fast.setSpeedMultiplier(1.5);
  for(int i=0;i<60;i++){slow.update(1.0/60);fast.update(1.0/60);}
  check(slow.cars[0].position>0,"Single car lane must keep moving");
  check(Math.abs(fast.cars[0].position-3*slow.cars[0].position)<1e-7,"Speed multiplier");
  for(int bits=0;bits<16;bits++) {
   boolean surface=(bits&1)!=0,visible=(bits&2)!=0,interactive=(bits&4)!=0,destroyed=(bits&8)!=0;
   check(PlaybackPolicy.shouldAnimate(surface,visible,interactive,destroyed)==(bits==7),"Only visible active surface may animate");
  }
  check(PlaybackPolicy.effectiveFps(60,true,true)==15,"Saver must cap fps");
  check(PlaybackPolicy.effectiveFps(60,false,true)==60,"Saver opt out");
  check(PlaybackPolicy.effectiveFps(30,true,false)==30,"Saver inactive");
  check(PlaybackPolicy.effectiveFps(999,true,false)==30,"Invalid fps defaults");
  System.out.println("PASS: density/speed combinations, isolated lane movement, render gating, saver policy");
  System.out.println("PASS: directions, 30/60 fps equivalence, long-run spacing, recycling, stall clamp, invalid delta");
 }
}
