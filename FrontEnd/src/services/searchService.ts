import { getMessages } from "./messagesService";
import type { Message } from "../types/message";

export async function searchMessages(
  query: string,
  channelId: string,
  currentUserId: string,
): Promise<Message[]> {
  const trimmed = query.trim().toLowerCase();

  if (!trimmed) {
    return [];
  }

  const messages = await getMessages({ channelId, currentUserId, limit: 100 });
  return messages.filter((message) => message.text.toLowerCase().includes(trimmed));
}
