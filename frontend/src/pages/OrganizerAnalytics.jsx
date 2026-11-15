
import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { Download, IndianRupee, Ticket, CalendarDays, TrendingUp } from "lucide-react";
import api from "../api/client";
import Loading from "../components/Loading";

export default function OrganizerAnalytics() {
  const [events, setEvents] = useState([]);
  const [revenues, setRevenues] = useState([]);
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    const loadAnalytics = async () => {
      try {
        const response = await api.get("/organizer/events", {
          params: { page: 0, size: 100 }
        });

        const data = response.data.data;
        const eventList = Array.isArray(data) ? data : data?.content || [];
        setEvents(eventList);

        const results = await Promise.all(
          eventList.map(async (event) => {
            try {
              const revenueResponse = await api.get(
                `/organizer/events/${event.id}/revenue`
              );
              return revenueResponse.data.data;
            } catch {
              return {
                eventId: event.id,
                eventTitle: event.title,
                totalRevenue: 0,
                confirmedBookings: 0,
                ticketsSold: 0,
                currency: "INR"
              };
            }
          })
        );

        setRevenues(results);
      } catch (err) {
        setError(err.response?.data?.message || "Unable to load analytics");
      } finally {
        setLoading(false);
      }
    };

    loadAnalytics();
  }, []);

  const totals = useMemo(
    () =>
      revenues.reduce(
        (result, item) => ({
          revenue: result.revenue + Number(item.totalRevenue || 0),
          bookings: result.bookings + Number(item.confirmedBookings || 0),
          tickets: result.tickets + Number(item.ticketsSold || 0)
        }),
        { revenue: 0, bookings: 0, tickets: 0 }
      ),
    [revenues]
  );

  const exportAttendees = async (event) => {
    setExporting(event.id);
    setError("");
    setMessage("");

    try {
      const response = await api.get(
        `/organizer/events/${event.id}/attendees/export`
      );

      const csv = response.data.data.csvContent;
      const fileName =
        response.data.data.fileName || `event-${event.id}-attendees.csv`;

      const blob = new Blob(["\uFEFF", csv], {
        type: "text/csv;charset=utf-8;"
      });

      const url = window.URL.createObjectURL(blob);
      const anchor = document.createElement("a");
      anchor.href = url;
      anchor.download = fileName;
      document.body.appendChild(anchor);
      anchor.click();
      anchor.remove();
      window.URL.revokeObjectURL(url);

      setMessage(`Attendee roster downloaded for ${event.title}`);
    } catch (err) {
      setError(
        err.response?.data?.message || `Unable to export attendees for ${event.title}`
      );
    } finally {
      setExporting(null);
    }
  };

  if (loading) return <Loading />;

  const maxRevenue = Math.max(
    ...revenues.map((item) => Number(item.totalRevenue || 0)),
    1
  );

  return (
    <main className="mx-auto max-w-7xl px-5 py-10">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-black text-slate-900">
            Revenue analytics
          </h1>
          <p className="mt-2 text-slate-500">
            Track confirmed ticket sales and export attendee rosters.
          </p>
        </div>

        <div className="flex gap-3">
          <Link
            to="/organizer"
            className="rounded-xl border border-slate-200 px-5 py-3 font-semibold text-slate-700"
          >
            Dashboard
          </Link>
          <Link
            to="/organizer/events"
            className="rounded-xl bg-indigo-600 px-5 py-3 font-semibold text-white"
          >
            Manage events
          </Link>
        </div>
      </div>

      {error && (
        <div className="mt-6 rounded-xl bg-red-50 p-4 text-sm text-red-700">
          {error}
        </div>
      )}

      {message && (
        <div className="mt-6 rounded-xl bg-emerald-50 p-4 text-sm text-emerald-700">
          {message}
        </div>
      )}

      <section className="mt-8 grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
        <div className="rounded-2xl border border-slate-200 bg-white p-6">
          <IndianRupee className="text-indigo-600" size={25} />
          <p className="mt-4 text-sm text-slate-500">Confirmed revenue</p>
          <p className="mt-1 text-2xl font-black">
            ₹{totals.revenue.toLocaleString("en-IN", {
              minimumFractionDigits: 2,
              maximumFractionDigits: 2
            })}
          </p>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-6">
          <Ticket className="text-emerald-600" size={25} />
          <p className="mt-4 text-sm text-slate-500">Tickets sold</p>
          <p className="mt-1 text-2xl font-black">
            {totals.tickets.toLocaleString("en-IN")}
          </p>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-6">
          <TrendingUp className="text-amber-600" size={25} />
          <p className="mt-4 text-sm text-slate-500">Confirmed bookings</p>
          <p className="mt-1 text-2xl font-black">
            {totals.bookings.toLocaleString("en-IN")}
          </p>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-6">
          <CalendarDays className="text-violet-600" size={25} />
          <p className="mt-4 text-sm text-slate-500">Total events</p>
          <p className="mt-1 text-2xl font-black">{events.length}</p>
        </div>
      </section>

      <section className="mt-10 rounded-2xl border border-slate-200 bg-white p-6">
        <h2 className="text-xl font-black">Revenue by event</h2>
        <p className="mt-1 text-sm text-slate-500">
          Revenue is calculated from confirmed bookings only.
        </p>

        {revenues.length === 0 ? (
          <p className="py-10 text-center text-slate-500">
            No event analytics available.
          </p>
        ) : (
          <div className="mt-7 space-y-6">
            {revenues.map((item) => {
              const amount = Number(item.totalRevenue || 0);
              const percentage = (amount / maxRevenue) * 100;

              return (
                <div key={item.eventId}>
                  <div className="mb-2 flex flex-wrap justify-between gap-2">
                    <span className="font-semibold text-slate-800">
                      {item.eventTitle}
                    </span>
                    <span className="font-bold text-slate-900">
                      ₹{amount.toLocaleString("en-IN", {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 2
                      })}
                    </span>
                  </div>

                  <div className="h-3 overflow-hidden rounded-full bg-slate-100">
                    <div
                      className="h-full rounded-full bg-indigo-600"
                      style={{ width: `${percentage}%` }}
                    />
                  </div>

                  <p className="mt-2 text-xs text-slate-500">
                    {item.ticketsSold || 0} tickets · {item.confirmedBookings || 0} confirmed bookings
                  </p>
                </div>
              );
            })}
          </div>
        )}
      </section>

      <section className="mt-10">
        <div className="mb-5">
          <h2 className="text-2xl font-black">Attendee exports</h2>
          <p className="mt-1 text-sm text-slate-500">
            Download attendee details for each event as a CSV file.
          </p>
        </div>

        {events.length === 0 ? (
          <div className="rounded-2xl border border-dashed border-slate-300 bg-white p-10 text-center text-slate-500">
            Create an event before exporting attendee lists.
          </div>
        ) : (
          <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white">
            {events.map((event) => (
              <div
                key={event.id}
                className="flex flex-wrap items-center justify-between gap-4 border-b border-slate-100 p-5 last:border-0"
              >
                <div>
                  <h3 className="font-bold text-slate-900">{event.title}</h3>
                  <p className="mt-1 text-sm text-slate-500">
                    {event.status} · {event.venueName || "Venue not specified"}
                  </p>
                </div>

                <button
                  onClick={() => exportAttendees(event)}
                  disabled={exporting !== null}
                  className="flex items-center gap-2 rounded-xl bg-emerald-600 px-4 py-3 text-sm font-bold text-white disabled:opacity-50"
                >
                  <Download size={17} />
                  {exporting === event.id ? "Exporting..." : "Export CSV"}
                </button>
              </div>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}
