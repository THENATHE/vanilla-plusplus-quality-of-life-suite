package net.atlas.defaulted.mixin;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
//? <26.1 {
/*import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.file.Path;
*///?}

@Mixin(StructureTemplateManager.class)
public interface StructureTemplateManagerAccessor {
    //? <26.1 {
    /*@Accessor
    Path getGeneratedDir();
    *///?}
}