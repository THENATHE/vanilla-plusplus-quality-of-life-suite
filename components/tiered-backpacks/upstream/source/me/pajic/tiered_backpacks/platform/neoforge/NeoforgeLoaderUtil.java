package me.pajic.tiered_backpacks.platform.neoforge;

//? neoforge {

/*import me.pajic.tiered_backpacks.network.ModNetworking;
import me.pajic.tiered_backpacks.platform.MultiLoaderUtil;
import me.pajic.tiered_backpacks.ui.BackpackMenu;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

public class NeoforgeLoaderUtil implements MultiLoaderUtil {

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevEnv() {
        return !FMLLoader.getCurrent().isProduction();
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }

    @Override
    public MenuType<BackpackMenu> constructMenu() {
        return IMenuTypeExtension.create((windowId, inv, data) ->
                new BackpackMenu(windowId, inv, ModNetworking.S2CBackpackScreenPayload.CODEC.decode(data))
        );
    }

    @SuppressWarnings("resource")
    @Override
    public void openBackpackScreen(Player player, ItemStack backpack) {
        if (!player.level().isClientSide()) {
            player.openMenu(
                    new SimpleMenuProvider((containerId, playerInventory, _) ->
                            new BackpackMenu(containerId, playerInventory, backpack), backpack.getDisplayName()
                    ),
                    buf -> ModNetworking.S2CBackpackScreenPayload.CODEC.encode(
                            buf, new ModNetworking.S2CBackpackScreenPayload(backpack)
                    )
            );
        }
    }
}
*///?}
