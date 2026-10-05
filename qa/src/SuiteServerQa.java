package suite.qa;

import com.google.gson.JsonObject;
import com.thenathe.suite.network.SuiteCapabilities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import java.nio.file.*;

/** Bounded connection assertions only: gameplay is documented for manual testing. */
public final class SuiteServerQa implements ModInitializer {
    @Override public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            JsonObject result = new JsonObject();
            boolean expected = handler.player.getGameProfile().name().startsWith("SuiteNative");
            boolean passed = true;
            JsonObject selected = new JsonObject();
            for (String mod : SuiteCapabilities.MODULES) {
                boolean actual = SuiteCapabilities.isNative(handler.getPacketContext(), mod);
                selected.addProperty(mod, actual);
                passed &= actual == expected;
            }
            var loader = FabricLoader.getInstance();
            result.addProperty("player", handler.player.getGameProfile().name());
            result.addProperty("expected_native", expected);
            result.addProperty("polymer", loader.isModLoaded("polymer-core"));
            result.add("native_modules", selected);
            result.addProperty("chalk_conversion_registered", BuiltInRegistries.RECIPE_SERIALIZER.containsKey(Identifier.parse("chalk_polymer_compat:chalk_conversion")));
            passed &= BuiltInRegistries.RECIPE_SERIALIZER.containsKey(Identifier.parse("chalk_polymer_compat:chalk_conversion"));
            result.addProperty("defaulted_version", loader.getModContainer("defaulted").orElseThrow().getMetadata().getVersion().getFriendlyString());
            result.addProperty("passed", passed);
            try { Files.writeString(Path.of(System.getProperty("suite.qa.control"), handler.player.getGameProfile().name() + "-join.json"), result.toString()); }
            catch (Exception error) { throw new RuntimeException(error); }
            System.out.println("SUITE_QA_JOIN=" + result);
        });
    }
}
