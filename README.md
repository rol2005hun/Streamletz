# Streamletz 🎵

> **Your sound. Your stream. Your rules.**

A modern, full-stack music streaming application inspired by Spotify, built with cutting-edge technologies.

## 🎯 Overview

Streamletz is a music streaming platform that gives you complete control over your listening experience. Stream your favorite tracks, discover new music, and enjoy a seamless audio experience across all your devices.

## 🛠️ Tech Stack

### Frontend
- **Svelte** with TypeScript
- **SCSS Modules** for styling
- **Vite** as build tool
- Responsive, modern UI design

### Backend
- **Java 17+** with Spring Boot
- **PostgreSQL** database
- **JWT** authentication
- **Swagger/OpenAPI** documentation
- HTTP Range support for audio streaming

### DevOps
- **Docker** and Docker Compose
- **GitHub Actions** CI/CD
- **Railway/Render** deployment ready

## 🚀 Features

- 🔐 User authentication (registration & login)
- 🎵 Audio streaming with HTTP Range support
- ⏯️ Full-featured audio player (play/pause, seek, volume control)
- 🖼️ Album cover art and metadata display
- 📥 YouTube/Spotify download integration (placeholder)
- 📱 Fully responsive design
- 🎨 Modern, Spotify-inspired UI

## 📋 Prerequisites

- **Node.js** 18+ and npm
- **Java** 17+
- **Maven** 3.8+
- **Docker** and Docker Compose
- **PostgreSQL** 14+

## 🏃 Getting Started

### Local Development with Docker

1. Clone the repository:
```bash
git clone <repository-url>
cd Streamletz
```

2. Create environment file:
```bash
cp .env.example .env
# Edit .env with your configuration
```

3. Start all services:
```bash
docker-compose up -d
```

4. Access the application:
   - Frontend: http://localhost:5173
   - Backend API: http://localhost:8080
   - API Documentation: http://localhost:8080/swagger-ui.html

### Manual Setup

#### Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

#### Frontend

```bash
cd frontend
npm install
npm run dev
```

## 📁 Project Structure

```
Streamletz/
├── backend/               # Spring Boot backend
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/streamletz/
│   │       │       ├── controller/
│   │       │       ├── service/
│   │       │       ├── repository/
│   │       │       ├── model/
│   │       │       ├── config/
│   │       │       └── util/
│   │       └── resources/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/              # Svelte frontend
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── styles/
│   │   └── lib/
│   ├── Dockerfile
│   └── package.json
├── .github/
│   └── workflows/
│       └── ci-cd.yml
├── docker-compose.yml
├── .env.example
└── README.md
```

## 🔧 Configuration

Key environment variables (see `.env.example`):

- `DATABASE_URL` - PostgreSQL connection string
- `JWT_SECRET` - Secret key for JWT tokens
- `API_BASE_URL` - Backend API URL
- `PORT` - Application port

## 🚢 Deployment

The project includes GitHub Actions workflows for automated deployment to Railway or Render.

1. Set up secrets in your GitHub repository
2. Push to main branch
3. GitHub Actions will automatically build and deploy

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License.

## 💬 Motto

**Your sound. Your stream. Your rules.**

---

Made with ❤️ using Svelte and Spring Boot
