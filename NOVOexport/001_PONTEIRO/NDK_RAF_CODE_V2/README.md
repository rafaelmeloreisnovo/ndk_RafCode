# NDK_RAF_CODE_V2 — Java primitive L0 successor

SOURCE: rafaelmeloreisnovo/ndk_RafCode master (PR#1 merged); upstream Android NDK material is not relicensed.
TARGET: opt-in Java 8 bytecode mirror of existing C11 bit-level functions.
PRODUCER: this repository; Java source `authorial/raf_l0/java/src/rafcode/l0/RafL0.java`.
TEST: `authorial/raf_l0/java/tests/rafcode/l0/RafL0Test.java`, tested on hosted JVM only.
CI: `.github/workflows/raf-l0-java.yml` for compilation, bytecode inspection, tests.

No JNI, DEX, ART integration, APK or physical device execution is proven. No Java runtime dependencies are added to the C11 freestanding core. Java requires VM/ART and may have runtime allocations outside hot methods. No new bytecodes in the public hot methods is a scoped source/bytecode property, not a global GC guarantee.

Callers reuse arrays; only `int`, `byte[]`, `int[]`, loops, shifts, masks, and returns are used. CRC is IEEE reflected polynomial 0xedb88320 and an unfinalized seed compatible with the C11 contract. For the standard CRC32 use seed 0xffffffff, compare the signed Java int as a 32-bit pattern.

INDEX: L:NDK_RAF_CODE; O:JAVA_ART_BOUNDARY; T:CRC_ENDIAN; P:master/PR1; C:LOW_LEVEL; R:JAVA_MIRROR_OF_C; I:NOVOexport/001_PONTEIRO/NDK_RAF_CODE_V2; E:SCOPED; A:HUMAN_REVIEW.
ROLLBACK: discard unmerged branch/PR; `master` unchanged until reviewed.
LICENSE: authorial additions have no assigned redistribution license pending owner's explicit decision; do not modify inherited licensing.
SOURCE != ARTIFACT != EXECUTION != EVIDENCE != CLAIM.
CLAIM_ALLOWED=false for JNI/Android/device/realtime performance.
