package com.thenathe.chalkcompat.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.thenathe.chalkcompat.WireRegistries;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.IdMapper;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Fabric rebuilds client state IDs in synchronized block order, independently of server order. */
@Mixin(IdMapper.class)
public abstract class RegistryStateIdsMixin {
    @ModifyReturnValue(method = "getId", at = @At("RETURN"))
    private int chalkcompat$outgoingState(int raw) {
        return (Object) this == Block.BLOCK_STATE_REGISTRY && WireRegistries.writing()
                ? WireRegistries.toWire(WireRegistries.STATES, raw, PacketContext.get()) : raw;
    }
    @ModifyVariable(method = "byId", at = @At("HEAD"), argsOnly = true)
    private int chalkcompat$incomingState(int wire) {
        return (Object) this == Block.BLOCK_STATE_REGISTRY && WireRegistries.reading()
                ? WireRegistries.toServer(WireRegistries.STATES, wire, PacketContext.get()) : wire;
    }
}
