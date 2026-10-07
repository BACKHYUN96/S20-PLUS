package com.s20plus.pixeltraffic;

/** Distance-only vehicle dimensions; heading spans the complete body instead of a local tangent. */
final class VehicleLayout {
 private static final float[] OCCUPANCY={.625f,.625f,.65f,.625f,.72f,.72f};
 private static final float[] CONSERVATIVE_ASPECT={1.6f,1.6f,1.65f,1.6f,2.15f,2.0f};
 // Fixed margins calibrated against the full measured road with conservative aspect ratios.
 // Unlike a per-frame fit, these cannot make an approaching vehicle shrink at a road bend.
 private static final float[] MARGIN={1,1,1,1,.88f,.88f};
 private final float[][] aspect=new float[6][2];
 private final float[] farWidth=new float[6],widthPerPixel=new float[6];

 VehicleLayout(float[][] sourceAspect){
  for(int type=0;type<6;type++)for(int direction=0;direction<2;direction++){
   float value=sourceAspect[type][direction];
   if(!Float.isFinite(value)||value<=0||value>CONSERVATIVE_ASPECT[type]+.001f)
    throw new IllegalArgumentException("Vehicle artwork exceeds calibrated aspect ratio");
   aspect[type][direction]=value;
  }
  // A car's physical scale depends on depth/type, not the unequal painted lane widths.
  // Shared endpoints retain monotone scaling; whole-body checks cover every actual lane.
  float farLane=Float.POSITIVE_INFINITY,nearLane=Float.POSITIVE_INFINITY;
  for(int lane=0;lane<4;lane++){
   farLane=Math.min(farLane,CinematicGeometry.laneWidth(lane,CinematicGeometry.HORIZON));
   nearLane=Math.min(nearLane,CinematicGeometry.laneWidth(lane,CinematicGeometry.HEIGHT));
  }
  for(int type=0;type<6;type++){
   float fraction=OCCUPANCY[type]*MARGIN[type];
   float far=farLane*fraction,near=nearLane*fraction*1.15f;
   farWidth[type]=far;
   widthPerPixel[type]=(near-far)/(CinematicGeometry.HEIGHT-CinematicGeometry.HORIZON);
  }
 }
 private static final class Defaults {
  static final VehicleLayout VALUE=create();
  private static VehicleLayout create(){
   float[][] ratios=new float[6][2];
   for(int type=0;type<6;type++)ratios[type][0]=ratios[type][1]=CONSERVATIVE_ASPECT[type];
   return new VehicleLayout(ratios);
  }
 }
 static VehicleLayout defaultLayout(){return Defaults.VALUE;}
 float width(int lane,int type,float groundY){
  return farWidth[type]+Math.max(0,groundY-CinematicGeometry.HORIZON)*widthPerPixel[type];
 }
 float length(int lane,int type,float groundY){return width(lane,type,groundY)*aspect[type][lane<2?0:1];}
 float slope(int lane,int type,float groundY){
  float length=length(lane,type,groundY);
  return (CinematicGeometry.laneX(lane,groundY)-CinematicGeometry.laneX(lane,groundY-length))/length;
 }
}
