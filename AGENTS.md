# Repository Guidelines

## Project Structure & Module Organization
- Multi-module Maven build driven by the root `pom.xml`.
- Core library lives in `flyway-core/`; CLI and build tool integrations live in `flyway-commandline/`, `flyway-gradle-plugin/`, and `flyway-maven-plugin/`.
- Database-specific support modules follow the `flyway-<db>/` pattern (for example `flyway-mysql/`, `flyway-sqlserver/`, `flyway-firebird/`, `flyway-gcp-bigquery/`).
- Source code uses the standard Maven layout in each module (e.g., `flyway-core/src/main/java`).
- Documentation assets are under `documentation/`; BOM metadata is in `flyway-bom/`.

## Build, Test, and Development Commands
- `./mvnw -DskipTests package` — build all modules without running tests.
- `./mvnw -pl flyway-core -am package` — build a single module plus dependencies.
- `./mvnw test` — run unit tests (only relevant once tests exist in `src/test/java`).
- `./mvnw -pl flyway-mysql -am test` — run tests for a specific module.

## Coding Style & Naming Conventions
- Follow existing Java conventions: 4-space indentation, braces on the same line, and package names under `org.flywaydb.*`.
- Class names use `UpperCamelCase`, methods/fields use `lowerCamelCase`, constants use `UPPER_SNAKE_CASE`.
- Logging uses Lombok `@CustomLog` with the `LOG` field name (see `lombok.config`); other Lombok log annotations are disabled.

## Testing Guidelines
- This repository currently has no `src/test` trees; add tests using the Maven layout (`src/test/java`, `src/test/resources`).
- Name test classes with the `*Test.java` suffix so Surefire detects them.
- Keep database-specific tests in their corresponding `flyway-<db>/` module.

## Commit & Pull Request Guidelines
- Commit messages are short, imperative, and sentence case (e.g., “Add support for OceanBase database integration”).
- PRs should include a concise summary, any linked issues, and required documentation updates (see `CONTRIBUTING.md`).
- If behavior changes, include repro steps and note affected modules (for example `flyway-core` or `flyway-mysql`).

## Security & Configuration Tips
- Do not commit credentials or local connection strings; use local environment variables or Maven settings files instead.
- For database testing, document any required container/image versions in the PR description.
