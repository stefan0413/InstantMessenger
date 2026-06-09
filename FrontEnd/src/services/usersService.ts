import { mapBackendUser } from "./channelsService";
import { apiUrl, authHeaders } from "./apiConfig";
import type { User } from "../types/user";

interface BackendUser {
  id: number;
  username: string;
  email: string;
}

export async function searchUsers(params: {
  query?: string;
  limit?: number;
}): Promise<User[]> {
  const query = new URLSearchParams({
    query: params.query ?? "",
    limit: String(params.limit ?? 25),
  });

  const response = await fetch(apiUrl(`/users?${query}`), {
    headers: authHeaders(),
  });

  if (!response.ok) {
    throw new Error("Could not load users");
  }

  const users: BackendUser[] = await response.json();
  return users.map(mapBackendUser);
}
