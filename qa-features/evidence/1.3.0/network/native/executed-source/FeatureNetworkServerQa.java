package qa.featurenetwork;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Observes ordinary menu packets on connected players; does not override capabilities. */
public final class FeatureNetworkServerQa implements ModInitializer {
    private final HashMap<UUID, Integer> ages = new HashMap<>();
    private final Set<UUID> seeded = new HashSet<>();
    private final Set<UUID> completed = new HashSet<>();

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            try {
                Path control = Path.of(System.getProperty("features.qa.control"));
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    int age = ages.merge(player.getUUID(), 1, Integer::sum);
                    String name = player.getGameProfile().name();
                    if (age >= 40 && Files.exists(control.resolve(name + "-open")) && seeded.add(player.getUUID())) {
                        player.getInventory().clearContent();
                        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                        player.giveExperienceLevels(100 - player.experienceLevel);
                        var sword = new ItemStack(Items.DIAMOND_SWORD);
                        sword.enchant(server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                                .getOrThrow(Enchantments.SHARPNESS), 5);
                        sword.setDamageValue(120);
                        sword.set(DataComponents.CUSTOM_NAME, Component.literal("Network extraction sword"));
                        player.getInventory().setItem(9, sword);
                        player.getInventory().setItem(10, new ItemStack(Items.BOOK));
                        var pos = player.blockPosition().offset(2, 0, 0);
                        player.level().setBlock(pos, Blocks.GRINDSTONE.defaultBlockState(), 3);
                        var access = ContainerLevelAccess.create(player.level(), pos);
                        player.openMenu(new SimpleMenuProvider((id, inventory, ignored) ->
                                new GrindstoneMenu(id, inventory, access), Component.literal("Native feature network QA")));
                    }
                    var result = new JsonObject();
                    result.addProperty("age", age);
                    result.addProperty("seeded", seeded.contains(player.getUUID()));
                    result.addProperty("xp_levels", player.experienceLevel);
                    if (player.containerMenu instanceof GrindstoneMenu menu) {
                        result.addProperty("top_count", menu.getSlot(0).getItem().getCount());
                        result.addProperty("bottom_count", menu.getSlot(1).getItem().getCount());
                        result.addProperty("output_count", menu.getSlot(2).getItem().getCount());
                        result.addProperty("carried_count", menu.getCarried().getCount());
                        if (seeded.contains(player.getUUID()) && player.getInventory().getNonEquipmentItems().stream()
                                .anyMatch(stack -> stack.is(Items.ENCHANTED_BOOK))) {
                            var books = player.getInventory().getNonEquipmentItems().stream()
                                    .filter(stack -> stack.is(Items.ENCHANTED_BOOK)).toList();
                            var sword = menu.getSlot(0).getItem();
                            if (sword.isEmpty()) sword = player.getInventory().getNonEquipmentItems().stream()
                                    .filter(stack -> stack.is(Items.DIAMOND_SWORD)).findFirst().orElse(ItemStack.EMPTY);
                            int swordCount = (int) player.getInventory().getNonEquipmentItems().stream()
                                    .filter(stack -> stack.is(Items.DIAMOND_SWORD)).mapToInt(ItemStack::getCount).sum()
                                    + (menu.getSlot(0).getItem().is(Items.DIAMOND_SWORD) ? menu.getSlot(0).getItem().getCount() : 0);
                            int bookCount = books.stream().mapToInt(ItemStack::getCount).sum();
                            int plainBooks = player.getInventory().getNonEquipmentItems().stream()
                                    .filter(stack -> stack.is(Items.BOOK)).mapToInt(ItemStack::getCount).sum()
                                    + (menu.getSlot(1).getItem().is(Items.BOOK) ? menu.getSlot(1).getItem().getCount() : 0);
                            var enchantment = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                                    .getOrThrow(Enchantments.SHARPNESS);
                            if (swordCount != 1 || bookCount != 1 || plainBooks != 0 || sword.getDamageValue() != 120
                                    || !sword.getHoverName().getString().equals("Network extraction sword")
                                    || sword.getOrDefault(DataComponents.ENCHANTMENTS,
                                    net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY).getLevel(enchantment) != 0
                                    || books.getFirst().getOrDefault(DataComponents.STORED_ENCHANTMENTS,
                                    net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY).getLevel(enchantment) != 5) {
                                throw new AssertionError("Real grindstone extraction did not conserve equipment and enchantment");
                            }
                            completed.add(player.getUUID());
                            result.addProperty("complete", true);
                            result.addProperty("sword_preserved", true);
                            result.addProperty("book_sharpness_level", 5);
                            result.addProperty("plain_book_consumed", true);
                        }
                    }
                    result.addProperty("completed_once", completed.contains(player.getUUID()));
                    Files.writeString(control.resolve(name + "-server.json"),
                            new GsonBuilder().setPrettyPrinting().create().toJson(result));
                }
            } catch (Throwable error) {
                error.printStackTrace();
                try {
                    Files.writeString(Path.of(System.getProperty("features.qa.control"), "failure"), error.toString());
                } catch (Exception writeError) {
                    throw new RuntimeException(writeError);
                }
            }
        });
    }
}
