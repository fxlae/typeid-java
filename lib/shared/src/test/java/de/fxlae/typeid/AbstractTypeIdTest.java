package de.fxlae.typeid;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Note: the actual encoding and decoding of TypeIDs is mainly tested by {@link AbstractSpecTest}.
 * <p> This class is indented to test auxiliary methods, e.g. regarding the construction of TypeId instances.
 */
public abstract class AbstractTypeIdTest {

    static final UUID SOME_UUID = UUID.fromString("01890a5d-ac96-774b-bcce-b302099a8057");
    static final String SOME_PREFIX = "theprefix";
    static final String SOME_SUFFIX = "01h455vb4pex5vsknk084sn02q";
    static final String SOME_TYPE_ID = SOME_PREFIX + "_" + SOME_SUFFIX;

    TypeIdStaticContext staticFacade;

    @BeforeEach
    void setupFacade() {
        this.staticFacade = createStaticFacade();
    }

    abstract TypeIdStaticContext createStaticFacade();

    @Test
    void generateShouldReturnTypeIdForUuidV7() {
        TypeIdInstance typeId = staticFacade.generate();
        assertNotNull(typeId);
        assertAll(
                () -> assertEquals("", typeId.prefix()),
                () -> assertEquals('7', typeId.uuid().toString().charAt(14)));
    }

    @Test
    void generateWithPrefixShouldReturnTypeIdForUuidV7() {
        TypeIdInstance typeId = staticFacade.generate(SOME_PREFIX);
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
                        () -> staticFacade.generate("i think this prefix is not allowed")));
    }

    @Test
    void generateWithNullPrefixShouldFail() {
        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> staticFacade.generate(null)));
    }

    @Test
    void ofWithUuidShouldReturnTypeId() {
        TypeIdInstance typeId = staticFacade.of(SOME_UUID);
        assertNotNull(typeId);
        assertAll(
                () -> assertEquals("", typeId.prefix()),
                () -> assertEquals(SOME_UUID, typeId.uuid()));
    }

    @Test
    void ofWithNullUuidShouldFail() {
        assertThrows(
                NullPointerException.class,
                () -> staticFacade.of(null));
    }

    @Test
    void ofWithPrefixAndUuidShouldReturnTypeId() {
        TypeIdInstance typeId = staticFacade.of(SOME_PREFIX, SOME_UUID);
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
                        () -> staticFacade.of("i think this prefix is not allowed", SOME_UUID))
        );
    }

    @Test
    void ofWithNullPrefixOrNullUuidShouldFail() {
        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> staticFacade.of(null, SOME_UUID)),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> staticFacade.of(SOME_PREFIX, null))
        );
    }

    @Test
    void parseWithoutPrefixWithSuffixShouldReturnTypeId() {
        TypeIdInstance typeId = staticFacade.parse(SOME_SUFFIX);
        assertNotNull(typeId);
        assertAll(
                () -> assertEquals("", typeId.prefix()),
                () -> assertEquals(SOME_UUID, typeId.uuid()));
    }

    @Test
    void parseWithPrefixWithSuffixShouldReturnTypeId() {
        TypeIdInstance typeId = staticFacade.parse(SOME_PREFIX + "_" + SOME_SUFFIX);
        assertNotNull(typeId);
        assertAll(
                () -> assertEquals(SOME_PREFIX, typeId.prefix()),
                () -> assertEquals(SOME_UUID, typeId.uuid()));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "01h455vb4pex5vsknk084sn02q", // suffix only
            "abcdefghijklmnopqrstuvw_01h455vb4pex5vsknk084sn02q", // prefix with all allowed chars
            "sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss_01h455vb4pex5vsknk084sn02q" // prefix with 63 chars
    })
    void parseWithValidInputsShouldReturnTypeId(String input) {
        TypeIdInstance typeId = staticFacade.parse(input);
        assertNotNull(typeId);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "_",
            "someprefix_", // no suffix at all
            "_01h455vb4pex5vsknk084sn02q", // suffix only, but with preceding underscore
            "sömeprefix_01h455vb4pex5vsknk084sn02q", // prefix with 'ö'
            "someprefix_01h455öb4pex5vsknk084sn02q", // suffix with 'ö'
            "sOmeprefix_01h455vb4pex5vsknk084sn02q", // prefix with 'O'
            "someprefix_01h455Vb4pex5vsknk084sn02q", // suffix with 'V'
            "someprefix_01h455lb4pex5vsknk084sn02q", // suffix with 'l'
            "ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss_01h455vb4pex5vsknk084sn02q", // prefix with 64 chars
            "someprefix_01h455vb4pex5vsknk084sn02", // suffix with 25 chars
            "someprefix_01h455vb4pex5vsknk084sn02q2", // suffix with 27 chars
            "someprefix_81h455vb4pex5vsknk084sn02q" // leftmost suffix char is != 0-7
    })
    void parseWithInvalidInputShouldFail(String input) {
        assertThrows(
                IllegalArgumentException.class,
                () -> staticFacade.parse(input));
    }

    @Test
    void parseWithNullInputShouldFail() {
        assertThrows(
                NullPointerException.class,
                () -> staticFacade.parse(null));
    }

    @Test
    void parseWithHandlersShouldReturnTypeIdOnSuccess() {

        String result = staticFacade.parse(SOME_TYPE_ID,
                TypeIdInstance::toString,
                error -> error);

        assertNotNull(result);
        assertEquals(SOME_TYPE_ID, result);
    }

    @Test
    void parseWithHandlersShouldReturnMessageOnFailure() {

        String result = staticFacade.parse("?_" + SOME_SUFFIX,
                TypeIdInstance::toString,
                error -> error);

        assertNotNull(result);
        assertEquals("Illegal character in prefix, must be one of [a-z]", result);
    }

    @Test
    void parseToOptionalShouldReturnNonEmptyOptionalOnParseSuccess() {
        Optional<TypeIdInstance> result = staticFacade.parseToOptional(SOME_TYPE_ID);
        assertThat(result).isNotEmpty();
        assertThat(result.get().prefix()).isEqualTo(SOME_PREFIX);
        assertThat(result.get().uuid()).isEqualTo(SOME_UUID);
    }

    @Test
    void parseToOptionalShouldReturnEmptyOptionalOnParseFailure() {
        Optional<TypeIdInstance> result = staticFacade.parseToOptional("some invalid typeid");
        assertThat(result).isEmpty();
    }

    @Test
    void toStringShouldReturnTypeIdAsString() {
        TypeIdInstance typeId = staticFacade.of(SOME_PREFIX, SOME_UUID);
        assertNotNull(typeId);
        assertEquals(SOME_PREFIX + "_" + SOME_SUFFIX, typeId.toString());
    }

    @Test
    void toStringWithoutPrefixShouldReturnTypeIdAsStringWithoutUnderscore() {
        TypeIdInstance typeId = staticFacade.of(SOME_UUID);
        assertNotNull(typeId);
        assertEquals(SOME_SUFFIX, typeId.toString());
    }

    @Test
    void equalsShouldSucceedForEqualTypeIds() {

        TypeIdInstance typeIdA = staticFacade.of(SOME_UUID);
        TypeIdInstance typeIdB = staticFacade.of(SOME_UUID);

        // reflexivity
        assertEquals(typeIdA.getWrapped(), typeIdA.getWrapped());

        // symmetry
        assertEquals(typeIdA.getWrapped(), typeIdB.getWrapped());
        assertEquals(typeIdB.getWrapped(), typeIdA.getWrapped());
    }

    @Test
    void equalsShouldFailForAnythingElse() {

        TypeIdInstance typeIdA = staticFacade.of(SOME_UUID);
        TypeIdInstance typeIdB = staticFacade.of("different", SOME_UUID);
        TypeIdInstance typeIdC = staticFacade.of(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        Object otherType = new Object();

        assertNotEquals(typeIdA.getWrapped(), typeIdB.getWrapped());
        assertNotEquals(typeIdA.getWrapped(), typeIdC.getWrapped());
        assertNotEquals(null, typeIdA.getWrapped());
        assertNotEquals(typeIdA.getWrapped(), otherType);
    }

    @Test
    void hashCodeShouldBeTheSameForSameTypeIds() {
        TypeIdInstance typeIdA = staticFacade.of(SOME_UUID);
        TypeIdInstance typeIdB = staticFacade.of(SOME_UUID);
        assertEquals(typeIdA.getWrapped().hashCode(), typeIdB.getWrapped().hashCode());
    }

}