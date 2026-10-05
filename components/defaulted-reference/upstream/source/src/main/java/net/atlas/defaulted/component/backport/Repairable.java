package net.atlas.defaulted.component.backport;

//? <=1.21.1 {
/*import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.atomic.AtomicBoolean;

public record Repairable(Either<HolderSet<Item>, TagKey<Item>> repairItems) {
    public static Codec<Repairable> CODEC = Codec.either(RegistryCodecs.holderSet(Registries.ITEM), TagKey.hashedCodec(Registries.ITEM)).xmap(Repairable::new, Repairable::repairItems).fieldOf("items").codec();
    public boolean isValidRepairItem(ItemStack repairStack) {
        AtomicBoolean matches = new AtomicBoolean(false);
        repairItems.ifLeft(holders -> matches.set(holders.contains(repairStack.getItemHolder())));
        repairItems.ifRight(tag -> matches.set(repairStack.is(tag)));
        return matches.get();
    }
}
*///?}