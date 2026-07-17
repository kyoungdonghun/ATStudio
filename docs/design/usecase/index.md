---
version: 15.0
last_updated: 2026-07-17
project: ATS
owner: docops
category: registry
status: stable
dependencies:
  - path: ../api-spec.md
    reason: Current method/path contract
  - path: ../db-schema.md
    reason: Current persistence contract
---

# Use Case Specification Index

> Purpose: Navigate current domain use cases. API and DB counts are owned by the
> current API and DB specifications; this index does not maintain a second
> aggregate count.

## Current Files

| File | Current subject |
|---|---|
| [sound-track.md](sound-track.md) | Track create/list/detail/update/delete, Public Listening, Official Download |
| [sound-tag.md](sound-tag.md) | Tag management and available-tag reads |
| [sound-playlist.md](sound-playlist.md) | Subscriber playlist CRUD and Track membership |
| [sound-playhistory.md](sound-playhistory.md) | Browser-local Play History |
| [sound-album.md](sound-album.md) | Album CRUD, Track membership/order, and likes |
| [likes.md](likes.md) | Track and album likes |
| [download-queue.md](download-queue.md) | Official Download History; legacy filename retained for link stability |
| [user-info.md](user-info.md) | Registration, auth/profile, password, withdrawal, ADMIN users/stats |
| [user-subscription.md](user-subscription.md) | Recurring subscription lifecycle and emergency ADMIN controls |
| [user-license.md](user-license.md) | User and ADMIN License reads |
| [user-question.md](user-question.md) | Inquiry, answer, attachment, and ADMIN status |
| [user-notice.md](user-notice.md) | Notice reads and ADMIN mutations |
| [whitelist.md](whitelist.md) | User whitelist and ADMIN export workflow |
| [company-certification.md](company-certification.md) | BUSINESS certification and protected review documents |
| [business-license.md](business-license.md) | Redirect/reference to company certification |
| [util.md](util.md) | Six current Utility API use cases |

## Current Cross-Cutting Rules

- Public Listening serves the complete active Track through the controller and
  does not create download or License records.
- Official Download remains subscription/License/quota controlled and records
  `track_downloads`.
- Play History is browser-local.
- The subscriber download screen is `/downloads`.
- Subscription purchase and change use TOSS recurring billing only.
- Direct subscription creation and removed compatibility APIs/routes are not
  current use cases.
- Site settings are read publicly and updated through the ADMIN path.
