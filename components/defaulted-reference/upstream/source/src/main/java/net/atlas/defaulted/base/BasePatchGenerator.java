package net.atlas.defaulted.base;

import com.mojang.serialization.MapCodec;

public interface BasePatchGenerator<T extends BasePatchGenerator<T>> {
    MapCodec<? extends T> codec();
}
