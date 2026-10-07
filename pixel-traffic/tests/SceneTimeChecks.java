package com.s20plus.pixeltraffic;
public final class SceneTimeChecks {
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args) {
  for(int hour=0;hour<24;hour++) {
   int expected=hour<6||hour>=20?2:hour<17?0:1;
   check(SceneTimePolicy.themeForHour(hour)==expected,"Schedule at "+hour);
  }
  check(SceneTimePolicy.themeForHour(24)==2&&SceneTimePolicy.themeForHour(-1)==2,"Normalize hours");
  QuickMode low=QuickMode.forIndex(0),balanced=QuickMode.forIndex(1),smooth=QuickMode.forIndex(2);
  check(low.densityIndex==0&&low.fpsIndex==0,"Economy");
  check(balanced.densityIndex==1&&balanced.fpsIndex==1,"Balanced");
  check(smooth.densityIndex==2&&smooth.fpsIndex==2,"Smooth");
  check(QuickMode.forIndex(99).fpsIndex==1,"Invalid preset fallback");
  System.out.println("PASS: all 24 local hours, boundary normalization, quick-mode mapping");
 }
}
