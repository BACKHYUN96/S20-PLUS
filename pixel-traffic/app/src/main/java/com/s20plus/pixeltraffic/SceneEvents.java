package com.s20plus.pixeltraffic;
import android.graphics.Canvas;
import android.graphics.Paint;

/** Small pixel events remain in background corridors and never modify lane traffic. */
final class SceneEvents {
 private final SceneEventModel model=new SceneEventModel();
 private final Paint paint=new Paint();
 private Canvas canvas;
 void configure(boolean enabled,int frequency){model.configure(enabled,frequency);}
 void update(double seconds){model.update(seconds);}
 private void rect(int x,int y,int width,int height,int color){paint.setColor(color);canvas.drawRect(x,y,x+width,y+height,paint);}
 void draw(Canvas target,int road,double nightAmount) {
  if(!model.visible())return;canvas=target;
  double p=model.progress();boolean night=nightAmount>.4;
  switch(road){case 0:train(p,night);break;case 1:boat(p,night);break;case 2:birds(p,night);break;default:delivery(p,night);break;}
  canvas=null;
 }
 private void train(double progress,boolean night) {
  int x=(int)Math.round(-58+progress*420),y=228;
  // Elevated train: warm windows, linked carriages and understated rail lights.
  for(int car=0;car<3;car++) {
   int offset=x+car*19;rect(offset,y,18,8,0xff485267);rect(offset,y,18,1,0xff9097a4);
   rect(offset,y+6,18,2,0xffa36959);rect(offset+1,y+2,3,3,night?0xffffd37e:0xffc3d1d5);
   rect(offset+6,y+2,4,3,night?0xffffbf69:0xffa8bdc9);rect(offset+12,y+2,4,3,night?0xffffca77:0xffb7c8cc);
   rect(offset+2,y+8,3,1,0xff171f30);rect(offset+12,y+8,3,1,0xff171f30);
   if(car<2)rect(offset+18,y+4,1,2,0xff6a737c);
  }
  rect(x+55,y+3,2,2,night?0xffffe5ad:0xffe5d5a2);
 }
 private void boat(double progress,boolean night) {
  int y=318,limit=(int)PixelArt.left(y+13)-25;
  int x=(int)Math.round(-28+progress*(limit+2));
  // Rightmost extent stays at least25 scene pixels outside the asphalt.
  x=Math.min(x,limit-26);
  rect(x+2,y+8,24,2,0xffb39e86);rect(x+5,y+10,18,2,0xff49414b);rect(x+8,y+12,12,1,0xff263844);
  rect(x+8,y+4,13,4,0xffc1c7bd);rect(x+10,y+5,3,2,0xff385b69);rect(x+15,y+5,4,2,0xff385b69);
  rect(x+14,y+1,1,3,0xffd2c8b0);rect(x+12,y,5,1,0xff71595d);
  if(night){rect(x+20,y+5,2,1,0xffffd28d);rect(x+20,y+14,2,1,0x8873b0b8);}
  for(int i=0;i<3;i++)rect(x-5-i*4,y+12+i%2,3,1,night?0xff608390:0xff9bc1cc);
 }
 private void birds(double progress,boolean night) {
  int x=(int)Math.round(-30+progress*410),phase=(int)(progress*50)%4;
  int color=night?0xff8b9da9:0xff273b4d;
  for(int i=0;i<6;i++) {
   int bx=x-i*6,by=124+Math.abs(i-2)*4;rect(bx,by,1,1,color);
   int rise=phase<2?-1:1;rect(bx-2,by+rise,2,1,color);rect(bx+1,by+rise,2,1,color);
  }
 }
 private void delivery(double progress,boolean night) {
  int y=424,limit=(int)PixelArt.left(y+14)-22;
  int x=(int)Math.round(-23+progress*(limit+1));x=Math.min(x,limit-22);
  // A service-access van, separate from the main-road vehicle simulation.
  rect(x+1,y+3,17,11,0xffcfbfa1);rect(x+2,y+3,14,2,0xffefe0bb);
  rect(x+3,y+6,11,4,0xffab7455);rect(x+5,y+7,7,2,0xffdac394);
  rect(x+16,y+6,6,8,model.variant()%2==0?0xff689fab:0xff789d72);
  rect(x+17,y+7,4,3,0xff233c4e);rect(x+1,y+14,21,2,0xff3e4650);
  rect(x+4,y+13,3,4,0xff172331);rect(x+17,y+13,3,4,0xff172331);
  rect(x+21,y+12,1,2,night?0xffffd68b:0xffd8d3b4);
 }
}
