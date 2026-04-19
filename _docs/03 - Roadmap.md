# Roadmap — Controle Financeiro Pessoal

## Fase 1 — Base ✓
- [x] Setup Spring Boot 3 + Maven
- [x] Entidades JPA: Usuario, Categoria, Lancamento, Meta
- [x] H2 para dev
- [x] Spring Security + JWT
- [x] POST /auth/register e /auth/login

## Fase 2 — CRUD principal ✓
- [x] CRUD Categorias
- [x] CRUD Lancamentos com filtros (?mes, ?ano, ?tipo)
- [x] Isolamento por usuário em todos os endpoints
- [x] GET /resumo com agregação por categoria (GROUP BY JPA)

## Fase 3 — Metas e alertas ✓
- [x] CRUD Metas
- [x] GET /metas/alertas — categorias que estouraram o limite
- [x] Validação: Meta única por categoria + mês/ano

## Fase 4 — Importação de extrato ✓
- [x] Parser OFX
- [x] Parser CSV (formato Inter e C6)
- [x] POST /extrato/upload
- [x] Criar TransacaoPendente para cada transação importada

## Fase 5 — Categorização com IA
- [ ] Integração Claude API
- [ ] Lógica de confiança: ≥80% → Lancamento, <80% → TransacaoPendente
- [ ] GET /extrato/pendentes
- [ ] PUT /extrato/pendentes/{id} — usuário confirma

## Fase 6 — Investimentos ✓
- [x] Entidades: Investimento, SnapshotMensal
- [x] CRUD Investimentos
- [x] POST /investimentos/{id}/snapshot
- [x] GET /investimentos/{id}/historico — evolução mensal

## Fase 7 — Entrega ✓
- [x] Swagger/OpenAPI configurado
- [x] README com como rodar + exemplos Postman
- [x] Mencionar uso pessoal real no README
- [x] Commits organizados por feature
- [x] Push no GitHub
