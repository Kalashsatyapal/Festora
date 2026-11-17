import { useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { CalendarDays, LogOut, Menu, X } from "lucide-react";
import { useAuth } from "../context/AuthContext";
export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);
  const signOut = () => {
    setMenuOpen(false);
    logout();
    navigate("/");
  };
  const linkClass = ({ isActive }) =>
    `transition hover:text-indigo-700 ${
      isActive ? "font-bold text-indigo-700" : "text-slate-600"
    }`;
  const closeMenu = () => setMenuOpen(false);
  return (
    <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/95 backdrop-blur">
      <nav className="mx-auto flex max-w-7xl items-center justify-between px-5 py-4">
        <Link
          to="/"
          onClick={closeMenu}
          className="flex items-center gap-2 text-2xl font-black tracking-tight text-indigo-700"
        >
          <CalendarDays size={25} />
          Festora
        </Link>
        <div className="hidden items-center gap-6 text-sm font-semibold md:flex">
          <NavLink to="/events" className={linkClass}>
            Explore events
          </NavLink>
          {user?.role === "USER" && (
            <>
              <NavLink to="/dashboard" className={linkClass}>
                Dashboard
              </NavLink>
              <NavLink to="/my-bookings" className={linkClass}>
                My bookings
              </NavLink>
            </>
          )}
          {user?.role === "ORGANIZER" && (
            <NavLink to="/organizer" className={linkClass}>
              Organizer
            </NavLink>
          )}

          {user?.role === "ADMIN" && (
            <NavLink to="/admin" className={linkClass}>
              Admin
            </NavLink>
          )}

          {user ? (
            <>
              <NavLink to="/profile" className={linkClass}>
                My Profile
              </NavLink>
              <button
                onClick={signOut}
                className="flex items-center gap-2 rounded-lg bg-slate-100 px-4 py-2 text-slate-800 hover:bg-slate-200"
              >
                <LogOut size={16} />
                Sign out
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" className={linkClass}>
                Login
              </NavLink>
              <Link
                to="/register"
                className="rounded-lg bg-indigo-600 px-4 py-2 text-white hover:bg-indigo-700"
              >
                Get started
              </Link>
            </>
          )}
        </div>

        <button
          type="button"
          onClick={() => setMenuOpen((open) => !open)}
          aria-label={menuOpen ? "Close navigation" : "Open navigation"}
          aria-expanded={menuOpen}
          className="rounded-lg p-2 text-slate-700 hover:bg-slate-100 md:hidden"
        >
          {menuOpen ? <X size={24} /> : <Menu size={24} />}
        </button>
      </nav>

      {menuOpen && (
        <div className="border-t border-slate-200 bg-white px-5 py-5 shadow-lg md:hidden">
          <div className="flex flex-col gap-4 text-sm font-semibold">
            <NavLink to="/events" onClick={closeMenu} className={linkClass}>
              Explore events
            </NavLink>

            {user?.role === "USER" && (
              <>
                <NavLink
                  to="/dashboard"
                  onClick={closeMenu}
                  className={linkClass}
                >
                  Dashboard
                </NavLink>
                <NavLink
                  to="/my-bookings"
                  onClick={closeMenu}
                  className={linkClass}
                >
                  My bookings
                </NavLink>
              </>
            )}

            {user?.role === "ORGANIZER" && (
              <NavLink
                to="/organizer"
                onClick={closeMenu}
                className={linkClass}
              >
                Organizer
              </NavLink>
            )}

            {user?.role === "ADMIN" && (
              <NavLink to="/admin" onClick={closeMenu} className={linkClass}>
                Admin
              </NavLink>
            )}

            {user ? (
              <>
                <NavLink to="/profile" onClick={closeMenu} className={linkClass}>
                  My Profile
                </NavLink>
                <button
                  onClick={signOut}
                  className="flex items-center gap-2 self-start rounded-lg bg-slate-100 px-4 py-2 text-slate-800"
                >
                  <LogOut size={16} />
                  Sign out
                </button>
              </>
            ) : (
              <div className="flex items-center gap-4">
                <NavLink to="/login" onClick={closeMenu} className={linkClass}>
                  Login
                </NavLink>
                <Link
                  to="/register"
                  onClick={closeMenu}
                  className="rounded-lg bg-indigo-600 px-4 py-2 text-white"
                >
                  Get started
                </Link>
              </div>
            )}
          </div>
        </div>
      )}
    </header>
  );
}