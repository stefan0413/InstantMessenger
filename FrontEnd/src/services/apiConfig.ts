export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export const WS_URL = import.meta.env.VITE_WS_URL ?? "ws://localhost:8080/ws-native";

export function apiUrl(path: string): string {
  return `${API_BASE_URL}${path}`;
}
