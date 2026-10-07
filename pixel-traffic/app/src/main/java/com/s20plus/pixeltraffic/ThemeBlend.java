package com.s20plus.pixeltraffic;
/** Retargetable 4-second blend. No allocation during update; time follows active simulation. */
final class ThemeBlend {
 final double[] weights={0,1,0};
 private final double[] start=new double[3];
 private int target=1;
 private double elapsed=4;
 int target(){return target;}
 void snap(int value) {
  target=Math.max(0,Math.min(2,value));elapsed=4;
  for(int i=0;i<3;i++)weights[i]=start[i]=i==target?1:0;
 }
 void select(int value) {int next=Math.max(0,Math.min(2,value));if(next==target)return;System.arraycopy(weights,0,start,0,3);target=next;elapsed=0;}
 void update(double seconds) {
  if(!Double.isFinite(seconds)||seconds<=0||elapsed>=4)return;
  elapsed=Math.min(4,elapsed+Math.min(.1,seconds));if(elapsed>4-1e-9)elapsed=4;double t=elapsed/4;t=t*t*(3-2*t);
  for(int i=0;i<3;i++)weights[i]=start[i]*(1-t)+(i==target?t:0);
 }
}
