# WI-20260603-ATS-001 Summary

## 결과
- 화이트리스트 채널을 사용자 1:N 채널 목록 도메인으로 확장했다.
- 채널 상태 모델(`DRAFT`, `PENDING`, `EXPORTED`, `REGISTERED`, `REVISION_REQUESTED`, `REJECTED`, `CANCELLED`, `REMOVAL_REQUESTED`)을 추가했다.
- 저장은 구독 없이 가능하고, 등록 요청 시점에만 활성 구독과 플랜 한도를 검사하도록 분리했다.
- 대표 채널 설정 API와 관리자 조회/상태 변경/CSV export API를 추가했다.
- CSV에는 `userEmail`을 포함하고, export batch/item 이력을 저장한다.
- `PENDING` export만 `EXPORTED`로 전환하고, 다른 상태 export는 기존 상태를 유지한다.

## 주요 정책
- 플랜 한도 포함 상태: `PENDING`, `EXPORTED`, `REGISTERED`, `REVISION_REQUESTED`, `REMOVAL_REQUESTED`.
- `EXPORTED`, `REGISTERED` 채널은 사용자 즉시 삭제 대신 `REMOVAL_REQUESTED`로 전환한다.
- 관리자 상태 변경 API는 `REGISTERED`, `REVISION_REQUESTED`, `REJECTED`, `REMOVAL_REQUESTED`, `CANCELLED`만 허용한다.
- Export item은 원본 채널 삭제 후에도 스냅샷 이력을 보존할 수 있도록 원본 채널 FK를 nullable로 둔다.
- `REVISION_REQUESTED` 재요청은 기존 자기 슬롯을 다시 차감하지 않고 `PENDING`으로 복귀한다.
- 외부 처리된 채널 수정이 재처리 요청으로 이어질 때도 동일한 구독/한도 검사를 적용한다.
- 대표 채널을 즉시 삭제하면 남은 채널 중 하나를 대표 채널로 승계한다.

## 검증
- `gradlew.bat test --tests "...WhitelistChannelServiceTest" --tests "...WhitelistChannelControllerTest" --tests "...AdminWhitelistChannelServiceTest"` 통과.
- `gradlew.bat test` 전체 통과.

## 주의
- 기본 `ddl-auto=validate` 구조라 기존 DB에는 새 whitelist 컬럼/테이블을 직접 반영해야 한다. `schema.sql`은 최신 구조로 갱신했다.
