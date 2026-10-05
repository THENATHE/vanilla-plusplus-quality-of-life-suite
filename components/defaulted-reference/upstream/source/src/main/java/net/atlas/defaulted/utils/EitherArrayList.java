package net.atlas.defaulted.utils;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.mehvahdjukaar.codecui.Schema;
import net.mehvahdjukaar.codecui.SchemaCodec;
import net.mehvahdjukaar.codecui.SchemaCodecs;
import net.mehvahdjukaar.codecui.internal.SchemaResolver;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Special form of {@link ArrayList} used to contain {@link Either} instances, storing multiple data types into one list.
 * @see ArrayList
 * @see Either
 * @param <L> The left-side data type.
 * @param <R> The right-side data type.
 */
public class EitherArrayList<L, R> extends ArrayList<Either<L, R>> {
    public EitherArrayList(int initialCapacity) {
        super(initialCapacity);
    }

    public EitherArrayList() {
        super();
    }

    public EitherArrayList(Collection<? extends Either<L, R>> c) {
        super(c);
    }

    public static <L, R> EitherArrayList<L, R> fromStreamWithListOfRight(Stream<? extends Either<L, List<R>>> c) {
        EitherArrayList<L, R> result = new EitherArrayList<>();
        c.forEach(either -> {
            if (either.left().isPresent()) result.addLeft(either.left().get());
            if (either.right().isPresent()) result.addAll(either.right().get().stream().<Either<L, R>>map(Either::right).toList());
        });
        return result;
    }

    public static <L, R> Codec<EitherArrayList<L, R>> codec(Codec<L> leftCodec, Codec<R> rightCodec, boolean alwaysUseList) {
        return codec(null, null, leftCodec, rightCodec, alwaysUseList);
    }

    @SuppressWarnings("unused")
    public static <L, R> Codec<EitherArrayList<L, R>> codec(String lName, String rName, Codec<L> leftCodec, Codec<R> rightCodec, boolean alwaysUseList) {
        Codec<Either<L, R>> eitherCodec = Codec.either(leftCodec, rightCodec);
        Schema<L> leftSchema = SchemaResolver.get().resolve(leftCodec);
        Schema<R> rightSchema = SchemaResolver.get().resolve(rightCodec);
        Schema<List<Either<L, R>>> listSchema = new Schema.ListOf<>(Schema.anyOf(Schema.option(lName, leftSchema), Schema.option(rName, rightSchema)), 0, Integer.MAX_VALUE);

        //noinspection RedundantCast
        return alwaysUseList ? SchemaCodecs.xmap(SchemaCodec.of(eitherCodec.listOf(), listSchema), EitherArrayList::new, eithers -> (List<Either<L,R>>) eithers) : SchemaCodecs.xmap(SchemaCodec.of(Codecs.compactListCodec(eitherCodec), Schema.anyOf(Schema.option(lName, leftSchema),
                Schema.option(rName, rightSchema),
                Schema.option("list", listSchema))), EitherArrayList::new, eithers -> (List<Either<L,R>>) eithers);
    }

    public static <L, R> Codec<EitherArrayList<L, R>> codec(Codec<L> leftCodec, Codec<R> rightCodec, Function<List<Either<L, R>>, DataResult<List<Either<L, R>>>> validation, boolean alwaysUseList) {
        return codec(null, null, leftCodec, rightCodec, validation, alwaysUseList);
    }

    @SuppressWarnings("unused")
    public static <L, R> Codec<EitherArrayList<L, R>> codec(String lName, String rName, Codec<L> leftCodec, Codec<R> rightCodec, Function<List<Either<L, R>>, DataResult<List<Either<L, R>>>> validation, boolean alwaysUseList) {
        Codec<Either<L, R>> eitherCodec = Codec.either(leftCodec, rightCodec);
        Codec<List<Either<L, R>>> listCodec = eitherCodec.listOf().validate(validation);
        Schema<L> leftSchema = SchemaResolver.get().resolve(leftCodec);
        Schema<R> rightSchema = SchemaResolver.get().resolve(rightCodec);
        Schema<List<Either<L, R>>> listSchema = new Schema.ListOf<>(Schema.anyOf(Schema.option(lName, leftSchema), Schema.option(rName, rightSchema)), 0, Integer.MAX_VALUE);

        //noinspection RedundantCast
        return alwaysUseList ? SchemaCodecs.xmap(SchemaCodec.of(listCodec, listSchema), EitherArrayList::new, eithers -> (List<Either<L,R>>) eithers) : SchemaCodecs.xmap(SchemaCodec.of(Codecs.compactListCodec(eitherCodec, listCodec), Schema.anyOf(Schema.option(lName, leftSchema),
                Schema.option(rName, rightSchema),
                Schema.option("list", listSchema))), EitherArrayList::new, eithers -> (List<Either<L,R>>) eithers);
    }

    public Set<L> leftSide() {
        return stream().filter(lrEither -> lrEither.left().isPresent()).map(lrEither -> lrEither.left().get()).collect(Collectors.toSet());
    }

    public Set<R> rightSide() {
        return stream().filter(lrEither -> lrEither.right().isPresent()).map(lrEither -> lrEither.right().get()).collect(Collectors.toSet());
    }

    public L getL(int index) {
        return get(index).orThrow();
    }

    public R getR(int index) {
        return get(index).swap().orThrow();
    }

    public boolean addLeft(L obj) {
        return add(Either.left(obj));
    }

    public void addLeft(int index, L obj) {
        add(index, Either.left(obj));
    }

    public boolean addRight(R obj) {
        return add(Either.right(obj));
    }

    public void addRight(int index, R obj) {
        add(index, Either.right(obj));
    }

    /**
     * Maps this list into an unmodifiable collection of a different type.
     * @param leftMapper - The mapper from elements of type L to T.
     * @param rightMapper - The mapper from elements of type R to T.
     * @return A collection made up of the contents of this list, mapped onto type T using the mapper functions.
     * @param <T> The new type produced from the mapping.
     */
    public <T> List<T> map(Function<L, T> leftMapper, Function<R, T> rightMapper) {
        return stream().map(either -> either.map(leftMapper, rightMapper)).toList();
    }

    /**
     * Maps this list into an unmodifiable collection of a different type.
     * @param leftMapper - The mapper from elements of type List<L> to T.
     * @param rightMapper - The mapper from elements of type R to T.
     * @return A collection made up of the contents of this list, mapped onto type T using the mapper functions.
     * @param <T> The new type produced from the mapping.
     */
    public <T> List<T> mapLeftAsList(Function<List<L>, T> leftMapper, Function<R, T> rightMapper) {
        List<T> result = new ArrayList<>(stream().filter(lrEither -> lrEither.right().isPresent()).map(either -> either.right().map(rightMapper).get()).toList());
        result.add(leftMapper.apply(stream().filter(lrEither -> lrEither.left().isPresent()).map(either -> either.left().get()).toList()));
        return Collections.unmodifiableList(result);
    }

    /**
     * Maps this list into an unmodifiable collection of a different type.
     * @param leftMapper - The mapper from elements of type L to T.
     * @param rightMapper - The mapper from elements of type List<R> to T.
     * @return A collection made up of the contents of this list, mapped onto type T using the mapper functions.
     * @param <T> The new type produced from the mapping.
     */
    public <T> List<T> mapRightAsList(Function<L, T> leftMapper, Function<List<R>, T> rightMapper) {
        List<T> result = new ArrayList<>(stream().filter(lrEither -> lrEither.left().isPresent()).map(either -> either.left().map(leftMapper).get()).toList());
        result.add(rightMapper.apply(stream().filter(lrEither -> lrEither.right().isPresent()).map(either -> either.right().get()).toList()));
        return Collections.unmodifiableList(result);
    }
}