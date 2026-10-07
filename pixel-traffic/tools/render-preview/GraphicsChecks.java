package com.s20plus.pixeltraffic;

import android.graphics.*;

/** Meaningful shim checks, kept separate from scene implementation assertions. */
public final class GraphicsChecks {
 public static void main(String[] args) {
  Bitmap bitmap=Bitmap.createBitmap(8,8,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(bitmap);Paint p=new Paint();
  canvas.drawColor(0xff0000ff,PorterDuff.Mode.SRC);p.setColor(0x80ff0000);canvas.drawRect(0,0,4,4,p);
  int mixed=bitmap.image.getRGB(1,1);check((mixed>>>24)==255&&Math.abs(((mixed>>>16)&255)-128)<=1&&Math.abs((mixed&255)-127)<=1,"source-over color alpha");
  canvas.drawColor(0x40112233,PorterDuff.Mode.SRC);check(bitmap.image.getRGB(1,1)==0x40112233,"SRC resets, rather than accumulates, alpha");
  canvas.drawColor(Color.BLACK,PorterDuff.Mode.SRC);canvas.save();canvas.translate(2,2);canvas.clipRect(0,0,2,2);p.setColor(Color.WHITE);canvas.drawRect(-3,-3,6,6,p);canvas.restore();
  check(bitmap.image.getRGB(2,2)==Color.WHITE&&bitmap.image.getRGB(1,2)==Color.BLACK&&bitmap.image.getRGB(4,2)==Color.BLACK,"transformed clip and save/restore");
  p.setColor(0xffff0000);canvas.drawRect(0,0,1,1,p);check(bitmap.image.getRGB(0,0)==0xffff0000,"restore removes transform and clip");
  Bitmap source=Bitmap.createBitmap(2,2,Bitmap.Config.ARGB_8888);Canvas sourceCanvas=new Canvas(source);sourceCanvas.drawColor(0xff00ff00,PorterDuff.Mode.SRC);
  p.setColor(Color.WHITE);p.setAlpha(128);canvas.drawBitmap(source,5,5,p);int green=bitmap.image.getRGB(5,5);check(((green>>>8)&255)==128,"bitmap Paint alpha");
  canvas.drawColor(Color.BLACK,PorterDuff.Mode.SRC);p.setAlpha(255);canvas.drawBitmap(source,null,new RectF(1,1,5,5),p);
  check(bitmap.image.getRGB(1,1)==0xff00ff00&&bitmap.image.getRGB(4,4)==0xff00ff00&&bitmap.image.getRGB(5,4)==Color.BLACK,"bitmap destination bounds");
  canvas.save();canvas.translate(1,1);canvas.clipRect(1,1,3,3);canvas.drawColor(0xff0000ff,PorterDuff.Mode.SRC);canvas.restore();
  check(bitmap.image.getRGB(2,2)==0xff0000ff&&bitmap.image.getRGB(1,1)==0xff00ff00,"drawColor uses device-space clip, independent of transform");
  canvas.drawColor(Color.BLACK,PorterDuff.Mode.SRC);canvas.save();canvas.translate(1,1);
  Path triangle=new Path();triangle.moveTo(0,0);triangle.lineTo(4,0);triangle.lineTo(0,4);triangle.close();canvas.clipPath(triangle);
  p.setColor(Color.WHITE);canvas.drawRect(-4,-4,12,12,p);canvas.restore();
  check(bitmap.image.getRGB(1,1)==Color.WHITE&&bitmap.image.getRGB(4,4)==Color.BLACK,"transformed path clip");
  System.out.println("PASS: desktop graphics SRC/source-over alpha, save/restore, transformed clip, bitmap opacity, destination bounds");
 }
 private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
