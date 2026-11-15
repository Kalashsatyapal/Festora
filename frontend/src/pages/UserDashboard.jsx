import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  CalendarDays,
  Ticket,
  Clock3,
  XCircle,
  QrCode,
  RefreshCw,
  ChevronLeft,
  ChevronRight
} from "lucide-react";
import api from "../api/client";

const statuses = ["ALL", "PENDING", "CONFIRMED", "CANCELLED", "EXPIRED", "REFUNDED"];

const formatDate = (value) =>
  value ? new Date(value).toLocaleString() : "Date unavailable";

const formatAmount = (value) =>
  Number(value || 0).toLocaleString("en-IN", {
    style: "currency",
    currency: "INR"
  });

const statusStyles = {
  PENDING: "bg-amber-100 text-amber-800",
  CONFIRMED: "bg-emerald-100 text-emerald-800",
  CANCELLED: "bg-red-100 text-red-800",
  EXPIRED: "bg-slate-100 text-slate-700",
  REFUNDED: "bg-blue-100 text-blue-800",
  REFUND_PENDING: "bg-orange-100 text-orange-800"
};

export default function UserDashboard() {
  const [bookings, setBookings] = useState([]);
  const [status, setStatus] = useState("ALL");
  const [page, setPage] = useState(0);
  const [pagination, setPagination] = useState({
    totalPages: 0,
    totalElements: 0,
    first: true,
    last: true
  });
  const [voucher, setVoucher] = useState(null);
  const [selectedBooking, setSelectedBooking] = useState("");
  const [loading, setLoading] = useState(true);
  const [voucherLoading, setVoucherLoading] = useState(false);
  const [error, setError] = useState("");
  const [actionMessage, setActionMessage] = useState("");

  const loadBookings = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const params = { page, size: 10 };

      if (status !== "ALL") {
        params.status = status;
      }

      const response = await api.get("/bookings", { params });
      const result = response.data.data;

      setBookings(result.content || []);
      setPagination({
        totalPages: result.totalPages || 0,
        totalElements: result.totalElements || 0,
        first: result.first,
        last: result.last
      });
    } catch (err) {
      setError(err.response?.data?.message || "Unable to load your bookings");
    } finally {
      setLoading(false);
    }
  }, [page, status]);

  useEffect(() => {
    loadBookings();
  }, [loadBookings]);

  const changeStatus = (value) => {
    setStatus(value);
    setPage(0);
    setVoucher(null);
    setSelectedBooking("");
  };

  const viewTickets = async (bookingReference) => {
    setVoucherLoading(true);
    setError("");
    setActionMessage("");
    setVoucher(null);
    setSelectedBooking(bookingReference);

    try {
      const response = await api.get(`/bookings/${bookingReference}/voucher`);
      setVoucher(response.data.data);
    } catch (err) {
      setError(err.response?.data?.message || "Unable to load ticket voucher");
      setSelectedBooking("");
    } finally {
      setVoucherLoading(false);
    }
  };

  const cancelBooking = async (bookingReference) => {
    const confirmed = window.confirm(
      "Are you sure you want to cancel this booking?"
    );

    if (!confirmed) {
      return;
    }

    setError("");
    setActionMessage("");

    try {
      const response = await api.post(`/bookings/${bookingReference}/cancel`);
      setActionMessage(response.data.message || "Booking cancelled successfully");
      setVoucher(null);
      setSelectedBooking("");
      await loadBookings();
    } catch (err) {
      setError(err.response?.data?.message || "Unable to cancel this booking");
    }
  };

  const confirmedCount = bookings.filter(
    (booking) => booking.status === "CONFIRMED"
  ).length;

  const pendingCount = bookings.filter(
    (booking) => booking.status === "PENDING"
  ).length;

  const ticketCount = bookings
    .filter((booking) => booking.status === "CONFIRMED")
    .reduce((total, booking) => total + booking.quantity, 0);

  return (
    <main className="mx-auto max-w-7xl px-5 py-10">
      <div className="flex flex-col justify-between gap-5 sm:flex-row sm:items-center">
        <div>
          <p className="text-sm font-bold uppercase tracking-widest text-indigo-600">
            Your account
          </p>
          <h1 className="mt-2 text-3xl font-black text-slate-900 sm:text-4xl">
            My Dashboard
          </h1>
          <p className="mt-2 text-slate-500">
            Manage your bookings and access your event tickets.
          </p>
        </div>

        <Link
          to="/events"
          className="inline-flex items-center justify-center gap-2 rounded-xl bg-indigo-600 px-5 py-3 font-bold text-white transition hover:bg-indigo-700"
        >
          <CalendarDays size={18} />
          Explore events
        </Link>
      </div>

      <section className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <div className="rounded-2xl border border-slate-200 bg-white p-6">
          <div className="flex items-center justify-between">
            <p className="text-sm font-semibold text-slate-500">Bookings on this page</p>
            <CalendarDays className="text-indigo-600" />
          </div>
          <p className="mt-4 text-3xl font-black">{bookings.length}</p>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-6">
          <div className="flex items-center justify-between">
            <p className="text-sm font-semibold text-slate-500">Confirmed bookings</p>
            <Ticket className="text-emerald-600" />
          </div>
          <p className="mt-4 text-3xl font-black">{confirmedCount}</p>
          <p className="mt-1 text-xs text-slate-400">On the current page</p>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-6">
          <div className="flex items-center justify-between">
            <p className="text-sm font-semibold text-slate-500">Confirmed tickets</p>
            <QrCode className="text-violet-600" />
          </div>
          <p className="mt-4 text-3xl font-black">{ticketCount}</p>
          <p className="mt-1 text-xs text-slate-400">
            {pendingCount} pending bookings on this page
          </p>
        </div>
      </section>

      <section className="mt-10">
        <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
          <div>
            <h2 className="text-2xl font-black text-slate-900">My Bookings</h2>
            <p className="mt-1 text-sm text-slate-500">
              {pagination.totalElements} booking records found
            </p>
          </div>

          <div className="flex flex-wrap gap-3">
            <select
              value={status}
              onChange={(event) => changeStatus(event.target.value)}
              className="rounded-xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold outline-none focus:border-indigo-600"
            >
              {statuses.map((item) => (
                <option key={item} value={item}>
                  {item === "ALL" ? "All bookings" : item.replaceAll("_", " ")}
                </option>
              ))}
            </select>

            <button
              onClick={loadBookings}
              disabled={loading}
              className="inline-flex items-center gap-2 rounded-xl border border-slate-300 px-4 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-50 disabled:opacity-50"
            >
              <RefreshCw size={16} />
              Refresh
            </button>
          </div>
        </div>

        {error && (
          <div className="mt-5 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
            {error}
          </div>
        )}

        {actionMessage && (
          <div className="mt-5 rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-700">
            {actionMessage}
          </div>
        )}

        {loading ? (
          <div className="py-16 text-center text-slate-500">Loading bookings...</div>
        ) : bookings.length === 0 ? (
          <div className="mt-6 rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-16 text-center">
            <Ticket className="mx-auto text-slate-300" size={42} />
            <h3 className="mt-4 text-xl font-bold text-slate-800">
              No bookings found
            </h3>
            <p className="mt-2 text-sm text-slate-500">
              Your event reservations will appear here.
            </p>
            <Link
              to="/events"
              className="mt-6 inline-flex rounded-xl bg-indigo-600 px-5 py-3 font-bold text-white"
            >
              Find an event
            </Link>
          </div>
        ) : (
          <div className="mt-6 space-y-4">
            {bookings.map((booking) => (
              <article
                key={booking.bookingReference}
                className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6"
              >
                <div className="flex flex-col justify-between gap-5 lg:flex-row lg:items-center">
                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-center gap-3">
                      <h3 className="text-lg font-bold text-slate-900">
                        {booking.eventTitle}
                      </h3>
                      <span
                        className={`rounded-full px-3 py-1 text-xs font-bold ${
                          statusStyles[booking.status] || "bg-slate-100 text-slate-700"
                        }`}
                      >
                        {booking.status.replaceAll("_", " ")}
                      </span>
                    </div>

                    <p className="mt-2 text-sm text-slate-500">
                      Booking reference:{" "}
                      <span className="break-all font-semibold text-slate-700">
                        {booking.bookingReference}
                      </span>
                    </p>

                    <div className="mt-4 grid gap-3 text-sm sm:grid-cols-3">
                      <div>
                        <p className="text-slate-400">Event date</p>
                        <p className="mt-1 font-semibold text-slate-700">
                          {formatDate(booking.eventStartAt)}
                        </p>
                      </div>
                      <div>
                        <p className="text-slate-400">Tickets</p>
                        <p className="mt-1 font-semibold text-slate-700">
                          {booking.quantity}
                        </p>
                      </div>
                      <div>
                        <p className="text-slate-400">Total amount</p>
                        <p className="mt-1 font-bold text-indigo-700">
                          {formatAmount(booking.totalAmount)}
                        </p>
                      </div>
                    </div>
                  </div>

                  <div className="flex flex-wrap gap-2 lg:justify-end">
                    {booking.status === "CONFIRMED" && (
                      <button
                        onClick={() => viewTickets(booking.bookingReference)}
                        disabled={voucherLoading && selectedBooking === booking.bookingReference}
                        className="inline-flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-3 text-sm font-bold text-white hover:bg-indigo-700 disabled:opacity-50"
                      >
                        <QrCode size={16} />
                        {voucherLoading && selectedBooking === booking.bookingReference
                          ? "Loading..."
                          : "View tickets"}
                      </button>
                    )}

                    {(booking.status === "PENDING" || booking.status === "CONFIRMED") && (
                      <button
                        onClick={() => cancelBooking(booking.bookingReference)}
                        className="inline-flex items-center gap-2 rounded-xl border border-red-200 px-4 py-3 text-sm font-bold text-red-600 hover:bg-red-50"
                      >
                        <XCircle size={16} />
                        Cancel
                      </button>
                    )}
                  </div>
                </div>
              </article>
            ))}
          </div>
        )}

        {!loading && pagination.totalPages > 1 && (
          <div className="mt-6 flex items-center justify-between">
            <button
              disabled={pagination.first}
              onClick={() => setPage((current) => Math.max(0, current - 1))}
              className="inline-flex items-center gap-2 rounded-xl border border-slate-300 px-4 py-2 text-sm font-semibold disabled:opacity-40"
            >
              <ChevronLeft size={16} />
              Previous
            </button>

            <span className="text-sm font-semibold text-slate-500">
              Page {page + 1} of {pagination.totalPages}
            </span>

            <button
              disabled={pagination.last}
              onClick={() => setPage((current) => current + 1)}
              className="inline-flex items-center gap-2 rounded-xl border border-slate-300 px-4 py-2 text-sm font-semibold disabled:opacity-40"
            >
              Next
              <ChevronRight size={16} />
            </button>
          </div>
        )}
      </section>

      {voucher && (
        <section className="mt-10 rounded-2xl border border-indigo-200 bg-indigo-50/50 p-5 sm:p-7">
          <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
            <div>
              <p className="text-sm font-bold uppercase tracking-widest text-indigo-600">
                Digital ticket
              </p>
              <h2 className="mt-1 text-2xl font-black text-slate-900">
                {voucher.title || "Your event tickets"}
              </h2>
              <p className="mt-2 break-all text-sm text-slate-500">
                Booking reference: {voucher.bookingReference}
              </p>
            </div>

            <button
              onClick={() => {
                setVoucher(null);
                setSelectedBooking("");
              }}
              className="rounded-xl border border-slate-300 bg-white px-4 py-2 text-sm font-semibold"
            >
              Close tickets
            </button>
          </div>

          <div className="mt-6 grid gap-4 md:grid-cols-2">
            {voucher.tickets?.map((ticket) => (
              <div
                key={ticket.ticketNumber}
                className="flex flex-col items-center rounded-2xl border border-slate-200 bg-white p-6 text-center"
              >
                {ticket.qrCode && (
                  <img
                    src={ticket.qrCode}
                    alt={`QR code for ticket ${ticket.ticketNumber}`}
                    className="h-48 w-48 object-contain"
                  />
                )}

                <p className="mt-4 text-lg font-black text-slate-900">
                  Seat {ticket.seatNumber}
                </p>
                <p className="mt-1 break-all text-xs text-slate-500">
                  {ticket.ticketNumber}
                </p>
                <span className="mt-3 rounded-full bg-emerald-100 px-3 py-1 text-xs font-bold text-emerald-800">
                  {ticket.status}
                </span>
              </div>
            ))}
          </div>
        </section>
      )}
    </main>
  );
}