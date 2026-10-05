package me.pajic.toolpouch.mixin;

import me.pajic.toolpouch.util.PlayerExtension;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerExtensionDataMixin implements PlayerExtension {

    @Unique private int toolpouch$shulkerSlot;
    @Unique private int toolpouch$arrowSlot;

    @Override
    public int toolpouch$getShulkerSlot() {
        return toolpouch$shulkerSlot;
    }

    @Override
    public void toolpouch$setShulkerSlot(int value) {
        toolpouch$shulkerSlot = value;
    }

    @Override
    public int toolpouch$getArrowSlot() {
        return toolpouch$arrowSlot;
    }

    @Override
    public void toolpouch$setArrowSlot(int value) {
        toolpouch$arrowSlot = value;
    }

    @Inject(
            method = "addAdditionalSaveData",
            at = @At("TAIL")
    )
    private void addArrowSlot(ValueOutput output, CallbackInfo ci) {
        output.putInt("ShulkerSlot", toolpouch$shulkerSlot);
        output.putInt("ArrowSlot", toolpouch$arrowSlot);
    }

    @Inject(
            method = "readAdditionalSaveData",
            at = @At("TAIL")
    )
    private void readArrowSlot(ValueInput input, CallbackInfo ci) {
        toolpouch$shulkerSlot = input.getIntOr("ShulkerSlot", 0);
        toolpouch$arrowSlot = input.getIntOr("ArrowSlot", 0);
    }
}
