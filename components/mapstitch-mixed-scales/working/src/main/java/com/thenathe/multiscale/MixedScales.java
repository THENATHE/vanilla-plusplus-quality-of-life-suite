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
        PayloadTypeRegistry.serverboundPlay().register(SelectGeneration.TYPE, SelectGeneration.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SelectScale.TYPE, (payload, context) -> select(context.player(), payload));
        ServerPlayNetworking.registerGlobalReceiver(SelectGeneration.TYPE, (payload, context) -> selectGeneration(context.player(), payload));
    }

    public static boolean select(ServerPlayer player, SelectScale payload) {
        if (!SuiteCapabilities.isNative(player.connection.getPacketContext(), "mapstitch") || !player.isAlive()
                || payload.scale() < 0 || payload.scale() > 4) return false;
        var handle = payload.target().resolve(player);
        if (handle == null || payload.target().identity().isEmpty()) return false;
        AtlasOptions.setGenerationMask(handle.atlas(), AtlasOptions.generationMask(handle.atlas()));
        handle.atlas().set(ModDataComponents.ATLAS_SCALE, payload.scale());
        handle.atlas().set(ModDataComponents.ATLAS_ACTIVE_MAP_ID, -1);
        // Selection itself consumes nothing; the ordinary inventory tick explores.
        MixedScaleMaps.selectActive(handle.atlas(), player.level(), player, false);
        handle.save().run();
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        return true;
    }

    public static boolean selectGeneration(ServerPlayer player, SelectGeneration payload) {
        if (!SuiteCapabilities.isNative(player.connection.getPacketContext(), "mapstitch") || !player.isAlive()
                || payload.mask() < 0 || payload.mask() > 31) return false;
        var handle = payload.target().resolve(player);
        if (handle == null || payload.target().identity().isEmpty()) return false;
        AtlasOptions.setGenerationMask(handle.atlas(), payload.mask());
        handle.save().run();
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        return true;
    }

    public record SelectGeneration(AtlasTarget target, int mask) implements CustomPacketPayload {
        public static final Type<SelectGeneration> TYPE = new Type<>(Identifier.parse("mapstitch_mixed_scales:select_generation_v2"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SelectGeneration> CODEC = StreamCodec.of((buf, value) -> {
            buf.writeVarInt(value.target.location()); buf.writeVarInt(value.target.index());
            buf.writeVarInt(value.target.anchor()); buf.writeUtf(value.target.identity(), 64); buf.writeVarInt(value.mask());
        }, buf -> new SelectGeneration(new AtlasTarget(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(64)), buf.readVarInt()));
        @Override public Type<SelectGeneration> type() { return TYPE; }
    }

    public record SelectScale(AtlasTarget target, int scale) implements CustomPacketPayload {
        public static final Type<SelectScale> TYPE = new Type<>(Identifier.parse("mapstitch_mixed_scales:select_scale_v2"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SelectScale> CODEC = StreamCodec.of((buf, value) -> {
            buf.writeVarInt(value.target.location()); buf.writeVarInt(value.target.index());
            buf.writeVarInt(value.target.anchor()); buf.writeUtf(value.target.identity(), 64); buf.writeVarInt(value.scale);
        }, buf -> new SelectScale(new AtlasTarget(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(64)), buf.readVarInt()));
        @Override public Type<SelectScale> type() { return TYPE; }
    }
}
