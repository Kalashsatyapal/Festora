
import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import api from "../api/client";

export default function ResetPassword() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get("token") || "";

  const [form, setForm] = useState({
    newPassword: "",
    confirmPassword: ""
  });

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  const submit = async (event) => {
    event.preventDefault();
    setError("");
    setSuccess("");

    if (!token) {
      setError("Password reset token is missing.");
      return;
    }

    if (form.newPassword !== form.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    if (form.newPassword.length < 8 || form.newPassword.length > 72) {
      setError("Password must contain between 8 and 72 characters.");
      return;
    }

    setLoading(true);

    try {
      const response = await api.post("/auth/reset-password", {
        token,
        newPassword: form.newPassword
      });

      setSuccess(
        response.data?.message || "Password reset successfully."
      );

      setForm({
        newPassword: "",
        confirmPassword: ""
      });

      setTimeout(() => navigate("/login"), 1800);
    } catch (err) {
      setError(
        err.response?.data?.message ||
        "Invalid or expired reset link. Request a new one."
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
          Reset password
        </h1>

        <p className="mt-3 text-sm text-slate-500">
          Choose a new password for your Festora account.
        </p>

        {error && (
          <p className="mt-6 rounded-xl bg-red-50 p-4 text-sm text-red-700">
            {error}
          </p>
        )}

        {success && (
          <p className="mt-6 rounded-xl bg-green-50 p-4 text-sm text-green-700">
            {success}
          </p>
        )}

        {!token ? (
          <div className="mt-7">
            <p className="text-sm text-red-600">
              This reset link does not contain a valid token.
            </p>
            <Link
              to="/forgot-password"
              className="mt-5 inline-block font-bold text-indigo-700"
            >
              Request another reset link
            </Link>
          </div>
        ) : (
          <>
            <label className="mt-7 block text-sm font-semibold">
              New password
            </label>

            <input
              type="password"
              required
              minLength={8}
              maxLength={72}
              autoComplete="new-password"
              value={form.newPassword}
              onChange={(event) =>
                setForm({ ...form, newPassword: event.target.value })
              }
              className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-600"
            />

            <label className="mt-5 block text-sm font-semibold">
              Confirm new password
            </label>

            <input
              type="password"
              required
              minLength={8}
              maxLength={72}
              autoComplete="new-password"
              value={form.confirmPassword}
              onChange={(event) =>
                setForm({ ...form, confirmPassword: event.target.value })
              }
              className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-600"
            />

            <button
              type="submit"
              disabled={loading || Boolean(success)}
              className="mt-7 w-full rounded-xl bg-indigo-600 py-3 font-bold text-white disabled:opacity-60"
            >
              {loading ? "Updating..." : "Reset password"}
            </button>
          </>
        )}

        <p className="mt-6 text-center text-sm text-slate-500">
          <Link to="/login" className="font-bold text-indigo-700">
            Back to login
          </Link>
        </p>
      </form>
    </main>
  );
}
