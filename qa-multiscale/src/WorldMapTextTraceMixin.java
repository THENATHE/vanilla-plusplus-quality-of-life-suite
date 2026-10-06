package qa.mixin;

import qa.MixedScaleClientQa;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Record the native visible source-status message, not a replacement UI. */
@Mixin(GuiGraphicsExtractor.class)
public abstract class WorldMapTextTraceMixin {
    @Inject(method = "text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V", at = @At("HEAD"))
    private void observe(Font font, Component text, int x, int y, int color, boolean shadow, CallbackInfo ci) {
        if (Minecraft.getInstance().gui.screen() instanceof WorldMapScreen && text.getContents() instanceof TranslatableContents contents)
            MixedScaleClientQa.worldMessages.add(contents.getKey());
    }
}
