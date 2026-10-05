package org.sharedregionmaps.qa;

import com.mojang.authlib.GameProfile;
import org.sharedregionmaps.SharedMaps;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/** Disposable dedicated-server QA; never packaged with the production mod. Run twice per world. */
public final class Qa implements ModInitializer {
    private static final Path MANIFEST = Path.of("sharedmaps-qa-persist.properties");
    private static final Path RESULT = Path.of("sharedmaps-qa-result.txt");
    private static final String BANNER_NAME = "Shared region QA banner";
    private MinecraftServer server;
    private ServerLevel level;
    private ServerPlayer player;
    private int checks;

    @Override public void onInitialize() {
        if (!Boolean.getBoolean("sharedmaps.qa")) {
            throw new IllegalStateException("QA requires -Dsharedmaps.qa=true and a disposable server world");
        }
        ServerLifecycleEvents.SERVER_STARTED.register(this::run);
    }

    private void run(MinecraftServer current) {
        server = current;
        level = current.overworld();
        String result;
        try {
            Files.deleteIfExists(RESULT);
            String corruptMode = System.getProperty("sharedmaps.qa.corrupt", "");
            String phase;
            if (!corruptMode.isEmpty()) {
                corruptIndex(corruptMode);
                phase = "corrupt-" + corruptMode;
            } else {
                player = player(level);
                boolean restarted = Files.exists(MANIFEST);
                persistentFixtures(restarted);
                alignedRegions();
                vanillaCreation();
                terrainExploration();
                zoomAndLocks();
                exclusions();
                damagedRecords();
                phase = restarted ? "restart" : "first";
            }
            result = "PASS " + checks + " checks; phase=" + phase
                    + "; mapstitch=" + FabricLoader.getInstance().isModLoaded("mapstitch");
            System.out.println("SHARED_MAPS_QA " + result);
        } catch (Throwable failure) {
            result = "FAIL " + failure;
            failure.printStackTrace();
        }
        try { Files.writeString(RESULT, result + "\n"); }
        catch (Exception failure) { throw new RuntimeException(failure); }
        finally { current.halt(false); }
    }

    private void corruptIndex(String mode) throws Exception {
        var accessor = (org.sharedregionmaps.mixin.SavedDataStorageAccessor) level.getDataStorage();
        Path path = accessor.sharedmaps$getDataFile(net.minecraft.resources.Identifier.fromNamespaceAndPath("shared_region_maps", "regions"));
        Files.createDirectories(path.getParent());
        if (mode.equals("bytes")) {
            Files.writeString(path, "Deliberately invalid compressed saved-data bytes for disposable QA");
        } else if (mode.equals("partial")) {
            var entries = new net.minecraft.nbt.ListTag();
            for (int scale : new int[] {0, 9}) {
                var region = new net.minecraft.nbt.CompoundTag();
                region.putString("dimension", "minecraft:overworld");
                region.putInt("center_x", 0);
                region.putInt("center_z", 0);
                region.putInt("scale", scale);
                var entry = new net.minecraft.nbt.CompoundTag();
                entry.put("region", region);
                entry.putInt("map_id", 900000 + scale);
                entries.add(entry);
            }
            var data = new net.minecraft.nbt.CompoundTag();
            data.put("maps", entries);
            var root = new net.minecraft.nbt.CompoundTag();
            root.put("data", data);
            root.putInt("DataVersion", net.minecraft.SharedConstants.getCurrentVersion().dataVersion().version());
            net.minecraft.nbt.NbtIo.writeCompressed(root, path);
        } else throw new IllegalArgumentException("Corruption mode must be bytes or partial");
        byte[] before = Files.readAllBytes(path);
        String hash = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(before));
        var evidence = new Properties();
        evidence.setProperty("path", path.toAbsolutePath().toString());
        evidence.setProperty("sha256", hash);
        try (var output = Files.newOutputStream(Path.of("sharedmaps-qa-corrupt.properties"))) {
            evidence.store(output, "Launcher must also verify unchanged after clean server shutdown");
        }
        ItemStack allocatorBefore = MapItem.create(level, 0, 0, (byte) 0, true, false);
        expectRefusal(() -> create(level, 0, 0, (byte) 0), "unreadable index must fail closed: " + mode);
        expectRefusal(() -> create(level, 4096, 4096, (byte) 4), "unreadable index must refuse uncached region: " + mode);
        check(java.util.Arrays.equals(before, Files.readAllBytes(path)), "corrupted registry is preserved byte for byte");
        ItemStack allocatorAfter = MapItem.create(level, 0, 0, (byte) 0, true, false);
        check(id(allocatorAfter).id() == id(allocatorBefore).id() + 1, "refused creations allocate no map IDs");
    }

    private void persistentFixtures(boolean restarted) throws Exception {
        Properties saved = new Properties();
        if (restarted) try (var input = Files.newInputStream(MANIFEST)) { saved.load(input); }
        Set<MapId> unique = new HashSet<>();
        BlockPos bannerPos = new BlockPos(4, 100, 8);
        if (!restarted) {
            level.setBlock(bannerPos.below(), Blocks.STONE.defaultBlockState(), 3);
            level.setBlock(bannerPos, Blocks.BANNER.white().defaultBlockState(), 3);
            var banner = (BannerBlockEntity) level.getBlockEntity(bannerPos);
            ItemStack named = new ItemStack(Blocks.BANNER.white());
            named.set(DataComponents.CUSTOM_NAME, Component.literal(BANNER_NAME));
            banner.applyComponentsFromItemStack(named);
            banner.setChanged();
        }
        for (byte scale = 0; scale <= 4; scale++) {
            ItemStack map = create(level, 0, 0, scale);
            MapItemSavedData data = data(map);
            check(unique.add(id(map)), "five scales use independent IDs, scale=" + scale);
            if (restarted) {
                check(id(map).id() == Integer.parseInt(saved.getProperty("id." + scale)),
                        "saved canonical ID reused after process restart, scale=" + scale);
                check(data.colors[12 * 128 + 11] == (byte) (91 + scale), "pixels survived restart, scale=" + scale);
                check(namedBanner(data), "named banner survived restart, scale=" + scale);
            } else {
                data.setColor(11, 12, (byte) (91 + scale));
                check(data.toggleBanner(level, bannerPos), "named banner registered, scale=" + scale);
                saved.setProperty("id." + scale, Integer.toString(id(map).id()));
            }
            ItemStack duplicate = create(level, 1, 1, scale);
            check(id(map).equals(id(duplicate)) && data(duplicate) == data,
                    "matching new item reuses saved record, scale=" + scale);
            check(data(duplicate).colors[12 * 128 + 11] == (byte) (91 + scale) && namedBanner(data(duplicate)),
                    "matching new item immediately inherits terrain and named banner, scale=" + scale);
            player.getInventory().setItem(0, duplicate);
            data.tickCarriedBy(player, duplicate, null);
            check(data.getUpdatePacket(id(duplicate), player) instanceof ClientboundMapItemDataPacket,
                    "shared record uses vanilla map update packets, scale=" + scale);
        }
        if (!restarted) try (var output = Files.newOutputStream(MANIFEST)) {
            saved.store(output, "Saved IDs to verify on a second independent dedicated-server launch");
        }
    }

    private void alignedRegions() {
        for (byte scale = 0; scale <= 4; scale++) {
            int width = 128 << scale;
            int[] coordinates = {-2 * width - 65, -width - 65, -width - 64, -65, -64, -1, 0, 63, 64,
                    width - 65, width - 64, width - 63, 2 * width - 64};
            for (int x : coordinates) for (int z : new int[] {-width - 65, -64, width - 64}) {
                var vanilla = MapItemSavedData.createFresh(x, z, scale, true, false, Level.OVERWORLD);
                ItemStack map = create(level, x, z, scale);
                var actual = data(map);
                check(actual.centerX == vanilla.centerX && actual.centerZ == vanilla.centerZ && actual.scale == scale,
                        "actual vanilla grid alignment at " + x + "," + z + ", scale=" + scale);
                check(id(map).equals(id(create(level, vanilla.centerX, vanilla.centerZ, scale))),
                        "cell center maps back to same ID, scale=" + scale);
            }
            for (int boundary : new int[] {-width - 64, -64, width - 64}) {
                check(!id(create(level, boundary - 1, 0, scale)).equals(id(create(level, boundary, 0, scale))),
                        "X boundary separates regions, scale=" + scale);
                check(!id(create(level, 0, boundary - 1, scale)).equals(id(create(level, 0, boundary, scale))),
                        "Z boundary separates regions, scale=" + scale);
            }
            Set<MapId> dimensions = new HashSet<>();
            for (var dimension : java.util.List.of(Level.OVERWORLD, Level.NETHER, Level.END)) {
                ServerLevel target = server.getLevel(dimension);
                check(target != null, "dimension present " + dimension);
                ItemStack map = create(target, 0, 0, scale);
                check(dimensions.add(id(map)), "dimension has independent ID at scale " + scale);
                check(MapItem.getSavedData(map, target).dimension.equals(dimension), "map keeps correct dimension");
                check(id(map).equals(id(create(target, 1, 1, scale))), "dimension-local duplicate reuses ID");
            }
        }
        // Probe allocator with two genuine independent maps around many shared lookups.
        ItemStack before = MapItem.create(level, 0, 0, (byte) 0, true, false);
        for (int i = 0; i < 50; i++) create(level, 0, 0, (byte) (i % 5));
        ItemStack after = MapItem.create(level, 0, 0, (byte) 0, true, false);
        check(id(after).id() == id(before).id() + 1, "shared lookups consume no extra map IDs");
    }

    private void vanillaCreation() {
        player.getInventory().clearContent();
        player.setPos(2, 100, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.MAP));
        var result = Items.MAP.use(level, player, InteractionHand.MAIN_HAND);
        check(result instanceof InteractionResult.Success, "vanilla empty-map use succeeds");
        ItemStack output = ((InteractionResult.Success) result).heldItemTransformedTo();
        check(output != null && output.is(Items.FILLED_MAP), "empty-map use returns filled map");
        check(id(output).equals(id(create(level, 2, 2, (byte) 0))), "vanilla empty-map path shares canonical ID");
        check(output.getComponentsPatch().split().added().keySet().stream().allMatch(type -> type == DataComponents.MAP_ID),
                "ordinary item has only vanilla MAP_ID customization");
    }

    private void zoomAndLocks() {
        for (byte sourceScale = 0; sourceScale < 4; sourceScale++) {
            ItemStack source = create(level, 0, 0, sourceScale);
            var sourceData = data(source);
            ItemStack destination = create(level, sourceData.centerX, sourceData.centerZ, (byte) (sourceScale + 1));
            var destinationData = data(destination);
            byte color = destinationData.colors[12 * 128 + 11];
            ItemStack processed = process(source, MapPostProcessing.SCALE);
            check(id(processed).equals(id(destination)) && data(processed) == destinationData,
                    "crafting postprocess reuses existing zoom record, scale=" + sourceScale);
            check(destinationData.colors[12 * 128 + 11] == color && namedBanner(destinationData),
                    "zoom keeps previously explored destination pixels and banner, scale=" + sourceScale);
            for (ContainerInput click : new ContainerInput[] {ContainerInput.PICKUP, ContainerInput.QUICK_MOVE}) {
                ItemStack zoomed = cartography(source, new ItemStack(Items.PAPER), click);
                check(id(zoomed).equals(id(destination)), "real cartography zoom " + click + ", scale=" + sourceScale);
                check(id(source).equals(id(create(level, 0, 0, sourceScale))) && data(source) == sourceData,
                        "zoom leaves source item and saved record intact");
            }
        }
        // New destination allocation is exercised separately from reusing a preexisting destination.
        ItemStack fresh = create(level, 100000, 120000, (byte) 0);
        for (byte scale = 1; scale <= 4; scale++) {
            var sourceData = data(fresh);
            ItemStack zoomed = cartography(fresh, new ItemStack(Items.PAPER), ContainerInput.PICKUP);
            check(data(zoomed).scale == scale && id(zoomed).equals(id(create(level, sourceData.centerX, sourceData.centerZ, scale))),
                    "first zoom allocates and enrolls destination, scale=" + scale);
            fresh = zoomed;
        }
        ItemStack source = create(level, 0, 0, (byte) 0);
        ItemStack locked = cartography(source, new ItemStack(Items.GLASS_PANE), ContainerInput.PICKUP);
        check(data(locked).locked && !id(locked).equals(id(source)), "cartography lock creates independent snapshot");
        check(namedBanner(data(locked)), "locked snapshot retains named banner");
        byte prior = data(source).colors[12 * 128 + 11];
        data(source).setColor(11, 12, (byte) 22);
        check(data(locked).colors[12 * 128 + 11] == prior, "locked snapshot unaffected by later shared exploration");
        data(source).setColor(11, 12, prior);
        check(!id(process(source, MapPostProcessing.LOCK)).equals(id(locked)), "each lock is its own snapshot");
        check(!data(create(level, 0, 0, (byte) 0)).locked, "locking never locks canonical shared record");
    }

    private void terrainExploration() {
        ItemStack map = create(level, 256, 0, (byte) 0);
        var record = data(map);
        // Reset only this disposable fixture so both launches prove a new terrain update.
        java.util.Arrays.fill(record.colors, (byte) 0);
        record.setDirty();
        ItemStack duplicate = create(level, 257, 1, (byte) 0);
        player.setPos(256, 100, 0);
        player.getInventory().setItem(0, map);
        for (int x = 15; x <= 16; x++) for (int z = -1; z <= 0; z++) level.getChunk(x, z);
        record.tickCarriedBy(player, map, null);
        for (int pass = 0; pass < 16; pass++) ((MapItem) Items.FILLED_MAP).update(level, player, record);
        boolean explored = false;
        for (byte pixel : record.colors) if (pixel != 0) { explored = true; break; }
        check(explored, "actual MapItem.update samples terrain into shared record");
        check(data(duplicate) == record && java.util.Arrays.equals(record.colors, data(duplicate).colors),
                "second physical item immediately sees actual terrain exploration");
        player.setPos(0, 100, 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void damagedRecords() throws Exception {
        ItemStack map = create(level, 0, 0, (byte) 0);
        MapId originalId = id(map);
        var original = data(map);
        try {
            level.setMapData(originalId, original.locked());
            expectRefusal(() -> create(level, 0, 0, (byte) 0), "externally locked canonical record is refused");
            check(level.getMapData(originalId).locked, "refusal does not overwrite changed record");
        } finally { level.setMapData(originalId, original); }
        check(id(create(level, 0, 0, (byte) 0)).equals(originalId), "association survives restoring changed record");
        var storage = server.getDataStorage();
        var field = storage.getClass().getDeclaredField("cache");
        field.setAccessible(true);
        java.util.Map cache = (java.util.Map) field.get(storage);
        var key = MapItemSavedData.type(originalId);
        Object cached = cache.put(key, Optional.empty());
        try {
            expectRefusal(() -> create(level, 0, 0, (byte) 0), "missing canonical map record is refused");
            check(level.getMapData(originalId) == null, "refusal never replaces missing map with blank data");
        } finally { cache.put(key, cached); }
        check(id(create(level, 0, 0, (byte) 0)).equals(originalId) && data(map) == original,
                "permanent ID and original record survive missing-record recovery");
    }

    private void expectRefusal(Runnable action, String description) {
        boolean refused = false;
        try { action.run(); } catch (IllegalStateException expected) { refused = true; }
        check(refused, description);
    }

    private void exclusions() {
        ItemStack canonical = create(level, 0, 0, (byte) 0);
        for (boolean[] flags : new boolean[][] {{true, false}, {false, false}, {true, true}, {false, true}}) {
            ItemStack external = MapItem.create(level, 0, 0, (byte) 0, flags[0], flags[1]);
            check(!id(external).equals(id(canonical)), "direct MapItem.create remains independent, flags=" + flags[0] + "/" + flags[1]);
            data(external).setColor(11, 12, (byte) 37);
            ItemStack scaled = process(external, MapPostProcessing.SCALE);
            check(!id(scaled).equals(id(create(level, 0, 0, (byte) 1))), "unenrolled legacy/custom zoom remains independent");
            check(data(external).colors[12 * 128 + 11] == 37, "legacy/custom source pixels preserved");
        }
        ItemStack custom = canonical.copy();
        CustomData.update(DataComponents.CUSTOM_DATA, custom, tag -> tag.putString("custom_renderer", "qa"));
        check(!id(process(custom, MapPostProcessing.SCALE)).equals(id(create(level, 0, 0, (byte) 1))),
                "custom-data source bypasses shared zoom");
        ItemStack explorer = canonical.copy();
        MapItemSavedData.addTargetDecoration(explorer, new BlockPos(4, 100, 8), "qa_explorer", MapDecorationTypes.WOODLAND_MANSION);
        check(!id(process(explorer, MapPostProcessing.SCALE)).equals(id(create(level, 0, 0, (byte) 1))),
                "target-decorated source bypasses shared zoom");
        check(data(canonical).colors[12 * 128 + 11] == 91 && namedBanner(data(canonical)),
                "special/legacy handling preserves canonical terrain and banner");
    }

    private ItemStack cartography(ItemStack input, ItemStack material, ContainerInput click) {
        player.getInventory().clearContent();
        var menu = new CartographyTableMenu(12, player.getInventory(), ContainerLevelAccess.create(level, new BlockPos(0, 100, 0)));
        player.containerMenu = menu;
        menu.getSlot(0).set(input.copy());
        menu.getSlot(1).set(material);
        check(!menu.getSlot(2).getItem().isEmpty(), "cartography preview available");
        menu.clicked(2, 0, click, player);
        ItemStack output = menu.getCarried();
        if (click == ContainerInput.QUICK_MOVE) {
            for (int slot = 0; slot < 36; slot++) {
                if (player.getInventory().getItem(slot).is(Items.FILLED_MAP)) { output = player.getInventory().getItem(slot); break; }
            }
        }
        check(output.is(Items.FILLED_MAP), "cartography transfers actual filled map");
        check(menu.getSlot(0).getItem().isEmpty() && menu.getSlot(1).getItem().isEmpty(), "cartography consumes ingredients once");
        return output.copy();
    }

    private ItemStack process(ItemStack input, MapPostProcessing action) {
        ItemStack result = input.copy();
        result.set(DataComponents.MAP_POST_PROCESSING, action);
        Items.FILLED_MAP.onCraftedPostProcess(result, level);
        return result;
    }

    private static ItemStack create(ServerLevel target, int x, int z, byte scale) {
        return SharedMaps.create(target, x, z, scale, true, false);
    }
    private static MapId id(ItemStack stack) { return java.util.Objects.requireNonNull(stack.get(DataComponents.MAP_ID)); }
    private MapItemSavedData data(ItemStack stack) { return java.util.Objects.requireNonNull(MapItem.getSavedData(stack, level)); }
    private static boolean namedBanner(MapItemSavedData map) {
        return map.getBanners().stream().anyMatch(banner -> banner.name().map(Component::getString).orElse("").equals(BANNER_NAME));
    }
    private void check(boolean condition, String description) {
        checks++;
        if (!condition) throw new AssertionError(description);
    }
    private static ServerPlayer player(ServerLevel level) {
        var profile = new GameProfile(UUID.randomUUID(), "SharedMapsQA");
        var player = new ServerPlayer(level.getServer(), level, profile, ClientInformation.createDefault());
        player.connection = new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND),
                player, CommonListenerCookie.createInitial(profile, false)) {
            @Override public void send(Packet<?> packet) {}
        };
        return player;
    }
}
