# INSTRUCOES_IA — Controle Financeiro Pessoal

## Desenvolvedor
Teilon Santos — Vitória da Conquista, BA
Farmacêutico em transição para TI. ADS Uniasselvi. Stack principal: Python/Flask.
Este projeto é Java/Spring Boot — segundo projeto Java após o Condomínio.

## Contexto do projeto
API REST de controle financeiro pessoal.
Usado pessoalmente por Teilon E como portfólio para entrevistas.
Diferenciais: importação de extrato OFX/CSV + categorização automática com IA + controle de investimentos.

## Antes de qualquer tarefa
1. Leia 00 - Visao Geral.md
2. Leia 01 - Arquitetura e Stack.md — entenda entidades e endpoints
3. Leia 02 - Decisoes e Bugs.md — não repita decisões já tomadas
4. Leia 03 - Roadmap.md — siga a ordem das fases

## Como me ajudar
- Teilon vem do Python/Flask — explique diferenças do mundo Java quando relevante
- Mostre código completo com package correto no topo
- Prefira exemplos práticos
- Siga a ordem do Roadmap — não pule fases
- Quando terminar uma tarefa, me lembre de marcar no Roadmap

## Regras do projeto
- Java 17+ — use records, var, features modernas
- Spring Boot 3 — não use padrões legados
- SEMPRE use DTOs — nunca exponha entidade diretamente na resposta
- JWT stateless — sem HttpSession
- Todo repository deve filtrar por usuário autenticado
- Bean Validation em todos os DTOs de entrada
- @ControllerAdvice centralizado para exceções
- Confiança IA: ≥80% → Lancamento direto, <80% → TransacaoPendente

## O que NÃO fazer
- Não expor senha ou dados sensíveis na resposta
- Não permitir que usuário A veja dados do usuário B
- Não usar Spring Boot 2 ou Java 8
- Não pular a fase de testes do Swagger antes de avançar para próxima fase
- Não usar XML de configuração — somente anotações
