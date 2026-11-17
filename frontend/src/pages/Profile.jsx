
import { useEffect, useRef, useState } from "react";
import { Camera, UserRound, Mail, ShieldCheck, CalendarDays, RefreshCw } from "lucide-react";
import api from "../api/client";
import { useAuth } from "../context/AuthContext";

export default function Profile() {
  const { user, updateUser } = useAuth();
  const fileRef = useRef(null);

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const loadProfile = async () => {
    setLoading(true);
    setError("");

    try {
      const response = await api.get("/users/me");
      const data = response.data.data;

      setProfile(data);
      updateUser({
        userId: data.userId,
        fullName: data.fullName,
        email: data.email,
        role: data.role,
        avatarUrl: data.avatarUrl
      });
    } catch (err) {
      setError(err.response?.data?.message || "Unable to load profile");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProfile();
  }, []);

  const uploadAvatar = async (event) => {
    const file = event.target.files?.[0];

    if (!file) return;

    if (!["image/jpeg", "image/png", "image/webp"].includes(file.type)) {
      setError("Choose a JPG, PNG, or WEBP image.");
      event.target.value = "";
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      setError("Avatar size must not exceed 5 MB.");
      event.target.value = "";
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    setUploading(true);
    setError("");
    setMessage("");

    try {
      const response = await api.post("/users/me/avatar", formData, {
        headers: {
          "Content-Type": "multipart/form-data"
        }
      });

      const avatarUrl = response.data.data;

      setProfile((current) => ({
        ...current,
        avatarUrl
      }));

      updateUser({ avatarUrl });
      setMessage("Profile picture updated successfully.");
    } catch (err) {
      setError(err.response?.data?.message || "Avatar upload failed");
    } finally {
      setUploading(false);
      event.target.value = "";
    }
  };

  if (loading) {
    return (
      <main className="mx-auto max-w-4xl px-5 py-20 text-center text-slate-500">
        Loading profile...
      </main>
    );
  }

  if (!profile) {
    return (
      <main className="mx-auto max-w-4xl px-5 py-20">
        <p className="text-red-600">{error || "Profile unavailable"}</p>
        <button
          onClick={loadProfile}
          className="mt-4 rounded-xl bg-indigo-600 px-5 py-3 font-bold text-white"
        >
          Try again
        </button>
      </main>
    );
  }

  const initials = profile.fullName
    ?.split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0])
    .join("")
    .toUpperCase();

  return (
    <main className="mx-auto max-w-5xl px-5 py-10">
      <div>
        <p className="text-sm font-bold uppercase tracking-widest text-indigo-600">
          Account settings
        </p>
        <h1 className="mt-2 text-3xl font-black text-slate-900">
          My Profile
        </h1>
        <p className="mt-2 text-slate-500">
          View your account information and manage your profile picture.
        </p>
      </div>

      {error && (
        <div className="mt-6 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          {error}
        </div>
      )}

      {message && (
        <div className="mt-6 rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-700">
          {message}
        </div>
      )}

      <section className="mt-8 overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
        <div className="h-36 bg-linear-to-r from-indigo-700 to-violet-600" />

        <div className="px-6 pb-8 sm:px-9">
          <div className="-mt-16 flex flex-col items-start gap-5 sm:flex-row sm:items-end">
            <div className="relative">
              {profile.avatarUrl ? (
                <img
                  src={profile.avatarUrl}
                  alt={profile.fullName}
                  className="size-32 rounded-3xl border-4 border-white bg-white object-cover shadow-md"
                />
              ) : (
                <div className="grid size-32 place-items-center rounded-3xl border-4 border-white bg-indigo-100 text-3xl font-black text-indigo-700 shadow-md">
                  {initials || <UserRound size={42} />}
                </div>
              )}

              <button
                type="button"
                onClick={() => fileRef.current?.click()}
                disabled={uploading}
                aria-label="Change profile picture"
                className="absolute -bottom-2 -right-2 grid size-11 place-items-center rounded-full bg-indigo-600 text-white shadow-lg hover:bg-indigo-700 disabled:opacity-50"
              >
                {uploading ? (
                  <RefreshCw size={18} className="animate-spin" />
                ) : (
                  <Camera size={18} />
                )}
              </button>

              <input
                ref={fileRef}
                type="file"
                accept="image/jpeg,image/png,image/webp"
                onChange={uploadAvatar}
                className="hidden"
              />
            </div>

            <div className="pb-1">
              <h2 className="text-2xl font-black text-slate-900">
                {profile.fullName}
              </h2>
              <p className="mt-1 text-sm text-slate-500">
                {profile.role?.replaceAll("_", " ")}
              </p>
            </div>
          </div>

          <div className="mt-9 grid gap-4 md:grid-cols-2">
            <div className="rounded-2xl border border-slate-200 p-5">
              <div className="flex items-center gap-3 text-indigo-600">
                <UserRound size={20} />
                <span className="text-sm font-semibold">Full name</span>
              </div>
              <p className="mt-3 font-bold text-slate-900">
                {profile.fullName}
              </p>
            </div>

            <div className="rounded-2xl border border-slate-200 p-5">
              <div className="flex items-center gap-3 text-indigo-600">
                <Mail size={20} />
                <span className="text-sm font-semibold">Email address</span>
              </div>
              <p className="mt-3 break-all font-bold text-slate-900">
                {profile.email}
              </p>
            </div>

            <div className="rounded-2xl border border-slate-200 p-5">
              <div className="flex items-center gap-3 text-indigo-600">
                <ShieldCheck size={20} />
                <span className="text-sm font-semibold">Account role</span>
              </div>
              <p className="mt-3 font-bold text-slate-900">
                {profile.role}
              </p>
            </div>

            <div className="rounded-2xl border border-slate-200 p-5">
              <div className="flex items-center gap-3 text-indigo-600">
                <CalendarDays size={20} />
                <span className="text-sm font-semibold">Member since</span>
              </div>
              <p className="mt-3 font-bold text-slate-900">
                {profile.createdAt
                  ? new Date(profile.createdAt).toLocaleDateString()
                  : "Unavailable"}
              </p>
            </div>
          </div>

          <div className="mt-6 flex flex-wrap items-center justify-between gap-3 rounded-2xl bg-slate-50 p-5">
            <div>
              <p className="font-bold text-slate-900">Account status</p>
              <p className="mt-1 text-sm text-slate-500">
                {profile.status}
              </p>
            </div>

            <span className="rounded-full bg-emerald-100 px-4 py-2 text-xs font-bold text-emerald-800">
              {profile.status}
            </span>
          </div>

          <p className="mt-5 text-xs text-slate-400">
            Supported formats: JPG, PNG, WEBP. Maximum file size: 5 MB.
          </p>
        </div>
      </section>
    </main>
  );
}
