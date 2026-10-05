package me.pajic.tiered_backpacks.platform;

//$ loader_util_import
import me.pajic.tiered_backpacks.platform.fabric.FabricLoaderUtil;
import me.pajic.tiered_backpacks.ui.BackpackMenu;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public interface MultiLoaderUtil {
    MultiLoaderUtil INSTANCE = /*$ loader_util_inst*/ new FabricLoaderUtil();

    boolean isModLoaded(String modId);
    boolean isDevEnv();
    void sendToServer(CustomPacketPayload payload);
    MenuType<BackpackMenu> constructMenu();
    void openBackpackScreen(Player player, ItemStack backpack);
}
