---
version: 1.0
last_updated: 2026-07-14
project: ATS
category: work-summary
status: stable
related_wi: WI-20260714-ATS-016
---

# WI-20260714-ATS-016 완료 요약

## 한 줄 요약

Cloudflare와 Vite를 거치는 인수환경에서도 사용자별 로그인 요청 제한이 유지되도록 Host, 프록시 헤더, 실제 사용자 IP 신뢰 경계를 보강했습니다.

## 적용 내용

- Vite의 전체 Host 허용을 제거하고 `localhost`, `127.0.0.1`, `APP_PUBLIC_BASE_URL`에서 얻은 공개 Host만 허용했습니다.
- `/api`, `/uploads` 프록시는 외부의 `Forwarded`, `X-Forwarded-*`, `X-ATStudio-Client-IP` 값을 제거합니다.
- 인수환경에서는 Cloudflare가 전달한 단일 IP만 검증해 내부 헤더로 다시 작성하고, 로컬에서는 socket peer만 사용합니다.
- Spring은 acceptance 모드 또는 명시적 설정에서만, 설정된 loopback Vite 프록시가 전달한 단일 IPv4/IPv6 literal을 신뢰합니다.
- 로그인 등 기존 rate-limit 키가 단순 `remoteAddr` 대신 검증된 실제 사용자 식별자를 사용하도록 연결했습니다.
- acceptance 백엔드는 `localhost`, `127.0.0.1`, `::1` Host만 허용합니다.
- 전역 `https://*.trycloudflare.com` CORS 허용을 제거하고, WI-015의 정확한 공개 URL만 추가합니다.
- Spring의 일반 forwarding-header 자동 신뢰를 명시적으로 끄고, WI-016 resolver만 신뢰 경계를 소유하도록 했습니다.

## 검증 결과

- 백엔드 WI-016 집중 테스트: 4개 클래스, 12개 테스트 통과
- MA 독립 확인: `TrustedClientIdentityResolverTest`, `AuthRateLimitFilterTest` 통과
- Vite 설정 테스트: 9개 통과
- 프론트 typecheck, lint, build 통과
- 백엔드 `compileJava` 통과
- WI-016 대상 `git diff --check` 통과

## 남은 확인

- 실제 Cloudflare가 `CF-Connecting-IP`를 기대대로 덮어쓰는지와 서로 다른 외부 사용자가 서로 다른 rate-limit 키로 분리되는지는 서버나 터널을 실행하지 않은 이번 WI에서 증명하지 않았습니다.
- 위 항목은 WI-022 공개 Cloudflare smoke에서 확인해야 합니다.
- 인수환경 rate-limit은 현재 단일 서버의 메모리 기반 제한입니다. 이번 승인 범위에 따라 멀티서버 공유 제한은 추가하지 않았습니다.

## 되돌리기

WI-016이 추가한 Vite Host/프록시 설정, trusted client identity 및 acceptance Host 필터, CORS 보정과 집중 테스트만 되돌립니다. WI-015의 acceptance 비밀값·공개 URL 계약과 다른 작업자의 payment/storage/auth 변경은 되돌리지 않습니다.
