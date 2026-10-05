package me.pajic.toolpouch.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(MapItemSavedData.class)
public class MapItemSavedDataMixin {

	@ModifyExpressionValue(
			method = "tickCarriedBy",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Inventory;contains(Ljava/util/function/Predicate;)Z"
			)
	)
	private boolean checkToolPouchForMaps(
			boolean original,
			@Local(argsOnly = true, name = "tickingPlayer") Player tickingPlayer,
			@Local(name = "mapMatcher") Predicate<ItemStack> mapMatcher
	) {
		return original || ToolPouchUtil.toolPouchHasItem(tickingPlayer, itemStack ->
				mapMatcher.test(itemStack.create()) || itemStack.is(ResourceKey.create(Registries.ITEM, Identifier.parse("improved-maps:atlas")))
		);
	}
}
