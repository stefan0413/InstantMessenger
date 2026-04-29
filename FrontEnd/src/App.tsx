import { useEffect, useState } from "react";
import { ChannelList } from "./components/ChannelList/ChannelList";
import { ChatWindow } from "./components/ChatWindow/ChatWindow";
import { NewDirectModal } from "./components/NewDirectModal/NewDirectModal";
import { NewGroupModal } from "./components/NewGroupModal/NewGroupModal";
import { currentUserId, mockUsers } from "./data/mockUsers";
import { getChannels } from "./services/channelsService";
import { Channel } from "./types/channel";
import { Message } from "./types/message";

function App() {
  const [channels, setChannels] = useState<Channel[]>([]);
  const [activeChannelId, setActiveChannelId] = useState<string>();
  const [isDirectModalOpen, setIsDirectModalOpen] = useState(false);
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

  function handleCreateDirectChat(userId: string): void {
    const existingChannel = channels.find(
      (channel) => channel.type === "direct" && channel.participantIds.includes(userId),
    );

    if (existingChannel) {
      setActiveChannelId(existingChannel.id);
      setIsDirectModalOpen(false);
      return;
    }

    const user = mockUsers.find((currentUser) => currentUser.id === userId);

    if (!user) {
      return;
    }

    const now = new Date().toISOString();
    const id = `direct-${Date.now()}`;

    const newChannel: Channel = {
      id,
      name: user.name,
      type: "direct",
      avatarUrl: user.avatarUrl,
      participantIds: [currentUserId, user.id],
      messages: [],
      lastMessage: "No messages yet",
      updatedAt: now,
    };

    setChannels([newChannel, ...channels]);
    setActiveChannelId(id);
    setSearchQuery("");
    setIsDirectModalOpen(false);
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
      <div className="app-shell">
        <ChannelList
          channels={filteredChannels}
          users={mockUsers}
          activeChannelId={activeChannelId}
          searchValue={searchQuery}
          onSearchChange={setSearchQuery}
          onSelectChannel={setActiveChannelId}
          onNewChatClick={() => setIsDirectModalOpen(true)}
          onNewGroupClick={() => setIsGroupModalOpen(true)}
        />

        <ChatWindow activeChannel={activeChannel} users={mockUsers} onSendMessage={handleSendMessage} />
      </div>

      <NewDirectModal
        users={mockUsers.filter((user) => user.id !== currentUserId)}
        isOpen={isDirectModalOpen}
        onClose={() => setIsDirectModalOpen(false)}
        onCreateChat={handleCreateDirectChat}
      />

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
