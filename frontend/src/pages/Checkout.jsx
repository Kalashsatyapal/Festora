import { useLocation, useNavigate } from "react-router-dom";

export default function Checkout() {
  const { state } = useLocation();
  const navigate = useNavigate();
  const booking = state?.booking;

  if (!booking) {
    return (
      <main className="mx-auto max-w-2xl px-5 py-16">
        <h1 className="text-2xl font-bold">No active checkout</h1>
        <button onClick={() => navigate("/events")} className="mt-5 rounded-lg bg-indigo-600 px-5 py-3 text-white">
          Browse events
        </button>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-2xl px-5 py-16">
      <div className="rounded-3xl border border-slate-200 bg-white p-8 shadow-lg">
        <h1 className="text-3xl font-black">Reservation created</h1>
        <p className="mt-2 text-slate-500">Your tickets are temporarily reserved.</p>

        <div className="mt-8 space-y-4 rounded-xl bg-slate-50 p-5">
          <div className="flex justify-between gap-4">
            <span className="text-slate-500">Booking reference</span>
            <strong className="break-all text-right">{booking.bookingReference}</strong>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-500">Tickets</span>
            <strong>{booking.quantity}</strong>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-500">Seats</span>
            <strong>{booking.seatNumbers?.join(", ")}</strong>
          </div>
          <div className="flex justify-between border-t border-slate-200 pt-4 text-lg">
            <span className="font-semibold">Total</span>
            <strong className="text-indigo-700">₹{booking.totalAmount}</strong>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-500">Reservation expires</span>
            <strong>{new Date(booking.expiresAt).toLocaleString()}</strong>
          </div>
        </div>

        <div className="mt-6 rounded-xl bg-amber-50 p-4 text-sm text-amber-800">
          Payment provider integration is not enabled yet. Your reservation will expire automatically if it is not confirmed through a payment flow.
        </div>

        <button onClick={() => navigate("/events")} className="mt-7 w-full rounded-xl bg-indigo-600 py-3 font-bold text-white">
          Continue exploring
        </button>
      </div>
    </main>
  );
}