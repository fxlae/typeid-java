package de.fxlae.typeid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This class tests against the specification published on
 * <a href="https://github.com/jetpack-io/typeid/tree/main/spec">github.com/jetpack-io/typeid</a>,
 * especially against <a href="https://github.com/jetpack-io/typeid/blob/main/spec/valid.yml">valid.yml</a>
 * and
 * <a href="https://github.com/jetpack-io/typeid/blob/main/spec/invalid.yml">invalid.yml</a>.
 *
 */
class SpecTest {

    @ParameterizedTest
    @MethodSource("provideSpecValid")
    void testEncodeAgainstSpecValid(String name, String typeIdAsString, String prefix, UUID uuid) {
        TypeId typeId = TypeId.of(prefix, uuid);
        assertEquals(typeIdAsString, typeId.toString());
    }

    @ParameterizedTest
    @MethodSource("provideSpecValid")
    void testDecodeAgainstSpecValid(String name, String typeIdAsString, String prefix, UUID uuid) {
        TypeId typeId = TypeId.parse(typeIdAsString);
        assertEquals(prefix, typeId.getPrefix());
        assertEquals(uuid, typeId.getUuid());
    }

    @ParameterizedTest
    @MethodSource("provideSpecInvalid")
    void testDecodeAgainstSpecInvalid(String name, String typeIdAsString, String description) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> TypeId.parse(typeIdAsString));
    }

    private static Stream<Arguments> provideSpecValid() throws IOException {
        return loadSpec(new File("src/test/resources/spec/valid.yml"), SpecValid.class)
                        .stream()
                        .map(s -> Arguments.of(s.name, s.typeid, s.prefix, UUID.fromString(s.uuid)));
    }

    private static Stream<Arguments> provideSpecInvalid() throws IOException {
        return loadSpec(new File("src/test/resources/spec/invalid.yml"), SpecInvalid.class)
                .stream()
                .map(s -> Arguments.of(s.name, s.typeid, s.description));
    }

    /**
     * Unit tests based on randomness have their pitfalls, however,
     * the specification recommends back-and-forth testing with a large number of random ids.
     */
    @Test
    void testRandomIds() {
        for (int i = 0; i < 2000; i++) {
            UUID uuid = UUID.randomUUID();
            TypeId typeId1 = TypeId.of("test", uuid);
            TypeId typeId2 = TypeId.parse(typeId1.toString());
            assertEquals("test", typeId2.getPrefix());
            assertEquals(uuid, typeId2.getUuid());
        }
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

    static <T> List<T> loadSpec(File file, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        CollectionType javaType = mapper.getTypeFactory()
                .constructCollectionType(List.class, clazz);
        return mapper.readValue(file, javaType);
    }
}