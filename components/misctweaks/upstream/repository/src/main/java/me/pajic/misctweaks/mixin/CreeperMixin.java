package me.pajic.misctweaks.mixin;

import me.pajic.misctweaks.MiscTweaks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

//~ if <26.1 'gamerules.GameRules' -> 'GameRules'
import net.minecraft.world.level.gamerules.GameRules;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Monster {

    protected CreeperMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @SuppressWarnings("resource")
    @ModifyArg(
            method = "explodeCreeper",
            at = @At(
                    value = "INVOKE",
                    //? >=26.1
                    target = "Lnet/minecraft/server/level/ServerLevel;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)V"
                    //? <26.1
                    //target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;"
            ),
            index = 5
    )
    private Level.ExplosionInteraction creeperExplosionDropsAll(Level.ExplosionInteraction original) {
        //~ if <26.1 'get(GameRules.MOB_GRIEFING)' -> 'getBoolean(GameRules.RULE_MOBGRIEFING)'
        boolean mobGriefing = ((ServerLevel) level()).getGameRules().get(GameRules.MOB_GRIEFING);
        return MiscTweaks.CONFIG.creeperExplosionDropsAllItems.get() && mobGriefing ? Level.ExplosionInteraction.TNT : original;
    }
}
