#include "../include/raf_l0.h"

int raf_l0_put_be32(unsigned char *buffer, unsigned int capacity,
                    unsigned int value)
{
    if (buffer == (void *)0)
        return RAF_L0_BAD_ARG;
    if (capacity < 4u)
        return RAF_L0_SHORT_BUFFER;

    buffer[0] = (unsigned char)(value >> 24);
    buffer[1] = (unsigned char)(value >> 16);
    buffer[2] = (unsigned char)(value >> 8);
    buffer[3] = (unsigned char)value;
    return RAF_L0_OK;
}

int raf_l0_get_be32(const unsigned char *buffer, unsigned int length,
                    unsigned int *output)
{
    if (buffer == (void *)0 || output == (void *)0)
        return RAF_L0_BAD_ARG;
    if (length < 4u)
        return RAF_L0_SHORT_BUFFER;

    *output = ((unsigned int)buffer[0] << 24) |
              ((unsigned int)buffer[1] << 16) |
              ((unsigned int)buffer[2] << 8) |
              (unsigned int)buffer[3];
    return RAF_L0_OK;
}
