package net.atlas.defaulted.init.registry;

import com.google.common.base.Supplier;
import com.mojang.datafixers.util.Either;
import net.atlas.defaulted.utils.IDUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
//? >=26.1
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class AbstractedHolder<T, A extends T> implements /*? neoforge {*/ /*Holder<T>, *//*?}*/ Supplier<A> {
    protected final ResourceKey<T> key;
    private final Holder<T> delegate;

    protected AbstractedHolder(ResourceKey<T> key, Holder<T> delegate) {
        this.key = Objects.requireNonNull(key);
        this.delegate = delegate;
    }

    protected AbstractedHolder(Holder<T> delegate) {
        this(delegate.unwrapKey().orElse(null), delegate);
    }

    public @NonNull T value() {
        return this.delegate.value();
    }

    public A get() {
        return (A) this.value();
    }

    public Identifier getId() {
        return IDUtils.identifier(this.key);
    }

    public ResourceKey<T> getKey() {
        return this.key;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else {
            if (obj instanceof Holder<?> h)
                return h.kind() == Holder.Kind.REFERENCE && h.unwrapKey().equals(Optional.of(this.key));
            return false;
        }
    }

    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public String toString() {
        return "AbstractedHolder{" +
                "key=" + this.key +
                '}';
    }

    public boolean isBound() {
        return this.delegate.isBound();
    }

    //? >=26.1 {
    public boolean areComponentsBound() {
        return this.delegate.areComponentsBound();
    }
    //?}

    public boolean is(Identifier id) {
        return id.equals(IDUtils.identifier(this.key));
    }

    public boolean is(@NonNull ResourceKey<T> key) {
        return this.key.equals(key);
    }

    public boolean is(Predicate<ResourceKey<T>> filter) {
        return filter.test(this.key);
    }

    public boolean is(@NonNull TagKey<T> tag) {
        return this.delegate != null && this.delegate.is(tag);
    }

    @Deprecated
    public boolean is(@NonNull Holder<T> holder) {
        return this.delegate != null && this.delegate.is(holder);
    }

    public @NonNull Stream<TagKey<T>> tags() {
        return this.delegate != null ? this.delegate.tags() : Stream.empty();
    }

    //? >=26.1 {
    public DataComponentMap components() {
        return this.delegate.components();
    }
    //?}

    public @NonNull Either<ResourceKey<T>, T> unwrap() {
        return Either.left(this.key);
    }

    public @NonNull Optional<ResourceKey<T>> unwrapKey() {
        return Optional.of(this.key);
    }

    public Holder.@NonNull Kind kind() {
        return Holder.Kind.REFERENCE;
    }

    public boolean canSerializeIn(@NonNull HolderOwner<T> owner) {
        return this.delegate != null && this.delegate.canSerializeIn(owner);
    }

    //? neoforge {
    /*public @NonNull Holder<T> getDelegate() {
        return this.delegate.getDelegate();
    }
    *///?}
}
