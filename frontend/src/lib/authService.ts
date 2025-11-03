import api from "./api";

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

let authToken: string | null = null;
let currentUser: User | null = null;

export const authService = {
  init(): void {
    if (typeof window === "undefined") return;

    const token = sessionStorage.getItem("token");
    const userStr = sessionStorage.getItem("user");

    if (token && userStr) {
      try {
        authToken = token;
        currentUser = JSON.parse(userStr);
      } catch (error) {
        console.error("Failed to parse user from sessionStorage:", error);
        this.logout();
      }
    }
  },

  async login(data: LoginData): Promise<AuthResponse> {
    const response = await api.post("/auth/login", data);
    return response.data;
  },

  async register(data: RegisterData): Promise<AuthResponse> {
    const response = await api.post("/auth/register", data);
    return response.data;
  },

  setAuth(token: string, user: User): void {
    authToken = token;
    currentUser = user;

    if (typeof window !== "undefined") {
      sessionStorage.setItem("token", token);
      sessionStorage.setItem("user", JSON.stringify(user));
    }
  },

  logout(): void {
    authToken = null;
    currentUser = null;

    if (typeof window !== "undefined") {
      sessionStorage.removeItem("token");
      sessionStorage.removeItem("user");
    }
  },

  getToken(): string | null {
    return authToken;
  },

  getUser(): User | null {
    return currentUser;
  },

  isAuthenticated(): boolean {
    return !!authToken;
  }
};