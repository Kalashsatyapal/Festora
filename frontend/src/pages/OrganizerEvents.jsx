
import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  Plus,
  Pencil,
  Trash2,
  Upload,
  ImagePlus,
  Send,
  XCircle,
  CalendarDays,
  MapPin,
  IndianRupee
} from "lucide-react";
import api from "../api/client";
import Loading from "../components/Loading";

const emptyForm = {
  title: "",
  description: "",
  categoryId: "",
  venueId: "",
  startAt: "",
  endAt: "",
  totalCapacity: "",
  ticketPrice: ""
};

const localDateTime = (value) => {
  if (!value) return "";
  const date = new Date(value);
  const offset = date.getTimezoneOffset() * 60000;
  return new Date(date.getTime() - offset).toISOString().slice(0, 16);
};

export default function OrganizerEvents() {
  const [events, setEvents] = useState([]);
  const [categories, setCategories] = useState([]);
  const [venues, setVenues] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [selectedFiles, setSelectedFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const loadEvents = useCallback(async () => {
    try {
      const response = await api.get("/organizer/events", {
        params: { page, size: 10 }
      });
      const data = response.data.data;
      setEvents(data?.content || []);
      setTotalPages(data?.totalPages || 0);
    } catch (err) {
      setError(err.response?.data?.message || "Unable to load events");
    } finally {
      setLoading(false);
    }
  }, [page]);

  const loadOptions = async () => {
    try {
      const [categoryResponse, venueResponse] = await Promise.all([
        api.get("/categories"),
        api.get("/organizer/venues")
      ]);
      setCategories(categoryResponse.data.data || []);
      setVenues(venueResponse.data.data || []);
    } catch (err) {
      setError(err.response?.data?.message || "Unable to load categories or venues");
    }
  };

  useEffect(() => {
    loadEvents();
  }, [loadEvents]);

  useEffect(() => {
    loadOptions();
  }, []);

  const resetForm = () => {
    setForm(emptyForm);
    setEditingId(null);
    setSelectedFiles([]);
    setMessage("");
    setError("");
  };

  const updateField = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const editEvent = async (eventId) => {
    setError("");
    setMessage("");

    try {
      const response = await api.get(`/organizer/events/${eventId}`);
      const event = response.data.data;

      if (event.status !== "DRAFT") {
        setError("Only draft events can be edited");
        return;
      }

      setEditingId(event.id);
      setForm({
        title: event.title || "",
        description: event.description || "",
        categoryId: String(event.categoryId),
        venueId: String(event.venueId),
        startAt: localDateTime(event.startAt),
        endAt: localDateTime(event.endAt),
        totalCapacity: String(event.totalCapacity),
        ticketPrice: String(event.ticketPrice)
      });
      setSelectedFiles([]);
      window.scrollTo({ top: 0, behavior: "smooth" });
    } catch (err) {
      setError(err.response?.data?.message || "Unable to load event");
    }
  };

  const saveEvent = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError("");
    setMessage("");

    const payload = {
      title: form.title.trim(),
      description: form.description.trim(),
      categoryId: Number(form.categoryId),
      venueId: Number(form.venueId),
      startAt: new Date(form.startAt).toISOString(),
      endAt: new Date(form.endAt).toISOString(),
      totalCapacity: Number(form.totalCapacity),
      ticketPrice: Number(form.ticketPrice)
    };

    try {
      const response = editingId
        ? await api.put(`/organizer/events/${editingId}`, payload)
        : await api.post("/organizer/events", payload);

      const savedEvent = response.data.data;

      if (selectedFiles.length > 0) {
        const data = new FormData();
        selectedFiles.forEach((file) => data.append("files", file));

        await api.post(
          `/organizer/events/${savedEvent.id}/images`,
          data,
          { headers: { "Content-Type": "multipart/form-data" } }
        );
      }

      setMessage(editingId ? "Event updated successfully" : "Event created successfully");
      resetForm();
      setMessage(editingId ? "Event updated successfully" : "Event created successfully");
      await loadEvents();
    } catch (err) {
      setError(err.response?.data?.message || "Unable to save event");
    } finally {
      setSaving(false);
    }
  };

  const uploadImages = async (eventId, files) => {
    if (!files.length) return;

    const data = new FormData();
    files.forEach((file) => data.append("files", file));

    await api.post(`/organizer/events/${eventId}/images`, data, {
      headers: { "Content-Type": "multipart/form-data" }
    });
  };

  const publishEvent = async (eventId) => {
    if (!window.confirm("Publish this event?")) return;

    try {
      await api.patch(`/organizer/events/${eventId}/publish`);
      setMessage("Event published successfully");
      setError("");
      await loadEvents();
    } catch (err) {
      setError(err.response?.data?.message || "Unable to publish event");
    }
  };

  const cancelEvent = async (eventId) => {
    if (!window.confirm("Cancel this event?")) return;

    try {
      await api.patch(`/organizer/events/${eventId}/cancel`);
      setMessage("Event cancelled successfully");
      setError("");
      await loadEvents();
    } catch (err) {
      setError(err.response?.data?.message || "Unable to cancel event");
    }
  };

  const deleteEvent = async (eventId) => {
    if (!window.confirm("Permanently delete this draft event?")) return;

    try {
      await api.delete(`/organizer/events/${eventId}`);
      setMessage("Draft event deleted successfully");
      setError("");
      await loadEvents();
    } catch (err) {
      setError(err.response?.data?.message || "Unable to delete event");
    }
  };

  const removeImage = async (eventId, imageIndex) => {
    if (!window.confirm("Remove this event image?")) return;

    try {
      await api.delete(`/organizer/events/${eventId}/images/${imageIndex}`);
      await loadEvents();
      setMessage("Image removed successfully");
      setError("");
    } catch (err) {
      setError(err.response?.data?.message || "Unable to remove image");
    }
  };

  const uploadExistingImages = async (eventId, files) => {
    try {
      await uploadImages(eventId, files);
      await loadEvents();
      setMessage("Images uploaded successfully");
      setError("");
    } catch (err) {
      setError(err.response?.data?.message || "Unable to upload images");
    }
  };

  if (loading) return <Loading />;

  return (
    <main className="mx-auto max-w-7xl px-5 py-10">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-black text-slate-900">Manage events</h1>
          <p className="mt-2 text-slate-500">
            Create, update, publish and manage your event listings.
          </p>
        </div>
        <Link
          to="/organizer"
          className="rounded-xl border border-slate-200 px-5 py-3 font-semibold text-slate-700"
        >
          Dashboard
        </Link>
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

      <section className="mt-8 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <div className="mb-6 flex items-center justify-between">
          <div>
            <h2 className="text-xl font-black">
              {editingId ? "Edit draft event" : "Create event"}
            </h2>
            <p className="mt-1 text-sm text-slate-500">
              Complete the event details and save as a draft.
            </p>
          </div>
          <Plus className="text-indigo-600" />
        </div>

        <form onSubmit={saveEvent} className="grid gap-5 md:grid-cols-2">
          <label className="md:col-span-2">
            <span className="mb-2 block text-sm font-semibold">Event title</span>
            <input
              name="title"
              value={form.title}
              onChange={updateField}
              maxLength={150}
              required
              className="w-full rounded-xl border border-slate-300 px-4 py-3"
              placeholder="Enter event title"
            />
          </label>

          <label className="md:col-span-2">
            <span className="mb-2 block text-sm font-semibold">Description</span>
            <textarea
              name="description"
              value={form.description}
              onChange={updateField}
              maxLength={3000}
              rows={4}
              className="w-full rounded-xl border border-slate-300 px-4 py-3"
              placeholder="Describe your event"
            />
          </label>

          <label>
            <span className="mb-2 block text-sm font-semibold">Category</span>
            <select
              name="categoryId"
              value={form.categoryId}
              onChange={updateField}
              required
              className="w-full rounded-xl border border-slate-300 px-4 py-3"
            >
              <option value="">Select category</option>
              {categories.filter((category) => category.active).map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </label>

          <label>
            <span className="mb-2 block text-sm font-semibold">Assigned venue</span>
            <select
              name="venueId"
              value={form.venueId}
              onChange={updateField}
              required
              className="w-full rounded-xl border border-slate-300 px-4 py-3"
            >
              <option value="">Select venue</option>
              {venues.map((venue) => (
                <option key={venue.venueId} value={venue.venueId}>
                  {venue.venueName} - {venue.city}
                </option>
              ))}
            </select>
          </label>

          <label>
            <span className="mb-2 block text-sm font-semibold">Start date and time</span>
            <input
              type="datetime-local"
              name="startAt"
              value={form.startAt}
              onChange={updateField}
              required
              className="w-full rounded-xl border border-slate-300 px-4 py-3"
            />
          </label>

          <label>
            <span className="mb-2 block text-sm font-semibold">End date and time</span>
            <input
              type="datetime-local"
              name="endAt"
              value={form.endAt}
              onChange={updateField}
              required
              className="w-full rounded-xl border border-slate-300 px-4 py-3"
            />
          </label>

          <label>
            <span className="mb-2 block text-sm font-semibold">Total capacity</span>
            <input
              type="number"
              name="totalCapacity"
              value={form.totalCapacity}
              onChange={updateField}
              min="1"
              required
              className="w-full rounded-xl border border-slate-300 px-4 py-3"
            />
          </label>

          <label>
            <span className="mb-2 block text-sm font-semibold">Ticket price (₹)</span>
            <input
              type="number"
              name="ticketPrice"
              value={form.ticketPrice}
              onChange={updateField}
              min="0"
              step="0.01"
              required
              className="w-full rounded-xl border border-slate-300 px-4 py-3"
            />
          </label>

          <label className="md:col-span-2">
            <span className="mb-2 block text-sm font-semibold">
              Event images (JPG, PNG, WEBP; maximum 5 MB each)
            </span>
            <input
              type="file"
              accept="image/jpeg,image/png,image/webp"
              multiple
              onChange={(event) => setSelectedFiles(Array.from(event.target.files || []))}
              className="w-full rounded-xl border border-slate-300 px-4 py-3"
            />
            <span className="mt-2 block text-xs text-slate-500">
              Maximum 10 images per event.
            </span>
          </label>

          {selectedFiles.length > 0 && (
            <div className="md:col-span-2 flex flex-wrap gap-2">
              {selectedFiles.map((file) => (
                <span key={`${file.name}-${file.size}`} className="rounded-lg bg-indigo-50 px-3 py-2 text-xs text-indigo-700">
                  {file.name}
                </span>
              ))}
            </div>
          )}

          <div className="md:col-span-2 flex flex-wrap gap-3">
            <button
              type="submit"
              disabled={saving}
              className="rounded-xl bg-indigo-600 px-6 py-3 font-bold text-white disabled:opacity-50"
            >
              {saving ? "Saving..." : editingId ? "Update draft" : "Create draft"}
            </button>

            {editingId && (
              <button
                type="button"
                onClick={resetForm}
                className="rounded-xl border border-slate-300 px-6 py-3 font-semibold text-slate-700"
              >
                Cancel editing
              </button>
            )}
          </div>
        </form>
      </section>

      <section className="mt-10">
        <div className="mb-5 flex items-center justify-between">
          <h2 className="text-2xl font-black">Your events</h2>
          <span className="text-sm text-slate-500">
            Page {page + 1} of {Math.max(totalPages, 1)}
          </span>
        </div>

        {events.length === 0 ? (
          <div className="rounded-2xl border border-dashed border-slate-300 bg-white p-12 text-center">
            <ImagePlus className="mx-auto text-slate-400" size={35} />
            <h3 className="mt-4 text-lg font-bold">No events created yet</h3>
            <p className="mt-2 text-sm text-slate-500">
              Create your first event using the form above.
            </p>
          </div>
        ) : (
          <div className="grid gap-6 md:grid-cols-2">
            {events.map((event) => (
              <article key={event.id} className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
                {event.imageUrls?.length > 0 ? (
                  <img
                    src={event.imageUrls[0]}
                    alt={event.title}
                    className="h-48 w-full object-cover"
                  />
                ) : (
                  <div className="flex h-48 items-center justify-center bg-slate-100 text-slate-400">
                    <ImagePlus size={42} />
                  </div>
                )}

                <div className="p-6">
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <h3 className="text-xl font-black">{event.title}</h3>
                      <p className="mt-2 text-sm text-slate-500">
                        {event.categoryName}
                      </p>
                    </div>
                    <span className="rounded-full bg-indigo-50 px-3 py-1 text-xs font-bold text-indigo-700">
                      {event.status}
                    </span>
                  </div>

                  <div className="mt-5 space-y-3 text-sm text-slate-600">
                    <p className="flex items-center gap-2">
                      <MapPin size={16} />
                      {event.venueName}, {event.venueCity}
                    </p>
                    <p className="flex items-center gap-2">
                      <CalendarDays size={16} />
                      {new Date(event.startAt).toLocaleString()}
                    </p>
                    <p className="flex items-center gap-2">
                      <IndianRupee size={16} />
                      {Number(event.ticketPrice).toFixed(2)} per ticket
                    </p>
                  </div>

                  {event.status === "DRAFT" && (
                    <>
                      <div className="mt-5 grid grid-cols-2 gap-2">
                        <button
                          onClick={() => editEvent(event.id)}
                          className="flex items-center justify-center gap-2 rounded-lg border border-slate-200 px-3 py-2 text-sm font-semibold"
                        >
                          <Pencil size={15} />
                          Edit
                        </button>
                        <button
                          onClick={() => publishEvent(event.id)}
                          className="flex items-center justify-center gap-2 rounded-lg bg-emerald-600 px-3 py-2 text-sm font-semibold text-white"
                        >
                          <Send size={15} />
                          Publish
                        </button>
                      </div>

                      <div className="mt-3 flex flex-wrap gap-2">
                        <label className="flex cursor-pointer items-center gap-2 rounded-lg bg-indigo-50 px-3 py-2 text-sm font-semibold text-indigo-700">
                          <Upload size={15} />
                          Upload images
                          <input
                            type="file"
                            accept="image/jpeg,image/png,image/webp"
                            multiple
                            className="hidden"
                            onChange={(e) => uploadExistingImages(event.id, Array.from(e.target.files || []))}
                          />
                        </label>
                        <button
                          onClick={() => deleteEvent(event.id)}
                          className="flex items-center gap-2 rounded-lg bg-red-50 px-3 py-2 text-sm font-semibold text-red-700"
                        >
                          <Trash2 size={15} />
                          Delete draft
                        </button>
                      </div>

                      {event.imageUrls?.length > 0 && (
                        <div className="mt-4 grid grid-cols-4 gap-2">
                          {event.imageUrls.map((url, index) => (
                            <div key={url} className="relative">
                              <img src={url} alt={`${event.title} ${index + 1}`} className="h-16 w-full rounded-lg object-cover" />
                              <button
                                onClick={() => removeImage(event.id, index)}
                                className="absolute -right-1 -top-1 rounded-full bg-red-600 p-1 text-white"
                                aria-label="Remove image"
                              >
                                <XCircle size={14} />
                              </button>
                            </div>
                          ))}
                        </div>
                      )}
                    </>
                  )}

                  {event.status === "PUBLISHED" && (
                    <button
                      onClick={() => cancelEvent(event.id)}
                      className="mt-5 w-full rounded-lg border border-red-200 px-4 py-2 text-sm font-semibold text-red-700"
                    >
                      Cancel event
                    </button>
                  )}
                </div>
              </article>
            ))}
          </div>
        )}

        <div className="mt-7 flex justify-center gap-3">
          <button
            disabled={page === 0}
            onClick={() => setPage((current) => current - 1)}
            className="rounded-lg border border-slate-200 px-5 py-2 disabled:opacity-40"
          >
            Previous
          </button>
          <button
            disabled={page + 1 >= totalPages}
            onClick={() => setPage((current) => current + 1)}
            className="rounded-lg border border-slate-200 px-5 py-2 disabled:opacity-40"
          >
            Next
          </button>
        </div>
      </section>
    </main>
  );
}
