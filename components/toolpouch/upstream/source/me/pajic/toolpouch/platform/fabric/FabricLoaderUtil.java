package me.pajic.toolpouch.platform.fabric;

//? fabric {

import me.pajic.toolpouch.menu.ToolPouchMenu;
import me.pajic.toolpouch.network.ModPayloads;
import me.pajic.toolpouch.platform.MultiLoaderUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class FabricLoaderUtil implements MultiLoaderUtil {

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevEnv() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public void sendToClient(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    public MenuType<ToolPouchMenu> constructMenu() {
        return new ExtendedMenuType<>(ToolPouchMenu::new, ModPayloads.S2CToolPouchScreenPayload.CODEC);
    }

    @SuppressWarnings("resource")
    @Override
    public void openToolPouchScreen(Player player, ItemStack toolPouch) {
        if (!player.level().isClientSide()) {
            player.openMenu(new ExtendedMenuProvider<ModPayloads.S2CToolPouchScreenPayload>() {
                @Override @NotNull
                public AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
                    return new ToolPouchMenu(i, inventory, toolPouch);
                }

                @Override @NotNull
                public Component getDisplayName() {
                    return toolPouch.getDisplayName();
                }

                @Override @NotNull
                public ModPayloads.S2CToolPouchScreenPayload getScreenOpeningData(@NonNull ServerPlayer serverPlayer) {
                    return new ModPayloads.S2CToolPouchScreenPayload(toolPouch);
                }
            });
        }
    }

    @Override
    public String getTagTranslationKey(TagKey<?> tagKey) {
        return tagKey.getTranslationKey();
    }
}
//?}
