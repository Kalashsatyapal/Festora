
import { useEffect, useRef, useState } from "react";
import { Html5QrcodeScanner } from "html5-qrcode";
import { Camera, CheckCircle2, XCircle, Search, RefreshCw } from "lucide-react";
import api from "../api/client";

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export default function OrganizerTicketScanner() {
  const scannerRef = useRef(null);
  const processingRef = useRef(false);

  const [ticketCode, setTicketCode] = useState("");
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");
  const [verifying, setVerifying] = useState(false);

  const verifyTicket = async (rawCode) => {
    const code = rawCode.trim();

    if (!UUID_PATTERN.test(code)) {
      setError("Invalid ticket QR code. Enter a valid ticket number.");
      setResult(null);
      return;
    }

    if (processingRef.current) return;

    processingRef.current = true;
    setVerifying(true);
    setError("");
    setResult(null);

    try {
      const response = await api.get(
        `/tickets/verify/${encodeURIComponent(code)}`
      );

      setResult(response.data.data);
      setTicketCode(code);

      if (scannerRef.current) {
        scannerRef.current.pause(true);
      }
    } catch (err) {
      setError(
        err.response?.data?.message || "Ticket verification failed"
      );

      if (scannerRef.current) {
        scannerRef.current.pause(true);
      }
    } finally {
      processingRef.current = false;
      setVerifying(false);
    }
  };

  useEffect(() => {
    const scanner = new Html5QrcodeScanner(
      "festora-ticket-reader",
      {
        fps: 10,
        qrbox: { width: 250, height: 250 },
        rememberLastUsedCamera: true,
        aspectRatio: 1
      },
      false
    );

    scannerRef.current = scanner;

    scanner.render(
      (decodedText) => {
        verifyTicket(decodedText);
      },
      () => {}
    );

    return () => {
      scannerRef.current = null;
      scanner.clear().catch(() => {});
    };
  }, []);

  const resetScanner = () => {
    setResult(null);
    setError("");
    setTicketCode("");

    if (scannerRef.current) {
      scannerRef.current.resume();
    }
  };

  const submitManualCode = (event) => {
    event.preventDefault();
    verifyTicket(ticketCode);
  };

  return (
    <main className="mx-auto max-w-6xl px-5 py-10">
      <div>
        <h1 className="text-3xl font-black text-slate-900">
          Ticket verification
        </h1>
        <p className="mt-2 text-slate-500">
          Scan a guest's ticket QR code to validate event entry.
        </p>
      </div>

      <div className="mt-8 grid gap-8 lg:grid-cols-2">
        <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-indigo-50 p-3 text-indigo-600">
              <Camera size={23} />
            </div>
            <div>
              <h2 className="text-xl font-bold">Scan ticket QR</h2>
              <p className="text-sm text-slate-500">
                Allow camera access when prompted.
              </p>
            </div>
          </div>

          <div className="mt-6 overflow-hidden rounded-xl">
            <div id="festora-ticket-reader" />
          </div>

          <div className="mt-5 rounded-xl bg-slate-50 p-4 text-sm text-slate-600">
            Camera access requires localhost or a secure HTTPS connection.
          </div>
        </section>

        <section className="space-y-6">
          <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="text-xl font-bold">Manual verification</h2>
            <p className="mt-2 text-sm text-slate-500">
              Enter the ticket number if camera scanning is unavailable.
            </p>

            <form onSubmit={submitManualCode} className="mt-5 space-y-4">
              <input
                value={ticketCode}
                onChange={(event) => setTicketCode(event.target.value)}
                placeholder="Enter ticket UUID"
                className="w-full rounded-xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500"
              />

              <button
                type="submit"
                disabled={verifying || !ticketCode.trim()}
                className="flex w-full items-center justify-center gap-2 rounded-xl bg-indigo-600 py-3 font-bold text-white hover:bg-indigo-700 disabled:opacity-50"
              >
                <Search size={18} />
                {verifying ? "Verifying..." : "Verify ticket"}
              </button>
            </form>
          </div>

          {error && (
            <div className="rounded-2xl border border-red-200 bg-red-50 p-6">
              <div className="flex items-center gap-3 text-red-700">
                <XCircle size={25} />
                <h2 className="text-lg font-bold">Entry not approved</h2>
              </div>
              <p className="mt-3 text-sm text-red-700">{error}</p>
              <button
                onClick={resetScanner}
                className="mt-5 flex items-center gap-2 rounded-lg border border-red-200 px-4 py-2 text-sm font-semibold text-red-700"
              >
                <RefreshCw size={16} />
                Scan another ticket
              </button>
            </div>
          )}

          {result && (
            <div className="rounded-2xl border border-emerald-200 bg-emerald-50 p-6">
              <div className="flex items-center gap-3 text-emerald-700">
                <CheckCircle2 size={28} />
                <h2 className="text-xl font-black">Entry approved</h2>
              </div>

              <div className="mt-6 space-y-4 text-sm">
                <div>
                  <p className="text-emerald-700">Event</p>
                  <p className="mt-1 font-bold text-slate-900">
                    {result.eventTitle}
                  </p>
                </div>

                <div>
                  <p className="text-emerald-700">Ticket number</p>
                  <p className="mt-1 break-all font-semibold text-slate-900">
                    {result.ticketNumber}
                  </p>
                </div>

                <div>
                  <p className="text-emerald-700">Seat number</p>
                  <p className="mt-1 text-2xl font-black text-slate-900">
                    {result.seatNumber}
                  </p>
                </div>

                <div>
                  <p className="text-emerald-700">Verification time</p>
                  <p className="mt-1 font-semibold text-slate-900">
                    {new Date(result.usedAt).toLocaleString()}
                  </p>
                </div>

                <div>
                  <p className="text-emerald-700">Status</p>
                  <p className="mt-1 font-bold text-emerald-700">
                    {result.status}
                  </p>
                </div>
              </div>

              <button
                onClick={resetScanner}
                className="mt-6 flex w-full items-center justify-center gap-2 rounded-xl bg-emerald-600 py-3 font-bold text-white hover:bg-emerald-700"
              >
                <RefreshCw size={17} />
                Scan next ticket
              </button>
            </div>
          )}
        </section>
      </div>
    </main>
  );
}
