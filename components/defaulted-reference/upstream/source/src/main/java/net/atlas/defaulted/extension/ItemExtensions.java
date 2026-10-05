package net.atlas.defaulted.extension;

import net.atlas.defaulted.component.ToolMaterialWrapper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface ItemExtensions {
    default <T> @Nullable T defaulted$get(DataComponentType<T> phantomType)  {
        throw new IllegalStateException("Extension has not been applied");
    }

    default <T> @NonNull T defaulted$getOrDefault(DataComponentType<T> phantomType, T defaultValue)  {
        throw new IllegalStateException("Extension has not been applied");
    }

    default boolean defaulted$has(DataComponentType<?> phantomType)  {
        throw new IllegalStateException("Extension has not been applied");
    }

    default <T> void defaulted$set(DataComponentType<T> phantomType, T phantom)  {
        throw new IllegalStateException("Extension has not been applied");
    }

    default PatchedDataComponentMap defaulted$getPhantomComponentMap()  {
        throw new IllegalStateException("Extension has not been applied");
    }
    default ToolMaterialWrapper defaulted$getToolMaterial() {
      throw new IllegalStateException("Extension has not been applied");
    }
    default void defaulted$setToolMaterial(ToolMaterialWrapper newTier) {
      throw new IllegalStateException("Extension has not been applied");
    }
}
