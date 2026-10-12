package suitesettingsqa;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import java.nio.file.*;
public final class SettingsServerQa implements ModInitializer {
 int ticks;
 public void onInitialize() { ServerTickEvents.END_SERVER_TICK.register(server -> {
  if(++ticks%20!=0)return;
  try{Files.writeString(Path.of(System.getProperty("settings.qa.control")).resolve("server-config.json"),"{\"preventShulkerDuplication\":"+me.pajic.misctweaks.MiscTweaks.CONFIG.preventShulkerDuplication.get()+",\"dropsIntoShulker\":"+com.thenathe.suite.features.FeatureConfig.CONFIG.dropsIntoShulker.get()+",\"savedDropsIntoShulker\":"+me.fzzyhmstrs.fzzy_config.api.ConfigApiJava.readOrCreateAndValidate(com.thenathe.suite.features.FeatureConfig::new).dropsIntoShulker.get()+"}");}catch(Exception e){throw new RuntimeException(e);}
 }); }
}
