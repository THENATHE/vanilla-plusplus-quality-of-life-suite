package com.thenathe.toolpouchcompat;

import me.pajic.mapstitch.item.ModItems;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Materializes immutable pouch contents once, retaining detached mutable results for callers. */
public final class AtlasStackCache {
    private static final boolean STACKABLES_LOADED = FabricLoader.getInstance().isModLoaded("sensible_stackables");
    private static final MethodHandle STACK_SIZE_SNAPSHOT = stackSizeSnapshot();
    private Level level;
    private ItemStack source = ItemStack.EMPTY;
    private ItemContainerContents contents;
    private List<ItemStack> atlases = List.of();
    private Object stackSizes;

    public List<ItemStack> copies(Level currentLevel, ItemStack currentSource) {
        var currentContents = currentSource.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        Object currentStackSizes = stackSizeRevision();
        boolean cacheable = !STACKABLES_LOADED || STACK_SIZE_SNAPSHOT != null;
        if (!cacheable || level != currentLevel || source != currentSource || contents != currentContents || stackSizes != currentStackSizes) {
            // Container components and their templates are immutable. Replacing the component,
            // even on the same pouch stack, invalidates every materialized atlas immediately.
            var materialized = new ArrayList<ItemStack>();
            for (var template : currentContents.nonEmptyItems()) {
                if (template.is(ModItems.ATLAS)) materialized.add(template.create());
            }
            atlases = List.copyOf(materialized);
            level = currentLevel;
            source = currentSource;
            contents = currentContents;
            stackSizes = currentStackSizes;
        }
        // MapStitch retains and can mutate returned stacks. Copies preserve that ownership
        // without repeating template validation, including validation of every nested map.
        return atlases.isEmpty() ? List.of() : atlases.stream().map(ItemStack::copy).toList();
    }

    private static MethodHandle stackSizeSnapshot() {
        // This addon also runs without Sensible Stackables. Resolve its public API only
        // when present, without making the standalone pouch integration depend on it.
        if (!STACKABLES_LOADED) return null;
        try {
            var overrides = Class.forName("me.pajic.sensible_stackables.handler.StackSizeOverrides");
            return MethodHandles.publicLookup().findStatic(overrides, "snapshot", MethodType.methodType(Map.class))
                    .asType(MethodType.methodType(Object.class));
        } catch (ReflectiveOperationException exception) {
            // Older optional releases have no revision API. Keep fresh validation
            // for those installations instead of caching across unknown changes.
            return null;
        }
    }

    private static Object stackSizeRevision() {
        if (STACK_SIZE_SNAPSHOT == null) return null;
        try {
            // set, clear and computeAndSet replace this table. A new table can make
            // an unchanged atlas or nested map invalid, or restore a rejected stack.
            return (Object) STACK_SIZE_SNAPSHOT.invokeExact();
        } catch (RuntimeException | Error exception) {
            throw exception;
        } catch (Throwable exception) {
            throw new IllegalStateException("Cannot read Sensible Stackables overrides", exception);
        }
    }
}
