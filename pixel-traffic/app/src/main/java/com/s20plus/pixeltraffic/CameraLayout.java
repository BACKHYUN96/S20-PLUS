package com.s20plus.pixeltraffic;
/** Bottom-anchored viewport; nearest-neighbour raster scaling is handled by Canvas. */
final class CameraLayout {
 static float scale(int width,int mode){return Math.max(0,width)/360f*(mode==1?1.5f:1f);}
 static float offsetX(int width,int mode,int position){float overflow=360*scale(width,mode)-width;return -Math.max(0,overflow)*Math.max(0,Math.min(2,position))/2;}
 static float offsetY(int width,int height,int mode){return height-800*scale(width,mode);}
}
