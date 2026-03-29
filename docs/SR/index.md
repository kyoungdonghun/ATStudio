# Screen Review (SR) Index

> **Sub-folder:** `confirm/` — Code quality findings flagged during documentation audits (SR-C-xx format). These are not user-visible screen changes; they are backend code correctness issues pending developer review.

| SR | Title | Status |
|----|-------|--------|
| SR-01 | 메인 화면 신규 앨범 좌우 이동 기능 추가 | DONE |
| SR-02 | 인기 앨범 노출 개수 제한 | DONE |
| SR-03 | 신규 음원 섹션 추가 | DONE |
| SR-04 | 분위기별 태그 탐색 섹션 추가 | DONE |
| SR-05 | 모바일 화면 신규 앨범 UI 깨짐 수정 | DONE |
| SR-06 | 더미 음원 재생 연결 수정 | DONE |
| SR-07 | 음원 목록 리스트 재생/정지 이벤트 수정 | DONE |
| SR-08 | 공지 상세 첨부파일 다운로드 기능 확인 및 구현 | DONE |
| SR-09 | 질문 게시판 메뉴 및 기능 확인 | DONE |
| SR-10 | 큐바 대기열 자동 다음곡 재생 기능 | DONE |
| SR-11 | 큐바 대기열 드래그 앤 드롭 순서 변경 | DONE |
| SR-12 | 비회원 액션 클릭 시 구독 유도 기능 | DONE |
| SR-13 | 관리자 음원 다중 등록 설계 검토 | DONE |
| SR-14 | 관리자 사용자 구독 상태 관리 기능 | DONE |
| SR-15 | 관리자 라이선스 관리 사용자 검색 Validation 수정 | DONE |
| SR-16 | 관리자 공지사항 작성 첨부파일 업로드 기능 추가 | DONE |
| SR-17 | 큐바 플레이어 다크/라이트 테마 색상 수정 | DONE |
| SR-18 | 음원 목록 정렬 기준 추가 (좋아요순, 다운로드순) | DONE |
| SR-19 | 앨범 리스트 뷰 테이블 컬럼 정렬 수정 | DONE |
| SR-20 | 앨범 페이지 정렬 기능 추가 (최신순, 곡 수순) | DONE |
| SR-21 | 공지 조회수 및 정렬 기능 추가 | DONE |
| SR-22 | 메인 배너 라이트 모드 텍스트 가독성 개선 | DONE |
| SR-23 | 큐바 대기열 버튼 텍스트 세로 깨짐 수정 | DONE |
| SR-24 | 재생목록 비구독자 접근 차단 (구독 페이지 리다이렉트) | DONE |
| SR-25 | 다운로드 페이지 비구독자 접근 차단 | DONE |
| SR-26 | 마이페이지 구조 리디자인 (사이드바 + 콘텐츠) | DONE |
| SR-27 | 큐바 구매하기 버튼 동작 구현 | DONE |
| SR-28 | 관리자 구독 메뉴 비노출 처리 | DONE |
| SR-29 | 관리자 문의 목록 조회 수정 | DONE |
| SR-30 | 일반 사용자 문의 목록 전체/내 문의 탭 추가 | DONE |
| SR-31 | 관리자 문의 답변 기능 추가 | DONE |
| SR-32 | 앨범 음원 추가/순서 관리 기능 | DONE |
| SR-33 | 로그아웃/계정 전환 시 플레이어+전역 상태 초기화 | DONE |
| SR-34 | 앨범 좋아요 기능 (DB+BE+FE+좋아요 목록 탭 분리) | DONE |

## Code Confirmation Reports (docs/SR/confirm/)

| SR-C | 대상 | 상태 |
|------|------|------|
| SR-C-01 | DATA_INTEGRITY_VIOLATION 오용 (4개 서비스) | OPEN |
| SR-C-02 | router/index.tsx 화면 수 주석 불일치 | OPEN |
| SR-C-03 | application.yml JWT secret fallback 미제거 (CR-P-004) | DEFERRED |
| SR-C-04 | docs/guides/ 미존재 — 30+ broken internal links (meta-only docs) | OPEN |
