package me.pajic.toolpouch.mixin.client;

import com.google.common.base.Strings;
import me.pajic.toolpouch.ToolPouch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(
            method = "extractLines",
            at = @At("HEAD")
    )
    private void filterLines(GuiGraphicsExtractor graphics, List<String> lines, boolean alignLeft,/*? >26.2 {*/ int scaledScreenWidth,/*?}*/ CallbackInfo ci) {
        if (ToolPouch.CONFIG.hideDebugInfoInSurvival.get() && minecraft.showOnlyReducedInfo()) {
            lines.removeIf(text -> !Strings.isNullOrEmpty(text) && text.startsWith("Section-relative: "));
            lines.removeIf(text -> !Strings.isNullOrEmpty(text) && text.startsWith("hunger: "));
        }
    }
}
