package de.fxlae.typeid;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Optional;
import java.util.UUID;

public class TypeIdBench {

    @Benchmark
    public void parse(Blackhole bh, Inputs inputs) {
        bh.consume(TypeId.parse(inputs.validTypeId));
    }

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
    public void parseWithErrorAsException(Blackhole bh, Inputs inputs) {
        try {
            TypeId.parse(inputs.invalidTypeId);
        } catch (IllegalArgumentException e) {
            bh.consume(e.getMessage());
        }
    }

    @Benchmark
    public void parseWithErrorAsValue(Blackhole bh, Inputs inputs) {
        String result = TypeId.parse(inputs.invalidTypeId,
                typeId -> null,
                message -> message);
        bh.consume(result);
    }


    public void xx(Blackhole bh, Inputs inputs) {
        var maybeTypeId = TypeId.parse(inputs.invalidTypeId,
                Optional::of,
                message -> {
                    System.out.println(message);
                    return Optional.empty();
                });
        bh.consume(maybeTypeId);
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
