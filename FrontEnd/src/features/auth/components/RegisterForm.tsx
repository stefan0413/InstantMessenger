import { useState } from "react";
import { useAuth } from "../context/AuthContext";

export default function RegisterForm() {
  const { register } = useAuth();

  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError("");

    if (username.trim().length < 3) {
      setError("Username must be at least 3 characters");
      return;
    }

    if (!email.includes("@")) {
      setError("Enter a valid email address");
      return;
    }

    if (password.length < 8) {
      setError("Password must be at least 8 characters");
      return;
    }

    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    setIsSubmitting(true);

    try {
      await register({ username: username.trim(), email: email.trim(), password, confirmPassword });
    } catch (error) {
      setError(error instanceof Error ? error.message : "Registration failed. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="auth-form" onSubmit={handleSubmit}>
      <div className="auth-form__header">
        <p>New account</p>
        <h2>Create your workspace</h2>
      </div>

      {error && <p className="auth-form__error">{error}</p>}

      <label className="auth-form__field">
        <span>Username</span>
        <input
          type="text"
          placeholder="test"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />
      </label>

      <label className="auth-form__field">
        <span>Email</span>
        <input
          type="email"
          placeholder="you@example.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
      </label>

      <label className="auth-form__field">
        <span>Password</span>
        <input
          type="password"
          placeholder="Choose a password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
      </label>

      <label className="auth-form__field">
        <span>Confirm password</span>
        <input
          type="password"
          placeholder="Repeat your password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
        />
      </label>

      <button className="auth-form__submit" disabled={isSubmitting} type="submit">
        {isSubmitting ? "Creating..." : "Create account"}
      </button>
    </form>
  );
}
