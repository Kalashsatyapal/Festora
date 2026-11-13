import { createContext, useContext, useMemo, useState } from "react";
import api from "../api/client";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem("festora_user");
    return saved ? JSON.parse(saved) : null;
  });

  const login = async (credentials) => {
    const response = await api.post("/auth/login", credentials);
    const data = response.data.data;

    localStorage.setItem("festora_token", data.accessToken);

    const profile = {
      userId: data.userId,
      fullName: data.fullName,
      email: data.email,
      role: data.role
    };

    localStorage.setItem("festora_user", JSON.stringify(profile));
    setUser(profile);
    return profile;
  };

  const logout = () => {
    localStorage.removeItem("festora_token");
    localStorage.removeItem("festora_user");
    setUser(null);
  };

  const value = useMemo(
    () => ({ user, login, logout, authenticated: Boolean(user) }),
    [user]
  );

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}