#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

BLUE='\033[0;34m'
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

cleanup() {
  echo ""
  echo -e "${YELLOW}==> Shutting down...${NC}"
  kill $INVENTORY_PID $ORDER_PID $PRODUCT_PID $GATEWAY_PID $KEYCLOAK_LOG_PID 2>/dev/null || true
  wait $INVENTORY_PID $ORDER_PID $PRODUCT_PID $GATEWAY_PID $KEYCLOAK_LOG_PID 2>/dev/null || true
  echo -e "${GREEN}All services stopped.${NC}"
  echo -e "DBs/Keycloak still running. To stop them: ${CYAN}docker compose -f docker-compose.dev.yml down${NC}"
  exit 0
}

trap cleanup SIGINT SIGTERM

echo -e "${CYAN}==> Starting databases (dev profile)...${NC}"
docker compose -f "$SCRIPT_DIR/docker-compose.dev.yml" up -d

echo -e "${YELLOW}==> Waiting for databases to be ready...${NC}"
until docker compose -f "$SCRIPT_DIR/docker-compose.dev.yml" exec -T postgres pg_isready -U admin > /dev/null 2>&1; do
  sleep 2
done
echo -e "${GREEN}PostgreSQL is ready.${NC}"
until docker compose -f "$SCRIPT_DIR/docker-compose.dev.yml" exec -T mongo mongosh --quiet --eval 'db.runCommand("ping").ok' > /dev/null 2>&1; do
  sleep 2
done
echo -e "${GREEN}MongoDB is ready.${NC}"

echo ""
echo -e "${CYAN}==> Starting all services with DevTools (auto-restart on class changes)...${NC}"
echo ""

run_service() {
  local name=$1
  local color=$2
  local dir=$3
  (cd "$SCRIPT_DIR/$dir" && ./mvnw spring-boot:run -q 2>&1 | while IFS= read -r line; do
    echo -e "${color}[$name]${NC} $line"
  done) &
}

run_service "inventory" "$GREEN" "inventory-service"
INVENTORY_PID=$!
run_service "order"    "$YELLOW" "order-service"
ORDER_PID=$!
run_service "product"  "$CYAN" "product-service"
PRODUCT_PID=$!
run_service "gateway"  "$RED" "api-gateway"
GATEWAY_PID=$!

(docker compose -f "$SCRIPT_DIR/docker-compose.dev.yml" logs -f keycloak 2>&1 | while IFS= read -r line; do
  echo -e "${BLUE}[keycloak]${NC} $line"
done) &
KEYCLOAK_LOG_PID=$!

echo ""
echo -e "${GREEN}All services starting...${NC}"
echo ""
echo "  api-gateway       → http://localhost:9000"
echo "  product-service   → http://localhost:8080"
echo "  order-service     → http://localhost:8081"
echo "  inventory-service → http://localhost:8082"
echo "  keycloak          → http://localhost:8181"
echo ""
echo -e "${YELLOW}DevTools active: recompile (IDE save or './mvnw compile') to trigger restart.${NC}"
echo -e "${YELLOW}Press Ctrl+C to stop all services.${NC}"

wait
