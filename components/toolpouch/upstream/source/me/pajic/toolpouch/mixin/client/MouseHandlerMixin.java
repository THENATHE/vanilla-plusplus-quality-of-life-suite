package me.pajic.toolpouch.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.toolpouch.keybind.ScrollHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

	@Shadow @Final private Minecraft minecraft;

	@WrapOperation(
            method = "onScroll",
            at = @At(
                    value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"
            )
    )
    private void redirectScroll(Inventory instance, int selected, Operation<Void> original, @Local(name = "wheel") int wheel) {
        if (ScrollHandler.handleMouseScroll(minecraft.player, (int) Math.signum(wheel))) original.call(instance, selected);
    }
}
