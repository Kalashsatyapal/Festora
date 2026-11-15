
import { useCallback, useEffect, useState } from "react";
import { Search, SlidersHorizontal, X } from "lucide-react";
import api from "../api/client";
import EventCard from "../components/EventCard";
import Loading from "../components/Loading";
import EmptyState from "../components/EmptyState";

const initialFilters = {
  keyword: "",
  categoryId: "",
  startFrom: "",
  startTo: ""
};

export default function Events() {
  const [filters, setFilters] = useState(initialFilters);
  const [appliedFilters, setAppliedFilters] = useState(initialFilters);
  const [categories, setCategories] = useState([]);
  const [events, setEvents] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    api.get("/categories")
      .then((response) => {
        const data = response.data.data;
        setCategories(Array.isArray(data) ? data : []);
      })
      .catch(() => setCategories([]));
  }, []);

  const loadEvents = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const params = { page, size: 9 };

      if (appliedFilters.keyword.trim()) {
        params.keyword = appliedFilters.keyword.trim();
      }

      if (appliedFilters.categoryId) {
        params.categoryId = Number(appliedFilters.categoryId);
      }

      if (appliedFilters.startFrom) {
        params.startFrom = new Date(appliedFilters.startFrom).toISOString();
      }

      if (appliedFilters.startTo) {
        params.startTo = new Date(appliedFilters.startTo).toISOString();
      }

      const response = await api.get("/events", { params });
      const data = response.data.data;

      setEvents(data?.content || []);
      setTotalPages(data?.totalPages || 0);
      setTotalElements(data?.totalElements || 0);
    } catch (err) {
      setError(err.response?.data?.message || "Unable to load events");
      setEvents([]);
    } finally {
      setLoading(false);
    }
  }, [appliedFilters, page]);

  useEffect(() => {
    loadEvents();
  }, [loadEvents]);

  const updateFilter = (name, value) => {
    setFilters((current) => ({ ...current, [name]: value }));
  };

  const searchEvents = (event) => {
    event.preventDefault();

    if (
      filters.startFrom &&
      filters.startTo &&
      new Date(filters.startTo) < new Date(filters.startFrom)
    ) {
      setError("End date cannot be earlier than start date.");
      return;
    }

    setPage(0);
    setAppliedFilters(filters);
  };

  const clearFilters = () => {
    setFilters(initialFilters);
    setPage(0);
    setAppliedFilters(initialFilters);
  };

  return (
    <main className="mx-auto max-w-7xl px-5 py-12">
      <section className="rounded-3xl bg-linear-to-br from-slate-950 via-indigo-950 to-indigo-800 px-7 py-12 text-white md:px-12">
        <p className="text-sm font-bold uppercase tracking-[0.2em] text-indigo-200">
          Discover experiences
        </p>
        <h1 className="mt-4 text-4xl font-black md:text-5xl">
          Find your next experience.
        </h1>
        <p className="mt-4 max-w-2xl text-indigo-100">
          Explore concerts, festivals, workshops, and unforgettable events.
        </p>
      </section>

      <form
        onSubmit={searchEvents}
        className="mt-8 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
      >
        <div className="mb-5 flex items-center gap-2 font-bold text-slate-900">
          <SlidersHorizontal size={19} />
          Find events
        </div>

        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <div className="relative">
            <Search className="absolute left-3 top-3.5 text-slate-400" size={18} />
            <input
              value={filters.keyword}
              onChange={(event) => updateFilter("keyword", event.target.value)}
              placeholder="Search events..."
              className="w-full rounded-xl border border-slate-300 py-3 pl-10 pr-3 outline-none focus:border-indigo-500"
            />
          </div>

          <select
            value={filters.categoryId}
            onChange={(event) => updateFilter("categoryId", event.target.value)}
            className="rounded-xl border border-slate-300 px-3 py-3 outline-none focus:border-indigo-500"
          >
            <option value="">All categories</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>

          <input
            type="datetime-local"
            value={filters.startFrom}
            onChange={(event) => updateFilter("startFrom", event.target.value)}
            className="min-w-0 rounded-xl border border-slate-300 px-3 py-3 outline-none focus:border-indigo-500"
            aria-label="Events starting from"
          />

          <input
            type="datetime-local"
            value={filters.startTo}
            onChange={(event) => updateFilter("startTo", event.target.value)}
            className="min-w-0 rounded-xl border border-slate-300 px-3 py-3 outline-none focus:border-indigo-500"
            aria-label="Events ending by"
          />
        </div>

        <div className="mt-5 flex flex-wrap gap-3">
          <button
            type="submit"
            className="rounded-xl bg-indigo-600 px-6 py-3 font-bold text-white transition hover:bg-indigo-700"
          >
            Search events
          </button>
          <button
            type="button"
            onClick={clearFilters}
            className="flex items-center gap-2 rounded-xl border border-slate-300 px-5 py-3 font-semibold text-slate-700 hover:bg-slate-50"
          >
            <X size={17} />
            Clear filters
          </button>
        </div>
      </form>

      <div className="mt-10 flex flex-wrap items-end justify-between gap-3">
        <div>
          <h2 className="text-2xl font-black text-slate-900">Upcoming events</h2>
          <p className="mt-1 text-sm text-slate-500">
            {totalElements} event{totalElements === 1 ? "" : "s"} found
          </p>
        </div>
        {totalPages > 0 && (
          <p className="text-sm text-slate-500">
            Page {page + 1} of {totalPages}
          </p>
        )}
      </div>

      {loading ? (
        <Loading />
      ) : error ? (
        <p className="mt-8 rounded-xl bg-red-50 p-5 text-red-700">{error}</p>
      ) : events.length === 0 ? (
        <div className="mt-8">
          <EmptyState
            title="No events found"
            message="Try changing your search or date filters."
          />
        </div>
      ) : (
        <>
          <div className="mt-7 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {events.map((event) => (
              <EventCard key={event.id} event={event} />
            ))}
          </div>

          <div className="mt-10 flex items-center justify-center gap-4">
            <button
              type="button"
              disabled={page === 0}
              onClick={() => setPage((current) => current - 1)}
              className="rounded-xl border border-slate-300 px-5 py-3 font-semibold disabled:cursor-not-allowed disabled:opacity-40"
            >
              Previous
            </button>
            <span className="text-sm font-semibold text-slate-600">
              {page + 1} / {Math.max(totalPages, 1)}
            </span>
            <button
              type="button"
              disabled={page + 1 >= totalPages}
              onClick={() => setPage((current) => current + 1)}
              className="rounded-xl border border-slate-300 px-5 py-3 font-semibold disabled:cursor-not-allowed disabled:opacity-40"
            >
              Next
            </button>
          </div>
        </>
      )}
    </main>
  );
}
