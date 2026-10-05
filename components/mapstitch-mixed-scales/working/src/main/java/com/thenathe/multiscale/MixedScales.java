package com.thenathe.multiscale;

import com.thenathe.suite.network.SuiteCapabilities;
import me.pajic.mapstitch.component.ModDataComponents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class MixedScales implements ModInitializer {
    @Override public void onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(SelectScale.TYPE, SelectScale.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SelectScale.TYPE, (payload, context) -> select(context.player(), payload));
    }

    public static boolean select(ServerPlayer player, SelectScale payload) {
        if (!SuiteCapabilities.isNative(player.connection.getPacketContext(), "mapstitch") || !player.isAlive()
                || payload.scale() < 0 || payload.scale() > 4) return false;
        var handle = payload.target().resolve(player);
        if (handle == null) return false;
        handle.atlas().set(ModDataComponents.ATLAS_SCALE, payload.scale());
        handle.atlas().set(ModDataComponents.ATLAS_ACTIVE_MAP_ID, -1);
        // Selection itself consumes nothing; the ordinary inventory tick explores.
        MixedScaleMaps.selectActive(handle.atlas(), player.level(), player, false);
        handle.save().run();
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        return true;
    }

    public record SelectScale(AtlasTarget target, int scale) implements CustomPacketPayload {
        public static final Type<SelectScale> TYPE = new Type<>(Identifier.parse("mapstitch_mixed_scales:select_scale_v1"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SelectScale> CODEC = StreamCodec.of((buf, value) -> {
            buf.writeVarInt(value.target.location()); buf.writeVarInt(value.target.index());
            buf.writeVarInt(value.target.anchor()); buf.writeVarInt(value.scale);
        }, buf -> new SelectScale(new AtlasTarget(buf.readVarInt(), buf.readVarInt(), buf.readVarInt()), buf.readVarInt()));
        @Override public Type<SelectScale> type() { return TYPE; }
    }
}
