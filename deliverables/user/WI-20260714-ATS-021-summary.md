# WI-20260714-ATS-021 Summary

## 결론

WI-021 disposable MySQL rehearsal은 부분 완료/차단 상태입니다.

- MySQL 8 접근, disposable DB 생성/적용/검증/drop은 성공했습니다.
- fresh schema와 관련 manual patch 적용은 성공했습니다.
- payment/storage ENUM, unique index, check constraint, insert/flush 검증은 성공했습니다.
- focused contract test 2개 클래스, 총 7개 테스트는 통과했습니다.
- Hibernate `ddl-auto=validate`는 실패했습니다.

차단 원인은 payment/storage가 아니라 기존 fresh schema 정합성입니다.

```text
Schema validation: missing column [waveform_data] in table [tracks]
```

`Track.waveformData`는 엔티티에 존재하지만 fresh `schema.sql`의 `tracks` 테이블에는 `waveform_data` 컬럼이 없습니다. WI-021 범위상 production code/schema는 수정하지 않았고, blocked evidence로 기록했습니다.

## 안전 확인

- 사용한 DB는 WI-021 전용 disposable DB뿐입니다.
- 기존 local/application DB는 schema apply/validate 대상이 아니었습니다.
- disposable DB drop 확인:
  - `rehearsal-jdbc.log`: `cleanup.database.exists: 0`
  - `drop-after-hibernate-validate-retry.log`: `cleanup.database.exists: 0`
- 제가 만든 임시 `.class` 파일은 제거했습니다.
- 남은 로그에서 실제 secret 값 hit는 0개, 로그 내 전체 JDBC URL hit도 0개입니다.

## 산출물

- Agent evidence: `deliverables/agent/WI-20260714-ATS-021-evidence-pack.md`
- Repro tools/logs: `deliverables/agent/WI-20260714-ATS-021/`
