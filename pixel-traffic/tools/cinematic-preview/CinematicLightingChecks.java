package com.s20plus.pixeltraffic;

import android.content.Context;
import android.graphics.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;

/** CCT references, independent two-lamp fixtures and real-source/pixel/lifetime integration. */
public final class CinematicLightingChecks {
 private static void check(boolean value,String why){if(!value)throw new AssertionError(why);}
 private static void near(double a,double b,double e,String why){check(Math.abs(a-b)<=e,why+": "+a+" / "+b);}
 private static Object field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
 private static void invoke(Object o,String name,Class<?>[] types,Object... args)throws Exception{Method m=o.getClass().getDeclaredMethod(name,types);m.setAccessible(true);m.invoke(o,args);}
 public static void main(String[] args)throws Exception{
  parameters();fixtures();realSources(new Context(Path.of(args[0])));scenePixels(new Context(Path.of(args[0])));
  System.out.println("PASS: CCT reference/profile/continuous gains; independent head+fog fixtures; 12 atlas lamp pairs and all 3 palettes; lamp-mask silhouette/lifetime, road-only bidirectional beams/CCT/fog output, wet ripples, dry/forward-projection contracts and draw/settings/traffic continuity");
 }
 private static void parameters(){
  // Published daylight/Planckian white points yield these display sRGB ranges (not phone measurements).
  int[] a={VehicleLighting.color(3000),VehicleLighting.color(4500),VehicleLighting.color(6500)};
  check((a[0]>>16&255)==255&&(a[0]>>8&255)>=180&&(a[0]>>8&255)<=187&&(a[0]&255)>=104&&(a[0]&255)<=114,"3000K reference");
  check((a[1]>>16&255)==255&&(a[1]>>8&255)>=223&&(a[1]>>8&255)<=230&&(a[1]&255)>=180&&(a[1]&255)<=191,"4500K reference");
  check(Math.min(a[2]>>16&255,Math.min(a[2]>>8&255,a[2]&255))>=252,"6500K neutral daylight LED");
  int[] head={6500,6500,6500,6500,4500,4500},fog={0,6500,0,0,3000,3000};
  for(int i=0;i<6;i++){check(VehicleLighting.PROFILES[i].headKelvin==head[i]&&VehicleLighting.PROFILES[i].fogKelvin==fog[i],"Requested type profile "+i);}
  double[] night={0,0,1},day={1,0,0},mix={.2,.3,.5};int samples=0;
  for(float depth=0;depth<=1;depth+=.002f)for(float weather=0;weather<=1;weather+=.05f){
   int beam=VehicleLighting.beamAlpha(depth,mix,weather,1.14f),halo=VehicleLighting.haloAlpha(depth,mix,weather,1.14f);
   check(beam>=0&&beam<65&&halo>=0&&halo<220,"Bounded glare");
   check(VehicleLighting.beamAlpha(depth,night,weather,1)>VehicleLighting.beamAlpha(depth,day,weather,1),"Day cone not restrained");
   check(VehicleLighting.beamLength(60,depth,true)<VehicleLighting.beamLength(60,depth,false),"Fog beam must be short/wide");
   samples++;
  }
  check(VehicleLighting.beamAlpha(1,night,1,1)<VehicleLighting.beamAlpha(1,night,0,1),"Fog must shorten/reduce distant road glare");
  check(VehicleLighting.haloAlpha(1,night,1,1)>VehicleLighting.haloAlpha(1,night,0,1),"Fog halo response");
  LightTextures textures=new LightTextures();for(int i=0;i<3;i++){
   check(textures.colors[i]==a[i],"Texture/profile CCT mismatch");
   check((textures.beam[i].image.getRGB(0,0)>>>24)==0&&(textures.beam[i].image.getRGB(32,32)>>>24)>0,"Cutoff/feathered cone");
   Bitmap fan=textures.beam[i];
   check(rowWidth(fan,115)>rowWidth(fan,60)*1.4&&rowWidth(fan,60)>rowWidth(fan,25)*1.4,"Headlight footprint does not widen into a fan");
   check((fan.image.getRGB(40,64)>>>24)>(fan.image.getRGB(32,64)>>>24)*.45,"Fan collapsed into a thin reflection stripe");
   check((fan.image.getRGB(32,176)>>>24)>(fan.image.getRGB(51,176)>>>24),"Fan cap not rounded/faded");
   for(int y=0;y<fan.getHeight();y++)for(int x=0;x<fan.getWidth()/2;x++)check((fan.image.getRGB(x,y)>>>24)==(fan.image.getRGB(fan.getWidth()-1-x,y)>>>24),"Lamp sector not symmetric");
   for(int x=0;x<fan.getWidth();x++)check((fan.image.getRGB(x,191)>>>24)==0,"Headlight far edge cuts off abruptly");
  }
  textures.release();textures.release();for(Bitmap[] set:new Bitmap[][]{textures.glow,textures.trail,textures.beam,textures.fogBeam})for(Bitmap image:set)check(image.isRecycled(),"Texture release");
  System.out.println("PASS: "+samples+" lighting depth/weather parameter samples; CCT 3000/4500/6500 reference ranges");
 }
 private static void fixtures(){
  Bitmap image=Bitmap.createBitmap(100,200,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);Paint p=new Paint();
  p.setColor(0xffffe8b0);canvas.drawRect(15,150,21,156,p);canvas.drawRect(77,152,83,158,p);
  p.setColor(0xffecc078);canvas.drawRect(17,180,22,184,p);canvas.drawRect(75,182,80,186,p);
  p.setColor(0xffffe8b0);canvas.drawRect(25,160,27,162,p);canvas.drawRect(40,40,60,60,p); // head fragment/roof decoys
  for(int type:new int[]{1,5}){
   VehicleLights l=new VehicleLights(image,new Rect(0,0,100,200),false,type);
   check(l.measured[0]&&l.measured[1]&&l.fogMeasured[0]&&l.fogMeasured[1],"Two distinct lamp pairs not measured");
   near(l.x[0],-.32,.001,"Independent main centroid");near(l.y[0],-.235,.001,"Main centroid mixed with fog");
   near(l.fogX[0],-.305,.001,"Fog centroid");near(l.fogY[0],-.09,.001,"Fog vertical anchor");
   int main=l.emission.image.getRGB(17,152),fog=l.emission.image.getRGB(19,182);
   if(type==1)check((main>>16&255)==(main&255)&&(fog>>16&255)==(fog&255),"Sport dual LED mask");
   else check((main&255)/(double)(main>>16&255)>.65&&(fog&255)/(double)(fog>>16&255)<.5,"Truck two separate CCT masks");
   check(l.emission.image.getRGB(26,161)==0&&l.emission.image.getRGB(45,45)==0,"Decoy acquired lamp emission");
   int[] source=copy(image),mask=copy(l.emission);for(int i=0;i<mask.length;i++)check((mask[i]>>>24)<=(source[i]>>>24),"Emission leaks original silhouette");
   l.release();l.release();check(l.emission.isRecycled()&&l.wash.isRecycled(),"Both masks must release");
  }
  Bitmap uniform=Bitmap.createBitmap(540,1200,Bitmap.Config.ARGB_8888);new Canvas(uniform).drawColor(0xffffddaa,PorterDuff.Mode.SRC);
  check(new CityLightAnchors(uniform).count==0,"Uniform art fabricated window lights");uniform.recycle();image.recycle();
 }
 private static void realSources(Context context)throws Exception{
  CinematicVehicles atlas=new CinematicVehicles(context);Bitmap source=(Bitmap)field(atlas,"atlas");Rect[] bounds=(Rect[])field(atlas,"sources");
  for(int type=0;type<6;type++)for(boolean away:new boolean[]{false,true}){
   VehicleLights l=atlas.lights(type,away);check(l.measured[0]&&l.measured[1],"Actual main lamps type="+type);
   if(away){check(l.emission==null,"Rear lamp recolored as a headlight");continue;}
   if(type==1||type>=4)for(int side=0;side<2;side++){
    check(l.fogMeasured[side]&&l.fogY[side]>l.y[side]+.04f,"Actual fog lamp type="+type+" side="+side);
    if(type==1)check(l.fogY[side]>-.145f&&l.fogY[side]<-.035f,"Sport fog mistakenly placed inside main lamp");
   }
   Rect b=bounds[type*2];int[] original=new int[b.width()*b.height()];source.getPixels(original,0,b.width(),b.left,b.top,b.width(),b.height());
   int[] emission=copy(l.emission);int heads=0,fogs=0;
   int mainColor=VehicleLighting.color(VehicleLighting.PROFILES[type].headKelvin),fogColor=VehicleLighting.color(VehicleLighting.PROFILES[type].fogKelvin==0?6500:VehicleLighting.PROFILES[type].fogKelvin);
   for(int i=0;i<emission.length;i++){
    check((emission[i]>>>24)<=(original[i]>>>24),"Real emission alpha outside source");
    if((emission[i]>>>24)==0)continue;
    if(sameTint(emission[i],mainColor))heads++;if(sameTint(emission[i],fogColor))fogs++;
   }
   check(heads>20,"Actual head mask missing CCT "+type);if(type>=4)check(fogs>4,"Actual commercial fog mask missing 3000K "+type);
  }
  VehicleLights owned=atlas.lights(5,false);atlas.release();check(owned.emission.isRecycled(),"Actual emission mask leaked");
 }
 private static void scenePixels(Context context)throws Exception{
  for(int palette=0;palette<3;palette++){
   CinematicScene scene=new CinematicScene(context);Bitmap output=Bitmap.createBitmap(540,1200,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(output);
   try{
    scene.configure(1,1,0,palette,0,100,0,1,false,false,0);scene.setTheme(2);
    double[] energy=new double[6];
    for(int type=0;type<6;type++){
     seed(scene,900,type);invoke(scene,"prepareCars",new Class<?>[]{});canvas.drawColor(0,PorterDuff.Mode.SRC);
     invoke(scene,"drawRoadLighting",new Class<?>[]{Canvas.class},canvas);int[] pixels=copy(output);int nonzero=0;long red=0,green=0,blue=0;
     for(int y=0;y<1200;y++)for(int x=0;x<540;x++){
      int p=pixels[y*540+x],a=p>>>24;if(a==0)continue;nonzero++;energy[type]+=a;red+=(long)(p>>16&255)*a;green+=(long)(p>>8&255)*a;blue+=(long)(p&255)*a;
      check(x+.5>=CinematicGeometry.left(y)&&x+.5<=CinematicGeometry.right(y),"Road beam spills onto sidewalk");
      if(type<4)check(Math.abs((p>>16&255)-(p&255))<=2,"LED beam inherited amber tint");
      else if(a>=16)check((p>>16&255)>=(p>>8&255)&&(p>>8&255)>=(p&255)&&((p>>16&255)-(p&255))>25,"Truck/bus beam not warm (visible alpha="+a+")"); // Transparent-edge RGB is quantized by premultiplication.
     }
     check(nonzero>500,"Production road beam invisible");
     if(type>=4)check((red-green)/energy[type]>12&&(green-blue)/energy[type]>20,"Integrated commercial road light lost requested warm CCT");
     if(type==1){
      // Isolate actual fog output; comparing different cars confounds head power and source anchors.
      LightTextures textures=(LightTextures)field(scene,"lamps");int fogIndex=VehicleLighting.PROFILES[1].fogIndex;
      Bitmap original=textures.fogBeam[fogIndex],blank=Bitmap.createBitmap(original.getWidth(),original.getHeight(),Bitmap.Config.ARGB_8888);
      try{
       textures.fogBeam[fogIndex]=blank;canvas.drawColor(0,PorterDuff.Mode.SRC);invoke(scene,"drawRoadLighting",new Class<?>[]{Canvas.class},canvas);
       check(energy[type]-energy(copy(output))>100,"Sport fog pair does not contribute a real road pool");
      }finally{textures.fogBeam[fogIndex]=original;blank.recycle();}
     }
    }
    TrafficModel model=(TrafficModel)field(scene,"traffic");
    for(TrafficModel.Car car:model.cars)if(car.lane>=2)car.position=1100;
    invoke(scene,"prepareCars",new Class<?>[]{});canvas.drawColor(0,PorterDuff.Mode.SRC);invoke(scene,"drawRoadLighting",new Class<?>[]{Canvas.class},canvas);
    int[] nearPixels=copy(output);check(energy(nearPixels)>100,"Approaching car has no forward road light");
    for(int y=0;y<899;y++)for(int x=0;x<540;x++)check((nearPixels[y*540+x]>>>24)==0,"Approaching headlights point behind the car");
    seed(scene,900,5);for(TrafficModel.Car car:model.cars)if(car.lane<2)car.position=0;
    invoke(scene,"prepareCars",new Class<?>[]{});canvas.drawColor(0,PorterDuff.Mode.SRC);invoke(scene,"drawRoadLighting",new Class<?>[]{Canvas.class},canvas);
    int[] awayPixels=copy(output);long awayEnergy=energy(awayPixels);
    check(awayEnergy>100,"Away-facing car has no forward road light");
    for(int y=0;y<1200;y++)for(int x=0;x<540;x++)if((awayPixels[y*540+x]>>>24)>0)
     check(y<900&&x+.5>=CinematicGeometry.left(y)&&x+.5<=CinematicGeometry.right(y),"Away headlights project backwards or onto sidewalk");
    // Reproduce the user's two white sedans stopped at the same painted line.
    CinematicTraffic driving=(CinematicTraffic)field(scene,"driving");
    for(TrafficModel.Car car:model.cars){car.type=0;car.position=driving.stopPosition(car.lane,0);}
    invoke(scene,"prepareCars",new Class<?>[]{});
    TrafficModel.Car[] ordered=(TrafficModel.Car[])field(scene,"drawOrder");
    float[] widths=(float[])field(scene,"bodyWidths"),lengths=(float[])field(scene,"bodyLengths"),grounds=(float[])field(scene,"groundY");
    int left=-1,right=-1;for(int i=0;i<ordered.length;i++){if(ordered[i].lane==0)left=i;if(ordered[i].lane==1)right=i;}
    check(left>=0&&right>=0,"Missing stopped sedan pair");near(grounds[left],810,.002,"Left sedan stop");near(grounds[right],810,.002,"Right sedan stop");
    near(widths[left],widths[right],.00001,"Stopped white sedan pair has different width");near(lengths[left],lengths[right],.00001,"Stopped white sedan pair has different length");
    // Wet reflection is colored by the same main/fog profile; frozen geometry separates ripple from movement.
    scene.configure(1,1,0,palette,2,100,0,1,false,false,0);for(int i=0;i<240;i++)scene.update(1.0/60);
    seed(scene,900,5);invoke(scene,"prepareCars",new Class<?>[]{});canvas.drawColor(0,PorterDuff.Mode.SRC);invoke(scene,"reflections",new Class<?>[]{Canvas.class,int.class},canvas,0);int[] before=copy(output);
    for(int i=0;i<12;i++)scene.update(1.0/60);seed(scene,900,5);invoke(scene,"prepareCars",new Class<?>[]{});canvas.drawColor(0,PorterDuff.Mode.SRC);invoke(scene,"reflections",new Class<?>[]{Canvas.class,int.class},canvas,0);
    check(!Arrays.equals(before,copy(output)),"Wet ripples stayed frozen while clock advanced");
    canvas.drawColor(0,PorterDuff.Mode.SRC);scene.draw(canvas,540,1200);before=copy(output);scene.draw(canvas,540,1200);check(Arrays.equals(before,copy(output)),"Lighting advances in draw");
    VehicleLights[][] lights=(VehicleLights[][])field(scene,"spriteLights");VehicleLights old=lights[1][0];
    scene.configure(1,1,0,(palette+1)%3,4,80,1,2,false,false,0);check(old.emission.isRecycled(),"Palette replacement leaks lamp mask");
    for(int i=0;i<240;i++)scene.update(1.0/60);scene.draw(canvas,540,1200);
    CityLightAnchors city=(CityLightAnchors)field(scene,"cityLights");check(city.count>0&&city.count<=8,"Missing or unbounded authored building lights");
    for(int i=0;i<city.count;i++)check(city.y[i]<390?city.x[i]<230||city.x[i]>505:city.x[i]<CinematicGeometry.left(city.y[i])-45||city.x[i]>CinematicGeometry.right(city.y[i])+35,"Window glow anchor entered roadway");
    scene.release();LightTextures textures=(LightTextures)field(scene,"lamps");check(textures.glow[2].isRecycled(),"Scene lamp texture lifetime");
   }finally{scene.release();output.recycle();}
  }
 }
 private static boolean sameTint(int a,int b){double ar=a>>16&255,br=b>>16&255;return Math.abs((a>>8&255)/ar-(b>>8&255)/br)<.008&&Math.abs((a&255)/ar-(b&255)/br)<.008;}
 private static int rowWidth(Bitmap image,int y){int threshold=(image.image.getRGB(32,y)>>>24)/4,n=0;for(int x=0;x<image.getWidth();x++)if((image.image.getRGB(x,y)>>>24)>threshold)n++;return n;}
 private static void seed(CinematicScene scene,float ground,int type)throws Exception{double original=CinematicGeometry.inverseModelY(ground);for(TrafficModel.Car car:((TrafficModel)field(scene,"traffic")).cars){car.type=type;car.position=car.lane<2?(original-160)/.65:(940-original)/.65;}}
 private static int[] copy(Bitmap image){int[] p=new int[image.getWidth()*image.getHeight()];image.getPixels(p,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());return p;}
 private static long energy(int[] pixels){long n=0;for(int p:pixels)n+=p>>>24;return n;}
}
