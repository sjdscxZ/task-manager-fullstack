# STUDY — task-manager-fullstack

## Headline claims
1. "Spring Boot REST API + React 18 Vite SPA."
2. "Three-column Kanban with order index + status transitions."
3. "Tested with MockMvc + JUnit 5 against embedded H2."

## Q&A

**Q1. Why React over Angular here?**
Wanted breadth across both ecosystems. Angular is in `ecommerce-fullstack`; React is here. React's `useState` + functional components are well-suited for this small UI. Vite gives faster HMR than CRA.

**Q2. Why no state library?**
For ~100 tasks, `useState([])` + `refresh()` after each mutation works. React Query would help if I added optimistic updates or wanted cache-invalidation-on-mutation. State libraries (Redux, Zustand) are unnecessary for state that fits the single-component tree.

**Q3. How does drag-and-drop fit in?**
The data model supports it: each task has `orderIndex` and `status`. The `/move` endpoint takes both, so the UI just calls it with new values when a card is dropped. The UI in this repo uses buttons instead of native drag for accessibility/keyboard support. `@dnd-kit` is on the roadmap.

**Q4. Concurrency: two users move the same task?**
Last-write-wins right now. For real multi-user, add `@Version` to `Task` for optimistic locking — second write fails with HTTP 409 and the UI refreshes.

**Q5. Why `status` as String, not enum?**
JSON serialization of Java enums is trivial but adds an extra mapping layer. For a 3-value field, plain String is fine. Tradeoff: typos at the application layer aren't caught at compile time. Mitigation: a `@Pattern` validation regex or a `@PrePersist` check.

**Q6. CORS — why scoped to `localhost:5173`?**
Vite's dev server uses port 5173. Only that origin is allowed. Production deployment needs to add the deployed frontend origin (e.g. `https://tasks.mydomain.com`). A wildcard origin (`*`) is unsafe with credentials.

**Q7. How would JWT integration look?**
- Add `/api/auth/register|login` (same pattern as `blog-api`).
- Each `Task` gets `userId` (current owner).
- Repository queries scope by authenticated user: `findByUserIdOrderByOrderIndexAsc(currentUserId)`.
- Frontend `tasks.ts` adds `Authorization: Bearer ${token}` to fetch calls.

**Q8. Frontend testing?**
None yet. Would add:
- Vitest + React Testing Library for component tests
- Playwright for e2e (start backend, open page, click "Add", assert it appears)

## Gaps
- No JWT auth.
- No drag-and-drop.
- No optimistic updates (UI feels laggy on slow networks).
- Frontend tests absent.
- `orderIndex` is not bulk-updated on move — re-ordering siblings breaks down at scale.
