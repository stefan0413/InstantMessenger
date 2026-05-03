export type User = {
  id: number;
  username: string;
  email: string;
};

export type AuthResponse = {
  user: User;
  token: string;
};

export type LoginCredentials = {
  email: string;
  password: string;
};

export type RegisterCredentials = {
  username: string;
  email: string;
  password: string;
};