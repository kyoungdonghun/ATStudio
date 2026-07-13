---
version: 2.0
last_updated: 2026-07-11
project: ATS
owner: qa
category: registry
status: stable
dependencies:
  - path: testing-guide.md
    reason: Primary client-facing entry point
  - path: 0-site-policy.md
    reason: Role and subscription baseline
---

# 클라이언트 테스트 문서 안내

> 목적: ATStudio를 클라이언트가 직접 확인할 때 필요한 문서를 한곳에서 찾도록 합니다.
> 작성 원칙: 개발자가 아니어도 바로 따라 할 수 있게 쉽고 짧게 씁니다.

## 먼저 볼 문서

| 순서 | 문서 | 언제 보나요? |
|------|------|--------------|
| 1 | [테스트 가이드](testing-guide.md) | 테스트 전에 준비할 것과 전체 순서를 볼 때 |
| 2 | [빠른 체크리스트](1-quick-checklist.md) | 30분~1시간 정도로 먼저 훑어볼 때 |
| 3 | [전체 기능 체크리스트](2-full-feature-checklist.md) | 주요 기능을 빠짐없이 확인할 때 |
| 4 | [관리자 체크리스트](3-admin-checklist.md) | 관리자 계정으로 운영 화면을 확인할 때 |
| 5 | [SR 작성 양식](4-sr-format.md) | 문제나 개선 의견을 남길 때 |
| 6 | [AI 정리 프롬프트](5-ai-prompt.md) | 말로 적은 메모를 SR 형식으로 정리할 때 |

## 참고 문서

| 문서 | 용도 |
|------|------|
| [사이트 정책 구조](0-site-policy.md) | 역할, 구독, 접근 권한을 요약해서 볼 때 |
| [내부 기능 근거 맵](_internal-feature-map.md) | 운영/개발자가 체크리스트와 실제 구현이 맞는지 확인할 때 |

## 이번 테스트에서 보는 범위

- 공개 화면: 홈, 음원, 앨범, 공지
- 회원 기능: 회원가입, 로그인, 프로필, 비밀번호, 문의
- 구독자 기능: 결제, 플랜 변경, 취소, 다운로드, 재생목록, 라이선스, 화이트리스트 채널
- 기업 기능: 기업 인증 신청, 보완 제출, 승인 후 기업 구독
- 관리자 기능: 음원/앨범/태그/회원/구독/결제/화이트리스트/기업 인증/문의/공지/설정

## 관리 원칙

기능이 바뀌면 [전체 기능 체크리스트](2-full-feature-checklist.md)와 [내부 기능 근거 맵](_internal-feature-map.md)을 같이 수정합니다. 클라이언트에게 보여줄 문서는 쉽게 쓰고, 기술 근거는 내부 문서에 둡니다.
