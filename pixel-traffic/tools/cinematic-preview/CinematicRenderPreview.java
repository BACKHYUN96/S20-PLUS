package com.s20plus.pixeltraffic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import java.io.File;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Production drawing path on desktop; never an Android/GPU performance test. */
public final class CinematicRenderPreview {
 public static void main(String[] args)throws Exception {
  if(args.length<2)throw new IllegalArgumentException("output.png assets-directory [seconds=10 density=6 weather=2 camera=0 position=1 width=540 height=1200 theme=1 signals=0]");
  double seconds=args.length>2?Double.parseDouble(args[2]):10;
  if(!Double.isFinite(seconds)||seconds<0||seconds>600)throw new IllegalArgumentException("Duration must be 0..600 seconds");
  int density=number(args,3,6),weather=number(args,4,2),camera=number(args,5,0),position=number(args,6,1);
  int width=number(args,7,540),height=number(args,8,1200);
  if(width<1||height<1||width>4000||height>4000)throw new IllegalArgumentException("Dimensions must be 1..4000");
  CinematicScene scene=new CinematicScene(new Context(Path.of(args[1])));
  Bitmap output=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
  try {
   if(!scene.available())throw new IllegalStateException("Cinematic asset did not load from "+args[1]);
   scene.configure(density,1,0,0,weather,100,camera,position,true,true,2);scene.setTheme(number(args,9,1));scene.setSignals(number(args,10,0)==1);
   for(int i=0;i<Math.round(seconds*30);i++)scene.update(1.0/30);
   scene.draw(new Canvas(output),width,height);
   File file=new File(args[0]);File parent=file.getAbsoluteFile().getParentFile();
   if(!parent.exists()&&!parent.mkdirs())throw new IllegalStateException("Cannot create preview directory");
   if(!ImageIO.write(output.image,"png",file))throw new IllegalStateException("No PNG writer");
  } finally {scene.release();output.recycle();}
  System.out.println("PASS: production CinematicScene desktop PNG exported; not Android pixels or a GPU performance measurement");
 }
 private static int number(String[] args,int index,int fallback){return args.length>index?Integer.parseInt(args[index]):fallback;}
}
