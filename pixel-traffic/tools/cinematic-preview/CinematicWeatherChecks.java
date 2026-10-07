package com.s20plus.pixeltraffic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import javax.imageio.ImageIO;

/** Weather/time color oracle, lazy ownership, decode-free frames and actual car invariants. */
public final class CinematicWeatherChecks {
 private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
 private static void near(double a,double b,double tolerance,String reason){check(Math.abs(a-b)<=tolerance,reason+": "+a+" != "+b);}
 public static void main(String[] args)throws Exception{
  verifyColors();verifyProduction(new Context(Path.of(args[0])));verifyFallback();
  System.out.println("PASS: weather/time joint pixel oracle, startup and 4-second retarget, real 24-car positions/poses, lazy cache/retirement/active reuse, decode-free update/draw, snow layers/fade, malformed weather fallback and owned release");
 }
 private static void verifyColors()throws Exception{
  Path root=Files.createTempDirectory("cinematic-weather-colors-");
  int[][] colors={{0xff007d14,0xffb51e09,0xff092594},{0xff949322,0xffd827ba,0xff1f9aa3},{0xffb4cadb,0xffdea788,0xff3b5875},{0xff70bebe,0xffbfa994,0xff447184}};
  for(int kind=0;kind<4;kind++)for(int theme=0;theme<3;theme++)write(root,CinematicArtwork.name(kind,theme),colors[kind][theme],16,36);
  CinematicScene scene=new CinematicScene(new Context(root));Bitmap output=Bitmap.createBitmap(540,1200,Bitmap.Config.ARGB_8888);
  try{
   configure(scene,3);scene.setTheme(0);verifyPixels(scene,output,colors,"Snow startup");
   configure(scene,4);scene.setTheme(2);for(int i=0;i<40;i++)scene.update(.05);verifyPixels(scene,output,colors,"Snow/fog and day/night midpoint");
   int frozen=sample(scene,output,1000);configure(scene,0);scene.setTheme(1);check(sample(scene,output,1000)==frozen,"Retarget jumps background");
   for(int i=0;i<25;i++)scene.update(.05);verifyPixels(scene,output,colors,"Three-theme/weather retarget");
   scene.update(Double.NaN);scene.update(-1);verifyPixels(scene,output,colors,"Invalid elapsed time");
   for(int i=0;i<80;i++)scene.update(.05);verifyPixels(scene,output,colors,"Dry endpoint");
   CinematicArtwork cache=(CinematicArtwork)field(scene,"artwork");check(cache.count()==2,"Retarget retains irrelevant weather/time cache");
  }finally{scene.release();output.recycle();for(int kind=0;kind<4;kind++)for(int theme=0;theme<3;theme++)Files.delete(root.resolve(CinematicArtwork.name(kind,theme)));Files.delete(root);}
 }
 private static void verifyPixels(CinematicScene scene,Bitmap output,int[][] colors,String label)throws Exception{
  ThemeBlend time=(ThemeBlend)field(scene,"theme"),weather=(ThemeBlend)field(scene,"weatherArt");RoadWetness surface=(RoadWetness)field(scene,"surface");
  for(int y:new int[]{100,1000}){
   int actual=sample(scene,output,y);check((actual>>>24)==255,label+" lost opacity");
   for(int shift:new int[]{16,8,0}){
    double expected=0;
    for(int i=0;i<3;i++){
     double normal=((colors[0][i]>>shift)&255)*(y<390?1:surface.value())+((colors[1][i]>>shift)&255)*(y<390?0:1-surface.value());
     expected+=time.weights[i]*(weather.weights[0]*normal+weather.weights[1]*((colors[2][i]>>shift)&255)+weather.weights[2]*((colors[3][i]>>shift)&255));
    }
    near((actual>>shift)&255,expected,3,label+" channel="+shift+" row="+y);
   }
  }
 }
 private static void verifyProduction(Context context)throws Exception{
  CinematicScene scene=new CinematicScene(context),reference=new CinematicScene(context);Bitmap output=Bitmap.createBitmap(540,1200,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(output);
  Set<Bitmap> owned=Collections.newSetFromMap(new IdentityHashMap<>());
  try{
   CinematicArtwork cache=(CinematicArtwork)field(scene,"artwork");check(cache.count()==1,"Constructor eagerly decodes full artwork set");
   configure(scene,3);configure(reference,3);scene.setTheme(2);reference.setTheme(2);
   check(cache.images[2][2]!=null&&cache.images[3][2]==null&&cache.images[1][2]==null,"Snow loads irrelevant fog/dry images");
   Bitmap initialSnow=cache.images[2][2];collect(cache,owned);int peak=cache.count();
   for(int tick=0;tick<240;tick++){
    if(tick==15){configure(scene,4);scene.setTheme(0);collect(cache,owned);}
    if(tick==45){configure(scene,0);scene.setTheme(1);collect(cache,owned);}
    if(tick==65){configure(scene,3);scene.setTheme(2);collect(cache,owned);}
    if(tick==100){configure(scene,4);scene.setTheme(1);collect(cache,owned);}
    int attempts=cache.decodeAttempts;scene.update(.05);reference.update(.05);
    scene.draw(canvas);reference.draw(canvas);
    check(cache.decodeAttempts==attempts,"Frame decodes artwork");peak=Math.max(peak,cache.count());
    TrafficModel.Car[] cars=((TrafficModel)field(scene,"traffic")).cars,other=((TrafficModel)field(reference,"traffic")).cars;
    for(int i=0;i<cars.length;i++)near(cars[i].position,other[i].position,0,"Weather/time changes traffic position");
    for(String name:new String[]{"bodyWidths","bodyLengths","groundX","groundY","slopes"})check(Arrays.equals((float[])field(scene,name),(float[])field(reference,name)),"Weather/time changes pose "+name);
   }
   check(cache.count()==2&&cache.images[3][1]!=null,"Settled fog retains retired scene cache");
   check(initialSnow.isRecycled(),"Old snow cache never retired");
   Bitmap activeFog=cache.images[3][1];int attempts=cache.decodeAttempts;configure(scene,4);scene.setTheme(1);
   check(cache.images[3][1]==activeFog&&cache.decodeAttempts==attempts,"Same selection reloads active artwork");
   verifySnowLayers(scene,canvas,output);
   collect(cache,owned);int releaseAttempts=cache.decodeAttempts;scene.release();scene.release();for(Bitmap image:owned)check(image.isRecycled(),"Retired/active bitmap leaked");
   check(cache.count()==0,"Cache remains after release");scene.update(.05);check(cache.decodeAttempts==releaseAttempts,"Released update decodes artwork");
   System.out.println("PASS: production transition peak "+peak+" cached backgrounds; settled sunset fog 2; no per-frame decode");
  }finally{scene.release();reference.release();output.recycle();}
 }
 private static void verifySnowLayers(CinematicScene scene,Canvas canvas,Bitmap output)throws Exception{
  Method snowDraw=CinematicScene.class.getDeclaredMethod("drawSnow",Canvas.class,boolean.class);snowDraw.setAccessible(true);
  configure(scene,3);for(int i=0;i<80;i++)scene.update(.05);
  SnowModel snow=(SnowModel)field(scene,"snow");for(int i=0;i<48;i++){snow.x[i]=40+i*4;snow.y[i]=200+i*4;}
  canvas.drawColor(0,PorterDuff.Mode.SRC);snowDraw.invoke(scene,canvas,false);check(countPixels(output)>0,"Rear snow missing");
  canvas.drawColor(0,PorterDuff.Mode.SRC);snowDraw.invoke(scene,canvas,true);check(countPixels(output)>0,"Foreground snow missing");
  configure(scene,0);scene.update(.05);check(snow.enabled,"Snow motion stops before weather fade completes");
  for(int i=0;i<80;i++)scene.update(.05);check(!snow.enabled,"Snow simulation stays enabled after dry endpoint");
  canvas.drawColor(0,PorterDuff.Mode.SRC);snowDraw.invoke(scene,canvas,false);snowDraw.invoke(scene,canvas,true);check(countPixels(output)==0,"Snow remains after fade");
 }
 private static void verifyFallback()throws Exception{
  Path root=Files.createTempDirectory("cinematic-weather-fallback-");write(root,"cinematic-city.png",0xff962a18,16,36);write(root,"cinematic-city-dry.png",0xff175d7c,16,36);
  Files.write(root.resolve("cinematic-city-snow.png"),new byte[]{1,2,3});write(root,"cinematic-city-fog.png",0xffeeeeee,8,8);
  CinematicScene scene=new CinematicScene(new Context(root));Bitmap output=Bitmap.createBitmap(540,1200,Bitmap.Config.ARGB_8888);
  try{
   for(int weather:new int[]{3,4}){
    configure(scene,weather);for(int i=0;i<80;i++)scene.update(.05);
    check(scene.available()&&scene.surfaceDescription().contains("대체"),"Missing weather art not reported/usable");
    check(sample(scene,output,1000)==0xff175d7c,"Corrupt/mismatched art is not replaced by loaded dry scene");
   }
   CinematicArtwork cache=(CinematicArtwork)field(scene,"artwork");int attempts=cache.decodeAttempts;
   for(int i=0;i<10;i++){scene.update(.05);scene.draw(new Canvas(output));configure(scene,4);}
   check(cache.decodeAttempts==attempts,"Failed optional weather decode retried each frame/selection");
  }finally{scene.release();output.recycle();for(String name:new String[]{"cinematic-city.png","cinematic-city-dry.png","cinematic-city-snow.png","cinematic-city-fog.png"})Files.delete(root.resolve(name));Files.delete(root);}
 }
 private static void collect(CinematicArtwork cache,Set<Bitmap> target){for(Bitmap[] row:cache.images)for(Bitmap image:row)if(image!=null)target.add(image);}
 private static int sample(CinematicScene scene,Bitmap output,int y)throws Exception{Method draw=CinematicScene.class.getDeclaredMethod("drawBackground",Canvas.class);draw.setAccessible(true);draw.invoke(scene,new Canvas(output));int[] pixel=new int[1];output.getPixels(pixel,0,1,100,y,1,1);return pixel[0];}
 private static int countPixels(Bitmap image){int[] pixels=new int[image.getWidth()*image.getHeight()];image.getPixels(pixels,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());int count=0;for(int pixel:pixels)if((pixel>>>24)>0)count++;return count;}
 private static void configure(CinematicScene scene,int weather){scene.configure(6,1.5,0,0,weather,100,0,1,false,false,0);}
 private static void write(Path root,String name,int color,int width,int height)throws Exception{BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);for(int y=0;y<height;y++)for(int x=0;x<width;x++)image.setRGB(x,y,color);ImageIO.write(image,"png",root.resolve(name).toFile());}
 private static Object field(Object object,String name)throws Exception{Field field=object.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(object);}
}
