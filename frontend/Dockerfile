# Build stage
FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
ARG VITE_API_BASE_URL
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN npm run build

# Production stage
FROM node:20-alpine

WORKDIR /app

COPY --from=build /app ./

RUN npm install --omit=dev

EXPOSE 3000

CMD ["node", "build"]