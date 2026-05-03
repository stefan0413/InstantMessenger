import type { Message } from "./message";
import type { User } from "./user";

export interface Channel {
  id: string;
  name: string;
  type: "direct" | "group";
  avatarUrl?: string;
  participantIds: string[];
  participants: User[];
  messages: Message[];
  hasMoreMessages?: boolean;
  isLoadingMessages?: boolean;
  lastMessage?: string;
  updatedAt: string;
}
