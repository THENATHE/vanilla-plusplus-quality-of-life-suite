package net.atlas.defaulted.neoforge.client;

//? neoforge {
/*import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.EnchantmentPatchesManager;
//? 1.21.11 || 1.21.1 {
/^import net.atlas.defaulted.component.ItemPatches;
import net.atlas.defaulted.enchantment.EnchantmentPatches;
^///?}
//? 1.21.11 || 1.21.1 {
/^import net.neoforged.fml.ModList;
import net.mehvahdjukaar.nautilus.NautilusStudioApi;
import net.mehvahdjukaar.nautilus.SchemaEditor;
^///?}
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@Mod(value = Defaulted.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = Defaulted.MOD_ID)
public class DefaultedNeoForgeClient {
    public DefaultedNeoForgeClient(ModContainer container) {
        //? 1.21.11 || 1.21.1 {
        /^if (ModList.get().isLoaded("nautilus_studio")) {
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
        ^///?}
    }
    @SubscribeEvent
    public static void onClientDisconnect(final ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        DefaultComponentPatchesManager.clearClient();
        EnchantmentPatchesManager.clearClient();
    }
}
*///?}