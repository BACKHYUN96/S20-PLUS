package com.s20plus.pixeltraffic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;

/** Source anchoring, exact body-mask alpha, effect continuity, and production wet/dry rendering. */
public final class CinematicEffectChecks {
 private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
 private static void near(double a,double b,double error,String reason){check(Math.abs(a-b)<=error,reason+": "+a+" != "+b);}
 public static void main(String[] args)throws Exception{
  check(args.length==1,"Expected assets directory");
  verifyParameters();verifySourceFixtures();verifyScene(new Context(Path.of(args[0])));
  System.out.println("PASS: lighting/rain continuity, independently placed source lamps, exact-alpha wash, real atlas/fallback anchors, wet/dry spray, active-time and release");
 }
 private static void verifyParameters(){
  int samples=0;
  for(float y=390;y<=1600;y+=.5f)for(int weather=0;weather<5;weather++){
   float previous=CinematicEffects.reflectionLength(60,y,weather,false),next=CinematicEffects.reflectionLength(60,y+.5f,weather,false);
   check(Float.isFinite(previous)&&previous>0&&next>=previous-.001f&&next-previous<.2f,"Reflection depth jumps");
   check(CinematicEffects.reflectionAlpha(y,weather)>=0&&CinematicEffects.reflectionAlpha(y,weather)<=255,"Reflection alpha out of bounds");
   check(CinematicEffects.rainScale(y)>=.28f&&CinematicEffects.rainScale(y)<=1.401f,"Rain scale out of bounds");
   for(int lane=0;lane<4;lane++){
    float exposure=CinematicEffects.streetExposure(y,lane);
    check(exposure>=0&&exposure<=1,"Street exposure out of bounds");
    check(Math.abs(exposure-CinematicEffects.streetExposure(y+.5f,lane))<.015f,"Street exposure jumps");
   }
   samples++;
  }
  check(CinematicEffects.reflectionLength(60,1000,2,false)>CinematicEffects.reflectionLength(60,1000,0,false),"Strong rain must lengthen reflection");
  check(CinematicEffects.reflectionAlpha(1000,2)>CinematicEffects.reflectionAlpha(1000,1),"Strong rain must strengthen reflection");
  check(CinematicEffects.rainScale(1100)>CinematicEffects.rainScale(500)*2,"Near rain must be larger");
  for(double hit=290;hit<=785;hit+=5){
   near(CinematicEffects.rainY(hit,hit),CinematicGeometry.modelY(hit),.001,"Rain misses its splash ground");
   check(CinematicEffects.rainY(hit-1,hit)<CinematicEffects.rainY(hit,hit),"Rain falls away from its ground");
  }
  for(int level=0;level<=2;level++)for(int i=0;i<=1000;i++){
   int alpha=CinematicEffects.sprayAlpha(i/1000f,level);
   check(alpha>=0&&alpha<=88&&(level!=0||alpha==0),"Spray intensity/weather bounds");
  }
  near(CinematicEffects.sprayPhase(119.999,17,2),CinematicEffects.sprayPhase(-.001,17,2),.00001,"Bounded spray clock wrap");
  System.out.println("PASS: "+samples+" depth/weather effect samples and 100 rain landings");
 }
 private static void verifySourceFixtures(){
  for(boolean away:new boolean[]{false,true}){
   Bitmap image=Bitmap.createBitmap(100,150,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);Paint ink=new Paint();
   ink.setColor(away?0xffff3344:0xffffe8b0);
   canvas.drawRect(15,120,21,124,ink);canvas.drawRect(77,129,83,133,ink);
   canvas.drawRect(40,40,60,65,ink); // bright roof/taxi sign must not attract either lamp
   VehicleLights lights=new VehicleLights(image,new Rect(0,0,100,150),away);
   check(lights.measured[0]&&lights.measured[1],"Fixture lamps not found");
   near(lights.x[0],-.32,.001,"Left source X");near(lights.x[1],.30,.001,"Right source X");
   near(lights.y[0],122.0/150-1,.001,"Left source Y");near(lights.y[1],131.0/150-1,.001,"Right source Y");
   int[] source=new int[15000],mask=new int[15000];
   image.getPixels(source,0,100,0,0,100,150);lights.wash.getPixels(mask,0,100,0,0,100,150);
   for(int i=0;i<source.length;i++)check((mask[i]>>>24)<=(source[i]>>>24),"Wash extends beyond source alpha");
   lights.release();lights.release();check(lights.wash.isRecycled(),"Wash ownership not released");image.recycle();
  }
 }
 private static void verifyScene(Context context)throws Exception{
  CinematicVehicles atlas=new CinematicVehicles(context);check(atlas.available(),"Real atlas missing");
  for(int type=0;type<6;type++)for(boolean away:new boolean[]{false,true}){
   VehicleLights lights=atlas.lights(type,away);
   check(lights.measured[0]&&lights.measured[1],"Real atlas lamp missing type="+type+" away="+away);
   check(lights.x[0]<-.1&&lights.x[1]>.1&&lights.y[0]<0&&lights.y[1]<0,"Real source anchors invalid");
  }
  VehicleLights owned=atlas.lights(4,false);atlas.release();check(owned.wash.isRecycled(),"Atlas wash not released");
  CinematicScene scene=new CinematicScene(context);Bitmap output=Bitmap.createBitmap(540,1200,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(output);
  Method prepare=CinematicScene.class.getDeclaredMethod("prepareCars");prepare.setAccessible(true);
  Method spray=CinematicScene.class.getDeclaredMethod("drawWheelSpray",Canvas.class,int.class);spray.setAccessible(true);
  Method reflections=CinematicScene.class.getDeclaredMethod("reflections",Canvas.class,int.class);reflections.setAccessible(true);
  try{
   scene.configure(1,1,0,0,0,100,0,1,false,false,0);seed(scene,1000);prepare.invoke(scene);
   canvas.drawColor(0,PorterDuff.Mode.SRC);spray.invoke(scene,canvas,0);
   check(countPixels(output)==0,"Dry scene produces wheel spray");
   scene.update(.05);near(number(scene,"rainSeconds"),0,0,"Dry rain clock advances");
   reflections.invoke(scene,canvas,0);int[] dry=copy(output);
   scene.configure(1,1,0,0,2,100,0,1,false,false,0);
   canvas.drawColor(0,PorterDuff.Mode.SRC);reflections.invoke(scene,canvas,0);
   check(Arrays.equals(dry,copy(output)),"Weather selection jumps reflection before active transition");
   for(int i=0;i<80;i++)scene.update(.05);
   canvas.drawColor(0,PorterDuff.Mode.SRC);reflections.invoke(scene,canvas,0);
   check(!Arrays.equals(dry,copy(output)),"Settled strong-rain reflection equals dry rendering");
   double wetClock=number(scene,"rainSeconds");scene.update(.05);near(number(scene,"rainSeconds"),wetClock+.05,.000001,"Rain clock not active");
   seed(scene,1000);prepare.invoke(scene);canvas.drawColor(0,PorterDuff.Mode.SRC);canvas.save();canvas.translate(270,1100);
   spray.invoke(scene,canvas,0);canvas.restore();check(countPixels(output)>0,"Wet production wheel spray is invisible");
   double clock=number(scene,"rainSeconds");scene.draw(canvas);int[] first=copy(output);scene.draw(canvas);
   check(Arrays.equals(first,copy(output)),"Effects advance during draw instead of update");
   near(number(scene,"rainSeconds"),clock,0,"Draw advances effect clock");
   scene.update(Double.NaN);scene.update(-1);near(number(scene,"rainSeconds"),clock,0,"Invalid elapsed time advances effects");
   scene.configure(1,1.5,0,0,2,100,0,1,false,false,0);
   double sprayClock=number(scene,"spraySeconds");scene.update(.04);
   near(number(scene,"spraySeconds"),sprayClock+.06,.000001,"Spray clock ignores vehicle speed");
   scene.configure(1,1,0,0,0,100,0,1,false,false,0);clock=number(scene,"rainSeconds");
   scene.update(.05);check(number(scene,"rainSeconds")>clock,"Residual wet surface stops effects too early");
   for(int i=0;i<80;i++)scene.update(.05);
   near(scene.surfaceWetness(),0,0,"Clear surface did not dry");
   clock=number(scene,"rainSeconds");scene.update(.05);near(number(scene,"rainSeconds"),clock,0,"Fully dry mode advances wet effects");
   Method drawRain=CinematicScene.class.getDeclaredMethod("drawRain",Canvas.class,boolean.class,boolean.class);drawRain.setAccessible(true);
   canvas.drawColor(0,PorterDuff.Mode.SRC);drawRain.invoke(scene,canvas,false,true);
   check(countPixels(output)==0,"Dry mode draws foreground rain");
   scene.configure(1,1,0,0,2,100,0,1,false,false,0);
   RainModel rain=(RainModel)field(scene,"rain");
   for(RainModel.Drop drop:rain.drops){drop.splashTicks=0;drop.hitY=700;drop.y=650;drop.x=250;}
   canvas.drawColor(0,PorterDuff.Mode.SRC);drawRain.invoke(scene,canvas,false,false);
   check(countPixels(output)==0,"Near rain drawn in far layer");
   drawRain.invoke(scene,canvas,false,true);check(countPixels(output)>0,"Near rain missing from foreground layer");
   for(RainModel.Drop drop:rain.drops){drop.hitY=350;drop.y=330;}
   canvas.drawColor(0,PorterDuff.Mode.SRC);drawRain.invoke(scene,canvas,false,true);
   check(countPixels(output)==0,"Far rain drawn in foreground layer");
   drawRain.invoke(scene,canvas,false,false);check(countPixels(output)>0,"Far rain missing from rear layer");
   for(int palette=1;palette<=2;palette++){
    scene.configure(1,1,0,palette,2,100,0,1,false,false,0);
    VehicleLights[][] lights=(VehicleLights[][])field(scene,"spriteLights");
    for(VehicleLights[] pair:lights)for(VehicleLights light:pair){
     check(!light.wash.isRecycled(),"Fallback wash already recycled");
     check(light.measured[0]&&light.measured[1],"Fallback source lamps not measured");
    }
    scene.draw(canvas);
   }
   VehicleLights[][] lights=(VehicleLights[][])field(scene,"spriteLights");scene.release();scene.release();
   for(VehicleLights[] pair:lights)for(VehicleLights light:pair)check(light.wash.isRecycled(),"Scene wash not released");
   clock=number(scene,"rainSeconds");scene.update(.05);near(number(scene,"rainSeconds"),clock,0,"Released effect clock advances");
  }finally{scene.release();output.recycle();}
 }
 private static void seed(CinematicScene scene,float ground)throws Exception{
  double original=CinematicGeometry.inverseModelY(ground);
  for(TrafficModel.Car car:((TrafficModel)field(scene,"traffic")).cars)car.position=car.lane<2?(original-160)/.65:(940-original)/.65;
 }
 private static int[] copy(Bitmap bitmap){int[] result=new int[bitmap.getWidth()*bitmap.getHeight()];bitmap.getPixels(result,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());return result;}
 private static int countPixels(Bitmap bitmap){int count=0;for(int pixel:copy(bitmap))if((pixel>>>24)>0)count++;return count;}
 private static Object field(Object object,String name)throws Exception{Field field=object.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(object);}
 private static double number(Object object,String name)throws Exception{return ((Number)field(object,name)).doubleValue();}
}
