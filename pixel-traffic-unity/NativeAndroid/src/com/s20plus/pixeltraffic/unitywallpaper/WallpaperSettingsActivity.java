package com.s20plus.pixeltraffic.unitywallpaper;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public final class WallpaperSettingsActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); setTitle("픽셀 트래픽 Unity");
        ScrollView scroll = new ScrollView(this); LinearLayout layout = new LinearLayout(this); layout.setOrientation(LinearLayout.VERTICAL);
        int gap = Math.round(20 * getResources().getDisplayMetrics().density); layout.setPadding(gap, gap, gap, gap);
        scroll.addView(layout); setContentView(scroll);
        TextView title = new TextView(this); title.setText("움직이는 도시를 홈 화면에"); title.setTextSize(24); layout.addView(title);
        TextView description = new TextView(this); description.setText("차량과 사람들이 오가는 도시를 미리 보고 배경화면으로 적용하세요. 화면이 꺼지거나 배경화면이 보이지 않을 때는 움직임을 멈춥니다."); description.setPadding(0, gap, 0, gap); layout.addView(description);
        TextView population = new TextView(this); population.setTextSize(18); layout.addView(population);
        SeekBar people = new SeekBar(this); people.setMax(24); people.setProgress((WallpaperPreferences.population(this) - 4) / 4); people.setContentDescription("보행자 수 4명부터 100명");
        population.setText("보행자 " + WallpaperPreferences.population(this) + "명");
        people.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) { int count = 4 + progress * 4; population.setText("보행자 " + count + "명"); if (fromUser) WallpaperPreferences.savePopulation(WallpaperSettingsActivity.this, count); }
            @Override public void onStartTrackingTouch(SeekBar bar) { }
            @Override public void onStopTrackingTouch(SeekBar bar) { }
        }); layout.addView(people);
        CheckBox economy = new CheckBox(this); economy.setText("절전 모드 · 15 FPS 목표"); economy.setChecked(WallpaperPreferences.economy(this));
        economy.setOnCheckedChangeListener((button, checked) -> WallpaperPreferences.saveEconomy(this, checked)); layout.addView(economy);
        TextView saving = new TextView(this); saving.setText("설정은 자동 저장됩니다. 기본은 32명 · 30 FPS 목표이며, 기기 절전 모드에서는 15 FPS 목표로 동작합니다."); saving.setPadding(0, gap / 2, 0, gap); layout.addView(saving);
        Button apply = new Button(this); apply.setText("배경화면 미리보기 및 적용"); apply.setOnClickListener(view -> openWallpaper()); layout.addView(apply);
    }
    private void openWallpaper() {
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(this, PixelTrafficWallpaperService.class));
        try { startActivity(intent); }
        catch (ActivityNotFoundException first) {
            try { startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)); }
            catch (ActivityNotFoundException unavailable) { Toast.makeText(this, "기기의 배경화면 설정에서 픽셀 트래픽 Unity를 선택해 주세요.", Toast.LENGTH_LONG).show(); }
        }
    }
}
