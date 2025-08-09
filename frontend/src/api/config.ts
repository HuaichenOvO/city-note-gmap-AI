import axios from 'axios';

// API配置 - 根据当前域名判断环境
const isProduction = window.location.hostname === '44.243.89.138';
const API_BASE_URL = isProduction
  ? 'http://44.243.89.138/api'  // prod environment
  : 'http://localhost:8080/api';     // dev environment

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
      // handle unauthorized errors
      console.log('Token expired or invalid, redirecting to login');
      localStorage.removeItem('token');
      // check if currently locates in the login page, preventing infinitely redirecting
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api; 