package com.thenathe.stackablescompat;

import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

/** Check patch precedence without materializing its added and removed components. */
public final class ComponentPatchLookup {
    private static final Object ABSENT = new Object();
    private static final DataComponentGetter ABSENT_PROTOTYPE = new DataComponentGetter() {
        @Override
        @SuppressWarnings("unchecked")
        public <T> T get(DataComponentType<? extends T> type) {
            return (T) ABSENT;
        }
    };

    private ComponentPatchLookup() {}

    public static boolean contains(DataComponentPatch patch, DataComponentType<?> type) {
        // Keep the result erased: the sentinel represents absence, whereas null
        // represents an explicit removal. Neither escapes as a component value.
        Object value = patch.get(ABSENT_PROTOTYPE, type);
        return value != ABSENT;
    }
}
