package com.s20plus.pixeltraffic;
public class ExpansionChecks {
 private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 public static void main(String[] args){
  ThemeBlend b=new ThemeBlend();b.select(2);for(int i=0;i<120;i++)b.update(1.0/60);
  check(Math.abs(b.weights[1]-.5)<1e-8&&Math.abs(b.weights[2]-.5)<1e-8,"blend midpoint");
  double before=b.weights[2];b.select(0);check(b.weights[2]==before,"retarget continuity");
  b.update(Double.NaN);for(int i=0;i<240;i++)b.update(1.0/60);
  check(Math.abs(b.weights[0]-1)<1e-8,"retarget completes");
  for(int mode=0;mode<4;mode++)for(int density=1;density<=3;density++){
   TrafficModel t=new TrafficModel(density);double position=t.cars[0].position;t.setVehicleMode(mode);check(t.cars[0].position==position,"selection preserves position");
   for(int i=0;i<36000;i++){t.update(1.0/60);for(TrafficModel.Car c:t.cars)check(mode==0?c.type>=0&&c.type<6:mode==1?c.type<4:mode==2?c.type>=4:c.type==3,"selected fleet wraps");}
  }
  SnowModel a=new SnowModel(),c=new SnowModel();double first=a.y[0];a.update(1);check(first==a.y[0],"disabled snow stops");a.enabled=c.enabled=true;
  for(int i=0;i<3600;i++)a.update(1.0/30);for(int i=0;i<7200;i++)c.update(1.0/60);
  for(int i=0;i<48;i++){check(Math.abs(a.x[i]-c.x[i])<1e-8&&Math.abs(a.y[i]-c.y[i])<1e-8,"snow rate independence");check(a.x[i]>=0&&a.x[i]<360&&a.y[i]>=0&&a.y[i]<=800,"snow wraps");}
  System.out.println("PASS: blend continuity/completion, all fleet filters over 10 minutes per density, snow pause/wrap/30-60 equivalence");
 }
}
