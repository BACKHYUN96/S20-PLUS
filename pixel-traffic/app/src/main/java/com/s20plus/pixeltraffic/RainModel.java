package com.s20plus.pixeltraffic;
import java.util.Random;

/** Fixed-size, deterministic particle pool. No Android or per-frame allocation. */
final class RainModel {
 static final int MAX_DROPS=72;
 static final double STEP=1.0/60;
 static final class Drop {
  double x,y,speed,hitY;
  int splashTicks;
 }
 final Drop[] drops=new Drop[MAX_DROPS];
 private final Random random=new Random(803);
 private int level;
 private double remainder;
 RainModel() {for(int i=0;i<drops.length;i++){drops[i]=new Drop();spawn(drops[i],true);}}
 int level() {return level;}
 int count() {return level==0?0:level==1?28:MAX_DROPS;}
 void setLevel(int value) {
  int next=Math.max(0,Math.min(2,value));if(next==level)return;
  level=next;remainder=0;
  for(int i=0;i<count();i++)spawn(drops[i],true);
 }
 private void spawn(Drop drop,boolean initial) {
  drop.hitY=290+random.nextDouble()*495;
  double t=(drop.hitY-260)/540,left=190-170*t,width=80+250*t;
  double hitX=left+8+random.nextDouble()*(width-16);
  drop.speed=level==2?440+random.nextDouble()*120:300+random.nextDouble()*80;
  drop.y=initial?180+random.nextDouble()*(drop.hitY-180):160-random.nextDouble()*100;
  drop.x=hitX+(drop.hitY-drop.y)/drop.speed*45;
  drop.splashTicks=0;
 }
 void update(double seconds) {
  if(level==0||!Double.isFinite(seconds)||seconds<=0)return;
  remainder+=Math.min(.1,seconds);
  while(remainder+1e-10>=STEP) {
   for(int i=0;i<count();i++) {
    Drop d=drops[i];
    if(d.splashTicks>0) {if(--d.splashTicks==0)spawn(d,false);continue;}
    double elapsed=Math.min(STEP,(d.hitY-d.y)/d.speed);
    d.y+=d.speed*elapsed;d.x-=45*elapsed;
    if(d.y>=d.hitY-1e-8) {d.y=d.hitY;d.splashTicks=8;}
   }
   remainder-=STEP;
  }
 }
}
