package de.fxlae.typeid;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.UUID;

public class TypeIdBench {

    @State(Scope.Benchmark)
    public static class Inputs {
        UUID uuid = UUID.fromString("01890a5d-ac96-774b-bcce-b302099a8057");
        String typeIdAsString = "prefix_01h455vb4pex5vsknk084sn02q";
        String prefix = "prefix";
    }

    @Benchmark
    public void parse(Blackhole bh, Inputs inputs) {
        TypeId typeId = TypeId.parse(inputs.typeIdAsString);
        bh.consume(typeId);
    }

    @Benchmark
    public void of(Blackhole bh, Inputs inputs) {
        TypeId typeId = TypeId.of(inputs.prefix, inputs.uuid);
        bh.consume(typeId);
    }

    @Benchmark
    public void generate(Blackhole bh, Inputs inputs) {
        TypeId typeId = TypeId.generate(inputs.prefix);
        String asString = typeId.toString();
        bh.consume(asString);
    }

}
