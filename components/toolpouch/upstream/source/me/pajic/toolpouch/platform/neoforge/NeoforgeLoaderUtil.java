package me.pajic.toolpouch.platform.neoforge;

//? neoforge {

/*import me.pajic.toolpouch.platform.MultiLoaderUtil;
import me.pajic.toolpouch.menu.ToolPouchMenu;
import me.pajic.toolpouch.network.ModPayloads;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoforgeLoaderUtil implements MultiLoaderUtil {

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevEnv() {
        return !FMLLoader/^? >=1.21.9 {^/.getCurrent()/^?}^/.isProduction();
    }

    @Override
	public void sendToServer(CustomPacketPayload payload) {
		ClientPacketDistributor.sendToServer(payload);
	}

	@Override
	public void sendToClient(ServerPlayer player, CustomPacketPayload payload) {
		PacketDistributor.sendToPlayer(player, payload);
	}

	@Override
	public MenuType<ToolPouchMenu> constructMenu() {
		return IMenuTypeExtension.create((windowId, inv, data) ->
				new ToolPouchMenu(windowId, inv, ModPayloads.S2CToolPouchScreenPayload.CODEC.decode(data))
		);
	}

	@SuppressWarnings("resource")
	@Override
	public void openToolPouchScreen(Player player, ItemStack toolPouch) {
		if (!player.level().isClientSide()) {
			player.openMenu(
					new SimpleMenuProvider((containerId, playerInventory, player1) ->
							new ToolPouchMenu(containerId, playerInventory, toolPouch), toolPouch.getDisplayName()
					),
					buf -> ModPayloads.S2CToolPouchScreenPayload.CODEC.encode(
							buf, new ModPayloads.S2CToolPouchScreenPayload(toolPouch)
					)
			);
		}
	}

	@Override
	public String getTagTranslationKey(TagKey<?> tagKey) {
		return Tags.getTagTranslationKey(tagKey);
	}
}
*///?}
