
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
      role: data.role,
      avatarUrl: null
    };

    localStorage.setItem("festora_user", JSON.stringify(profile));
    setUser(profile);

    return profile;
  };

  const updateUser = (updates) => {
    setUser((current) => {
      if (!current) return current;

      const updated = { ...current, ...updates };
      localStorage.setItem("festora_user", JSON.stringify(updated));
      return updated;
    });
  };

  const logout = async () => {
    const refreshToken = localStorage.getItem("festora_refresh_token");

    localStorage.removeItem("festora_token");
    localStorage.removeItem("festora_refresh_token");
    localStorage.removeItem("festora_user");
    setUser(null);

    if (refreshToken) {
      try {
        await api.post("/auth/logout", { refreshToken });
      } catch {
      }
    }
  };

  const value = useMemo(
    () => ({
      user,
      login,
      logout,
      updateUser,
      authenticated: Boolean(user)
    }),
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
