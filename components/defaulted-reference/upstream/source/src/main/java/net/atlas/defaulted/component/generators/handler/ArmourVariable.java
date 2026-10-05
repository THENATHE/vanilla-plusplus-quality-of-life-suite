package net.atlas.defaulted.component.generators.handler;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
//? >1.21.1
import net.minecraft.core.component.DataComponents;
//? <=1.21.1
//import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

import java.util.Objects;
import java.util.Optional;

public record ArmourVariable<T>(Optional<T> any, Optional<T> helmet, Optional<T> chestplate, Optional<T> leggings, Optional<T> boots, Optional<T> body) {
    public static <T> Codec<ArmourVariable<T>> codec(Codec<T> varCodec) {
        Codec<ArmourVariable<T>> simpleCodec = varCodec.xmap(ArmourVariable::create, armourVariable -> armourVariable.any().orElse(null));
        Codec<ArmourVariable<T>> fullCodec = RecordCodecBuilder.create(instance ->
            instance.group(varCodec.optionalFieldOf("any").forGetter(ArmourVariable::any),
                varCodec.optionalFieldOf("helmet").forGetter(ArmourVariable::helmet),
                varCodec.optionalFieldOf("chestplate").forGetter(ArmourVariable::chestplate),
                varCodec.optionalFieldOf("leggings").forGetter(ArmourVariable::leggings),
                varCodec.optionalFieldOf("boots").forGetter(ArmourVariable::boots),
                varCodec.optionalFieldOf("body").forGetter(ArmourVariable::body)).apply(instance, ArmourVariable::new));
        return Codec.withAlternative(fullCodec, simpleCodec);
    }
	public static final ArmourVariable<?> EMPTY = new ArmourVariable<>((Object) null, null, null, null, null, null);
	public ArmourVariable(T any, T helmet, T chestplate, T leggings, T boots, T body) {
		this(Optional.ofNullable(any), Optional.ofNullable(helmet), Optional.ofNullable(chestplate), Optional.ofNullable(leggings), Optional.ofNullable(boots), Optional.ofNullable(body));
	}
    @SuppressWarnings("unchecked")
    public static <T> ArmourVariable<T> empty() {
        return (ArmourVariable<T>) EMPTY;
    }
	public static <T> ArmourVariable<T> create(T any) {
		return new ArmourVariable<>(any, null, null, null, null, null);
	}
	public T getValue(Item item) {
		//? >1.21.1 {
		if (item.components().has(DataComponents.EQUIPPABLE)) {
			return switch (item.components().get(DataComponents.EQUIPPABLE).slot()) {
		//?} <=1.21.1 {
		/*if (item instanceof ArmorItem armorItem) {
			return switch (armorItem.getEquipmentSlot()) {
		*///?}
				case HEAD -> helmet.orElseGet(() -> any.orElse(null));
				case CHEST -> chestplate.orElseGet(() -> any.orElse(null));
				case LEGS -> leggings.orElseGet(() -> any.orElse(null));
				case FEET -> boots.orElseGet(() -> any.orElse(null));
				case BODY -> body.orElseGet(() -> any.orElse(null));
				default -> any.orElse(null);
			};
        }
		return any.orElse(null);
	}

	public boolean isEmpty() {
		return this.equals(EMPTY);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ArmourVariable that)) return false;
        return Objects.equals(any, that.any) && Objects.equals(helmet, that.helmet) && Objects.equals(chestplate, that.chestplate) && Objects.equals(leggings, that.leggings) && Objects.equals(boots, that.boots) && Objects.equals(body, that.body);
	}

	@Override
	public int hashCode() {
		return Objects.hash(any, helmet, chestplate, leggings, boots, body);
	}

    public static Optional<Integer> max(Optional<Integer> value, int min) {
        return value.map(integer -> Math.max(integer, min));
    }
}