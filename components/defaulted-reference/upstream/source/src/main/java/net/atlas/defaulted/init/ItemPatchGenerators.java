package net.atlas.defaulted.init;

import com.mojang.serialization.MapCodec;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.generators.*;
import net.atlas.defaulted.init.registry.Bootstrapper;

public class ItemPatchGenerators extends Bootstrapper<MapCodec<? extends PatchGenerator>> {
    public static final ItemPatchGenerators INSTANCE = new ItemPatchGenerators();

    public ItemPatchGenerators() {
        super(Defaulted.PATCH_GENERATOR_TYPE, "defaulted");
    }
    @Override
    protected void bootstrap() {
        register("armor_stats", () -> ArmourStatsGenerator.CODEC);
        register("conditional", () -> ConditionalPatch.CODEC);
        register("modify_attribute_modifiers", () -> AttributeModifiersGenerator.CODEC);
        //? >=1.21.5
        register("modify_blocks_attacks", () -> BlocksAttacksGenerator.CODEC);
        register("modify_enchantments", () -> EnchantmentModifierGenerator.CODEC);
        register("modify_from_tool_material", () -> ModifyTierStatsGenerator.CODEC);
        register("modify_use_seconds", () -> EditUseDurationGenerator.CODEC);
        register("patch_phantom_components", () -> PhantomDataComponentPatchGenerator.CODEC);
        register("tool_material", () -> ChangeTierGenerator.CODEC);
        register("vanilla_weapon_stats", () -> WeaponStatsGenerator.CODEC);
    }
}
