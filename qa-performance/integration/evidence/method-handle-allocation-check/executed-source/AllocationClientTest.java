package com.thenathe.hotkeywheel.qa;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;

/** Exercises atlas lookup behavior and allocation in a fresh graphical client world. */
public final class AllocationClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        var report = new LinkedHashMap<String, Object>();
        report.put("suite", FabricLoader.getInstance().getModContainer("vpp_mod_suite")
                .orElseThrow().getMetadata().getVersion().getFriendlyString());
        report.put("polymer_client", FabricLoader.getInstance().isModLoaded("polymer-core"));
        try {
            try (var world = context.worldBuilder().create()) {
                world.getConnection().waitForChunksDownload();
                context.waitTicks(20);
                context.runOnClient(client -> {
                    try {
                        report.putAll(AtlasCacheQa.run(client.player));
                    } catch (Exception failure) {
                        throw new RuntimeException("Atlas allocation fixture", failure);
                    }
                });
            }
        } catch (Throwable failure) {
            report.put("passed", false);
            report.put("failure", failure.toString());
            throw new AssertionError("Atlas allocation and behavior QA", failure);
        } finally {
            try {
                Files.writeString(Path.of(System.getProperty("wheel.qa.root"), "allocation-results.json"),
                        new GsonBuilder().setPrettyPrinting().create().toJson(report));
            } catch (Exception failure) {
                throw new RuntimeException(failure);
            }
        }
    }
}
