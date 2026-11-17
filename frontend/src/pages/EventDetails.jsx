
import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  CalendarDays,
  MapPin,
  Ticket,
  ChevronLeft,
  ChevronRight,
  Armchair,
  RefreshCw
} from "lucide-react";
import api from "../api/client";
import { useAuth } from "../context/AuthContext";
import Loading from "../components/Loading";

const MAX_SEATS = 10;
const REFRESH_INTERVAL = 10000;

export default function EventDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [event, setEvent] = useState(null);
  const [seats, setSeats] = useState([]);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [activeImage, setActiveImage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [seatsLoading, setSeatsLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [seatError, setSeatError] = useState("");

  const loadSeats = useCallback(async () => {
    try {
      const response = await api.get(`/events/${id}/seats`);
      const availableSeats = response.data.data || [];

      setSeats(availableSeats);

      const availableNumbers = new Set(
        availableSeats
          .filter((seat) => seat.status === "AVAILABLE")
          .map((seat) => seat.seatNumber)
      );

      setSelectedSeats((current) =>
        current.filter((seatNumber) => availableNumbers.has(seatNumber))
      );

      setSeatError("");
    } catch (err) {
      setSeatError(
        err.response?.data?.message || "Unable to load seat availability"
      );
    } finally {
      setSeatsLoading(false);
    }
  }, [id]);

  useEffect(() => {
    let active = true;

    api.get(`/events/${id}`)
      .then((response) => {
        if (active) {
          setEvent(response.data.data);
        }
      })
      .catch((err) => {
        if (active) {
          setError(err.response?.data?.message || "Unable to load event");
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [id]);

  useEffect(() => {
    loadSeats();

    const interval = window.setInterval(loadSeats, REFRESH_INTERVAL);

    return () => window.clearInterval(interval);
  }, [loadSeats]);

  const availableCount = useMemo(
    () => seats.filter((seat) => seat.status === "AVAILABLE").length,
    [seats]
  );

  const selectedSeatDetails = useMemo(
    () =>
      selectedSeats
        .map((seatNumber) =>
          seats.find((seat) => seat.seatNumber === seatNumber)
        )
        .filter(Boolean),
    [selectedSeats, seats]
  );

  const totalAmount = useMemo(
    () =>
      selectedSeatDetails.reduce(
        (total, seat) => total + Number(seat.price),
        0
      ),
    [selectedSeatDetails]
  );

  const images = event?.imageUrls || [];
  const selectedImage = images[activeImage];

  const toggleSeat = (seat) => {
    if (seat.status !== "AVAILABLE" || submitting) {
      return;
    }

    setSelectedSeats((current) => {
      if (current.includes(seat.seatNumber)) {
        return current.filter((number) => number !== seat.seatNumber);
      }

      if (current.length >= MAX_SEATS) {
        setSeatError(`You can select a maximum of ${MAX_SEATS} seats.`);
        return current;
      }

      setSeatError("");
      return [...current, seat.seatNumber];
    });
  };

  const changeImage = (direction) => {
    if (images.length === 0) return;

    setActiveImage((current) =>
      (current + direction + images.length) % images.length
    );
  };

  const checkout = async () => {
    if (!user) {
      navigate("/login");
      return;
    }

    if (selectedSeats.length === 0) {
      setSeatError("Please select at least one available seat.");
      return;
    }

    setSubmitting(true);
    setError("");
    setSeatError("");

    try {
      const response = await api.post("/bookings/checkout", {
        eventId: Number(id),
        quantity: selectedSeats.length,
        seatNumbers: selectedSeats
      });

      navigate("/checkout", {
        state: {
          booking: response.data.data
        }
      });
    } catch (err) {
      setSeatError(
        err.response?.data?.message ||
        "Unable to reserve selected seats. Please refresh availability."
      );

      await loadSeats();
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

  const getSeatStyle = (seat) => {
    if (selectedSeats.includes(seat.seatNumber)) {
      return "border-indigo-700 bg-indigo-600 text-white ring-2 ring-indigo-200";
    }

    switch (seat.status) {
      case "AVAILABLE":
        return "border-emerald-300 bg-emerald-50 text-emerald-700 hover:border-emerald-500 hover:bg-emerald-100";
      case "RESERVED":
        return "cursor-not-allowed border-amber-300 bg-amber-100 text-amber-700";
      case "SOLD":
      case "USED":
        return "cursor-not-allowed border-red-300 bg-red-100 text-red-700";
      default:
        return "cursor-not-allowed border-slate-200 bg-slate-100 text-slate-400";
    }
  };

  return (
    <main className="mx-auto max-w-7xl px-5 py-10">
      <section className="overflow-hidden rounded-3xl bg-slate-950">
        <div className="relative h-72 bg-linear-to-br from-indigo-800 to-fuchsia-700 md:h-120">
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
                className="absolute left-4 top-1/2 grid size-11 -translate-y-1/2 place-items-center rounded-full bg-black/50 text-white"
              >
                <ChevronLeft />
              </button>

              <button
                type="button"
                onClick={() => changeImage(1)}
                aria-label="Next image"
                className="absolute right-4 top-1/2 grid size-11 -translate-y-1/2 place-items-center rounded-full bg-black/50 text-white"
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
                  activeImage === index
                    ? "border-white"
                    : "border-transparent opacity-60"
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

      <div className="mt-8 grid gap-8 lg:grid-cols-[1fr_360px]">
        <div className="space-y-8">
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

          <section className="rounded-2xl border border-slate-200 bg-white p-6 md:p-8">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <h2 className="text-2xl font-black">Select your seats</h2>
                <p className="mt-1 text-sm text-slate-500">
                  Choose up to {MAX_SEATS} available seats.
                </p>
              </div>

              <button
                type="button"
                onClick={loadSeats}
                disabled={seatsLoading}
                className="flex items-center gap-2 rounded-lg border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 disabled:opacity-50"
              >
                <RefreshCw size={16} className={seatsLoading ? "animate-spin" : ""} />
                Refresh
              </button>
            </div>

            <div className="mt-7 flex flex-wrap gap-4 text-xs font-semibold">
              <div className="flex items-center gap-2">
                <span className="size-3 rounded bg-emerald-400" />
                Available
              </div>

              <div className="flex items-center gap-2">
                <span className="size-3 rounded bg-indigo-600" />
                Selected
              </div>

              <div className="flex items-center gap-2">
                <span className="size-3 rounded bg-amber-400" />
                Reserved
              </div>

              <div className="flex items-center gap-2">
                <span className="size-3 rounded bg-red-500" />
                Sold / Used
              </div>
            </div>

            <div className="mt-10 rounded-t-[50%] border-t-4 border-indigo-300 py-3 text-center text-xs font-bold tracking-[0.4em] text-slate-400">
              STAGE / SCREEN
            </div>

            {seatsLoading && seats.length === 0 ? (
              <div className="py-12 text-center text-slate-500">
                Loading seat availability...
              </div>
            ) : seats.length === 0 ? (
              <div className="py-12 text-center text-slate-500">
                No seat information is currently available.
              </div>
            ) : (
              <div className="mt-8 grid grid-cols-5 gap-2 sm:grid-cols-8 md:grid-cols-10">
                {seats.map((seat) => {
                  const selected = selectedSeats.includes(seat.seatNumber);
                  const unavailable = seat.status !== "AVAILABLE";

                  return (
                    <button
                      key={seat.seatNumber}
                      type="button"
                      disabled={unavailable || submitting}
                      onClick={() => toggleSeat(seat)}
                      title={`Seat ${seat.seatNumber} - ${seat.status}`}
                      aria-pressed={selected}
                      className={`flex min-h-16 flex-col items-center justify-center gap-1 rounded-xl border p-2 text-xs font-bold transition ${getSeatStyle(seat)}`}
                    >
                      <Armchair size={19} />
                      <span>{seat.seatNumber}</span>
                    </button>
                  );
                })}
              </div>
            )}

            <div className="mt-7 flex flex-wrap items-center justify-between gap-3 border-t border-slate-100 pt-5 text-sm">
              <span className="text-slate-500">
                {availableCount} seats currently available
              </span>

              <span className="font-semibold text-indigo-700">
                Availability refreshes every 10 seconds
              </span>
            </div>

            {seatError && (
              <div className="mt-5 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
                {seatError}
              </div>
            )}
          </section>
        </div>

        <aside className="h-fit rounded-2xl border border-slate-200 bg-white p-7 shadow-sm lg:sticky lg:top-24">
          <p className="text-sm text-slate-500">Ticket price</p>

          <p className="mt-1 text-3xl font-black text-indigo-700">
            ₹{Number(event.ticketPrice).toFixed(2)}
          </p>

          <div className="mt-7">
            <h3 className="font-bold">Selected seats</h3>

            {selectedSeats.length === 0 ? (
              <p className="mt-3 rounded-xl bg-slate-50 p-4 text-sm text-slate-500">
                Select available seats to continue.
              </p>
            ) : (
              <div className="mt-3 flex flex-wrap gap-2">
                {selectedSeats.map((seatNumber) => (
                  <span
                    key={seatNumber}
                    className="rounded-lg bg-indigo-50 px-3 py-2 text-sm font-bold text-indigo-700"
                  >
                    {seatNumber}
                  </span>
                ))}
              </div>
            )}
          </div>

          <div className="mt-6 space-y-3 border-t border-slate-100 pt-5 text-sm">
            <div className="flex justify-between">
              <span className="text-slate-500">Selected tickets</span>
              <strong>{selectedSeats.length}</strong>
            </div>

            <div className="flex justify-between">
              <span className="text-slate-500">Available seats</span>
              <strong>{availableCount}</strong>
            </div>

            <div className="flex justify-between border-t border-slate-100 pt-4 text-lg">
              <span className="font-bold">Total</span>
              <strong className="text-indigo-700">
                ₹{totalAmount.toFixed(2)}
              </strong>
            </div>
          </div>

          <button
            type="button"
            onClick={checkout}
            disabled={
              submitting ||
              selectedSeats.length === 0 ||
              seatsLoading
            }
            className="mt-7 w-full rounded-xl bg-indigo-600 py-3 font-bold text-white transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {submitting
              ? "Reserving seats..."
              : user
                ? "Reserve selected seats"
                : "Login to reserve seats"}
          </button>

          <p className="mt-4 text-center text-xs leading-5 text-slate-500">
            Selected seats are reserved for 15 minutes after successful checkout.
            Availability is confirmed by the server during reservation.
          </p>
        </aside>
      </div>
    </main>
  );
}
