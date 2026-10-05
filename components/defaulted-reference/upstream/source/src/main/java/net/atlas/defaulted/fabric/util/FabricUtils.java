package net.atlas.defaulted.fabric.util;

//? fabric {
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
//? >=26.1 {
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
//?}
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
//? <1.21.11 {
/*import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
 *///?} >=1.21.11 {
import net.fabricmc.fabric.api.resource.v1.DataResourceLoader;
//?}
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
//? >=1.21.11
import net.minecraft.server.packs.resources.PreparableReloadListener;
//? <1.21.11 {
/*import net.minecraft.server.packs.PackType;
 *///?}

import java.util.function.Function;

public class FabricUtils {
    public static RegistryAccess getRegistryAccess(ClientPlayNetworking.Context context) {
        //? >=26.1 {
        return context.packetContext().get(PacketContext.REGISTRY_ACCESS);
        //?} else {
        /*return context.player().registryAccess();
        *///?}
    }
    //? >=1.21.11 {
    public static void registerReloadListener(Identifier id, Function<HolderLookup.Provider, PreparableReloadListener> factory) {
    //?} <1.21.11 {
    /*public static void registerReloadListener(Identifier id, Function<HolderLookup.Provider, IdentifiableResourceReloadListener> factory) {
    *///?}
        //? >=1.21.11 {
        DataResourceLoader.get()
                //?} <1.21.11 {
                /*ResourceManagerHelper.get(PackType.SERVER_DATA)
                 *///?}
                //? 1.21.11 {
                /*.registerReloader(id, factory);
        *///?} else {
                .registerReloadListener(id, factory);
         //?}
    }
    public static <T> FabricRegistryBuilder<T, MappedRegistry<T>> createRegistry(ResourceKey<Registry<T>> registryKey) {
        //? >=26.1 {
        return FabricRegistryBuilder.create(registryKey);
        //?} else {
        /*return FabricRegistryBuilder.createSimple(registryKey);
        *///?}
    }
}
//?}