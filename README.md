# Financeiro Pessoal — API & Dashboard

Sistema de controle financeiro pessoal completo, com importação de extratos, categorização inteligente, controle de cartões de crédito, metas e gestão de investimentos.

## 🚀 Tecnologias
- **Backend:** Java 17, Spring Boot 3, Spring Security, JWT, JPA/Hibernate
- **Frontend:** React, Tailwind CSS, Recharts, Lucide React
- **Banco de Dados:** H2 (Desenvolvimento), PostgreSQL (Produção)
- **Documentação:** Swagger/OpenAPI

## 🛠️ Funcionalidades
- [x] **Gestão de Lançamentos:** CRUD completo de receitas e despesas com filtros por mês/ano.
- [x] **Categorias Customizadas:** Organização por tipo e cores.
- [x] **Importação de Extrato:** Suporte a arquivos OFX e CSV (Inter e C6 Bank).
- [x] **Revisão de Transações:** Transações importadas ficam pendentes até a confirmação da categoria.
- [x] **Metas e Orçamentos:** Definição de limites por categoria com alertas de estouro.
- [x] **Cartões de Crédito:** Controle de faturas e limite disponível.
- [x] **Investimentos:** Registro de snapshots mensais para acompanhamento da evolução patrimonial.
- [x] **Resumo e Gráficos:** Dashboard visual com fluxo de caixa e evolução mensal.

## 🏁 Como Rodar

### Pré-requisitos
- JDK 17+
- Node.js 18+
- Maven

### Backend
```bash
# Na raiz do projeto
./mvnw spring-boot:run
```
A API estará disponível em `http://localhost:8080`.
Swagger UI: `http://localhost:8080/swagger-ui.html`

### Frontend
```bash
cd financeiro-frontend
npm install
npm run dev
```
O dashboard estará disponível em `http://localhost:5173`.

## 📖 Documentação (API)
A API segue o padrão REST e utiliza JWT para autenticação. 
1. Crie um usuário em `POST /auth/register`
2. Obtenha o token em `POST /auth/login`
3. Use o token no header `Authorization: Bearer <seu_token>` para as demais requisições.

## 📝 Licença
Este projeto é de uso pessoal e portfólio. Desenvolvido por [Teilon Santos](https://github.com/TeilonS).
