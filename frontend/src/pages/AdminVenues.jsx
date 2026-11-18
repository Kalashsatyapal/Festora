import { useCallback, useEffect, useState } from "react";
import {
  Building2,
  MapPin,
  Users,
  Plus,
  RefreshCw,
  Link2,
  Unlink,
  AlertCircle,
  CheckCircle2
} from "lucide-react";
import api from "../api/client";
import Loading from "../components/Loading";

const initialVenue = {
  name: "",
  city: "",
  address: "",
  capacity: ""
};

export default function AdminVenues() {
  const [venues, setVenues] = useState([]);
  const [organizers, setOrganizers] = useState([]);
  const [venueForm, setVenueForm] = useState(initialVenue);
  const [assignment, setAssignment] = useState({
    organizerId: "",
    venueId: ""
  });
  const [loading, setLoading] = useState(true);
  const [savingVenue, setSavingVenue] = useState(false);
  const [assigning, setAssigning] = useState(false);
  const [revoking, setRevoking] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const loadData = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const [venueResponse, organizerResponse] = await Promise.all([
        api.get("/admin/venues"),
        api.get("/admin/organizers/approved")
      ]);

      setVenues(venueResponse.data.data || []);
      setOrganizers(organizerResponse.data.data || []);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Unable to load venues and approved organizers."
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const updateVenueForm = (event) => {
    const { name, value } = event.target;

    setVenueForm((current) => ({
      ...current,
      [name]: value
    }));
  };

  const createVenue = async (event) => {
    event.preventDefault();
    setSavingVenue(true);
    setError("");
    setMessage("");

    try {
      const payload = {
        name: venueForm.name.trim(),
        city: venueForm.city.trim(),
        address: venueForm.address.trim(),
        capacity: Number(venueForm.capacity)
      };

      await api.post("/admin/venues", payload);

      setVenueForm(initialVenue);
      setMessage("Venue created successfully.");
      await loadData();
    } catch (err) {
      setError(
        err.response?.data?.message || "Unable to create venue."
      );
    } finally {
      setSavingVenue(false);
    }
  };

  const assignVenue = async (event) => {
    event.preventDefault();

    if (!assignment.organizerId || !assignment.venueId) {
      setError("Select both an organizer and a venue.");
      return;
    }

    setAssigning(true);
    setError("");
    setMessage("");

    try {
      await api.post("/admin/organizer-venues", {
        organizerId: Number(assignment.organizerId),
        venueId: Number(assignment.venueId)
      });

      setMessage("Venue assigned to organizer successfully.");
      setAssignment({
        organizerId: "",
        venueId: ""
      });
    } catch (err) {
      setError(
        err.response?.data?.message || "Unable to assign venue."
      );
    } finally {
      setAssigning(false);
    }
  };

  const revokeAssignment = async () => {
    if (!assignment.organizerId || !assignment.venueId) {
      setError("Select the organizer and venue whose assignment you want to revoke.");
      return;
    }

    const organizer = organizers.find(
      (item) => String(item.organizerId) === assignment.organizerId
    );

    const venue = venues.find(
      (item) => String(item.id) === assignment.venueId
    );

    if (
      !window.confirm(
        `Revoke ${venue?.name || "this venue"} from ${organizer?.fullName || "this organizer"}?`
      )
    ) {
      return;
    }

    setRevoking(true);
    setError("");
    setMessage("");

    try {
      await api.delete(
        `/admin/organizer-venues/${assignment.organizerId}/${assignment.venueId}`
      );

      setMessage("Venue assignment revoked successfully.");
      setAssignment({
        organizerId: "",
        venueId: ""
      });
    } catch (err) {
      setError(
        err.response?.data?.message || "Unable to revoke venue assignment."
      );
    } finally {
      setRevoking(false);
    }
  };

  if (loading && venues.length === 0 && organizers.length === 0) {
    return <Loading />;
  }

  return (
    <main className="mx-auto max-w-7xl px-5 py-10">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <p className="text-sm font-bold uppercase tracking-wider text-indigo-600">
            Administration
          </p>
          <h1 className="mt-2 text-3xl font-black text-slate-900">
            Venue management
          </h1>
          <p className="mt-2 text-slate-500">
            Create venues and control which approved organizers can use them.
          </p>
        </div>

        <button
          type="button"
          onClick={loadData}
          disabled={loading}
          className="flex items-center gap-2 rounded-xl border border-slate-200 px-4 py-3 font-semibold text-slate-700 hover:bg-slate-50 disabled:opacity-50"
        >
          <RefreshCw size={17} className={loading ? "animate-spin" : ""} />
          Refresh
        </button>
      </div>

      {error && (
        <div className="mt-6 flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          <AlertCircle size={19} className="mt-0.5 shrink-0" />
          {error}
        </div>
      )}

      {message && (
        <div className="mt-6 flex items-start gap-3 rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-700">
          <CheckCircle2 size={19} className="mt-0.5 shrink-0" />
          {message}
        </div>
      )}

      <section className="mt-8 grid gap-6 lg:grid-cols-2">
        <form
          onSubmit={createVenue}
          className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm"
        >
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-indigo-50 p-3 text-indigo-600">
              <Building2 size={23} />
            </div>
            <div>
              <h2 className="text-xl font-black">Create venue</h2>
              <p className="mt-1 text-sm text-slate-500">
                Add a new event location to Festora.
              </p>
            </div>
          </div>

          <div className="mt-6 space-y-4">
            <div>
              <label className="text-sm font-semibold text-slate-700">
                Venue name
              </label>
              <input
                required
                maxLength={150}
                name="name"
                value={venueForm.name}
                onChange={updateVenueForm}
                placeholder="e.g. Pune Convention Centre"
                className="mt-2 w-full rounded-xl border border-slate-200 px-4 py-3 outline-none focus:border-indigo-500"
              />
            </div>

            <div>
              <label className="text-sm font-semibold text-slate-700">
                City
              </label>
              <input
                required
                maxLength={100}
                name="city"
                value={venueForm.city}
                onChange={updateVenueForm}
                placeholder="e.g. Pune"
                className="mt-2 w-full rounded-xl border border-slate-200 px-4 py-3 outline-none focus:border-indigo-500"
              />
            </div>

            <div>
              <label className="text-sm font-semibold text-slate-700">
                Full address
              </label>
              <textarea
                required
                maxLength={500}
                rows={3}
                name="address"
                value={venueForm.address}
                onChange={updateVenueForm}
                placeholder="Enter the complete venue address"
                className="mt-2 w-full rounded-xl border border-slate-200 px-4 py-3 outline-none focus:border-indigo-500"
              />
            </div>

            <div>
              <label className="text-sm font-semibold text-slate-700">
                Maximum capacity
              </label>
              <input
                required
                min={1}
                step={1}
                type="number"
                name="capacity"
                value={venueForm.capacity}
                onChange={updateVenueForm}
                placeholder="e.g. 500"
                className="mt-2 w-full rounded-xl border border-slate-200 px-4 py-3 outline-none focus:border-indigo-500"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={savingVenue}
            className="mt-6 flex w-full items-center justify-center gap-2 rounded-xl bg-indigo-600 px-5 py-3 font-bold text-white hover:bg-indigo-700 disabled:opacity-50"
          >
            <Plus size={18} />
            {savingVenue ? "Creating..." : "Create venue"}
          </button>
        </form>

        <form
          onSubmit={assignVenue}
          className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm"
        >
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-emerald-50 p-3 text-emerald-600">
              <Link2 size={23} />
            </div>
            <div>
              <h2 className="text-xl font-black">Assign venue</h2>
              <p className="mt-1 text-sm text-slate-500">
                Give an approved organizer access to a venue.
              </p>
            </div>
          </div>

          <div className="mt-6 space-y-5">
            <div>
              <label className="text-sm font-semibold text-slate-700">
                Approved organizer
              </label>
              <select
                required
                value={assignment.organizerId}
                onChange={(event) =>
                  setAssignment((current) => ({
                    ...current,
                    organizerId: event.target.value
                  }))
                }
                className="mt-2 w-full rounded-xl border border-slate-200 bg-white px-4 py-3 outline-none focus:border-indigo-500"
              >
                <option value="">Select organizer</option>
                {organizers.map((organizer) => (
                  <option
                    key={organizer.organizerId}
                    value={organizer.organizerId}
                  >
                    {organizer.fullName} ({organizer.email})
                  </option>
                ))}
              </select>

              {organizers.length === 0 && (
                <p className="mt-2 text-sm text-amber-700">
                  No approved organizers are available. Approve an organizer first.
                </p>
              )}
            </div>

            <div>
              <label className="text-sm font-semibold text-slate-700">
                Active venue
              </label>
              <select
                required
                value={assignment.venueId}
                onChange={(event) =>
                  setAssignment((current) => ({
                    ...current,
                    venueId: event.target.value
                  }))
                }
                className="mt-2 w-full rounded-xl border border-slate-200 bg-white px-4 py-3 outline-none focus:border-indigo-500"
              >
                <option value="">Select venue</option>
                {venues.filter((venue) => venue.active).map((venue) => (
                  <option key={venue.id} value={venue.id}>
                    {venue.name} — {venue.city}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <button
            type="submit"
            disabled={assigning || organizers.length === 0 || !venues.some((venue) => venue.active)}
            className="mt-6 flex w-full items-center justify-center gap-2 rounded-xl bg-emerald-600 px-5 py-3 font-bold text-white hover:bg-emerald-700 disabled:opacity-50"
          >
            <Link2 size={18} />
            {assigning ? "Assigning..." : "Assign venue"}
          </button>

          <div className="mt-5 border-t border-slate-100 pt-5">
            <h3 className="font-bold text-slate-900">Revoke assignment</h3>
            <p className="mt-1 text-sm text-slate-500">
              Select the same organizer and venue above, then revoke their assignment.
            </p>
            <button
              type="button"
              onClick={revokeAssignment}
              disabled={revoking || !assignment.organizerId || !assignment.venueId}
              className="mt-4 flex w-full items-center justify-center gap-2 rounded-xl border border-red-200 px-5 py-3 font-bold text-red-700 hover:bg-red-50 disabled:opacity-50"
            >
              <Unlink size={18} />
              {revoking ? "Revoking..." : "Revoke selected assignment"}
            </button>
          </div>
        </form>
      </section>

      <section className="mt-10">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="text-2xl font-black text-slate-900">
              Registered venues
            </h2>
            <p className="mt-1 text-sm text-slate-500">
              {venues.length} venues registered in Festora.
            </p>
          </div>
        </div>

        {venues.length === 0 ? (
          <div className="mt-5 rounded-2xl border border-dashed border-slate-300 bg-white p-10 text-center">
            <Building2 className="mx-auto text-slate-400" size={38} />
            <h3 className="mt-4 font-bold text-slate-800">
              No venues created yet
            </h3>
            <p className="mt-1 text-sm text-slate-500">
              Create your first venue using the form above.
            </p>
          </div>
        ) : (
          <div className="mt-5 grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {venues.map((venue) => (
              <article
                key={venue.id}
                className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="rounded-xl bg-indigo-50 p-3 text-indigo-600">
                    <Building2 size={22} />
                  </div>
                  <span className={`rounded-full px-3 py-1 text-xs font-bold ${
                    venue.active
                      ? "bg-emerald-50 text-emerald-700"
                      : "bg-slate-100 text-slate-500"
                  }`}>
                    {venue.active ? "Active" : "Inactive"}
                  </span>
                </div>

                <h3 className="mt-4 text-lg font-black text-slate-900">
                  {venue.name}
                </h3>

                <p className="mt-2 flex items-start gap-2 text-sm text-slate-500">
                  <MapPin size={17} className="mt-0.5 shrink-0" />
                  {venue.address}, {venue.city}
                </p>

                <p className="mt-3 flex items-center gap-2 text-sm font-semibold text-slate-700">
                  <Users size={17} />
                  Capacity: {venue.capacity}
                </p>
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}