package net.atlas.defaulted.fabric;

//? fabric {
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.DefaultedPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

@SuppressWarnings("unused")
public class DefaultedPlatformFabric implements DefaultedPlatform {
    public boolean isOnClientNetworkingThread() {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) return false;
        return Defaulted.isOnClientNetworkingThread();
    }
}
//?}