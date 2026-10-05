package net.atlas.defaulted.neoforge;

//? neoforge {
/*import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.DefaultedPlatform;
import net.neoforged.fml.loading.FMLEnvironment;

@SuppressWarnings("unused")
public class DefaultedPlatformNeoForge implements DefaultedPlatform {
    public boolean isOnClientNetworkingThread() {
        if (!FMLEnvironment./^? >=1.21.9 {^/ getDist() /^?} <1.21.9 {^/ /^dist ^//^?}^/.isClient()) return false;
        return Defaulted.isOnClientNetworkingThread();
    }
}
*///?}