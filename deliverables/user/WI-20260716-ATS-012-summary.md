---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: docops
category: work-summary
status: stable
dependencies:
  - path: REQ-20260716-ATS-002.md
    reason: Approved remediation scope
  - path: ../agent/WI-20260716-ATS-012-handoff.md
    reason: Documentation and PDF execution contract
  - path: ../../docs/design/remaining-remediation-design-20260716.md
    reason: P2-12 through P2-15 and P3-01 through P3-02 status
---

# WI-20260716-ATS-012 Summary

## Outcome

The current design, implementation, operational, UI, SR, registry, and client documents now describe one observed system. The client testing PDF is reproducible from the seven approved Korean source files and has been regenerated after independent layout findings.

No backend, frontend, schema, runtime, client-demo worktree, provider, database, secret, stage, commit, or push operation was performed by WI-012.

## Authoritative Current Behavior

- Billing agreement preparation matches the current Java records and TypeScript interfaces. Existing-subscriber payment-method re-registration uses `purpose=BILLING_AGREEMENT` and `amount=0`; it does not charge or change the current plan or paid period.
- The active React SPA play-history screen uses browser `localStorage` key `playHistory`, keeps at most 100 de-duplicated entries, and records only after playback starts. The three `/api/play-histories` endpoints and `play_histories` table are retained compatibility surfaces with no SPA synchronization.
- Admin dashboard statistics are `totalUsers`, `totalTracks`, `totalSubscribers`, and `recentUsers`. Site settings read and upsert `COMPANY_CERT_GUIDE`; an absent public value is returned as an empty string.
- Legacy direct/one-time subscription endpoints and callback aliases remain explicit compatibility boundaries. Removal requires approved caller/telemetry evidence, tested recurring replacements, coordinated API/route/test/client-document updates, and rollback guidance.
- Phase 2 React/Vite is active. `deliverables/user/` and `deliverables/agent/` are the current tracking source of truth; CTX/workboard paths are advisory, not parallel authority.

## Exact Current Counts

| Surface | Current count | Unit and rule |
|---|---:|---|
| REST API | 149 | Method-level mapping annotations in 24 `@RestController` classes; excludes one non-REST SPA forwarding mapping and class-level mappings |
| Database | 41 / 41 | `CREATE TABLE` declarations in `schema.sql` / Java files with `@Entity` |
| Router | 63 | 62 path route objects plus 1 index redirect |
| Page UI | 53 | 54 `lazyPage(...)` declarations minus the `/playlists/new` modal adapter; includes 2 error UIs |
| Payment aliases | 8 routes | One `SubscriptionPaymentPage` UI plus 7 extra callback/legacy aliases |
| Modal inventory | 23 / 17 | `<Modal` render occurrences / non-test TSX files |
| Agents | 13 | Markdown definitions under `.claude/agents/` |
| SR | 92 | 82 DONE, 7 OPEN, 2 NOT CONFIRMED, 1 DROPPED |
| Managed documents | 193 | Root-index direct-file rule; Design is recursive, category `index.md` files are excluded |

The old numeric comments inside `frontend/src/router/index.tsx` do not match the declarations. Product-code edits are forbidden in this WI, so the current UI document records the source-derived count and leaves comment-only correction to a code-owned follow-up.

## WI-005 Through WI-011 Alignment

Current documents now carry forward the implemented security/rate-limit boundaries, recurring-payment key and reconciliation behavior, whitelist state/export limits, company-certification file/review/audit behavior, typed OAuth and catalog/download concurrency behavior, frontend state/retry/accessibility contracts, full-tree formatting baseline, and coverage observations.

Coverage remains a risk baseline, not a release threshold. Key observations are backend branch coverage 59.05%, frontend branch coverage 34.00%, and frontend function coverage 27.82%.

The development branch resolves Vite 6.4.3. Both `npm audit --omit=dev` and unfiltered `npm audit` return 0, so there is no development-branch residual advisory. The frozen `codex/client-demo-stable` branch is a separate read-only environment boundary: Vite 6.4.1, 5 production findings, and 13 total findings. It was not modified and is not an approved public Vite dev-server target without user-authorized remediation or controlled access.

## Client PDF

- Generator: `scripts/docs/generate_client_testing_pdf.py` version 1.3.0.
- Output: `output/pdf/atstudio-client-testing-guide.pdf`.
- Final result: 12 pages, 169,090 bytes.
- SHA-256: `7a1db067628e44aa49e2f2febe455304cc88cc1ff2bc6edfdab9ae7a2fe7db1f`.
- PDF title: `AT.M 클라이언트 테스트 가이드`.
- Source text check: 272/272 segments present; no U+FFFD replacement character.
- Determinism: a second generation produced the same SHA-256.
- Layout correction: headings stay with following content, parser-built contiguous lists keep their immediately preceding heading in the same group, and later source boundaries use a conditional page break. Final page 5 completes section 3, page 6 starts section 4 with its list, page 8 completes company certification, and page 9 starts whitelist with its list.
- Render review: all 12 pages rendered at 144 DPI and inspected. Independent MA review confirmed pages 5, 6, 8, 9, 10, 11, and 12 have the intended boundaries with no clipping, overlap, or broken Hangul; the remaining pages also passed docops visual review.
- All 24 docops-owned v1.2/v1.3 render PNGs were removed after verification. The MA-owned `tmp/pdfs/wi012-independent-v2/` evidence was left untouched.

## Verification

- Documentation validator: PASS; Tier 0 present, no broken links, 402 supported traceability IDs, all documents indexed.
- Index count contract: PASS; category total 193.
- Live `docs/` U+FFFD count: 0.
- Metadata scan: 210 Markdown files, 96 with frontmatter, 114 without; invalid current `category`/`status` values: 0. Historical files were not bulk-rewritten.
- PDF verifier: PASS; 12 pages, Unicode title, 272/272 segments, manifest/output hash match.
- All-page render signal: 1191x1684 pixels per page, nonwhite ratio 0.0182-0.0534, edge contact 0 on every page.
- `git diff --check`: PASS with non-failing line-ending warnings only.
- Frozen client-demo branch: HEAD `cd876fcf84b3cb2490c27420c6c53a87a35b982d`, clean before and after read-only checks; package and lock hashes unchanged.

## Remaining Boundaries

- Social-only account withdrawal behavior still requires an approved product policy.
- Retained MySQL manual patches, real payment/OAuth providers, secrets, trusted-proxy/multi-egress behavior, production monitoring, and public runtime evidence remain environment-conditional.
- The current low coverage baseline requires later risk-based tests; it is not hidden by dependency or formatting success.
- Public exposure of a Vite dev server requires a patched compatible toolchain or a controlled-access alternative. The frozen client-demo branch does not currently meet the patched-toolchain condition.
- The supplied PDF override wrapper points to a missing Poppler path; generation used the bundled `native/poppler/Library/bin/pdftoppm.exe` and records that exact boundary in the manifest.

## Related Documents

- [Evidence Pack](../agent/WI-20260716-ATS-012-evidence-pack.md): Three-way matrix, exact commands, hashes, render evidence, and rollback.
- [Handoff](../agent/WI-20260716-ATS-012-handoff.md): Approved execution contract.
- [Remaining Remediation Design](../../docs/design/remaining-remediation-design-20260716.md): Current closure and retained status.
