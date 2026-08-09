---
version: 22.1
last_updated: 2026-08-09
project: ATS
owner: SA
category: design
status: confirmed
dependencies:
  - path: ../../src/main/resources/schema.sql
    reason: Authoritative V1 DDL
  - path: ../../src/main/resources/seed.sql
    reason: Authoritative six-plan baseline data
  - path: ../../src/main/java/com/atstudio/atstudio/entity/
    reason: Authoritative JPA entity mappings
  - path: api-spec.md
    reason: Current API persistence consumers
---

# ATStudio DB Schema Definition v22.1

## V1 Baseline

ATStudio V1 has **41 tables and 41 JPA entities**. Both counts are derived from
the current working tree:

- 41 unique `CREATE TABLE` statements in
  `src/main/resources/schema.sql`.
- 41 Java types annotated with `@Entity` under
  `src/main/java/com/atstudio/atstudio/entity/`.

The checked-in DDL is a fresh-only, fail-closed baseline:

1. Create a verified-empty MySQL 8 database.
2. Apply `schema.sql` once.
3. Apply `seed.sql` once.
4. Start the backend with Hibernate `ddl-auto=validate`.
5. A second schema application is expected to fail; the file is not an
   idempotent migration mechanism.

The retired manual SQL directory is not part of the current runtime,
bootstrap, or operator workflow. Existing databases are not upgraded by this
repository baseline. Any retained-data migration requires a separate approved
requirement and migration design.

### Verified Current Manifest

WI-20260809-ATS-001 verified the current fresh disposable manifest on an
isolated loopback database. The historical 2026-07-17 39-table manifest is a
predecessor record only; it is not the current certification baseline.

| Object | Count |
|---|---:|
| Tables | 41 |
| Columns | 493 |
| Indexes | 168 |
| Foreign keys | 89 |
| Seeded subscription plans | 6 |
| Manifest SHA-256 | `c581bef61cfba143744882b0674daf8d8fe742d82adbbf66d6b61699f5b86333` |

The verified manifest matches the current source: 41 canonical `CREATE TABLE`
statements, 41 JPA entities, and the listed column, index, foreign-key, and
seeded-plan totals. The bootstrap validator now enforces these values and the
SHA-256 above. The WI-022 source-derived reconciliation and its historical
39-table baseline remain preserved in its evidence record.

## Baseline Data Ownership

`src/main/resources/seed.sql` is the sole runtime baseline-data owner. It
contains one `INSERT INTO subscriptions` statement with exactly six plan rows:

| User type | Plans |
|---|---|
| `INDIVIDUAL` | `STANDARD`, `DELUXE`, `PREMIUM` |
| `BUSINESS` | `STANDARD`, `DELUXE`, `PREMIUM` |

The acceptance subscription-plan bootstrap runner validates this baseline; it
does not create a second copy. QA users, demo Tracks, playlists, tags, and
notices are explicit non-baseline workflows.

## Complete Table Inventory

### Identity and Access (4)

| Table | Primary role |
|---|---|
| `users` | User identity, role, type, profile, and soft deletion |
| `social_accounts` | Social identity linkage |
| `email_verification_tokens` | Single-use email verification |
| `password_reset_tokens` | Single-use password reset |

### Administrator Operations (2)

| Table | Primary role |
|---|---|
| `admin_operation_audit_logs` | Append-only role-change and local-correction security audit without foreign keys |
| `admin_subscription_corrections` | Explicit local subscription correction request/approval/execution ledger |

### Subscription and Payment (12)

| Table | Primary role |
|---|---|
| `subscriptions` | Six plan definitions |
| `user_subscriptions` | Current access, cycle, expiration, and pending change |
| `billing_agreements` | Encrypted recurring billing agreement |
| `payment_orders` | Stable payment command and provider-attempt ledger |
| `subscription_payments` | Finalized subscription charges |
| `payment_settlements` | Imported/generated settlement evidence |
| `payment_refunds` | Refund workflow and provider result ledger |
| `payment_entitlement_corrections` | Refund-linked local access correction |
| `payment_reconciliation_incidents` | Persistent mismatch and cleanup incidents |
| `payment_receipts` | Receipt evidence |
| `payment_operation_audit_logs` | Append-only payment operation audit |
| `licenses` | Track usage License issued by Official Download |

### Track, Discovery, and Collections (12)

| Table | Primary role |
|---|---|
| `tracks` | Track metadata and private storage keys |
| `tags` | Tag catalog |
| `track_tags` | Track-to-Tag mapping |
| `albums` | Curated album metadata |
| `album_tracks` | Album-to-Track mapping and ordering |
| `album_likes` | User album likes |
| `playlists` | Subscriber playlist metadata |
| `playlist_tracks` | Playlist-to-Track mapping and ordering |
| `likes` | User Track likes |
| `track_downloads` | Official Download history and daily accounting |
| `whitelist_channels` | User whitelist channel workflow |
| `site_settings` | Public/admin site settings |

### Whitelist and Certification Evidence (5)

| Table | Primary role |
|---|---|
| `whitelist_export_batches` | Immutable export batch metadata |
| `whitelist_export_items` | Immutable export row snapshots used by current CSV output |
| `company_certifications` | BUSINESS certification workflow |
| `company_certification_documents` | Protected document metadata |
| `company_certification_audit_logs` | Certification transition audit |

### Questions, Notices, and Storage (6)

| Table | Primary role |
|---|---|
| `questions` | Inquiry records |
| `answers` | Inquiry answers |
| `question_attachments` | Inquiry attachment metadata |
| `notices` | Notices |
| `notice_attachments` | Notice attachment metadata |
| `storage_mutations` | Durable storage mutation journal |

Total: **41 tables**.

## Current Contract Boundaries

### Track Media

- `tracks.audio_file` is the private storage key used by controller-mediated
  Public Listening and Official Download.
- Public DTOs do not expose private storage keys.
- There is no separate preview-column or preview-generation contract.
- Browser-local Play History does not own a database table.
- Track creation and audio replacement persist duration and waveform from one
  decoded-PCM analysis result. Existing rows are not rewritten by the
  read-only audio-analysis dry-run.
- New or replacement Track thumbnails must be square and are canonicalized to
  JPEG. Existing non-square thumbnail keys remain unchanged unless an operator
  explicitly uploads a replacement.

### Administrator Audit and Subscription Correction

- `admin_operation_audit_logs` stores minimal before/after state and outcome
  snapshots for administrator role changes and local subscription corrections.
  IDs are snapshots rather than foreign keys.
- Rejected role-change, ADMIN-withdrawal, and local-correction rows retain the
  stable action, target, actor, error code, and equal bounded state snapshots,
  but persist `reason_note` as null. Successful role-change/correction audits
  may retain the approved operator reason.
- `admin_subscription_corrections` is separate from refund-linked
  `payment_entitlement_corrections`. It records a local entitlement target,
  reason, approval/execution state, actors, timestamps, and failure state.
- Local correction may update `user_subscriptions` and local
  `billing_agreements` state after revalidation and ordered locking. It does not
  create a provider charge, refund, billing-key deletion, or email side effect.

### Official Download

- `track_downloads` records current download history.
- License issuance, plan quota, first-download serialization, re-download, and
  Track download-count behavior remain part of the backend contract.
- There is no Download Queue table.

### Payment Provider

- `PaymentProviderType` contains only `TOSS`.
- Every persisted provider column uses the `TOSS` V1 identity.
- `subscription_payments.provider` is non-null.
- Recurring, status-lookup, and refund interfaces remain provider-neutral so a
  future approved provider can be added atomically across enum, DDL, adapter,
  tests, and documentation. No speculative provider value is present now.

### Billing Key Encryption

- Billing keys use only the V2 key-ID envelope.
- `app.payment.billing.active-key-id` selects the write key.
- `app.payment.billing.encryption-keys` supplies active and retained V2 keys.
- Missing, blank, duplicate, unknown, or placeholder key configuration fails
  closed without logging key values.

### Whitelist Export

Current immutable export evidence retains the fields consumed by the CSV and
replay paths, including email, channel, plan, order, and transition context.
Removed user-ID and nickname snapshot columns are not part of the V1 schema.

## Runtime Configuration

- Base, acceptance, and explicit local profiles use
  `spring.jpa.hibernate.ddl-auto=validate`.
- The base configuration does not automatically import
  `application-local.yml`.
- Operators load an ignored local file explicitly, for example with
  `--spring.config.additional-location=file:./application-local.yml`.
- `application-local.yml` remains untracked and must never be copied into
  documentation or evidence.

## Verification

```powershell
$sql = Get-Content -Raw src/main/resources/schema.sql
([regex]::Matches($sql, '(?im)^CREATE\s+TABLE\s+')).Count

Get-ChildItem src/main/java/com/atstudio/atstudio/entity -Recurse -Filter *.java |
  Where-Object { Select-String -LiteralPath $_.FullName '^\s*@Entity\b' -Quiet } |
  Measure-Object
```

Expected results: `41` and `41`.
