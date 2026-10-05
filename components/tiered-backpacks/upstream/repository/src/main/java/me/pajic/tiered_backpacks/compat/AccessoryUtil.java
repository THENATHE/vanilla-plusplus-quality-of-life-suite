package me.pajic.tiered_backpacks.compat;

import me.pajic.tiered_backpacks.item.BackpackItem;
import me.pajic.tiered_backpacks.util.BackpackTier;
import me.pajic.tiered_backpacks.util.CompatFlags;
import net.minecraft.world.entity.LivingEntity;
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

    BackpackItem makeBackpack(BackpackTier tier);
    ItemStack getBackpack(LivingEntity entity);
}
