package me.pajic.tiered_backpacks.recipe;

import com.mojang.serialization.MapCodec;
import me.pajic.tiered_backpacks.component.ModDataComponents;
import me.pajic.tiered_backpacks.util.BackpackUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class DetachBackpackRecipe extends CustomRecipe {

	private static final DetachBackpackRecipe INSTANCE = new DetachBackpackRecipe();
	public static final MapCodec<DetachBackpackRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
	public static final StreamCodec<RegistryFriendlyByteBuf, DetachBackpackRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
		if (input.size() != 1) return false;
		ItemStack itemStack = input.getItem(0);
		return !itemStack.isEmpty() && BackpackUtil.isChestplateWithBackpackAttached(itemStack);
	}

	@Override
	public @NotNull ItemStack assemble(CraftingInput inputStacks) {
		ItemStack backpack = ItemStack.EMPTY;
		ItemStack input = inputStacks.getItem(0);
		if (BackpackUtil.isChestplateWithBackpackAttached(input)) {
			backpack = BackpackUtil.stackByTier(input);
			backpack.set(DataComponents.CONTAINER, input.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
			DyedItemColor dye = input.get(ModDataComponents.STORED_BACKPACK_DYE);
			if (dye != null) backpack.set(DataComponents.DYED_COLOR, dye);
		}
		return backpack;
	}

	@Override
	public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingInput inputStacks) {
		NonNullList<ItemStack> nonNullList = NonNullList.withSize(inputStacks.size(), ItemStack.EMPTY);
		for (int i = 0; i < nonNullList.size(); i++) {
			ItemStack input = inputStacks.getItem(i);
			if (BackpackUtil.isChestplateWithBackpackAttached(input)) {
				ItemStack chestplate = input.copy();
				chestplate.remove(ModDataComponents.BACKPACK_TIER);
				chestplate.remove(DataComponents.CONTAINER);
				chestplate.remove(ModDataComponents.STORED_BACKPACK_DYE);
				nonNullList.set(i, chestplate);
			}
		}
		return nonNullList;
	}

	@Override
	public @NotNull RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return ModRecipes.DETACH_BACKPACK;
	}
}
