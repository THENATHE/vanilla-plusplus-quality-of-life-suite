package me.pajic.simple_death_improvements.util;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.pajic.simple_death_improvements.SDI;
import me.pajic.simple_death_improvements.access.PlayerAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

//~ if <26.1 'gamerules.GameRules' -> 'GameRules'
import net.minecraft.world.level.gamerules.GameRules;

public class ModUtil {

	@SuppressWarnings("unchecked")
	public static final EntityType<ExperienceOrb> XP_ORB = (EntityType<ExperienceOrb>) BuiltInRegistries.ENTITY_TYPE
            //~ if <26.1 'getValueOrThrow' -> 'getOrThrow'
			.getValueOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("experience_orb")));

	public static ItemEntity trySaveItemsOnDeath(
			Player player,
			Level level,
			double posX,
			double posY,
			double posZ,
			ItemStack itemStack,
			Operation<ItemEntity> original
	) {
		if (player.isDeadOrDying()) {
			Vec3 dropPos = ((PlayerAccess) player).sdi$getDeathDropPos();
			if (dropPos != null) return original.call(level, dropPos.x, dropPos.y, dropPos.z, itemStack);
		}
		return original.call(level, posX, posY, posZ, itemStack);
	}

	public static @Nullable BlockPos getLastSafePos(Player player) {
		GlobalPos safePos = ((PlayerAccess) player).sdi$getLastSafePos();
		if (safePos == null || !safePos.dimension().equals(player.level().dimension())) return null;
		return safePos.pos();
	}

	public static @Nullable Vec3 resolveDeathDropPos(Player player) {
		BlockPos lastSafePos = getLastSafePos(player);
		if (lastSafePos == null) return null;
		Level level = player.level();
		if (player.getBlockStateOn().is(BlockTags.AIR)) {
			SDI.debugLog("Player {} died while airborne", player.getName().getString());
			boolean isSafeBelow = false;
			BlockPos onPos = player.getOnPos();
			int x = onPos.getX();
			int z = onPos.getZ();
            //~ if <26.1 'getMinY' -> 'getMinBuildHeight'
			for (int y = onPos.getY() - 1; y >= level.getMinY(); y--) {
				BlockPos pos = new BlockPos(x, y, z);
				BlockState state = level.getBlockState(pos);
				if (SDI.CONFIG.tryItemLavaSaveOnDeath.get() && state.is(Blocks.LAVA) && !player.isInLava()) {
					SDI.debugLog(
							"Block below player at {} {} {} is lava",
							pos.getX(), pos.getY(), pos.getZ()
					);
					return relocate(player, lastSafePos, ItemSaveReason.LAVA_AIR);
				}
				if (state.isFaceSturdy(level, pos, Direction.UP)) {
					SDI.debugLog(
							"Block {} below player at {} {} {} is safe",
							state.getBlock().getName().getString(),
							pos.getX(), pos.getY(), pos.getZ()
					);
					isSafeBelow = true;
					break;
				}
			}
			if (SDI.CONFIG.tryItemVoidSaveOnDeath.get() && !isSafeBelow) {
				SDI.debugLog("Didn't find safe block below player");
				return relocate(player, lastSafePos, ItemSaveReason.VOID_AIR);
			}
		}
		if (SDI.CONFIG.tryItemLavaSaveOnDeath.get() && player.isInLava()) {
			return relocate(player, lastSafePos, ItemSaveReason.LAVA);
		}
        //~ if <26.1 'getMinY' -> 'getMinBuildHeight'
		if (SDI.CONFIG.tryItemVoidSaveOnDeath.get() && player.getY() < (double) (level.getMinY() - 64)) {
			return relocate(player, lastSafePos, ItemSaveReason.VOID);
		}
		return null;
	}

	private static Vec3 relocate(Player player, BlockPos lastSafePos, ItemSaveReason reason) {
		SDI.debugLog(
				"Dropping death loot for player {} at {} {} {}. Reason: {}",
				player.getDisplayName().getString(),
				lastSafePos.getX(), lastSafePos.getY(), lastSafePos.getZ(),
				reason.toString()
		);
		return new Vec3(lastSafePos.getX() + 0.5, lastSafePos.getY() + 1, lastSafePos.getZ() + 0.5);
	}

	public static boolean tryKeepDamageableItem(LivingEntity entity, ItemStack stack) {
		if (
				!SDI.CONFIG.keepDamageableItemsOnDeath.get() ||
				!(entity instanceof Player player) ||
				!player.isDeadOrDying() ||
				!(player.level() instanceof ServerLevel level) ||
                //~ if <26.1 'get(GameRules.KEEP_INVENTORY)' -> 'getBoolean(GameRules.RULE_KEEPINVENTORY)'
				level.getGameRules().get(GameRules.KEEP_INVENTORY) ||
				!stack.isDamageableItem() ||
				EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP) ||
				SDI.CONFIG.damageableItemDropList.contains(SDI.getItemId(stack))
		) return false;
		if (!((PlayerAccess) player).sdi$markKeptDamagedStack(stack)) return true;
		int maxDamage = stack.getMaxDamage();
		int durabilityLoss = (int) Math.ceil(maxDamage * SDI.CONFIG.keptItemDurabilityLossPercent.get() / 100.0);
		stack.setDamageValue(Math.min(stack.getDamageValue() + durabilityLoss, maxDamage - 1));
		return true;
	}
}
