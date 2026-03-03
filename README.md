# Gh-Workflows

![Build](https://github.com/wad-leeduhwan/Gh-Workflows/workflows/Build/badge.svg)

IntelliJ IDE에서 GitHub Actions 워크플로우를 조회, 모니터링, 트리거할 수 있는 플러그인입니다.

<!-- Plugin description -->
**Gh-Workflows** lets you browse, monitor, and trigger GitHub Actions workflows directly from your IntelliJ-based IDE.

### Features
- **Workflow Browser** — Tree view showing all workflows and recent runs with status icons
- **Trigger Workflows** — Run `workflow_dispatch` workflows with branch/tag selection and input parameters
- **Open in Browser** — Jump to any workflow or run on GitHub
- **Auto-detect Repository** — Automatically detects GitHub repository from git remotes
- **Seamless Auth** — Uses your IntelliJ GitHub account or a manual Personal Access Token
<!-- Plugin description end -->

## Requirements

- IntelliJ IDEA 2025.2+
- Git and GitHub plugins enabled
- GitHub account configured in IDE (or a Personal Access Token with `repo` and `workflow` scopes)

## Installation

- **From disk:**

  Download the [latest release](https://github.com/wad-leeduhwan/Gh-Workflows/releases/latest) and install using
  <kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>Install plugin from disk...</kbd>

## Usage

1. Open a project with a GitHub remote
2. Open the **GitHub Workflows** tool window (right sidebar)
3. Browse workflows and recent runs
4. Click the trigger button to dispatch a workflow with custom inputs

## Building from Source

```bash
./gradlew buildPlugin -x buildSearchableOptions
```

The plugin ZIP will be generated in `build/distributions/`.

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
