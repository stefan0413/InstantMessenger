import type { Message } from "../types/message";
import { apiUrl, authHeaders } from "./apiConfig";
import { isBackendMessage, mapBackendMessage } from "./messagesService";


export async function searchMessages(
  query: string,
  channelId: string,
  currentUserId: string,
): Promise<Message[]> {
  const trimmed = query.trim();

  if (!trimmed) {
    return [];
  }

  const params = new URLSearchParams({
    query: trimmed,
    channelId,
  });

  const response = await fetch(apiUrl(`/search?${params}`), {
    headers: authHeaders(),
  });

  if (!response.ok) {
    throw new Error("Search request failed");
  }

  const data: unknown = await response.json();

  if (!Array.isArray(data)) {
    return [];
  }

  return data
    .filter(isBackendMessage)
    .map((message) => mapBackendMessage(message, currentUserId));
}
