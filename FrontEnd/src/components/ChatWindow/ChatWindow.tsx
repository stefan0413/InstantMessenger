import { FormEvent, useEffect, useState } from "react";
import { searchMessages } from "../../services/searchService";
import type { Channel } from "../../types/channel";
import type { Message } from "../../types/message";
import type { User } from "../../types/user";
import { MessageBubble } from "../MessageBubble/MessageBubble";
import { SearchBar } from "../SearchBar/SearchBar";
import "./ChatWindow.css";

interface ChatWindowProps {
  activeChannel?: Channel;
  users: User[];
  onSendMessage: (text: string) => void;
}

export function ChatWindow({ activeChannel, users, onSendMessage }: ChatWindowProps) {
  const [messageText, setMessageText] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState<Message[] | null>(null);
  const [isSearching, setIsSearching] = useState(false);

  // Reset search state when switching channels
  useEffect(() => {
    setSearchQuery("");
    setSearchResults(null);
    setIsSearching(false);
  }, [activeChannel?.id]);

  // Debounced search: wait 250ms after the user stops typing,
  // then call the (mock) search service.
  useEffect(() => {
    const trimmed = searchQuery.trim();

    if (!trimmed || !activeChannel) {
      setSearchResults(null);
      setIsSearching(false);
      return;
    }

    setIsSearching(true);
    const timeoutId = setTimeout(() => {
      searchMessages(trimmed, activeChannel.id).then((results) => {
        setSearchResults(results);
        setIsSearching(false);
      });
    }, 250);

    return () => clearTimeout(timeoutId);
  }, [searchQuery, activeChannel?.id]);

  const participants = activeChannel
    ? users.filter((user) => activeChannel.participantIds.includes(user.id))
    : [];
  const directUser = participants.find((user) => user.name !== "You");

  const isSearchActive = searchQuery.trim().length > 0;
  const messagesToRender: Message[] = isSearchActive
    ? searchResults ?? []
    : activeChannel?.messages ?? [];

  function handleSubmit(event: FormEvent<HTMLFormElement>): void {
    event.preventDefault();
    const trimmedText = messageText.trim();

    if (!trimmedText) {
      return;
    }

    onSendMessage(trimmedText);
    setMessageText("");
  }

  if (!activeChannel) {
    return (
      <main className="chat-window chat-window--empty">
        <div>
          <h2>Select a conversation</h2>
          <p>Choose a chat from the sidebar to see messages and details.</p>
        </div>
      </main>
    );
  }

  return (
    <main className="chat-window">
      <header className="chat-window__header">
        <div className={`chat-window__avatar ${activeChannel.type === "group" ? "chat-window__avatar--group" : ""}`}>
          {activeChannel.type === "direct" ? (
            <>
              <img src={activeChannel.avatarUrl} alt={activeChannel.name} />
              {directUser && directUser.isOnline && <span />}
            </>
          ) : (
            participants.slice(0, 4).map((user) => <img key={user.id} src={user.avatarUrl} alt={user.name} />)
          )}
        </div>
        <div>
          <h2>{activeChannel.name}</h2>
          <p>{activeChannel.type === "group" ? `${participants.length} members` : directUser?.isOnline ? "Online now" : "Offline"}</p>
        </div>
      </header>

      <SearchBar
        value={searchQuery}
        onChange={setSearchQuery}
        resultCount={searchResults?.length ?? null}
        isSearching={isSearching}
      />

      <section className="chat-window__messages" aria-label={`${activeChannel.name} messages`}>
        {isSearchActive && !isSearching && messagesToRender.length === 0 ? (
          <div className="chat-window__empty-search">
            <p>No messages match your search.</p>
            <span>Try a different keyword.</span>
          </div>
        ) : (
          messagesToRender.map((message) => (
            <MessageBubble
              key={message.id}
              message={message}
              sender={users.find((user) => user.id === message.senderId)}
              highlight={isSearchActive ? searchQuery : undefined}
            />
          ))
        )}
      </section>

      <form className="chat-window__composer" onSubmit={handleSubmit}>
        <input
          value={messageText}
          onChange={(event) => setMessageText(event.target.value)}
          placeholder={`Message ${activeChannel.name}`}
        />
        <button disabled={!messageText.trim()} type="submit">
          Send
        </button>
      </form>
    </main>
  );
}
