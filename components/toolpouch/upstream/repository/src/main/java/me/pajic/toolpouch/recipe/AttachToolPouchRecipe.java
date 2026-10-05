package me.pajic.toolpouch.recipe;

import com.mojang.serialization.MapCodec;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.component.ModDataComponents;
import me.pajic.toolpouch.item.ModItems;
import me.pajic.toolpouch.util.GameplayUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class AttachToolPouchRecipe extends CustomRecipe {

	private static final AttachToolPouchRecipe INSTANCE = new AttachToolPouchRecipe();
	public static final MapCodec<AttachToolPouchRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
	public static final StreamCodec<RegistryFriendlyByteBuf, AttachToolPouchRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	private ItemStack toolPouch = ItemStack.EMPTY;

	@Override
	public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
		if (ToolPouch.CONFIG.canAttachToLeggings.get() && input.size() == 2) {
			ItemStack leggings = ItemStack.EMPTY;
			boolean hasToolPouch = false;
			boolean hasLeggings = false;
			for (int i = 0; i < input.size(); i++) {
				ItemStack itemStack = input.getItem(i);
				if (!itemStack.isEmpty()) {
					if (itemStack.is(ItemTags.LEG_ARMOR)) {
						if (hasLeggings) return false;
						hasLeggings = true;
						leggings = itemStack.copy();
					} else if (itemStack.is(GameplayUtil.TOOL_POUCHES)) {
						if (hasToolPouch) return false;
						hasToolPouch = true;
						toolPouch = itemStack.copy();
					}
				}
			}
			if (hasToolPouch && hasLeggings) {
				if (toolPouch.is(ModItems.NETHERITE_TOOL_POUCH)) {
					DamageResistant resist = leggings.get(DataComponents.DAMAGE_RESISTANT);
					return resist != null && resist.types().stream().anyMatch(type -> type.is(DamageTypeTags.IS_FIRE));
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public @NotNull ItemStack assemble(CraftingInput input) {
		ItemStack itemStack = ItemStack.EMPTY;
		for (int i = 0; i < input.size(); i++) {
			ItemStack itemStack2 = input.getItem(i);
			if (itemStack2.is(ItemTags.LEG_ARMOR)) {
				itemStack = itemStack2.copy();
				itemStack.set(DataComponents.CONTAINER, toolPouch.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
				DyedItemColor dye = toolPouch.get(DataComponents.DYED_COLOR);
				if (dye != null) itemStack.set(ModDataComponents.STORED_TOOL_POUCH_DYE, dye);
				if (toolPouch.is(ModItems.NETHERITE_TOOL_POUCH)) itemStack.set(ModDataComponents.IS_NETHERITE_POUCH, true);
			}
		}
		return itemStack;
	}

	@Override
	public @NotNull RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return ModRecipes.ATTACH_TOOL_POUCH;
	}
}
