import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/client";
import Loading from "../components/Loading";
import EmptyState from "../components/EmptyState";

export default function OrganizerDashboard() {
  const [events, setEvents] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get("/organizer/events")
      .then((response) => {
        const data = response.data.data;
        setEvents(Array.isArray(data) ? data : data?.content || []);
      })
      .catch((err) => setError(err.response?.data?.message || "Unable to load organizer events"))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Loading />;

  return (
    <main className="mx-auto max-w-7xl px-5 py-12">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-black">Organizer dashboard</h1>
          <p className="mt-2 text-slate-500">Manage your events and performance.</p>
        </div>
        <Link to="/events" className="rounded-xl bg-indigo-600 px-5 py-3 font-semibold text-white">
          Browse published events
        </Link>
      </div>

      {error && <p className="mt-8 rounded-xl bg-red-50 p-4 text-red-700">{error}</p>}

      {!error && events.length === 0 && (
        <div className="mt-8">
          <EmptyState title="No events yet" message="Your events will appear here when available." />
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
          {events.map((event) => (
            <div key={event.id} className="grid grid-cols-4 gap-4 border-b px-5 py-4 text-sm">
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