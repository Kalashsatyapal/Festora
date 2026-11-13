export default function EmptyState({ title, message }) {
  return (
    <div className="rounded-2xl border border-dashed border-slate-300 bg-white p-12 text-center">
      <h3 className="text-lg font-bold text-slate-800">{title}</h3>
      <p className="mt-2 text-sm text-slate-500">{message}</p>
    </div>
  );
}