import type { Message } from "../../types/message";
import type { User } from "../../types/user";
import { formatMessageTime } from "../../utils/dateFormat";
import "./MessageBubble.css";

interface MessageBubbleProps {
  message: Message;
  sender?: User;
  highlight?: string;
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

const IMAGE_TYPES = /\.(png|jpe?g|gif|webp|svg|avif)$/i;

function FileAttachment({ fileUrl, fileName }: { fileUrl: string; fileName?: string }) {
  if (IMAGE_TYPES.test(fileName ?? fileUrl)) {
    return (
      <a href={fileUrl} target="_blank" rel="noopener noreferrer" className="message-bubble__image-link">
        <img src={fileUrl} alt={fileName ?? "image"} className="message-bubble__image" />
      </a>
    );
  }

  return (
    <a href={fileUrl} target="_blank" rel="noopener noreferrer" download={fileName} className="message-bubble__file">
      <span className="message-bubble__file-icon">📄</span>
      <span className="message-bubble__file-name">{fileName ?? "Download file"}</span>
    </a>
  );
}

export function MessageBubble({ message, sender, highlight }: MessageBubbleProps) {
  return (
    <div className={`message-bubble ${message.isMine ? "message-bubble--mine" : ""}`}>
      {!message.isMine && <img className="message-bubble__avatar" src={sender?.avatarUrl} alt={sender?.name ?? "User"} />}
      <div className="message-bubble__body">
        {!message.isMine && <span className="message-bubble__sender">{sender?.name}</span>}
        {message.fileUrl && <FileAttachment fileUrl={message.fileUrl} fileName={message.fileName} />}
        {message.text && <p>{highlight ? renderHighlighted(message.text, highlight) : message.text}</p>}
        <time>{formatMessageTime(message.createdAt)}</time>
      </div>
    </div>
  );
}
