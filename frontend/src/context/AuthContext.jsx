
import { createContext, useContext, useEffect, useMemo, useState } from "react";
import api from "../api/client";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem("festora_user");

    try {
      return saved ? JSON.parse(saved) : null;
    } catch {
      localStorage.removeItem("festora_user");
      return null;
    }
  });

  useEffect(() => {
    const handleSessionExpired = () => {
      setUser(null);
    };

    window.addEventListener("festora:session-expired", handleSessionExpired);

    return () => {
      window.removeEventListener("festora:session-expired", handleSessionExpired);
    };
  }, []);

  const login = async (credentials) => {
    const response = await api.post("/auth/login", credentials);
    const data = response.data.data;

    if (!data?.accessToken || !data?.refreshToken) {
      throw new Error("Invalid login response");
    }

    localStorage.setItem("festora_token", data.accessToken);
    localStorage.setItem("festora_refresh_token", data.refreshToken);

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
    localStorage.removeItem("festora_refresh_token");
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
