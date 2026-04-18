---
version: 1.0
last_updated: 2026-04-17
project: ATS
owner: qa
category: guide
status: stable
dependencies:
  - path: index.md
    reason: Category entry point for client-facing testing documents
  - path: 0-site-policy.md
    reason: Access and subscription baseline for testers
  - path: 4-sr-format.md
    reason: Feedback intake format referenced from this guide
---

# ATStudio 테스트 가이드

> 본 문서는 ATStudio 외부 테스터를 위한 종합 가이드입니다.

## 문서 구성

| # | 문서 | 설명 |
|---|------|------|
| 0 | [사이트 정책](0-site-policy.md) | 역할 3계층, 유형 2종, 개인/기업별 플랜 구조, 접근 권한 매트릭스 |
| 1 | [기능 시나리오](1-scenarios.md) | 104개 테스트 시나리오 (비회원/인증/회원/구독자/기업/관리자/플레이어/가드/에러) |
| 2 | [예시 테스트](2-test-cases.md) | 8개 예시 테스트 케이스 (정상/에러/경계값) |
| 3 | [테스트 방법론](3-test-methodology.md) | 블랙박스, 경계값, 동등분할, 상태전이, 시나리오, 탐색적 |
| 4 | [SR 양식](4-sr-format.md) | SR(수정 요청) 문서 템플릿 + 작성 예시 |
| 5 | [AI 프롬프트](5-ai-prompt.md) | AI를 활용한 SR 작성 프롬프트 + 사용 예시 |

## 테스트 계정

| 역할 | 계정 제공 방식 | 비고 |
|------|---------------|------|
| 관리자 | 환경 운영자 별도 전달 | 관리자 기능 테스트가 필요할 때 요청 |
| 일반회원 | 직접 회원가입 또는 운영자 전달 | 일반 기능/구독 흐름 확인용 |

> 저장소 기준으로 고정 공용 계정과 비밀번호는 보장하지 않습니다.
> 관리자 계정이 필요하면 테스트 환경 운영자에게 별도 전달받아야 합니다.
> 회원가입 후 이메일 인증/비밀번호 재설정 메일 수신 여부는 배포 환경의 메일 설정에 따라 달라질 수 있습니다.

## SR 접수 원칙

- SR 번호는 비워두고 보내주세요. 최종 `SR-XX` 번호는 운영 측에서 부여합니다.
- 현상, 환경, 페이지/경로, 재현 방법만 정확하면 바로 작업으로 연결할 수 있습니다.
- 문서 작성이 번거로우면 [SR 양식](4-sr-format.md)의 초간단 버전이나 [AI 프롬프트](5-ai-prompt.md)를 사용해도 됩니다.
