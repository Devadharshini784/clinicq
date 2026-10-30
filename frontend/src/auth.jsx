import { createContext, useContext, useState } from 'react';
import { clearAuth, getAuth, saveAuth } from './api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => getAuth());

  function login(authData) {
    saveAuth(authData);
    setAuth(authData);
  }

  function logout() {
    clearAuth();
    setAuth(null);
  }

  return (
    <AuthContext.Provider value={{ auth, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}