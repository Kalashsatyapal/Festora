
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { CalendarDays, MapPin, Ticket, ChevronLeft, ChevronRight } from "lucide-react";
import api from "../api/client";
import { useAuth } from "../context/AuthContext";
import Loading from "../components/Loading";

export default function EventDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [event, setEvent] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [activeImage, setActiveImage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;

    api.get(`/events/${id}`)
      .then((response) => {
        if (active) setEvent(response.data.data);
      })
      .catch((err) => {
        if (active) {
          setError(err.response?.data?.message || "Unable to load event");
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
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

  if (!event) {
    return (
      <main className="mx-auto max-w-4xl px-5 py-16 text-red-600">
        {error || "Event not found"}
      </main>
    );
  }

  const images = event.imageUrls || [];
  const selectedImage = images[activeImage];

  const changeImage = (direction) => {
    setActiveImage((current) =>
      (current + direction + images.length) % images.length
    );
  };

  return (
    <main className="mx-auto max-w-6xl px-5 py-12">
      <section className="overflow-hidden rounded-3xl bg-slate-950">
        <div className="relative h-72 bg-linear-to-br from-indigo-800 to-fuchsia-700 md:h-[480px]">
          {selectedImage && (
            <img
              src={selectedImage}
              alt={event.title}
              className="h-full w-full object-cover"
            />
          )}

          {images.length > 1 && (
            <>
              <button
                type="button"
                onClick={() => changeImage(-1)}
                aria-label="Previous image"
                className="absolute left-4 top-1/2 grid size-11 -translate-y-1/2 place-items-center rounded-full bg-black/50 text-white hover:bg-black/70"
              >
                <ChevronLeft />
              </button>
              <button
                type="button"
                onClick={() => changeImage(1)}
                aria-label="Next image"
                className="absolute right-4 top-1/2 grid size-11 -translate-y-1/2 place-items-center rounded-full bg-black/50 text-white hover:bg-black/70"
              >
                <ChevronRight />
              </button>
            </>
          )}

          <div className="absolute inset-x-0 bottom-0 bg-linear-to-t from-black/80 to-transparent p-7 text-white md:p-10">
            <span className="rounded-full bg-white/20 px-4 py-2 text-sm font-semibold backdrop-blur">
              {event.categoryName}
            </span>
            <h1 className="mt-5 text-3xl font-black md:text-5xl">
              {event.title}
            </h1>
          </div>
        </div>

        {images.length > 1 && (
          <div className="flex gap-3 overflow-x-auto p-4">
            {images.map((image, index) => (
              <button
                key={`${image}-${index}`}
                type="button"
                onClick={() => setActiveImage(index)}
                className={`h-20 w-28 shrink-0 overflow-hidden rounded-xl border-2 ${
                  activeImage === index ? "border-white" : "border-transparent opacity-60"
                }`}
              >
                <img
                  src={image}
                  alt={`${event.title} ${index + 1}`}
                  className="h-full w-full object-cover"
                />
              </button>
            ))}
          </div>
        )}
      </section>

      <div className="mt-8 grid gap-8 md:grid-cols-[1fr_340px]">
        <section className="rounded-2xl border border-slate-200 bg-white p-7">
          <h2 className="text-xl font-bold">About this event</h2>
          <p className="mt-4 whitespace-pre-line leading-7 text-slate-600">
            {event.description}
          </p>

          <div className="mt-8 space-y-5 text-slate-600">
            <p className="flex items-center gap-3">
              <CalendarDays className="shrink-0 text-indigo-600" />
              {new Date(event.startAt).toLocaleString()}
            </p>
            <p className="flex items-center gap-3">
              <MapPin className="shrink-0 text-indigo-600" />
              {event.venueName}, {event.venueAddress}, {event.venueCity}
            </p>
            <p className="flex items-center gap-3">
              <Ticket className="shrink-0 text-indigo-600" />
              Organized by {event.organizerName}
            </p>
          </div>
        </section>

        <section className="h-fit rounded-2xl border border-slate-200 bg-white p-7 shadow-sm">
          <p className="text-sm text-slate-500">Ticket price</p>
          <p className="mt-1 text-3xl font-black text-indigo-700">
            ₹{Number(event.ticketPrice).toFixed(2)}
          </p>

          <label className="mt-6 block text-sm font-semibold">Quantity</label>
          <input
            type="number"
            min="1"
            max="10"
            value={quantity}
            onChange={(inputEvent) => {
              const value = Number(inputEvent.target.value);
              setQuantity(Math.max(1, Math.min(10, value || 1)));
            }}
            className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3"
          />

          <p className="mt-4 flex justify-between text-sm">
            <span>Total</span>
            <strong>
              ₹{(Number(event.ticketPrice) * quantity).toFixed(2)}
            </strong>
          </p>

          {error && (
            <p className="mt-4 text-sm text-red-600">{error}</p>
          )}

          <button
            onClick={checkout}
            disabled={submitting}
            className="mt-6 w-full rounded-xl bg-indigo-600 py-3 font-bold text-white disabled:opacity-60"
          >
            {submitting ? "Reserving..." : "Reserve tickets"}
          </button>
        </section>
      </div>
    </main>
  );
}
