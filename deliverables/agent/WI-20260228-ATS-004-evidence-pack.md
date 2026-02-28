# WI-20260228-ATS-004 Evidence Pack — 전체 회귀 테스트

## 실행 명령

```bash
cd C:\Users\jm991\Desktop\project\ATStudio
gradlew.bat test
```

강제 재실행 (캐시 우회):
```bash
gradlew.bat cleanTest test
```

## 결과 요약

- **BUILD SUCCESSFUL**
- 총 테스트: 478건
- 실패: 0건
- 소요: 28.832s
- 타임스탬프: 2026-02-28 17:16:48 KST

## 증거 소스

- HTML 리포트: `build/reports/tests/test/index.html`
  - `<div class="counter">478</div>` (tests), `<div class="counter">0</div>` (failures)
- XML 결과: `build/test-results/test/` (65개 파일)
  - Grep 패턴 `failures="[^0]` → **No matches** (전 파일 failures=0)

## Phase 1 수정 대상 XML 검증

| XML 파일 | tests | failures | errors |
|---------|-------|----------|--------|
| SecurityFilterChainTest.xml | 10 | 0 | 0 |
| AuthServiceTest.xml | 7 | 0 | 0 |
| UserServiceTest.xml | 12 | 0 | 0 |
| QuestionServiceTest$DeleteQuestion.xml | 7 | 0 | 0 |
| TrackServiceTest.xml | 11 | 0 | 0 |
| UserSubscriptionControllerTest.xml | 20 | 0 | 0 |
| UserSubscriptionServiceTest$*.xml (8파일) | - | 0 | 0 |

## 판정

**PASS — Phase 1 (WI-001~003) 전체 수정 회귀 없음 독립 확인 완료.**
