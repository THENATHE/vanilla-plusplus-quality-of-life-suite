package me.pajic.simple_death_improvements.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.pajic.simple_death_improvements.SDI;
import me.pajic.simple_death_improvements.access.PlayerAccess;
import me.pajic.simple_death_improvements.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

//~ if <26.1 'gamerules.GameRules' -> 'GameRules'
import net.minecraft.world.level.gamerules.GameRules;

//? >=26.1 {
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?} else {
/*import me.pajic.simple_death_improvements.access.ItemEntityAccess;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
*///?}

@Mixin(Player.class)
public abstract class PlayerMixin implements PlayerAccess {

    @Shadow public abstract Component getDisplayName();

    @Unique Player sdi$self = (Player) (Object) this;
    @Unique @Nullable GlobalPos sdi$lastSafePos = null;
    @Unique boolean sdi$deathDropPosResolved = false;
    @Unique @Nullable Vec3 sdi$deathDropPos = null;
    @Unique final Set<ItemStack> sdi$keptDamagedStacks = Collections.newSetFromMap(new IdentityHashMap<>());
    @Unique boolean sdi$startedTrackingSafePos = false;

    //? <26.1 {
    /*@Inject(
            method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/item/ItemEntity;setPickUpDelay(I)V"
            )
    )
    private void preventItemDespawnOnDeath(
            ItemStack droppedItem,
            boolean dropAround,
            boolean includeThrowerName,
            CallbackInfoReturnable<ItemEntity> cir,
            @Local ItemEntity itemEntity
    ) {
        if (sdi$self.isDeadOrDying()) {
            if (SDI.CONFIG.noDeathItemDespawn.get()) {
                itemEntity.setUnlimitedLifetime();
                SDI.debugLog("Set infinite lifetime to items dropped by player {}", sdi$self.getDisplayName().getString());
            } else ((ItemEntityAccess) itemEntity).sdi$setLifetime(SDI.CONFIG.itemDespawnTimer.get() * 20);
        }
    }

    @ModifyArgs(
            method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/item/ItemEntity;setDeltaMovement(DDD)V"
            )
    )
    private void preventItemThrowOnDeath(Args args) {
        if (SDI.CONFIG.noItemSplatterOnDeath.get() && sdi$self.isDeadOrDying()) {
            args.setAll(0.0d, 0.0d, 0.0d);
            SDI.debugLog("Prevented item splatter for player {}", sdi$self.getDisplayName().getString());
        }
    }

    @WrapOperation(
            method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;"
            )
    )
    private ItemEntity trySaveItemsOnDeath(Level level, double posX, double posY, double posZ, ItemStack itemStack, Operation<ItemEntity> original) {
		return ModUtil.trySaveItemsOnDeath(sdi$self, level, posX, posY, posZ, itemStack, original);
    }
    *///?}

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void trackLastSafeSpot(CallbackInfo ci) {
        if (!sdi$self.isDeadOrDying()) {
            sdi$deathDropPosResolved = false;
            sdi$deathDropPos = null;
            if (!sdi$keptDamagedStacks.isEmpty()) sdi$keptDamagedStacks.clear();
        }
        if (sdi$self.onGround() && !sdi$self.isSpectator() && !sdi$self.isInLava()) {
            BlockPos pos = sdi$self.getOnPos();
            if (sdi$self.getBlockStateOn().entityCanStandOn(sdi$self.level(), pos, sdi$self)) {
                if (!sdi$startedTrackingSafePos) {
                    sdi$startedTrackingSafePos = true;
                    SDI.debugLog("Started tracking last safe position for player {}", sdi$self.getDisplayName().getString());
                }
                sdi$lastSafePos = GlobalPos.of(sdi$self.level().dimension(), pos);
            }
        }
    }

    @WrapMethod(method = "getBaseExperienceReward")
    private int modifyDroppedXpOnDeath(/*? >=26.1 {*/ServerLevel level, /*?}*/Operation<Integer> original) {
        //~ if <26.1 'get(GameRules.KEEP_INVENTORY)' -> 'getBoolean(GameRules.RULE_KEEPINVENTORY)'
        //~ if <26.1 'level' -> 'sdi$self.level()'
        if (SDI.CONFIG.playerDropMoreXpOnDeath.get() && !sdi$self.isSpectator() && !level.getGameRules().get(GameRules.KEEP_INVENTORY)) {
            int xp = 0;
            int xpLevel = sdi$self.experienceLevel;
            for (int i = 0; i < xpLevel; i++) {
                sdi$self.experienceLevel = i;
                xp += sdi$self.getXpNeededForNextLevel();
            }
            sdi$self.experienceLevel = xpLevel;
            xp += (int) (sdi$self.experienceProgress * sdi$self.getXpNeededForNextLevel());
            return (int) (xp * (float) SDI.CONFIG.droppedExperiencePercent.get() / 100);
        }
        return original.call(/*? >=26.1 {*/level/*?}*/);
    }

    //? if 1.21.1 {
    /*@Inject(
            method = "addAdditionalSaveData",
            at = @At("TAIL")
    )
    private void saveLastSafeSpot(CompoundTag compound, CallbackInfo ci) {
        if (sdi$lastSafePos != null) GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, sdi$lastSafePos).result().ifPresent(tag -> compound.put("LastSafePos", tag));
        SDI.debugLog("Saved safe position {} for player {}", sdi$lastSafePos, getDisplayName().getString());
    }

    @Inject(
            method = "readAdditionalSaveData",
            at = @At("TAIL")
    )
    private void loadLastSafeSpot(CompoundTag compound, CallbackInfo ci) {
        sdi$lastSafePos = compound.contains("LastSafePos") ? GlobalPos.CODEC.parse(NbtOps.INSTANCE, compound.get("LastSafePos")).result().orElse(null) : null;
        SDI.debugLog("Loaded safe position {} for player {}", sdi$lastSafePos, getDisplayName().getString());
    }
    *///?} else {
    @Inject(
            method = "addAdditionalSaveData",
            at = @At("TAIL")
    )
    private void saveLastSafeSpot(ValueOutput output, CallbackInfo ci) {
        output.storeNullable("LastSafePos", GlobalPos.CODEC, sdi$lastSafePos);
        SDI.debugLog("Saved safe position {} for player {}", sdi$lastSafePos, getDisplayName().getString());
    }

    @Inject(
            method = "readAdditionalSaveData",
            at = @At("TAIL")
    )
    private void loadLastSafeSpot(ValueInput input, CallbackInfo ci) {
        sdi$lastSafePos = input.read("LastSafePos", GlobalPos.CODEC).orElse(null);
        SDI.debugLog("Loaded safe position {} for player {}", sdi$lastSafePos, getDisplayName().getString());
    }
    //?}

    @Override
    public @Nullable GlobalPos sdi$getLastSafePos() {
        return sdi$lastSafePos;
    }

    @Override
    public @Nullable Vec3 sdi$getDeathDropPos() {
        if (!sdi$deathDropPosResolved) {
            sdi$deathDropPos = ModUtil.resolveDeathDropPos(sdi$self);
            sdi$deathDropPosResolved = true;
        }
        return sdi$deathDropPos;
    }

    @Override
    public boolean sdi$markKeptDamagedStack(ItemStack stack) {
        return sdi$keptDamagedStacks.add(stack);
    }
}
