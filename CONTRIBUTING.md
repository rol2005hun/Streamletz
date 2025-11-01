# Contributing to Streamletz 🎵

Thank you for your interest in contributing to Streamletz! We appreciate all contributions, whether it's bug fixes, new features, documentation improvements, or suggestions.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Code Style Guidelines](#code-style-guidelines)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Testing](#testing)
- [Documentation](#documentation)

## 🤝 Code of Conduct

- Be respectful and inclusive
- Welcome newcomers and help them learn
- Focus on constructive feedback
- Keep discussions on-topic
- Report unacceptable behavior to project maintainers

## 🚀 Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/Streamletz.git
   cd Streamletz
   ```
3. **Add upstream remote**:
   ```bash
   git remote add upstream https://github.com/rol2005hun/Streamletz.git
   ```
4. **Create a new branch** for your feature or fix:
   ```bash
   git checkout -b feature/your-feature-name
   ```

## 🛠️ Development Setup

### Prerequisites
- Node.js 18+
- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Docker (optional, recommended)

### Setup Steps

1. **Install dependencies**:
   ```bash
   # Backend
   cd backend
   mvn clean install
   
   # Frontend
   cd ../frontend
   npm install
   ```

2. **Setup database**:
   ```sql
   CREATE DATABASE streamletz;
   CREATE USER streamletz_user WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE streamletz TO streamletz_user;
   ```

3. **Configure environment**:
   ```bash
   cp .env.example .env
   # Edit .env with your local settings
   ```

4. **Run the application**:
   ```bash
   # Backend (Terminal 1)
   cd backend
   mvn spring-boot:run
   
   # Frontend (Terminal 2)
   cd frontend
   npm run dev
   ```

## 📝 Code Style Guidelines

### Backend (Java/Spring Boot)

- **Naming Conventions**:
  - Classes: `PascalCase` (e.g., `TrackService`)
  - Methods: `camelCase` (e.g., `getTrackById`)
  - Constants: `UPPER_SNAKE_CASE` (e.g., `MAX_FILE_SIZE`)
  
- **Code Organization**:
  - Use Lombok annotations (`@Data`, `@RequiredArgsConstructor`, etc.)
  - Keep controllers thin, logic in services
  - Use DTOs for request/response objects
  - Add `@Operation` annotations for Swagger docs

- **Documentation**:
  ```java
  /**
   * Retrieves a track by its ID.
   * 
   * @param id the track ID
   * @return the track entity
   * @throws RuntimeException if track not found
   */
  public Track getTrackById(Long id) {
      // implementation
  }
  ```

### Frontend (Svelte/TypeScript)

- **Code Style** (from `.github/copilot-instructions.md`):
  - Use "double quotes" for strings in SCSS, JS, and TS
  - Use "double quotes" for HTML attributes
  - Avoid unnecessary comments
  - Follow Svelte 5 runes syntax (`$state`, `$props`, `$effect`, `$bindable`)
  - Use TypeScript for type safety

- **Component Structure**:
  ```svelte
  <script lang="ts">
    // Imports
    import { onMount } from "svelte";
    
    // Props
    let { prop1, prop2 = $bindable() } = $props();
    
    // State
    let state = $state(initialValue);
    
    // Effects
    $effect(() => {
      // side effects
    });
    
    // Functions
    function handleAction() {
      // logic
    }
  </script>
  
  <!-- Template -->
  <div class="component">
    <!-- content -->
  </div>
  
  <!-- Styles -->
  <style lang="scss">
    @use "../styles/variables.scss" as *;
    
    .component {
      // styles
    }
  </style>
  ```

- **File Organization**:
  - Components: `src/components/ComponentName.svelte`
  - Pages: `src/pages/PageName.svelte`
  - Services: `src/lib/serviceName.ts`
  - Styles: `src/styles/components/ComponentName.scss`

## 💬 Commit Guidelines

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification.

### Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, semicolons, etc.)
- `refactor`: Code refactoring without feature changes
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `chore`: Maintenance tasks, dependencies
- `ci`: CI/CD pipeline changes

### Examples
```bash
feat(player): add volume control and mute button

feat(api): implement play count tracking at 90% threshold

fix(auth): resolve JWT token expiration issue

docs(readme): update installation instructions

style(dashboard): improve hover effects on track cards
```

## 🔄 Pull Request Process

1. **Update your branch** with latest upstream changes:
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Run tests** and ensure everything works:
   ```bash
   # Backend
   cd backend
   mvn test
   
   # Frontend
   cd frontend
   npm test
   ```

3. **Lint your code**:
   ```bash
   # Frontend
   npm run lint
   ```

4. **Push your changes**:
   ```bash
   git push origin feature/your-feature-name
   ```

5. **Create Pull Request**:
   - Go to GitHub and create a PR from your fork
   - Use a clear, descriptive title
   - Fill out the PR template
   - Link related issues
   - Add screenshots for UI changes

6. **PR Requirements**:
   - ✅ All tests pass
   - ✅ Code follows style guidelines
   - ✅ Documentation is updated
   - ✅ Commits follow conventional format
   - ✅ No merge conflicts
   - ✅ Reviewed and approved by maintainer

## 🧪 Testing

### Backend Testing
```bash
cd backend
mvn test                    # Run all tests
mvn test -Dtest=ClassName   # Run specific test
```

### Frontend Testing
```bash
cd frontend
npm test                    # Run all tests
npm run test:watch          # Run tests in watch mode
```

### Manual Testing Checklist
- [ ] Feature works as expected
- [ ] No console errors
- [ ] Responsive on mobile/tablet/desktop
- [ ] Authentication still works
- [ ] Existing features not broken

## 📚 Documentation

When contributing, please update relevant documentation:

- **README.md**: For new features or setup changes
- **API Docs**: Add Swagger annotations for new endpoints
- **Code Comments**: For complex logic
- **CONTRIBUTING.md**: For process changes

## 🎯 What to Contribute?

### Good First Issues
- UI/UX improvements
- Documentation fixes
- Code formatting
- Adding tests
- Bug fixes

### Feature Ideas
- Playlist management
- User profiles
- Social features (sharing, following)
- Advanced search filters
- Audio equalizer
- Lyrics display
- Download functionality
- Mobile app

### Areas Needing Help
- [ ] Unit test coverage
- [ ] E2E testing
- [ ] Performance optimization
- [ ] Accessibility improvements
- [ ] Internationalization (i18n)

## 💡 Questions?

If you have questions or need help:
- Open a [GitHub Discussion](https://github.com/rol2005hun/Streamletz/discussions)
- Comment on the relevant issue
- Reach out to maintainers

## 🎵 Project Motto

**Your sound. Your stream. Your rules.**

---

Thank you for contributing to Streamletz! Together we're building something awesome! 🎉