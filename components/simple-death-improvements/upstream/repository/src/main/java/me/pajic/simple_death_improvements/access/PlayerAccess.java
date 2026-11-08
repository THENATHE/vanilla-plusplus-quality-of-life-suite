package me.pajic.simple_death_improvements.access;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface PlayerAccess {
    @Nullable GlobalPos sdi$getLastSafePos();
    @Nullable Vec3 sdi$getDeathDropPos();
    boolean sdi$markKeptDamagedStack(ItemStack stack);
}
