package net.atlas.defaulted.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.base.BasePatches;
import net.atlas.defaulted.base.BasePatchesBuilder;
import net.atlas.defaulted.base.PatchType;
import net.atlas.defaulted.command.argument.PatchTypeArgument;
import net.atlas.defaulted.utils.CommonUtils;
import net.atlas.defaulted.utils.IDUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

import java.io.*;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static net.atlas.defaulted.utils.ComponentMapUtils.components;
import static net.atlas.defaulted.utils.ComponentMapUtils.prototype;

@SuppressWarnings("NoTranslation")
public class DefaultedCommand {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final DynamicCommandExceptionType ERROR_NON_EXISTING_BUILDER = new DynamicCommandExceptionType(
            (id) -> Component.translatableWithFallback("argument.defaulted.non_existing_builder", "Patch builder %s does not exist!", id.toString())
    );
    public static final DynamicCommandExceptionType ERROR_EXISTING_BUILDER = new DynamicCommandExceptionType(
            (id) -> Component.translatableWithFallback("argument.defaulted.existing_builder", "Patch builder %s already exists, please build or discard before adding a new one!", id.toString())
    );

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext context) {
        commandDispatcher.register(Commands.literal("defaulted")
                .requires(CommonUtils::hasAdminPerms)
                .then(Commands.literal("item")
                        .then(Commands.argument("item", ResourceArgument.resource(context, Registries.ITEM))
                                .then(Commands.argument("to_read", StringArgumentType.word())
                                        .suggests((context1, builder) -> PatchTypeArgument.suggestionsReadCodec(context1, builder, PatchType.ITEM))
                                        .then(Commands.literal("get-prototype").executes(DefaultedCommand::readItemPrototype))
                                        .then(Commands.literal("get-patched").executes(DefaultedCommand::readItemPatch))
                                        .then(Commands.literal("diff").executes(DefaultedCommand::readItemDiff))
                                        .then(Commands.literal("split-diff").executes(DefaultedCommand::readSplitItemDiff)))))
                .then(Commands.literal("enchantment")
                        .then(Commands.argument("enchantment", ResourceArgument.resource(context, Registries.ENCHANTMENT))
                                .then(Commands.argument("to_read", StringArgumentType.word())
                                        .suggests((context1, builder) -> PatchTypeArgument.suggestionsReadCodec(context1, builder, PatchType.ENCHANTMENT))
                                        .then(Commands.literal("get-prototype").executes(DefaultedCommand::readEnchantmentPrototype))
                                        .then(Commands.literal("get-patched").executes(DefaultedCommand::readEnchantmentPatch))
                                        .then(Commands.literal("diff").executes(DefaultedCommand::readEnchantmentDiff))
                                        .then(Commands.literal("split-diff").executes(DefaultedCommand::readSplitEnchantmentDiff)))))
                .then(Commands.literal("generate")
                        .then(Commands.literal("build")
                                .then(Commands.argument("id", IDUtils.id())
                                        .suggests(PatchTypeArgument::suggestionsExistingBuilder)
                                        .executes(DefaultedCommand::buildPatch)))
                        .then(Commands.literal("discard")
                                .then(Commands.argument("id", IDUtils.id())
                                        .suggests(PatchTypeArgument::suggestionsExistingBuilder)
                                        .executes(DefaultedCommand::discardPatch)))
                        .then(Commands.literal("start")
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests(PatchTypeArgument::suggestions)
                                        .then(Commands.argument("id", IDUtils.id())
                                                .executes(DefaultedCommand::createUniversal)
                                                .then(Commands.argument("elements", StringArgumentType.greedyString())
                                                        .executes(DefaultedCommand::create)))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("id", IDUtils.id())
                                        .suggests(PatchTypeArgument::suggestionsExistingBuilder)
                                        .then(Commands.argument("to_write", StringArgumentType.word())
                                                .suggests(((context1, builder) -> PatchTypeArgument.suggestionsEditCodec(context1, builder, "id")))
                                                .then(Commands.argument("value", StringArgumentType.greedyString()).executes(DefaultedCommand::setValue)))))
                        .then(Commands.literal("append-patch-generator")
                                .then(Commands.argument("id", IDUtils.id())
                                        .suggests(PatchTypeArgument::suggestionsExistingBuilder)
                                        .then(Commands.argument("generator", StringArgumentType.greedyString())
                                                .executes(DefaultedCommand::appendPatchGenerator))))
                        .then(Commands.literal("priority")
                                .then(Commands.argument("id", IDUtils.id())
                                        .suggests(PatchTypeArgument::suggestionsExistingBuilder)
                                        .then(Commands.argument("priority", IntegerArgumentType.integer())
                                                .executes(DefaultedCommand::priority))))));
    }

    private static int createUniversal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        PatchType<?, ?, ?, ?> patchType = PatchTypeArgument.getPatchType(context, "type");
        Identifier id = IDUtils.getId(context, "id");
        if (!patchType.addBuilder(id)) throw ERROR_EXISTING_BUILDER.create(id);
        emit(context, Collections.singletonList(started(id)));
        return 0;
    }

    private static int create(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        PatchType<?, ?, ?, ?> patchType = PatchTypeArgument.getPatchType(context, "type");
        Identifier id = IDUtils.getId(context, "id");
        String elements = StringArgumentType.getString(context, "elements");
        if (!patchType.addBuilder(id, new StringReader(elements), context.getSource().registryAccess())) throw ERROR_EXISTING_BUILDER.create(id);
        emit(context, Collections.singletonList(started(id)));
        return 0;
    }

    private static int discardPatch(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier id = IDUtils.getId(context, "id");
        PatchType<?, ?, ?, ?> patchType = PatchType.forId(id);
        if (patchType == null) throw ERROR_NON_EXISTING_BUILDER.create(id);
        patchType.removeBuilder(id);
        emit(context, Collections.singletonList(discarded(id)));
        return 0;
    }

    private static int buildPatch(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier id = IDUtils.getId(context, "id");
        PatchType<?, ?, ?, ?> patchType = PatchType.forId(id);
        if (patchType == null) throw ERROR_NON_EXISTING_BUILDER.create(id);
        BasePatches<?, ?> patches = patchType.get(id).build();
        JsonElement root = patches.save(context.getSource().registryAccess());
        Path file = CommonUtils.createAndValidatePath(id, patches, context.getSource().getLevel());
        try {
            save(file, root);
        } catch (IOException e) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(new StringReader(""), e.getMessage());
        }
        patchType.removeBuilder(id);
        emit(context, Collections.singletonList(outputToFile(id, file.toString())));
        return 0;
    }

    public static void save(final Path file, final JsonElement root) throws IOException {
        Path parent = file.getParent();
        if (parent == null) return;
        FileUtil.createDirectoriesSafe(parent);
        File outputFile = file.toFile();
        if (!outputFile.createNewFile()) throw new IOException("Could not create " + outputFile.getAbsolutePath());
        try (PrintWriter writer = new PrintWriter(outputFile)) {
            GSON.toJson(root, writer);
        }
    }

    private static int appendPatchGenerator(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier id = IDUtils.getId(context, "id");
        PatchType<?, ?, ?, ?> patchType = PatchType.forId(id);
        if (patchType == null) throw ERROR_NON_EXISTING_BUILDER.create(id);
        BasePatchesBuilder<?, ?, ?, ?> patches = patchType.get(id);
        String generator = StringArgumentType.getString(context, "generator");
        patches.addGeneratorRaw(CommonUtils.parse(new StringReader(generator), context.getSource().registryAccess(), patchType.generatorCodec()));
        emit(context, Collections.singletonList(append(id, generator)));
        return 0;
    }

    private static int setValue(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier id = IDUtils.getId(context, "id");
        PatchType<?, ?, ?, ?> patchType = PatchType.forId(id);
        if (patchType == null) throw ERROR_NON_EXISTING_BUILDER.create(id);
        String value = StringArgumentType.getString(context, "value");
        String toWrite = PatchTypeArgument.writeValue(context, id, "to_write", value, patchType);
        emit(context, Collections.singletonList(set(toWrite, id, value)));
        return 0;
    }

    private static int priority(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier id = IDUtils.getId(context, "id");
        PatchType<?, ?, ?, ?> patchType = PatchType.forId(id);
        if (patchType == null) throw ERROR_NON_EXISTING_BUILDER.create(id);
        BasePatchesBuilder<?, ?, ?, ?> patches = patchType.get(id);
        int priority = IntegerArgumentType.getInteger(context, "priority");
        patches.setPriority(priority);
        emit(context, Collections.singletonList(set("priority", id, String.valueOf(priority))));
        return 0;
    }

    private static Component started(Identifier id) {
        return Component.translatableWithFallback("commands.defaulted.started_patch_builder", "Patch builder started with id %s.", id.toString());
    }

    private static Component discarded(Identifier id) {
        return Component.translatableWithFallback("commands.defaulted.discarded_patch_builder", "Patch builder with id %s discarded.", id.toString());
    }

    private static Component outputToFile(Identifier id, String path) {
        return Component.translatableWithFallback("commands.defaulted.output_patch", "Patch %s output to %s successfully.", id.toString(), path);
    }

    private static Component append(Identifier id, String value) {
        return Component.translatableWithFallback("commands.defaulted.append_patch_generator", "Added %s to patch generators for patch builder %s successfully.", value, id.toString());
    }

    private static Component set(String field, Identifier id, String value) {
        return Component.translatableWithFallback("commands.defaulted.set", "The %s for patch builder %s was set to %s successfully.", field, id.toString(), value);
    }

    private static int readItemPrototype(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Holder.Reference<Item> reference = ResourceArgument.getResource(context, "item", Registries.ITEM);
        emit(context, CommonUtils.processTag(PatchTypeArgument.getReadValue(context, "to_read", PatchType.ITEM, prototype(reference))));
        return 0;
    }

    private static int readItemPatch(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Holder.Reference<Item> reference = ResourceArgument.getResource(context, "item", Registries.ITEM);
        emit(context, CommonUtils.processTag(PatchTypeArgument.getReadValue(context, "to_read", PatchType.ITEM, components(reference))));
        return 0;
    }

    private static int readItemDiff(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return itemDiff(context, true);
    }

    private static int readSplitItemDiff(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return itemDiff(context, false);
    }

    private static int readEnchantmentPrototype(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Holder.Reference<Enchantment> reference = ResourceArgument.getResource(context, "enchantment", Registries.ENCHANTMENT);
        emit(context, CommonUtils.processTag(PatchTypeArgument.getReadValue(context, "to_read", PatchType.ENCHANTMENT, Defaulted.getOriginalEnchantment(reference))));
        return 0;
    }

    private static int readEnchantmentPatch(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Holder.Reference<Enchantment> reference = ResourceArgument.getResource(context, "enchantment", Registries.ENCHANTMENT);
        emit(context, CommonUtils.processTag(PatchTypeArgument.getReadValue(context, "to_read", PatchType.ENCHANTMENT, reference.value())));
        return 0;
    }

    private static int readEnchantmentDiff(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return enchantmentDiff(context, true);
    }

    private static int readSplitEnchantmentDiff(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return enchantmentDiff(context, false);
    }

    private static int itemDiff(CommandContext<CommandSourceStack> context, boolean unified) throws CommandSyntaxException {
        Holder.Reference<Item> reference = ResourceArgument.getResource(context, "item", Registries.ITEM);
        String prototype = PatchTypeArgument.getReadValue(context, "to_read", PatchType.ITEM, prototype(reference));
        String patched = PatchTypeArgument.getReadValue(context, "to_read", PatchType.ITEM, components(reference));
        emitDiff(context, prototype, patched, unified);
        return 0;
    }

    private static int enchantmentDiff(CommandContext<CommandSourceStack> context, boolean unified) throws CommandSyntaxException {
        Holder.Reference<Enchantment> reference = ResourceArgument.getResource(context, "enchantment", Registries.ENCHANTMENT);
        String prototype = PatchTypeArgument.getReadValue(context, "to_read", PatchType.ENCHANTMENT, Defaulted.getOriginalEnchantment(reference));
        String patched = PatchTypeArgument.getReadValue(context, "to_read", PatchType.ENCHANTMENT, reference.value());
        emitDiff(context, prototype, patched, unified);
        return 0;
    }

    private static void emitDiff(CommandContext<CommandSourceStack> context, String prototype, String patched, boolean unified) {
        emit(context, unified ? CommonUtils.unifiedDiff(prototype, patched) : CommonUtils.splitDiff(prototype, patched));
    }

    private static void emit(CommandContext<CommandSourceStack> context, List<? extends Component> output) {
        output.forEach(component -> context.getSource().sendSuccess(() -> component, false));
    }
}
