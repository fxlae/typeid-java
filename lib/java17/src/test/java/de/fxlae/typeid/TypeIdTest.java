package de.fxlae.typeid;

import de.fxlae.typeid.util.Validated;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeIdTest extends AbstractTypeIdTest {

    @Override
    TypeIdStaticContext createStaticFacade() {
        return new TypeIdFacade();
    }

    @Test
    void shouldReturnAValidValidatedOnParseSuccess() {
        Validated<TypeId> validated = TypeId.parseToValidated(AbstractTypeIdTest.SOME_TYPE_ID);
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
}
