package com.thenathe.suite.pets.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerPlayer.class)
public interface ShoulderReleaseAccessor {
    @Invoker("respawnEntityOnShoulder")
    void vpp$releaseShoulderEntity(CompoundTag entity);

    @Invoker("setShoulderEntityLeft")
    void vpp$setLeftShoulder(CompoundTag entity);

    @Invoker("setShoulderEntityRight")
    void vpp$setRightShoulder(CompoundTag entity);
}
