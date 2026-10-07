package com.s20plus.pixeltraffic;

public class SceneEventChecks {
 private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 private static void advance(SceneEventModel model,int fps,int seconds){for(int i=0;i<fps*seconds;i++)model.update(1.0/fps);}
 public static void main(String[] args) {
  SceneEventModel rare=new SceneEventModel(),normal=new SceneEventModel(),frequent=new SceneEventModel();
  rare.configure(true,0);normal.configure(true,1);frequent.configure(true,2);
  advance(rare,60,18);advance(normal,60,10);advance(frequent,60,5);
  check(rare.visible()&&normal.visible()&&frequent.visible(),"first event respects frequency waits");
  check(rare.starts()==1&&normal.starts()==1&&frequent.starts()==1,"one first spawn");
  advance(rare,60,10);check(!rare.visible(),"event finishes at ten active seconds");
  SceneEventModel a=new SceneEventModel(),b=new SceneEventModel();advance(a,30,600);advance(b,60,600);
  check(a.starts()==b.starts()&&a.visible()==b.visible()&&Math.abs(a.progress()-b.progress())<1e-7,"30/60 cadence equivalence");
  check(a.starts()==15,"normal ten-minute cadence");
  SceneEventModel r=new SceneEventModel(),f=new SceneEventModel();r.configure(true,0);f.configure(true,2);advance(r,60,600);advance(f,60,600);
  check(r.starts()<a.starts()&&f.starts()>a.starts(),"frequency changes event count");
  SceneEventModel stopped=new SceneEventModel();advance(stopped,60,12);double old=stopped.progress();long starts=stopped.starts();
  stopped.configure(false,1);advance(stopped,60,120);check(!stopped.visible()&&stopped.progress()==old&&stopped.starts()==starts,"disabled time freezes active event");
  stopped.configure(true,1);check(stopped.visible(),"resume keeps active event");
  stopped.update(Double.NaN);stopped.update(Double.POSITIVE_INFINITY);stopped.update(-1);check(stopped.progress()==old,"invalid time rejected");
  stopped.update(300);check(Math.abs(stopped.progress()-old-.01)<1e-9,"long stall capped to100ms");
  stopped.configure(true,2);check(!stopped.visible(),"frequency change resets cadence cleanly");
  advance(stopped,60,5);check(stopped.visible(),"new frequency first wait applied");
  System.out.println("PASS: first spawn/cadence, ten-second expiry, 30/60 equality, frequency, disable/resume and stall cap");
 }
}
