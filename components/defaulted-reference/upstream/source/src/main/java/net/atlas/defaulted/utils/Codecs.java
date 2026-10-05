package net.atlas.defaulted.utils;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
//? >1.21.1
import net.minecraft.util.ExtraCodecs;

import java.util.List;

public class Codecs {
    public static final Codec<Float> NON_NEGATIVE_FLOAT = /*? >1.21.1 {*/ ExtraCodecs.NON_NEGATIVE_FLOAT /*?} <=1.21.1 {*/ /*Codec.floatRange(0, Float.MAX_VALUE) *//*?}*/;
    public static <E> Codec<List<E>> compactListCodec(Codec<E> elementCodec) {
        return compactListCodec(elementCodec, elementCodec.listOf());
    }

    public static <E> Codec<List<E>> compactListCodec(Codec<E> elementCodec, Codec<List<E>> listCodec) {
        return Codec.either(listCodec, elementCodec).xmap((either) -> either.map((list) -> list, List::of), (list) -> list.size() == 1 ? Either.right(list.getFirst()) : Either.left(list));
    }
}