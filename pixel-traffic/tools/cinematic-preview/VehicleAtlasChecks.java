package com.s20plus.pixeltraffic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import java.nio.file.Path;

/** Desktop checks of complete source bodies, lane-authoritative width and ground anchoring. */
public final class VehicleAtlasChecks {
 private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
 private static void near(float actual,float expected,float tolerance,String label){
  require(Math.abs(actual-expected)<=tolerance,label+": "+actual+" != "+expected);
 }
 private static int pixel(Bitmap bitmap,int x,int y){
  int[] result=new int[1];bitmap.getPixels(result,0,1,x,y,1,1);return result[0];
 }
 private static int color(int index){return 0xff000000|((40+index*15)<<16)|((50+index*11)<<8)|(70+index*7);}
 private static Bitmap fixture(int omitted){
  // The second body straddles the old height/3 boundary. A uniform-cell crop must fail this fixture.
  Bitmap sheet=Bitmap.createBitmap(64,84,Bitmap.Config.ARGB_8888);
  Canvas canvas=new Canvas(sheet);Paint paint=new Paint();int[] rows={4,25,57};
  for(int index=0;index<12;index++){
   if(index==omitted)continue;
   int x=index%4*16+3+index%3,y=rows[index/4]+index%2;
   paint.setColor(color(index));canvas.drawRect(x,y,x+4,y+8,paint);
  }
  return sheet;
 }
 private static int[] alphaBounds(Bitmap target){
  int width=target.getWidth(),height=target.getHeight(),left=width,top=height,right=-1,bottom=-1;
  int[] row=new int[width];
  for(int y=0;y<height;y++){
   target.getPixels(row,0,width,0,y,width,1);
   for(int x=0;x<width;x++)if((row[x]>>>24)>8){left=Math.min(left,x);right=Math.max(right,x);top=Math.min(top,y);bottom=Math.max(bottom,y);}
  }
  return new int[]{left,top,right+1,bottom+1};
 }
 public static void main(String[] args){
  Bitmap sheet=fixture(-1);CinematicVehicles vehicles=new CinematicVehicles(sheet);
  require(vehicles.available(),"Valid transparent, unevenly spaced three-row sheet was rejected");
  Paint paint=new Paint();paint.setFilterBitmap(false);
  for(int type=0;type<6;type++)for(int direction=0;direction<2;direction++){
   Bitmap target=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);
   float length=vehicles.bodyLength(type,direction==1,24);
   near(length,48,.001f,"Whole body aspect at authoritative width");
   vehicles.draw(new Canvas(target),paint,type,direction==1,64,64-length/2,24);
   require(pixel(target,64,40)==color(type*2+direction),"Wrong type/direction cell");
   int[] box=alphaBounds(target);
   require(box[0]==52&&box[1]==16&&box[2]==76&&box[3]==64,"Body must fill width and end at ground y=64");
   for(int y=16;y<64;y++)for(int x=52;x<76;x++)
    require(pixel(target,x,y)==color(type*2+direction),"Neighboring vehicle leaked into source "+type+"/"+direction);
   target.recycle();
  }
  Bitmap ignoredTarget=Bitmap.createBitmap(16,16,Bitmap.Config.ARGB_8888);Canvas ignoredCanvas=new Canvas(ignoredTarget);
  vehicles.draw(ignoredCanvas,paint,-1,false,8,8,8);
  vehicles.draw(ignoredCanvas,paint,0,false,8,8,Float.NaN);
  vehicles.draw(ignoredCanvas,paint,0,false,8,8,0);
  require(pixel(ignoredTarget,8,8)==0,"Invalid draw parameters should do nothing");
  require(vehicles.bodyLength(-1,false,24)==0&&vehicles.bodyLength(0,false,Float.NaN)==0,"Invalid dimensions must return zero");
  vehicles.release();vehicles.release();
  require(!vehicles.available()&&sheet.isRecycled(),"Release must recycle owned sheet and be idempotent");
  require(vehicles.bodyLength(0,false,24)==0,"Released atlas must report no length");
  vehicles.draw(ignoredCanvas,paint,0,false,8,8,8);
  require(pixel(ignoredTarget,8,8)==0,"Released atlas should not draw");
  require(!new CinematicVehicles((Bitmap)null).available(),"Missing atlas should fall back");
  Bitmap badDimensions=Bitmap.createBitmap(63,72,Bitmap.Config.ARGB_8888);
  require(!new CinematicVehicles(badDimensions).available()&&badDimensions.isRecycled(),"Unequal columns should be rejected");
  Bitmap emptyCell=fixture(11);
  require(!new CinematicVehicles(emptyCell).available()&&emptyCell.isRecycled(),"Missing body row should be rejected");
  Bitmap opaque=Bitmap.createBitmap(64,72,Bitmap.Config.ARGB_8888);
  Paint solid=new Paint();solid.setColor(0xff667788);new Canvas(opaque).drawRect(0,0,64,72,solid);
  require(!new CinematicVehicles(opaque).available()&&opaque.isRecycled(),"Opaque sheet should be rejected");
  ignoredTarget.recycle();
  if(args.length>0)verifyActualAtlas(Path.of(args[0]),paint);
  System.out.println("PASS: complete transparent vehicle rows, type/direction, full width/aspect, ground anchor, invalid fallback, owned release"+(args.length>0?", and all twelve actual artwork dimensions":""));
 }
 private static void verifyActualAtlas(Path assets,Paint paint){
  // Independent alpha>8 bounding-box measurements from the shipped PNG, not loader-produced values.
  int[][] measured={{173,254},{183,249},{174,238},{174,237},{203,309},{190,282},
   {191,280},{182,279},{216,374},{188,375},{235,372},{201,365}};
  CinematicVehicles vehicles=new CinematicVehicles(new Context(assets));
  try{
   require(vehicles.available(),"Shipped atlas must load");
   for(int index=0;index<12;index++){
    float width=80,ground=224,length=vehicles.bodyLength(index/2,index%2==1,width);
    near(length,width*measured[index][1]/measured[index][0],.001f,"Measured complete native body "+index);
    Bitmap target=Bitmap.createBitmap(256,256,Bitmap.Config.ARGB_8888);
    try{
     vehicles.draw(new Canvas(target),paint,index/2,index%2==1,128,ground-length/2,width);
     int[] box=alphaBounds(target);
     near(box[2]-box[0],width,2,"Actual sprite fills intended lane width "+index);
     near(box[3],ground,1,"Actual sprite ends at road ground point "+index);
     near(box[3]-box[1],length,2,"Actual sprite length retains full body "+index);
    }finally{target.recycle();}
   }
  }finally{vehicles.release();}
 }
}
