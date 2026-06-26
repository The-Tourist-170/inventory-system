#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}==> Running tests and building JARs for all services...${NC}"

(cd "$SCRIPT_DIR/inventory-service" && ./mvnw clean verify -q) &
(cd "$SCRIPT_DIR/order-service"     && ./mvnw clean verify -q) &
(cd "$SCRIPT_DIR/product-service"   && ./mvnw clean verify -q) &
(cd "$SCRIPT_DIR/api-gateway"       && ./mvnw clean verify -q) &
wait

echo -e "${GREEN}All services built and tested.${NC}"

echo -e "${YELLOW}==> Building Docker images and starting containers...${NC}"
docker compose -f "$SCRIPT_DIR/docker-compose.yml" up --build -d

echo ""
echo -e "${GREEN}All services starting. Ports:${NC}"
echo "  api-gateway       → http://localhost:9000"
echo "  product-service   → http://localhost:8080"
echo "  order-service     → http://localhost:8081"
echo "  inventory-service → http://localhost:8082"
echo ""
echo "View logs:  docker compose -f '$SCRIPT_DIR/docker-compose.yml' logs -f"
echo "Stop:       docker compose -f '$SCRIPT_DIR/docker-compose.yml' down"
