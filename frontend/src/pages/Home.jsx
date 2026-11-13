import { Link } from "react-router-dom";
import { ArrowRight, CalendarCheck, ShieldCheck, Ticket } from "lucide-react";

export default function Home() {
  return (
    <main>
      <section className="bg-linear-to-br from-slate-950 via-indigo-950 to-indigo-800 text-white">
        <div className="mx-auto grid max-w-7xl gap-12 px-5 py-28 md:grid-cols-2 md:items-center">
          <div>
            <span className="rounded-full border border-white/20 px-4 py-2 text-sm text-indigo-100">
              Discover. Book. Experience.
            </span>
            <h1 className="mt-8 text-5xl font-black leading-tight md:text-7xl">
              Your next great experience starts here.
            </h1>
            <p className="mt-6 max-w-xl text-lg leading-8 text-indigo-100">
              Discover events, reserve your tickets, and keep every experience in one place.
            </p>
            <Link to="/events" className="mt-9 inline-flex items-center gap-2 rounded-xl bg-white px-6 py-3 font-bold text-indigo-800">
              Explore events <ArrowRight size={18} />
            </Link>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="rounded-3xl border border-white/10 bg-white/10 p-8 backdrop-blur">
              <CalendarCheck size={36} />
              <h3 className="mt-6 text-xl font-bold">Discover</h3>
              <p className="mt-2 text-sm text-indigo-100">Find events that match your interests.</p>
            </div>
            <div className="mt-12 rounded-3xl border border-white/10 bg-white/10 p-8 backdrop-blur">
              <Ticket size={36} />
              <h3 className="mt-6 text-xl font-bold">Book</h3>
              <p className="mt-2 text-sm text-indigo-100">Reserve tickets with a simple checkout.</p>
            </div>
            <div className="rounded-3xl border border-white/10 bg-white/10 p-8 backdrop-blur">
              <ShieldCheck size={36} />
              <h3 className="mt-6 text-xl font-bold">Secure</h3>
              <p className="mt-2 text-sm text-indigo-100">Manage your bookings securely.</p>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}