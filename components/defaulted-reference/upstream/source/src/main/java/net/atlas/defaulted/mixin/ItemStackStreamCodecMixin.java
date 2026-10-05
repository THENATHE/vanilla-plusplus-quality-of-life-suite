package net.atlas.defaulted.mixin;

//? <1.21.5 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
*///?}
import net.atlas.defaulted.Defaulted;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(/*? <1.21.5 {*/ /*targets = {"net.minecraft.world.item.ItemStack$1"} *//*?} else {*/ Defaulted.class /*?}*/)
public class ItemStackStreamCodecMixin {
    //? <1.21.5 {
    /*@WrapMethod(method = "decode")
    public ItemStack wrapDecode(RegistryFriendlyByteBuf buffer, Operation<ItemStack> original) {
        return original.call(buffer);
    }
    @WrapMethod(method = "encode")
    public void wrapEncode(RegistryFriendlyByteBuf buffer, ItemStack stack, Operation<Void> original) {
        Defaulted.encode(original::call, buffer, stack);
    }
    *///?}
}