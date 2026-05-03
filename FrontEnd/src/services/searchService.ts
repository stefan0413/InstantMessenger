import { mockChannels } from "../data/mockChannels";
import type { Message } from "../types/message";

/**
 * Mock implementation of GET /search?query=...&channelId=...
 *
 * Filters messages within a single channel by a case-insensitive
 * substring match on the message text. Returns matches in the same
 * order as the original conversation.
 *
 * When the backend is ready, replace the body with:
 *   const response = await fetch(`/search?query=${encodeURIComponent(query)}&channelId=${channelId}`);
 *   return response.json();
 */
export async function searchMessages(
  query: string,
  channelId: string,
): Promise<Message[]> {
  return new Promise((resolve) => {
    setTimeout(() => {
      const trimmed = query.trim().toLowerCase();

      if (!trimmed) {
        resolve([]);
        return;
      }

      const channel = mockChannels.find((c) => c.id === channelId);

      if (!channel) {
        resolve([]);
        return;
      }

      const matches = channel.messages.filter((message) =>
        message.text.toLowerCase().includes(trimmed),
      );

      resolve(matches);
    }, 150);
  });
}
