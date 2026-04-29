import { Channel } from "../../types/channel";
import { User } from "../../types/user";
import { ChannelItem } from "../ChannelItem/ChannelItem";
import "./ChannelList.css";

interface ChannelListProps {
  channels: Channel[];
  users: User[];
  activeChannelId?: string;
  searchValue: string;
  onSearchChange: (value: string) => void;
  onSelectChannel: (channelId: string) => void;
  onNewGroupClick: () => void;
}

export function ChannelList({
  channels,
  users,
  activeChannelId,
  searchValue,
  onSearchChange,
  onSelectChannel,
  onNewGroupClick,
}: ChannelListProps) {
  return (
    <aside className="channel-list">
      <div className="channel-list__header">
        <div>
          <p className="channel-list__eyebrow">Inbox</p>
          <h1>Messages</h1>
        </div>
        <button className="channel-list__new" onClick={onNewGroupClick} type="button">
          New group
        </button>
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
