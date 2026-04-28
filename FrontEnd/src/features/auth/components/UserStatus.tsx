import { useAuth } from "../context/AuthContext";

export default function UserStatus() {
  const { user, isAuthenticated, logout } = useAuth();

  if (!isAuthenticated || !user) {
    return <p>Not logged in</p>;
  }

  return (
    <div>
      <p>Logged in as: {user.username}</p>
      <p>Email: {user.email}</p>

      <button onClick={logout}>Logout</button>
    </div>
  );
}