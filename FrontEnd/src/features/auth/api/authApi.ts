import type {
  AuthResponse,
  LoginCredentials,
  RegisterCredentials,
} from "../types";

import { mockLogin, mockRegister } from "../services/mockAuthService";

const USE_MOCK = true;

export async function loginRequest(
  credentials: LoginCredentials
): Promise<AuthResponse> {
  if (USE_MOCK) {
    return mockLogin(credentials);
  }

  const response = await fetch("/api/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(credentials),
  });

  if (!response.ok) {
    throw new Error("Login failed");
  }

  return response.json();
}

export async function registerRequest(
  credentials: RegisterCredentials
): Promise<AuthResponse> {
  if (USE_MOCK) {
    return mockRegister(credentials);
  }

  const response = await fetch("/api/auth/register", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(credentials),
  });

  if (!response.ok) {
    throw new Error("Registration failed");
  }

  return response.json();
}