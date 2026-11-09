package rafcode.l0;

/** Optional Java/ART mirror of the C11 byte-level contract; never a C replacement. */
public final class RafL0 {
    public static final int OK = 0;
    public static final int BAD_ARG = 1;
    public static final int SHORT_BUFFER = 2;

    private RafL0() {}

    /** CRC-32/IEEE reflected. Seed is unfinalized; output receives ~finalState.
     *  No allocations, exceptions, external calls or mutation on validation failure.
     */
    public static int crc32(byte[] input, int offset, int length,
                            int seed, int[] output, int outputIndex) {
        if (output == null || outputIndex < 0 || outputIndex >= output.length ||
            offset < 0 || length < 0) return BAD_ARG;
        if (input == null) {
            if (offset != 0 || length != 0) return BAD_ARG;
            output[outputIndex] = ~seed;
            return OK;
        }
        if (offset > input.length) return BAD_ARG;
        if (length > input.length - offset) return SHORT_BUFFER;

        int state = seed;
        for (int i = 0; i < length; ++i) {
            state ^= input[offset + i] & 255;
            for (int bit = 0; bit < 8; ++bit)
                state = (state >>> 1) ^ (0xedb88320 & -(state & 1));
        }
        output[outputIndex] = ~state;
        return OK;
    }

    /** Write unsigned 32-bit value in network byte order. */
    public static int putBe32(byte[] buffer, int offset, int value) {
        if (buffer == null || offset < 0 || offset > buffer.length) return BAD_ARG;
        if (buffer.length - offset < 4) return SHORT_BUFFER;
        buffer[offset] = (byte) (value >>> 24);
        buffer[offset + 1] = (byte) (value >>> 16);
        buffer[offset + 2] = (byte) (value >>> 8);
        buffer[offset + 3] = (byte) value;
        return OK;
    }

    /** Read unsigned 32-bit pattern into a Java signed int (same 32 bits). */
    public static int getBe32(byte[] buffer, int offset,
                              int[] output, int outputIndex) {
        if (buffer == null || output == null || offset < 0 ||
            outputIndex < 0 || outputIndex >= output.length ||
            offset > buffer.length) return BAD_ARG;
        if (buffer.length - offset < 4) return SHORT_BUFFER;
        output[outputIndex] = ((buffer[offset] & 255) << 24) |
                ((buffer[offset + 1] & 255) << 16) |
                ((buffer[offset + 2] & 255) << 8) |
                (buffer[offset + 3] & 255);
        return OK;
    }
}
