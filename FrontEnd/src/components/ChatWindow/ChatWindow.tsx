import { FormEvent, useEffect, useRef, useState } from "react";
import { Channel } from "../../types/channel";
import { User } from "../../types/user";
import { MessageBubble } from "../MessageBubble/MessageBubble";
import "./ChatWindow.css";

interface ChatWindowProps {
  activeChannel?: Channel;
  users: User[];
  onSendMessage: (text: string) => void;
}

export function ChatWindow({ activeChannel, users, onSendMessage }: ChatWindowProps) {
  const [messageText, setMessageText] = useState("");
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const participants = activeChannel
    ? users.filter((user) => activeChannel.participantIds.includes(user.id))
    : [];

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView();
  }, [activeChannel?.id, activeChannel?.messages.length]);

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

      <section className="chat-window__messages" aria-label={`${activeChannel.name} messages`}>
        {activeChannel.messages.length === 0 && (
          <div className="chat-window__no-messages">No messages yet</div>
        )}
        {activeChannel.messages.map((message) => (
          <MessageBubble
            key={message.id}
            message={message}
            sender={users.find((user) => user.id === message.senderId)}
          />
        ))}
        <div ref={messagesEndRef} />
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
