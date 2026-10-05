package me.pajic.tiered_backpacks.recipe;

import com.mojang.serialization.MapCodec;
import me.pajic.tiered_backpacks.TieredBackpacks;
import me.pajic.tiered_backpacks.component.ModDataComponents;
import me.pajic.tiered_backpacks.item.ModItems;
import me.pajic.tiered_backpacks.util.BackpackTier;
import me.pajic.tiered_backpacks.util.BackpackUtil;
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

public class AttachBackpackRecipe extends CustomRecipe {

	private static final AttachBackpackRecipe INSTANCE = new AttachBackpackRecipe();
	public static final MapCodec<AttachBackpackRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
	public static final StreamCodec<RegistryFriendlyByteBuf, AttachBackpackRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	private ItemStack backpack = ItemStack.EMPTY;

	@Override
	public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
		if (!TieredBackpacks.CONFIG.canAttachToChestplate.get() || input.size() != 2) {
			return false;
		} else {
			ItemStack chestplate = ItemStack.EMPTY;
			boolean hasBackpack = false;
			boolean hasChestplate = false;
			for (int i = 0; i < input.size(); i++) {
				ItemStack itemStack = input.getItem(i);
				if (!itemStack.isEmpty()) {
					if (itemStack.is(ItemTags.CHEST_ARMOR)) {
						if (hasChestplate) return false;
						hasChestplate = true;
						chestplate = itemStack.copy();
					} else if (itemStack.is(BackpackUtil.BACKPACKS)) {
						if (hasBackpack) return false;
						hasBackpack = true;
						backpack = itemStack.copy();
					}
				}
			}
			if (hasBackpack && hasChestplate) {
				if (backpack.is(ModItems.NETHERITE_BACKPACK)) {
					DamageResistant resist = chestplate.get(DataComponents.DAMAGE_RESISTANT);
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
			if (itemStack2.is(ItemTags.CHEST_ARMOR)) {
				itemStack = itemStack2.copy();
				itemStack.set(ModDataComponents.BACKPACK_TIER, backpack.getOrDefault(ModDataComponents.BACKPACK_TIER, BackpackTier.LEATHER));
				itemStack.set(DataComponents.CONTAINER, backpack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
				DyedItemColor dye = backpack.get(DataComponents.DYED_COLOR);
				if (dye != null) itemStack.set(ModDataComponents.STORED_BACKPACK_DYE, dye);
			}
		}
		return itemStack;
	}

	@Override
	public @NotNull RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return ModRecipes.ATTACH_BACKPACK;
	}
}
