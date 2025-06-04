import api from './config';
import { User } from './userApi';

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface RegisterData extends User {
  confirmPassword: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export const authApi = {
  // 用户登录
  login: async (credentials: LoginCredentials) => {
    const response = await api.post<AuthResponse>('/auth/login', credentials);
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
    }
    return response.data;
  },

  // 用户注册
  register: async (userData: RegisterData) => {
    const response = await api.post<AuthResponse>('/auth/register', userData);
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
    }
    return response.data;
  },

  // 用户登出
  logout: () => {
    localStorage.removeItem('token');
  },

  // 获取当前用户信息
  getCurrentUser: async () => {
    const response = await api.get<User>('/auth/me');
    return response.data;
  },
}; 