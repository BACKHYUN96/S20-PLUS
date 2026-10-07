package com.s20plus.pixeltraffic;
/** Local-clock schedule, not sunrise/sunset or location data. */
final class SceneTimePolicy {
 static int themeForHour(int hour) {
  int local=Math.floorMod(hour,24);
  return local>=6&&local<17?0:local>=17&&local<20?1:2;
 }
}
