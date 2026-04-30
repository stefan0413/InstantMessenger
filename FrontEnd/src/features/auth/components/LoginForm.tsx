import { useState } from "react";
import { useAuth } from "../context/AuthContext";

export default function LoginForm() {
  const { login } = useAuth();

  const [email, setEmail] = useState("test@test.com");
  const [password, setPassword] = useState("123456");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError("");
    setIsSubmitting(true);

    try {
      await login({ email, password });
    } catch {
      setError("Invalid email or password");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="auth-form" onSubmit={handleSubmit}>
      <div className="auth-form__header">
        <p>Welcome back</p>
        <h2>Login to your inbox</h2>
      </div>

      {error && <p className="auth-form__error">{error}</p>}

      <label className="auth-form__field">
        <span>Email</span>
        <input
          type="email"
          placeholder="test@test.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
      </label>

      <label className="auth-form__field">
        <span>Password</span>
        <input
          type="password"
          placeholder="123456"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
      </label>

      <button className="auth-form__submit" disabled={isSubmitting} type="submit">
        {isSubmitting ? "Logging in..." : "Login"}
      </button>
    </form>
  );
}