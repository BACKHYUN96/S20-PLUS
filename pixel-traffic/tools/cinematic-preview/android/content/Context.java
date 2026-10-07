package android.content;

import android.content.res.AssetManager;
import java.nio.file.Path;

/** Minimal asset context for desktop render tests. */
public class Context {
 private final AssetManager assets;
 public Context(Path root) {assets=new AssetManager(root);}
 public AssetManager getAssets() {return assets;}
 public Context getApplicationContext() {return this;}
}
