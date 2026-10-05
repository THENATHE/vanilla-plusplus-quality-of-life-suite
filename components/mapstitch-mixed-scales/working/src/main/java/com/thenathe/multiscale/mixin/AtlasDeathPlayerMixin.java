package com.thenathe.multiscale.mixin;

import com.thenathe.multiscale.AtlasDeathRetention;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.ArrayList;
import java.util.List;

@Mixin(Player.class)
public abstract class AtlasDeathPlayerMixin implements AtlasDeathRetention.State {
    @Unique private final List<ItemStack> suite$retainedAtlases = new ArrayList<>();
    @Override public List<ItemStack> suite$retainedAtlases() { return suite$retainedAtlases; }
    @Inject(method = "dropEquipment", at = @At("HEAD"))
    private void suite$extractAtlases(ServerLevel level, CallbackInfo ci) {
        AtlasDeathRetention.beforeDrops((Player) (Object) this);
    }
    @Inject(method = "tick", at = @At("TAIL"))
    private void suite$returnOverflowAtlases(CallbackInfo ci) {
        AtlasDeathRetention.flush((Player) (Object) this);
    }
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void suite$saveOverflow(ValueOutput output, CallbackInfo ci) {
        if (!suite$retainedAtlases.isEmpty()) output.store(AtlasDeathRetention.SAVE_KEY, ItemStack.CODEC.listOf(), suite$retainedAtlases);
    }
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void suite$loadOverflow(ValueInput input, CallbackInfo ci) {
        suite$retainedAtlases.clear();
        input.read(AtlasDeathRetention.SAVE_KEY, ItemStack.CODEC.listOf()).ifPresent(items -> items.stream()
                .filter(stack -> !stack.isEmpty() && stack.is(me.pajic.mapstitch.item.ModItems.ATLAS)).forEach(suite$retainedAtlases::add));
    }
}
