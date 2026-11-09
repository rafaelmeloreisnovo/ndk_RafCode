/* Hosted harness only. These headers are not used by the freestanding core. */
#include <stdio.h>
#include "../include/raf_l0.h"

static int check(int ok, const char *name)
{
    if (!ok) {
        printf("FAIL %s\n", name);
        return 1;
    }
    printf("PASS %s\n", name);
    return 0;
}

int main(void)
{
    static const unsigned char text[] = "123456789";
    unsigned char data[5] = { 0xa5u, 0xa5u, 0xa5u, 0xa5u, 0xa5u };
    unsigned int out = 0u;
    int failures = 0;

    failures += check(raf_l0_crc32(text, 9u, 0xffffffffu, &out) ==
                      RAF_L0_OK && out == 0xcbf43926u, "crc32 IEEE vector");
    failures += check(raf_l0_crc32((const unsigned char *)0, 0u,
                      0xffffffffu, &out) == RAF_L0_OK && out == 0u,
                      "crc32 zero length");
    failures += check(raf_l0_crc32((const unsigned char *)0, 1u,
                      0xffffffffu, &out) == RAF_L0_BAD_ARG,
                      "crc32 null nonempty");
    failures += check(raf_l0_crc32(text, 9u, 0xffffffffu,
                      (unsigned int *)0) == RAF_L0_BAD_ARG,
                      "crc32 null output");
    failures += check(raf_l0_put_be32(data, 3u, 0x12345678u) ==
                      RAF_L0_SHORT_BUFFER && data[0] == 0xa5u,
                      "put short buffer does not write");
    failures += check(raf_l0_put_be32(data, 5u, 0x12345678u) ==
                      RAF_L0_OK && data[0] == 0x12u &&
                      data[1] == 0x34u && data[2] == 0x56u &&
                      data[3] == 0x78u && data[4] == 0xa5u,
                      "put be32 and sentinel");
    failures += check(raf_l0_get_be32(data, 4u, &out) ==
                      RAF_L0_OK && out == 0x12345678u,
                      "get be32 roundtrip");
    failures += check(raf_l0_get_be32(data, 3u, &out) ==
                      RAF_L0_SHORT_BUFFER && out == 0x12345678u,
                      "get short buffer does not modify output");
    failures += check(raf_l0_get_be32(data, 4u, (unsigned int *)0) ==
                      RAF_L0_BAD_ARG, "get null output");
    failures += check(raf_l0_put_be32((unsigned char *)0, 4u, 1u) ==
                      RAF_L0_BAD_ARG, "put null buffer");
    return failures != 0;
}
