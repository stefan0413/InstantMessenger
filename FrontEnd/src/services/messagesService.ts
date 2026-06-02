import type { Message } from "../types/message";
import { apiUrl } from "./apiConfig";

interface BackendMessage {
  id: number;
  content: string | null;
  userId: number;
  channelId: number;
  time: string;
  fileUrl?: string | null;
  fileName?: string | null;
}

export interface ChannelEvent {
  type: "MESSAGE_NEW" | "MESSAGE_EDIT" | "MESSAGE_DELETE" | "TYPING";
  data: unknown;
}

export function mapBackendMessage(message: BackendMessage, currentUserId: string): Message {
  return {
    id: String(message.id),
    channelId: String(message.channelId),
    senderId: String(message.userId),
    text: message.content ?? "",
    createdAt: message.time,
    isMine: String(message.userId) === currentUserId,
    fileUrl: message.fileUrl ?? undefined,
    fileName: message.fileName ?? undefined,
  };
}

export function isBackendMessage(value: unknown): value is BackendMessage {
  if (!value || typeof value !== "object") {
    return false;
  }

  const candidate = value as Partial<BackendMessage>;
  return (
    typeof candidate.id === "number" &&
    (candidate.content === null || typeof candidate.content === "string") &&
    typeof candidate.userId === "number" &&
    typeof candidate.channelId === "number" &&
    typeof candidate.time === "string"
  );
}

export async function getMessages(params: {
  channelId: string;
  currentUserId: string;
  limit?: number;
  before?: string;
}): Promise<Message[]> {
  const query = new URLSearchParams({
    channelId: params.channelId,
    limit: String(params.limit ?? 50),
  });

  if (params.before) {
    query.set("before", params.before);
  }

  const response = await fetch(apiUrl(`/messages?${query}`));

  if (!response.ok) {
    throw new Error("Could not load messages");
  }

  const messages: BackendMessage[] = await response.json();
  return messages.map((message) => mapBackendMessage(message, params.currentUserId));
}
