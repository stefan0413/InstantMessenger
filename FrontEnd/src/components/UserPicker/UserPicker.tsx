import { User } from "../../types/user";
import "./UserPicker.css";

interface UserPickerProps {
  users: User[];
  selectedUserIds: string[];
  onChange: (selectedUserIds: string[]) => void;
}

export function UserPicker({ users, selectedUserIds, onChange }: UserPickerProps) {
  function toggleUser(userId: string): void {
    if (selectedUserIds.includes(userId)) {
      onChange(selectedUserIds.filter((id) => id !== userId));
      return;
    }

    onChange([...selectedUserIds, userId]);
  }

  const selectedUsers = users.filter((user) => selectedUserIds.includes(user.id));

  return (
    <div className="user-picker">
      <div className="user-picker__chips" aria-label="Selected users">
        {selectedUsers.length > 0 ? (
          selectedUsers.map((user) => (
            <button key={user.id} onClick={() => toggleUser(user.id)} type="button">
              <img src={user.avatarUrl} alt="" />
              {user.name}
            </button>
          ))
        ) : (
          <span>Select at least two people</span>
        )}
      </div>

      <div className="user-picker__list">
        {users.map((user) => {
          const isSelected = selectedUserIds.includes(user.id);

          return (
            <label className="user-picker__row" key={user.id}>
              <img src={user.avatarUrl} alt={user.name} />
              <span>
                <strong>{user.name}</strong>
              </span>
              <input checked={isSelected} onChange={() => toggleUser(user.id)} type="checkbox" />
            </label>
          );
        })}
      </div>
    </div>
  );
}
