package de.fxlae.typeid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This class tests against the specification published on
 * <a href="https://github.com/jetpack-io/typeid/tree/main/spec">github.com/jetpack-io/typeid</a>,
 * specifically against <a href="https://github.com/jetpack-io/typeid/blob/main/spec/valid.yml">valid.yml</a>
 * and
 * <a href="https://github.com/jetpack-io/typeid/blob/main/spec/invalid.yml">invalid.yml</a>.
 */
public class SpecTest {

    @ParameterizedTest
    @ArgumentsSource(SpecValidProvider.class)
    void testEncodeAgainstSpecValid(String name, String expectedTypeIdAsString, String prefix, UUID uuid) {
        var typeId = TypeId.of(prefix, uuid);
        assertEquals(expectedTypeIdAsString, typeId.toString());
    }

    @ParameterizedTest
    @ArgumentsSource(SpecValidProvider.class)
    void testDecodeAgainstSpecValid(String name, String typeIdAsString, String expectedPrefix, UUID expectedUuid) {
        var typeId = TypeId.parse(typeIdAsString);
        assertEquals(expectedPrefix, typeId.prefix());
        assertEquals(expectedUuid, typeId.uuid());
    }

    @ParameterizedTest
    @ArgumentsSource(SpecInvalidProvider.class)
    void testDecodeAgainstSpecInvalid(String name, String typeIdAsString, String description) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> TypeId.parse(typeIdAsString), description);
    }

    /**
     * Unit tests based on randomness have their pitfalls, however,
     * the specification recommends back-and-forth testing with a large number of random ids.
     */
    @Test
    void testRandomIds() {
        for (int i = 0; i < 2000; i++) {
            UUID uuid = UUID.randomUUID();
            var typeId1 = TypeId.of("test", uuid);
            var typeId2 = TypeId.parse(typeId1.toString());
            assertEquals("test", typeId2.prefix());
            assertEquals(uuid, typeId2.uuid());
        }
    }

    public static class SpecValidProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws IOException {
            return loadSpec("/spec/valid.yml", SpecValid.class)
                    .stream()
                    .map(s -> Arguments.of(s.name, s.typeid, s.prefix, UUID.fromString(s.uuid)));
        }
    }

    public static class SpecInvalidProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws IOException {
            return loadSpec("/spec/invalid.yml", SpecInvalid.class)
                    .stream()
                    .map(s -> Arguments.of(s.name, s.typeid, s.description));
        }
    }

    static <T> List<T> loadSpec(String path, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        CollectionType javaType = mapper.getTypeFactory()
                .constructCollectionType(List.class, clazz);
        return mapper.readValue(SpecTest.class.getResourceAsStream(path), javaType);
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    static class SpecValid {
        String name;
        String typeid;
        String prefix;
        String uuid;
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    static class SpecInvalid {
        String name;
        String typeid;
        String description;
    }
}