package me.pajic.toolpouch.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.toolpouch.ToolPouch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipe;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingMenu.class)
public abstract class SmithingMenuMixin extends ItemCombinerMenu {

	public SmithingMenuMixin(@Nullable MenuType<?> menuType, int containerId, Inventory inventory, ContainerLevelAccess access, ItemCombinerMenuSlotDefinition slotDefinition) {
		super(menuType, containerId, inventory, access, slotDefinition);
	}

	@Inject(
			method = "lambda$createResult$0",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/world/inventory/SmithingMenu;resultSlots:Lnet/minecraft/world/inventory/ResultContainer;",
					ordinal = 0,
					opcode = Opcodes.GETFIELD
			)
	)
	private void handleToolPouchUpgrade(
			CallbackInfo ci,
			@Local(argsOnly = true, name = "recipe") RecipeHolder<SmithingRecipe> recipe,
			@Local(name = "result") ItemStack result
	) {
		if (recipe.id().identifier().equals(ToolPouch.id("netherite_tool_pouch_smithing"))) {
			ItemStack input = slots.get(1).getItem();
			result.set(
					DataComponents.CONTAINER,
					input.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
			);
			result.set(
					DataComponents.DYED_COLOR,
					input.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(DyedItemColor.LEATHER_COLOR))
			);
		}
	}
}
