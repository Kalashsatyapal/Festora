import { useEffect, useState } from "react";
import { Users, CalendarDays, Ticket, IndianRupee } from "lucide-react";
import api from "../api/client";
import Loading from "../components/Loading";

const metrics = [
  { key: "totalUsers", label: "Users", icon: Users },
  { key: "totalOrganizers", label: "Organizers", icon: Users },
  { key: "activeEvents", label: "Published events", icon: CalendarDays },
  { key: "confirmedBookings", label: "Confirmed bookings", icon: Ticket },
  { key: "totalTicketsSold", label: "Tickets sold", icon: Ticket },
  { key: "totalRevenue", label: "Total revenue", icon: IndianRupee },
  { key: "pendingOrganizers", label: "Pending organizers", icon: Users },
  { key: "pendingRefundAmount", label: "Pending refunds", icon: IndianRupee }
];

export default function AdminDashboard() {
  const [dashboard, setDashboard] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api.get("/admin/dashboard")
      .then((response) => setDashboard(response.data.data))
      .catch((err) => setError(err.response?.data?.message || "Unable to load dashboard"));
  }, []);

  if (!dashboard && !error) return <Loading />;

  return (
    <main className="mx-auto max-w-7xl px-5 py-12">
      <h1 className="text-3xl font-black">Admin dashboard</h1>
      <p className="mt-2 text-slate-500">Platform overview and performance.</p>

      {error && <p className="mt-6 rounded-xl bg-red-50 p-4 text-red-700">{error}</p>}

      {dashboard && (
        <div className="mt-8 grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
          {metrics.map(({ key, label, icon: Icon }) => (
            <article key={key} className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-slate-500">{label}</span>
                <Icon className="text-indigo-600" />
              </div>
              <p className="mt-5 text-3xl font-black text-slate-900">
                {key.toLowerCase().includes("revenue") || key.toLowerCase().includes("amount")
                  ? `₹${Number(dashboard[key] || 0).toLocaleString("en-IN")}`
                  : Number(dashboard[key] || 0).toLocaleString("en-IN")}
              </p>
            </article>
          ))}
        </div>
      )}
    </main>
  );
}