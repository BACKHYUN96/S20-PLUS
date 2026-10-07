package com.s20plus.pixeltraffic;
final class PreviewLayout {
 static final class Bounds {
  final float left,top,right,bottom;
  Bounds(float l,float t,float r,float b){left=l;top=t;right=r;bottom=b;}
 }
 static Bounds fit(int width,int height) {
  if(width<=0||height<=0)return new Bounds(0,0,0,0);
  float scale=Math.min(width/360f,height/800f);
  float w=360*scale,h=800*scale,left=(width-w)/2,top=(height-h)/2;
  return new Bounds(left,top,left+w,top+h);
 }
}
