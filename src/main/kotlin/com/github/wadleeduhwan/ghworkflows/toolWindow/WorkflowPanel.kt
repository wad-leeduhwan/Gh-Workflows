package com.github.wadleeduhwan.ghworkflows.toolWindow

import com.github.wadleeduhwan.ghworkflows.WorkflowBundle
import com.github.wadleeduhwan.ghworkflows.api.models.Job
import com.github.wadleeduhwan.ghworkflows.api.models.Workflow
import com.github.wadleeduhwan.ghworkflows.api.models.WorkflowRun
import com.github.wadleeduhwan.ghworkflows.auth.GitHubTokenManager
import com.github.wadleeduhwan.ghworkflows.services.GitHubWorkflowService
import com.github.wadleeduhwan.ghworkflows.toolWindow.components.WorkflowTreeCellRenderer
import com.github.wadleeduhwan.ghworkflows.toolWindow.components.WorkflowTreeNode
import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JProgressBar
import javax.swing.JSeparator
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class WorkflowPanel(
    private val project: Project,
) : JPanel(BorderLayout()), Disposable {

    private val service = project.getService(GitHubWorkflowService::class.java)
    private val rootNode = DefaultMutableTreeNode("Workflows")
    private val treeModel = DefaultTreeModel(rootNode)
    private val tree = Tree(treeModel)
    private val statusLabel = JBLabel("", SwingConstants.CENTER)
    private val repoLabel = JBLabel("", SwingConstants.LEFT)
    private val progressBar = JProgressBar().apply {
        isIndeterminate = true
        isVisible = false
    }

    private val dataListener = GitHubWorkflowService.WorkflowDataListener {
        ApplicationManager.getApplication().invokeLater { updateTree() }
    }

    init {
        tree.cellRenderer = WorkflowTreeCellRenderer()
        tree.isRootVisible = false
        tree.showsRootHandles = true
        tree.rowHeight = 0 // variable row heights for 2-line run nodes

        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2 && !e.isPopupTrigger && SwingUtilities.isLeftMouseButton(e)) {
                    val path = tree.getPathForLocation(e.x, e.y) ?: return
                    val node = path.lastPathComponent as? DefaultMutableTreeNode ?: return
                    when (val userObj = node.userObject) {
                        is WorkflowTreeNode.RunNode -> BrowserUtil.browse(userObj.run.htmlUrl)
                        is WorkflowTreeNode.WorkflowNode -> BrowserUtil.browse(userObj.workflow.htmlUrl)
                        is WorkflowTreeNode.JobNode -> BrowserUtil.browse(userObj.job.htmlUrl)
                    }
                }
            }

            override fun mousePressed(e: MouseEvent) = handlePopup(e)
            override fun mouseReleased(e: MouseEvent) = handlePopup(e)

            private fun handlePopup(e: MouseEvent) {
                if (!e.isPopupTrigger) return
                val path = tree.getPathForLocation(e.x, e.y) ?: return
                tree.selectionPath = path
                val node = path.lastPathComponent as? DefaultMutableTreeNode ?: return
                when (val userObj = node.userObject) {
                    is WorkflowTreeNode.RunNode -> showRunContextMenu(e, userObj.run)
                    is WorkflowTreeNode.WorkflowNode -> showWorkflowContextMenu(e, userObj.workflow)
                    is WorkflowTreeNode.JobNode -> showJobContextMenu(e, userObj.job)
                }
            }
        })

        repoLabel.foreground = java.awt.Color.GRAY
        val bottomPanel = JPanel(BorderLayout()).apply {
            add(repoLabel, BorderLayout.WEST)
            add(statusLabel, BorderLayout.CENTER)
        }

        add(progressBar, BorderLayout.NORTH)
        add(JBScrollPane(tree), BorderLayout.CENTER)
        add(bottomPanel, BorderLayout.SOUTH)

        service.addListener(dataListener)

        if (!GitHubTokenManager.hasToken()) {
            showStatus(WorkflowBundle.message("status.noToken"))
        }
    }

    fun refresh() {
        progressBar.isVisible = true
        Thread({ service.refreshAll() }, "gh-workflows-refresh").start()
    }

    fun getSelectedWorkflow(): Workflow? {
        val path = tree.selectionPath ?: return null
        for (i in path.pathCount - 1 downTo 0) {
            val node = path.getPathComponent(i) as? DefaultMutableTreeNode ?: continue
            val userObj = node.userObject
            if (userObj is WorkflowTreeNode.WorkflowNode) return userObj.workflow
        }
        return null
    }

    fun getSelectedRun(): WorkflowRun? {
        val node = tree.lastSelectedPathComponent as? DefaultMutableTreeNode ?: return null
        return (node.userObject as? WorkflowTreeNode.RunNode)?.run
    }

    private fun updateTree() {
        // 로딩 중: progress bar만 표시, 기존 트리 유지
        if (service.isLoading) {
            progressBar.isVisible = true
            statusLabel.isVisible = false
            return
        }

        progressBar.isVisible = false

        // 에러: 기존 트리 유지, 상태 메시지만 갱신
        service.lastError?.let { error ->
            showStatus(WorkflowBundle.message("status.error", error))
            return
        }

        // 데이터 없음
        if (service.workflows.isEmpty()) {
            rootNode.removeAllChildren()
            treeModel.reload()
            showStatus(WorkflowBundle.message("status.noWorkflows"))
            return
        }

        // 성공: 확장/선택 상태 저장 → 트리 갱신 → 복원
        val expandedIds = saveExpandedState()
        val selection = saveSelection()

        statusLabel.isVisible = false

        val repo = service.getRepo()
        repoLabel.text = if (repo != null) " ${repo.fullName}" else ""
        repoLabel.isVisible = repo != null

        rootNode.removeAllChildren()

        for (workflow in service.workflows) {
            val workflowNode = DefaultMutableTreeNode(WorkflowTreeNode.WorkflowNode(workflow))
            val runs = service.workflowRuns[workflow.id] ?: emptyList()
            if (runs.isEmpty()) {
                workflowNode.add(DefaultMutableTreeNode(WorkflowBundle.message("status.noRuns")))
            } else {
                for (run in runs) {
                    val runNode = DefaultMutableTreeNode(WorkflowTreeNode.RunNode(run))
                    if (run.conclusion == "failure") {
                        val jobs = service.workflowRunJobs[run.id] ?: emptyList()
                        for (job in jobs) {
                            runNode.add(DefaultMutableTreeNode(WorkflowTreeNode.JobNode(job)))
                        }
                    }
                    workflowNode.add(runNode)
                }
            }
            rootNode.add(workflowNode)
        }

        treeModel.reload()
        restoreExpandedState(expandedIds)
        restoreSelection(selection)
    }

    private fun saveSelection(): Pair<Long?, Long?> {
        val path = tree.selectionPath ?: return null to null
        var workflowId: Long? = null
        var runId: Long? = null
        for (i in 0 until path.pathCount) {
            val node = path.getPathComponent(i) as? DefaultMutableTreeNode ?: continue
            when (val obj = node.userObject) {
                is WorkflowTreeNode.WorkflowNode -> workflowId = obj.workflow.id
                is WorkflowTreeNode.RunNode -> runId = obj.run.id
            }
        }
        return workflowId to runId
    }

    private fun restoreSelection(saved: Pair<Long?, Long?>) {
        val (workflowId, runId) = saved
        if (workflowId == null) return

        for (i in 0 until rootNode.childCount) {
            val wfNode = rootNode.getChildAt(i) as? DefaultMutableTreeNode ?: continue
            val wfObj = wfNode.userObject as? WorkflowTreeNode.WorkflowNode ?: continue
            if (wfObj.workflow.id != workflowId) continue

            if (runId != null) {
                for (j in 0 until wfNode.childCount) {
                    val runNode = wfNode.getChildAt(j) as? DefaultMutableTreeNode ?: continue
                    val runObj = runNode.userObject as? WorkflowTreeNode.RunNode ?: continue
                    if (runObj.run.id == runId) {
                        tree.selectionPath = TreePath(runNode.path)
                        return
                    }
                }
            }
            tree.selectionPath = TreePath(wfNode.path)
            return
        }
    }

    private fun saveExpandedState(): Pair<Set<Long>, Set<Long>> {
        val expandedWorkflows = mutableSetOf<Long>()
        val expandedRuns = mutableSetOf<Long>()
        for (i in 0 until rootNode.childCount) {
            val wfNode = rootNode.getChildAt(i) as? DefaultMutableTreeNode ?: continue
            val wfObj = wfNode.userObject as? WorkflowTreeNode.WorkflowNode ?: continue
            if (tree.isExpanded(TreePath(wfNode.path))) {
                expandedWorkflows.add(wfObj.workflow.id)
            }
            for (j in 0 until wfNode.childCount) {
                val runNode = wfNode.getChildAt(j) as? DefaultMutableTreeNode ?: continue
                val runObj = runNode.userObject as? WorkflowTreeNode.RunNode ?: continue
                if (tree.isExpanded(TreePath(runNode.path))) {
                    expandedRuns.add(runObj.run.id)
                }
            }
        }
        return expandedWorkflows to expandedRuns
    }

    private fun restoreExpandedState(state: Pair<Set<Long>, Set<Long>>) {
        val (expandedWorkflows, expandedRuns) = state
        for (i in 0 until rootNode.childCount) {
            val wfNode = rootNode.getChildAt(i) as? DefaultMutableTreeNode ?: continue
            val wfObj = wfNode.userObject as? WorkflowTreeNode.WorkflowNode ?: continue
            if (expandedWorkflows.contains(wfObj.workflow.id)) {
                tree.expandPath(TreePath(wfNode.path))
            }
            for (j in 0 until wfNode.childCount) {
                val runNode = wfNode.getChildAt(j) as? DefaultMutableTreeNode ?: continue
                val runObj = runNode.userObject as? WorkflowTreeNode.RunNode ?: continue
                if (expandedRuns.contains(runObj.run.id)) {
                    tree.expandPath(TreePath(runNode.path))
                }
            }
        }
    }

    private fun showStatus(message: String) {
        statusLabel.text = message
        statusLabel.isVisible = true
    }

    private fun showRunContextMenu(e: MouseEvent, run: WorkflowRun) {
        val popup = JPopupMenu()
        val isCompleted = run.status == "completed"
        val isInProgress = run.status == "in_progress" || run.status == "queued" || run.status == "waiting"
        val hasFailed = run.conclusion == "failure"

        popup.add(JMenuItem(WorkflowBundle.message("action.rerun")).apply {
            isEnabled = isCompleted
            addActionListener { executeRunAction { service.rerunWorkflowRun(run.id) } }
        })
        popup.add(JMenuItem(WorkflowBundle.message("action.rerunFailed")).apply {
            isEnabled = isCompleted && hasFailed
            addActionListener { executeRunAction { service.rerunFailedJobs(run.id) } }
        })
        popup.add(JSeparator())
        popup.add(JMenuItem(WorkflowBundle.message("action.cancel")).apply {
            isEnabled = isInProgress
            addActionListener { executeRunAction { service.cancelWorkflowRun(run.id) } }
        })
        popup.add(JSeparator())
        popup.add(JMenuItem(WorkflowBundle.message("action.delete")).apply {
            isEnabled = isCompleted
            addActionListener {
                val confirm = Messages.showYesNoDialog(
                    project,
                    WorkflowBundle.message("action.delete.confirm", run.runNumber),
                    WorkflowBundle.message("action.delete"),
                    Messages.getWarningIcon(),
                )
                if (confirm == Messages.YES) {
                    executeRunAction { service.deleteWorkflowRun(run.id) }
                }
            }
        })
        popup.add(JSeparator())
        popup.add(JMenuItem(WorkflowBundle.message("action.openInBrowser")).apply {
            addActionListener { BrowserUtil.browse(run.htmlUrl) }
        })
        popup.show(tree, e.x, e.y)
    }

    private fun showWorkflowContextMenu(e: MouseEvent, workflow: Workflow) {
        val popup = JPopupMenu()
        popup.add(JMenuItem(WorkflowBundle.message("action.openInBrowser")).apply {
            addActionListener { BrowserUtil.browse(workflow.htmlUrl) }
        })
        popup.show(tree, e.x, e.y)
    }

    private fun showJobContextMenu(e: MouseEvent, job: Job) {
        val popup = JPopupMenu()
        popup.add(JMenuItem(WorkflowBundle.message("action.openInBrowser")).apply {
            addActionListener { BrowserUtil.browse(job.htmlUrl) }
        })
        popup.show(tree, e.x, e.y)
    }

    private fun executeRunAction(action: () -> Result<Unit>) {
        Thread({
            action()
                .onSuccess {
                    ApplicationManager.getApplication().invokeLater {
                        NotificationGroupManager.getInstance()
                            .getNotificationGroup("GhWorkflows.Notification")
                            .createNotification(
                                WorkflowBundle.message("notification.action.success"),
                                NotificationType.INFORMATION,
                            )
                            .notify(project)
                        refresh()
                    }
                }
                .onFailure { ex ->
                    ApplicationManager.getApplication().invokeLater {
                        NotificationGroupManager.getInstance()
                            .getNotificationGroup("GhWorkflows.Notification")
                            .createNotification(
                                WorkflowBundle.message("notification.action.failure", ex.message ?: "Unknown error"),
                                NotificationType.ERROR,
                            )
                            .notify(project)
                    }
                }
        }, "gh-workflows-action").start()
    }

    override fun dispose() {
        service.removeListener(dataListener)
    }
}
