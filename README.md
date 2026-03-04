<img src="https://capsule-render.vercel.app/api?type=waving&height=300&color=gradient&text=Multitenant-starter"/>

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

The starter reads properties under `katarem.multitenant`.

```yaml
katarem:
  multitenant:
    default: auth
    datasources:
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

Equivalent `application.properties`:

```properties
katarem.multitenant.default=auth
katarem.multitenant.datasources.auth.url=jdbc:postgresql://localhost:5432/auth
katarem.multitenant.datasources.auth.username=auth
katarem.multitenant.datasources.auth.password=auth
katarem.multitenant.datasources.tenant-1.url=jdbc:postgresql://localhost:5433/app
katarem.multitenant.datasources.tenant-1.username=tenant1
katarem.multitenant.datasources.tenant-1.password=tenant1
katarem.multitenant.datasources.tenant-2.url=jdbc:postgresql://localhost:5434/app
katarem.multitenant.datasources.tenant-2.username=tenant2
katarem.multitenant.datasources.tenant-2.password=tenant2
```

### Supported Properties

- `katarem.multitenant.default`
- `katarem.multitenant.datasources.<name>.url`
- `katarem.multitenant.datasources.<name>.username`
- `katarem.multitenant.datasources.<name>.password`
- `katarem.multitenant.datasources.<name>.driver-class-name` (optional)

### Validation Rules

- Startup fails if `katarem.multitenant.datasources` is missing or empty.
- Startup fails if `katarem.multitenant.default` is missing.
- Startup fails if `katarem.multitenant.default` is not present in `datasources`.

## Runtime Routing Model

Routing depends on `TenantContext`:

- `TenantContext.set("tenant-1")` routes to datasource `tenant-1`
- `TenantContext.clear()` falls back to `katarem.multitenant.default`

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

- `multitenant-starter/target/multitenant-starter-v1.3.3.jar`

## Use in Another Project

Add dependency:

```xml
<dependency>
  <groupId>io.github.katarem</groupId>
  <artifactId>multitenant-starter</artifactId>
  <version>v1.3.3</version>
</dependency>
```

Then define `katarem.multitenant` in your `application.yaml` and set/clear `TenantContext` around tenant-scoped operations.

### Gradle

```gradle
dependencies {
    implementation("io.github.katarem:multitenant-starter:v1.3.3")
}
```

## Install via JitPack

Use this option when you want to consume the library directly from this GitHub repository.

1. Create and push a Git tag in this repository (example: `v1.3.3`).
2. Open `https://jitpack.io/#katarem/multi-tenant/v1.3.3` and wait for a successful build.
3. In your target project, add the JitPack repository:

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

4. Add the starter dependency. For this repository, use:

```xml
<dependency>
  <groupId>com.github.katarem.multitenant</groupId>
  <artifactId>multitenant-starter</artifactId>
  <version>1.3.2</version>
</dependency>
```

5. Run:

```bash
mvn -U dependency:resolve
```

If you are unsure about coordinates for a given tag, copy the exact snippet shown by JitPack on the build page for that version.

### Gradle (JitPack)

```gradle
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.katarem.multitenant:multitenant-starter:1.3.2")
}
```

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

## Contributing

Contributions are welcome.

1. Fork the repository and create a feature branch.
2. Keep changes focused and aligned with the scope of `multitenant-starter`.
3. Ensure the project builds before opening a PR:

```bash
./mvnw clean install
```

4. Open a pull request with:
- a clear description of the change
- motivation and expected behavior
- notes about compatibility impact (if any)

When changing public behavior or configuration, update the README accordingly.
If you add tests in the future, include how to run them in this section.

## License

This project is licensed under the GNU General Public License v3.0 (GPL-3.0).  
See the `LICENSE` file for details.

<img src="https://capsule-render.vercel.app/api?type=waving&height=300&color=gradient&section=footer"/>