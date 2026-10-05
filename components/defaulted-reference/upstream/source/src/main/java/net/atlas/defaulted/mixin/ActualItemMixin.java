package net.atlas.defaulted.mixin;

//? >=1.21.5 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.atlas.defaulted.extension.WrapHashedStreamCodec;
import net.minecraft.network.HashedStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
//?}
//? <1.21.5
//import net.atlas.defaulted.Defaulted;
import org.spongepowered.asm.mixin.Mixin;
//? >=1.21.5 {
import org.spongepowered.asm.mixin.injection.At;
//?}

@Mixin(/*? >=1.21.5 {*/ HashedStack.ActualItem.class /*?} else {*/ /*Defaulted.class *//*?}*/)
public class ActualItemMixin {
    //? >= 1.21.5 {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;composite(Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function3;)Lnet/minecraft/network/codec/StreamCodec;"))
    private static StreamCodec<RegistryFriendlyByteBuf, HashedStack.ActualItem> patchCodec(StreamCodec<RegistryFriendlyByteBuf, HashedStack.ActualItem> original) {
        return new WrapHashedStreamCodec(original);
    }
    //?}
}
