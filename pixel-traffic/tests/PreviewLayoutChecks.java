package com.s20plus.pixeltraffic;
public final class PreviewLayoutChecks {
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args) {
  for(int[] size:new int[][]{{1080,600},{591,350},{320,800},{1600,200},{1,1}}) {
   PreviewLayout.Bounds b=PreviewLayout.fit(size[0],size[1]);
   check(b.left>=-.001&&b.top>=-.001&&b.right<=size[0]+.001&&b.bottom<=size[1]+.001,"Fits without crop");
   check(Math.abs((b.right-b.left)/(b.bottom-b.top)-360f/800)<.0001,"Portrait aspect preserved");
   check(Math.abs(b.left-(size[0]-b.right))<.001&&Math.abs(b.top-(size[1]-b.bottom))<.001,"Centered");
  }
  check(PreviewLayout.fit(0,100).right==0&&PreviewLayout.fit(100,0).bottom==0,"Empty layout safe");
  System.out.println("PASS: portrait/landscape/compact preview fit, centered aspect and empty layout");
 }
}
