package com.s20plus.pixeltraffic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import javax.imageio.ImageIO;

/** Independent color oracle for the joint theme/surface blend and real scene ownership. */
public final class CinematicSurfaceChecks {
 private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
 private static void near(double a,double b,double error,String reason){check(Math.abs(a-b)<=error,reason+": "+a+" != "+b);}
 public static void main(String[] args)throws Exception{
  RoadWetness road=new RoadWetness();road.select(2);near(road.value(),1,0,"Wet startup flashes dry");
  road.select(0);near(road.value(),1,0,"Selection jumps wetness");
  for(int i=0;i<40;i++)road.update(.05);near(road.value(),.5,.00001,"Drying midpoint");
  road.select(1);near(road.value(),.5,.00001,"Retarget jumps wetness");
  road.update(Double.NaN);road.update(-1);near(road.value(),.5,.00001,"Invalid time changes surface");
  for(int i=0;i<80;i++)road.update(.05);near(road.value(),.68,.00001,"Light rain target");
  road.select(0);for(int i=0;i<80;i++)road.update(.05);near(road.value(),0,0,"Clear endpoint");
  verifyJointBlend();verifyRealScene(new Context(Path.of(args[0])));verifyFallback();
  System.out.println("PASS: initial/smooth/retargeted wetness, joint time/surface pixel oracle, original sky retained, residual drying, weather-independent vehicle poses, optional dry fallback and selected-art cache/release");
 }
 private static final String[] NAMES={"cinematic-city-day.png","cinematic-city.png","cinematic-city-night.png","cinematic-city-day-dry.png","cinematic-city-dry.png","cinematic-city-night-dry.png"};
 private static void verifyJointBlend()throws Exception{
  Path root=Files.createTempDirectory("cinematic-surface-fixture-");
  int[] colors={0xff00ff00,0xffff0000,0xff0000ff,0xffffff00,0xff00ffff,0xffff00ff};
  for(int i=0;i<6;i++)write(root,NAMES[i],colors[i]);
  CinematicScene scene=new CinematicScene(new Context(root));Bitmap output=Bitmap.createBitmap(540,1200,Bitmap.Config.ARGB_8888);
  try{
   scene.setTheme(0);configure(scene,0);verifyPixels(scene,output,colors,"Dry startup");
   scene.setTheme(2);configure(scene,2);
   for(int i=0;i<40;i++)scene.update(.05);
   near(scene.surfaceWetness(),.5,.00001,"Simultaneous weather midpoint");verifyPixels(scene,output,colors,"Joint midpoint");
   int frozen=sample(scene,output,100,1000);configure(scene,1);scene.setTheme(1);
   check(sample(scene,output,100,1000)==frozen,"Joint retarget jumps image");
   for(int i=0;i<80;i++)scene.update(.05);verifyPixels(scene,output,colors,"Joint endpoint");
   configure(scene,0);for(int i=0;i<80;i++)scene.update(.05);verifyPixels(scene,output,colors,"Dry endpoint");
  }finally{scene.release();output.recycle();for(String name:NAMES)Files.delete(root.resolve(name));Files.delete(root);}
 }
 private static void verifyPixels(CinematicScene scene,Bitmap output,int[] colors,String label)throws Exception{
  ThemeBlend theme=(ThemeBlend)field(scene,"theme");RoadWetness road=(RoadWetness)field(scene,"surface");
  for(boolean lower:new boolean[]{false,true}){
   int actual=sample(scene,output,100,lower?1000:100);check((actual>>>24)==255,label+" loses opacity");
   for(int shift:new int[]{16,8,0}){
    double expected=0;for(int i=0;i<3;i++)expected+=theme.weights[i]*(((colors[i]>>shift)&255)*(lower?road.value():1)+((colors[i+3]>>shift)&255)*(lower?1-road.value():0));
    near((actual>>shift)&255,expected,2,label+" channel "+shift+" lower="+lower);
   }
  }
 }
 private static void verifyRealScene(Context context)throws Exception{
  CinematicScene scene=new CinematicScene(context),reference=new CinematicScene(context);Bitmap output=Bitmap.createBitmap(540,1200,Bitmap.Config.ARGB_8888);
  try{
   configure(scene,2);configure(reference,2);scene.setTheme(1);reference.setTheme(1);
   Bitmap[] dry=(Bitmap[])field(scene,"dryBackgrounds");for(Bitmap bitmap:dry)check(bitmap==null,"Strong rain eagerly loads dry artwork");
   Bitmap selectedDry=null;
   for(int tick=0;tick<180;tick++){
    if(tick==10){configure(scene,0);selectedDry=dry[1];check(selectedDry!=null,"Selected dry artwork missing");}if(tick==45)configure(scene,1);if(tick==80)configure(scene,0);
    scene.update(.05);reference.update(.05);scene.draw(new Canvas(output));reference.draw(new Canvas(output));
    TrafficModel.Car[] cars=((TrafficModel)field(scene,"traffic")).cars,others=((TrafficModel)field(reference,"traffic")).cars;
    for(int i=0;i<cars.length;i++)near(cars[i].position,others[i].position,0,"Weather resets traffic");
    for(String name:new String[]{"bodyWidths","bodyLengths","groundX","groundY","slopes"})check(Arrays.equals((float[])field(scene,name),(float[])field(reference,name)),"Weather changes pose "+name);
   }
   near(scene.surfaceWetness(),0,0,"Road fails to finish drying");check(scene.surfaceDescription().contains("마름"),"Dry diagnostic wrong");
   check(dry[1]==selectedDry&&dry[0]==null&&dry[2]==null,"Active dry artwork reloaded/unselected artwork cached");
   scene.release();scene.release();check(selectedDry.isRecycled(),"Dry artwork leaks");
  }finally{scene.release();reference.release();output.recycle();}
 }
 private static void verifyFallback()throws Exception{
  Path root=Files.createTempDirectory("cinematic-dry-fallback-");write(root,"cinematic-city.png",0xffff0000);
  Files.write(root.resolve("cinematic-city-dry.png"),new byte[]{1,2,3});
  CinematicScene scene=new CinematicScene(new Context(root));
  try{configure(scene,0);near(scene.surfaceWetness(),1,0,"Wet fallback effects disagree with artwork");check(scene.available()&&scene.surfaceDescription().contains("대체"),"Dry fallback not usable/reported");}
  finally{scene.release();Files.delete(root.resolve("cinematic-city.png"));Files.delete(root.resolve("cinematic-city-dry.png"));Files.delete(root);}
 }
 private static void configure(CinematicScene scene,int weather){scene.configure(6,1.5,0,0,weather,100,0,1,false,false,0);}
 private static void write(Path root,String name,int color)throws Exception{
  BufferedImage image=new BufferedImage(16,36,BufferedImage.TYPE_INT_RGB);for(int y=0;y<36;y++)for(int x=0;x<16;x++)image.setRGB(x,y,color);ImageIO.write(image,"png",root.resolve(name).toFile());
 }
 private static int sample(CinematicScene scene,Bitmap output,int x,int y)throws Exception{
  Method draw=CinematicScene.class.getDeclaredMethod("drawBackground",Canvas.class);draw.setAccessible(true);draw.invoke(scene,new Canvas(output));int[] pixel=new int[1];output.getPixels(pixel,0,1,x,y,1,1);return pixel[0];
 }
 private static Object field(Object scene,String name)throws Exception{Field field=scene.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(scene);}
}
