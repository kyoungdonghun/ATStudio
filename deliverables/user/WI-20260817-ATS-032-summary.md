# WI-20260817-ATS-032 Integration Verification Summary

## 결과: Local isolated rehearsal scope verified

WI-030은 일부만 완료되었고 WI-031은 자체 산출물을 남기기 전에 중단되었습니다. 이 문서는 WI-031의 격리 리허설에서 MA가 제공한 검증 관찰을 보정 기록으로 남깁니다. 검증은 로컬 격리 리허설에 한정되며, 외부 효과 단계의 통과나 출시 준비 완료를 뜻하지 않습니다.

## 통과한 로컬 범위

| 구분 | 확인된 관찰 | 확인하지 않은 항목 |
|---|---|---|
| Public browser UI | loopback proxy에서 공개 카탈로그, tag 필터, 상세 재생, 앨범 전체 재생, waveform/progress가 보였고 browser console error가 없었습니다. 데모 fixture는 10개 track, 1개 10-track album, category tag로 구성되었습니다. | 외부 공개 URL 또는 Cloudflare 경유 동작 |
| Authentication / administrator UI | CORS 보정 후 QA administrator browser login이 성공했고 `/admin/dashboard`, `/admin/payments`가 console error 없이 로드되었습니다. | 결제 실행, provider 응답, 환불 또는 운영자 결제 변경 |
| Subscriber download / persisted state | QA subscriber가 UI에서 demo track 1개를 다운로드한 뒤 `/downloads`에 history 1건과 일일 `1 / 20`이 표시되었습니다. 이는 application download flow의 영속 결과 증거입니다. | browser download-event capture는 timeout이므로 browser download-event API 성공으로 주장하지 않습니다. |
| Runtime configuration | 새 schema-and-seed rehearsal DB에서 Hibernate `ddl-auto=validate`로 backend가 기동되었습니다. | client acceptance runtime, production runtime, 백업/복구 또는 scheduler 검증 |

## CORS 발견 및 격리 보정

초기 browser password login은 disposable runner CORS allowlist에 `127.0.0.1` origin이 빠져 HTTP 403으로 재현되었습니다. 그 시점에도 direct backend login과 `/api/users/me`는 성공했으므로, 확인된 문제는 browser-origin 경계였습니다.

disposable runner의 allowlist만 loopback browser origin을 포함하도록 확장한 뒤 QA administrator browser login이 성공했습니다. 이 보정은 미추적 일회성 리허설 artifact에만 적용되었으며, repository source, tracked application configuration, client acceptance runtime에는 변경이 없습니다.

## 증거 경계

- 실행하지 않음: Gmail/SMTP delivery, Toss 또는 기타 provider, payment/refund, Cloudflare tunnel, backup/restore, production check.
- provider와 SMTP endpoint는 loopback fail-closed였고, 외부 요청은 수행하지 않았습니다.
- browser UI 관찰, direct API 선행 확인, download history 영속 결과는 서로 대체하지 않습니다.

## 다음 승인 게이트

1. WI-023 외부 효과 리허설은 exact account, provider mode, SMTP recipient, expected records, rollback을 포함한 실행 계획과 즉시 전 별도 승인이 필요합니다.
2. WI-024 backup/restore, monitoring, scheduler 리허설은 exact database scope 승인 없이는 시작할 수 없습니다.
3. WI-025 독립 증거 검토는 WI-023과 WI-024 완료 후, WI-026 current-state documentation과 release report는 WI-025 완료 후에 진행합니다.

## 변경 파일

- `deliverables/user/WI-20260817-ATS-032-summary.md`
- `deliverables/agent/WI-20260817-ATS-032-evidence-pack.md`
