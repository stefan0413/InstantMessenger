export interface Message {
  id: string;
  channelId: string;
  senderId: string;
  text: string;
  createdAt: string;
  isMine?: boolean;
}
