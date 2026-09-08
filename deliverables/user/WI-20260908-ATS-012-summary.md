---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: docops
category: work-summary
status: stable
dependencies:
  - path: REQ-20260908-ATS-002.md
    reason: Approved WI012 scope
  - path: ../agent/WI-20260908-ATS-012-evidence-pack.md
    reason: Execution evidence and verification boundaries
---

# WI-20260908-ATS-012: Backend Gmail Restoration

Current status: **backend Gmail restoration, actual website-triggered send, and user-reported receipt/link opening passed**. The user confirmed "열렸어. 다음은 뭐야?". This does not claim a new password was submitted or that future deliverability is guaranteed. MA performed runtime/browser work and finalized this record after DocOps preparation.

- WI011 receipt is confirmed for three messages by the user-image observations supplied in this delegation: readable Korean templates and Inbox labels. The same-subject subscription messages are grouped with TEST1/3 and TEST2/3; the admin message carries TEST3/3. DocOps did not independently inspect the images. No future deliverability guarantee follows.
- MA replaced owned backend PID24016 with PID24792 at 21:55 KST using the same pinned JAR, DB, storage and public origin. Frontend16160 and Tunnel1888 were not restarted. The existing Gmail settings now override local SMTP settings using explicit child-process Spring mail properties; credential source files are untouched.
- The public website's normal forgot-password form was submitted once. At 21:56:54.194 KST, the actual backend logged EmailService SUCCESS; subject `[AT.M] 비밀번호 재설정 안내`. UI receipt alone was not treated as proof. The user later confirmed receipt/link opening; WI012 Inbox/Spam placement and final password submission were not separately verified. Only the normal reset-token flow ran, with no subscription/payment changes.
- Only owned documents were edited. Historical evidence and production/SR-93 gates remain **OPEN**; no financial/provider/manual scheduler operations, runtime/product/script changes, secret inspection, external requests or Git actions were performed by DocOps.
- Local/public HTTP and public-origin CORS passed. The pinned runtime launcher passed syntax/preflight and invalid-origin/occupied-port rejection checks. Historical storage warnings remain unchanged at 10 missing references out of 30 checked. Documentation validation is recorded in the evidence pack. The later user request authorized scoped commit/push before stage 2 baseline assessment; no deployment or financial action follows automatically.

Evidence: [WI012 Evidence Pack](../agent/WI-20260908-ATS-012-evidence-pack.md). `Blocks: -`; no downstream WI.
