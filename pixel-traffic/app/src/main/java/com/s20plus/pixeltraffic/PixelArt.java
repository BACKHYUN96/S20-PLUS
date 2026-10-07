package com.s20plus.pixeltraffic;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import java.util.Random;

/** Original cached pixel scenery. Fixed geometry and seeds across the three light palettes. */
final class PixelArt {
 private final Paint paint=new Paint();
 private final int theme,road;
 private final Random random=new Random(2048);
 PixelArt(){this(1,0);} PixelArt(int theme){this(theme,0);}
 PixelArt(int theme,int road){this.theme=Math.max(0,Math.min(2,theme));this.road=Math.max(0,Math.min(3,road));}
 int skyColor(){return theme==0?0xff6a99b5:theme==2?0xff111a34:0xff34314e;}
 static float left(float y){return 190-170*(y-260)/540;}
 static float right(float y){return 270+80*(y-260)/540;}
 private static int mix(int a,int b,double t){int out=0xff000000;for(int shift=16;shift>=0;shift-=8)out|=(int)(((a>>shift)&255)*(1-t)+((b>>shift)&255)*t)<<shift;return out;}
 private int tone(int c){if(theme==0)return mix(c,0xffb1bbc1,.29);if(theme==2)return mix(c,0xff111d35,.23);return c;}
 private void raw(Canvas c,int x,int y,int w,int h,int color){if(w<=0||h<=0)return;paint.setColor(color);c.drawRect(x,y,x+w,y+h,paint);}
 private void box(Canvas c,int x,int y,int w,int h,int color){raw(c,x,y,w,h,tone(color));}
 private void poly(Canvas c,int color,float... pts){Path p=new Path();p.moveTo(pts[0],pts[1]);for(int i=2;i<pts.length;i+=2)p.lineTo(pts[i],pts[i+1]);p.close();paint.setColor(tone(color));c.drawPath(p,paint);}
 private void glow(Canvas c,int x,int y,int size,int color){if(theme==0)return;for(int i=3;i>0;i--)raw(c,x-size*i,y-size*i,size*i*2+1,size*i*2+1,(18+(3-i)*15)<<24|color&0xffffff);}
 private void lit(Canvas c,int x,int y,int w,int h,int color){raw(c,x,y,w,h,theme==0?mix(color,0xff617b8d,.55):color);}
 private void sky(Canvas c){
  int top=skyColor(),bottom=theme==0?0xffd6cbb5:theme==2?0xff48536c:0xffffa15d;
  for(int y=0;y<220;y+=2){int color=theme==1?(y<100?mix(0xff393954,0xffa75b79,y/100.0):mix(0xffa75b79,0xffffb268,(y-100)/120.0)):mix(top,bottom,y/220.0);raw(c,0,y,360,2,color);}
  Random clouds=new Random(371);
  for(int row=0;row<12;row++){int y=18+row*16;for(int k=0;k<7;k++){int x=clouds.nextInt(370)-20,w=9+clouds.nextInt(41);int col=theme==0?0xffc7d0d2:theme==2?0xff202c47:row<7?0xff53506c:0xffb55b69;raw(c,x,y,w,2,col);raw(c,x+3,y-2,w-8,2,col);raw(c,x+6,y+2,w-12,1,theme==1?0xffdf866e:col);}}
  if(theme==2){Random stars=new Random(73);for(int i=0;i<40;i++)raw(c,stars.nextInt(360),16+stars.nextInt(148),1,1,0xffacbbca);raw(c,298,45,6,12,0xffe4dfc3);raw(c,295,48,12,6,0xffe4dfc3);raw(c,302,49,3,2,0xffbfc5b9);}
  poly(c,0xff3f405c,0,216,28,184,56,197,86,170,117,199,148,182,185,210,221,172,244,190,288,157,330,179,360,159,360,261,0,261);
  poly(c,0xff262f49,0,230,43,209,71,225,109,190,140,219,183,198,230,223,266,190,318,211,360,182,360,263,0,263);
 }
 private void skyline(Canvas c){
  Random towers=new Random(882);
  for(int i=0;i<38;i++){int x=i*10-5,w=7+towers.nextInt(8),top=199-towers.nextInt(33);box(c,x,top,w,38+199-top,i%3==0?0xff35445b:0xff26364c);box(c,x+1,top+1,w-2,1,0xff667382);for(int y=top+4;y<233;y+=5)for(int wx=x+2;wx<x+w-1;wx+=3){boolean on=towers.nextInt(4)!=0;if(on)lit(c,wx,y,1,2,0xffddb474);}}
  // Distant water and promenade lights beyond the road opening.
  for(int y=233;y<260;y++){box(c,0,y,360,1,(y%4==0)?0xff414962:0xff242f49);}
  for(int x=4;x<357;x+=9){lit(c,x,237,1,1,0xfffac774);if(theme!=0)for(int k=0;k<6;k++)raw(c,x-k%2,241+k*2,1+k%2,1,(70-k*8)<<24|0x00e8aa65);}
 }
 private void asphalt(Canvas c){
  poly(c,0xff4a4d59,176,260,284,260,364,800,-2,800);
  poly(c,0xff222d44,190,260,270,260,350,800,20,800);
  // Long broken puddle bands follow lane perspective, with fine blue surface grain.
  Random wet=new Random(948);
  for(int i=0;i<4400;i++){int y=263+wet.nextInt(537),l=(int)left(y),w=(int)(right(y)-l),x=l+wet.nextInt(Math.max(1,w));box(c,x,y,1+wet.nextInt(3),1,i%7==0?0xff586071:i%3==0?0xff364358:0xff28344b);}
  for(int y=275;y<800;y+=19){float t=(y-260)/540f,width=right(y)-left(y);for(int lane=1;lane<4;lane++){if(lane==2)continue;int x=Math.round(left(y)+width*lane/4);box(c,x,y,Math.max(1,(int)(2*t)),Math.max(3,(int)(10*t)),0xffa5afb7);}}
  poly(c,0xff8a805a,228,260,230,260,185,800,182,800);poly(c,0xffc1a45f,232,260,233,260,189,800,187,800);
  // Narrow curb edges and perspective pavement seams.
  for(int y=265;y<800;y+=5){int l=(int)left(y),r=(int)right(y);box(c,l-5,y,3,5,y%10==0?0xff8b9293:0xff455263);box(c,r+2,y,3,5,y%10==0?0xff8b9293:0xff455263);for(int side=0;side<2;side++){int x=side==0?l-19:r+7;box(c,x,y,12,1,0xff788085);if(y%15==0)box(c,x+6,y,1,4,0xff313e50);}}
  if(road==0){for(int y=640;y<655;y+=4){int l=(int)left(y),r=(int)right(y);for(int x=l+5;x<r-7;x+=12)box(c,x,y,7,3,0xffbac0b9);}for(int lane=0;lane<4;lane++){int y=708,x=(int)(left(y)+(right(y)-left(y))*(lane+.5f)/4);box(c,x,y,2,12,0xff9ca9b3);poly(c,0xff9ca9b3,x-4,y+4,x+1,y-3,x+5,y+4);}}
 }
 private void facade(Canvas c,int x,int base,int w,int h,int style){
  int front=style%3==0?0xff536074:style%3==1?0xff6f6c75:0xff46536a;
  int side=style%3==0?0xff303e55:0xff3d465c;
  box(c,x,base-h,w,h,front);box(c,x+w-10,base-h+5,10,h-5,side);
  box(c,x-1,base-h-3,w+2,4,0xff899097);box(c,x+2,base-h,w-4,2,0xff333f53);
  box(c,x+5,base-h-10,13,7,0xff687c84);box(c,x+7,base-h-10,9,1,0xffb1ada3);
  box(c,x+w-17,base-h-15,1,12,0xffb0a99f);box(c,x+w-20,base-h-12,7,1,0xffb0a99f);
  for(int y=base-h+9;y<base-14;y+=13){box(c,x+1,y+10,w-2,2,0xff364255);for(int wx=x+5;wx<x+w-12;wx+=9){box(c,wx-1,y-1,6,9,0xff27374d);boolean light=random.nextInt(5)>0;if(light){lit(c,wx,y,4,7,style%2==0?0xffffc16a:0xffe1a56e);lit(c,wx,y,4,2,0xffffd99a);box(c,wx+2,y,1,7,0xff756259);}else box(c,wx,y,4,7,0xff40546d);box(c,wx-1,y+7,6,1,0xff8b8680);}box(c,x+w-8,y+2,5,4,0xff607786);box(c,x+w-7,y+2,3,1,0xff98a39f);}
  box(c,x+2,base-12,w-5,12,0xff243349);lit(c,x+5,base-10,w-18,7,0xffdda56c);box(c,x+7,base-10,2,10,0xff586479);
  if(style%2==0){box(c,x-2,base-16,w+2,4,0xff407b7c);for(int k=0;k<w;k+=8)lit(c,x+k,base-15,4,1,0xff87d5c6);}
  box(c,x,base-1,w,2,0xff161f35);
 }
 private void city(Canvas c){
  // Overlapping, varied stepped blocks. Geometric randomness does not depend on time palette.
  for(int row=0;row<9;row++){int y=305+row*62,w=30+row*4,h=44+row*9;int l=(int)left(y)-24;
   facade(c,l-w-20,y-9,w+13,h+20,row+2);facade(c,l-w+7,y,w,h,row);
   int r=(int)right(y)+19;facade(c,r+20,y-14,w+10,h+21,row+4);facade(c,r,y,w,h+5,row+1);
  }
  facade(c,-7,389,48,185,2);facade(c,5,604,54,174,1);
  // Shop corner: panes, illuminated awning, individual shelves and striped fascia.
  box(c,1,582,51,24,0xff1a2c40);lit(c,4,585,45,18,0xffffd47d);for(int i=0;i<6;i++){box(c,5+i*8,585,1,19,0xff385067);for(int j=0;j<3;j++)box(c,7+i*8,588+j*5,4,2,j%2==0?0xff796e51:0xff5c936f);}box(c,0,577,53,3,0xff63b4a3);lit(c,0,580,53,1,0xfff49c6b);
  for(int y=385;y<790;y+=96){int x=(int)left(y)-30;box(c,x-3,y+12,12,3,0xff8b6651);box(c,x-2,y+15,1,4,0xff354555);box(c,x+6,y+15,1,4,0xff354555);int rx=(int)right(y)+23;box(c,rx,y-18,22,11,0xff162c43);lit(c,rx+2,y-16,18,2,0xff57c8c4);lit(c,rx+4,y-11,3,2,0xfff6a87a);lit(c,rx+9,y-11,11,2,0xffd97db2);}
 }
 private void pine(Canvas c,int x,int y,int size){box(c,x-1,y-9,3,10,0xff776347);poly(c,0xff183b36,x-size/2,y-2,x,y-size,x+size/2,y-2);poly(c,0xff416b51,x-size/3,y-8,x,y-size+2,x+size/5,y-8);for(int i=0;i<5;i++)box(c,x-size/4+i*2,y-size+8+i*4,3,1,0xff6b8655);}
 private void rock(Canvas c,int x,int y,int w){poly(c,0xff303d50,x,y,x+w/4,y-w/2,x+w*3/4,y-w*2/3,x+w,y-2);poly(c,0xff71808b,x+1,y-3,x+w/4,y-w/2,x+w*3/4,y-w*2/3,x+w/2,y-5);box(c,x+w/3,y-w/3,3,1,0xffa6ac9d);}
 private void coast(Canvas c){
  for(int y=261;y<800;y++){int edge=Math.max(0,(int)left(y)-20);box(c,0,y,edge,1,y%8==0?0xff46768b:0xff245c73);}
  poly(c,0xffb2a07f,180,260,190,260,11,800,-5,800);
  Random water=new Random(992);for(int i=0;i<700;i++){int y=270+water.nextInt(530),edge=Math.max(1,(int)left(y)-23),x=water.nextInt(edge);box(c,x,y,2+water.nextInt(7),1,i%4==0?0xffa4bab5:0xff398294);}
  for(int y=278;y<800;y+=6){int x=(int)left(y)-24;box(c,x,y,5,1,0xffe2d4b6);box(c,x-4,y+2,4,1,0xff639698);}
  box(c,54,323,37,4,0xff8b8775);rock(c,49,328,44);poly(c,0xffe1cfaa,66,317,69,278,79,278,83,317);box(c,69,286,10,5,0xffb46557);box(c,68,300,13,5,0xffb46557);box(c,67,273,15,6,0xff354858);lit(c,69,274,11,3,0xffffd196);poly(c,0xff535a68,65,273,74,267,84,273);glow(c,73,275,3,0xffffc878);
  for(int y=348;y<800;y+=73){int x=(int)right(y)+20;box(c,x,y-16,32,18,0xffa39d88);poly(c,0xff9c625e,x-3,y-16,x+15,y-32,x+35,y-16);for(int k=0;k<3;k++){lit(c,x+4+k*9,y-11,4,5,0xffeebd7a);box(c,x+4+k*9,y-7,4,1,0xff485468);}box(c,x+14,y-5,4,7,0xff415362);rock(c,x-3,y+10,12);}
 }
 private void mountains(Canvas c){
  poly(c,0xff4b6570,0,256,25,212,68,225,106,185,150,239,185,259,0,259);poly(c,0xff719082,0,254,25,212,39,234,68,225,106,185,114,214,150,239,0,258);
  for(int y=278;y<800;y+=22){int x=(int)left(y)-27;for(int k=0;k<4;k++)pine(c,x-k*17,y,22+random.nextInt(18));rock(c,x-6,y+11,15+random.nextInt(7));int rx=(int)right(y)+23;pine(c,rx+15,y+10,30);rock(c,rx,y+17,14);}
  box(c,106,330,21,14,0xff896b51);poly(c,0xff3b5053,103,316,117,303,130,316);lit(c,111,320,5,5,0xfff1ba71);box(c,121,321,3,9,0xff41515a);
 }
 private void highway(Canvas c){
  for(int y=279;y<800;y+=8){int edge=(int)left(y)-20;box(c,0,y,Math.max(0,edge),2,y%16==0?0xff36584c:0xff657854);for(int x=3;x<edge;x+=13)box(c,x,y-1,2,1,0xffa0a17d);}
  for(int x=10;x<165;x+=26){box(c,x,239,22,19,0xff4f6172);box(c,x+2,235,18,4,0xff93a0a4);box(c,x+17,218,3,21,0xff6c8391);for(int k=0;k<3;k++)lit(c,x+3+k*6,243,3,2,0xffceae74);}
  box(c,4,373,79,6,0xffb9b294);lit(c,4,376,79,2,0xffffc67c);box(c,10,380,2,29,0xff8b9d9f);box(c,75,380,2,29,0xff8b9d9f);box(c,6,410,78,3,0xff778585);
  for(int x=18;x<72;x+=18){box(c,x,398,8,12,0xffb15d5c);box(c,x+1,399,6,4,0xff1b344a);box(c,x+8,403,1,6,0xff243749);}
  box(c,4,419,81,26,0xff39485a);for(int x=9;x<80;x+=13)box(c,x,424,1,16,0xffc0bd9b);
  box(c,89,350,2,48,0xffacaca1);box(c,81,348,20,15,0xff386960);lit(c,84,351,14,2,0xfff1c687);lit(c,86,356,10,2,0xffafd0c4);
  box(c,178,273,2,32,0xff899ba3);box(c,278,273,2,32,0xff899ba3);box(c,178,274,102,2,0xffbdc6bd);box(c,203,278,50,15,0xff326b60);lit(c,210,282,15,2,0xffd5e0b7);lit(c,239,282,2,7,0xffd5e0b7);
  for(int y=470;y<800;y+=95){int x=(int)left(y)-23;box(c,x,y-18,2,23,0xff8b9eaa);box(c,x-7,y-24,16,10,0xff43796a);lit(c,x-4,y-21,10,1,0xffd3d5bb);}
 }
 private void reflections(Canvas c){
  double strength=theme==0?.28:theme==2?1.6:1.9;
  for(int y=306;y<800;y+=51)for(int side=0;side<2;side++){
   float t=(y-260)/540f;int x=(int)(side==0?left(y)+7:right(y)-7),length=15+(int)(27*t);
   for(int k=0;k<length;k+=2){int spread=1+(int)(t*3),alpha=(int)((70-k)*strength);if(alpha<5)continue;raw(c,x-spread+k%3,y+k,2*spread+1,1,Math.min(190,alpha)<<24|0x00ffb64f);if(k%4==0&&theme!=0)raw(c,x,y+k,1,1,0xd0ffdc90);if(k%6==0)raw(c,x-spread*2,y+k+1,spread*4,1,(alpha/2)<<24|0x00ffc27e);}
  }
 }
 Bitmap background(){Bitmap b=Bitmap.createBitmap(360,800,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);sky(c);box(c,0,260,360,540,0xff273e43);if(road<=1)skyline(c);else {for(int y=239;y<260;y++)box(c,0,y,360,1,road==2?0xff304c47:0xff3c4b57);}asphalt(c);if(road==0)city(c);else if(road==1)coast(c);else if(road==2)mountains(c);else highway(c);
  if(road!=0)for(int y=280;y<800;y+=11)for(int side=0;side<2;side++){int x=(int)(side==0?left(y)-7:right(y)+7);box(c,x,y,2,9,0xff788e9b);box(c,x-2,y,6,2,0xffbec4b8);}
  reflections(c);return b;}
 private void canopy(Canvas c,int x,int y,int size){
  box(c,x,y-12,2,14,0xff6f6043);box(c,x+1,y-9,1,7,0xffa59162);
  Random leaves=new Random(x*53L+y*137L);for(int i=0;i<38;i++){int dx=leaves.nextInt(size*2)-size,dy=leaves.nextInt(size*2)-size;int distance=dx*dx+dy*dy;if(distance>size*size)continue;int col=i%5==0?0xff75975a:i%3==0?0xff3f6a46:0xff224e3a;box(c,x+dx,y-12-size+dy,3+leaves.nextInt(3),2+leaves.nextInt(3),col);}box(c,x-size/3,y-12-size*2+4,3,2,0xffa4a064);
 }
 Bitmap foreground(){Bitmap b=Bitmap.createBitmap(360,800,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);
  for(int y=306;y<800;y+=51)for(int side=0;side<2;side++){
   float t=(y-260)/540f;int x=(int)(side==0?left(y)-8:right(y)+9),h=16+(int)(26*t),lampX=x+(side==0?5:-7);
   if(road==0)canopy(c,x+(side==0?-12:14),y+18,8+(int)(9*t));
   else if(road==1&&side==1){box(c,x+12,y-18,2,27,0xff8d7953);for(int k=-2;k<=2;k++){box(c,x+12+k*3,y-20+Math.abs(k),5,2,0xff4d7354);box(c,x+12+k*3,y-18+Math.abs(k),3,2,0xff2b5842);}}
   box(c,x,y-h,1,h,0xff9d9c8a);box(c,x+1,y-h,1,h,0xff384c5d);box(c,Math.min(x,lampX),y-h,Math.abs(x-lampX)+4,1,0xffb0a692);box(c,lampX-1,y-h-1,6,3,0xff405367);glow(c,lampX+1,y-h+1,3,0xffffb65a);lit(c,lampX,y-h,4,2,0xffffdb88);
   if(theme!=0)raw(c,x-6,y+1,12,2,0x25e4a75c);
  }
  return b;}
 Bitmap car(int type,boolean away){return car(type,away,0);}
 Bitmap car(int type,boolean away,int palette){return VehicleArt.create(type,away,palette);}
}
