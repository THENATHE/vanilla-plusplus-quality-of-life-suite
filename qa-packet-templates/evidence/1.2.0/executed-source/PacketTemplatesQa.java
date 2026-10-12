package qa;

import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.context.*;
import net.minecraft.core.component.*;
import net.minecraft.network.*;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;

/** Encodes real nested container-content packets; no display-only projection substitutes. */
public final class PacketTemplatesQa implements ModInitializer {
    private final JsonArray rows = new JsonArray();
    private int failures;
    private int assertions;

    private void check(boolean value, String message) {
        assertions++;
        if (!value) throw new AssertionError(message);
    }

    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            JsonObject result = new JsonObject();
            try {
                boolean polymer = net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("polymer-core");
                boolean uncapped = Boolean.getBoolean("templates.qa.uncapped");
                check(!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("defaulted"), "Defaulted runtime absent");
                check(me.pajic.sensible_stackables.SensibleStackables.CONFIG.uncapStackSize.get() == uncapped, "uncapping matches profile");
                run("template-effective-hot-override-and-explicit-priority", () -> {
                    var saved = me.pajic.sensible_stackables.handler.StackSizeOverrides.snapshot();
                    var template = ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.POTION));
                    var explicit = new ItemStack(Items.POTION);
                    explicit.set(DataComponents.MAX_STACK_SIZE, 7);
                    var explicitTemplate = ItemStackTemplate.fromNonEmptyStack(explicit);
                    var removedTemplate = new ItemStackTemplate(Items.POTION.builtInRegistryHolder(), 1,
                        DataComponentPatch.builder().remove(DataComponents.MAX_STACK_SIZE).build());
                    try {
                        me.pajic.sensible_stackables.handler.StackSizeOverrides.set(Map.of(Items.POTION, 5));
                        check(template.get(DataComponents.MAX_STACK_SIZE) == 5, "existing template reflects live effective override");
                        check(explicitTemplate.get(DataComponents.MAX_STACK_SIZE) == 7, "explicit template patch outranks registry override");
                        check(removedTemplate.get(DataComponents.MAX_STACK_SIZE) == null, "explicit template removal outranks registry override");
                        me.pajic.sensible_stackables.handler.StackSizeOverrides.clear();
                        check(template.get(DataComponents.MAX_STACK_SIZE) == 1, "cleared override restores vanilla template maximum");
                        check(explicitTemplate.get(DataComponents.MAX_STACK_SIZE) == 7, "clearing overrides preserves explicit template patch");
                    } finally { me.pajic.sensible_stackables.handler.StackSizeOverrides.set(saved); }
                });
                if (polymer) for (boolean withConnection : new boolean[]{true, false}) {
                    var apiContext = context(server, 2, PacketFlow.CLIENTBOUND, withConnection);
                    run("preview-hook-API-guard/" + (withConnection ? "clientbound" : "no-connection"), () -> {
                        var stone = new ItemStack(Items.STONE, 1243);
                        stone.set(DataComponents.MAX_STACK_SIZE, 2048);
                        var carrier = new ItemStack(me.pajic.toolpouch.item.ModItems.NETHERITE_TOOL_POUCH);
                        carrier.set(DataComponents.MAX_STACK_SIZE, 2048);
                        carrier.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(ItemStack.EMPTY, stone)));
                        var before = carrier.copy();
                        var projected = eu.pb4.polymer.core.api.item.PolymerItemUtils.getPolymerItemStack(carrier, TooltipFlag.NORMAL, apiContext, server.registryAccess());
                        check(projected == carrier, "non-serverbound native carrier API path preserves upstream original return");
                        check(projected.getMaxStackSize() == 2048, "preview hook leaves non-serverbound explicit carrier maximum alone");
                        check(projected.get(DataComponents.CONTAINER) == before.get(DataComponents.CONTAINER), "preview hook leaves non-serverbound unsafe-preview component alone");
                        check(ItemStack.matches(carrier, before), "non-serverbound API guard preserves authoritative stack");
                    });
                }
                for (int recipient : new int[]{0, 1, 2}) {
                    boolean nativeClient = recipient == 1;
                    String recipientName = recipient == 0 ? "fallback" : recipient == 1 ? "native" : "partial-native-without-stackables";
                    var context = context(server, recipient);
                    for (Item item : new Item[]{Items.POTION, Items.SADDLE, Items.ENCHANTED_BOOK, Items.STONE}) {
                        int limit = new ItemStack(item).getMaxStackSize();
                        for (int count : uncapped && item == Items.STONE ? new int[]{1, limit, 1243} : new int[]{1, limit}) {
                            var content = new ItemStack(item, count);
                            content.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Preserve " + item + " x" + count));
                            run("template-create/" + item + "/" + count + "/recipient=" + recipientName, () -> {
                                var template = ItemStackTemplate.fromNonEmptyStack(content);
                                var created = template.create();
                                check(!created.isEmpty() && created.getCount() == count, "template materializes full valid quantity");
                                check(created.getMaxStackSize() == limit, "template created effective stack limit");
                                check(ItemStack.matches(content, created), "template preserves all original components");
                            });
                            run("top-level-quantity/" + item + "/" + count + "/recipient=" + recipientName,
                                () -> encode(server, context, content));
                            int carrierIndex = 0;
                            for (var carrier : carriers(content)) {
                                run("packet/carrier=" + carrierIndex++ + "/" + carrier.getItem() + "/" + item + "/" + count + "/recipient=" + recipientName,
                                    () -> encode(server, context, carrier));
                            }
                            var charged = new ItemStack(Items.CROSSBOW);
                            charged.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(ItemStackTemplate.fromNonEmptyStack(content)));
                            run("charged-projectiles/" + item + "/" + count + "/recipient=" + recipientName,
                                () -> encode(server, context, charged));
                            var remainder = new ItemStack(Items.HONEY_BOTTLE);
                            remainder.set(DataComponents.USE_REMAINDER, new UseRemainder(ItemStackTemplate.fromNonEmptyStack(content)));
                            run("use-remainder/" + item + "/" + count + "/recipient=" + recipientName,
                                () -> encode(server, context, remainder));
                        }
                    }
                    var explicit = new ItemStack(Items.POTION, 7);
                    explicit.set(DataComponents.MAX_STACK_SIZE, 7);
                    for (var carrier : carriers(explicit)) run("explicit-override/" + carrier.getItem() + "/recipient=" + recipientName,
                        () -> encode(server, context, carrier));
                    if (uncapped) for (Item item : new Item[]{me.pajic.toolpouch.item.ModItems.NETHERITE_TOOL_POUCH,
                            me.pajic.tiered_backpacks.item.ModItems.NETHERITE_BACKPACK, me.pajic.mapstitch.item.ModItems.ATLAS}) {
                        for (int count : new int[]{1, 99}) {
                            var explicitCarrier = new ItemStack(item, count);
                            explicitCarrier.set(DataComponents.MAX_STACK_SIZE, 2048);
                            explicitCarrier.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(ItemStack.EMPTY, new ItemStack(Items.COMPASS))));
                            run("explicit-native-carrier-limit-2048/" + item + "/" + count + "/recipient=" + recipientName,
                                () -> encode(server, context, explicitCarrier));
                        }
                    }
                    var savedOverrides = me.pajic.sensible_stackables.handler.StackSizeOverrides.snapshot();
                    try {
                        var overrides = new HashMap<>(savedOverrides);
                        overrides.put(me.pajic.toolpouch.item.ModItems.NETHERITE_TOOL_POUCH, 84);
                        me.pajic.sensible_stackables.handler.StackSizeOverrides.set(overrides);
                        var carrier = new ItemStack(me.pajic.toolpouch.item.ModItems.NETHERITE_TOOL_POUCH, 15);
                        carrier.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(ItemStack.EMPTY, new ItemStack(Items.COMPASS))));
                        run("native-carrier-registry-limit-84/recipient=" + recipientName, () -> encode(server, context, carrier));
                    } finally { me.pajic.sensible_stackables.handler.StackSizeOverrides.set(savedOverrides); }
                    var invalid = new ItemStackTemplate(Items.STONE, -1);
                    run("malformed-negative-template/recipient=" + recipientName, () -> {
                        check(invalid.create().isEmpty(), "invalid template remains invalid rather than inventing an item");
                        check(invalid.count() == -1, "malformed template count is not normalized in authoritative data");
                    });
                    run("genuinely-invalid-damageable-template/recipient=" + recipientName, () -> {
                        var sword = new ItemStack(Items.DIAMOND_SWORD);
                        sword.set(DataComponents.MAX_STACK_SIZE, 2);
                        var invalidSword = ItemStackTemplate.fromNonEmptyStack(sword);
                        check(invalidSword.get(DataComponents.MAX_STACK_SIZE) == 2, "explicit invalid maximum is not silently normalized");
                        check(invalidSword.create().isEmpty(), "strict validation still rejects stackable damageable item");
                        check(sword.getMaxStackSize() == 2 && sword.getCount() == 1 && !sword.isEmpty(), "invalid original properties remain intact");
                    });
                }
                result.addProperty("polymer", polymer);
                result.addProperty("uncapped", uncapped);
            } catch (Throwable failure) {
                failure.printStackTrace(); failures++;
                result.addProperty("fatal", failure.toString());
            } finally {
                result.addProperty("passed", failures == 0);
                result.addProperty("failures", failures);
                result.addProperty("assertions", assertions);
                result.add("cases", rows);
                try { Files.writeString(Path.of("packet-templates-result.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result)); }
                catch (Exception error) { throw new RuntimeException(error); }
                server.halt(false);
            }
        });
    }

    private List<ItemStack> carriers(ItemStack content) {
        var holders = new ArrayList<ItemStack>();
        for (Item item : new Item[]{Items.SHULKER_BOX, me.pajic.toolpouch.item.ModItems.TOOL_POUCH,
                me.pajic.toolpouch.item.ModItems.NETHERITE_TOOL_POUCH, Items.NETHERITE_LEGGINGS,
                me.pajic.tiered_backpacks.item.ModItems.NETHERITE_BACKPACK, Items.NETHERITE_CHESTPLATE}) {
            var carrier = new ItemStack(item);
            carrier.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(ItemStack.EMPTY, content.copy(), ItemStack.EMPTY, new ItemStack(Items.COMPASS), ItemStack.EMPTY)));
            holders.add(carrier);
        }
        var attachedPouch = new me.pajic.toolpouch.recipe.AttachToolPouchRecipe().assemble(
            net.minecraft.world.item.crafting.CraftingInput.of(2, 1, List.of(holders.get(2), new ItemStack(Items.NETHERITE_LEGGINGS))));
        holders.set(3, attachedPouch);
        var attachedBackpack = new me.pajic.tiered_backpacks.recipe.AttachBackpackRecipe().assemble(
            net.minecraft.world.item.crafting.CraftingInput.of(2, 1, List.of(holders.get(4), new ItemStack(Items.NETHERITE_CHESTPLATE))));
        holders.set(5, attachedBackpack);
        var outer = new ItemStack(Items.SHULKER_BOX);
        outer.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(ItemStack.EMPTY, holders.getFirst().copy(), new ItemStack(Items.CLOCK))));
        holders.add(outer);
        var bundle = new ItemStack(Items.BUNDLE);
        bundle.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(List.of(ItemStackTemplate.fromNonEmptyStack(content), ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.COMPASS)))));
        holders.add(bundle);
        var atlas = new ItemStack(me.pajic.mapstitch.item.ModItems.ATLAS);
        atlas.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(List.of(ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.MAP, 3)), ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.PAPER, 7)))));
        holders.add(atlas);
        var shulkerPouch = new ItemStack(me.pajic.toolpouch.item.ModItems.NETHERITE_TOOL_POUCH);
        shulkerPouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(ItemStack.EMPTY, holders.getFirst().copy(), new ItemStack(Items.CLOCK))));
        holders.add(shulkerPouch);
        holders.add(new me.pajic.toolpouch.recipe.AttachToolPouchRecipe().assemble(
            net.minecraft.world.item.crafting.CraftingInput.of(2, 1, List.of(shulkerPouch, new ItemStack(Items.NETHERITE_LEGGINGS)))));
        return holders;
    }

    private void run(String name, Runnable test) {
        var row = new JsonObject(); row.addProperty("case", name);
        try { test.run(); row.addProperty("passed", true); }
        catch (Throwable error) { failures++; row.addProperty("passed", false); row.addProperty("error", error.toString()); error.printStackTrace(); }
        rows.add(row);
    }

    private void encode(MinecraftServer server, PacketContext context, ItemStack carrier) {
        carrier.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Unrelated carrier name"));
        var original = carrier.copy();
        var buffer = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), server.registryAccess());
        var packet = new ClientboundContainerSetContentPacket(0, 123, List.of(ItemStack.EMPTY, carrier, ItemStack.EMPTY), ItemStack.EMPTY);
        try {
            PacketContext.runWithContext(new PacketContextProvider() {
                public PacketContext getPacketContext() { return context; }
            }, () -> ClientboundContainerSetContentPacket.STREAM_CODEC.encode(buffer, packet));
            check(buffer.readableBytes() > 0, "real inventory packet produced wire bytes");
            var decoded = ClientboundContainerSetContentPacket.STREAM_CODEC.decode(buffer);
            check(decoded.items().size() == 3 && decoded.items().getFirst().isEmpty() && decoded.items().getLast().isEmpty(), "empty outer inventory slots roundtrip");
            check(!decoded.items().get(1).isEmpty(), "carrier survives real packet roundtrip");
            check(decoded.items().get(1).getCount() == carrier.getCount(), "top-level quantity remains exact");
            check(Objects.equals(decoded.items().get(1).get(DataComponents.CUSTOM_NAME), original.get(DataComponents.CUSTOM_NAME)), "unrelated custom name survives projection");
            boolean vanillaMetadata = net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("polymer-core")
                && !com.thenathe.suite.network.SuiteCapabilities.isNative(context, "sensible_stackables");
            if (vanillaMetadata) {
                Integer rawMax = decoded.items().get(1).getComponentsPatch().get(decoded.items().get(1).getItem().components(), DataComponents.MAX_STACK_SIZE);
                check(rawMax != null && rawMax == Math.min(99, original.getMaxStackSize()), "fallback carrier explicitly projects its effective maximum without exceeding 99");
            }
            validateNested(decoded.items().get(1), 0, vanillaMetadata);
            if (!vanillaMetadata) {
                check(flatten(original).equals(flatten(decoded.items().get(1))), "native nested item types, positions, and quantities remain exact");
            } else if (net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(original.getItem()).getNamespace().equals("minecraft")
                    && !original.is(Items.NETHERITE_LEGGINGS) && !original.is(Items.NETHERITE_CHESTPLATE)
                    && safePreview(original)) {
                check(flatten(original).equals(flatten(decoded.items().get(1))), "safe vanilla preview keeps exact item types, positions, and quantities");
            }
            check(decoded.carriedItem().isEmpty(), "empty cursor roundtrip");
            check(buffer.readableBytes() == 0, "packet decoder consumed exact bytes");
            check(ItemStack.matches(carrier, original), "network transformation leaves authoritative carrier unchanged");
            check(carrier.get(DataComponents.CUSTOM_NAME) == original.get(DataComponents.CUSTOM_NAME), "unrelated authoritative component identity is untouched");
        } finally { buffer.release(); }
    }

    private boolean safePreview(ItemStack stack) {
        var container = stack.get(DataComponents.CONTAINER);
        if (container != null) for (var template : container.nonEmptyItems()) if (template.count() > 99 || !safePreview(template.create())) return false;
        var bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null) for (var template : bundle.items()) if (template.count() > 99 || !safePreview(template.create())) return false;
        var charged = stack.get(DataComponents.CHARGED_PROJECTILES);
        if (charged != null) for (var template : charged.items()) if (template.count() > 99 || !safePreview(template.create())) return false;
        var remainder = stack.get(DataComponents.USE_REMAINDER);
        return remainder == null || (remainder.convertInto().count() <= 99 && safePreview(remainder.convertInto().create()));
    }

    private List<String> flatten(ItemStack stack) {
        var out = new ArrayList<String>();
        flatten(stack, "root", out);
        return out;
    }

    private void flatten(ItemStack stack, String path, List<String> out) {
        out.add(path + ":" + net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()) + ":" + stack.getCount());
        var container = stack.get(DataComponents.CONTAINER);
        if (container != null) {
            var copies = container.itemCopies().toList();
            for (int i = 0; i < copies.size(); i++) flatten(copies.get(i), path + "/container/" + i, out);
        }
        var bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null) for (int i = 0; i < bundle.size(); i++) flatten(bundle.items().get(i).create(), path + "/bundle/" + i, out);
        var charged = stack.get(DataComponents.CHARGED_PROJECTILES);
        if (charged != null) for (int i = 0; i < charged.size(); i++) flatten(charged.items().get(i).create(), path + "/charged/" + i, out);
        var remainder = stack.get(DataComponents.USE_REMAINDER);
        if (remainder != null) flatten(remainder.convertInto().create(), path + "/remainder", out);
    }

    private void validateNested(ItemStack stack, int depth, boolean vanillaMetadata) {
        check(depth < 20, "bounded nested fixture");
        var container = stack.get(DataComponents.CONTAINER);
        if (container != null) for (var template : container.nonEmptyItems()) validateTemplate(template, depth, vanillaMetadata);
        var bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null) for (var template : bundle.items()) validateTemplate(template, depth, vanillaMetadata);
        var charged = stack.get(DataComponents.CHARGED_PROJECTILES);
        if (charged != null) for (var template : charged.items()) validateTemplate(template, depth, vanillaMetadata);
        var remainder = stack.get(DataComponents.USE_REMAINDER);
        if (remainder != null) validateTemplate(remainder.convertInto(), depth, vanillaMetadata);
    }

    private void validateTemplate(ItemStackTemplate template, int depth, boolean vanillaMetadata) {
        if (vanillaMetadata) {
            Integer rawMax = template.components().get(template.item().value().components(), DataComponents.MAX_STACK_SIZE);
            check(rawMax != null && rawMax >= 1 && rawMax <= 99, "fallback template carries safe explicit or vanilla maximum");
            check(template.count() <= rawMax, "vanilla template strict validation accepts nested count " + template.count() + " at maximum " + rawMax);
        }
        var created = template.create();
        check(!created.isEmpty(), "decoded nested template materializes rather than silently becoming EMPTY: " + template);
        check(created.getCount() == template.count(), "decoded template retains advertised count");
        validateNested(created, depth + 1, vanillaMetadata);
    }

    private PacketContext context(MinecraftServer server, int recipient) throws ReflectiveOperationException {
        return context(server, recipient, PacketFlow.SERVERBOUND, true);
    }

    private PacketContext context(MinecraftServer server, int recipient, PacketFlow flow, boolean withConnection) throws ReflectiveOperationException {
        var connection = withConnection ? new Connection(flow) : null;
        var values = new IdentityHashMap<Object, Object>();
        if (recipient != 0) {
            var field = Class.forName("com.thenathe.suite.network.SuiteCapabilities").getDeclaredField("MODULES_KEY");
            field.setAccessible(true);
            var modules = new HashSet<>(Set.of("simple_smithing_overhaul", "toolpouch", "tiered_backpacks", "mapstitch", "chalk"));
            if (recipient == 1) modules.add("sensible_stackables");
            values.put(field.get(null), Set.copyOf(modules));
        }
        return new PacketContext() {
            @SuppressWarnings("unchecked") public <T> T get(ReadKey<T> key) {
                if (key == REGISTRY_ACCESS) return (T) server.registryAccess();
                if (key == SERVER_INSTANCE) return (T) server;
                if (key == CONNECTION) return (T) connection;
                return (T) values.get(key);
            }
            public <T> void set(Key<T> key, T value) { values.put(key, value); }
        };
    }
}
