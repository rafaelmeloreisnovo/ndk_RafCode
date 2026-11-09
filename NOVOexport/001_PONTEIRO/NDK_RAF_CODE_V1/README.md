# NDK_RAF_CODE_V1 - ponteiro

Fonte produtora: `rafaelmeloreisnovo/ndk_RafCode`, branch `master`.
Escopo: overlay C11 freestanding **autoral, aditivo e opt-in**.
Nenhum arquivo oficial foi substituido. Nao e uma distribuicao completa do Android NDK.

## Rotas
- `authorial/raf_l0/include/raf_l0.h`: ABI e contratos.
- `authorial/raf_l0/src/`: duas unidades de traducao, sem libc.
- `authorial/raf_l0/tests/`: harness hosted (nao vai no produto).
- `authorial/raf_l0/build.sh`: compilacao manual por alvo.
- `.github/workflows/raf-l0-freestanding.yml`: gates por ABI.
- `authorial/raf_l0/PROVENANCE.md`: direitos, fronteiras e rollback.

## Gates
SOURCE = commit base + arquivos deste overlay.
ARTIFACT = objetos ELF relocaveis por ABI.
EXECUTION = compilacao/execucao informada separadamente.
EVIDENCE = readelf, nm, testes C e logs por HEAD.
CLAIM = PASS_SCOPED somente se gates realmente executados.

Lacunas: integracao ART/DEX/JNI, APK, device ARM e benchmark real = TOKEN_VAZIO.
Rollback: reverter apenas os arquivos novos deste overlay, sem alterar upstream.
