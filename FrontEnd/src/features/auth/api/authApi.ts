import type {
  AuthResponse,
  LoginCredentials,
  RegisterCredentials,
} from "../types";

async function parseAuthResponse(response: Response): Promise<AuthResponse> {
  if (!response.ok) {
    let backendMessage: string | undefined;

    try {
      const body = await response.json();
      backendMessage = body.detail || body.message || body.error;
    } catch {
      backendMessage = undefined;
    }

    const fallback =
      response.status === 409
        ? "Account already exists"
        : response.status === 401
          ? "Invalid email or password"
          : response.status === 400
            ? "Check the form fields and try again"
          : "Authentication failed";
    throw new Error(backendMessage || fallback);
  }

  return response.json();
}

async function authFetch(path: string, credentials: LoginCredentials | RegisterCredentials): Promise<AuthResponse> {
  try {
    const response = await fetch(path, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(credentials),
    });

    return parseAuthResponse(response);
  } catch (error) {
    if (error instanceof Error && error.message !== "Failed to fetch") {
      throw error;
    }

    throw new Error("Backend is not running or cannot reach the database", { cause: error });
  }
}

export async function loginRequest(
  credentials: LoginCredentials
): Promise<AuthResponse> {
  return authFetch("/api/auth/login", credentials);
}

export async function registerRequest(
  credentials: RegisterCredentials
): Promise<AuthResponse> {
  return authFetch("/api/auth/register", credentials);
}
