package me.pajic.toolpouch.platform.fabric;

//? fabric {

import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.component.ModDataComponents;
import me.pajic.toolpouch.item.ModItems;
import me.pajic.toolpouch.menu.ModMenuTypes;
import me.pajic.toolpouch.network.ModPayloads;
import me.pajic.toolpouch.network.NetworkEvents;
import me.pajic.toolpouch.recipe.ModRecipes;
import me.pajic.toolpouch.util.ItemSuggestions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;

@Entrypoint("main")
public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		ToolPouch.onInitialize();
        ModDataComponents.init();
        Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                ToolPouch.id("stored_dye"),
                ModDataComponents.STORED_TOOL_POUCH_DYE
        );
        Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                ToolPouch.id("is_netherite_pouch"),
                ModDataComponents.IS_NETHERITE_POUCH
        );
        ModMenuTypes.init();
        Registry.register(
                BuiltInRegistries.MENU,
                ToolPouch.id("tool_pouch_menu"),
                ModMenuTypes.TOOL_POUCH_MENU
        );
        ModItems.init();
        Registry.register(
                BuiltInRegistries.ITEM,
                ToolPouch.id("tool_pouch"),
                ModItems.TOOL_POUCH
        );
        Registry.register(
                BuiltInRegistries.ITEM,
                ToolPouch.id("netherite_tool_pouch"),
                ModItems.NETHERITE_TOOL_POUCH
        );
        Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                ToolPouch.id("crafting_special_attach_tool_pouch"),
                ModRecipes.ATTACH_TOOL_POUCH
        );
        Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                ToolPouch.id("crafting_special_detach_tool_pouch"),
                ModRecipes.DETACH_TOOL_POUCH
        );
        ServerLevelEvents.LOAD.register((_, level) -> ItemSuggestions.update(level));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries ->
                entries.insertBefore(Items.COMPASS, ModItems.TOOL_POUCH, ModItems.NETHERITE_TOOL_POUCH)
        );
        ModPayloads.init();
        PayloadTypeRegistry.serverboundPlay().register(ModPayloads.C2SOpenToolPouchPayload.TYPE, ModPayloads.C2SOpenToolPouchPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ModPayloads.C2SOpenShulkerBoxPayload.TYPE, ModPayloads.C2SOpenShulkerBoxPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ModPayloads.C2SOpenEnderContainerPayload.TYPE, ModPayloads.C2SOpenEnderContainerPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ModPayloads.C2SSyncShulkerSlot.TYPE, ModPayloads.C2SSyncShulkerSlot.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ModPayloads.S2CSyncShulkerSlot.TYPE, ModPayloads.S2CSyncShulkerSlot.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ModPayloads.C2SSyncArrowSlot.TYPE, ModPayloads.C2SSyncArrowSlot.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ModPayloads.S2CSyncArrowSlot.TYPE, ModPayloads.S2CSyncArrowSlot.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ModPayloads.C2SElytraBoostFromPouchPayload.TYPE, ModPayloads.C2SElytraBoostFromPouchPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ModPayloads.C2SPlaySoundPayload.TYPE, ModPayloads.C2SPlaySoundPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
                ModPayloads.C2SOpenToolPouchPayload.TYPE,
                (payload, context) -> NetworkEvents.tryOpenToolPouch(context.player(), payload.openMethod())
        );
        ServerPlayNetworking.registerGlobalReceiver(
                ModPayloads.C2SOpenShulkerBoxPayload.TYPE,
                (payload, context) -> NetworkEvents.openShulkerBox(context.player(), payload.index())
        );
        ServerPlayNetworking.registerGlobalReceiver(
                ModPayloads.C2SOpenEnderContainerPayload.TYPE,
                (_, context) -> NetworkEvents.openEnderContainer(context.player())
        );
        ServerPlayNetworking.registerGlobalReceiver(
                ModPayloads.C2SSyncShulkerSlot.TYPE,
                (payload, context) -> NetworkEvents.syncShulkerSlotToServer(context.player(), payload.slot())
        );
        ServerPlayNetworking.registerGlobalReceiver(
                ModPayloads.C2SSyncArrowSlot.TYPE,
                (payload, context) -> NetworkEvents.syncArrowSlotToServer(context.player(), payload.slot())
        );
        ServerPlayNetworking.registerGlobalReceiver(
                ModPayloads.C2SElytraBoostFromPouchPayload.TYPE,
                (_, context) -> NetworkEvents.elytraBoostFromPouch(context.player())
        );
        ServerPlayNetworking.registerGlobalReceiver(
                ModPayloads.C2SPlaySoundPayload.TYPE,
                (payload, context) -> NetworkEvents.playSound(context.player(), payload.sound())
        );
	}
}
//?}
