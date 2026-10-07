package com.s20plus.pixeltraffic;
/** Bounded active-time phase shared by scenery effects. Hidden time never advances. */
final class SceneryClock {
 private double seconds;
 boolean enabled=true;
 void update(double elapsed){if(enabled&&Double.isFinite(elapsed)&&elapsed>0)seconds=(seconds+Math.min(.1,elapsed))%120;}
 double seconds(){return seconds;}
}
