# NDK RAFCODE V3 — Java L0, no explicit hot-path allocation

ROUTE: CURRENT_STATE -> ndk_RafCode PR#2 -> source L0 Java -> CI exact HEAD -> receipt -> R3.
SOURCE: rafaelmeloreisnovo/ndk_RafCode; PR#1 merged native C11 core in master; PR#2 DRAFT/HOLD. Android NDK inherited sources unchanged.

## Precise boundary
- JVM/ART Java SE bytecode requires a class representation and runtime. RafL0.java remains ONE final utility class with static entry points; no domain classes in hot path. Private constructor is never called. Runtime independence is NOT claimed.
- Actual freestanding producer is authorial/raf_l0/ C11 core from merged PR#1. Java source and JDK/ART compiler/runtime are optional hosted adapter and cannot substitute for a freestanding executable.
- Existing CRC32/BE32 APIs and method descriptors retained. V3 adds probeDex/probeElf in the SAME class with only primitive locals, caller-owned byte[]/int[], bounded loops and return status; no new, reflection, JNI, third-party packages, or external invoke* bytecodes in public hot paths.
- probeDex accepts legacy DEX 035,037,038,039,040 header (112 bytes, little-endian tag, exact file_size, bounded aligned map_off; zero map means weak plausibility). Rejects DEX036, DEX041+ and corrupted ranges. On OK writes [version,fileSize,mapOff] to caller output slots; errors do not mutate output.
- probeElf checks ELF32/ELF64 and little/big-endian e_machine, header size, ordinary program/section-table arithmetic. On OK writes [class,endian,machine,phnum,shnum]. Extended ELF table numbering is not implemented and must fail closed. No sections, segment payloads, dynamic relocation, signature or ELF safety validated.
- Synthetic header plausibility != full valid DEX/ELF and != executable load or ART/JDK runtime. Input must be a byte array already provided by its caller.
- Java has no portable inline ARM assembly syntax; introduce native assembly only after ABI register clobber/alignment, stack, cross-compiler and empirical performance gates. Hand-authored hex opcodes are not automatically smaller or faster.

## Scoped gates
- Precedent only: previous Java L0 CI run42578925437 SUCCESS (hosted 8464 assertions); C11 freestanding build run42578925446 SUCCESS.
- Local Java 8 prototype, synthetic ELF/DEX inputs, 16451 assertions PASS_SCOPED; the GitHub exact-head CI must still be examined.
- CI workflow .github/workflows/raf-l0-java.yml compiles existing CRC/BE tests and additional ELF/DEX tests. javap bytecode gate rejects new/array allocations and external invoke{static,virtual,interface}; no artifact upload. Java compiler and VM remain external build/runtime tools.
- Full ELF/DEX validator, checksums, real signed binaries/APKs, JNI, class loading, DEX/ART, Android hardware armv7/aarch64 runtime, heap/GC/latency/stack/performance = NOT_RUN.
- Independent authorial distribution license TOKEN_VAZIO pending owner's decision and compatibility review. Inherited licensing preserved. claim_allowed=false, merge_allowed=false, release_allowed=false.

References: https://source.android.com/docs/core/runtime/dex-format ; https://docs.oracle.com/en/java/javase/26/docs/specs/jvms/
ROLLBACK: close DRAFT PR2 or revert only new V3 files/edits; original NDK and previous history preserved.
R3=<F_ok: bounded byte-reader extension and adversarial test, F_gap: real runtime/validator and legal release authority, F_next: exact HEAD CI + independent file fixtures + ARM device profiling>
