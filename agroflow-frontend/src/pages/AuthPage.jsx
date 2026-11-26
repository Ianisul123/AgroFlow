import { Link, useLocation, useNavigate } from "react-router-dom";
import { useState } from "react";
import { useAuth } from "../context/useAuth";

function AuthPage({ mode }) {
  const isRegisterMode = mode === "register";
  const navigate = useNavigate();
  const location = useLocation();
  const { login, register } = useAuth();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setIsSubmitting(true);

    try {
      if (isRegisterMode) {
        await register(name, email, password);
      } else {
        await login(email, password);
      }

      const redirectTo = location.state?.from?.pathname || "/";
      navigate(redirectTo, { replace: true });
    } catch (err) {
      if (err.errors && err.errors.length > 0) {
        setError(err.errors[0].message);
      } else if (err.message) {
        setError(err.message);
      } else {
        setError("Authentication failed.");
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-card">
        <div className="auth-brand">
          <div className="auth-logo">AF</div>
          <div>
            <span>AgroFlow</span>
            <p>Irrigation management workspace</p>
          </div>
        </div>

        <div className="auth-copy">
          <h1>{isRegisterMode ? "Create your account" : "Sign in to AgroFlow"}</h1>
          <p>
            {isRegisterMode
              ? "Create an account before managing farms and irrigation zones."
              : "Use your account to access farm data and real-time updates."}
          </p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          {isRegisterMode && (
            <div className="form-group">
              <label>Name</label>
              <input
                type="text"
                value={name}
                placeholder="e.g. Farmer One"
                autoComplete="name"
                onChange={(event) => setName(event.target.value)}
              />
            </div>
          )}

          <div className="form-group">
            <label>Email address</label>
            <input
              type="email"
              value={email}
              placeholder="e.g. farmer@example.com"
              autoComplete="email"
              onChange={(event) => setEmail(event.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              value={password}
              placeholder="8-50 characters"
              autoComplete={isRegisterMode ? "new-password" : "current-password"}
              onChange={(event) => setPassword(event.target.value)}
            />
            {isRegisterMode && (
              <span className="field-hint">
                Use 8-50 characters, at least one uppercase letter, and at least one number.
              </span>
            )}
          </div>

          <button className="submit-btn auth-submit" type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Please wait..." : isRegisterMode ? "Create Account" : "Sign In"}
          </button>

          {error && <p className="error-message">{error}</p>}
        </form>

        <p className="auth-switch">
          {isRegisterMode ? "Already have an account?" : "Need an account?"} {" "}
          <Link to={isRegisterMode ? "/login" : "/register"}>
            {isRegisterMode ? "Sign in" : "Register"}
          </Link>
        </p>
      </section>
    </main>
  );
}

export default AuthPage;
