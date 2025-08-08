import axios from 'axios';

// API配置 - 根据当前域名判断环境
const isProduction = window.location.hostname === '44.243.89.138';
const API_BASE_URL = isProduction 
  ? 'http://44.243.89.138/api'  // 生产环境
  : 'http://localhost:8080/api';     // 开发环境

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // 处理未授权错误
      console.log('Token expired or invalid, redirecting to login');
      localStorage.removeItem('token');
      // 检查当前是否已经在登录页面，避免无限重定向
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api; 