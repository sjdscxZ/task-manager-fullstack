import { useEffect, useState } from 'react';
import { list, create, move, remove, Status, Task } from './api/tasks';

const COLUMNS: { key: Status; label: string }[] = [
  { key: 'TODO',  label: 'To Do' },
  { key: 'DOING', label: 'In Progress' },
  { key: 'DONE',  label: 'Done' },
];

export function App() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [title, setTitle] = useState('');
  const [err, setErr] = useState<string | null>(null);

  async function refresh() {
    try { setTasks(await list()); } catch (e) { setErr(String(e)); }
  }

  useEffect(() => { refresh(); }, []);

  async function add(e: React.FormEvent) {
    e.preventDefault();
    if (!title.trim()) return;
    await create(title);
    setTitle('');
    refresh();
  }

  async function moveTo(id: number, status: Status) {
    await move(id, status, 0);
    refresh();
  }

  async function del(id: number) {
    await remove(id);
    refresh();
  }

  const grouped: Record<Status, Task[]> = { TODO: [], DOING: [], DONE: [] };
  for (const t of tasks) grouped[t.status].push(t);

  return (
    <div style={{ maxWidth: 1100, margin: '2rem auto', fontFamily: 'sans-serif' }}>
      <h1>Task Manager</h1>
      {err && <p style={{ color: 'crimson' }}>{err}</p>}

      <form onSubmit={add} style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
        <input
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="New task title…"
          style={{ flex: 1, padding: 8 }}
        />
        <button type="submit">Add</button>
      </form>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16 }}>
        {COLUMNS.map((col) => (
          <section key={col.key} style={{ background: '#f4f5f7', padding: 12, borderRadius: 8 }}>
            <h3>{col.label} ({grouped[col.key].length})</h3>
            {grouped[col.key].map((t) => (
              <article key={t.id} style={{ background: 'white', padding: 10, marginBottom: 8, borderRadius: 6 }}>
                <strong>{t.title}</strong>
                {t.description && <p style={{ margin: '4px 0', color: '#555' }}>{t.description}</p>}
                <div style={{ display: 'flex', gap: 4, marginTop: 6 }}>
                  {COLUMNS.filter((c) => c.key !== t.status).map((c) => (
                    <button key={c.key} onClick={() => moveTo(t.id, c.key)} style={{ fontSize: 12 }}>
                      → {c.label}
                    </button>
                  ))}
                  <button onClick={() => del(t.id)} style={{ fontSize: 12, marginLeft: 'auto' }}>Delete</button>
                </div>
              </article>
            ))}
          </section>
        ))}
      </div>
    </div>
  );
}
