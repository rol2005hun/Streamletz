# Streamletz Frontend

Frontend application for Streamletz music streaming platform.

## Tech Stack

- Svelte 4
- TypeScript
- SCSS Modules
- Vite
- Axios

## Getting Started

### Prerequisites

- Node.js 18+
- npm

### Setup

1. Install dependencies:
```bash
npm install
```

2. Configure environment (create `.env` file):
```
VITE_API_BASE_URL=http://localhost:8080/api
```

3. Run development server:
```bash
npm run dev
```

The app will be available at `http://localhost:5173`

### Build for Production

```bash
npm run build
```

## Docker

Build and run with Docker:
```bash
docker build -t streamletz-frontend .
docker run -p 80:80 streamletz-frontend
```

## Project Structure

```
src/
├── components/     # Reusable Svelte components
├── pages/          # Page components
├── lib/            # Services and utilities
├── styles/         # Global styles and variables
└── main.ts         # Application entry point
```

## Features

- 🔐 User authentication
- 🎵 Music streaming with audio player
- 🔍 Track search
- 📱 Responsive design
- 🎨 Modern, Spotify-inspired UI

## Motto

**Your sound. Your stream. Your rules.**
