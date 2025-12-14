
import axios from 'axios';
import { showToast } from './toast';

const VITE_API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

function normalizeApiBaseUrl(url: string): string {
  const trimmed = url.replace(/\/+$/, "");
  // This app's backend is served under the /api prefix.
  return trimmed.endsWith("/api") ? trimmed : `${trimmed}/api`;
}

function isValidApiUrl(url: any): url is string {
  return typeof url === 'string' && url.startsWith('http');
}

if (!isValidApiUrl(VITE_API_BASE_URL)) {
  console.error('[API] VITE_API_BASE_URL is invalid: ', VITE_API_BASE_URL);
  throw new Error('VITE_API_BASE_URL is not set or invalid. Please check your .env file.');
}

export const API_BASE_URL = normalizeApiBaseUrl(VITE_API_BASE_URL);

let serverToken: string | null = null;

export function setServerToken(token: string | null) {
  serverToken = token;
}

export function clearServerToken() {
  serverToken = null;
}

const api = axios.create({
  baseURL: isValidApiUrl(API_BASE_URL) ? API_BASE_URL : undefined,
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
    const tokenMatch = document.cookie.match(/(?:^|;\s*)streamletz-token=([^;]+)/);
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
    if (typeof window !== 'undefined') {
      if (error.response?.status === 401) {
        const isAuthRoute =
          error.config?.url?.includes('/auth/login') ||
          error.config?.url?.includes('/auth/register');
        if (!isAuthRoute) {
          document.cookie = 'streamletz-token=; path=/; max-age=0';
          document.cookie = 'user=; path=/; max-age=0';
          window.location.href = '/login';
        }
      }

      let message = '';
      if (error.response?.data?.message) {
        message = error.response.data.message;
      } else if (error.response?.data) {
        if (typeof error.response.data === 'string') {
          message = error.response.data;
        } else {
          message = JSON.stringify(error.response.data);
        }
      } else if (error.message) {
        message = error.message;
      } else {
        message = JSON.stringify(error);
      }

      showToast(message, 'error');
    }
    return Promise.reject(error);
  }
);

export default api;