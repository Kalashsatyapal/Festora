
import { useCallback, useEffect, useState } from "react";
import {
  Check,
  X,
  Ban,
  UserCheck,
  Search,
  ChevronLeft,
  ChevronRight,
  RefreshCw
} from "lucide-react";
import api from "../api/client";
import Loading from "../components/Loading";

const sections = ["Organizers", "Users", "Bookings", "Refunds"];

const bookingStatuses = ["PENDING", "CONFIRMED", "CANCELLED", "EXPIRED", "REFUND_PENDING", "REFUNDED"];
const refundStatuses = ["INITIATED", "SUCCESS", "FAILED", "REFUND_PENDING", "REFUNDED"];

export default function AdminManagement() {
  const [section, setSection] = useState("Organizers");
  const [organizers, setOrganizers] = useState([]);
  const [records, setRecords] = useState([]);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [refundReason, setRefundReason] = useState({});

  const loadOrganizers = useCallback(async () => {
    const response = await api.get("/admin/organizers/pending");
    setOrganizers(response.data.data || []);
  }, []);

  const loadRecords = useCallback(async () => {
    const endpoint = section === "Users"
      ? "/admin/users"
      : section === "Bookings"
        ? "/admin/bookings"
        : "/admin/refunds";

    const params = { page, size: 20 };
    if (search.trim()) params.search = search.trim();
    if (status) params.status = status;

    const response = await api.get(endpoint, { params });
    const data = response.data.data;

    setRecords(data?.content || []);
    setTotalPages(data?.totalPages || 0);
  }, [section, page, search, status]);

  const loadData = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      if (section === "Organizers") {
        await loadOrganizers();
      } else {
        await loadRecords();
      }
    } catch (err) {
      setError(err.response?.data?.message || "Unable to load management records");
    } finally {
      setLoading(false);
    }
  }, [section, loadOrganizers, loadRecords]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const changeSection = (value) => {
    setSection(value);
    setPage(0);
    setSearch("");
    setStatus("");
    setRecords([]);
    setMessage("");
    setError("");
  };

  const runAction = async (id, action, successMessage) => {
    setBusyId(id);
    setError("");
    setMessage("");

    try {
      await action();
      setMessage(successMessage);
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Action could not be completed");
    } finally {
      setBusyId(null);
    }
  };

  const approveOrganizer = (id) =>
    runAction(
      id,
      () => api.put(`/admin/organizers/${id}/approve`),
      "Organizer approved successfully"
    );

  const rejectOrganizer = (id) => {
    if (!window.confirm("Reject this organizer application?")) return;

    runAction(
      id,
      () => api.put(`/admin/organizers/${id}/reject`),
      "Organizer rejected successfully"
    );
  };

  const changeUserStatus = (user) => {
    const suspended = user.status === "SUSPENDED";
    const action = suspended ? "reactivate" : "suspend";

    if (!window.confirm(`${action === "suspend" ? "Suspend" : "Reactivate"} ${user.fullName}?`)) return;

    runAction(
      user.id,
      () => api.patch(`/admin/users/${user.id}/${action}`),
      `User ${action}d successfully`
    );
  };

  const initiateRefund = (booking) => {
    const reason = (refundReason[booking.bookingReference] || "").trim();

    if (!reason) {
      setError("Enter a refund reason before processing.");
      return;
    }

    if (!window.confirm(`Initiate refund for booking ${booking.bookingReference}?`)) return;

    runAction(
      booking.bookingReference,
      () => api.post(`/admin/refunds/${booking.bookingReference}`, { reason }),
      "Refund request submitted"
    );
  };

  const formatAmount = (value) =>
    `₹${Number(value || 0).toLocaleString("en-IN", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    })}`;

  const formatDate = (value) =>
    value ? new Date(value).toLocaleString() : "—";

  if (loading && !organizers.length && !records.length) {
    return <Loading />;
  }

  return (
    <main className="mx-auto max-w-7xl px-5 py-10">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-black text-slate-900">Admin management</h1>
          <p className="mt-2 text-slate-500">
            Manage organizer applications, users, bookings and refunds.
          </p>
        </div>
        <button
          onClick={loadData}
          disabled={loading}
          className="flex items-center gap-2 rounded-xl border border-slate-200 px-4 py-3 font-semibold"
        >
          <RefreshCw size={17} />
          Refresh
        </button>
      </div>

      <div className="mt-8 flex flex-wrap gap-2">
        {sections.map((item) => (
          <button
            key={item}
            onClick={() => changeSection(item)}
            className={`rounded-xl px-5 py-3 text-sm font-bold ${
              section === item
                ? "bg-indigo-600 text-white"
                : "border border-slate-200 bg-white text-slate-600"
            }`}
          >
            {item}
          </button>
        ))}
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

      {section !== "Organizers" && (
        <div className="mt-7 flex flex-wrap gap-3 rounded-2xl border border-slate-200 bg-white p-4">
          <div className="flex min-w-64 flex-1 items-center gap-2 rounded-xl border border-slate-200 px-3">
            <Search size={18} className="text-slate-400" />
            <input
              value={search}
              onChange={(event) => {
                setSearch(event.target.value);
                setPage(0);
              }}
              placeholder="Search records"
              className="w-full py-3 outline-none"
            />
          </div>

          {(section === "Bookings" || section === "Refunds") && (
            <select
              value={status}
              onChange={(event) => {
                setStatus(event.target.value);
                setPage(0);
              }}
              className="rounded-xl border border-slate-200 px-4 py-3"
            >
              <option value="">All statuses</option>
              {(section === "Bookings" ? bookingStatuses : refundStatuses).map((item) => (
                <option key={item} value={item}>{item}</option>
              ))}
            </select>
          )}

          <button
            onClick={() => {
              setPage(0);
              loadData();
            }}
            className="rounded-xl bg-indigo-600 px-5 py-3 font-semibold text-white"
          >
            Apply filters
          </button>
        </div>
      )}

      {loading ? (
        <div className="py-12"><Loading /></div>
      ) : section === "Organizers" ? (
        <section className="mt-7 space-y-4">
          {organizers.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-slate-300 bg-white p-12 text-center text-slate-500">
              No pending organizer applications.
            </div>
          ) : organizers.map((organizer) => (
            <article key={organizer.organizerId} className="flex flex-wrap items-center justify-between gap-5 rounded-2xl border border-slate-200 bg-white p-6">
              <div>
                <h3 className="font-bold text-slate-900">{organizer.fullName}</h3>
                <p className="mt-1 text-sm text-slate-500">{organizer.email}</p>
                <p className="mt-2 text-xs text-slate-400">
                  Applied {formatDate(organizer.createdAt)}
                </p>
              </div>
              <div className="flex gap-2">
                <button
                  disabled={busyId === organizer.organizerId}
                  onClick={() => approveOrganizer(organizer.organizerId)}
                  className="flex items-center gap-2 rounded-lg bg-emerald-600 px-4 py-2 font-semibold text-white disabled:opacity-50"
                >
                  <Check size={16} /> Approve
                </button>
                <button
                  disabled={busyId === organizer.organizerId}
                  onClick={() => rejectOrganizer(organizer.organizerId)}
                  className="flex items-center gap-2 rounded-lg bg-red-50 px-4 py-2 font-semibold text-red-700 disabled:opacity-50"
                >
                  <X size={16} /> Reject
                </button>
              </div>
            </article>
          ))}
        </section>
      ) : section === "Users" ? (
        <section className="mt-7 overflow-x-auto rounded-2xl border border-slate-200 bg-white">
          <table className="w-full min-w-[850px] text-left text-sm">
            <thead className="bg-slate-50 text-slate-500">
              <tr>
                <th className="p-4">User</th>
                <th className="p-4">Role</th>
                <th className="p-4">Status</th>
                <th className="p-4">Joined</th>
                <th className="p-4">Action</th>
              </tr>
            </thead>
            <tbody>
              {records.map((user) => (
                <tr key={user.id} className="border-t border-slate-100">
                  <td className="p-4">
                    <p className="font-bold">{user.fullName}</p>
                    <p className="text-slate-500">{user.email}</p>
                  </td>
                  <td className="p-4">{user.role}</td>
                  <td className="p-4">{user.status}</td>
                  <td className="p-4">{formatDate(user.createdAt)}</td>
                  <td className="p-4">
                    {user.role !== "ADMIN" && (
                      <button
                        disabled={busyId === user.id}
                        onClick={() => changeUserStatus(user)}
                        className={`flex items-center gap-2 rounded-lg px-3 py-2 font-semibold ${
                          user.status === "SUSPENDED"
                            ? "bg-emerald-50 text-emerald-700"
                            : "bg-red-50 text-red-700"
                        }`}
                      >
                        {user.status === "SUSPENDED"
                          ? <><UserCheck size={15} /> Reactivate</>
                          : <><Ban size={15} /> Suspend</>}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {records.length === 0 && (
            <p className="p-10 text-center text-slate-500">No users found.</p>
          )}
        </section>
      ) : section === "Bookings" ? (
        <section className="mt-7 space-y-4">
          {records.map((booking) => (
            <article key={booking.id} className="rounded-2xl border border-slate-200 bg-white p-6">
              <div className="flex flex-wrap justify-between gap-4">
                <div>
                  <h3 className="font-bold">{booking.eventTitle}</h3>
                  <p className="mt-1 text-sm text-slate-500">
                    {booking.customerName} · {booking.customerEmail}
                  </p>
                  <p className="mt-2 break-all text-xs text-slate-400">
                    {booking.bookingReference}
                  </p>
                </div>
                <div className="text-right">
                  <p className="font-black">{formatAmount(booking.totalAmount)}</p>
                  <p className="mt-1 text-sm text-slate-500">{booking.status}</p>
                </div>
              </div>
              <div className="mt-4 flex flex-wrap gap-5 text-sm text-slate-500">
                <span>{booking.quantity} tickets</span>
                <span>Booking #{booking.id}</span>
                <span>{formatDate(booking.createdAt)}</span>
              </div>
              {booking.status === "CONFIRMED" && (
                <div className="mt-5 flex flex-wrap gap-3">
                  <input
                    value={refundReason[booking.bookingReference] || ""}
                    onChange={(event) => setRefundReason((current) => ({
                      ...current,
                      [booking.bookingReference]: event.target.value
                    }))}
                    placeholder="Refund reason"
                    className="min-w-60 flex-1 rounded-xl border border-slate-200 px-4 py-3"
                  />
                  <button
                    disabled={busyId === booking.bookingReference}
                    onClick={() => initiateRefund(booking)}
                    className="rounded-xl bg-amber-600 px-5 py-3 font-semibold text-white disabled:opacity-50"
                  >
                    Initiate refund
                  </button>
                </div>
              )}
            </article>
          ))}
          {records.length === 0 && (
            <p className="rounded-xl bg-white p-10 text-center text-slate-500">No bookings found.</p>
          )}
        </section>
      ) : (
        <section className="mt-7 space-y-4">
          {records.map((refund) => (
            <article key={refund.refundId} className="rounded-2xl border border-slate-200 bg-white p-6">
              <div className="flex flex-wrap justify-between gap-4">
                <div>
                  <h3 className="font-bold">{refund.eventTitle}</h3>
                  <p className="mt-1 text-sm text-slate-500">
                    {refund.customerName} · {refund.customerEmail}
                  </p>
                  <p className="mt-2 break-all text-xs text-slate-400">
                    Booking {refund.bookingReference}
                  </p>
                </div>
                <div className="text-right">
                  <p className="font-black">₹{Number(refund.amount || 0).toLocaleString("en-IN")}</p>
                  <p className="mt-1 text-sm font-semibold">{refund.status}</p>
                </div>
              </div>
              <div className="mt-4 space-y-1 text-sm text-slate-500">
                <p>Reason: {refund.reason || "—"}</p>
                <p>Billing reference: {refund.billingReference || "—"}</p>
                <p>Failure reason: {refund.failureReason || "—"}</p>
                <p>Created: {formatDate(refund.createdAt)}</p>
              </div>
            </article>
          ))}
          {records.length === 0 && (
            <p className="rounded-xl bg-white p-10 text-center text-slate-500">No refunds found.</p>
          )}
        </section>
      )}

      {section !== "Organizers" && (
        <div className="mt-7 flex items-center justify-center gap-4">
          <button
            disabled={page === 0 || loading}
            onClick={() => setPage((current) => current - 1)}
            className="flex items-center gap-1 rounded-lg border border-slate-200 px-4 py-2 disabled:opacity-40"
          >
            <ChevronLeft size={17} /> Previous
          </button>
          <span className="text-sm text-slate-500">
            Page {page + 1} of {Math.max(totalPages, 1)}
          </span>
          <button
            disabled={page + 1 >= totalPages || loading}
            onClick={() => setPage((current) => current + 1)}
            className="flex items-center gap-1 rounded-lg border border-slate-200 px-4 py-2 disabled:opacity-40"
          >
            Next <ChevronRight size={17} />
          </button>
        </div>
      )}
    </main>
  );
}
