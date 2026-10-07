package android.graphics;

public final class Color {
 public static final int WHITE=0xffffffff,BLACK=0xff000000,TRANSPARENT=0;
 private Color(){}
 public static int argb(int a,int r,int g,int b){return (a&255)<<24|(r&255)<<16|(g&255)<<8|(b&255);}
 public static int rgb(int r,int g,int b){return argb(255,r,g,b);}
}
