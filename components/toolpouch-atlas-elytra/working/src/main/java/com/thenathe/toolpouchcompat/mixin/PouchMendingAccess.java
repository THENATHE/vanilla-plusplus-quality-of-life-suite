package com.thenathe.toolpouchcompat.mixin;

import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = ToolPouchUtil.class, remap = false)
public interface PouchMendingAccess {
    @Invoker("getToolPouch")
    static ItemStack toolpouchCompat$activePouch(Player player) { throw new AssertionError(); }
}
