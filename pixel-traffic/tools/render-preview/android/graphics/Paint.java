package android.graphics;

public final class Paint {
 public static final int ANTI_ALIAS_FLAG=1,FILTER_BITMAP_FLAG=2;
 int color=0xff000000;
 boolean antialias,filter;
 public Paint(){}
 public Paint(int flags){antialias=(flags&ANTI_ALIAS_FLAG)!=0;filter=(flags&FILTER_BITMAP_FLAG)!=0;}
 public void setColor(int value){color=value;}
 public int getColor(){return color;}
 public void setAlpha(int value){color=(color&0x00ffffff)|(Math.max(0,Math.min(255,value))<<24);}
 public int getAlpha(){return color>>>24;}
 public void setAntiAlias(boolean value){antialias=value;}
 public void setFilterBitmap(boolean value){filter=value;}
}
