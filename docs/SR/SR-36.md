# SR-C-02

## 대상

`frontend/src/router/index.tsx` line 87

## 수정 내용

라우터 주석 `/* ── Route definitions (48 screens) ── */` 이 실제 화면 수와 불일치한다.

현재 라우터에 정의된 named routes:
- MainLayout 하위: 9 public + 6 auth + 19 subscriber/auth-required + 2 error = 36
- AdminLayout 하위: 5 creator/admin + 9 admin = 14 (index redirect 제외)
- 합계: 50 named routes

`atstudio-front-list.md` v5 기준 49개 화면 + ERR-1/ERR-2 = 51개로 `screen-flow.md`에 반영됨.

**Before:**
```
/* ── Route definitions (48 screens) ── */
```

**After:**
```
/* ── Route definitions (49 screens + 2 error pages) ── */
```

> 주석은 기능에 영향 없는 코드 품질 이슈. 다음 라우터 수정 시 함께 정정 권장.
