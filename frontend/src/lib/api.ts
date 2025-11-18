
import axios from 'axios';

const VITE_API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

function isValidApiUrl(url: any): url is string {
  return typeof url === 'string' && url.startsWith('http');
}

if (!isValidApiUrl(VITE_API_BASE_URL)) {
  console.error('[API] VITE_API_BASE_URL is invalid: ', VITE_API_BASE_URL);
  throw new Error('VITE_API_BASE_URL is not set or invalid. Please check your .env file.');
}

let serverToken: string | null = null;

export function setServerToken(token: string | null) {
  serverToken = token;
}

export function clearServerToken() {
  serverToken = null;
}

const api = axios.create({
  baseURL: isValidApiUrl(VITE_API_BASE_URL) ? VITE_API_BASE_URL : undefined,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true
});

api.interceptors.request.use((config) => {
  config.headers = config.headers || {};

  if (typeof window === 'undefined') {
    if (serverToken) {
      (config.headers as any)['Authorization'] = `Bearer ${serverToken}`;
    }
  } else {
    const tokenMatch = document.cookie.match(/(?:^|;\s*)token=([^;]+)/);
    const token = tokenMatch?.[1];
    if (token) {
      (config.headers as any)['Authorization'] = `Bearer ${token}`;
    }
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (typeof window !== 'undefined' && error.response?.status === 401) {
      const isAuthRoute =
        error.config?.url?.includes('/auth/login') ||
        error.config?.url?.includes('/auth/register');
      if (!isAuthRoute) {
        document.cookie = 'token=; path=/; max-age=0';
        document.cookie = 'user=; path=/; max-age=0';
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;