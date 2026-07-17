# WI-20260717-ATS-004 Summary

## 상태

WI-004의 V1 백엔드 DB/config/payment-provider 기준선 정리와 검증을 완료했다. 중단 시점에 남아 있던 6개 실패는 production 결함이 아니라 enum 정규화 뒤 남은 테스트 fixture 불일치였으며, 지정된 네 테스트 파일만 보정했다.

## 최종 보정

| 대상 | 결과 |
|---|---|
| `PaymentControllerTest` | JSON provider 기대값을 `TOSS`로 정규화했다. |
| `AdminPaymentSettlementServiceTest` | CSV 3개 행의 provider를 `TOSS`로 정규화했다. |
| `AdminPaymentRefundServiceTest` | 유효한 `TOSS`를 invalid 값으로 사용하던 사례를 `null` provider 사례로 교체했다. |
| `PaymentReconciliationTransactionServiceTest` | 단일 enum으로 만들 수 없는 provider mismatch fixture를 제거하고 order/status/transaction incident만 검증했다. |

Production의 refund fail-closed 검사와 reconciliation provider-mismatch 분기는 변경하지 않았다.

## V1 기준선 증거

| 항목 | 결과 |
|---|---|
| Provider | persisted V1 provider는 `TOSS` 하나이며 확장용 provider interface는 유지했다. |
| Billing key | V2 key-ID AES-GCM 경로만 유지하고 V1 envelope/property 경로를 제거했다. |
| Configuration | base config의 local 자동 import를 제거하고 local loading을 명시적으로 전환했다. Acceptance와 local은 `ddl-auto=validate`를 사용한다. |
| Schema/seed | fresh-only 39-table schema와 정확히 6개 plan을 소유하는 단일 seed 경로를 확정했다. |
| Disposable MySQL | first apply PASS, second apply expected failure, manifest/Hibernate validate/MySQL races/cleanup PASS. |
| Local recreation | loopback·정확한 DB명·세션 preflight 뒤 local `atstudio` 재생성, manifest와 Hibernate validate PASS. |
| Manual SQL | disposable proof 당시 9개 존재를 확인한 뒤 삭제했으며 local recreation은 0개 gate로 통과했다. |
| Runtime preflight | 현재 5173/8080 listener와 `cloudflared` process가 모두 0이다. |

## 최종 검증

| 검사 | 결과 |
|---|---|
| 지정 4개 클래스 | 28 tests, failures/errors 0 |
| 전체 백엔드 | 147 suites, 1,074 tests, failures/errors 0, 기존 환경 조건 skip 9 |
| JaCoCo | `jacocoTestReport` PASS, XML/HTML 생성 |
| Build | `gradlew.bat build` PASS |
| Diff integrity | unstaged/staged `git diff --check` PASS |
| Exact residual | legacy provider/config/crypto/one-time endpoint/schema/seed 잔존 0, manual SQL 0 |
| Secret scan | ignored local 값 미열람, high-confidence candidate 0 |

JaCoCo 전체 저장소 수치는 line 78.83%, branch 59.65%, method 79.09%, instruction 78.05%다. 현재 `build.gradle`에는 coverage verification threshold가 없고 WI-004의 명시 gate는 report 생성이므로 완료 조건은 통과했지만, 일반 개발 표준 목표보다 낮은 수치는 별도 전역 품질 부채로 남는다.

## 범위 메모

- 이번 완료 단계에서는 frontend, active docs, Git refs를 수정하지 않았다.
- active docs에 남아 있는 retired manual SQL 경로 정리는 downstream WI-005/WI-006 범위다.
- 기존 WI-002/WI-003 변경과 기타 미추적 산출물은 되돌리거나 stage하지 않았다.
- 상세 명령, redacted MySQL 증거, 해시 대조, residual/secret scan은 `deliverables/agent/WI-20260717-ATS-004-evidence-pack.md`에 기록했다.
