import type { Channel } from "../../types/channel";
import type { User } from "../../types/user";
import { ChannelItem } from "../ChannelItem/ChannelItem";
import "./ChannelList.css";

interface ChannelListProps {
  channels: Channel[];
  users: User[];
  activeChannelId?: string;
  searchValue: string;
  onSearchChange: (value: string) => void;
  onSelectChannel: (channelId: string) => void;
  onNewChatClick: () => void;
  onNewGroupClick: () => void;
}

export function ChannelList({
  channels,
  users,
  activeChannelId,
  searchValue,
  onSearchChange,
  onSelectChannel,
  onNewChatClick,
  onNewGroupClick,
}: ChannelListProps) {
  return (
    <aside className="channel-list">
      <div className="channel-list__header">
        <div>
          <p className="channel-list__eyebrow">Inbox</p>
          <h1>Messages</h1>
        </div>
        <div className="channel-list__actions">
          <button className="channel-list__new" onClick={onNewChatClick} type="button">
            New chat
          </button>
          <button className="channel-list__new channel-list__new--secondary" onClick={onNewGroupClick} type="button">
            New group
          </button>
        </div>
      </div>

      <label className="channel-list__search">
        <span>Search</span>
        <input
          value={searchValue}
          onChange={(event) => onSearchChange(event.target.value)}
          placeholder="Search conversations"
          type="search"
        />
      </label>

      <div className="channel-list__items">
        {channels.length > 0 ? (
          channels.map((channel) => (
            <ChannelItem
              key={channel.id}
              channel={channel}
              users={users}
              isActive={channel.id === activeChannelId}
              onSelect={onSelectChannel}
            />
          ))
        ) : (
          <div className="channel-list__empty">No conversations found</div>
        )}
      </div>
    </aside>
  );
}
