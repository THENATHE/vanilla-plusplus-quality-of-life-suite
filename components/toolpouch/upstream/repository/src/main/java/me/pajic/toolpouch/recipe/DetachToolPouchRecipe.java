package me.pajic.toolpouch.recipe;

import com.mojang.serialization.MapCodec;
import me.pajic.toolpouch.component.ModDataComponents;
import me.pajic.toolpouch.util.GameplayUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class DetachToolPouchRecipe extends CustomRecipe {

	private static final DetachToolPouchRecipe INSTANCE = new DetachToolPouchRecipe();
	public static final MapCodec<DetachToolPouchRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
	public static final StreamCodec<RegistryFriendlyByteBuf, DetachToolPouchRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
		if (input.size() != 1) return false;
		ItemStack itemStack = input.getItem(0);
		return !itemStack.isEmpty() && GameplayUtil.isLeggingsWithPouchAttached(itemStack);
	}

	@Override
	public @NotNull ItemStack assemble(CraftingInput inputStacks) {
		ItemStack input = inputStacks.getItem(0);
		return GameplayUtil.isLeggingsWithPouchAttached(input) ? GameplayUtil.detachToolPouch(input) : ItemStack.EMPTY;
	}

	@Override
	public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingInput inputStacks) {
		NonNullList<ItemStack> nonNullList = NonNullList.withSize(inputStacks.size(), ItemStack.EMPTY);
		for (int i = 0; i < nonNullList.size(); i++) {
			ItemStack input = inputStacks.getItem(i);
			if (GameplayUtil.isLeggingsWithPouchAttached(input)) {
				ItemStack leggings = input.copy();
				leggings.remove(DataComponents.CONTAINER);
				leggings.remove(ModDataComponents.STORED_TOOL_POUCH_DYE);
				leggings.remove(ModDataComponents.IS_NETHERITE_POUCH);
				leggings.remove(ModDataComponents.DISABLED_SLOTS);
				nonNullList.set(i, leggings);
			}
		}
		return nonNullList;
	}

	@Override
	public @NotNull RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return ModRecipes.DETACH_TOOL_POUCH;
	}
}
