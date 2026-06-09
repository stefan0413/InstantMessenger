import { useEffect, useRef, useState } from "react";
import type { ChangeEvent, FormEvent } from "react";
import { uploadFile } from "../../services/filesService";
import { searchMessages } from "../../services/searchService";
import type { Channel } from "../../types/channel";
import type { Message } from "../../types/message";
import type { User } from "../../types/user";
import { formatMessageDateLabel, shouldShowDateSeparator } from "../../utils/dateFormat";
import { MessageBubble } from "../MessageBubble/MessageBubble";
import { SearchBar } from "../SearchBar/SearchBar";
import "./ChatWindow.css";

interface ChatWindowProps {
  activeChannel?: Channel;
  users: User[];
  currentUserId: string;
  socketStatus: "connecting" | "connected" | "disconnected" | "error";
  error: string | null;
  onSendMessage: (text: string, fileUrl?: string, fileName?: string) => void;
  onLoadOlder: (channelId: string) => void;
  typingUserIds: Set<string>;
  onTyping: (channelId: string, isTyping: boolean) => void;
}

function formatTypingLabel(names: string[]): string {
  if (names.length === 1) return `${names[0]} is typing…`;
  if (names.length === 2) return `${names[0]} and ${names[1]} are typing…`;
  return "Several people are typing…";
}

export function ChatWindow({ activeChannel, users, currentUserId, socketStatus, error, onSendMessage, onLoadOlder, typingUserIds, onTyping }: ChatWindowProps) {
  const [messageText, setMessageText] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState<Message[] | null>(null);
  const [isSearching, setIsSearching] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const typingTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    setSearchQuery("");
    setSearchResults(null);
    setIsSearching(false);
  }, [activeChannel?.id]);

  useEffect(() => {
    const channelId = activeChannel?.id;
    return () => {
      if (typingTimerRef.current !== null) {
        clearTimeout(typingTimerRef.current);
        typingTimerRef.current = null;
      }
      if (channelId) onTyping(channelId, false);
    };
  }, [activeChannel?.id]);

  useEffect(() => {
    const trimmed = searchQuery.trim();

    if (!trimmed || !activeChannel) {
      setSearchResults(null);
      setIsSearching(false);
      return;
    }

    setIsSearching(true);
    const timeoutId = setTimeout(() => {
      searchMessages(trimmed, activeChannel.id, currentUserId).then((results) => {
        setSearchResults(results);
        setIsSearching(false);
      });
    }, 250);

    return () => clearTimeout(timeoutId);
  }, [searchQuery, activeChannel?.id]);

  const participants = activeChannel
    ? users.filter((user) => activeChannel.participantIds.includes(user.id))
    : [];

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView();
  }, [activeChannel?.id, activeChannel?.messages.length]);

  const isSearchActive = searchQuery.trim().length > 0;
  const messagesToRender: Message[] = isSearchActive
    ? searchResults ?? []
    : activeChannel?.messages ?? [];

  function handleInputChange(event: ChangeEvent<HTMLInputElement>): void {
    setMessageText(event.target.value);
    if (!activeChannel) return;

    if (typingTimerRef.current === null) {
      onTyping(activeChannel.id, true);
    } else {
      clearTimeout(typingTimerRef.current);
    }
    typingTimerRef.current = setTimeout(() => {
      typingTimerRef.current = null;
      if (activeChannel) onTyping(activeChannel.id, false);
    }, 3000);
  }

  async function handleFileChange(event: ChangeEvent<HTMLInputElement>): Promise<void> {
    const file = event.target.files?.[0];
    if (!file) return;

    setIsUploading(true);
    setUploadError(null);

    try {
      const { publicUrl, fileName } = await uploadFile(file);
      onSendMessage("", publicUrl, fileName);
    } catch {
      setUploadError("File upload failed. Please try again.");
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>): void {
    event.preventDefault();
    const trimmedText = messageText.trim();

    if (!trimmedText) return;

    if (typingTimerRef.current !== null) {
      clearTimeout(typingTimerRef.current);
      typingTimerRef.current = null;
    }
    if (activeChannel) onTyping(activeChannel.id, false);

    onSendMessage(trimmedText);
    setMessageText("");
  }

  const typingNames = [...typingUserIds].map(
    (id) => users.find((u) => u.id === id)?.name ?? "Someone",
  );

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
            <img src={activeChannel.avatarUrl} alt={activeChannel.name} />
          ) : (
            participants.slice(0, 4).map((user) => <img key={user.id} src={user.avatarUrl} alt={user.name} />)
          )}
        </div>
        <div>
          <h2>{activeChannel.name}</h2>
          <p>{activeChannel.type === "group" ? `${participants.length} members` : "Direct chat"}</p>
        </div>
      </header>

      <SearchBar
        value={searchQuery}
        onChange={setSearchQuery}
        resultCount={searchResults?.length ?? null}
        isSearching={isSearching}
      />

      <section className="chat-window__messages" aria-label={`${activeChannel.name} messages`}>
        {!isSearchActive && activeChannel.hasMoreMessages && (
          <button
            className="chat-window__load-older"
            disabled={activeChannel.isLoadingMessages}
            onClick={() => onLoadOlder(activeChannel.id)}
            type="button"
          >
            {activeChannel.isLoadingMessages ? "Loading..." : "Load older messages"}
          </button>
        )}
        {isSearchActive && !isSearching && messagesToRender.length === 0 ? (
          <div className="chat-window__empty-search">
            <p>No messages match your search.</p>
            <span>Try a different keyword.</span>
          </div>
        ) : messagesToRender.length === 0 ? (
          <div className="chat-window__no-messages">No messages yet</div>
        ) : (
          messagesToRender.map((message, index) => {
            const previousMessage = messagesToRender[index - 1];
            const showDateSeparator = shouldShowDateSeparator(message.createdAt, previousMessage?.createdAt);

            return (
              <div className="chat-window__message-row" key={message.id}>
                {showDateSeparator && (
                  <div className="chat-window__date-separator">
                    <time dateTime={message.createdAt}>{formatMessageDateLabel(message.createdAt)}</time>
                  </div>
                )}
                <MessageBubble
                  message={message}
                  sender={users.find((user) => user.id === message.senderId)}
                  highlight={isSearchActive ? searchQuery : undefined}
                />
              </div>
            );
          })
        )}
        <div ref={messagesEndRef} />
      </section>

      <div className="chat-window__typing-indicator" aria-live="polite">
        {typingNames.length > 0 && <span>{formatTypingLabel(typingNames)}</span>}
      </div>

      <form className="chat-window__composer" onSubmit={handleSubmit}>
        {(error || uploadError || socketStatus !== "connected") && (
          <div className="chat-window__composer-status">
            {uploadError ?? error ?? `WebSocket ${socketStatus}`}
          </div>
        )}
        <input
          ref={fileInputRef}
          type="file"
          className="chat-window__file-input"
          onChange={handleFileChange}
          disabled={isUploading || socketStatus !== "connected"}
          aria-label="Attach file"
        />
        <button
          type="button"
          className="chat-window__attach-btn"
          disabled={isUploading || socketStatus !== "connected"}
          onClick={() => fileInputRef.current?.click()}
          title="Attach file"
        >
          {isUploading ? "..." : "📎"}
        </button>
        <input
          value={messageText}
          onChange={handleInputChange}
          placeholder={`Message ${activeChannel.name}`}
        />
        <button disabled={!messageText.trim() || socketStatus !== "connected"} type="submit">
          Send
        </button>
      </form>
    </main>
  );
}
