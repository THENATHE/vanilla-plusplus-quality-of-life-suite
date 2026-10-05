package org.sharedregionmaps.mixin;

import java.util.List;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Replaces only nested metadata, retaining order, counts and selected entry. */
@Mixin(BundleContents.class)
public interface BundleContentsAccessor {
    @Invoker("<init>")
    static BundleContents sharedmaps$create(List<ItemStackTemplate> items, int selectedItem) {
        throw new AssertionError("Mixin constructor invoker was not applied");
    }
}
