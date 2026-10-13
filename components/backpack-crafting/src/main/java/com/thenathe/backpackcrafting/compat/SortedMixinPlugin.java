package com.thenathe.backpackcrafting.compat;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Avoids loading optional Sorted classes when the supported mod is absent. */
public final class SortedMixinPlugin implements IMixinConfigPlugin {
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return SortedCompat.isSupported()
                && (!mixinClassName.endsWith(".SortedPortableMixin") || SortedCompat.supportsPortableMenus());
    }

    @Override public void onLoad(String mixinPackage) { }
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String name, ClassNode node, String mixin, IMixinInfo info) { }
    @Override public void postApply(String name, ClassNode node, String mixin, IMixinInfo info) { }
}
