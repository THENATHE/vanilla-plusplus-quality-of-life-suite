package net.atlas.defaulted.component.backport;

import net.atlas.defaulted.Defaulted;
//? <=1.21.1
//import net.atlas.defaulted.init.registry.AbstractedHolder;
import net.atlas.defaulted.component.phantom.EnchantmentOverrides;
import net.atlas.defaulted.init.registry.AbstractedHolder;
import net.atlas.defaulted.init.registry.Bootstrapper;
import net.minecraft.core.component.DataComponentType;

public class PhantomDataComponents extends Bootstrapper<DataComponentType<?>> {
    public static final PhantomDataComponents INSTANCE = new PhantomDataComponents();
    //? <=1.21.1 {
    /*public static AbstractedHolder<DataComponentType<?>, DataComponentType<Enchantable>> ENCHANTABLE = null;
    public static AbstractedHolder<DataComponentType<?>, DataComponentType<Repairable>> REPAIRABLE = null;
    *///?}
    public static AbstractedHolder<DataComponentType<?>, DataComponentType<EnchantmentOverrides>> ENCHANTMENT_OVERRIDES = null;
    public PhantomDataComponents() {
        super(Defaulted.PHANTOM_COMPONENT_TYPE, "minecraft");
    }

    @Override
    protected void bootstrap() {
        //? <=1.21.1 {
        /*ENCHANTABLE = register("enchantable", () -> DataComponentType.<Enchantable>builder().persistent(Enchantable.CODEC).build());
        REPAIRABLE = register("repairable", () -> DataComponentType.<Repairable>builder().persistent(Repairable.CODEC).build());
        *///?}
        ENCHANTMENT_OVERRIDES = register("enchantment_overrides", () -> DataComponentType.<EnchantmentOverrides>builder().persistent(EnchantmentOverrides.CODEC).build());
    }
}
