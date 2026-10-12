import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.thenathe.suite.pets.SwiftFlight;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.Vec3;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

public final class SwiftAnvilProbe implements ModInitializer {
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            JsonObject result = new JsonObject();
            result.addProperty("passed", false);
            result.addProperty("polymer", FabricLoader.getInstance().isModLoaded("polymer-core"));
            try {
                var level = server.overworld();
                var pos = new BlockPos(0, 100, 0);
                level.getChunkAt(pos);
                level.setBlock(pos, Blocks.ANVIL.defaultBlockState(), 3);
                var profile = new GameProfile(UUID.fromString("a807b702-cea6-4bdb-9444-1efda43b24b5"), "SwiftAnvilProbe");
                var player = new ServerPlayer(server, level, profile, ClientInformation.createDefault());
                player.connection = new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.SERVERBOUND), player,
                    CommonListenerCookie.createInitial(profile, false)) {
                    @Override public void send(Packet<?> packet) {}
                };
                player.setGameMode(GameType.SURVIVAL);
                player.setPos(1, 100, 0);
                var enchantment = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, SwiftFlight.ID));
                var loot = server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,
                    Identifier.withDefaultNamespace("chests/end_city_treasure")));
                var params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN,
                    Vec3.atCenterOf(pos)).create(LootContextParamSets.CHEST);
                var books = new HashMap<Integer, ItemStack>();
                for (int seed = 1; seed <= 1000 && books.size() < 3; seed++) {
                    for (var stack : loot.getRandomItems(params, seed)) {
                        if (stack.is(Items.ENCHANTED_BOOK)) {
                            int rank = EnchantmentHelper.getEnchantmentsForCrafting(stack).getLevel(enchantment);
                            if (rank > 0) books.putIfAbsent(rank, stack.copy());
                        }
                    }
                }
                check(books.size() == 3, "real End City books cover levels I-II-III");
                for (int rank = 1; rank <= 3; rank++) {
                    for (boolean shift : new boolean[]{false, true}) {
                        apply(player, level, pos, enchantment, Items.HARNESS.blue(), books.get(rank), rank, shift,
                            "swift_" + rank, result);
                    }
                }
                var unbreaking = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING);
                var vanillaBook = new ItemStack(Items.ENCHANTED_BOOK);
                vanillaBook.enchant(unbreaking, 1);
                for (boolean shift : new boolean[]{false, true}) {
                    apply(player, level, pos, unbreaking, Items.DIAMOND_PICKAXE, vanillaBook, 1, shift,
                        "vanilla_unbreaking", result);
                }
                for (boolean shift : new boolean[]{false, true}) {
                    repairUnenchanted(player, level, pos, shift, result);
                }
                result.addProperty("passed", true);
            } catch (Throwable failure) {
                failure.printStackTrace();
                result.addProperty("error", failure.toString());
            }
            try {
                Files.writeString(Path.of("swift-anvil-result.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result));
            } catch (Exception failure) {
                throw new RuntimeException(failure);
            }
            server.halt(false);
        });
    }
    private static void apply(ServerPlayer player, net.minecraft.server.level.ServerLevel level, BlockPos pos,
        net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment,
        net.minecraft.world.item.Item item, ItemStack book, int rank, boolean shift, String label, JsonObject result) {
        player.getInventory().clearContent();
        player.giveExperienceLevels(100 - player.experienceLevel);
        int startingLevels = player.experienceLevel;
        var source = new ItemStack(item);
        source.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Original name"));
        var menu = new AnvilMenu(rank + (shift ? 10 : 20), player.getInventory(), ContainerLevelAccess.create(level, pos));
        player.containerMenu = menu;
        menu.getSlot(0).set(source);
        menu.getSlot(1).set(book.copy());
        menu.setItemName("Original name");
        menu.createResult();
        String route = label + (shift ? "_shift" : "_normal");
        var preview = menu.getSlot(2).getItem();
        int price = menu.getCost();
        check(!preview.isEmpty() && preview.is(item), "ordinary anvil creates result " + route);
        check(EnchantmentHelper.getItemEnchantmentLevel(enchantment, preview) == rank,
            "ordinary anvil retains book enchantment " + route);
        check(price > 0 && price <= 100, "survival preview XP cost valid " + route + " cost=" + price);
        check(menu.getSlot(2).mayPickup(player), "survival result pickup allowed " + route);
        menu.clicked(2, 0, shift ? net.minecraft.world.inventory.ContainerInput.QUICK_MOVE
            : net.minecraft.world.inventory.ContainerInput.PICKUP, player);
        check(menu.getSlot(0).getItem().isEmpty() && menu.getSlot(1).getItem().isEmpty(),
            "one original item and one book consumed " + route);
        var outputs = new java.util.ArrayList<ItemStack>(player.getInventory().getNonEquipmentItems().stream()
            .filter(stack -> stack.is(item)).toList());
        if (menu.getCarried().is(item)) outputs.add(menu.getCarried());
        check(outputs.size() == 1 && outputs.getFirst().getCount() == 1, "exactly one result returned " + route);
        check(EnchantmentHelper.getItemEnchantmentLevel(enchantment, outputs.getFirst()) == rank,
            "taken result retains enchantment " + route);
        check(outputs.getFirst().getHoverName().getString().equals("Original name"), "original name preserved " + route);
        int charged = startingLevels - player.experienceLevel;
        check(charged == price, "charged previewed XP exactly " + route);
        result.addProperty(route + "_preview_cost", price);
        result.addProperty(route + "_charged_levels", charged);
        System.out.println("SWIFT_ANVIL_COST " + route + " preview=" + price + " charged=" + charged);
        level.setBlock(pos, Blocks.ANVIL.defaultBlockState(), 3);
    }
    private static void repairUnenchanted(ServerPlayer player, net.minecraft.server.level.ServerLevel level,
        BlockPos pos, boolean shift, JsonObject result) {
        player.getInventory().clearContent();
        player.giveExperienceLevels(-player.experienceLevel);
        var tool = new ItemStack(Items.DIAMOND_PICKAXE);
        tool.setDamageValue(400);
        var menu = new AnvilMenu(40 + (shift ? 1 : 0), player.getInventory(), ContainerLevelAccess.create(level, pos));
        player.containerMenu = menu;
        menu.getSlot(0).set(tool);
        menu.getSlot(1).set(new ItemStack(Items.DIAMOND));
        menu.createResult();
        String route = "unenchanted_repair" + (shift ? "_shift" : "_normal");
        var preview = menu.getSlot(2).getItem();
        check(!preview.isEmpty() && preview.getDamageValue() < 400, "repair result exists " + route);
        check(menu.getCost() == 0 && menu.getSlot(2).mayPickup(player), "free repair available at zero XP " + route);
        menu.clicked(2, 0, shift ? net.minecraft.world.inventory.ContainerInput.QUICK_MOVE
            : net.minecraft.world.inventory.ContainerInput.PICKUP, player);
        check(player.experienceLevel == 0, "free repair remains free " + route);
        check(menu.getSlot(0).getItem().isEmpty() && menu.getSlot(1).getItem().isEmpty(), "repair inputs consumed " + route);
        var outputs = new java.util.ArrayList<ItemStack>(player.getInventory().getNonEquipmentItems().stream()
            .filter(stack -> stack.is(Items.DIAMOND_PICKAXE)).toList());
        if (menu.getCarried().is(Items.DIAMOND_PICKAXE)) outputs.add(menu.getCarried());
        check(outputs.size() == 1 && outputs.getFirst().getCount() == 1 && outputs.getFirst().getDamageValue() < 400,
            "exactly one repaired tool returned " + route);
        result.addProperty(route + "_charged_levels", 0);
        level.setBlock(pos, Blocks.ANVIL.defaultBlockState(), 3);
    }
    private static void check(boolean value, String name) {
        if (!value) throw new AssertionError(name);
        System.out.println("SWIFT_ANVIL_PASS " + name);
    }
}
