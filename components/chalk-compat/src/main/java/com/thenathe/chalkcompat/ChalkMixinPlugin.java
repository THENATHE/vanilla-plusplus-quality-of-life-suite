package com.thenathe.chalkcompat;

import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Avoid resolving optional Chalk/Polymer classes on a client that only installs the companion. */
public final class ChalkMixinPlugin implements IMixinConfigPlugin {
    public void onLoad(String mixinPackage) {}
    public String getRefMapperConfig() { return null; }
    public boolean shouldApplyMixin(String target, String mixin) {
        var loader = FabricLoader.getInstance();
        if (mixin.endsWith("ChalkInitializationMixin")) return loader.isModLoaded("chalk");
        return loader.isModLoaded("chalk") && loader.isModLoaded("polymer-core")
                && loader.isModLoaded("polymer-resource-pack") && loader.isModLoaded("polymer-virtual-entity");
    }
    public void acceptTargets(Set<String> mine, Set<String> others) {}
    public List<String> getMixins() { return null; }
    public void preApply(String name, ClassNode node, String mixin, IMixinInfo info) {}
    public void postApply(String name, ClassNode node, String mixin, IMixinInfo info) {}
}
