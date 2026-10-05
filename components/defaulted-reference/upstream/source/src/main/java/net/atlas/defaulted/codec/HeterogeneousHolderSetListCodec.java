package net.atlas.defaulted.codec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.atlas.defaulted.utils.EitherArrayList;
import net.atlas.defaulted.utils.IDUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public class HeterogeneousHolderSetListCodec<E> implements Codec<List<HolderSet<E>>> {
	private final ResourceKey<? extends Registry<E>> registry;
	private final Codec<Holder<E>> elementCodec;
    private final Codec<EitherArrayList<TagKey<E>, Holder<E>>> innerListCodec;

	public static <E> Codec<List<HolderSet<E>>> create(
		final ResourceKey<? extends Registry<E>> registry, final Codec<Holder<E>> elementCodec, final boolean alwaysUseList
	) {
		return new HeterogeneousHolderSetListCodec<>(registry, elementCodec, alwaysUseList);
	}

	private HeterogeneousHolderSetListCodec(final ResourceKey<? extends Registry<E>> registry, final Codec<Holder<E>> elementCodec, final boolean alwaysUseList) {
		this.registry = registry;
		this.elementCodec = elementCodec;
		this.innerListCodec = EitherArrayList.codec("tag", "element", TagKey.hashedCodec(registry), elementCodec, alwaysUseList);
	}

	@Override
	public <T> DataResult<Pair<List<HolderSet<E>>, T>> decode(final DynamicOps<T> ops, final T input) {
		if (ops instanceof RegistryOps<T> registryOps) {
			Optional<HolderGetter<E>> registryOptional = registryOps.getter(this.registry);
			if (registryOptional.isPresent()) {
				HolderGetter<E> registry = registryOptional.get();
				return this.innerListCodec.decode(ops, input).flatMap(p -> {
					List<DataResult.Error<HolderSet<E>>> errors = new ArrayList<>();
					List<HolderSet<E>> holderSets = p.getFirst().mapRightAsList(tag -> lookupTag(registry, tag).mapOrElse(Function.identity(), error -> {
						errors.add(error);
						return HolderSet.empty();
					}), HolderSet::direct);
					DataResult<List<HolderSet<E>>> result = errors.isEmpty() ? DataResult.success(holderSets) : DataResult.error(() -> "Errors: [" + errors.stream().map(DataResult.Error::message).reduce((message1, message2) -> message1 + ", " + message2) + ']', holderSets);
					return result.map(holders -> Pair.of(holders, p.getSecond()));
				});
			}
		}

		return this.decodeWithoutRegistry(ops, input);
	}

	private static <E> DataResult<HolderSet<E>> lookupTag(final HolderGetter<E> registry, final TagKey<E> key) {
		return registry.get(key)
				.map(DataResult::success)
				.orElseGet(() -> DataResult.error(() -> "Missing tag: '" + key.location() + "' in '" + IDUtils.identifier(key.registry()) + "'"))
				.map(Function.identity());
	}

	public <T> DataResult<T> encode(final List<HolderSet<E>> input, final DynamicOps<T> ops, final T prefix) {
		if (ops instanceof RegistryOps<T> registryOps) {
			//? <26.3 {
			/*Optional<HolderOwner<E>> maybeOwner = registryOps.owner(this.registry);
			*///?} else {
			Optional<? extends HolderOwner<E>> maybeOwner = registryOps.getter(this.registry);
			//?}
			if (maybeOwner.isPresent()) {
				if (input.stream().anyMatch(holders -> !holders.canSerializeIn(maybeOwner.get()))) {
					return DataResult.error(() -> "HolderSet " + input + " is not valid in current registry set");
				}

				return this.innerListCodec.encode(EitherArrayList.fromStreamWithListOfRight(input.stream().map(holderSet -> holderSet.unwrap().mapRight(List::copyOf))), ops, prefix);
			}
		}

		return this.encodeWithoutRegistry(input, ops, prefix);
	}

	private <T> DataResult<Pair<List<HolderSet<E>>, T>> decodeWithoutRegistry(final DynamicOps<T> ops, final T input) {
		return this.elementCodec.listOf().decode(ops, input).flatMap(p -> {
			List<Holder.Direct<E>> directHolders = new ArrayList<>();

			for (Holder<E> holder : p.getFirst()) {
				if (!(holder instanceof Holder.Direct<E> direct)) {
					return DataResult.error(() -> "Can't decode element " + holder + " without registry");
				}

				directHolders.add(direct);
			}

			return DataResult.success(new Pair<>(List.of(HolderSet.direct(directHolders)), p.getSecond()));
		});
	}

	private <T> DataResult<T> encodeWithoutRegistry(final List<HolderSet<E>> input, final DynamicOps<T> ops, final T prefix) {
		Optional<List<Holder<E>>> toEncode = input.stream().filter(holders -> holders.unwrap().right().isPresent()).map(holders -> holders.unwrap().right().get()).findFirst();
		return toEncode.isPresent() ? this.elementCodec.listOf().encode(toEncode.get(), ops, prefix) : DataResult.error(() -> "Can't encode " + input + " without registries");
	}

	public Codec<EitherArrayList<TagKey<E>, Holder<E>>> getListCodec() {
		return this.innerListCodec;
	}
}
