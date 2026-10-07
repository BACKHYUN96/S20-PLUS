package com.s20plus.pixeltraffic;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/** Checks whole-scene drawing, without asserting Android pixels or implementation details. */
public final class SceneRenderChecks {
 public static void main(String[] args) {
  CityScene scene=new CityScene();
  Bitmap output=Bitmap.createBitmap(360,800,Bitmap.Config.ARGB_8888);Canvas target=new Canvas(output);
  int cases=0;
  try {
   scene.setEvents(true,2);scene.setSceneryEnabled(true);
   for(int road=0;road<4;road++) {
    scene.setRoad(road);
    for(int theme=0;theme<3;theme++) {
     scene.setTheme(theme);
     for(int weather=0;weather<5;weather++) {
      scene.configure(weather%2==0?6:3,1);scene.setWeather(weather);scene.setVehicles(0,theme);
      scene.setCamera(weather%2,weather%3);scene.setBrightness(weather==4?60:100);
      for(int tick=0;tick<120;tick++)scene.update(1.0/30);
      scene.draw(target);
      int[] first=output.image.getRGB(0,0,360,800,null,0,360);
      scene.draw(target);
      int[] repeated=output.image.getRGB(0,0,360,800,null,0,360);
      long variation=0;int base=first[0];
      for(int i=0;i<first.length;i++) {
       if((first[i]>>>24)!=255)throw new AssertionError("Transparent final frame at "+road+"/"+theme+"/"+weather);
       if(first[i]!=repeated[i])throw new AssertionError("Repeated draw changes stable scene at "+road+"/"+theme+"/"+weather+" pixel "+i);
       if(first[i]!=base)variation++;
      }
      if(variation<10000)throw new AssertionError("Scene lost most visible artwork");
      cases++;
     }
    }
   }
  } finally {scene.release();output.recycle();}
  System.out.println("PASS: "+cases+" desktop CityScene road/theme/weather composites, opaque output, repeat-draw stability, density/camera/brightness variants (not Android tests)");
 }
}
