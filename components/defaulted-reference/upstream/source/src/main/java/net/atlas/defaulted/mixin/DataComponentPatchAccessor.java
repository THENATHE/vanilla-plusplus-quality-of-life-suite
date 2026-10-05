package net.atlas.defaulted.mixin;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

//? <26.3
//import java.util.Optional;

@Mixin(DataComponentPatch.class)
public interface DataComponentPatchAccessor {
    @Invoker("<init>")
    //~ if >26.2 'Optional<?>> map' -> 'Object> map'
    static DataComponentPatch create(final Reference2ObjectMap<DataComponentType<?>, Object> map) {
        throw new AssertionError();
    }
    @Accessor
    //~ if >26.2 'Optional<?>>' -> 'Object>'
    Reference2ObjectMap<DataComponentType<?>, Object> getMap();
}
