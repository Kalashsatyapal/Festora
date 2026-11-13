import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { CalendarDays, MapPin, Ticket } from "lucide-react";
import api from "../api/client";
import { useAuth } from "../context/AuthContext";
import Loading from "../components/Loading";

export default function EventDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [event, setEvent] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    api.get(`/events/${id}`)
      .then((response) => setEvent(response.data.data))
      .catch((err) => setError(err.response?.data?.message || "Unable to load event"))
      .finally(() => setLoading(false));
  }, [id]);

  const checkout = async () => {
    if (!user) {
      navigate("/login");
      return;
    }

    setSubmitting(true);
    setError("");

    try {
      const response = await api.post("/bookings/checkout", {
        eventId: Number(id),
        quantity: Number(quantity)
      });

      navigate("/checkout", { state: { booking: response.data.data } });
    } catch (err) {
      setError(err.response?.data?.message || "Unable to reserve tickets");
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <Loading />;
  if (!event) return <main className="mx-auto max-w-4xl px-5 py-16 text-red-600">{error || "Event not found"}</main>;

  return (
    <main className="mx-auto max-w-5xl px-5 py-12">
      <div className="rounded-3xl bg-linear-to-br from-indigo-800 to-fuchsia-700 p-10 text-white md:p-16">
        <span className="rounded-full bg-white/20 px-4 py-2 text-sm">{event.categoryName}</span>
        <h1 className="mt-7 text-4xl font-black md:text-6xl">{event.title}</h1>
        <p className="mt-5 max-w-2xl text-indigo-100">{event.description}</p>
      </div>

      <div className="mt-8 grid gap-8 md:grid-cols-[1fr_340px]">
        <section className="rounded-2xl border border-slate-200 bg-white p-7">
          <h2 className="text-xl font-bold">Event information</h2>
          <div className="mt-6 space-y-5 text-slate-600">
            <p className="flex items-center gap-3"><CalendarDays />{new Date(event.startAt).toLocaleString()}</p>
            <p className="flex items-center gap-3"><MapPin />{event.venueName}, {event.venueAddress}, {event.venueCity}</p>
            <p className="flex items-center gap-3"><Ticket />Organized by {event.organizerName}</p>
          </div>
        </section>

        <section className="h-fit rounded-2xl border border-slate-200 bg-white p-7 shadow-sm">
          <p className="text-sm text-slate-500">Ticket price</p>
          <p className="mt-1 text-3xl font-black text-indigo-700">₹{event.ticketPrice}</p>

          <label className="mt-6 block text-sm font-semibold">Quantity</label>
          <input
            type="number"
            min="1"
            max="10"
            value={quantity}
            onChange={(e) => setQuantity(Math.max(1, Math.min(10, Number(e.target.value))))}
            className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3"
          />

          <p className="mt-4 flex justify-between text-sm">
            <span>Total</span>
            <strong>₹{(Number(event.ticketPrice) * quantity).toFixed(2)}</strong>
          </p>

          {error && <p className="mt-4 text-sm text-red-600">{error}</p>}

          <button onClick={checkout} disabled={submitting} className="mt-6 w-full rounded-xl bg-indigo-600 py-3 font-bold text-white disabled:opacity-60">
            {submitting ? "Reserving..." : "Reserve tickets"}
          </button>
        </section>
      </div>
    </main>
  );
}