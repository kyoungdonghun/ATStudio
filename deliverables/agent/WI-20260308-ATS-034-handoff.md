[WI HEADER]
WI ID: WI-20260308-ATS-034
REQ: REQ-20260308-ATS-011
Agent: se
Depends On: -
Blocks: -

---

[WI SUMMARY]
Why: TrackController 파일 업로드 패턴을 다수파(Playlist/Album) 패턴으로 통일
     DTO 내 MultipartFile → Controller 레벨 @RequestPart 분리
Scope (in):
  - TrackCreateRequest — audioFile, thumbnail 필드 제거
  - TrackUpdateRequest — audioFile, thumbnail 필드 제거
  - TrackController — createTrack/updateTrack에 @RequestPart 파라미터 추가, TrackService 호출 수정
  - TrackService — createTrack/updateTrack 시그니처 수정 (MultipartFile 별도 파라미터)
  - 관련 테스트 수정
Scope (out):
  - 파일 저장 로직 변경 금지 (storageService.store() 호출 유지)
  - 다른 Controller/Service 수정 금지
  - api-spec 수정 금지 (클라이언트 동작 동일)

DoD:
  - TrackCreateRequest, TrackUpdateRequest에 MultipartFile 필드 없음
  - TrackController.createTrack() — @RequestPart MultipartFile audioFile, @RequestPart(required=false) MultipartFile thumbnail 별도 수신
  - TrackController.updateTrack() — @RequestPart(required=false) MultipartFile audioFile, @RequestPart(required=false) MultipartFile thumbnail 별도 수신
  - TrackService 시그니처 수정 완료
  - ./gradlew test 전체 통과

Constraints/Forbidden:
  - storageService.store() 호출 로직 변경 금지
  - 기존 기능 동작 변경 금지

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] TrackCreateRequest — MultipartFile audioFile 필드 없음
- [ ] TrackCreateRequest — MultipartFile thumbnail 필드 없음
- [ ] TrackUpdateRequest — MultipartFile audioFile 필드 없음
- [ ] TrackUpdateRequest — MultipartFile thumbnail 필드 없음
- [ ] TrackController.createTrack() — @RequestPart MultipartFile audioFile 파라미터 존재
- [ ] TrackController.createTrack() — @RequestPart(required=false) MultipartFile thumbnail 파라미터 존재
- [ ] TrackController.updateTrack() — @RequestPart(required=false) MultipartFile audioFile 파라미터 존재
- [ ] TrackController.updateTrack() — @RequestPart(required=false) MultipartFile thumbnail 파라미터 존재
- [ ] TrackService.createTrack() — MultipartFile 별도 파라미터로 수신
- [ ] TrackService.updateTrack() — MultipartFile 별도 파라미터로 수신

Quality:
- [ ] ./gradlew test 전체 통과 (failures=0)
- [ ] 패턴이 PlaylistController/AlbumController와 일치

참조 (수정 금지):
- PlaylistController.java, AlbumController.java (다수파 패턴 참조용)

---

[INPUT POINTERS]

Tier 0 (Standards):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

REQ:
- deliverables/user/REQ-20260308-ATS-011.md

Files (수정 대상):
- src/main/java/com/atstudio/atstudio/dto/track/TrackCreateRequest.java
- src/main/java/com/atstudio/atstudio/dto/track/TrackUpdateRequest.java
- src/main/java/com/atstudio/atstudio/controller/TrackController.java
- src/main/java/com/atstudio/atstudio/service/TrackService.java
- src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java (있을 경우)
- src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java (있을 경우)

참조 (수정 금지):
- src/main/java/com/atstudio/atstudio/controller/PlaylistController.java
- src/main/java/com/atstudio/atstudio/controller/AlbumController.java

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260308-ATS-034-summary.md
Agent-facing -> deliverables/agent/WI-20260308-ATS-034-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260308-ATS-034-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 수정 파일:라인 포인터 포함
Tests: ./gradlew test 실행 결과 (테스트 수, failures=0 확인)
