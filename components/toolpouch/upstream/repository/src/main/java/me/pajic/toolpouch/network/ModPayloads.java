package me.pajic.toolpouch.network;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ModPayloads {

	public record C2SOpenToolPouchPayload(int openMethod) implements CustomPacketPayload {
		public static final Type<C2SOpenToolPouchPayload> TYPE = new Type<>(NetworkConstants.OPEN_TOOL_POUCH);
		public static final StreamCodec<RegistryFriendlyByteBuf, C2SOpenToolPouchPayload> CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, C2SOpenToolPouchPayload::openMethod,
				C2SOpenToolPouchPayload::new
		);

		@Override
		public @NotNull Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record S2CToolPouchScreenPayload(ItemStack toolPouch) implements CustomPacketPayload {
		public static final Type<S2CToolPouchScreenPayload> TYPE = new Type<>(NetworkConstants.TOOL_POUCH_SCREEN);
		public static final StreamCodec<RegistryFriendlyByteBuf, S2CToolPouchScreenPayload> CODEC = StreamCodec.composite(
				ItemStack.STREAM_CODEC, S2CToolPouchScreenPayload::toolPouch,
				S2CToolPouchScreenPayload::new
		);

		@Override
		public @NotNull Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record C2SOpenShulkerBoxPayload(int index) implements CustomPacketPayload {
		public static final Type<C2SOpenShulkerBoxPayload> TYPE = new Type<>(NetworkConstants.OPEN_SHULKER_BOX);
		public static final StreamCodec<RegistryFriendlyByteBuf, C2SOpenShulkerBoxPayload> CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, C2SOpenShulkerBoxPayload::index,
				C2SOpenShulkerBoxPayload::new
		);

		@Override
		public @NotNull Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record C2SOpenEnderContainerPayload() implements CustomPacketPayload {
		public static final Type<C2SOpenEnderContainerPayload> TYPE = new Type<>(NetworkConstants.OPEN_ENDER_CONTAINER);
		public static final StreamCodec<RegistryFriendlyByteBuf, C2SOpenEnderContainerPayload> CODEC = StreamCodec.unit(
				new C2SOpenEnderContainerPayload()
		);

		@Override
		public @NotNull Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record C2SSyncShulkerSlot(int slot) implements CustomPacketPayload {
		public static final Type<C2SSyncShulkerSlot> TYPE = new Type<>(NetworkConstants.C2S_SYNC_SHULKER_SLOT);
		public static final StreamCodec<RegistryFriendlyByteBuf, C2SSyncShulkerSlot> CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, C2SSyncShulkerSlot::slot,
				C2SSyncShulkerSlot::new
		);

		@Override
		public @NotNull Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record S2CSyncShulkerSlot(int slot) implements CustomPacketPayload {
		public static final Type<S2CSyncShulkerSlot> TYPE = new Type<>(NetworkConstants.S2C_SYNC_SHULKER_SLOT);
		public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncShulkerSlot> CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, S2CSyncShulkerSlot::slot,
				S2CSyncShulkerSlot::new
		);

		@Override
		public @NotNull Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record C2SSyncArrowSlot(int slot) implements CustomPacketPayload {
		public static final Type<C2SSyncArrowSlot> TYPE = new Type<>(NetworkConstants.C2S_SYNC_ARROW_SLOT);
		public static final StreamCodec<RegistryFriendlyByteBuf, C2SSyncArrowSlot> CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, C2SSyncArrowSlot::slot,
				C2SSyncArrowSlot::new
		);

		@Override
		public @NotNull Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record S2CSyncArrowSlot(int slot) implements CustomPacketPayload {
		public static final Type<S2CSyncArrowSlot> TYPE = new Type<>(NetworkConstants.S2C_SYNC_ARROW_SLOT);
		public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncArrowSlot> CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, S2CSyncArrowSlot::slot,
				S2CSyncArrowSlot::new
		);

		@Override
		public @NotNull Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record C2SElytraBoostFromPouchPayload() implements CustomPacketPayload {
		public static final Type<C2SElytraBoostFromPouchPayload> TYPE = new Type<>(NetworkConstants.ELYTRA_BOOST_FROM_POUCH);
		public static final StreamCodec<RegistryFriendlyByteBuf, C2SElytraBoostFromPouchPayload> CODEC = StreamCodec.unit(
				new C2SElytraBoostFromPouchPayload()
		);

		@Override
		public @NotNull Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record C2SPlaySoundPayload(Holder<SoundEvent> sound) implements CustomPacketPayload {
		public static final Type<C2SPlaySoundPayload> TYPE = new Type<>(NetworkConstants.PLAY_SOUND);
		public static final StreamCodec<RegistryFriendlyByteBuf, C2SPlaySoundPayload> CODEC = StreamCodec.composite(
				SoundEvent.STREAM_CODEC, C2SPlaySoundPayload::sound,
				C2SPlaySoundPayload::new
		);

		@Override
		public @NotNull Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public static void init() {}
}
