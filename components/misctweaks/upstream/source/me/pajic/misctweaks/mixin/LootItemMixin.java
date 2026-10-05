package me.pajic.misctweaks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.misctweaks.MiscTweaks;
import me.pajic.misctweaks.util.ModUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LootItem.class)
public abstract class LootItemMixin {

    @SuppressWarnings("unchecked")
	@ModifyArg(
            method = "createItemStack",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"
            )
    )
    private <T> T swapLootItem(T stack, @Local(argsOnly = true) LootContext context) {
		ItemStack original = (ItemStack) stack;
        if (MiscTweaks.CONFIG.randomizeDiscLoot.get() && (original.is(Items.MUSIC_DISC_13) || original.is(Items.MUSIC_DISC_CAT))) {
            return (T) new ItemStack(ModUtil.DISCS.get(context.getRandom().nextInt(ModUtil.DISCS.size())));
        }
        return (T) original;
    }
}
