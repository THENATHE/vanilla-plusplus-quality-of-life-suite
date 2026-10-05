package org.sharedregionmaps.mixin;

import java.nio.file.Path;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Uses Minecraft's exact world/dimension storage path for fail-closed loading. */
@Mixin(SavedDataStorage.class)
public interface SavedDataStorageAccessor {
    @Invoker("getDataFile")
    Path sharedmaps$getDataFile(Identifier id);
}
