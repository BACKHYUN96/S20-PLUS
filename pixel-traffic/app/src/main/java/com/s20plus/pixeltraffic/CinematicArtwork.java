package com.s20plus.pixeltraffic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.IOException;
import java.io.InputStream;

/** Scene-owned lazy cache: wet, dry, snow, fog × three times. Decode only on selection. */
final class CinematicArtwork {
 final Bitmap[][] images=new Bitmap[4][3];
 private final boolean[][] attempted=new boolean[4][3];
 private final Context context;
 private final int width,height;
 private boolean released;
 int decodeAttempts;
 CinematicArtwork(Context context){this(context,loadBase(context));}
 CinematicArtwork(Bitmap image){this(null,image);}
 private CinematicArtwork(Context context,Bitmap image){
  this.context=context;images[0][1]=image;attempted[0][1]=true;
  width=image==null?0:image.getWidth();height=image==null?0:image.getHeight();
 }
 private static Bitmap loadBase(Context context){
  for(String name:new String[]{"cinematic-city.png","cinematic-city.webp"}){
   try(InputStream input=context.getAssets().open(name)){
    Bitmap image=BitmapFactory.decodeStream(input,null,options());
    if(image!=null&&image.getWidth()>0&&image.getHeight()>0)return image;
    if(image!=null)image.recycle();
   }catch(IOException|OutOfMemoryError ignored){/* Scene availability selects classic fallback. */}
  }
  return null;
 }
 private static BitmapFactory.Options options(){
  BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;options.inPreferredConfig=Bitmap.Config.ARGB_8888;return options;
 }
 static String name(int kind,int theme){
  return "cinematic-city"+(theme==0?"-day":theme==2?"-night":"")+(kind==0?"":kind==1?"-dry":kind==2?"-snow":"-fog")+".png";
 }
 Bitmap load(int kind,int theme){
  if(released||attempted[kind][theme])return images[kind][theme];
  attempted[kind][theme]=true;
  if(context==null||width==0)return null;
  decodeAttempts++;
  try(InputStream input=context.getAssets().open(name(kind,theme))){
   Bitmap image=BitmapFactory.decodeStream(input,null,options());
   int tolerance=kind==0?0:1;
   if(image!=null&&Math.abs(image.getWidth()-width)<=tolerance&&Math.abs(image.getHeight()-height)<=tolerance){images[kind][theme]=image;return image;}
   if(image!=null)image.recycle();
  }catch(IOException|OutOfMemoryError ignored){/* Optional art uses the loaded normal scene. */}
  return null;
 }
 void keepOnly(boolean[][] required){
  if(released)return;
  for(int kind=0;kind<4;kind++)for(int theme=0;theme<3;theme++){
   if(kind==0&&theme==1)continue; // Small fixed fallback guarantee; never decode in draw/update.
   Bitmap image=images[kind][theme];
   if(!required[kind][theme]&&image!=null){image.recycle();images[kind][theme]=null;attempted[kind][theme]=false;}
  }
 }
 int count(){int count=0;for(Bitmap[] row:images)for(Bitmap image:row)if(image!=null)count++;return count;}
 long pixelBytes(){long bytes=0;for(Bitmap[] row:images)for(Bitmap image:row)if(image!=null)bytes+=(long)image.getWidth()*image.getHeight()*4;return bytes;}
 void release(){
  if(released)return;released=true;
  for(Bitmap[] row:images)for(int i=0;i<3;i++){if(row[i]!=null)row[i].recycle();row[i]=null;}
 }
}
