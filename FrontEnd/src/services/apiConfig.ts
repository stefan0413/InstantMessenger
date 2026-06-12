export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "/api";

const protocol = window.location.protocol === "https:" ? "wss" : "ws";

export const WS_URL = import.meta.env.VITE_WS_URL ??
                          `${protocol}://${window.location.host}/ws-native`;

export function apiUrl(path: string): string {
  return `${API_BASE_URL}${path}`;
}

export function getAuthToken(): string | null {
  return localStorage.getItem("token");
}

export function authHeaders(headers: Record<string, string> = {}): Record<string, string> {
  const token = getAuthToken();
  return token ? { ...headers, Authorization: `Bearer ${token}` } : headers;
}
