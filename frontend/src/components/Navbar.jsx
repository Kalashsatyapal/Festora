import { Link, NavLink, useNavigate } from "react-router-dom";
import { CalendarDays, LogOut } from "lucide-react";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const signOut = () => {
    logout();
    navigate("/");
  };

  return (
    <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/95 backdrop-blur">
      <nav className="mx-auto flex max-w-7xl items-center justify-between px-5 py-4">
        <Link to="/" className="flex items-center gap-2 text-2xl font-black tracking-tight text-indigo-700">
          <CalendarDays size={25} />
          Festora
        </Link>

        <div className="flex items-center gap-6 text-sm font-semibold text-slate-600">
          <NavLink to="/events">Explore events</NavLink>

          {user?.role === "USER" && <NavLink to="/my-bookings">My bookings</NavLink>}
          {user?.role === "ORGANIZER" && <NavLink to="/organizer">Organizer</NavLink>}
          {user?.role === "ADMIN" && <NavLink to="/admin">Admin</NavLink>}

          {user ? (
            <button onClick={signOut} className="flex items-center gap-2 rounded-lg bg-slate-100 px-4 py-2 text-slate-800">
              <LogOut size={16} />
              Sign out
            </button>
          ) : (
            <>
              <NavLink to="/login">Login</NavLink>
              <Link to="/register" className="rounded-lg bg-indigo-600 px-4 py-2 text-white">
                Get started
              </Link>
            </>
          )}
        </div>
      </nav>
    </header>
  );
}