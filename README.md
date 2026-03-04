# Gh-Workflows

![Build](https://github.com/wad-leeduhwan/Gh-Workflows/workflows/Build/badge.svg)

IntelliJ IDE에서 GitHub Actions 워크플로우를 조회, 모니터링, 트리거할 수 있는 플러그인입니다.

<!-- Plugin description -->
**Gh-Workflows** lets you browse, monitor, and trigger GitHub Actions workflows directly from your IntelliJ-based IDE.
Take full control of GitHub Actions without leaving your IDE.

### Features

**Workflow Browser**
Tree view displaying all workflows and recent runs at a glance.
Each run shows its commit message, status icon, elapsed time, and branch name.

**Trigger Workflows**
Dispatch `workflow_dispatch` workflows with branch/tag selection and input parameters.
Supports `string`, `number`, `choice`, `boolean`, and `environment` input types.
Required inputs and default values are displayed automatically.

**Status Icons**
Intuitive icons for every workflow run state:
success, failure, in-progress, queued, cancelled, and skipped.

**Run Management (Context Menu)**
Right-click any workflow run to re-run all jobs, re-run failed jobs, cancel, or delete.
Dangerous actions like delete require confirmation before execution.

**Open in Browser**
Double-click any workflow or run node to jump directly to the GitHub page.

**Auto-detect Repository**
Automatically identifies the GitHub repository from your project's git remotes.
Supports SSH (`git@github.com:owner/repo.git`) and HTTPS formats.

**Auto-Refresh**
Automatically refreshes workflow data in the background at a configurable interval (default: 10 minutes).
Enable or disable auto-refresh and adjust the interval from the Settings dialog.

**Seamless Auth**
Authenticates using your IntelliJ GitHub account (recommended) or a manual Personal Access Token as fallback.
PAT requires `repo` and `workflow` scopes, stored securely in the IDE's PasswordSafe.

**CI/CD**
Automatic deployment via GitHub Actions — when `pluginVersion` in `gradle.properties` is bumped on the main branch, the plugin is built, tested, verified, and published to JetBrains Marketplace automatically.

### Getting Started

1. Open a project with a GitHub remote
2. Click the **GitHub Workflows** tab in the right sidebar
3. Browse workflows and recent runs in the tree view
4. Select a workflow and click **Run Workflow** to trigger it with custom inputs

### Requirements

- IntelliJ IDEA 2025.2 or later
- Git and GitHub plugins enabled
- GitHub account configured in IDE, or a Personal Access Token
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
