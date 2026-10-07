package com.s20plus.pixeltraffic;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.*;
import android.graphics.Bitmap;
public class Preview {
 public static void main(String[] args)throws Exception {
  PixelArt art=new PixelArt(args.length>1?Integer.parseInt(args[1]):1,args.length>2?Integer.parseInt(args[2]):0);Bitmap b=art.background(),fg=art.foreground();
  Graphics2D g=b.image.createGraphics();
  TrafficModel traffic=new TrafficModel();
  for(int i=0;i<120;i++)traffic.update(1.0/30);
  for(TrafficModel.Car car:traffic.cars) {
   double y=car.y();if(y<270||y>800)continue;
   double t=(y-260)/540,scale=.28+.72*t;
   double x=PixelArt.left((float)y)+(PixelArt.right((float)y)-PixelArt.left((float)y))*(car.lane+.5)/4;
   java.awt.geom.AffineTransform a=new java.awt.geom.AffineTransform();a.translate(x,y);a.shear((-170+250*(car.lane+.5)/4)/540,0);a.scale(scale,scale);a.translate(-20,-40);
   g.drawImage(new PixelArt().car(car.type,car.lane>=2).image,a,null);
  }
  g.drawImage(fg.image,0,0,null);g.dispose();
  ImageIO.write(b.image,"png",new File(args[0]));
  for(int palette=0;palette<3;palette++)for(int i=0;i<6;i++)for(int d=0;d<2;d++) {
   Bitmap sprite=art.car(i,d==1,palette);int visible=0;
   for(int y=0;y<80;y++)for(int x=0;x<40;x++)if((sprite.image.getRGB(x,y)>>>24)!=0)visible++;
   if(visible<100)throw new AssertionError("Empty sprite");
   for(int x=0;x<40;x++)if((sprite.image.getRGB(x,0)>>>24)!=0||(sprite.image.getRGB(x,79)>>>24)!=0)throw new AssertionError("Sprite clipped vertically");
   for(int y=0;y<80;y++)if((sprite.image.getRGB(0,y)>>>24)!=0||(sprite.image.getRGB(39,y)>>>24)!=0)throw new AssertionError("Sprite clipped horizontally");
  }
  System.out.println("PASS: all 36 sprite variants contain artwork and transparent unclipped borders; desktop asset preview exported (not an Android screenshot)");
 }
}
