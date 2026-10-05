package me.pajic.toolpouch.mixin.client.compat;

//? fabric {

import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.pajic.toolpouch.util.ItemStackTemplateUtil;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.pneumono.locator_lodestones.WaypointTracking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Collectors;

@IfModLoaded("locator_lodestones")
@Mixin(WaypointTracking.class)
public abstract class WaypointTrackingMixin {

	@Inject(
			method = "getWaypointsFromPlayer",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Inventory;getNonEquipmentItems()Lnet/minecraft/core/NonNullList;",
					ordinal = 0
			)
	)
	private static void checkToolPouch(
			Player player,
			CallbackInfoReturnable<List<TrackedWaypoint>> cir,
			@Local(name = "stacks") List<ItemStack> stacks
	) {
		stacks.addAll(ToolPouchUtil
				.getItemsFromToolPouch(
						player,
						stack -> stack.is(Items.RECOVERY_COMPASS) ||
								ItemStackTemplateUtil.has(stack, DataComponents.LODESTONE_TRACKER)
				).stream().map(ItemStackTemplate::create)
				.collect(Collectors.toUnmodifiableSet())
		);
	}
}
//?}
