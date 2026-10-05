package net.atlas.defaulted.component.generators;

import java.util.List;
import java.util.stream.IntStream;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public interface WeaponLevelBasedValue {
    Codec<WeaponLevelBasedValue> DISPATCH_CODEC = WeaponLevelBasedValues.INSTANCE.getRegistry().byNameCodec()
            .dispatch(WeaponLevelBasedValue::codec, mapCodec -> mapCodec);
    Codec<WeaponLevelBasedValue> CODEC = Codec.either(Codec.FLOAT.xmap(Constant::new, Constant::value), DISPATCH_CODEC)
            .xmap((either) -> either.map((left) -> left, (right) -> right), (weaponLevelBasedValue) -> weaponLevelBasedValue instanceof Constant constant ? Either.left(constant) : Either.right(weaponLevelBasedValue));
    Float getResult(int weaponLevel, float addedValue, boolean applyTier);
    default Float getResult(int weaponLevel, boolean applyTier) {
        return getResult(weaponLevel, 0, applyTier);
    }
    MapCodec<? extends WeaponLevelBasedValue> codec();
    interface LevelCondition {
        Codec<LevelCondition> DISPATCH_CODEC = LevelConditions.INSTANCE.getRegistry().byNameCodec()
                .dispatch(LevelCondition::codec, mapCodec -> mapCodec);
        Codec<LevelCondition> CODEC = Codec.either(Codec.INT.xmap(i -> new ClampedCondition(i, i), ClampedCondition::min), DISPATCH_CODEC)
                .xmap((either) -> either.map((left) -> left, (right) -> right), (levelCondition) -> levelCondition instanceof ClampedCondition clampedCondition ? Either.left(clampedCondition) : Either.right(levelCondition));
        boolean matches(int value);
        MapCodec<? extends LevelCondition> codec();
    }
    record ClampedCondition(int min, int max) implements LevelCondition {
        public static final MapCodec<ClampedCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(Codec.INT.fieldOf("min").forGetter(ClampedCondition::min),
                        Codec.INT.fieldOf("max").forGetter(ClampedCondition::max)).apply(instance, ClampedCondition::new));
        @Override
        public boolean matches(int value) {
            return value >= min && value <= max;
        }

        @Override
        public MapCodec<? extends LevelCondition> codec() {
            return CODEC;
        }
    }
    record ListCondition(IntStream values) implements LevelCondition {
        public static final MapCodec<ListCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(Codec.INT_STREAM.fieldOf("values").forGetter(ListCondition::values))
                        .apply(instance, ListCondition::new));
        @Override
        public boolean matches(int value) {
            return values.anyMatch(val -> val == value);
        }

        @Override
        public MapCodec<? extends LevelCondition> codec() {
            return CODEC;
        }
    }
    record Constant(float value, boolean appliesAdditional) implements WeaponLevelBasedValue {
        public static final MapCodec<Constant> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(Codec.FLOAT.fieldOf("value").forGetter(Constant::value),
                                Codec.BOOL.optionalFieldOf("applies_additional", true).forGetter(Constant::appliesAdditional))
                        .apply(instance, Constant::new));
        public Constant(float value) {
            this(value, true);
        }
        @Override
        public Float getResult(int weaponLevel, float addedValue, boolean applyTier) {
            return value + ((applyTier && appliesAdditional) ? addedValue : 0);
        }
        @Override
        public MapCodec<? extends WeaponLevelBasedValue> codec() {
            return CODEC;
        }
    }
    record Lookup(List<MatchingLevel> values, WeaponLevelBasedValue fallback) implements WeaponLevelBasedValue {
        public static final MapCodec<Lookup> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(MatchingLevel.CODEC.listOf().fieldOf("values").forGetter(Lookup::values),
                        WeaponLevelBasedValue.CODEC.fieldOf("fallback").forGetter(Lookup::fallback)).apply(instance, Lookup::new));
        @Override
        public Float getResult(int weaponLevel, float addedValue, boolean applyTier) {
            for (MatchingLevel value : values) {
                Float res = value.getResult(weaponLevel, addedValue, applyTier);
                if (res != null) return value.getResult(weaponLevel, addedValue, applyTier);
            }
            return fallback.getResult(weaponLevel, addedValue, applyTier);
        }
        @Override
        public MapCodec<? extends WeaponLevelBasedValue> codec() {
            return CODEC;
        }
    }
    record MatchingLevel(WeaponLevelBasedValue value, LevelCondition levelCondition) {
        public static final Codec<MatchingLevel> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(WeaponLevelBasedValue.CODEC.fieldOf("value").forGetter(MatchingLevel::value),
                        LevelCondition.CODEC.fieldOf("condition").forGetter(MatchingLevel::levelCondition)).apply(instance, MatchingLevel::new));
        public Float getResult(int weaponLevel, float addedValue, boolean applyTier) {
            return levelCondition.matches(weaponLevel) || !applyTier ? value.getResult(weaponLevel, addedValue, applyTier) : null;
        }
    }
    record Linear(WeaponLevelBasedValue base, WeaponLevelBasedValue perLevelAboveFirst) implements WeaponLevelBasedValue {
        public static final MapCodec<Linear> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(WeaponLevelBasedValue.CODEC.fieldOf("base").forGetter(Linear::base),
                        WeaponLevelBasedValue.CODEC.fieldOf("per_level_above_first").forGetter(Linear::perLevelAboveFirst)).apply(instance, Linear::new));

        @Override
        public Float getResult(int weaponLevel, float addedValue, boolean applyTier) {
            return base.getResult(weaponLevel, addedValue, applyTier) + weaponLevel * perLevelAboveFirst.getResult(weaponLevel, addedValue, applyTier);
        }

        @Override
        public MapCodec<? extends WeaponLevelBasedValue> codec() {
            return CODEC;
        }
    }
}