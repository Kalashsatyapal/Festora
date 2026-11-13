import { Link } from "react-router-dom";
import { CalendarDays, MapPin } from "lucide-react";

export default function EventCard({ event }) {
  const date = new Date(event.startAt);

  return (
    <article className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-lg">
      <div className="flex h-36 items-end bg-linear-to-br from-indigo-700 via-violet-600 to-fuchsia-500 p-5">
        <span className="rounded-full bg-white/20 px-3 py-1 text-xs font-bold text-white backdrop-blur">
          {event.categoryName || "Event"}
        </span>
      </div>

      <div className="p-5">
        <h3 className="line-clamp-1 text-lg font-bold text-slate-900">{event.title}</h3>
        <p className="mt-2 line-clamp-2 min-h-10 text-sm text-slate-500">{event.description}</p>

        <div className="mt-4 space-y-2 text-sm text-slate-600">
          <p className="flex items-center gap-2">
            <CalendarDays size={16} />
            {date.toLocaleString()}
          </p>
          <p className="flex items-center gap-2">
            <MapPin size={16} />
            {event.venueName}, {event.venueCity}
          </p>
        </div>

        <div className="mt-5 flex items-center justify-between border-t border-slate-100 pt-4">
          <span className="font-bold text-indigo-700">₹{event.ticketPrice}</span>
          <Link to={`/events/${event.id}`} className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white">
            View event
          </Link>
        </div>
      </div>
    </article>
  );
}