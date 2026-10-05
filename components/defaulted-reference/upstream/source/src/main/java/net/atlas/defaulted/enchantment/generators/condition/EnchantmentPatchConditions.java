package net.atlas.defaulted.enchantment.generators.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.enchantment.EnchantmentBuilder;
import net.atlas.defaulted.init.registry.Bootstrapper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;

import java.util.List;
import java.util.Objects;

public class EnchantmentPatchConditions extends Bootstrapper<MapCodec<? extends EnchantmentPatchConditions.EnchantmentPatchCondition>> {
    public static final EnchantmentPatchConditions INSTANCE = new EnchantmentPatchConditions();
    public static final MapCodec<EnchantmentPatchCondition> MAP_CODEC = INSTANCE.getRegistry().byNameCodec()
            .dispatchMap("condition", EnchantmentPatchCondition::codec, mapCodec -> mapCodec);

    public EnchantmentPatchConditions() {
        super(Defaulted.key("enchantment_patch_conditions"), "minecraft");
    }

    public void bootstrap() {
        register("invert", () -> InvertCondition.CODEC);
        register("condition_list", () -> ListCondition.CODEC);
        register("is_enchantment", () -> EnchantmentIsCondition.CODEC);
        register("in_tag", () -> EnchantmentHasTagCondition.CODEC);
        register("has_components", () -> ComponentsPresentCondition.CODEC);
        register("matches_components", () -> ExactComponentsCondition.CODEC);
    }
    
    public interface EnchantmentPatchCondition {
        boolean matches(Holder<Enchantment> enchantment, EnchantmentBuilder builder);
        MapCodec<? extends EnchantmentPatchCondition> codec();
    }
    public record InvertCondition(EnchantmentPatchCondition enchantmentPatchCondition) implements EnchantmentPatchCondition {
        public static final MapCodec<InvertCondition> CODEC = EnchantmentPatchConditions.MAP_CODEC.codec().fieldOf("inverted").xmap(InvertCondition::new, InvertCondition::enchantmentPatchCondition);
        @Override
        public boolean matches(Holder<Enchantment> enchantment, EnchantmentBuilder builder) {
            return !enchantmentPatchCondition.matches(enchantment, builder);
        }

        @Override
        public MapCodec<? extends EnchantmentPatchCondition> codec() {
            return CODEC;
        }
        
    }
    public record ListCondition(List<EnchantmentPatchCondition> conditions, boolean allMatch) implements EnchantmentPatchCondition {
        public static final MapCodec<ListCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(MAP_CODEC.codec().listOf().fieldOf("conditions").forGetter(ListCondition::conditions),
                Codec.BOOL.fieldOf("all_required").forGetter(ListCondition::allMatch)).apply(instance, ListCondition::new));
        @Override
        public boolean matches(Holder<Enchantment> enchantment, EnchantmentBuilder builder) {
            for (EnchantmentPatchCondition condition : conditions) {
                boolean result = condition.matches(enchantment, builder);
                if (result != allMatch) return result;
            }
            return allMatch;
        }
        @Override
        public MapCodec<? extends EnchantmentPatchCondition> codec() {
            return null;
        }
    }
    public record EnchantmentIsCondition(HolderSet<Enchantment> enchantments) implements EnchantmentPatchCondition {
        public static final MapCodec<EnchantmentIsCondition> CODEC = ExtraCodecs.nonEmptyHolderSet(RegistryCodecs.holderSet(Registries.ENCHANTMENT)).xmap(EnchantmentIsCondition::new, EnchantmentIsCondition::enchantments).fieldOf("enchantments");
        @SuppressWarnings("deprecation")
        @Override
        public boolean matches(Holder<Enchantment> enchantment, EnchantmentBuilder builder) {
            for (Holder<Enchantment> enchantmentHolder : enchantments) {
                if (enchantment.is(enchantmentHolder)) return true;
            }
            return false;
        }

        @Override
        public MapCodec<? extends EnchantmentPatchCondition> codec() {
            return CODEC;
        }
    }
    public record EnchantmentHasTagCondition(List<TagKey<Enchantment>> tags) implements EnchantmentPatchCondition {
        public static final MapCodec<EnchantmentHasTagCondition> CODEC = TagKey.codec(Registries.ENCHANTMENT).listOf().xmap(EnchantmentHasTagCondition::new, EnchantmentHasTagCondition::tags).fieldOf("tags");
        @Override
        public boolean matches(Holder<Enchantment> enchantment, EnchantmentBuilder builder) {
            for (TagKey<Enchantment> enchantmentTag : tags) {
                if (enchantment.is(enchantmentTag)) return true;
            }
            return false;
        }

        @Override
        public MapCodec<? extends EnchantmentPatchCondition> codec() {
            return CODEC;
        }
    }
    public record ComponentsPresentCondition(List<DataComponentType<?>> presentComponents, boolean allMatch) implements EnchantmentPatchCondition {
        public static final MapCodec<ComponentsPresentCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(EnchantmentEffectComponents.COMPONENT_CODEC.listOf().fieldOf("components").forGetter(ComponentsPresentCondition::presentComponents),
                Codec.BOOL.fieldOf("all_required").forGetter(ComponentsPresentCondition::allMatch)).apply(instance, ComponentsPresentCondition::new));
        @Override
        public boolean matches(Holder<Enchantment> enchantment, EnchantmentBuilder builder) {
            for (DataComponentType<?> dataComponentType : presentComponents) {
                boolean result = builder.has(dataComponentType);
                if (result != allMatch) return result;
            }
            return allMatch;
        }
        @Override
        public MapCodec<? extends EnchantmentPatchCondition> codec() {
            return CODEC;
        }
    }
    public record ExactComponentsCondition(DataComponentMap exactComponents, boolean allMatch) implements EnchantmentPatchCondition {
        public static final MapCodec<ExactComponentsCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(EnchantmentEffectComponents.CODEC.fieldOf("components").forGetter(ExactComponentsCondition::exactComponents),
                Codec.BOOL.fieldOf("all_required").forGetter(ExactComponentsCondition::allMatch)).apply(instance, ExactComponentsCondition::new));
        @Override
        public boolean matches(Holder<Enchantment> enchantment, EnchantmentBuilder builder) {
            for (TypedDataComponent<?> dataComponent : exactComponents) {
                TypedDataComponent<?> present = builder/*? <1.21.5 {*/ /*.getComponents() *//*?}*/.getTyped(dataComponent.type());
                boolean result = present == null ? false : Objects.equals(dataComponent.value(), present.value());
                if (result != allMatch) return result;
            }
            return allMatch;
        }
        @Override
        public MapCodec<? extends EnchantmentPatchCondition> codec() {
            return CODEC;
        }
    }
}
