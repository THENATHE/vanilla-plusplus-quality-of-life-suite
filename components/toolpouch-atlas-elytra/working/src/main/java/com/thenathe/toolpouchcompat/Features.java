package com.thenathe.toolpouchcompat;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
public final class Features {
 public static final boolean ATLAS_ALREADY_MIGRATED = migratedConfig();
 public static final boolean NATIVE_ATLAS = hasClass("mapstitch", "me/pajic/mapstitch/compat/ToolPouchCompat.class");
 private static boolean hasClass(String mod, String path) {
  return FabricLoader.getInstance().getModContainer(mod).flatMap(c -> c.findPath(path)).filter(Files::exists).isPresent();
 }
 private static boolean migratedConfig() {
  var path=FabricLoader.getInstance().getConfigDir().resolve("toolpouch/config.toml");
  if (!Files.exists(path)) return false;
  try {
   var match=java.util.regex.Pattern.compile("(?m)^version\\s*=\\s*(\\d+)").matcher(Files.readString(path));
   return match.find() && Integer.parseInt(match.group(1))>=2;
  } catch (java.io.IOException e) { throw new IllegalStateException("Cannot inspect existing atlas migration",e); }
 }
 private Features() {}
}
