package qa;

import com.thenathe.multiscale.AtlasOptions;
import me.pajic.mapstitch.component.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.saveddata.maps.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Actual server AtlasItem.useOn calls; no production test hooks or fake banner records. */
public final class AtlasBannerQa {
    private int checks;
    private void check(boolean ok, String message) {
        checks++;
        if (!ok) throw new AssertionError("atlas banner: " + message);
    }
    private static MapItemSavedData data(ItemStack map, ServerLevel level) { return MapItem.getSavedData(map, level); }
    private static boolean marked(ItemStack map, ServerLevel level, BlockPos pos) {
        return data(map, level).getBanners().stream().anyMatch(b -> b.pos().equals(pos));
    }
    private static ItemStack atlas(List<ItemStack> maps, int mask) {
        var atlas = new ItemStack(me.pajic.mapstitch.item.ModItems.ATLAS);
        atlas.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(maps.stream().map(ItemStackTemplate::fromNonEmptyStack).toList()));
        atlas.set(ModDataComponents.ATLAS_SCALE, 4); // Minimap intentionally differs from generation.
        AtlasOptions.setGenerationMask(atlas, mask);
        return atlas;
    }
    private static InteractionResult click(ServerPlayer player, ItemStack item, BlockPos pos) {
        player.setItemInHand(InteractionHand.MAIN_HAND, item);
        return item.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)));
    }
    private static BannerBlockEntity banner(ServerLevel level, BlockPos pos, String name) {
        level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(pos, net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.withDefaultNamespace("blue_banner")).defaultBlockState(), 3);
        var banner = (BannerBlockEntity) level.getBlockEntity(pos);
        if (name != null) {
            var stack = blueBanner();
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
            banner.applyComponentsFromItemStack(stack);
        }
        return banner;
    }
    private static ItemStack blueBanner() {
        return new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.withDefaultNamespace("blue_banner")));
    }
    private static void fill(MapItemSavedData data, ServerLevel level) throws Exception {
        var method = MapItemSavedData.class.getDeclaredMethod("addDecoration", net.minecraft.core.Holder.class,
                net.minecraft.world.level.LevelAccessor.class, String.class, double.class, double.class, double.class, Component.class);
        method.setAccessible(true);
        // Vanilla tests count > 256, so 256 decorations still allow one more.
        for (int i = 0; !data.isTrackedCountOverLimit(256) && i <= 256; i++)
            method.invoke(data, MapDecorationTypes.BLUE_BANNER, level,
                    "qa-full-" + i, (double) data.centerX, (double) data.centerZ, 180D, null);
    }
    public static int run(MinecraftServer server, ServerPlayer player) throws Exception {
        return new AtlasBannerQa().runChecks(server, player);
    }
    private int runChecks(MinecraftServer server, ServerPlayer player) throws Exception {
        ServerLevel level = server.overworld();
        player.getInventory().clearContent();
        player.setPos(0, 65, 0);
        var pos = new BlockPos(0, 64, 0);
        var banner = banner(level, pos, "Blue base");
        var current = new ArrayList<ItemStack>();
        var foreign = new ArrayList<ItemStack>();
        var distant = new ArrayList<ItemStack>();
        for (byte scale = 0; scale <= 4; scale++) {
            current.add(MapItem.create(level, 0, 0, scale, true, false));
            distant.add(MapItem.create(level, 32768, 32768, scale, true, false));
            for (var dimension : List.of(Level.NETHER, Level.END))
                foreign.add(MapItem.create(server.getLevel(dimension), 0, 0, scale, true, false));
        }
        var entries = new ArrayList<>(current);
        entries.addAll(foreign); entries.addAll(distant);
        entries.add(current.getFirst().copy()); // Duplicate saved ID must toggle only once.
        entries.add(new ItemStack(Items.MAP, 3)); entries.add(new ItemStack(Items.PAPER, 2));
        var unknown = new ItemStack(Items.FILLED_MAP);
        unknown.set(DataComponents.MAP_ID, new MapId(Integer.MAX_VALUE)); entries.add(unknown);
        var book = atlas(entries, 21); // 1:1, 1:4 and 1:16, independently of M16.
        var snapshot = book.copy();
        var saved = current.stream().map(m -> data(m, level)).toList();
        check(click(player, book, pos) == InteractionResult.SUCCESS, "subset add succeeds through real AtlasItem.useOn");
        for (int i = 0; i < 5; i++) {
            check(marked(current.get(i), level, pos) == ((21 & (1 << i)) != 0), "only enabled layer receives banner " + i);
            check(saved.get(i) == data(current.get(i), level), "saved map object retained " + i);
        }
        check(ItemStack.isSameItemSameComponents(book, snapshot), "atlas IDs, blanks, paper, names and components unchanged");
        for (var map : foreign) check(!marked(map, level, pos), "foreign dimension untouched");
        for (var map : distant) check(!marked(map, level, pos), "distant map untouched");
        for (int i : new int[]{0, 2, 4}) {
            var marker = data(current.get(i), level).getBanners().iterator().next();
            check(marker.color() == DyeColor.BLUE && marker.name().orElseThrow().getString().equals("Blue base"), "live name and dye preserved " + i);
        }
        check(click(player, book, pos) == InteractionResult.SUCCESS, "coherent remove succeeds");
        check(current.stream().noneMatch(m -> marked(m, level, pos)), "all enabled markers removed, duplicate ID not re-added");
        data(current.getFirst(), level).toggleBanner(level, pos);
        check(click(player, book, pos) == InteractionResult.SUCCESS, "mixed-state click adds missing markers");
        for (int i : new int[]{0, 2, 4}) check(marked(current.get(i), level, pos), "existing marker retained, missing filled " + i);
        AtlasOptions.setGenerationMask(book, 31);
        check(click(player, book, pos) == InteractionResult.SUCCESS, "enable all fills absent layers");
        check(current.stream().allMatch(m -> marked(m, level, pos)), "all five enabled scales receive marker");
        AtlasOptions.setGenerationMask(book, 0);
        check(click(player, book, pos) == InteractionResult.FAIL, "all-off returns failure");
        check(current.stream().allMatch(m -> marked(m, level, pos)), "all-off preserves existing markers");
        AtlasOptions.setGenerationMask(book, 31);
        var renamed = blueBanner();
        renamed.set(DataComponents.CUSTOM_NAME, Component.literal("Renamed base"));
        banner.applyComponentsFromItemStack(renamed);
        check(click(player, book, pos) == InteractionResult.SUCCESS, "renaming updates all selected markers");
        for (var map : current) check(data(map, level).getBanners().iterator().next().name().orElseThrow().getString().equals("Renamed base"), "updated marker name");
        check(click(player, book, pos) == InteractionResult.SUCCESS, "next renamed click removes all");
        check(current.stream().noneMatch(m -> marked(m, level, pos)), "renamed markers coherently removed");

        var noCoverage = atlas(List.of(distant.getFirst(), foreign.getFirst(), unknown, new ItemStack(Items.MAP, 3)), 31);
        var before = noCoverage.copy();
        check(click(player, noCoverage, pos) == InteractionResult.FAIL, "no eligible existing coverage fails");
        check(ItemStack.isSameItemSameComponents(noCoverage, before), "no coverage creates no map or consumes blanks");
        var partiallyCovered = atlas(List.of(current.getFirst()), 31);
        check(click(player, partiallyCovered, pos) == InteractionResult.SUCCESS, "available enabled coverage works when other enabled layers absent");
        check(marked(current.getFirst(), level, pos), "absent scales skipped without creating maps");
        click(player, partiallyCovered, pos);

        var lockedMap = MapItem.create(level, 0, 0, (byte) 0, true, false);
        level.setMapData(lockedMap.get(DataComponents.MAP_ID), data(lockedMap, level).locked());
        var lockedBook = atlas(List.of(lockedMap), 1);
        check(click(player, lockedBook, pos) == InteractionResult.SUCCESS && marked(lockedMap, level, pos), "locked map retains vanilla marker support");
        check(click(player, lockedBook, pos) == InteractionResult.SUCCESS && !marked(lockedMap, level, pos), "locked marker removal works");

        var full = MapItem.create(level, 0, 0, (byte) 1, true, false);
        fill(data(full, level), level);
        check(data(full, level).isTrackedCountOverLimit(256), "actual tracked decorations fill vanilla limit");
        var fullBook = atlas(List.of(current.getFirst(), full), 3);
        check(click(player, fullBook, pos) == InteractionResult.FAIL, "full later layer blocks whole addition");
        check(!marked(current.getFirst(), level, pos) && !marked(full, level, pos), "full layer causes no partial addition");
        data(current.getFirst(), level).toggleBanner(level, pos);
        check(click(player, fullBook, pos) == InteractionResult.FAIL && marked(current.getFirst(), level, pos), "full layer preserves previously present marker");
        data(current.getFirst(), level).toggleBanner(level, pos);
        var atLimitWithMarker = MapItem.create(level, 0, 0, (byte) 1, true, false);
        data(atLimitWithMarker, level).toggleBanner(level, pos);
        fill(data(atLimitWithMarker, level), level);
        var removalBook = atlas(List.of(current.getFirst(), atLimitWithMarker), 3);
        check(click(player, removalBook, pos) == InteractionResult.SUCCESS, "full map already equal does not block adding missing other layer");
        check(click(player, removalBook, pos) == InteractionResult.SUCCESS, "full maps still allow coherent removal");
        check(!marked(current.getFirst(), level, pos) && !marked(atLimitWithMarker, level, pos), "full map marker removed");

        // A block on a map's covered border lies outside vanilla's ±63 marker pixels.
        var edgeMap = MapItem.create(level, 0, 0, (byte) 0, true, false);
        var edge = new BlockPos(data(edgeMap, level).centerX + 63, 64, data(edgeMap, level).centerZ);
        banner(level, edge, null);
        var wide = MapItem.create(level, edge.getX(), edge.getZ(), (byte) 4, true, false);
        var edgeBook = atlas(List.of(wide, edgeMap), 17);
        check(click(player, edgeBook, edge) == InteractionResult.FAIL, "invalid later edge layer blocks batch");
        check(!marked(wide, level, edge) && !marked(edgeMap, level, edge), "edge preflight prevents partial marker addition");

        var ordinary = MapItem.create(level, 0, 0, (byte) 0, true, false);
        check(click(player, ordinary, pos) == InteractionResult.SUCCESS && marked(ordinary, level, pos), "ordinary filled-map banner add unchanged");
        check(click(player, ordinary, pos) == InteractionResult.SUCCESS && !marked(ordinary, level, pos), "ordinary filled-map banner remove unchanged");
        check(click(player, book, pos.below()) == InteractionResult.PASS, "non-banner atlas use delegates upstream");

        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("bannerpoint")) {
            var config = me.pajic.bannerpoint.Bannerpoint.CONFIG;
            boolean named = config.transmitWhenNamed.get(), tied = config.transmitWhenTiedToMap.get();
            config.transmitWhenNamed.accept(false); config.transmitWhenTiedToMap.accept(true);
            var tiedPos = new BlockPos(1, 64, 1);
            var tiedBanner = banner(level, tiedPos, null);
            var first = MapItem.create(level, 0, 0, (byte) 0, true, false);
            var second = MapItem.create(level, 0, 0, (byte) 1, true, false);
            var tiedBook = atlas(List.of(first, second), 3);
            var waypoint = (net.minecraft.world.waypoints.WaypointTransmitter) tiedBanner;
            check(click(player, tiedBook, tiedPos) == InteractionResult.SUCCESS && waypoint.isTransmittingWaypoint(), "Bannerpoint tied-to-map tracking begins after batch add");
            AtlasOptions.setGenerationMask(tiedBook, 1);
            check(click(player, tiedBook, tiedPos) == InteractionResult.SUCCESS && !marked(first, level, tiedPos) && marked(second, level, tiedPos), "removal affects enabled scale only");
            check(waypoint.isTransmittingWaypoint(), "disabled-scale marker retains Bannerpoint tied-to-map tracking");
            AtlasOptions.setGenerationMask(tiedBook, 2);
            check(click(player, tiedBook, tiedPos) == InteractionResult.SUCCESS && !waypoint.isTransmittingWaypoint(), "last atlas marker removal stops tied-to-map tracking");
            config.transmitWhenNamed.accept(named); config.transmitWhenTiedToMap.accept(tied);
        }
        player.getInventory().clearContent();
        return checks;
    }
}
