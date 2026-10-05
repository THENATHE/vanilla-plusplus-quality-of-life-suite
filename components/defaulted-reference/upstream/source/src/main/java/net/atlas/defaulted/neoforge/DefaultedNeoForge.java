package net.atlas.defaulted.neoforge;

//? neoforge {
/*import net.atlas.defaulted.EnchantmentPatchesManager;
import net.atlas.defaulted.init.DefaultedRegistries;
import net.atlas.defaulted.neoforge.event.DefaultedNeoForgeEventHandlers;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.atlas.defaulted.networking.ClientboundEnchantmentsSyncPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Defaulted.MOD_ID)
public final class DefaultedNeoForge {
    public DefaultedNeoForge(IEventBus modBus) {
        // Run our common setup.
        Defaulted.init();
        DefaultedRegistries.init(modBus);
        modBus.register(this);
        Defaulted.hasOwo = ModList.get().isLoaded("owo");
        NeoForge.EVENT_BUS.register(DefaultedNeoForgeEventHandlers.class);
    }
    @SubscribeEvent
    public void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(
                ClientboundDefaultComponentsSyncPacket.TYPE,
                ClientboundDefaultComponentsSyncPacket.CODEC,
                DefaultedNeoForge::receiveDefaults
        );
        registrar.playToClient(
                ClientboundEnchantmentsSyncPacket.TYPE,
                ClientboundEnchantmentsSyncPacket.CODEC,
                DefaultedNeoForge::receiveEnchantments
        );
    }

    public static void receiveEnchantments(ClientboundEnchantmentsSyncPacket payload, IPayloadContext payloadContext) {
        if (!Minecraft.getInstance().hasSingleplayerServer()) EnchantmentPatchesManager.loadClientCache(payloadContext.player().registryAccess(), payload.list());
        else EnchantmentPatchesManager.setClientCache(payloadContext.player().registryAccess());
    }

    public static void receiveDefaults(final ClientboundDefaultComponentsSyncPacket payload, final IPayloadContext payloadContext) {
        if (!Minecraft.getInstance().hasSingleplayerServer()) DefaultComponentPatchesManager.loadClientCache(payload.list());
        else DefaultComponentPatchesManager.setClientCache(payloadContext.player().registryAccess());
    }
}
*///?}