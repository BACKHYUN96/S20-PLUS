package com.s20plus.pixeltraffic;

/** Shared artwork/traffic coordinates. No Android dependencies or per-frame objects. */
final class CinematicGeometry {
 static final int WIDTH=540,HEIGHT=1200;
 static final float HORIZON=390;
 private static final float ROAD_HEIGHT=HEIGHT-HORIZON;
 private static final double PERSPECTIVE=2.1;
 private static final float[] SAMPLE_Y={390,450,500,600,700,800,900,1000,1100,1200};
 // Independently measured road-edge lines, dashed dividers and double-yellow midpoint.
 // Authored lanes have unequal widths: dividing each half-road in two is incorrect.
 private static final float[][] BOUNDARIES={
  {350,310,282,226,192,135,94,58,20,-18},
  {368,345,333,296,265,238,214,189,167,143},
  {384,369,360.5f,347,335.3f,323.6f,310.4f,297.6f,287,274.4f},
  {393,395,396,400,403.5f,407.5f,408,410,414,414},
  {407,421,434,462,485,509,522,530,538,550}
 };
 private static final float[][] TANGENTS=tangents();
 private CinematicGeometry(){}
 static float modelY(double originalY){
  double t=(originalY-260)/540;
  return (float)(HORIZON+ROAD_HEIGHT*t/(PERSPECTIVE+(1-PERSPECTIVE)*t));
 }
 static double inverseModelY(float groundY){
  double fraction=(groundY-HORIZON)/ROAD_HEIGHT;
  return 260+540*PERSPECTIVE*fraction/(1+(PERSPECTIVE-1)*fraction);
 }
 private static float depth(float y){return Math.max(0,Math.min(1,(y-HORIZON)/ROAD_HEIGHT));}
 private static float[][] tangents(){
  float[][] values=new float[BOUNDARIES.length][SAMPLE_Y.length];
  for(int line=0;line<BOUNDARIES.length;line++){
   float[] points=BOUNDARIES[line],slopes=values[line];int last=points.length-1;
   slopes[0]=(points[1]-points[0])/(SAMPLE_Y[1]-SAMPLE_Y[0]);
   slopes[last]=(points[last]-points[last-1])/(SAMPLE_Y[last]-SAMPLE_Y[last-1]);
   for(int i=1;i<last;i++){
    float previousStep=SAMPLE_Y[i]-SAMPLE_Y[i-1],nextStep=SAMPLE_Y[i+1]-SAMPLE_Y[i];
    float before=(points[i]-points[i-1])/previousStep,after=(points[i+1]-points[i])/nextStep;
    if(before*after<=0){slopes[i]=0;continue;}
    float w1=2*nextStep+previousStep,w2=nextStep+2*previousStep;
    slopes[i]=(w1+w2)/(w1/before+w2/after);
   }
  }
  return values;
 }
 private static float sample(int line,float y,boolean derivative){
  float[] points=BOUNDARIES[line],slopes=TANGENTS[line];int last=SAMPLE_Y.length-1;
  if(y<=SAMPLE_Y[0])return derivative?slopes[0]:points[0]+slopes[0]*(y-SAMPLE_Y[0]);
  if(y>=SAMPLE_Y[last])return derivative?slopes[last]:points[last]+slopes[last]*(y-SAMPLE_Y[last]);
  int index=0;while(y>SAMPLE_Y[index+1])index++;
  float step=SAMPLE_Y[index+1]-SAMPLE_Y[index],t=(y-SAMPLE_Y[index])/step,t2=t*t;
  if(derivative)return (6*t2-6*t)*points[index]/step+(3*t2-4*t+1)*slopes[index]
    +(-6*t2+6*t)*points[index+1]/step+(3*t2-2*t)*slopes[index+1];
  return (2*t2*t-3*t2+1)*points[index]+(t2*t-2*t2+t)*step*slopes[index]
    +(-2*t2*t+3*t2)*points[index+1]+(t2*t-t2)*step*slopes[index+1];
 }
 static float boundary(int line,float y){return sample(Math.max(0,Math.min(4,line)),y,false);}
 static float left(float y){return boundary(0,y);}
 static float right(float y){return boundary(4,y);}
 static float center(float y){return boundary(2,y);}
 static float laneWidth(int lane,float y){int safe=Math.max(0,Math.min(3,lane));return boundary(safe+1,y)-boundary(safe,y);}
 static float laneX(int lane,float y){int safe=Math.max(0,Math.min(3,lane));return (boundary(safe,y)+boundary(safe+1,y))/2;}
 static float laneSlope(int lane,float y){int safe=Math.max(0,Math.min(3,lane));return (sample(safe,y,true)+sample(safe+1,y,true))/2;}
 /** Conservative default profile for pure geometry checks; scenes use their actual artwork ratios. */
 static float bodyWidth(int lane,int type,float y){
  return VehicleLayout.defaultLayout().width(Math.max(0,Math.min(3,lane)),Math.max(0,Math.min(5,type)),y);
 }
 static float spriteScale(float y){return .33f+1.42f*depth(y);}
 /** RainModel stores hits in the classic road's coordinates; preserve lateral hit fraction. */
 static float remapX(double oldX,double oldY){
  double t=(oldY-260)/540,oldLeft=190-170*t,oldWidth=80+250*t;
  float y=modelY(oldY),newLeft=left(y),newWidth=right(y)-newLeft;
  return (float)(newLeft+(oldX-oldLeft)/Math.max(1,oldWidth)*newWidth);
 }
 static float scale(int width,int camera){return Math.max(0,width)/(float)WIDTH*(camera==1?1.5f:1);}
 static float offsetX(int width,int camera,int position){return -Math.max(0,WIDTH*scale(width,camera)-width)*Math.max(0,Math.min(2,position))/2;}
 static float offsetY(int width,int height,int camera){return height-HEIGHT*scale(width,camera);}
}
