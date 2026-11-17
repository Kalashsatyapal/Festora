
import { useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/client";

export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const submit = async (event) => {
    event.preventDefault();
    setMessage("");
    setError("");
    setLoading(true);

    try {
      const response = await api.post("/auth/forgot-password", {
        email: email.trim()
      });

      setMessage(
        response.data?.message ||
        "If the account exists, password reset instructions will be sent."
      );
    } catch (err) {
      setError(
        err.response?.data?.message ||
        "Unable to process your request. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="flex min-h-[80vh] items-center justify-center px-5 py-12">
      <form
        onSubmit={submit}
        className="w-full max-w-md rounded-3xl border border-slate-200 bg-white p-8 shadow-xl"
      >
        <h1 className="text-3xl font-black text-slate-900">
          Forgot password?
        </h1>

        <p className="mt-3 text-sm text-slate-500">
          Enter your registered email address. If your account exists, we will
          send you a password reset link.
        </p>

        {message && (
          <p className="mt-6 rounded-xl bg-green-50 p-4 text-sm text-green-700">
            {message}
          </p>
        )}

        {error && (
          <p className="mt-6 rounded-xl bg-red-50 p-4 text-sm text-red-700">
            {error}
          </p>
        )}

        <label className="mt-7 block text-sm font-semibold">
          Email address
        </label>

        <input
          type="email"
          required
          autoComplete="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          placeholder="you@example.com"
          className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-600"
        />

        <button
          type="submit"
          disabled={loading}
          className="mt-7 w-full rounded-xl bg-indigo-600 py-3 font-bold text-white disabled:opacity-60"
        >
          {loading ? "Sending..." : "Send reset link"}
        </button>

        <p className="mt-6 text-center text-sm text-slate-500">
          Remember your password?{" "}
          <Link to="/login" className="font-bold text-indigo-700">
            Back to login
          </Link>
        </p>
      </form>
    </main>
  );
}
