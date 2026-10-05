package me.pajic.toolpouch.mixin;

import me.pajic.toolpouch.network.ModPayloads;
import me.pajic.toolpouch.platform.MultiLoaderUtil;
import me.pajic.toolpouch.util.PlayerExtension;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void syncSlots(MinecraftServer server, Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        MultiLoaderUtil.INSTANCE.sendToClient(player, new ModPayloads.S2CSyncShulkerSlot(((PlayerExtension) player).toolpouch$getShulkerSlot()));
		MultiLoaderUtil.INSTANCE.sendToClient(player, new ModPayloads.S2CSyncArrowSlot(((PlayerExtension) player).toolpouch$getArrowSlot()));
    }
}
