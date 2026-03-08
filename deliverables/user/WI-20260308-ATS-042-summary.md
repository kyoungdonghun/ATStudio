[WI SUMMARY]
WI ID: WI-20260308-ATS-042
REQ: REQ-20260308-ATS-012
Agent: se
Status: DONE (pending quality gate verification)

---

## What Changed

Admin (ADMIN-only) pages implemented with full API integration, CSS Modules, and ConfirmModal usage:

| Page | File | Description |
|------|------|-------------|
| Dashboard | `DashboardPage.tsx` | Stats cards (users/tracks/subscribers), recent user list |
| User Manage | `UserManagePage.tsx` | User table with search, role change via ConfirmModal |
| Company Cert | `CompanyCertManagePage.tsx` | Cert list with status filter, approve/reject via ConfirmModal |
| Tag Manage | `TagManagePage.tsx` | Full CRUD with create/edit/delete modals |
| Notice Create | `NoticeCreatePage.tsx` | Notice creation form (title, content, pinned) |
| Notice Edit | `NoticeEditPage.tsx` | Notice edit + delete with ConfirmModal |

API layer additions:
- `api/admin.ts` (new): Dashboard stats, user management, company certification APIs
- `api/tags.ts` (appended): `createTag`, `updateTag`, `deleteTag`
- `api/notices.ts` (appended): `createNotice`, `updateNotice`, `deleteNotice`

## Risk

- LOW: All changes are new files or appended functions. No existing functionality modified.
- Dashboard stats aggregation has no dedicated backend endpoint; uses existing paginated APIs to extract `total` count from `pageInfo`. `totalSubscribers` is hardcoded to 0 until a stats API is added.

## Verification (User Action Required)

Node.js was not available in the agent's bash PATH. Please run these commands manually:

```bash
cd frontend
npm run typecheck    # Expected: 0 errors
npm run lint         # Expected: 0 errors
npm run build        # Expected: success (dist/ created)
```
