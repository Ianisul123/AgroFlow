import { useState } from "react";
import { AuthContext } from "./AuthContext";
import {
  clearAuthSession,
  getStoredAuthToken,
  getStoredAuthUser,
  login as loginRequest,
  logout as logoutRequest,
  register as registerRequest,
  storeAuthSession,
} from "../services/AuthService";

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => getStoredAuthToken());
  const [user, setUser] = useState(() => getStoredAuthUser());

  async function login(email, password) {
    const response = await loginRequest(email, password);
    storeAuthSession(response);
    setToken(response.token);
    setUser({ name: response.name, email: response.email, role: response.role });
    return response;
  }

  async function register(name, email, password) {
    const response = await registerRequest(name, email, password);
    storeAuthSession(response);
    setToken(response.token);
    setUser({ name: response.name, email: response.email, role: response.role });
    return response;
  }

  async function logout() {
    await logoutRequest(token);
    clearAuthSession();
    setToken(null);
    setUser(null);
  }

  return (
    <AuthContext.Provider
      value={{
        token,
        user,
        isAuthenticated: Boolean(token),
        isAdmin: user?.role === "ADMIN",
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}
