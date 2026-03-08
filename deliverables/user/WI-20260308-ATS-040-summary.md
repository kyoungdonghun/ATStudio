# WI-20260308-ATS-040 Summary

## 변경 내용

구독자(USER) 전용 6개 페이지를 구현했습니다.

### 구현된 페이지

| 페이지 | 라우트 | 주요 기능 |
|--------|--------|-----------|
| 재생목록 목록 | `/playlists/list` | 카드 그리드, 최대 3개 제한 (초과 시 버튼 비노출), 생성/삭제 모달 |
| 재생목록 상세 | `/playlists/:id` | 수록곡 테이블, 곡 삭제 기능 |
| 라이선스 목록 | `/licenses` | 보유 라이선스 테이블 + 페이지네이션 |
| 라이선스 상세 | `/licenses/:id` | 라이선스 코드, 곡 정보, 발급일 |
| 내 계정 | `/profile` | 계정 정보 조회, 닉네임 수정, 비밀번호 변경 |
| 구독 관리 | `/subscriptions/manage` | 현재 플랜 표시, 업/다운그레이드 미리보기, 취소 (유예 기간) |

### API 연동 파일 (3개 신규)

| 파일 | 연동 엔드포인트 |
|------|----------------|
| `api/playlists.ts` | POST/GET/PUT/DELETE /api/playlists, tracks 관련 |
| `api/licenses.ts` | GET /api/licenses/me, GET /api/licenses/{id} |
| `api/userSubscriptions.ts` | GET/PUT/DELETE /api/user-subscriptions/me, 변경 미리보기 |

## 핵심 비즈니스 로직

- **재생목록 3개 제한**: `count < 3`이면 "새 재생목록" 버튼 + 추가 카드 노출, `>= 3`이면 완전 비노출
- **구독 업그레이드**: 즉시 적용 + 비례 요금 표시
- **구독 다운그레이드**: 예약 처리 (현재 기간 유지, 만료 후 적용)
- **구독 취소**: CANCELLED + 만료일까지 서비스 유예

## 리스크

- 없음 (신규 파일 생성, 기존 코드 변경 없음)

## 검증

- **lint/typecheck/build**: 사용자 직접 실행 필요 (`npm run lint && npm run typecheck && npm run build`)
- **롤백**: 생성된 파일 삭제로 복구 가능 (git에서 stub 복원)
