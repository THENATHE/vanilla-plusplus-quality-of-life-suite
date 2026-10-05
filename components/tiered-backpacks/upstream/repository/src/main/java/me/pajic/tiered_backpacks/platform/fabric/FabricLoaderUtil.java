package me.pajic.tiered_backpacks.platform.fabric;

//? fabric {

import me.pajic.tiered_backpacks.network.ModNetworking;
import me.pajic.tiered_backpacks.platform.MultiLoaderUtil;
import me.pajic.tiered_backpacks.ui.BackpackMenu;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

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
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    public MenuType<BackpackMenu> constructMenu() {
        return new ExtendedMenuType<>(BackpackMenu::new, ModNetworking.S2CBackpackScreenPayload.CODEC);
    }

    @SuppressWarnings("resource")
    @Override
    public void openBackpackScreen(Player player, ItemStack backpack) {
        if (!player.level().isClientSide()) {
            player.openMenu(new ExtendedMenuProvider<ModNetworking.S2CBackpackScreenPayload>() {
                @Override
                public @NotNull AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
                    return new BackpackMenu(i, inventory, backpack);
                }

                @Override @NotNull
                public Component getDisplayName() {
                    return backpack.getDisplayName();
                }

                @Override @NotNull
                public ModNetworking.S2CBackpackScreenPayload getScreenOpeningData(@NotNull ServerPlayer serverPlayer) {
                    return new ModNetworking.S2CBackpackScreenPayload(backpack);
                }
            });
        }
    }
}
//?}
