# WI-20260714-ATS-013 화이트리스트 CSV 수식 주입 방어 요약

## 결과

관리자 화이트리스트 CSV의 사용자 제어 문자열 열에 출력 전용 수식 중화를 적용했습니다.

- 대상 열은 사용자 이메일, 닉네임, 채널명, YouTube 핸들, 채널 URL, YouTube 채널 ID입니다.
- 첫 유효 문자가 `=`, `+`, `-`, `@`인 값은 원문 앞에 작은따옴표 하나를 붙인 뒤 기존 CSV 따옴표 이스케이프를 적용합니다.
- 선행 ASCII 공백·탭과 셀 내부 BOM 뒤의 수식 문자, 셀 시작 탭·CR·LF도 같은 방식으로 중화합니다.
- null, 빈 문자열, 기존 작은따옴표 시작 값, 한글, 내장 따옴표·줄바꿈은 기존 CSV 표현을 유지합니다.
- 엔티티와 `WhitelistExportItem` 스냅샷에는 원문을 그대로 저장하며, 중화는 파일 직렬화 단계에서만 수행합니다.
- UTF-8 BOM, 열 순서, 행 상태 전이, 서버 생성 값 직렬화는 변경하지 않았고 의존성도 추가하지 않았습니다.

## 검증

- `gradlew.bat test --tests "com.atstudio.atstudio.service.AdminWhitelistChannelServiceTest"`: PASS, 5 tests, 0 failures, 0 errors, 0 skipped
- `gradlew.bat compileJava`: PASS
- `git diff --check -- <owned files>`: PASS

## 제한과 위험

- 작은따옴표는 스프레드시트 수식 실행을 막기 위한 CSV 원시 값의 일부이므로, 스프레드시트가 아닌 소비자가 원시 CSV를 직접 읽으면 이를 볼 수 있습니다.
- 실제 Excel/Google Sheets UI smoke는 수행하지 않았고, 생성된 UTF-8 CSV 바이트와 저장 스냅샷을 단위 테스트로 검증했습니다.
- 중화 대상은 현재 계약상 사용자 제어 문자열 6개 열입니다. 숫자 ID, 서버 생성 시각, 플랜명, 청구주기는 기존 직렬화를 유지합니다.

## 후속 WI

본 WI 완료는 `WI-20260714-ATS-019`, `WI-20260714-ATS-024`, `WI-20260714-ATS-025`의 체인 트리거입니다. MA는 REQ 병렬 작업 계획에 따라 다음 WI 핸드오프와 위임을 즉시 진행해야 합니다.
