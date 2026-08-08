[WI HEADER]
WI ID: WI-20260808-ATS-005
REQ: REQ-20260808-ATS-002
Agent: tr
Depends On: -
Blocks: WI-20260808-ATS-006
[WI SUMMARY]
Why: 음원 썸네일 잘림의 실제 원인과 일반적인 전자상거래 이미지 처리 방식을 확인하고, 단일 운영자 환경에 맞는 저복잡도 권고를 만든다.
Scope (in/out): 업로드 화면, 저장·정규화 service, 표시 CSS, 테스트, 기존 SR과 공식 외부 자료의 읽기 전용 조사만 포함한다. SR/코드/이미지는 수정하지 않는다.
DoD: 리사이즈·크롭·왜곡을 구분하고, 현행 원인과 단계별 대안, 권장 비율/미리보기/검증 요구가 근거 포인터와 함께 정리된다.
Constraints/Forbidden: 리사이즈를 곧 왜곡으로 간주하지 않는다. `cover`와 `contain`의 장단점을 숨기지 않는다. 운영 규모에 비해 과도한 이미지 편집 기능을 기본안으로 권고하지 않는다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 현재 업로드 규격 안내·미리보기·비율 검증 여부와 표시 화면의 `object-fit` 사용을 확인한다.
- [ ] Track 썸네일과 다른 이미지 유형의 서버 정규화 적용 차이를 확인한다.
- [ ] 비율 보존 축소, cover 잘림, contain 여백, 강제 늘림 왜곡을 명확히 구분한다.
- [ ] 단일 운영자 환경에 맞는 최소안과 후속 확장안을 제안한다.
- [ ] MDN, Shopify Help, Cloudinary 공식 자료를 근거로 사용한다.
Performance:
- [ ] 해당 없음(읽기 전용 조사).
Quality:
- [ ] 기존 SR-68과 중복되는 범위와 이번 후속 SR의 새 범위를 분리한다.
- [ ] 제안하는 픽셀 수치와 비율은 권고값인지 필수값인지 구분한다.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 2 (Task Context):
- docs/SR/SR-68.md
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260808-ATS-002.md

Files:
- frontend/src/pages/creator/TrackUploadPage.tsx
- frontend/src/pages/public/TrackDetailPage.module.css
- frontend/src/**/*.module.css
- src/main/java/com/atstudio/atstudio/service/TrackService.java
- src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java
- src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java

External official references:
- https://developer.mozilla.org/en-US/docs/Web/CSS/Reference/Properties/object-fit
- https://help.shopify.com/en/manual/products/product-media/product-media-types
- https://cloudinary.com/documentation/resizing_and_cropping

Repro/Logs:
- `rg -n "object-fit|aspect-ratio|thumbnail|IMAGE_MAX_SIZE|canonicalizeThumbnail" frontend/src src/main/java src/test/java docs/SR/SR-68.md`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-005-summary.md :
- 현재 원인, 외부 관행, 권고안, 선택지
Agent-facing -> deliverables/agent/WI-20260808-ATS-005-evidence-pack.md :
- Evidence pointers, 외부 출처, 대안 비교, SR-98 필수 요구
Handoff Packet -> deliverables/agent/WI-20260808-ATS-005-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: 실행하지 않더라도 필요한 업로드·표시 회귀 테스트를 명시
Rollback (if needed): 읽기 전용 조사 산출물 제거 방법 기록
