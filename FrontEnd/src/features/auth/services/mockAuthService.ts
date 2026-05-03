import type {
  AuthResponse,
  LoginCredentials,
  RegisterCredentials,
} from "../types.ts";

const MOCK_USER = {
  id: 1,
  username: "stefan",
  email: "test@test.com",
  password: "123456",
};

export async function mockLogin(
  credentials: LoginCredentials
): Promise<AuthResponse> {
  await new Promise((resolve) => setTimeout(resolve, 500));

  if (
    credentials.email === MOCK_USER.email &&
    credentials.password === MOCK_USER.password
  ) {
    return {
      user: {
        id: MOCK_USER.id,
        username: MOCK_USER.username,
        email: MOCK_USER.email,
      },
      token: "mock-jwt-token",
    };
  }

  throw new Error("Invalid email or password");
}

export async function mockRegister(
  credentials: RegisterCredentials
): Promise<AuthResponse> {
  await new Promise((resolve) => setTimeout(resolve, 500));

  return {
    user: {
      id: Date.now(),
      username: credentials.username,
      email: credentials.email,
    },
    token: "mock-jwt-token",
  };
}