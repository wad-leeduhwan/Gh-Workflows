# Gh-Workflows — Custom Pages

> JetBrains Marketplace Custom Pages data.
> Each section (`## Page: ...`) corresponds to a single custom page.

---

## Page: Overview

### Gh-Workflows

Take full control of GitHub Actions without leaving your IDE.

**Gh-Workflows** is an IntelliJ plugin that lets you browse, monitor, and trigger GitHub Actions workflows directly from your IDE. View all workflows and run history in one sidebar panel, and dispatch workflows instantly — no browser switching required.

#### Key Features

| Feature | Description |
|---------|-------------|
| **Workflow Browser** | Tree view showing all workflows and recent runs at a glance |
| **Trigger Workflows** | Run `workflow_dispatch` workflows with branch/tag selection and input parameters |
| **Status Icons** | Intuitive icons for success, failure, in-progress, queued, cancelled, and skipped states |
| **Open in Browser** | Double-click any workflow or run to open it on GitHub |
| **Auto-detect Repository** | Automatically detects GitHub repository from git remotes |
| **Auto-Refresh** | Background auto-refresh at configurable interval (default: 10 min) |
| **Run Management** | Right-click context menu for Re-run, Cancel, Delete, and more |
| **Failed Run Jobs** | Expand failed runs to see individual job statuses |
| **Seamless Auth** | Uses your IntelliJ GitHub account or a manual Personal Access Token |
| **Auto-Deploy** | Automatically publishes to Marketplace via GitHub Actions on version bump |

#### Supported Environments

- IntelliJ IDEA 2025.2+
- Git and GitHub plugins must be enabled
- GitHub account or PAT (`repo`, `workflow` scopes)

---

## Page: Features

### Feature Details

#### 1. Workflow Browser

Browse all workflows in your repository as a tree structure from the **GitHub Workflows** tool window in the right sidebar.

```
Workflows
├── CI Build [.github/workflows/ci.yml]
│   ├── #142  feat: Add login API        ✅  (2h ago)   main
│   ├── #141  fix: Null pointer issue     ❌  (1d ago)   dev
│   └── #140  chore: Update deps          ✅  (3d ago)   main
├── Deploy [.github/workflows/deploy.yml]
│   ├── #85   Release v2.1.0             ⏳  (5m ago)   release
│   └── #84   Release v2.0.0             ✅  (7d ago)   release
└── Nightly Test [.github/workflows/nightly.yml]
    └── No runs found
```

**Status Icon Reference:**

| Icon | Status | Description |
|------|--------|-------------|
| ✅ | Success | Workflow run completed successfully |
| ❌ | Failure | Workflow run failed |
| ⏳ | In Progress | Currently running |
| 🔘 | Queued | Waiting to be executed |
| ✖️ | Cancelled | Cancelled by user |
| ⊘ | Skipped | Skipped due to condition mismatch |

- Displays up to 10 recent runs per workflow.
- Selection state is preserved across refreshes.
- A progress bar is shown while loading.

---

#### 2. Trigger Workflows

Trigger workflows with `workflow_dispatch` events directly from the IDE.

**Trigger Dialog Layout:**

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

**Supported Input Types:**

| Type | UI Component | Example |
|------|-------------|---------|
| `string` | Text field | Deploy message, region code |
| `number` | Text field (numeric) | Replica count, timeout |
| `choice` | Dropdown selector | Environment (staging/production) |
| `boolean` | Checkbox | Debug mode ON/OFF |
| `environment` | Dropdown selector | GitHub Environment |

- Required inputs (*) and default values are displayed automatically.
- Workflows can be triggered from both branches and tags.
- Workflow YAML is parsed directly to read input definitions.

---

#### 3. Run Management

Right-click a run node in the tree view to open the context menu.

| Menu Item | Description | Enabled When |
|-----------|-------------|-------------|
| **Re-run all jobs** | Re-run all jobs in the run | Completed runs |
| **Re-run failed jobs** | Re-run only failed jobs | Failed runs |
| **Cancel run** | Cancel the running workflow | In-progress/queued runs |
| **Delete run** | Delete the run record (with confirmation) | Completed runs |
| **Open in Browser** | Open on GitHub | Always |

---

#### 4. Failed Run Job Details

Expand a failed workflow run to see the status of each individual job.

```
▶ deploy.yml
  ├─ #42 Fix login bug          ✅
  ├─ ▼ #41 Add new feature      ❌   ← Expand failed run
  │    ├─ build                  ✅
  │    ├─ test                   ❌   ← Failed job
  │    └─ deploy                 ⊘   (skipped)
  └─ #40 Update docs            ✅
```

- Job information is only loaded for failed runs (minimizing API calls).
- Each job displays a status icon (✅❌⊘, etc.).
- **Double-click** a job to open its log page on GitHub.
- **Right-click** a job to access the Open in Browser menu.

---

#### 5. Auto-Refresh

Automatically refreshes workflow data in the background at a configurable interval.

- **Default**: Auto-refresh ON, 10-minute interval
- **Configuration**: Toggle on/off and set interval (1–120 min) in the Settings dialog
- **Persistence**: Settings are saved across IDE restarts
- Changes take effect immediately (no IDE restart required)

---

#### 6. Open in Browser

- **Double-click** a workflow or run in the tree view to open the corresponding GitHub page.
- The **Open in Browser** toolbar button provides the same functionality.
- Selecting a workflow navigates to the workflow page.
- Selecting a run navigates to the run detail page.

---

#### 7. Auto-detect Repository

Automatically detects the GitHub repository by analyzing the project's git remotes.

**Supported URL Formats:**

```
SSH:   git@github.com:owner/repo.git
HTTPS: https://github.com/owner/repo.git
HTTPS: https://github.com/owner/repo
```

- You can also manually set `owner/repo` in the settings.
- The auto-detected repository name is displayed in the bottom status bar.

---

#### 8. Auto-Deploy

When a commit with a changed `pluginVersion` in `gradle.properties` is pushed to the `main` branch, GitHub Actions automatically handles the deployment.

**Deployment Pipeline:**

```
push to main (version bumped)
     │
     ▼
[Check Version] ── Detect version change
     │
     ▼
[Build & Test] ── buildPlugin → check → verifyPlugin
     │
     ▼
[Deploy] ── publishPlugin → Create GitHub Release
```

- Pushes without a version change do not trigger deployment.
- Release notes are automatically extracted from the CHANGELOG.
- Build artifacts are uploaded as GitHub Release assets.

---

#### 9. Seamless Auth

Two authentication methods are supported, automatically selected by priority.

**Authentication Priority:**

```
1st: IntelliJ GitHub Account
     Uses the token from the account registered in
     Settings > Version Control > GitHub
     (No additional setup required — recommended)

2nd: Manual Personal Access Token (PAT)
     Entered directly in the Settings dialog
     Required scopes: repo, workflow
     Stored securely in the IDE's PasswordSafe
```

---

## Page: Installation

### Installation Guide

#### Requirements

| Item | Minimum Requirement |
|------|---------------------|
| IDE | IntelliJ IDEA 2025.2 or later |
| JDK | 21+ |
| Plugins | Git and GitHub plugins enabled |
| Auth | GitHub account or PAT |

#### Installation Methods

##### Method 1: Install from Marketplace (Recommended)

1. Open <kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> tab
2. Search for **"Gh-Workflows"**
3. Click **Install** and restart the IDE

##### Method 2: Install from Disk

1. Download the latest ZIP from [GitHub Releases](https://github.com/wad-leeduhwan/Gh-Workflows/releases/latest)
2. Go to <kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install Plugin from Disk...</kbd>
3. Select the downloaded ZIP file and restart the IDE

##### Method 3: Build from Source

```bash
git clone https://github.com/wad-leeduhwan/Gh-Workflows.git
cd Gh-Workflows
./gradlew buildPlugin -x buildSearchableOptions
```

Build output: `build/distributions/Gh-Workflows-*.zip`

---

#### Initial Setup

##### Step 1: Configure GitHub Authentication

**Using IntelliJ GitHub Account (Recommended):**

1. Go to <kbd>Settings</kbd> > <kbd>Version Control</kbd> > <kbd>GitHub</kbd>
2. Click the **+** button to add your GitHub account
3. Sign in — ready to use with no additional configuration

**Using a Personal Access Token (Alternative):**

1. Go to GitHub > Settings > Developer settings > Personal access tokens > Tokens (classic)
2. Click **Generate new token**
3. Select scopes: `repo`, `workflow`
4. Copy the token
5. In the IDE, open the GitHub Workflows tool window > ⚙️ Settings > paste into the Manual Token field

##### Step 2: Open the Tool Window

- Click the **GitHub Workflows** tab in the right sidebar
- Or go to <kbd>View</kbd> > <kbd>Tool Windows</kbd> > <kbd>GitHub Workflows</kbd>

##### Step 3: Verify Workflows

If your project has a GitHub remote configured, workflows will be loaded automatically.

---

## Page: Usage Guide

### Usage Guide

#### Basic Workflow

```
Open Project → Click Sidebar Tab → Workflows Auto-load
     │                                    │
     │         ┌────────────────────────────┤
     │         │                            │
     ▼         ▼                            ▼
 Browse       View Run                Trigger
 Workflows    History                 Workflows
     │              │                      │
     ▼              ▼                      ▼
 Double-click → Double-click →     Set Branch/Inputs →
 Open on GitHub  Open Run Details   Click Run Workflow
```

---

#### Browsing Workflows

1. Open the **GitHub Workflows** tool window from the right sidebar.
2. All workflows in the repository are displayed in a tree structure.
3. Expand any workflow to see its 10 most recent runs.
4. Run info includes: commit message, status icon, elapsed time, and branch name.

#### Refreshing Workflows

- Click the **Refresh** button in the toolbar.
- Data is fetched in the background with a progress bar displayed.
- Existing selection and expansion state are preserved after refresh.

#### Triggering a Workflow

1. Select a workflow that has `workflow_dispatch` configured in the tree view.
2. Click the **Run Workflow** button in the toolbar.
3. The trigger dialog opens:
   - **Branch/Tag Selection**: Choose the target branch or tag from the dropdown
   - **Input Parameters**: Fill in the input values defined in the workflow
4. Click **Run workflow** to dispatch the workflow.
5. A balloon notification will indicate success or failure.

#### Opening in Browser

- **Double-click** a workflow node to open the GitHub workflow page.
- **Double-click** a run node to open the GitHub run detail page.
- The **Open in Browser** toolbar button provides the same functionality.

#### Changing Settings

Click the **⚙️ Settings** button in the toolbar to open the settings dialog:

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

### Frequently Asked Questions (FAQ)

#### Q: Workflows are not showing up.

**Checklist:**

1. Verify that your project has a GitHub remote configured.
   ```bash
   git remote -v
   # origin  git@github.com:owner/repo.git (fetch)
   ```
2. Ensure the Git and GitHub plugins are enabled.
   - <kbd>Settings</kbd> > <kbd>Plugins</kbd> > search for "Git" and "GitHub"
3. Verify that GitHub authentication is properly configured.
   - <kbd>Settings</kbd> > <kbd>Version Control</kbd> > <kbd>GitHub</kbd>

---

#### Q: The "Run Workflow" button is disabled.

- Only workflows with a `workflow_dispatch` event can be triggered.
- Verify that your workflow YAML includes the following:
  ```yaml
  on:
    workflow_dispatch:
      inputs:
        # ... (optional)
  ```

---

#### Q: I'm getting authentication errors.

**When using an IntelliJ account:**
- Re-authenticate your account at <kbd>Settings</kbd> > <kbd>Version Control</kbd> > <kbd>GitHub</kbd>.

**When using a PAT:**
- Ensure the token includes the `repo` and `workflow` scopes.
- Verify the token has not expired.
- Try re-entering the token in the Settings dialog.

---

#### Q: Does it work with private repositories?

Yes. As long as your authenticated GitHub account or PAT has access to the repository, you can browse and trigger workflows in private repositories.

---

#### Q: Which IDEs are supported?

All IntelliJ-based IDEs version 2025.2 or later are supported:
- IntelliJ IDEA (Community / Ultimate)
- WebStorm
- PyCharm
- GoLand
- PhpStorm
- Rider
- CLion
- Android Studio (verify compatible build number)

---

#### Q: How do I check the results after triggering a workflow?

1. **In the tool window**: Click the Refresh button to update with the latest run status.
2. **On GitHub**: Double-click the run node to view detailed logs on GitHub.

---

#### Q: How often is the data refreshed?

By default, data is **automatically refreshed every 10 minutes** in the background. You can disable auto-refresh or change the interval (1–120 minutes) in the Settings dialog. To manually refresh, click the **Refresh** button in the toolbar. Up to 10 recent runs are fetched per workflow.

---

## Page: Shortcuts & Tips

### Shortcuts & Tips

#### Toolbar Actions

| Action | Description | Location |
|--------|-------------|----------|
| **Refresh** | Refresh the workflow and run list | Toolbar |
| **Run Workflow** | Trigger the selected workflow | Toolbar |
| **Open in Browser** | Open the selected item on GitHub | Toolbar |
| **Settings** | Configure authentication and repository | Toolbar |

#### Mouse Interactions

| Action | Result |
|--------|--------|
| Click a workflow node | Expand/collapse the run list |
| Double-click a run node | Open the GitHub run page |
| Double-click a workflow node | Open the GitHub workflow page |
| Right-click a run node | Context menu: Re-run, Cancel, Delete, etc. |
| Expand a failed run node | View individual job statuses |
| Double-click a job node | Open the GitHub job log page |

#### Useful Tips

1. **Quick Monitoring**: After a deployment, hit Refresh to instantly check the run status.
2. **Tag Deployments**: Select a tag in the trigger dialog to run a workflow from a specific version.
3. **Multi-Repository**: Open multiple projects to manage independent workflow lists per project.
4. **Status Check**: View the currently connected repository name and status message in the bottom status bar.
5. **Error Details**: Check balloon notifications for detailed error messages when something goes wrong.
6. **Auto-Refresh**: Data refreshes automatically every 10 minutes by default. Adjust or disable in Settings.
