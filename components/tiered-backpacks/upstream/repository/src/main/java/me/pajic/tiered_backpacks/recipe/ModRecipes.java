package me.pajic.tiered_backpacks.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;

public class ModRecipes {

	public static RecipeSerializer<AttachBackpackRecipe> ATTACH_BACKPACK = new RecipeSerializer<>(AttachBackpackRecipe.MAP_CODEC, AttachBackpackRecipe.STREAM_CODEC);
	public static RecipeSerializer<DetachBackpackRecipe> DETACH_BACKPACK = new RecipeSerializer<>(DetachBackpackRecipe.MAP_CODEC, DetachBackpackRecipe.STREAM_CODEC);
}
