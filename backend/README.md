# Streamletz Backend

Backend service for Streamletz music streaming application.

## Tech Stack

- Java 21 LTS
- Spring Boot 3.2.0
- PostgreSQL
- JWT Authentication
- Swagger/OpenAPI

## Getting Started

### Prerequisites

- JDK 21+
- Maven 3.8+
- PostgreSQL 14+

### Setup

1. Configure database in `application.properties` or use environment variables:
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/streamletz
SPRING_DATASOURCE_USERNAME=streamletz_user
SPRING_DATASOURCE_PASSWORD=changeme123
JWT_SECRET=your-secret-key
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:1124`

### API Documentation

Swagger UI: `http://localhost:1124/swagger-ui.html`
OpenAPI JSON: `http://localhost:1124/api-docs`

## Docker

Build and run with Docker:
```bash
docker build -t streamletz-backend .
docker run -p 1124:1124 streamletz-backend
```

## Project Structure

```
src/main/java/com/streamletz/
├── controller/     # REST API endpoints
├── service/        # Business logic
├── repository/     # Data access layer
├── model/          # Entity models
├── config/         # Configuration classes
└── util/           # Utility classes and DTOs
```

## Motto

**Your sound. Your stream. Your rules.**
