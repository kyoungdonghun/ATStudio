# User — Business License 유스케이스

> **API 참조**: `docs/design/api-spec.md` 섹션 13 (기업 라이센스 심사)
> **DB 참조**: `docs/design/db-schema.md` 섹션 3 (`business_license_requests`)
>
> **라이센스 종류 구분**:
> - **기업 심사 라이센스** (이 파일): `business_license_requests` 테이블. 기업회원이 구독 전 서류 제출 → 관리자 심사.
> - **음원 사용 라이센스**: `licenses` 테이블. → `user-license.md` 참조.
>
> **심사 프로세스**: 기업 회원 → 서류 제출(PENDING) → 관리자 검토 → 보완 요청(REVISION_REQUESTED) 또는 승인(APPROVED) / 반려(REJECTED) → 승인 완료 시 license_code 발급 → 구독제 결제 가능

---

## BL-001: 기업 라이센스 신청 [신규]

| 항목 | 내용 |
|------|------|
| **코드** | BL-001 |
| **버전** | 26-02-20 |
| **설명** | 기업 회원이 구독제 가입을 위한 기업 라이센스 심사를 신청한다. |
| **액터** | 사용자(기업회원), 백엔드 시스템 |
| **사전 조건** | 로그인 상태. userType=BUSINESS인 회원. 기존 PENDING 신청 없음. APPROVED 신청 없음. (REJECTED 또는 REVISION_REQUESTED 후 재신청 가능.) |
| **트리거** | 사용자가 구독 플랜 선택 후 '기업 라이센스 심사 신청' 버튼을 클릭한다. |
| **관련 UC** | BL-002(내 현황 조회), PAYMENT-001(구독 신청) |

**기본 흐름**
1. 사용자가 심사용 서류 파일(documents, 필수, 복수 가능)을 업로드한다.
2. 프론트엔드가 multipart/form-data로 백엔드에 전송한다.
3. 백엔드가 userType=BUSINESS 여부를 확인한다.
4. 백엔드가 서류 파일을 `/uploads/business-docs/{userId}/` 경로에 저장한다.
5. 백엔드가 business_license_requests 레코드를 생성(status=PENDING)한다.
6. 백엔드가 성공 응답(201 Created, id/status/documentPath/createdAt 포함)을 반환한다.

**예외/대안 흐름**
- 기업회원(BUSINESS)이 아닌 경우: 403 응답.
- 이미 PENDING 또는 APPROVED 신청 존재: 409 Conflict.

**사후 조건**
- business_license_requests 레코드 생성됨(status=PENDING). 서류가 파일시스템에 저장됨.

---

## BL-002: 내 라이센스 신청 현황 조회 [신규]

| 항목 | 내용 |
|------|------|
| **코드** | BL-002 |
| **버전** | 26-02-20 |
| **설명** | 기업 회원이 본인의 기업 라이센스 심사 현황을 조회한다. |
| **액터** | 사용자(기업회원), 백엔드 시스템 |
| **사전 조건** | 로그인 상태. userType=BUSINESS인 회원. |
| **트리거** | 사용자가 '기업 라이센스 현황' 화면에 접근한다. |
| **관련 UC** | BL-001(신청) |

**기본 흐름**
1. 프론트엔드가 인증 토큰을 포함한 요청을 백엔드에 전송한다.
2. 백엔드가 JWT에서 userId를 추출하여 해당 사용자의 business_license_requests 레코드를 조회한다.
3. 백엔드가 현황(id, status, adminNote, licenseCode, createdAt)을 반환한다.

**사후 조건**
- 심사 현황이 화면에 출력됨. 신청 없는 경우 null 반환.

> **status 의미**: PENDING(심사 대기) / APPROVED(승인) / REVISION_REQUESTED(보완 요청) / REJECTED(반려)
> - REVISION_REQUESTED 시 adminNote에 보완 사유가 포함됨.
> - APPROVED 시 licenseCode가 포함됨 (구독 신청 가능 상태).

---

## BL-003: 라이센스 신청 목록 조회 (관리자) [신규]

| 항목 | 내용 |
|------|------|
| **코드** | BL-003 |
| **버전** | 26-02-20 |
| **설명** | 관리자가 기업 라이센스 심사 신청 목록을 조회한다. |
| **액터** | 관리자, 백엔드 시스템 |
| **사전 조건** | 관리자 로그인 상태. |
| **트리거** | 관리자가 '기업 라이센스 심사 목록' 화면에 접근한다. |
| **관련 UC** | BL-004(상세 조회), BL-005(심사 처리) |

**기본 흐름**
1. 프론트엔드가 status(선택), 페이지 파라미터를 포함한 요청을 백엔드에 전송한다.
2. 백엔드가 business_license_requests 목록을 페이지네이션하여 반환한다.

**사후 조건**
- 심사 신청 목록과 pageInfo가 화면에 출력됨.

---

## BL-004: 라이센스 신청 상세 조회 (관리자) [신규]

| 항목 | 내용 |
|------|------|
| **코드** | BL-004 |
| **버전** | 26-02-20 |
| **설명** | 관리자가 특정 기업 라이센스 신청의 상세 정보를 조회한다. |
| **액터** | 관리자, 백엔드 시스템 |
| **사전 조건** | 관리자 로그인 상태. 조회 대상 신청이 DB에 존재. |
| **트리거** | 관리자가 심사 목록에서 특정 신청을 클릭한다. |
| **관련 UC** | BL-003(목록 조회), BL-005(심사 처리) |

**기본 흐름**
1. 프론트엔드가 requestId를 포함한 요청을 백엔드에 전송한다.
2. 백엔드가 해당 신청의 상세 정보(신청자 정보, status, documentPath, adminNote, licenseCode 등)를 반환한다.

**사후 조건**
- 신청 상세 정보 및 제출 서류 경로가 화면에 출력됨.

---

## BL-005: 라이센스 심사 처리 (관리자) [신규]

| 항목 | 내용 |
|------|------|
| **코드** | BL-005 |
| **버전** | 26-02-20 |
| **설명** | 관리자가 기업 라이센스 신청에 대해 승인/보완 요청/반려 처리를 한다. 승인 시 license_code가 자동 생성됨. |
| **액터** | 관리자, 백엔드 시스템 |
| **사전 조건** | 관리자 로그인 상태. 처리 대상 신청이 DB에 존재. |
| **트리거** | 관리자가 신청 상세 화면에서 심사 결과 버튼을 클릭한다. |
| **관련 UC** | BL-004(상세 조회) |

**기본 흐름**
1. 관리자가 처리 결과(status: APPROVED/REVISION_REQUESTED/REJECTED)와 adminNote를 입력한다.
2. 프론트엔드가 requestId, status, adminNote를 백엔드에 전송한다.
3. 백엔드가 권한 확인 후 business_license_requests 레코드를 업데이트한다.
   - APPROVED인 경우: license_code(UUID 기반) 자동 생성, approved_at 기록.
   - REVISION_REQUESTED/REJECTED인 경우: adminNote에 사유 저장.
4. 백엔드가 처리 결과(id, status, licenseCode, approvedAt 포함)를 반환하고 200 OK를 반환한다.

**사후 조건**
- business_license_requests 레코드 status 갱신됨.
- APPROVED인 경우: license_code 발급됨, approved_at 기록됨. 해당 기업회원은 이후 구독 신청(PAYMENT-001) 가능.

> **연계**: 승인된 기업 회원이 PAYMENT-001(구독 신청) 진행 시, 백엔드가 business_license_requests.status=APPROVED 여부를 확인함.
