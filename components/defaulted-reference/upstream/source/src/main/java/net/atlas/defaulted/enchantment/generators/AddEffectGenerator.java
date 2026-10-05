package net.atlas.defaulted.enchantment.generators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.defaulted.enchantment.EnchantmentBuilder;
import net.atlas.defaulted.enchantment.EnchantmentPatchGenerator;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.Util;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record AddEffectGenerator<T>(DataComponentType<List<T>> type, List<T> toAdd, List<T> toRemove) implements EnchantmentPatchGenerator {
    public static final List<DataComponentType<? extends List<?>>> VALID_TYPES = Util.make(new ArrayList<>(), list -> {
        list.add(EnchantmentEffectComponents.DAMAGE_PROTECTION);
        list.add(EnchantmentEffectComponents.DAMAGE_IMMUNITY);
        list.add(EnchantmentEffectComponents.DAMAGE);
        list.add(EnchantmentEffectComponents.SMASH_DAMAGE_PER_FALLEN_BLOCK);
        list.add(EnchantmentEffectComponents.KNOCKBACK);
        list.add(EnchantmentEffectComponents.ARMOR_EFFECTIVENESS);
        list.add(EnchantmentEffectComponents.POST_ATTACK);
        //? >=1.21.11
        list.add(EnchantmentEffectComponents.POST_PIERCING_ATTACK);
        list.add(EnchantmentEffectComponents.HIT_BLOCK);
        list.add(EnchantmentEffectComponents.ITEM_DAMAGE);
        list.add(EnchantmentEffectComponents.EQUIPMENT_DROPS);
        list.add(EnchantmentEffectComponents.LOCATION_CHANGED);
        list.add(EnchantmentEffectComponents.TICK);
        list.add(EnchantmentEffectComponents.AMMO_USE);
        list.add(EnchantmentEffectComponents.PROJECTILE_PIERCING);
        list.add(EnchantmentEffectComponents.PROJECTILE_SPAWNED);
        list.add(EnchantmentEffectComponents.PROJECTILE_SPREAD);
        list.add(EnchantmentEffectComponents.PROJECTILE_COUNT);
        list.add(EnchantmentEffectComponents.TRIDENT_RETURN_ACCELERATION);
        list.add(EnchantmentEffectComponents.FISHING_TIME_REDUCTION);
        list.add(EnchantmentEffectComponents.FISHING_LUCK_BONUS);
        list.add(EnchantmentEffectComponents.BLOCK_EXPERIENCE);
        list.add(EnchantmentEffectComponents.MOB_EXPERIENCE);
        list.add(EnchantmentEffectComponents.REPAIR_WITH_XP);
        list.add(EnchantmentEffectComponents.ATTRIBUTES);
        list.add(EnchantmentEffectComponents.CROSSBOW_CHARGING_SOUNDS);
        list.add(EnchantmentEffectComponents.TRIDENT_SOUND);
    });
    public static final MapCodec<AddEffectGenerator<?>> CODEC = EnchantmentEffectComponents.COMPONENT_CODEC.validate(dataComponentType -> VALID_TYPES.contains(dataComponentType) ? DataResult.success(dataComponentType) : DataResult.error(() -> "Not a valid list component type!"))
            .dispatchMap("component", AddEffectGenerator::type, type -> RecordCodecBuilder.mapCodec(instance ->
                    instance.group(((Codec<List>)type.codec()).optionalFieldOf("to_add", Collections.emptyList()).forGetter(AddEffectGenerator::toAdd),
                                    ((Codec<List>)type.codec()).optionalFieldOf("to_remove", Collections.emptyList()).forGetter(AddEffectGenerator::toRemove))
                            .apply(instance, (toAdd, toRemove) -> new AddEffectGenerator<Object>((DataComponentType<List<Object>>) type, toAdd, toRemove))));

    @Override
    public void patchDataComponentMap(Holder<Enchantment> enchantment, EnchantmentBuilder builder) {
        if (this.toAdd().isEmpty() && this.toRemove().isEmpty()) return;
        List<T> original = new ArrayList<>(builder.getOrDefault(this.type(), Collections.emptyList()));
        original.removeAll(this.toRemove());
        original.addAll(this.toAdd());
        builder.set(this.type, Collections.unmodifiableList(original));
    }

    @Override
    public MapCodec<? extends EnchantmentPatchGenerator> codec() {
        return CODEC;
    }
}
