package net.atlas.defaulted.extension;

public interface ItemStackExtensions {
    default void defaulted$updatePrototype() {
        throw new IllegalStateException("Extension has not been applied");
    }
}
