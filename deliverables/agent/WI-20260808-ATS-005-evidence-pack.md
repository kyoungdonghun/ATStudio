# Evidence Pack: WI-20260808-ATS-005
## Summary (one-liner)

- 트랙 썸네일 잘림을 원본 저장과 정사각형 `cover` 표시의 비율 불일치로 특정하고, 공식 전자상거래·이미지 처리 자료에 근거한 1:1 업로드 계약 중심의 저복잡도 권고를 정리했다.

## Scope / DoD Check

- DoD items:
  - [x] 현재 업로드 규격 안내·미리보기·비율 검증 부재를 확인했다.
  - [x] 트랙 표시 화면의 `object-fit: cover` 사용과 정사각형 컨테이너를 확인했다.
  - [x] 트랙·앨범의 원본 저장과 플레이리스트의 서버 정규화 적용 차이를 확인했다.
  - [x] 비율 보존 축소, cover 크롭, contain 여백, fill 왜곡을 구분했다.
  - [x] 단일 운영자 환경의 최소안과 후속 확장안을 제안했다.
  - [x] MDN, Shopify Help, Cloudinary 공식 자료를 대조했다.
  - [x] 기존 SR-68과 후속 SR-98의 범위를 분리했다.
  - [x] 1:1은 필수 계약 제안, 2048×2048px은 권고값으로 구분했다.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | 모든 에이전트의 헌법 및 단순성·투명성 원칙 |
| 1 | `docs/policies/quality-gates.md` | 읽기 전용 조사 산출물의 추적성과 회귀 검증 기준 |
| 2 | `docs/SR/SR-68.md` | 기존 앨범 이미지 표시·반응형 요구와 중복 범위 확인 |
| Context | `deliverables/user/REQ-20260808-ATS-002.md` | 승인 범위, 성공 기준, SR-98 목표 |

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `tr`
- Task type: research, UI/UX, testing
- `agent_required_tiers`: `[0]`
- Handoff가 지정한 `quality-gates.md`, `SR-68.md`를 추가 로드했다.

## Evidence Pointers (required)

### Upload UI

- `frontend/src/pages/creator/TrackUploadPage.tsx:210-215`
  - 제출 검증은 제목·BPM·조성만 확인하며 썸네일의 비율이나 해상도를 확인하지 않는다.
- `frontend/src/pages/creator/TrackUploadPage.tsx:422-445`
  - 썸네일 입력은 `accept="image/*"`이고 10MB 크기만 검사한다. 규격 안내, 이미지 디코딩, 미리보기, 비율 검증이 없다.
- `frontend/src/pages/creator/TrackUploadPage.tsx:251-261`
  - 선택된 썸네일을 가공하지 않고 multipart `thumbnail`로 전송한다.

### Track storage path

- `src/main/java/com/atstudio/atstudio/service/TrackService.java:71-81`
  - 생성 시 썸네일 `MultipartFile`을 `StorageWriteRequest`에 그대로 넣어 저장한다.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:180-187`
  - 수정 시에도 썸네일을 정규화하지 않고 그대로 교체한다.
- `src/main/java/com/atstudio/atstudio/service/storage/LocalStorageService.java:65-74`
  - 로컬 저장 구현은 입력 스트림을 staged 파일에 그대로 복사한다.
- `src/main/java/com/atstudio/atstudio/service/storage/StorageMutationCoordinator.java:135-150`
  - 저장 키 생성과 mutation draft만 담당하며 이미지 비율·포맷 변환은 수행하지 않는다.

### Canonicalization difference

- `src/main/java/com/atstudio/atstudio/service/PlaylistService.java:58-63`
  - 플레이리스트 썸네일은 저장 전에 `canonicalImageService.canonicalizeThumbnail`을 호출한다.
- `src/main/java/com/atstudio/atstudio/service/PlaylistService.java:200-207`
  - 플레이리스트 수정 경로도 동일한 정규화를 적용한다.
- `src/main/java/com/atstudio/atstudio/service/AlbumService.java:55-60`
  - 앨범 생성 경로는 트랙과 마찬가지로 원본 파일을 직접 저장한다.
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:40-57`
  - 정규화 서비스는 크기 제한, JPEG/PNG 시그니처·MIME 검증, APNG 거부 후 canonical JPEG를 생성한다.
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:157-187`
  - 최대 입력 경계 검증 후 `scale = min(1, 2048 / max(width, height))`를 양 축에 동일 적용한다. 비율을 보존하고 작은 이미지는 확대하지 않으며, 크롭이나 정사각형 패딩은 하지 않는다.
- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java:56-64`
  - 3000×1000 입력이 2048×683으로 축소되는 테스트가 비율 보존을 입증한다.

### Display path

- `frontend/src/pages/public/TrackDetailPage.module.css:51-74`
  - 280×280px 정사각형 컨테이너와 `overflow: hidden`, 이미지 `object-fit: cover` 조합이다.
- `frontend/src/pages/public/TrackDetailPage.module.css:290-306`
  - 모바일에서도 `aspect-ratio: 1`을 유지한다.
- `frontend/src/components/track/TrackRow.module.css:131-150`
  - 목록의 40×40px 썸네일도 `cover`다.
- `frontend/src/layouts/PlayerBar.module.css:70-85`
  - 플레이어 썸네일도 `cover`다.
- `frontend/src/pages/admin/TrackManagePage.module.css:140-155`
  - 관리자 트랙 목록 썸네일도 `cover`다.
- 저장소 전체 CSS 조사 결과 `object-fit: cover`는 18개, `object-fit: contain`은 0개였다. 이는 현재 제품이 균일한 카드 채움 방식을 일관되게 선택했음을 보여 준다.

### Current runtime evidence

- Read-only endpoint: `GET http://localhost:8080/api/tracks/1`
- Description:
  - `음원 업로드 테스트`
  - `테스트 내용`
  - `1. 이미지의 비율 관련 ux 확인(세로로 김)`
  - `2. 단건 음원 잘 올라가는지 확인.`
- Thumbnail key: `tracks/thumbnail/baecf782d5f04e2997cbea7d6ef094f1.png`
- Read-only image measurement: 564×1404px, width/height 0.402, 1,229,440 bytes, HTTP 200.
- Inference from code and runtime: portrait input dimensions are preserved at storage, then the square `cover` presentation clips the long axis. This is not evidence of image stretching.

### Existing SR boundary

- `docs/SR/SR-68.md:1-21`
  - SR-68은 앨범 이미지 때문에 레이아웃이 깨지는 문제를 고정 영역, 반응형 크기, `cover` 표시로 방지하는 요구다.
  - SR-98의 새 범위는 트랙 썸네일 업로드 계약, 실제 카드 미리보기, 비율 검증, 트랙 정규화 누락이다. SR-68의 레이아웃 보호 요구를 반복하는 문서로 만들면 안 된다.

## External Official Evidence

| Source | Directly supported point | Application to SR-98 |
|--------|--------------------------|----------------------|
| [MDN `object-fit`](https://developer.mozilla.org/en-US/docs/Web/CSS/Reference/Properties/object-fit) | `contain`은 비율을 유지하고 전체를 맞춰 여백이 생길 수 있다. `cover`는 비율을 유지해 박스를 채우며 넘치는 부분을 자른다. `fill`은 비율이 다르면 늘어난다. | 현재 잘림은 `cover`의 정상 동작이며 왜곡과 구분해야 한다. `contain` 전환에는 여백 비용이 있다. |
| [Shopify Help: Product media types](https://help.shopify.com/en/manual/products/product-media/product-media-types) | 나란히 표시하는 대표 이미지를 같은 크기로 보이게 하려면 일관된 종횡비가 필요하다. 정사각형 제품 이미지는 2048×2048px이 일반적으로 가장 잘 표시된다고 안내한다. | 정사각형 카드가 많은 ATStudio에는 일관된 1:1 입력 계약이 타당하다. 2048×2048px은 권고 근거이지 필수 정책의 자동 확정값은 아니다. |
| [Cloudinary: Resizing and cropping](https://cloudinary.com/documentation/resizing_and_cropping) | `fill`은 왜곡 없이 채우지만 잘릴 수 있고, `fit`은 비율을 유지해 박스 안에 맞추며, `pad`는 여백을 더한다. 한 축 기준 축소는 원본 비율을 보존한다. | 저장 축소와 표시 크롭을 별도 결정으로 설계하고, 자동 중앙 크롭 도입 시 초점 손실 위험을 다뤄야 한다. |

## Technique Comparison

| Technique | Aspect ratio | Crop | Empty space | Distortion | Suitable use |
|-----------|--------------|------|-------------|------------|--------------|
| Existing canonical resize | Preserved | No | N/A at stored file | No | Decode safety, format normalization, byte/dimension reduction |
| CSS `cover` / image fill crop | Preserved | Yes | No | No | Uniform marketplace cards with known focal-safe source |
| CSS `contain` / fit | Preserved | No | Yes when ratios differ | No | Full artwork visibility |
| Padding / letterbox | Preserved | No | Filled with chosen background | No | Canonical square output while retaining all content |
| CSS `fill` or independent width/height scaling | Broken when ratios differ | No | No | Yes | Not recommended |

## Recommendation for SR-98

### Minimum option for the single-operator environment

1. **Choose the explicit source contract over an interactive crop editor.**
2. Required policy proposal:
   - aspect ratio: 1:1;
   - accepted formats: JPEG/PNG, aligned with the existing canonicalizer;
   - frontend guidance and preview are advisory UX, backend decoding and ratio validation are authoritative.
3. Recommended input, not a hard requirement without a separate policy decision:
   - 2048×2048px;
   - current 10MB maximum;
   - important text or subjects kept away from edges.
4. Preview:
   - render immediately after file selection;
   - use the same square container, `object-fit: cover`, and default center `object-position` as actual consumers;
   - explain expected clipping before submission.
5. Storage:
   - reuse `CanonicalImageService` for Track create/update;
   - preserve aspect ratio, downscale only, never upscale or stretch;
   - treat this as storage safety/performance, not as a square conversion.
6. Validation:
   - reject non-square decoded dimensions on the backend if the 1:1 contract is adopted;
   - map the validation error to a field-level message instead of a generic upload failure.

### Follow-up expansion

- If non-square inputs are an actual recurring operational need, add a simple 1:1 crop step or stored focal point with matching preview.
- If the complete artwork must always be visible, select `contain` or server-side padding through a separate visual policy decision; accept the resulting empty space.
- Do not add an AI/smart crop or full image editor until repeated use proves the cost justified.

## SR-98 Required Requirements

- Current behavior must state that Track thumbnails are stored without canonicalization and displayed by square `cover` consumers.
- Reproduction must cite track 1's 564×1404px runtime asset and description without claiming the UI stretched it.
- The document must distinguish:
  - aspect-preserving resize from crop;
  - `cover` crop from `contain` whitespace;
  - both from `fill` distortion.
- Proposed acceptance criteria must include:
  - explicit 1:1 contract text;
  - same-as-production square preview;
  - frontend preflight plus backend authoritative dimension validation;
  - Track create and update parity;
  - JPEG/PNG canonicalization reuse and no-upscale behavior;
  - regression across detail, list, player, and admin thumbnail consumers;
  - decision/migration behavior for existing non-square assets.
- 2048×2048px must be labeled `Recommended`, not silently made a required limit.
- Interactive crop tooling must be a follow-up option, not the default requirement.

## Commands & Outputs

- Source reconnaissance:
  - `rg -n "object-fit|aspect-ratio|thumbnail|IMAGE_MAX_SIZE|canonicalizeThumbnail" frontend/src src/main/java src/test/java docs/SR/SR-68.md`
  - Result: upload and storage paths identified; repository-wide CSS includes 18 `cover` declarations and 0 `contain` declarations.
- Targeted storage-path comparison:
  - `rg -n --fixed-strings "canonicalizeThumbnail" src/main/java src/test/java`
  - Result: production callers are `PlaylistService` and `CompanyCertificationService`; `TrackService` and `AlbumService` do not call it.
- Runtime metadata:
  - `curl.exe -s http://localhost:8080/api/tracks/1 | ConvertFrom-Json`
  - Result: test description and thumbnail key above.
- Runtime image measurement:
  - `System.Net.WebClient.DownloadData(...)` + `System.Drawing.Image.FromStream(...)`
  - Result: HTTP 200, 564×1404px, 1,229,440 bytes.
- External research:
  - MDN `object-fit` Values section reviewed.
  - Shopify Help Product images note reviewed.
  - Cloudinary Resize dimensions and Resize/crop modes sections reviewed.

## Tests

- Automated tests were not executed because this WI is a read-only investigation and changes no product code.
- Required implementation regression tests:
  - frontend: file selection preview, non-square warning/block, valid square submit, object URL cleanup;
  - backend: Track create/update JPEG/PNG canonicalization, square validation, oversized/unsupported image rejection, no-upscale, aspect-preserving downscale;
  - integration: uploaded thumbnail is visually equivalent in detail, row, player, and admin card consumers;
  - compatibility: defined handling for existing non-square stored assets.

## Risks / Rollback

- Risks:
  - Guidance text without backend validation permits the same issue to recur.
  - Strict 1:1 rejection may add operator friction if source assets are routinely portrait/landscape; preview and actionable messaging are required.
  - Applying canonicalization only to new uploads leaves existing assets unchanged.
  - Center crop can remove important text or subjects even though it does not distort pixels.
  - Changing all consumers to `contain` would alter the product's established card appearance and introduce empty space.
- Rollback:
  - Product code, SR, image, and DB were not modified. Remove only `deliverables/user/WI-20260808-ATS-005-summary.md` and `deliverables/agent/WI-20260808-ATS-005-evidence-pack.md` to roll back this WI's outputs.

## Follow-ups

- Next WI candidate:
  - `WI-20260808-ATS-006`: incorporate the required findings into `SR-98` and update document indexes.
