package com.s20plus.pixeltraffic;

/** Independent artwork landmarks and complete vehicle paths, without Android rendering. */
public final class CinematicGeometryChecks {
 private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
 private static void near(float actual,float expected,float tolerance,String reason){
  check(Math.abs(actual-expected)<=tolerance,reason+": "+actual+" != "+expected);
 }
 public static void main(String[] args){
  near(CinematicGeometry.modelY(260),CinematicGeometry.HORIZON,.001f,"model horizon");
  near(CinematicGeometry.modelY(800),1200,.001f,"model bottom");
  // Coordinates measured independently in the 841x1870 artwork, normalized to 540x1200.
  // A car-center-inside-road assertion missed the old wrong dashed-line alignment.
  float[][] measured={
   {390,368,384,393,5},{450,345,369,395,5},{500,333,360.5f,396,4},{600,296,347,400,6},
   {700,265,335.3f,403.5f,3},{750,251,329.2f,406.5f,3},{800,238,323.6f,407.5f,3},
   {900,214,310.4f,408,3},{1000,189,297.6f,410,3},{1100,167,287,414,4},{1200,143,274.4f,414,4}
  };
  for(float[] point:measured){
   near(CinematicGeometry.boundary(1,point[0]),point[1],point[4],"measured left dashed lane y="+point[0]);
   near(CinematicGeometry.boundary(2,point[0]),point[2],point[4],"measured yellow median y="+point[0]);
   near(CinematicGeometry.boundary(3,point[0]),point[3],point[4],"measured right dashed lane y="+point[0]);
  }
  for(float y=CinematicGeometry.HORIZON-20;y<=1350;y+=.5f){
   near(CinematicGeometry.modelY(CinematicGeometry.inverseModelY(y)),y,.003f,"projective inverse y="+y);
   for(int lane=0;lane<4;lane++){
    float left=CinematicGeometry.boundary(lane,y),right=CinematicGeometry.boundary(lane+1,y);
    check(left<right,"lane ordering y="+y);
    near(CinematicGeometry.laneX(lane,y),(left+right)/2,.001f,"lane follows measured boundaries");
    near(CinematicGeometry.laneWidth(lane,y),right-left,.001f,"lane width follows measured boundaries");
    float x=CinematicGeometry.laneX(lane,y),next=CinematicGeometry.laneX(lane,y+.1f);
    check(Math.abs(next-x)<.25,"lane path jumps y="+y+" lane="+lane);
    check(Float.isFinite(CinematicGeometry.laneSlope(lane,y)),"finite lane heading");
   }
  }
  verifyContinuousSize();
  // Whole-body conservative rectangles include the far corners, not only a center or near edge.
  VehicleLayout profile=VehicleLayout.defaultLayout();
  int poses=0;
  for(float y=CinematicGeometry.HORIZON+15;y<=1600;y+=1)for(int lane=0;lane<4;lane++)for(int type=0;type<6;type++){
   float width=CinematicGeometry.bodyWidth(lane,type,y),length=profile.length(lane,type,y);
   check(width>0&&width<CinematicGeometry.laneWidth(lane,y),"vehicle width inside lane");
   float center=CinematicGeometry.laneX(lane,y),slope=profile.slope(lane,type,y);
   for(int sample=0;sample<=16;sample++){
    float offset=-length*sample/16,bodyY=y+offset;
    if(bodyY<CinematicGeometry.HORIZON||bodyY>1200)continue;
    float bodyX=center+slope*offset;
    check(bodyX-width/2>=CinematicGeometry.boundary(lane,bodyY)-.75,
     "left full-body corner crosses lane: lane="+lane+" type="+type+" ground="+y+" top="+bodyY);
    check(bodyX+width/2<=CinematicGeometry.boundary(lane+1,bodyY)+.75,
     "right full-body corner crosses lane: lane="+lane+" type="+type+" ground="+y+" top="+bodyY);
   }
   if(y>=700&&y<=1200&&type<4)
    check(width/CinematicGeometry.laneWidth(lane,y)>=.46,"car too small for calibrated lane y="+y+" lane="+lane);
   double oldY=CinematicGeometry.inverseModelY(y),headway=y-CinematicGeometry.modelY(oldY-TrafficModel.MIN_GAP*.65);
   check(length<headway-.5,"body exceeds projected minimum headway: lane="+lane+" type="+type+" y="+y);
   poses++;
  }
  // Rain lateral hits retain their classic-road fraction under the new projective depth.
  for(int oldY=260;oldY<=800;oldY++){
   float y=CinematicGeometry.modelY(oldY),l=CinematicGeometry.left(y),r=CinematicGeometry.right(y);
   double t=(oldY-260)/540.0,oldLeft=190-170*t,oldWidth=80+250*t;
   near(CinematicGeometry.remapX(oldLeft,oldY),l,.001f,"rain left edge");
   near(CinematicGeometry.remapX(oldLeft+oldWidth,oldY),r,.001f,"rain right edge");
  }
  for(int width:new int[]{1,320,540,1080,1440})for(int height:new int[]{160,1200,2400})for(int camera=0;camera<2;camera++){
   float scale=CinematicGeometry.scale(width,camera),bottom=CinematicGeometry.offsetY(width,height,camera)+1200*scale;
   near(bottom,height,.001f,"bottom anchor");
   float overflow=540*scale-width;
   near(CinematicGeometry.offsetX(width,camera,0),0,.001f,"left anchor");
   near(CinematicGeometry.offsetX(width,camera,1),-overflow/2,.001f,"center anchor");
   near(CinematicGeometry.offsetX(width,camera,2),-overflow,.001f,"right anchor");
  }
  check(CinematicGeometry.scale(0,1)==0,"empty viewport");
  System.out.println("PASS: independently measured dashed/median lines, projective inverse, continuous off-screen lanes, "+poses+" full-body lane/scale/headway poses, rain remap and camera anchors");
 }
 private static void verifyContinuousSize(){
  // The 0.20 fit shrank an approaching bus from 56.53 to 45.81 pixels here.
  check(CinematicGeometry.bodyWidth(0,4,840)>=CinematicGeometry.bodyWidth(0,4,800),
   "Approaching bus shrinks between y=800 and y=840");
  int samples=0;
  for(int lane=0;lane<4;lane++)for(int type=0;type<6;type++){
   for(float y=CinematicGeometry.HORIZON+15;y<=1599.5f;y+=.25f){
    float first=CinematicGeometry.bodyWidth(lane,type,y);
    float next=CinematicGeometry.bodyWidth(lane,type,y+.25f);
    float after=CinematicGeometry.bodyWidth(lane,type,y+.5f);
    check(next>=first-.001f,"Approaching body shrinks: lane="+lane+" type="+type+" y="+y+" widths="+first+","+next);
    check(next-first<=first*.02f+.03f,"Body size jumps: lane="+lane+" type="+type+" y="+y);
    check(Math.abs(after-2*next+first)<=.004f,
     "Body size rate changes abruptly: lane="+lane+" type="+type+" y="+y);
    samples++;
   }
  }
  System.out.println("PASS: "+samples+" depth samples have monotone body width and continuous size rate");
 }
}
