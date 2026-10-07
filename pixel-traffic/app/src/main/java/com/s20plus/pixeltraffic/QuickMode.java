package com.s20plus.pixeltraffic;
final class QuickMode {
 final int densityIndex,fpsIndex;
 private QuickMode(int density,int fps){densityIndex=density;fpsIndex=fps;}
 static QuickMode forIndex(int mode) {
  return mode==0?new QuickMode(0,0):mode==2?new QuickMode(2,2):new QuickMode(1,1);
 }
}
