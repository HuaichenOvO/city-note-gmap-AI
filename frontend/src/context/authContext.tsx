import React, { createContext, useContext, useState, ReactNode, useEffect } from 'react';
import { authApi } from '../api/authApi';
import { User } from '../api/userApi';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  // Initialize auth state
  useEffect(() => {
    const initializeAuth = async () => {
      const token = localStorage.getItem('token');
      if (token) {
        try {
          const userData = await authApi.getCurrentUser();
          setUser(userData);
        } catch (error) {
          console.log('Token validation failed:', error);
          // clear invalid tokens
          localStorage.removeItem('token');
          setUser(null);
        }
      } else {
        setUser(null);
      }
      setLoading(false);
    };

    initializeAuth();
  }, []);

  const login = async (username: string, password: string) => {
    try {
      const response = await authApi.login({ username, password });
      setUser(response.user);
    } catch (error) {
      throw error;
    }
  };

  const logout = () => {
    try {
      authApi.logout();
      setUser(null);
      // clear corresponding local cache
      localStorage.removeItem('token');
      // redirect to the login page
      window.location.href = '/login';
    } catch (error) {
      console.error('Logout error:', error);
      // plan B: clear the cache and reload
      localStorage.clear();
      window.location.href = '/login';
    }
  };

  const value: AuthContextType = {
    user,
    isAuthenticated: !!user,
    login,
    logout,
    loading
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}; 