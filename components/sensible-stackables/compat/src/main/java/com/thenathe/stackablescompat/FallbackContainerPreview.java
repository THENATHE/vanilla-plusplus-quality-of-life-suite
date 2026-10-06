package com.thenathe.stackablescompat;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

/** Vanilla cannot materialize nested templates whose counts exceed its limit of 99. */
public final class FallbackContainerPreview {
    private FallbackContainerPreview() {}

    public static boolean needsProjection(ItemInstance item) {
        return unsafeComponent(item, DataComponents.CONTAINER, 0)
                || unsafeComponent(item, DataComponents.BUNDLE_CONTENTS, 0)
                || unsafeComponent(item, DataComponents.CHARGED_PROJECTILES, 0)
                || unsafeComponent(item, DataComponents.USE_REMAINDER, 0);
    }

    public static ItemStack project(ItemStack original, ItemStack outgoing) {
        if (original.isEmpty() || outgoing.isEmpty()) return outgoing;
        ItemStack result = outgoing;
        for (var type : new DataComponentType<?>[]{DataComponents.CONTAINER,
                DataComponents.BUNDLE_CONTENTS, DataComponents.CHARGED_PROJECTILES,
                DataComponents.USE_REMAINDER}) {
            if (unsafeComponent(original, type, 0) && result.has(type)) {
                if (result == outgoing) result = outgoing.copy();
                // Suppress only an unsupported preview. The authoritative inventory,
                // top-level quantities and Polymer's recovery data are untouched.
                result.remove(type);
            }
        }
        return result;
    }

    private static boolean unsafeTemplate(ItemStackTemplate template, int depth) {
        if (template.count() > 99 || depth > 32) return true;
        return unsafeComponent(template, DataComponents.CONTAINER, depth)
                || unsafeComponent(template, DataComponents.BUNDLE_CONTENTS, depth)
                || unsafeComponent(template, DataComponents.CHARGED_PROJECTILES, depth)
                || unsafeComponent(template, DataComponents.USE_REMAINDER, depth);
    }

    private static boolean unsafeComponent(ItemInstance item, DataComponentType<?> type, int depth) {
        Iterable<ItemStackTemplate> templates;
        if (type == DataComponents.CONTAINER) {
            var value = item.get(DataComponents.CONTAINER);
            if (value == null) return false;
            templates = value.nonEmptyItems();
        } else if (type == DataComponents.BUNDLE_CONTENTS) {
            var value = item.get(DataComponents.BUNDLE_CONTENTS);
            if (value == null) return false;
            templates = value.items();
        } else if (type == DataComponents.CHARGED_PROJECTILES) {
            var value = item.get(DataComponents.CHARGED_PROJECTILES);
            if (value == null) return false;
            templates = value.items();
        } else {
            var value = item.get(DataComponents.USE_REMAINDER);
            return value != null && unsafeTemplate(value.convertInto(), depth + 1);
        }
        for (var template : templates) if (unsafeTemplate(template, depth + 1)) return true;
        return false;
    }
}
