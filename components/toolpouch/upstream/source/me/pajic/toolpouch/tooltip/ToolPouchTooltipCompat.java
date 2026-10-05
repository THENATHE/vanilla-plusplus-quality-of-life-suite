package me.pajic.toolpouch.tooltip;

import com.misterpemodder.shulkerboxtooltip.api.ShulkerBoxTooltipApi;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProviderRegistry;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.util.GameplayUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Entrypoint("shulkerboxtooltip")
public class ToolPouchTooltipCompat implements ShulkerBoxTooltipApi {

    @Override
    public void registerProviders(@NotNull PreviewProviderRegistry registry) {
		Set<Item> items = new HashSet<>();
		BuiltInRegistries.ITEM.getTagOrEmpty(GameplayUtil.TOOL_POUCHES).forEach(item -> items.add(item.value()));
		BuiltInRegistries.ITEM.getTagOrEmpty(ItemTags.LEG_ARMOR).forEach(item -> items.add(item.value()));
        registry.register(ToolPouch.id("tool_pouch_tooltip"), new ToolPouchPreviewProvider(), items);
    }
}
