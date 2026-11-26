import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/useAuth";

function Navbar() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const displayName = user?.name || user?.email || "User";
  const roleLabel = user?.role === "ADMIN" ? "Admin" : "User";

  async function handleLogout() {
    await logout();
    navigate("/login", { replace: true });
  }

  return (
    <header className="navbar">
      <div className="navbar-left">
        <div className="logo">AF</div>
        <span className="brand">AgroFlow</span>

        <nav className="nav-links">
          <Link to="/" className="active-link">Farms</Link>
        </nav>
      </div>

      <div className="navbar-right">
        <div className="navbar-user">
          <div className="avatar">{displayName.charAt(0).toUpperCase()}</div>
          <div className="navbar-user-copy">
            <span>{displayName}</span>
            <small className={`role-badge ${user?.role === "ADMIN" ? "admin" : "user"}`}>
              {roleLabel}
            </small>
          </div>
        </div>
        <button type="button" className="logout-btn" onClick={handleLogout}>
          Logout
        </button>
      </div>
    </header>
  );
}

export default Navbar;
