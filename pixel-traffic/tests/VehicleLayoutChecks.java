package com.s20plus.pixeltraffic;

/** Dense numeric regressions for cached size profiles; no Android dependencies. */
public final class VehicleLayoutChecks {
 private static final float STEP=.25f;
 private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
 private static void near(float actual,float expected,float tolerance,String reason){
  check(Math.abs(actual-expected)<=tolerance,reason+": "+actual+" != "+expected);
 }
 public static void main(String[] args){
  // Different front/rear silhouettes prevent a direction-independent ratio from passing.
  float[][] aspects={{1.30f,1.42f},{1.43f,1.54f},{1.56f,1.65f},{1.38f,1.60f},{2.10f,2.15f},{1.85f,2.00f}};
  verify(new VehicleLayout(aspects),aspects,"direction-specific profile");
  verify(VehicleLayout.defaultLayout(),null,"conservative default profile");
 }
 static void verify(VehicleLayout profile,float[][] aspects,String label){
  int poses=0;
  check(profile.width(0,4,840)>=profile.width(0,4,800),label+": bus shrinks y=800 to y=840");
  for(int lane=0;lane<4;lane++)for(int type=0;type<6;type++){
   float referenceRatio=aspects==null?profile.length(lane,type,900)/profile.width(lane,type,900):aspects[type][lane>=2?1:0];
   for(float y=CinematicGeometry.HORIZON+15;y<=1599.5f;y+=STEP){
    float width=profile.width(lane,type,y),length=profile.length(lane,type,y),slope=profile.slope(lane,type,y);
    String pose=label+": lane="+lane+" type="+type+" y="+y;
    check(Float.isFinite(width)&&Float.isFinite(length)&&Float.isFinite(slope)&&width>0&&length>0,pose+" invalid body dimensions or heading");
    near(width,profile.width(0,type,y),.00001f,pose+" same type/depth changes physical size across lanes");
    near(length,profile.length(lane<2?0:2,type,y),.00001f,pose+" same direction/depth changes body length across lanes");
    near(length/width,referenceRatio,.00001f,pose+" body aspect changed with depth");
    float nextWidth=profile.width(lane,type,y+STEP),afterWidth=profile.width(lane,type,y+2*STEP);
    float nextLength=profile.length(lane,type,y+STEP),afterLength=profile.length(lane,type,y+2*STEP);
    check(nextWidth>=width-.001f&&nextLength>=length-.002f,pose+" approaching body shrinks");
    check(nextWidth-width<=width*.02f+.03f&&nextLength-length<=length*.02f+.06f,pose+" body size jumps");
    check(Math.abs(afterWidth-2*nextWidth+width)<=.004f&&Math.abs(afterLength-2*nextLength+length)<=.009f,pose+" body size rate jumps");
    float angle=(float)Math.atan(slope),nextAngle=(float)Math.atan(profile.slope(lane,type,y+STEP));
    float afterAngle=(float)Math.atan(profile.slope(lane,type,y+2*STEP));
    check(Math.abs(nextAngle-angle)<=.003f,pose+" heading jumps");
    check(Math.abs(afterAngle-2*nextAngle+angle)<=.0001f,pose+" heading rate jumps");
    near(CinematicGeometry.laneX(lane,y)-slope*length,CinematicGeometry.laneX(lane,y-length),.003f,pose+" heading misses far body center");
    for(int sample=0;sample<=16;sample++){
     float offset=-length*sample/16,bodyY=y+offset;
     if(bodyY<CinematicGeometry.HORIZON||bodyY>1200)continue;
     float bodyX=CinematicGeometry.laneX(lane,y)+slope*offset;
     check(bodyX-width/2>=CinematicGeometry.boundary(lane,bodyY)-.75f&&bodyX+width/2<=CinematicGeometry.boundary(lane+1,bodyY)+.75f,pose+" complete body crosses lane at y="+bodyY);
    }
    double original=CinematicGeometry.inverseModelY(y);
    double headway=y-CinematicGeometry.modelY(original-TrafficModel.MIN_GAP*.65);
    check(length<headway-.5,pose+" body exceeds projected minimum headway");
    poses++;
   }
  }
  System.out.println("PASS: "+label+", "+poses+" monotone width/length, size-rate, aspect, whole-body heading, heading-rate, lane and headway poses");
 }
}
