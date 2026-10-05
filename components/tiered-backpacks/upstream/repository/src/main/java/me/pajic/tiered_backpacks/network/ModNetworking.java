package me.pajic.tiered_backpacks.network;

import me.pajic.tiered_backpacks.TieredBackpacks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ModNetworking {

    public static final Identifier OPEN_BACKPACK = TieredBackpacks.id("open_backpack");
    public static final Identifier BACKPACK_SCREEN = TieredBackpacks.id("backpack_screen");

    public record C2SOpenBackpackPayload(int openMethod) implements CustomPacketPayload {
        public static final Type<C2SOpenBackpackPayload> TYPE = new Type<>(OPEN_BACKPACK);
        public static final StreamCodec<RegistryFriendlyByteBuf, C2SOpenBackpackPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, C2SOpenBackpackPayload::openMethod,
                C2SOpenBackpackPayload::new
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record S2CBackpackScreenPayload(ItemStack backpack) implements CustomPacketPayload {
        public static final Type<S2CBackpackScreenPayload> TYPE = new Type<>(BACKPACK_SCREEN);
        public static final StreamCodec<RegistryFriendlyByteBuf, S2CBackpackScreenPayload> CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, S2CBackpackScreenPayload::backpack,
                S2CBackpackScreenPayload::new
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

	public static void init() {}
}
