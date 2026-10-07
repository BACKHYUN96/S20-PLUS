package android.content.res;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/** Desktop asset directory adapter; accepts only paths inside its root. */
public final class AssetManager {
 private final Path root;
 public AssetManager(Path directory) {root=directory.toAbsolutePath().normalize();}
 public InputStream open(String name)throws IOException {
  Path file=root.resolve(name).normalize();
  if(!file.startsWith(root))throw new IOException("Asset outside root");
  return Files.newInputStream(file);
 }
}
