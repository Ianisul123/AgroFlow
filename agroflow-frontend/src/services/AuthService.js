const AUTH_API_BASE_URL = "http://localhost:8080/auth";
const TOKEN_KEY = "agroflowAuthToken";
const USER_KEY = "agroflowAuthUser";

export function getStoredAuthToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function getStoredAuthUser() {
  const rawUser = localStorage.getItem(USER_KEY);

  if (!rawUser) {
    return null;
  }

  try {
    return JSON.parse(rawUser);
  } catch {
    localStorage.removeItem(USER_KEY);
    return null;
  }
}

export function storeAuthSession(authResponse) {
  localStorage.setItem(TOKEN_KEY, authResponse.token);
  localStorage.setItem(
    USER_KEY,
    JSON.stringify({
      name: authResponse.name,
      email: authResponse.email,
      role: authResponse.role,
    })
  );
}

export function clearAuthSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

async function parseResponse(response) {
  const data = await response.json().catch(() => null);

  if (!response.ok) {
    throw data || { message: "Authentication request failed." };
  }

  return data;
}

export async function login(email, password) {
  const response = await fetch(`${AUTH_API_BASE_URL}/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ email, password }),
  });

  return parseResponse(response);
}

export async function register(name, email, password) {
  const response = await fetch(`${AUTH_API_BASE_URL}/register`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ name, email, password }),
  });

  return parseResponse(response);
}

export async function logout(token) {
  if (!token) {
    return;
  }

  await fetch(`${AUTH_API_BASE_URL}/logout`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  }).catch(() => undefined);
}
