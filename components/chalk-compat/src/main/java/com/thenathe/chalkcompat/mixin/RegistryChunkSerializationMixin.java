package com.thenathe.chalkcompat.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.thenathe.chalkcompat.WireRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;

/** Chunk byte arrays are prepared before the outer packet encoder runs. */
@Mixin(LevelChunkSection.class)
public abstract class RegistryChunkSerializationMixin {
    @WrapMethod(method = "write")
    private void chalkcompat$write(FriendlyByteBuf buffer, Operation<Void> original) {
        WireRegistries.encode(false, () -> original.call(buffer));
    }
    @WrapMethod(method = "getSerializedSize")
    private int chalkcompat$size(Operation<Integer> original) {
        int[] size = {0};
        WireRegistries.encode(false, () -> size[0] = original.call());
        return size[0];
    }
}
