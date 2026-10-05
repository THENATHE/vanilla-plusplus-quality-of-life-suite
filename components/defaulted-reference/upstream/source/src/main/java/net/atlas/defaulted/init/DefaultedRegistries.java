package net.atlas.defaulted.init;

import net.atlas.defaulted.component.backport.PhantomDataComponents;
import net.atlas.defaulted.component.generators.*;
import net.atlas.defaulted.component.generators.condition.PatchConditions;
import net.atlas.defaulted.enchantment.generators.condition.EnchantmentPatchConditions;
import net.atlas.defaulted.enchantment.value_provider.ValueProviders;
//? fabric {
import net.atlas.defaulted.fabric.util.FabricUtils;
//? >1.21.1
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
//?} neoforge {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
*///?}
import net.minecraft.core.Registry;
//? fabric
import net.minecraft.resources.ResourceKey;


public class DefaultedRegistries {

    //? neoforge {
    /*public static void init(IEventBus modBus) {
    *///?} fabric {
    public static void init() {
    //?}
        ItemPatchGenerators.INSTANCE.init(/*? neoforge {*/ /*modBus *//*?}*/);
        EnchantmentPatchGenerators.INSTANCE.init(/*? neoforge {*/ /*modBus *//*?}*/);
        PhantomDataComponents.INSTANCE.init(/*? neoforge {*/ /*modBus *//*?}*/);
        PatchConditions.INSTANCE.init(/*? neoforge {*/ /*modBus *//*?}*/);
        EnchantmentPatchConditions.INSTANCE.init(/*? neoforge {*/ /*modBus *//*?}*/);
        ValueProviders.INSTANCE.init(/*? neoforge {*/ /*modBus *//*?}*/);
        WeaponLevelBasedValues.INSTANCE.init(/*? neoforge {*/ /*modBus *//*?}*/);
        LevelConditions.INSTANCE.init(/*? neoforge {*/ /*modBus *//*?}*/);
    }

    //? fabric {
    public static <T> Registry<T> createRegistry(ResourceKey<Registry<T>> key) {
    //?} neoforge {
    /*public static <T> Registry<T> createRegistry(DeferredRegister<T> register) {
    *///?}
        //? fabric {
        return FabricUtils.createRegistry(
                key
        )/*? >1.21.1 {*/ .attribute(RegistryAttribute.OPTIONAL) /*?}*/.buildAndRegister();
        //?} neoforge {
        /*return register.makeRegistry(builder -> builder.sync(false));
        *///?}
    }
}