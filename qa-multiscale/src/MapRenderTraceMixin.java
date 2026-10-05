package qa.mixin;

import qa.MixedScaleClientQa;
import me.pajic.mapstitch.platform.version.Util26_3;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Observe the existing renderer; leave its behavior unchanged. */
@Mixin(value = Util26_3.class, remap = false)
public abstract class MapRenderTraceMixin {
    @Inject(method = "renderMap", at = @At("HEAD"))
    private void observe(Minecraft mc, GuiGraphicsExtractor graphics, MapId id, MapItemSavedData data,
                         WorldMapScreen.GridPos gridPos, boolean minimap, CallbackInfo ci) {
        if (!minimap) MixedScaleClientQa.rendered.add(data.dimension.identifier() + "/" + data.scale);
    }
}
