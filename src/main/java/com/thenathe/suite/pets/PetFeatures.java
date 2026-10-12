package com.thenathe.suite.pets;

import com.thenathe.suite.features.FeatureConfig;
import com.thenathe.suite.pets.mixin.ShoulderReleaseAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
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
import net.minecraft.server.level.TicketType;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Server-owned horn gestures and native entity relocation, with no commands or cloned pets. */
public final class PetFeatures {
    private static final int BIND_TICKS = 80;
    private static final int RESET_TICKS = 40;
    private static final int CALL_TIMEOUT = 100;
    private static final Map<UUID, Gesture> GESTURES = new HashMap<>();
    private static final Map<UUID, PendingCall> CALLS = new HashMap<>();
    private static final Map<TicketKey, Integer> TICKETS = new HashMap<>();
    private static TicketType callTicket;

    private record Gesture(UUID target, ItemStack held, boolean reset, int ticks) {}
    private record TicketKey(ServerLevel level, ChunkPos chunk) {}
    private record PendingCall(PetHorn.Binding binding, long expires, TicketKey ticket) {}
    private record LeashConnection(Entity animal, Entity holder) {}

    private PetFeatures() {}

    public static void initialize() {
        callTicket = Registry.register(BuiltInRegistries.TICKET_TYPE,
            Identifier.fromNamespaceAndPath("vpp_mod_suite", "pet_call"), new TicketType(200, 14));
        SwiftFlight.initialize();
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) ->
            PetIndex.get(level.getServer()).updateIfTracked(entity));
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) ->
            PetIndex.get(level.getServer()).updateIfTracked(entity));
        ServerTickEvents.END_SERVER_TICK.register(PetFeatures::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            GESTURES.clear();
            CALLS.clear();
            TICKETS.clear();
        });
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (!(player instanceof ServerPlayer serverPlayer) || player.isSpectator()) return InteractionResult.PASS;
            ItemStack horn = player.getItemInHand(hand);
            PetHorn.Binding binding = PetHorn.binding(horn);
            if (binding == null || !binding.kind().enabled() || player.getCooldowns().isOnCooldown(horn)) {
                return InteractionResult.PASS;
            }
            if (!canUseBinding(serverPlayer, binding)) {
                feedback(serverPlayer, "This horn belongs to another player.");
                return InteractionResult.FAIL;
            }
            requestCall(serverPlayer, binding);
            return InteractionResult.PASS;
        });
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) tickGesture(player);
        GESTURES.keySet().removeIf(uuid -> server.getPlayerList().getPlayer(uuid) == null);
        if (server.getTickCount() % 20 == 0) {
            PetIndex index = PetIndex.get(server);
            for (UUID uuid : index.trackedPets()) {
                Entity pet = server.overworld().getEntityInAnyDimension(uuid);
                if (pet != null) index.update(pet);
            }
        }
        for (UUID playerId : List.copyOf(CALLS.keySet())) {
            PendingCall pending = CALLS.get(playerId);
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            Entity pet = server.overworld().getEntityInAnyDimension(pending.binding().pet());
            if (player == null || !pending.binding().kind().enabled()) finishCall(playerId);
            else if (pet != null) {
                summon(player, pet, pending.binding());
                finishCall(playerId);
            } else if (server.overworld().getGameTime() >= pending.expires()) {
                feedback(player, "Your pet could not be found. It may be on an offline player's shoulder or no longer alive.");
                finishCall(playerId);
            }
        }
    }

    private static void tickGesture(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (!player.isShiftKeyDown() || player.isSpectator()
            || (!held.is(Items.GOAT_HORN) && !held.is(Items.WRITABLE_BOOK))) {
            GESTURES.remove(player.getUUID());
            return;
        }
        boolean reset = held.is(Items.GOAT_HORN) && PetHorn.binding(held) != null && player.getXRot() <= -85;
        Entity target = reset ? null : lookedAtPet(player);
        if (!reset && target == null) {
            GESTURES.remove(player.getUUID());
            return;
        }
        UUID targetId = reset ? null : target.getUUID();
        Gesture prior = GESTURES.get(player.getUUID());
        int ticks = prior != null && prior.held() == held && prior.reset() == reset
            && java.util.Objects.equals(prior.target(), targetId) ? prior.ticks() + 1 : 1;
        GESTURES.put(player.getUUID(), new Gesture(targetId, held, reset, ticks));
        int required = reset ? RESET_TICKS : held.is(Items.WRITABLE_BOOK) ? 20 : BIND_TICKS;
        if (ticks != required) return;
        if (reset) {
            PetHorn.reset(held);
            feedback(player, "Pet horn reset. Its original name and sound are preserved.");
        } else if (held.is(Items.WRITABLE_BOOK)) {
            if (FeatureConfig.CONFIG.transferPetOwner.get()) transfer(player, target, held);
        } else bind(player, target, held);
    }

    private static Entity lookedAtPet(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(10));
        Vec3 blockHit = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, player)).getLocation();
        double distance = eye.distanceToSqr(blockHit);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, end,
            player.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1),
            entity -> entity.isAlive() && PetKind.of(entity) != null, distance);
        return hit == null ? null : hit.getEntity();
    }

    public static boolean authorized(ServerPlayer player, Entity pet, PetKind kind) {
        if (kind == null || !kind.allows(pet)) return false;
        if (pet instanceof OwnableEntity owned && owned.getOwnerReference() != null) {
            return owned.getOwnerReference().getUUID().equals(player.getUUID());
        }
        String type = BuiltInRegistries.ENTITY_TYPE.getKey(pet.getType()).getPath();
        return kind == PetKind.HAPPY_GHAST || kind == PetKind.HORSE
            && (type.equals("camel") || type.equals("camel_husk")
                || type.equals("skeleton_horse") || type.equals("zombie_horse"));
    }

    public static boolean bind(ServerPlayer player, Entity pet, ItemStack horn) {
        PetKind kind = PetKind.of(pet);
        if (!horn.is(Items.GOAT_HORN) || !authorized(player, pet, kind)) {
            feedback(player, "You must own this pet to bind a horn.");
            return false;
        }
        if (!pet.hasCustomName()) {
            feedback(player, "Name your pet before binding a horn.");
            return false;
        }
        PetHorn.bind(horn, pet.getUUID(), player.getUUID(), kind, pet.getName());
        PetIndex.get(player.level().getServer()).update(pet);
        feedback(player, "Horn bound to " + pet.getName().getString() + ". Crouch and look straight up for two seconds to reset.");
        return true;
    }

    public static boolean transfer(ServerPlayer player, Entity pet, ItemStack book) {
        if (!FeatureConfig.CONFIG.transferPetOwner.get() || !book.is(Items.WRITABLE_BOOK)) return false;
        WritableBookContent content = book.get(DataComponents.WRITABLE_BOOK_CONTENT);
        String name = content == null ? "" : content.getPages(false).findFirst().orElse("").trim();
        ServerPlayer recipient = player.level().getServer().getPlayerList().getPlayerByName(name);
        if (recipient == null) {
            feedback(player, "Write the exact name of an online player on the first page.");
            return false;
        }
        if (!(pet instanceof OwnableEntity ownable) || ownable.getOwnerReference() == null
            || !ownable.getOwnerReference().getUUID().equals(player.getUUID())) {
            feedback(player, "You can only transfer a pet you own.");
            return false;
        }
        if (pet instanceof TamableAnimal animal) animal.setOwner(recipient);
        else if (pet instanceof AbstractHorse horse) horse.setOwner(recipient);
        else return false;
        feedback(player, "Transferred " + pet.getName().getString() + " to " + recipient.getName().getString() + ".");
        feedback(recipient, "You now own " + pet.getName().getString() + ".");
        return true;
    }

    public static boolean requestCall(ServerPlayer player, PetHorn.Binding binding) {
        if (!binding.kind().enabled() || !canUseBinding(player, binding)
            || CALLS.containsKey(player.getUUID())) return false;
        if (binding.kind() == PetKind.PARROT) releaseShoulderedParrot(player, binding.pet());
        Entity pet = player.level().getServer().overworld().getEntityInAnyDimension(binding.pet());
        if (pet != null) return summon(player, pet, binding);
        PetIndex.Location location = PetIndex.get(player.level().getServer()).get(binding.pet());
        if (location == null) {
            feedback(player, "This pet has no location record. Bind the horn again while looking at it.");
            return false;
        }
        ServerLevel source = player.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, location.dimension()));
        if (source == null || !FeatureConfig.CONFIG.callCrossDimensions.get() && source != player.level()) {
            feedback(player, "This pet is in another dimension.");
            return false;
        }
        TicketKey ticket = new TicketKey(source, location.chunk());
        TICKETS.merge(ticket, 1, Integer::sum);
        source.getChunkSource().addTicketWithRadius(callTicket, ticket.chunk(), 2);
        CALLS.put(player.getUUID(), new PendingCall(binding,
            player.level().getServer().overworld().getGameTime() + CALL_TIMEOUT, ticket));
        feedback(player, "Calling your pet...");
        return true;
    }

    private static void finishCall(UUID playerId) {
        PendingCall pending = CALLS.remove(playerId);
        if (pending == null) return;
        TicketKey ticket = pending.ticket();
        int remaining = TICKETS.getOrDefault(ticket, 1) - 1;
        if (remaining <= 0) {
            TICKETS.remove(ticket);
            ticket.level().getChunkSource().removeTicketWithRadius(callTicket, ticket.chunk(), 2);
        } else TICKETS.put(ticket, remaining);
    }

    private static void releaseShoulderedParrot(ServerPlayer caller, UUID pet) {
        MinecraftServer server = caller.level().getServer();
        for (ServerPlayer owner : server.getPlayerList().getPlayers()) {
            boolean left = shoulderMatches(owner.getShoulderEntityLeft(), pet);
            boolean right = shoulderMatches(owner.getShoulderEntityRight(), pet);
            if (left || right) {
                if (!owner.getUUID().equals(caller.getUUID())) return;
                CompoundTag saved = left ? owner.getShoulderEntityLeft() : owner.getShoulderEntityRight();
                if (!saved.read("Owner", net.minecraft.core.UUIDUtil.CODEC).filter(caller.getUUID()::equals).isPresent()) return;
                var shoulders = (ShoulderReleaseAccessor) owner;
                shoulders.vpp$releaseShoulderEntity(saved);
                if (server.overworld().getEntityInAnyDimension(pet) != null) {
                    if (left) shoulders.vpp$setLeftShoulder(new CompoundTag());
                    else shoulders.vpp$setRightShoulder(new CompoundTag());
                }
                return;
            }
        }
    }

    private static boolean shoulderMatches(CompoundTag data, UUID uuid) {
        return data.read("UUID", net.minecraft.core.UUIDUtil.CODEC).filter(uuid::equals).isPresent();
    }

    public static boolean summon(ServerPlayer player, Entity pet, PetHorn.Binding binding) {
        PetKind kind = PetKind.of(pet);
        if (!binding.pet().equals(pet.getUUID()) || !canUseBinding(player, binding)
            || binding.kind() != kind || !authorized(player, pet, kind)) {
            feedback(player, "This horn no longer belongs to the owner of that pet.");
            return false;
        }
        if (!FeatureConfig.CONFIG.callCrossDimensions.get() && pet.level() != player.level()) {
            feedback(player, "Calling pets across dimensions is disabled.");
            return false;
        }
        boolean occupiedAllowed = kind == PetKind.HAPPY_GHAST ? FeatureConfig.CONFIG.callOccupiedHappyGhasts.get()
            : FeatureConfig.CONFIG.callMountedPets.get();
        if (!occupiedAllowed && pet.countPlayerPassengers() > 0) {
            feedback(player, "Someone is riding this pet.");
            return false;
        }
        List<LeashConnection> leashes = new ArrayList<>();
        if (!collectLeashes(pet, player, leashes, 0)) {
            feedback(player, "The attached leash group cannot be moved safely. Release its leash first.");
            return false;
        }
        Vec3 destination = safeDestination(player, pet, kind, leashes);
        if (destination == null) {
            feedback(player, kind == PetKind.NAUTILUS ? "There is no safe water nearby for your nautilus."
                : "There is no safe space nearby for your pet and its passengers or leash group.");
            return false;
        }
        if (!FeatureConfig.CONFIG.preservePetLeashes.get()) {
            for (LeashConnection leash : leashes) ((Leashable) leash.animal()).dropLeash();
            leashes.clear();
        }
        // Detached connections are rebuilt using the actual post-teleport entities, including cross-dimension copies.
        for (LeashConnection leash : leashes) ((Leashable) leash.animal()).removeLeash();
        Map<UUID, Entity> moved = new HashMap<>();
        Entity relocated = pet.teleport(new TeleportTransition(player.level(), destination, Vec3.ZERO,
            pet.getYRot(), pet.getXRot(), TeleportTransition.PLACE_PORTAL_TICKET));
        if (relocated == null) {
            for (LeashConnection leash : leashes) ((Leashable) leash.animal()).setLeashedTo(leash.holder(), true);
            feedback(player, "Your pet could not be moved.");
            return false;
        }
        relocated.getSelfAndPassengers().forEach(entity -> moved.put(entity.getUUID(), entity));
        for (LeashConnection leash : leashes) {
            Entity child = moved.get(leash.animal().getUUID());
            if (child == null) {
                child = leash.animal().teleport(new TeleportTransition(player.level(), destination, Vec3.ZERO,
                    leash.animal().getYRot(), leash.animal().getXRot(), TeleportTransition.PLACE_PORTAL_TICKET));
                if (child != null) child.getSelfAndPassengers().forEach(entity -> moved.put(entity.getUUID(), entity));
            }
            Entity holder = moved.getOrDefault(leash.holder().getUUID(), leash.holder());
            if (child instanceof Leashable leashable && holder.level() == child.level()) leashable.setLeashedTo(holder, true);
        }
        PetIndex.get(player.level().getServer()).update(relocated);
        if (FeatureConfig.CONFIG.mountCalledPets.get() && (kind == PetKind.HORSE || kind == PetKind.NAUTILUS
            || kind == PetKind.HAPPY_GHAST)) player.startRiding(relocated, false, false);
        feedback(player, "Called " + relocated.getName().getString() + ".");
        return true;
    }

    private static boolean collectLeashes(Entity holder, ServerPlayer player, List<LeashConnection> result, int depth) {
        if (depth > 8 || result.size() > 32) return false;
        if (holder instanceof Leashable leashable && leashable.isLeashed()) {
            Entity external = leashable.getLeashHolder();
            if (FeatureConfig.CONFIG.preservePetLeashes.get() && external != player && result.stream().noneMatch(connection -> connection.holder() == external
                || connection.animal() == external)) return false;
            if (depth == 0 && external != null) result.add(new LeashConnection(holder, external));
        }
        for (Leashable child : Leashable.leashableLeashedTo(holder)) {
            if (!(child instanceof Entity animal)) continue;
            if (result.stream().anyMatch(connection -> connection.animal() == animal)) continue;
            if (!FeatureConfig.CONFIG.callMountedPets.get() && animal.countPlayerPassengers() > 0) return false;
            result.add(new LeashConnection(animal, holder));
            if (!collectLeashes(animal, player, result, depth + 1)) return false;
        }
        return true;
    }

    public static Vec3 safeDestination(ServerPlayer player, Entity pet, PetKind kind) {
        List<LeashConnection> leashes = new ArrayList<>();
        if (!collectLeashes(pet, player, leashes, 0)) return null;
        return safeDestination(player, pet, kind, leashes);
    }

    private static Vec3 safeDestination(ServerPlayer player, Entity pet, PetKind kind,
                                        List<LeashConnection> leashes) {
        ServerLevel level = player.level();
        BlockPos center = player.blockPosition();
        for (int radius = 1; radius <= 6; radius++) {
            for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
                if (Math.abs(dx) != radius && Math.abs(dz) != radius) continue;
                for (int dy = kind.flies() ? 1 : -2; dy <= 3; dy++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!level.getWorldBorder().isWithinBounds(pos) || !level.isInWorldBounds(pos)) continue;
                    Vec3 next = Vec3.atBottomCenterOf(pos);
                    if (!safeGroupAt(level, pet, next)) continue;
                    if (FeatureConfig.CONFIG.preservePetLeashes.get() && leashes.stream().anyMatch(
                        leash -> !safeGroupAt(level, leash.animal(), next))) continue;
                    if (FeatureConfig.CONFIG.mountCalledPets.get() && (kind == PetKind.HORSE
                        || kind == PetKind.NAUTILUS || kind == PetKind.HAPPY_GHAST)
                        && !safeEntityAt(level, player, passengerDestination(pet, player, next))) continue;
                    if (kind == PetKind.NAUTILUS && FeatureConfig.CONFIG.nautilusRequiresWater.get()) {
                        if (!level.getFluidState(pos).is(FluidTags.WATER)
                            || !level.getFluidState(pos.above()).is(FluidTags.WATER)) continue;
                    } else if (!kind.flies() && !level.getBlockState(pos.below()).isFaceSturdy(level,
                        pos.below(), net.minecraft.core.Direction.UP)) continue;
                    return next;
                }
            }
        }
        return null;
    }

    private static boolean safeGroupAt(ServerLevel level, Entity entity, Vec3 destination) {
        if (!safeEntityAt(level, entity, destination)) return false;
        for (Entity passenger : entity.getPassengers()) {
            if (!safeGroupAt(level, passenger, passengerDestination(entity, passenger, destination))) return false;
        }
        return true;
    }

    private static Vec3 passengerDestination(Entity vehicle, Entity passenger, Vec3 destination) {
        // Match vanilla's riding attachments instead of trusting a passenger's possibly stale position.
        return vehicle.getPassengerRidingPosition(passenger).subtract(vehicle.position())
            .subtract(passenger.getVehicleAttachmentPoint(vehicle)).add(destination);
    }

    private static boolean safeEntityAt(ServerLevel level, Entity entity, Vec3 destination) {
        AABB box = entity.getBoundingBox().move(destination.subtract(entity.position()));
        BlockPos minimum = BlockPos.containing(box.minX + 1.0E-7, box.minY, box.minZ + 1.0E-7);
        BlockPos maximum = BlockPos.containing(box.maxX - 1.0E-7, box.maxY - 1.0E-7, box.maxZ - 1.0E-7);
        if (!level.isInWorldBounds(minimum) || !level.isInWorldBounds(maximum)
            || !level.getWorldBorder().isWithinBounds(minimum) || !level.getWorldBorder().isWithinBounds(maximum)
            || !level.noCollision(entity, box)) return false;
        // Include the supporting surface and every occupied block, including non-solid fire and fluids.
        BlockPos support = BlockPos.containing(box.minX + 1.0E-7, box.minY - .01, box.minZ + 1.0E-7);
        for (BlockPos pos : BlockPos.betweenClosed(support, maximum)) {
            var state = level.getBlockState(pos);
            if (level.getFluidState(pos).is(FluidTags.LAVA) || state.is(BlockTags.FIRE)
                || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)
                || state.is(Blocks.CACTUS) || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.WITHER_ROSE)) {
                return false;
            }
        }
        return true;
    }

    private static void feedback(ServerPlayer player, String message) {
        player.sendOverlayMessage(Component.literal(message));
    }

    private static boolean canUseBinding(ServerPlayer player, PetHorn.Binding binding) {
        return binding.owner().equals(player.getUUID()) || FeatureConfig.CONFIG.callUnownedHornsShared.get()
            && (binding.kind() == PetKind.HORSE || binding.kind() == PetKind.HAPPY_GHAST);
    }
}
