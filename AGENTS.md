# Agent Notes

This repo is a small Spring Boot microservices demo. There is no root build file; each service is a standalone Maven project.

## Build & Test

- Use the Maven wrapper in each service directory:
  - `cd <service> && ./mvnw clean verify`
  - `./mvnw -Dtest=<ClassName> test` to run a single test class.
- Tests require **Docker** because they use Testcontainers:
  - `inventory-service` and `order-service` start a `postgres:latest` container.
  - `product-service` starts a `mongo:latest` container.
- Java target is **21** (`<java.version>21</java.version>`). The current environment runs JDK 25, which is fine for compilation.
- Lombok is used everywhere; each `pom.xml` already configures `maven-compiler-plugin` `annotationProcessorPaths` for it. Do not remove those blocks.

## Service Layout

| Service | Package | Port (dev) | Tech |
|---|---|---|---|
| `api-gateway` | `com.tourist.api_gateway` | **not set** (defaults to `8080`) | Spring Cloud Gateway Server MVC (Spring Boot 4.1.0) |
| `product-service` | `com.tourist.microservices.product` | `8080` | Spring MVC + Spring Data MongoDB |
| `order-service` | `com.tourist.order` | `8081` | Spring MVC + Spring Data JPA + PostgreSQL + Flyway + OpenFeign |
| `inventory-service` | `com.tourist.inventory` | `8082` | Spring MVC + Spring Data JPA + PostgreSQL + Flyway |

- `api-gateway` only routes `/api/product` to `http://localhost:8080`. It has **no routes** for order or inventory.
- `order-service` calls inventory via a hardcoded OpenFeign client at `http://localhost:8082`. Make sure inventory is running on that port for order placement to work.
- The gateway and product service both default to port `8080`; start the gateway on a different port when running both locally, e.g. `-Dserver.port=8083`.

## Local Dev Data

- `order-service` and `inventory-service` expect PostgreSQL on `localhost:5432` with databases `order_service` / `inventory_service`, user `admin`, password `admin123` (see `application.properties`).
- `product-service` expects MongoDB at `mongodb://admin:admin123@localhost:6543/productService?authSource=admin`.
- Flyway migrations live in `src/main/resources/db/migration/` for the PostgreSQL services.

## Spring Boot Versions

- `product-service`, `order-service`, `inventory-service`: Spring Boot **4.0.6**.
- `api-gateway`: Spring Boot **4.1.0** with Spring Cloud **2025.1.2**.

## Notable Code Conventions

- Package naming is inconsistent by service; do not assume a shared root package.
- `order-service` entry class has `@EnableFeignClients`.
- `api-gateway` package was generated as `com.tourist.api_gateway` because `api-gateway` is an invalid package name.
