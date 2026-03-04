# Gh-Workflows — Custom Pages

> JetBrains Marketplace Custom Pages 데이터
> 각 섹션(`## Page: ...`)이 하나의 커스텀 페이지에 대응합니다.

---

## Page: Overview

### Gh-Workflows

IDE를 떠나지 않고 GitHub Actions를 완벽하게 제어하세요.

**Gh-Workflows**는 IntelliJ 기반 IDE에서 GitHub Actions 워크플로우를 조회, 모니터링, 트리거할 수 있는 플러그인입니다. 사이드바 한 곳에서 모든 워크플로우와 실행 이력을 실시간으로 확인하고, 브라우저 전환 없이 워크플로우를 즉시 실행할 수 있습니다.

#### 핵심 기능

| 기능 | 설명 |
|------|------|
| **Workflow Browser** | 트리 뷰로 모든 워크플로우와 최근 실행 내역을 한눈에 확인 |
| **Trigger Workflows** | `workflow_dispatch` 워크플로우를 브랜치/태그 선택 및 입력 파라미터와 함께 실행 |
| **Status Icons** | 성공, 실패, 진행 중, 대기, 취소, 건너뜀 상태를 아이콘으로 직관적 표시 |
| **Open in Browser** | 워크플로우나 실행을 더블 클릭하여 GitHub에서 바로 열기 |
| **Auto-detect Repository** | Git remote에서 GitHub 리포지토리를 자동 감지 |
| **Auto-Refresh** | 백그라운드 자동 갱신 (기본 10분, 설정에서 인터벌 변경 가능) |
| **Run Management** | 우클릭 메뉴로 Re-run, Cancel, Delete 등 실행 관리 |
| **Seamless Auth** | IntelliJ GitHub 계정 또는 Personal Access Token으로 간편 인증 |
| **Auto-Deploy** | 버전 변경 시 GitHub Actions가 자동으로 Marketplace에 배포 |

#### 지원 환경

- IntelliJ IDEA 2025.2+
- Git 및 GitHub 플러그인 활성화 필요
- GitHub 계정 또는 PAT (`repo`, `workflow` 스코프)

---

## Page: Features

### 기능 상세

#### 1. Workflow Browser — 워크플로우 브라우저

오른쪽 사이드바의 **GitHub Workflows** 도구 창에서 리포지토리의 모든 워크플로우를 트리 구조로 탐색할 수 있습니다.

```
Workflows
├── CI Build [.github/workflows/ci.yml]
│   ├── #142  feat: Add login API        ✅  (2시간 전)  main
│   ├── #141  fix: Null pointer issue     ❌  (1일 전)    dev
│   └── #140  chore: Update deps          ✅  (3일 전)    main
├── Deploy [.github/workflows/deploy.yml]
│   ├── #85   Release v2.1.0             ⏳  (5분 전)    release
│   └── #84   Release v2.0.0             ✅  (7일 전)    release
└── Nightly Test [.github/workflows/nightly.yml]
    └── 실행 기록 없음
```

**상태 아이콘 안내:**

| 아이콘 | 상태 | 설명 |
|--------|------|------|
| ✅ | Success | 워크플로우 실행 성공 |
| ❌ | Failure | 워크플로우 실행 실패 |
| ⏳ | In Progress | 현재 실행 중 |
| 🔘 | Queued | 실행 대기 중 |
| ✖️ | Cancelled | 사용자에 의해 취소됨 |
| ⊘ | Skipped | 조건 불일치로 건너뜀 |

- 워크플로우당 최근 10개의 실행 이력을 표시합니다.
- 새로고침 시 기존 선택 상태가 유지됩니다.
- 로딩 중 프로그레스 바가 표시됩니다.

---

#### 2. Trigger Workflows — 워크플로우 트리거

`workflow_dispatch` 이벤트가 설정된 워크플로우를 IDE에서 직접 실행할 수 있습니다.

**트리거 다이얼로그 구성:**

```
┌─────────────────────────────────────────┐
│  Run workflow: deploy.yml               │
│                                         │
│  Use workflow from:  [main         ▼]   │
│                       main              │
│                       develop           │
│                       tag: v2.1.0       │
│                       tag: v2.0.0       │
│                                         │
│  ─────────── Inputs ───────────         │
│                                         │
│  Environment *:      [production   ▼]   │
│                       staging           │
│                       production        │
│                                         │
│  Debug mode:         [☑]                │
│                                         │
│  Deploy region:      [us-east-1     ]   │
│    hint: AWS region code                │
│                                         │
│              [Run workflow]  [Cancel]    │
└─────────────────────────────────────────┘
```

**지원하는 입력 타입:**

| 타입 | UI 컴포넌트 | 예시 |
|------|-------------|------|
| `string` | 텍스트 필드 | 배포 메시지, 리전 코드 |
| `number` | 텍스트 필드 (숫자) | 레플리카 수, 타임아웃 |
| `choice` | 드롭다운 셀렉터 | 환경(staging/production) |
| `boolean` | 체크박스 | 디버그 모드 ON/OFF |
| `environment` | 드롭다운 셀렉터 | GitHub Environment |

- 필수 입력(*) 항목과 기본값이 자동으로 표시됩니다.
- 브랜치뿐 아니라 태그에서도 워크플로우를 실행할 수 있습니다.
- 워크플로우 YAML을 직접 파싱하여 입력 정의를 읽어옵니다.

---

#### 3. Run Management — 실행 관리

트리 뷰에서 실행 노드를 **우클릭**하면 컨텍스트 메뉴가 나타납니다.

| 메뉴 | 설명 | 활성화 조건 |
|------|------|------------|
| **Re-run all jobs** | 모든 job 재실행 | 완료된 실행 |
| **Re-run failed jobs** | 실패한 job만 재실행 | 실패한 실행 |
| **Cancel run** | 실행 중지 | 진행 중/대기 중 실행 |
| **Delete run** | 실행 기록 삭제 (확인 다이얼로그) | 완료된 실행 |
| **Open in Browser** | GitHub에서 열기 | 항상 |

---

#### 4. Auto-Refresh — 자동 갱신

백그라운드에서 설정된 인터벌마다 자동으로 워크플로우 데이터를 갱신합니다.

- **기본 설정**: 자동 갱신 ON, 10분 간격
- **설정 변경**: Settings 다이얼로그에서 on/off 및 인터벌(1~120분) 변경
- **설정 영속화**: IDE 재시작 후에도 설정이 유지됩니다
- 설정 변경 시 즉시 반영 (IDE 재시작 불필요)

---

#### 5. Open in Browser — 브라우저에서 열기

- 트리 뷰에서 워크플로우 또는 실행을 **더블 클릭**하면 GitHub 페이지가 열립니다.
- 툴바의 **Open in Browser** 버튼으로도 동일한 동작이 가능합니다.
- 워크플로우 선택 시 → 워크플로우 페이지로 이동
- 실행 선택 시 → 해당 실행 상세 페이지로 이동

---

#### 6. Auto-detect Repository — 리포지토리 자동 감지

프로젝트의 Git remote를 분석하여 GitHub 리포지토리를 자동으로 감지합니다.

**지원 URL 형식:**

```
SSH:   git@github.com:owner/repo.git
HTTPS: https://github.com/owner/repo.git
HTTPS: https://github.com/owner/repo
```

- 수동으로 `owner/repo`를 설정할 수도 있습니다.
- 자동 감지된 리포지토리 이름이 하단 상태바에 표시됩니다.

---

#### 7. Auto-Deploy — 자동 배포

`gradle.properties`의 `pluginVersion`이 변경된 커밋이 `main` 브랜치에 push되면 GitHub Actions가 자동으로 배포를 수행합니다.

**배포 파이프라인:**

```
push to main (version bumped)
     │
     ▼
[Check Version] ── 버전 변경 감지
     │
     ▼
[Build & Test] ── buildPlugin → check → verifyPlugin
     │
     ▼
[Deploy] ── publishPlugin → GitHub Release 생성
```

- 버전이 변경되지 않은 push에서는 배포가 실행되지 않습니다.
- CHANGELOG에서 릴리즈 노트가 자동 추출됩니다.
- 빌드 아티팩트가 GitHub Release asset으로 업로드됩니다.

---

#### 8. Seamless Auth — 인증

두 가지 인증 방식을 지원하며, 우선순위에 따라 자동 선택됩니다.

**인증 우선순위:**

```
1순위: IntelliJ GitHub 계정
       Settings > Version Control > GitHub에 등록된 계정의 토큰 사용
       (별도 설정 불필요, 추천)

2순위: Manual Personal Access Token (PAT)
       Settings 다이얼로그에서 직접 입력
       필요 스코프: repo, workflow
       IDE의 PasswordSafe에 안전하게 저장
```

---

## Page: Installation

### 설치 가이드

#### 요구사항

| 항목 | 최소 요구사항 |
|------|--------------|
| IDE | IntelliJ IDEA 2025.2 이상 |
| JDK | 21+ |
| 플러그인 | Git, GitHub 플러그인 활성화 |
| 인증 | GitHub 계정 또는 PAT |

#### 설치 방법

##### 방법 1: Marketplace에서 설치 (권장)

1. <kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> 탭 열기
2. **"Gh-Workflows"** 검색
3. **Install** 클릭 후 IDE 재시작

##### 방법 2: 디스크에서 설치

1. [GitHub Releases](https://github.com/wad-leeduhwan/Gh-Workflows/releases/latest)에서 최신 ZIP 다운로드
2. <kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install Plugin from Disk...</kbd>
3. 다운로드한 ZIP 파일 선택 후 IDE 재시작

##### 방법 3: 소스에서 빌드

```bash
git clone https://github.com/wad-leeduhwan/Gh-Workflows.git
cd Gh-Workflows
./gradlew buildPlugin -x buildSearchableOptions
```

빌드 결과물: `build/distributions/Gh-Workflows-*.zip`

---

#### 초기 설정

##### Step 1: GitHub 인증 설정

**IntelliJ GitHub 계정 사용 (권장):**

1. <kbd>Settings</kbd> > <kbd>Version Control</kbd> > <kbd>GitHub</kbd>
2. **+** 버튼으로 GitHub 계정 추가
3. 로그인 완료 — 별도 설정 없이 바로 사용 가능

**Personal Access Token 사용 (대안):**

1. GitHub > Settings > Developer settings > Personal access tokens > Tokens (classic)
2. **Generate new token** 클릭
3. 스코프 선택: `repo`, `workflow`
4. 토큰 복사
5. IDE에서 GitHub Workflows 도구 창 > ⚙️ Settings > Manual Token 필드에 붙여넣기

##### Step 2: 도구 창 열기

- 오른쪽 사이드바에서 **GitHub Workflows** 탭 클릭
- 또는 <kbd>View</kbd> > <kbd>Tool Windows</kbd> > <kbd>GitHub Workflows</kbd>

##### Step 3: 워크플로우 확인

프로젝트에 GitHub remote가 설정되어 있다면 자동으로 워크플로우 목록을 불러옵니다.

---

## Page: Usage Guide

### 사용 가이드

#### 기본 워크플로우

```
프로젝트 열기 → 사이드바 탭 클릭 → 워크플로우 자동 로드
     │                                    │
     │         ┌────────────────────────────┤
     │         │                            │
     ▼         ▼                            ▼
  워크플로우 조회    실행 이력 확인      워크플로우 트리거
     │              │                      │
     ▼              ▼                      ▼
 더블 클릭 →     더블 클릭 →         브랜치/입력 설정 →
 GitHub에서 열기  실행 상세 열기       Run workflow 클릭
```

---

#### 워크플로우 조회

1. 오른쪽 사이드바에서 **GitHub Workflows** 도구 창을 엽니다.
2. 리포지토리의 모든 워크플로우가 트리 구조로 표시됩니다.
3. 각 워크플로우를 펼치면 최근 10개의 실행 이력이 나타납니다.
4. 실행 정보: 커밋 메시지, 상태 아이콘, 경과 시간, 브랜치명

#### 워크플로우 새로고침

- 툴바의 **Refresh** 버튼 클릭
- 백그라운드에서 데이터를 가져오며, 프로그레스 바가 표시됩니다.
- 새로고침 후에도 기존 선택과 펼침 상태가 유지됩니다.

#### 워크플로우 트리거

1. 트리 뷰에서 `workflow_dispatch`가 설정된 워크플로우를 선택합니다.
2. 툴바의 **Run Workflow** (▶) 버튼을 클릭합니다.
3. 트리거 다이얼로그가 열립니다:
   - **브랜치/태그 선택**: 드롭다운에서 대상 브랜치 또는 태그를 선택
   - **입력 파라미터**: 워크플로우에 정의된 입력값을 채움
4. **Run workflow** 버튼을 클릭하면 워크플로우가 실행됩니다.
5. 성공/실패 알림이 풍선 노티피케이션으로 표시됩니다.

#### GitHub에서 열기

- 워크플로우 노드를 **더블 클릭** → GitHub 워크플로우 페이지
- 실행 노드를 **더블 클릭** → GitHub 실행 상세 페이지
- 툴바의 **Open in Browser** 버튼으로도 가능합니다.

#### 설정 변경

툴바의 **⚙️ Settings** 버튼을 클릭하면 설정 다이얼로그가 열립니다:

```
┌─────────────────────────────────────────────┐
│  Settings                                   │
│                                             │
│  IntelliJ GitHub Account: user@email.com ✓  │
│  └ Using token from IDE settings            │
│                                             │
│  ─────────────────────────────────────────  │
│                                             │
│  Manual Token (fallback):                   │
│  [ghp_xxxx...                          ]    │
│  └ Only needed if IDE account unavailable   │
│                                             │
│  ─────────────────────────────────────────  │
│                                             │
│  Repository (owner/repo):                   │
│  [                                     ]    │
│  Detected: wad-leeduhwan/Gh-Workflows       │
│  └ Leave empty to use auto-detected repo    │
│                                             │
│  ─────────────────────────────────────────  │
│                                             │
│  [☑] Enable auto-refresh                    │
│  Refresh interval (minutes): [10       ↕]   │
│  └ Minimum 1 minute, default 10 minutes     │
│                                             │
│                         [Save]  [Cancel]    │
└─────────────────────────────────────────────┘
```

---

## Page: FAQ

### 자주 묻는 질문 (FAQ)

#### Q: 워크플로우가 표시되지 않습니다.

**확인사항:**

1. 프로젝트에 GitHub remote가 설정되어 있는지 확인하세요.
   ```bash
   git remote -v
   # origin  git@github.com:owner/repo.git (fetch)
   ```
2. Git 및 GitHub 플러그인이 활성화되어 있는지 확인하세요.
   - <kbd>Settings</kbd> > <kbd>Plugins</kbd> > "Git", "GitHub" 검색
3. GitHub 인증이 올바르게 설정되어 있는지 확인하세요.
   - <kbd>Settings</kbd> > <kbd>Version Control</kbd> > <kbd>GitHub</kbd>

---

#### Q: "Run Workflow" 버튼이 비활성화되어 있습니다.

- `workflow_dispatch` 이벤트가 설정된 워크플로우만 트리거할 수 있습니다.
- 워크플로우 YAML에 아래 설정이 있는지 확인하세요:
  ```yaml
  on:
    workflow_dispatch:
      inputs:
        # ... (optional)
  ```

---

#### Q: 인증 오류가 발생합니다.

**IntelliJ 계정 사용 시:**
- <kbd>Settings</kbd> > <kbd>Version Control</kbd> > <kbd>GitHub</kbd>에서 계정을 재인증하세요.

**PAT 사용 시:**
- 토큰에 `repo`와 `workflow` 스코프가 포함되어 있는지 확인하세요.
- 토큰이 만료되지 않았는지 확인하세요.
- Settings 다이얼로그에서 토큰을 다시 입력해보세요.

---

#### Q: 비공개(private) 리포지토리에서도 사용할 수 있나요?

네, 가능합니다. 인증된 GitHub 계정 또는 PAT에 해당 리포지토리에 대한 접근 권한이 있으면 비공개 리포지토리의 워크플로우도 조회 및 트리거할 수 있습니다.

---

#### Q: 지원하는 IDE는 무엇인가요?

IntelliJ IDEA 2025.2 이상의 모든 IntelliJ 기반 IDE를 지원합니다:
- IntelliJ IDEA (Community / Ultimate)
- WebStorm
- PyCharm
- GoLand
- PhpStorm
- Rider
- CLion
- Android Studio (호환 빌드 번호 확인 필요)

---

#### Q: 워크플로우 실행 후 결과를 어떻게 확인하나요?

1. **도구 창에서 확인**: Refresh 버튼을 클릭하면 최신 실행 상태가 업데이트됩니다.
2. **GitHub에서 확인**: 실행 노드를 더블 클릭하여 GitHub에서 상세 로그를 확인하세요.

---

#### Q: 데이터는 얼마나 자주 갱신되나요?

기본적으로 **10분마다 백그라운드에서 자동 갱신**됩니다. Settings 다이얼로그에서 자동 갱신을 끄거나 인터벌(1~120분)을 변경할 수 있습니다. 수동으로 갱신하려면 툴바의 **Refresh** 버튼을 클릭하세요. 각 워크플로우당 최근 10개의 실행 이력을 가져옵니다.

---

## Page: Shortcuts & Tips

### 단축키 및 팁

#### 툴바 액션

| 액션 | 설명 | 위치 |
|------|------|------|
| **Refresh** | 워크플로우 및 실행 목록 새로고침 | 툴바 |
| **Run Workflow** | 선택한 워크플로우 트리거 | 툴바 |
| **Open in Browser** | 선택 항목을 GitHub에서 열기 | 툴바 |
| **Settings** | 인증 및 리포지토리 설정 | 툴바 |

#### 마우스 인터랙션

| 동작 | 결과 |
|------|------|
| 워크플로우 노드 클릭 | 실행 목록 펼치기/접기 |
| 실행 노드 더블 클릭 | GitHub 실행 페이지 열기 |
| 워크플로우 노드 더블 클릭 | GitHub 워크플로우 페이지 열기 |
| 실행 노드 우클릭 | Re-run, Cancel, Delete 등 컨텍스트 메뉴 |

#### 유용한 팁

1. **빠른 모니터링**: 배포 후 Refresh를 눌러 실행 상태를 즉시 확인하세요.
2. **태그 배포**: 트리거 다이얼로그에서 태그를 선택하면 특정 버전으로 워크플로우를 실행할 수 있습니다.
3. **멀티 리포지토리**: 여러 프로젝트를 열어두면 각 프로젝트별로 독립된 워크플로우 목록을 관리합니다.
4. **상태 확인**: 하단 상태바에서 현재 연결된 리포지토리 이름과 상태 메시지를 확인하세요.
5. **오류 발생 시**: 풍선 노티피케이션에서 상세 오류 메시지를 확인할 수 있습니다.
6. **자동 갱신**: 기본 10분 간격으로 자동 갱신됩니다. Settings에서 인터벌을 조정하거나 끌 수 있습니다.