package de.fxlae.typeid;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;

import java.util.UUID;

/**
 * Encapsulates the UUIDv7 generator implementation.
 */
interface UuidProvider {

    UUID getUuidV7();

    static UuidProvider getDefault() {
        return new UuidProvider() {
            private final TimeBasedEpochGenerator generator = Generators.timeBasedEpochGenerator();

            @Override
            public UUID getUuidV7() {
                return generator.generate();
            }
        };
    }

}
