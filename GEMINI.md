# CLAUDE.md — Configuração Global de Teilon Santos

## Quem sou eu
- Nome: Teilon Santos
- Localização: Vitória da Conquista, BA, Brasil
- Formação: ADS na Uniasselvi (2024–2026), foco em Segurança da Informação
- Background: Farmacêutico em transição para TI
- Stack principal: Python, Flask, PostgreSQL, Linux (Bazzite)
- GitHub: github.com/TeilonS
- LinkedIn: linkedin.com/in/teilon-santos

## Pasta de trabalho
Todos os projetos ficam em: /dados/Programação/
Nunca crie arquivos fora dessa pasta sem perguntar primeiro.

## Como me responder
- Sempre em português
- Direto ao ponto — mostre o código, não enrole
- Quando resolver bug, diga qual arquivo mudou e o que mudou
- Sempre termine entregas com: git add . && git commit -m "msg" && git push

## Regras globais de código
- NUNCA use cores hardcoded em style="" inline no HTML/CSS
- NUNCA hardcode credenciais — use variáveis de ambiente
- SEMPRE confirme a causa no arquivo real antes de diagnosticar um bug

## Projetos existentes

### Validades app-py (MedControl)
- Pasta: /dados/Programação/Validades app-py
- Tipo: Flask (Python)
- Status: Em produção — www.medcontrol.app.br
- Stack: Flask + SQLAlchemy + PostgreSQL (Supabase) + Render + Mercado Pago + Resend
- Roles: superadmin, dono_rede, filial
- ATENÇÃO: Não sugerir Railway, não reativar scanner de barras, não usar cores hardcoded

## Design de Frontend
Antes de criar qualquer interface visual, leia e siga AMBOS os skills:
1. ~/.claude/skills/frontend-design/SKILL.md (Anthropic oficial)
2. ~/.claude/skills/ui-ux-pro-max/SKILL.md (paletas, fontes, estilos)
Nunca use gradientes roxos genéricos, fontes Inter/Roboto padrão ou layouts de cards repetitivos.

## Ferramentas e referencias
- Padroes de commits: ~/.claude/referencias/padroes-commits/README.md
- README template: ~/.claude/referencias/readme-template/BLANK_README.md
- Rath (visualizacao): ~/.claude/referencias/rath/README.md
- Trigger.dev: para background jobs e tarefas longas com IA
- Hoppscotch: https://hoppscotch.io (testar APIs no browser)

Quando criar README de qualquer projeto, use o template como base.
Quando criar commits, siga os padroes-commits.
Quando precisar de background jobs no projeto financeiro, use Trigger.dev.

## Sistema de Saude & Performance
- Pasta: /run/media/system/dados/Programação/saude/
- Opcao 7 no menu agente
- App local: saude/app/protocolo.html
- Slash command: /abrir-protocolo
- Perfil Teilon: 22 anos, 71kg, 1,70m
- Restricoes: sem corrida, sem unipodal E, sem dorsiflexao profunda E, sem saltos
- Pendencias: palmilha ortopedica, avaliacao fisiatra, perfil da esposa

## Estilo de resposta
Frase curta. Sem introdução. Sem cumprimento.
Ferramenta primeiro. Resultado primeiro. Para.
Remove artigos desnecessários.
Exemplo: "Corrigir bug" não "Vou corrigir o bug agora".

## Analise e correcao de bugs
Quando eu pedir analise, revisao ou correcao de bugs em qualquer projeto:
1. Execute: python3 ~/.claude/mdnator.py . > /tmp/projeto-completo.md
2. Leia /tmp/projeto-completo.md
3. Analise o projeto completo de uma vez
4. Aponte todos os problemas encontrados
5. Corrija diretamente nos arquivos

## Estilo de resposta
Frase curta. Sem introducao. Sem cumprimento.
Ferramenta primeiro. Resultado primeiro. Para.
Remove artigos desnecessarios.

## Instrucoes especificas Gemini
- Sempre leia os _docs/ do projeto atual antes de começar
- Leia os skills em ~/.claude/skills/ antes de criar qualquer interface
- Use --yolo ja esta ativo, execute sem pedir confirmacao
- Ao retomar trabalho, leia _docs/02 - Decisoes e Bugs.md para entender o historico
