# WI-20260714-ATS-017 Summary

## 결과

- `scripts/acceptance/AcceptanceLifecycle.psm1`의 readiness 판정을 보정해 HTTP `200-399`만 성공으로 처리하도록 수정했습니다. 이제 `403`/`404`는 ready로 오판되지 않습니다.
- `Start-AcceptanceEnvironment`는 `$completedSuccessfully` 플래그와 `finally` 기반 cleanup 구조로 보강되어, 비정상 종료 시 owned process만 `tunnel -> frontend -> backend` 순서로 정리합니다.
- `scripts/acceptance/test-dry-run.ps1`는 실제 서버/터널을 띄우지 않고도 다음 계약을 검증하도록 갱신했습니다.
  - mock HTTP 응답으로 `200`, `399` 성공 / `403`, `404` 실패
  - catch 가능한 startup failure에서 `finally` cleanup 순서 검증
  - no-manifest `status.ps1` / `stop.ps1`
  - `completedSuccessfully + finally` 구조에 대한 정적 확인

## 검증 결과

- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\acceptance\test-dry-run.ps1`
  - PASS
  - checks:
    - `parser`
    - `quick-tunnel-url-parser`
    - `public-base-url-validation`
    - `dry-run-contract`
    - `status-no-manifest`
    - `stop-no-manifest`
    - `readiness-http-status-contract`
    - `abnormal-start-cleanup-contract`
    - `start-finally-structure`
    - `secret-free-dry-run-output`
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\acceptance\status.ps1 -RuntimeRoot "$env:TEMP\atstudio-acceptance-empty-check"`
  - PASS (`state: not-started`)
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\acceptance\stop.ps1 -RuntimeRoot "$env:TEMP\atstudio-acceptance-empty-check"`
  - PASS (`state: not-started`)
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\acceptance\start.ps1 -RuntimeRoot "$env:TEMP\atstudio-acceptance-dry-run-check" -DryRun`
  - PASS (`dryRun: true`)

## 보정 메모

- 이전 시도에서 `PipelineStoppedException`을 테스트 안에서 직접 던지는 방식은 PowerShell 호스트를 종료시켜 `test-dry-run.ps1` 자체를 실패시키는 문제가 있었습니다.
- 현재는 실제 Ctrl+C 보장은 구현 구조(`completedSuccessfully` + `finally`)로 유지하고, 테스트는 호스트를 종료하지 않는 방식으로 계약을 검증합니다.

## 범위 준수

- 실제 Spring/Vite/cloudflared/DB/Toss/SMTP는 실행하지 않았습니다.
- `application/payment/storage/image/auth` 관련 파일은 수정하지 않았습니다.
- 기존 untracked logs 4개는 건드리지 않았습니다.
