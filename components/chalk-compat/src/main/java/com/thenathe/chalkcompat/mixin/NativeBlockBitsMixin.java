package com.thenathe.chalkcompat.mixin;

import com.thenathe.chalkcompat.NativeClients;
import eu.pb4.polymer.core.impl.ClientMetadataKeys;
import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.network.Connection;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Shared native block palettes use the actual client's state width even without client Polymer. */
@Mixin(value = PolymerServerNetworking.class, remap = false)
public abstract class NativeBlockBitsMixin {
    @Inject(method = "getMetadata(Lnet/minecraft/network/Connection;Lnet/minecraft/resources/Identifier;Lnet/minecraft/nbt/TagType;)Lnet/minecraft/nbt/Tag;",
            at = @At("HEAD"), cancellable = true)
    private static <T extends Tag> void chalkcompat$stateBits(Connection connection, Identifier key, TagType<T> type,
                                                            CallbackInfoReturnable<T> cir) {
        var context = PacketContext.get();
        if (context != null && context.get(PacketContext.CONNECTION) == connection
                && NativeClients.nativeBlocks(context) && key.equals(ClientMetadataKeys.BLOCKSTATE_BITS) && type == IntTag.TYPE) {
            Integer bits = context.get(NativeClients.STATE_BITS);
            if (bits != null) cir.setReturnValue((T) IntTag.valueOf(bits));
        }
    }
}
