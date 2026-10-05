package net.atlas.defaulted.init.registry;

import net.atlas.defaulted.init.DefaultedRegistries;
import net.minecraft.core.Registry;
//? fabric
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
//? neoforge {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
*///?}

import java.util.function.Supplier;

public abstract class Bootstrapper<T> {
    private final ResourceKey<Registry<T>> key;
    //? fabric
    private final String namespace;
    //? neoforge
    //private final DeferredRegister<T> deferredRegister;
    private final Registry<T> registry;

    public Bootstrapper(ResourceKey<Registry<T>> key, String namespace) {
        this.key = key;
        //? fabric
        this.namespace = namespace;
        //? neoforge
        //this.deferredRegister = DeferredRegister.create(this.key, namespace);
        this.registry = DefaultedRegistries.createRegistry(/*? fabric {*/ this.key /*?} neoforge {*/ /*this.deferredRegister *//*?}*/);
    }

    @SuppressWarnings("unused")
    public Bootstrapper(ResourceKey<Registry<T>> key, String namespace, Registry<T> registry) {
        this.key = key;
        //? fabric
        this.namespace = namespace;
        //? neoforge
        //this.deferredRegister = DeferredRegister.create(this.key, namespace);
        this.registry = registry;
    }
    public <A extends T> AbstractedHolder<T, A> register(String path, Supplier<A> value) {
        //? fabric {
        return new AbstractedHolder<>(Registry.registerForHolder(this.registry, ResourceKey.create(this.key, Identifier.fromNamespaceAndPath(this.namespace, path)), value.get()));
         //?} neoforge {
        /*return new AbstractedHolder<>(this.deferredRegister.register(path, value));
        *///?}
    }
    //? neoforge {
    /*public void init(IEventBus modBus) {
    *///?} fabric {
    public void init() {
    //?}
        bootstrap();
        //? neoforge
        //this.deferredRegister.register(modBus);
    }

    protected abstract void bootstrap();

    public ResourceKey<Registry<T>> getKey() {
        return key;
    }

    public Registry<T> getRegistry() {
        return registry;
    }
}
