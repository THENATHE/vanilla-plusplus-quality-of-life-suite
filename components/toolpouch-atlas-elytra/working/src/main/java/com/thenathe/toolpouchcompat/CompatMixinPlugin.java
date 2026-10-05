package com.thenathe.toolpouchcompat;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.*;
import java.util.*;
public final class CompatMixinPlugin implements IMixinConfigPlugin {
 public void onLoad(String mixinPackage) {}
 public String getRefMapperConfig() { return null; }
 public boolean shouldApplyMixin(String target, String mixin) {
  if (mixin.substring(mixin.lastIndexOf('.') + 1).startsWith("ClientSort")) return FabricLoader.getInstance().isModLoaded("clientsort");
  if (mixin.endsWith(".PouchSlotQuotaMixin")) return true;
  if (mixin.endsWith(".PouchCapacityMixin") || mixin.endsWith(".PouchAttachmentTierMixin") || mixin.endsWith(".PouchDetachmentTierMixin")) return true;
  if (mixin.substring(mixin.lastIndexOf('.') + 1).startsWith("PouchMending")) return true;
  if (mixin.contains("HudLayout") || mixin.endsWith(".PouchInfoLayoutMixin")) return FabricLoader.getInstance().isModLoaded("mapstitch");
  if (mixin.endsWith(".AtlasMinimapCacheMixin")) return FabricLoader.getInstance().isModLoaded("mapstitch");
  if (mixin.substring(mixin.lastIndexOf('.') + 1).startsWith("Atlas"))
   return FabricLoader.getInstance().isModLoaded("mapstitch") && !Features.NATIVE_ATLAS;
  return !Features.NATIVE_ELYTRA;
 }
 public void acceptTargets(Set<String> mine, Set<String> others) {}
 public List<String> getMixins() { return null; }
 public void preApply(String name, ClassNode node, String mixin, IMixinInfo info) {}
 public void postApply(String name, ClassNode node, String mixin, IMixinInfo info) {}
}
