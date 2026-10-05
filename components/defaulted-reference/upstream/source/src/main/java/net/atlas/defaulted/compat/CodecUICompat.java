package net.atlas.defaulted.compat;

import net.atlas.defaulted.codec.HeterogeneousHolderSetListCodec;
import net.atlas.defaulted.codec.TagOnlyHolderSetCodec;
import net.mehvahdjukaar.codecui.internal.SchemaResolver;

public class CodecUICompat {
    public static void registerSchemaHandlers() {
        SchemaResolver.registerHandler((codec, resolver) -> {
            if (codec instanceof TagOnlyHolderSetCodec<?> hs) {
                return resolver.resolve(hs.getCodec());
            }
            if (codec instanceof HeterogeneousHolderSetListCodec<?> hs) {
                return resolver.resolve(hs.getListCodec());
            }
            return null;
        });
    }
}
