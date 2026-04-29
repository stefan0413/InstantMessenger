import { useEffect, useState } from "react";
import { ChannelList } from "./components/ChannelList/ChannelList";
import { ChatWindow } from "./components/ChatWindow/ChatWindow";
import { NewGroupModal } from "./components/NewGroupModal/NewGroupModal";
import { currentUserId, mockUsers } from "./data/mockUsers";
import { getChannels } from "./services/channelsService";
import { Channel } from "./types/channel";
import { Message } from "./types/message";

function App() {
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
