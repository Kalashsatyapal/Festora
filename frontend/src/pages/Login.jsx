import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const submit = async (event) => {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      const user = await login(form);
      const destination = {
        ADMIN: "/admin",
        ORGANIZER: "/organizer",
        USER: "/events"
      };
      navigate(destination[user.role] || "/");
    } catch (err) {
      setError(err.response?.data?.message || "Unable to sign in");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="flex min-h-[80vh] items-center justify-center px-5 py-12">
      <form onSubmit={submit} className="w-full max-w-md rounded-3xl border border-slate-200 bg-white p-8 shadow-xl">
        <h1 className="text-3xl font-black text-slate-900">Welcome back</h1>
        <p className="mt-2 text-sm text-slate-500">Sign in to continue to Festora.</p>

        {error && <p className="mt-5 rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</p>}

        <label className="mt-6 block text-sm font-semibold">Email</label>
        <input
          type="email"
          required
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
          className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-600"
        />

        <label className="mt-5 block text-sm font-semibold">Password</label>
        <input
          type="password"
          required
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
          className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-600"
        />

        <button disabled={loading} className="mt-7 w-full rounded-xl bg-indigo-600 py-3 font-bold text-white disabled:opacity-60">
          {loading ? "Signing in..." : "Sign in"}
        </button>

        <p className="mt-6 text-center text-sm text-slate-500">
          New to Festora? <Link to="/register" className="font-bold text-indigo-700">Create an account</Link>
        </p>
      </form>
    </main>
  );
}