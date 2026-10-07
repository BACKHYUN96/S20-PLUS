package com.s20plus.pixeltraffic;

import android.graphics.Bitmap;
import android.graphics.Rect;
import java.util.ArrayList;
import java.util.List;

/** Source-local lamp groups and silhouette-preserving cached lighting; never scanned in a frame. */
final class VehicleLights {
 final float[] x=new float[2],y=new float[2],fogX=new float[2],fogY=new float[2];
 final boolean[] measured=new boolean[2],fogMeasured=new boolean[2];
 final Bitmap wash,emission;
 VehicleLights(Bitmap image,Rect source,boolean away){this(image,source,away,0);}
 VehicleLights(Bitmap image,Rect source,boolean away,int type){
  int width=source.width(),height=source.height();
  int[] pixels=new int[width*height];image.getPixels(pixels,0,width,source.left,source.top,width,height);
  int[] labels=new int[pixels.length],queue=new int[pixels.length],colored=away?null:new int[pixels.length];
  VehicleLighting.Profile profile=VehicleLighting.PROFILES[type];
  for(int side=0;side<2;side++){
   int left=(int)(width*(side==0?.06f:.62f)),right=(int)(width*(side==0?.38f:.94f));
   int top=(int)(height*.72f),bottom=(int)(height*.99f);
   List<Group> groups=groups(pixels,labels,queue,width,left,right,top,bottom,away,false,side*pixels.length+1);
   Group main=null,fog=null;
   for(Group group:groups)if(main==null||group.sum>main.sum)main=group;
   if(main!=null&&!away){
    int fogTop=(int)Math.ceil(main.sy/main.sum+height*(type>=4?.045:.085));
    // A separate lower band avoids selecting a disconnected half of the main lamp as fog.
    for(int py=fogTop;py<bottom;py++)for(int px=left;px<right;px++)labels[py*width+px]=0;
    List<Group> lower=groups(pixels,labels,queue,width,left,right,fogTop,bottom,false,true,(side+2)*pixels.length+1);
    for(Group group:lower)if(fog==null||group.sum>fog.sum)fog=group;
   }
   measured[side]=main!=null;
   x[side]=main!=null?(float)(main.sx/main.sum/width-.5):(side==0?-.30f:.30f);
   y[side]=main!=null?(float)(main.sy/main.sum/height-1):-.16f;
   fogMeasured[side]=fog!=null;
   fogX[side]=fog!=null?(float)(fog.sx/fog.sum/width-.5):x[side]*.88f;
   fogY[side]=fog!=null?(float)(fog.sy/fog.sum/height-1):Math.min(-.025f,y[side]+.085f);
   if(!away){
    int head=VehicleLighting.color(profile.headKelvin),lower=VehicleLighting.color(profile.fogKelvin==0?profile.headKelvin:profile.fogKelvin);
    for(int py=top;py<bottom;py++)for(int px=left;px<right;px++){
     int index=py*width+px,label=labels[index];
     if(main!=null&&label==main.id)colored[index]=coloredPixel(pixels[index],head);
     else if(fog!=null&&label==fog.id)colored[index]=coloredPixel(pixels[index],lower);
    }
   }
  }
  if(away)emission=null;
  else {emission=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);emission.setPixels(colored,0,width,0,0,width,height);}
  // Preserve every original alpha value; the street-light wash never leaks outside the body.
  for(int py=0;py<height;py++)for(int px=0;px<width;px++){
   int alpha=pixels[py*width+px]>>>24;
   float across=1-Math.abs(px/(float)width-.5f)*.7f;
   pixels[py*width+px]=((int)(alpha*across)<<24)|0x00ffe0a2;
  }
  wash=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);wash.setPixels(pixels,0,width,0,0,width,height);
 }
 private static List<Group> groups(int[] pixels,int[] labels,int[] queue,int width,int left,int right,int top,int bottom,boolean away,boolean lower,int nextId){
  List<Group> groups=new ArrayList<>();
  for(int py=top;py<bottom;py++)for(int px=left;px<right;px++){
   int root=py*width+px;if(labels[root]!=0||!candidate(pixels[root],away,lower))continue;
   Group group=new Group(nextId++);int start=0,end=0;queue[end++]=root;labels[root]=group.id;
   while(start<end){
    int index=queue[start++],gx=index%width,gy=index/width,pixel=pixels[index];
    double weight=away?(pixel>>16&255)-(pixel>>8&255):((pixel>>8&255)+(pixel&255))*.5;
    group.sum+=weight;group.sx+=(gx+.5)*weight;group.sy+=(gy+.5)*weight;
    for(int direction=0;direction<4;direction++){
     int nx=gx+(direction==0?-1:direction==1?1:0),ny=gy+(direction==2?-1:direction==3?1:0);
     if(nx<left||nx>=right||ny<top||ny>=bottom)continue;
     int next=ny*width+nx;if(labels[next]==0&&candidate(pixels[next],away,lower)){labels[next]=group.id;queue[end++]=next;}
    }
   }
   groups.add(group);
  }
  return groups;
 }
 private static boolean candidate(int pixel,boolean away,boolean lower){
  if(!lower)return lamp(pixel,away);
  int r=pixel>>16&255,g=pixel>>8&255,b=pixel&255;
  return (pixel>>>24)>=200&&r>195&&g>140&&b>g*.45f&&r>=g&&r>b*1.12f;
 }
 private static boolean lamp(int pixel,boolean away){
  int r=pixel>>16&255,g=pixel>>8&255,b=pixel&255;
  return (pixel>>>24)>=200&&(away?r>150&&r>g*1.65f&&r>b*1.65f:r>205&&g>165&&b>100&&r>b*1.12f&&g>b*1.06f);
 }
 private static int coloredPixel(int source,int color){
  float luminance=((source>>16&255)*.2126f+(source>>8&255)*.7152f+(source&255)*.0722f)/255;
  float gain=.72f+.28f*luminance;
  return source&0xff000000|Math.round((color>>16&255)*gain)<<16|Math.round((color>>8&255)*gain)<<8|Math.round((color&255)*gain);
 }
 private static final class Group {final int id;double sum,sx,sy;Group(int id){this.id=id;}}
 void release(){if(!wash.isRecycled())wash.recycle();if(emission!=null&&!emission.isRecycled())emission.recycle();}
}
