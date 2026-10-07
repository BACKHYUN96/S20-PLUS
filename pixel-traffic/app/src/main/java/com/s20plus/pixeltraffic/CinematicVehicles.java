package com.s20plus.pixeltraffic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import java.io.IOException;
import java.io.InputStream;

/** A single cached transparent sheet, with all twelve source bounds measured once. */
final class CinematicVehicles {
 private static final int COLUMNS=4, ROWS=3, TYPES=6;
 private static final int VISIBLE_ALPHA=8;
 private final Rect[] sources=new Rect[TYPES*2];
 private final VehicleLights[] lights=new VehicleLights[TYPES*2];
 private final RectF destination=new RectF();
 private Bitmap atlas;
 private boolean released;

 CinematicVehicles(Context context){this(load(context));}
 /** Takes ownership; invalid sheets and released sheets are recycled exactly once. */
 CinematicVehicles(Bitmap sheet){
  atlas=sheet;
  if(!measureSources()){
   if(atlas!=null&&!atlas.isRecycled())atlas.recycle();
   atlas=null;
  }
  if(atlas!=null)for(int i=0;i<lights.length;i++)lights[i]=new VehicleLights(atlas,sources[i],i%2==1,i/2);
 }
 private static Bitmap load(Context context){
  BitmapFactory.Options options=new BitmapFactory.Options();
  options.inScaled=false;options.inPreferredConfig=Bitmap.Config.ARGB_8888;
  try(InputStream input=context.getAssets().open("cinematic-vehicles.png")){
   return BitmapFactory.decodeStream(input,null,options);
  }catch(IOException ignored){return null;}
 }
 private boolean measureSources(){
  if(atlas==null||atlas.isRecycled()||!atlas.hasAlpha())return false;
  int width=atlas.getWidth(),height=atlas.getHeight();
  if(width<COLUMNS||height<ROWS||width%COLUMNS!=0)return false;
  int cellWidth=width/COLUMNS;
  int[] row=new int[cellWidth];
  // The authored sheet has four columns, but its three rows are not evenly spaced.
  // A height/3 crop cut SUV roofs into sedans and bus roofs into SUVs. Separate
  // vehicles at their transparent horizontal gaps, then trim each complete body.
  int toleratedGap=Math.max(1,height/200);
  for(int column=0;column<COLUMNS;column++){
   int cellX=column*cellWidth,found=0;
   int left=cellWidth,top=-1,right=-1,bottom=-1,gap=0;
   boolean transparent=false;
   for(int y=0;y<height;y++){
    atlas.getPixels(row,0,cellWidth,cellX,y,cellWidth,1);
    int rowLeft=cellWidth,rowRight=-1;
    for(int x=0;x<cellWidth;x++){
     int alpha=row[x]>>>24;
     if(alpha==0)transparent=true;
     // Disregard nearly invisible export fringes when establishing a vehicle's size.
     if(alpha>VISIBLE_ALPHA){rowLeft=Math.min(rowLeft,x);rowRight=Math.max(rowRight,x);}
    }
    if(rowRight>=rowLeft){
     if(top<0)top=y;
     left=Math.min(left,rowLeft);right=Math.max(right,rowRight);bottom=y+1;gap=0;
    }else if(top>=0&&++gap>toleratedGap){
     if(found>=ROWS)return false;
     sources[found*COLUMNS+column]=new Rect(cellX+left,top,cellX+right+1,bottom);
     found++;left=cellWidth;top=-1;right=-1;bottom=-1;gap=0;
    }
   }
   if(top>=0){
    if(found>=ROWS)return false;
    sources[found*COLUMNS+column]=new Rect(cellX+left,top,cellX+right+1,bottom);found++;
   }
   // Opaque sheets and missing/extra rows cannot represent six isolated types.
   if(!transparent||found!=ROWS)return false;
  }
  return true;
 }
 boolean available(){return !released&&atlas!=null;}
 VehicleLights lights(int type,boolean away){return lights[type*2+(away?1:0)];}
 /** The complete body's height at a lane-calibrated width, including its authored perspective. */
 float bodyLength(int type,boolean away,float bodyWidth){
  if(!available()||type<0||type>=TYPES||!Float.isFinite(bodyWidth)||bodyWidth<=0)return 0;
  Rect source=sources[type*2+(away?1:0)];
  return bodyWidth*source.height()/source.width();
 }
 /** Uses width as the authority, preserving aspect ratio and the complete-body center. */
 void draw(Canvas canvas,Paint paint,int type,boolean away,float centerX,float centerY,
   float bodyWidth){
  if(!available()||type<0||type>=TYPES||!Float.isFinite(bodyWidth)
    ||!Float.isFinite(centerX)||!Float.isFinite(centerY)||bodyWidth<=0)return;
  Rect source=sources[type*2+(away?1:0)];
  float height=bodyLength(type,away,bodyWidth);
  destination.set(centerX-bodyWidth/2,centerY-height/2,centerX+bodyWidth/2,centerY+height/2);
  canvas.drawBitmap(atlas,source,destination,paint);
 }
 void release(){
  if(released)return;
  released=true;
  for(VehicleLights light:lights)if(light!=null)light.release();
  if(atlas!=null&&!atlas.isRecycled())atlas.recycle();
  atlas=null;
 }
}
