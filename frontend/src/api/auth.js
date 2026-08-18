import axios from 'axios';
import api, { API_BASE_URL } from './axiosConfig';

// These functions receive the input and make a post request

export const login = (credentials) => api.post('/auth/login', credentials);

export const register = (userInput) => api.post('/auth/register', userInput);

// Uses a plain axios call (not the shared `api` instance) so a 401 here
// doesn't re-enter api's response interceptor and deadlock against itself.
export const refreshToken = () => {
  const token = localStorage.getItem('token');
  return axios.post(`${API_BASE_URL}/auth/refresh`, null, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
};