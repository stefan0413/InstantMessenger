import { useAuth } from "../context/AuthContext";

export default function UserStatus() {
  const { user, isAuthenticated, logout } = useAuth();

  if (!isAuthenticated || !user) {
    return null;
  }

  return (
    <div className="user-status">
      <div className="user-status__avatar">{user.username.slice(0, 1).toUpperCase()}</div>
      <div className="user-status__copy">
        <strong>{user.username}</strong>
        <span>{user.email}</span>
      </div>
      <button onClick={logout} type="button">
        Logout
      </button>
    </div>
  );
}
