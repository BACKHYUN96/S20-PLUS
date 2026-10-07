package android.graphics;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/** Desktop decode adapter. It never enters the Android APK. */
public final class BitmapFactory {
 public static final class Options {
  public boolean inScaled;
  public Bitmap.Config inPreferredConfig=Bitmap.Config.ARGB_8888;
  public int inSampleSize=1;
 }
 private BitmapFactory() {}
 public static Bitmap decodeStream(InputStream stream) {return decodeStream(stream,null,null);}
 public static Bitmap decodeStream(InputStream stream,Rect padding,Options options) {
  try {
   BufferedImage source=ImageIO.read(stream);
   if(source==null)return null;
   int sample=options==null?1:Math.max(1,options.inSampleSize);
   Bitmap image=Bitmap.createBitmap(Math.max(1,source.getWidth()/sample),Math.max(1,source.getHeight()/sample),Bitmap.Config.ARGB_8888);
   Graphics2D g=image.image.createGraphics();g.drawImage(source,0,0,image.getWidth(),image.getHeight(),null);g.dispose();
   return image;
  } catch(IOException ex) {return null;}
 }
}
