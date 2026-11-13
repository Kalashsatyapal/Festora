import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../api/client";

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: "",
    email: "",
    password: "",
    role: "USER"
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const submit = async (event) => {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      const endpoint = form.role === "ORGANIZER"
        ? "/auth/register/organizer"
        : "/auth/register";

      await api.post(endpoint, form);
      navigate("/login", {
        state: { message: "Registration successful. Please sign in." }
      });
    } catch (err) {
      setError(err.response?.data?.message || "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="flex min-h-[80vh] items-center justify-center px-5 py-12">
      <form onSubmit={submit} className="w-full max-w-md rounded-3xl border border-slate-200 bg-white p-8 shadow-xl">
        <h1 className="text-3xl font-black">Create your account</h1>
        <p className="mt-2 text-sm text-slate-500">Join the Festora community.</p>

        {error && <p className="mt-5 rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</p>}

        <label className="mt-6 block text-sm font-semibold">Full name</label>
        <input
          required
          value={form.fullName}
          onChange={(e) => setForm({ ...form, fullName: e.target.value })}
          className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3"
        />

        <label className="mt-5 block text-sm font-semibold">Email</label>
        <input
          type="email"
          required
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
          className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3"
        />

        <label className="mt-5 block text-sm font-semibold">Password</label>
        <input
          type="password"
          required
          minLength={8}
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
          className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3"
        />

        <label className="mt-5 block text-sm font-semibold">Account type</label>
        <select
          value={form.role}
          onChange={(e) => setForm({ ...form, role: e.target.value })}
          className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3"
        >
          <option value="USER">Attendee</option>
          <option value="ORGANIZER">Organizer</option>
        </select>

        <button disabled={loading} className="mt-7 w-full rounded-xl bg-indigo-600 py-3 font-bold text-white disabled:opacity-60">
          {loading ? "Creating account..." : "Create account"}
        </button>

        <p className="mt-6 text-center text-sm text-slate-500">
          Already registered? <Link to="/login" className="font-bold text-indigo-700">Sign in</Link>
        </p>
      </form>
    </main>
  );
}