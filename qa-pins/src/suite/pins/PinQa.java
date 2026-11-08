package suite.pins;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.networking.ServerNetworkEvents;
import me.pajic.mapstitch.networking.payload.C2SPlacePin;
import me.pajic.mapstitch.pin.PinUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Takes one atlas pin through MapStitch's own placement handler and through its share-time
 * payment (findSimilarPinColor + consumePin) from four container layouts, then checks that
 * exactly one pin was spent and every other stack survived unchanged.
 */
public final class PinQa implements ModInitializer {
    private static final int COLOR = 0x3366CC;
    private final JsonObject result = new JsonObject();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            var cases = new JsonArray();
            boolean passed = true;
            try {
                MapStitch.CONFIG.atlasPins.item.validateAndSet(true);
                result.addProperty("item_required", PinUtil.itemRequired());
                result.addProperty("polymer", FabricLoader.getInstance().isModLoaded("polymer-core"));
                for (String scenario : List.of("shulker", "backpack-early", "bundle-in-backpack", "backpack-past-27")) {
                    for (String path : List.of("place", "share")) {
                        var row = run(server, scenario, path);
                        passed &= row.get("passed").getAsBoolean();
                        cases.add(row);
                    }
                }
            } catch (Throwable error) {
                error.printStackTrace();
                result.addProperty("error", error.toString());
                passed = false;
            }
            result.add("cases", cases);
            result.addProperty("passed", passed);
            try {
                Files.writeString(Path.of("pins-result.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n");
            } catch (Exception error) {
                throw new RuntimeException(error);
            }
            server.halt(false);
        });
    }

    private static JsonObject run(MinecraftServer server, String scenario, String path) {
        var player = createPlayer(server, path + "-" + scenario);
        var holder = holder(scenario);
        player.getInventory().setItem(0, holder.copy());
        player.getInventory().setItem(1, new ItemStack(Items.COBBLESTONE, 32));
        var ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        Function<ItemStack, JsonElement> encode = stack -> stack.isEmpty() ? null : ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();
        int before = PinUtil.countPins(player, COLOR);
        boolean paid;
        if (path.equals("place")) {
            int stored = me.pajic.mapstitch.pin.PinStorage.get(player).size();
            ServerNetworkEvents.placePin(new C2SPlacePin("QA " + scenario, COLOR, Identifier.withDefaultNamespace("overworld"), 10, 20), player);
            paid = me.pajic.mapstitch.pin.PinStorage.get(player).size() == stored + 1;
        } else {
            Integer similar = PinUtil.findSimilarPinColor(player, COLOR);
            paid = similar != null && PinUtil.consumePin(player, similar);
        }
        int after = PinUtil.countPins(player, COLOR);
        var expected = withoutOnePin(holder);
        var actual = player.getInventory().getItem(0);
        var row = new JsonObject();
        row.addProperty("scenario", scenario);
        row.addProperty("path", path);
        row.addProperty("pins_before", before);
        row.addProperty("pins_after", after);
        row.addProperty("paid", paid);
        row.addProperty("stored_entries_before", contentsSize(holder));
        row.addProperty("stored_entries_after", contentsSize(actual));
        boolean conserved = encode.apply(expected).equals(encode.apply(actual))
                && ItemStack.matches(player.getInventory().getItem(1), new ItemStack(Items.COBBLESTONE, 32));
        row.addProperty("other_stacks_conserved", conserved);
        if (!conserved) {
            row.add("expected", encode.apply(expected));
            row.add("actual", encode.apply(actual));
        }
        row.addProperty("passed", paid && before - after == 1 && conserved);
        player.getInventory().clearContent();
        return row;
    }

    /** One holder per scenario; pins sit at the documented positions, other items around them. */
    private static ItemStack holder(String scenario) {
        var pin = PinUtil.createPin(COLOR);
        var items = new ArrayList<ItemStack>();
        switch (scenario) {
            case "shulker" -> {
                for (int i = 0; i < 27; i++) items.add(i == 4 ? pin.copyWithCount(2) : new ItemStack(Items.TORCH, i + 1));
                return container(Items.SHULKER_BOX.getDefaultInstance(), items);
            }
            case "backpack-early", "backpack-past-27", "bundle-in-backpack" -> {
                for (int i = 0; i < 60; i++) items.add(ItemStack.EMPTY);
                items.set(1, new ItemStack(Items.OAK_LOG, 20));
                items.set(30, new ItemStack(Items.DIAMOND, 7));
                items.set(45, new ItemStack(Items.EMERALD, 13));
                items.set(59, new ItemStack(Items.SHULKER_BOX));
                if (scenario.equals("backpack-early")) items.set(3, pin.copyWithCount(3));
                if (scenario.equals("backpack-past-27")) items.set(40, pin.copyWithCount(2));
                if (scenario.equals("bundle-in-backpack")) {
                    var bundle = new ItemStack(Items.BUNDLE);
                    bundle.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(List.of(
                            ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.STRING, 5)),
                            ItemStackTemplate.fromNonEmptyStack(pin.copyWithCount(2)))));
                    items.set(5, bundle);
                }
                var backpack = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getValue(Identifier.parse("tiered_backpacks:netherite_backpack")));
                return container(backpack, items);
            }
            default -> throw new IllegalArgumentException(scenario);
        }
    }

    private static ItemStack withoutOnePin(ItemStack holder) {
        var copy = holder.copy();
        var items = new ArrayList<ItemStack>(copy.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).itemCopies().toList());
        for (int i = 0; i < items.size(); i++) {
            var stack = items.get(i);
            if (PinUtil.isPin(stack, COLOR)) {
                stack.shrink(1);
                return container(copy, items);
            }
        }
        for (int i = 0; i < items.size(); i++) {
            var bundle = items.get(i).get(DataComponents.BUNDLE_CONTENTS);
            if (bundle == null) continue;
            var entries = new ArrayList<ItemStackTemplate>();
            boolean removed = false;
            for (var entry : bundle.items()) {
                var stack = entry.create();
                if (!removed && PinUtil.isPin(stack, COLOR)) {
                    removed = true;
                    stack.shrink(1);
                    if (stack.isEmpty()) continue;
                }
                entries.add(ItemStackTemplate.fromNonEmptyStack(stack));
            }
            if (removed) {
                items.get(i).set(DataComponents.BUNDLE_CONTENTS, new BundleContents(entries));
                return container(copy, items);
            }
        }
        throw new IllegalStateException("Fixture holder has no pin");
    }

    private static ItemStack container(ItemStack stack, List<ItemStack> items) {
        stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
        return stack;
    }

    private static int contentsSize(ItemStack stack) {
        return (int) stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItemCopyStream().count();
    }

    private static ServerPlayer createPlayer(MinecraftServer server, String name) {
        var profile = new GameProfile(UUID.nameUUIDFromBytes(name.getBytes()), name.length() > 16 ? name.substring(0, 16) : name);
        var player = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
        player.connection = new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.SERVERBOUND), player,
                CommonListenerCookie.createInitial(profile, false)) {
            @Override
            public void send(Packet<?> packet) {
            }
        };
        player.setPos(0, 100, 0);
        return player;
    }
}
