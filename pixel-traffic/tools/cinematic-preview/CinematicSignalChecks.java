package com.s20plus.pixeltraffic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;

/** Real Scene/pixel integration: traffic phases, measured lamps, spray, and configuration continuity. */
public final class CinematicSignalChecks {
 private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 private static Object field(Object object,String name)throws Exception{Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
 private static void invoke(Object object,String name,Class<?>[] types,Object... args)throws Exception{Method m=object.getClass().getDeclaredMethod(name,types);m.setAccessible(true);m.invoke(object,args);}
 private static Bitmap layer(CinematicScene scene,String method,int index)throws Exception{
  Bitmap image=Bitmap.createBitmap(540,1200,Bitmap.Config.ARGB_8888);
  if(index<0)invoke(scene,method,new Class<?>[]{Canvas.class},new Canvas(image));
  else {Canvas canvas=new Canvas(image);if(method.equals("drawWheelSpray"))canvas.translate(270,600);invoke(scene,method,new Class<?>[]{Canvas.class,int.class},canvas,index);}
  return image;
 }
 public static void main(String[] args)throws Exception{
  Context context=new Context(Path.of(args[0]));
  for(int palette=0;palette<3;palette++){
   CinematicScene scene=new CinematicScene(context);
   try{
    scene.configure(6,1,2,palette,2,100,0,1,true,true,1);scene.setTheme(2);scene.setSignals(true);
    CinematicTraffic driving=(CinematicTraffic)field(scene,"driving");TrafficModel model=(TrafficModel)field(scene,"traffic");
    Bitmap green=layer(scene,"drawSignals",-1);
    for(int tick=0;tick<14*60;tick++)scene.update(1.0/60);
    check(driving.phase()==1,"Scene never turned amber");Bitmap amber=layer(scene,"drawSignals",-1);
    for(int tick=0;tick<10*60;tick++)scene.update(1.0/60);
    check(driving.phase()==2&&driving.waiting()>0,"Scene never held red queue");Bitmap red=layer(scene,"drawSignals",-1);
    check(difference(green,amber)>40&&difference(amber,red)>40,"Signal lamp pixels did not follow phase");
    for(Bitmap image:new Bitmap[]{green,amber,red}){
     for(int y=390;y<1200;y++)for(int x=(int)Math.ceil(CinematicGeometry.left(y)+3);x<CinematicGeometry.right(y)-3&&x<540;x++)
      if(x>=0)check((image.image.getRGB(x,y)>>>24)==0,"Signal pole/lamp lies in a traffic lane");
     image.recycle();
    }
    invoke(scene,"prepareCars",new Class<?>[]{});
    TrafficModel.Car[] order=(TrafficModel.Car[])field(scene,"drawOrder");double[] brakes=(double[])field(driving,"brakes"),velocity=(double[])field(driving,"velocity");
    float[] grounds=(float[])field(scene,"groundY"),widths=(float[])field(scene,"bodyWidths");
    float[] lengths=(float[])field(scene,"bodyLengths");int nearLine=0;
    for(int i=0;i<order.length;i++)if(driving.speed(order[i])<.05&&Math.abs(order[i].position-driving.stopPosition(order[i].lane,order[i].type))<.02){
     float front=order[i].lane<2?grounds[i]:grounds[i]-lengths[i];
     check(Math.abs(front-(order[i].lane<2?810:902))<.02,"Actual atlas/fallback body-front misses stop line");nearLine++;
    }
    check(nearLine>=2,"Need actual body-front stops in both rendering profiles");
    int stoppedAway=-1,stoppedFront=-1;for(int i=0;i<order.length;i++)if(grounds[i]>500&&grounds[i]<1190&&widths[i]>15&&driving.speed(order[i])<.05&&driving.brake(order[i])>.9){if(order[i].lane>=2)stoppedAway=i;else stoppedFront=i;}
    check(stoppedAway>=0&&stoppedFront>=0,"Both directions must be held for integration checks");
    Bitmap tail=layer(scene,"drawCar",stoppedAway),head=layer(scene,"drawCar",stoppedFront),reflection=layer(scene,"reflections",stoppedAway);
    double[] saved=brakes.clone();java.util.Arrays.fill(brakes,0);
    Bitmap tailNormal=layer(scene,"drawCar",stoppedAway),headNormal=layer(scene,"drawCar",stoppedFront),normalReflection=layer(scene,"reflections",stoppedAway);
    check(difference(tail,tailNormal)>10,"Held tail lamps failed to brighten: palette="+palette+" diff="+difference(tail,tailNormal)+" y="+grounds[stoppedAway]+" lane="+order[stoppedAway].lane+" brake="+saved[order[stoppedAway].lane*6]+" pixels="+nonempty(tail));check(difference(head,headNormal)==0,"Braking changed front headlights");
    check(difference(reflection,normalReflection)>30,"Brake reflection failed to change");System.arraycopy(saved,0,brakes,0,saved.length);
    Bitmap stoppedSpray=layer(scene,"drawWheelSpray",stoppedAway);check(nonempty(stoppedSpray)==0,"Stopped wheels still spray");
    int carIndex=-1;for(int i=0;i<model.cars.length;i++)if(model.cars[i]==order[stoppedAway])carIndex=i;
    double savedVelocity=velocity[carIndex];velocity[carIndex]=70;
    Bitmap movingSpray=layer(scene,"drawWheelSpray",stoppedAway);check(nonempty(movingSpray)>0,"Moving wheels lost spray");velocity[carIndex]=savedVelocity;
    for(Bitmap image:new Bitmap[]{tail,head,reflection,tailNormal,headNormal,normalReflection,stoppedSpray,movingSpray})image.recycle();
    // Non-traffic settings and repeated preference notifications must preserve actual car/clock state.
    double phaseTime=driving.seconds();double[] positions=new double[24];for(int i=0;i<24;i++)positions[i]=model.cars[i].position;
    scene.configure(6,1,2,palette,4,80,1,2,false,false,0);scene.setTheme(0);scene.setSignals(true);
    check(field(scene,"traffic")==model&&field(scene,"driving")==driving,"Unrelated settings replaced traffic");
    check(driving.seconds()==phaseTime,"Unrelated settings restarted signal");for(int i=0;i<24;i++)check(positions[i]==model.cars[i].position,"Unrelated settings moved car");
    Bitmap output=Bitmap.createBitmap(540,1200,Bitmap.Config.ARGB_8888);scene.draw(new Canvas(output),540,1200);scene.draw(new Canvas(output),540,1200);output.recycle();
    check(driving.seconds()==phaseTime,"Drawing advanced traffic clock");
    scene.setSignals(false);Bitmap off=layer(scene,"drawSignals",-1);check(nonempty(off)==0,"Disabled signals remain visible");off.recycle();
    for(int tick=0;tick<20*60;tick++)scene.update(1.0/60);check(driving.waiting()==0,"Disabled Scene queue failed to resume");
   }finally{scene.release();}
  }
  System.out.println("PASS: actual atlas and two fallback palettes; green/amber/red sidewalk pixels, real held queues, brake-tail/reflection pixels, unchanged headlights, stationary/moving spray and settings/draw continuity");
 }
 private static int difference(Bitmap a,Bitmap b){int count=0;for(int y=0;y<a.getHeight();y++)for(int x=0;x<a.getWidth();x++)if(a.image.getRGB(x,y)!=b.image.getRGB(x,y))count++;return count;}
 private static int nonempty(Bitmap image){int count=0;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)if((image.image.getRGB(x,y)>>>24)>0)count++;return count;}
}
