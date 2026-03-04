<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Gh-Workflows Changelog

## [Unreleased]

## [0.0.2]
### Added
- Right-click context menu on workflow runs with Re-run, Re-run failed jobs, Cancel, Delete, and Open in Browser actions
- Delete workflow run with confirmation dialog
- Non-destructive refresh with progress bar (existing tree preserved during loading)
- Background auto-refresh with configurable interval (default: 10 minutes)
- Auto-refresh enable/disable and interval settings in Settings dialog
- Settings persisted across IDE restarts via PersistentStateComponent
- Tree expansion state preserved across refreshes
- Auto-deploy GitHub Actions workflow: publishes to Marketplace on version bump

## [0.0.1]
### Added
- Workflow browser with tree view displaying workflows and recent runs
- Status icons for workflow run states (success, failure, in-progress, queued, cancelled, skipped)
- Trigger `workflow_dispatch` workflows with branch/tag selection and input parameters
- Open workflow or run in browser
- Auto-detect GitHub repository from git remotes
- Authentication via IntelliJ GitHub account or manual Personal Access Token
- Settings dialog for token and repository configuration
