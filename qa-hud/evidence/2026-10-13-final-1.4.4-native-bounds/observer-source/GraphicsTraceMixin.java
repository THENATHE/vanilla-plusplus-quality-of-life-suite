package suitehudqa.mixin;
import suitehudqa.HudObservation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.resources.Identifier;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.textures.GpuSampler;
/** Observe final renderer inputs after all caller-side placement wrappers. */
@Mixin(GuiGraphicsExtractor.class)
public abstract class GraphicsTraceMixin {
    @Inject(method="map",at=@At("HEAD"))
    private void beginCanvas(net.minecraft.client.renderer.state.MapRenderState state,CallbackInfo ci){HudObservation.inMapCanvas=HudObservation.inMap;}
    @Inject(method="map",at=@At("RETURN"))
    private void endCanvas(net.minecraft.client.renderer.state.MapRenderState state,CallbackInfo ci){HudObservation.inMapCanvas=false;}

    @Inject(method="innerBlit(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Lcom/mojang/renderpearl/api/textures/GpuSampler;IIIIFFFFI)V",at=@At("HEAD"))
    private void mapCanvas(RenderPipeline pipeline,GpuTextureView texture,GpuSampler sampler,int x0,int y0,int x1,int y1,float u0,float u1,float v0,float v1,int color,CallbackInfo ci){
        if(HudObservation.inMap)HudObservation.map(((GuiGraphicsExtractor)(Object)this).pose(),x0,y0,x1,y1);
    }
    @Inject(method="text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V",at=@At("HEAD"))
    private void observe(Font font,Component text,int x,int y,int color,boolean shadow,CallbackInfo ci){
        if(HudObservation.inDetails)HudObservation.text(x,y,font.width(text),font.lineHeight);
        if(HudObservation.inDetails)HudObservation.detail(((GuiGraphicsExtractor)(Object)this).pose(),x,y,x+font.width(text)+(shadow?1:0),y+font.lineHeight+(shadow?1:0));
        if(HudObservation.inMap)HudObservation.map(((GuiGraphicsExtractor)(Object)this).pose(),x,y,x+font.width(text)+(shadow?1:0),y+font.lineHeight+(shadow?1:0));
    }
    @Inject(method="text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V",at=@At("HEAD"))
    private void mapLabel(Font font,Component text,int x,int y,int color,CallbackInfo ci){
        if(HudObservation.inMap)HudObservation.map(((GuiGraphicsExtractor)(Object)this).pose(),x,y,x+font.width(text)+1,y+font.lineHeight+1);
    }
    @Inject(method="fill(IIIII)V",at=@At("HEAD"))
    private void rectangle(int x1,int y1,int x2,int y2,int color,CallbackInfo ci){
        if(HudObservation.inMap)HudObservation.map(((GuiGraphicsExtractor)(Object)this).pose(),x1,y1,x2,y2);
        if(HudObservation.inDetails)HudObservation.detail(((GuiGraphicsExtractor)(Object)this).pose(),x1,y1,x2,y2);
    }
    @Inject(method="blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",at=@At("HEAD"))
    private void sprite(RenderPipeline pipeline,Identifier texture,int x,int y,int width,int height,CallbackInfo ci){
        if(HudObservation.inMap)HudObservation.map(((GuiGraphicsExtractor)(Object)this).pose(),x,y,x+width,y+height);
    }
}
