package net.atlas.defaulted.neoforge.event;

//? neoforge {
/*//? >1.21.1
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.atlas.defaulted.EnchantmentPatchesManager;
import net.atlas.defaulted.command.DefaultedCommand;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.atlas.defaulted.networking.ClientboundEnchantmentsSyncPacket;
//? >1.21.1
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
//? <=1.21.1
//import net.neoforged.neoforge.event.AddReloadListenerEvent;
//? >1.21.1
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;

public class DefaultedNeoForgeEventHandlers {
    @SubscribeEvent
    public static void onCommandRegistration(RegisterCommandsEvent event) {
        DefaultedCommand.register(event.getDispatcher(), event.getBuildContext());
    }
    @SubscribeEvent
    public static void onDatapackSync(final OnDatapackSyncEvent onDatapackSyncEvent) {
        ClientboundDefaultComponentsSyncPacket[] defaultComponentsSyncPacket = {null};
        ClientboundEnchantmentsSyncPacket[] enchantmentsSyncPacket = {null};
        onDatapackSyncEvent.getRelevantPlayers().forEach(player -> {
            if (defaultComponentsSyncPacket[0] == null) defaultComponentsSyncPacket[0] = new ClientboundDefaultComponentsSyncPacket(new ArrayList<>(DefaultComponentPatchesManager.getCached(player.registryAccess())));
            if (enchantmentsSyncPacket[0] == null) enchantmentsSyncPacket[0] = new ClientboundEnchantmentsSyncPacket(new ArrayList<>(EnchantmentPatchesManager.getCached(player.registryAccess())));
            if (player.connection.hasChannel(enchantmentsSyncPacket[0])) PacketDistributor.sendToPlayer(player, enchantmentsSyncPacket[0]);
            if (player.connection.hasChannel(defaultComponentsSyncPacket[0])) PacketDistributor.sendToPlayer(player, defaultComponentsSyncPacket[0]);
        });
    }
    //? >1.21.1 {
    @SubscribeEvent
    public static void onDatapackReload(final AddServerReloadListenersEvent addReloadListenerEvent) {
        Identifier defaultComponentPatches = Defaulted.id("default_component_patches");
        Identifier enchantmentPatches = Defaulted.id("enchantment_patches");
        //~ if >26.2 'getRegistryAccess' -> 'getServerResources().getRegistryLookup' {
        addReloadListenerEvent.addListener(defaultComponentPatches, new DefaultComponentPatchesManager(addReloadListenerEvent.getServerResources().getRegistryLookup()));
        addReloadListenerEvent.addListener(enchantmentPatches, new EnchantmentPatchesManager(addReloadListenerEvent.getServerResources().getRegistryLookup()));
        //~}
        addReloadListenerEvent.addDependency(enchantmentPatches, defaultComponentPatches);
    }
    //?} <=1.21.1 {
    /^@SubscribeEvent
    public static void onDatapackReload(final AddReloadListenerEvent addReloadListenerEvent) {
        addReloadListenerEvent.addListener(new EnchantmentPatchesManager());
        addReloadListenerEvent.addListener(new DefaultComponentPatchesManager());
    }
    ^///?}
    @SubscribeEvent
    public static void serverStart(final ServerStartedEvent event) {
        DefaultComponentPatchesManager.getInstance().load(event.getServer().registryAccess());
        EnchantmentPatchesManager.getInstance().load(event.getServer().registryAccess());
    }
}
*///?}