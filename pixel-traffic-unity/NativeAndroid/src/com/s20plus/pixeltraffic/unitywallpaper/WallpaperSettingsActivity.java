package com.s20plus.pixeltraffic.unitywallpaper;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.unity3d.player.R;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public final class WallpaperSettingsActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); setTitle(R.string.pixel_traffic_wallpaper_name);
        ScrollView scroll = new ScrollView(this); LinearLayout layout = new LinearLayout(this); layout.setOrientation(LinearLayout.VERTICAL);
        int gap = Math.round(20 * getResources().getDisplayMetrics().density); layout.setPadding(gap, gap, gap, gap);
        scroll.setFitsSystemWindows(true); scroll.addView(layout); setContentView(scroll);
        TextView title = new TextView(this); title.setText(R.string.pixel_traffic_settings_title); title.setTextSize(24); layout.addView(title);
        TextView description = new TextView(this); description.setText(R.string.pixel_traffic_settings_description); description.setPadding(0, gap, 0, gap); layout.addView(description);
        selection(layout, R.string.pixel_traffic_time_label, R.array.pixel_traffic_times, WallpaperPreferences.theme(this), true);
        selection(layout, R.string.pixel_traffic_weather_label, R.array.pixel_traffic_weather, WallpaperPreferences.weather(this), false);
        TextView population = new TextView(this); population.setTextSize(18); layout.addView(population);
        SeekBar people = new SeekBar(this); people.setMax(24); people.setProgress((WallpaperPreferences.population(this) - 4) / 4); people.setContentDescription(getString(R.string.pixel_traffic_population_description));
        population.setText(getString(R.string.pixel_traffic_population, WallpaperPreferences.population(this)));
        people.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) { int count = 4 + progress * 4; population.setText(getString(R.string.pixel_traffic_population, count)); if (fromUser) WallpaperPreferences.savePopulation(WallpaperSettingsActivity.this, count); }
            @Override public void onStartTrackingTouch(SeekBar bar) { }
            @Override public void onStopTrackingTouch(SeekBar bar) { }
        }); layout.addView(people);
        CheckBox economy = new CheckBox(this); economy.setText(R.string.pixel_traffic_economy); economy.setChecked(WallpaperPreferences.economy(this));
        economy.setOnCheckedChangeListener((button, checked) -> WallpaperPreferences.saveEconomy(this, checked)); layout.addView(economy);
        TextView saving = new TextView(this); saving.setText(R.string.pixel_traffic_saving); saving.setPadding(0, gap / 2, 0, gap); layout.addView(saving);
        Button apply = new Button(this); apply.setText(R.string.pixel_traffic_apply); apply.setOnClickListener(view -> openWallpaper()); layout.addView(apply);
    }
    private void selection(LinearLayout layout, int label, int choices, int selected, boolean time) {
        TextView heading = new TextView(this); heading.setText(label); heading.setTextSize(18); layout.addView(heading);
        Spinner picker = new Spinner(this); picker.setContentDescription(getString(label));
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, choices, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); picker.setAdapter(adapter); picker.setSelection(selected);
        picker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (time) WallpaperPreferences.saveTheme(WallpaperSettingsActivity.this, position);
                else WallpaperPreferences.saveWeather(WallpaperSettingsActivity.this, position);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        }); layout.addView(picker);
    }
    private void openWallpaper() {
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(this, PixelTrafficWallpaperService.class));
        try { startActivity(intent); }
        catch (ActivityNotFoundException first) {
            try { startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)); }
            catch (ActivityNotFoundException unavailable) { Toast.makeText(this, R.string.pixel_traffic_unavailable, Toast.LENGTH_LONG).show(); }
        }
    }
}
