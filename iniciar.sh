#!/usr/bin/env bash
# Inicializa o backend (Spring Boot) e o frontend (React/Vite)
# e abre o navegador automaticamente.

# Garante que mvn e java (via SDKMAN) estejam no PATH ao iniciar pelo ícone do DE
export PATH="$HOME/.sdkman/candidates/maven/current/bin:$HOME/.sdkman/candidates/java/current/bin:/home/linuxbrew/.linuxbrew/bin:$PATH"

BACKEND_DIR="/run/media/system/dados/Programação/financeiro-pessoal"
FRONTEND_DIR="/run/media/system/dados/Programação/financeiro-frontend"
BACKEND_PORT=8080
FRONTEND_PORT=5173
FRONTEND_URL="http://localhost:${FRONTEND_PORT}"

# ──────────────────────────────────────────────
# Verifica se uma porta está em uso
port_em_uso() {
  ss -tlnp 2>/dev/null | grep -q ":$1 " || \
  nc -z localhost "$1" 2>/dev/null
}

# ──────────────────────────────────────────────
# Backend
if port_em_uso "$BACKEND_PORT"; then
  echo "[backend] já está rodando na porta ${BACKEND_PORT}."
else
  echo "[backend] iniciando Spring Boot..."
  cd "$BACKEND_DIR" || { echo "Pasta do backend não encontrada: $BACKEND_DIR"; exit 1; }
  nohup mvn spring-boot:run > /tmp/financeiro-backend.log 2>&1 &
  echo "[backend] PID: $!"

  # Aguarda o backend subir (até 30s)
  echo -n "[backend] aguardando... "
  for i in $(seq 1 30); do
    sleep 1
    if port_em_uso "$BACKEND_PORT"; then
      echo "pronto!"
      break
    fi
    echo -n "."
  done

  if ! port_em_uso "$BACKEND_PORT"; then
    echo ""
    echo "[backend] ERRO: não subiu em 30s. Verifique /tmp/financeiro-backend.log"
    exit 1
  fi
fi

# ──────────────────────────────────────────────
# Frontend
if port_em_uso "$FRONTEND_PORT"; then
  echo "[frontend] já está rodando na porta ${FRONTEND_PORT}."
else
  echo "[frontend] iniciando Vite..."
  cd "$FRONTEND_DIR" || { echo "Pasta do frontend não encontrada: $FRONTEND_DIR"; exit 1; }
  nohup npm run dev > /tmp/financeiro-frontend.log 2>&1 &
  echo "[frontend] PID: $!"

  # Aguarda o Vite subir (até 15s)
  echo -n "[frontend] aguardando... "
  for i in $(seq 1 15); do
    sleep 1
    if port_em_uso "$FRONTEND_PORT"; then
      echo "pronto!"
      break
    fi
    echo -n "."
  done

  if ! port_em_uso "$FRONTEND_PORT"; then
    echo ""
    echo "[frontend] ERRO: não subiu. Verifique /tmp/financeiro-frontend.log"
    exit 1
  fi
fi

# ──────────────────────────────────────────────
# Abre o navegador
echo "[browser] abrindo ${FRONTEND_URL}..."
xdg-open "$FRONTEND_URL" 2>/dev/null || \
  firefox "$FRONTEND_URL" 2>/dev/null || \
  google-chrome "$FRONTEND_URL" 2>/dev/null || \
  echo "Abra manualmente: ${FRONTEND_URL}"
