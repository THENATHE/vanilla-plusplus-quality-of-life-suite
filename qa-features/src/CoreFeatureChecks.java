import com.thenathe.suite.features.AllWeatherChanneling;
import com.thenathe.suite.features.FeatureConfig;
import com.thenathe.suite.features.GrindExtraction;
import com.thenathe.suite.features.NetheriteInstamine;
import com.thenathe.suite.features.ShulkerPickup;
import com.thenathe.suite.features.StructurePlacement;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConduitBlock;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

/** Exercises actual server menus and hooks; these are not physical client acceptance tests. */
public final class CoreFeatureChecks {
    private static int checks;

    private static void check(boolean passed, String message) {
        if (!passed) throw new AssertionError(message);
        checks++;
        System.out.println("CORE_FEATURE_PASS " + message);
    }

    public static void run(MinecraftServer server, ServerPlayer player) throws Exception {
        checks = 0;
        var config = FeatureConfig.CONFIG;
        config.dropsIntoShulker.accept(true);
        config.beaconPlacement.accept(true);
        config.conduitPlacement.accept(true);
        config.grindEnchantments.accept(true);
        config.netheriteInstamine.accept(true);
        config.noEndermanGrief.accept(true);
        config.allWeatherChanneling.accept(true);
        shulkers(player);
        pickupPriority(player);
        placement(player);
        grindstones(server, player);
        instamine(server, player);
        endermen(player);
        channeling(server, player);
        System.out.println("CORE_FEATURE_PASS total=" + checks);
    }

    private static int containerCount(ItemStack holder) {
        return holder.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
                .nonEmptyItemCopyStream().mapToInt(ItemStack::getCount).sum();
    }

    private static void shulkers(ServerPlayer player) {
        player.getInventory().clearContent();
        var holder = new ItemStack(Items.SHULKER_BOX);
        player.setItemSlot(EquipmentSlot.OFFHAND, holder);
        var incoming = new ItemStack(Items.DIRT, 77);
        check(ShulkerPickup.collect(player, incoming) == 77 && incoming.isEmpty()
                && containerCount(holder) == 77, "shulker insertion conserves oversized incoming count");
        var nested = new ItemStack(Items.SHULKER_BOX);
        check(ShulkerPickup.collect(player, nested) == 0 && nested.getCount() == 1, "shulker nesting rejected");
        holder.setCount(2);
        var dirt = new ItemStack(Items.DIRT, 3);
        check(ShulkerPickup.collect(player, dirt) == 0 && dirt.getCount() == 3, "stacked receiver rejected without mutation");
        holder.setCount(1);
        var slots = NonNullList.withSize(27, new ItemStack(Items.COBBLESTONE, 64));
        slots.set(0, new ItemStack(Items.DIRT, 63));
        holder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(slots));
        int before = containerCount(holder);
        check(ShulkerPickup.collect(player, dirt) == 1 && dirt.getCount() == 2
                && containerCount(holder) == before + 1, "partial shulker insertion preserves remainder");
        player.getInventory().add(dirt);
        check(dirt.isEmpty() && player.getInventory().countItem(Items.DIRT) == 2, "full shulker falls back to normal inventory");
        FeatureConfig.CONFIG.dropsIntoShulker.accept(false);
        check(ShulkerPickup.collect(player, new ItemStack(Items.APPLE)) == 0, "shulker disable switch");
        FeatureConfig.CONFIG.dropsIntoShulker.accept(true);
        player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHULKER_BOX));
        var apples = new ItemStack(Items.APPLE, 3);
        check(player.getInventory().add(apples) && apples.isEmpty()
                && containerCount(player.getOffhandItem()) == 3, "actual Inventory.add hook inserts into offhand");
        player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
    }

    private static void placement(ServerPlayer player) {
        var level = player.level();
        var config = FeatureConfig.CONFIG;
        config.beaconMaximumLayers.accept(4);
        config.breakBeaconBase.accept(true);
        config.breakConduitFrame.accept(true);
        config.clearCompletedConduit.accept(true);
        BlockPos beacon = new BlockPos(48, 110, 48);
        player.setPos(48, 110, 48);
        level.getChunkAt(beacon);
        level.setBlock(beacon, Blocks.BEACON.defaultBlockState(), 3);
        var positions = StructurePlacement.beaconPositions(beacon);
        check(positions.size() == 164 && positions.stream().distinct().count() == 164,
                "beacon has four layers and 164 unique positions");
        positions.forEach(pos -> level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3));
        BlockPos bedrock = positions.getFirst();
        BlockPos denied = positions.get(1);
        level.setBlock(bedrock, Blocks.BEDROCK.defaultBlockState(), 3);
        level.setBlock(denied, Blocks.STONE.defaultBlockState(), 3);
        AtomicReference<BlockPos> protectedBlock = new AtomicReference<>(denied);
        PlayerBlockBreakEvents.BEFORE.register((world, actor, pos, state, entity) -> !pos.equals(protectedBlock.get()));
        var held = new ItemStack(Items.IRON_BLOCK, 3);
        player.setItemSlot(EquipmentSlot.MAINHAND, held);
        int placed = StructurePlacement.fill(player, InteractionHand.MAIN_HAND, beacon, true,
                held, Blocks.IRON_BLOCK.defaultBlockState(), false);
        check(placed == 1 && held.getCount() == 2, "beacon one-block placement charges one supplied block");
        check(level.getBlockState(bedrock).is(Blocks.BEDROCK) && level.getBlockState(denied).is(Blocks.STONE),
                "beacon placement preserves bedrock and protected targets");
        check(StructurePlacement.fill(player, InteractionHand.MAIN_HAND, beacon, true,
                held, Blocks.IRON_BLOCK.defaultBlockState(), true) == 2 && held.isEmpty(), "beacon bulk consumes exact stack");
        player.setGameMode(GameType.ADVENTURE);
        check(StructurePlacement.fill(player, InteractionHand.MAIN_HAND, beacon, true,
                new ItemStack(Items.IRON_BLOCK), Blocks.IRON_BLOCK.defaultBlockState(), false) == 0,
                "adventure mode refuses unpermitted placement");
        player.setGameMode(GameType.SURVIVAL);
        StructurePlacement.dismantle(level, player, beacon, Blocks.BEACON.defaultBlockState(), null);
        check(positions.stream().noneMatch(pos -> level.getBlockState(pos).is(Blocks.IRON_BLOCK)), "beacon dismantling removes placed base only");
        check(level.getBlockState(bedrock).is(Blocks.BEDROCK) && level.getBlockState(denied).is(Blocks.STONE),
                "beacon dismantling retains unrelated blocks");
        protectedBlock.set(null);

        BlockPos conduit = new BlockPos(64, 110, 64);
        level.getChunkAt(conduit);
        player.setPos(68, 110, 68);
        level.setBlock(conduit, Blocks.CONDUIT.defaultBlockState().setValue(ConduitBlock.WATERLOGGED, true), 3);
        var frame = StructurePlacement.conduitPositions(conduit);
        check(frame.size() == 42 && frame.stream().distinct().count() == 42, "conduit frame has 42 vanilla positions");
        frame.forEach(pos -> level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3));
        for (int x = -1; x <= 1; x++) for (int y = -1; y <= 1; y++) for (int z = -1; z <= 1; z++) {
            if (x != 0 || y != 0 || z != 0) level.setBlock(conduit.offset(x, y, z), Blocks.STONE.defaultBlockState(), 3);
        }
        held = new ItemStack(Items.PRISMARINE, 42);
        player.setItemSlot(EquipmentSlot.MAINHAND, held);
        check(StructurePlacement.fill(player, InteractionHand.MAIN_HAND, conduit, false,
                held, Blocks.PRISMARINE.defaultBlockState(), true) == 42 && held.isEmpty(), "conduit bulk builds full frame without material loss");
        check(frame.stream().allMatch(pos -> level.getBlockState(pos).is(Blocks.PRISMARINE)), "complete conduit geometry preserved");
        check(level.isWaterAt(conduit.offset(1, 0, 0)), "completed conduit clears interior to water");
        StructurePlacement.dismantle(level, player, conduit, Blocks.CONDUIT.defaultBlockState(), null);
        check(frame.stream().noneMatch(pos -> level.getBlockState(pos).is(Blocks.PRISMARINE)), "conduit frame dismantling");

        player.setPos(92.5, 110, 92.5);
        player.setXRot(0);
        player.setYRot(0);
        config.conduitPlacementDistance.accept(4.0F);
        BlockPos target = BlockPos.containing(player.getEyePosition().add(player.getLookAngle().scale(4)));
        level.getChunkAt(target);
        level.setBlock(target, Blocks.WATER.defaultBlockState(), 3);
        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CONDUIT, 2));
        var outcome = Items.CONDUIT.use(level, player, InteractionHand.MAIN_HAND);
        check(outcome.consumesAction() && level.getBlockState(target).is(Blocks.CONDUIT)
                && player.getMainHandItem().getCount() == 1, "actual conduit item use places in targeted water");

        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND));
        check(StructurePlacement.use(player, level, InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(beacon),
                        net.minecraft.core.Direction.UP, beacon, false)) == net.minecraft.world.InteractionResult.PASS,
                "ordinary beacon menu interaction remains available");
    }

    private static void pickupPriority(ServerPlayer player) {
        me.pajic.toolpouch.ToolPouch.CONFIG.allowUseFromInventory.accept(true);
        player.removeTag(com.thenathe.suite.pickup.PickupUpgrades.DISABLED_TAG);
        for (boolean shulkerFirst : new boolean[]{true, false}) {
            player.getInventory().clearContent();
            var pouch = new ItemStack(me.pajic.toolpouch.item.ModItems.TOOL_POUCH);
            pouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(java.util.List.of(
                    new ItemStack(com.thenathe.suite.SuiteItems.POUCH_COLLECTOR))));
            player.getInventory().setItem(4, pouch);
            var backpack = new ItemStack(me.pajic.tiered_backpacks.item.ModItems.LEATHER_BACKPACK);
            player.getInventory().setItem(25, backpack);
            var shulker = new ItemStack(Items.SHULKER_BOX);
            player.setItemSlot(EquipmentSlot.OFFHAND, shulker);
            FeatureConfig.CONFIG.shulkerBeforeBackpack.accept(shulkerFirst);
            var drop = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), new ItemStack(Items.DIAMOND, 17));
            drop.setNoPickUpDelay();
            drop.playerTouch(player);
            check(drop.isRemoved() && containerCount(shulker) == (shulkerFirst ? 17 : 0)
                    && containerCount(backpack) == (shulkerFirst ? 0 : 17),
                    "actual pickup routing honors " + (shulkerFirst ? "shulker" : "backpack") + " priority");
        }
        FeatureConfig.CONFIG.shulkerBeforeBackpack.accept(true);
        player.getInventory().clearContent();
    }

    private static void grindstones(MinecraftServer server, ServerPlayer player) {
        var ench = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var config = FeatureConfig.CONFIG;
        config.grindExtraction.accept(true);
        config.grindTransfer.accept(true);
        config.extractCurses.accept(false);
        config.grindOnlyFirst.accept(false);
        config.grindConsumeSource.accept(false);
        config.grindCostInPoints.accept(false);
        var gear = new ItemStack(Items.DIAMOND_PICKAXE);
        gear.setDamageValue(31);
        gear.enchant(ench.getOrThrow(Enchantments.EFFICIENCY), 5);
        gear.enchant(ench.getOrThrow(Enchantments.VANISHING_CURSE), 1);
        for (int orientation = 0; orientation < 2; orientation++) {
            player.getInventory().clearContent();
            player.experienceLevel = 100;
            var menu = new GrindstoneMenu(10 + orientation, player.getInventory());
            menu.getSlot(orientation).set(gear.copy());
            check(menu.getSlot(1 - orientation).mayPlace(new ItemStack(Items.BOOK)), "grindstone accepts plain book " + orientation);
            menu.getSlot(1 - orientation).set(new ItemStack(Items.BOOK, 2));
            var result = menu.getSlot(2).getItem();
            check(result.is(Items.ENCHANTED_BOOK) && result.getCount() == 1, "grindstone creates one enchanted book " + orientation);
            check(result.has(DataComponents.LORE), "grindstone preview displays cost " + orientation);
            var match = GrindExtraction.match(player, menu.getSlot(0).getItem(), menu.getSlot(1).getItem());
            int expectedCost = (int) Math.ceil(ench.getOrThrow(Enchantments.EFFICIENCY).value().getMinCost(5) * .3 + 8);
            check(match.cost() == expectedCost, "grindstone default extraction cost " + orientation);
            player.experienceLevel = 0;
            check(!menu.getSlot(2).mayPickup(player) && menu.quickMoveStack(player, 2).isEmpty(), "insufficient XP blocks all extraction routes " + orientation);
            player.experienceLevel = 100;
            ItemStack taken = menu.getSlot(2).remove(1);
            menu.getSlot(2).onTake(player, taken);
            check(taken.is(Items.ENCHANTED_BOOK) && ItemStack.matches(taken, match.output()), "taken book has no preview-only lore " + orientation);
            check(player.experienceLevel == 100 - expectedCost, "XP charged once " + orientation);
            var returned = menu.getSlot(orientation).getItem();
            check(returned.is(Items.DIAMOND_PICKAXE) && returned.getDamageValue() == 31
                    && EnchantmentHelper.getItemEnchantmentLevel(ench.getOrThrow(Enchantments.VANISHING_CURSE), returned) == 1
                    && EnchantmentHelper.getItemEnchantmentLevel(ench.getOrThrow(Enchantments.EFFICIENCY), returned) == 0,
                    "original equipment returned with damage and curse " + orientation);
            check(menu.getSlot(1 - orientation).getItem().getCount() == 1, "exactly one blank book consumed " + orientation);
        }
        player.experienceLevel = 100;
        var menu = new GrindstoneMenu(15, player.getInventory());
        var book = new ItemStack(Items.ENCHANTED_BOOK);
        book.enchant(ench.getOrThrow(Enchantments.EFFICIENCY), 5);
        book.enchant(ench.getOrThrow(Enchantments.UNBREAKING), 3);
        menu.getSlot(0).set(book);
        menu.getSlot(1).set(new ItemStack(Items.BOOK));
        check(menu.getSlot(2).hasItem(), "book transfer preview available");
        ItemStack output = menu.quickMoveStack(player, 2);
        check(output.is(Items.ENCHANTED_BOOK) && output.getOrDefault(DataComponents.LORE,
                net.minecraft.world.item.component.ItemLore.EMPTY).lines().isEmpty(), "shift-click transfer uses clean output");
        check(EnchantmentHelper.getEnchantmentsForCrafting(menu.getSlot(0).getItem()).size() == 1,
                "book transfer leaves remaining enchantment in source");
        check(player.getInventory().countItem(Items.ENCHANTED_BOOK) == 1 && menu.getSlot(1).getItem().isEmpty(),
                "book transfer conserves both inputs");

        var cursed = new ItemStack(Items.DIAMOND_CHESTPLATE);
        cursed.enchant(ench.getOrThrow(Enchantments.BINDING_CURSE), 1);
        menu = new GrindstoneMenu(16, player.getInventory());
        menu.getSlot(0).set(cursed);
        menu.getSlot(1).set(new ItemStack(Items.AMETHYST_SHARD));
        check(menu.getSlot(2).hasItem(), "amethyst curse-cleanser recipe retained");
        ItemStack cleansed = menu.getSlot(2).remove(1);
        menu.getSlot(2).onTake(player, cleansed);
        check(EnchantmentHelper.getItemEnchantmentLevel(ench.getOrThrow(Enchantments.BINDING_CURSE), cleansed) == 0
                && menu.getSlot(0).getItem().isEmpty() && menu.getSlot(1).getItem().isEmpty(), "curse cleanser result and consumption retained");
        var scrapGear = gear.copy();
        scrapGear.set(DataComponents.REPAIR_COST, 10);
        menu = new GrindstoneMenu(17, player.getInventory());
        menu.setCarried(scrapGear);
        menu.clicked(0, 0, ContainerInput.PICKUP, player);
        check(menu.getCarried().isEmpty(), "SSO repair-cost equipment accepts ordinary cursor insertion");
        menu.setCarried(new ItemStack(Items.NETHERITE_SCRAP));
        menu.clicked(1, 0, ContainerInput.PICKUP, player);
        check(menu.getCarried().isEmpty() && menu.getSlot(1).getItem().is(Items.NETHERITE_SCRAP),
                "SSO netherite scrap accepts ordinary cursor insertion");
        check(menu.getSlot(2).getItem().getOrDefault(DataComponents.REPAIR_COST, 0) == 5, "SSO scrap repair-cost reduction retained");
        menu.clicked(2, 0, ContainerInput.PICKUP, player);
        check(menu.getCarried().getOrDefault(DataComponents.REPAIR_COST, 0) == 5
                && menu.getSlot(0).getItem().isEmpty() && menu.getSlot(1).getItem().isEmpty(),
                "SSO ordinary cursor pickup consumes one equipment item and one scrap");
        config.grindEnchantments.accept(false);
        check(GrindExtraction.match(player, gear, new ItemStack(Items.BOOK)) == null, "grindstone module disable switch");
        check(menu.getSlot(1).mayPlace(new ItemStack(Items.NETHERITE_SCRAP)),
                "SSO scrap remains accepted with extraction disabled");
        var scrapRecipe = me.pajic.simple_smithing_overhaul.SSO.CONFIG.grindstoneImprovements.repairCostReductionRecipe;
        boolean originalScrapRecipe = scrapRecipe.get();
        try {
            scrapRecipe.accept(false);
            check(!menu.getSlot(1).mayPlace(new ItemStack(Items.NETHERITE_SCRAP)),
                    "disabled SSO scrap recipe rejects scrap");
            scrapRecipe.accept(true);
            check(!menu.getSlot(0).mayPlace(new ItemStack(Items.NETHERITE_SCRAP)),
                    "SSO scrap is accepted only in the additional slot");
        } finally {
            scrapRecipe.accept(originalScrapRecipe);
        }
        config.grindEnchantments.accept(true);
    }

    private static void instamine(MinecraftServer server, ServerPlayer player) {
        var efficiency = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY);
        var attribute = player.getAttribute(Attributes.MINING_EFFICIENCY);
        double originalBase = attribute.getBaseValue();
        attribute.setBaseValue(7);
        var tool = new ItemStack(Items.NETHERITE_PICKAXE);
        tool.enchant(efficiency, 5);
        player.setItemSlot(EquipmentSlot.MAINHAND, tool);
        player.addEffect(new MobEffectInstance(MobEffects.HASTE, 200, 1));
        NetheriteInstamine.update(player);
        check(attribute.getModifier(NetheriteInstamine.MODIFIER).amount() == 30 && attribute.getBaseValue() == 7,
                "instamine pickaxe modifier preserves foreign base");
        tool = new ItemStack(Items.NETHERITE_AXE);
        tool.enchant(efficiency, 5);
        player.setItemSlot(EquipmentSlot.MAINHAND, tool);
        NetheriteInstamine.update(player);
        check(attribute.getModifier(NetheriteInstamine.MODIFIER).amount() == 10, "instamine axe uses correct amount");
        player.removeEffect(MobEffects.HASTE);
        NetheriteInstamine.update(player);
        check(attribute.getModifier(NetheriteInstamine.MODIFIER) == null && attribute.getBaseValue() == 7,
                "instamine cleans only owned modifier after Haste ends");
        attribute.setBaseValue(originalBase);
    }

    private static void endermen(ServerPlayer player) throws Exception {
        var enderman = EntityTypes.ENDERMAN.create(player.level(), EntitySpawnReason.COMMAND);
        enderman.setCarriedBlock(Blocks.DIRT.defaultBlockState());
        Class<?> type = Class.forName("net.minecraft.world.entity.monster.Enderman$EndermanTakeBlockGoal");
        var constructor = type.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object goal = constructor.newInstance(enderman);
        Method canUse = type.getDeclaredMethod("canUse");
        canUse.setAccessible(true);
        check(!(Boolean) canUse.invoke(goal), "enderman pickup goal disabled");
        check(enderman.getCarriedBlock().is(Blocks.DIRT), "existing enderman carried block preserved");
    }

    private static void channeling(MinecraftServer server, ServerPlayer player) throws Exception {
        var weapon = new ItemStack(Items.TRIDENT);
        var enchantments = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        weapon.enchant(enchantments.getOrThrow(Enchantments.CHANNELING), 1);
        weapon.enchant(enchantments.getOrThrow(Enchantments.LOYALTY), 3);
        ItemStack filtered = AllWeatherChanneling.vanillaAttackWeapon(weapon);
        check(EnchantmentHelper.getItemEnchantmentLevel(enchantments.getOrThrow(Enchantments.CHANNELING), filtered) == 0
                && EnchantmentHelper.getItemEnchantmentLevel(enchantments.getOrThrow(Enchantments.LOYALTY), filtered) == 3,
                "channeling ownership suppresses duplicate vanilla effect only");
        var trident = new ThrownTrident(player.level(), player, weapon);
        var target = EntityTypes.COW.create(player.level(), EntitySpawnReason.COMMAND);
        target.setPos(80, 110, 80);
        player.level().getChunkAt(target.blockPosition());
        check(AllWeatherChanneling.strike(trident, target), "all-weather channeling strikes surviving entity");
        target.setHealth(0);
        check(!AllWeatherChanneling.strike(trident, target), "all-weather channeling ignores lethal hits");

        var managerField = net.minecraft.server.level.ServerLevel.class.getDeclaredField("entityManager");
        managerField.setAccessible(true);
        @SuppressWarnings("unchecked")
        var manager = (net.minecraft.world.level.entity.PersistentEntitySectionManager<net.minecraft.world.entity.Entity>)
                managerField.get(player.level());
        BlockPos position = new BlockPos(80, 110, 80);
        manager.updateChunkStatus(net.minecraft.world.level.ChunkPos.containing(position),
                net.minecraft.world.level.entity.Visibility.TRACKED);
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "weather clear 1000");
        player.level().setBlock(position.above(4), Blocks.STONE.defaultBlockState(), 3);
        Method hit = ThrownTrident.class.getDeclaredMethod("onHitEntity", net.minecraft.world.phys.EntityHitResult.class);
        hit.setAccessible(true);
        var survivor = EntityTypes.COW.create(player.level(), EntitySpawnReason.COMMAND);
        survivor.setPos(80, 110, 80);
        player.level().addFreshEntity(survivor);
        int before = lightningCount(player);
        check(!player.level().canSeeSky(position), "channeling indoor fixture has blocked sky");
        hit.invoke(new ThrownTrident(player.level(), player, weapon), new net.minecraft.world.phys.EntityHitResult(survivor));
        check(survivor.isAlive() && lightningCount(player) == before + 1,
                "actual clear-weather indoor trident hit creates exactly one lightning");
        var lethal = EntityTypes.COW.create(player.level(), EntitySpawnReason.COMMAND);
        lethal.setPos(80, 110, 80);
        lethal.setHealth(1);
        player.level().addFreshEntity(lethal);
        hit.invoke(new ThrownTrident(player.level(), player, weapon), new net.minecraft.world.phys.EntityHitResult(lethal));
        check(!lethal.isAlive() && lightningCount(player) == before + 1,
                "actual lethal trident hit creates no lightning");
        var immune = EntityTypes.COW.create(player.level(), EntitySpawnReason.COMMAND);
        immune.setPos(80, 110, 80);
        player.level().addFreshEntity(immune);
        immune.hurtServer(player.level(), player.damageSources().generic(), 10);
        float health = immune.getHealth();
        hit.invoke(new ThrownTrident(player.level(), player, weapon), new net.minecraft.world.phys.EntityHitResult(immune));
        check(immune.getHealth() == health && lightningCount(player) == before + 1,
                "unaccepted trident damage creates no lightning");
    }

    private static int lightningCount(ServerPlayer player) {
        int count = 0;
        for (var entity : player.level().getAllEntities()) if (entity.getType() == EntityTypes.LIGHTNING_BOLT) count++;
        return count;
    }
}
