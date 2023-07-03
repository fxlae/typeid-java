package de.fxlae.typeid;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.UUID;

public class TypeIdBench {

    @State(Scope.Benchmark)
    public static class Inputs {

        UUID uuid;
        String typeIdAsString;
        String prefix;
        TypeId typeId;

        @Setup(Level.Trial)
        public void setup() {
            uuid = UUID.fromString("01890a5d-ac96-774b-bcce-b302099a8057");
            typeIdAsString = "prefix_01h455vb4pex5vsknk084sn02q";
            prefix = "prefix";
            typeId = TypeId.of(prefix, uuid);
        }
    }

    @Benchmark
    public void parse(Blackhole bh, Inputs inputs) {
        bh.consume(TypeId.parse(inputs.typeIdAsString));
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

}
