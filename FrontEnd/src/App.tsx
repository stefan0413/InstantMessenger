import { useState } from "react";
import LoginForm from "./features/auth/components/LoginForm";
import RegisterForm from "./features/auth/components/RegisterForm";
import UserStatus from "./features/auth/components/UserStatus";
import { useAuth } from "./features/auth/context/AuthContext";

function App() {
  const { isAuthenticated } = useAuth();
  const [mode, setMode] = useState<"login" | "register">("login");

  return (
    <div>
      <UserStatus />

      <hr />

      {!isAuthenticated && (
        <>
          <button onClick={() => setMode("login")}>Login</button>
          <button onClick={() => setMode("register")}>Register</button>

          <hr />

          {mode === "login" ? <LoginForm /> : <RegisterForm />}
        </>
      )}
    </div>
  );
}

export default App;