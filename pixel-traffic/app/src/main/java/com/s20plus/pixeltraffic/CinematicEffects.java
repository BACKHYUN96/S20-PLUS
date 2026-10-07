package com.s20plus.pixeltraffic;

/** Bounded, continuous effect parameters in the same ground coordinates as VehicleLayout. */
final class CinematicEffects {
 private CinematicEffects(){}
 static float depth(float groundY){return Math.max(0,Math.min(1,(groundY-CinematicGeometry.HORIZON)/810));}
 // Clear/snow have a dry base; fog keeps a slightly damp surface.
 static float wetness(int weather){return weather==2?1:weather==1?.68f:weather==4?.35f:0;}
 static float reflectionLength(float width,float groundY,int weather,boolean away){return reflectionLength(width,groundY,wetness(weather),away);}
 static float reflectionLength(float width,float groundY,float wetness,boolean away){
  return width*(.5f+wetness*2.7f)*(.65f+.35f*depth(groundY))*(away?1:1.12f);
 }
 static int reflectionAlpha(float groundY,int weather){return reflectionAlpha(groundY,wetness(weather));}
 static int reflectionAlpha(float groundY,float wetness){return (int)(175*wetness*(.5f+.5f*depth(groundY)));}
 static int lampAlpha(float groundY,float wetness){return (int)(100+depth(groundY)*55+wetness*20);}
 static float streetExposure(float bodyCenterY,int lane){
  float exposure=0;
  for(int i=0;i<5;i++){
   float distance=(bodyCenterY-(530+i*140))/42;
   exposure=Math.max(exposure,(float)Math.exp(-distance*distance*.5));
  }
  return exposure*(lane==0||lane==3?1:.65f);
 }
 static float rainScale(float groundY){return .28f+1.12f*depth(groundY);}
 static float rainY(double modelY,double hitY){
  float ground=CinematicGeometry.modelY(hitY);
  return ground-(float)(hitY-modelY)*1.5f*rainScale(ground);
 }
 static float sprayPhase(double seconds,int seed,int particle){
  double value=seconds*3+seed*.173+particle*.25;
  return (float)(value-Math.floor(value));
 }
 static int sprayAlpha(float phase,int level){return level==0?0:(int)(Math.sin(phase*Math.PI)*(level==2?88:48));}
}
