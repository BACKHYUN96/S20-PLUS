package com.s20plus.pixeltraffic;
public class SceneryChecks {
 static void check(boolean ok){if(!ok)throw new AssertionError();}
 public static void main(String[] args){SceneryClock a=new SceneryClock(),b=new SceneryClock();for(int i=0;i<9000;i++)a.update(1.0/30);for(int i=0;i<18000;i++)b.update(1.0/60);check(Math.abs(a.seconds()-b.seconds())<1e-7);check(a.seconds()>=0&&a.seconds()<120);double old=a.seconds();a.enabled=false;a.update(.1);check(a.seconds()==old);a.enabled=true;a.update(Double.NaN);a.update(-1);a.update(Double.POSITIVE_INFINITY);check(a.seconds()==old);a.update(30);check(Math.abs(a.seconds()-old-.1)<1e-7);System.out.println("PASS: 5-minute 30/60 phase equivalence, wrapping, disable, invalid delta, stall cap");}
}
