package qa;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonObject;
import java.nio.file.*;
import java.util.UUID;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.*;
import net.minecraft.server.network.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;

public final class StackablesQa implements ModInitializer {
    private int assertions;
    private void check(boolean value, String label) {
        if (!value) throw new AssertionError(label);
        assertions++;
    }
    private long total(AbstractContainerMenu menu) {
        long total = menu.getCarried().getCount();
        for (var slot : menu.slots) total += slot.getItem().getCount();
        return total;
    }
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            JsonObject result = new JsonObject();
            try {
                boolean uncap = Boolean.getBoolean("stackables.qa.uncapped");
                int size = uncap ? 2048 : 64;
                check(me.pajic.sensible_stackables.SensibleStackables.CONFIG.uncapStackSize.get() == uncap, "startup uncapping config");
                check(new ItemStack(Items.STONE).getMaxStackSize() == size, "common stack prototype");
                check(new ItemStack(Items.POTION).getMaxStackSize() == 3, "potion default");
                check(new ItemStack(Items.SADDLE).getMaxStackSize() == 16, "saddle default");
                check(new ItemStack(Items.ENCHANTED_BOOK).getMaxStackSize() == 64, "book default");
                var level = server.overworld();
                var player = new ServerPlayer(server, level, new GameProfile(UUID.randomUUID(), "StackablesQA"), ClientInformation.createDefault());
                player.connection = new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.SERVERBOUND), player, CommonListenerCookie.createInitial(player.getGameProfile(), false));
                player.experienceLevel = 100;
                player.setPos(0, -59, 0);
                var menu = ChestMenu.threeRows(1, player.getInventory());
                menu.getSlot(0).set(new ItemStack(Items.STONE, size));
                menu.clicked(0, 1, ContainerInput.PICKUP, player);
                check(menu.getCarried().getCount() == size / 2 && total(menu) == size, "right click half conserves");
                menu.clicked(1, 1, ContainerInput.PICKUP, player);
                check(menu.getSlot(1).getItem().getCount() == 1 && total(menu) == size, "place one conserves");
                menu.clicked(2, 0, ContainerInput.PICKUP, player);
                check(menu.getCarried().isEmpty() && total(menu) == size, "place remainder conserves");
                menu.clicked(0, 0, ContainerInput.QUICK_MOVE, player);
                check(menu.getSlot(0).getItem().isEmpty() && total(menu) == size, "shift move conserves");
                menu.clicked(2, 0, ContainerInput.PICKUP, player);
                menu.clicked(-999, 0, ContainerInput.QUICK_CRAFT, player);
                menu.clicked(3, 1, ContainerInput.QUICK_CRAFT, player);
                menu.clicked(4, 1, ContainerInput.QUICK_CRAFT, player);
                menu.clicked(-999, 2, ContainerInput.QUICK_CRAFT, player);
                check(total(menu) == size, "drag distribution conserves");
                menu.clicked(-1, 0, ContainerInput.PICKUP, player);
                check(total(menu) == size, "invalid slot cannot lose quantities");
                var enchantments = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                var left = new ItemStack(Items.ENCHANTED_BOOK, 3);
                var right = new ItemStack(Items.ENCHANTED_BOOK, 4);
                left.enchant(enchantments.getOrThrow(Enchantments.SHARPNESS), 1);
                right.enchant(enchantments.getOrThrow(Enchantments.SHARPNESS), 1);
                var anvilPos = new BlockPos(2, -60, 0);
                level.setBlock(anvilPos, Blocks.ANVIL.defaultBlockState(), 3);
                var anvil = new AnvilMenu(2, player.getInventory(), ContainerLevelAccess.create(level, anvilPos));
                anvil.getSlot(0).set(left); anvil.getSlot(1).set(right); anvil.createResult();
                check(anvil.getSlot(2).getItem().getCount() == 1, "anvil combines only one book");
                var taken = anvil.getSlot(2).remove(1); anvil.getSlot(2).onTake(player, taken);
                check(anvil.getSlot(0).getItem().getCount() == 2 && anvil.getSlot(1).getItem().getCount() == 3, "anvil retains both book remainders");
                var named = new ItemStack(Items.STONE, size);
                named.set(DataComponents.CUSTOM_NAME, Component.literal("Stackables saved quantity"));
                if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("polymer-core")) {
                    var transformed = PolymerChecks.transform(named, server);
                    check(transformed.getCount() == size && transformed.getMaxStackSize() == Math.min(99, size),
                            "ordinary vanilla item enters fallback transform with safe max and exact count");
                    check(named.getCount() == size && named.getMaxStackSize() == size,
                            "fallback transform does not mutate authoritative stack");
                }
                var ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
                var encoded = ItemStack.CODEC.encodeStart(ops, named).getOrThrow();
                var decoded = ItemStack.CODEC.parse(ops, encoded).getOrThrow();
                check(decoded.getCount() == size && decoded.getHoverName().getString().equals("Stackables saved quantity"), "persistent codec preserves count and name");
                for (int count : new int[]{size, 99, 100, 32768, Integer.MAX_VALUE}) {
                    var exact = new ItemStack(Items.STONE, count);
                    exact.set(DataComponents.MAX_STACK_SIZE, Integer.MAX_VALUE);
                    var buffer = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), server.registryAccess());
                    try {
                        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, exact);
                        var wire = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
                        check(wire.getCount() == count && wire.getMaxStackSize() == Integer.MAX_VALUE, "wire boundary " + count);
                    } finally { buffer.release(); }
                }
                if (!Files.exists(Path.of("stackables-phase-one.txt"))) {
                level.getChunkAt(new BlockPos(8, 81, 8));
                var area = new AABB(5,78,5,12,85,12);
                long before = level.getEntitiesOfClass(ItemEntity.class, area).stream().mapToLong(e -> e.getItem().getCount()).sum();
                Containers.dropItemStack(level, 8, 81, 8, new ItemStack(Items.STONE, size));
                long after = level.getEntitiesOfClass(ItemEntity.class, area).stream().mapToLong(e -> e.getItem().getCount()).sum();
                check(after - before == size, "container drops conserve all items before=" + before + " after=" + after);
                }
                var pos = new BlockPos(0, -60, 0);
                level.getChunkAt(pos);
                boolean restart = Files.exists(Path.of("stackables-phase-one.txt"));
                if (restart) {
                    var chest = (ChestBlockEntity) level.getBlockEntity(pos);
                    check(chest != null && chest.getItem(0).getCount() == size, "restart preserves actual chest quantity");
                    check(chest.getItem(0).getHoverName().getString().equals("Stackables saved quantity"), "restart preserves item name");
                } else {
                    level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
                    var chest = (ChestBlockEntity) level.getBlockEntity(pos);
                    chest.setItem(0, named); chest.setChanged();
                    Files.writeString(Path.of("stackables-phase-one.txt"), "restart pending\n");
                }
                result.addProperty("passed", true);result.addProperty("uncapped", uncap);
                result.addProperty("common_stack_size", size);result.addProperty("restart", restart);result.addProperty("assertions", assertions);
            } catch (Throwable failure) {
                failure.printStackTrace();result.addProperty("passed", false);result.addProperty("error",failure.toString());
            } finally {
                try { Files.writeString(Path.of("stackables-result.json"), result.toString()); } catch (Exception failure) {throw new RuntimeException(failure);}
                server.halt(false);
            }
        });
    }
    private static final class PolymerChecks {
        static ItemStack transform(ItemStack stack, net.minecraft.server.MinecraftServer server) throws ReflectiveOperationException {
            var connection = new Connection(PacketFlow.SERVERBOUND);
            var context = new net.fabricmc.fabric.api.networking.v1.context.PacketContext() {
                @SuppressWarnings("unchecked") public <T> T get(ReadKey<T> key) {
                    if (key == REGISTRY_ACCESS) return (T) server.registryAccess();
                    if (key == SERVER_INSTANCE) return (T) server;
                    if (key == CONNECTION) return (T) connection;
                    return null;
                }
                public <T> void set(Key<T> key, T value) { throw new UnsupportedOperationException(); }
            };
            return (ItemStack) Class.forName("eu.pb4.polymer.core.api.item.PolymerItemUtils")
                    .getMethod("getPolymerItemStack", ItemStack.class,
                            net.fabricmc.fabric.api.networking.v1.context.PacketContext.class,
                            net.minecraft.core.HolderLookup.Provider.class)
                    .invoke(null, stack, context, server.registryAccess());
        }
    }
}
