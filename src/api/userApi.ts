import api from './config';

export interface User {
  id?: number;
  username: string;
  email: string;
  password?: string;
}

export const userApi = {
  // 创建用户
  createUser: async (user: User) => {
    const response = await api.post<User>('/users', user);
    return response.data;
  },

  // 获取用户信息
  getUserById: async (id: number) => {
    const response = await api.get<User>(`/users/${id}`);
    return response.data;
  },

  // 获取所有用户
  getAllUsers: async () => {
    const response = await api.get<User[]>('/users');
    return response.data;
  },

  // 更新用户信息
  updateUser: async (id: number, user: User) => {
    const response = await api.put<User>(`/users/${id}`, user);
    return response.data;
  },

  // 删除用户
  deleteUser: async (id: number) => {
    await api.delete(`/users/${id}`);
  },
}; 