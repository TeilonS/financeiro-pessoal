# Arquitetura e Stack — Controle Financeiro Pessoal

## Stack
| Camada | Tecnologia |
|--------|-----------|
| Linguagem | Java 17+ |
| Framework | Spring Boot 3 |
| ORM | Spring Data JPA + Hibernate |
| Segurança | Spring Security + JWT |
| Banco (dev) | H2 (em memória) |
| Banco (prod) | PostgreSQL |
| Build | Maven |
| Documentação | Swagger / springdoc-openapi |
| Extrato OFX | bibliotecas de parsing OFX/CSV |
| IA categorização | Claude API (Anthropic) |
| Versionamento | Git + GitHub |

## Estrutura de pastas
```
financeiro-pessoal/
├── src/
│   ├── main/
│   │   ├── java/com/teilon/financeiro/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── model/
│   │   │   │   ├── Usuario.java
│   │   │   │   ├── Categoria.java
│   │   │   │   ├── Lancamento.java
│   │   │   │   ├── Meta.java
│   │   │   │   ├── ExtratoBruto.java
│   │   │   │   ├── TransacaoPendente.java
│   │   │   │   ├── Investimento.java
│   │   │   │   └── SnapshotMensal.java
│   │   │   ├── security/        ← JWT + filtros
│   │   │   ├── dto/             ← request/response
│   │   │   ├── exception/       ← @ControllerAdvice
│   │   │   ├── extrato/         ← parsers OFX e CSV
│   │   │   ├── ia/              ← integração Claude API
│   │   │   └── FinanceiroApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
├── README.md
└── _docs/
```

## Entidades
| Entidade | Campos principais |
|----------|-------------------|
| `Usuario` | nome, email, senha (bcrypt) |
| `Categoria` | nome, tipo (RECEITA/DESPESA), cor hex |
| `Lancamento` | descrição, valor, data, tipo, categoria, usuario |
| `Meta` | categoria, valor limite mensal, mês/ano |
| `ExtratoBruto` | arquivo original, data upload, banco, status (PROCESSADO/PENDENTE) |
| `TransacaoPendente` | descrição original, valor, data, sugestão IA, aguardando confirmação |
| `Investimento` | nome, instituição, tipo (renda fixa, ações, etc) |
| `SnapshotMensal` | investimento, mês/ano, valor informado |

## Endpoints
```
POST   /auth/register
POST   /auth/login

GET    /categorias
POST   /categorias
DELETE /categorias/{id}

GET    /lancamentos              ← filtros: ?mes=3&ano=2026&tipo=DESPESA
POST   /lancamentos
PUT    /lancamentos/{id}
DELETE /lancamentos/{id}

GET    /resumo?mes=3&ano=2026   ← total receitas, despesas, saldo, breakdown por categoria
GET    /metas
POST   /metas
GET    /metas/alertas           ← categorias que ultrapassaram o limite

POST   /extrato/upload          ← envia OFX ou CSV
GET    /extrato/pendentes       ← transações aguardando categorização
PUT    /extrato/pendentes/{id}  ← usuário confirma/corrige categoria

GET    /investimentos
POST   /investimentos
PUT    /investimentos/{id}
GET    /investimentos/{id}/historico
POST   /investimentos/{id}/snapshot  ← registra saldo do mês
```

## Variáveis de ambiente
```
JWT_SECRET=
DATABASE_URL=
DATABASE_USERNAME=
DATABASE_PASSWORD=
ANTHROPIC_API_KEY=    ← para categorização com IA
```
