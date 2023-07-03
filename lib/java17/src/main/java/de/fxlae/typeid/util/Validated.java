package de.fxlae.typeid.util;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A container for a value that is either valid or not.
 * The two states are represented by {@link Valid} and {@link Invalid} respectively.
 * In this library, in is used to represent the result of parsing a TypeId string. However,
 * the data structure itself is completely independent of TypeIDs.
 *
 * @param <T> the type of the contained value.
 */
public sealed interface Validated<T> {

    /**
     * Returns a new valid {@link Validated} with the given value.
     *
     * @param value the value to wrap
     * @param <T>   the type of the value
     * @return the valid {@link Validated}
     * @throws NullPointerException if the provided value is null
     */
    static <T> Validated<T> valid(T value) {
        return new Valid<>(Objects.requireNonNull(value));
    }

    /**
     * Returns a new invalid {@link Validated} with the given error message.
     *
     * @param message the error message
     * @param <T>     the type of the invalid value
     * @return the invalid {@link Validated}
     * @throws NullPointerException if the message is null
     */
    static <T> Validated<T> invalid(String message) {
        return new Invalid<>(Objects.requireNonNull(message));
    }

    /**
     * Returns an {@link Optional} with the valid value, otherwise an empty {@link Optional}.
     * In the latter case, the error message is lost.
     *
     * @return the {@link Optional}
     */
    Optional<T> toOptional();

    /**
     * Returns {@code true} if the value is valid, otherwise {@code false}.
     *
     * @return {@code true} if the value is valid, otherwise {@code false}
     */
    boolean isValid();

    /**
     * Returns the value if it's valid, otherwise throws.
     *
     * @return the valid value
     * @throws NoSuchElementException if the value is invalid
     */
    T get();

    /**
     * Returns the value if valid, otherwise return {@code other}.
     *
     * @param other the value to be returned if the value is invalid, can be {@code null}
     * @return the value if valid, else {@code other}
     */
    T orElse(T other);

    /**
     * Returns the message if it's invalid, otherwise throws.
     *
     * @return the message
     * @throws NoSuchElementException if the value is valid
     */
    String message();

    /**
     * Applies the provided mapping function if the value is valid, otherwise returns the current instance
     *
     * @param mapper the mapping function
     * @param <O>    The type of the mapping function's result
     * @return the result of applying the mapping function to the value of this {@link  Validated} instance
     * @throws NullPointerException if the mapping function is null
     */
    <O> Validated<O> map(Function<? super T, ? extends O> mapper);

    /**
     * Applies the provided mapping function if the value is valid, otherwise returns the current instance
     *
     * @param mapper the mapping function
     * @param <O>    The type parameter of the mapping function's returned {@link  Validated}
     * @return the result of applying the mapping function to the value of this {@link  Validated} instance
     * @throws NullPointerException if the mapping function is null
     */
    <O> Validated<O> flatMap(Function<? super T, Validated<O>> mapper);

    /**
     * If the value is valid and matches the predicate, return it as a valid
     * {@link Validated}, otherwise return an invalid {@link Validated} with the given
     * error message. If the value is invalid in the first place, return the current invalid
     * {@link Validated}
     *
     * @param message   the message in case the predicate doesn't match
     * @param predicate the predicate that checks the value
     * @return the resulting {@link Validated}
     * @throws NullPointerException if the message and/or predicate is null
     */
    Validated<T> filter(String message, Predicate<? super T> predicate);

    /**
     * Applies the consuming function if the value is valid, otherwise does nothing.
     *
     * @param valueConsumer the value {@link Consumer}
     * @throws NullPointerException if the {@link Consumer} is null
     */
    void ifValid(Consumer<T> valueConsumer);

    /**
     * Applies the message consuming function if the value is invalid, otherwise does nothing.
     *
     * @param messageConsumer the message {@link Consumer}
     * @throws NullPointerException if the {@link Consumer} is null
     */
    void ifInvalid(Consumer<String> messageConsumer);

    /**
     * Implementation of a "valid {@link Validated}".
     *
     * @param value the value to wrap
     * @param <T>   the type of the value
     */
    record Valid<T>(T value) implements Validated<T> {

        /**
         * @param value the value to wrap
         * @throws NullPointerException if the value is null
         */
        public Valid {
            Objects.requireNonNull(value);
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.of(value);
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public T orElse(T other) {
            return value;
        }

        @Override
        public String message() {
            throw new NoSuchElementException("no message, element is valid");
        }

        @Override
        public <O> Validated<O> map(Function<? super T, ? extends O> mapper) {
            Objects.requireNonNull(mapper);
            return valid(mapper.apply(value));
        }

        @Override
        public <O> Validated<O> flatMap(Function<? super T, Validated<O>> mapper) {
            Objects.requireNonNull(mapper);
            return mapper.apply(value);
        }

        @Override
        public Validated<T> filter(String message, Predicate<? super T> predicate) {
            Objects.requireNonNull(message);
            Objects.requireNonNull(predicate);
            if (predicate.test(value)) {
                return this;
            } else {
                return invalid(message);
            }
        }

        @Override
        public void ifValid(Consumer<T> valueConsumer) {
            Objects.requireNonNull(valueConsumer);
            valueConsumer.accept(value);
        }

        @Override
        public void ifInvalid(Consumer<String> messageConsumer) {
            Objects.requireNonNull(messageConsumer);
            // do nothing
        }
    }

    /**
     * Implementation of an "invalid {@link Validated}".
     *
     * @param message the error message
     */
    record Invalid<T>(String message) implements Validated<T> {

        /**
         * @param message the error message
         * @throws NullPointerException if the value is null
         */
        public Invalid {
            Objects.requireNonNull(message);
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.empty();
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public T get() {
            throw new NoSuchElementException("Validation failed: " + message);
        }

        @Override
        public T orElse(T other) {
            return other;
        }

        @Override
        public <O> Validated<O> map(Function<? super T, ? extends O> mapper) {
            Objects.requireNonNull(mapper);
            return invalid(message);
        }

        @Override
        public <O> Validated<O> flatMap(Function<? super T, Validated<O>> mapper) {
            Objects.requireNonNull(mapper);
            return invalid(message);
        }

        @Override
        public Validated<T> filter(String message, Predicate<? super T> predicate) {
            Objects.requireNonNull(message);
            Objects.requireNonNull(predicate);
            return this;
        }

        @Override
        public void ifValid(Consumer<T> valueConsumer) {
            Objects.requireNonNull(valueConsumer);
            // do nothing
        }

        @Override
        public void ifInvalid(Consumer<String> messageConsumer) {
            Objects.requireNonNull(messageConsumer);
            messageConsumer.accept(message);
        }
    }

}
