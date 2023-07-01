package de.fxlae.typeid;

import java.util.Objects;
import java.util.UUID;

/**
 * An implementation of <a href="https://github.com/jetpack-io/typeid">TypeID</a> for Java.
 */
public final class TypeId {

    private static final char SEPARATOR = '_';
    private static final String PREFIX_ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private static final int PREFIX_MAX_LENGTH = 63;
    private static final String SUFFIX_ALPHABET = "0123456789abcdefghjkmnpqrstvwxyz";

    // inspired by base32.go from the official go implementation
    // https://github.com/jetpack-io/typeid-go/blob/main/base32/base32.go
    // lookup: [ascii pos] -> binary representation of block
    // FF means invalid character that is not part of the suffix alphabet
    private static final long[] SUFFIX_REVERSE_LOOKUP = new long[]{
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x00, 0x01,
            0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x0A, 0x0B, 0x0C,
            0x0D, 0x0E, 0x0F, 0x10, 0x11, 0xFF, 0x12, 0x13, 0xFF, 0x14,
            0x15, 0xFF, 0x16, 0x17, 0x18, 0x19, 0x1A, 0xFF, 0x1B, 0x1C,
            0x1D, 0x1E, 0x1F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
    };

    private static final UuidProvider uuidProvider = UuidProvider.getDefault();

    private final UUID uuid;
    private final String prefix;

    private TypeId(String prefix, UUID uuid) {
        this.prefix = prefix;
        this.uuid = uuid;
    }

    /**
     * Returns the prefix of this {@link TypeId}.
     *
     * @return the prefix.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the underlying {@link UUID} of this {@link TypeId}.
     *
     * @return the {@link UUID}.
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Creates a new prefixed {@link TypeId} based on UUIDv7.
     *
     * @param prefix the prefix to use.
     * @return the new {@link TypeId}.
     */
    public static TypeId generate(String prefix) throws IllegalArgumentException {
        return of(prefix, uuidProvider.getUuidV7());
    }

    /**
     * Creates a new {@link TypeId} without prefix, based on UUIDv7.
     * <p> Note: "no prefix" means empty string, not null.
     *
     * @return the new {@link TypeId}.
     */
    public static TypeId generate() {
        return of("", uuidProvider.getUuidV7());
    }

    /**
     * Creates a new {@link TypeId} without prefix, based on the given {@link UUID}.
     * <p> The {@link UUID} can be of any version.
     * <p> Note: "no prefix" means empty string, not null.
     *
     * @param uuid the {@link UUID} to use.
     * @return the new {@link TypeId}.
     */
    public static TypeId of(UUID uuid) throws IllegalArgumentException {
        return of("", uuid);
    }

    /**
     * Creates a new {@link TypeId}, based on the given prefix and {@link UUID}.
     *
     * @param prefix the prefix to use.
     * @param uuid   the {@link UUID} to use.
     * @return the new {@link TypeId}.
     */
    public static TypeId of(String prefix, UUID uuid) throws IllegalArgumentException {

        if (uuid == null) {
            throw new IllegalArgumentException("Provided UUID must not be null");
        }

        if (prefix == null) {
            throw new IllegalArgumentException("Provided prefix must not be null");
        }

        if (prefix.length() > PREFIX_MAX_LENGTH) {
            throw new IllegalArgumentException("Provided prefix must not be larger than " + PREFIX_MAX_LENGTH + " characters");
        }

        for (int i = 0; i < prefix.length(); i++) {
            char c = prefix.charAt(i);
            if (PREFIX_ALPHABET.indexOf(c) == -1) {
                throw new IllegalArgumentException("Illegal prefix character: '" + c + "'");
            }
        }

        return new TypeId(prefix, uuid);
    }

    /**
     * Parses the textual representation of a TypeID and returns a {@link TypeId} instance.
     *
     * @param text the textual representation.
     * @return the new {@link TypeId}.
     */
    public static TypeId parse(final String text) throws IllegalArgumentException {

        if (text == null) {
            throw new IllegalArgumentException("Provided TypeId must not be null");
        }

        final int separatorIndex = text.lastIndexOf(SEPARATOR);

        if (separatorIndex == 0) {
            throw new IllegalArgumentException("Provided TypeId must not start with '_'");
        }

        if (separatorIndex == text.length() - 1) {
            throw new IllegalArgumentException("Provided TypeId must not end with '_'");
        }

        final String prefixSegment;
        final String suffixSegment;

        if (separatorIndex != -1) {
            prefixSegment = text.substring(0, separatorIndex);
            suffixSegment = text.substring(separatorIndex + 1);
        } else {
            prefixSegment = "";
            suffixSegment = text;
        }

        return of(prefixSegment, base32Decode(suffixSegment));
    }

    private static String base32Encode(UUID uuid) {

        final long msb = uuid.getMostSignificantBits();
        final long lsb = uuid.getLeastSignificantBits();
        StringBuilder stringBuilder = new StringBuilder();

        // encode the MSBs except the last bit, as the block it belongs to overlaps with the LSBs
        for (int i = 0; i < 13; i++) {
            long block = (msb >>> (61 - 5 * i)) & 0x1F;
            stringBuilder.append(SUFFIX_ALPHABET.charAt((int) block));
        }

        // encode the overlap between MSBs (1 bit) and LSBs (4 bits)
        long overlap = ((msb & 0x1) << 4) | (lsb >>> 60);
        stringBuilder.append(SUFFIX_ALPHABET.charAt((int) overlap));

        // encode the rest of LSBs
        for (int i = 1; i < 13; i++) {
            long block = (lsb >>> (60 - 5 * i)) & 0x1F;
            stringBuilder.append(SUFFIX_ALPHABET.charAt((int) block));
        }

        return stringBuilder.toString();
    }

    private static UUID base32Decode(final String suffix) {

        if (suffix.length() != 26) {
            throw new IllegalArgumentException("Suffix with invalid length (expected: 26, actual: '" + suffix.length() + "')");
        }

        for (int i = 0; i < suffix.length(); i++) {
            if (SUFFIX_REVERSE_LOOKUP[suffix.charAt(i)] == 0xFF) {
                throw new IllegalArgumentException("Illegal base32 character: '" + suffix.charAt(i) + "'");
            }
        }

        // the leftmost block of 5 bits has to start with 0b00
        if (((SUFFIX_REVERSE_LOOKUP[suffix.charAt(0)] >>> 3) & 0x3) > 0) {
            throw new IllegalArgumentException("Illegal leftmost character: " + suffix.charAt(0));
        }

        long lsb = 0;
        long msb = 0;

        // decode characters [25] to [14] into the LSBs
        for (int i = 0; i < 12; i++) {
            char c = suffix.charAt(25 - i);
            lsb |= (SUFFIX_REVERSE_LOOKUP[c]) << 5 * i;
        }

        // decode the overlap between LSBs and MSBs (character [13])
        long bitsAtOverlap = SUFFIX_REVERSE_LOOKUP[suffix.charAt(13)];
        lsb |= (bitsAtOverlap & 0xF) << 60;
        msb |= (bitsAtOverlap & 0x10) >>> 4;

        // decode characters [12] to [0] into the MSBs
        for (int i = 0; i < 13; i++) {
            char c = suffix.charAt(12 - i);
            msb |= (SUFFIX_REVERSE_LOOKUP[c]) << 5 * i + 1;
        }

        return new UUID(msb, lsb);
    }

    /**
     * Returns the textual representation of this {@link TypeId}.
     *
     * @return the textual representation.
     */
    @Override
    public String toString() {
        String suffix = base32Encode(uuid);
        if (!prefix.isEmpty()) {
            return prefix + "_" + suffix;
        } else {
            return suffix;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeId typeId = (TypeId) o;
        return Objects.equals(uuid, typeId.uuid) && Objects.equals(prefix, typeId.prefix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, prefix);
    }
}
