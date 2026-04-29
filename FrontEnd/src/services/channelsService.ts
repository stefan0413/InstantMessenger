import { mockChannels } from "../data/mockChannels";
import { Channel } from "../types/channel";

export async function getChannels(): Promise<Channel[]> {
  return new Promise((resolve) => {
    setTimeout(() => resolve(mockChannels), 300);
  });
}
