package com.s20plus.pixeltraffic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import javax.imageio.ImageIO;

/** Independent artwork landmarks plus production theme composition, continuity and ownership. */
public final class CinematicThemeChecks {
 private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
 private static void near(double a,double b,double limit,String reason){check(Math.abs(a-b)<=limit,reason+": "+a+" != "+b);}
 public static void main(String[] args)throws Exception{
  check(args.length==1,"Expected asset directory");Path root=Path.of(args[0]);
  verifyArtwork(root);verifySolidBlend();verifyTrafficAndOwnership(new Context(root));verifyFallback();
  ThemeBlend blend=new ThemeBlend();blend.snap(2);blend.select(0);
  for(int i=0;i<40;i++)blend.update(.05);
  near(blend.weights[0],.5,1e-8,"Snap/transition midpoint");near(blend.weights[2],.5,1e-8,"Snap lost start theme");
  check(SceneTimePolicy.themeForHour(6)==0&&SceneTimePolicy.themeForHour(17)==1&&SceneTimePolicy.themeForHour(20)==2,"Automatic time policy changed");
  System.out.println("PASS: day/sunset/night road landmarks, exact weighted blend/retarget, startup selection, 4-second active transition, unchanged vehicle poses, theme fallback/cache release and automatic time boundaries");
 }
 private static void verifyArtwork(Path root)throws Exception{
  String[] names={"cinematic-city-day.png","cinematic-city.png","cinematic-city-night.png","cinematic-city-day-dry.png","cinematic-city-dry.png","cinematic-city-night-dry.png","cinematic-city-day-snow.png","cinematic-city-snow.png","cinematic-city-night-snow.png","cinematic-city-day-fog.png","cinematic-city-fog.png","cinematic-city-night-fog.png"};
  BufferedImage original=ImageIO.read(root.resolve(names[1]).toFile());
  // Independent median landmarks measured in 0.20.0; do not derive them from Geometry.
  float[][] marks={{500,360.5f},{600,347},{700,335.3f},{800,323.6f},{900,310.4f},{1000,297.6f},{1100,287},{1200,274.4f}};
  for(String name:names){
   BufferedImage image=ImageIO.read(root.resolve(name).toFile());check(image!=null,"Missing illustration "+name);
   int tolerance=name.contains("dry")||name.contains("snow")||name.contains("fog")?1:0;
   check(Math.abs(image.getWidth()-original.getWidth())<=tolerance&&Math.abs(image.getHeight()-original.getHeight())<=tolerance,"Theme framing dimensions changed "+name);
   for(float[] mark:marks){
    double baseline=median(original,mark),actual=median(image,mark);
    near(actual,baseline,3.5,"Median differs from original artwork "+name+" at "+mark[0]);
   }
   // Six independently sampled white crossing bars must retain their centers in this full-width row.
   int row=Math.round(850*image.getHeight()/1200f);
   for(float x:new float[]{175.3f,213.8f,251.7f,290.5f,328.4f,365.7f}){
    int center=Math.round(x*image.getWidth()/540);int bright=0;
    for(int px=center-9;px<=center+9;px++){
     int pixel=image.getRGB(px,row),r=pixel>>16&255,g=pixel>>8&255,b=pixel&255;
     if(r>110&&g>100&&b>100&&Math.max(r,Math.max(g,b))-Math.min(r,Math.min(g,b))<65)bright++;
    }
    check(bright>=8,"Crosswalk bar missing/moved "+name+" x="+x);
   }
  }
  System.out.println("PASS: 96 independently measured median landmarks and 72 crosswalk-bar anchors in shared logical framing");
 }
 private static double median(BufferedImage image,float[] mark){
  int row=Math.min(image.getHeight()-1,Math.round(mark[0]*image.getHeight()/1200));
  int center=Math.round(mark[1]*image.getWidth()/540);
  int first=-1,last=-1,count=0,bestCount=0,bestFirst=0,bestLast=0;
  // Two road-colored gap pixels separate the double stripe; never bridge that gap.
  // Pick a coherent paint stripe, not a color-weighted centroid of both stripes.
  // The second stripe is darker in the sunset source, so brightness weighting shifts it falsely.
  for(int x=Math.max(0,center-20);x<=Math.min(image.getWidth()-1,center+23);x++){
   int pixel=image.getRGB(x,row),r=pixel>>16&255,g=pixel>>8&255,b=pixel&255;
   boolean hit=x<=center+20&&r>115&&g>75&&r>b*1.45&&g>b*1.2;
   if(hit){if(first<0)first=x;last=x;count++;}
   if(first>=0&&x-last>1){
    if(bestCount==0&&count>=2){bestCount=count;bestFirst=first;bestLast=last;}
    first=-1;count=0;
   }
  }
  check(bestCount>0,"Yellow median missing at "+mark[0]);
  return (bestFirst+bestLast)*270.0/image.getWidth();
 }
 private static void verifySolidBlend()throws Exception{
  Path root=Files.createTempDirectory("cinematic-theme-fixture-");
  write(root,"cinematic-city-day.png",0xff00ff00);write(root,"cinematic-city.png",0xffff0000);write(root,"cinematic-city-night.png",0xff0000ff);
  CinematicScene scene=new CinematicScene(new Context(root));Bitmap output=Bitmap.createBitmap(540,1200,Bitmap.Config.ARGB_8888);
  try{
   scene.configure(1,1,0,0,0,100,0,1,false,false,0);
   scene.setTheme(0);near(sample(scene,output),0xff00ff00,0,"Startup must show selected theme without sunset flash");
   scene.setTheme(2);for(int i=0;i<40;i++)scene.update(.05);
   int mid=sample(scene,output);check((mid>>>24)==255,"Blend became transparent");
   near(mid>>16&255,0,0,"Unrelated sunset contaminates day/night blend");near(mid>>8&255,128,1,"Day/night green weight");near(mid&255,128,1,"Day/night blue weight");
   scene.setTheme(1);check(sample(scene,output)==mid,"Retarget causes a visible jump");
   for(int i=0;i<80;i++)scene.update(.05);check(sample(scene,output)==0xffff0000,"Retarget did not finish at 4 active seconds");
   int frozen=sample(scene,output);scene.update(Double.NaN);scene.update(-1);check(sample(scene,output)==frozen,"Invalid time changes theme");
  }finally{scene.release();output.recycle();for(String name:new String[]{"cinematic-city-day.png","cinematic-city.png","cinematic-city-night.png"})Files.delete(root.resolve(name));Files.delete(root);}
 }
 private static void verifyTrafficAndOwnership(Context context)throws Exception{
  CinematicScene scene=new CinematicScene(context),reference=new CinematicScene(context);Bitmap output=Bitmap.createBitmap(540,1200,Bitmap.Config.ARGB_8888);
  try{
   scene.configure(6,1.5,0,0,2,100,0,1,true,true,2);reference.configure(6,1.5,0,0,2,100,0,1,true,true,2);
   scene.setTheme(0);reference.setTheme(1);
   Bitmap[] cached=((Bitmap[])field(scene,"themeBackgrounds")).clone();
   check(cached[0]!=null&&cached[1]!=null&&cached[2]==null,"Unselected night artwork eagerly loaded");
   double before=((TrafficModel)field(scene,"traffic")).cars[0].position;
   scene.setTheme(2);Bitmap retiredNight=((Bitmap[])field(scene,"themeBackgrounds"))[2];near(((TrafficModel)field(scene,"traffic")).cars[0].position,before,0,"Selecting theme resets traffic");
   for(int tick=0;tick<180;tick++){
    if(tick==30)scene.setTheme(1);if(tick==75)scene.setTheme(0);
    scene.update(.05);reference.update(.05);scene.draw(new Canvas(output));reference.draw(new Canvas(output));
    TrafficModel.Car[] cars=((TrafficModel)field(scene,"traffic")).cars,other=((TrafficModel)field(reference,"traffic")).cars;
    for(int i=0;i<cars.length;i++)near(cars[i].position,other[i].position,0,"Transition changes vehicle movement");
    for(String name:new String[]{"bodyWidths","bodyLengths","groundX","groundY","slopes"})
     check(Arrays.equals((float[])field(scene,name),(float[])field(reference,name)),"Transition changes vehicle pose "+name);
   }
   Bitmap[] current=(Bitmap[])field(scene,"themeBackgrounds");
   check(current[0]==cached[0]&&current[1]==cached[1],"Active/fallback artwork reloaded");
   check(current[2]==null&&retiredNight.isRecycled(),"Retired theme artwork not released");
   ThemeBlend blend=(ThemeBlend)field(scene,"theme");near(blend.weights[0],1,1e-8,"Retargeted daytime did not settle");
   scene.release();scene.release();for(Bitmap image:cached)if(image!=null)check(image.isRecycled(),"Theme bitmap not released");
  }finally{scene.release();reference.release();output.recycle();}
 }
 private static void verifyFallback()throws Exception{
  Path root=Files.createTempDirectory("cinematic-theme-fallback-");write(root,"cinematic-city.png",0xffff0000);
  Files.write(root.resolve("cinematic-city-day.png"),new byte[]{1,2,3});
  BufferedImage mismatch=new BufferedImage(8,8,BufferedImage.TYPE_INT_RGB);ImageIO.write(mismatch,"png",root.resolve("cinematic-city-night.png").toFile());
  CinematicScene scene=new CinematicScene(new Context(root));
  try{
   check(scene.available(),"Optional broken variants discard usable sunset");
   for(int selected:new int[]{0,2}){scene.setTheme(selected);near(((ThemeBlend)field(scene,"theme")).weights[1],1,0,"Invalid theme variant not replaced by sunset");check(scene.themeDescription().contains("대체"),"Fallback not reported");}
  }finally{scene.release();for(String name:new String[]{"cinematic-city-day.png","cinematic-city.png","cinematic-city-night.png"})Files.delete(root.resolve(name));Files.delete(root);}
 }
 private static void write(Path root,String name,int color)throws Exception{
  BufferedImage image=new BufferedImage(16,36,BufferedImage.TYPE_INT_RGB);for(int y=0;y<36;y++)for(int x=0;x<16;x++)image.setRGB(x,y,color);
  ImageIO.write(image,"png",root.resolve(name).toFile());
 }
 private static int sample(CinematicScene scene,Bitmap image){scene.draw(new Canvas(image));int[] pixel=new int[1];image.getPixels(pixel,0,1,20,20,1,1);return pixel[0];}
 private static Object field(Object object,String name)throws Exception{Field value=object.getClass().getDeclaredField(name);value.setAccessible(true);return value.get(object);}
}
