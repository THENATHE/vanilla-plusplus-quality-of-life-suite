package qa;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonObject;
import java.nio.file.*;
import java.util.UUID;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.*;
import net.minecraft.server.network.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;

public final class StackablesQa implements ModInitializer {
    private int assertions;
    private void check(boolean value, String label) {
        if (!value) throw new AssertionError(label);
        assertions++;
    }
    private long total(AbstractContainerMenu menu) {
        long total = menu.getCarried().getCount();
        for (var slot : menu.slots) total += slot.getItem().getCount();
        return total;
    }
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            JsonObject result = new JsonObject();
            try {
                boolean uncap = Boolean.getBoolean("stackables.qa.uncapped");
                int size = uncap ? 2048 : 64;
                check(me.pajic.sensible_stackables.SensibleStackables.CONFIG.uncapStackSize.get() == uncap, "startup uncapping config");
                check(new ItemStack(Items.STONE).getMaxStackSize() == size, "common stack prototype");
                check(new ItemStack(Items.POTION).getMaxStackSize() == 3, "potion default");
                check(new ItemStack(Items.SADDLE).getMaxStackSize() == 16, "saddle default");
                check(new ItemStack(Items.ENCHANTED_BOOK).getMaxStackSize() == 64, "book default");
                result.addProperty("effective_override_registry", checkOverrideRegistry(server, uncap));
                var level = server.overworld();
                var player = new ServerPlayer(server, level, new GameProfile(UUID.randomUUID(), "StackablesQA"), ClientInformation.createDefault());
                player.connection = new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.SERVERBOUND), player, CommonListenerCookie.createInitial(player.getGameProfile(), false));
                player.experienceLevel = 100;
                player.setPos(0, -59, 0);
                var menu = ChestMenu.threeRows(1, player.getInventory());
                menu.getSlot(0).set(new ItemStack(Items.STONE, size));
                menu.clicked(0, 1, ContainerInput.PICKUP, player);
                check(menu.getCarried().getCount() == size / 2 && total(menu) == size, "right click half conserves");
                menu.clicked(1, 1, ContainerInput.PICKUP, player);
                check(menu.getSlot(1).getItem().getCount() == 1 && total(menu) == size, "place one conserves");
                menu.clicked(2, 0, ContainerInput.PICKUP, player);
                check(menu.getCarried().isEmpty() && total(menu) == size, "place remainder conserves");
                menu.clicked(0, 0, ContainerInput.QUICK_MOVE, player);
                check(menu.getSlot(0).getItem().isEmpty() && total(menu) == size, "shift move conserves");
                menu.clicked(2, 0, ContainerInput.PICKUP, player);
                menu.clicked(-999, 0, ContainerInput.QUICK_CRAFT, player);
                menu.clicked(3, 1, ContainerInput.QUICK_CRAFT, player);
                menu.clicked(4, 1, ContainerInput.QUICK_CRAFT, player);
                menu.clicked(-999, 2, ContainerInput.QUICK_CRAFT, player);
                check(total(menu) == size, "drag distribution conserves");
                menu.clicked(-1, 0, ContainerInput.PICKUP, player);
                check(total(menu) == size, "invalid slot cannot lose quantities");
                var enchantments = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                var left = new ItemStack(Items.ENCHANTED_BOOK, 3);
                var right = new ItemStack(Items.ENCHANTED_BOOK, 4);
                left.enchant(enchantments.getOrThrow(Enchantments.SHARPNESS), 1);
                right.enchant(enchantments.getOrThrow(Enchantments.SHARPNESS), 1);
                var anvilPos = new BlockPos(2, -60, 0);
                level.setBlock(anvilPos, Blocks.ANVIL.defaultBlockState(), 3);
                var anvil = new AnvilMenu(2, player.getInventory(), ContainerLevelAccess.create(level, anvilPos));
                anvil.getSlot(0).set(left); anvil.getSlot(1).set(right); anvil.createResult();
                check(anvil.getSlot(2).getItem().getCount() == 1, "anvil combines only one book");
                var taken = anvil.getSlot(2).remove(1); anvil.getSlot(2).onTake(player, taken);
                check(anvil.getSlot(0).getItem().getCount() == 2 && anvil.getSlot(1).getItem().getCount() == 3, "anvil retains both book remainders");
                var named = new ItemStack(Items.STONE, size);
                named.set(DataComponents.CUSTOM_NAME, Component.literal("Stackables saved quantity"));
                if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("polymer-core")) {
                    var transformed = PolymerChecks.transform(named, server);
                    check(transformed.getCount() == size && transformed.getMaxStackSize() == Math.min(99, size),
                            "ordinary vanilla item enters fallback transform with safe max and exact count");
                    check(named.getCount() == size && named.getMaxStackSize() == size,
                            "fallback transform does not mutate authoritative stack");
                }
                var ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
                var encoded = ItemStack.CODEC.encodeStart(ops, named).getOrThrow();
                var decoded = ItemStack.CODEC.parse(ops, encoded).getOrThrow();
                check(decoded.getCount() == size && decoded.getHoverName().getString().equals("Stackables saved quantity"), "persistent codec preserves count and name");
                for (int count : new int[]{size, 99, 100, 32768, Integer.MAX_VALUE}) {
                    var exact = new ItemStack(Items.STONE, count);
                    exact.set(DataComponents.MAX_STACK_SIZE, Integer.MAX_VALUE);
                    var buffer = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), server.registryAccess());
                    try {
                        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, exact);
                        var wire = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
                        check(wire.getCount() == count && wire.getMaxStackSize() == Integer.MAX_VALUE, "wire boundary " + count);
                    } finally { buffer.release(); }
                }
                if (!Files.exists(Path.of("stackables-phase-one.txt"))) {
                level.getChunkAt(new BlockPos(8, 81, 8));
                var area = new AABB(5,78,5,12,85,12);
                long before = level.getEntitiesOfClass(ItemEntity.class, area).stream().mapToLong(e -> e.getItem().getCount()).sum();
                Containers.dropItemStack(level, 8, 81, 8, new ItemStack(Items.STONE, size));
                long after = level.getEntitiesOfClass(ItemEntity.class, area).stream().mapToLong(e -> e.getItem().getCount()).sum();
                check(after - before == size, "container drops conserve all items before=" + before + " after=" + after);
                }
                var pos = new BlockPos(0, -60, 0);
                level.getChunkAt(pos);
                boolean restart = Files.exists(Path.of("stackables-phase-one.txt"));
                if (restart) {
                    var chest = (ChestBlockEntity) level.getBlockEntity(pos);
                    check(chest != null && chest.getItem(0).getCount() == size, "restart preserves actual chest quantity");
                    check(chest.getItem(0).getHoverName().getString().equals("Stackables saved quantity"), "restart preserves item name");
                } else {
                    level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
                    var chest = (ChestBlockEntity) level.getBlockEntity(pos);
                    chest.setItem(0, named); chest.setChanged();
                    Files.writeString(Path.of("stackables-phase-one.txt"), "restart pending\n");
                }
                result.addProperty("passed", true);result.addProperty("uncapped", uncap);
                result.addProperty("common_stack_size", size);result.addProperty("restart", restart);result.addProperty("assertions", assertions);
            } catch (Throwable failure) {
                failure.printStackTrace();result.addProperty("passed", false);result.addProperty("error",failure.toString());
            } finally {
                try { Files.writeString(Path.of("stackables-result.json"), result.toString()); } catch (Exception failure) {throw new RuntimeException(failure);}
                server.halt(false);
            }
        });
    }
    /** Exercise the public developer release while keeping the historical baseline compilable. */
    private boolean checkOverrideRegistry(net.minecraft.server.MinecraftServer server, boolean uncap) throws Exception {
        Class<?> overrides;
        try { overrides = Class.forName("me.pajic.sensible_stackables.handler.StackSizeOverrides"); }
        catch (ClassNotFoundException historicalBaseline) { return false; }
        var cfg = me.pajic.sensible_stackables.SensibleStackables.CONFIG;
        var savedItems = new java.util.HashMap<>(cfg.items.get());
        int savedCommon = cfg.commonStackSize.get();
        var update = me.pajic.sensible_stackables.SensibleStackables.class.getMethod("onUpdateConfig", net.minecraft.server.MinecraftServer.class);
        var snapshot = overrides.getMethod("snapshot");
        int fixtureCount = uncap ? 1243 : 73;
        var existing = new ItemStack(Items.STONE, fixtureCount);
        existing.set(DataComponents.CUSTOM_NAME, Component.literal("Existing override fixture"));
        boolean polymer = net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("polymer-core");
        try {
            @SuppressWarnings("unchecked") var cushions = (java.util.List<Item>) Items.class.getField("CUSHION").get(null)
                    .getClass().getMethod("asList").invoke(Items.class.getField("CUSHION").get(null));
            check(cushions.size() == 16, "all cushion colors available");
            for (var cushion : cushions) {
                check(cushion.components().getOrDefault(DataComponents.MAX_STACK_SIZE, 1) == 16,
                        "cushion prototype remains vanilla");
                check(new ItemStack(cushion).getMaxStackSize() == 64, "cushion configured default is 64");
            }
            check(Items.STONE.components().getOrDefault(DataComponents.MAX_STACK_SIZE, 1) == 64,
                    "registry overrides leave item prototype unchanged");
            checkPartialNativePayload(server, uncap);
            if (polymer) checkEmptyStackProjection(server);
            int stoneLimit = uncap ? 512 : 84;
            cfg.commonStackSize.validateAndSet(23);
            cfg.items.validateAndSet(java.util.Map.of("minecraft:stone", stoneLimit,
                    "#minecraft:planks", 61, "minecraft:oak_planks", 45));
            update.invoke(null, server);
            check(existing.getMaxStackSize() == stoneLimit && existing.getCount() == fixtureCount,
                    "hot update changes preexisting stack limit without changing quantity");
            check(new ItemStack(Items.STONE).getMaxStackSize() == stoneLimit, "new stacks use live override");
            check(new ItemStack(Items.DIRT).getMaxStackSize() == 23, "common override applies");
            check(new ItemStack(Items.BIRCH_PLANKS).getMaxStackSize() == 61, "tag overrides common size");
            check(new ItemStack(Items.OAK_PLANKS).getMaxStackSize() == 45, "explicit item overrides tag size");
            var explicit = existing.copy();
            explicit.set(DataComponents.MAX_STACK_SIZE, 7);
            check(explicit.getMaxStackSize() == 7, "explicit per-stack component overrides live table");
            if (polymer) {
                var fallback = PolymerChecks.transform(existing, server);
                check(fallback.getCount() == fixtureCount && fallback.getMaxStackSize() == Math.min(stoneLimit, 99),
                        "fallback effective override keeps full count and safe metadata");
                var nativeStack = PolymerChecks.transform(existing, server, true);
                check(nativeStack.getCount() == fixtureCount && nativeStack.getMaxStackSize() == stoneLimit,
                        "native effective override keeps full limit and count");
                check(PolymerChecks.transform(explicit, server).getMaxStackSize() == 7,
                        "fallback preserves explicit per-stack override");
                check(existing.getMaxStackSize() == stoneLimit && existing.getCount() == fixtureCount
                                && existing.getHoverName().getString().equals("Existing override fixture"),
                        "wire projections preserve authoritative components and quantity");
            }
            cfg.commonStackSize.validateAndSet(64);
            cfg.items.validateAndSet(java.util.Map.of("minecraft:potion", 5));
            update.invoke(null, server);
            @SuppressWarnings("unchecked") var replacement = (java.util.Map<Item, Integer>) snapshot.invoke(null);
            check(replacement.size() == 1 && replacement.get(Items.POTION) == 5,
                    "replacement recompute removes stale common, tag and explicit entries");
            check(existing.getMaxStackSize() == 64 && new ItemStack(Items.BIRCH_PLANKS).getMaxStackSize() == 64,
                    "existing stacks restore vanilla after override removal");
            check(explicit.getMaxStackSize() == 7, "hot recompute preserves explicit per-stack override");
            if (polymer) {
                check(PolymerChecks.transform(new ItemStack(Items.POTION, 37), server).getMaxStackSize() == 5,
                        "fallback carries a replacement limit below 99");
                var restored = PolymerChecks.transform(existing, server);
                check(restored.getCount() == fixtureCount && restored.getMaxStackSize() == 64,
                        "fallback metadata restores after live override removal");
            }
            cfg.items.validateAndSet(java.util.Map.of());
            update.invoke(null, server);
            check(((java.util.Map<?, ?>) snapshot.invoke(null)).isEmpty(), "empty hot update clears entire override table");
            check(new ItemStack(Items.POTION).getMaxStackSize() == 1, "empty update restores vanilla potion limit");
            if (polymer) {
                var restoredPotion = PolymerChecks.transform(new ItemStack(Items.POTION, 37), server);
                check(restoredPotion.getCount() == 37 && restoredPotion.getMaxStackSize() == 1,
                        "fallback cleared potion metadata preserves all items");
            }
            return true;
        } finally {
            cfg.commonStackSize.validateAndSet(savedCommon);
            cfg.items.validateAndSet(savedItems);
            update.invoke(null, server);
            check(new ItemStack(Items.STONE).getMaxStackSize() == (uncap ? 2048 : 64),
                    "override fixture restores original runtime configuration");
        }
    }
    private void checkEmptyStackProjection(net.minecraft.server.MinecraftServer server) throws Exception {
        var singletonPatch = ItemStack.EMPTY.getComponentsPatch();
        var zeroCount = new ItemStack(Items.STONE, 0);
        var air = new ItemStack(Items.AIR, 1);
        var zeroPatch = zeroCount.getComponentsPatch();
        var airPatch = air.getComponentsPatch();
        for (boolean nativeClient : new boolean[]{false, true}) {
            for (var empty : new ItemStack[]{ItemStack.EMPTY, zeroCount, air}) {
                check(empty.isEmpty(), "empty fixture has no usable item");
                var transformed = PolymerChecks.transform(empty, server, nativeClient);
                check(transformed == empty && transformed.isEmpty(),
                        "Polymer preserves empty slot without constructing transformed air native=" + nativeClient);
            }
        }
        check(ItemStack.EMPTY.getComponentsPatch().equals(singletonPatch), "empty transformations never mutate shared EMPTY");
        check(zeroCount.getComponentsPatch().equals(zeroPatch) && air.getComponentsPatch().equals(airPatch),
                "empty transformations preserve zero-count and air component patches");
        var context = PolymerChecks.context(server, false);
        var buffer = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), server.registryAccess());
        try {
            // Exercise the same optional item codec used for empty inventory slots.
            net.fabricmc.fabric.api.networking.v1.context.PacketContext.runWithContext(
                    new net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider() {
                        @Override public net.fabricmc.fabric.api.networking.v1.context.PacketContext getPacketContext() {
                            return context;
                        }
                    }, () -> ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, ItemStack.EMPTY));
            check(ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer).isEmpty(), "empty slot optional wire codec remains empty");
        } finally { buffer.release(); }
    }
    private void checkPartialNativePayload(net.minecraft.server.MinecraftServer server, boolean uncap) throws Exception {
        var registry = net.minecraft.core.registries.BuiltInRegistries.ITEM;
        var hidden = registry.stream().filter(item -> registry.getKey(item).getNamespace()
                .equals("simple_smithing_overhaul")).findFirst().orElseThrow();
        int limit = uncap ? 2048 : 84;
        var payloadClass = Class.forName("me.pajic.sensible_stackables.handler.StackSizeSyncPayload");
        var payload = payloadClass.getConstructor(java.util.Map.class).newInstance(
                java.util.Map.of(Items.STONE, limit, Items.DIRT, 23, hidden, 32));
        var context = PolymerChecks.context(server, true);
        var projection = Class.forName("com.thenathe.stackablescompat.StackSizePayloadProjection")
                .getMethod("project", payloadClass, net.fabricmc.fabric.api.networking.v1.context.PacketContext.class);
        check(projection.invoke(null, payload, context) == payload,
                "full native table is retained without a partial wire registry");
        var known = new it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap<net.minecraft.resources.Identifier>();
        known.put(registry.getKey(Items.STONE), registry.getId(Items.STONE));
        known.put(registry.getKey(Items.DIRT), registry.getId(Items.DIRT));
        Class.forName("com.thenathe.combinedshim.WireRegistries")
                .getMethod("prepare", java.util.Map.class, net.fabricmc.fabric.api.networking.v1.context.PacketContext.class)
                .invoke(null, java.util.Map.of(Registries.ITEM.identifier(), known), context);
        var partial = projection.invoke(null, payload, context);
        @SuppressWarnings("unchecked") var retained = (java.util.Map<Item, Integer>) payloadClass.getMethod("sizes").invoke(partial);
        check(retained.size() == 2 && retained.get(Items.STONE) == limit && retained.get(Items.DIRT) == 23,
                "partial native table retains exact known vanilla limits");
        check(!retained.containsKey(hidden), "partial native table excludes unknown module item");
        check(((java.util.Map<?, ?>) payloadClass.getMethod("sizes").invoke(payload)).size() == 3,
                "partial projection never mutates original server override table");
    }
    private static final class PolymerChecks {
        static ItemStack transform(ItemStack stack, net.minecraft.server.MinecraftServer server) throws ReflectiveOperationException {
            return transform(stack, server, false);
        }
        static ItemStack transform(ItemStack stack, net.minecraft.server.MinecraftServer server, boolean nativeStackables) throws ReflectiveOperationException {
            return (ItemStack) Class.forName("eu.pb4.polymer.core.api.item.PolymerItemUtils")
                    .getMethod("getPolymerItemStack", ItemStack.class,
                            net.fabricmc.fabric.api.networking.v1.context.PacketContext.class,
                            net.minecraft.core.HolderLookup.Provider.class)
                    .invoke(null, stack, context(server, nativeStackables), server.registryAccess());
        }
        static net.fabricmc.fabric.api.networking.v1.context.PacketContext context(net.minecraft.server.MinecraftServer server,
                                                                                 boolean nativeStackables) throws ReflectiveOperationException {
            var connection = new Connection(PacketFlow.SERVERBOUND);
            var values = new java.util.IdentityHashMap<Object, Object>();
            if (nativeStackables) {
                var key = Class.forName("com.thenathe.suite.network.SuiteCapabilities").getDeclaredField("MODULES_KEY");
                key.setAccessible(true);
                values.put(key.get(null), java.util.Set.of("sensible_stackables"));
            }
            return new net.fabricmc.fabric.api.networking.v1.context.PacketContext() {
                @SuppressWarnings("unchecked") public <T> T get(ReadKey<T> key) {
                    if (key == REGISTRY_ACCESS) return (T) server.registryAccess();
                    if (key == SERVER_INSTANCE) return (T) server;
                    if (key == CONNECTION) return (T) connection;
                    return (T) values.get(key);
                }
                public <T> void set(Key<T> key, T value) { values.put(key, value); }
            };
        }
    }
}
