package com.s20plus.pixeltraffic;

/** Display-referred CCT approximations; Kelvin describes the design, not measured phone output. */
final class VehicleLighting {
 static final int[] TEMPERATURES={3000,4500,6500};
 static final Profile[] PROFILES={
  new Profile(6500,0,1,0),new Profile(6500,6500,1.05f,.48f),
  new Profile(6500,0,1.05f,0),new Profile(6500,0,1,0),
  new Profile(4500,3000,1.12f,.55f),new Profile(4500,3000,1.14f,.62f)
 };
 static final class Profile {
  final int headKelvin,fogKelvin,headIndex,fogIndex;
  final float headPower,fogPower;
  Profile(int head,int fog,float power,float fogPower){
   headKelvin=head;fogKelvin=fog;headIndex=index(head);fogIndex=index(fog==0?head:fog);
   headPower=power;this.fogPower=fogPower;
  }
 }
 private VehicleLighting(){}
 static int index(int kelvin){return kelvin<=3000?0:kelvin<=4500?1:2;}
 /** CIE daylight locus >=4000K, Planckian locus below; XYZ -> linear sRGB -> display gamma. */
 static int color(int kelvin){
  double t=Math.max(2000,Math.min(10000,kelvin)),x,y;
  if(t>=4000){
   x=t<=7000?-4.607e9/(t*t*t)+2.9678e6/(t*t)+99.11/t+.244063:
     -2.0064e9/(t*t*t)+1.9018e6/(t*t)+247.48/t+.23704;
   y=-3*x*x+2.87*x-.275;
  }else{
   x=-.2661239e9/(t*t*t)-.234358e6/(t*t)+877.6956/t+.179910;
   y=-.9549476*x*x*x-1.37418593*x*x+2.09137015*x-.16748867;
  }
  double X=x/y,Z=(1-x-y)/y;
  double r=3.2406*X-1.5372-.4986*Z,g=-.9689*X+1.8758+.0415*Z,b=.0557*X-.204+1.057*Z;
  double max=Math.max(r,Math.max(g,b));
  return 0xff000000|channel(r/max)<<16|channel(g/max)<<8|channel(b/max);
 }
 private static int channel(double linear){double v=Math.max(0,Math.min(1,linear));return (int)Math.round(255*(v<=.0031308?12.92*v:1.055*Math.pow(v,1/2.4)-.055));}
 static float blend(double[] time,float day,float sunset,float night){return (float)(time[0]*day+time[1]*sunset+time[2]*night);}
 static int beamAlpha(float depth,double[] time,double fog,float power){return Math.round((18+24*depth)*blend(time,.055f,.72f,1)*(float)(1-.32*fog)*power);}
 static int haloAlpha(float depth,double[] time,double fog,float power){return Math.round((75+55*depth)*blend(time,.3f,.85f,1)*(float)(1+.28*fog)*power);}
 static float beamLength(float width,float depth,boolean fog){return width*(fog?.9f+depth*.35f:1.75f+depth*1.35f);}
 static int core(int color){int r=color>>16&255,g=color>>8&255,b=color&255;return 0xff000000|Math.round(r*.58f+255*.42f)<<16|Math.round(g*.58f+255*.42f)<<8|Math.round(b*.58f+255*.42f);}
}
