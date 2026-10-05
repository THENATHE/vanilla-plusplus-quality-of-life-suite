package suite.qa;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import java.nio.file.*;
public final class SuiteClientQa implements ClientModInitializer {
    private int ticks;
    private boolean done;
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (done || client.player == null || client.level == null) return;
            // Several frames in-world catch immediate post-configuration packet failures.
            if (++ticks < 60) return;
            done = true;
            try { Files.writeString(Path.of(System.getProperty("suite.qa.control"), "client-joined.txt"), "PASS in-world client ticks\n"); }
            catch (Exception error) { throw new RuntimeException(error); }
        });
    }
}
