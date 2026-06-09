import type { Channel } from "../../types/channel";
import type { User } from "../../types/user";
import { formatChannelTimestamp } from "../../utils/dateFormat";
import "./ChannelItem.css";

interface ChannelItemProps {
  channel: Channel;
  users: User[];
  isActive: boolean;
  onSelect: (channelId: string) => void;
  currentUserId: string;
  onlineUserIds: Set<string>;
}

export function ChannelItem({ channel, users, isActive, onSelect, currentUserId, onlineUserIds }: ChannelItemProps) {
  const participants = users.filter((user) => channel.participantIds.includes(user.id));
  const otherParticipantId = channel.type === "direct"
    ? channel.participantIds.find((id) => id !== currentUserId)
    : undefined;
  const isOnline = !!otherParticipantId && onlineUserIds.has(otherParticipantId);

  return (
    <button
      className={`channel-item ${isActive ? "channel-item--active" : ""}`}
      onClick={() => onSelect(channel.id)}
      type="button"
    >
      <div className={`channel-item__avatar ${channel.type === "group" ? "channel-item__avatar--group" : ""}`}>
        {channel.type === "direct" ? (
          <img src={channel.avatarUrl} alt={channel.name} />
        ) : (
          participants.slice(0, 3).map((user) => <img key={user.id} src={user.avatarUrl} alt={user.name} />)
        )}
        {isOnline && <span className="channel-item__online-dot" aria-label="Online" />}
      </div>

      <div className="channel-item__content">
        <div className="channel-item__topline">
          <span className="channel-item__name">{channel.name}</span>
          <time className="channel-item__time" title={new Date(channel.updatedAt).toLocaleString()}>
            {formatChannelTimestamp(channel.updatedAt)}
          </time>
        </div>
        <div className="channel-item__bottomline">
          <span className="channel-item__last">{channel.lastMessage}</span>
          <span className={`channel-item__type channel-item__type--${channel.type}`}>
            {channel.type === "group" ? "Group" : "Direct"}
          </span>
        </div>
      </div>
    </button>
  );
}
