package com.s20plus.pixeltraffic;
import java.util.Random;
/** Fixed deterministic snow pool, capped fixed-step simulation. */
final class SnowModel {
 final double[] x=new double[48],y=new double[48];
 private double remainder;boolean enabled;
 SnowModel(){Random r=new Random(712);for(int i=0;i<48;i++){x[i]=r.nextDouble()*360;y[i]=r.nextDouble()*800;}}
 void update(double seconds){if(!enabled||!Double.isFinite(seconds)||seconds<=0)return;remainder+=Math.min(.1,seconds);while(remainder+1e-10>=1.0/60){for(int i=0;i<48;i++){y[i]+=(20+i%7*5)/60.0;x[i]+=(i%2==0?9:-7)/60.0;if(y[i]>800)y[i]-=800;if(x[i]<0)x[i]+=360;if(x[i]>=360)x[i]-=360;}remainder-=1.0/60;}}
}
