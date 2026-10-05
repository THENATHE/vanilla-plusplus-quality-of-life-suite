package me.pajic.toolpouch.compat;

import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.world.item.Items;
import page.langeweile.ok_zoomer.utils.ZoomUtils;

public class OkZoomerCompat {

    public static void init() {
        ZoomUtils.addSpyglassProvider(localPlayer -> ToolPouchUtil.toolPouchHasItem(
                localPlayer, stack -> stack.is(Items.SPYGLASS)
        ));
    }
}
