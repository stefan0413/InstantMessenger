import { createContext, useContext, useEffect, useState } from "react";
import type { ReactNode } from "react";
import type { User, LoginCredentials, RegisterCredentials } from "../types";
import { loginRequest, registerRequest } from "../api/authApi";

type AuthContextType = {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (credentials: LoginCredentials) => Promise<void>;
  register: (credentials: RegisterCredentials) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextType | null>(null);

type AuthProviderProps = {
  children: ReactNode;
};

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
useEffect(() => {
  const savedUser = localStorage.getItem("user");
  const savedToken = localStorage.getItem("token");

  if (savedUser && savedToken) {
    setUser(JSON.parse(savedUser));
    setToken(savedToken);
  }
}, []);
  async function login(credentials: LoginCredentials) {
    const data = await loginRequest(credentials);

    setUser(data.user);
    setToken(data.token);
    localStorage.setItem("user", JSON.stringify(data.user));
localStorage.setItem("token", data.token);
  }

  async function register(credentials: RegisterCredentials) {
    const data = await registerRequest(credentials);

    setUser(data.user);
    setToken(data.token);
    localStorage.setItem("user", JSON.stringify(data.user));
    localStorage.setItem("token", data.token);
  }

  function logout() {
    setUser(null);
    setToken(null);
    localStorage.removeItem("user");
localStorage.removeItem("token");
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: Boolean(user && token),
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }

  return context;
}