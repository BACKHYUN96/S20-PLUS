package android.graphics;
import java.awt.*;
public class Canvas { Graphics2D g; public Canvas(Bitmap b){g=b.image.createGraphics();} public void drawRect(float l,float t,float r,float b,Paint p){g.setColor(new Color(p.color,true));g.fill(new java.awt.geom.Rectangle2D.Float(l,t,r-l,b-t));}public void drawPath(Path path,Paint p){g.setColor(new Color(p.color,true));g.fill(path.p);} }
