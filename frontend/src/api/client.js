
import axios from "axios";

const api = axios.create({
  baseURL: "/api",
  headers: {
    "Content-Type": "application/json"
  }
});

let refreshPromise = null;

const clearSession = () => {
  localStorage.removeItem("festora_token");
  localStorage.removeItem("festora_refresh_token");
  localStorage.removeItem("festora_user");
  window.dispatchEvent(new Event("festora:session-expired"));
};

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("festora_token");

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const status = error.response?.status;
    const requestUrl = originalRequest?.url || "";

    const excludedEndpoints = [
      "/auth/login",
      "/auth/register",
      "/auth/register/organizer",
      "/auth/refresh",
      "/auth/logout",
      "/auth/forgot-password",
      "/auth/reset-password"
    ];

    if (
      status !== 401 ||
      !originalRequest ||
      originalRequest._retry ||
      excludedEndpoints.some((endpoint) => requestUrl.includes(endpoint))
    ) {
      return Promise.reject(error);
    }

    const refreshToken = localStorage.getItem("festora_refresh_token");

    if (!refreshToken) {
      clearSession();
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      if (!refreshPromise) {
        refreshPromise = api
          .post("/auth/refresh", { refreshToken })
          .then((response) => {
            const data = response.data.data;

            if (!data?.accessToken || !data?.refreshToken) {
              throw new Error("Invalid token refresh response");
            }

            localStorage.setItem("festora_token", data.accessToken);
            localStorage.setItem("festora_refresh_token", data.refreshToken);

            return data.accessToken;
          })
          .catch((refreshError) => {
            clearSession();
            throw refreshError;
          })
          .finally(() => {
            refreshPromise = null;
          });
      }

      const newAccessToken = await refreshPromise;

      originalRequest.headers = originalRequest.headers || {};
      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

      return api(originalRequest);
    } catch (refreshError) {
      return Promise.reject(refreshError);
    }
  }
);

export default api;
