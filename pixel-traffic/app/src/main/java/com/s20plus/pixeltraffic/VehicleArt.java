package com.s20plus.pixeltraffic;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/** Original, cached 40 x 80 pixel vehicles. Lighting always comes from the upper left. */
final class VehicleArt {
 private static final int INK=0xff101927, TIRE=0xff080e17;
 private static final int GLASS=0xff152b3e, GLASS_MID=0xff35576f, GLASS_LIGHT=0xff789ca9;
 private static final int[][] COLORS={
  {0xffb9c5ce,0xff263f65,0xff365e50,0xffd6a333,0xff255e99,0xff71919f},
  {0xffaf4557,0xff6651a0,0xff337d8f,0xffe1af37,0xff2b807b,0xffb3684a},
  {0xffe1d7c8,0xff8eaab9,0xff97ad94,0xffe3c786,0xff8bb1bf,0xffb5a5a5}
 };
 private final Paint paint=new Paint();
 private final Canvas canvas;
 private final int color;
 private VehicleArt(Bitmap bitmap,int color) {canvas=new Canvas(bitmap);this.color=color;}
 static Bitmap create(int type,boolean away,int palette) {
  int safeType=Math.max(0,Math.min(5,type)), safePalette=Math.max(0,Math.min(2,palette));
  Bitmap bitmap=Bitmap.createBitmap(40,80,Bitmap.Config.ARGB_8888);
  VehicleArt art=new VehicleArt(bitmap,COLORS[safePalette][safeType]);
  if(safeType==4)art.bus(away);
  else if(safeType==5)art.truck(away);
  else art.car(safeType,away);
  return bitmap;
 }
 private void box(int x,int y,int w,int h,int value) {
  paint.setColor(value);canvas.drawRect(x,y,x+w,y+h,paint);
 }
 private void poly(int value,int... points) {
  Path path=new Path();path.moveTo(points[0],points[1]);
  for(int i=2;i<points.length;i+=2)path.lineTo(points[i],points[i+1]);
  path.close();paint.setColor(value);canvas.drawPath(path,paint);
 }
 private static int blend(int a,int b,int weight) {
  int r=(((a>>16)&255)*(100-weight)+((b>>16)&255)*weight)/100;
  int g=(((a>>8)&255)*(100-weight)+((b>>8)&255)*weight)/100;
  int bl=((a&255)*(100-weight)+(b&255)*weight)/100;
  return 0xff000000|r<<16|g<<8|bl;
 }
 private int light(int amount) {return blend(color,0xffe5edf0,amount);}
 private int dark(int amount) {return blend(color,0xff0c1728,amount);}
 private void wheel(int x,int y,int width,int height) {
  box(x,y+1,width,height-2,TIRE);box(x+1,y,width-2,height,TIRE);
  box(x+1,y+2,1,height-4,0xff586773);
  box(x+1,y+3,1,2,0xff909c9b);
 }
 private void pane(int x,int y,int width,int height) {
  // Stepped glass edge and two unequal reflections make the tiny pane read as a surface.
  box(x+1,y,width-2,height,GLASS);box(x,y+1,width,height-2,GLASS);
  box(x+2,y+1,width-4,2,GLASS_MID);box(x+3,y+1,Math.max(1,width/3),1,GLASS_LIGHT);
  box(x+2,y+2,2,Math.max(1,height-4),0xff4d7287);
  box(x+width-4,y+height-2,2,1,0xff253e55);
 }
 private void mirror(int x,int y,boolean right) {
  box(x,y+1,4,2,INK);box(x+(right?0:1),y,3,2,right?dark(15):light(22));
  box(x+1,y,2,1,right?dark(34):light(45));
 }
 private void lamps(int x,int y,int width,boolean away) {
  int lamp=away?0xffd12f40:0xffffd88d,core=away?0xfffc785b:0xfffff4ce;
  box(x,y,5,3,dark(55));box(x+width-5,y,5,3,dark(55));
  box(x,y,5,2,lamp);box(x+width-5,y,5,2,lamp);
  box(x+1,y,3,1,core);box(x+width-4,y,3,1,core);
  if(!away) {box(x,y+3,2,1,0xffbf9958);box(x+width-2,y+3,2,1,0xffbf9958);}
 }
 private void car(int type,boolean away) {
  boolean suv=type==2,coupe=type==1;
  int y=suv?18:20,h=suv?44:40,x=suv?6:8,w=suv?28:24;
  int bottom=y+h;
  wheel(x-2,y+8,4,8);wheel(x+w-2,bottom-13,4,9);
  wheel(x+w-2,y+8,4,8);wheel(x-2,bottom-13,4,9);
  // Narrow distant end, broad near end, dark outer trim and softened pixel corners.
  poly(INK,x+5,y,x+w-6,y,x+w-3,y+2,x+w-1,y+6,x+w,y+h-8,
   x+w-2,bottom-3,x+w-5,bottom,x+5,bottom,x+2,bottom-2,x,bottom-7,x+1,y+5,x+3,y+2);
  poly(color,x+5,y+1,x+w-6,y+1,x+w-3,y+4,x+w-2,bottom-8,
   x+w-4,bottom-3,x+5,bottom-2,x+2,bottom-6,x+2,y+6);
  poly(light(28),x+5,y+2,x+8,y+2,x+5,bottom-8,x+3,bottom-7,x+3,y+7);
  poly(dark(35),x+w-5,y+3,x+w-3,y+5,x+w-2,bottom-8,x+w-4,bottom-4,x+w-6,bottom-5);
  box(x+6,y+2,w-13,1,light(44));box(x+5,bottom-3,w-10,1,dark(45));
  int frontGlass=away?y+9:y+20,rearGlass=away?y+25:y+6;
  if(suv) {frontGlass=away?y+10:y+24;rearGlass=away?y+31:y+5;}
  if(coupe) {frontGlass=away?y+11:y+21;rearGlass=away?y+28:y+7;}
  int roofTop=away?frontGlass+7:rearGlass+5;
  int roofBottom=away?rearGlass:frontGlass;
  // Side windows are darker than the top glass; pillar lines separate front and rear doors.
  box(x+3,roofTop-1,2,roofBottom-roofTop+6,GLASS);
  box(x+w-5,roofTop,2,roofBottom-roofTop+5,GLASS);
  box(x+3,roofTop+4,2,1,light(20));box(x+w-5,roofTop+4,2,1,dark(34));
  poly(light(coupe?10:22),x+7,roofTop,x+w-8,roofTop,x+w-6,roofBottom,x+5,roofBottom);
  box(x+7,roofTop+1,w-16,1,light(47));box(x+w-7,roofTop+2,1,Math.max(1,roofBottom-roofTop-3),dark(24));
  pane(x+5,frontGlass,w-10,suv?7:6);
  pane(x+6,rearGlass,w-12,coupe?4:5);
  // Windscreen wipers and inset glazing border, scaled to one pixel.
  box(x+7,frontGlass+(suv?6:5),4,1,INK);box(x+w-12,frontGlass+(suv?6:5),4,1,INK);
  int bonnet=away?y+4:frontGlass+7;
  box(x+6,bonnet,w-12,1,light(38));box(x+6,bonnet+2,1,3,light(35));
  box(x+w-7,bonnet+2,1,3,dark(27));
  int mirrorY=away?frontGlass+5:frontGlass;
  mirror(x-3,mirrorY,false);mirror(x+w-1,mirrorY,true);
  // Door handles and a subtle shoulder line connect roof and fenders.
  box(x+3,roofTop+2,2,1,0xffbdc7c5);box(x+w-5,roofTop+2,2,1,0xff687986);
  box(x+2,bottom-12,2,1,light(30));box(x+w-4,bottom-12,2,1,dark(45));
  if(suv) {
   box(x+5,roofTop-1,1,roofBottom-roofTop+1,0xffbdc5be);
   box(x+w-7,roofTop-1,1,roofBottom-roofTop+1,0xff72847f);
   box(x+4,bottom-8,w-8,2,dark(40));
   if(away) {box(17,bottom-10,6,5,INK);box(18,bottom-10,4,1,0xff56675d);}
  } else if(coupe) {
   box(x+8,roofTop+2,w-17,Math.max(1,roofBottom-roofTop-3),dark(45));
   box(18,bonnet+1,1,3,light(49));box(21,bonnet+1,1,3,light(49));
   if(away) {box(x+3,bottom-9,w-6,2,INK);box(x+5,bottom-10,w-10,1,light(20));}
  } else if(type==3) {
   // Original taxi livery: roof sign and small checker fragments on both doors.
   box(16,roofTop+2,8,4,INK);box(17,roofTop+1,6,3,0xffffe0a2);
   box(18,roofTop+2,1,1,0xff7d5d2c);box(20,roofTop+2,2,1,0xff7d5d2c);
   for(int yy=roofTop+6;yy<roofBottom+2;yy+=3) {
    box(x+3,yy,2,1,INK);box(x+w-5,yy+1,2,1,INK);
   }
  }
  int bumperY=bottom-6;
  box(x+5,bumperY,w-10,3,away?dark(32):INK);
  if(away) {
   box(x+7,bumperY,w-14,1,light(24));box(x+5,bottom-3,w-10,1,0xff55636e);
   box(x+4,bottom-2,2,1,INK);box(x+w-6,bottom-2,2,1,INK);
  } else {
   box(x+8,bumperY,w-16,1,0xff738793);box(x+9,bumperY+1,w-18,1,0xff273744);
   box(x+5,bottom-3,w-10,1,dark(33));
  }
  lamps(x+3,bottom-8,w-6,away);
  box(18,bottom-4,4,2,0xffd9d6be);box(19,bottom-4,2,1,0xff727c77);
 }
 private void bus(boolean away) {
  int y=4;
  wheel(3,17,4,10);wheel(33,17,4,10);wheel(3,57,4,12);wheel(33,57,4,12);
  poly(INK,10,y,29,y,33,y+3,35,16,35,69,32,75,8,75,5,70,5,16,7,y+3);
  poly(color,10,5,29,5,32,9,33,17,33,68,30,73,9,73,7,69,7,16,8,9);
  box(8,12,2,55,light(30));box(31,14,2,55,dark(38));
  // Side glazing bands separated by structural pillars, with individual street reflections.
  for(int yy=19;yy<53;yy+=8) {
   box(7,yy,3,6,GLASS);box(8,yy,1,3,GLASS_LIGHT);
   box(30,yy,3,6,GLASS);box(31,yy+1,1,3,GLASS_MID);
  }
  box(11,16,18,38,light(20));box(11,16,2,38,light(39));box(27,17,2,37,dark(18));
  box(13,20,13,13,0xff8b9a9e);box(14,20,11,2,0xffc0cbcb);
  for(int xx=15;xx<25;xx+=3)box(xx,24,1,5,0xff566a76);
  box(14,37,11,10,0xff7c9198);box(15,37,9,2,0xffafbdba);
  for(int yy=40;yy<45;yy+=2)box(16,yy,7,1,0xff4f6775);
  box(12,50,15,2,light(39));
  if(away) {
   pane(10,9,20,7);box(13,7,14,2,0xffe9cc87);
   pane(10,56,20,8);box(12,66,16,2,dark(38));
   for(int xx=14;xx<27;xx+=3)box(xx,66,1,2,0xff1e3948);
   box(9,70,22,2,dark(50));
  } else {
   pane(11,9,18,6);
   box(11,54,18,4,INK);box(13,55,14,2,0xffdbbc79);
   // Destination lettering is abstract pixels rather than branding.
   for(int xx=14;xx<26;xx+=4)box(xx,55,2,1,0xff6b5d36);
   pane(9,59,22,8);
   box(10,65,5,1,INK);box(23,65,5,1,INK);box(11,65,1,1,0xffc0bcb2);
   box(13,69,14,3,INK);box(15,70,10,1,0xff557588);
  }
  mirror(3,away?13:59,false);mirror(33,away?13:59,true);
  lamps(8,69,24,away);box(18,73,5,1,0xffd4d0b7);
  box(10,72,7,1,light(22));box(24,72,7,1,dark(35));
 }
 private void truck(boolean away) {
  wheel(3,23,4,9);wheel(33,23,4,9);wheel(3,53,4,10);wheel(33,53,4,10);
  int cargoTop=away?30:10,cargoBottom=away?67:49;
  box(6,cargoTop+2,28,cargoBottom-cargoTop-2,INK);
  box(8,cargoTop,24,cargoBottom-cargoTop,0xffc0c9c6);
  box(7,cargoTop+2,3,cargoBottom-cargoTop-4,0xffdce1d5);
  box(30,cargoTop+2,3,cargoBottom-cargoTop-3,0xff687d86);
  box(10,cargoTop+2,19,cargoBottom-cargoTop-5,0xffcdd4cc);
  for(int yy=cargoTop+6;yy<cargoBottom-2;yy+=5) {
   box(10,yy,20,1,0xff9faaab);box(11,yy+1,17,1,0xffe2e4d8);
  }
  box(10,cargoTop+1,19,1,0xffedf0de);box(28,cargoTop+3,2,cargoBottom-cargoTop-5,0xffb2bfc0);
  if(away) {
   poly(INK,11,10,28,10,32,14,33,27,30,33,9,33,7,27,8,14);
   poly(color,12,11,27,11,30,14,31,26,28,31,11,31,9,26,10,15);
   pane(11,16,18,7);box(12,12,15,1,light(44));
   box(12,24,16,5,light(19));box(13,25,12,1,light(44));
   mirror(6,20,false);mirror(30,20,true);
   // Two rear doors, latches and a narrow red reflector rail.
   box(19,32,1,32,0xff748992);box(20,33,1,31,0xffe8e8d9);
   box(13,36,1,26,0xff7d8d8f);box(26,36,1,26,0xff7d8d8f);
   box(12,55,3,2,0xff617a82);box(25,55,3,2,0xff617a82);
   box(8,66,24,3,INK);box(10,66,20,1,0xffa84243);
   lamps(8,66,24,true);box(18,69,5,1,0xffd6d3b9);
  } else {
   // The visible cab sits below the long cargo roof; cab width grows toward the viewer.
   poly(INK,10,48,29,48,33,53,34,64,31,70,8,70,6,65,7,54);
   poly(color,11,49,28,49,31,53,32,64,29,68,10,68,8,64,9,54);
   box(10,51,2,13,light(37));box(29,53,2,11,dark(32));
   pane(10,53,20,8);box(11,59,5,1,INK);box(24,59,5,1,INK);
   box(11,62,17,1,light(35));box(13,64,14,3,INK);box(15,64,10,1,0xff74919c);
   for(int xx=15;xx<26;xx+=3)box(xx,65,1,1,0xff5d7582);
   mirror(4,55,false);mirror(32,55,true);
   lamps(8,65,24,false);box(18,69,5,1,0xffdedbc6);
  }
 }
}
