import { Message } from "../../types/message";
import { User } from "../../types/user";
import "./MessageBubble.css";

interface MessageBubbleProps {
  message: Message;
  sender?: User;
}

function formatMessageTime(value: string): string {
  return new Intl.DateTimeFormat("en", {
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value));
}

export function MessageBubble({ message, sender }: MessageBubbleProps) {
  return (
    <div className={`message-bubble ${message.isMine ? "message-bubble--mine" : ""}`}>
      {!message.isMine && <img className="message-bubble__avatar" src={sender?.avatarUrl} alt={sender?.name ?? "User"} />}
      <div className="message-bubble__body">
        {!message.isMine && <span className="message-bubble__sender">{sender?.name}</span>}
        <p>{message.text}</p>
        <time>{formatMessageTime(message.createdAt)}</time>
      </div>
    </div>
  );
}
