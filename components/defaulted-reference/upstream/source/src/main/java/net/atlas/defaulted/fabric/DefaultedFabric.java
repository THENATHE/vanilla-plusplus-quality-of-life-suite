package net.atlas.defaulted.fabric;

//? fabric {
import net.atlas.defaulted.EnchantmentPatchesManager;
import net.atlas.defaulted.command.DefaultedCommand;
import net.atlas.defaulted.init.DefaultedRegistries;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.atlas.defaulted.networking.ClientboundEnchantmentsSyncPacket;
import net.fabricmc.api.ModInitializer;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//? >=1.21.11 {
import net.fabricmc.fabric.api.resource.v1.DataResourceLoader;
//?}
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import java.util.*;

import static net.atlas.defaulted.fabric.util.FabricUtils.registerReloadListener;

public final class DefaultedFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Defaulted.init();
        Defaulted.hasOwo = FabricLoader.getInstance().isModLoaded("owo");
        DefaultedRegistries.init();
        Identifier defaultComponentPatches = Defaulted.id("default_component_patches");
        Identifier enchantmentPatches = Defaulted.id("enchantment_patches");
        registerReloadListener(defaultComponentPatches, DefaultComponentPatchesManager::new);
        registerReloadListener(enchantmentPatches, EnchantmentPatchesManager::new);
        //? >=26.1 {
        DataResourceLoader.get()
                .addListenerOrdering(enchantmentPatches, defaultComponentPatches);
        //?} >=1.21.11 {
        /*DataResourceLoader.get()
                        .addReloaderOrdering(enchantmentPatches, defaultComponentPatches);
        *///?}
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            if (!client) {
                DefaultComponentPatchesManager.getInstance().load(registries);
                EnchantmentPatchesManager.getInstance().load(registries);
            }
        });
        PayloadTypeRegistry.clientboundPlay().register(ClientboundDefaultComponentsSyncPacket.TYPE, ClientboundDefaultComponentsSyncPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundEnchantmentsSyncPacket.TYPE, ClientboundEnchantmentsSyncPacket.CODEC);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            if (ServerPlayNetworking.canSend(player, ClientboundEnchantmentsSyncPacket.TYPE))
                ServerPlayNetworking.send(player, new ClientboundEnchantmentsSyncPacket(new ArrayList<>(EnchantmentPatchesManager.getCached(player.registryAccess()))));
            if (ServerPlayNetworking.canSend(player, ClientboundDefaultComponentsSyncPacket.TYPE))
                ServerPlayNetworking.send(player, new ClientboundDefaultComponentsSyncPacket(new ArrayList<>(DefaultComponentPatchesManager.getCached(player.registryAccess()))));
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext, environment) -> DefaultedCommand.register(dispatcher, commandBuildContext));
    }
}
//?}