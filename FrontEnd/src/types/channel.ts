import type { Message } from "./message";

export interface Channel {
  id: string;
  name: string;
  type: "direct" | "group";
  avatarUrl?: string;
  participantIds: string[];
  messages: Message[];
  lastMessage?: string;
  updatedAt: string;
}
