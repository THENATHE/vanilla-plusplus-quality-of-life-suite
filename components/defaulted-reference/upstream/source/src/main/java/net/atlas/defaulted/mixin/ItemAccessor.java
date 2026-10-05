package net.atlas.defaulted.mixin;

//? <26.1 {
/*import net.minecraft.core.component.DataComponentMap;
*///?}
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
//? <26.1 {
/*import org.spongepowered.asm.mixin.gen.Accessor;
*///?}

@Mixin(Item.class)
public interface ItemAccessor {
    //? <26.1 {
    /*@Accessor
    void setComponents(DataComponentMap dataComponents);
    *///?}
}