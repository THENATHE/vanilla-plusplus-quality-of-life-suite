package com.thenathe.chalkcompat;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.dafuqs.chalk.common.items.ChalkItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

/** Shapeless chalk conversions with vanilla recipe-book displays and original stack components. */
public final class ChalkConversionRecipe extends NormalCraftingRecipe {
    public static final MapCodec<ChalkConversionRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
            Ingredient.CODEC.listOf(2, 3).fieldOf("ingredients").forGetter(recipe -> recipe.ingredients)
    ).apply(instance, ChalkConversionRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChalkConversionRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, recipe -> recipe.bookInfo,
            ItemStackTemplate.STREAM_CODEC, recipe -> recipe.result,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), recipe -> recipe.ingredients,
            ChalkConversionRecipe::new);
    public static final RecipeSerializer<ChalkConversionRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
    private final ItemStackTemplate result;
    private final List<Ingredient> ingredients;
    private final ShapelessRecipe shapeless;

    public ChalkConversionRecipe(Recipe.CommonInfo common, CraftingRecipe.CraftingBookInfo book,
                                 ItemStackTemplate result, List<Ingredient> ingredients) {
        super(common, book);
        this.result = result;
        this.ingredients = List.copyOf(ingredients);
        this.shapeless = new ShapelessRecipe(common, book, result, this.ingredients);
    }

    public static void initialize() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath("chalk_polymer_compat", "chalk_conversion"), SERIALIZER);
    }

    @Override public RecipeSerializer<ChalkConversionRecipe> getSerializer() { return SERIALIZER; }
    @Override protected PlacementInfo createPlacementInfo() { return shapeless.placementInfo(); }
    @Override public List<RecipeDisplay> display() { return shapeless.display(); }
    @Override public boolean matches(CraftingInput input, Level level) {
        return shapeless.matches(input, level) && input.items().stream()
                .filter(stack -> stack.getItem() instanceof ChalkItem).count() == 1;
    }
    @Override public ItemStack assemble(CraftingInput input) {
        for (var stack : input.items()) {
            if (stack.getItem() instanceof ChalkItem) {
                // Minecraft's transmutation preserves the source patch (damage, names, custom data,
                // enchantments, component removals) while adopting the new item's default model/name.
                return TransmuteRecipe.createWithOriginalComponents(result, stack);
            }
        }
        return ItemStack.EMPTY;
    }
}
