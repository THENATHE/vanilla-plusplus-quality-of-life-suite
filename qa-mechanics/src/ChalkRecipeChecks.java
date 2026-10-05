package chalk.qa;

import de.dafuqs.chalk.common.ChalkRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/** Exhaustive conversion matrix against the actual loaded server recipes, including reloads. */
public final class ChalkRecipeChecks {
    public static ItemStack namedDamaged(Item item) {
        var stack = new ItemStack(item);
        stack.setDamageValue(37);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Chalk conversion QA"));
        var data = new CompoundTag();
        data.putString("test", "preserved");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return stack;
    }

    public static void inspect(ItemStack result, Item expected) {
        require(result.is(expected) && result.getCount() == 1, "conversion item/count");
        require(result.getDamageValue() == 37, "conversion preserves durability");
        require(Component.literal("Chalk conversion QA").equals(result.get(DataComponents.CUSTOM_NAME)), "conversion preserves custom name");
        require(namedDamaged(expected).get(DataComponents.CUSTOM_DATA).equals(result.get(DataComponents.CUSTOM_DATA)), "conversion preserves custom data");
    }

    public static void check(MinecraftServer server) {
        int checks = 0;
        for (var source : ChalkRegistry.chalkVariants.entrySet()) {
            var normal = source.getValue().chalkItem;
            var glow = source.getValue().glowChalkItem;
            checkConversion(server, glow, namedDamaged(normal), new ItemStack(Items.GLOW_INK_SAC)); checks++;
            reject(server, namedDamaged(glow), new ItemStack(Items.GLOW_INK_SAC));
            reject(server, namedDamaged(normal));
            reject(server, namedDamaged(normal), namedDamaged(normal), new ItemStack(Items.GLOW_INK_SAC));
            reject(server, namedDamaged(normal), new ItemStack(Items.GLOW_INK_SAC), new ItemStack(Items.GLOW_INK_SAC));
            for (DyeColor targetColor : DyeColor.values()) {
                var target = ChalkRegistry.chalkVariants.get(targetColor);
                var dye = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(targetColor.getSerializedName() + "_dye"));
                for (boolean wasGlowing : new boolean[]{false, true}) {
                    var inputItem = wasGlowing ? glow : normal;
                    if (target == null || source.getKey() == targetColor) {
                        reject(server, namedDamaged(inputItem), new ItemStack(dye));
                    } else {
                        checkConversion(server, wasGlowing ? target.glowChalkItem : target.chalkItem, namedDamaged(inputItem), new ItemStack(dye)); checks++;
                    }
                    if (!wasGlowing && target != null) {
                        checkConversion(server, target.glowChalkItem, namedDamaged(inputItem), new ItemStack(dye), new ItemStack(Items.GLOW_INK_SAC)); checks++;
                    } else {
                        reject(server, namedDamaged(inputItem), new ItemStack(dye), new ItemStack(Items.GLOW_INK_SAC));
                    }
                    reject(server, namedDamaged(inputItem), new ItemStack(dye), new ItemStack(dye));
                    reject(server, namedDamaged(inputItem), new ItemStack(dye), new ItemStack(Items.DIRT));
                }
            }
        }
        System.out.println("CHALK_QA_PASS conversions " + checks + " positive transitions, all dyes, invalid extras/no-ops, preserved damage/name/custom data");
    }

    private static CraftingInput input(ItemStack... stacks) {
        var items = new ArrayList<>(List.of(stacks));
        while (items.size() < 4) items.add(ItemStack.EMPTY);
        return CraftingInput.of(2, 2, items);
    }
    private static void checkConversion(MinecraftServer server, Item expected, ItemStack... stacks) {
        var input = input(stacks);
        var recipe = server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, server.overworld()).orElseThrow();
        inspect(recipe.value().assemble(input), expected);
        require(stacks[0].getDamageValue() == 37 && stacks[0].getCount() == 1, "assembly does not mutate source");
        var reversed = new ArrayList<>(input.items());
        java.util.Collections.reverse(reversed);
        while (reversed.size() < 4) reversed.add(ItemStack.EMPTY);
        var reverseInput = CraftingInput.of(2, 2, reversed);
        require(recipe.value().matches(reverseInput, server.overworld()), "conversion is shapeless");
        inspect(recipe.value().assemble(reverseInput), expected);
        var result = recipe.value().assemble(input);
        require(java.util.Objects.equals(result.get(DataComponents.ITEM_MODEL), expected.getDefaultInstance().get(DataComponents.ITEM_MODEL)), "conversion adopts target model");
        require(java.util.Objects.equals(result.get(DataComponents.ITEM_NAME), expected.getDefaultInstance().get(DataComponents.ITEM_NAME)), "conversion adopts target item name");
        var removed = stacks[0].copy();
        removed.remove(DataComponents.ITEM_NAME);
        var removalInputs = stacks.clone();
        removalInputs[0] = removed;
        var removalResult = recipe.value().assemble(input(removalInputs));
        inspect(removalResult, expected);
        require(!removalResult.has(DataComponents.ITEM_NAME), "conversion preserves explicit default-component removal");
        require(recipe.value().display().stream().allMatch(display -> display instanceof net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay), "vanilla recipe-book display");
    }
    private static void reject(MinecraftServer server, ItemStack... stacks) {
        require(server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input(stacks), server.overworld()).isEmpty(), "invalid/no-op conversion rejected: " + List.of(stacks));
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError("CHALK_QA_FAIL: " + message);
    }
}
