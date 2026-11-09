package com.thenathe.toolpouchcompat;

import com.mojang.blaze3d.platform.InputConstants;
import me.pajic.toolpouch.keybind.SlotToggleKeys;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Suites up to 1.5.1 had their own "Toggle Tool Pouch Elytra" key (key.toolpouch.toggle_elytra).
 * A key the player had bound there becomes a Tool Pouch slot-toggle bind for the pouch slots that
 * hold an Elytra, the first time such a pouch is seen. Minecraft drops the unknown line from
 * options.txt on its next save, so the key is remembered in a small file until it has been moved.
 */
public final class LegacyElytraKey {
    private static final String OPTION = "key_key.toolpouch.toggle_elytra:";
    private static final String DONE = "done";
    private static Path record;
    private static String pending;

    private LegacyElytraKey() {}

    public static void initialize() {
        var config = FabricLoader.getInstance().getConfigDir();
        record = config.resolve("vpp_mod_suite/legacy-elytra-key.txt");
        try {
            if (Files.exists(record)) {
                String saved = Files.readString(record).strip();
                pending = saved.equals(DONE) || saved.isEmpty() ? null : saved;
            } else {
                pending = readOption(FabricLoader.getInstance().getGameDir().resolve("options.txt")).orElse(null);
                write(pending == null ? DONE : pending);
            }
        } catch (IOException exception) {
            LoggerFactory.getLogger("vpp_mod_suite").warn("Could not read the old Tool Pouch Elytra key", exception);
            return;
        }
        if (pending != null) ClientTickEvents.END_CLIENT_TICK.register(client -> carryOver(client));
    }

    static Optional<String> readOption(Path options) throws IOException {
        if (!Files.exists(options)) return Optional.empty();
        for (String line : Files.readAllLines(options)) {
            if (!line.startsWith(OPTION)) continue;
            String key = line.substring(OPTION.length()).strip();
            return key.isEmpty() || key.equals(InputConstants.UNKNOWN.getName()) ? Optional.empty() : Optional.of(key);
        }
        return Optional.empty();
    }

    private static void carryOver(Minecraft client) {
        if (pending == null || client.player == null || client.player.tickCount % 20 != 0) return;
        var pouch = ToolPouchUtil.getToolPouch(client.player);
        if (pouch.isEmpty()) return;
        InputConstants.Key key;
        try {
            key = InputConstants.getKey(pending);
        } catch (IllegalArgumentException invalid) {
            key = InputConstants.UNKNOWN;
        }
        boolean found = false;
        var items = ToolPouchUtil.readItems(pouch);
        for (int slot = 0; slot < items.size(); slot++) {
            if (!items.get(slot).has(DataComponents.GLIDER)) continue;
            found = true;
            // Never replace a bind the player already chose for that slot.
            if (!key.equals(InputConstants.UNKNOWN) && SlotToggleKeys.getKey(slot) == null) SlotToggleKeys.bind(slot, key);
        }
        if (!found && !key.equals(InputConstants.UNKNOWN)) return;
        pending = null;
        try {
            write(DONE);
        } catch (IOException exception) {
            LoggerFactory.getLogger("vpp_mod_suite").warn("Could not record the moved Tool Pouch Elytra key", exception);
        }
    }

    private static void write(String value) throws IOException {
        Files.createDirectories(record.getParent());
        Files.writeString(record, value + "\n");
    }
}
