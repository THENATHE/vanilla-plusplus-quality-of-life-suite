package me.pajic.misctweaks.mixin;

import me.pajic.misctweaks.MiscTweaks;
import me.pajic.misctweaks.ai.AnimalConfig;
import me.pajic.misctweaks.ai.SearchForFoodGoal;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public abstract class AnimalMixin extends Mob {

    protected AnimalMixin(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void addSearchForFoodGoal(EntityType<? extends Animal> type, Level level, CallbackInfo ci) {
        if (MiscTweaks.CONFIG.animalsSearchForFood.get()) {
            //~ if <26.1 'lookupOrThrow' -> 'registryOrThrow'
			Identifier id = level.registryAccess().lookupOrThrow(Registries.ENTITY_TYPE).getKey(type);
            if (id != null) {
                AnimalConfig config = new AnimalConfig();
                for (AnimalConfig conf : MiscTweaks.CONFIG.animalConfiguration.get()) {
                    if (conf.id.get().equals(id)) {
                        config = conf;
                        break;
                    }
                }
                if (config.doSearch.get()) {
                    goalSelector.addGoal(
                            config.searchGoalPriority.get(),
                            new SearchForFoodGoal<>((Animal) (Object) this, config.searchSpeed.get(), config.searchRange.get())
                    );
                }
            }
        }
    }
}
