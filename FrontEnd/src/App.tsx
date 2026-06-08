import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { ChannelList } from "./components/ChannelList/ChannelList";
import { ChatWindow } from "./components/ChatWindow/ChatWindow";
import { NewDirectModal } from "./components/NewDirectModal/NewDirectModal";
import { NewGroupModal } from "./components/NewGroupModal/NewGroupModal";
import { createChannel, getChannels, getUsersFromChannels, isBackendChannel, mapBackendChannel } from "./services/channelsService";
import { ChatSocketClient } from "./services/chatSocketService";
import type { PresenceEvent } from "./services/chatSocketService";
import { getMessages, isBackendMessage, mapBackendMessage } from "./services/messagesService";
import { searchUsers } from "./services/usersService";
import type { Channel } from "./types/channel";
import type { Message } from "./types/message";
import type { User } from "./types/user";
import LoginForm from "./features/auth/components/LoginForm";
import RegisterForm from "./features/auth/components/RegisterForm";
import UserStatus from "./features/auth/components/UserStatus";
import { useAuth } from "./features/auth/context/AuthContext";

interface TypingEventData {
  userId: number;
  channelId: number;
  typing: boolean;
}

function isTypingEventData(data: unknown): data is TypingEventData {
  return (
    typeof data === "object" &&
    data !== null &&
    "userId" in data &&
    "channelId" in data &&
    "typing" in data
  );
}

function App() {
  const { isAuthenticated, user } = useAuth();
  const [channels, setChannels] = useState<Channel[]>([]);
  const [activeChannelId, setActiveChannelId] = useState<string>();
  const [isDirectModalOpen, setIsDirectModalOpen] = useState(false);
  const [isGroupModalOpen, setIsGroupModalOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [availableUsers, setAvailableUsers] = useState<User[]>([]);
  const [availableUsersLoading, setAvailableUsersLoading] = useState(false);
  const [availableUsersError, setAvailableUsersError] = useState<string | null>(null);
  const [createError, setCreateError] = useState<string | null>(null);
  const [loadedMessageChannelIds, setLoadedMessageChannelIds] = useState<Set<string>>(() => new Set());
  const [socketStatus, setSocketStatus] = useState<"connecting" | "connected" | "disconnected" | "error">("disconnected");
  const [sendError, setSendError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [authMode, setAuthMode] = useState<"login" | "register">("login");
  const [onlineUserIds, setOnlineUserIds] = useState<Set<string>>(new Set());
  const [typingUsers, setTypingUsers] = useState<Record<string, Set<string>>>({});
  const socketRef = useRef<ChatSocketClient | null>(null);
  const currentUserId = user ? String(user.id) : "";

  useEffect(() => {
    if (!isAuthenticated || !currentUserId) return;

    setLoading(true);
    getChannels(currentUserId).then((data) => {
      setChannels(data);
      setActiveChannelId(data[0]?.id);
      setLoading(false);
    }).catch(() => {
      setChannels([]);
      setLoading(false);
    });
  }, [isAuthenticated, currentUserId]);

  useEffect(() => {
    if (!isAuthenticated || !currentUserId) {
      socketRef.current?.disconnect();
      socketRef.current = null;
      setOnlineUserIds(new Set());
      setTypingUsers({});
      return;
    }

    const socket = new ChatSocketClient();
    socket.connect(setSocketStatus);
    socket.subscribeToPresence((event: PresenceEvent) => {
      setOnlineUserIds((prev) => {
        const next = new Set(prev);
        const uid = String(event.userId);
        if (event.status === "ONLINE") next.add(uid);
        else next.delete(uid);
        return next;
      });
    });
    socket.subscribeToUserNotifications(currentUserId, (event) => {
      if (event.type === "CHANNEL_NEW" && isBackendChannel(event.data)) {
        const newChannel = mapBackendChannel(event.data, currentUserId);
        setChannels((prev) => {
          if (prev.some((c) => c.id === newChannel.id)) return prev;
          return [newChannel, ...prev];
        });
      }
    });
    socket.sendUserConnect();
    socketRef.current = socket;

    return () => socket.disconnect();
  }, [isAuthenticated, currentUserId]);

  useEffect(() => {
    const socket = socketRef.current;
    if (!socket || !currentUserId) {
      return;
    }

    channels.forEach((channel) => {
      socket.subscribeToChannel(channel.id, (event) => {
        if (event.type === "MESSAGE_NEW") {
          if (!isBackendMessage(event.data)) return;

          const message = mapBackendMessage(event.data, currentUserId);
          setChannels((currentChannels) =>
            currentChannels.map((currentChannel) => {
              if (currentChannel.id !== message.channelId) return currentChannel;
              const alreadyExists = currentChannel.messages.some((item) => item.id === message.id);
              const messages = alreadyExists ? currentChannel.messages : [...currentChannel.messages, message];
              return { ...currentChannel, messages, lastMessage: message.text, updatedAt: message.createdAt };
            }),
          );
          return;
        }

        if (event.type === "TYPING" && isTypingEventData(event.data)) {
          const { userId, channelId, typing } = event.data;
          const uid = String(userId);
          const cid = String(channelId);
          setTypingUsers((prev) => {
            const next = { ...prev };
            const set = new Set(next[cid] ?? []);
            if (typing) set.add(uid);
            else set.delete(uid);
            next[cid] = set;
            return next;
          });
        }
      });
    });
  }, [channels, currentUserId]);

  useEffect(() => {
    if (!activeChannelId || !currentUserId) {
      return;
    }

    const activeChannel = channels.find((channel) => channel.id === activeChannelId);
    if (!activeChannel || loadedMessageChannelIds.has(activeChannelId) || activeChannel.isLoadingMessages) {
      return;
    }

    void loadMessages(activeChannelId);
  }, [activeChannelId, channels, currentUserId, loadedMessageChannelIds]);

  const filteredChannels = channels.filter((channel) =>
    channel.name.toLowerCase().includes(searchQuery.toLowerCase()),
  );
  const activeChannel = channels.find((channel) => channel.id === activeChannelId);
  const scopedUsers = useMemo(() => getUsersFromChannels(channels), [channels]);

  const activeChannelTypingUserIds = useMemo(() => {
    const set = new Set(typingUsers[activeChannelId ?? ""] ?? []);
    set.delete(currentUserId);
    return set;
  }, [typingUsers, activeChannelId, currentUserId]);

  const handleSendTyping = useCallback((channelId: string, isTyping: boolean) => {
    socketRef.current?.sendTyping({ channelId, typing: isTyping });
  }, []);
  const usersForCreation = useMemo(() => {
    const byId = new Map<string, User>();
    [...availableUsers, ...scopedUsers].forEach((candidate) => {
      if (candidate.id !== currentUserId) {
        byId.set(candidate.id, candidate);
      }
    });
    return Array.from(byId.values());
  }, [availableUsers, scopedUsers, currentUserId]);

  async function openDirectModal(): Promise<void> {
    setIsDirectModalOpen(true);
    setCreateError(null);
    await loadAvailableUsers();
  }

  async function openGroupModal(): Promise<void> {
    setIsGroupModalOpen(true);
    setCreateError(null);
    await loadAvailableUsers();
  }

  async function loadAvailableUsers(): Promise<void> {
    if (!currentUserId) {
      return;
    }

    setAvailableUsersLoading(true);
    setAvailableUsersError(null);

    try {
      const users = await searchUsers({ limit: 25 });
      setAvailableUsers(users);
    } catch {
      setAvailableUsers([]);
      setAvailableUsersError("Could not load users. Check that the backend was restarted and /users is available.");
    } finally {
      setAvailableUsersLoading(false);
    }
  }

  async function loadMessages(channelId: string, before?: string): Promise<void> {
    if (!currentUserId) {
      return;
    }

    setChannels((currentChannels) =>
      currentChannels.map((channel) =>
        channel.id === channelId ? { ...channel, isLoadingMessages: true } : channel,
      ),
    );

    let messages: Message[] = [];
    try {
      messages = await getMessages({ channelId, currentUserId, before, limit: 30 });
    } catch {
      if (!before) {
        setLoadedMessageChannelIds((current) => {
          const next = new Set(current);
          next.add(channelId);
          return next;
        });
      }

      setChannels((currentChannels) =>
        currentChannels.map((channel) =>
          channel.id === channelId ? { ...channel, isLoadingMessages: false } : channel,
        ),
      );
      return;
    }

    if (!before) {
      setLoadedMessageChannelIds((current) => {
        const next = new Set(current);
        next.add(channelId);
        return next;
      });
    }

    setChannels((currentChannels) =>
      currentChannels.map((channel) =>
        channel.id === channelId
          ? {
              ...channel,
              messages: before ? [...messages, ...channel.messages] : messages,
              hasMoreMessages: messages.length === 30,
              isLoadingMessages: false,
              lastMessage: before ? channel.lastMessage : messages.at(-1)?.text ?? channel.lastMessage,
              updatedAt: before ? channel.updatedAt : messages.at(-1)?.createdAt ?? channel.updatedAt,
            }
          : channel,
      ),
    );
  }

  function handleLoadOlder(channelId: string): void {
    const channel = channels.find((item) => item.id === channelId);
    const oldestMessageId = channel?.messages[0]?.id;

    if (!oldestMessageId || channel?.isLoadingMessages) {
      return;
    }

    void loadMessages(channelId, oldestMessageId);
  }

  function handleSendMessage(text: string, fileUrl?: string, fileName?: string): void {
    if (!activeChannel || !socketRef.current || !currentUserId) {
      return;
    }

    const wasSent = socketRef.current.sendMessage({
      content: text,
      channelId: activeChannel.id,
      fileUrl,
      fileName,
    });

    if (!wasSent) {
      setSendError("WebSocket is not connected yet. Message will send after reconnect.");
      return;
    }

    setSendError(null);
  }

  async function handleCreateGroup(payload: { name: string; participantIds: string[] }): Promise<void> {
    try {
      const newChannel = await createChannel({
        name: payload.name,
        memberIds: [currentUserId, ...payload.participantIds],
        currentUserId,
      });

      setChannels((currentChannels) => {
        if (currentChannels.some((c) => c.id === newChannel.id)) return currentChannels;
        return [newChannel, ...currentChannels];
      });
      setActiveChannelId(newChannel.id);
      setSearchQuery("");
      setCreateError(null);
      setIsGroupModalOpen(false);
    } catch {
      setCreateError("Could not create channel. Check backend logs and database migrations.");
    }
  }

  async function handleCreateDirectChat(userId: string): Promise<void> {
    const existingChannel = channels.find(
      (channel) => channel.type === "direct" && channel.participantIds.includes(userId),
    );

    if (existingChannel) {
      setActiveChannelId(existingChannel.id);
      setIsDirectModalOpen(false);
      return;
    }

    const user = usersForCreation.find((currentUser) => currentUser.id === userId);

    if (!user) {
      return;
    }

    try {
      const newChannel = await createChannel({
        name: user.name,
        memberIds: [currentUserId, user.id],
        currentUserId,
      });

      setChannels((currentChannels) => {
        if (currentChannels.some((c) => c.id === newChannel.id)) return currentChannels;
        return [newChannel, ...currentChannels];
      });
      setActiveChannelId(newChannel.id);
      setSearchQuery("");
      setCreateError(null);
      setIsDirectModalOpen(false);
    } catch {
      setCreateError("Could not create channel. Check backend logs and database migrations.");
    }
  }

  if (!isAuthenticated) {
    return (
      <main className="auth-shell">
        <section className="auth-panel" aria-label="Authentication">
          <div className="auth-preview">
            <div className="auth-preview__brand">
              <span>IM</span>
              <strong>InstantMessenger</strong>
            </div>
            <div className="auth-preview__thread">
              <div className="auth-preview__message">
                <span>Mila</span>
                <p>Campaign drafts are ready for review.</p>
              </div>
              <div className="auth-preview__message auth-preview__message--mine">
                <span>You</span>
                <p>Great, send them here and I will check tonight.</p>
              </div>
              <div className="auth-preview__message">
                <span>Team Studio</span>
                <p>New group created with 4 members.</p>
              </div>
            </div>
          </div>

          <div className="auth-card">
            <div className="auth-toggle" aria-label="Authentication mode">
              <button
                className={authMode === "login" ? "auth-toggle__button auth-toggle__button--active" : "auth-toggle__button"}
                onClick={() => setAuthMode("login")}
                type="button"
              >
                Login
              </button>
              <button
                className={authMode === "register" ? "auth-toggle__button auth-toggle__button--active" : "auth-toggle__button"}
                onClick={() => setAuthMode("register")}
                type="button"
              >
                Register
              </button>
            </div>

            {authMode === "login" ? <LoginForm /> : <RegisterForm />}
          </div>
        </section>
      </main>
    );
  }

  if (loading) {
    return (
      <div className="app-loading">
        <div className="app-loading__pulse" />
        <p>Loading conversations...</p>
      </div>
    );
  }

  return (
    <>
      <UserStatus />
      <div className="app-shell">
        <ChannelList
          channels={filteredChannels}
          users={scopedUsers}
          activeChannelId={activeChannelId}
          searchValue={searchQuery}
          onSearchChange={setSearchQuery}
          onSelectChannel={setActiveChannelId}
          onNewChatClick={() => void openDirectModal()}
          onNewGroupClick={() => void openGroupModal()}
          currentUserId={currentUserId}
          onlineUserIds={onlineUserIds}
        />

        <ChatWindow
          activeChannel={activeChannel}
          users={activeChannel?.participants ?? []}
          currentUserId={currentUserId}
          socketStatus={socketStatus}
          error={sendError}
          onSendMessage={handleSendMessage}
          onLoadOlder={handleLoadOlder}
          typingUserIds={activeChannelTypingUserIds}
          onTyping={handleSendTyping}
        />
      </div>

      <NewDirectModal
        users={usersForCreation}
        isLoading={availableUsersLoading}
        error={availableUsersError ?? createError}
        isOpen={isDirectModalOpen}
        onClose={() => setIsDirectModalOpen(false)}
        onCreateChat={handleCreateDirectChat}
      />

      <NewGroupModal
        users={usersForCreation}
        isLoadingUsers={availableUsersLoading}
        error={availableUsersError ?? createError}
        isOpen={isGroupModalOpen}
        onClose={() => setIsGroupModalOpen(false)}
        onCreateGroup={handleCreateGroup}
      />
    </>
  );
}

export default App;
