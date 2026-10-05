package me.pajic.toolpouch.mixin.client.compat;

//? fabric {

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import dev.isxander.zoomify.Zoomify;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
@IfModLoaded("zoomify")
@Mixin(Zoomify.class)
public class ZoomifyMixin {

	@ModifyExpressionValue(
			method = "handleSpyglass",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Inventory;hasAnyMatching(Ljava/util/function/Predicate;)Z"
			)
	)
	private boolean checkToolPouchForSpyglass(boolean original, @Local(argsOnly = true, name = "minecraft") Minecraft minecraft) {
		return original || ToolPouchUtil.toolPouchHasItem(minecraft.player, stack -> stack.is(Items.SPYGLASS));
	}
}
//?}
