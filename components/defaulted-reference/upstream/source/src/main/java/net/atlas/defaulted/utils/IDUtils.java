package net.atlas.defaulted.utils;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class IDUtils {
	public static <T> Identifier identifier(ResourceKey<T> key) {
		//? >= 1.21.11 {
		return key.identifier();
		//?} <1.21.11 {
		/*return key.location();
		*///?}
	}

	public static <S> Identifier getId(CommandContext<S> context, String name) {
		return context.getArgument(name, Identifier.class);
	}

    public static ArgumentType<Identifier> id() {
        return IdentifierArgument.id();
    }
}