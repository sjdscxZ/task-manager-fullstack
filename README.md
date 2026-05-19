# task-manager-fullstack

Full-stack Kanban task manager: **Spring Boot 3** backend + **React 18 + Vite + TypeScript** frontend. Three columns (To Do / In Progress / Done), task creation, move, delete.

## Stack

| Layer | Tech |
|---|---|
| Backend | Java 17, Spring Boot 3.3, Spring Data JPA, Jakarta Validation, PostgreSQL (H2 for tests) |
| Frontend | React 18, TypeScript 5, Vite 5 |
| Tests | JUnit 5 + MockMvc |

## Run

```bash
# Terminal A — backend
cd backend
./mvnw spring-boot:run
# → http://localhost:8080

# Terminal B — frontend
cd frontend
npm install
npm run dev
# → http://localhost:5173
```

CORS allows `http://localhost:5173` (Vite default).

## Try it

```bash
# REST directly
curl http://localhost:8080/api/tasks

curl -X POST http://localhost:8080/api/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title":"Write report","description":"Q2 results","status":"TODO"}'

# Move a task to DOING
curl -X POST http://localhost:8080/api/tasks/1/move \
  -H 'Content-Type: application/json' \
  -d '{"status":"DOING","orderIndex":0}'
```

## Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/tasks` | List tasks (ordered by `orderIndex`) |
| POST | `/api/tasks` | Create task |
| PUT | `/api/tasks/{id}` | Update title/description/status |
| POST | `/api/tasks/{id}/move` | Reposition task across columns |
| DELETE | `/api/tasks/{id}` | Delete task |

## Project layout

```
task-manager-fullstack/
├── backend/                              # Spring Boot 3
│   ├── pom.xml
│   ├── src/main/java/com/sjdscxz/taskmgr/TaskManagerApplication.java
│   ├── src/main/resources/application.yml
│   └── src/test/java/com/sjdscxz/taskmgr/TaskManagerApplicationTest.java
└── frontend/                             # React 18 + Vite
    ├── package.json
    ├── vite.config.ts
    ├── tsconfig.json
    ├── index.html
    └── src/
        ├── main.tsx
        ├── App.tsx                       # Kanban board
        └── api/tasks.ts                  # Fetch wrappers
```

## Design notes

- **No state library** — React's built-in `useState` + a `refresh()` after each mutation is fine for ~100 tasks. For larger scale I'd reach for React Query (server-state caching + optimistic updates).
- **Status as enum string** — simple, JSON-friendly. Server validates membership implicitly via business logic (could be tightened with `@Pattern` on the request).
- **`orderIndex`** lets the frontend stable-sort tasks within a column without timestamps.
- **CORS** is scoped tight to the Vite dev origin. Production needs the deployed origin.

## Resume reference

> *"Full-stack Kanban task manager: Spring Boot 3 backend + React 18 frontend, JWT, PostgreSQL"*

JWT auth is on the roadmap; current scope is the CRUD + UI integration.

## Roadmap

- [ ] JWT login + per-user task lists
- [ ] Drag-and-drop via `@dnd-kit`
- [ ] WebSocket push for multi-user sync
- [ ] React Query for optimistic UI
- [ ] e2e tests with Playwright

## License

MIT — see [LICENSE](LICENSE).
