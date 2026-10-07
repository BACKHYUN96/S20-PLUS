package com.s20plus.pixeltraffic;
import android.graphics.*;

/** Composite into one low-resolution frame so moving objects share the pixel grid. */
final class CityScene {
 private final Paint paint=new Paint();
 private TrafficModel traffic=new TrafficModel();
 private TrafficModel.Car[] drawOrder=new TrafficModel.Car[12];
 private final Bitmap[] backgrounds=new Bitmap[3],foregrounds=new Bitmap[3];
 private final ThemeBlend blend=new ThemeBlend();
 private final SnowModel snow=new SnowModel();
 private final SceneryClock scenery=new SceneryClock();
 private final SceneEvents events=new SceneEvents();
 private int road=-1,weather,vehicleMode,palette=-1,cameraMode,cameraPosition=1;
 private int theme=1,brightness=100;
 private final RainModel rain=new RainModel();
 private static final int[] SKIES={0xff6a99b5,0xff34314e,0xff111a34},CHANNELS={16,8,0};
 private final Bitmap[][] sprites=new Bitmap[6][2];
 private final Bitmap frame=Bitmap.createBitmap(360,800,Bitmap.Config.ARGB_8888);
 private final Canvas canvas=new Canvas(frame);
 CityScene() {
  setRoad(0);setVehicles(0,0);
  paint.setFilterBitmap(false);paint.setAntiAlias(false);
 }
 void setTheme(int value) {theme=Math.max(0,Math.min(2,value));blend.select(theme);}
 void setRoad(int value) {
  int selected=Math.max(0,Math.min(3,value));if(selected==road)return;
  // Build all palettes before releasing the previous set; blends only reuse cached images.
  Bitmap[] nextBg=new Bitmap[3],nextFg=new Bitmap[3];
  for(int i=0;i<3;i++){PixelArt art=new PixelArt(i,selected);nextBg[i]=art.background();nextFg[i]=art.foreground();}
  for(int i=0;i<3;i++){if(backgrounds[i]!=null){backgrounds[i].recycle();foregrounds[i].recycle();}backgrounds[i]=nextBg[i];foregrounds[i]=nextFg[i];}
  road=selected;
 }
 void setVehicles(int mode,int colors) {
  vehicleMode=Math.max(0,Math.min(3,mode));traffic.setVehicleMode(vehicleMode);
  int selected=Math.max(0,Math.min(2,colors));if(selected==palette)return;
  PixelArt art=new PixelArt();
  for(int type=0;type<6;type++)for(int d=0;d<2;d++){Bitmap image=art.car(type,d==1,selected);if(sprites[type][d]!=null)sprites[type][d].recycle();sprites[type][d]=image;}
  palette=selected;
 }
 private void layers(Bitmap[] images) {
  double accumulated=0;
  for(int i=0;i<3;i++) {
   double w=blend.weights[i];if(w<=0)continue;
   // Sequential source-over alphas yield the requested mixture for opaque backgrounds.
   accumulated+=w;paint.setAlpha((int)Math.round(255*(images==backgrounds?w/accumulated:w)));canvas.drawBitmap(images[i],0,0,paint);
  }
  paint.setAlpha(255);
 }
 void configure(int perLane,double speed) {
  if(traffic.cars.length!=perLane*4) {traffic=new TrafficModel(perLane);drawOrder=new TrafficModel.Car[traffic.cars.length];}
  traffic.setSpeedMultiplier(speed);traffic.setVehicleMode(vehicleMode);
 }
 void setEvents(boolean enabled,int frequency){events.configure(enabled,frequency);}
 void setCamera(int mode,int position){cameraMode=Math.max(0,Math.min(1,mode));cameraPosition=Math.max(0,Math.min(2,position));}
 void setSceneryEnabled(boolean value) {scenery.enabled=value;}
 void setBrightness(int percent) {brightness=Math.max(0,Math.min(100,percent));}
 void setWeather(int level) {weather=Math.max(0,Math.min(4,level));rain.setLevel(weather<=2?weather:0);snow.enabled=weather==3;}
 void update(double seconds) {traffic.update(seconds);rain.update(seconds);snow.update(seconds);blend.update(seconds);scenery.update(seconds);events.update(seconds);}
 private void rect(float x,float y,float w,float h,int color) {
  paint.setColor(color);canvas.drawRect(Math.round(x),Math.round(y),Math.round(x+w),Math.round(y+h),paint);
 }
 private void drawCar(TrafficModel.Car car,boolean reflection) {
  float y=(float)car.y();if(y<215||y>850) return;
  float t=Math.max(0,Math.min(1,(y-260)/540));
  float left=PixelArt.left(y),width=PixelArt.right(y)-left;
  float x=Math.round(left+width*(car.lane+.5f)/4),scale=.28f+.72f*t;
  boolean away=car.lane>=2;
  int type=car.type, length=type==4?72:type==5?60:type==2?44:40;
  if(reflection) {
   float base=y+length*scale/2;
   double darkness=1-blend.weights[0];
   int steps=rain.level()>0?18:12;
   for(int step=0;step<steps;step++) {
    float ry=base+step*2.5f*scale,spread=(3+step*.45f)*scale;
    int alpha=(int)((rain.level()>0?118:78)*(1-step/(double)steps)*(.38+.62*darkness));
    int tint=away?0x00ff4756:0x00ffc476;
    float lampOffset=7*scale;
    rect(x-lampOffset-spread/2,ry,spread,Math.max(1,scale),alpha<<24|tint);
    rect(x+lampOffset-spread/2,ry,spread,Math.max(1,scale),alpha<<24|tint);
    if(step%3==0){rect(x-lampOffset,ry,Math.max(1,scale),1,(Math.min(235,alpha+85)<<24)|tint);rect(x+lampOffset,ry,Math.max(1,scale),1,(Math.min(235,alpha+85)<<24)|tint);}
   }
   rect(x-12*scale+2,y-length*scale/2+3,24*scale,length*scale,0x60202a35);
   return;
  }
  canvas.save(); canvas.translate(x,Math.round(y));
  float slope=(-170+250*(car.lane+.5f)/4)/540;
  canvas.skew(slope,0);canvas.scale(scale,scale);
  paint.setColor(Color.WHITE);canvas.drawBitmap(sprites[type][away?1:0],-20,-40,paint);canvas.restore();
  float lightY=y+(length/2f-4)*scale;
  int glowAlpha=(int)(55*(1-blend.weights[0]));
  int glowTint=away?0x00ff4c60:0x00ffce83;
  for(int lamp=-1;lamp<=1;lamp+=2){float lx=x+lamp*8*scale;rect(lx-4*scale,lightY-2*scale,8*scale,4*scale,glowAlpha<<24|glowTint);}

 }
 private void drawRain(boolean splashes) {
  for(int i=0;i<rain.count();i++) {
   RainModel.Drop d=rain.drops[i];
   if(splashes) {
    if(d.splashTicks==0)continue;
    int radius=1+(8-d.splashTicks)/2;
    int alpha=30+d.splashTicks*7,color=alpha<<24|0x0087adb9;
    rect((float)d.x-radius,(float)d.hitY,radius*2,1,color);
    rect((float)d.x-radius,(float)d.hitY-2,1,2,color);
    rect((float)d.x+radius,(float)d.hitY-2,1,2,color);
   } else {
    if(d.splashTicks>0||d.y<180)continue;
    int color=(theme==0?0x557395a5:0x668aa4b8);
    rect((float)d.x,(float)d.y,1,3,color);
    rect((float)d.x-1,(float)d.y+3,1,rain.level()==2?5:3,color);
   }
  }
 }
 private void drawScenery() {
  if(!scenery.enabled)return;
  double seconds=scenery.seconds();
  // All accents use integer rectangles and no per-frame objects.
  if(road!=0) {
   for(int i=0;i<3;i++) {
    float x=(float)((i*137+seconds*1.2)%430)-65,y=105+i*23;
    rect(x,y,39,2,0x183f5366);rect(x+8,y-2,23,2,0x183f5366);rect(x+27,y+2,29,1,0x183f5366);
   }
  }
  double dark=1-blend.weights[0];
  if(road==0) {
   int alpha=(int)(dark*(20+20*(.5+.5*Math.sin(seconds*.8))));
   for(int y=410;y<760;y+=145){int x=(int)PixelArt.right(y)+25;rect(x,y-38,21,2,(alpha<<24)|0x006bc9c1);}
  } else if(road==1) {
   for(int i=0;i<22;i++) {
    float y=(float)(284+i*22+Math.sin(seconds*.6+i)*2);
    int shoreline=(int)(180-180*(y-260)/540),edge=Math.max(0,shoreline-14);
    if(edge>4){float x=(float)((i*17+seconds*2)%Math.max(1,edge-3));rect(x,y,Math.min(8,edge-x),1,0x356d9ba6);rect(edge-4,y+2,5,1,0x558daeb1);}
   }
   // A slow beacon pulse rather than a broad beam across vehicle lanes.
   int alpha=(int)(dark*(35+90*Math.pow(.5+.5*Math.cos(seconds*Math.PI/4),4)));
   rect(69,274,11,3,(alpha<<24)|0x00ffdb99);rect(50,275,18,1,(alpha/2<<24)|0x00ffdb99);
  } else if(road==2) {
   for(int i=0;i<4;i++){float x=(float)((i*101+seconds*.8)%440)-80;rect(x,225+i*7,68,2,0x167c9494);rect(x+17,223+i*7,38,2,0x127c9494);}
  } else {
   int alpha=(int)(dark*(35+15*Math.sin(seconds*.5)));
   rect(210,282,15,2,(alpha<<24)|0x00deeac5);rect(239,282,2,7,(alpha<<24)|0x00deeac5);
  }
 }
 void draw(Canvas target) {
  paint.setColor(Color.WHITE);layers(backgrounds);
  drawScenery();events.draw(canvas,road,1-blend.weights[0]);
  if(rain.level()>0)rect(0,0,360,800,rain.level()==1?0x14172435:0x28172435);
  System.arraycopy(traffic.cars,0,drawOrder,0,drawOrder.length);
  for(int i=1;i<drawOrder.length;i++) {
   TrafficModel.Car car=drawOrder[i];int j=i-1;
   while(j>=0&&drawOrder[j].y()>car.y()) {drawOrder[j+1]=drawOrder[j];j--;}
   drawOrder[j+1]=car;
  }
  canvas.save();canvas.clipRect(0,260,360,800);
  drawRain(true);
  for(TrafficModel.Car car:drawOrder) drawCar(car,true);
  for(TrafficModel.Car car:drawOrder) drawCar(car,false);
  canvas.restore();paint.setColor(Color.WHITE);layers(foregrounds);
  drawRain(false);
  if(weather==3) {
   // Snow settles in strips outside the lanes and on roof/road edges.
   for(int y=290;y<800;y+=18){rect(PixelArt.left(y)-8,y,6,2,0xffcad7d9);rect(PixelArt.right(y)+3,y,6,2,0xffcad7d9);}
   for(int i=0;i<48;i++){rect((float)snow.x[i],(float)snow.y[i],i%3==0?2:1,2,0xcce1eced);}
  }
  if(weather==4) {
   // Stronger fog near the horizon; lower strips preserve foreground visibility.
   for(int y=180;y<800;y+=20){int alpha=75-(y-180)*55/620;rect(0,y,360,20,(alpha<<24)|0x00b5c6ca);}
  }
  paint.setColor(Color.WHITE);
  int sky=0xff000000;
  for(int shift:CHANNELS) {double value=0;for(int i=0;i<3;i++)value+=((SKIES[i]>>shift)&255)*blend.weights[i];sky|=((int)Math.round(value))<<shift;}
  target.drawColor(sky);
  float scale=CameraLayout.scale(target.getWidth(),cameraMode);
  target.save();target.translate(CameraLayout.offsetX(target.getWidth(),cameraMode,cameraPosition),CameraLayout.offsetY(target.getWidth(),target.getHeight(),cameraMode));target.scale(scale,scale);
  target.drawBitmap(frame,0,0,paint);target.restore();
  if(brightness<100) {paint.setColor(((100-brightness)*255/100)<<24);target.drawRect(0,0,target.getWidth(),target.getHeight(),paint);}
 }
 void release() {
  for(int i=0;i<3;i++){backgrounds[i].recycle();foregrounds[i].recycle();}frame.recycle();
  for(Bitmap[] pair:sprites) for(Bitmap sprite:pair) sprite.recycle();
 }
}
