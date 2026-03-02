# WI-20260302-ATS-005 Evidence Pack — 보안 수정 코드 리뷰

## CR-C-006: Notice 소유권

**판정: ✅ PASS**

`NoticeService.java:90-97` — validateNoticeOwnership():
- L91-93: ADMIN 역할 → 즉시 return (소유권 체크 우회)
- L94-96: 비작성자 → RESOURCE_NOT_ACCESS (403) throw
- L75: updateNotice() 호출, L84: deleteNotice() 호출

테스트: ADMIN 타인 수정 허용(L121), 비ADMIN 거부(L134), ADMIN 삭제 허용(L174), 비ADMIN 거부(L186)

**MINOR**: NoticeController L55/L68에 `@PreAuthorize("hasRole('ADMIN')")` 이미 존재 → 서비스 ADMIN 분기는 defense-in-depth (기능 오류 아님)

## CR-A-004: Playlist 소유권

**판정: ✅ PASS**

`PlaylistService.java:198-208` — getOwnedPlaylist():
- L201: `playlist.getUser().getId().equals(userId)` → 불일치 시 RESOURCE_NOT_ACCESS
- L204-206: isActive 확인
- 호출: getPlaylistDetail(L79), addTrack(L97), updatePlaylist(L128), reorderTracks(L147), removeTrack(L170), deletePlaylist(L184) — 전체 적용

## CR-C-008: TestController 삭제

**판정: ✅ PASS**

Glob 검색 결과: TestController.java 미존재 확인

## CR-B-005: URL 검증 강화

**판정: ✅ PASS**

`WhitelistChannelService.java:100-108`:
- `URI.create(channelUrl)` — java.net.URI 파싱
- `host.equals("youtube.com") || host.endsWith(".youtube.com")` — 정확한 도메인 매칭
- catch IllegalArgumentException → INVALID_ARGUMENT (400)
- "notarealsite-youtube.com" → endsWith(".youtube.com") false, equals false → 거부 ✅

테스트: vimeo.com 거부(L79), spoofed domain 거부(L89), malformed URL 거부(L100), www.youtube.com 허용(L112)

**SUGGESTION**: scheme(http/https) 미검증 — `ftp://youtube.com` 통과 가능

## CR-P-005: 만료 RefreshToken 거부

**판정: ✅ PASS**

`AuthService.java:74-76`: `result == TokenValidationResult.EXPIRED` → REFRESH_TOKEN_EXPIRED
`BUSINESS_ERROR.java:134-137`: REFRESH_TOKEN_EXPIRED → HttpStatus.UNAUTHORIZED (401)
VALID 경로(L81-104) 기존과 동일 유지

테스트: EXPIRED → REFRESH_TOKEN_EXPIRED(L113), INVALID(L127), VALID rotation(L89), DB불일치(L141), 탈퇴계정(L163)

**SUGGESTION**: 탈퇴 계정 체크(L94-96)가 BCrypt 연산 이후 위치 — 순서 최적화 가능
