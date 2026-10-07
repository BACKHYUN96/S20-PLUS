package com.s20plus.pixeltraffic;
public class CameraChecks {
 public static void main(String[] args){for(int width:new int[]{360,591,1080,1600})for(int height:new int[]{350,800,2400})for(int mode=0;mode<2;mode++)for(int pos=0;pos<3;pos++){
 float scale=CameraLayout.scale(width,mode),x=CameraLayout.offsetX(width,mode,pos),y=CameraLayout.offsetY(width,height,mode);
 if(x>0.001||x+360*scale<width-.001||Math.abs(y+800*scale-height)>.001)throw new AssertionError("viewport edge/bottom");
 if(mode==0&&(Math.abs(x)>.001||Math.abs(scale-width/360f)>.001))throw new AssertionError("legacy changed");
 }
 if(CameraLayout.offsetX(360,1,0)!=0||CameraLayout.offsetX(360,1,1)!=-90||CameraLayout.offsetX(360,1,2)!=-180)throw new AssertionError("pan endpoints");
 System.out.println("PASS: camera legacy scale, zoom/pan coverage, bottom anchor across screen sizes");}
}
