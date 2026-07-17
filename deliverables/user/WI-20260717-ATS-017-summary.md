# WI-20260717-ATS-017 요약

## 결과

ATStudio V1의 활성 current-state 문서를 공식 기준에 맞춰 마감했습니다.

- 공식 V1 기준 브랜치는 `codex/p1-acceptance-hardening`입니다.
- 별도 client-demo 브랜치는 더 이상 유지하지 않습니다.
- 삭제된 수동 SQL과 레거시 단건결제/직접 구독 API 및 콜백 경로는 현행 운영 경로가 아닙니다.
- SR-92는 `DROPPED (rejected and retired)` 상태를 유지합니다.
- SR-93은 로컬 품질 마감과 별개인 실제 production gate만 남긴 채 OPEN 상태를 유지합니다.

## 최종 기준

- 문서: Standards 13개, 전체 Markdown 194개
- 인벤토리: API 137개, DB 테이블/JPA 엔티티 39/39개, 화면 53개
- DB: 39 tables, 449 columns, 153 indexes, 80 foreign keys
- DB manifest SHA-256: `c48d3c75378aaf2364d89ed06833ba68e27a5a334dbc4670d1443bd938c6c506`
- 백엔드: 1,208 tests, 0 failures/errors, 9 environment-dependent skips
- JaCoCo: instruction 85.673%, branch 71.682%, line 85.726%, method 82.931%
- 프론트엔드: 468 tests, 0 failures
- 프론트엔드 coverage: statements 86.73%, branches 76.98%, functions 85.41%, lines 88.75%
- Typecheck, ESLint, Prettier, build: PASS

## 추가 리뷰 수정

SR-93에 `app.payment.provider`가 legacy/non-subscription one-time provider setting으로 남아 있다는 잘못된 설명을 발견해 수정했습니다.

현행 V1에서는 다음 항목이 모두 제거되어 있습니다.

- `app.payment.provider`
- `APP_PAYMENT_PROVIDER`
- 레거시 단건결제 provider 경로

전체 검색에서 남은 참조는 SR-93의 명시적 부재 설명과 `V1BackendBaselineContractTest`의 금지 assertion뿐이며, 실제 런타임 소스/리소스/프론트엔드에서는 0건입니다.

## 변경 파일

- `docs/index.md`
- `docs/SR/SR-42.md`
- `docs/SR/SR-92.md`
- `docs/SR/SR-93.md`
- `docs/design/db-schema.md`
- `docs/design/payment-operations-runbook.md`
- `docs/client/_internal-feature-map.md`
- `docs/client/testing-guide.md`
- `docs/payment/index.md`
- `docs/payment/feature-inventory.md`
- `docs/payment/acceptance-test-checklist.md`
- `docs/payment/known-limits-and-next-steps.md`
- `docs/registry/project-registry.md`

## 검증

- `validate_docs.py`: PASS
  - Tier 0 문서 존재
  - 내부 링크 오류 0건
  - 추적 ID 443개 형식 일치
  - 문서 인덱스 누락 0건
- 활성 stale reference 검색: 0건
- provider 설정 런타임 검색: 0건
- 명시적 부재 설명/금지 assertion 외 provider 검색 잔존: 0건
- `git diff --check`: PASS
  - 공백 오류 없음
  - Windows LF-to-CRLF 안내만 출력

## 남은 Production Gates

1. Production DB 전략 확정: 검증된 빈 DB로 V1을 시작하거나, 별도 승인된 retained-data migration을 설계·리허설해야 합니다.
2. Production secrets를 구성하고 live Toss 결제, 갱신, reconciliation, refund, billing-key cleanup을 검증해야 합니다.
3. HTTPS/proxy/CORS, secret 관리, backup/restore, scheduler owner, log, alert, incident response를 검증해야 합니다.
4. 새로 검증된 운영자 통제 환경에서 client acceptance를 완료해야 합니다.
5. Production 및 최종 release 승인을 명시적으로 기록해야 합니다.

위 gate가 증거로 확인되기 전까지 SR-93은 OPEN입니다.

## Rollback

- WI-017이 수정한 위 13개 current-state 문서만 역패치하거나 해당 문서 변경 커밋만 revert합니다.
- WI 전체를 되돌릴 때는 이번에 새로 만든 WI-017 Evidence Pack과 사용자 요약만 제거합니다.
- 기존 REQ/WI 증거, archived 설계 기록, 코드, 테스트, DB, 로컬 설정, 생성 PDF, Git refs/index, 다른 작업자의 변경은 건드리지 않습니다.
