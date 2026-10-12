package com.thenathe.suite.pets;

import com.thenathe.suite.features.FeatureConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;

/** The adapters deliberately share storage, binding and relocation behavior. */
public enum PetKind {
    PARROT, NAUTILUS, HORSE, HAPPY_GHAST, DOG, CAT;

    public static PetKind of(Entity entity) {
        String type = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
        return switch (type) {
            case "parrot" -> PARROT;
            case "nautilus", "zombie_nautilus" -> NAUTILUS;
            case "horse", "donkey", "mule", "llama", "trader_llama", "skeleton_horse",
                 "zombie_horse", "camel", "camel_husk" -> HORSE;
            case "happy_ghast" -> HAPPY_GHAST;
            case "wolf" -> DOG;
            case "cat" -> CAT;
            default -> null;
        };
    }

    public boolean enabled() {
        return switch (this) {
            case PARROT -> FeatureConfig.CONFIG.callParrot.get();
            case NAUTILUS -> FeatureConfig.CONFIG.callNautilus.get();
            case HORSE -> FeatureConfig.CONFIG.callHorse.get();
            case HAPPY_GHAST -> FeatureConfig.CONFIG.callHappyGhast.get();
            case DOG -> FeatureConfig.CONFIG.callDog.get();
            case CAT -> FeatureConfig.CONFIG.callCat.get();
        };
    }

    public boolean flies() {
        return this == PARROT || this == HAPPY_GHAST;
    }

    public boolean allows(Entity entity) {
        if (!enabled()) return false;
        if (this != HORSE && this != NAUTILUS) return true;
        String allowed = this == HORSE ? FeatureConfig.CONFIG.callHorseTypes.get()
            : FeatureConfig.CONFIG.callNautilusTypes.get();
        String type = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
        for (String entry : allowed.split(",")) {
            if (entry.trim().replace("minecraft:", "").equals(type)) return true;
        }
        return false;
    }
}
