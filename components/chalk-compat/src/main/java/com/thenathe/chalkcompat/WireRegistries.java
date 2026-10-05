package com.thenathe.chalkcompat;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Per-connection dense IDs; Polymer-only items must not leave gaps in Fabric's registry map. */
public final class WireRegistries {
    private static final Set<Identifier> REGISTRIES = Set.of(Identifier.parse("minecraft:item"),
            Identifier.parse("minecraft:block"), Identifier.parse("minecraft:data_component_type"), Identifier.parse("minecraft:recipe_serializer"));
    // The combined shim performs final shared-registry compaction after all restorations.
    // Its codecs already own these IDs. Running two independent maps would translate twice.
    private static final boolean COMBINED_OWNER = combinedOwnsSharedRegistries();
    private static final Set<Identifier> COMBINED_REGISTRIES = Set.of(Identifier.parse("minecraft:item"),
            Identifier.parse("minecraft:data_component_type"), Identifier.parse("minecraft:recipe_serializer"));
    public static final Identifier STATES = Identifier.parse("chalk_polymer_compat:block_states");
    private static final PacketContext.Key<Map<Identifier, Ids>> KEY = PacketContext.key(
            Identifier.parse("chalk_polymer_compat:wire_ids"));
    private static final ScopedValue<Boolean> WRITE = ScopedValue.newInstance();
    private static final ScopedValue<Boolean> READ = ScopedValue.newInstance();

    private WireRegistries() {}

    private static boolean combinedOwnsSharedRegistries() {
        var owner = net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("sso_backpack_toolpouch_mapstitch_shim");
        if (owner.isEmpty()) return false;
        try {
            if (owner.get().getMetadata().getVersion().compareTo(net.fabricmc.loader.api.Version.parse("1.0.3+26.3")) < 0)
                throw new IllegalStateException("Chalk native coexistence requires Combined Polymer Shim 1.0.3+26.3 or newer");
            var api = Class.forName("com.thenathe.combinedshim.WireRegistries", false, WireRegistries.class.getClassLoader());
            api.getMethod("hasMapping", PacketContext.class);
            api.getMethod("prepare", Map.class, PacketContext.class);
            api.getMethod("toWire", Identifier.class, int.class, PacketContext.class);
            return true;
        } catch (ReflectiveOperationException | net.fabricmc.loader.api.VersionParsingException error) {
            throw new IllegalStateException("Combined Polymer Shim does not expose the supported final registry mapping API", error);
        }
    }


    public static void clear(PacketContext context) { context.set(KEY, null); }
    public static boolean handles(Identifier id) { return REGISTRIES.contains(id); }
    public static boolean writing() { return WRITE.orElse(false); }
    public static boolean reading() { return READ.orElse(false); }
    public static void encode(boolean tags, Runnable operation) {
        ScopedValue.where(WRITE, true).where(READ, tags).run(operation);
    }
    public static void decode(Runnable operation) {
        ScopedValue.where(WRITE, false).where(READ, true).run(operation);
    }

    public static Map<Identifier, Object2IntMap<Identifier>> prepare(
            Map<Identifier, Object2IntMap<Identifier>> original, PacketContext context) {
        var result = new LinkedHashMap<Identifier, Object2IntMap<Identifier>>();
        var mappings = new LinkedHashMap<Identifier, Ids>();
        original.forEach((key, entries) -> {
            Registry<?> registry = BuiltInRegistries.REGISTRY.getValue(key);
            if (!handles(key) || registry == null || (COMBINED_OWNER && COMBINED_REGISTRIES.contains(key))) {
                result.put(key, entries);
                return;
            }
            var wire = new Object2IntLinkedOpenHashMap<Identifier>();
            var ids = new Ids();
            entries.keySet().stream().sorted(Comparator.comparingInt(entries::getInt)).forEach(id -> {
                int next = wire.size();
                int raw = rawId(registry, id);
                wire.put(id, next);
                ids.out.put(raw, next);
                ids.in.put(next, raw);
            });
            result.put(key, wire);
            mappings.put(key, ids);
        });
        var blocks = result.get(Identifier.parse("minecraft:block"));
        if (blocks != null) {
            var states = new Ids();
            int[] nextState = {0};
            blocks.keySet().stream().sorted(Comparator.comparingInt(blocks::getInt)).forEach(id -> {
                for (var state : BuiltInRegistries.BLOCK.getValue(id).getStateDefinition().getPossibleStates()) {
                    int raw = net.minecraft.world.level.block.Block.BLOCK_STATE_REGISTRY.getId(state);
                    int next = nextState[0]++;
                    states.out.put(raw, next);
                    states.in.put(next, raw);
                }
            });
            mappings.put(STATES, states);
        }
        context.set(KEY, Map.copyOf(mappings));
        return result;
    }

    /** The client reports the final post-Fabric state order, including Polymer's own reorder. */
    public static void applyClientStates(java.util.List<NativeClients.BlockIds> blocks, PacketContext context) {
        var current = context.get(KEY);
        if (current == null) return; // Local integrated owner shares registries directly.
        var oldStates = current.get(STATES);
        if (oldStates == null) throw new IllegalStateException("Missing native block registry map");
        var translated = new Ids();
        var seen = new java.util.HashSet<Identifier>();
        for (var entry : blocks) {
            if (!seen.add(entry.block())) throw new IllegalArgumentException("Repeated block mapping");
            if (!BuiltInRegistries.BLOCK.containsKey(entry.block())) continue;
            var states = BuiltInRegistries.BLOCK.getValue(entry.block()).getStateDefinition().getPossibleStates();
            if (states.size() != entry.ids().length) throw new IllegalArgumentException("Different block state definition: " + entry.block());
            for (int i = 0; i < states.size(); i++) {
                int raw = net.minecraft.world.level.block.Block.BLOCK_STATE_REGISTRY.getId(states.get(i));
                if (!oldStates.out.containsKey(raw)) continue;
                int wire = entry.ids()[i];
                if (wire < 0 || translated.in.containsKey(wire)) throw new IllegalArgumentException("Invalid block state mapping");
                translated.out.put(raw, wire);
                translated.in.put(wire, raw);
            }
        }
        if (translated.out.size() != oldStates.out.size()) throw new IllegalArgumentException("Incomplete native block state mapping");
        var updated = new LinkedHashMap<>(current);
        updated.put(STATES, translated);
        context.set(KEY, Map.copyOf(updated));
    }

    private static <T> int rawId(Registry<T> registry, Identifier id) {
        return registry.getId(registry.getValue(id));
    }

    public static int toWire(Identifier registry, int raw, PacketContext context) {
        Ids ids = mapping(registry, context);
        return ids == null ? raw : ids.out.get(raw);
    }
    public static int toServer(Identifier registry, int wire, PacketContext context) {
        Ids ids = mapping(registry, context);
        return ids == null ? wire : ids.in.get(wire);
    }
    private static Ids mapping(Identifier registry, PacketContext context) {
        var map = context == null ? null : context.get(KEY);
        return map == null ? null : map.get(registry);
    }
    private static final class Ids {
        final Int2IntOpenHashMap out = new Int2IntOpenHashMap();
        final Int2IntOpenHashMap in = new Int2IntOpenHashMap();
        Ids() { out.defaultReturnValue(-1); in.defaultReturnValue(-1); }
    }
}
