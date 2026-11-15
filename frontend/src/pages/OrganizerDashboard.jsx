import { TrendingUp } from "lucide-react";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Plus, CalendarDays, Ticket } from "lucide-react";
import api from "../api/client";
import Loading from "../components/Loading";
import EmptyState from "../components/EmptyState";

export default function OrganizerDashboard() {
  const [events, setEvents] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .get("/organizer/events", { params: { page: 0, size: 100 } })
      .then((response) => {
        const data = response.data.data;
        setEvents(data?.content || []);
      })
      .catch((err) =>
        setError(
          err.response?.data?.message || "Unable to load organizer events",
        ),
      )
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Loading />;

  const published = events.filter(
    (event) => event.status === "PUBLISHED",
  ).length;
  const drafts = events.filter((event) => event.status === "DRAFT").length;

  return (
    <main className="mx-auto max-w-7xl px-5 py-12">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-black">Organizer dashboard</h1>
          <p className="mt-2 text-slate-500">
            Manage your events and performance.
          </p>
        </div>
        <div className="flex flex-wrap gap-3">
          <Link
            to="/organizer/events"
            className="flex items-center gap-2 rounded-xl bg-indigo-600 px-5 py-3 font-semibold text-white"
          >
            <Plus size={18} />
            Manage events
          </Link>
          <Link
            to="/events"
            className="rounded-xl border border-slate-200 px-5 py-3 font-semibold text-slate-700"
          >
            Browse events
          </Link>
          <Link
            to="/organizer/analytics"
            className="flex items-center gap-2 rounded-xl bg-emerald-600 px-5 py-3 font-semibold text-white"
          >
            <TrendingUp size={18} />
            Revenue analytics
          </Link>
        </div>
      </div>

      {error && (
        <p className="mt-8 rounded-xl bg-red-50 p-4 text-red-700">{error}</p>
      )}

      {!error && (
        <div className="mt-8 grid gap-5 sm:grid-cols-3">
          <div className="rounded-2xl border border-slate-200 bg-white p-6">
            <CalendarDays className="text-indigo-600" size={25} />
            <p className="mt-4 text-sm text-slate-500">Total events</p>
            <p className="mt-1 text-3xl font-black">{events.length}</p>
          </div>
          <div className="rounded-2xl border border-slate-200 bg-white p-6">
            <Ticket className="text-emerald-600" size={25} />
            <p className="mt-4 text-sm text-slate-500">Published events</p>
            <p className="mt-1 text-3xl font-black">{published}</p>
          </div>
          <div className="rounded-2xl border border-slate-200 bg-white p-6">
            <CalendarDays className="text-amber-600" size={25} />
            <p className="mt-4 text-sm text-slate-500">Draft events</p>
            <p className="mt-1 text-3xl font-black">{drafts}</p>
          </div>
        </div>
      )}

      {!error && events.length === 0 && (
        <div className="mt-8">
          <EmptyState
            title="No events yet"
            message="Create your first event to get started."
          />
        </div>
      )}

      {events.length > 0 && (
        <div className="mt-8 overflow-hidden rounded-2xl border border-slate-200 bg-white">
          <div className="grid grid-cols-4 gap-4 border-b bg-slate-50 px-5 py-4 text-sm font-bold text-slate-600">
            <span>Event</span>
            <span>Venue</span>
            <span>Status</span>
            <span>Start date</span>
          </div>
          {events.slice(0, 10).map((event) => (
            <div
              key={event.id}
              className="grid grid-cols-4 gap-4 border-b px-5 py-4 text-sm"
            >
              <span className="font-semibold">{event.title}</span>
              <span>{event.venueName}</span>
              <span>{event.status}</span>
              <span>{new Date(event.startAt).toLocaleDateString()}</span>
            </div>
          ))}
        </div>
      )}
    </main>
  );
}
