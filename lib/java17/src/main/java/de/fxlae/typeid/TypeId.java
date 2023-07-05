package de.fxlae.typeid;

import de.fxlae.typeid.lib.TypeIdLib;
import de.fxlae.typeid.util.Validated;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static de.fxlae.typeid.lib.TypeIdLib.encode;

/**
 * A {@code record} for representing TypeIDs.
 *
 * @param prefix the prefix of the {@link TypeId} to create. Might be an empty string, but not null.
 * @param uuid   the {@link UUID} of the {@link TypeId} to create.
 */
public record TypeId(String prefix, UUID uuid) {

    /**
     * @param prefix the prefix
     * @param uuid   the UUID
     * @throws NullPointerException     if the prefix and/or UUID is null
     * @throws IllegalArgumentException if the prefix is invalid
     */
    public TypeId {

        Objects.requireNonNull(prefix);
        Objects.requireNonNull(uuid);

        String err = TypeIdLib.validatePrefixOnInput(prefix, prefix.length());
        if (err != TypeIdLib.VALID_REF) {
            throw new IllegalArgumentException(err);
        }
    }

    /**
     * Creates a new prefixed {@link TypeId} based on UUIDv7.
     *
     * @param prefix the prefix to use
     * @return the new {@link TypeId}
     * @throws NullPointerException     if the prefix is null
     * @throws IllegalArgumentException if the prefix is invalid
     */
    public static TypeId generate(String prefix) {
        return of(prefix, TypeIdLib.getUuidV7());
    }

    /**
     * Creates a new {@link TypeId} without prefix, based on UUIDv7.
     * <p> Note: "no prefix" means empty string, not null.
     *
     * @return the new {@link TypeId}.
     */
    public static TypeId generate() {
        return of("", TypeIdLib.getUuidV7());
    }

    /**
     * Creates a new {@link TypeId} without prefix, based on the given {@link UUID}.
     * <p> The {@link UUID} can be of any version.
     * <p> Note: "no prefix" means empty string, not null.
     *
     * @param uuid the {@link UUID} to use
     * @return the new {@link TypeId}
     * @throws NullPointerException if the UUID is null
     */
    public static TypeId of(UUID uuid) {
        return of("", uuid);
    }

    /**
     * Creates a new {@link TypeId}, based on the given prefix and {@link UUID}.
     *
     * @param prefix the prefix to use
     * @param uuid   the {@link UUID} to use
     * @return the new {@link TypeId}
     * @throws NullPointerException     if the prefix and/or UUID is null
     * @throws IllegalArgumentException if the prefix is invalid
     */
    public static TypeId of(String prefix, UUID uuid) {
        return new TypeId(prefix, uuid);
    }

    /**
     * Parses the textual representation of a TypeID and returns a {@link TypeId} instance.
     *
     * @param text the textual representation.
     * @return the new {@link TypeId}.
     * @throws NullPointerException     if the text is null
     * @throws IllegalArgumentException if the text is invalid
     */
    public static TypeId parse(final String text) {

        int separatorIndex = TypeIdLib.findSeparatorIndex(text);
        String err = TypeIdLib.validateInput(text, separatorIndex);

        if (err != TypeIdLib.VALID_REF) {
            throw new IllegalArgumentException(err);
        }

        return new TypeId(
                TypeIdLib.extractPrefix(text, separatorIndex),
                TypeIdLib.decodeSuffixOnInput(text, separatorIndex));
    }

    /**
     * Parses the textual representation of a TypeID and returns an {@link Optional}.
     *
     * @param text the textual representation of the TypeID
     * @return an {@link Optional} containing a {@link TypeId} or an empty {@link TypeId} in case of validation errors
     * @throws NullPointerException if the text is null
     */
    public static Optional<TypeId> parseToOptional(final String text) {

        int separatorIndex = TypeIdLib.findSeparatorIndex(text);
        String err = TypeIdLib.validateInput(text, separatorIndex);

        if (err != TypeIdLib.VALID_REF) {
            return Optional.empty();
        }

        return Optional.of(new TypeId(
                TypeIdLib.extractPrefix(text, separatorIndex),
                TypeIdLib.decodeSuffixOnInput(text, separatorIndex)));
    }

    /**
     * Parses the textual representation of a TypeID and returns a {@link Validated}.
     *
     * @param text the textual representation of the TypeID
     * @return a valid {@link Validated} containing a {@link TypeId} or an invalid {@link Validated} with an error message
     * @throws NullPointerException if the text is null
     */
    public static Validated<TypeId> parseToValidated(final String text) {

        int separatorIndex = TypeIdLib.findSeparatorIndex(text);
        String err = TypeIdLib.validateInput(text, separatorIndex);

        if (err != TypeIdLib.VALID_REF) {
            return Validated.invalid(err);
        }

        return Validated.valid(new TypeId(
                TypeIdLib.extractPrefix(text, separatorIndex),
                TypeIdLib.decodeSuffixOnInput(text, separatorIndex)));
    }

    /**
     * Parses the textual representation of a TypeID and executes a handler {@link Function}, depending
     * on the outcome. Both provided functions must have the same return type.
     *
     * @param text         the textual representation of the TypeID
     * @param okHandler    the {@link Function} that is executed if the TypeID is valid, providing the {@link TypeId}
     * @param errorHandler the {@link Function} that is executed if the TypeID could not be parsed, providing the error message
     * @param <T>          the result type of the handler {@link Function} that was executed
     * @return the result of the handler {@link Function} that was executed
     * @throws NullPointerException if the okHandler and/or errorHandler is null
     */
    public static <T> T parse(
            final String text,
            Function<TypeId, T> okHandler,
            Function<String, T> errorHandler) {

        Objects.requireNonNull(okHandler);
        Objects.requireNonNull(errorHandler);

        int separatorIndex = TypeIdLib.findSeparatorIndex(text);
        String err = TypeIdLib.validateInput(text, separatorIndex);

        if (err != TypeIdLib.VALID_REF) {
            return errorHandler.apply(err);
        }

        TypeId typeId = new TypeId(
                TypeIdLib.extractPrefix(text, separatorIndex),
                TypeIdLib.decodeSuffixOnInput(text, separatorIndex));

        return okHandler.apply(typeId);
    }


    /**
     * Returns the textual representation of this {@link TypeId}.
     *
     * @return the textual representation.
     */
    @Override
    public String toString() {
        return encode(prefix, uuid);
    }

}