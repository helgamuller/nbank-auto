# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run all tests
mvn clean test

# Run all tests in a package
mvn test -Dtest="iteration1.api.*"
mvn test -Dtest="iteration2.ui.*"

# Run a single test class
mvn test -Dtest=iteration1.api.CreateUserTest

# Run a single test method
mvn test -Dtest=iteration1.api.CreateUserTest#adminCanCreateUserWithValidData
```

The application under test must be running at `http://localhost:4111` (configured in `src/main/resources/config.properties`).

## Architecture

**nbank-auto** is a Java 23 / Maven test automation framework for a banking web application. It covers both REST API and browser UI. Tests are grouped into **iteration1** (basic CRUD) and **iteration2** (deposits, transfers, name changes, deletion).

### Layer overview

| Layer | Location | Role |
|---|---|---|
| Config & Specs | `src/main/java/.../config/`, `requestspecs/`, `responsespecs/` | Singleton config, reusable REST Assured specs |
| HTTP | `httprequests/` | `HttpRequest` base → `CrudRequester` → `ValidatedCrudRequester<T>` |
| Steps | `steps/` | `AdminSteps`, `UserSteps` — business-level operations used by tests |
| Models | `models/` | Lombok `@Data @Builder` POJOs for all requests/responses |
| Generators | `generators/` | `RandomModelGenerator` (reflection + `@GeneratingRule` regex annotation) |
| Model comparison | `modelcomparison/` | `ModelComparator` + `ModelAssertions` for request/response field validation |
| Tests | `src/test/java/iteration{1,2}/{api,ui}/` | JUnit 5 test classes |

### Key design patterns

- **`ValidatedCrudRequester<T>`** — used in positive tests; deserializes response body to the typed model automatically.
- **`CrudRequester`** (untyped) — used in negative tests; validates status code / error message via `ResponseSpecs`, no deserialization.
- **`BaseTest`** — JUnit 5 base class with `@BeforeEach`/`@AfterEach`; initialises `SoftAssertions` and cleans up created users after each test via admin API.
- **`AdminSteps` / `UserSteps`** — wrap HTTP calls at the business level; `UserSteps` also handles browser-localStorage token injection for UI tests.
- **`RandomModelGenerator`** — generates test data via reflection; annotate a model field with `@GeneratingRule(regex = "...")` to control generated values.
- **`ModelComparator`** — compares request fields against response fields using mapping rules from `src/main/resources/model-comparison.properties` (supports nested paths like `customer.name`).

### Authentication

- **Admin**: hardcoded Basic Auth header in `RequestSpecs`.
- **Users**: `POST /api/v1/auth/login` → `Authorization` header cached per session in a map; UI tests inject the token into browser `localStorage` via JavaScript.

### Frameworks

| Library | Version | Purpose |
|---|---|---|
| JUnit Jupiter | 5.12.2 | Test runner (`@ParameterizedTest`, `@MethodSource`) |
| REST Assured | 5.5.1 | API test DSL |
| Selenide | 7.9.3 | Browser automation (wraps Selenium) |
| Lombok | 1.18.38 | `@Data`, `@Builder`, `@AllArgsConstructor` |
| Jackson | 2.19.0 | JSON ser/deser |
| AssertJ | 3.27.3 | Fluent assertions + custom `ModelAssertions` |
| rgxgen | 1.4 | Regex-based string generation |
