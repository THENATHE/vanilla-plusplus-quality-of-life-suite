package com.thenathe.toolpouchcompat.mixin;

import me.pajic.toolpouch.menu.ToolPouchMenu;
import net.minecraft.world.SimpleContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ToolPouchMenu.class, remap = false)
public interface PouchMendingMenuAccess {
    @Accessor("container")
    SimpleContainer toolpouchCompat$liveContents();
}
