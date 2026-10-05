package me.pajic.toolpouch.platform;

//$ loader_util_import
import me.pajic.toolpouch.platform.fabric.FabricLoaderUtil;
import me.pajic.toolpouch.menu.ToolPouchMenu;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public interface MultiLoaderUtil {
    MultiLoaderUtil INSTANCE = /*$ loader_util_inst*/ new FabricLoaderUtil();

    boolean isModLoaded(String modId);
    boolean isDevEnv();
    void sendToServer(CustomPacketPayload payload);
    void sendToClient(ServerPlayer player, CustomPacketPayload payload);
    MenuType<ToolPouchMenu> constructMenu();
    void openToolPouchScreen(Player player, ItemStack backpack);
    String getTagTranslationKey(TagKey<?> tagKey);
}
