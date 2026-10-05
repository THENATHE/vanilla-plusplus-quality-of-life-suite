package net.atlas.defaulted.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import net.atlas.defaulted.base.PatchType;
import net.atlas.defaulted.utils.CommonUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;

import java.util.concurrent.CompletableFuture;

public record PatchTypeArgument() {
    public static <S extends CommandSourceStack> PatchType<?, ?, ?, ?> getPatchType(final CommandContext<S> context, String name) throws CommandSyntaxException {
        String type = context.getArgument(name, String.class);
        return CommonUtils.parse(new StringReader(type), context.getSource().registryAccess(), PatchType.CODEC);
    }
    public static <S> CompletableFuture<Suggestions> suggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        return SharedSuggestionProvider.suggest(PatchType.values(), suggestionsBuilder);
    }
    public static <S extends CommandSourceStack, D> String getReadValue(final CommandContext<S> context, String name, PatchType<?, D, ?, ?> toChoose, D value) throws CommandSyntaxException {
        String readCodecName = context.getArgument(name, String.class);
        Codec<Object> readCodec = toChoose.forReadArg(readCodecName);
        if (readCodec == null) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
        return CommonUtils.readValueOf(toChoose.get(readCodecName, value), readCodec, context.getSource().registryAccess().createSerializationContext(NbtOps.INSTANCE));
    }
    public static <S extends CommandSourceStack> CompletableFuture<Suggestions> suggestionsReadCodec(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder, PatchType<?, ?, ?, ?> type) {
        return SharedSuggestionProvider.suggest(type.readArgs(), suggestionsBuilder);
    }
    public static <S extends CommandSourceStack, T> String writeValue(final CommandContext<S> context, Identifier id, String name, String input, PatchType<T, ?, ?, ?> toChoose) throws CommandSyntaxException {
        String editCodecName = context.getArgument(name, String.class);
        Codec<Object> codec = toChoose.forArg(editCodecName);
        if (codec == null) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
        toChoose.get(id).writeData(editCodecName, CommonUtils.parse(new StringReader(input), context.getSource().registryAccess(), codec));
        return editCodecName;
    }
    public static <S extends CommandSourceStack> CompletableFuture<Suggestions> suggestionsEditCodec(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder, String idName) throws CommandSyntaxException {
        Identifier id = commandContext.getArgument(idName, Identifier.class);
        PatchType<?, ?, ?, ?> type = PatchType.forId(id);
        if (type == null) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
        return SharedSuggestionProvider.suggest(type.args(), suggestionsBuilder);
    }
    public static <S extends CommandSourceStack> CompletableFuture<Suggestions> suggestionsExistingBuilder(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        return SharedSuggestionProvider.suggestResource(PatchType.builders(), suggestionsBuilder);
    }
}