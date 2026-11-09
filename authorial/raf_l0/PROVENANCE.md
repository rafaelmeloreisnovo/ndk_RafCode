# Fronteira de autoria e de licenca

- `README.md`, `CONTRIBUTING.md`, `.github/ISSUE_TEMPLATE/*` na base:
  herdados do projeto Android NDK; NAO reclassificar como autoria RAFCODE.
- `authorial/raf_l0/*`, `.github/workflows/raf-l0-freestanding.yml`,
  `NOVOexport/001_PONTEIRO/NDK_RAF_CODE_V1/*`: adicoes independentes,
  produzidas nesta proposta de refatoracao. Nao copiam codigos do AOSP.
- Licencas aplicaveis ao conjunto original: verificar cada arquivo upstream,
  a raiz atual nao inclui LICENSE. Estado de auditoria = TOKEN_VAZIO.
- Licenca de redistribuicao das novas adicoes: pendente de decisao expressa
  do titular. Nao presumir dual-licensing nem relicenciar o projeto oficial.
- Compiler e linker: dependencias de BUILD; os objetos do core nao requerem
  libc ou heap em runtime. Android e kernel continuam plataforma externa.
- Sem ART, JNI, DEX ou .so nesta etapa; compatibilidade nao presumida.
- Auditoria: upstream intacto, mudancas aditivas, branch/PR em HOLD.
- Rollback: descartar a branch ou reverter somente o overlay.
