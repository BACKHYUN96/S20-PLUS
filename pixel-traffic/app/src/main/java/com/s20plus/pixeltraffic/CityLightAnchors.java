package com.s20plus.pixeltraffic;

import android.graphics.Bitmap;

/** A few authored bright window/sign positions, selected once; uniform test/fallback art stays empty. */
final class CityLightAnchors {
 final float[] x=new float[8],y=new float[8];
 private final double[] scores=new double[8];
 int count;
 CityLightAnchors(Bitmap image){
  if(image==null||image.isRecycled())return;
  int width=image.getWidth(),height=image.getHeight();int[] pixels=new int[width*height];
  image.getPixels(pixels,0,width,0,0,width,height);
  boolean varied=false;for(int i=0;i<pixels.length;i+=Math.max(1,pixels.length/41))if(pixels[i]!=pixels[0]){varied=true;break;}if(!varied)return;
  int cellW=Math.max(5,width/27),cellH=Math.max(5,height/50);
  for(int top=height*180/1200;top<height*710/1200;top+=cellH)for(int left=0;left<width;left+=cellW){
   int bottom=Math.min(height,top+cellH),right=Math.min(width,left+cellW),n=0;double sx=0,sy=0,score=0;
   for(int py=top;py<bottom;py+=2)for(int px=left;px<right;px+=2){
    int p=pixels[py*width+px],r=p>>16&255,g=p>>8&255,b=p&255;
    float worldX=(px+.5f)*540/width,worldY=(py+.5f)*1200/height;
    boolean building=worldY<390?worldX<230||worldX>505:worldX<CinematicGeometry.left(worldY)-45||worldX>CinematicGeometry.right(worldY)+35;
    if(!building||r<225||g<160||r<g*1.08||g<b*1.13)continue;
    double weight=g+b*.4;score+=weight;sx+=worldX*weight;sy+=worldY*weight;n++;
   }
   double fraction=n/(double)(Math.max(1,(bottom-top+1)/2)*Math.max(1,(right-left+1)/2));
   if(n<3||fraction>.28)continue;float ax=(float)(sx/score),ay=(float)(sy/score);
   boolean close=false;for(int i=0;i<count;i++)if(Math.hypot(x[i]-ax,y[i]-ay)<38){close=true;break;}if(close)continue;
   int slot=count<8?count++:0;
   if(count==8){for(int i=1;i<8;i++)if(scores[i]<scores[slot])slot=i;if(score<=scores[slot])continue;}
   x[slot]=ax;y[slot]=ay;scores[slot]=score;
  }
 }
}
