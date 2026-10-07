package com.s20plus.pixeltraffic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Direct composition of cached artwork and small textures. Hardware Canvas records these
 * layers for GPU rendering; unlike CityScene this path never rasterizes a full software frame.
 * Day, sunset and night share the calibrated road and use a retargetable active-time blend.
 */
final class CinematicScene {
 private final Paint paint=new Paint();
 private final LightTextures lamps=new LightTextures();
 private final Path roadLightMask=createRoadLightMask();
 private final Rect trailSlice=new Rect();
 private final RectF destination=new RectF();
 private final RectF backgroundBounds=new RectF(0,0,CinematicGeometry.WIDTH,CinematicGeometry.HEIGHT);
 private Bitmap background;
 private final CityLightAnchors cityLights;
 private final CinematicArtwork artwork;
 private final Bitmap[] themeBackgrounds,dryBackgrounds,snowBackgrounds,fogBackgrounds;
 private final boolean[][] needed=new boolean[4][3];
 private final ThemeBlend weatherArt=new ThemeBlend();
 private boolean weatherInitialized;
 private final RoadWetness surface=new RoadWetness();
 private final ThemeBlend theme=new ThemeBlend();
 private boolean themeInitialized;
 private int requestedTheme=1;
 private CinematicVehicles vehicleAtlas;
 private VehicleLayout vehicleLayout;
 private final Bitmap[][] sprites=new Bitmap[6][2];
 private final Rect[][] spriteBounds=new Rect[6][2];
 private final VehicleLights[][] spriteLights=new VehicleLights[6][2];
 private final Bitmap warmGlow,redGlow,warmTrail,redTrail,shadow;
 private TrafficModel traffic=new TrafficModel();
 private CinematicTraffic driving=new CinematicTraffic(traffic);
 private boolean signalsEnabled;
 private TrafficModel.Car[] drawOrder=new TrafficModel.Car[traffic.cars.length];
 private float[] bodyWidths=new float[traffic.cars.length],bodyLengths=new float[traffic.cars.length],
   groundX=new float[traffic.cars.length],groundY=new float[traffic.cars.length],slopes=new float[traffic.cars.length];
 private final RainModel rain=new RainModel();
 private final SnowModel snow=new SnowModel();
 private final SceneryClock scenery=new SceneryClock();
 private final SceneEventModel events=new SceneEventModel();
 private int palette=-1,vehicleMode,weather,brightness=100,camera,cameraPosition=1;
 private boolean released;
 private double rainSeconds,spraySeconds;
 private double selectedSpeed=1;

 CinematicScene(Context context){this(new CinematicArtwork(context));vehicleAtlas=new CinematicVehicles(context);refreshVehicleLayout();}
 /** Takes ownership of the supplied bitmap, including in desktop render checks. */
 CinematicScene(Bitmap image){this(new CinematicArtwork(image));}
 private CinematicScene(CinematicArtwork cache){
  artwork=cache;themeBackgrounds=cache.images[0];dryBackgrounds=cache.images[1];snowBackgrounds=cache.images[2];fogBackgrounds=cache.images[3];
  background=themeBackgrounds[1];cityLights=new CityLightAnchors(background);weatherArt.snap(0);
  paint.setAntiAlias(false);paint.setFilterBitmap(false);
  warmGlow=createGlow(0x00ffd18c);redGlow=createGlow(0x00ff4557);
  warmTrail=createTrail(0x00ffbe71);redTrail=createTrail(0x00ff3e52);shadow=createGlow(0);setVehicles(0,0);
 }
 boolean available(){return !released&&background!=null;}
 /** Load only selected/transition art; trim may recycle retired art but never decodes. */
 private void refreshArtwork(boolean load){
  for(boolean[] row:needed)java.util.Arrays.fill(row,false);
  needed[0][1]=true;
  boolean normal=weatherArt.weights[0]>0||weatherArt.target()==0;
  boolean dry=surface.value()<1||surface.targetValue()<1;
  for(int i=0;i<3;i++)if(theme.weights[i]>0||theme.target()==i){
   needed[0][i]=true;
   if(load)artwork.load(0,i);
   boolean fallback=false;
   for(int weatherKind=1;weatherKind<=2;weatherKind++)if(weatherArt.weights[weatherKind]>0||weatherArt.target()==weatherKind){
    int kind=weatherKind+1;needed[kind][i]=true;
    if(load)artwork.load(kind,i);
    if(artwork.images[kind][i]==null)fallback=true;
   }
   if((normal||fallback)&&dry){needed[1][i]=true;if(load)artwork.load(1,i);}
  }
  artwork.keepOnly(needed);
 }
 void setTheme(int selected){
  if(released)return;
  requestedTheme=Math.max(0,Math.min(2,selected));
  int actual=artwork.load(0,requestedTheme)==null?1:requestedTheme;
  if(!themeInitialized){theme.snap(actual);themeInitialized=true;}else theme.select(actual);
  refreshArtwork(true);
 }
 String themeDescription(){
  String selected=new String[]{"낮","노을","밤"}[requestedTheme];
  return themeBackgrounds[requestedTheme]==null?selected+" · 아트 없음/노을 대체":selected+" · 전용 아트";
 }
 float surfaceWetness(){
  double normal=0,snowWet=0,fogWet=0;
  for(int i=0;i<3;i++){
   double weight=theme.weights[i];if(weight==0)continue;
   float fallback=dryBackgrounds[i]==null?1:surface.value();
   normal+=weight*fallback;
   snowWet+=weight*(snowBackgrounds[i]!=null?0:fallback);
   fogWet+=weight*(fogBackgrounds[i]!=null?.35f:fallback);
  }
  return (float)(weatherArt.weights[0]*normal+weatherArt.weights[1]*snowWet+weatherArt.weights[2]*fogWet);
 }
 String surfaceDescription(){
  int percent=Math.round(surfaceWetness()*100);
  String state=percent==0?"마름":percent==100?"젖음":"전환/습윤";
  boolean fallback=false;
  for(int i=0;i<3;i++)if(theme.weights[i]>0){
   if(weatherArt.weights[0]>0&&surface.value()<1&&dryBackgrounds[i]==null)fallback=true;
   if(weatherArt.weights[1]>0&&snowBackgrounds[i]==null)fallback=true;
   if(weatherArt.weights[2]>0&&fogBackgrounds[i]==null)fallback=true;
  }
  return state+" · 젖음 "+percent+"%"+(fallback?" · 날씨 아트 없음/대체":"")+" · 배경 캐시 "+artwork.count()+"장";
 }
 private float lightGain(float day,float sunset,float night){
  return (float)(theme.weights[0]*day+theme.weights[1]*sunset+theme.weights[2]*night);
 }
 private void drawNormalBackground(Canvas canvas){
  // Normalize source-over alpha to produce the requested weighted sum, not three faded layers.
  double accumulated=0;
  for(int i=0;i<3;i++){
   double weight=theme.weights[i];if(weight<=0||themeBackgrounds[i]==null)continue;
   accumulated+=weight;paint.setColor(Color.WHITE);paint.setAlpha((int)Math.round(255*weight/accumulated));
   canvas.drawBitmap(themeBackgrounds[i],null,backgroundBounds,paint);
  }
  // Wet blend is the base. Normalized dry layers add the second axis of the weighted sum.
  // Above the road horizon the original sky/river artwork always remains untouched.
  float wetness=surface.value();accumulated=wetness;
  if(wetness<1){
   canvas.save();canvas.clipRect(0,CinematicGeometry.HORIZON,540,1200);
   for(int i=0;i<3;i++){
    double weight=theme.weights[i]*(1-wetness);if(weight<=0||themeBackgrounds[i]==null)continue;
    accumulated+=weight;paint.setColor(Color.WHITE);paint.setAlpha((int)Math.round(255*weight/accumulated));
    canvas.drawBitmap(dryBackgrounds[i]!=null?dryBackgrounds[i]:themeBackgrounds[i],null,backgroundBounds,paint);
   }
   canvas.restore();
  }
  paint.setColor(Color.WHITE);
 }
 private void drawBackground(Canvas canvas){
  double accumulated=weatherArt.weights[0];
  if(accumulated>0)drawNormalBackground(canvas);
  for(int kind=1;kind<=2;kind++)for(int i=0;i<3;i++){
   double weight=weatherArt.weights[kind]*theme.weights[i];if(weight<=0)continue;
   Bitmap image=artwork.images[kind+1][i];
   if(image==null)image=dryBackgrounds[i]!=null?dryBackgrounds[i]:themeBackgrounds[i];
   if(image==null)image=background;
   accumulated+=weight;paint.setColor(Color.WHITE);paint.setAlpha((int)Math.round(255*weight/accumulated));
   canvas.drawBitmap(image,null,backgroundBounds,paint);
  }
  paint.setColor(Color.WHITE);
 }
 void configure(int perLane,double speed,int mode,int colors,int selectedWeather,int selectedBrightness,
   int selectedCamera,int position,boolean sceneryEnabled,boolean eventsEnabled,int frequency){
  if(released)return;
  int count=Math.max(1,Math.min(6,perLane));
  if(traffic.cars.length!=count*4){
   traffic=new TrafficModel(count);driving=new CinematicTraffic(traffic);drawOrder=new TrafficModel.Car[count*4];
   bodyWidths=new float[count*4];bodyLengths=new float[count*4];groundX=new float[count*4];groundY=new float[count*4];slopes=new float[count*4];
  }
  traffic.setSpeedMultiplier(speed);selectedSpeed=Double.isFinite(speed)?Math.max(.5,Math.min(1.5,speed)):1;setVehicles(mode,colors);
  weather=Math.max(0,Math.min(4,selectedWeather));surface.select(weather);rain.setLevel(weather<=2?weather:0);
  int artKind=weather==3?1:weather==4?2:0;
  if(!weatherInitialized){weatherArt.snap(artKind);weatherInitialized=true;}else weatherArt.select(artKind);
  snow.enabled=weatherArt.weights[1]>0||artKind==1;refreshArtwork(true);
  brightness=Math.max(0,Math.min(100,selectedBrightness));camera=selectedCamera==1?1:0;
  cameraPosition=Math.max(0,Math.min(2,position));scenery.enabled=sceneryEnabled;events.configure(eventsEnabled,frequency);
  driving.configure(vehicleLayout,selectedSpeed,vehicleMode);driving.setEnabled(signalsEnabled);
 }
 private void setVehicles(int mode,int colors){
  vehicleMode=Math.max(0,Math.min(3,mode));traffic.setVehicleMode(vehicleMode);
  int selected=Math.max(0,Math.min(2,colors));if(selected==palette)return;
  for(int type=0;type<6;type++)for(int direction=0;direction<2;direction++){
   Bitmap next=VehicleArt.create(type,direction==1,selected);
   if(sprites[type][direction]!=null)sprites[type][direction].recycle();sprites[type][direction]=next;
   spriteBounds[type][direction]=measureBody(next);
   if(spriteLights[type][direction]!=null)spriteLights[type][direction].release();
   spriteLights[type][direction]=new VehicleLights(next,spriteBounds[type][direction],direction==1,type);
  }
  palette=selected;
  refreshVehicleLayout();
 }
 private void refreshVehicleLayout(){
  float[][] ratios=new float[6][2];
  for(int type=0;type<6;type++)for(int direction=0;direction<2;direction++){
   Rect source=spriteBounds[type][direction];
   ratios[type][direction]=usesAtlas()?vehicleAtlas.bodyLength(type,direction==1,1):(float)source.height()/source.width();
  }
  vehicleLayout=new VehicleLayout(ratios);
 }
 VehicleLayout vehicleLayout(){return vehicleLayout;}
 void setSignals(boolean enabled){if(released)return;signalsEnabled=enabled;driving.configure(vehicleLayout,selectedSpeed,vehicleMode);driving.setEnabled(enabled);}
 String trafficDescription(){return driving.description();}
 private static Rect measureBody(Bitmap image){
  int width=image.getWidth(),height=image.getHeight(),left=width,top=height,right=0,bottom=0;
  int[] pixels=new int[width*height];image.getPixels(pixels,0,width,0,0,width,height);
  for(int y=0;y<height;y++)for(int x=0;x<width;x++)if((pixels[y*width+x]>>>24)>8){
   left=Math.min(left,x);top=Math.min(top,y);right=Math.max(right,x+1);bottom=Math.max(bottom,y+1);
  }
  return right>left&&bottom>top?new Rect(left,top,right,bottom):new Rect(0,0,width,height);
 }
 void update(double seconds){
  if(released)return;
  theme.update(seconds);surface.update(seconds);weatherArt.update(seconds);refreshArtwork(false);
  snow.enabled=weatherArt.weights[1]>0||weatherArt.target()==1;
  driving.update(seconds);rain.update(seconds);snow.update(seconds);scenery.update(seconds);events.update(seconds);
  if(surfaceWetness()>0&&Double.isFinite(seconds)&&seconds>0){
   double active=Math.min(.1,seconds);
   rainSeconds=(rainSeconds+active)%120;
   spraySeconds=(spraySeconds+active*selectedSpeed)%120;
  }
 }
 private static Bitmap createGlow(int tint){
  Bitmap image=Bitmap.createBitmap(64,64,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);Paint ink=new Paint();
  for(int y=0;y<64;y++)for(int x=0;x<64;x++){
   double radius=Math.sqrt(Math.pow((x-31.5)/31.5,2)+Math.pow((y-31.5)/31.5,2));
   int alpha=(int)(Math.max(0,1-radius)*Math.max(0,1-radius)*220);
   if(alpha>0){ink.setColor(alpha<<24|tint);canvas.drawRect(x,y,x+1,y+1,ink);}
  }
  return image;
 }
 private static Bitmap createTrail(int tint){
  Bitmap image=Bitmap.createBitmap(64,192,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);Paint ink=new Paint();
  // Three widths form a broad faint veil, middle color and narrow bright core.
  // Broken rows preserve a wet pixel-road texture without changing/uploads each frame.
  for(int y=0;y<192;y++){
   double fade=Math.pow(1-y/192.0,1.55),fragment=y%11<3?.52:1;
   for(int x=0;x<64;x++){
    int distance=Math.abs(x-32),base=distance<3?220:distance<11?104:distance<25?35:0;
    int alpha=(int)(base*fade*fragment);
    if(alpha>0){ink.setColor(alpha<<24|tint);canvas.drawRect(x,y,x+1,y+1,ink);}
   }
  }
  return image;
 }
 private void rectangle(Canvas canvas,float x,float y,float width,float height,int color){
  paint.setColor(color);canvas.drawRect(Math.round(x),Math.round(y),Math.round(x+width),Math.round(y+height),paint);
 }
 private void texture(Canvas canvas,Bitmap image,float left,float top,float width,float height,int alpha){
  paint.setColor(Color.WHITE);paint.setAlpha(Math.max(0,Math.min(255,alpha)));
  destination.set(left,top,left+width,top+height);canvas.drawBitmap(image,null,destination,paint);paint.setAlpha(255);
 }
 private void sortCars(){
  System.arraycopy(traffic.cars,0,drawOrder,0,drawOrder.length);
  for(int i=1;i<drawOrder.length;i++){
   TrafficModel.Car value=drawOrder[i];int j=i-1;
   while(j>=0&&drawOrder[j].y()>value.y()){drawOrder[j+1]=drawOrder[j];j--;}
   drawOrder[j+1]=value;
  }
 }
 private boolean usesAtlas(){return palette==0&&vehicleAtlas!=null&&vehicleAtlas.available();}
 private void prepareCars(){
  sortCars();
  for(int i=0;i<drawOrder.length;i++){
   TrafficModel.Car car=drawOrder[i];float y=CinematicGeometry.modelY(car.y());
   float width=y<CinematicGeometry.HORIZON?0:vehicleLayout.width(car.lane,car.type,y);
   float length=vehicleLayout.length(car.lane,car.type,y);
   groundY[i]=y;groundX[i]=CinematicGeometry.laneX(car.lane,y);slopes[i]=vehicleLayout.slope(car.lane,car.type,y);
   bodyWidths[i]=y-length>CinematicGeometry.HEIGHT+20?0:width;bodyLengths[i]=length;
  }
 }
 private VehicleLights carLights(int index){
  TrafficModel.Car car=drawOrder[index];
  return usesAtlas()?vehicleAtlas.lights(car.type,car.lane>=2):spriteLights[car.type][car.lane>=2?1:0];
 }
 private VehicleLights headLights(int type){return usesAtlas()?vehicleAtlas.lights(type,false):spriteLights[type][0];}
 private static Path createRoadLightMask(){
  Path path=new Path();path.moveTo(CinematicGeometry.left(390)+2,390);
  for(int y=420;y<=1200;y+=30)path.lineTo(CinematicGeometry.left(y)+2,y);
  path.lineTo(CinematicGeometry.right(1200)-2,1200);
  for(int y=1170;y>=390;y-=30)path.lineTo(CinematicGeometry.right(y)-2,y);
  path.close();return path;
 }
 String lightingDescription(){return "승용/택시 LED 6500K · 스포츠 안개등 6500K · 버스/트럭 4500K+3000K";}
 private void drawRoadLighting(Canvas canvas){
  canvas.save();canvas.clipPath(roadLightMask);
  for(int index=0;index<drawOrder.length;index++){
   TrafficModel.Car car=drawOrder[index];float width=bodyWidths[index];
   boolean away=car.lane>=2;
   float origin=groundY[index]-(away?bodyLengths[index]:0);
   if(width<5||origin>1200||origin<CinematicGeometry.HORIZON)continue;
   VehicleLighting.Profile profile=VehicleLighting.PROFILES[car.type];VehicleLights lights=headLights(car.type);
   float depth=CinematicEffects.depth(origin);
   int alpha=VehicleLighting.beamAlpha(depth,theme.weights,weatherArt.weights[2],profile.headPower);
   float reach=VehicleLighting.beamLength(width,depth,false);
   canvas.save();canvas.translate(groundX[index],groundY[index]);canvas.skew(slopes[index],0);
   // The far-facing front bumper is at the top of the complete body. Light projects away.
   if(away){canvas.translate(0,-bodyLengths[index]);canvas.scale(1,-1);}
   for(int side=0;side<2;side++){
    float x=lights.x[side]*width;
    texture(canvas,lamps.beam[profile.headIndex],x-width*.57f,0,width*1.14f,reach,alpha);
    if(profile.fogPower>0){
     float fogX=lights.fogX[side]*width;
     texture(canvas,lamps.fogBeam[profile.fogIndex],fogX-width*.53f,0,width*1.06f,
      VehicleLighting.beamLength(width,depth,true),Math.round(alpha*profile.fogPower));
    }
   }
   canvas.restore();
  }
  canvas.restore();
 }
 private void reflectedTrail(Canvas canvas,Bitmap image,float x,float y,float width,float length,int alpha,int seed){
  // Moving, contiguous source strips add restrained ripples without rebuilding or stretching the car.
  for(int part=0;part<3;part++){
   float offset=(float)Math.sin(rainSeconds*1.6+y*.025+seed*.73+part*1.4)*width*.045f;
   trailSlice.set(0,part*image.getHeight()/3,image.getWidth(),(part+1)*image.getHeight()/3);
   destination.set(x-width/2+offset,y+length*part/3,x+width/2+offset,y+length*(part+1)/3);
   paint.setColor(Color.WHITE);paint.setAlpha(Math.max(0,Math.min(255,alpha)));canvas.drawBitmap(image,trailSlice,destination,paint);
  }
  paint.setAlpha(255);
 }
 private void reflections(Canvas canvas,int index){
  float width=bodyWidths[index];if(width<=0)return;
  TrafficModel.Car car=drawOrder[index];boolean away=car.lane>=2;
  float length=bodyLengths[index];VehicleLights lights=carLights(index);
  VehicleLighting.Profile profile=VehicleLighting.PROFILES[car.type];
  canvas.save();canvas.translate(groundX[index],groundY[index]);canvas.skew(slopes[index],0);
  texture(canvas,shadow,-width*.55f,-length*.78f,width*1.1f,length*.92f,120);
  float wetness=surfaceWetness();
  float shimmer=wetness>0?(float)(.94+.06*Math.sin(rainSeconds*Math.PI*2/3+groundY[index]*.045)):1;
  int alpha=(int)(CinematicEffects.reflectionAlpha(groundY[index],wetness)*shimmer*lightGain(.18f,1,1.25f));
  float reflectionLength=CinematicEffects.reflectionLength(width,groundY[index],wetness,away);
  float brake=away?driving.brake(car):0;
  alpha=Math.min(255,(int)(alpha*(1+.75f*brake)));reflectionLength*=1+.18f*brake;
  float reflectionWidth=width*(away?.22f:.27f);
  for(int side=0;side<2;side++){
   float lampX=lights.x[side]*width,lampY=lights.y[side]*length;
   if(!away)texture(canvas,lamps.glow[profile.headIndex],lampX-width*.35f,lampY,width*.7f,width*1.2f,
    (int)((8+45*wetness)*lightGain(.1f,.78f,1.05f)*profile.headPower));
   if(alpha>0)reflectedTrail(canvas,away?redTrail:lamps.trail[profile.headIndex],lampX,lampY,reflectionWidth,reflectionLength,
    Math.min(255,(int)(alpha*(away?1:profile.headPower))),car.lane*7+car.type+side);
   if(!away&&profile.fogPower>0&&alpha>0)reflectedTrail(canvas,lamps.trail[profile.fogIndex],lights.fogX[side]*width,
    lights.fogY[side]*length,width*.16f,reflectionLength*.58f,(int)(alpha*profile.fogPower),car.lane*11+side);
  }
  canvas.restore();
 }
 private void drawWheelSpray(Canvas canvas,int index){
  float wetness=surfaceWetness();
  if(weather>2||wetness<=0||bodyWidths[index]<15)return;
  TrafficModel.Car car=drawOrder[index];float motion=driving.motion(car);if(motion<.03f)return;
  float width=bodyWidths[index],length=bodyLengths[index];
  float depth=CinematicEffects.depth(groundY[index]),direction=car.lane>=2?1:-1;
  for(int side=-1;side<=1;side+=2)for(int particle=0;particle<4;particle++){
   float phase=CinematicEffects.sprayPhase(spraySeconds,car.lane*13+car.type*7,particle);
   float x=side*width*(.43f+.12f*phase);
   float y=-length*.18f+direction*width*.38f*phase-width*.06f*(float)Math.sin(phase*Math.PI);
   float size=.6f+depth*(1.2f+phase);
   int alpha=(int)(CinematicEffects.sprayAlpha(phase,2)*wetness*(.4f+.6f*depth)*motion);
   rectangle(canvas,x,y,size*1.6f,size,alpha<<24|0x00c4d2dc);
  }
 }
 private void drawCar(Canvas canvas,int index){
  float width=bodyWidths[index];if(width<=0)return;
  TrafficModel.Car car=drawOrder[index];boolean away=car.lane>=2;float length=bodyLengths[index];
  VehicleLights lights=carLights(index);
  canvas.save();canvas.translate(groundX[index],groundY[index]);canvas.skew(slopes[index],0);
  drawWheelSpray(canvas,index);
  paint.setColor(Color.WHITE);
  paint.setAlpha((int)(255*(1-.18*weatherArt.weights[2]*(1-CinematicEffects.depth(groundY[index])))));
  if(usesAtlas())vehicleAtlas.draw(canvas,paint,car.type,away,0,-length/2,width);
  else {
   destination.set(-width/2,-length,width/2,0);
   canvas.drawBitmap(sprites[car.type][away?1:0],spriteBounds[car.type][away?1:0],destination,paint);
  }
  paint.setAlpha(255);
  int exposure=(int)(42*CinematicEffects.streetExposure(groundY[index]-length*.5f,car.lane)*lightGain(.08f,1,1.1f));
  if(exposure>0)texture(canvas,lights.wash,-width/2,-length,width,length,exposure);
  if(!away&&lights.emission!=null)texture(canvas,lights.emission,-width/2,-length,width,length,255);
  float lampGain=lightGain(.30f,.86f,1.05f)*(1+(float)weatherArt.weights[1]*.08f);
  VehicleLighting.Profile profile=VehicleLighting.PROFILES[car.type];float depth=CinematicEffects.depth(groundY[index]);
  float brake=away?driving.brake(car):0;
  int alpha=away?Math.min(255,(int)(CinematicEffects.lampAlpha(groundY[index],surfaceWetness())*lampGain+100*brake)):
   VehicleLighting.haloAlpha(depth,theme.weights,weatherArt.weights[2],profile.headPower);
  Bitmap glow=away?redGlow:lamps.glow[profile.headIndex];
  for(int side=0;side<2;side++){
   float lampX=lights.x[side]*width,lampY=lights.y[side]*length;
   if(weatherArt.weights[2]>0)texture(canvas,glow,lampX-width*.47f,lampY-width*.34f,width*.94f,width*.68f,
    (int)(40*weatherArt.weights[2]*lampGain));
   texture(canvas,glow,lampX-width*(away?.24f:.19f),lampY-width*(away?.15f:.12f),width*(away?.48f:.38f),width*(away?.30f:.24f),alpha);
   int core=away?0x00ff8b74:lamps.cores[profile.headIndex]&0x00ffffff;
   float coreWidth=away?Math.max(.9f,width*.085f):Math.max(.65f,width*.05f);
   float coreHeight=away?Math.max(.65f,width*.035f):Math.max(.5f,width*.024f);
   rectangle(canvas,lampX-coreWidth*.5f,lampY-coreHeight*.5f,coreWidth,coreHeight,
    (Math.min(255,(int)((away?191:235)*lampGain+120*brake))<<24)|core);
   if(!away&&profile.fogPower>0&&width>=10){
    float fx=lights.fogX[side]*width,fy=lights.fogY[side]*length;
    texture(canvas,lamps.glow[profile.fogIndex],fx-width*.14f,fy-width*.09f,width*.28f,width*.18f,
     Math.round(alpha*profile.fogPower));
    rectangle(canvas,fx-width*.016f,fy-width*.01f,Math.max(.5f,width*.032f),Math.max(.4f,width*.02f),
     (Math.round(215*lampGain)<<24)|(lamps.cores[profile.fogIndex]&0x00ffffff));
   }
  }
  canvas.restore();
 }
 private void drawSignals(Canvas canvas){
  if(!signalsEnabled)return;
  int phase=driving.phase();
  for(int side=0;side<2;side++){
   float ground=side==0?CinematicTraffic.APPROACH_STOP:CinematicTraffic.AWAY_STOP;
   float scale=CinematicGeometry.spriteScale(ground);
   float x=side==0?CinematicGeometry.left(ground)-11*scale:CinematicGeometry.right(ground)+10*scale;
   canvas.save();canvas.translate(x,ground);canvas.scale(scale,scale);
   rectangle(canvas,-3,-2,8,3,0x99202b35);
   rectangle(canvas,0,-48,2,48,0xff465762);rectangle(canvas,1,-48,1,48,0xff8e9ba0);
   rectangle(canvas,-4,-50,10,25,0xff14212c);rectangle(canvas,-3,-49,8,23,0xff243644);
   rectangle(canvas,-3,-50,8,2,0xff7c8c91);
   for(int lamp=0;lamp<3;lamp++){
    float y=-46+lamp*7;boolean on=lamp==2-phase;
    int tint=lamp==0?0x00ff5260:lamp==1?0x00ffcc65:0x0056eeae;
    rectangle(canvas,-2,y,6,5,0xff101923);
    rectangle(canvas,-1,y+1,4,3,(on?0xff000000:0x39000000)|tint);
    if(on){
     texture(canvas,lamp==0?redGlow:lamp==1?warmGlow:warmGlow,-8,y-5,16,14,(int)(75*lightGain(.25f,1,1.15f)));
     rectangle(canvas,0,y+1,2,1,0xe6fff4d4);
    }
   }
   canvas.restore();
  }
 }
 private void drawStreetLights(Canvas canvas){
  // Authored light centers only, with small bounded halos to avoid doubling the painted bloom.
  for(int i=0;i<cityLights.count;i++){
   float x=cityLights.x[i],y=cityLights.y[i];
   float pulse=scenery.enabled?(float)(.985+.015*Math.sin(scenery.seconds()*.28+i)):1;
   canvas.save();canvas.clipRect(x-7,y-7,x+7,y+7);
   texture(canvas,lamps.glow[0],x-9,y-9,18,18,
    (int)(18*pulse*lightGain(.01f,.55f,1)*(1-.6*weatherArt.weights[2])));canvas.restore();
  }
  // Reflections react subtly to the scenery clock; the background illumination stays authored.
  float pulse=scenery.enabled?(float)(.92+.08*Math.sin(scenery.seconds()*.7)):1;
  for(int i=0;i<5;i++){
   float y=530+i*140,scale=CinematicGeometry.spriteScale(y);
   for(int side=0;side<2;side++){
    float x=side==0?CinematicGeometry.left(y)-18*scale:CinematicGeometry.right(y)+13*scale;
    float radius=15*scale*(1+.5f*(float)weatherArt.weights[2]);
    texture(canvas,lamps.glow[0],x-radius,y-7*scale-radius,radius*2,radius*2,(int)(80*pulse*lightGain(.02f,.72f,1)));
    canvas.save();canvas.clipPath(roadLightMask);
    float poolX=side==0?CinematicGeometry.left(y)+12*scale:CinematicGeometry.right(y)-12*scale;
    texture(canvas,lamps.glow[0],poolX-24*scale,y-5*scale,48*scale,38*scale,
     (int)((15+16*surfaceWetness())*lightGain(.01f,.7f,1)*pulse));
    canvas.restore();
   }
  }
 }
 private void drawScenery(Canvas canvas){
  if(!scenery.enabled)return;
  double seconds=scenery.seconds();
  for(int i=0;i<4;i++){
   float x=(float)((i*181+seconds*.8)%660)-100,y=95+i*28;
   rectangle(canvas,x,y,72,2,0x126b4770);rectangle(canvas,x+20,y-2,38,2,0x126b4770);
  }
 }
 private int eventColor(int color){return (color&0x00ffffff)|((int)((color>>>24)*(1-.65*weatherArt.weights[2]))<<24);}
 private void drawEvent(Canvas canvas){
  if(!events.visible())return;
  // A distant train crosses the river corridor, entirely above the road's horizon.
  int x=(int)Math.round(-84+events.progress()*708),y=213;
  canvas.save();canvas.clipRect(250,205,540,230);
  for(int carriage=0;carriage<3;carriage++){
   int left=x+carriage*28;
   rectangle(canvas,left,y,27,11,eventColor(0xff525068));rectangle(canvas,left,y,27,2,eventColor(0xff9c91a3));
   rectangle(canvas,left,y+9,27,2,eventColor(0xffac7161));
   for(int window=0;window<4;window++)rectangle(canvas,left+3+window*6,y+3,4,4,eventColor(0xffffcf88));
   rectangle(canvas,left+3,y+11,4,2,eventColor(0xff222a3b));rectangle(canvas,left+20,y+11,4,2,eventColor(0xff222a3b));
   if(carriage<2)rectangle(canvas,left+27,y+6,1,3,eventColor(0xff6e6f80));
  }
  canvas.restore();
 }
 private void drawRain(Canvas canvas,boolean splashes,boolean foreground){
  for(int i=0;i<rain.count();i++){
   RainModel.Drop drop=rain.drops[i];
   float ground=CinematicGeometry.modelY(drop.hitY),scale=CinematicEffects.rainScale(ground);
   float x=CinematicGeometry.remapX(drop.x,drop.hitY),y=splashes?ground:CinematicEffects.rainY(drop.y,drop.hitY);
   if(splashes){
    if(drop.splashTicks==0)continue;
    float age=(8-drop.splashTicks)/8f,radius=(2+age*6)*scale;
    int alpha=(int)((18+drop.splashTicks*6)*surfaceWetness()*(.4f+.6f*CinematicEffects.depth(ground)));
    rectangle(canvas,x-radius,y,radius*2,Math.max(.6f,scale),alpha<<24|0x00b7ccda);
    rectangle(canvas,x-radius,y-3*scale,scale,2*scale,alpha<<24|0x00b7ccda);
    rectangle(canvas,x+radius,y-2*scale,scale,2*scale,alpha<<24|0x00b7ccda);
   }else{
    if(drop.splashTicks>0||y<0||(CinematicEffects.depth(ground)>=.42f)!=foreground)continue;
    float streak=(rain.level()==2?13:8)*scale,thickness=Math.max(.6f,scale);
    int alpha=(int)((rain.level()==2?100:76)*(.45f+.55f*CinematicEffects.depth(ground)));
    rectangle(canvas,x,y-streak,thickness,streak,alpha<<24|0x00a6c1d4);
    rectangle(canvas,x-thickness,y-streak*.45f,thickness,streak*.45f,(alpha/2)<<24|0x00a6c1d4);
   }
  }
 }
 private void drawSnow(Canvas canvas,boolean foreground){
  float strength=(float)weatherArt.weights[1];if(strength<=0)return;
  for(int i=0;i<48;i++){
   float depth=(i%12)/11f;if((depth>=.65f)!=foreground)continue;
   float x=(float)snow.x[i]*1.5f,y=(float)snow.y[i]*1.5f;
   float size=.7f+depth*2.5f;
   boolean nearLamp=y>500&&(x<CinematicGeometry.left(y)||x>CinematicGeometry.right(y));
   int tint=nearLamp&&lightGain(0,1,1)>.5f?0x00fff1d5:0x00e5edf0;
   int alpha=(int)(strength*(85+125*depth));
   rectangle(canvas,x,y,size,size*.72f,alpha<<24|tint);
  }
 }
 void draw(Canvas canvas){draw(canvas,canvas.getWidth(),canvas.getHeight());}
 void draw(Canvas canvas,int width,int height){
  if(!available()||width<=0||height<=0)return;
  canvas.save();canvas.clipRect(0,0,width,height);canvas.drawColor(0xff342946);
  float scale=CinematicGeometry.scale(width,camera);
  canvas.translate(CinematicGeometry.offsetX(width,camera,cameraPosition),CinematicGeometry.offsetY(width,height,camera));canvas.scale(scale,scale);
  drawBackground(canvas);
  drawScenery(canvas);drawEvent(canvas);drawStreetLights(canvas);
  if(rain.level()>0)rectangle(canvas,0,0,540,1200,rain.level()==1?0x07101c35:0x0d101c35);
  drawSnow(canvas,false);prepareCars();canvas.save();canvas.clipRect(0,CinematicGeometry.HORIZON,540,1200);
  drawRain(canvas,true,false);drawRain(canvas,false,false);drawRoadLighting(canvas);
  canvas.save();canvas.clipPath(roadLightMask);for(int i=0;i<drawOrder.length;i++)reflections(canvas,i);canvas.restore();
  for(int i=0;i<drawOrder.length;i++)drawCar(canvas,i);canvas.restore();
  drawSignals(canvas);drawRain(canvas,false,true);drawSnow(canvas,true);canvas.restore();
  if(brightness<100){paint.setColor(((100-brightness)*255/100)<<24);canvas.save();canvas.clipRect(0,0,width,height);canvas.drawRect(0,0,width,height,paint);canvas.restore();}
  paint.setColor(Color.WHITE);
 }
 void release(){
  if(released)return;released=true;
  artwork.release();
  background=null;
  if(vehicleAtlas!=null)vehicleAtlas.release();
  for(Bitmap[] pair:sprites)for(Bitmap sprite:pair)if(sprite!=null)sprite.recycle();
  for(VehicleLights[] pair:spriteLights)for(VehicleLights light:pair)if(light!=null)light.release();
  warmGlow.recycle();redGlow.recycle();warmTrail.recycle();redTrail.recycle();shadow.recycle();lamps.release();
 }
}
