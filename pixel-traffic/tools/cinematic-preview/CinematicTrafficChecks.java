package com.s20plus.pixeltraffic;

/** Independent position/body assertions on real controller trajectories, not Android performance. */
public final class CinematicTrafficChecks {
 private static final double STEP=1.0/60;
 private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 private static void near(double a,double b,double tolerance,String message){check(Math.abs(a-b)<=tolerance,message+": "+a+" / "+b);}
 private static VehicleLayout layout(){float[][] ratios=new float[6][2];for(int t=0;t<6;t++)for(int d=0;d<2;d++)ratios[t][d]=t==4?2.15f:t==5?2:t==2?1.65f:1.6f;return new VehicleLayout(ratios);}
 private static CinematicTraffic controller(TrafficModel model,double speed,int mode){model.setSpeedMultiplier(speed);model.setVehicleMode(mode);CinematicTraffic c=new CinematicTraffic(model);c.configure(layout(),speed,mode);c.setEnabled(true);return c;}
 public static void main(String[] args){
  if(args.length>1||(args.length==1&&!args[0].equals("--edges")))throw new IllegalArgumentException("Expected no args or --edges");
  freeAndClock();stopsAndDeparture();if(args.length==0)longTrajectories();frameRatesAndToggles();amberDecision();
  System.out.println("PASS: "+(args.length==0?"48 full trajectories; ":"edge contracts; ")+"city signal cycles, both body-front stops, staggered starts, braking, circular/body gaps, free-mode parity and active-time/toggle/frame-rate contracts");
 }
 private static void freeAndClock(){
  TrafficModel a=new TrafficModel(6),b=new TrafficModel(6);CinematicTraffic c=new CinematicTraffic(b);c.configure(layout(),1,0);
  for(int i=0;i<600;i++){a.update(STEP);c.update(STEP);}
  for(int i=0;i<a.cars.length;i++)near(a.cars[i].position,b.cars[i].position,1e-9,"Initially disabled must preserve legacy traffic");
  c.setEnabled(true);double before=c.seconds(),position=b.cars[0].position;
  for(double dt:new double[]{0,-1,Double.NaN,Double.POSITIVE_INFINITY})c.update(dt);
  near(c.seconds(),before,0,"Invalid/hidden time advanced clock");near(b.cars[0].position,position,0,"Invalid time moved car");
  for(int i=0;i<14*60;i++)c.update(STEP);check(c.phase()==1,"Green boundary");
  for(int i=0;i<3*60;i++)c.update(STEP);check(c.phase()==2,"Amber boundary");
  for(int i=0;i<9*60;i++)c.update(STEP);check(c.phase()==0,"Cycle boundary");
  c.update(60);near(c.seconds(),.1,1e-8,"Resume must not simulate hidden minute");
 }
 private static void stopsAndDeparture(){
  TrafficModel model=new TrafficModel(3);CinematicTraffic c=controller(model,1,2);VehicleLayout dimensions=layout();
  // Independent rendered front coordinate must hit the intended near/far side of the painted crossing.
  for(int lane=0;lane<4;lane++)for(int type=0;type<6;type++){
   double p=c.stopPosition(lane,type);float ground=CinematicGeometry.modelY(lane<2?160+p*.65:940-p*.65);
   float front=lane<2?ground:ground-dimensions.length(lane,type,ground);
   near(front,lane<2?810:902,.002,"Body-front stop, lane/type "+lane+"/"+type);
  }
  // Arrange each lane to arrive just after amber begins; preserve its circular index order.
  for(int lane=0;lane<4;lane++)for(int j=0;j<3;j++){
   TrafficModel.Car car=model.cars[lane*3+j];double lead=c.stopPosition(lane,car.type)-250;
   car.position=wrap(lead-(2-j)*240-((65+lane*5)*14));
  }
  int held=0;boolean brakeSeen=false;int[] firstStart={-1,-1,-1};
  for(int tick=0;tick<26*60;tick++){
   c.update(STEP);for(TrafficModel.Car car:model.cars){if(c.brake(car)>.5)brakeSeen=true;if(c.speed(car)<.05&&c.phase()==2)held++;}
  }
  check(held>100,"No real red-light hold");check(brakeSeen,"No actual brake signal");
  // Use the actual queued order in a lane at a later red, then watch movement after green.
  for(int i=0;i<26*60-1;i++)c.update(STEP);
  int[] queued={-1,-1,-1};int n=0;
  for(int i=0;i<3;i++)if(c.speed(model.cars[i])<.05)queued[n++]=i;
  check(n>=2,"Expected a multi-car queue");
  double[] origin=new double[3];for(int j=0;j<n;j++)origin[j]=model.cars[queued[j]].position;
  for(int tick=0;tick<8*60;tick++){
   c.update(STEP);for(int j=0;j<n;j++)if(firstStart[j]<0&&wrap(model.cars[queued[j]].position-origin[j])>.1)firstStart[j]=tick;
  }
  int distinct=0;for(int j=0;j<n;j++){check(firstStart[j]>=0,"Queue failed to depart");for(int k=0;k<j;k++)if(firstStart[j]!=firstStart[k])distinct++;}
  check(distinct>0,"Queue departed simultaneously");
 }
 private static void longTrajectories(){
  long samples=0;int stops=0,wraps=0;double worstDecel=0;VehicleLayout dimensions=layout();
  for(int count:new int[]{1,2,3,6})for(int mode=0;mode<4;mode++)for(double speed:new double[]{.5,1,1.5}){
   TrafficModel model=new TrafficModel(count);CinematicTraffic c=controller(model,speed,mode);
   double[] previous=new double[model.cars.length],previousVelocity=new double[model.cars.length];int[] types=new int[model.cars.length];
   for(int tick=0;tick<90*60;tick++){
    for(int i=0;i<previous.length;i++){previous[i]=model.cars[i].position;previousVelocity[i]=c.speed(model.cars[i]);types[i]=model.cars[i].type;}
    c.update(STEP);
    for(int i=0;i<previous.length;i++){
     TrafficModel.Car car=model.cars[i];double moved=wrap(car.position-previous[i]),v=c.speed(car);
     String at=" count="+count+" mode="+mode+" speed="+speed+" tick="+tick+" car="+i;
     check(Double.isFinite(v)&&v>=0&&car.position>=0&&car.position<1200,"Invalid trajectory"+at);
     check(moved<4,"Teleport/backwards movement"+at);if(car.position<previous[i])wraps++;
     check(v-previousVelocity[i]<=CinematicTraffic.ACCELERATION*STEP+.00001,"Acceleration jump"+at);
     worstDecel=Math.max(worstDecel,(previousVelocity[i]-v)/STEP);
     check(previousVelocity[i]-v<=CinematicTraffic.DECELERATION*STEP+.02,"Deceleration jump"+at+" old="+previousVelocity[i]+" new="+v);
     if(count>1){int leader=car.lane*count+(i%count+1)%count;double gap=wrap(model.cars[leader].position-car.position);check(gap>=149.99,"Circular minimum gap"+at);
      // Compare projected physical front/rear positions, independently of controller offsets.
      float y=CinematicGeometry.modelY(car.y()),ly=CinematicGeometry.modelY(model.cars[leader].y());
      float front=car.lane<2?y:y-dimensions.length(car.lane,car.type,y);
      float rear=car.lane<2?ly-dimensions.length(car.lane,model.cars[leader].type,ly):ly;
      if(tick>120&&types[i]==car.type&&model.cars[leader].position>car.position&&gap<500&&y>450&&y<1300&&ly>450&&ly<1300)
       check(car.lane<2?rear>front:rear<front,"Projected bodies overlap"+at+" front="+front+" rear="+rear);
     }
     if(c.phase()==2&&v<.05){stops++;check(c.brake(car)>.1,"Stationary hold missing brake"+at);}
     samples++;
    }
   }
  }
  check(stops>1000&&wraps>100,"Trajectories did not exercise holds/wraps");
  System.out.println("PASS: "+samples+" trajectory samples; "+stops+" red-held samples, "+wraps+" wraps; worst deceleration "+worstDecel);
 }
 private static void frameRatesAndToggles(){
  TrafficModel[] models={new TrafficModel(6),new TrafficModel(6),new TrafficModel(6)};
  CinematicTraffic[] cs={controller(models[0],1.5,0),controller(models[1],1.5,0),controller(models[2],1.5,0)};
  int[] fps={15,30,60};for(int j=0;j<3;j++)for(int i=0;i<fps[j]*50;i++)cs[j].update(1.0/fps[j]);
  for(int j=1;j<3;j++)for(int i=0;i<24;i++){near(models[0].cars[i].position,models[j].cars[i].position,1e-7,"Frame-rate changed trajectory");near(cs[0].speed(models[0].cars[i]),cs[j].speed(models[j].cars[i]),1e-7,"Frame-rate changed velocity");}
  check(cs[0].waiting()>0,"Toggle test needs stopped cars");
  double p=models[0].cars[0].position,v=cs[0].speed(models[0].cars[0]);cs[0].setEnabled(false);near(models[0].cars[0].position,p,0,"Disable teleported");near(cs[0].speed(models[0].cars[0]),v,0,"Disable jumped speed");
  for(int i=0;i<20*60;i++)cs[0].update(STEP);check(cs[0].waiting()==0,"Disabled queue stayed blocked");near(cs[0].seconds(),0,0,"Disabled clock advanced");
  p=models[0].cars[0].position;cs[0].setEnabled(true);near(models[0].cars[0].position,p,0,"Enable teleported");check(cs[0].phase()==0,"Enable must start green");
 }
 private static void amberDecision(){
  TrafficModel model=new TrafficModel(1);CinematicTraffic c=controller(model,1,0);
  for(int i=0;i<14*60-1;i++)c.update(STEP);
  // One car is too close to stop comfortably, the other has ample stopping room.
  model.cars[0].position=c.stopPosition(0,model.cars[0].type)-3;
  model.cars[1].position=c.stopPosition(1,model.cars[1].type)-170;
  double start=model.cars[0].position;
  for(int i=0;i<6*60;i++)c.update(STEP);
  check(wrap(model.cars[0].position-start)>300&&c.speed(model.cars[0])>40,"Amber committed car froze in crossing");
  near(model.cars[1].position,c.stopPosition(1,model.cars[1].type),.02,"Amber distant car did not stop");
  TrafficModel farModel=new TrafficModel(1);CinematicTraffic far=controller(farModel,1,0);
  for(int i=0;i<14*60-1;i++)far.update(STEP);
  farModel.cars[0].position=far.stopPosition(0,farModel.cars[0].type)-650;
  for(int i=0;i<9*60;i++)far.update(STEP);
  near(farModel.cars[0].position,far.stopPosition(0,farModel.cars[0].type),.02,"Far approaching car was misclassified as already through the signal");
 }
 private static double wrap(double x){return (x%1200+1200)%1200;}
}
