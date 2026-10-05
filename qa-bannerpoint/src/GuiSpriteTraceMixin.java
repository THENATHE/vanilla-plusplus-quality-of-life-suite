package qa.bannerpoint.mixin;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qa.bannerpoint.BannerpointClientQa;

@Mixin(GuiGraphicsExtractor.class)
public class GuiSpriteTraceMixin {
    @Inject(method = "blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V", at = @At("HEAD"))
    private void trace(RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, int tint, CallbackInfo ci) {
        BannerpointClientQa.render(sprite, tint);
    }
}
