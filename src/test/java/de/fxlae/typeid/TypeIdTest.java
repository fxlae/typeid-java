package de.fxlae.typeid;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Note: the actual encoding and decoding of TypeIDs is mainly tested by {@link SpecTest}.
 * <p> This class is indented to test auxiliary methods, e.g. regarding the construction of {@link TypeId} instances.
 */
class TypeIdTest {

    static final UUID SOME_UUID = UUID.fromString("01890a5d-ac96-774b-bcce-b302099a8057");
    static final String SOME_PREFIX = "theprefix";
    static final String SOME_SUFFIX = "01h455vb4pex5vsknk084sn02q";

    @Test
    void generateShouldReturnTypeIdForUuidV7() {
        TypeId typeId = TypeId.generate();
        assertNotNull(typeId);
        assertAll(
                () -> assertEquals("", typeId.getPrefix()),
                () -> assertEquals('7', typeId.getUuid().toString().charAt(14)));
    }

    @Test
    void generateWithPrefixShouldReturnTypeIdForUuidV7() {
        TypeId typeId = TypeId.generate(SOME_PREFIX);
        assertNotNull(typeId);
        assertAll(
                () -> assertEquals(SOME_PREFIX, typeId.getPrefix()),
                () -> assertEquals('7', typeId.getUuid().toString().charAt(14)));
    }

    @Test
    void generateWithInvalidPrefixShouldFail() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> TypeId.generate("i think this prefix is not allowed")),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> TypeId.generate(null)));
    }

    @Test
    void ofWithUuidShouldReturnTypeId() {
        TypeId typeId = TypeId.of(SOME_UUID);
        assertNotNull(typeId);
        assertAll(
                () -> assertEquals("", typeId.getPrefix()),
                () -> assertEquals(SOME_UUID, typeId.getUuid()));
    }

    @Test
    void ofWithInvalidUuidShouldFail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> TypeId.of(null));
    }

    @Test
    void ofWithPrefixAndUuidShouldReturnTypeId() {
        TypeId typeId = TypeId.of(SOME_PREFIX, SOME_UUID);
        assertNotNull(typeId);
        assertAll(
                () -> assertEquals(SOME_PREFIX, typeId.getPrefix()),
                () -> assertEquals(SOME_UUID, typeId.getUuid()));
    }

    @Test
    void ofWithInvalidPrefixOrInvalidUuidShouldFail() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> TypeId.of("i think this prefix is not allowed", SOME_UUID)),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> TypeId.of(null, SOME_UUID)),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> TypeId.of(SOME_PREFIX, null))
        );
    }

    @Test
    void parseWithoutPrefixWithSuffixShouldReturnTypeId() {
        TypeId typeId = TypeId.parse(SOME_SUFFIX);
        assertNotNull(typeId);
        assertAll(
                () -> assertEquals("", typeId.getPrefix()),
                () -> assertEquals(SOME_UUID, typeId.getUuid()));
    }

    @Test
    void parseWithPrefixWithSuffixShouldReturnTypeId() {
        TypeId typeId = TypeId.parse(SOME_PREFIX + "_" + SOME_SUFFIX);
        assertNotNull(typeId);
        assertAll(
                () -> assertEquals(SOME_PREFIX, typeId.getPrefix()),
                () -> assertEquals(SOME_UUID, typeId.getUuid()));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "01h455vb4pex5vsknk084sn02q", // suffix only
            "abcdefghijklmnopqrstuvw_01h455vb4pex5vsknk084sn02q", // prefix with all allowed chars
            "sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss_01h455vb4pex5vsknk084sn02q" // prefix with 63 chars
    })
    void parseWithValidInputsShouldReturnTypeId(String input) {
        TypeId typeId = TypeId.parse(input);
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
            "someprefix_01h455vb4pex5vsknk084sn02q2" // suffix with 27 chars
    })
    void parseForInvalidInputShouldFail(String input) {
        assertThrows(
                IllegalArgumentException.class,
                () -> TypeId.parse(input));
    }

    @Test
    void toStringShouldReturnTypeIdAsString() {
        TypeId typeId = TypeId.of(SOME_PREFIX, SOME_UUID);
        assertNotNull(typeId);
        assertEquals(SOME_PREFIX + "_" + SOME_SUFFIX, typeId.toString());
    }

    @Test
    void toStringWithoutPrefixShouldReturnTypeIdAsStringWithoutUnderscore() {
        TypeId typeId = TypeId.of(SOME_UUID);
        assertNotNull(typeId);
        assertEquals(SOME_SUFFIX, typeId.toString());
    }

}