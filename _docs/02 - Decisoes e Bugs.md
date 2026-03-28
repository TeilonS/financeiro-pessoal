# Decisões e Bugs — Controle Financeiro Pessoal

## Decisões técnicas
| Data | Decisão | Motivo |
|------|---------|--------|
| 2026 | H2 para dev, PostgreSQL para prod | Agilidade local sem instalar banco |
| 2026 | JWT stateless | Sem sessão no servidor |
| 2026 | Isolamento por usuário no nível do repository | Segurança — nunca expor dados de outro usuário |
| 2026 | Categorização via Claude API | IA real com confiança mensurável, aprende padrões |
| 2026 | Confiança baixa → TransacaoPendente | Usuário revisa só o que a IA não tem certeza |
| 2026 | SnapshotMensal manual | Mais simples e confiável que integração com corretoras |
| 2026 | OFX + CSV como formatos de extrato | Suportados pela maioria dos bancos BR |

## Regras de negócio importantes
- Usuário só vê seus próprios Lancamentos, Metas, Investimentos
- Lancamento.tipo deve bater com Categoria.tipo
- Meta é por categoria + mês/ano — não pode duplicar
- SnapshotMensal é único por investimento + mês/ano
- TransacaoPendente some após confirmação → vira Lancamento real
- Confiança da IA: acima de 80% → categoriza automaticamente, abaixo → pendente

## Bugs resolvidos
| Data | Bug | Causa | Solução |
|------|-----|-------|---------|
| — | — | — | — |
