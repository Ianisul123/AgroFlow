# Architecture and Domain Model Notes

This document explains the current backend architecture, the difference between an anemic/IMIC-style model and a Rich Model approach, and how test coverage can be measured with SonarQube.

## Current Architecture

The backend uses a layered Spring Boot structure. Each layer has a separate responsibility, so HTTP code, business decisions, persistence details, and API data contracts do not all end up in the same class.

### Controller Layer

Controllers receive HTTP requests, validate request DTOs, call services, and return HTTP responses. They should not contain infrastructure configuration or persistence logic.

Current examples:

- `FarmController`
- `IrrigationZoneController`

The CORS configuration was moved out of the controllers and into `CorsConfig`. This keeps the controllers focused on API endpoints and avoids duplicated CORS annotations.

### DTO Layer

DTOs define the input and output shape of the API. Request DTOs also contain validation annotations, such as required fields or numeric constraints.

Current examples:

- `CreateFarmRequest`
- `UpdateFarmRequest`
- `CreateIrrigationZoneRequest`
- response DTOs such as `CreateFarmResponse` and `CreateIrrigationZoneResponse`

DTOs are useful because the external API contract can differ from the internal domain model or database entity.

### Service Layer

Services coordinate use cases and contain application/business rules that are not HTTP-specific. For example, `IrrigationZoneService` checks that a farm exists before creating an irrigation zone and validates the zone data before saving it.

Current examples:

- `FarmService`
- `IrrigationZoneService`

### Domain Layer

Domain classes represent business concepts used by the application logic. In this project, examples are:

- `Farm`
- `IrrigationZone`

The current domain model is relatively lightweight. Some behavior exists, such as `Farm.updateInformation`, but most validation and orchestration currently lives in services.

### Repository Layer

Repositories hide persistence details from services. Services work with domain objects instead of directly depending on JPA entities.

Current examples:

- `FarmRepository`
- `IrrigationZoneRepository`

These repository classes wrap Spring Data JPA repositories and use mappers to convert between domain objects and persistence entities.

### Entity Layer

Entities describe how data is stored in the database using JPA annotations.

Current examples:

- `FarmEntity`
- `IrrigationZoneEntity`

Entities are persistence-specific and should not be exposed as the main API contract.

### Mapper Layer

Mappers translate between domain objects and persistence entities.

Current examples:

- `FarmMapper`
- `IrrigationZoneMapper`

This separation prevents database concerns from leaking into the service and controller layers.

## Anemic/IMIC-Style Model vs Rich Model

The current implementation is closer to an anemic-domain or IMIC-style approach. In this style, domain objects mostly hold data, while services contain most of the validation and business logic.

In a Rich Model approach, the domain objects contain both state and behavior. Business rules are placed close to the data they protect. For example, instead of having `IrrigationZoneService` validate all irrigation-zone rules, the `IrrigationZone` class could protect its own invariants through constructors or methods.

### Anemic/IMIC-Style Model

How it works:

- Domain objects mostly store data.
- Services perform validations, decisions, and orchestration.
- Controllers call services and return DTO responses.
- Repositories handle persistence through mappers and entities.

Effect on application structure:

- Services become the main place for business logic.
- Domain classes stay simple and easy to serialize/test.
- The flow is easy to follow for CRUD-focused applications.

Advantages:

- Simple to understand for small applications.
- Fits CRUD use cases well.
- Easy to combine with Spring services, DTOs, and repositories.
- Keeps persistence and HTTP concerns separate when layers are used correctly.

Disadvantages:

- Business rules can become scattered across services as the project grows.
- Domain objects may not protect their own valid state.
- More complex domain behavior can become procedural instead of object-oriented.
- Similar validation rules can be duplicated across use cases if discipline is not maintained.

### Rich Model

How it works:

- Domain objects contain business behavior and protect their own invariants.
- Services become thinner and mainly coordinate use cases.
- Business rules are expressed through domain methods instead of external procedural checks.

Effect on application structure:

- More logic moves from services into domain classes.
- Services delegate decisions to domain objects.
- Tests can focus strongly on domain behavior without needing Spring.

Advantages:

- Business rules are easier to find because they live near the related data.
- Domain objects are harder to put into an invalid state.
- Better fit for complex business domains.
- Encourages object-oriented design and clearer domain language.

Disadvantages:

- More design effort is needed at the start.
- It can be overkill for simple CRUD applications.
- Mapping between domain objects and persistence entities can become more complex.
- Developers must clearly separate domain behavior from persistence and API concerns.

## Why This Project Uses the Current Approach

For the current AgroFlow backend, the chosen layered anemic/IMIC-style approach is practical because the application is still mostly CRUD-oriented:

- Create, read, update, and delete farms.
- Create and list irrigation zones for a farm.
- Validate simple fields such as name, area size, and moisture threshold.

This approach keeps the structure clear and easy to explain. It also separates controllers, services, repositories, entities, DTOs, and mappers, which makes the backend maintainable at the current project size.

If the project grows and starts to include more complex irrigation decisions, scheduling rules, sensor rules, or crop-specific calculations, moving toward a richer domain model would be a good improvement. For example, `IrrigationZone` could contain methods such as `requiresIrrigation(currentMoisture)` or protect threshold changes through a domain method.

## Pros and Cons of the Current Architecture

Pros:

- Clear separation of responsibilities between API, business logic, domain, and persistence.
- Controllers stay small and focused on HTTP.
- Services are easy to unit test with mocked repositories.
- Repositories hide JPA details from the service layer.
- DTOs protect the API contract from database/entity changes.
- Mappers make conversions explicit.
- Centralized CORS configuration avoids duplicated controller annotations.

Cons:

- More classes are needed than in a very small CRUD prototype.
- Mapping code adds maintenance overhead.
- Business rules can become too service-heavy if the domain model stays too passive.
- The current domain model does not fully enforce all invariants by itself.
- If many use cases are added, service classes may become large unless logic is moved into domain methods.

## SonarQube and Test Coverage

The project already uses JaCoCo and the SonarQube Gradle plugin. JaCoCo generates the XML coverage report that SonarQube can read.

Run tests and generate the local coverage report:

```powershell
.\gradlew.bat clean test jacocoTestReport
```

Local HTML coverage report:

```text
build/reports/jacoco/test/html/index.html
```

The XML report used by SonarQube:

```text
build/reports/jacoco/test/jacocoTestReport.xml
```

Example SonarQube command when a local SonarQube server is running:

```powershell
.\gradlew.bat sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.token=<your-token>
```

The important evidence to include in the report is:

- The SonarQube dashboard screenshot.
- The coverage percentage.
- The number of tests.
- The main uncovered areas and what could be tested next.

## References

- Martin Fowler, "Anemic Domain Model": https://martinfowler.com/bliki/AnemicDomainModel.html
