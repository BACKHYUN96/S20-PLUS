package android.graphics;

import java.awt.geom.Path2D;

public final class Path {
 final Path2D.Float shape=new Path2D.Float();
 public void moveTo(float x,float y){shape.moveTo(x,y);}
 public void lineTo(float x,float y){shape.lineTo(x,y);}
 public void close(){shape.closePath();}
 public void reset(){shape.reset();}
}
