package me.pajic.tiered_backpacks.tooltip;

import com.misterpemodder.shulkerboxtooltip.api.ShulkerBoxTooltipApi;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProviderRegistry;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import me.pajic.tiered_backpacks.TieredBackpacks;
import me.pajic.tiered_backpacks.item.BackpackItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Entrypoint("shulkerboxtooltip")
public class BackpackTooltipCompat implements ShulkerBoxTooltipApi {

    @Override
    public void registerProviders(@NotNull PreviewProviderRegistry registry) {
        Set<Item> items = new HashSet<>();
        BuiltInRegistries.ITEM.forEach(item -> {
            if (item instanceof BackpackItem) {
                items.add(item);
                return;
            }
            Equippable equippable = item.components().get(DataComponents.EQUIPPABLE);
            if (equippable != null && equippable.slot() == EquipmentSlot.CHEST) items.add(item);
        });
        registry.register(TieredBackpacks.id("backpack_tooltip"), new BackpackPreviewProvider(), items);
    }
}
