package com.s20plus.pixeltraffic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/** Checks actual desktop compositions, keeping Android and GPU claims separate. */
public final class CinematicRenderChecks {
 private static long cases;
 public static void main(String[] args)throws Exception {
  if(args.length!=1)throw new IllegalArgumentException("Expected assets-directory");
  Context context=new Context(Path.of(args[0]));
  verifyVehicleFootprints(context);
  CinematicScene scene=new CinematicScene(context);
  try {
   check(scene.available(),"Required cinematic asset failed to load");
   // Phone portrait, wider desktop viewport, short preview card, and near-square viewport.
   int[][] sizes={{540,1200},{1080,2400},{960,540},{360,800},{591,350},{800,800}};
   for(int theme=0;theme<3;theme++){
   scene.setTheme(theme);
   for(int sizeIndex=0;sizeIndex<sizes.length;sizeIndex++) {
    int[] size=sizes[sizeIndex];
    for(int weather=0;weather<5;weather++) {
     int camera=weather%2,position=weather%3;
     scene.configure(weather%2==0?6:3,weather%2==0?1.5:.5,weather%4,weather%3,weather,
      weather==4?60:100,camera,position,true,true,2);
     for(int tick=0;tick<180;tick++)scene.update(1.0/30);
     verifyFrame(scene,size[0],size[1],"theme "+theme+" viewport "+size[0]+"x"+size[1]+" weather "+weather);
    }
   }
   // All close-camera positions, a nonstandard viewport, low density, and disabled effects.
   for(int position=0;position<3;position++) {
    scene.configure(1,1,0,0,0,100,1,position,false,false,0);
    scene.update(.1);verifyFrame(scene,731,1499,"close-camera position "+position);
   }
   }
  } finally {scene.release();}
  // A missing or undecodable optional illustration must leave the scene unavailable for fallback.
  Path empty=Files.createTempDirectory("cinematic-assets-missing-");
  try {
   verifyUnavailable(empty,"missing illustration");
   Files.write(empty.resolve("cinematic-city.png"),new byte[]{1,2,3,4});
   verifyUnavailable(empty,"undecodable illustration");
  } finally {Files.deleteIfExists(empty.resolve("cinematic-city.png"));Files.delete(empty);}
  check(BitmapFactory.decodeStream(new ByteArrayInputStream(new byte[]{0,1,2}))==null,"Bad image decode must return null");
  // The injected desktop bitmap is scene-owned, matching the production release contract.
  try(InputStream stream=context.getAssets().open("cinematic-city.png")) {
   Bitmap image=BitmapFactory.decodeStream(stream);check(image!=null,"Decode fixture for injection");
   CinematicScene injected=new CinematicScene(image);check(injected.available(),"Injected asset available");
   injected.release();check(image.isRecycled(),"Scene release should recycle its owned bitmap");
  }
  System.out.println("PASS: "+cases+" production CinematicScene desktop composites, opaque/repeated/advanced frames, viewport/camera/weather/density/brightness variants, actual-asset lane/scale/ground footprints, missing/corrupt assets and owned asset release (not Android/GPU tests)");
 }
 private static void verifyVehicleFootprints(Context context)throws Exception{
  CinematicVehicles atlas=new CinematicVehicles(context);
  CinematicScene scene=new CinematicScene(context);
  Bitmap output=Bitmap.createBitmap(800,1450,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(output);
  int[] row=new int[800];int poses=0;
  Method prepare=CinematicScene.class.getDeclaredMethod("prepareCars");prepare.setAccessible(true);
  Method drawCar=CinematicScene.class.getDeclaredMethod("drawCar",Canvas.class,int.class);drawCar.setAccessible(true);
  try{
   check(atlas.available(),"Actual vehicle atlas must load for lane checks");
   // A palette round trip also catches a stale atlas profile after fallback rendering.
   for(int palette:new int[]{0,1,2,0}){
    scene.configure(1,1,0,palette,0,100,0,1,false,false,0);
    Rect[][] bounds=(Rect[][])field(scene,"spriteBounds");
    float[][] aspects=new float[6][2];
    for(int type=0;type<6;type++)for(int direction=0;direction<2;direction++){
     Rect source=bounds[type][direction];
     aspects[type][direction]=palette==0?atlas.bodyLength(type,direction==1,1):source.height()/(float)source.width();
    }
    VehicleLayout expected=new VehicleLayout(aspects),actual=scene.vehicleLayout();
    if(poses==0||palette!=0)VehicleLayoutChecks.verify(actual,aspects,"actual Scene palette "+palette);
    TrafficModel traffic=(TrafficModel)field(scene,"traffic");
    for(float ground:new float[]{500,700,800,840,900,1100,1240})for(int type=0;type<6;type++){
     double original=CinematicGeometry.inverseModelY(ground);
     for(TrafficModel.Car car:traffic.cars){
      car.type=type;car.position=car.lane<2?(original-160)/.65:(940-original)/.65;
     }
     prepare.invoke(scene);
     TrafficModel.Car[] order=(TrafficModel.Car[])field(scene,"drawOrder");
     float[] widths=(float[])field(scene,"bodyWidths"),lengths=(float[])field(scene,"bodyLengths"),slopes=(float[])field(scene,"slopes");
     float[] grounds=(float[])field(scene,"groundY");
     for(int index=0;index<order.length;index++){
      int lane=order[index].lane;
      String pose="palette="+palette+" lane="+lane+" type="+type+" ground="+ground;
      float width=expected.width(lane,type,ground),length=expected.length(lane,type,ground);
      near(grounds[index],ground,.003f,"Production ground moved: "+pose);
      near(widths[index],width,.003f,"Production width bypasses actual-source profile: "+pose);
      near(lengths[index],length,.003f,"Production length bypasses actual-source profile: "+pose);
      near(slopes[index],expected.slope(lane,type,ground),.00002f,"Production heading bypasses whole-body profile: "+pose);
      near(actual.width(lane,type,ground),width,.003f,"Scene selected stale profile: "+pose);
      canvas.drawColor(0,PorterDuff.Mode.SRC);canvas.save();canvas.translate(130,0);
      drawCar.invoke(scene,canvas,index);canvas.restore();
      int bottom=-1,largestRow=0,pixels=0;
      for(int y=Math.max(0,(int)(ground-length)-2);y<=Math.min(1449,(int)ground+2);y++){
       output.getPixels(row,0,800,0,y,800,1);int left=800,right=-1;
       for(int x=0;x<800;x++)if((row[x]>>>24)>200){
        left=Math.min(left,x);right=Math.max(right,x);bottom=Math.max(bottom,y);pixels++;
        float roadX=x+.5f-130,roadY=y+.5f;
        if(roadY<CinematicGeometry.HORIZON||roadY>1200||roadX<0||roadX>540)continue;
        check(roadX>=CinematicGeometry.boundary(lane,roadY)-1.5f&&roadX<=CinematicGeometry.boundary(lane+1,roadY)+1.5f,
         "Production opaque artwork crosses lane: "+pose+" pixel="+roadX+","+roadY);
       }
       if(right>=left)largestRow=Math.max(largestRow,right-left+1);
      }
      check(pixels>width*length*.25,"Production vehicle body disappeared or source was truncated: "+pose);
      check(largestRow>=width*.8,"Production vehicle is smaller than requested profile width: "+pose);
      check(Math.abs(bottom+1-ground)<=2,"Production vehicle body is not grounded: "+pose);
      poses++;
     }
    }
   }
   System.out.println("PASS: "+poses+" production Scene atlas/fallback footprints use actual-source profiles, stay in lanes, occupy requested width, and end at ground points across palette round trip");
  }finally{scene.release();atlas.release();output.recycle();}
 }
 private static Object field(Object target,String name)throws Exception{
  Field value=target.getClass().getDeclaredField(name);value.setAccessible(true);return value.get(target);
 }
 private static void near(float actual,float expected,float tolerance,String label){
  check(Math.abs(actual-expected)<=tolerance,label+": "+actual+" != "+expected);
 }
 private static void verifyUnavailable(Path root,String label) {
  CinematicScene unavailable=new CinematicScene(new Context(root));
  try {check(!unavailable.available(),label+" must offer fallback");}
  finally {unavailable.release();}
 }
 private static void verifyFrame(CinematicScene scene,int width,int height,String label) {
  Bitmap output=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);Canvas target=new Canvas(output);
  try {
   scene.draw(target,width,height);
   int[] first=output.image.getRGB(0,0,width,height,null,0,width);
   scene.draw(target,width,height);
   int[] second=output.image.getRGB(0,0,width,height,null,0,width);
   check(Arrays.equals(first,second),"Drawing twice without updating changes "+label);
   int variation=0;
   for(int pixel:first) {
    check((pixel>>>24)==255,"Transparent final pixel in "+label);
    if(pixel!=first[0])variation++;
   }
   check(variation>width*height/5,"Most artwork disappeared in "+label);
   // A narrow bottom crop can contain no vehicles for a few seconds. Check a moving
   // layer enters within a traffic interval, without requiring motion in every crop/frame.
   boolean changed=false;
   for(int attempt=0;attempt<30&&!changed;attempt++){
    for(int i=0;i<12;i++)scene.update(1.0/30);
    scene.draw(target,width,height);
    int[] advanced=output.image.getRGB(0,0,width,height,null,0,width);
    changed=!Arrays.equals(first,advanced);
   }
   check(changed,"Traffic/time advancement made no visible change within twelve seconds in "+label);
   cases++;
  } finally {output.recycle();}
 }
 private static void check(boolean condition,String label) {if(!condition)throw new AssertionError(label);}
}
