import { Route, Routes } from "react-router-dom";
import Navbar from "./components/Navbar";
import ProtectedRoute from "./components/ProtectedRoute";
import { AuthProvider } from "./context/AuthContext";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Events from "./pages/Events";
import EventDetails from "./pages/EventDetails";
import Checkout from "./pages/Checkout";
import AdminDashboard from "./pages/AdminDashboard";
import OrganizerDashboard from "./pages/OrganizerDashboard";
import UserDashboard from "./pages/UserDashboard";
import OrganizerTicketScanner from "./pages/OrganizerTicketScanner";
import OrganizerEvents from "./pages/OrganizerEvents";
import OrganizerAnalytics from "./pages/OrganizerAnalytics";
import AdminManagement from "./pages/AdminManagement";
function NotFound() {
  return (
    <main className="mx-auto max-w-4xl px-5 py-24 text-center">
      <h1 className="text-5xl font-black">404</h1>
      <p className="mt-3 text-slate-500">
        The page you requested could not be found.
      </p>
    </main>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <div className="min-h-screen">
        <Navbar />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/events" element={<Events />} />
          <Route path="/events/:id" element={<EventDetails />} />
          <Route element={<ProtectedRoute roles={["USER"]} />}>
            <Route path="/checkout" element={<Checkout />} />
            <Route path="/dashboard" element={<UserDashboard />} />
            <Route path="/my-bookings" element={<UserDashboard />} />
          </Route>

          <Route element={<ProtectedRoute roles={["ORGANIZER"]} />}>
            <Route path="/organizer" element={<OrganizerDashboard />} />
            <Route
              path="/organizer/ticket-scanner"
              element={<OrganizerTicketScanner />}
            />
            <Route
              path="/organizer/analytics"
              element={<OrganizerAnalytics />}
            />
            <Route path="/organizer/events" element={<OrganizerEvents />} />
          </Route>

          <Route element={<ProtectedRoute roles={["ADMIN"]} />}>
            <Route path="/admin" element={<AdminDashboard />} />
             <Route path="/admin/management" element={<AdminManagement />} />
          </Route>

          <Route path="*" element={<NotFound />} />
        </Routes>
      </div>
    </AuthProvider>
  );
}
