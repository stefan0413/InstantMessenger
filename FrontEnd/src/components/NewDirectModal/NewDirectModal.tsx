import type { User } from "../../types/user";
import "./NewDirectModal.css";

interface NewDirectModalProps {
  users: User[];
  isOpen: boolean;
  onClose: () => void;
  onCreateChat: (userId: string) => void;
}

export function NewDirectModal({ users, isOpen, onClose, onCreateChat }: NewDirectModalProps) {
  if (!isOpen) {
    return null;
  }

  return (
    <div className="new-direct-modal">
      <div className="new-direct-modal__panel">
        <div className="new-direct-modal__header">
          <h2>New chat</h2>
          <button onClick={onClose} type="button" aria-label="Close new chat">
            x
          </button>
        </div>

        <div className="new-direct-modal__users">
          {users.map((user) => (
            <button key={user.id} onClick={() => onCreateChat(user.id)} type="button">
              <img src={user.avatarUrl} alt={user.name} />
              <span>
                <strong>{user.name}</strong>
              </span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
