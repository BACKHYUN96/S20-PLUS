package com.s20plus.pixeltraffic;

/** Active-time signal/velocity controller for the calibrated city only. No drawing or wall clock. */
final class CinematicTraffic {
 static final double GREEN_SECONDS=14,AMBER_SECONDS=3,RED_SECONDS=9;
 static final double CYCLE=GREEN_SECONDS+AMBER_SECONDS+RED_SECONDS;
 static final double ACCELERATION=35,DECELERATION=70;
 static final float APPROACH_STOP=810,AWAY_STOP=902;
 private static final double STEP=1.0/60,TRACK=TrafficModel.TRACK_LENGTH;
 private final TrafficModel traffic;
 private final int perLane;
 private final double[] velocity,nextVelocity,advance,brakes,frontOffset,rearOffset,reaction,spaces;
 private final boolean[] committed,held;
 private final double[][] stops=new double[4][6];
 private VehicleLayout layout;
 private double multiplier=1,remainder,clock;
 private int mode;
 private boolean enabled,controlled;

 CinematicTraffic(TrafficModel model){
  traffic=model;perLane=model.cars.length/4;int n=model.cars.length;
  velocity=new double[n];nextVelocity=new double[n];advance=new double[n];brakes=new double[n];
  frontOffset=new double[n];rearOffset=new double[n];reaction=new double[n];
  spaces=new double[n];committed=new boolean[n];held=new boolean[n];
 }
 void configure(VehicleLayout selectedLayout,double speed,int selectedMode){
  multiplier=Double.isFinite(speed)?Math.max(.5,Math.min(1.5,speed)):1;
  mode=Math.max(0,Math.min(3,selectedMode));
  if(layout==selectedLayout)return;
  layout=selectedLayout;
  for(int lane=0;lane<4;lane++)for(int type=0;type<6;type++){
   float ground=APPROACH_STOP;
   if(lane>=2){
    // The away vehicle's front is the TOP of its complete body, not its ground anchor.
    float lo=AWAY_STOP,hi=1200+300;
    for(int i=0;i<28;i++){float mid=(lo+hi)*.5f;if(mid-layout.length(lane,type,mid)<AWAY_STOP)lo=mid;else hi=mid;}
    ground=(lo+hi)*.5f;
   }
   stops[lane][type]=positionAt(lane,ground);
  }
 }
 void setEnabled(boolean selected){
  if(enabled==selected)return;
  enabled=selected;clock=0;
  java.util.Arrays.fill(committed,false);
  if(selected&&!controlled){
   controlled=true;remainder=0;
   // A new dense scene starts with a safe common lane speed, not overlapping braking envelopes.
   for(int lane=0;lane<4;lane++){
    double initial=Double.POSITIVE_INFINITY;
    for(int j=0;j<perLane;j++){
     TrafficModel.Car car=traffic.cars[lane*perLane+j],leader=traffic.cars[lane*perLane+(j+1)%perLane];
     initial=Math.min(initial,cruise(car));
     if(perLane>1){
      double clearance=TrafficModel.MIN_GAP;
      if(leader.position>car.position){
       TrafficModel.Car body=lane<2?leader:car;float ground=CinematicGeometry.modelY(body.y());
       double length=Math.abs(positionAt(lane,ground-layout.length(lane,body.type,ground))-body.position);
       clearance=Math.max(clearance,length+16);
      }
      initial=Math.min(initial,safeSpeed(Math.max(0,forward(leader.position-car.position)-clearance),0,STEP));
     }
    }
    for(int j=0;j<perLane;j++)velocity[lane*perLane+j]=initial;
   }
  }
  // Turning off removes the obstacle, but queued cars accelerate rather than teleporting.
 }
 boolean enabled(){return enabled;}
 int phase(){return clock<GREEN_SECONDS?0:clock<GREEN_SECONDS+AMBER_SECONDS?1:2;}
 double seconds(){return clock;}
 double stopPosition(int lane,int type){return stops[lane][type];}
 double speed(TrafficModel.Car car){int i=index(car);return controlled?velocity[i]:cruise(car);}
 float motion(TrafficModel.Car car){return (float)Math.min(1,speed(car)/Math.max(1,cruise(car)));}
 float brake(TrafficModel.Car car){return (float)brakes[index(car)];}
 int waiting(){int n=0;for(int i=0;i<velocity.length;i++)if(held[i]&&velocity[i]<.3)n++;return n;}
 String description(){return (enabled?new String[]{"초록 · 주행","노랑 · 감속","빨강 · 정차"}[phase()]:"꺼짐 · 자유 주행")+" · 대기 "+waiting()+"대";}
 void update(double seconds){
  if(!Double.isFinite(seconds)||seconds<=0)return;
  if(!controlled){traffic.update(seconds);return;}
  remainder+=Math.min(.1,seconds);
  while(remainder+1e-12>=STEP){tick();remainder=Math.max(0,remainder-STEP);}
 }
 private void tick(){
  int before=phase();if(enabled){clock+=STEP;if(clock+1e-9>=CYCLE)clock=0;}
  int after=phase();
  if(enabled&&before!=after){
   for(int i=0;i<velocity.length;i++){
    double distance=stopDistance(traffic.cars[i]);
    if(after==0)committed[i]=false;
    else if(after==1)committed[i]=distance<velocity[i]*velocity[i]/(2*DECELERATION)+4;
   }
  }
  for(int i=0;i<velocity.length;i++){
   TrafficModel.Car car=traffic.cars[i];float ground=CinematicGeometry.modelY(car.y());
   float rearOrFront=ground-layout.length(car.lane,car.type,ground);
   double bodyDistance=Math.abs(positionAt(car.lane,rearOrFront)-car.position);
   frontOffset[i]=car.lane>=2?bodyDistance:0;
   rearOffset[i]=car.lane<2?bodyDistance:0;
  }
  for(int i=0;i<velocity.length;i++){
   TrafficModel.Car car=traffic.cars[i];int leader=car.lane*perLane+(i%perLane+1)%perLane;
   double gap=perLane==1?TRACK:forward(traffic.cars[leader].position-car.position);
   double clearance=traffic.cars[leader].position>car.position?Math.max(TrafficModel.MIN_GAP,frontOffset[i]+rearOffset[leader]+16):TrafficModel.MIN_GAP;
   double space=Math.max(0,gap-clearance);spaces[i]=space;
   double target=cruise(car);
   if(perLane>1)target=Math.min(target,safeSpeed(space,Math.max(0,velocity[leader]-DECELERATION*STEP),1));
   double stop=stopDistance(car);
   boolean blocking=enabled&&after!=0&&!committed[i]&&stop<TRACK/2;
   if(blocking)target=Math.min(target,safeSpeed(stop,0,STEP));
   boolean nowHeld=target<.3&&velocity[i]<.3;
   if(nowHeld){held[i]=true;reaction[i]=.25;target=0;}
   else if(held[i]&&target>.3){
    reaction[i]=Math.max(0,reaction[i]-STEP);
    if(reaction[i]>0)target=0;else held[i]=false;
   }
   double previous=velocity[i];
   double next=previous<target?Math.min(target,previous+ACCELERATION*STEP):Math.max(target,previous-DECELERATION*STEP);
   double distance=next*STEP;
   // Signal guard operates on the body-front stop anchor. Following guards use simultaneous movement below.
   if(blocking)distance=Math.min(distance,stop);
   if(distance+1e-8<next*STEP)next=Math.min(next,distance/STEP);
   if(distance<1e-6&&target<.3){next=0;held[i]=true;}
   nextVelocity[i]=Math.max(0,next);advance[i]=Math.max(0,distance);

  }
  // Resolve leader movement together; a snapshot-only gap clamp makes close followers jerk.
  if(perLane>1)for(int pass=0;pass<perLane;pass++)for(int i=0;i<velocity.length;i++){
   int leader=traffic.cars[i].lane*perLane+(i%perLane+1)%perLane;
   double maximum=spaces[i]+advance[leader];
   if(advance[i]>maximum){advance[i]=maximum;nextVelocity[i]=Math.min(nextVelocity[i],maximum/STEP);}
  }
  for(int i=0;i<velocity.length;i++){
   double brakeTarget=velocity[i]-nextVelocity[i]>.01||held[i]?1:0;
   brakes[i]+=Math.max(-STEP*4,Math.min(STEP*10,brakeTarget-brakes[i]));
   TrafficModel.Car car=traffic.cars[i];velocity[i]=nextVelocity[i];car.position+=advance[i];
   if(car.position>=TRACK){car.position-=TRACK;car.type=allowed(car.type+1);committed[i]=false;}
  }
 }
 private double safeSpeed(double space,double leaderSpeed,double headway){
  double discrete=DECELERATION*headway;
  return Math.max(0,Math.sqrt(discrete*discrete+leaderSpeed*leaderSpeed+2*DECELERATION*Math.max(0,space))-discrete);
 }
 private double stopDistance(TrafficModel.Car car){double distance=stops[car.lane][car.type]-car.position;return Math.abs(distance)<1e-7?0:forward(distance);}
 private int allowed(int type){return mode==1?type%4:mode==2?4+type%2:mode==3?3:type%6;}
 private double cruise(TrafficModel.Car car){return ((car.type>=4?65:85+car.type*7)+car.lane*5)*multiplier;}
 private int index(TrafficModel.Car car){for(int i=car.lane*perLane;i<(car.lane+1)*perLane;i++)if(traffic.cars[i]==car)return i;throw new IllegalArgumentException("Unbound vehicle");}
 private static double forward(double distance){return (distance%TRACK+TRACK)%TRACK;}
 static double positionAt(int lane,float ground){double modelY=CinematicGeometry.inverseModelY(ground);return lane<2?(modelY-160)/.65:(940-modelY)/.65;}
}
