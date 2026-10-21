package qa;

import com.google.gson.JsonObject;
import com.thenathe.stackablescompat.ComponentPatchLookup;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import me.pajic.sensible_stackables.handler.StackSizeOverrides;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

/** Actual transformed template getters, including the allocation-heavy nonempty patch. */
final class TemplateLookupQa {
    private static volatile long benchmarkResult;
    private int assertions;

    private void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        assertions++;
    }

    static JsonObject run(boolean uncapped) {
        return new TemplateLookupQa().checkTemplates(uncapped);
    }

    private JsonObject checkTemplates(boolean uncapped) {
        var savedOverrides = new HashMap<>(StackSizeOverrides.snapshot());
        var name = Component.literal("Template allocation fixture");
        var unrelatedPatch = DataComponentPatch.builder().set(DataComponents.CUSTOM_NAME, name).build();
        var template = new ItemStackTemplate(Items.POTION, unrelatedPatch);
        var emptyPatchTemplate = new ItemStackTemplate(Items.POTION);
        var removedPatch = DataComponentPatch.builder().remove(DataComponents.MAX_STACK_SIZE).build();
        var removedTemplate = new ItemStackTemplate(Items.POTION, removedPatch);
        var unrelatedRemoval = new ItemStackTemplate(Items.POTION,
                DataComponentPatch.builder().remove(DataComponents.CUSTOM_NAME).build());
        JsonObject result = new JsonObject();
        try {
            StackSizeOverrides.set(Map.of(Items.POTION, 84));
            check(template.get(DataComponents.MAX_STACK_SIZE) == 84, "nonempty unrelated patch receives live override");
            check(emptyPatchTemplate.get(DataComponents.MAX_STACK_SIZE) == 84, "empty patch receives live override");
            check(unrelatedRemoval.get(DataComponents.MAX_STACK_SIZE) == 84, "unrelated removal receives live override");
            check(removedTemplate.get(DataComponents.MAX_STACK_SIZE) == null, "explicit removal remains null with override");
            check(template.get(DataComponents.CUSTOM_NAME) == name, "other component getter preserves identity");
            check(template.get(DataComponents.CUSTOM_DATA) == null, "missing unrelated component remains null");
            check(unrelatedRemoval.get(DataComponents.CUSTOM_NAME) == null, "unrelated explicit removal remains null");
            check(template.components() == unrelatedPatch, "lookup preserves authoritative immutable patch");
            check(ComponentPatchLookup.contains(unrelatedPatch, DataComponents.CUSTOM_NAME), "generic helper recognizes component addition");
            check(!ComponentPatchLookup.contains(unrelatedPatch, DataComponents.CUSTOM_DATA), "generic helper does not expose sentinel as component");
            check(ComponentPatchLookup.contains(removedPatch, DataComponents.MAX_STACK_SIZE), "generic helper recognizes removal");
            for (int explicit : new int[]{Integer.MIN_VALUE, -1, 0, 1, 7, 99, 2048, Integer.MAX_VALUE}) {
                var patch = DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, explicit).build();
                var patched = new ItemStackTemplate(Items.POTION, patch);
                check(patched.get(DataComponents.MAX_STACK_SIZE) == explicit, "explicit maximum retained exactly: " + explicit);
                check(patched.components() == patch, "explicit patch is not rewritten: " + explicit);
            }

            result.add("allocation", measureAllocation(template));
            int updatedLimit = uncapped ? 2048 : 73;
            StackSizeOverrides.set(Map.of(Items.POTION, updatedLimit));
            check(template.get(DataComponents.MAX_STACK_SIZE) == updatedLimit, "existing template sees changed live table");
            check(template.create().getMaxStackSize() == updatedLimit, "created stack agrees with changed template limit");
            check(Items.POTION.components().get(DataComponents.MAX_STACK_SIZE) == 1, "live override does not mutate item prototype");
            for (int invalidOverride : new int[]{0, -1}) {
                StackSizeOverrides.set(Map.of(Items.POTION, invalidOverride));
                check(template.get(DataComponents.MAX_STACK_SIZE) == 1, "nonpositive override falls back to prototype");
                check(removedTemplate.get(DataComponents.MAX_STACK_SIZE) == null, "nonpositive override preserves removal");
            }
            StackSizeOverrides.clear();
            check(template.get(DataComponents.MAX_STACK_SIZE) == 1, "table clear restores existing template prototype");
            check(removedTemplate.get(DataComponents.MAX_STACK_SIZE) == null, "table clear preserves explicit removal");

            var sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.set(DataComponents.MAX_STACK_SIZE, 2);
            var invalidTemplate = ItemStackTemplate.fromNonEmptyStack(sword);
            check(invalidTemplate.get(DataComponents.MAX_STACK_SIZE) == 2, "invalid explicit stackability remains visible");
            check(invalidTemplate.create().isEmpty(), "strict validation still rejects damageable stackable template");
            check(new ItemStackTemplate(Items.STONE, -1).create().isEmpty(), "strict validation still rejects negative quantity");
        } finally {
            StackSizeOverrides.set(savedOverrides);
        }
        result.addProperty("passed", true);
        result.addProperty("assertions", assertions);
        return result;
    }

    private JsonObject measureAllocation(ItemStackTemplate template) {
        var bean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        check(bean.isThreadAllocatedMemorySupported(), "JVM supports allocation accounting");
        if (!bean.isThreadAllocatedMemoryEnabled()) bean.setThreadAllocatedMemoryEnabled(true);
        for (int round = 0; round < 4; round++) {
            readLimits(template, true, 25_000);
            readLimits(template, false, 25_000);
        }
        int iterations = 200_000;
        long thread = Thread.currentThread().threadId();
        long beforeLegacy = bean.getThreadAllocatedBytes(thread);
        readLimits(template, true, iterations);
        long legacyBytes = bean.getThreadAllocatedBytes(thread) - beforeLegacy;
        long beforeOptimized = bean.getThreadAllocatedBytes(thread);
        readLimits(template, false, iterations);
        long optimizedBytes = bean.getThreadAllocatedBytes(thread) - beforeOptimized;
        check(benchmarkResult == 84L * iterations, "benchmark consumes real template results");
        check(legacyBytes > 0 && optimizedBytes < legacyBytes / 10,
                "nonempty patch getter removes split allocation: old=" + legacyBytes + " new=" + optimizedBytes);
        JsonObject allocation = new JsonObject();
        allocation.addProperty("lookups", iterations);
        allocation.addProperty("legacy_split_bytes", legacyBytes);
        allocation.addProperty("optimized_getter_bytes", optimizedBytes);
        allocation.addProperty("legacy_bytes_per_lookup", (double) legacyBytes / iterations);
        allocation.addProperty("optimized_bytes_per_lookup", (double) optimizedBytes / iterations);
        return allocation;
    }

    private static void readLimits(ItemStackTemplate template, boolean legacy, int iterations) {
        long sum = 0;
        for (int index = 0; index < iterations; index++) {
            if (legacy) {
                var type = DataComponents.MAX_STACK_SIZE;
                Integer original = template.components().get(template.typeHolder().components(), type);
                var split = template.components().split();
                int override = split.added().has(type) || split.removed().contains(type)
                        ? -1 : StackSizeOverrides.get(template.typeHolder().value());
                sum += override > 0 ? override : original;
            } else {
                sum += template.get(DataComponents.MAX_STACK_SIZE);
            }
        }
        benchmarkResult = sum;
    }
}
