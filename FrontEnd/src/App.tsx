import { useEffect, useState } from "react";
import LoginForm from "./features/auth/components/LoginForm";
import RegisterForm from "./features/auth/components/RegisterForm";
import UserStatus from "./features/auth/components/UserStatus";
import { useAuth } from "./features/auth/context/AuthContext";
import { ChannelList } from "./components/ChannelList/ChannelList";
import { ChatWindow } from "./components/ChatWindow/ChatWindow";
import { NewGroupModal } from "./components/NewGroupModal/NewGroupModal";
import { currentUserId, mockUsers } from "./data/mockUsers";
import { getChannels } from "./services/channelsService";
import type { Channel } from "./types/channel";
import type { Message } from "./types/message";

function App() {
  const { isAuthenticated } = useAuth();
  const [authMode, setAuthMode] = useState<"login" | "register">("login");
  const [channels, setChannels] = useState<Channel[]>([]);
  const [activeChannelId, setActiveChannelId] = useState<string>();
  const [isGroupModalOpen, setIsGroupModalOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getChannels().then((data) => {
      setChannels(data);
      setActiveChannelId(data[0]?.id);
      setLoading(false);
    });
  }, []);

  const filteredChannels = channels.filter((channel) =>
    channel.name.toLowerCase().includes(searchQuery.toLowerCase()),
  );
  const activeChannel = channels.find((channel) => channel.id === activeChannelId);

  function handleSendMessage(text: string): void {
    if (!activeChannel) {
      return;
    }

    const now = new Date().toISOString();
    const newMessage: Message = {
      id: `message-${Date.now()}`,
      channelId: activeChannel.id,
      senderId: currentUserId,
      text,
      createdAt: now,
      isMine: true,
    };

    setChannels((currentChannels) =>
      currentChannels.map((channel) =>
        channel.id === activeChannel.id
          ? {
              ...channel,
              messages: [...channel.messages, newMessage],
              lastMessage: text,
              updatedAt: now,
            }
          : channel,
      ),
    );
  }

  function handleCreateGroup(payload: { name: string; participantIds: string[] }): void {
    const now = new Date().toISOString();
    const id = `group-${Date.now()}`;

    const welcomeMessage: Message = {
      id: `welcome-${Date.now()}`,
      channelId: id,
      senderId: currentUserId,
      text: `Created "${payload.name}" and added ${payload.participantIds.length} people.`,
      createdAt: now,
      isMine: true,
    };

    const newChannel: Channel = {
      id,
      name: payload.name,
      type: "group",
      participantIds: [currentUserId, ...payload.participantIds],
      messages: [welcomeMessage],
      lastMessage: welcomeMessage.text,
      updatedAt: now,
    };

    setChannels((currentChannels) => [newChannel, ...currentChannels]);
    setActiveChannelId(newChannel.id);
    setSearchQuery("");
    setIsGroupModalOpen(false);
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
          users={mockUsers}
          activeChannelId={activeChannelId}
          searchValue={searchQuery}
          onSearchChange={setSearchQuery}
          onSelectChannel={setActiveChannelId}
          onNewGroupClick={() => setIsGroupModalOpen(true)}
        />

        <ChatWindow activeChannel={activeChannel} users={mockUsers} onSendMessage={handleSendMessage} />
      </div>

      <NewGroupModal
        users={mockUsers.filter((user) => user.id !== currentUserId)}
        isOpen={isGroupModalOpen}
        onClose={() => setIsGroupModalOpen(false)}
        onCreateGroup={handleCreateGroup}
      />
    </>
  );
}

export default App;
