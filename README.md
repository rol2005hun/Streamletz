# Streamletz 🎵

> **Your sound. Your stream. Your rules.**

A modern, full-stack music streaming application inspired by Spotify, built with cutting-edge technologies.

![Streamletz Banner](https://img.shields.io/badge/Streamletz-Music%20Streaming-1DB954?style=for-the-badge&logo=spotify&logoColor=white)
![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot)
![Svelte](https://img.shields.io/badge/Svelte-5-FF3E00?style=for-the-badge&logo=svelte&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-336791?style=for-the-badge&logo=postgresql&logoColor=white)

## 🎯 Overview

Streamletz is a **self-hosted** music streaming platform that gives you complete control over your listening experience. Stream your favorite tracks, discover new music, and enjoy a seamless audio experience with a modern, intuitive interface.

### 🏠 Self-Hosted Solution

**Important:** This application is designed for **personal, private use only**. It allows you to stream your own music collection that you legally own. 

- ✅ Stream your personal music library
- ✅ Full control over your data and privacy
- ✅ No subscription fees or cloud dependencies
- ✅ Host it on your own server or local network
- ❌ **NOT** for public streaming services
- ❌ **NOT** for distributing copyrighted content

**Legal Notice:** Users are responsible for ensuring they have the legal rights to stream any music files they add to their library. This software is provided for personal use only and should not be used to infringe on copyright laws.

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

## ✨ Features

### 🔐 Authentication & Security
- JWT-based user authentication
- Secure registration and login
- Token-based session management

### 🎵 Music Streaming
- HTTP Range support for seamless streaming
- Smart play count tracking (90% threshold)
- High-quality audio playback
- Track metadata management

### 🎮 Audio Player
- Play, pause, skip controls
- Real-time progress tracking
- Volume control with mute
- Seek functionality
- Buffer visualization
- Persistent playback state

### 🎨 User Interface
- Modern, Spotify-inspired design
- Hover-activated play buttons
- Responsive grid layout
- Real-time search
- Album cover art display
- Smooth animations and transitions
- Mobile-friendly design

### 🛠️ Developer Features
- RESTful API with Swagger documentation
- Docker support for easy deployment
- CI/CD pipeline with GitHub Actions
- Clean architecture (MVC pattern)
- Type-safe frontend with TypeScript

## 📋 Prerequisites

- **Node.js** 18+ and npm
- **Java** 17+ (or 21+ recommended)
- **Maven** 3.8+
- **Docker** and Docker Compose
- **PostgreSQL** 14+

## 🏃 Getting Started

### Quick Start with Docker (Recommended)

1. **Clone the repository:**
```bash
git clone https://github.com/rol2005hun/Streamletz.git
cd Streamletz
```

2. **Create environment file:**
```bash
cp .env.example .env
# Edit .env with your configuration (database credentials, JWT secret, music/cover paths, etc.)
```

**Mandatory:** The `.env` file in the root is required for both backend and frontend to work. See `.env.example` for all required variables. You must set at least:

- `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_NAME` (PostgreSQL connection)
- `JWT_SECRET` (backend auth)
- `MUSIC_PATH`, `COVER_PATH` (absolute or relative paths to your music and cover folders)
- `VITE_API_BASE_URL` (frontend API URL, usually `http://localhost:1124/api`)

3. **Create music directory (if not using external path):**
```bash
mkdir -p backend/music
```
Or set `MUSIC_PATH` in `.env` to any folder you want to use. The backend will scan this folder recursively (up to 3 levels deep) for audio files.

4. **Start all services:**
```bash
docker-compose up -d
```

5. **Access the application:**
   - 🎵 Frontend: http://localhost:5173
   - 🔧 Backend API: http://localhost:1124
   - 📚 API Docs: http://localhost:1124/swagger-ui.html
   - 🗄️ Database: localhost:5432

### Manual Setup (Development)

#### 1. Database Setup
```bash
# Create PostgreSQL database
psql -U postgres
CREATE DATABASE streamletz;
CREATE USER streamletz_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE streamletz TO streamletz_user;
\q
```

#### 2. Backend
```bash
cd backend

# Install dependencies
mvn clean install

# Run the application
mvn spring-boot:run

# Backend will be available at http://localhost:1124
```

#### 3. Frontend
```bash
cd frontend

# Install dependencies
npm install

# Run development server
npm run dev

# Frontend will be available at http://localhost:5173
```


#### 4. Music and Covers Folders Configuration

Set the following in your `.env` (see `.env.example`):

```env
MUSIC_PATH=./backend/music         # or any absolute path
COVER_PATH=./backend/covers        # or any absolute path
```

You can use any folder path (absolute or relative). The backend will scan all music files (recursively, up to 3 levels deep) in the folder you specify. You do **not** need to copy files manually to a fixed directory—just set the correct path in your `.env`.

Album covers will be generated and stored in the covers folder you specify. Existing covers are detected automatically; no manual intervention is needed.

## 📁 Folder Structure

```
Streamletz/
├── backend/               # Java Spring Boot backend
│   ├── src/main/java/com/streamletz/
│   │   ├── controller/    # REST API endpoints
│   │   ├── service/       # Business logic
│   │   ├── repository/    # Data access
│   │   ├── model/         # Entity classes
│   │   ├── config/        # Config (JWT, Security, etc.)
│   │   └── util/          # DTOs, helpers
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── music/             # (Optional) Local music files
│   ├── Dockerfile
│   └── pom.xml
├── frontend/              # Svelte + TypeScript frontend
│   ├── src/
│   │   ├── lib/           # API clients, stores, services
│   │   ├── components/    # Svelte components
│   │   ├── styles/        # SCSS modules
│   │   └── routes/        # SvelteKit routes
│   ├── Dockerfile
│   └── package.json
├── .github/
│   └── workflows/
│       └── ci-cd.yml
├── docker-compose.yml
├── .env.example           # Example env file (copy to .env)
└── README.md
```

## 🔧 Configuration

### Environment Variables

Create a `.env` file in the root directory (see `.env.example` for all required and optional variables):

```env
# Database
DATABASE_URL=postgresql://localhost:5432/streamletz
DB_USERNAME=streamletz_user
DB_PASSWORD=changeme123
DB_NAME=streamletz

# Backend
BACKEND_PORT=1124
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production
JWT_EXPIRATION=86400000
MUSIC_PATH=./backend/music
COVER_PATH=./backend/covers

# Frontend
VITE_API_BASE_URL=http://localhost:1124/api
FRONTEND_PORT=5173
```

### Backend Configuration (`application.properties`)

Key settings (can be set via `.env`):
- `spring.datasource.*` - Database connection (from `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD`)
- `jwt.secret` - JWT signing key (`JWT_SECRET`)
- `jwt.expiration` - Token validity (`JWT_EXPIRATION`, default: 86400000ms = 24h)
- `music.storage.path` - Music files location (`MUSIC_PATH`)
- `music.covers.path` - Album covers location (`COVER_PATH`)
- `server.port` - Backend port (`BACKEND_PORT`, default: 1124)

## 🚢 Deployment

The project is containerized and ready for **self-hosted deployment** on your own server or local network. All configuration is handled via the `.env` file in the root.

### 🏠 Self-Hosting Options

**Local Network (Recommended for Personal Use)**
- Run on a home server or NAS
- Access within your local network only
- Most secure and private option

**Private VPS/Cloud Server**
- Deploy to your own VPS (DigitalOcean, Linode, etc.)
- Keep it password-protected and private
- Use VPN for remote access

**⚠️ Important Security Notes:**
- **Do NOT expose this publicly** without proper authentication and legal compliance
- Use strong passwords and enable HTTPS in production
- Consider using a VPN for remote access instead of public exposure
- This is intended for personal use, not as a public streaming service

### Docker Deployment

```bash
# Build images
docker-compose build

# Deploy
docker-compose up -d
```

### Platform-Specific Deployment

**For Personal/Private Use Only:**

- **Home Server**: Best option for complete privacy and control
- **Private VPN**: Deploy and access through WireGuard/OpenVPN
- **Local Docker**: Run on your personal computer or NAS
- **Private Cloud**: VPS with firewall rules (block public access)

**NOT Recommended for Public Deployment:**
- ❌ Public hosting without authentication
- ❌ Sharing with unauthorized users
- ❌ Commercial use or public streaming service

## 🧪 API Documentation

Once the backend is running, visit the Swagger UI for interactive API documentation:

**http://localhost:1124/swagger-ui.html**

### Main Endpoints

#### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and receive JWT token

#### Tracks
- `GET /api/tracks` - Get all tracks
- `GET /api/tracks/{id}` - Get track by ID
- `GET /api/tracks/search?query={q}` - Search tracks
- `GET /api/tracks/stream/{id}` - Stream audio file
- `POST /api/tracks/{id}/play` - Increment play count

## 🤝 Contributing

Contributions are welcome! Please check out our [Contributing Guidelines](CONTRIBUTING.md) for details.

**Commit convention:** Use [Conventional Commits](https://www.conventionalcommits.org/) (e.g. `feat: add playlist sharing`, `fix: correct play count logic`).

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'feat: add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](https://github.com/rol2005hun/Streamletz/tree/master?tab=GPL-3.0-1-ov-file) file for details.

### ⚖️ Copyright and Legal Compliance

**This software is for personal, self-hosted use only.**

- You must own or have legal rights to all music files you add to your library
- Do not use this software to distribute copyrighted content
- Do not make this publicly accessible for unauthorized users
- Respect artists' rights and support them through legal means

**The developers of Streamletz are not responsible for any copyright infringement or illegal use of this software.**

## 👤 Author

**rol2005hun**
- GitHub: [@rol2005hun](https://github.com/rol2005hun)

## 🙏 Acknowledgments

- Spotify for UI/UX inspiration
- The Spring Boot and Svelte communities
- All contributors and supporters

---

<p align="center">
  <strong>Your sound. Your stream. Your rules.</strong> 🎵
</p>

## 💬 Motto

**Your sound. Your stream. Your rules.**

---

Made with ❤️ using Svelte and Spring Boot