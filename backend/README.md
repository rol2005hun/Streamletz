# Streamletz Backend

Backend service for Streamletz music streaming application.

## Tech Stack

- Java 17+
- Spring Boot 3.2.0
- PostgreSQL
- JWT Authentication
- Swagger/OpenAPI

## Getting Started

### Prerequisites

- JDK 17+
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

The API will be available at `http://localhost:8080`

### API Documentation

Swagger UI: `http://localhost:8080/swagger-ui.html`
OpenAPI JSON: `http://localhost:8080/api-docs`

## Docker

Build and run with Docker:
```bash
docker build -t streamletz-backend .
docker run -p 8080:8080 streamletz-backend
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
