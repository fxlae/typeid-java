package de.fxlae.typeid;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class TypeIdFacade implements TypeIdStaticContext {
    @Override
    public TypeIdInstance generate(String prefix) {
        return wrap(TypeId.generate(prefix));
    }

    @Override
    public TypeIdInstance generate() {
        return wrap(TypeId.generate());
    }

    @Override
    public TypeIdInstance of(UUID uuid) {
        return wrap(TypeId.of(uuid));
    }

    @Override
    public TypeIdInstance of(String prefix, UUID uuid) {
        return wrap(TypeId.of(prefix, uuid));
    }

    @Override
    public TypeIdInstance parse(String text) {
        return wrap(TypeId.parse(text));
    }

    @Override
    public Optional<TypeIdInstance> parseToOptional(String text) {
        return TypeId.parseToOptional(text).map(this::wrap);
    }

    @Override
    public <O> O parse(String text, Function<TypeIdInstance, O> okHandler, Function<String, O> errorHandler) {
        Function<TypeId, TypeIdInstance> wrapAsFunction = (this::wrap);
        return TypeId.parse(text, wrapAsFunction.andThen(okHandler), errorHandler);
    }

    private TypeIdInstance wrap(final TypeId typeId) {

        return new TypeIdInstance() {

            @Override
            public String prefix() {
                return typeId.prefix();
            }

            @Override
            public UUID uuid() {
                return typeId.uuid();
            }

            @Override
            public String toString() {
                return typeId.toString();
            }

            @Override
            public Object getWrapped() {
                return typeId;
            }

        };
    }
}
