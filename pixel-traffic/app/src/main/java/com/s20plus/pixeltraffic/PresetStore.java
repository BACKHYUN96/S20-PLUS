package com.s20plus.pixeltraffic;
import android.content.Context;
import android.content.SharedPreferences;
import org.json.*;
import java.util.*;

/** Versioned local snapshots. Apply only after complete validation. */
final class PresetStore {
 private static final String[] KEYS={"road","vehicles","palette","theme","weather","brightness","density","speed","fps","camera","camera_position","event_frequency"};
 private static final int[] DEFAULTS={0,0,0,1,0,0,2,1,1,0,1,1},MAX={3,3,2,2,4,2,3,2,2,1,2,2};
 private final SharedPreferences store,current;
 PresetStore(Context context){store=context.getSharedPreferences("saved_presets",Context.MODE_PRIVATE);current=WallpaperSettings.prefs(context);}
 String[] names(){List<String> names=new ArrayList<>();for(String key:store.getAll().keySet())if(key.startsWith("preset:"))names.add(key.substring(7));Collections.sort(names);return names.toArray(new String[0]);}
 boolean exists(String name){return store.contains("preset:"+name);}
 void save(String name) throws JSONException {
  name=name.trim();if(name.isEmpty()||name.codePointCount(0,name.length())>32)throw new IllegalArgumentException("이름은 1~32자로 입력해주세요.");
  if(!exists(name)&&names().length>=10)throw new IllegalArgumentException("최대 10개까지 저장할 수 있어요. 기존 프리셋을 삭제하거나 덮어써주세요.");
  JSONObject value=new JSONObject();value.put("schema",1);
  for(int i=0;i<KEYS.length;i++)value.put(KEYS[i],WallpaperSettings.index(current,KEYS[i],DEFAULTS[i]));
  value.put("scenery_animation",current.getBoolean("scenery_animation",true));
  value.put("scene_events",current.getBoolean("scene_events",true));
  value.put("cinematic_city",current.getBoolean("cinematic_city",false));
  value.put("traffic_signals",current.getBoolean("traffic_signals",true));
  value.put("auto_time",current.getBoolean("auto_time",false));value.put("follow_saver",current.getBoolean("follow_saver",true));
  store.edit().putString("preset:"+name,value.toString()).apply();
 }
 void load(String name) throws JSONException {
  String raw=store.getString("preset:"+name,null);if(raw==null)throw new JSONException("프리셋을 찾을 수 없습니다.");
  JSONObject value=new JSONObject(raw);validate(value);
  int[] selected=new int[KEYS.length];for(int i=0;i<KEYS.length;i++)selected[i]=value.has(KEYS[i])?value.getInt(KEYS[i]):DEFAULTS[i];
  boolean automatic=value.getBoolean("auto_time"),saver=value.getBoolean("follow_saver");
  boolean scenery=value.has("scenery_animation")?value.getBoolean("scenery_animation"):true;
  boolean events=value.has("scene_events")?value.getBoolean("scene_events"):true;
  boolean cinematic=value.has("cinematic_city")?value.getBoolean("cinematic_city"):false;
  boolean signals=!value.has("traffic_signals")||value.getBoolean("traffic_signals");
  SharedPreferences.Editor edit=current.edit();for(int i=0;i<KEYS.length;i++)edit.putInt(KEYS[i],selected[i]);edit.putBoolean("auto_time",automatic).putBoolean("follow_saver",saver).putBoolean("scenery_animation",scenery).putBoolean("scene_events",events).putBoolean("cinematic_city",cinematic).putBoolean("traffic_signals",signals).apply();
 }
 private static void validate(JSONObject value) throws JSONException {
  if(value.getInt("schema")!=1)throw new JSONException("지원하지 않는 형식");
  for(int i=0;i<KEYS.length;i++){int v=i>=9&&!value.has(KEYS[i])?DEFAULTS[i]:value.getInt(KEYS[i]);if(v<0||v>MAX[i])throw new JSONException("설정 범위 오류");}
  if(value.has("scene_events")&&!(value.get("scene_events") instanceof Boolean))throw new JSONException("이벤트 설정 형식 오류");
  if(value.has("traffic_signals")&&!(value.get("traffic_signals") instanceof Boolean))throw new JSONException("신호 설정 형식 오류");
  if(value.has("cinematic_city")&&!(value.get("cinematic_city") instanceof Boolean))throw new JSONException("전용 아트 설정 형식 오류");
  if(value.has("event_frequency")){Object frequency=value.get("event_frequency");if(!(frequency instanceof Number)||((Number)frequency).doubleValue()!=value.getInt("event_frequency"))throw new JSONException("이벤트 빈도 형식 오류");}
  value.getBoolean("auto_time");value.getBoolean("follow_saver");if(value.has("scenery_animation"))value.getBoolean("scenery_animation");
 }
 private static void validName(String name){if(name.isEmpty()||name.codePointCount(0,name.length())>32)throw new IllegalArgumentException("이름은 1~32자여야 합니다.");}
 void rename(String oldName,String newName){newName=newName.trim();validName(newName);if(oldName.equals(newName))return;if(exists(newName))throw new IllegalArgumentException("이미 있는 이름입니다.");String raw=store.getString("preset:"+oldName,null);if(raw==null)throw new IllegalArgumentException("프리셋이 없습니다.");store.edit().putString("preset:"+newName,raw).remove("preset:"+oldName).apply();}
 String exportAll() throws JSONException {
  JSONArray items=new JSONArray();for(String name:names()){JSONObject settings=new JSONObject(store.getString("preset:"+name,""));validate(settings);items.put(new JSONObject().put("name",name).put("settings",settings));}
  return new JSONObject().put("format","pixel-traffic-presets").put("version",1).put("presets",items).toString(2);
 }
 int importAll(String raw) throws JSONException {
  if(raw.length()>131072)throw new JSONException("파일이 너무 큽니다.");JSONObject file=new JSONObject(raw);
  if(!file.getString("format").equals("pixel-traffic-presets")||file.getInt("version")!=1)throw new JSONException("지원하지 않는 파일입니다.");
  JSONArray items=file.getJSONArray("presets");if(items.length()>10)throw new JSONException("최대 10개까지 가져올 수 있습니다.");
  Map<String,String> pending=new LinkedHashMap<>();
  for(int i=0;i<items.length();i++){JSONObject item=items.getJSONObject(i);String name=item.getString("name").trim();try{validName(name);}catch(IllegalArgumentException e){throw new JSONException(e.getMessage());}if(exists(name)||pending.containsKey(name))throw new JSONException("중복 이름: "+name+". 기존 이름을 변경한 뒤 다시 가져와주세요.");JSONObject settings=item.getJSONObject("settings");validate(settings);pending.put(name,settings.toString());}
  if(names().length+pending.size()>10)throw new JSONException("저장 공간은 최대 10개입니다.");
  SharedPreferences.Editor edit=store.edit();for(Map.Entry<String,String> item:pending.entrySet())edit.putString("preset:"+item.getKey(),item.getValue());edit.apply();return pending.size();
 }
 void recommended(int index) {
  SharedPreferences.Editor edit=current.edit();for(int i=0;i<KEYS.length;i++)edit.putInt(KEYS[i],DEFAULTS[i]);
  boolean art=index==4,cityStyle=index==3||art;
  edit.putInt("road",index==0||cityStyle?0:index==1?1:2).putInt("theme",cityStyle?1:index==0?2:0).putInt("weather",index==0||cityStyle?2:index==1?0:3).putInt("density",cityStyle?3:1).putInt("palette",index==1?1:0).putBoolean("auto_time",false).putBoolean("follow_saver",true).putBoolean("scenery_animation",true).putBoolean("scene_events",true).putBoolean("cinematic_city",art).putBoolean("traffic_signals",true).apply();
 }
 void resetCurrent(){current.edit().clear().apply();}
 void delete(String name){store.edit().remove("preset:"+name).apply();}
}
