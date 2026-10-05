package net.atlas.defaulted.mixin;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.WritableRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(MappedRegistry.class)
public interface MappedRegistryAccessor<T> extends WritableRegistry<T> {
    @Accessor
    Reference2IntMap<T> getToId();
    @Accessor
    Map<T, Holder.Reference<T>> getByValue();
}
