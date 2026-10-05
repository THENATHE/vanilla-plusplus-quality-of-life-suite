package me.pajic.toolpouch.keybind;

import me.pajic.toolpouch.hud.ContextualSelectionWidget;
import me.pajic.toolpouch.network.ModPayloads;
import me.pajic.toolpouch.platform.MultiLoaderUtil;
import me.pajic.toolpouch.util.ClientUtil;
import me.pajic.toolpouch.util.GameplayUtil;
import me.pajic.toolpouch.util.ItemStackTemplateUtil;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.List;

public class ScrollHandler {

    public static int selectedShulkerSlot = 0;
    public static int selectedArrowSlot = 0;

    public static boolean handleMouseScroll(LocalPlayer player, int direction) {
        if (ClientUtil.shouldScope) {
            if (direction != 0) {
                ClientUtil.zoomModifier -= direction * (0.1F * ClientUtil.zoomModifier);
                if (ClientUtil.zoomModifier > 10) ClientUtil.zoomModifier = 10;
                else if (ClientUtil.zoomModifier < 0.1) ClientUtil.zoomModifier = 0.1F;
                else {
					player.playSound(SoundEvents.SPYGLASS_STOP_USING);
	                MultiLoaderUtil.INSTANCE.sendToServer(new ModPayloads.C2SPlaySoundPayload(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.SPYGLASS_STOP_USING)));
                }
            }
            return false;
        } else if (GameplayUtil.isHoldingProjectileWeapon(player) && ContextualSelectionWidget.widgetOpen) {
			List<ItemStackTemplate> arrows = ToolPouchUtil.getItemsFromToolPouch(player, GameplayUtil.getSupportedAmmo(player));
			if (!arrows.isEmpty()) {
				int size = arrows.size();
				do {
					selectedArrowSlot -= direction;
					if (selectedArrowSlot < 0) selectedArrowSlot = size - 1;
					if (selectedArrowSlot >= size) selectedArrowSlot = 0;
				} while (ItemStackTemplateUtil.isEmpty(arrows.get(selectedArrowSlot)));
				MultiLoaderUtil.INSTANCE.sendToServer(new ModPayloads.C2SSyncArrowSlot(selectedArrowSlot));
				return false;
			}
        } else if (ContextualSelectionWidget.widgetOpen) {
			List<ItemStackTemplate> shulkers = ToolPouchUtil.getItemsFromToolPouch(player, stack -> stack.is(ItemTags.SHULKER_BOXES));
			if (!shulkers.isEmpty()) {
				int size = shulkers.size();
				do {
					selectedShulkerSlot -= direction;
					if (selectedShulkerSlot < 0) selectedShulkerSlot = size - 1;
					if (selectedShulkerSlot >= size) selectedShulkerSlot = 0;
				} while (ItemStackTemplateUtil.isEmpty(shulkers.get(selectedShulkerSlot)));
				MultiLoaderUtil.INSTANCE.sendToServer(new ModPayloads.C2SSyncShulkerSlot(selectedShulkerSlot));
				return false;
			}
        }
        return true;
    }
}
