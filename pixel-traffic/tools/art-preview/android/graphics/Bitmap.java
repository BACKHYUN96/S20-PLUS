package android.graphics;
import java.awt.image.BufferedImage;
public class Bitmap { public enum Config{ARGB_8888} public BufferedImage image; public static Bitmap createBitmap(int w,int h,Config c){Bitmap b=new Bitmap();b.image=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);return b;} }
