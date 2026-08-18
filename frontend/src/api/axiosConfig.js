import axios from 'axios';
import { refreshToken } from './auth';

// Relative by default: nginx proxies /api to the backend in production, and the
// CRA dev server's "proxy" field (package.json) does the same in local dev.
export const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || '/api';

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) prom.reject(error);
    else prom.resolve(token);
  });
  failedQueue = [];
};

const api = axios.create({
  baseURL: API_BASE_URL,
});

// Interceptor de request: adiciona o token no header
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor de response: trata erros 401
api.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;

    // Se não for 401 ou já foi retentado, rejeita
    if (error.response?.status !== 401 || originalRequest._retry) {
      return Promise.reject(error);
    }

    // Se já estamos a tentar refresh, coloca a requisição na fila
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      })
        .then(token => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return api(originalRequest);
        })
        .catch(err => Promise.reject(err));
    }

    originalRequest._retry = true;
    isRefreshing = true;

    try {
      const { data } = await refreshToken();
      const newToken = data.accessToken; // campo retornado pelo backend
      localStorage.setItem('token', newToken);
      api.defaults.headers.common.Authorization = `Bearer ${newToken}`;
      originalRequest.headers.Authorization = `Bearer ${newToken}`;
      processQueue(null, newToken);
      return api(originalRequest);
    } catch (refreshError) {
      processQueue(refreshError, null);
      // Se o refresh falhar, faz logout e redireciona para o login
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  }
);

export default api;