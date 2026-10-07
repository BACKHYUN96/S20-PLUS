package com.s20plus.pixeltraffic;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Runs the real CityScene draw path through a desktop shim, not an Android screenshot. */
public final class SceneRenderPreview {
 private static final String[] ROADS={"City","Coast","Mountain","Highway"};
 static Bitmap render(int road,int theme,int weather,int camera,int position,double seconds,int density,boolean events,int frequency) {
  CityScene scene=new CityScene();
  try {
   scene.configure(density,1);scene.setRoad(road);scene.setTheme(theme);scene.setVehicles(0,0);
   scene.setWeather(weather);scene.setCamera(camera,position);scene.setSceneryEnabled(true);scene.setEvents(events,frequency);
   int steps=(int)Math.round(seconds*30);
   for(int i=0;i<steps;i++)scene.update(1.0/30);
   Bitmap output=Bitmap.createBitmap(360,800,Bitmap.Config.ARGB_8888);
   scene.draw(new Canvas(output));return output;
  } finally {scene.release();}
 }
 private static void write(Bitmap bitmap,File file)throws Exception {
  File parent=file.getAbsoluteFile().getParentFile();if(!parent.exists()&&!parent.mkdirs())throw new IllegalStateException("Cannot create preview directory");
  if(!ImageIO.write(bitmap.image,"png",file))throw new IllegalStateException("No PNG writer");
 }
 public static void main(String[] args)throws Exception {
  if(args.length==0)throw new IllegalArgumentException("output.png [road=0 theme=1 weather=2 camera=0 position=1 seconds=10 density=3 events=true frequency=2], or --sheet output.png");
  if(args[0].equals("--sheet")) {
   if(args.length<2)throw new IllegalArgumentException("--sheet needs output filename");
   int cellWidth=360,cellHeight=830;
   Bitmap sheet=Bitmap.createBitmap(cellWidth*2,cellHeight*2,Bitmap.Config.ARGB_8888);
   Graphics2D g=sheet.image.createGraphics();g.setColor(new Color(0xff111522,true));g.fillRect(0,0,sheet.getWidth(),sheet.getHeight());
   g.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,16));
   for(int road=0;road<4;road++) {
    Bitmap rendered=render(road,1,2,0,1,10,6,true,2);int x=(road%2)*cellWidth,y=(road/2)*cellHeight;
    g.drawImage(rendered.image,x,y,null);g.setColor(new Color(0xffd6dbe7,true));g.drawString(ROADS[road]+" / desktop CityScene render",x+8,y+820);rendered.recycle();
   }
   g.dispose();write(sheet,new File(args[1]));
  } else {
   int road=number(args,1,0),theme=number(args,2,1),weather=number(args,3,2),camera=number(args,4,0),position=number(args,5,1);
   double seconds=args.length>6?Double.parseDouble(args[6]):10;
   int density=number(args,7,3);boolean events=args.length>8?Boolean.parseBoolean(args[8]):true;int frequency=number(args,9,2);
   if(!Double.isFinite(seconds)||seconds<0||seconds>600)throw new IllegalArgumentException("Preview duration must be 0..600 seconds");
   Bitmap rendered=render(road,theme,weather,camera,position,seconds,density,events,frequency);write(rendered,new File(args[0]));rendered.recycle();
  }
  System.out.println("PASS: production CityScene desktop PNG exported; Java2D pixels are not Android device output or a performance measurement");
 }
 private static int number(String[] args,int index,int fallback){return args.length>index?Integer.parseInt(args[index]):fallback;}
}
