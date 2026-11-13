import { useEffect, useState } from "react";
import api from "../api/client";
import EventCard from "../components/EventCard";
import Loading from "../components/Loading";
import EmptyState from "../components/EmptyState";

export default function Events() {
  const [events, setEvents] = useState([]);
  const [keyword, setKeyword] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;

    api.get("/events")
      .then((response) => {
        if (active) {
          const data = response.data.data;
          setEvents(Array.isArray(data) ? data : data?.content || []);
        }
      })
      .catch((err) => {
        if (active) setError(err.response?.data?.message || "Unable to load events");
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, []);

  const filtered = events.filter((event) =>
    `${event.title} ${event.description} ${event.categoryName} ${event.venueCity}`
      .toLowerCase()
      .includes(keyword.toLowerCase())
  );

  return (
    <main className="mx-auto max-w-7xl px-5 py-12">
      <div className="flex flex-col justify-between gap-6 md:flex-row md:items-end">
        <div>
          <p className="font-semibold text-indigo-600">Discover experiences</p>
          <h1 className="mt-2 text-4xl font-black text-slate-900">Explore events</h1>
          <p className="mt-2 text-slate-500">Find your next unforgettable experience.</p>
        </div>

        <input
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="Search events..."
          className="w-full rounded-xl border border-slate-300 bg-white px-4 py-3 md:max-w-sm"
        />
      </div>

      {loading ? <Loading /> : error ? (
        <p className="mt-10 rounded-xl bg-red-50 p-5 text-red-700">{error}</p>
      ) : filtered.length === 0 ? (
        <div className="mt-10">
          <EmptyState title="No events found" message="Try another search term or check again later." />
        </div>
      ) : (
        <div className="mt-10 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {filtered.map((event) => <EventCard key={event.id} event={event} />)}
        </div>
      )}
    </main>
  );
}