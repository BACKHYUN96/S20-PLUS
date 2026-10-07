package com.s20plus.pixeltraffic;

/** Circular tracks extend beyond both ends of the visible road. No Android dependencies. */
final class TrafficModel {
 static final double TRACK_LENGTH=1200, MIN_GAP=150, STEP=1.0/60;
 static final int LANES=4, PER_LANE=3;
 static final class Car {
  final int lane;
  double position;
  int type;
  Car(int lane,double position,int type) { this.lane=lane; this.position=position; this.type=type; }
  double y() { return lane<2?160+position*.65:940-position*.65; }
 }
 final Car[] cars;
 private final double[] advances;
 private final int perLane;
 private double speedMultiplier=1;
 private double remainder;
 private int vehicleMode;
 void setVehicleMode(int mode) {vehicleMode=Math.max(0,Math.min(3,mode));for(Car c:cars)c.type=allowed(c.type);}
 private int allowed(int type) {return vehicleMode==1?type%4:vehicleMode==2?4+type%2:vehicleMode==3?3:type%6;}
 TrafficModel() { this(PER_LANE); }
 TrafficModel(int countPerLane) {
  perLane=Math.max(1,Math.min(6,countPerLane));
  cars=new Car[LANES*perLane];advances=new double[cars.length];
  for(int lane=0;lane<LANES;lane++) for(int i=0;i<perLane;i++) {
   int index=lane*perLane+i;
   cars[index]=new Car(lane,(i*TRACK_LENGTH/perLane+lane*67)%TRACK_LENGTH,index%6);
  }
 }
 void setSpeedMultiplier(double value) { speedMultiplier=Double.isFinite(value)?Math.max(.5,Math.min(1.5,value)):1; }

 void update(double seconds) {
  if(!Double.isFinite(seconds)||seconds<=0) return;
  remainder+=Math.min(seconds,.1);
  while(remainder+1e-10>=STEP) { tick(); remainder-=STEP; }
 }
 private void tick() {
  for(int i=0;i<cars.length;i++) {
   Car car=cars[i];
   Car front=cars[car.lane*perLane+(i%perLane+1)%perLane];
   double gap=perLane==1?TRACK_LENGTH:(front.position-car.position+TRACK_LENGTH)%TRACK_LENGTH;
   double speed=(car.type>=4?65:85+car.type*7)+car.lane*5;
   advances[i]=Math.min(speed*speedMultiplier*STEP,Math.max(0,gap-MIN_GAP));
  }
  for(int i=0;i<cars.length;i++) {
   Car car=cars[i]; car.position+=advances[i];
   if(car.position>=TRACK_LENGTH) { car.position-=TRACK_LENGTH; car.type=allowed(car.type+1); }
  }
 }
}
