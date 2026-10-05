package net.atlas.defaulted.base;

import net.minecraft.core.HolderSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BasePatchesBuilder<T, G extends BasePatchGenerator<G>, P extends BasePatches<T, G>, D> {
    protected final List<HolderSet<T>> elements;
    protected final List<G> generators = new ArrayList<>();
    protected int priority = 1000;
    protected D data;

    public BasePatchesBuilder(List<HolderSet<T>> elements) {
        this.elements = elements;
    }

    public abstract void writeData(String input, Object o);

    public BasePatchesBuilder<T, G, P, D> setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public BasePatchesBuilder<T, G, P, D> setData(D data) {
        this.data = data;
        return this;
    }

    public BasePatchesBuilder<T, G, P, D> addGenerator(G generator) {
        this.generators.add(generator);
        return this;
    }

    public BasePatchesBuilder<T, G, P, D> addGeneratorRaw(BasePatchGenerator<?> generator) {
        //noinspection unchecked
        addGenerator((G) generator);
        return this;
    }

    public abstract P build();

    @FunctionalInterface
    public interface Factory<T, B extends BasePatchesBuilder<T, ?, ?, ?>> {
        B create(List<HolderSet<T>> elements);
        default B create() {
            return create(Collections.emptyList());
        }
    }
}
