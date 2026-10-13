package com.thenathe.backpackcrafting.mixin.client;

import java.util.List;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RecipeBookComponent.class)
public interface RecipeBookBoundsAccessor {
    @Invoker("getXOrigin")
    int backpackCrafting$getBookX();

    @Invoker("getYOrigin")
    int backpackCrafting$getBookY();

    @Accessor("tabButtons")
    List<RecipeBookTabButton> backpackCrafting$getTabs();
}
