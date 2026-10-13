package com.thenathe.backpackcrafting.compat;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Reads the original request predicates without changing BRBR's crafting validation. */
@Pseudo
@Mixin(targets = "marsh.town.brb.InstantCraftingManager$Pending", remap = false)
public interface BrbrPendingAccess {
    @Accessor("menu") AbstractContainerMenu backpackCrafting$menu();
    @Accessor("resultSlot") int backpackCrafting$resultSlot();
    @Accessor("maximum") boolean backpackCrafting$maximum();
    @Accessor("matchesOutput") Predicate<ItemStack> backpackCrafting$matchesOutput();
    @Accessor("matchesGrid") BooleanSupplier backpackCrafting$matchesGrid();
}
