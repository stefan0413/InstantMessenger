import { Message } from "../../types/message";
import { User } from "../../types/user";
import "./MessageBubble.css";

interface MessageBubbleProps {
  message: Message;
  sender?: User;
  highlight?: string;
}

function formatMessageTime(value: string): string {
  return new Intl.DateTimeFormat("en", {
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value));
}

/**
 * Splits the text into segments where the matching parts are
 * wrapped in <mark> tags for visual highlighting. Case-insensitive.
 */
function renderHighlighted(text: string, query: string) {
  const trimmed = query.trim();
  if (!trimmed) {
    return text;
  }

  // Escape regex special characters in the user input
  const escaped = trimmed.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
  const regex = new RegExp(`(${escaped})`, "gi");
  const parts = text.split(regex);

  return parts.map((part, index) =>
    part.toLowerCase() === trimmed.toLowerCase() ? (
      <mark key={index} className="message-bubble__match">
        {part}
      </mark>
    ) : (
      <span key={index}>{part}</span>
    ),
  );
}

export function MessageBubble({ message, sender, highlight }: MessageBubbleProps) {
  return (
    <div className={`message-bubble ${message.isMine ? "message-bubble--mine" : ""}`}>
      {!message.isMine && <img className="message-bubble__avatar" src={sender?.avatarUrl} alt={sender?.name ?? "User"} />}
      <div className="message-bubble__body">
        {!message.isMine && <span className="message-bubble__sender">{sender?.name}</span>}
        <p>{highlight ? renderHighlighted(message.text, highlight) : message.text}</p>
        <time>{formatMessageTime(message.createdAt)}</time>
      </div>
    </div>
  );
}
