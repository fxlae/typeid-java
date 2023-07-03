package de.fxlae.typeid;

import java.util.UUID;

/**
 * A test facade for testing both Java 8 and Java 17 variants with a common test set
 */
public interface TypeIdInstance {
    String prefix();
    UUID uuid();
    Object getWrapped();
}
