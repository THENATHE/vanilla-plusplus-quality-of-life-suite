package net.atlas.defaulted.enchantment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.EquipmentSlotGroup;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record Slots(Optional<List<EquipmentSlotGroup>> slots,
                    List<EquipmentSlotGroup> addedSlots,
                    List<EquipmentSlotGroup> removedSlots) {
    public static final MapCodec<Slots> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(EquipmentSlotGroup.CODEC.listOf().optionalFieldOf("slots").forGetter(Slots::slots),
                            EquipmentSlotGroup.CODEC.listOf().optionalFieldOf("added_slots", Collections.emptyList()).forGetter(Slots::addedSlots),
                            EquipmentSlotGroup.CODEC.listOf().optionalFieldOf("removed_slots", Collections.emptyList()).forGetter(Slots::removedSlots))
                    .apply(instance, Slots::new));
}