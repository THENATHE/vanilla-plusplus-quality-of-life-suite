package net.atlas.defaulted;

//? fabric {
import net.atlas.defaulted.fabric.DefaultedPlatformFabric;
//?}
//? neoforge {
/*import net.atlas.defaulted.neoforge.DefaultedPlatformNeoForge;
*///?}

public interface DefaultedPlatform {
    //? fabric {
    DefaultedPlatform INSTANCE = new DefaultedPlatformFabric();
    //?}
    //? neoforge {
    /*DefaultedPlatform INSTANCE = new DefaultedPlatformNeoForge();
    *///?}
    boolean isOnClientNetworkingThread();
}
