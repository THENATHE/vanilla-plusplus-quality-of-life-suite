package me.pajic.tiered_backpacks.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.pajic.tiered_backpacks.component.ModDataComponents;
import me.pajic.tiered_backpacks.util.BackpackTier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.equipment.Equippable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(ItemContainerContents.class)
public class ItemContainerContentsMixin {

	@WrapMethod(method = "addToTooltip")
	private void addAttachedBackpackTooltip(
            Item.TooltipContext context,
            Consumer<Component> consumer,
            TooltipFlag flag,
            DataComponentGetter components,
            Operation<Void> original
    ) {
		Equippable equippable = components.get(DataComponents.EQUIPPABLE);
		ItemAttributeModifiers modifiers = components.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		if (equippable != null && equippable.slot() == EquipmentSlot.CHEST && modifiers != ItemAttributeModifiers.EMPTY) {
			BackpackTier tier = components.getOrDefault(ModDataComponents.BACKPACK_TIER, BackpackTier.LEATHER);
			consumer.accept(Component.translatable(
					"text.tiered_backpacks.attachment",
					Component.translatable(
							"tiered_backpacks.backpackTier." +
							tier.getSerializedName().toUpperCase()
					).withColor(tier.getColor())
			).withStyle(ChatFormatting.BLUE));
		}
		original.call(context, consumer, flag, components);
	}
}
