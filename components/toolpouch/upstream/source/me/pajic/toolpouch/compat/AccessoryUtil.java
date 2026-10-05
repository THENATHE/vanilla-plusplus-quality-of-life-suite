package me.pajic.toolpouch.compat;

import me.pajic.toolpouch.item.ToolPouchItem;
import me.pajic.toolpouch.util.CompatFlags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface AccessoryUtil {

    @Nullable AccessoryUtil INSTANCE = makeInstance();

    @Nullable static AccessoryUtil makeInstance() {
        if (CompatFlags.TRINKETS_LOADED) return new TrinketsCompat();
        if (CompatFlags.OHMEGA_LOADED) return new OhmegaCompat();
        //? neoforge
        //if (CompatFlags.CURIOS_LOADED) return new CuriosCompat();
        return null;
    }

    ToolPouchItem makeToolPouch(Item.Properties properties);
    ItemStack tryGetToolPouch(LivingEntity entity);
}
