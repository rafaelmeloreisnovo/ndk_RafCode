package rafcode.l0;

import java.util.zip.CRC32; // Hosted test oracle only, NOT core runtime.

public final class RafL0Test {
    private static int count;

    private static void require(boolean yes, String description) {
        ++count;
        if (!yes) throw new AssertionError(description);
    }

    public static void main(String[] args) {
        byte[] sample = {49, 50, 51, 52, 53, 54, 55, 56, 57};
        byte[] buffer = new byte[12]; // Harness allocates; core never allocates.
        int[] output = {0x76543210, 0x12345678};
        CRC32 oracle = new CRC32();

        require(RafL0.crc32(sample, 0, 9, -1, output, 0) == RafL0.OK &&
                output[0] == 0xcbf43926, "CRC-32 standard test vector");
        require(RafL0.crc32(null, 0, 0, -1, output, 0) == RafL0.OK &&
                output[0] == 0, "CRC-32 null empty input");
        require(RafL0.crc32(null, 0, 1, -1, output, 0) == RafL0.BAD_ARG,
                "CRC-32 rejects null nonempty input");
        require(RafL0.crc32(sample, 0, -1, -1, output, 0) == RafL0.BAD_ARG,
                "CRC-32 rejects negative length");
        require(RafL0.crc32(sample, 0, 10, -1, output, 0) == RafL0.SHORT_BUFFER,
                "CRC-32 rejects short input");
        require(RafL0.crc32(sample, Integer.MAX_VALUE, 2, -1, output, 0) ==
                RafL0.BAD_ARG, "CRC-32 refuses overflow-prone offset");
        require(RafL0.crc32(sample, 0, 9, -1, null, 0) == RafL0.BAD_ARG,
                "CRC-32 rejects null destination");
        require(RafL0.crc32(sample, 0, 9, -1, output, output.length) ==
                RafL0.BAD_ARG, "CRC-32 rejects destination overflow");

        // Deterministic LCG data; compare with independent standard JDK CRC oracle.
        int rng = 0x31415926;
        for (int n = 0; n <= 256; ++n) {
            byte[] data = new byte[n + 4];
            for (int i = 0; i < data.length; ++i) {
                rng = rng * 1664525 + 1013904223;
                data[i] = (byte) (rng >>> 24);
            }
            oracle.reset();
            oracle.update(data, 2, n);
            output[0] = 0x22334455;
            require(RafL0.crc32(data, 2, n, -1, output, 0) == RafL0.OK &&
                    (output[0] & 0xffffffffL) == oracle.getValue(),
                    "CRC-32 oracle length=" + n);
        }

        for (int i = 0; i < 4096; ++i) {
            rng = rng * 1664525 + 1013904223;
            for (int j = 0; j < buffer.length; ++j) buffer[j] = (byte) 0x5a;
            require(RafL0.putBe32(buffer, 3, rng) == RafL0.OK,
                    "BE32 encode " + i);
            require(buffer[2] == (byte) 0x5a && buffer[7] == (byte) 0x5a &&
                    RafL0.getBe32(buffer, 3, output, 1) == RafL0.OK &&
                    output[1] == rng, "BE32 roundtrip / sentinels " + i);
        }
        output[0] = 0x76543210;
        buffer[0] = (byte) 0xa5;
        require(RafL0.putBe32(buffer, 9, 123) == RafL0.SHORT_BUFFER &&
                buffer[0] == (byte) 0xa5, "BE32 write failure is atomic");
        require(RafL0.getBe32(buffer, 9, output, 0) == RafL0.SHORT_BUFFER &&
                output[0] == 0x76543210, "BE32 read failure is atomic");
        require(RafL0.putBe32(null, 0, 123) == RafL0.BAD_ARG,
                "BE32 null write");
        require(RafL0.getBe32(null, 0, output, 0) == RafL0.BAD_ARG,
                "BE32 null read");
        require(RafL0.getBe32(buffer, 0, null, 0) == RafL0.BAD_ARG,
                "BE32 null destination");
        require(RafL0.getBe32(buffer, -1, output, 0) == RafL0.BAD_ARG,
                "BE32 negative offset");
        require(RafL0.getBe32(buffer, Integer.MAX_VALUE, output, 0) ==
                RafL0.BAD_ARG, "BE32 huge offset");
        System.out.println("PASS Java L0 hosted assertions=" + count);
    }
}
