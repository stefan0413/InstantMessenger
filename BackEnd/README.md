# InstantMessenger — BackEnd

Spring Boot application providing the REST API and WebSocket server for InstantMessenger.

## Stack

- Java 25, Spring Boot 4
- PostgreSQL (Flyway migrations in `src/main/resources/db/migration`)
- STOMP over WebSocket for real-time messaging
- JWT authentication (HMAC-SHA256)
- AWS S3 for file uploads

## Running

```bash
./mvnw spring-boot:run
```

Requires a PostgreSQL instance. Default connection: `localhost:5432/messenger` (user `postgres`, password `root`).

## Tests

```bash
./mvnw test
```
