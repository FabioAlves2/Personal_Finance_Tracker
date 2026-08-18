import { useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { refreshToken } from '../api/auth';

export default function TokenRefresher() {
  const { token, logout } = useAuth();

  useEffect(() => {
    if (!token) return;

    const interval = setInterval(async () => {
      try {
        const { data } = await refreshToken();
        localStorage.setItem('token', data.accessToken);
      } catch (err) {
        console.error('Refresh periódico falhou', err);
        logout();
      }
    }, 30 * 1000);

    return () => clearInterval(interval);
  }, [token, logout]);

  return null;
}