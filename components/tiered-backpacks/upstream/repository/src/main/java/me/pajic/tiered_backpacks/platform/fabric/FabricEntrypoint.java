package me.pajic.tiered_backpacks.platform.fabric;

//? fabric {

import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import me.pajic.tiered_backpacks.TieredBackpacks;
import me.pajic.tiered_backpacks.component.ModDataComponents;
import me.pajic.tiered_backpacks.item.ModItems;
import me.pajic.tiered_backpacks.menu.ModMenuTypes;
import me.pajic.tiered_backpacks.network.ModNetworking;
import me.pajic.tiered_backpacks.recipe.ModRecipes;
import me.pajic.tiered_backpacks.util.BackpackUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
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
		TieredBackpacks.onInitialize();
        ModDataComponents.init();
        ModItems.init();
        ModMenuTypes.init();
        ModNetworking.init();
        Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                TieredBackpacks.id("tier"),
                ModDataComponents.BACKPACK_TIER
        );
        Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                TieredBackpacks.id("stored_dye"),
                ModDataComponents.STORED_BACKPACK_DYE
        );
        Registry.register(
                BuiltInRegistries.MENU,
                TieredBackpacks.id("backpack_menu"),
                ModMenuTypes.BACKPACK_MENU
        );
        Registry.register(
                BuiltInRegistries.ITEM,
                TieredBackpacks.id("leather_backpack"),
                ModItems.LEATHER_BACKPACK
        );
        Registry.register(
                BuiltInRegistries.ITEM,
                TieredBackpacks.id("copper_backpack"),
                ModItems.COPPER_BACKPACK
        );
        Registry.register(
                BuiltInRegistries.ITEM,
                TieredBackpacks.id("iron_backpack"),
                ModItems.IRON_BACKPACK
        );
        Registry.register(
                BuiltInRegistries.ITEM,
                TieredBackpacks.id("golden_backpack"),
                ModItems.GOLDEN_BACKPACK
        );
        Registry.register(
                BuiltInRegistries.ITEM,
                TieredBackpacks.id("diamond_backpack"),
                ModItems.DIAMOND_BACKPACK
        );
        Registry.register(
                BuiltInRegistries.ITEM,
                TieredBackpacks.id("netherite_backpack"),
                ModItems.NETHERITE_BACKPACK
        );
        Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                TieredBackpacks.id("crafting_special_attach_backpack"),
                ModRecipes.ATTACH_BACKPACK
        );
        Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                TieredBackpacks.id("crafting_special_detach_backpack"),
                ModRecipes.DETACH_BACKPACK
        );
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries ->
                entries.insertBefore(Items.COMPASS,
                        ModItems.LEATHER_BACKPACK,
                        ModItems.COPPER_BACKPACK,
                        ModItems.IRON_BACKPACK,
                        ModItems.GOLDEN_BACKPACK,
                        ModItems.DIAMOND_BACKPACK,
                        ModItems.NETHERITE_BACKPACK
                )
        );
        PayloadTypeRegistry.serverboundPlay().register(ModNetworking.C2SOpenBackpackPayload.TYPE, ModNetworking.C2SOpenBackpackPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
                ModNetworking.C2SOpenBackpackPayload.TYPE,
                (payload, context) ->
                        BackpackUtil.tryOpenBackpack(payload.openMethod(), context.player())
        );
	}
}
//?}
