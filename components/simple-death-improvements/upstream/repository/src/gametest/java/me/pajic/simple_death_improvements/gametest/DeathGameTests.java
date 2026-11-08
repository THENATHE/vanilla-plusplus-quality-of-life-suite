package me.pajic.simple_death_improvements.gametest;

import me.pajic.simple_death_improvements.SDI;
import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.api.TrinketsApi;
import me.pajic.simple_death_improvements.access.PlayerAccess;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DeathGameTests {

    private static final BlockPos SAFE_BLOCK = new BlockPos(1, 0, 1);
    private static final BlockPos HAZARD_COLUMN = new BlockPos(5, 0, 5);
    private static final int XP_FOR_10_LEVELS_AT_80_PERCENT = 128;

    @GameTest
    public void safeSpotTrackedOnFirstGroundedTick(GameTestHelper helper) {
        ServerPlayer player = spawnOnSafeBlock(helper);
        try {
            helper.succeed();
        } finally {
            cleanup(helper, player);
        }
    }

    @GameTest
    public void safeSpotNotTrackedWhileAirborne(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            helper.setBlock(SAFE_BLOCK, Blocks.STONE);
            moveTo(helper, player, SAFE_BLOCK.above(3));
            player.setOnGround(false);
            player.doTick();
            helper.assertTrue(((PlayerAccess) player).sdi$getLastSafePos() == null, "Safe spot was tracked while airborne");
            helper.succeed();
        } finally {
            cleanup(helper, player);
        }
    }

    @GameTest
    public void normalDeathDropsInPlace(GameTestHelper helper) {
        ServerPlayer player = spawnOnSafeBlock(helper);
        try {
            player.getInventory().setItem(0, new ItemStack(Items.DIAMOND_SWORD));
            player.getInventory().setItem(20, new ItemStack(Items.DIRT, 32));
            player.giveExperienceLevels(10);
            Vec3 deathPos = player.position();
            kill(helper, player);
            List<ItemEntity> items = itemsNear(helper, deathPos, 2);
            helper.assertTrue(items.size() == 2, "Expected 2 dropped stacks at death spot, found " + items.size());
            for (ItemEntity item : items) {
                helper.assertTrue(item.getDeltaMovement().equals(Vec3.ZERO), "Death drop has velocity " + item.getDeltaMovement());
                helper.assertTrue(save(helper, item).getShortOr("Age", (short) 0) == -32768, "Death drop does not have unlimited lifetime");
            }
            assertSingleXpOrbAt(helper, deathPos);
            helper.succeed();
        } finally {
            cleanup(helper, player);
        }
    }

    @GameTest
    public void lavaDeathRelocatesToSafeSpot(GameTestHelper helper) {
        ServerPlayer player = spawnOnSafeBlock(helper);
        try {
            player.getInventory().setItem(0, new ItemStack(Items.DIAMOND_SWORD));
            player.giveExperienceLevels(10);
            helper.setBlock(HAZARD_COLUMN, Blocks.STONE);
            helper.setBlock(HAZARD_COLUMN.above(), Blocks.LAVA);
            moveTo(helper, player, HAZARD_COLUMN.above());
            player.setOnGround(false);
            player.doTick();
            helper.assertTrue(player.isInLava(), "Player is not in lava");
            kill(helper, player);
            Vec3 dropPos = safeDropPos(helper);
            assertItemsAt(helper, dropPos, Items.DIAMOND_SWORD);
            assertSingleXpOrbAt(helper, dropPos);
            helper.succeed();
        } finally {
            cleanup(helper, player);
        }
    }

    @GameTest
    public void airborneDeathAboveLavaRelocatesToSafeSpot(GameTestHelper helper) {
        ServerPlayer player = spawnOnSafeBlock(helper);
        try {
            player.getInventory().setItem(0, new ItemStack(Items.DIAMOND_SWORD));
            helper.setBlock(HAZARD_COLUMN, Blocks.STONE);
            helper.setBlock(HAZARD_COLUMN.above(), Blocks.LAVA);
            moveTo(helper, player, HAZARD_COLUMN.above(5));
            player.setOnGround(false);
            kill(helper, player);
            assertItemsAt(helper, safeDropPos(helper), Items.DIAMOND_SWORD);
            helper.succeed();
        } finally {
            cleanup(helper, player);
        }
    }

    @GameTest
    public void airborneDeathAboveFloorOverLavaDropsInPlace(GameTestHelper helper) {
        ServerPlayer player = spawnOnSafeBlock(helper);
        try {
            player.getInventory().setItem(0, new ItemStack(Items.DIAMOND_SWORD));
            helper.setBlock(HAZARD_COLUMN, Blocks.STONE);
            helper.setBlock(HAZARD_COLUMN.above(), Blocks.LAVA);
            helper.setBlock(HAZARD_COLUMN.above(3), Blocks.STONE);
            moveTo(helper, player, HAZARD_COLUMN.above(5));
            player.setOnGround(false);
            Vec3 deathPos = player.position();
            kill(helper, player);
            helper.assertTrue(itemsNear(helper, safeDropPos(helper), 0.1).isEmpty(), "Items were relocated despite a floor above the lava");
            helper.assertTrue(itemsNear(helper, deathPos, 2).size() == 1, "Items were not dropped at the death spot");
            helper.succeed();
        } finally {
            cleanup(helper, player);
        }
    }

    @GameTest
    public void voidDeathRelocatesToSafeSpot(GameTestHelper helper) {
        ServerPlayer player = spawnOnSafeBlock(helper);
        try {
            player.getInventory().setItem(0, new ItemStack(Items.DIAMOND_SWORD));
            player.giveExperienceLevels(10);
            Vec3 above = player.position();
            player.setPos(above.x, helper.getLevel().getMinY() - 70, above.z);
            player.setOnGround(false);
            kill(helper, player);
            Vec3 dropPos = safeDropPos(helper);
            assertItemsAt(helper, dropPos, Items.DIAMOND_SWORD);
            assertSingleXpOrbAt(helper, dropPos);
            helper.succeed();
        } finally {
            cleanup(helper, player);
        }
    }

    @GameTest
    public void safeSpotFromOtherDimensionIsIgnored(GameTestHelper helper) {
        ServerPlayer player = spawnOnSafeBlock(helper);
        try {
            player.getInventory().setItem(0, new ItemStack(Items.DIAMOND_SWORD));
            ServerLevel nether = helper.getLevel().getServer().getLevel(Level.NETHER);
            helper.assertTrue(nether != null, "The Nether is not loaded");
            Vec3 pos = player.position();
            player.teleport(new TeleportTransition(nether, new Vec3(pos.x, nether.getMinY() - 70, pos.z), Vec3.ZERO, 0, 0, TeleportTransition.DO_NOTHING));
            helper.assertTrue(player.level() == nether, "Player did not change dimension");
            player.hasChangedDimension();
            player.setOnGround(false);
            kill(helper, player);
            helper.assertTrue(((PlayerAccess) player).sdi$getDeathDropPos() == null, "Death drops were relocated to a safe spot in another dimension");
            helper.assertTrue(itemsNear(helper, safeDropPos(helper), 0.1).isEmpty(), "Items appeared at the overworld safe spot");
            helper.succeed();
        } finally {
            cleanup(helper, player);
        }
    }

    @GameTest
    public void keptItemsAreRestoredOnRespawn(GameTestHelper helper) {
        ServerPlayer player = spawnOnSafeBlock(helper);
        ServerPlayer respawned = null;
        boolean keepHotbar = SDI.CONFIG.keepHotbarOnDeath.get();
        boolean keepArmor = SDI.CONFIG.keepArmorOnDeath.get();
        boolean keepOffhand = SDI.CONFIG.keepOffhandOnDeath.get();
        List<Identifier> hotbarDropList = List.copyOf(SDI.CONFIG.hotbarDropList);
        List<Identifier> armorDropList = List.copyOf(SDI.CONFIG.armorDropList);
        try {
            SDI.CONFIG.keepHotbarOnDeath.accept(true);
            SDI.CONFIG.keepArmorOnDeath.accept(true);
            SDI.CONFIG.keepOffhandOnDeath.accept(true);
            SDI.CONFIG.hotbarDropList.accept(List.of(SDI.getItemId(new ItemStack(Items.DIAMOND_PICKAXE))));
            SDI.CONFIG.armorDropList.accept(List.of(SDI.getItemId(new ItemStack(Items.DIAMOND_CHESTPLATE))));
            player.getInventory().setItem(0, new ItemStack(Items.DIAMOND_SWORD));
            player.getInventory().setItem(1, new ItemStack(Items.DIAMOND_PICKAXE));
            player.getInventory().setItem(20, new ItemStack(Items.DIRT, 32));
            player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
            player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_CHESTPLATE));
            player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
            Vec3 deathPos = player.position();
            kill(helper, player);
            respawned = helper.getLevel().getServer().getPlayerList().respawn(player, false, Entity.RemovalReason.KILLED);
            helper.assertTrue(respawned.getInventory().getItem(0).is(Items.DIAMOND_SWORD), "Hotbar item was not kept");
            helper.assertTrue(respawned.getInventory().getItem(1).isEmpty(), "Hotbar item on the drop list was kept");
            helper.assertTrue(respawned.getInventory().getItem(20).isEmpty(), "Main inventory item was kept");
            helper.assertTrue(respawned.getItemBySlot(EquipmentSlot.CHEST).is(Items.IRON_CHESTPLATE), "Chestplate was not kept");
            helper.assertTrue(respawned.getItemBySlot(EquipmentSlot.LEGS).is(Items.IRON_LEGGINGS), "Leggings were not kept");
            helper.assertTrue(respawned.getItemBySlot(EquipmentSlot.FEET).isEmpty(), "Armor item on the drop list was kept");
            helper.assertTrue(respawned.getItemBySlot(EquipmentSlot.OFFHAND).is(Items.SHIELD), "Offhand item was not kept");
            List<ItemEntity> dropped = itemsNear(helper, deathPos, 2);
            helper.assertTrue(dropped.size() == 3, "Expected 3 dropped stacks, found " + dropped.size());
            helper.assertTrue(containsItem(dropped, Items.DIRT), "Main inventory item was not dropped");
            helper.assertTrue(containsItem(dropped, Items.DIAMOND_PICKAXE), "Hotbar item on the drop list was not dropped");
            helper.assertTrue(containsItem(dropped, Items.DIAMOND_CHESTPLATE), "Armor item on the drop list was not dropped");
            helper.succeed();
        } finally {
            SDI.CONFIG.keepHotbarOnDeath.accept(keepHotbar);
            SDI.CONFIG.keepArmorOnDeath.accept(keepArmor);
            SDI.CONFIG.keepOffhandOnDeath.accept(keepOffhand);
            SDI.CONFIG.hotbarDropList.accept(hotbarDropList);
            SDI.CONFIG.armorDropList.accept(armorDropList);
            cleanup(helper, respawned != null ? respawned : player);
        }
    }

    @GameTest
    public void damageableItemsAreKeptWithDurabilityLoss(GameTestHelper helper) {
        ServerPlayer player = spawnOnSafeBlock(helper);
        ServerPlayer respawned = null;
        boolean keepHotbar = SDI.CONFIG.keepHotbarOnDeath.get();
        boolean keepDamageable = SDI.CONFIG.keepDamageableItemsOnDeath.get();
        int durabilityLoss = SDI.CONFIG.keptItemDurabilityLossPercent.get();
        List<Identifier> damageableDropList = List.copyOf(SDI.CONFIG.damageableItemDropList);
        try {
            SDI.CONFIG.keepHotbarOnDeath.accept(true);
            SDI.CONFIG.keepDamageableItemsOnDeath.accept(true);
            SDI.CONFIG.keptItemDurabilityLossPercent.accept(25);
            SDI.CONFIG.damageableItemDropList.accept(List.of(SDI.getItemId(new ItemStack(Items.DIAMOND_SHOVEL))));
            ItemStack almostBroken = new ItemStack(Items.IRON_PICKAXE);
            almostBroken.setDamageValue(240);
            player.getInventory().setItem(0, new ItemStack(Items.IRON_SWORD));
            player.getInventory().setItem(20, new ItemStack(Items.DIAMOND_SWORD));
            player.getInventory().setItem(21, almostBroken);
            player.getInventory().setItem(22, new ItemStack(Items.DIRT, 32));
            player.getInventory().setItem(23, new ItemStack(Items.DIAMOND_SHOVEL));
            player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            Vec3 deathPos = player.position();
            kill(helper, player);
            respawned = helper.getLevel().getServer().getPlayerList().respawn(player, false, Entity.RemovalReason.KILLED);
            assertKeptWithDamage(helper, respawned.getInventory().getItem(0), Items.IRON_SWORD, 0);
            assertKeptWithDamage(helper, respawned.getInventory().getItem(20), Items.DIAMOND_SWORD, 391);
            assertKeptWithDamage(helper, respawned.getInventory().getItem(21), Items.IRON_PICKAXE, 249);
            assertKeptWithDamage(helper, respawned.getItemBySlot(EquipmentSlot.CHEST), Items.IRON_CHESTPLATE, 60);
            helper.assertTrue(respawned.getInventory().getItem(22).isEmpty(), "Item without durability was kept");
            helper.assertTrue(respawned.getInventory().getItem(23).isEmpty(), "Item on the drop list was kept");
            List<ItemEntity> dropped = itemsNear(helper, deathPos, 2);
            helper.assertTrue(dropped.size() == 2, "Expected 2 dropped stacks, found " + dropped.size());
            helper.assertTrue(containsItem(dropped, Items.DIRT), "Item without durability was not dropped");
            helper.assertTrue(containsItem(dropped, Items.DIAMOND_SHOVEL), "Item on the drop list was not dropped");
            helper.succeed();
        } finally {
            SDI.CONFIG.keepHotbarOnDeath.accept(keepHotbar);
            SDI.CONFIG.keepDamageableItemsOnDeath.accept(keepDamageable);
            SDI.CONFIG.keptItemDurabilityLossPercent.accept(durabilityLoss);
            SDI.CONFIG.damageableItemDropList.accept(damageableDropList);
            cleanup(helper, respawned != null ? respawned : player);
        }
    }

    @GameTest
    public void damageableTrinketsAreKeptWithDurabilityLoss(GameTestHelper helper) {
        ServerPlayer player = spawnOnSafeBlock(helper);
        boolean keepAccessories = SDI.CONFIG.keepAccessoriesOnDeath.get();
        boolean keepDamageable = SDI.CONFIG.keepDamageableItemsOnDeath.get();
        int durabilityLoss = SDI.CONFIG.keptItemDurabilityLossPercent.get();
        try {
            SDI.CONFIG.keepAccessoriesOnDeath.accept(false);
            SDI.CONFIG.keepDamageableItemsOnDeath.accept(true);
            SDI.CONFIG.keptItemDurabilityLossPercent.accept(25);
            List<TrinketInventory> inventories = TrinketsApi.getAttachment(player).getInventories().values().stream()
                    .filter(inventory -> inventory.getContainerSize() > 0)
                    .toList();
            helper.assertTrue(inventories.size() >= 2, "Expected at least 2 trinket slots, found " + inventories.size());
            TrinketInventory damageableSlot = inventories.get(0);
            TrinketInventory otherSlot = inventories.get(1);
            damageableSlot.setItem(0, new ItemStack(Items.IRON_SWORD));
            otherSlot.setItem(0, new ItemStack(Items.DIRT));
            Vec3 deathPos = player.position();
            kill(helper, player);
            assertKeptWithDamage(helper, damageableSlot.getItem(0), Items.IRON_SWORD, 63);
            helper.assertTrue(otherSlot.getItem(0).isEmpty(), "Trinket without durability was kept");
            helper.assertTrue(containsItem(itemsNear(helper, deathPos, 2), Items.DIRT), "Trinket without durability was not dropped");
            TrinketsApi.getAttachment(player).forEachDroppable((access, stack) -> {});
            assertKeptWithDamage(helper, damageableSlot.getItem(0), Items.IRON_SWORD, 63);
            helper.succeed();
        } finally {
            SDI.CONFIG.keepAccessoriesOnDeath.accept(keepAccessories);
            SDI.CONFIG.keepDamageableItemsOnDeath.accept(keepDamageable);
            SDI.CONFIG.keptItemDurabilityLossPercent.accept(durabilityLoss);
            cleanup(helper, player);
        }
    }

    @GameTest
    public void customDespawnTimerSurvivesReload(GameTestHelper helper) {
        ServerPlayer player = spawnOnSafeBlock(helper);
        boolean noDespawn = SDI.CONFIG.noDeathItemDespawn.get();
        int timer = SDI.CONFIG.itemDespawnTimer.get();
        try {
            SDI.CONFIG.noDeathItemDespawn.accept(false);
            SDI.CONFIG.itemDespawnTimer.accept(30);
            player.getInventory().setItem(0, new ItemStack(Items.DIAMOND_SWORD));
            Vec3 deathPos = player.position();
            kill(helper, player);
            List<ItemEntity> items = itemsNear(helper, deathPos, 2);
            helper.assertTrue(items.size() == 1, "Expected 1 dropped stack, found " + items.size());
            CompoundTag saved = save(helper, items.getFirst());
            helper.assertTrue(saved.getIntOr("DeathItemLifetime", -1) == 600, "Lifetime was not saved");
            ItemEntity reloaded = new ItemEntity(helper.getLevel(), 0, 0, 0, new ItemStack(Items.STONE));
            reloaded.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved));
            helper.assertTrue(save(helper, reloaded).getIntOr("DeathItemLifetime", -1) == 600, "Lifetime was not loaded");
            helper.succeed();
        } finally {
            SDI.CONFIG.noDeathItemDespawn.accept(noDespawn);
            SDI.CONFIG.itemDespawnTimer.accept(timer);
            cleanup(helper, player);
        }
    }

    @GameTest
    public void itemsIgnoreExplosions(GameTestHelper helper) {
        helper.setBlock(HAZARD_COLUMN, Blocks.STONE);
        ItemEntity item = helper.spawnItem(Items.DIAMOND, Vec3.atBottomCenterOf(HAZARD_COLUMN.above()));
        Vec3 pos = item.position();
        helper.getLevel().explode(null, pos.x, pos.y, pos.z, 4, Level.ExplosionInteraction.NONE);
        helper.assertTrue(item.isAlive(), "Item was destroyed by an explosion");
        helper.killAllEntitiesOfClass(ItemEntity.class);
        helper.succeed();
    }

    private static ServerPlayer spawnOnSafeBlock(GameTestHelper helper) {
        helper.setBlock(SAFE_BLOCK, Blocks.STONE);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        moveTo(helper, player, SAFE_BLOCK.above());
        player.setOnGround(true);
        player.doTick();
        GlobalPos expected = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(SAFE_BLOCK));
        GlobalPos tracked = ((PlayerAccess) player).sdi$getLastSafePos();
        helper.assertTrue(expected.equals(tracked), "Expected safe spot " + expected + ", got " + tracked);
        return player;
    }

    private static void moveTo(GameTestHelper helper, ServerPlayer player, BlockPos relative) {
        player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(relative)));
    }

    private static void kill(GameTestHelper helper, ServerPlayer player) {
        player.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
        player.kill(player.level());
        helper.assertTrue(player.isDeadOrDying(), "Player did not die");
    }

    private static void cleanup(GameTestHelper helper, ServerPlayer player) {
        helper.getLevel().getServer().getPlayerList().remove(player);
        helper.killAllEntitiesOfClass(ItemEntity.class);
        helper.killAllEntitiesOfClass(ExperienceOrb.class);
    }

    private static Vec3 safeDropPos(GameTestHelper helper) {
        return Vec3.atBottomCenterOf(helper.absolutePos(SAFE_BLOCK).above());
    }

    private static List<ItemEntity> itemsNear(GameTestHelper helper, Vec3 pos, double radius) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, AABB.ofSize(pos, radius * 2, radius * 2, radius * 2));
    }

    private static boolean containsItem(List<ItemEntity> items, Item item) {
        return items.stream().anyMatch(entity -> entity.getItem().is(item));
    }

    private static void assertItemsAt(GameTestHelper helper, Vec3 pos, Item item) {
        List<ItemEntity> items = itemsNear(helper, pos, 0.1);
        helper.assertTrue(items.size() == 1 && items.getFirst().getItem().is(item), "Expected " + item + " at " + pos + ", found " + items);
    }

    private static void assertSingleXpOrbAt(GameTestHelper helper, Vec3 pos) {
        List<ExperienceOrb> orbs = helper.getLevel().getEntitiesOfClass(ExperienceOrb.class, helper.getBounds());
        helper.assertTrue(orbs.size() == 1, "Expected 1 XP orb, found " + orbs.size());
        ExperienceOrb orb = orbs.getFirst();
        helper.assertTrue(orb.position().distanceTo(pos) < 0.1, "Expected XP orb at " + pos + ", found " + orb.position());
        helper.assertTrue(orb.getValue() == XP_FOR_10_LEVELS_AT_80_PERCENT, "Expected XP value " + XP_FOR_10_LEVELS_AT_80_PERCENT + ", found " + orb.getValue());
    }

    private static void assertKeptWithDamage(GameTestHelper helper, ItemStack stack, Item item, int damage) {
        helper.assertTrue(stack.is(item), "Expected kept " + item + ", found " + stack);
        helper.assertTrue(stack.getDamageValue() == damage, "Expected " + item + " damage " + damage + ", found " + stack.getDamageValue());
    }

    private static CompoundTag save(GameTestHelper helper, Entity entity) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        entity.saveWithoutId(output);
        return output.buildResult();
    }
}
