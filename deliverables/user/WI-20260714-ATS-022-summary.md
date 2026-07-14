# WI-20260714-ATS-022 Summary

## 결론

WI-022 공개 Cloudflare acceptance smoke는 핵심 경계 검증을 완료했지만, 최종 판정은 **부분 완료 / client sharing NO-SHARE**입니다.

- acceptance profile, disposable MySQL, 테스트 사용자 bootstrap, Toss TEST 설정으로 공개 환경을 기동했습니다.
- 로컬/공개 SPA와 `/api/tracks`는 모두 200이었습니다.
- `/api`와 `/uploads`는 동일 origin 경계를 유지했고, 공개 Playlist 썸네일의 안전 헤더와 로컬/공개 본문 길이 일치를 확인했습니다.
- 원본 Track 오디오와 Company Certification 문서는 익명 401, 인증 ADMIN 403으로 차단됐습니다.
- unknown Host는 backend 400, Vite/public ingress 403으로 거부됐습니다.
- 전달 헤더를 번갈아 위조해도 로그인 rate-limit bucket을 우회하지 못하고 429에 도달했습니다.
- ADMIN 로그인, `/api/admin/stats`, SPA 관리자 경로, logout 204, logout 후 refresh replay 401을 확인했습니다.
- Toss success/fail callback SPA 경로는 200이었고 실제 결제나 provider 호출은 수행하지 않았습니다.

## 제한사항

- 별도 외부 egress가 없어 두 실제 외부 클라이언트의 identity 분리는 검증하지 못했습니다.
- fresh schema에는 구독 플랜 seed가 없어 subscriber fixture의 활성 구독 생성이 건너뛰어졌습니다. USER 로그인은 성공했지만 Playlist/구독 API는 403으로 닫혔으므로 subscriber 성공 경로는 미검증입니다.
- 인증된 브라우저 화면 전환은 수행하지 않았습니다. 역할 API와 SPA route shell까지만 검증했습니다.
- Toss는 callback/readiness만 확인했습니다. SDK 실행, billing auth, 결제 승인, provider mutation은 수행하지 않았습니다.

따라서 이 실행의 공개 URL은 클라이언트에게 전달하지 않았고, 후속 WI에서 제한사항이 해소되기 전까지 공유 준비 완료로 판정할 수 없습니다.

## 정리

- 승인된 `stop.ps1`을 실행했고 반복 stop도 멱등하게 성공했습니다.
- `5173`, `8080`, `cloudflared`, acceptance-owned process는 모두 0입니다.
- 공개 URL은 unreachable 상태입니다.
- disposable DB는 drop 후 존재 수 0입니다.
- WI-021 helper `.class` 파일 3개를 제거했고 잔여 수 0입니다.
- 정확한 공개 URL, JDBC URL, 계정, 토큰, 비밀값, 외부 런타임 로그는 저장소 산출물에 복사하지 않았습니다.

## 산출물

- Agent evidence: `deliverables/agent/WI-20260714-ATS-022-evidence-pack.md`
