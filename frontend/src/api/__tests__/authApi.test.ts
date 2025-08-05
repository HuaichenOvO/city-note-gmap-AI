import { vi, describe, test, expect, beforeEach } from 'vitest';

// Mock axios before importing authApi
vi.mock('axios', () => {
  const mockPost = vi.fn();
  const mockGet = vi.fn();
  const mockAxiosInstance = {
    post: mockPost,
    get: mockGet,
    interceptors: {
      request: {
        use: vi.fn(),
      },
      response: {
        use: vi.fn(),
      },
    },
  };

  return {
    default: {
      create: vi.fn(() => mockAxiosInstance),
    },
  };
});

import { authApi } from '../authApi';
import axios from 'axios';

const mockAxios = axios as any;

describe('authApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('login', () => {
    test('successful login', async () => {
      const mockResponse = {
        data: {
          token: 'mock-jwt-token',
          user: {
            id: 1,
            username: 'testuser',
            email: 'test@example.com',
          },
        },
      };

      mockAxios.create().post.mockResolvedValueOnce(mockResponse);

      const result = await authApi.login({ username: 'testuser', password: 'password123' });

      expect(mockAxios.create().post).toHaveBeenCalledWith('/auth/login', {
        username: 'testuser',
        password: 'password123',
      });
      expect(result).toEqual(mockResponse.data);
    });

    test('login failure', async () => {
      const mockError = new Error('Invalid credentials');
      mockAxios.create().post.mockRejectedValueOnce(mockError);

      await expect(authApi.login({ username: 'wronguser', password: 'wrongpass' })).rejects.toThrow('Invalid credentials');
    });
  });

  describe('register', () => {
    test('successful registration', async () => {
      const mockResponse = {
        data: {
          message: 'User registered successfully',
        },
      };

      const registerData = {
        username: 'newuser',
        email: 'new@example.com',
        password: 'password123',
        confirmPassword: 'password123',
      };

      mockAxios.create().post.mockResolvedValueOnce(mockResponse);

      const result = await authApi.register(registerData);

      expect(mockAxios.create().post).toHaveBeenCalledWith('/auth/register', registerData);
      expect(result).toEqual(mockResponse.data);
    });

    test('registration failure', async () => {
      const mockError = new Error('Username already exists');
      mockAxios.create().post.mockRejectedValueOnce(mockError);

      const registerData = {
        username: 'existinguser',
        email: 'existing@example.com',
        password: 'password123',
        confirmPassword: 'password123',
      };

      await expect(authApi.register(registerData)).rejects.toThrow('Username already exists');
    });
  });

  describe('getCurrentUser', () => {
    test('successful user fetch', async () => {
      const mockResponse = {
        data: {
          id: 1,
          username: 'testuser',
          email: 'test@example.com',
          firstName: 'John',
          lastName: 'Doe',
        },
      };

      mockAxios.create().get.mockResolvedValueOnce(mockResponse);

      const result = await authApi.getCurrentUser();

      expect(mockAxios.create().get).toHaveBeenCalledWith('/auth/me');
      expect(result).toEqual(mockResponse.data);
    });

    test('user fetch failure', async () => {
      const mockError = new Error('Unauthorized');
      mockAxios.create().get.mockRejectedValueOnce(mockError);

      await expect(authApi.getCurrentUser()).rejects.toThrow('Unauthorized');
    });
  });
}); 