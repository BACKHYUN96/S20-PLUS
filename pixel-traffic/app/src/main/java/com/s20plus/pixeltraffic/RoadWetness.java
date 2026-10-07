package com.s20plus.pixeltraffic;

/** Retargetable surface transition, driven only by active simulation time. */
final class RoadWetness {
 private double value,start,target,elapsed=4;
 private boolean initialized;
 float targetValue(){return (float)target;}
 float value(){return (float)value;}
 void select(int weather){
  double next=CinematicEffects.wetness(weather);
  if(!initialized){value=start=target=next;initialized=true;return;}
  if(next==target)return;
  start=value;target=next;elapsed=0;
 }
 void update(double seconds){
  if(!Double.isFinite(seconds)||seconds<=0||elapsed>=4)return;
  elapsed=Math.min(4,elapsed+Math.min(.1,seconds));if(elapsed>4-1e-9)elapsed=4;double t=elapsed/4;t=t*t*(3-2*t);
  value=start*(1-t)+target*t;
 }
}
