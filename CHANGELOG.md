<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Gh-Workflows Changelog

## [Unreleased]

## [0.0.5]
### Added
- Favorites: save frequently used workflow URLs for quick access from the toolbar

### Fixed
- Branch/tag dropdown arrow-key navigation no longer breaks filtering in the trigger dialog

## [0.0.4]
### Added
- Two-line rendering for workflow run nodes: line 1 shows status icon, run number, and commit message; line 2 shows actor, elapsed time, and branch name

## [0.0.3]
### Added
- Failed run job details: expand failed runs to see individual job statuses
- Auto-deploy GitHub Actions workflow: publishes to Marketplace on version bump

### Fixed
- Right-click no longer triggers double-click browser open

## [0.0.2]
### Added
- Right-click context menu on workflow runs with Re-run, Re-run failed jobs, Cancel, Delete, and Open in Browser actions
- Delete workflow run with confirmation dialog
- Non-destructive refresh with progress bar (existing tree preserved during loading)
- Background auto-refresh with configurable interval (default: 10 minutes)
- Auto-refresh enable/disable and interval settings in Settings dialog
- Settings persisted across IDE restarts via PersistentStateComponent
- Tree expansion state preserved across refreshes

## [0.0.1]
### Added
- Workflow browser with tree view displaying workflows and recent runs
- Status icons for workflow run states (success, failure, in-progress, queued, cancelled, skipped)
- Trigger `workflow_dispatch` workflows with branch/tag selection and input parameters
- Open workflow or run in browser
- Auto-detect GitHub repository from git remotes
- Authentication via IntelliJ GitHub account or manual Personal Access Token
- Settings dialog for token and repository configuration
