package com.thenathe.multiscale.mixin;

import com.thenathe.multiscale.AtlasCartographyCopy;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CartographyTableMenu.class)
public abstract class AtlasCartographyMenuMixin extends AbstractContainerMenu {
    protected AtlasCartographyMenuMixin(MenuType<?> type, int id) { super(type, id); }
    @Shadow @Final private ResultContainer resultContainer;

    @Inject(method = "setupResultSlot", at = @At("HEAD"), cancellable = true)
    private void atlasCopy$preview(ItemStack source, ItemStack addition, ItemStack previous, CallbackInfo ci) {
        // Two atlases are MapStitch's own copy/merge recipe; let its result handling run.
        if (!source.is(ModItems.ATLAS) || addition.is(ModItems.ATLAS)) return;
        var result = AtlasCartographyCopy.matches(source, addition)
                ? AtlasCartographyCopy.preview(source, previous) : ItemStack.EMPTY;
        if (!ItemStack.matches(result, previous)) {
            resultContainer.setItem(2, result);
            ((CartographyTableMenu) (Object) this).broadcastChanges();
        }
        ci.cancel();
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void atlasCopy$shiftInputs(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        var menu = (CartographyTableMenu) (Object) this;
        if (index < 3 || index >= menu.slots.size()) return;
        var slot = menu.getSlot(index); var stack = slot.getItem();
        // MapStitch routes atlases itself, including a second atlas into the lower slot.
        if (!stack.is(Items.BOOK)) return;
        int target = 1;
        var before = stack.copy();
        if (!moveItemStackTo(stack, target, target + 1, false) || stack.getCount() == before.getCount()) {
            cir.setReturnValue(ItemStack.EMPTY); return;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        slot.setChanged(); slot.onTake(player, stack); menu.broadcastChanges();
        cir.setReturnValue(before);
    }
}
