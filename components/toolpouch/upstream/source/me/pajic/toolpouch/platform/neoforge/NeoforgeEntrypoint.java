package me.pajic.toolpouch.platform.neoforge;

//? neoforge {

/*import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.component.ModDataComponents;
import me.pajic.toolpouch.item.ModItems;
import me.pajic.toolpouch.menu.ModMenuTypes;
import me.pajic.toolpouch.network.ModPayloads;
import me.pajic.toolpouch.network.NetworkClientEvents;
import me.pajic.toolpouch.network.NetworkEvents;
import me.pajic.toolpouch.recipe.ModRecipes;
import me.pajic.toolpouch.tooltip.PreviewExtensionPoint;
import me.pajic.toolpouch.util.CompatFlags;
import me.pajic.toolpouch.util.ItemSuggestions;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(ToolPouch.MOD_ID)
@EventBusSubscriber(modid = ToolPouch.MOD_ID)
public class NeoforgeEntrypoint {

	@SubscribeEvent
	private static void onCommonSetup(FMLCommonSetupEvent event) {
        ToolPouch.onInitialize();
        if (CompatFlags.SBT_LOADED) PreviewExtensionPoint.register();
	}

    @SubscribeEvent
    private static void initRegistry(RegisterEvent event) {
        ModDataComponents.init();
        event.register(
                Registries.DATA_COMPONENT_TYPE,
                registry -> {
                    registry.register(ToolPouch.id("is_netherite_pouch"), ModDataComponents.IS_NETHERITE_POUCH);
                    registry.register(ToolPouch.id("stored_dye"), ModDataComponents.STORED_TOOL_POUCH_DYE);
                }
        );
        ModMenuTypes.init();
        event.register(Registries.MENU, registry -> registry.register(
                ToolPouch.id("tool_pouch_menu"), ModMenuTypes.TOOL_POUCH_MENU
        ));
        ModItems.init();
        event.register(Registries.ITEM, registry -> {
            registry.register(ToolPouch.id("tool_pouch"), ModItems.TOOL_POUCH);
            registry.register(ToolPouch.id("netherite_tool_pouch"), ModItems.NETHERITE_TOOL_POUCH);
        });
        event.register(
                Registries.RECIPE_SERIALIZER,
                registry -> {
                    registry.register(ToolPouch.id("crafting_special_attach_tool_pouch"), ModRecipes.ATTACH_TOOL_POUCH);
                    registry.register(ToolPouch.id("crafting_special_detach_tool_pouch"), ModRecipes.DETACH_TOOL_POUCH);
                }
        );
    }

    @SubscribeEvent
    private static void onLevelLoad(LevelEvent.Load event) {
        ItemSuggestions.update(event.getLevel());
    }

    @SubscribeEvent
    private static void initCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.insertBefore(
                    Items.COMPASS.getDefaultInstance(),
                    ModItems.TOOL_POUCH.getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.insertBefore(
                    Items.COMPASS.getDefaultInstance(),
                    ModItems.NETHERITE_TOOL_POUCH.getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }
    }

    @SubscribeEvent
    private static void initNetworking(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        ModPayloads.init();
        registrar.playToServer(
                ModPayloads.C2SOpenToolPouchPayload.TYPE,
                ModPayloads.C2SOpenToolPouchPayload.CODEC,
                (payload, context) ->
                        NetworkEvents.tryOpenToolPouch((ServerPlayer) context.player(), payload.openMethod())
        );
        registrar.playToServer(
                ModPayloads.C2SOpenShulkerBoxPayload.TYPE,
                ModPayloads.C2SOpenShulkerBoxPayload.CODEC,
                (payload, context) ->
                        NetworkEvents.openShulkerBox((ServerPlayer) context.player(), payload.index())
        );
        registrar.playToServer(
                ModPayloads.C2SOpenEnderContainerPayload.TYPE,
                ModPayloads.C2SOpenEnderContainerPayload.CODEC,
                (_, context) ->
                        NetworkEvents.openEnderContainer((ServerPlayer) context.player())
        );
        registrar.playToServer(
                ModPayloads.C2SSyncShulkerSlot.TYPE,
                ModPayloads.C2SSyncShulkerSlot.CODEC,
                (payload, context) ->
                        NetworkEvents.syncShulkerSlotToServer((ServerPlayer) context.player(), payload.slot())
        );
        registrar.playToServer(
                ModPayloads.C2SSyncArrowSlot.TYPE,
                ModPayloads.C2SSyncArrowSlot.CODEC,
                (payload, context) ->
                        NetworkEvents.syncArrowSlotToServer((ServerPlayer) context.player(), payload.slot())
        );
        registrar.playToClient(
                ModPayloads.S2CSyncShulkerSlot.TYPE,
                ModPayloads.S2CSyncShulkerSlot.CODEC,
                (payload, _) ->
                        NetworkClientEvents.syncShulkerSlotToClient(payload.slot())
        );
        registrar.playToClient(
                ModPayloads.S2CSyncArrowSlot.TYPE,
                ModPayloads.S2CSyncArrowSlot.CODEC,
                (payload, _) ->
                        NetworkClientEvents.syncArrowSlotToClient(payload.slot())
        );
        registrar.playToServer(
                ModPayloads.C2SElytraBoostFromPouchPayload.TYPE,
                ModPayloads.C2SElytraBoostFromPouchPayload.CODEC,
                (_, context) ->
                        NetworkEvents.elytraBoostFromPouch((ServerPlayer) context.player())
        );
        registrar.playToServer(
                ModPayloads.C2SPlaySoundPayload.TYPE,
                ModPayloads.C2SPlaySoundPayload.CODEC,
                (payload, context) ->
                        NetworkEvents.playSound((ServerPlayer) context.player(), payload.sound())
        );
    }
}
*///?}
