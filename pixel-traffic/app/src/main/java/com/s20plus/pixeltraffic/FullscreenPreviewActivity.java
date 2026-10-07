package com.s20plus.pixeltraffic;
import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.view.*;
public class FullscreenPreviewActivity extends Activity {
 private ScenePreviewView preview;
 @Override public void onCreate(Bundle state){super.onCreate(state);FrameLayout root=new FrameLayout(this);preview=new ScenePreviewView(this);root.addView(preview,new FrameLayout.LayoutParams(-1,-1));Button close=new Button(this);close.setText("설정으로 돌아가기");FrameLayout.LayoutParams position=new FrameLayout.LayoutParams(-2,-2,Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);root.addView(close,position);close.setOnClickListener(v->finish());setContentView(root);root.setOnApplyWindowInsetsListener((v,insets)->{if(android.os.Build.VERSION.SDK_INT>=30){android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);}else v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});}
 @Override protected void onResume(){super.onResume();preview.startPreview();}
 @Override protected void onPause(){preview.stopPreview();super.onPause();}
 @Override protected void onDestroy(){preview.release();super.onDestroy();}
}
