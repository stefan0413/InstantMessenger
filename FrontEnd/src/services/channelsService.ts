import type { Channel } from "../types/channel";
import type { User } from "../types/user";
import { apiUrl } from "./apiConfig";

interface BackendUser {
  id: number;
  username: string;
  email: string;
}

interface BackendChannel {
  id: number;
  name: string;
  memberIds: number[];
  members: BackendUser[];
}

function avatarForUser(userId: string): string {
  return `https://api.dicebear.com/9.x/initials/svg?seed=${encodeURIComponent(userId)}`;
}

export function mapBackendUser(user: BackendUser): User {
  return {
    id: String(user.id),
    name: user.username,
    avatarUrl: avatarForUser(user.username),
    isOnline: false,
  };
}

function mapBackendChannel(channel: BackendChannel, currentUserId?: string): Channel {
  const members = channel.members.map(mapBackendUser);
  const participantIds = channel.memberIds.map(String);
  const isDirect = participantIds.length === 2;
  const otherParticipant = members.find((member) => member.id !== currentUserId);

  return {
    id: String(channel.id),
    name: isDirect && otherParticipant ? otherParticipant.name : channel.name,
    type: isDirect ? "direct" : "group",
    avatarUrl: isDirect ? otherParticipant?.avatarUrl : undefined,
    participantIds,
    participants: members,
    messages: [],
    hasMoreMessages: true,
    lastMessage: "No messages yet",
    updatedAt: new Date(0).toISOString(),
  };
}

export async function getChannels(currentUserId: string): Promise<Channel[]> {
  const response = await fetch(apiUrl(`/channels?userId=${encodeURIComponent(currentUserId)}`));

  if (!response.ok) {
    throw new Error("Could not load channels");
  }

  const channels: BackendChannel[] = await response.json();
  return channels.map((channel) => mapBackendChannel(channel, currentUserId));
}

export async function createChannel(payload: { name: string; memberIds: string[]; currentUserId: string }): Promise<Channel> {
  const response = await fetch(apiUrl(`/channels?userId=${encodeURIComponent(payload.currentUserId)}`), {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      name: payload.name,
      memberIds: payload.memberIds.map(Number),
    }),
  });

  if (!response.ok) {
    throw new Error("Could not create channel");
  }

  const channel: BackendChannel = await response.json();
  return mapBackendChannel(channel, payload.currentUserId);
}

export function getUsersFromChannels(channels: Channel[]): User[] {
  const seen = new Set<string>();
  const users: User[] = [];

  for (const channel of channels) {
    users.push(...channel.participants);
  }

  return users.filter((user) => {
    if (seen.has(user.id)) {
      return false;
    }
    seen.add(user.id);
    return true;
  });
}
