# GitHub Copilot Instructions for Streamletz

## Project Overview
Streamletz is a music streaming application with:
- **Backend**: Java Spring Boot application
- **Frontend**: Svelte application with TypeScript
- **Architecture**: RESTful API with JWT authentication

## Technology Stack

### Backend
- Java 17+
- Spring Boot
- Spring Security with JWT
- JPA/Hibernate
- Maven

### Frontend
- Svelte
- TypeScript
- Vite
- SCSS

## Code Style Guidelines

### Java (Backend)
- Follow standard Java naming conventions
- Use Spring annotations appropriately
- Keep controllers thin, business logic in services
- Use DTOs for API responses
- Implement proper exception handling

### TypeScript/Svelte (Frontend)
- Use TypeScript strict mode
- Follow Svelte best practices
- Use reactive declarations ($:) appropriately
- Keep components modular and reusable
- Use SCSS modules for styling

## Project Structure

### Backend (`/backend`)
- `controller/`: REST API endpoints
- `service/`: Business logic layer
- `repository/`: Data access layer
- `model/`: Entity classes
- `config/`: Configuration classes (JWT, Security, etc.)
- `util/`: Utility classes and DTOs

### Frontend (`/frontend`)
- `src/components/`: Reusable Svelte components
- `src/pages/`: Page components
- `src/lib/`: Service classes and API clients
- `src/styles/`: SCSS stylesheets
- `src/routes/`: SvelteKit routes (if applicable)

## API Conventions
- Use RESTful patterns
- JWT token in Authorization header: `Bearer <token>`
- Consistent error response format
- Use appropriate HTTP status codes

## Development Guidelines
- Write clean, maintainable code
- Add comments for complex logic
- Handle errors gracefully
- Validate user inputs
- Follow security best practices
- Keep dependencies up to date