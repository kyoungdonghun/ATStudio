# SR-C-03

## 대상

`src/main/resources/application.yml` line 47

## 수정 내용

CR-P-004 (CRITICAL — JWT secret Base64 fallback) 이 아직 해결되지 않은 상태로 코드베이스에 남아 있음.

**현재 상태:**
```yaml
jwt:
  secret: ${JWT_SECRET:YXRzdHVkaW8tc2VjcmV0LWtleS1mb3ItZGV2ZWxvcG1lbnQtb25seS0yMDI2}
```

Base64 디코딩 결과: `atstudio-secret-key-for-development-only-2026`

**요구되는 상태 (프로덕션 배포 전 필수):**
```yaml
jwt:
  secret: ${JWT_SECRET}
```

**영향:**
- 저장소가 공개되거나 `JWT_SECRET` 환경변수가 미설정된 배포 환경에서 임의의 JWT 토큰 위조 가능 (ADMIN 권한 포함).
- `security-policy.md` Section 6.1 위반: "Never hardcode in application.yml. Use `${JWT_SECRET}` placeholder."

**현재 조치:**
- 로컬 개발 편의상 유지 결정 (MEMORY.md CR-P-004 보류 항목).
- 프로덕션 배포 전 반드시 제거 필요.
- `backend-audit-report.md` Remediation Status 섹션에 ⏸ 상태로 기록됨.
