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

    // DEX 035/037/038/039/040 header plausibility only.
    // Output on success: version, file_size, map_off. No writes on failure.
    // Uses only supplied arrays, integer operations and bounded loops.
    public static final int UNSUPPORTED = 3;
    public static final int MALFORMED = 4;

    public static int probeDex(byte[] bytes, int off, int len, int[] output, int slot) {
        if (bytes == null || output == null || off < 0 || len < 0 ||
            slot < 0 || slot > output.length || output.length - slot < 3) return BAD_ARG;
        if (off > bytes.length || len > bytes.length - off) return SHORT_BUFFER;
        if (len < 112) return SHORT_BUFFER;
        if (bytes[off] != 100 || bytes[off+1] != 101 ||
            bytes[off+2] != 120 || bytes[off+3] != 10 ||
            bytes[off+4] != 48 || bytes[off+7] != 0) return MALFORMED;
        int a = bytes[off+5] & 255, b = bytes[off+6] & 255;
        if (a < 48 || a > 57 || b < 48 || b > 57) return MALFORMED;
        int version = (a - 48) * 10 + b - 48;
        if (version != 35 && version != 37 && version != 38 &&
            version != 39 && version != 40) return UNSUPPORTED;
        long size = (bytes[off+32] & 255L) | ((bytes[off+33] & 255L) << 8) |
                    ((bytes[off+34] & 255L) << 16) | ((bytes[off+35] & 255L) << 24);
        long header = (bytes[off+36] & 255L) | ((bytes[off+37] & 255L) << 8) |
                      ((bytes[off+38] & 255L) << 16) | ((bytes[off+39] & 255L) << 24);
        long endian = (bytes[off+40] & 255L) | ((bytes[off+41] & 255L) << 8) |
                      ((bytes[off+42] & 255L) << 16) | ((bytes[off+43] & 255L) << 24);
        long map = (bytes[off+52] & 255L) | ((bytes[off+53] & 255L) << 8) |
                   ((bytes[off+54] & 255L) << 16) | ((bytes[off+55] & 255L) << 24);
        if (size != len || header != 112 || endian != 0x12345678L ||
            (map != 0 && (map < 112 || map >= size || (map & 3L) != 0)))
            return MALFORMED;
        output[slot] = version;
        output[slot+1] = (int)size;
        output[slot+2] = (int)map;
        return OK;
    }

    // ELF32/ELF64 little/big-endian header and table bounds only.
    // Output on success: EI_CLASS, EI_DATA, e_machine, e_phnum, e_shnum.
    // This is not an executable loader, linker, or trust verdict.
    public static int probeElf(byte[] bytes, int off, int len, int[] output, int slot) {
        if (bytes == null || output == null || off < 0 || len < 0 ||
            slot < 0 || slot > output.length || output.length - slot < 5) return BAD_ARG;
        if (off > bytes.length || len > bytes.length - off) return SHORT_BUFFER;
        if (len < 16) return SHORT_BUFFER;
        if ((bytes[off] & 255) != 127 || bytes[off+1] != 69 ||
            bytes[off+2] != 76 || bytes[off+3] != 70) return MALFORMED;
        int cls = bytes[off+4] & 255, end = bytes[off+5] & 255;
        if ((cls != 1 && cls != 2) || (end != 1 && end != 2) || bytes[off+6] != 1)
            return UNSUPPORTED;
        int min = cls == 1 ? 52 : 64;
        if (len < min) return SHORT_BUFFER;
        int machine = end == 1 ?
            (bytes[off+18] & 255) | ((bytes[off+19] & 255) << 8) :
            ((bytes[off+18] & 255) << 8) | (bytes[off+19] & 255);
        int base = cls == 1 ? 40 : 52;
        int ehsize = end == 1 ?
            (bytes[off+base] & 255) | ((bytes[off+base+1] & 255) << 8) :
            ((bytes[off+base] & 255) << 8) | (bytes[off+base+1] & 255);
        if (ehsize != min) return MALFORMED;
        int phentsize = end == 1 ?
            (bytes[off+base+2] & 255) | ((bytes[off+base+3] & 255) << 8) :
            ((bytes[off+base+2] & 255) << 8) | (bytes[off+base+3] & 255);
        int phnum = end == 1 ?
            (bytes[off+base+4] & 255) | ((bytes[off+base+5] & 255) << 8) :
            ((bytes[off+base+4] & 255) << 8) | (bytes[off+base+5] & 255);
        int shentsize = end == 1 ?
            (bytes[off+base+6] & 255) | ((bytes[off+base+7] & 255) << 8) :
            ((bytes[off+base+6] & 255) << 8) | (bytes[off+base+7] & 255);
        int shnum = end == 1 ?
            (bytes[off+base+8] & 255) | ((bytes[off+base+9] & 255) << 8) :
            ((bytes[off+base+8] & 255) << 8) | (bytes[off+base+9] & 255);
        long phoff = 0, shoff = 0;
        int phpos = cls == 1 ? 28 : 32;
        int shpos = cls == 1 ? 32 : 40;
        for (int i = 0; i < (cls == 1 ? 4 : 8); ++i) {
            int p = bytes[off+phpos+i] & 255;
            int s = bytes[off+shpos+i] & 255;
            if (end == 1) {
                phoff |= ((long)p << (i * 8));
                shoff |= ((long)s << (i * 8));
            } else {
                phoff = (phoff << 8) | p;
                shoff = (shoff << 8) | s;
            }
        }
        if (phnum == 65535 || (shnum == 0 && shoff != 0)) return UNSUPPORTED;
        if (phnum != 0 && (phentsize < (cls == 1 ? 32 : 56) ||
            phoff < min || phoff > len || (long)phnum * phentsize > len - phoff))
            return MALFORMED;
        if (shnum != 0 && (shentsize < (cls == 1 ? 40 : 64) ||
            shoff < min || shoff > len || (long)shnum * shentsize > len - shoff))
            return MALFORMED;
        output[slot] = cls;
        output[slot+1] = end;
        output[slot+2] = machine;
        output[slot+3] = phnum;
        output[slot+4] = shnum;
        return OK;
    }

}
