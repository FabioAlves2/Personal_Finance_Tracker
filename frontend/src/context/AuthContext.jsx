//Creating a context to store the user info so it can be shared to 
// all the tree below it


import { createContext, useContext, useState, useCallback } from 'react';
import { login as apiLogin } from '../api/auth';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(() => localStorage.getItem('token'));
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });

  const login = useCallback(async (credentials) => {
    const response = await apiLogin(credentials);
    const { accessToken, userId, userName } = response.data;

    localStorage.setItem('token', accessToken);
    const userData = { id: userId, name: userName };
    localStorage.setItem('user', JSON.stringify(userData));

    setToken(accessToken);
    setUser(userData);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  }, []);

  const updateUser = useCallback((newUserData) => {
    setUser(newUserData);
    localStorage.setItem('user', JSON.stringify(newUserData));
  }, []);

  return (
    <AuthContext.Provider value={{ token, user, login, logout, updateUser}}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);