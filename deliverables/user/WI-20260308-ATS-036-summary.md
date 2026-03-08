# WI-20260308-ATS-036 Summary

## What Changed
ATStudio React 프론트엔드의 공통 컴포넌트 20개 파일을 구현했습니다.

### Layout (3 컴포넌트)
- **Header**: 로고, 검색바, 탭 네비게이션(현재 경로 자동 활성), 로그인/내 계정 전환
- **PlayerBar**: 하단 고정 플레이어(곡 정보, 재생/일시정지/이전/다음, 프로그레스바, 대기열/구매 버튼). 곡 없으면 숨김.
- **MainLayout**: Header + 본문(Outlet) + PlayerBar 래퍼

### UI Atoms (5 컴포넌트)
- **Button**: 4 variant(primary/ghost/outline/danger), 3 size(sm/md/lg), disabled/loading 상태
- **Badge**: 인라인 텍스트 배지 (new/hot/accent)
- **Tag**: 장르/분위기 토글 칩
- **FilterChip**: 필터바 선택 칩
- **Modal**: 포털 기반 모달 (ESC 닫기, 배경 클릭 닫기, 포커스 트랩)

### Composite (2 컴포넌트)
- **TrackRow**: 트랙 테이블 행 (번호-재생 전환, 썸네일, 제목/아티스트, 장르칩, BPM, 길이, 액션)
- **AlbumCard**: 앨범 카드 (썸네일 + hover play overlay, 제목, 장르/곡수)

## Quality Gates
- TypeScript: 0 errors
- ESLint: 0 errors, 0 warnings
- Build: SUCCESS (85 modules, 741ms)

## Risk
- 없음. 신규 파일 생성만 수행. 기존 파일 미수정.

## Next
- WI-037~042 (페이지 컴포넌트)가 이 공통 컴포넌트들을 import하여 사용.
