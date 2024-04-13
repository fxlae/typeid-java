package de.fxlae.typeid;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.UUID;

public class TypeIdBench {

    @Benchmark
    public void of(Blackhole bh, Inputs inputs) {
        bh.consume(TypeId.of(inputs.prefix, inputs.uuid));
    }

    @Benchmark
    public void generate(Blackhole bh, Inputs inputs) {
        bh.consume(TypeId.generate(inputs.prefix));
    }

    @Benchmark
    public void toString(Blackhole bh, Inputs inputs) {
        bh.consume(inputs.typeId.toString());
    }

    @Benchmark
    public void generateAndToString(Blackhole bh, Inputs inputs) {
        TypeId typeId = TypeId.generate(inputs.prefix);
        bh.consume(typeId.toString());
    }

    @Benchmark
    public void ofAndToString(Blackhole bh, Inputs inputs) {
        TypeId typeId = TypeId.of(inputs.prefix, inputs.uuid);
        bh.consume(typeId.toString());
    }

    @Benchmark
    public void parseSuccess(Blackhole bh, Inputs inputs) {
        bh.consume(TypeId.parse(inputs.validTypeId));
    }

    @Benchmark
    public void parseWithHandlersSuccess(Blackhole bh, Inputs inputs) {
        bh.consume(TypeId.parse(inputs.validTypeId,
                typeId -> typeId,
                error -> error));
    }

    @Benchmark
    public void parseToOptionalSuccess(Blackhole bh, Inputs inputs) {
        bh.consume(TypeId.parseToOptional(inputs.validTypeId));
    }

    @Benchmark
    public void parseToValidatedSuccess(Blackhole bh, Inputs inputs) {
        bh.consume(TypeId.parseToValidated(inputs.validTypeId));
    }

    @Benchmark
    public void parseError(Blackhole bh, Inputs inputs) {
        try {
            TypeId.parse(inputs.invalidTypeId);
        } catch (Exception e) {
            bh.consume(e);
        }
    }

    @Benchmark
    public void parseWithHandlersError(Blackhole bh, Inputs inputs) {
        bh.consume(TypeId.parse(inputs.invalidTypeId,
                typeId -> typeId,
                error -> error));
    }

    @Benchmark
    public void parseToOptionalError(Blackhole bh, Inputs inputs) {
        bh.consume(TypeId.parseToOptional(inputs.invalidTypeId));
    }

    @Benchmark
    public void parseToValidatedError(Blackhole bh, Inputs inputs) {
        bh.consume(TypeId.parseToValidated(inputs.invalidTypeId));
    }

    @State(Scope.Benchmark)
    public static class Inputs {

        UUID uuid;
        String validTypeId;
        String invalidTypeId;
        String prefix;
        TypeId typeId;

        @Setup(Level.Trial)
        public void setup() {
            uuid = UUID.fromString("01890a5d-ac96-774b-bcce-b302099a8057");
            validTypeId = "prefix_01h455vb4pex5vsknk084sn02q";
            invalidTypeId = "prefix_01h455vb4pexÖvsknk084sn02q";
            prefix = "prefix";
            typeId = TypeId.of(prefix, uuid);
        }
    }

}
