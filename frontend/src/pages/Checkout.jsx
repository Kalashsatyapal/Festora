
import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { Clock3, Ticket, ArrowLeft } from "lucide-react";

export default function Checkout() {
  const { state } = useLocation();
  const navigate = useNavigate();
  const booking = state?.booking;

  const [remaining, setRemaining] = useState(() => {
    if (!booking?.expiresAt) return 0;
    return Math.max(
      0,
      Math.floor((new Date(booking.expiresAt).getTime() - Date.now()) / 1000)
    );
  });

  useEffect(() => {
    if (!booking?.expiresAt) return;

    const updateCountdown = () => {
      const seconds = Math.max(
        0,
        Math.floor((new Date(booking.expiresAt).getTime() - Date.now()) / 1000)
      );
      setRemaining(seconds);
    };

    updateCountdown();

    const interval = window.setInterval(updateCountdown, 1000);

    return () => window.clearInterval(interval);
  }, [booking?.expiresAt]);

  if (!booking) {
    return (
      <main className="mx-auto max-w-2xl px-5 py-16">
        <h1 className="text-2xl font-bold">No active checkout</h1>
        <button
          onClick={() => navigate("/events")}
          className="mt-5 rounded-lg bg-indigo-600 px-5 py-3 text-white"
        >
          Browse events
        </button>
      </main>
    );
  }

  const minutes = Math.floor(remaining / 60);
  const seconds = remaining % 60;
  const expired = remaining <= 0;

  return (
    <main className="mx-auto max-w-2xl px-5 py-16">
      <div className="rounded-3xl border border-slate-200 bg-white p-8 shadow-lg">
        <div className="flex items-center gap-3">
          <div className="rounded-xl bg-indigo-50 p-3 text-indigo-600">
            <Ticket size={25} />
          </div>
          <div>
            <h1 className="text-3xl font-black">Your reservation</h1>
            <p className="mt-1 text-sm text-slate-500">
              Your selected tickets are temporarily reserved.
            </p>
          </div>
        </div>

        <div className={`mt-8 rounded-2xl p-5 ${expired ? "bg-red-50 text-red-700" : "bg-amber-50 text-amber-800"}`}>
          <div className="flex items-center gap-3">
            <Clock3 size={22} />
            <div>
              <p className="text-sm font-semibold">
                {expired ? "Reservation time expired" : "Complete your reservation before"}
              </p>
              <p className="mt-1 text-3xl font-black tabular-nums">
                {String(minutes).padStart(2, "0")}:{String(seconds).padStart(2, "0")}
              </p>
            </div>
          </div>
          <p className="mt-3 text-sm">
            {expired
              ? "Your reservation window has ended. The backend will release expired seats."
              : "Your reservation expires automatically when the timer reaches zero."}
          </p>
        </div>

        <div className="mt-8 space-y-4 rounded-xl bg-slate-50 p-5">
          <div className="flex justify-between gap-4">
            <span className="text-slate-500">Booking reference</span>
            <strong className="break-all text-right">
              {booking.bookingReference}
            </strong>
          </div>

          <div className="flex justify-between">
            <span className="text-slate-500">Tickets</span>
            <strong>{booking.quantity}</strong>
          </div>

          <div className="flex justify-between gap-4">
            <span className="text-slate-500">Selected seats</span>
            <strong className="text-right">
              {booking.seatNumbers?.join(", ") || "Not available"}
            </strong>
          </div>

          <div className="flex justify-between border-t border-slate-200 pt-4 text-lg">
            <span className="font-semibold">Total</span>
            <strong className="text-indigo-700">
              ₹{Number(booking.totalAmount).toFixed(2)}
            </strong>
          </div>

          <div className="flex justify-between gap-4">
            <span className="text-slate-500">Reservation expires</span>
            <strong className="text-right">
              {new Date(booking.expiresAt).toLocaleString()}
            </strong>
          </div>
        </div>

        <div className="mt-6 rounded-xl bg-slate-100 p-4 text-sm text-slate-600">
          Payment provider integration is not enabled yet. This reservation is not a confirmed or paid booking.
        </div>

        <button
          onClick={() => navigate("/events")}
          className="mt-7 flex w-full items-center justify-center gap-2 rounded-xl bg-indigo-600 py-3 font-bold text-white hover:bg-indigo-700"
        >
          <ArrowLeft size={17} />
          Continue exploring
        </button>
      </div>
    </main>
  );
}
