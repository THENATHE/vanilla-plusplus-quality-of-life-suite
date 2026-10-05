package me.pajic.toolpouch.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @WrapMethod(method = "findTotem")
    private static ItemStack findTotemAccessory(Player player, Operation<ItemStack> original) {
        if (ToolPouchUtil.toolPouchHasItem(player, stack -> stack.is(Items.TOTEM_OF_UNDYING))) {
            return new ItemStack(Items.TOTEM_OF_UNDYING);
        }
        return original.call(player);
    }
}
