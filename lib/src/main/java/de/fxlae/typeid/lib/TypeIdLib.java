package de.fxlae.typeid.lib;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;

import java.util.Objects;
import java.util.UUID;

public final class TypeIdLib {

    public static final String VALID_REF = "";
    private static final char SEPARATOR = '_';
    private static final int PREFIX_MAX_LENGTH = 63;
    private static final String SUFFIX_ALPHABET = "0123456789abcdefghjkmnpqrstvwxyz";
    private static final int SUFFIX_LENGTH = 26;

    // inspired by base32.go from the official go implementation
    // https://github.com/jetpack-io/typeid-go/blob/main/base32/base32.go
    // lookup: [ascii pos] -> binary representation of block

    // sentinel value for characters that are not part of the alphabets
    private static final long NOOP = Long.MAX_VALUE;

    // these values currently are longs because they are directly shifted into the two longs
    // of a UUID
    private static final long[] SUFFIX_LOOKUP = new long[]{
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, 0x00, 0x01, // 0, 1
            0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, NOOP, NOOP, // 2, 3, 4, 5, 6, 7, 8, 9
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, 0x0A, 0x0B, 0x0C, // a, b, c
            0x0D, 0x0E, 0x0F, 0x10, 0x11, NOOP, 0x12, 0x13, NOOP, 0x14, // d, e, f, g, h, j, k, m
            0x15, NOOP, 0x16, 0x17, 0x18, 0x19, 0x1A, NOOP, 0x1B, 0x1C, // n, p, q, r, s, value, v, w
            0x1D, 0x1E, 0x1F, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, // x, y, z
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
            NOOP, NOOP, NOOP, NOOP, NOOP, NOOP
    };
    private static final TimeBasedEpochGenerator generator = Generators.timeBasedEpochGenerator();

    private TypeIdLib() {
    }

    public static UUID decodeSuffixOnInput(final String input, final int separatorIndex) {

        final int start = (separatorIndex == -1) ? 0 : separatorIndex + 1;

        long lsb = 0;
        long msb = 0;

        // decode characters [25] to [14] into the LSBs
        lsb |= (SUFFIX_LOOKUP[input.charAt(25 + start)]);
        lsb |= (SUFFIX_LOOKUP[input.charAt(24 + start)]) << 5;
        lsb |= (SUFFIX_LOOKUP[input.charAt(23 + start)]) << 10;
        lsb |= (SUFFIX_LOOKUP[input.charAt(22 + start)]) << 15;
        lsb |= (SUFFIX_LOOKUP[input.charAt(21 + start)]) << 20;
        lsb |= (SUFFIX_LOOKUP[input.charAt(20 + start)]) << 25;
        lsb |= (SUFFIX_LOOKUP[input.charAt(19 + start)]) << 30;
        lsb |= (SUFFIX_LOOKUP[input.charAt(18 + start)]) << 35;
        lsb |= (SUFFIX_LOOKUP[input.charAt(17 + start)]) << 40;
        lsb |= (SUFFIX_LOOKUP[input.charAt(16 + start)]) << 45;
        lsb |= (SUFFIX_LOOKUP[input.charAt(15 + start)]) << 50;
        lsb |= (SUFFIX_LOOKUP[input.charAt(14 + start)]) << 55;

        // decode the overlap between LSBs and MSBs (character [13])
        long bitsAtOverlap = SUFFIX_LOOKUP[input.charAt(13 + start)];
        lsb |= (bitsAtOverlap & 0xF) << 60;
        msb |= (bitsAtOverlap & 0x10) >>> 4;

        // decode characters [12] to [0] into the MSBs
        msb |= (SUFFIX_LOOKUP[input.charAt(12 + start)]) << 1;
        msb |= (SUFFIX_LOOKUP[input.charAt(11 + start)]) << 6;
        msb |= (SUFFIX_LOOKUP[input.charAt(10 + start)]) << 11;
        msb |= (SUFFIX_LOOKUP[input.charAt(9 + start)]) << 16;
        msb |= (SUFFIX_LOOKUP[input.charAt(8 + start)]) << 21;
        msb |= (SUFFIX_LOOKUP[input.charAt(7 + start)]) << 26;
        msb |= (SUFFIX_LOOKUP[input.charAt(6 + start)]) << 31;
        msb |= (SUFFIX_LOOKUP[input.charAt(5 + start)]) << 36;
        msb |= (SUFFIX_LOOKUP[input.charAt(4 + start)]) << 41;
        msb |= (SUFFIX_LOOKUP[input.charAt(3 + start)]) << 46;
        msb |= (SUFFIX_LOOKUP[input.charAt(2 + start)]) << 51;
        msb |= (SUFFIX_LOOKUP[input.charAt(1 + start)]) << 56;
        msb |= (SUFFIX_LOOKUP[input.charAt(start)]) << 61;

        return new UUID(msb, lsb);
    }

    public static String encode(final String prefix, final UUID uuid) {

        final long msb = uuid.getMostSignificantBits();
        final long lsb = uuid.getLeastSignificantBits();

        StringBuilder sb;
        if (prefix.isEmpty()) {
            sb = new StringBuilder(26);
        } else {
            sb = new StringBuilder(27 + prefix.length());
            sb.append(prefix).append(SEPARATOR);
        }

        // encode the MSBs except the last bit, as the block it belongs to overlaps with the LSBs
        sb.append(SUFFIX_ALPHABET.charAt((int) (msb >>> 61) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (msb >>> 56) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (msb >>> 51) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (msb >>> 46) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (msb >>> 41) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (msb >>> 36) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (msb >>> 31) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (msb >>> 26) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (msb >>> 21) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (msb >>> 16) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (msb >>> 11) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (msb >>> 6) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (msb >>> 1) & 0x1F));

        // encode the overlap between MSBs (1 bit) and LSBs (4 bits)
        long overlap = ((msb & 0x1) << 4) | (lsb >>> 60);
        sb.append(SUFFIX_ALPHABET.charAt((int) overlap));

        // encode the rest of LSBs
        sb.append(SUFFIX_ALPHABET.charAt((int) (lsb >>> 55) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (lsb >>> 50) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (lsb >>> 45) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (lsb >>> 40) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (lsb >>> 35) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (lsb >>> 30) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (lsb >>> 25) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (lsb >>> 20) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (lsb >>> 15) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (lsb >>> 10) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) (lsb >>> 5) & 0x1F))
                .append(SUFFIX_ALPHABET.charAt((int) lsb & 0x1F));

        return sb.toString();
    }

    public static int findSeparatorIndex(String input) {
        return (input == null) ? -1 : input.lastIndexOf(SEPARATOR);
    }

    public static String extractPrefix(String input, int separatorIndex) {
        if (separatorIndex == -1) {
            return "";
        } else {
            return input.substring(0, separatorIndex);
        }
    }

    public static String validateInput(String input, int separatorIndex) {

        Objects.requireNonNull(input);

        if (input.isEmpty()) {
            return "Provided TypeId must not be empty";
        }

        // empty prefix, but with unexpected separator
        if (separatorIndex == 0) {
            return "TypeId with empty prefix must not contain the separator '_'";
        }

        String suffixErr = validateSuffixOnInput(input, separatorIndex);
        if (suffixErr != VALID_REF) {
            return suffixErr;
        }

        return validatePrefixOnInput(input, separatorIndex);
    }

    // validates the suffix without creating an intermediary object for it
    private static String validateSuffixOnInput(final String input, final int separatorIndex) {

        final int start = (separatorIndex != -1) ? separatorIndex + 1 : 0;

        if (input.length() - start != SUFFIX_LENGTH) {
            return "Suffix with illegal length, must be " + SUFFIX_LENGTH;
        }

        if (((SUFFIX_LOOKUP[input.charAt(start)] >>> 3) & 0x3) > 0) {
            return "Illegal leftmost suffix character, must be one of [01234567]";
        }

        for (int i = start; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= SUFFIX_LOOKUP.length || SUFFIX_LOOKUP[c] == NOOP) {
                return "Illegal character in suffix, must be one of [" + SUFFIX_ALPHABET + "]";
            }
        }

        return VALID_REF;
    }

    // validates the prefix without creating an intermediary object for it
    public static String validatePrefixOnInput(final String input, final int separatorIndex) {

        // empty prefix, no separator
        if (separatorIndex == -1) {
            return VALID_REF;
        }

        if (separatorIndex > PREFIX_MAX_LENGTH) {
            return "Prefix with illegal length, must not have more than " + PREFIX_MAX_LENGTH + " characters";
        }

        for (int i = 0; i < separatorIndex; i++) {
            char c = input.charAt(i);
            if (!(c >= 'a' && c <= 'z') /* && c != '_' */) {
                return "Illegal character in prefix, must be one of [a-z]";
            }
        }

        return VALID_REF;
    }

    public static UUID getUuidV7() {
        return generator.generate();
    }
}
