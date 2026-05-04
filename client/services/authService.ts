import api from './api';

export interface RegisterData {
  email: string;
  password: string;
}

export interface LoginData {
  email: string;
  password: string;
}

export interface UserInfo {
  id: number;
  email: string;
}

export interface AuthResponse {
  user: UserInfo;
  accessToken: string;
  refreshToken: string;
}

export const authService = {
  async register(email: string, password: string): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/api/auth/register', { email, password });
    return response.data;
  },

  async login(data: LoginData): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/api/auth/login', data);
    return response.data;
  },

  async logout(): Promise<void> {
    await api.post('/api/auth/logout');
  },

  async forgotPassword(email: string): Promise<{ token: string; message: string }> {
    const response = await api.post<{ token: string; message: string }>('/api/auth/forgot-password', { email });
    return response.data;
  },

  async resetPassword(token: string, password: string): Promise<void> {
    await api.post('/api/auth/reset-password', { token, password, confirmPassword: password });
  },

  async getCurrentUser(): Promise<UserInfo> {
    const response = await api.get<UserInfo>('/api/auth/me');
    return response.data;
  },

  async checkAuth(): Promise<UserInfo | null> {
    try {
      const response = await api.get<UserInfo>('/api/auth/me');
      return response.data;
    } catch {
      return null;
    }
  },
};
