import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import com.thenathe.suite.features.FeatureConfig;
import com.thenathe.suite.pets.PetFeatures;
import com.thenathe.suite.pets.PetHorn;
import com.thenathe.suite.pets.PetIndex;
import com.thenathe.suite.pets.PetKind;
import com.thenathe.suite.pets.SwiftFlight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/** Uses real world entities, component codecs, anvil-compatible enchantments and generated loot. */
public final class PetFeatureChecks {
    private static final BlockPos CENTER = new BlockPos(1024, 100, 1024);
    private static final BlockPos CHEST = CENTER.offset(12, 0, 0);
    private static final BlockPos UNLOADED = new BlockPos(5000, 100, 5000);
    private static final UUID SAVED_PET = UUID.fromString("f47e2bd9-f9c6-4f19-a453-fd0e62e02db0");
    private static ServerPlayer asyncPlayer;
    private static long asyncDeadline;
    private static int asyncTicks;
    private static java.util.Set<Long> originalForcedChunks;

    private PetFeatureChecks() {}

    public static void run(MinecraftServer server, ServerPlayer player) throws Exception {
        ServerLevel level = server.overworld();
        player.setPos(Vec3.atBottomCenterOf(CENTER));
        level.getChunkAt(CENTER);
        level.getChunkAt(CHEST);
        for (int x = -1; x <= 3; x++) for (int z = -1; z <= 3; z++) {
            track(level, CENTER.offset(x * 16, 0, z * 16));
        }
        for (int x = -10; x <= 10; x++) for (int z = -10; z <= 10; z++) {
            level.setBlock(CENTER.offset(x, -1, z), Blocks.STONE.defaultBlockState(), 3);
            for (int y = 0; y <= 7; y++) level.setBlock(CENTER.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
        }
        hornComponents(player);
        adapterRules(level, player);
        gestureTiming(level, player);
        nativeRelocation(level, player);
        shoulderParrot(server, player);
        crossDimensionAndLeashes(server, player);
        transferAndOwnership(server, player);
        swiftFlight(server);
        persistedFixture(server, player);
    }

    private static void hornComponents(ServerPlayer player) {
        ItemStack horn = new ItemStack(Items.GOAT_HORN);
        Component name = Component.literal("My original horn");
        horn.set(DataComponents.CUSTOM_NAME, name);
        horn.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
        var instrument = horn.get(DataComponents.INSTRUMENT);
        CompoundTag data = new CompoundTag();
        data.putString("audioplayer", "preserve-this-reference");
        data.putString("unrelated", "untouched");
        horn.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        PetHorn.bind(horn, SAVED_PET, player.getUUID(), PetKind.CAT, Component.literal("Companion"));
        check(PetHorn.binding(horn).pet().equals(SAVED_PET), "native horn UUID schema");
        check(horn.get(DataComponents.INSTRUMENT).equals(instrument), "horn instrument preserved when binding");
        check(horn.get(DataComponents.CUSTOM_DATA).copyTag().getStringOr("audioplayer", "").equals("preserve-this-reference"),
            "AudioPlayer reference preserved");
        check(PetHorn.reset(horn), "bound horn reset");
        check(horn.get(DataComponents.CUSTOM_NAME).equals(name), "original styled name restored");
        check(!horn.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE), "original glint restored");
        check(horn.get(DataComponents.CUSTOM_DATA).copyTag().equals(data), "unrelated custom data restored exactly");
        check(PetHorn.binding(horn) == null, "reset clears only suite binding");
        CompoundTag malformed = data.copy();
        CompoundTag broken = new CompoundTag();
        broken.putInt("version", 1);
        broken.putString("pet", "invalid-uuid");
        malformed.put(PetHorn.BINDING_KEY, broken);
        horn.set(DataComponents.CUSTOM_DATA, CustomData.of(malformed));
        check(PetHorn.binding(horn) == null, "malformed binding fails closed");
    }

    private static void adapterRules(ServerLevel level, ServerPlayer player) {
        String[] horses = {"horse", "donkey", "mule", "llama", "trader_llama", "skeleton_horse",
            "zombie_horse", "camel", "camel_husk"};
        for (String type : horses) {
            Entity horse = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace(type))
                .create(level, EntitySpawnReason.COMMAND);
            check(PetKind.of(horse) == PetKind.HORSE, "horse adapter " + type);
        }
        check(PetKind.of(EntityTypes.ZOMBIE_NAUTILUS.create(level, EntitySpawnReason.COMMAND)) == PetKind.NAUTILUS,
            "zombie nautilus adapter");
        var untamed = EntityTypes.WOLF.create(level, EntitySpawnReason.COMMAND);
        check(!PetFeatures.authorized(player, untamed, PetKind.DOG), "untamed animal cannot bind");
        untamed.setTame(true, false);
        untamed.setOwner(player);
        FeatureConfig.CONFIG.callDog.validateAndSet(false);
        check(!PetFeatures.authorized(player, untamed, PetKind.DOG), "disabled module refuses binding");
        FeatureConfig.CONFIG.callDog.validateAndSet(true);
    }

    private static void nativeRelocation(ServerLevel level, ServerPlayer player) {
        var types = List.of(EntityTypes.CAT, EntityTypes.WOLF, EntityTypes.PARROT, EntityTypes.HORSE,
            EntityTypes.HAPPY_GHAST, EntityTypes.NAUTILUS);
        for (var type : types) {
            Entity pet = type.create(level, EntitySpawnReason.COMMAND);
            pet.setPos(CENTER.getX() + 40, CENTER.getY(), CENTER.getZ());
            pet.setCustomName(Component.literal("Named " + BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath()));
            if (pet instanceof TamableAnimal animal) {
                animal.setTame(true, false);
                animal.setOwner(player);
            } else if (pet instanceof AbstractHorse horse) {
                horse.setTamed(true);
                horse.setOwner(player);
            }
            level.getChunkAt(pet.blockPosition());
            check(level.addFreshEntity(pet), "test pet added " + PetKind.of(pet));
            ItemStack horn = new ItemStack(Items.GOAT_HORN);
            check(PetFeatures.bind(player, pet, horn), "bind named owner-authorized " + PetKind.of(pet));
            UUID uuid = pet.getUUID();
            if (PetKind.of(pet) == PetKind.NAUTILUS) {
                check(PetFeatures.safeDestination(player, pet, PetKind.NAUTILUS) == null,
                    "nautilus refuses dry destination");
                for (int x = -5; x <= -1; x++) for (int z = -5; z <= -1; z++) for (int y = 0; y <= 3; y++) {
                    level.setBlock(CENTER.offset(x, y, z), Blocks.WATER.defaultBlockState(), 3);
                }
            }
            check(PetFeatures.summon(player, pet, PetHorn.binding(horn)), "native call " + PetKind.of(pet));
            Entity after = level.getEntityInAnyDimension(uuid);
            check(after == pet && after.getUUID().equals(uuid), "same-dimension call preserves original entity " + PetKind.of(pet));
            check(PetIndex.get(level.getServer()).get(uuid).dimension().equals(level.dimension().identifier()),
                "index tracks native call " + PetKind.of(pet));
            pet.discard();
        }
        var wolf = EntityTypes.WOLF.create(level, EntitySpawnReason.COMMAND);
        wolf.setTame(true, false);
        wolf.setOwner(player);
        wolf.setCustomName(Component.literal("Armored friend"));
        wolf.setPos(CENTER.getX() + 15, CENTER.getY(), CENTER.getZ());
        wolf.setItemSlot(EquipmentSlot.BODY, new ItemStack(Items.WOLF_ARMOR));
        level.addFreshEntity(wolf);
        var horn = new ItemStack(Items.GOAT_HORN);
        check(PetFeatures.bind(player, wolf, horn), "armored wolf binding");
        check(PetFeatures.summon(player, wolf, PetHorn.binding(horn)), "armored wolf relocation");
        check(wolf.getItemBySlot(EquipmentSlot.BODY).is(Items.WOLF_ARMOR), "body equipment retained");
        wolf.discard();
    }

    private static void gestureTiming(ServerLevel level, ServerPlayer player) throws Exception {
        var cat = EntityTypes.CAT.create(level, EntitySpawnReason.COMMAND);
        cat.setTame(true, false);
        cat.setOwner(player);
        cat.setCustomName(Component.literal("Gesture cat"));
        cat.setPos(CENTER.getX() + 3.5, CENTER.getY(), CENTER.getZ() + .5);
        level.addFreshEntity(cat);
        var horn = new ItemStack(Items.GOAT_HORN);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, horn);
        player.setShiftKeyDown(true);
        player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, cat.getEyePosition());
        var gesture = PetFeatures.class.getDeclaredMethod("tickGesture", ServerPlayer.class);
        gesture.setAccessible(true);
        for (int tick = 0; tick < 79; tick++) gesture.invoke(null, player);
        check(PetHorn.binding(horn) == null, "horn does not bind before four-second gesture");
        gesture.invoke(null, player);
        check(PetHorn.binding(horn) != null, "four-second crouch and look gesture binds horn");
        player.setXRot(-90);
        for (int tick = 0; tick < 39; tick++) gesture.invoke(null, player);
        check(PetHorn.binding(horn) != null, "reset waits full two-second gesture");
        gesture.invoke(null, player);
        check(PetHorn.binding(horn) == null, "crouch and look sky resets horn");
        player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, cat.getEyePosition());
        level.setBlock(CENTER.offset(1, 1, 0), Blocks.STONE.defaultBlockState(), 3);
        for (int tick = 0; tick < 80; tick++) gesture.invoke(null, player);
        check(PetHorn.binding(horn) == null, "solid wall blocks binding raycast");
        level.setBlock(CENTER.offset(1, 1, 0), Blocks.AIR.defaultBlockState(), 3);
        player.setShiftKeyDown(false);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        gesture.invoke(null, player);
        cat.discard();
    }

    private static void shoulderParrot(MinecraftServer server, ServerPlayer player) {
        var parrot = EntityTypes.PARROT.create(player.level(), EntitySpawnReason.COMMAND);
        parrot.setTame(true, false);
        parrot.setOwner(player);
        parrot.setCustomName(Component.literal("Shoulder friend"));
        parrot.setPos(player.position().add(1, 0, 0));
        player.level().addFreshEntity(parrot);
        var horn = new ItemStack(Items.GOAT_HORN);
        check(PetFeatures.bind(player, parrot, horn), "shoulder parrot horn binding");
        UUID uuid = parrot.getUUID();
        player.setOnGround(true);
        server.getPlayerList().getPlayers().add(player);
        try {
            check(parrot.setEntityOnShoulder(player), "native parrot shoulder storage");
            check(PetFeatures.requestCall(player, PetHorn.binding(horn)), "horn releases recently shouldered parrot");
            Entity after = player.level().getEntityInAnyDimension(uuid);
            check(after != null && after.getUUID().equals(uuid), "shoulder release retains pet UUID");
            check(player.getShoulderEntityLeft().isEmpty() && player.getShoulderEntityRight().isEmpty(),
                "shoulder record removed after successful native release");
            after.discard();
        } finally {
            server.getPlayerList().getPlayers().remove(player);
        }
    }

    private static void crossDimensionAndLeashes(MinecraftServer server, ServerPlayer player) {
        var wolf = EntityTypes.WOLF.create(player.level(), EntitySpawnReason.COMMAND);
        wolf.setTame(true, false);
        wolf.setOwner(player);
        wolf.setCustomName(Component.literal("Travel friend"));
        wolf.setPos(CENTER.getX() + 18, CENTER.getY(), CENTER.getZ());
        wolf.setItemSlot(EquipmentSlot.BODY, new ItemStack(Items.WOLF_ARMOR));
        player.level().addFreshEntity(wolf);
        var horn = new ItemStack(Items.GOAT_HORN);
        check(PetFeatures.bind(player, wolf, horn), "cross-dimension bound wolf");
        var nether = server.getLevel(net.minecraft.world.level.Level.NETHER);
        nether.getChunkAt(CENTER);
        try {
            track(nether, CENTER);
        } catch (Exception error) {
            throw new AssertionError(error);
        }
        UUID uuid = wolf.getUUID();
        Entity away = wolf.teleport(new net.minecraft.world.level.portal.TeleportTransition(nether,
            Vec3.atBottomCenterOf(CENTER), Vec3.ZERO, 0, 0,
            net.minecraft.world.level.portal.TeleportTransition.PLACE_PORTAL_TICKET));
        FeatureConfig.CONFIG.callCrossDimensions.validateAndSet(false);
        check(!PetFeatures.summon(player, away, PetHorn.binding(horn)), "disabled cross-dimension call leaves original pet in place");
        check(away.level() == nether, "refused cross-dimension call preserves location");
        FeatureConfig.CONFIG.callCrossDimensions.validateAndSet(true);
        check(PetFeatures.summon(player, away, PetHorn.binding(horn)), "cross-dimension native call");
        Entity back = player.level().getEntityInAnyDimension(uuid);
        check(back != null && back.level() == player.level() && back.getUUID().equals(uuid),
            "cross-dimension entity UUID preserved");
        check(((net.minecraft.world.entity.LivingEntity) back).getItemBySlot(EquipmentSlot.BODY).is(Items.WOLF_ARMOR),
            "cross-dimension body equipment retained");
        back.discard();

        var ghast = EntityTypes.HAPPY_GHAST.create(player.level(), EntitySpawnReason.COMMAND);
        ghast.setCustomName(Component.literal("Leashed transport"));
        ghast.setPos(CENTER.getX() + 18, CENTER.getY() + 5, CENTER.getZ());
        player.level().addFreshEntity(ghast);
        var pig = EntityTypes.PIG.create(player.level(), EntitySpawnReason.COMMAND);
        pig.setPos(ghast.position());
        player.level().addFreshEntity(pig);
        ((Leashable) pig).setLeashedTo(ghast, true);
        check(PetFeatures.bind(player, ghast, horn), "happy ghast horn binding");
        check(PetFeatures.summon(player, ghast, PetHorn.binding(horn)), "happy ghast attached leash group call");
        check(((Leashable) pig).getLeashHolder() == ghast && pig.distanceToSqr(ghast) < 10,
            "attached leash holder and entity preserved");
        pig.discard();
        ghast.discard();
    }

    private static void transferAndOwnership(MinecraftServer server, ServerPlayer player) {
        var cat = EntityTypes.CAT.create(player.level(), EntitySpawnReason.COMMAND);
        cat.setTame(true, false);
        cat.setOwner(player);
        cat.setCustomName(Component.literal("Gift cat"));
        cat.setPos(CENTER.getX() + 12, CENTER.getY(), CENTER.getZ() + 12);
        player.level().addFreshEntity(cat);
        var horn = new ItemStack(Items.GOAT_HORN);
        check(PetFeatures.bind(player, cat, horn), "pre-transfer horn binding");
        ItemStack book = new ItemStack(Items.WRITABLE_BOOK);
        book.set(DataComponents.WRITABLE_BOOK_CONTENT,
            new WritableBookContent(List.of(Filterable.passThrough("OfflinePlayer"))));
        check(!PetFeatures.transfer(player, cat, book), "offline recipient rejected");
        var recipient = new ServerPlayer(server, player.level(),
            new GameProfile(UUID.fromString("df7a52be-9d22-4250-ae5a-f176a002af51"), "PetRecipient"),
            net.minecraft.server.level.ClientInformation.createDefault());
        recipient.connection = player.connection;
        server.getPlayerList().getPlayers().add(recipient);
        try {
            book.set(DataComponents.WRITABLE_BOOK_CONTENT,
                new WritableBookContent(List.of(Filterable.passThrough("PetRecipient"))));
            check(PetFeatures.transfer(player, cat, book), "native book transfer to online recipient");
            check(cat.getOwnerReference().getUUID().equals(recipient.getUUID()), "native transfer preserves animal with new owner UUID");
            check(cat.getCustomName().getString().equals("Gift cat"), "transfer preserves animal name");
        } finally {
            server.getPlayerList().getPlayers().remove(recipient);
        }
        cat.setOwnerReference(net.minecraft.world.entity.EntityReference.of(UUID.randomUUID()));
        check(!PetFeatures.summon(player, cat, PetHorn.binding(horn)), "old owner's horn rejected after owner change");
        cat.discard();
    }

    private static void swiftFlight(MinecraftServer server) {
        var enchant = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
            .getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, SwiftFlight.ID));
        check(enchant.value().getMaxLevel() == 3, "Swift Flight original registry identity and three levels");
        var ghast = EntityTypes.HAPPY_GHAST.create(server.overworld(), EntitySpawnReason.COMMAND);
        ghast.setPos(Vec3.atBottomCenterOf(CENTER).add(20, 5, 20));
        double baseSpeed = ghast.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.FLYING_SPEED);
        try {
            var equipment = net.minecraft.world.entity.LivingEntity.class.getDeclaredMethod("detectEquipmentUpdates");
            equipment.setAccessible(true);
            for (int level = 1; level <= 3; level++) {
                ItemStack worn = new ItemStack(Items.HARNESS.blue());
                worn.enchant(enchant, level);
                ghast.setItemSlot(EquipmentSlot.BODY, worn);
                equipment.invoke(ghast);
                check(Math.abs(ghast.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.FLYING_SPEED)
                    - (baseSpeed + .01 * (level + 1))) < .000001,
                    "equipped happy ghast native flight attribute level " + level);
            }
            FeatureConfig.CONFIG.swiftFlight.validateAndSet(false);
            ghast.setItemSlot(EquipmentSlot.BODY, ItemStack.EMPTY);
            equipment.invoke(ghast);
            ItemStack disabled = new ItemStack(Items.HARNESS.blue());
            disabled.enchant(enchant, 3);
            ghast.setItemSlot(EquipmentSlot.BODY, disabled);
            equipment.invoke(ghast);
            check(Math.abs(ghast.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.FLYING_SPEED)
                - baseSpeed) < .000001, "disabled Swift Flight does not apply worn harness speed");
        } catch (ReflectiveOperationException error) {
            throw new AssertionError(error);
        } finally {
            FeatureConfig.CONFIG.swiftFlight.validateAndSet(true);
        }
        ItemStack harness = new ItemStack(Items.HARNESS.blue());
        harness.enchant(enchant, 3);
        double[] bonus = {0};
        EnchantmentHelper.forEachModifier(harness, EquipmentSlot.BODY, (attribute, modifier) -> bonus[0] += modifier.amount());
        check(Math.abs(bonus[0] - 0.04) < 0.000001, "Swift Flight III native attribute equals original effect");
        FeatureConfig.CONFIG.swiftFlight.validateAndSet(false);
        bonus[0] = 0;
        EnchantmentHelper.forEachModifier(harness, EquipmentSlot.BODY, (attribute, modifier) -> bonus[0] += modifier.amount());
        check(bonus[0] == 0, "Swift Flight disabled without deleting saved enchantment");
        check(EnchantmentHelper.getItemEnchantmentLevel(enchant, harness) == 3, "disabled Swift Flight harness level retained");
        FeatureConfig.CONFIG.swiftFlight.validateAndSet(true);
        var loot = server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,
            Identifier.withDefaultNamespace("chests/end_city_treasure")));
        var params = new LootParams.Builder(server.overworld()).withParameter(LootContextParams.ORIGIN,
            Vec3.atCenterOf(CENTER)).create(LootContextParamSets.CHEST);
        int found = 0;
        for (int seed = 1; seed <= 1000; seed++) {
            for (ItemStack drop : loot.getRandomItems(params, seed)) {
                if (!drop.is(Items.ENCHANTED_BOOK)) continue;
                int swift = EnchantmentHelper.getEnchantmentsForCrafting(drop).getLevel(enchant);
                if (swift > 0) {
                    check(swift <= 3, "End City Swift Flight book level " + swift);
                    found++;
                }
            }
        }
        check(found >= 40 && found <= 120, "End City 8 percent Swift Flight loot observed " + found + "/1000");
    }

    private static void persistedFixture(MinecraftServer server, ServerPlayer player) throws Exception {
        ServerLevel level = server.overworld();
        if (Files.exists(Path.of("features-initial-complete"))) {
            check(PetIndex.get(server).get(SAVED_PET) != null, "saved pet index read back after real restart");
            check(level.getBlockEntity(CHEST) instanceof ChestBlockEntity, "fixture chest read back after real restart");
            var chest = (ChestBlockEntity) level.getBlockEntity(CHEST);
            check(PetHorn.binding(chest.getItem(0)).pet().equals(SAVED_PET), "saved bound horn UUID read back after real restart");
            var enchant = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, SwiftFlight.ID));
            check(EnchantmentHelper.getItemEnchantmentLevel(enchant, chest.getItem(1)) == 3,
                "existing enchanted harness level read back after real restart");
            check(EnchantmentHelper.getEnchantmentsForCrafting(chest.getItem(2)).getLevel(enchant) == 2,
                "existing enchantment book read back after real restart");
            check(level.getEntityInAnyDimension(SAVED_PET) == null,
                "saved pet starts in genuinely unloaded distant chunk");
            originalForcedChunks = new java.util.HashSet<>(level.getForceLoadedChunks());
            server.getPlayerList().getPlayers().add(player);
            server.getPlayerList().getPlayersByUUID().put(player.getUUID(), player);
            check(PetFeatures.requestCall(player, PetHorn.binding(chest.getItem(0))),
                "unloaded pet queues native temporary chunk ticket");
            asyncPlayer = player;
            asyncDeadline = level.getGameTime() + 150;
            asyncTicks = 0;
            return;
        }
        var cat = EntityTypes.CAT.create(level, EntitySpawnReason.COMMAND);
        cat.setUUID(SAVED_PET);
        cat.setTame(true, false);
        cat.setOwner(player);
        cat.setCustomName(Component.literal("Persistent named cat"));
        cat.setPos(Vec3.atBottomCenterOf(UNLOADED));
        level.getChunkAt(cat.blockPosition());
        track(level, UNLOADED);
        level.setBlock(UNLOADED.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(UNLOADED, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(UNLOADED.above(), Blocks.AIR.defaultBlockState(), 3);
        level.addFreshEntity(cat);
        var horn = new ItemStack(Items.GOAT_HORN);
        check(PetFeatures.bind(player, cat, horn), "persistent fixture binds original entity");
        var index = PetIndex.get(server);
        var encoded = PetIndex.CODEC.encodeStart(JsonOps.INSTANCE, index).getOrThrow();
        check(PetIndex.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow().get(SAVED_PET).equals(index.get(SAVED_PET)),
            "dimension and chunk index codec roundtrip");
        level.setBlock(CHEST, Blocks.CHEST.defaultBlockState(), 3);
        var chest = (ChestBlockEntity) level.getBlockEntity(CHEST);
        chest.setItem(0, horn);
        var enchant = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
            .getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, SwiftFlight.ID));
        ItemStack harness = new ItemStack(Items.HARNESS.blue());
        harness.enchant(enchant, 3);
        chest.setItem(1, harness);
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        book.enchant(enchant, 2);
        chest.setItem(2, book);
        chest.setChanged();
        check(true, "bound horn harness and book staged for world save");
    }

    public static boolean tick(MinecraftServer server, ServerPlayer player) {
        if (asyncPlayer == null) return true;
        asyncTicks++;
        Entity pet = server.overworld().getEntityInAnyDimension(SAVED_PET);
        if (pet != null && pet.distanceToSqr(player) < 100) {
            if (asyncTicks < 4) return false;
            try {
                check(pet.getUUID().equals(SAVED_PET), "unloaded persisted pet restored with original UUID");
                check(pet.getCustomName().getString().equals("Persistent named cat"), "unloaded native call preserves saved animal name");
                check(((TamableAnimal) pet).getOwnerReference().getUUID().equals(player.getUUID()),
                    "unloaded native call preserves saved owner UUID");
                check(server.overworld().getForceLoadedChunks().equals(originalForcedChunks),
                    "pet call does not change persistent forced chunk set");
                check(PetIndex.get(server).get(SAVED_PET).chunk().equals(pet.chunkPosition()),
                    "unloaded call updates persisted chunk index");
            } finally {
                server.getPlayerList().getPlayers().remove(player);
                server.getPlayerList().getPlayersByUUID().remove(player.getUUID());
                asyncPlayer = null;
            }
            return true;
        }
        if (server.overworld().getGameTime() > asyncDeadline) {
            server.getPlayerList().getPlayers().remove(player);
            server.getPlayerList().getPlayersByUUID().remove(player.getUUID());
            asyncPlayer = null;
            throw new AssertionError("Unloaded pet did not arrive through the native temporary chunk ticket");
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static void track(ServerLevel level, BlockPos position) throws Exception {
        var field = ServerLevel.class.getDeclaredField("entityManager");
        field.setAccessible(true);
        var manager = (net.minecraft.world.level.entity.PersistentEntitySectionManager<Entity>) field.get(level);
        manager.updateChunkStatus(net.minecraft.world.level.ChunkPos.containing(position), net.minecraft.world.level.entity.Visibility.TRACKED);
    }

    private static void check(boolean passed, String name) {
        if (!passed) throw new AssertionError(name);
        System.out.println("PET_FEATURE_PASS " + name);
    }
}
