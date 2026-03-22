# Repository Guidelines

## Project Structure & Module Organization
- `app/auth-web/`: Spring Boot web app (controllers, security, config, resources).
- `app/common/auth-common/`: Shared DTOs used across modules.
- `app/biz/auth-service-impl/`: Domain services, models, utilities.
- Key entries: `KipAuthBootApplication.java`, `SecurityConfig.java`, `UserAuthController.java`.
- Config: `application.yml` (port 5001, JWT, datasource), optional `bootstrap-local.yml` for Nacos.

## Build, Test, and Development Commands
- Build all modules: `mvn clean install` (use `-DskipTests` to speed up).
- Run web app: `mvn -pl app/auth-web -am spring-boot:run`.
  - Use profile/env if needed: `-Dspring-boot.run.profiles=local`.
- Run tests (all modules): `mvn test`.

## Coding Style & Naming Conventions
- Java 21, Spring Boot 3.5.x.
- Indentation: 4 spaces; UTF-8; keep lines reasonably short (~120 cols).
- Packages: lowercase dot-separated (e.g., `xyz.kip.security`).
- Classes: PascalCase; methods/fields: camelCase; constants: UPPER_SNAKE_CASE.
- REST: base path `"/api/auth"`; resources use kebab or path segments (e.g., `/user/{userId}`).
- Prefer DTOs in `auth-common` and service models in `auth-service-impl` to keep layers clean.

## Testing Guidelines
- Frameworks: Spring Boot Test, Security Test (deps present). Add tests under `src/test/java`.
- Naming: `*Tests.java` for unit/integration tests (e.g., `UserAuthControllerTests`).
- Run locally with `mvn test`; target critical flows: login, token validation, password change.
- Aim for meaningful assertions; mock external systems. Add integration tests for `SecurityFilterChain`.

## Commit & Pull Request Guidelines
- Commits: small, focused, imperative mood (e.g., "Add JWT validation to filter"). Conventional Commits are encouraged but not required.
- PRs should include: scope/intent, linked issues, how to test (commands, sample requests), and screenshots or curl examples when relevant.
- Keep changes within module boundaries; update README and configuration notes when behavior changes.

## Security & Configuration Tips
- JWT: rotate and externalize `auth.jwt.secret` in production (env vars or config server). Avoid committing secrets.
- DB: `application.yml` includes MySQL settings; configure local DB or disable if using in-memory stores.
- Nacos: `bootstrap-local.yml` contains discovery/config endpoints—only enable on networks where available.
