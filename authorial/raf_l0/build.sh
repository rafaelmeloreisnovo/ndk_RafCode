#!/bin/sh
# Hosted build orchestrator; never linked into the target core.
set -eu
: "${CC:=clang}"
: "${TARGET:=aarch64-linux-android21}"
: "${OUT:=build/raf_l0}"
mkdir -p "$OUT"
FLAGS="-std=c11 -Oz -ffreestanding -fno-builtin -fno-stack-protector -fno-unwind-tables -fno-asynchronous-unwind-tables -fno-optimize-sibling-calls -fno-ident -fvisibility=hidden -ffunction-sections -fdata-sections -Wall -Wextra -Wconversion -Wshadow -Werror -pedantic"
"$CC" --target="$TARGET" $FLAGS -c authorial/raf_l0/src/raf_crc32.c -o "$OUT/raf_crc32.o"
"$CC" --target="$TARGET" $FLAGS -c authorial/raf_l0/src/raf_be32.c -o "$OUT/raf_be32.o"
printf 'OBJECTS=%s\nTARGET=%s\n' "$OUT" "$TARGET"
