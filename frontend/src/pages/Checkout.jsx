
import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import {
  Clock3,
  Ticket,
  ArrowLeft,
  ShieldCheck,
  CreditCard,
  LoaderCircle,
  CheckCircle2,
  AlertCircle
} from "lucide-react";
import api from "../api/client";
import { useAuth } from "../context/AuthContext";

const RAZORPAY_SCRIPT = "https://checkout.razorpay.com/v1/checkout.js";

function loadRazorpayScript() {
  return new Promise((resolve) => {
    if (window.Razorpay) {
      resolve(true);
      return;
    }

    const existingScript = document.querySelector(
      `script[src="${RAZORPAY_SCRIPT}"]`
    );

    if (existingScript) {
      existingScript.addEventListener("load", () => resolve(true), { once: true });
      existingScript.addEventListener("error", () => resolve(false), { once: true });
      return;
    }

    const script = document.createElement("script");
    script.src = RAZORPAY_SCRIPT;
    script.async = true;
    script.onload = () => resolve(true);
    script.onerror = () => resolve(false);
    document.body.appendChild(script);
  });
}

export default function Checkout() {
  const { state } = useLocation();
  const navigate = useNavigate();
  const { user } = useAuth();
  const booking = state?.booking;

  const [remaining, setRemaining] = useState(() => {
    if (!booking?.expiresAt) return 0;

    return Math.max(
      0,
      Math.floor(
        (new Date(booking.expiresAt).getTime() - Date.now()) / 1000
      )
    );
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [paymentResult, setPaymentResult] = useState(null);

  useEffect(() => {
    if (!booking?.expiresAt || paymentResult) return;

    const updateCountdown = () => {
      const seconds = Math.max(
        0,
        Math.floor(
          (new Date(booking.expiresAt).getTime() - Date.now()) / 1000
        )
      );

      setRemaining(seconds);
    };

    updateCountdown();

    const interval = window.setInterval(updateCountdown, 1000);

    return () => window.clearInterval(interval);
  }, [booking?.expiresAt, paymentResult]);

  if (!booking) {
    return (
      <main className="mx-auto max-w-2xl px-5 py-16">
        <h1 className="text-2xl font-bold">No active checkout</h1>
        <p className="mt-2 text-slate-500">
          Your booking information is unavailable. Please select your seats again.
        </p>
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

  const verifyPayment = async (response) => {
    setLoading(true);
    setError("");

    try {
      const verification = await api.post(
        `/bookings/${booking.bookingReference}/verify-payment`,
        {
          razorpayPaymentId: response.razorpay_payment_id,
          razorpaySignature: response.razorpay_signature
        }
      );

      const result = verification.data.data;

      if (
        result?.paymentStatus !== "SUCCESS" ||
        result?.bookingStatus !== "CONFIRMED"
      ) {
        throw new Error("Payment could not be confirmed by the server.");
      }

      setPaymentResult(result);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.message ||
          "Payment verification failed. Please retry verification or contact support."
      );
    } finally {
      setLoading(false);
    }
  };

  const startPayment = async () => {
    if (loading || expired || paymentResult) return;

    setLoading(true);
    setError("");

    try {
      const scriptLoaded = await loadRazorpayScript();

      if (!scriptLoaded || !window.Razorpay) {
        throw new Error("Unable to load Razorpay Checkout. Check your internet connection.");
      }

      const orderResponse = await api.post(
        `/bookings/${booking.bookingReference}/payment-order`
      );

      const order = orderResponse.data.data;

      if (
        !order?.razorpayOrderId ||
        !order?.keyId ||
        !order?.amount ||
        !order?.currency
      ) {
        throw new Error("Invalid payment order received from the server.");
      }

      const options = {
        key: order.keyId,
        amount: order.amount,
        currency: order.currency,
        name: "Festora",
        description: "Event ticket booking",
        order_id: order.razorpayOrderId,
        prefill: {
          name: user?.fullName || "",
          email: user?.email || ""
        },
        notes: {
          booking_reference: booking.bookingReference
        },
        theme: {
          color: "#4f46e5"
        },
        handler: async (response) => {
          await verifyPayment(response);
        },
        modal: {
          ondismiss: () => {
            setLoading(false);
          }
        }
      };

      const razorpay = new window.Razorpay(options);

      razorpay.on("payment.failed", (response) => {
        setError(
          response.error?.description ||
            "Payment failed. You can retry while your reservation is active."
        );
        setLoading(false);
      });

      razorpay.open();
      setLoading(false);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.message ||
          "Unable to initiate payment."
      );
      setLoading(false);
    }
  };

  return (
    <main className="mx-auto max-w-2xl px-5 py-16">
      <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-lg sm:p-8">
        <div className="flex items-center gap-3">
          <div className="rounded-xl bg-indigo-50 p-3 text-indigo-600">
            <Ticket size={25} />
          </div>
          <div>
            <h1 className="text-3xl font-black">Your reservation</h1>
            <p className="mt-1 text-sm text-slate-500">
              Secure your tickets with Festora.
            </p>
          </div>
        </div>

        {paymentResult ? (
          <div className="mt-8">
            <div className="rounded-2xl bg-emerald-50 p-6 text-center">
              <CheckCircle2 className="mx-auto text-emerald-600" size={52} />
              <h2 className="mt-4 text-2xl font-black text-emerald-800">
                Payment successful
              </h2>
              <p className="mt-2 text-sm text-emerald-700">
                Your payment has been verified and your booking is confirmed.
              </p>
            </div>

            <div className="mt-6 space-y-4 rounded-xl bg-slate-50 p-5">
              <div className="flex justify-between gap-4">
                <span className="text-slate-500">Booking reference</span>
                <strong className="break-all text-right">
                  {paymentResult.bookingReference}
                </strong>
              </div>
              <div className="flex justify-between gap-4">
                <span className="text-slate-500">Payment ID</span>
                <strong className="break-all text-right">
                  {paymentResult.razorpayPaymentId}
                </strong>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Payment status</span>
                <strong className="text-emerald-700">
                  {paymentResult.paymentStatus}
                </strong>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Booking status</span>
                <strong className="text-emerald-700">
                  {paymentResult.bookingStatus}
                </strong>
              </div>
            </div>

            <button
              onClick={() => navigate("/my-bookings")}
              className="mt-7 w-full rounded-xl bg-indigo-600 py-3 font-bold text-white hover:bg-indigo-700"
            >
              View my bookings
            </button>
          </div>
        ) : (
          <>
            <div
              className={`mt-8 rounded-2xl p-5 ${
                expired
                  ? "bg-red-50 text-red-700"
                  : "bg-amber-50 text-amber-800"
              }`}
            >
              <div className="flex items-center gap-3">
                <Clock3 size={22} />
                <div>
                  <p className="text-sm font-semibold">
                    {expired
                      ? "Reservation time expired"
                      : "Complete payment before"}
                  </p>
                  <p className="mt-1 text-3xl font-black tabular-nums">
                    {String(minutes).padStart(2, "0")}:
                    {String(seconds).padStart(2, "0")}
                  </p>
                </div>
              </div>
              <p className="mt-3 text-sm">
                {expired
                  ? "Your reservation window has ended. Payment cannot be initiated for this reservation."
                  : "Your reserved seats are held temporarily."}
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

            {error && (
              <div className="mt-6 flex gap-3 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
                <AlertCircle className="shrink-0" size={20} />
                <p>{error}</p>
              </div>
            )}

            <div className="mt-6 flex items-center gap-3 rounded-xl bg-indigo-50 p-4 text-sm text-indigo-800">
              <ShieldCheck size={22} className="shrink-0" />
              <p>
                Payment is confirmed only after server-side Razorpay verification.
              </p>
            </div>

            <button
              onClick={startPayment}
              disabled={loading || expired}
              className="mt-7 flex w-full items-center justify-center gap-2 rounded-xl bg-indigo-600 py-4 font-bold text-white hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {loading ? (
                <>
                  <LoaderCircle size={19} className="animate-spin" />
                  Processing...
                </>
              ) : (
                <>
                  <CreditCard size={19} />
                  Pay ₹{Number(booking.totalAmount).toFixed(2)}
                </>
              )}
            </button>

            <button
              onClick={() => navigate("/events")}
              disabled={loading}
              className="mt-4 flex w-full items-center justify-center gap-2 rounded-xl border border-slate-200 py-3 font-semibold text-slate-700 hover:bg-slate-50 disabled:opacity-50"
            >
              <ArrowLeft size={17} />
              Continue exploring
            </button>
          </>
        )}
      </div>
    </main>
  );
}
