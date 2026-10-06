package qa.atlasnetwork.mixin;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qa.atlasnetwork.AtlasNetworkServerQa;
@Mixin(ServerCommonPacketListenerImpl.class)
public class AtlasPacketTraceMixin {
    @Inject(method="send(Lnet/minecraft/network/protocol/Packet;)V",at=@At("HEAD"))
    private void observe(Packet<?> packet,CallbackInfo ci){if((Object)this instanceof ServerGamePacketListenerImpl game)AtlasNetworkServerQa.packet(game.player,packet);}
}
