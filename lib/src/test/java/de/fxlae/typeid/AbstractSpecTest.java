package de.fxlae.typeid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
 */
abstract class AbstractSpecTest {

    TypeIdStaticContext staticFacade;

    private static Stream<Arguments> provideSpecValid() throws IOException {
        return loadSpec("/spec/valid.yml", SpecValid.class)
                .stream()
                .map(s -> Arguments.of(s.name, s.typeid, s.prefix, UUID.fromString(s.uuid)));
    }

    private static Stream<Arguments> provideSpecInvalid() throws IOException {
        return loadSpec("/spec/invalid.yml", SpecInvalid.class)
                .stream()
                .map(s -> Arguments.of(s.name, s.typeid, s.description));
    }

    static <T> List<T> loadSpec(String path, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        CollectionType javaType = mapper.getTypeFactory()
                .constructCollectionType(List.class, clazz);
        return mapper.readValue(AbstractSpecTest.class.getResourceAsStream(path), javaType);
    }

    @BeforeEach
    void setupFacade() {
        this.staticFacade = createStaticFacade();
    }

    abstract TypeIdStaticContext createStaticFacade();

    @ParameterizedTest
    @MethodSource("provideSpecValid")
    void testEncodeAgainstSpecValid(String name, String typeIdAsString, String prefix, UUID uuid) {
        TypeIdInstance typeId = staticFacade.of(prefix, uuid);
        assertEquals(typeIdAsString, typeId.toString());
    }

    @ParameterizedTest
    @MethodSource("provideSpecValid")
    void testDecodeAgainstSpecValid(String name, String typeIdAsString, String prefix, UUID uuid) {
        TypeIdInstance typeId = staticFacade.parse(typeIdAsString);
        assertEquals(prefix, typeId.prefix());
        assertEquals(uuid, typeId.uuid());
    }

    @ParameterizedTest
    @MethodSource("provideSpecInvalid")
    void testDecodeAgainstSpecInvalid(String name, String typeIdAsString, String description) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> staticFacade.parse(typeIdAsString), description);
    }

    /**
     * Unit tests based on randomness have their pitfalls, however,
     * the specification recommends back-and-forth testing with a large number of random ids.
     */
    @Test
    void testRandomIds() {
        for (int i = 0; i < 2000; i++) {
            UUID uuid = UUID.randomUUID();
            TypeIdInstance typeId1 = staticFacade.of("test", uuid);
            TypeIdInstance typeId2 = staticFacade.parse(typeId1.toString());
            assertEquals("test", typeId2.prefix());
            assertEquals(uuid, typeId2.uuid());
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
}