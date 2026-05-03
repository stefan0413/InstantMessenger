import { useState } from "react";
import type { FormEvent } from "react";
import type { User } from "../../types/user";
import { UserPicker } from "../UserPicker/UserPicker";
import "./NewGroupModal.css";

interface CreateGroupPayload {
  name: string;
  participantIds: string[];
}

interface NewGroupModalProps {
  users: User[];
  isLoadingUsers: boolean;
  error: string | null;
  isOpen: boolean;
  onClose: () => void;
  onCreateGroup: (payload: CreateGroupPayload) => void;
}

export function NewGroupModal({ users, isLoadingUsers, error, isOpen, onClose, onCreateGroup }: NewGroupModalProps) {
  const [groupName, setGroupName] = useState("");
  const [selectedUserIds, setSelectedUserIds] = useState<string[]>([]);

  const isValid = groupName.trim().length > 0 && selectedUserIds.length >= 2;

  function handleSubmit(event: FormEvent<HTMLFormElement>): void {
    event.preventDefault();

    if (!isValid) {
      return;
    }

    onCreateGroup({
      name: groupName.trim(),
      participantIds: selectedUserIds,
    });
    setGroupName("");
    setSelectedUserIds([]);
  }

  function handleClose(): void {
    setGroupName("");
    setSelectedUserIds([]);
    onClose();
  }

  if (!isOpen) {
    return null;
  }

  return (
    <div className="new-group-modal" role="dialog" aria-modal="true" aria-labelledby="new-group-title">
      <div className="new-group-modal__panel">
        <div className="new-group-modal__header">
          <div>
            <p>Create conversation</p>
            <h2 id="new-group-title">New group</h2>
          </div>
          <button aria-label="Close modal" className="new-group-modal__close" onClick={handleClose} type="button">
            x
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <label className="new-group-modal__field">
            <span>Group name</span>
            <input
              value={groupName}
              onChange={(event) => setGroupName(event.target.value)}
              placeholder="Weekend plans"
            />
          </label>

          {isLoadingUsers ? (
            <div className="new-group-modal__status">Loading users...</div>
          ) : error ? (
            <div className="new-group-modal__error">{error}</div>
          ) : (
            <UserPicker users={users} selectedUserIds={selectedUserIds} onChange={setSelectedUserIds} />
          )}

          <div className="new-group-modal__actions">
            <button className="new-group-modal__cancel" onClick={handleClose} type="button">
              Cancel
            </button>
            <button className="new-group-modal__create" disabled={!isValid} type="submit">
              Create group
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
