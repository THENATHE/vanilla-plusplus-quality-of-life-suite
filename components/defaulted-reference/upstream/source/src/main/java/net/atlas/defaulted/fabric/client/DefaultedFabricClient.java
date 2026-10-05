package net.atlas.defaulted.fabric.client;

//? fabric {
import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.atlas.defaulted.EnchantmentPatchesManager;
//? 1.21.11 || 1.21.1 {
/*import net.atlas.defaulted.component.ItemPatches;
import net.atlas.defaulted.enchantment.EnchantmentPatches;
*///?}
import net.atlas.defaulted.fabric.util.FabricUtils;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.atlas.defaulted.networking.ClientboundEnchantmentsSyncPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
//? 1.21.11 || 1.21.1 {
/*import net.fabricmc.loader.api.FabricLoader;
import net.mehvahdjukaar.nautilus.NautilusStudioApi;
import net.mehvahdjukaar.nautilus.SchemaEditor;
*///?}
import net.minecraft.client.Minecraft;

public class DefaultedFabricClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundDefaultComponentsSyncPacket.TYPE, (clientboundDefaultComponentsSyncPacket, context) -> {
            if (!Minecraft.getInstance().hasSingleplayerServer()) DefaultComponentPatchesManager.loadClientCache(clientboundDefaultComponentsSyncPacket.list());
            else DefaultComponentPatchesManager.setClientCache(FabricUtils.getRegistryAccess(context));
        });
        ClientPlayNetworking.registerGlobalReceiver(ClientboundEnchantmentsSyncPacket.TYPE, (clientboundEnchantmentsSyncPacket, context) -> {
            if (!Minecraft.getInstance().hasSingleplayerServer()) EnchantmentPatchesManager.loadClientCache(FabricUtils.getRegistryAccess(context), clientboundEnchantmentsSyncPacket.list());
            else EnchantmentPatchesManager.setClientCache(FabricUtils.getRegistryAccess(context));
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            DefaultComponentPatchesManager.clearClient();
            EnchantmentPatchesManager.clearClient();
        });
        //? 1.21.11 || 1.21.1 {
        /*if (FabricLoader.getInstance().isModLoaded("nautilus_studio")) {
            NautilusStudioApi.register("Defaulted",
                    "Default Component Patches",
                    ItemPatches.CODEC,
                    SchemaEditor.Side.SERVER_DATA,
                    "defaulted/default_component_patches");
            NautilusStudioApi.register("Defaulted",
                    "Enchantment Patches",
                    EnchantmentPatches.CODEC,
                    SchemaEditor.Side.SERVER_DATA,
                    "defaulted/enchantment_patches");
        }
        *///?}
    }
}
//?}