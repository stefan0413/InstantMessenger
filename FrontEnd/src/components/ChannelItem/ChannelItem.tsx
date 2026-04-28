import { Channel } from "../../types/channel";
import { User } from "../../types/user";
import "./ChannelItem.css";

interface ChannelItemProps {
  channel: Channel;
  users: User[];
  isActive: boolean;
  onSelect: (channelId: string) => void;
}

function formatTime(value: string): string {
  return new Intl.DateTimeFormat("en", {
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value));
}

export function ChannelItem({ channel, users, isActive, onSelect }: ChannelItemProps) {
  const participants = users.filter((user) => channel.participantIds.includes(user.id));
  const directUser = participants.find((user) => user.name !== "You");

  return (
    <button
      className={`channel-item ${isActive ? "channel-item--active" : ""}`}
      onClick={() => onSelect(channel.id)}
      type="button"
    >
      <div className={`channel-item__avatar ${channel.type === "group" ? "channel-item__avatar--group" : ""}`}>
        {channel.type === "direct" ? (
          <>
            <img src={channel.avatarUrl} alt={channel.name} />
            {directUser && directUser.isOnline && <span className="channel-item__online" />}
          </>
        ) : (
          participants.slice(0, 3).map((user) => <img key={user.id} src={user.avatarUrl} alt={user.name} />)
        )}
      </div>

      <div className="channel-item__content">
        <div className="channel-item__topline">
          <span className="channel-item__name">{channel.name}</span>
          <time className="channel-item__time">{formatTime(channel.updatedAt)}</time>
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
