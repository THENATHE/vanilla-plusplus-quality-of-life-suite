package net.atlas.defaulted.extension;

public interface ReferenceHolderExtensions {
    default <T> void defaulted$forceBind(T newValue) {
        throw new IllegalStateException("Extension has not been applied");
    }
}
