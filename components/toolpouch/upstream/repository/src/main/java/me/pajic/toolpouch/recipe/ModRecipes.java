package me.pajic.toolpouch.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;

public class ModRecipes {

	public static RecipeSerializer<AttachToolPouchRecipe> ATTACH_TOOL_POUCH = new RecipeSerializer<>(AttachToolPouchRecipe.MAP_CODEC, AttachToolPouchRecipe.STREAM_CODEC);
	public static RecipeSerializer<DetachToolPouchRecipe> DETACH_TOOL_POUCH = new RecipeSerializer<>(DetachToolPouchRecipe.MAP_CODEC, DetachToolPouchRecipe.STREAM_CODEC);
}
