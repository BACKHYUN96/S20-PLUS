package com.s20plus.pixeltraffic;

import android.graphics.Bitmap;

/** Small immutable CCT light textures uploaded/reused with the Scene, never rebuilt in a frame. */
final class LightTextures {
 final Bitmap[] glow=new Bitmap[3],trail=new Bitmap[3],beam=new Bitmap[3],fogBeam=new Bitmap[3];
 final int[] colors=new int[3],cores=new int[3];
 LightTextures(){
  for(int i=0;i<3;i++){
   colors[i]=VehicleLighting.color(VehicleLighting.TEMPERATURES[i]);cores[i]=VehicleLighting.core(colors[i]);
   glow[i]=create(colors[i],0);trail[i]=create(colors[i],1);beam[i]=create(colors[i],2);fogBeam[i]=create(colors[i],3);
  }
 }
 private static Bitmap create(int color,int kind){
  int width=64,height=kind==0?64:kind==3?96:192;int[] pixels=new int[width*height];
  for(int y=0;y<height;y++)for(int x=0;x<width;x++){
   double u=(x+.5)/width-.5,v=(y+.5)/height,alpha;
   if(kind==0){double radius=Math.hypot(u*2,(v-.5)*2);alpha=220*Math.pow(Math.max(0,1-radius),2);}
   else if(kind==1){
    double stripe=Math.abs(u)<.05?220:Math.abs(u)<.18?105:Math.abs(u)<.4?35:0;
    alpha=stripe*Math.pow(1-v,1.6)*(y%13<3?.38:y%7==0?.65:1);
   }else if(kind==2){
    // A filled, feathered sector reads as road illumination rather than a thin reflection.
    double spread=.055+.445*v;
    double edge=smooth((1-Math.abs(u)/spread)/.38);
    double radius=Math.hypot(v,u*.45);
    double distance=Math.max(0,1-radius);
    alpha=215*edge*Math.min(1,v/.055)*(.35+.65*Math.pow(distance,.65))*smooth(distance/.22);
   }else{
    double spread=.2+.3*v;
    double edge=Math.max(0,1-Math.abs(u)/spread);
    alpha=215*edge*edge*Math.min(1,v/.085)*Math.pow(1-v,1.9);
   }
   pixels[y*width+x]=((int)Math.round(alpha)<<24)|(color&0x00ffffff);
  }
 Bitmap result=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);result.setPixels(pixels,0,width,0,0,width,height);return result;
 }
 private static double smooth(double value){double t=Math.max(0,Math.min(1,value));return t*t*(3-2*t);}
 void release(){for(Bitmap[] set:new Bitmap[][]{glow,trail,beam,fogBeam})for(Bitmap image:set)if(!image.isRecycled())image.recycle();}
}
