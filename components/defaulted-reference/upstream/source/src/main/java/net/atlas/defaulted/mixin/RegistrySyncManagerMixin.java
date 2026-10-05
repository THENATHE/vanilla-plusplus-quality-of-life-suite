package net.atlas.defaulted.mixin;

//? <=1.21.1 && fabric {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
*///?}
//? >1.21.1 || neoforge
import net.atlas.defaulted.Defaulted;
import org.spongepowered.asm.mixin.Mixin;
//? <=1.21.1 && fabric
//import org.spongepowered.asm.mixin.injection.At;

@Mixin(/*? <=1.21.1 && fabric {*/ /*RegistrySyncManager.class *//*?} else {*/ Defaulted.class /*?}*/)
public class RegistrySyncManagerMixin {
    //? <=1.21.1 && fabric {
    /*@WrapOperation(method = "createAndPopulateRegistryMap", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/event/registry/RegistryAttributeHolder;hasAttribute(Lnet/fabricmc/fabric/api/event/registry/RegistryAttribute;)Z"), require = 0, remap = false)
    private static boolean skipDefaultedRegistries(RegistryAttributeHolder instance, RegistryAttribute registryAttribute, Operation<Boolean> original, @Local(ordinal = 0) Registry<?> reg) {
        if (registryAttribute == RegistryAttribute.MODDED && reg.key().location().getNamespace().equals("defaulted")) {
            return false;
        }

        return original.call(instance, registryAttribute);
    }

    @WrapOperation(method = "createAndPopulateRegistryMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;getKey(Ljava/lang/Object;)Lnet/minecraft/resources/Identifier;"), require = 0)
    private static Identifier skipServerEntries(Registry<?> instance, Object t, Operation<Identifier> original) {
        Identifier loc = original.call(instance, t);
        if (loc.getNamespace().equals("defaulted")) {
            return null;
        }
        return loc;
    }
    *///?}
}