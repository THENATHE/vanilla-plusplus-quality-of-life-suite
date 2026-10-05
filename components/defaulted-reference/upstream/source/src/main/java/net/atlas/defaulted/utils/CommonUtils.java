package net.atlas.defaulted.utils;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.atlas.defaulted.base.BasePatches;
//? <26.1 {
/*import net.atlas.defaulted.mixin.StructureTemplateManagerAccessor;
*///?}
import net.minecraft.ChatFormatting;
//? <26.1 {
/*import net.minecraft.IdentifierException;
*///?}
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.RegistryAccess;
//? <26.1
//import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
//? >=1.21.11
import net.minecraft.server.permissions.Permissions;
//? <26.1 {
/*import net.minecraft.util.FileUtil;

import java.nio.file.InvalidPathException;
*///?}
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommonUtils {
    private static final DiffRowGenerator GENERATOR = DiffRowGenerator.create()
            .showInlineDiffs(true)
            .inlineDiffByWord(true)
            .oldTag(left -> left ? "§c" : "§r§4")
            .newTag(left -> left ? "§a" : "§r§2")
            .build();
    public static Tag readTag(/*? >=1.21.5 {*/ DynamicOps<Tag> ops, /*?}*/ StringReader reader) throws CommandSyntaxException {
        //? >=1.21.5 {
        return TagParser.create(ops).parseAsArgument(reader);
        //?} <1.21.5 {
        /*TagParser parser = new TagParser(reader);
        return parser.readValue();
        *///?}
    }
    public static <A> A parse(StringReader reader, RegistryAccess registryAccess, Codec<A> codec) throws CommandSyntaxException {
        DynamicOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, registryAccess);
        return codec.parse(ops, readTag(/*? >=1.21.5 {*/ ops, /*?}*/ reader))
                .getOrThrow(s -> CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(reader, s));
    }
    public static <A> String readValueOf(A value, Codec<A> codec, DynamicOps<Tag> ops) {
        return codec.encodeStart(ops, value).mapOrElse(tag -> {
            PrettyStringTagVisitor tagVisitor = new PrettyStringTagVisitor();
            tag.accept(tagVisitor);
            return tagVisitor.build();
        }, DataResult.Error::message);
    }
    public static List<MutableComponent> processTag(String str) {
        return Arrays.stream(str.split("\n")).map(s -> Component.literal(s).withStyle(ChatFormatting.GRAY)).toList();
    }
    public static List<MutableComponent> unifiedDiff(String oldStr, String newStr) {
        List<String> oldLines = Arrays.stream(oldStr.split("\n")).toList();
        List<String> newLines = Arrays.stream(newStr.split("\n")).toList();
        List<DiffRow> rows = GENERATOR.generateDiffRows(oldLines, newLines);
        List<MutableComponent> diff = new ArrayList<>();
        rows.forEach(row -> addUnifiedDiffLines(diff, row));
        return diff;
    }
    public static List<MutableComponent> splitDiff(String oldStr, String newStr) {
        List<String> oldLines = Arrays.stream(oldStr.split("\n")).toList();
        List<String> newLines = Arrays.stream(newStr.split("\n")).toList();
        List<DiffRow> rows = GENERATOR.generateDiffRows(oldLines, newLines);
        List<MutableComponent> diff = new ArrayList<>();
        rows.forEach(row -> addSplitDiffLines(diff, row));
        return diff;
    }

    public static void addUnifiedDiffLines(List<MutableComponent> diff, DiffRow diffRow) {
        switch (diffRow.getTag()) {
            case INSERT -> diff.add(Component.literal("§2+")
                    .append(Component.literal("§2" + diffRow.getNewLine())));
            case DELETE -> diff.add(Component.literal("§4-")
                    .append(Component.literal("§4" + diffRow.getOldLine())));
            case CHANGE -> {
                diff.add(Component.literal("§4-")
                        .append(Component.literal("§4" + diffRow.getOldLine())));
                diff.add(Component.literal("§2+")
                        .append(Component.literal("§2" + diffRow.getNewLine())));
            }
            case EQUAL -> diff.add(Component.literal(" §7" + diffRow.getNewLine()));
        }
    }

    public static void addSplitDiffLines(List<MutableComponent> diff, DiffRow diffRow) {
        String verticalBar = "§7 | ";
        switch (diffRow.getTag()) {
            case INSERT -> diff.add(Component.literal("§2+" + diffRow.getNewLine()));
            case DELETE -> diff.add(Component.literal("§4-" + diffRow.getOldLine()));
            case CHANGE -> diff.add(Component.literal("§4-" + diffRow.getOldLine() + verticalBar)
                    .append(Component.literal("§2+" + diffRow.getNewLine())));
            case EQUAL -> diff.add(Component.literal(" §7" + diffRow.getNewLine()));
        }
    }

    public static Path createAndValidatePath(Identifier id, BasePatches<?, ?> patches, ServerLevel level) {
        //? >=26.1 {
        //~ if >=26.3 'getStructureManager' -> 'getStructureTemplateManager'
        return level.getStructureTemplateManager().worldTemplates().createAndValidatePathToStructure(id, FileToIdConverter.registry(patches.key()));
        //?} <26.1 {
        /*return createAndValidatePathToResource(((StructureTemplateManagerAccessor)level.getStructureManager()).getGeneratedDir(), id, FileToIdConverter.json(Registries.elementsDirPath(patches.key())));
        *///?}
    }

    //? <26.1 {
    /*public static Path createAndValidatePathToResource(Path generatedDir, Identifier id, FileToIdConverter converter) {
        if (id.getPath().contains("//")) {
            throw new IdentifierException("Invalid resource path: " + id);
        } else {
            try {
                Path resource = generatedDir.resolve(id.getNamespace() + converter.idToFile(id).getPath());
                if (resource.startsWith(generatedDir) && FileUtil.isPathNormalized(resource) && FileUtil.isPathPortable(resource)) {
                    return resource;
                } else {
                    throw new IdentifierException("Invalid resource path: " + resource);
                }
            } catch (InvalidPathException invalidPathException) {
                throw new IdentifierException("Invalid resource path: " + id, invalidPathException);
            }
        }
    }
    *///?}

    public static boolean hasAdminPerms(CommandSourceStack commandSourceStack) {
        //? >=1.21.11 {
        return commandSourceStack.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
        //?} <1.21.11 {
        /*return commandSourceStack.hasPermission(2);
        *///?}
    }
}
