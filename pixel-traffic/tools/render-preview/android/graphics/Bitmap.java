package android.graphics;

import java.awt.image.BufferedImage;

/** Desktop-only bitmap; this shim is never included in the Android app. */
public final class Bitmap {
 public enum Config { ARGB_8888 }
 public final BufferedImage image;
 private boolean recycled;
 private Bitmap(int width,int height) {image=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);}
 public static Bitmap createBitmap(int width,int height,Config config) {
  if(config!=Config.ARGB_8888)throw new IllegalArgumentException("Unsupported desktop bitmap format");
  return new Bitmap(width,height);
 }
 public int getWidth(){return image.getWidth();}
 public int getHeight(){return image.getHeight();}
 public boolean hasAlpha(){checkAlive();return image.getColorModel().hasAlpha();}
 public void getPixels(int[] pixels,int offset,int stride,int x,int y,int width,int height) {
  checkAlive();image.getRGB(x,y,width,height,pixels,offset,stride);
 }
 public void setPixels(int[] pixels,int offset,int stride,int x,int y,int width,int height) {
  checkAlive();image.setRGB(x,y,width,height,pixels,offset,stride);
 }
 public boolean isRecycled(){return recycled;}
 public void recycle(){recycled=true;}
 void checkAlive(){if(recycled)throw new IllegalStateException("Drawing recycled bitmap");}
}
