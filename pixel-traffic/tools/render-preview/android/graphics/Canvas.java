package android.graphics;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayDeque;

/** Functional subset used by production scene code, backed by desktop Java2D. */
public final class Canvas {
 private final Bitmap bitmap;
 private Graphics2D graphics;
 private final ArrayDeque<Graphics2D> stack=new ArrayDeque<>();
 public Canvas(Bitmap bitmap){this.bitmap=bitmap;bitmap.checkAlive();graphics=bitmap.image.createGraphics();}
 public int getWidth(){return bitmap.getWidth();}
 public int getHeight(){return bitmap.getHeight();}
 public int save(){int count=stack.size()+1;stack.push(graphics);graphics=(Graphics2D)graphics.create();return count;}
 public void restore(){if(stack.isEmpty())throw new IllegalStateException("Unbalanced Canvas restore");graphics.dispose();graphics=stack.pop();}
 public void restoreToCount(int count){while(stack.size()>=count)restore();}
 public void translate(float x,float y){graphics.translate(x,y);}
 public void scale(float x,float y){graphics.scale(x,y);}
 public void skew(float x,float y){graphics.shear(x,y);}
 public void rotate(float degrees){graphics.rotate(Math.toRadians(degrees));}
 public boolean clipRect(float l,float t,float r,float b){graphics.clip(new Rectangle2D.Float(l,t,r-l,b-t));return !graphics.getClipBounds().isEmpty();}
 public boolean clipPath(Path path){graphics.clip(path.shape);return !graphics.getClipBounds().isEmpty();}
 private void prepare(Paint paint,boolean image) {
  bitmap.checkAlive();
  graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,paint!=null&&paint.antialias?RenderingHints.VALUE_ANTIALIAS_ON:RenderingHints.VALUE_ANTIALIAS_OFF);
  graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,paint!=null&&paint.filter?RenderingHints.VALUE_INTERPOLATION_BILINEAR:RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
  graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,image&&paint!=null?paint.getAlpha()/255f:1f));
  if(!image)graphics.setColor(new java.awt.Color(paint==null?0xff000000:paint.color,true));
 }
 public void drawRect(float l,float t,float r,float b,Paint paint){if(r<=l||b<=t)return;prepare(paint,false);graphics.fill(new Rectangle2D.Float(l,t,r-l,b-t));}
 public void drawPath(Path path,Paint paint){prepare(paint,false);graphics.fill(path.shape);}
 public void drawBitmap(Bitmap image,float left,float top,Paint paint) {
  image.checkAlive();prepare(paint,true);graphics.drawImage(image.image,AffineTransform.getTranslateInstance(left,top),null);
 }
 public void drawBitmap(Bitmap image,Rect source,RectF dest,Paint paint) {
  image.checkAlive();if(dest.width()<=0||dest.height()<=0)return;prepare(paint,true);
  int l=source==null?0:source.left,t=source==null?0:source.top,r=source==null?image.getWidth():source.right,b=source==null?image.getHeight():source.bottom;
  if(r<=l||b<=t)return;
  Graphics2D clipped=(Graphics2D)graphics.create();
  clipped.clip(new Rectangle2D.Float(dest.left,dest.top,dest.width(),dest.height()));
  AffineTransform placement=new AffineTransform();placement.translate(dest.left,dest.top);placement.scale(dest.width()/(r-l),dest.height()/(b-t));placement.translate(-l,-t);
  clipped.drawImage(image.image,placement,null);clipped.dispose();
 }
 public void drawBitmap(Bitmap image,Rect source,Rect dest,Paint paint){drawBitmap(image,source,new RectF(dest.left,dest.top,dest.right,dest.bottom),paint);}
 public void drawColor(int color){drawColor(color,PorterDuff.Mode.SRC_OVER);}
 public void drawColor(int color,PorterDuff.Mode mode) {
  bitmap.checkAlive();Graphics2D fill=(Graphics2D)graphics.create();
  Shape clip=fill.getClip();if(clip!=null)clip=fill.getTransform().createTransformedShape(clip);
  fill.setTransform(new AffineTransform());fill.setClip(clip);
  fill.setComposite(mode==PorterDuff.Mode.SRC?AlphaComposite.Src:mode==PorterDuff.Mode.CLEAR?AlphaComposite.Clear:AlphaComposite.SrcOver);
  fill.setColor(new java.awt.Color(color,true));fill.fillRect(0,0,getWidth(),getHeight());fill.dispose();
 }
}
