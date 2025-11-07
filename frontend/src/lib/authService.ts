import api from './api';

export interface User {
  username: string;
  email: string;
  profileImage?: string | null;
}

export interface AuthResponse {
  token: string;
  username: string;
  email: string;
  profileImage?: string | null;
}

export interface LoginData {
  username: string;
  password: string;
}

export interface RegisterData {
  username: string;
  email: string;
  password: string;
}

export const authService = {
  async login(data: LoginData): Promise<AuthResponse> {
    const response = await api.post('/auth/login', data);
    return response.data;
  },

  async register(data: RegisterData): Promise<AuthResponse> {
    const response = await api.post('/auth/register', data);
    return response.data;
  },

  setAuth(token: string, user: User): void {
    if (typeof document !== 'undefined') {
      document.cookie = `token=${token}; path=/; max-age=86400; samesite=lax`;
      document.cookie = `user=${encodeURIComponent(JSON.stringify(user))}; path=/; max-age=86400; samesite=lax`;
    }
  },

  getToken(): string | null {
    if (typeof document === 'undefined') return null;

    const cookie = document.cookie
      .split('; ')
      .find((row) => row.startsWith('token='));

    if (!cookie) return null;

    return cookie.split('=')[1];
  },

  getUser(): User | null {
    if (typeof document === 'undefined') return null;

    const cookie = document.cookie
      .split('; ')
      .find((row) => row.startsWith('user='));

    if (!cookie) return null;

    try {
      const encoded = cookie.split('=')[1];
      return JSON.parse(decodeURIComponent(encoded)) as User;
    } catch {
      return null;
    }
  },

  logout(): void {
    if (typeof document !== 'undefined') {
      document.cookie = 'token=; path=/; max-age=0';
      document.cookie = 'user=; path=/; max-age=0';
    }
  }
};