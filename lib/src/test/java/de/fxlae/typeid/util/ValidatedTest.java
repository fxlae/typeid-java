package de.fxlae.typeid.util;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidatedTest {

    static final String VALUE = "the value";
    static final String ERROR_MESSAGE = "the error message";

    static final Validated<String> VALID_VALIDATED = Validated.valid(VALUE);
    static final Validated<String> INVALID_VALIDATED = Validated.invalid(ERROR_MESSAGE);

    @Test
    void validShouldCreateValidInstanceWhenValid() {
        assertThat(VALID_VALIDATED)
                .isNotNull()
                .isInstanceOfSatisfying(
                        Validated.Valid.class,
                        v -> assertThat(v.value()).isEqualTo(VALUE));
    }

    @Test
    void invalidShouldCreateInvalidInstanceWhenInvalid() {
        assertThat(INVALID_VALIDATED)
                .isNotNull()
                .isInstanceOfSatisfying(
                        Validated.Invalid.class,
                        iv -> assertThat(iv.message()).isEqualTo(ERROR_MESSAGE));
    }

    @Test
    void isValidShouldReturnTrueWhenValid() {
        assertThat(VALID_VALIDATED.isValid()).isTrue();
    }

    @Test
    void isValidShouldReturnFalseWhenInvalid() {
        assertThat(INVALID_VALIDATED.isValid()).isFalse();
    }

    @Test
    void toOptionalShouldReturnNonEmptyOptionalWhenValid() {
        assertThat(VALID_VALIDATED.toOptional()).contains(VALUE);
    }

    @Test
    void toOptionalShouldReturnEmptyOptionalWhenInvalid() {
        assertThat(INVALID_VALIDATED.toOptional()).isEmpty();
    }

    @Test
    void getShouldReturnValueWhenValid() {
        assertThat(VALID_VALIDATED.get()).isEqualTo(VALUE);
    }

    @Test
    void getShouldThrowWhenInvalid() {
        assertThatThrownBy(INVALID_VALIDATED::get)
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(ERROR_MESSAGE);
    }

    @Test
    void orElseShouldReturnValueWhenValid() {
        assertThat(VALID_VALIDATED.orElse("other")).isEqualTo(VALUE);
    }

    @Test
    void orElseShouldReturnOtherWhenValid() {
        assertThat(INVALID_VALIDATED.orElse("other")).isEqualTo("other");
        assertThat(INVALID_VALIDATED.orElse(null)).isNull();
    }

    @Test
    void messageShouldReturnMessageWhenInvalid() {
        assertThat(INVALID_VALIDATED.message()).isEqualTo(ERROR_MESSAGE);
    }

    @Test
    void messageShouldThrowWhenValid() {
        assertThatThrownBy(VALID_VALIDATED::message)
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void mapShouldApplyWhenValid() {
        Validated<Integer> validated = Validated.valid(1);
        Validated<String> mapped = validated.map(Object::toString);
        assertThat(mapped.get()).isEqualTo("1");
    }

    @Test
    void mapShouldNotApplyWhenInvalid() {
        Validated<String> mapped = INVALID_VALIDATED.map(Object::toString);
        assertThat(mapped).isInstanceOfSatisfying(
                Validated.Invalid.class,
                iv -> assertThat(iv.message()).isEqualTo(ERROR_MESSAGE));
    }

    @Test
    void flatMapShouldApplyWhenValid() {
        Validated<Integer> validated = Validated.valid(1);
        Validated<String> mapped1 = validated.flatMap(i -> Validated.valid(i.toString()));
        Validated<String> mapped2 = validated.flatMap(i -> Validated.invalid(ERROR_MESSAGE));

        assertThat(mapped1.get()).isEqualTo("1");
        assertThat(mapped2).isInstanceOfSatisfying(
                Validated.Invalid.class,
                iv -> assertThat(iv.message()).isEqualTo(ERROR_MESSAGE));
    }

    @Test
    void flatMapShouldNotApplyWhenInvalid() {
        Validated<String> mapped = INVALID_VALIDATED.flatMap(Validated::valid);
        assertThat(mapped).isInstanceOfSatisfying(
                Validated.Invalid.class,
                iv -> assertThat(iv.message()).isEqualTo(ERROR_MESSAGE));
    }

    @Test
    void filterShouldApplyForMatchingPredicateWhenValid() {
        Validated<String> filtered = VALID_VALIDATED.filter(ERROR_MESSAGE, v -> v.equals(VALUE));
        assertThat(filtered).isEqualTo(VALID_VALIDATED);
    }

    @Test
    void filterShouldNotApplyForNotMatchingPredicateWhenValid() {
        Validated<String> filtered = VALID_VALIDATED.filter(ERROR_MESSAGE, v -> v.equals("something else"));
        assertThat(filtered).isInstanceOfSatisfying(
                Validated.Invalid.class,
                iv -> assertThat(iv.message()).isEqualTo(ERROR_MESSAGE));
    }

    @Test
    void filterShouldNotApplyWhenInvalid() {
        Validated<String> filtered = INVALID_VALIDATED.filter("yet another " + ERROR_MESSAGE, v -> v.equals("something else"));
        assertThat(filtered).isInstanceOfSatisfying(
                Validated.Invalid.class,
                iv -> assertThat(iv.message()).isEqualTo(ERROR_MESSAGE));
    }

    @Test
    void ifValidShouldExecuteWhenValid() {
        AtomicReference<String> str = new AtomicReference<>(null);
        VALID_VALIDATED.ifValid(str::set);
        assertThat(str.get()).isEqualTo(VALUE);
    }

    @Test
    void ifValidShouldNotExecuteWhenInvalid() {
        AtomicReference<String> str = new AtomicReference<>(null);
        INVALID_VALIDATED.ifValid(str::set);
        assertThat(str.get()).isNull();
    }

    @Test
    void ifInvalidShouldExecuteWhenInvalid() {
        AtomicReference<String> str = new AtomicReference<>(null);
        INVALID_VALIDATED.ifInvalid(str::set);
        assertThat(str.get()).isEqualTo(ERROR_MESSAGE);
    }

    @Test
    void ifInvalidShouldNotExecuteWhenValid() {
        AtomicReference<String> str = new AtomicReference<>(null);
        VALID_VALIDATED.ifInvalid(str::set);
        assertThat(str.get()).isNull();
    }

    @Test
    void shouldThrowWhenCalledWithNullParams() {

        assertThatThrownBy(() -> Validated.valid(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Validated.invalid(null)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> VALID_VALIDATED.filter(null, v -> v.equals(VALUE))).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> INVALID_VALIDATED.filter(null, v -> v.equals(VALUE))).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> VALID_VALIDATED.filter(ERROR_MESSAGE, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> INVALID_VALIDATED.filter(ERROR_MESSAGE, null)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> VALID_VALIDATED.map(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> INVALID_VALIDATED.map(null)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> VALID_VALIDATED.flatMap(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> INVALID_VALIDATED.flatMap(null)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> VALID_VALIDATED.ifValid(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> INVALID_VALIDATED.ifValid(null)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> VALID_VALIDATED.ifInvalid(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> INVALID_VALIDATED.ifInvalid(null)).isInstanceOf(NullPointerException.class);
    }
}
