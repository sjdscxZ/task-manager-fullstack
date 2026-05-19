const API = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080';

export type Status = 'TODO' | 'DOING' | 'DONE';

export interface Task {
  id: number;
  title: string;
  description?: string;
  status: Status;
  orderIndex: number;
  createdAt: string;
  updatedAt: string;
}

export async function list(): Promise<Task[]> {
  const res = await fetch(`${API}/api/tasks`);
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

export async function create(title: string, description?: string): Promise<Task> {
  const res = await fetch(`${API}/api/tasks`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ title, description, status: 'TODO' }),
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

export async function move(id: number, status: Status, orderIndex: number): Promise<Task> {
  const res = await fetch(`${API}/api/tasks/${id}/move`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status, orderIndex }),
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

export async function remove(id: number): Promise<void> {
  const res = await fetch(`${API}/api/tasks/${id}`, { method: 'DELETE' });
  if (!res.ok && res.status !== 204) throw new Error(`HTTP ${res.status}`);
}
