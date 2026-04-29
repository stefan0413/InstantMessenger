import { User } from "../types/user";

export const currentUserId = "user-me";

export const mockUsers: User[] = [
  {
    id: currentUserId,
    name: "You",
    avatarUrl: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=160&q=80",
    isOnline: true,
  },
  {
    id: "user-1",
    name: "Mila Petrova",
    avatarUrl: "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?auto=format&fit=crop&w=160&q=80",
    isOnline: true,
  },
  {
    id: "user-2",
    name: "Alex Marinov",
    avatarUrl: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=160&q=80",
    isOnline: false,
  },
  {
    id: "user-3",
    name: "Nora Ivanova",
    avatarUrl: "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=160&q=80",
    isOnline: true,
  },
  {
    id: "user-4",
    name: "Victor Stoyanov",
    avatarUrl: "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=160&q=80",
    isOnline: true,
  },
  {
    id: "user-5",
    name: "Elena Dimitrova",
    avatarUrl: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=160&q=80",
    isOnline: false,
  },
  {
    id: "user-6",
    name: "Daniel Kolev",
    avatarUrl: "https://images.unsplash.com/photo-1519345182560-3f2917c472ef?auto=format&fit=crop&w=160&q=80",
    isOnline: true,
  },
  {
    id: "user-7",
    name: "Sofia Georgieva",
    avatarUrl: "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=160&q=80",
    isOnline: false,
  },
];
