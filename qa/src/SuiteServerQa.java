package suite.qa;

import com.google.gson.JsonObject;
import com.thenathe.suite.network.SuiteCapabilities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import de.dafuqs.chalk.common.blocks.ChalkMarkBlock;
import java.nio.file.*;

/** Bounded connection assertions only: gameplay is documented for manual testing. */
public final class SuiteServerQa implements ModInitializer {
    private static final java.util.Map<String, Integer> JOINS = new java.util.HashMap<>();
    @Override public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) -> dispatcher.register(
                Commands.literal("suite-qa-reconfigure").then(Commands.argument("player", EntityArgument.player()).executes(context -> {
                    ServerPlayNetworking.reconfigure(EntityArgument.getPlayer(context, "player"));
                    return 1;
                }))));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            JsonObject result = new JsonObject();
            String name = handler.player.getGameProfile().name();
            boolean expected = name.startsWith("SuiteNative") || name.equals("SuiteMismatch");
            boolean passed = true;
            JsonObject selected = new JsonObject();
            for (String mod : SuiteCapabilities.MODULES) {
                boolean actual = SuiteCapabilities.isNative(handler.getPacketContext(), mod);
                selected.addProperty(mod, actual);
                boolean moduleExpected = expected && !(name.equals("SuiteMismatch") && mod.equals("simple_smithing_overhaul"));
                passed &= actual == moduleExpected;
            }
            var position = handler.player.blockPosition().offset(2, 0, 0);
            var level = handler.player.level();
            level.setBlock(position.below(), Blocks.STONE.defaultBlockState(), 3);
            var mark = BuiltInRegistries.BLOCK.getValue(Identifier.parse("chalk:red_glow_chalk_mark")).defaultBlockState()
                    .setValue(ChalkMarkBlock.FACING, Direction.UP).setValue(ChalkMarkBlock.ORIENTATION, 3);
            level.setBlock(position, mark, 3);
            ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("chalk:red_glow_chalk")));
            stack.setDamageValue(13);
            stack.set(DataComponents.CUSTOM_NAME, Component.literal("Suite QA Chalk"));
            handler.player.getInventory().setItem(0, stack);
            handler.player.inventoryMenu.broadcastFullState();
            JsonObject marker = new JsonObject();
            marker.addProperty("x",position.getX());marker.addProperty("y",position.getY());marker.addProperty("z",position.getZ());
            marker.addProperty("orientation",3);marker.addProperty("facing","up");marker.addProperty("damage",13);marker.addProperty("custom_name","Suite QA Chalk");
            marker.addProperty("server_block",BuiltInRegistries.BLOCK.getKey(level.getBlockState(position).getBlock()).toString());
            marker.addProperty("server_item",BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            result.add("marker",marker);
            passed &= level.getBlockState(position).equals(mark);
            var loader = FabricLoader.getInstance();
            result.addProperty("player", handler.player.getGameProfile().name());
            result.addProperty("expected_native", expected);
            result.addProperty("join_count", JOINS.merge(name, 1, Integer::sum));
            result.addProperty("intentional_sso_mismatch", name.equals("SuiteMismatch"));
            result.addProperty("polymer", loader.isModLoaded("polymer-core"));
            boolean local = ((net.fabricmc.fabric.mixin.networking.accessor.ServerCommonPacketListenerImplAccessor) handler).getConnection().isMemoryConnection();
            result.addProperty("local_memory_connection", local);
            Integer bits = handler.getPacketContext().get(com.thenathe.chalkcompat.NativeClients.STATE_BITS);
            result.addProperty("chalk_state_bits", bits);
            if (!local && loader.isModLoaded("polymer-core") && SuiteCapabilities.isNative(handler.getPacketContext(), "chalk")) passed &= bits != null && bits > 0;
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
