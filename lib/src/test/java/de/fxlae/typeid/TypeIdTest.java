package de.fxlae.typeid;

import de.fxlae.typeid.lib.TypeIdLibTest;
import de.fxlae.typeid.util.Validated;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Note: the actual encoding and decoding of TypeIDs is mainly tested by {@link SpecTest}.
 * <p> This class is indented to test auxiliary methods, e.g. regarding the construction of TypeId instances.
 */
class TypeIdTest {

    static final UUID SOME_UUID = UUID.fromString("01890a5d-ac96-774b-bcce-b302099a8057");
    static final String SOME_PREFIX = "theprefix";
    static final String SOME_SUFFIX = "01h455vb4pex5vsknk084sn02q";
    static final String SOME_TYPE_ID = SOME_PREFIX + "_" + SOME_SUFFIX;

    @Test
    void generateShouldReturnTypeIdForUuidV7() {
        var typeId = TypeId.generate();
        assertNotNull(typeId);
        assertAll(
                () -> assertEquals("", typeId.prefix()),
                () -> assertEquals('7', typeId.uuid().toString().charAt(14)));
    }

    @Test
    void generateWithPrefixShouldReturnTypeIdForUuidV7() {
        var typeId = TypeId.generate(SOME_PREFIX);
        assertNotNull(typeId);
        assertAll(
                () -> assertEquals(SOME_PREFIX, typeId.prefix()),
                () -> assertEquals('7', typeId.uuid().toString().charAt(14)));
    }

    @Test
    void generateWithInvalidPrefixShouldFail() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> TypeId.generate("i think this prefix is not allowed")));
    }

    @Test
    void generateWithNullPrefixShouldFail() {
        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> TypeId.generate(null)));
    }

    @Test
    void ofWithUuidShouldReturnTypeId() {
        var typeId = TypeId.of(SOME_UUID);
        assertNotNull(typeId);
        assertAll(
                () -> assertEquals("", typeId.prefix()),
                () -> assertEquals(SOME_UUID, typeId.uuid()));
    }

    @Test
    void ofWithNullUuidShouldFail() {
        assertThrows(
                NullPointerException.class,
                () -> TypeId.of(null));
    }

    @Test
    void ofWithPrefixAndUuidShouldReturnTypeId() {
        var typeId = TypeId.of(SOME_PREFIX, SOME_UUID);
        assertNotNull(typeId);
        assertAll(
                () -> assertEquals(SOME_PREFIX, typeId.prefix()),
                () -> assertEquals(SOME_UUID, typeId.uuid()));
    }

    @Test
    void ofWithInvalidPrefixOrInvalidUuidShouldFail() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> TypeId.of("i think this prefix is not allowed", SOME_UUID))
        );
    }

    @Test
    void ofWithNullPrefixOrNullUuidShouldFail() {
        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> TypeId.of(null, SOME_UUID)),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> TypeId.of(SOME_PREFIX, null))
        );
    }

    @Test
    void parseWithoutPrefixWithSuffixShouldReturnTypeId() {
        var typeId = TypeId.parse(SOME_SUFFIX);
        assertNotNull(typeId);
        assertAll(
                () -> assertEquals("", typeId.prefix()),
                () -> assertEquals(SOME_UUID, typeId.uuid()));
    }

    @Test
    void parseWithPrefixWithSuffixShouldReturnTypeId() {
        var typeId = TypeId.parse(SOME_PREFIX + "_" + SOME_SUFFIX);
        assertNotNull(typeId);
        assertAll(
                () -> assertEquals(SOME_PREFIX, typeId.prefix()),
                () -> assertEquals(SOME_UUID, typeId.uuid()));
    }

    @ParameterizedTest
    @ArgumentsSource(TypeIdLibTest.ValidTypeIdProvider.class)
    void parseWithValidInputsShouldReturnTypeId(String typeIdAsString, String expectedPrefix, UUID expectedUuid) {
        var typeId = TypeId.parse(typeIdAsString);
        assertThat(typeId.prefix()).isEqualTo(expectedPrefix);
        assertThat(typeId.uuid()).isEqualTo(expectedUuid);
    }

    @ParameterizedTest
    @ArgumentsSource(TypeIdLibTest.InvalidTypeIdProvider.class)
    void parseWithInvalidInputShouldThrow(String typeIdAsString) {
        assertThrows(
                IllegalArgumentException.class,
                () -> TypeId.parse(typeIdAsString));
    }

    @ParameterizedTest
    @ArgumentsSource(TypeIdLibTest.ValidTypeIdProvider.class)
    void parseToOptionalWithValidInputsShouldReturnOptionalWithTypeId(String typeIdAsString, String expectedPrefix, UUID expectedUuid) {
        var maybeTypeId = TypeId.parseToOptional(typeIdAsString);
        assertThat(maybeTypeId).isNotEmpty();
        assertThat(maybeTypeId.get().prefix()).isEqualTo(expectedPrefix);
        assertThat(maybeTypeId.get().uuid()).isEqualTo(expectedUuid);
    }

    @ParameterizedTest
    @ArgumentsSource(TypeIdLibTest.InvalidTypeIdProvider.class)
    void parseToOptionalWithInvalidInputShouldReturnEmptyOptional(String typeIdAsString) {
        assertThat(TypeId.parseToOptional(typeIdAsString)).isEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(TypeIdLibTest.ValidTypeIdProvider.class)
    void parseToValidatedWithValidInputsShouldReturnValid(String typeIdAsString, String expectedPrefix, UUID expectedUuid) {
        var validatedTypeId = TypeId.parseToValidated(typeIdAsString);
        assertThat(validatedTypeId.isValid()).isTrue();
        assertThat(validatedTypeId.get().prefix()).isEqualTo(expectedPrefix);
        assertThat(validatedTypeId.get().uuid()).isEqualTo(expectedUuid);
        assertThrows(NoSuchElementException.class, validatedTypeId::message);
    }

    @ParameterizedTest
    @ArgumentsSource(TypeIdLibTest.InvalidTypeIdProvider.class)
    void parseToValidatedWithInvalidInputsShouldReturnInvalid(String typeIdAsString) {
        var validatedTypeId = TypeId.parseToValidated(typeIdAsString);
        assertThat(validatedTypeId.isValid()).isFalse();
        assertThat(validatedTypeId.message()).isNotEmpty();
        assertThrows(NoSuchElementException.class, validatedTypeId::get);
    }

    @Test
    void parseWithNullInputShouldFail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> TypeId.parse(null));
    }

    @Test
    void parseWithHandlersShouldReturnTypeIdOnSuccess() {

        String result = TypeId.parse(SOME_TYPE_ID,
                TypeId::toString,
                error -> error);

        assertNotNull(result);
        assertEquals(SOME_TYPE_ID, result);
    }

    @Test
    void parseWithHandlersShouldReturnMessageOnFailure() {

        String result = TypeId.parse("?_" + SOME_SUFFIX,
                TypeId::toString,
                error -> error);

        assertNotNull(result);
        assertEquals("Illegal character in prefix, must be one of [a-z_]", result);
    }

    @Test
    void parseToOptionalShouldReturnNonEmptyOptionalOnParseSuccess() {
        Optional<TypeId> result = TypeId.parseToOptional(SOME_TYPE_ID);
        assertThat(result).isNotEmpty();
        assertThat(result.get().prefix()).isEqualTo(SOME_PREFIX);
        assertThat(result.get().uuid()).isEqualTo(SOME_UUID);
    }

    @Test
    void parseToOptionalShouldReturnEmptyOptionalOnParseFailure() {
        Optional<TypeId> result = TypeId.parseToOptional("some invalid typeid");
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnAValidValidatedOnParseSuccess() {
        Validated<TypeId> validated = TypeId.parseToValidated(TypeIdTest.SOME_TYPE_ID);
        assertThat(validated.isValid()).isTrue();
        assertThat(validated.get().prefix()).isEqualTo(SOME_PREFIX);
        assertThat(validated.get().uuid()).isEqualTo(SOME_UUID);
    }

    @Test
    void shouldReturnAnInvalidValidatedOnParseFailure() {
        Validated<TypeId> validated = TypeId.parseToValidated("some invalid typeid");
        assertThat(validated.isValid()).isFalse();
        assertThat(validated.message()).contains("illegal length");
    }

    @Test
    void toStringShouldReturnTypeIdAsString() {
        var typeId = TypeId.of(SOME_PREFIX, SOME_UUID);
        assertNotNull(typeId);
        assertEquals(SOME_PREFIX + "_" + SOME_SUFFIX, typeId.toString());
    }

    @Test
    void toStringWithoutPrefixShouldReturnTypeIdAsStringWithoutUnderscore() {
        var typeId = TypeId.of(SOME_UUID);
        assertNotNull(typeId);
        assertEquals(SOME_SUFFIX, typeId.toString());
    }

    @Test
    void equalsShouldSucceedForEqualTypeIds() {

        var typeIdA = TypeId.of(SOME_UUID);
        var typeIdB = TypeId.of(SOME_UUID);

        // symmetry
        assertEquals(typeIdA, typeIdB);
        assertEquals(typeIdB, typeIdA);
    }

    @Test
    void equalsShouldFailForAnythingElse() {

        var typeIdA = TypeId.of(SOME_UUID);
        var typeIdB = TypeId.of("different", SOME_UUID);
        var typeIdC = TypeId.of(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        Object otherType = new Object();

        assertNotEquals(typeIdA, typeIdB);
        assertNotEquals(typeIdA, typeIdC);
        assertNotEquals(null, typeIdA);
        assertNotEquals(typeIdA, otherType);
    }

    @Test
    void hashCodeShouldBeTheSameForSameTypeIds() {
        var typeIdA = TypeId.of(SOME_UUID);
        var typeIdB = TypeId.of(SOME_UUID);
        assertEquals(typeIdA.hashCode(), typeIdB.hashCode());
    }

}
