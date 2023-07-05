package de.fxlae.typeid;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * A test facade for testing both Java 8 and Java 17 variants with a common test set
 */
public interface TypeIdStaticContext {
    TypeIdInstance generate(String prefix);
    TypeIdInstance generate();
    TypeIdInstance of(UUID uuid);
    TypeIdInstance of(String prefix, UUID uuid);
    TypeIdInstance parse(final String text);
    Optional<TypeIdInstance> parseToOptional(final String text);
    <O> O parse(
            final String text,
            Function<TypeIdInstance, O> okHandler,
            Function<String, O> errorHandler);
}
