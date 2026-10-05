package pouchmendingqa.mixin;
import java.nio.file.*;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ExperienceOrb.class)
public class MendingServerPickupTraceMixin{
    @Inject(method="playerTouch",at=@At("HEAD"))
    private void trace(Player player,CallbackInfo ci){if(!(player instanceof ServerPlayer)||player.takeXpDelay!=0)return;ExperienceOrb orb=(ExperienceOrb)(Object)this;try{Files.writeString(Path.of(System.getProperty("mending.qa.control"),"pickup-trace.txt"),"NATURAL_TOUCH orb="+orb.getId()+" value="+orb.getValue()+" xpBefore="+player.totalExperience+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);}catch(Exception e){throw new IllegalStateException(e);}}
}
