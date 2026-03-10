# 11. Claude Code 셋업 가이드 — IntelliJ에서 시작하기

> **이 문서의 목표:** Claude Code를 설치하고 IntelliJ IDEA에서 ATStudio 프로젝트와 연동하는 것까지 완료하기.

---

## 전체 흐름 요약

```
1. Node.js 설치 확인
2. Claude Code CLI 설치
3. Anthropic 계정 로그인 (API 키 또는 구독)
4. IntelliJ 플러그인 설치
5. 프로젝트 열기 + 동작 확인
```

---

## 1단계: 사전 조건

| 항목 | 요구사항 | 확인 방법 |
|------|---------|----------|
| Node.js | 18 이상 | `node -version` |
| npm | 9 이상 | `npm -version` |
| IntelliJ IDEA | 2024.1 이상 (Community 또는 Ultimate) | Help → About |
| Anthropic 계정 | claude.ai 가입 필요 | [claude.ai](https://claude.ai) |

> **요금 안내:** Claude Code는 Anthropic API 사용료 기반입니다.
> - **Max 구독** ($100/월): API 사용량 포함, 가장 편리
> - **API 키 방식**: 사용한 만큼 과금 (토큰당 가격)
> - 어떤 방식이든 시작 전에 Anthropic 계정이 필요합니다.

---

## 2단계: Claude Code CLI 설치

터미널(PowerShell, CMD, Git Bash 중 아무거나)을 열고:

```bash
npm install -g @anthropic-ai/claude-code
```

설치 확인:

```bash
claude --version
```

버전 번호가 출력되면 성공입니다.

> **Windows 주의:** "권한 오류"가 나면 PowerShell을 **관리자 권한**으로 실행 후 다시 시도하세요.
> Git Bash를 사용하는 경우 npm PATH가 필요할 수 있습니다:
> ```bash
> export PATH="$PATH:/c/Program Files/nodejs"
> ```

---

## 3단계: 최초 로그인 (API 인증)

```bash
claude
```

처음 실행하면 인증 방법을 선택하라는 프롬프트가 나옵니다:

### 방법 A: Anthropic 계정 로그인 (Max 구독자 추천)

1. `claude` 실행
2. "Log in with Anthropic account" 선택
3. 브라우저가 열리면 Claude 계정으로 로그인
4. 인증 완료 → 터미널로 돌아옴

### 방법 B: API 키 직접 입력

1. [console.anthropic.com](https://console.anthropic.com)에서 API 키 발급
2. `claude` 실행 후 "Enter API key" 선택
3. API 키 붙여넣기
4. 인증 완료

> **참고:** API 키는 `sk-ant-...`로 시작합니다. 절대 git에 커밋하지 마세요.

---

## 4단계: IntelliJ 플러그인 설치

### 4-1. 플러그인 설치

1. IntelliJ IDEA를 엽니다
2. **File → Settings** (Windows) 또는 **IntelliJ IDEA → Preferences** (Mac)
3. 왼쪽 메뉴에서 **Plugins** 클릭
4. **Marketplace** 탭에서 `Claude Code` 검색
5. **"Claude Code"** (by Anthropic) → **Install** 클릭
6. IntelliJ **재시작**

### 4-2. 플러그인 확인

재시작 후:
- 하단 Tool Window에 **"Claude Code"** 탭이 생겨야 합니다
- 또는 **View → Tool Windows → Claude Code**로 열 수 있습니다

> **플러그인을 못 찾겠다면:**
> IntelliJ 버전이 2024.1 미만일 수 있습니다. Help → About에서 버전을 확인하세요.

---

## 5단계: ATStudio 프로젝트에서 시작하기

### 5-1. 프로젝트 열기

1. IntelliJ에서 **File → Open** → ATStudio 폴더 선택
2. Gradle 프로젝트로 인식되면 **Import** 허용

### 5-2. Claude Code 패널 열기

1. 하단의 **Claude Code** 탭 클릭 (또는 단축키 지정 가능)
2. 채팅 창이 열립니다 — 여기서 Claude와 대화합니다

### 5-3. 첫 대화 테스트

Claude Code 패널에 아래를 입력해보세요:

```
이 프로젝트가 뭔지 설명해줘
```

Claude가 `CLAUDE.md`를 자동으로 읽고 ATStudio 프로젝트에 대해 설명하면 **셋업 성공**입니다!

### 5-4. 권한 설정 (중요)

Claude Code가 파일을 읽고/쓰고/실행할 때 **권한 승인 팝업**이 뜹니다.

| 권한 | 설명 | 추천 |
|------|------|------|
| Read | 파일 읽기 | 항상 허용 |
| Write/Edit | 파일 수정 | 항상 허용 (또는 매번 확인) |
| Bash | 터미널 명령어 실행 | 매번 확인 (처음엔 이게 안전) |

> **팁:** 익숙해지면 **Settings → Claude Code → Permissions**에서 자주 쓰는 도구를 자동 허용으로 설정할 수 있습니다.

---

## 6단계: ATStudio 에이전트 시스템 확인

ATStudio는 단순 Claude Code가 아니라 **커스텀 에이전트 시스템**이 설정되어 있습니다.
이 시스템은 `.claude/` 폴더에 정의되어 있으며, `git clone` 시 자동으로 포함됩니다.

```
.claude/
├── agents/     ← 11개 전문 에이전트 정의 (se, sa, cr, pg, docops 등)
├── skills/     ← 18개 스킬 정의 (/create-req, /build-check 등)
├── config/     ← workspace.json (프로젝트 설정)
└── scripts/    ← 자동화 스크립트
```

**별도 설정이 필요 없습니다.** 프로젝트를 열면 Claude가 자동으로 인식합니다.

확인 방법:
```
"에이전트 목록 보여줘"
```
→ 11개 에이전트(ps, eo, sa, se, re, pg, tr, uv, docops, qa, cr)가 표시되면 정상.

---

## 7단계: 실제 사용해보기

### 예시 1: 프로젝트 파악

```
docs/forYou/ 폴더에 있는 온보딩 문서들 요약해줘
```

### 예시 2: 코드 질문

```
TrackController가 어떤 API를 제공하는지 알려줘
```

### 예시 3: 빌드 실행

```
백엔드 빌드 돌려줘
```
→ Claude가 `gradlew.bat build -x test`를 실행하고 결과를 보고합니다.

### 예시 4: 기능 요청 (REQ 워크플로우)

```
음원 검색에 BPM 범위 필터를 추가하고 싶어
```
→ Claude가 REQ 초안 → 승인 → WI 생성 → 구현 워크플로우를 시작합니다.
→ 자세한 워크플로우는 [10-Claude-에이전트-가이드.md](10-Claude-에이전트-가이드.md)를 참고하세요.

---

## 터미널에서 사용하기 (IntelliJ 외)

IntelliJ 플러그인 없이도 터미널에서 직접 사용할 수 있습니다:

```bash
# ATStudio 프로젝트 폴더로 이동
cd ATStudio

# Claude Code 시작
claude
```

IntelliJ 플러그인과 터미널 방식은 **동일한 기능**을 제공합니다.
차이점은 IntelliJ 플러그인이 에디터와 통합되어 코드 선택 → Claude에게 질문 같은 기능을 추가로 제공한다는 것입니다.

---

## 자주 발생하는 문제

### Q: `claude` 명령어를 찾을 수 없어요

```bash
# npm 글로벌 설치 경로 확인
npm list -g --depth=0

# PATH에 npm 글로벌 bin 추가 (Windows)
npm config get prefix
# 출력된 경로에 \bin 을 붙여서 환경변수 PATH에 추가
```

### Q: 인증이 자꾸 풀려요

```bash
# 인증 상태 확인
claude auth status

# 재로그인
claude auth login
```

### Q: IntelliJ 플러그인에서 Claude Code 탭이 안 보여요

1. IntelliJ를 완전히 종료 후 재시작
2. **View → Tool Windows** 목록에 "Claude Code"가 있는지 확인
3. 없다면 플러그인이 설치되지 않은 것 → **Settings → Plugins**에서 재확인

### Q: "CLAUDE.md를 못 찾겠다"는 메시지가 나와요

프로젝트 루트가 `ATStudio/`인지 확인하세요. IntelliJ에서 열 때 상위 폴더가 아닌 `ATStudio` 폴더 자체를 열어야 합니다.

### Q: API 사용료가 걱정돼요

- **짧고 구체적인 요청**이 토큰을 절약합니다
- 불필요하게 긴 대화를 이어가지 말고 `/clear`로 새 세션 시작
- 자세한 내용은 [10-Claude-에이전트-가이드.md](10-Claude-에이전트-가이드.md) 하단의 비용 관리 팁 참고

---

## 셋업 완료 체크리스트

- [ ] `claude --version` 이 버전 번호를 출력한다
- [ ] `claude` 실행 시 인증이 통과된다
- [ ] IntelliJ에 Claude Code 플러그인이 설치되어 있다
- [ ] IntelliJ에서 ATStudio 프로젝트를 열었을 때 Claude Code 탭이 보인다
- [ ] Claude에게 "이 프로젝트가 뭔지 설명해줘"라고 물었을 때 ATStudio에 대해 답한다

**모두 체크되면 셋업 완료! 🎉**

---

## 다음으로 읽을 문서

| 순서 | 문서 | 내용 |
|------|------|------|
| 1 | [10-Claude-에이전트-가이드.md](10-Claude-에이전트-가이드.md) | REQ→WI→위임 워크플로우, 에이전트 역할 전체 |
| 2 | [01-프로젝트-개요.md](01-프로젝트-개요.md) | ATStudio가 무엇인지 |
| 3 | [03-개발-스탠다드.md](03-개발-스탠다드.md) | 코드 작성 규칙 |

---

## 원본 참조 문서

| 문서 | 경로 |
|------|------|
| Claude Code 시작하기 (한국어) | `docs/guides/ko/01-시작하기.md` |
| 효과적인 대화법 | `docs/guides/ko/02-효과적인-대화법.md` |
| 시스템 한눈에 보기 | `docs/guides/ko/03-시스템-한눈에-보기.md` |
| 토큰과 비용 이해하기 | `docs/guides/ko/06-토큰과-비용-이해하기.md` |
