# multitenant-starter

`multitenant-starter` is a Spring Boot auto-configuration library for datasource-per-tenant routing.

It creates one `HikariDataSource` pool per configured tenant and exposes a primary routing `DataSource` that resolves the current tenant from `TenantContext`.

## What This Library Provides

- Auto-configures a primary routing `DataSource`
- Creates and manages one connection pool per datasource entry
- Uses `TenantContext` (`ThreadLocal`) as the routing key
- Supports a configurable default datasource
- Validates startup configuration early

## Modules in This Repository

- `multitenant-starter`: the library (main artifact)
- `multitenant-demo`: demo application that shows the library in action

## Requirements

- Java 21
- Spring Boot 3.5.x

## Configuration

The starter reads properties under `app.datasources`.

```yaml
app:
  datasources:
    default-ds: auth
    configs:
      auth:
        url: jdbc:postgresql://localhost:5432/auth
        username: auth
        password: auth
      tenant-1:
        url: jdbc:postgresql://localhost:5433/app
        username: tenant1
        password: tenant1
      tenant-2:
        url: jdbc:postgresql://localhost:5434/app
        username: tenant2
        password: tenant2
```

### Supported Properties

- `app.datasources.default-ds`
- `app.datasources.configs.<name>.url`
- `app.datasources.configs.<name>.username`
- `app.datasources.configs.<name>.password`
- `app.datasources.configs.<name>.driver-class-name` (optional)

### Validation Rules

- Startup fails if `app.datasources.configs` is missing or empty.
- Startup fails if `default-ds` is not present in `configs`.

## Runtime Routing Model

Routing depends on `TenantContext`:

- `TenantContext.set("tenant-1")` routes to datasource `tenant-1`
- `TenantContext.clear()` falls back to `default-ds`

Minimal usage pattern:

```java
TenantContext.set(tenantId);
try {
    // execute repository/JdbcTemplate operations
} finally {
    TenantContext.clear();
}
```

## Build the Library

From repository root:

```bash
./mvnw clean install
```

Library artifact:

- `multitenant-starter/target/multitenant-starter-1.0.0.jar`

## Use in Another Project

Add dependency:

```xml
<dependency>
  <groupId>com.katarem</groupId>
  <artifactId>multitenant-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

Then define `app.datasources` in your `application.yaml` and set/clear `TenantContext` around tenant-scoped operations.

## Demo Application (Reference Only)

`multitenant-demo` is included only to demonstrate behavior.

Run the demo app:

```bash
./mvnw -pl multitenant-demo spring-boot:run
```

Test routing quickly:

```bash
curl http://localhost:8080/api/tenants/tenant-1/probe
curl http://localhost:8080/api/tenants/tenant-2/probe
```

## Notes

- This starter currently uses programmatic `TenantContext` assignment.
- Tenant propagation across async boundaries is not handled automatically.

## License

No license file is currently included in this repository.
