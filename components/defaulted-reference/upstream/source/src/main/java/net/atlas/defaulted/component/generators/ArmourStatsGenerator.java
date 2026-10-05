package net.atlas.defaulted.component.generators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.generators.handler.ArmourVariable;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
//? <=1.21.1
//import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record ArmourStatsGenerator(ArmourVariable<Integer> durability, ArmourVariable<Integer> protection, ArmourVariable<Double> armourToughness, ArmourVariable<Double> armourKbRes, List<ArmorAttributeEntry> additionalModifiers, boolean persistPrevious) implements PatchGenerator {
    public static final Codec<Double> POSITIVE_DOUBLE = doubleMiniumumExclusiveWithMessage(0, double_ -> "Value must be positive: " + double_);
    public static final MapCodec<ArmourStatsGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(ArmourVariable.codec(ExtraCodecs.POSITIVE_INT).optionalFieldOf("max_damage", ArmourVariable.empty()).forGetter(ArmourStatsGenerator::durability),
                            ArmourVariable.codec(ExtraCodecs.POSITIVE_INT).optionalFieldOf("armor", ArmourVariable.empty()).forGetter(ArmourStatsGenerator::protection),
                            ArmourVariable.codec(POSITIVE_DOUBLE).optionalFieldOf("armor_toughness", ArmourVariable.empty()).forGetter(ArmourStatsGenerator::armourToughness),
                            ArmourVariable.codec(POSITIVE_DOUBLE).optionalFieldOf("knockback_resistance", ArmourVariable.empty()).forGetter(ArmourStatsGenerator::armourKbRes),
                            ArmorAttributeEntry.CODEC.listOf().optionalFieldOf("additional_modifiers", Collections.emptyList()).forGetter(ArmourStatsGenerator::additionalModifiers),
                            Codec.BOOL.fieldOf("persist_previous").forGetter(ArmourStatsGenerator::persistPrevious))
        .apply(instance, ArmourStatsGenerator::new));
    @Override
    public void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap) {
        Integer maxDamage = durability.getValue(item);
        if (maxDamage != null) Defaulted.setDurability(maxDamage, patchedDataComponentMap);
        Integer armor = protection.getValue(item);
        Double toughness = armourToughness.getValue(item);
        Double kbRes = armourKbRes.getValue(item);
        if (armor == null && toughness == null && kbRes == null && additionalModifiers.isEmpty()) return;
        ItemAttributeModifiers oldModifiers = patchedDataComponentMap.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        //? <=1.21.1 {
        /*ItemAttributeModifiers defaultModifiers = item.getDefaultAttributeModifiers();
        if (oldModifiers == ItemAttributeModifiers.EMPTY && defaultModifiers != ItemAttributeModifiers.EMPTY) oldModifiers = defaultModifiers;
        *///?}
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        List<Pair<Holder<Attribute>, Identifier>> addedEntries = new ArrayList<>();
		Identifier identifier = Identifier.withDefaultNamespace("armor.any");
		EquipmentSlotGroup slotGroup;
        //? >1.21.1 {
		if (patchedDataComponentMap.has(DataComponents.EQUIPPABLE))
			slotGroup = EquipmentSlotGroup.bySlot(patchedDataComponentMap.get(DataComponents.EQUIPPABLE).slot());
        //?} <=1.21.1 {
        /*if (item instanceof Equipable equipable)
            slotGroup = EquipmentSlotGroup.bySlot(equipable.getEquipmentSlot());
        *///?}
        else
            slotGroup = EquipmentSlotGroup.ARMOR;
		identifier = switch (slotGroup) {
			case HEAD -> Identifier.withDefaultNamespace("armor.helmet");
			case CHEST -> Identifier.withDefaultNamespace("armor.chestplate");
			case LEGS -> Identifier.withDefaultNamespace("armor.leggings");
			case FEET -> Identifier.withDefaultNamespace("armor.boots");
			case BODY -> Identifier.withDefaultNamespace("armor.body");
			default -> identifier;
		};

		if (armor != null) {
            addedEntries.add(Pair.of(Attributes.ARMOR, identifier));
			builder.add(Attributes.ARMOR,
                new AttributeModifier(identifier, armor, AttributeModifier.Operation.ADD_VALUE),
                slotGroup);
		}
		if (toughness != null) {
            addedEntries.add(Pair.of(Attributes.ARMOR_TOUGHNESS, identifier));
			if (toughness > 0) builder.add(Attributes.ARMOR_TOUGHNESS,
				new AttributeModifier(identifier, toughness, AttributeModifier.Operation.ADD_VALUE),
				slotGroup);
		}
		if (kbRes != null) {
			addedEntries.add(Pair.of(Attributes.KNOCKBACK_RESISTANCE, identifier));
			if (kbRes > 0) builder.add(Attributes.KNOCKBACK_RESISTANCE,
				new AttributeModifier(identifier, kbRes, AttributeModifier.Operation.ADD_VALUE),
				slotGroup);
		}

        additionalModifiers.forEach(armorAttributeEntry -> armorAttributeEntry.addEntry(item, builder, addedEntries, slotGroup));
        
        if (persistPrevious && oldModifiers != ItemAttributeModifiers.EMPTY)
            for (ItemAttributeModifiers.Entry entry : oldModifiers.modifiers()) {
                innerloop: {
                    for (Pair<Holder<Attribute>, Identifier> attributeIdPair : addedEntries) if (entry.matches(attributeIdPair.getFirst(), attributeIdPair.getSecond())) break innerloop;
                    builder.add(entry.attribute(), entry.modifier(), entry.slot());
                }
            }

        patchedDataComponentMap.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }
    @Override
    public MapCodec<? extends PatchGenerator> codec() {
        return CODEC;
    }
    public static Codec<Double> doubleMiniumumExclusiveWithMessage(double min, Function<Double, String> function) {
        return Codec.DOUBLE
            .validate(
                double_ -> double_.compareTo(min) > 0 ? DataResult.success(double_) : DataResult.error(() -> (String)function.apply(double_))
            );
    }
    public record ArmorAttributeEntry(Holder<Attribute> attribute, Identifier baseId, ArmourVariable<Double> amount, AttributeModifier.Operation operation) {
        public static final Codec<ArmorAttributeEntry> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                Attribute.CODEC.fieldOf("type").forGetter(ArmorAttributeEntry::attribute),
                                Identifier.CODEC.fieldOf("base_id").forGetter(ArmorAttributeEntry::baseId),
                                ArmourVariable.codec(Codec.DOUBLE).fieldOf("amount").forGetter(ArmorAttributeEntry::amount),
                                AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(ArmorAttributeEntry::operation)
                        )
                        .apply(instance, ArmorAttributeEntry::new)
        );

        public void addEntry(Item item, ItemAttributeModifiers.Builder builder, List<Pair<Holder<Attribute>, Identifier>> addedEntries, EquipmentSlotGroup slotGroup) {
            Double value = amount.getValue(item);
            if (value == null) return;
            Identifier id = switch (slotGroup) {
                case HEAD -> baseId.withSuffix(".helmet");
                case CHEST -> baseId.withSuffix(".chestplate");
                case LEGS -> baseId.withSuffix(".leggings");
                case FEET -> baseId.withSuffix(".boots");
                case BODY -> baseId.withSuffix(".body");
                default -> baseId.withSuffix(".any");
            };
            addedEntries.add(Pair.of(attribute, id));
            builder.add(attribute,
                    new AttributeModifier(id, value, operation),
                    slotGroup);
        }
    }
}
