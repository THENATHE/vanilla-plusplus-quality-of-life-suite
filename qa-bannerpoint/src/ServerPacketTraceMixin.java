package qa.bannerpoint.mixin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qa.bannerpoint.BannerpointServerQa;

@Mixin(ServerCommonPacketListenerImpl.class)
public class ServerPacketTraceMixin {
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    private void traceSend(Packet<?> packet, CallbackInfo ci) {
        if ((Object) this instanceof ServerGamePacketListenerImpl game) BannerpointServerQa.packet(game.player, packet);
    }
    @Inject(method = "handleResourcePackResponse", at = @At("HEAD"))
    private void traceStatus(ServerboundResourcePackPacket packet, CallbackInfo ci) {
        BannerpointServerQa.observe(((ServerCommonPacketListenerImpl) (Object) this).getOwner().name(), "pack-status", packet.id() + " " + packet.action());
    }
}
