package me.pajic.toolpouch.mixin;

import me.pajic.toolpouch.util.ItemStackTemplateUtil;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MapItem.class)
public abstract class MapItemMixin {

	@Shadow
	public abstract void update(Level level, Entity player, MapItemSavedData data);

	@SuppressWarnings("DataFlowIssue")
	@Inject(
			method = "inventoryTick",
			at = @At("TAIL")
	)
	private void updateMapsInToolPouch(ItemStack itemStack, ServerLevel level, Entity owner, EquipmentSlot slot, CallbackInfo ci) {
		if (owner instanceof Player player) {
			List<ItemStackTemplate> maps = ToolPouchUtil.getItemsFromToolPouch(player, stack -> ItemStackTemplateUtil.has(stack, DataComponents.MAP_ID));
			if (!maps.isEmpty()) {
				ItemStackTemplate map = maps.getFirst();
				MapId mapId = map.get(DataComponents.MAP_ID);
				MapItemSavedData mapData = MapItem.getSavedData(map.get(DataComponents.MAP_ID), level);
				update(level, owner, mapData);
				if (player instanceof ServerPlayer serverPlayer) {
					Packet<?> packet = mapData.getUpdatePacket(mapId, serverPlayer);
					if (packet != null) serverPlayer.connection.send(packet);
				}
			}
		}
	}
}
