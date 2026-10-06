package com.thenathe.combinedshim;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.*;
import java.util.*;

/** Optional Polymer integration is separate from plain original-module bug fixes. */
public final class ModuleMixinPlugin implements IMixinConfigPlugin {
    private static final Map<String, String> MODULES = Map.of("ssopolymer", "simple_smithing_overhaul",
            "backpackcompat", "tiered_backpacks", "toolpouchcompat", "toolpouch", "mapstitchcompat", "mapstitch");
    private static final Set<String> ALWAYS = Set.of(
            "com.thenathe.ssopolymer.mixin.SmithingNetworkingMixin",
            "com.thenathe.ssopolymer.mixin.AnvilMenuMixin",
            "com.thenathe.ssopolymer.mixin.DataPatchesMixin",
            "com.thenathe.ssopolymer.mixin.MendingUseMixin",
            "com.thenathe.ssopolymer.mixin.MendingRepairCountMixin",
            "com.thenathe.ssopolymer.mixin.MendingWhetstoneSelectionMixin",
            "com.thenathe.ssopolymer.mixin.EnchantingPowerMixin",
            "com.thenathe.toolpouchcompat.mixin.ToolpouchAccess",
            "com.thenathe.toolpouchcompat.mixin.ShulkerOpenMixin",
            "com.thenathe.toolpouchcompat.mixin.ShulkerContainerMixin",
            "com.thenathe.toolpouchcompat.mixin.ShulkerSlotMixin");
    public static boolean polymerAvailable() {
        var loader = FabricLoader.getInstance();
        return loader.isModLoaded("polymer-core") && loader.isModLoaded("polymer-resource-pack");
    }
    public void onLoad(String name) {}
    public String getRefMapperConfig() { return null; }
    public boolean shouldApplyMixin(String target, String mixin) {
        if (mixin.equals("com.thenathe.combinedshim.mixin.RegistrySyncSchedulingMixin")) return true;
        if (!polymerAvailable() && !ALWAYS.contains(mixin)) return false;
        if (mixin.equals("com.thenathe.ssopolymer.mixin.DefaultedSyncMixin") && !FabricLoader.getInstance().isModLoaded("defaulted")) return false;
        for (var entry : MODULES.entrySet()) if (mixin.startsWith("com.thenathe." + entry.getKey() + ".mixin.")) return Modules.enabled(entry.getValue());
        return true;
    }
    public void acceptTargets(Set<String> mine, Set<String> others) {}
    public List<String> getMixins() { return null; }
    public void preApply(String name, ClassNode node, String mixin, IMixinInfo info) {}
    public void postApply(String name, ClassNode node, String mixin, IMixinInfo info) {}
}
