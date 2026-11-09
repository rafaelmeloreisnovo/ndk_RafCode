#ifndef RAF_L0_H
#define RAF_L0_H

/* The 8-bit byte and 32-bit unsigned int are part of this module's ABI. */
#if !defined(__CHAR_BIT__) || __CHAR_BIT__ != 8
#error "raf_l0 requires 8-bit bytes"
#endif
#if !defined(__SIZEOF_INT__) || __SIZEOF_INT__ != 4
#error "raf_l0 requires 32-bit unsigned int"
#endif

enum {
    RAF_L0_OK = 0,
    RAF_L0_BAD_ARG = 1,
    RAF_L0_SHORT_BUFFER = 2
};

/* Seed is the unfinalized CRC state. The result is finalized (bitwise not).
   A zero length is valid with a null input; output must not be null. */
int raf_l0_crc32(const unsigned char *input, unsigned int length,
                 unsigned int seed, unsigned int *output);

/* Big-endian encode/decode, no reads/writes outside four bytes on success. */
int raf_l0_put_be32(unsigned char *buffer, unsigned int capacity,
                    unsigned int value);
int raf_l0_get_be32(const unsigned char *buffer, unsigned int length,
                    unsigned int *output);

#endif
