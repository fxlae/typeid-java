package de.fxlae.typeid.lib;


import de.fxlae.typeid.SpecTest;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeIdLibTest {

    static final UUID TEST_UUID = UUID.fromString("01890a5d-ac96-774b-bcce-b302099a8057");

    @ParameterizedTest
    @ArgumentsSource(InvalidTypeIdProvider.class)
    void parseInvalid(String typeIdAsString) {
        Optional<String> result = TypeIdLib.parse(
                typeIdAsString,
                (prefix, uuid) -> Optional.empty(),
                Optional::of);
        assertThat(result).isNotEmpty();
    }

    public static class InvalidTypeIdProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    "",
                    "_",
                    "someprefix_", // no suffix at all
                    "_01h455vb4pex5vsknk084sn02q", // suffix only, but with the preceding underscore
                    "__01h455vb4pex5vsknk084sn02q", // prefix is single underscore
                    "_someprefix_01h455vb4pex5vsknk084sn02q", // prefix starts with underscore
                    "someprefix__01h455vb4pex5vsknk084sn02q", // prefix ends with underscore
                    "_someprefix__01h455vb4pex5vsknk084sn02q", // prefix starts and ends with underscore
                    "sömeprefix_01h455vb4pex5vsknk084sn02q", // prefix with 'ö'
                    "someprefix_01h455öb4pex5vsknk084sn02q", // suffix with 'ö'
                    "someprefix_Ă01h455b4pex5vsknk084sn02q", // suffix with 'Ă' (> ascii 255) as first char
                    "sOmeprefix_01h455vb4pex5vsknk084sn02q", // prefix with 'O'
                    "someprefix_01h455Vb4pex5vsknk084sn02q", // suffix with 'V'
                    "someprefix_01h455lb4pex5vsknk084sn02q", // suffix with 'l'
                    "ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss_01h455vb4pex5vsknk084sn02q", // prefix with 64 chars
                    "someprefix_01h455vb4pex5vsknk084sn02", // suffix with 25 chars
                    "someprefix_01h455vb4pex5vsknk084sn02q2", // suffix with 27 chars
                    "someprefix_81h455vb4pex5vsknk084sn02q" // leftmost suffix char is != 0-7
            ).map(Arguments::of);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(ValidTypeIdProvider.class)
    void parseValid(String typeIdAsString, String expectedPrefix, UUID expectedUuid) {
        Optional<Tuple<String, UUID>> result = TypeIdLib.parse(
                typeIdAsString,
                (prefix, uuid) -> Optional.of(new Tuple<>(prefix, uuid)),
                (message) -> Optional.empty());
        assertThat(result).hasValue(new Tuple<>(expectedPrefix, expectedUuid));
    }

    public static class ValidTypeIdProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("01h455vb4pex5vsknk084sn02q", "", TEST_UUID), // suffix only
                    Arguments.of("abcdefghijklmnopqrstuvw_01h455vb4pex5vsknk084sn02q", "abcdefghijklmnopqrstuvw", TEST_UUID), // prefix with allowed chars
                    Arguments.of("some_prefix_01h455vb4pex5vsknk084sn02q", "some_prefix", TEST_UUID), // prefix with underscore
                    Arguments.of("sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss_01h455vb4pex5vsknk084sn02q", "sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss", TEST_UUID) // prefix with 63 chars
            );
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SpecTest.SpecValidProvider.class)
    void parseValidAgainstSpec(String name, String typeIdAsString, String expectedPrefix, UUID expectedUuid) {
        Optional<Tuple<String, UUID>> result = TypeIdLib.parse(
                typeIdAsString,
                (prefix, uuid) -> Optional.of(new Tuple<>(prefix, uuid)),
                (message) -> Optional.empty());
        assertThat(result).hasValue(new Tuple<>(expectedPrefix, expectedUuid));
    }

    @ParameterizedTest
    @ArgumentsSource(SpecTest.SpecValidProvider.class)
    void encodeValidAgainstSpec(String name, String expectedTypeIdAsString, String prefix, UUID uuid) {
        var typeIdAsString = TypeIdLib.encode(prefix, uuid);
        assertEquals(expectedTypeIdAsString, typeIdAsString);
    }

    @ParameterizedTest
    @ArgumentsSource(SpecTest.SpecInvalidProvider.class)
    void parseInvalidAgainstSpec(String name, String typeIdAsString, String description) {
        Optional<String> result = TypeIdLib.parse(
                typeIdAsString,
                (prefix, uuid) -> Optional.empty(),
                Optional::of);
        assertThat(result).isNotEmpty();
    }

    record Tuple<A, B>(A first, B second) {
    }
}
